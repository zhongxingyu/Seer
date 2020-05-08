 package core.ga;
 
 import core.io.repr.col.DomainMemoizable;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author Rekin
  */
 public class RuleChromosomeSignatureTest {
 
     Set cs = new HashSet();
     List<Set> mockSets = new ArrayList<Set>();
     List<Integer> sizes = new ArrayList<Integer>();
     List<DomainMemoizable> mockatts = new ArrayList<DomainMemoizable>();
     DomainMemoizable classSet;
 
     public RuleChromosomeSignatureTest() {
         for (Integer i = 1; i < 155; i += 8) {
             cs.add(i);
             HashSet hashSet = new HashSet(cs);
             mockSets.add(hashSet);
             sizes.add(hashSet.size());
         }
         for (final Set mo : mockSets) {
             mockatts.add(new DomainMemoizable() {
 
                 public Set getDomain() {
                     return mo;
                 }
             });
         }
         System.out.println(mockatts.get(3).getDomain());
         classSet = mockatts.get(3);
     }
 
     @Test
     public void testGetGeneAddresses() {
         RuleChromosomeSignature sig = new RuleChromosomeSignature(mockatts, classSet);
         Integer[] expectedGeneAddresses = new Integer[]{
             0, 3, 6, 10, 14, 19, 24, 29, 34, 40, 46,
             52, 58, 64, 70, 76, 82, 89, 96, 103};
         Integer[] result = sig.getGeneAddresses();
         assertArrayEquals(expectedGeneAddresses, result);
     }
 
     @Test
     public void testValueCodeSizes() {
         RuleChromosomeSignature sig = new RuleChromosomeSignature(mockatts, classSet);
         Integer[] expectedCodeSizes = new Integer[]{
             1, 1, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5};
         assertArrayEquals(expectedCodeSizes, sig.getValueCodeSizes());
     }
 
     @Test
     public void testGetBits() {
         RuleChromosomeSignature sig = new RuleChromosomeSignature(mockatts, classSet);
         int expectedBits = 112;
         assertEquals(expectedBits, sig.getBits());
     }
     @Test
     public void testGetClassSize() {
         RuleChromosomeSignature sig = new RuleChromosomeSignature(mockatts, classSet);
        Integer expectedBits = 2;
         assertEquals(expectedBits, sig.getClazzSize());
     }
 
     @Test
     public void testGetClassAddress() {
         RuleChromosomeSignature sig = new RuleChromosomeSignature(mockatts, classSet);
         Integer expectedClassAddr = 110;
         assertEquals(expectedClassAddr, sig.getClazzAddress());
 
     }
 }
