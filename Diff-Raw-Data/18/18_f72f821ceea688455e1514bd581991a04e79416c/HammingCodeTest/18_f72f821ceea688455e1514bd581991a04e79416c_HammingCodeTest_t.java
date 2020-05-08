 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package kryptoprojekt.model;
 
 import java.util.Hashtable;
 import junit.framework.TestCase;
 
 /**
  *
  * @author LiTTle, Mario
  */
 public class HammingCodeTest extends TestCase{
 
 
     public HammingCodeTest(String name) {
         super(name);
     }
 
     public void testGenerateGeneratorMatrix() {
         HammingCode c = new HammingCode("110");
         Matrix m = Matrix.valueOf("1,0,0,0,1,1,1|0,1,0,1,0,1,1|0,0,1,1,1,0,1");
         KryptoType<PrimeFieldElement>[][] t = new KryptoType[m.getMatrixRowCapacity()][m.getMatrixColumnCapacity()];
         for (int i=0; i< m.getMatrixRowCapacity(); i++)
             for (int j=0; j<m.getMatrixColumnCapacity(); j++)
                 t[i][j] = new PrimeFieldElement((Z)m.get(i, j), new Z(2));
         Matrix<PrimeFieldElement> fin = new Matrix(t);
         assertEquals(fin, c.generateGeneratorMatrix());
     }
 
     public void testGenerateControlMatrix() {
         HammingCode c = new HammingCode("110");
         Matrix cMatrix = Matrix.valueOf("0,1,1,1|1,0,1,1|1,1,0,1|1,0,0,0|0,1,0,0|0,0,1,0|0,0,0,1");
         KryptoType<PrimeFieldElement>[][] t = new KryptoType[cMatrix.getMatrixRowCapacity()][cMatrix.getMatrixColumnCapacity()];
         for (int i=0; i< cMatrix.getMatrixRowCapacity(); i++)
             for (int j=0; j<cMatrix.getMatrixColumnCapacity(); j++)
                 t[i][j] = new PrimeFieldElement((Z)cMatrix.get(i, j), new Z(2));
         Matrix<PrimeFieldElement> fin = new Matrix(t);
         assertEquals(fin, c.generateControlMatrix());
     }
 
    public void testControlMatrix2(){
        HammingCode c= new HammingCode("1101");
    }

     public void testEncode() {
         Coder c = new HammingCode("110");
         assertEquals("1101100", c.encode());
     }
 
     public void testCalculateSyndrom(){
         Coder c = new HammingCode("110");
         c.encode();
         assertEquals("0000", c.calculateSyndrom());
 
     }
 
     public void testDetectError(){
         // No Errors detected
         HammingCode c = new HammingCode("110");
         c.encode();
         c.calculateSyndrom();
         c.detectError();
         assertFalse(c.isErrorsFound());
         assertEquals("110",c.decode());
         //generate too many errors
         try{
         c.generateBitError(1);
         c.calculateSyndrom();
 
         c.detectError();
         assertTrue((Boolean) c.isErrorsFound());
             fail();
         }catch(RuntimeException r){
 
         }
 
         //generate one bit error and correct it
 
         c = new HammingCode("110");
         c.encode();
         c.generateBitError(0.1);
         c.calculateSyndrom();
         c.detectError();
 
         if(c.isErrorsFound()){
             if(c.getErrorPos() > -1){
                 assertEquals("110",c.decode());
             }
             else{
                 fail();
             }
         }
         else{
             fail();
         }
 
     }
 
 
     public void testDecode(){
         HammingCode c = new HammingCode("110");
         c.encode();
         assertEquals("110", c.decode());
     }
 
     public void testHammingDistance() {
        KryptoType<PrimeFieldElement> [][] t = new KryptoType[1][7];
        KryptoType<PrimeFieldElement> [][] t2 = new KryptoType[1][7];
 
        t[0][0] = new PrimeFieldElement(1,2);
        t[0][1] = new PrimeFieldElement(0,2);
        t[0][2] = new PrimeFieldElement(1,2);
        t[0][3] = new PrimeFieldElement(1,2);
        t[0][4] = new PrimeFieldElement(1,2);
        t[0][5] = new PrimeFieldElement(1,2);
        t[0][6] = new PrimeFieldElement(0,2);
 
        t2[0][0] = new PrimeFieldElement(0,2);
        t2[0][1] = new PrimeFieldElement(0,2);
        t2[0][2] = new PrimeFieldElement(1,2);
        t2[0][3] = new PrimeFieldElement(0,2);
        t2[0][4] = new PrimeFieldElement(1,2);
        t2[0][5] = new PrimeFieldElement(1,2);
        t2[0][6] = new PrimeFieldElement(1,2);
 
        assertEquals(new Z(3), HammingCode.hammingDistance(new Matrix(t), new Matrix(t2)));
     }
 
     public void testVectorWeight() {
         KryptoType<PrimeFieldElement>[][] t = new KryptoType[1][3];
         t[0][0] = new PrimeFieldElement(0, 2);
         t[0][1] = new PrimeFieldElement(1, 2);
         t[0][2] = new PrimeFieldElement(1, 2);
         assertEquals(new Z(2),HammingCode.vectorWeight(new Matrix(t)));
     }
 }
