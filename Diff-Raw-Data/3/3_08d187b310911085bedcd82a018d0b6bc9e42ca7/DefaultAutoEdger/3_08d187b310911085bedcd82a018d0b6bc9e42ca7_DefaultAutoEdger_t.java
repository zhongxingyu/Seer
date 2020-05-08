 package au.net.netstorm.boost.nursery.autoedge;
 
 import au.net.netstorm.boost.edge.java.lang.reflect.ProxySupplier;
 import au.net.netstorm.boost.spider.instantiate.Nu;
 
 public final class DefaultAutoEdger implements AutoEdger {
     ProxySupplier proxier;
     TempMultiNu multiNu;
     Nu nu;
 
     @SuppressWarnings("unchecked") // FIXME-MH case for a nuer that supports generics using super type token
     public <E extends Edge<T>, T> E edge(Class<E> edge, T target) {
         ClassLoader loader = edge.getClassLoader();
         AutoEdge<T> impl = nu.nu(DefaultAutoEdge.class, target);
         Class<?>[] type = { edge };
         Object proxy = proxier.getProxy(loader, type, impl);
         return edge.cast(proxy);
     }
 
     public <E extends Edge<T>, T> E newEdge(Class<E> edge, Class<T> type, Object... params) {
         T target = multiNu.nu(type, params);
         return edge(edge, target);
     }
 }
