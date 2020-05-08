 package edu.uw.zookeeper.safari.peer.protocol;
 
 import java.io.IOException;
 import java.util.Properties;
 
 import com.fasterxml.jackson.core.JsonGenerationException;
 import com.fasterxml.jackson.core.JsonGenerator;
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.core.Version;
 import com.fasterxml.jackson.databind.DeserializationContext;
 import com.fasterxml.jackson.databind.JsonDeserializer;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.SerializerProvider;
 import com.fasterxml.jackson.databind.deser.std.FromStringDeserializer;
 import com.fasterxml.jackson.databind.module.SimpleDeserializers;
 import com.fasterxml.jackson.databind.module.SimpleModule;
 import com.fasterxml.jackson.databind.module.SimpleSerializers;
 import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
 import com.google.common.collect.ImmutableMap;
 
 import edu.uw.zookeeper.EnsembleRoleView;
 import edu.uw.zookeeper.EnsembleView;
 import edu.uw.zookeeper.ServerAddressView;
 import edu.uw.zookeeper.ServerInetAddressView;
 import edu.uw.zookeeper.ServerView;
 import edu.uw.zookeeper.common.Reference;
 import edu.uw.zookeeper.common.Singleton;
 import edu.uw.zookeeper.data.Serializers;
 import edu.uw.zookeeper.data.Serializes;
 import edu.uw.zookeeper.data.ZNodeLabel;
 import edu.uw.zookeeper.safari.Identifier;
 
 public class JacksonModule extends SimpleModule {
 
     public static JacksonModule getModule() {
         return Holder.INSTANCE.get();
     }
     
     public static ObjectMapper getMapper() {
         return ObjectMapperHolder.INSTANCE.get();
     }
     
     public static JacksonSerializer getSerializer() {
         return JacksonSerializer.Holder.INSTANCE.get();
     }
 
     public static class ToStringRegistrySerializer extends StdScalarSerializer<Object> {
 
         public static ToStringRegistrySerializer getInstance() {
             return Holder.INSTANCE.get();
         }
         
         public static enum Holder implements Singleton<ToStringRegistrySerializer> {
             INSTANCE(new ToStringRegistrySerializer());
             
             private final ToStringRegistrySerializer instance;
             
             private Holder(ToStringRegistrySerializer instance) {
                 this.instance = instance;
             }
 
             @Override
             public ToStringRegistrySerializer get() {
                 return instance;
             }
         }
         
         public ToStringRegistrySerializer() { super(Object.class); }
 
         @Override
         public void serialize(
                 Object value, 
                 JsonGenerator jgen, 
                 SerializerProvider provider) throws IOException,
                 JsonGenerationException {
             String stringOf = Serializers.ToString.TO_STRING.apply(value);
             jgen.writeString(stringOf);
         }
         
     }
     
     public static class FromStringRegistryDeserializer extends FromStringDeserializer<Object> {
 
         private static final long serialVersionUID = -8157879499774544500L;
         
         public FromStringRegistryDeserializer(Class<?> vc) {
             super(vc);
         }
 
         @Override
         protected Object _deserialize(String value, DeserializationContext ctxt)
                 throws IOException, JsonProcessingException {
             return Serializers.getInstance().toClass(value, getValueClass());
         }
     }
 
     public static enum Holder implements Singleton<JacksonModule> {
         INSTANCE(new JacksonModule());
         
         private final JacksonModule instance;
         
         private Holder(JacksonModule instance) {
             this.instance = instance;
         }
         
         @Override
         public JacksonModule get() {
             return instance;
         }
     }
     
     public static enum ObjectMapperHolder implements Singleton<ObjectMapper> {
         INSTANCE(new ObjectMapper());
 
         private final ObjectMapper instance;
         
         private ObjectMapperHolder(ObjectMapper instance) {
             this.instance = instance;
             instance.registerModule(Holder.INSTANCE.get());
         }
         
         @Override
         public ObjectMapper get() {
             return instance;
         }
     }
 
     private static final long serialVersionUID = -1013929286111475121L;
     
     public static final Properties MAVEN_PROPS = new Properties();
     public static final String DEFAULT_VERSION = "0.0.0-SNAPSHOT";
     static {
         try {
             MAVEN_PROPS.load(JacksonModule.class.getClassLoader().getResourceAsStream("META-INF/maven/edu.uw.zookeeper/safari/pom.properties"));
         } catch (Exception e) {
         }
     }
     public static final String MAVEN_VERSION = MAVEN_PROPS.getProperty("version", DEFAULT_VERSION);
     public static final String PROJECT_NAME = "Safari";
     public static final String[] VERSION_FIELDS = MAVEN_VERSION.split("[.-]");
     public static final Version PROJECT_VERSION = new Version(
             Integer.valueOf(VERSION_FIELDS[0]), 
             Integer.valueOf(VERSION_FIELDS[1]), 
             Integer.valueOf(VERSION_FIELDS[2]),
            VERSION_FIELDS.length > 3 ? VERSION_FIELDS[3] : null, 
             "edu.uw.zookeeper", "safari");
     
     protected final SimpleDeserializers deserializers;
     protected final SimpleSerializers serializers;
     
     public JacksonModule() {
         super(PROJECT_NAME, PROJECT_VERSION);
         this.serializers = new SimpleSerializers();
         this.deserializers = new SimpleDeserializers();
         
         Class<?>[] registryClasses = {
                 ZNodeLabel.class, 
                 ZNodeLabel.Component.class,
                 ZNodeLabel.Path.class,
                 ServerView.Address.class,
                 ServerInetAddressView.class,
                 EnsembleView.class,
                 EnsembleRoleView.class,
                 Identifier.class };
         ImmutableMap.Builder<Class<?>, JsonDeserializer<?>> fromStrings = ImmutableMap.builder();
         for (Class<?> cls: registryClasses) {
             serializers.addSerializer(cls,
                 ToStringRegistrySerializer.getInstance());
             fromStrings.put(cls,
                 new FromStringRegistryDeserializer(cls));
         }
         deserializers.addDeserializers(fromStrings.build());
         
         Serializers.getInstance().add(ServerAddressView.class);
     }
     
     @Override
     public void setupModule(SetupContext context) {
         context.addSerializers(serializers);
         context.addDeserializers(deserializers);
     }
     
     public static class JacksonSerializer implements Reference<ObjectMapper>, Serializers.ByteCodec<Object> {
         
         public static enum Holder implements Singleton<JacksonSerializer> {
             INSTANCE(newInstance());
 
             private final JacksonSerializer instance;
             
             private Holder(JacksonSerializer instance) {
                 this.instance = instance;
             }
             
             @Override
             public JacksonSerializer get() {
                 return instance;
             }
         }
 
         public static JacksonSerializer newInstance() {
             return newInstance(ObjectMapperHolder.INSTANCE.get());
         }
         
         public static JacksonSerializer newInstance(ObjectMapper mapper) {
             return new JacksonSerializer(mapper);
         }
         
         protected final ObjectMapper mapper;
 
         protected JacksonSerializer(ObjectMapper mapper) {
             this.mapper = mapper;
         }
 
         @Override
         @Serializes(from=byte[].class, to=Object.class)
         public <T> T fromBytes(byte[] input, Class<T> type) throws IOException {
             return mapper.readValue(input, type);
         }
         
         @Override
         @Serializes(from=Object.class, to=byte[].class)
         public byte[] toBytes(Object input) throws JsonProcessingException {
             return mapper.writeValueAsBytes(input);
         }
 
         @Serializes(from=String.class, to=Object.class)
         public <T> T fromString(String input, Class<T> type) throws IOException {
             return mapper.readValue(input, type);
         }
         
         @Serializes(from=Object.class, to=String.class)
         public String toString(Object input) throws JsonProcessingException {
             return mapper.writeValueAsString(input);
         }
 
         @Override
         public ObjectMapper get() {
             return mapper;
         }
     }
 }
