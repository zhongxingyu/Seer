 package sugar;
 
import junit.framework.AssertionFailedError;
 import org.junit.Test;
 
import static junit.framework.Assert.fail;
 import static sugar.Then.and;
 import static sugar.Then.then;
 
 
 public class SyntaxTest {
 
     @Test
     public void shouldEqual() {
         String val = "hei";
         then(val).should.be("hei");
         and(val).should_not.be("heim");
     }
 
     @Test
     public void intShouldEqual() {
         then(1).should.be(1);
 
     }
 
 
     @Test
     public void intShouldEqualFails() {
        boolean failed = false;
        try {
            then(1).should.be(2);

        } catch (AssertionFailedError e) {
            failed = true;
           
        }

        if(!failed) fail("should fail");
     }
 
 }
 
 
 
