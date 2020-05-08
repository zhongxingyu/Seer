 package test.FastChainAlgoritms.FastChain;
 
 import junit.framework.TestCase;
import libiada.FastChainAlgorithms.FastChain.FastAlphabet;
 
 /**
  * Created by IntelliJ IDEA.
  * User: alex
  * Date: 20.01.12
  * Time: 1:13
  */
 public class testFastAlphabet extends TestCase {
    private FastAlphabet alphabet;
 
    public void testAdd() throws Exception {
        alphabet = new FastAlphabet();
        alphabet.add("123");
        alphabet.add("11");

        assertEquals("123", alphabet.get(0));
        assertEquals("11", alphabet.get(1));
    }

    public void addNull() throws Exception {

    }
 }
