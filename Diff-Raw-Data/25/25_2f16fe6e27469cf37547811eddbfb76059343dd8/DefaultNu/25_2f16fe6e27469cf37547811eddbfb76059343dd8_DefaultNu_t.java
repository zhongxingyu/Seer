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
 
    public <T> T nu(Class<? extends T> impl, Object... params) {
        Interface iface = new DefaultInterface(NoInterface.class);
        Implementation implementation = new DefaultImplementation(impl);
        ResolvedInstance resolved = engine.provide(iface, implementation, params);
        return (T) resolved.getRef();
     }
 }
