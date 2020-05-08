 package org.analogweb.core;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 
 import org.analogweb.InvocationArguments;
 import org.analogweb.InvocationMetadata;
 import org.analogweb.RequestContext;
 import org.analogweb.RequestValueResolver;
 import org.analogweb.RequestValueResolvers;
 import org.analogweb.TypeMapperContext;
 import org.analogweb.annotation.Bean;
 import org.analogweb.annotation.Resolver;
 import org.analogweb.util.AnnotationUtils;
import org.analogweb.util.ArrayUtils;
 import org.analogweb.util.ReflectionUtils;
 import org.analogweb.util.StringUtils;
 
 /**
  * @author snowgoose
  */
 public class BeanAttributeArgumentPreparator extends AbstractApplicationProcessor {
 
     @Override
     public Object prepareInvoke(Method method, InvocationArguments args,
             InvocationMetadata metadata, RequestContext context, TypeMapperContext converters,
             RequestValueResolvers resolvers) {
         Annotation[][] parameterAnnotations = method.getParameterAnnotations();
         Class<?>[] argTypes = metadata.getArgumentTypes();
        if(method == null || ArrayUtils.isEmpty(argTypes) || ArrayUtils.isEmpty(parameterAnnotations)){
            return NO_INTERRUPTION;
        }
         for (int index = 0, limit = argTypes.length; index < limit; index++) {
             Bean beanAnnotation = AnnotationUtils.findAnnotation(Bean.class,
                     parameterAnnotations[index]);
             if (beanAnnotation != null) {
                 Object beanInstance = instanticate(
                         argTypes[index],
                         AnnotationUtils.findAnnotation(Resolver.class, parameterAnnotations[index]),
                         metadata, context, resolvers);
                 if (beanInstance != null) {
                     for (Field field : argTypes[index].getDeclaredFields()) {
                         Object convertedValue = AnnotatedArguments.resolveArguent(field.getName(),
                                 field.getAnnotations(), field.getType(), context, metadata,
                                 converters, resolvers);
                         if (convertedValue != null) {
                             ReflectionUtils.writeValueToField(field, beanInstance, convertedValue);
                         }
                     }
                     args.putInvocationArgument(index, beanInstance);
                 }
             }
         }
         return NO_INTERRUPTION;
     }
 
     private Object instanticate(Class<?> clazz, Resolver resolverAnn, InvocationMetadata metadata,
             RequestContext context, RequestValueResolvers resolvers) {
         if (resolverAnn != null) {
             Class<? extends RequestValueResolver> resolverClass = resolverAnn.value();
             RequestValueResolver resolver = resolvers.findRequestValueResolver(resolverClass);
             if (resolver != null) {
                 return resolver.resolveValue(context, metadata, StringUtils.EMPTY, clazz);
             }
         }
         return ReflectionUtils.getInstanceQuietly(clazz);
     }
 }
