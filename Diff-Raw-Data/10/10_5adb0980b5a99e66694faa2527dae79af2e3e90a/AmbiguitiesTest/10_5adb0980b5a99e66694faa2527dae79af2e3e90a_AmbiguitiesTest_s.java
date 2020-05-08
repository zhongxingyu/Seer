 package see.parser;
 
 import org.junit.Test;
 import see.See;
 import see.tree.FunctionNode;
 
 import static org.junit.Assert.assertEquals;
 
 public class AmbiguitiesTest {
     See see = new See();
 
     @Test
     public void testBindingVsCompareNegative() throws Exception {
         FunctionNode<?, ?> parsed = (FunctionNode<?, ?>) see.parseExpression("a<-4");
         assertEquals("<", parsed.getFunctionName());
     }
 }
