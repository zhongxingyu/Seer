 package br.com.ingenieux.cloudy.data.util;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 import java.util.Arrays;
 import java.util.Collection;
 
 import org.junit.runners.BlockJUnit4ClassRunner;
 import org.junit.runners.model.InitializationError;
import org.nnsoft.guice.rocoto.configuration.ConfigurationModule;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Module;
 
 public class SDBTestRunner extends BlockJUnit4ClassRunner {
     @Retention(RetentionPolicy.RUNTIME)
     @Target(ElementType.TYPE)
     public static @interface SDB {
         public String domain();
     }
     
     public SDBTestRunner(Class<?> klass) throws InitializationError {
         super(klass);
     }
     
     @Override
     protected Object createTest() throws Exception {
         Object parent = super.createTest();
         
         Injector injector = Guice.createInjector(getModules());
         
         injector.injectMembers(parent);
         
         return parent;
     }
     
     public Collection<Module> getModules() {
        ConfigurationModule configurationModule = new ConfigurationModule() {
             @Override
            protected void bindConfigurations() {
                bindProperties("simplejpa.properties");
             }
         };
 
         return Arrays.<Module> asList(configurationModule, new TestModule("test"));
     }
 
 }
