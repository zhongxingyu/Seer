 package br.com.ingenieux.cloudy.data.util;
 
import java.io.IOException;
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 import java.util.Arrays;
 import java.util.Collection;
import java.util.Map;
import java.util.Properties;
 
 import org.junit.runners.BlockJUnit4ClassRunner;
 import org.junit.runners.model.InitializationError;
 
import com.google.inject.AbstractModule;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Module;
import com.google.inject.name.Names;
 
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
        Module configurationModule = new AbstractModule() {
             @Override
            protected void configure() {
            	Properties p = new Properties();
            	
            	try {
					p.load(getClass().getResourceAsStream("/simplejpa.properties"));
				} catch (IOException e1) {
					throw new RuntimeException(e1);
				}
            	
            	for (Map.Entry<Object, Object> e : p.entrySet())
            		bind(String.class).annotatedWith(Names.named("" + e.getKey())).toInstance("" + e.getValue());
             }
         };
 
         return Arrays.<Module> asList(configurationModule, new TestModule("test"));
     }
 
 }
