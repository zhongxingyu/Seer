 package au.net.netstorm.boost.nursery.autoedge;
 
 import au.net.netstorm.boost.edge.java.lang.reflect.ProxySupplier;
 import au.net.netstorm.boost.gunge.generics.TypeTokenInstance;
 import au.net.netstorm.boost.gunge.generics.TypeTokenResolver;
 import au.net.netstorm.boost.spider.instantiate.Nu;
 
 public final class DefaultAutoEdger implements AutoEdger {
     TypeTokenResolver typeResolver;
     EdgeValidator validator;
     ProxySupplier proxier;
     RealNu realNu;
     Nu nu;
 
     public <E extends Edge<R>, R> E edge(Class<E> edge, R real) {
         return createEdge(Edge.class, edge, real);
     }
 
     public <E extends StaticEdge<R>, R> E edge(Class<E> edge) {
         return createEdge(StaticEdge.class, edge, null);
     }
 
     public <E extends Edge<R>, R> E nu(Class<E> edge, Object... params) {
         R real = realNu.nu(edge, params);
         return edge(edge, real);
     }
 
     // FIX 2328 realClass not used?  Remove and follow out.
     // FIX 2328 Mark I'm going to assume you need realClass for something.
     // FIX 2328 Cause if you don't TTR field goes (and maybe TTR).
 
     // FIX 2328 yeh it is required... being used now... still bit of work
     // FIX 2328 to do here have to tidy up these two methods, gotten a bit
     // FIX 2328 out of control
 
     private <E> E createEdge(Class<?> edgeType, Class<E> edge, Object real) {
         AutoEdge handler = buildHandler(edgeType, edge, real);
         ClassLoader loader = edge.getClassLoader();
         Class<?>[] type = {edge};
         Object proxy = proxier.getProxy(loader, type, handler);
         return edge.cast(proxy);
     }
 
     private <E> AutoEdge buildHandler(Class<?> edgeType, Class<E> edge, Object real) {
         TypeTokenInstance typeToken = typeResolver.resolve(edgeType, edge);
         Class<?> realClass = typeToken.rawType();
         validator.validate(edge, realClass);
         return nu.nu(DefaultAutoEdge.class, realClass, real);
     }
 }
