 package pl.com.it_crowd.arquillian.mock_contexts.container;
 
 import javax.enterprise.context.Dependent;
 import javax.enterprise.context.spi.Context;
 import javax.enterprise.context.spi.CreationalContext;
 import javax.enterprise.event.Observes;
 import javax.enterprise.inject.Any;
 import javax.enterprise.inject.Default;
 import javax.enterprise.inject.spi.AfterBeanDiscovery;
 import javax.enterprise.inject.spi.Bean;
 import javax.enterprise.inject.spi.BeanManager;
 import javax.enterprise.inject.spi.BeforeBeanDiscovery;
 import javax.enterprise.inject.spi.Extension;
 import javax.enterprise.inject.spi.InjectionPoint;
 import javax.enterprise.inject.spi.InjectionTarget;
 import javax.enterprise.inject.spi.ProcessAnnotatedType;
 import javax.enterprise.util.AnnotationLiteral;
 import javax.faces.bean.ViewScoped;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Type;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 public class MockContextsCDIExtension implements Extension {
 
     @SuppressWarnings("UnusedDeclaration")
     private void addScope(@Observes final BeforeBeanDiscovery event)
     {
        event.addScope(ViewScoped.class, true, true);
     }
 
     private <T extends Context> Bean<T> getContextBean(final T viewContext, BeanManager beanManager)
     {
         final Class<? extends Context> contextClass = viewContext.getClass();
         @SuppressWarnings("unchecked") final InjectionTarget<T> injectionTarget = (InjectionTarget<T>) beanManager.createInjectionTarget(
             beanManager.createAnnotatedType(contextClass));
         final HashSet<Type> types = new HashSet<Type>();
         types.add(contextClass);
         final HashSet<Annotation> qualifiers = new HashSet<Annotation>();
         qualifiers.add(new AnnotationLiteral<Default>() {
         });
         qualifiers.add(new AnnotationLiteral<Any>() {
         });
         return new Bean<T>() {
             public Set<Type> getTypes()
             {
                 return types;
             }
 
             public Set<Annotation> getQualifiers()
             {
                 return qualifiers;
             }
 
             public Class<? extends Annotation> getScope()
             {
                 return Dependent.class;
             }
 
             public String getName()
             {
                 return null;
             }
 
             public Set<Class<? extends Annotation>> getStereotypes()
             {
                 return Collections.emptySet();
             }
 
             public Class<?> getBeanClass()
             {
                 return contextClass;
             }
 
             public boolean isAlternative()
             {
                 return false;
             }
 
             public boolean isNullable()
             {
                 return false;
             }
 
             public Set<InjectionPoint> getInjectionPoints()
             {
                 return injectionTarget.getInjectionPoints();
             }
 
             public T create(CreationalContext<T> context)
             {
                 return viewContext;
             }
 
             public void destroy(T instance, CreationalContext<T> context)
             {
                 // Call @PreDestroy
                 injectionTarget.preDestroy(instance);
                 // Have CDI release the bean
                 injectionTarget.dispose(instance);
                 // Release any dependent objects
                 context.release();
             }
         };
     }
 
     @SuppressWarnings("UnusedDeclaration")
     private void processAnnotatedType(@Observes final ProcessAnnotatedType<AbstractMockContext> event)
     {
         event.veto();
     }
 
     @SuppressWarnings("UnusedDeclaration")
     private void registerContext(@Observes final AfterBeanDiscovery event, BeanManager beanManager)
     {
         final MockViewScopedContext mockViewScopedContext = new MockViewScopedContext();
         final Bean<MockViewScopedContext> mockViewScopedContextBean = getContextBean(mockViewScopedContext, beanManager);
         event.addBean(mockViewScopedContextBean);
         event.addContext(mockViewScopedContext);
 
         final MockConversationScopedContext mockConversationScopedContext = new MockConversationScopedContext();
         final Bean<MockConversationScopedContext> conversationScopedContextBean = getContextBean(mockConversationScopedContext, beanManager);
         event.addBean(conversationScopedContextBean);
         event.addContext(mockConversationScopedContext);
     }
 }
