 package osgitest.itest;
 
 import org.junit.Assert;
 import osgitest.app.Service;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.ops4j.pax.exam.Configuration;
 import org.ops4j.pax.exam.Option;
 import org.ops4j.pax.exam.junit.PaxExam;
 import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
 import org.ops4j.pax.exam.spi.reactors.PerMethod;
 
 import javax.inject.Inject;
 
 import static org.ops4j.pax.exam.CoreOptions.*;
 import static org.ops4j.pax.exam.osgi.KarafOptions.*;
 
 @RunWith(PaxExam.class)
 @ExamReactorStrategy(PerMethod.class)
 public class OSGiIT {
 
     @Inject
     private Service service;
 
     @Configuration
     public Option[] config() {
         return options(
                //features("mvn:osgi-test/osgi-app/1.0-SNAPSHOT/xml/features", "app"),
                features("file:../osgi-app/target/classes/features.xml", "app"),
                 junitBundles(),
                 frameworkStartLevel(60)
         );
     }
 
     @Test
     public void doSomething() {
         Assert.assertEquals("Hello John", service.hello("John"));
     }
 }
