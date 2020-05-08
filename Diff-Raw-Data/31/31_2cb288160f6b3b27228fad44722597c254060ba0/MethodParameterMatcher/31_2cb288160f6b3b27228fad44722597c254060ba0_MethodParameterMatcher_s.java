 package org.polyforms.parameter.support;
 
 import java.lang.annotation.Annotation;
 import java.util.Map;
 import java.util.WeakHashMap;
 
 import org.polyforms.parameter.ArgumentProvider;
 import org.polyforms.parameter.Parameters;
 import org.polyforms.parameter.provider.ArgumentAt;
 
 /**
  * Parameter Matcher used to match parameters from two methods.
  * 
  * @author Kuisong Tong
  * @since 1.0
  */
 public class MethodParameterMatcher extends AbstractParameterMatcher<MethodParameter, MethodParameter> {
     private final Map<ParametersPair, ArgumentProvider[]> argumentProvidersCache = new WeakHashMap<ParametersPair, ArgumentProvider[]>();
     private final ArgumentProviderBuilder argumentProviderBuilder = new ArgumentProviderBuilder();
 
     @Override
     public ArgumentProvider[] match(final Parameters<MethodParameter> sourceParameters,
             final Parameters<MethodParameter> targetParameters) {
         final ParametersPair parametersPair = new ParametersPair(sourceParameters, targetParameters);
        if (!argumentProvidersCache.containsKey(parametersPair)) {
            argumentProvidersCache.put(parametersPair, super.match(sourceParameters, targetParameters));
         }
        return argumentProvidersCache.get(parametersPair);
     }
 
     @Override
     protected ArgumentProvider getArgumentProvider(final SourceParameters sourceParameters,
             final MethodParameter parameter) {
         final Annotation annotation = parameter.getAnnotation();
         if (annotation != null) {
             return argumentProviderBuilder.fromAnnotation(annotation);
         }
 
         return new ArgumentAt(sourceParameters.match(parameter).getIndex());
     }
 
     protected static class ParametersPair {
         private final Parameters<?> sourceParameters;
         private final Parameters<?> targetParameters;
 
         protected ParametersPair(final Parameters<?> sourceParameters, final Parameters<?> targetParameters) {
             this.sourceParameters = sourceParameters;
             this.targetParameters = targetParameters;
         }
 
         @Override
         public int hashCode() {
             final int prime = 31;
             int result = 1;
             result = prime * result + sourceParameters.hashCode();
             result = prime * result + targetParameters.hashCode();
             return result;
         }
 
         @Override
         public boolean equals(final Object obj) {
             if (this == obj) {
                 return true;
             }
 
             if (!(obj instanceof ParametersPair)) {
                 return false;
             }
 
             final ParametersPair other = (ParametersPair) obj;
             return sourceParameters.equals(other.sourceParameters) && targetParameters.equals(other.targetParameters);
         }
     }
 }
