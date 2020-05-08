 import static org.junit.Assert.*;
 
 import component1.*;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 
 /**
  * Just a quick test to fire up Spring.. Debugging some JBoss/Spring issues.
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration
 public final class SillyTest {
 
     @Autowired
     private SomeBean someBean;
 
 
     @Test
     public void java_compiler_handles_utf8() {
         assertEquals("æøåÆØÅ", someBean.utf8String());
        System.err.println(someBean.utf8String());
     }
 }
