 package org.analogweb.core;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 
 import org.analogweb.InvocationArguments;
 import org.analogweb.InvocationMetadata;
 import org.analogweb.RequestContext;
 import org.analogweb.RequestValueResolvers;
 import org.analogweb.TypeMapperContext;
 import org.analogweb.annotation.As;
 
 /**
  * エントリポイントメソッドの引数から{@link As}が付与されている引数を検知し、 {@link As}
  * に定義されたスコープと属性名に応じた値をエントリポイントメソッド の引数の値として設定する
  * {@link AbstractApplicationProcessor}の実装です。
  * @author snowgoose
  */
 public class BindAttributeArgumentPreparator extends AbstractApplicationProcessor {
 
     @Override
     public Object prepareInvoke(Method method, InvocationArguments args,
             InvocationMetadata metadata, RequestContext context, TypeMapperContext converters,
             RequestValueResolvers resolvers) {
         Annotation[][] parameterAnnotations = method.getParameterAnnotations();
         Class<?>[] argTypes = metadata.getArgumentTypes();
         for (int index = 0, limit = argTypes.length; index < limit; index++) {
             Object convertedValue = AnnotatedArguments.resolveArguent(parameterAnnotations[index],
                     argTypes[index], context, metadata, converters, resolvers);
             if (convertedValue != null) {
                 args.putInvocationArgument(index, convertedValue);
             }
         }
         return NO_INTERRUPTION;
     }
 }
