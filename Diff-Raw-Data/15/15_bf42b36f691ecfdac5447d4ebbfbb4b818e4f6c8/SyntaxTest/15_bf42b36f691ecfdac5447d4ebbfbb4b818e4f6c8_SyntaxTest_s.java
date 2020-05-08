 package sugar;
 
 import org.junit.Test;
 
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
        then(1).should.be(2);
     }
 
 }
 
 
 
