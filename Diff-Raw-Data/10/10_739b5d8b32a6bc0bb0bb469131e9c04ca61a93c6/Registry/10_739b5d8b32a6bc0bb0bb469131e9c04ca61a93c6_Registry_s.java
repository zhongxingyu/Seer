 package au.net.netstorm.boost.spider.registry;
 
 public interface Registry {
     <T, U extends T> void multiple(Class<T> iface, Class<U> impl);
 
     <T, U extends T> void single(Class<T> iface, Class<U> impl);
 
    <T, U extends T> void instance(Class<T> iface, U ref);
 
    void factory(Factory factory);
 
     <T extends Factory> void factory(Class<T> cls);
 }
