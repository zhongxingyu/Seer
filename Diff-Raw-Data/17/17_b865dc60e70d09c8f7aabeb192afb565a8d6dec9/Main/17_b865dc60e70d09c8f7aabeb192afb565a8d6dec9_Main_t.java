import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
 
 import java.util.Arrays;
 import java.util.Collection;
 
 /**
  * @author Thomas Duncan
  */
 public class Main {
     public static void main(String[] arguments) {
         Injector injector = Guice.createInjector(new Module());
         
         startup(injector.getInstance(ServiceManager.class));
         inspect(injector.getInstance(DataCache.class));
         shutdown(injector.getInstance(ServiceManager.class));
     }
     
     private static void startup(ServiceManager manager) {
         manager.startup();
     } 
     
     private static void inspect(DataCache cache) {
         report(cache, "username");
         report(cache, "email");
         report(cache, "id");
     }
     
     private static void report(DataCache cache, String key) {
         report(key, cache.get(key));
     }
     
     private static void report(String key, Object value) {
         System.out.print(key + " -> ");
         System.out.print(value);
         System.out.println();
     }
     
     private static void shutdown(ServiceManager manager) {
         manager.shutdown();
     }
 
     private static class Module extends AbstractModule {
         @Override
         protected void configure() {
             bind(StartupService.class).in(Singleton.class);
             bind(ShutdownService.class).in(Singleton.class);
             bind(DataCache.class).in(Singleton.class);
             bind(ServiceManager.class).to(AnnotationServiceManager.class);
         }
 
         @Provides
         @SuppressWarnings("unchecked")
         protected Collection<Class<?>> providesServiceClasses() {
             return Arrays.asList(StartupService.class, DataCache.class, ShutdownService.class);
         }
     }
 }
