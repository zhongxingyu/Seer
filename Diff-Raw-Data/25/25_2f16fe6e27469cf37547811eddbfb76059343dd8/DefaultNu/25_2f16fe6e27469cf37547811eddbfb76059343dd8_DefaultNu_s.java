 package au.net.netstorm.boost.spider.instantiate;
 
 import au.net.netstorm.boost.spider.core.NoInterface;
 import au.net.netstorm.boost.spider.core.ProviderEngine;
 import au.net.netstorm.boost.util.type.DefaultImplementation;
 import au.net.netstorm.boost.util.type.DefaultInterface;
 import au.net.netstorm.boost.util.type.Implementation;
 import au.net.netstorm.boost.util.type.Interface;
 import au.net.netstorm.boost.util.type.ResolvedInstance;
 
 public final class DefaultNu implements Nu {
     private final ProviderEngine engine;
 
     public DefaultNu(ProviderEngine engine) {
         this.engine = engine;
     }
 
    public <T, U extends T> T nu(Class<U> impl, Object... params) {
        // FIX BREADCRUMB - NoInterface hurts me... Can't make ProviderEngine type safe
        Interface<NoInterface> iface = new DefaultInterface<NoInterface>(NoInterface.class);
        Implementation<U> implementation = new DefaultImplementation<U>(impl);
        ResolvedInstance<T> resolved = engine.provide(iface, implementation, params);
        return resolved.getRef();
     }
 }
