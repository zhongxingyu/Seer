 package au.net.netstorm.boost.spider.registry;
 
 public interface Registry {
    // FIX ()   2237 Add specific host for all methods?
     <T, U extends T> void multiple(Class<T> iface, Class<U> impl);
 
     <T, U extends T> void single(Class<T> iface, Class<U> impl);
 
//    <T, U extends T> void single(Class<?> host, Class<T> iface, Class<U> impl);
 
    <T, U extends T> void instance(Class<T> iface, U ref);
 
     <T extends Factory> void factory(Class<T> cls);

    void factory(Factory factory);
 }
