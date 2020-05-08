 package test.FastChainAlgoritms.FastChain.Calculators;
 
 import junit.framework.TestCase;
 import libiada.FastChainAlgorithms.FastChain.Calculators.FastCalculatorFactory;
 import libiada.FastChainAlgorithms.FastChain.FastChain;
 
 /**
  * Created by IntelliJ IDEA.
  * User: alex
  * Date: 28.10.11
  * Time: 3:09
  */
 public class testMuCalculator extends TestCase {
     public void testMuValue() throws Exception {
         FastChain ch = new FastChain("121211");
         double mu = FastCalculatorFactory.getMu().getValue(ch.getFastUniformChain("1"), null);
 
         double testRes = (1.0 * 3.0 * 5.0 * 6.0) / (1.0 * 2.0 * 3.0 * 4.0 * 5.0 * 6.0);
         assertEquals(testRes, mu);
     }
 }
