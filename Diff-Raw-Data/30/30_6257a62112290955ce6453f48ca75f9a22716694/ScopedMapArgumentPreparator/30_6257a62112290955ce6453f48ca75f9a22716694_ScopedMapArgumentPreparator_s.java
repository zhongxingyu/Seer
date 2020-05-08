 package org.analogweb.core;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.analogweb.AttributesHandler;
 import org.analogweb.InvocationArguments;
 import org.analogweb.InvocationMetadata;
 import org.analogweb.RequestContext;
 import org.analogweb.RequestValueResolvers;
 import org.analogweb.TypeMapperContext;
 import org.analogweb.annotation.Attributes;
 import org.analogweb.util.AnnotationUtils;
 
 /**
  * {@link Attributes}注釈が付与された{@link org.analogweb.annotation.Route} メソッドの引数に対して、
  * {@link Map}インスタンスを生成し、スコープを操作するためのインターフェースとして提供します。<br/>
  * スコープは{@link Attributes#value()}属性に指定された値が適用されます。 適用されるメソッドの引数は{@link String}を キーとする
  * {@link Map}でなければなりません。{@link Attributes}注釈を付与している場合でも、この条件を満たさない場合は {@link Map}
  * インスタンスの適用はされません。
  * @author snowgoose
  */
 public class ScopedMapArgumentPreparator extends AbstractApplicationProcessor {
 
     @Override
     public Object prepareInvoke(Method method, InvocationArguments args,
             InvocationMetadata metadata, RequestContext context, TypeMapperContext converters,
             RequestValueResolvers handlers) {
         Annotation[][] argumentAnnotations = method.getParameterAnnotations();
         Class<?>[] argTypes = metadata.getArgumentTypes();
         for (int index = 0, limit = argTypes.length; index < limit; index++) {
             Attributes viewAttributes = AnnotationUtils
                     .findAnnotation(Attributes.class, argumentAnnotations[index]);
             if (viewAttributes != null
                     && argTypes[index].getCanonicalName().equals(Map.class.getCanonicalName())) {
                 args.putInvocationArgument(index,
                         new ContextExtractor<Object>(viewAttributes.value()));
             }
         }
         return NO_INTERRUPTION;
     }
 
     @Override
     public void postInvoke(Object invocationResult, InvocationArguments args,
             InvocationMetadata metadata, RequestContext context, RequestValueResolvers handlers) {
         for (Object arg : args.asList()) {
             if (arg instanceof ContextExtractor) {
                 ContextExtractor<?> scopedAttributes = (ContextExtractor<?>) arg;
                 scopedAttributes.extract(context, handlers);
             }
         }
     }
 
     static final class ContextExtractor<V> extends HashMap<String, V> {
 
         private static final long serialVersionUID = -944143676425859153L;
        private Set<String> removedKeys = new HashSet<String>();
        private Class<? extends AttributesHandler> handlerClass;
 
         ContextExtractor(Class<? extends AttributesHandler> handlerClass) {
             super();
             this.handlerClass = handlerClass;
         }
 
         Class<? extends AttributesHandler> getHandlerClass() {
             return this.handlerClass;
         }
 
         @Override
         public V remove(Object key) {
             V removed = super.remove(key);
             removedKeys.add((String) key);
             return removed;
         }
 
         void extract(RequestContext context, RequestValueResolvers handlers) {
             AttributesHandler handler = handlers.findAttributesHandler(getHandlerClass());
             if (handler != null) {
                 for (Entry<String, ?> entry : entrySet()) {
                     handler.putAttributeValue(context, entry.getKey(), entry.getValue());
                 }
                 for (String removedKey : removedKeys) {
                     handler.removeAttribute(context, removedKey);
                 }
             }
         }
 
     }
 
 }
