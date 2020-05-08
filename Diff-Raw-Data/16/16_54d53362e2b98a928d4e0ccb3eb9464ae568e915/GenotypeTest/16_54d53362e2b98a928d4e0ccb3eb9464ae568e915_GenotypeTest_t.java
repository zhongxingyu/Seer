 package to.richard.test;
 
 import org.junit.Test;
 import static org.junit.Assert.assertArrayEquals;
 import static org.junit.Assert.assertEquals;
 
 import to.richard.Genotype;
 
 /**
  * Author: Richard To
  * Date: 1/28/13
  */
 
 
 public class GenotypeTest {
 
     @Test
     public void testConvertIntToBitArray() throws Exception {
         Genotype genotype = new Genotype(3, 5);
         int[] bitArray = genotype.convertIntToBitArray(20, 5);
         int[] expectedBitArray = new int[]{1, 0, 1, 0, 0};
         assertArrayEquals(expectedBitArray, bitArray);
     }
 
     @Test
     public void testConvertIntToBitArrayOverflow() throws Exception {
         Genotype genotype = new Genotype(3, 5);
         int[] bitArray = genotype.convertIntToBitArray(32, 5);
         int[] expectedBitArray = new int[]{0, 0, 0, 0, 0};
         assertArrayEquals(expectedBitArray, bitArray);
     }
 
     @Test
     public void testConvertBitArrayToInt() throws Exception {
         int[] expectedBitArray = new int[]{1, 0, 0, 1, 0};
         Genotype genotype = new Genotype(expectedBitArray);
         assertEquals(18, genotype.getValue());
         assertEquals(324, genotype.getFitness());
         assertArrayEquals(expectedBitArray, genotype.getGenes());
     }
 
     @Test
    public void testConvertBitArrayToIntBitFirst() throws Exception {
        int[] expectedBitArray = new int[]{1, 0, 0, 1, 1};
        Genotype genotype = new Genotype(expectedBitArray);
        assertEquals(19, genotype.getValue());
        assertEquals(361, genotype.getFitness());
        assertArrayEquals(expectedBitArray, genotype.getGenes());
    }

    @Test
     public void testProperties() throws Exception {
         Genotype phenoType = new Genotype(3, 5);
         int[] expectedBitArray = new int[]{0, 0, 0, 1, 1};
         assertArrayEquals(expectedBitArray, phenoType.getGenes());
         assertEquals(3, phenoType.getValue());
         assertEquals(9, phenoType.getFitness());
     }
 }
