 package com.google.code.joliratools;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Type;
 
 import javax.inject.Inject;
 
 import org.apache.wicket.Page;
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.WicketRuntimeException;
 
 import com.google.inject.Injector;
 import com.google.inject.Key;
 import com.google.inject.Provider;
 
 final class PageConstructor {
     static PageConstructor get(final Injector injector, final Constructor<Page> constructor) {
         final Annotation[][] paramAnnotations = constructor.getParameterAnnotations();
         int paramCount = 0;
         boolean isParametersOnly = false;
         final Type[] genericParamsTypes = constructor.getGenericParameterTypes();
         final Class<?>[] paramTypes = constructor.getParameterTypes();
         final Parameter[] params = new Parameter[genericParamsTypes.length];
         final Provider<?>[] providers = new Provider<?>[genericParamsTypes.length];
         final boolean injectAnnotationPresent = isAnnotationPresent(constructor);
 
         for (int idx = 0; idx < genericParamsTypes.length; idx++) {
             final Annotation[] annos = paramAnnotations[idx];
             final Parameter parameter = getParameterAnnotation(annos);
             final boolean isPageParameters = PageParameters.class.isAssignableFrom(paramTypes[idx]);
 
             if (parameter != null || isPageParameters) {
                 paramCount++;
                 params[idx] = parameter;
                 isParametersOnly = paramCount == 1 && isPageParameters && !injectAnnotationPresent;
             } else {
                 if (!injectAnnotationPresent) {
                     return null;
                 }
 
                 final Annotation anno = getNonParamAnnotation(annos);
                 final Type type = genericParamsTypes[idx];
                 final Key<?> key = anno != null ? Key.get(type, anno) : Key.get(type);
 
                 providers[idx] = injector.getProvider(key);
             }
         }
 
         final Guicier gpp = injector.getInstance(Guicier.class);
 
         return new PageConstructor(gpp, paramCount == 0, injectAnnotationPresent, params, providers, isParametersOnly,
                 constructor, injector, paramTypes);
     }
 
     private static Annotation getNonParamAnnotation(final Annotation[] annos) {
         for (final Annotation anno : annos) {
             if (!(anno instanceof Parameter)) {
                 return anno;
             }
         }
 
         return null;
     }
 
     private static Parameter getParameterAnnotation(final Annotation[] annos) {
         for (final Annotation anno : annos) {
             if (anno instanceof Parameter) {
                 return (Parameter) anno;
             }
         }
 
         return null;
     }
 
     private static boolean isAnnotationPresent(final Constructor<Page> constructor) {
         return constructor.isAnnotationPresent(Inject.class)
                 || constructor.isAnnotationPresent(com.google.inject.Inject.class);
     }
 
     private final boolean isDefault;
 
     private final boolean isInjected;
 
     private final Parameter[] params;
 
     private final Provider<?>[] providers;
 
     private final boolean isParametersOnly;
 
     private final Constructor<Page> constructor;
 
     private final Injector injector;
 
     private final Class<?>[] parameterTypes;
 
     private final Guicier gpp;
 
     private PageConstructor(final Guicier gpp, final boolean isDefault, final boolean isInjected,
             final Parameter[] params, final Provider<?>[] providers, final boolean isParametersOnly,
             final Constructor<Page> constructor, final Injector injector, final Class<?>[] parameterTypes) {
         this.gpp = gpp;
         this.params = params;
         this.providers = providers;
         this.isParametersOnly = isParametersOnly;
         this.isInjected = isInjected;
         this.isDefault = isDefault;
         this.constructor = constructor;
         this.injector = injector;
         this.parameterTypes = parameterTypes;
     }
 
    private Throwable getCause(final Throwable e) {
        final Throwable cause = e.getCause();

        if (cause == null) {
            return e;
        }

        return getCause(cause);
    }

     int getMatchCount(final PageParameters parameters) {
         int count = 0;
 
         for (final Parameter param : params) {
             if (param == null) {
                 continue;
             }
 
             final String key = param.value();
 
             if (parameters == null || !parameters.containsKey(key)) {
                 if (!param.optional()) {
                     return -1;
                 }
 
                 continue;
             }
 
             count++;
         }
 
         return count;
     }
 
     boolean isDefault() {
         return isDefault;
     }
 
     boolean isInjectAnnotated() {
         return isInjected;
     }
 
     public boolean isPageParametersOnly() {
         return isParametersOnly;
     }
 
     private Page newInstance(final Object[] args) throws Error {
         constructor.setAccessible(true);
 
         try {
             return constructor.newInstance(args);
         } catch (final InstantiationException e) {
            throw new WicketRuntimeException(getCause(e));
         } catch (final IllegalAccessException e) {
            throw new WicketRuntimeException(getCause(e));
         } catch (final InvocationTargetException e) {
            throw new WicketRuntimeException(getCause(e));
         }
     }
 
     public Page newInstance(final PageParameters parameters) {
         if (isParametersOnly) {
             return newInstance(new Object[] { parameters });
         }
 
         final Object[] args = new Object[params.length];
         final PageParameters cleansed = new PageParameters();
 
         for (int idx = 0; idx < params.length; idx++) {
             final Provider<?> provider = providers[idx];
 
             if (provider != null) {
                 args[idx] = provider.get();
             } else {
                 final Parameter param = params[idx];
                 final Class<?> cls = parameterTypes[idx];
 
                 args[idx] = gpp.get(parameters, param, cls, cleansed, idx == 0);
             }
         }
 
         final Page page = newInstance(args);
 
         injector.injectMembers(page);
 
         return page;
     }
 }
