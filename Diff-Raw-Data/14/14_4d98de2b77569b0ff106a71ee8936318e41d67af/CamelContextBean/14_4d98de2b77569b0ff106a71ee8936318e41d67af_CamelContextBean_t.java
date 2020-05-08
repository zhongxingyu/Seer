 package com.github.rmannibucau.cdi.camel;
 
 import org.apache.camel.CamelContext;
 import org.apache.camel.RoutesBuilder;
 import org.apache.camel.builder.RouteBuilder;
 import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 import javax.enterprise.context.ApplicationScoped;
 import javax.enterprise.context.spi.CreationalContext;
 import javax.enterprise.inject.spi.Bean;
 import javax.enterprise.inject.spi.BeanManager;
 import javax.enterprise.inject.spi.InjectionPoint;
 import javax.enterprise.util.AnnotationLiteral;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Type;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 public class CamelContextBean implements Bean<CamelContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CamelContextBean.class);

     private final BeanManager beanManager;
     private final String name;
     private final List<Class<?>> builderClasses;
 
     public CamelContextBean(final BeanManager bm, final String id, final List<Class<?>> builders) {
         this.beanManager = bm;
         this.builderClasses = builders;
         this.name =  id;
     }
 
     @Override
     public Set<Type> getTypes() {
         final Set<Type> types = new HashSet<Type>();
         types.add(CamelContext.class);
         return types;
     }
 
     @Override
     public Set<Annotation> getQualifiers() {
         final Set<Annotation> types = new HashSet<Annotation>();
         types.add(new CamelContextIdImpl());
         return types;
     }
 
     @Override
     public Class<? extends Annotation> getScope() {
         return ApplicationScoped.class;
     }
 
     @Override
     public String getName() {
         return name;
     }
 
     @Override
     public boolean isNullable() {
         return false;
     }
 
     @Override
     public Set<InjectionPoint> getInjectionPoints() {
         return Collections.emptySet();
     }
 
     @Override
     public Class<?> getBeanClass() {
         return CamelContext.class;
     }
 
     @Override
     public Set<Class<? extends Annotation>> getStereotypes() {
         return Collections.emptySet();
     }
 
     @Override
     public boolean isAlternative() {
         return false;
     }
 
     @Override
     public CamelContext create(final CreationalContext<CamelContext> camelContextCreationalContext) {
         final DefaultCamelContext context = new DefaultCamelContext();
         for (Class<?> clazz : builderClasses) {
             final Set<Bean<?>> beans = beanManager.getBeans(clazz);
             final Bean<?> bean = beanManager.resolve(beans);
             final Object instance = beanManager.getReference(bean, RouteBuilder.class, camelContextCreationalContext);
             try {
                 context.addRoutes(RoutesBuilder.class.cast(instance));
             } catch (Exception e) {
                LOGGER.error("can't get route builder " + clazz.getName());
             }
         }
 
         context.setName(name);
 
         try {
             context.start();
         } catch (Exception e) {
            LOGGER.error("can't start route '" + name + "'");
         }
 
         return context;
     }
 
     @Override
     public void destroy(final CamelContext camelContext, final CreationalContext<CamelContext> camelContextCreationalContext) {
         try {
             camelContext.stop();
         } catch (Exception e) {
            LOGGER.error("can't stop route '" + name + "'");
         } finally {
             camelContextCreationalContext.release();
         }
     }
 
     private class CamelContextIdImpl extends AnnotationLiteral<CamelContextId> implements CamelContextId {
         @Override
         public String value() {
             return name;
         }
     }
 }
