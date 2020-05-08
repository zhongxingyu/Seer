 package au.net.netstorm.boost.demo.spider.core;
 
 import au.net.netstorm.boost.spider.inject.core.Injector;
 import au.net.netstorm.boost.spider.instantiate.Nu;
 import au.net.netstorm.boost.spider.resolve.Resolver;
 
 // FIX 1676 Test drive.  This triggers work on "why use facades" discussion paper.
 public final class DefaultSpider implements Spider {
     private final Nu nu;
     private final Injector injector;
     private final Resolver resolver;
 
     public DefaultSpider(Nu nu, Injector injector, Resolver resolver) {
         this.nu = nu;
         this.injector = injector;
         this.resolver = resolver;
     }
 
    public <T, U extends T> T nu(Class<U> impl, Object... params) {
         return nu.nu(impl, params);
     }
 
     public void inject(Object ref) {
         injector.inject(ref);
     }
 
     public <T> T resolve(Class<T> type) {
         return resolver.resolve(type);
     }
 }
