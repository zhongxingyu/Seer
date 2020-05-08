 package edu.uw.zookeeper.clients.trace;
 
 import static com.google.common.base.Preconditions.checkState;
 
 import java.io.File;
 import java.util.concurrent.Executor;
 import java.util.concurrent.ExecutorService;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import com.fasterxml.jackson.core.JsonEncoding;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.ObjectWriter;
 import com.google.common.base.Function;
 import com.typesafe.config.Config;
 import com.typesafe.config.ConfigValueType;
 
 import edu.uw.zookeeper.ZooKeeperApplication;
 import edu.uw.zookeeper.common.Configurable;
 import edu.uw.zookeeper.common.Configuration;
 import edu.uw.zookeeper.common.EventBusPublisher;
 import edu.uw.zookeeper.common.RuntimeModule;
 
 public abstract class Tracing {
 
     public static JsonEncoding ENCODING = JsonEncoding.UTF8;
     
     public static File getTraceOutputFileConfiguration(Configuration configuration) {
         return TraceOutputFileConfiguration.get(configuration);
     }
 
     public static File getTraceInputFileConfiguration(Configuration configuration) {
         return TraceInputFileConfiguration.get(configuration);
     }
     
     public static String getTraceDescription(Configuration configuration) {
         return TraceDescriptionConfiguration.get(configuration);
     }
     
     @Configurable(path="Trace", arg="output", key="OutputFile", value="trace-%d.json", help="Path")
     public static class TraceOutputFileConfiguration implements Function<Configuration, File> {
 
         public static File get(Configuration configuration) {
             return new TraceOutputFileConfiguration().apply(configuration);
         }
 
         @Override
         public File apply(Configuration configuration) {
             Configurable configurable = getClass().getAnnotation(Configurable.class);
             String value =
                     configuration.withConfigurable(configurable)
                         .getConfigOrEmpty(configurable.path())
                             .getString(configurable.key());
             if (value.indexOf('%') != -1) {
                 value = String.format(value, System.currentTimeMillis());
             }
             return new File(value).getAbsoluteFile();
         }   
     }
     
     @Configurable(path="Trace", arg="input", key="InputFile", value="trace.json", help="Path")
     public static class TraceInputFileConfiguration implements Function<Configuration, File> {
 
         public static File get(Configuration configuration) {
             return new TraceInputFileConfiguration().apply(configuration);
         }
 
         @Override
         public File apply(Configuration configuration) {
             Configurable configurable = getClass().getAnnotation(Configurable.class);
             String value =
                     configuration.withConfigurable(configurable)
                         .getConfigOrEmpty(configurable.path())
                             .getString(configurable.key());
             return new File(value).getAbsoluteFile();
         }   
     }
     
     @Configurable(arg="description", path="Trace", key="Description", help="Description", type=ConfigValueType.STRING)
     public static class TraceDescriptionConfiguration implements Function<Configuration, String> {
 
         public static String get(Configuration configuration) {
             return new TraceDescriptionConfiguration().apply(configuration);
         }
 
         @Override
         public String apply(Configuration configuration) {
             Configurable configurable = getClass().getAnnotation(Configurable.class);
             Config config = configuration.withConfigurable(configurable)
                     .getConfigOrEmpty(configurable.path());
             if (config.hasPath(configurable.key())) {
                 return config.getString(configurable.key());
             } else {
                 return "";
             }
         }
     }
     
     public static abstract class TracingBuilder<T, C extends TracingBuilder<T,C>> implements ZooKeeperApplication.RuntimeBuilder<T, C> {
         
         protected final Logger logger = LogManager.getLogger(getClass());
         protected final ObjectMapper mapper;
         protected final RuntimeModule runtime;
         
         protected TracingBuilder(
                 ObjectMapper mapper,
                 RuntimeModule runtime) {
             this.mapper = mapper;
             this.runtime = runtime;
         }
 
         @Override
         public RuntimeModule getRuntimeModule() {
             return runtime;
         }
 
         @Override
         public C setRuntimeModule(RuntimeModule runtime) {
             return newInstance(mapper, runtime);
         }
         
         public ObjectMapper getObjectMapper() {
             return mapper;
         }
         
         public C setObjectMapper(ObjectMapper mapper) {
             return newInstance(mapper, runtime);
         }
 
         @SuppressWarnings("unchecked")
         @Override
         public C setDefaults() {
             checkState(getRuntimeModule() != null);
         
             if (mapper == null) {
                 return setObjectMapper(getDefaultObjectMapper()).setDefaults();
             }
             
             return (C) this;
         }
         
         @Override
         public T build() {
             return setDefaults().doBuild();
         }
         
         protected ObjectMapper getDefaultObjectMapper() {
             return ObjectMapperBuilder.defaults().build();
         }
 
         protected abstract C newInstance(ObjectMapper mapper, RuntimeModule runtime);
         
         protected abstract T doBuild();
     }
     
     public static abstract class TraceWritingBuilder<T, C extends TraceWritingBuilder<T,C>> extends TracingBuilder<T,C> {
 
         protected final TraceWriterBuilder writerBuilder;
         protected final TraceEventPublisherService tracePublisher;
         
         protected TraceWritingBuilder(
                 TraceWriterBuilder writerBuilder,
                 TraceEventPublisherService tracePublisher,
                 ObjectMapper mapper,
                 RuntimeModule runtime) {
             super(mapper, runtime);
             this.writerBuilder = writerBuilder;
             this.tracePublisher = tracePublisher;
         }
 
         public TraceWriterBuilder getTraceWriterBuilder() {
             return writerBuilder;
         }
 
         public C setTraceWriterBuilder(
                 TraceWriterBuilder writerBuilder) {
             return newInstance(writerBuilder, tracePublisher, mapper, runtime);
         }
 
         public TraceEventPublisherService getTracePublisher() {
             return tracePublisher;
         }
 
         public C setTracePublisher(
                 TraceEventPublisherService tracePublisher) {
             return newInstance(writerBuilder, tracePublisher, mapper, runtime);
         }
 
         @Override
         public C setDefaults() {
            if (writerBuilder == null) {
                return setTraceWriterBuilder(getDefaultWriterBuilder()).setDefaults();
             }
            if (tracePublisher == null) {
                return setTracePublisher(getDefaultTracePublisher()).setDefaults();
            }
            return (C) super.setDefaults();
         }
         
         @Override
         protected C newInstance(
                 ObjectMapper mapper,
                 RuntimeModule runtime) {
             return newInstance(writerBuilder, tracePublisher, mapper, runtime);
         }
         
         protected abstract C newInstance(
                 TraceWriterBuilder writerBuilder,
                 TraceEventPublisherService tracePublisher,
                 ObjectMapper mapper,
                 RuntimeModule runtime);
 
         protected TraceHeader getDefaultTraceHeader() {
             return TraceHeader.create(
                     Tracing.getTraceDescription(getRuntimeModule().getConfiguration()), 
                     TraceEventTag.TIMESTAMP_EVENT, 
                     TraceEventTag.PROTOCOL_REQUEST_EVENT, 
                     TraceEventTag.PROTOCOL_RESPONSE_EVENT);
         }
 
         protected TraceWriterBuilder getDefaultWriterBuilder() {
             ObjectWriter writer = mapper.writer();
             File file = Tracing.getTraceOutputFileConfiguration(getRuntimeModule().getConfiguration());
             logger.info("Trace output: {}", file);
             Executor executor = getRuntimeModule().getExecutors().get(ExecutorService.class);
             TraceHeader header = getDefaultTraceHeader();
             return TraceWriterBuilder.defaults()
                     .setHeader(header)
                     .setWriter(writer)
                     .setExecutor(executor)
                     .setFile(file);
         }
         
         protected TraceEventPublisherService getDefaultTracePublisher() {
             return TraceEventPublisherService.newInstance(
                     EventBusPublisher.newInstance(), 
                     writerBuilder.build());
         }
     }
 }
