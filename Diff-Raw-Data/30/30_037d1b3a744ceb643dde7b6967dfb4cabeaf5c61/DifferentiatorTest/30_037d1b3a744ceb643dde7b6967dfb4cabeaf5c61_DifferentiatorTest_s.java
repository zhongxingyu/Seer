 package differentiator;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 public class DifferentiatorTest {
 
     @Test
     public void singletonVar() {
         Differentiator diff = new Differentiator();
         String output = "1";
         String actual = diff.evaluate("(x)","x");
         assertEquals(output, actual);
     }
     
     @Test
     public void singletonNum() {
         Differentiator diff = new Differentiator();
         String output = "0";
         String actual = diff.evaluate("(3)","x");
         assertEquals(output, actual);
     }
     
     @Test
     public void singleExpression() {
         Differentiator diff = new Differentiator();
         String output = "(0+1)";
         String actual = diff.evaluate("(3+x)","x");
         assertEquals(output, actual);
     }
     
     @Test
     public void nestedExpression() {
         Differentiator diff = new Differentiator();
         String output = "(((2*1)+(x*0))+((3*1)+(x*0)))";
         String actual = diff.evaluate("((2*x)+(3*x))","x");
         assertEquals(output, actual);
     }
     
     @Test
     public void differentVariable() {
         Differentiator diff = new Differentiator();
         String output = "(((2*1)+(x*0))+((3*0)+(y*0)))";
         String actual = diff.evaluate("((2*x)+(3*y))","x");
         assertEquals(output, actual);
     }
     
     @Test
     public void multiParenthesisVariable() {
         Differentiator diff = new Differentiator();
         String output = "(1+0)";
         String actual = diff.evaluate("((((x+3))))","x");
         assertEquals(output, actual);
     }
 
 }
