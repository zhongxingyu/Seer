 package test.FastChainAlgoritms.FastChain;
 
import junit.framework.Assert;
 import junit.framework.TestCase;
 import libiada.FastChainAlgorithms.FastChain.FastChain;
 import libiada.FastChainAlgorithms.FastChain.FastUniformChain;
import org.junit.Test;
 
 import java.util.ArrayList;
 
 /**
  * Created by IntelliJ IDEA.
  * User: alex
  * Date: 28.10.11
  * Time: 0:40
  */
 public class testFastUniformChain extends TestCase {
    @Test
     public void testGetPositions() throws Exception {
         FastChain ch = new FastChain("12112141");
         FastUniformChain uChain = ch.getFastUniformChain("1");
 
         ArrayList<Integer> poses = uChain.getPositions();
         assertEquals(poses.get(0), (Integer)1);
         assertEquals(poses.get(1), (Integer)3);
         assertEquals(poses.get(2), (Integer)4);
         assertEquals(poses.get(3), (Integer)6);
         assertEquals(poses.get(4), (Integer)8);
     }
 }
