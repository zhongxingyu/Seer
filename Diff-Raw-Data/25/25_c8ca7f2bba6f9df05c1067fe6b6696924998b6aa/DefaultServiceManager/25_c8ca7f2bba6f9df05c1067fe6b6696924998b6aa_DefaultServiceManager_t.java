 package uk.ac.ebi.mdk.service;
 
 import com.google.common.base.Joiner;
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Multimap;
 import com.google.common.collect.TreeMultimap;
 import net.sf.cglib.proxy.Enhancer;
 import net.sf.cglib.proxy.MethodInterceptor;
 import net.sf.cglib.proxy.MethodProxy;
 import org.apache.log4j.Logger;
 import uk.ac.ebi.mdk.domain.identifier.Identifier;
 import uk.ac.ebi.mdk.service.query.QueryService;
 
 import java.lang.reflect.Method;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Queue;
 import java.util.ServiceLoader;
 import java.util.Set;
 import java.util.TreeSet;
 
 /**
  * @author John May
  */
 public class DefaultServiceManager implements ServiceManager {
 
     private static final Logger LOGGER = Logger.getLogger(DefaultServiceManager.class);
 
     private final ServiceLoader<QueryService> serviceLoader = ServiceLoader.load(QueryService.class);
 
     private final Multimap<ServiceKey, QueryService>                services     = TreeMultimap.create(new ServiceKeyComparator(),
                                                                                                        new ServiceComparator());
     private final Multimap<Class<?>, Class<? extends QueryService>> interfaceMap = HashMultimap.create();
 
     private static class DefaultServiceManagerHolder {
         private static DefaultServiceManager INSTANCE = new DefaultServiceManager();
     }
 
 
     public static ServiceManager getInstance() {
         return DefaultServiceManagerHolder.INSTANCE;
     }
 
 
     private DefaultServiceManager() {
 
         // load services using the JDK's Service Provider Interface
         for (QueryService service : serviceLoader) {
 
             Class identifierClass = service.getIdentifier().getClass();
 
             for (Class<? extends QueryService> serviceClass : getImplementingInterfaces(service.getClass())) {
                 ServiceKey key = new ServiceKey(identifierClass, serviceClass);
                 services.put(key, service);
                 LOGGER.debug(serviceClass.getSimpleName() + "service loaded");
             }
         }
 
     }
 
 
     @Override
     public <S extends QueryService<I>, I extends Identifier> boolean hasService(I identifier, Class<? extends S> serviceClass) {
         return hasService((Class<? extends I>) identifier.getClass(), serviceClass);
     }
 
 
     @Override
     public <S extends QueryService<I>, I extends Identifier> boolean hasService(Class<? extends I> identifierClass,
                                                                                 Class<? extends S> serviceClass) {
         ServiceKey key = new ServiceKey(identifierClass, serviceClass);
         if (!services.containsKey(key)) {
             return false;
         }
 
         for (QueryService service : services.get(key)) {
             if (service.startup()) {
                 return true;
             }
         }
 
         return false;
 
     }
 
 
     @Override
     public <S extends QueryService<I>, I extends Identifier> S getService(I identifier,
                                                                                       Class<? extends S> serviceClass) {
         return getService((Class<? extends I>) identifier.getClass(), serviceClass);
     }
 
 
     @Override
     public Collection<Identifier> getIdentifiers(Class<? extends QueryService> c) {
 
         Set<Identifier> identifiers = new HashSet<Identifier>();
 
         for (QueryService service : services.values()) {
             if (c.isInstance(service)) {
                 identifiers.add(service.getIdentifier().newInstance());
             }
         }
 
         return identifiers;
 
     }
 
 
     /**
      * @inheritDoc
      */
     @Override
    public <S extends QueryService<I>, I extends Identifier> S getService(Class<? extends I> identifierClass,
                                                                          Class<? extends S> serviceClass) {
 
         ServiceKey key = new ServiceKey(identifierClass, serviceClass);
 
         Collection<QueryService> implementations = (Collection<QueryService>) services.get(key);
 
         // sort into desired order
         for (QueryService service : implementations) {
             if (service.startup()) {
                return (S) service;
             }
         }
 
         throw new NoSuchElementException("No " + serviceClass.getSimpleName()
                                                  + " implementation available for " + identifierClass.getSimpleName());
 
     }
 
 
     /**
      * @inheritDOc
      */
     public <I extends Identifier, S extends QueryService> S createService(Class<? extends I> identifierClass,
                                                                           Class<? extends S> serviceClass) {
 
         Collection<Class<? extends QueryService>> interfaces = getImplementingInterfaces(serviceClass);
         interfaces.add(serviceClass);
         interfaces.add(QueryService.class);
 
         Enhancer enhancer = new Enhancer();
         enhancer.setInterfaces(interfaces.toArray(new Class[0]));
 
         Set<QueryService> services = new HashSet<QueryService>();
         for (Class<? extends QueryService> serviceInterface : getImplementingInterfaces(serviceClass)) {
            services.add((QueryService<?>) getService(identifierClass, serviceInterface));
         }
 
         enhancer.setCallback(new ServiceInterceptor(services));
 
         return (S) enhancer.create();
 
 
     }
 
 
     /**
      * Class traversal algorithm adapted from: <a href="http://today.java.net/pub/a/today/2008/08/21/complex-table-cell-rendering.html">Article</a>
      *
      * @param c
      *
      * @return
      */
     public Collection<Class<? extends QueryService>> getImplementingInterfaces(Class<?> c) {
 
         if (interfaceMap.containsKey(c)) {
             return interfaceMap.get(c);
         }
 
         Queue<Class<?>> queue = new LinkedList<Class<?>>();
         Set<Class<?>> visited = new HashSet<Class<?>>();
         Set<Class<? extends QueryService>> implementations = new HashSet<Class<? extends QueryService>>();
 
         queue.add(c);
         visited.add(c);
 
         while (!queue.isEmpty()) {
             Class<?> curClass = queue.remove();
 
             // get the super types to visit.
             List<Class<?>> supers = new LinkedList<Class<?>>();
             for (Class<?> itrfce : curClass.getInterfaces()) {
                 supers.add(itrfce);
             }
             Class<?> superClass = curClass.getSuperclass(); // this would be null for interfaces.
             if (superClass != null) {
                 supers.add(superClass);
             }
 
             for (Class<?> ifs : supers) {
                 if (QueryService.class.isAssignableFrom(ifs)
                         && ifs.isInterface()
                         && !QueryService.class.equals(ifs)) {
                     implementations.add((Class<? extends QueryService>) ifs);
                 }
                 implementations.addAll(getImplementingInterfaces(ifs));
             }
 
         }
 
         interfaceMap.putAll(c, implementations);
 
         return implementations;
     }
 
 
     /**
      * Compares SerivceKeys based on their identifier class and then their
      * service class.
      */
     private class ServiceKeyComparator implements Comparator<ServiceKey> {
         @Override
         public int compare(ServiceKey o1, ServiceKey o2) {
             int diff = o1.identifier.compareTo(o2.identifier);
             if (diff != 0) {
                 return diff;
             }
             return o1.service.compareTo(o2.service);
         }
     }
 
     /**
      * Compare services instances using the priority and then the name of the
      * service class. This way the top services are always the highest
      * priority.
      */
     private class ServiceComparator implements Comparator<QueryService> {
         @Override
         public int compare(QueryService o1, QueryService o2) {
             int preference = o1.getServiceType().getPriority().compareTo(o2.getServiceType().getPriority());
             if (preference != 0) {
                 return preference;
             }
             int cmp = o1.getIdentifier().compareTo(o2.getIdentifier());
             if(cmp != 0)
                 return cmp;
             return o1.getClass().getName().compareTo(o2.getClass().getName());
         }
     }
 
 
     /**
      * Provides the dispatching of the methods for aggregated services
      */
     private class ServiceInterceptor implements MethodInterceptor {
 
         private Set<QueryService>         services = new TreeSet<QueryService>(new ServiceComparator());
         private Map<Method, QueryService> methods  = new HashMap<Method, QueryService>();
 
 
         public ServiceInterceptor(Set<QueryService> services) {
             this.services.addAll(services);
         }
 
 
         @Override
         public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
 
             // previously located methods
             if (methods.containsKey(method)) {
                 return method.invoke(methods.get(method), objects);
             }
 
             // methods from the QueryService super-type (e.g. setMaxResults)
             // are invoked on all delegate services
             for (Method interface_method : QueryService.class.getMethods()) {
                 if (interface_method.equals(method)) {
 
                     // for void type methods be invoke for all services
                     // this will be the setMinSimilarity setMaxResults etc.
                     if (interface_method.getReturnType().equals(Void.TYPE)) {
                         for (QueryService service : services) {
                             method.invoke(service, objects);
                         }
                     } else {
                         if (method.equals(QueryService.class.getMethod("getServiceType"))) {
                             return QueryService.ServiceType.AGGREGATED;
                         } else {
 
                             // invoke all possible methods and collect all possible return values
                             // if the values match we can safely return the value
                             Set<Object> values = new HashSet<Object>();
                             for (QueryService service : services) {
                                 values.add(method.invoke(service, objects));
                             }
 
                             if (values.size() == 1) {
                                 // should we cache?
                                 return values.iterator().next();
                             }
 
                             System.err.println("Potentially unsafe method call to " + method.getName());
 
                         }
                     }
 
                 }
             }
 
             // collect candidate methods for caching
             for (QueryService service : services) {
                 Class[] interfaces = getImplementingInterfaces(service.getClass()).toArray(new Class[0]);
                 for (Class anInterface : interfaces) {
                     Method[] methods = anInterface.getMethods();
                     for (Method interface_method : methods) {
                         if (interface_method.equals(method)) {
                             this.methods.put(method, service);
                             return method.invoke(service, objects);
                         }
                     }
                 }
             }
 
             throw new NoSuchMethodException("Unable to find " + method.getName()
                                                     + "(" + Joiner.on(",").join(method.getParameterTypes()) + ");"
                                                     + " in services: " + services);
 
         }
     }
 
     // Note: does not account for inherited identifiers
     private class ServiceKey {
 
         private final String identifier;
         private final String service;
 
 
         public ServiceKey(Class<? extends Identifier> identifier,
                           Class<? extends QueryService> service) {
             this.identifier = identifier.getName();
             this.service = service.getName();
         }
 
 
         @Override
         public boolean equals(Object o) {
 
             if (this == o) return true;
             if (!(o instanceof ServiceKey)) return false;
 
             ServiceKey that = (ServiceKey) o;
 
             if (identifier != null ? !identifier.equals(that.identifier)
                                    : that.identifier != null) return false;
             if (service != null ? !service.equals(that.service)
                                 : that.service != null) return false;
 
             return true;
         }
 
 
         @Override
         public int hashCode() {
             int result = identifier != null ? identifier.hashCode() : 0;
             result = 31 * result + (service != null ? service.hashCode() : 0);
             return result;
         }
     }
 
 
 }
