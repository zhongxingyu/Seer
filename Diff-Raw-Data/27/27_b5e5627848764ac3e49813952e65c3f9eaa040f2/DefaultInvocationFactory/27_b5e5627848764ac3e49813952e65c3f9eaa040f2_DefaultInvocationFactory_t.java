 package org.analogweb.core;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Constructor;
import java.util.ArrayList;
 import java.util.List;
 
 import org.analogweb.AttributesHandlers;
 import org.analogweb.ContainerAdaptor;
 import org.analogweb.Invocation;
 import org.analogweb.InvocationFactory;
 import org.analogweb.InvocationMetadata;
 import org.analogweb.InvocationProcessor;
 import org.analogweb.RequestContext;
 import org.analogweb.ResponseContext;
 import org.analogweb.TypeMapperContext;
 import org.analogweb.exception.UnresolvableInvocationException;
 import org.analogweb.util.ArrayUtils;
 import org.analogweb.util.ReflectionUtils;
 import org.analogweb.util.logging.Log;
 import org.analogweb.util.logging.Logs;
 import org.analogweb.util.logging.Markers;
 
 /**
  * 既定の{@link InvocationFactory}の実装です。
  * @author snowgoose
  */
 public class DefaultInvocationFactory implements InvocationFactory {
 
     private static final Log log = Logs.getLog(DefaultInvocationFactory.class);
     private AnnotatedInvocationParameterValueResolver resolver;
 
     public DefaultInvocationFactory() {
         this(new ScopedParameterValueResolver());
     }
 
     public DefaultInvocationFactory(AnnotatedInvocationParameterValueResolver resolver) {
         this.resolver = resolver;
     }
 
     public Invocation createInvocation(ContainerAdaptor instanceProvider,
             InvocationMetadata metadata, RequestContext context, ResponseContext responseContext,
             TypeMapperContext converters, List<InvocationProcessor> processors,
             AttributesHandlers handlers) {
         Object invocationInstance = resolveInvocationInstance(instanceProvider, metadata, context);
         if (invocationInstance == null) {
             invocationInstance = resolveByDefault(metadata, context, responseContext, converters,
                     handlers);
             if (invocationInstance == null) {
                 throw new UnresolvableInvocationException(metadata);
             }
         }
         log.log(Markers.LIFECYCLE, "DL000001", invocationInstance, instanceProvider);
         return new DefaultInvocation(invocationInstance, metadata, context, responseContext,
                 converters, processors, handlers);
     }
 
     protected Object resolveByDefault(InvocationMetadata metadata, RequestContext context,
             ResponseContext responseContext, TypeMapperContext converters,
             AttributesHandlers handlers) {
         Class<?> invocationClass = metadata.getInvocationClass();
         Constructor<?>[] crs = invocationClass.getConstructors();
         if (ArrayUtils.isEmpty(crs)) {
             return ReflectionUtils.getInstanceQuietly(invocationClass);
         }
         Constructor<?> firstConstructor = crs[0];
         Annotation[][] ans = firstConstructor.getParameterAnnotations();
         Class<?>[] types = firstConstructor.getParameterTypes();
         AnnotatedInvocationParameterValueResolver resolver = getParameterValueResolver();
        List<Object> argValues = new ArrayList<Object>();
         for (int index = 0, limit = types.length; index < limit; index++) {
             Class<?> type = types[index];
             Annotation[] ann = ans[index];
             argValues.add(resolver.resolve(ann, type, context, metadata, converters, handlers));
         }
         return ReflectionUtils.getInstanceQuietly(firstConstructor,
                 argValues.toArray(new Object[argValues.size()]));
     }
 
     protected Object resolveInvocationInstance(ContainerAdaptor instanceProvider,
             InvocationMetadata metadata, RequestContext context)
             throws UnresolvableInvocationException {
         Object invocationInstance = instanceProvider.getInstanceOfType(metadata
                 .getInvocationClass());
         return invocationInstance;
     }
 
     protected AnnotatedInvocationParameterValueResolver getParameterValueResolver() {
         return this.resolver;
     }
 }
