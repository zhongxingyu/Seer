 package to.richard;
 
 /**
  * Author: Richard To
  * Date: 1/28/13
  */
 
 /**
  * Represents a genotype for SGA of x squared.
  */
 public class Genotype {
 
     public static int BASE_2 = 2;
 
     private int[] _xValueBinary;
     private int _xValue;
     private int _xSquaredValue;
     private int _bitStringLength;
 
     public Genotype(int value, int bitStringLength) {
         _xValue = value;
         _xSquaredValue = _xValue * _xValue;
         _xValueBinary = convertIntToBitArray(_xValue, bitStringLength);
         _bitStringLength = bitStringLength;
     }
 
     public Genotype(int[] genes) {
         _xValue = convertBitArrayToInt(genes);
         _xSquaredValue = _xValue * _xValue;
         _bitStringLength = genes.length;
         _xValueBinary = genes.clone();
     }
 
     /**
      * Converts bit array to int
      * @param bitArray
      * @return
      */
     public int convertBitArrayToInt(int[] bitArray) {
         int value = 0;
         int placeValue = 0;
         int bitLength = bitArray.length - 1;
        for (int i = 0; i < bitLength; i++) {
             placeValue += bitArray[i] * Math.pow(BASE_2, bitLength - i);
         }
         return placeValue;
     }
 
     /**
      * Converts int to padded bit array.
      *
      * If the value is greater than the bit array length, then
      * there will overflow will occur.
      *
      * @param value  An integer
      * @param bitArrayLength Length of bit array
      * @return int[]
      */
     public int[] convertIntToBitArray(int value, int bitArrayLength) {
         int[] bitArray = new int[bitArrayLength];
         String bitString = Integer.toBinaryString(value);
         int bitStringStart = 0;
         int bitArrayStart =  bitArrayLength - bitString.length();
         if (bitArrayStart < 0) {
             bitStringStart = bitArrayStart * -1;
         }
         for (int i = bitStringStart; i < bitString.length() ; ++i) {
             bitArray[bitArrayStart + i] = (bitString.charAt(i) == '1') ? 1 : 0;
         }
         return bitArray;
     }
 
     public int[] getGenes() {
         return _xValueBinary.clone();
     }
 
     public int getValue() {
         return _xValue;
     }
 
     public int getFitness() {
         return _xSquaredValue;
     }
 
     public Genotype clone() {
         return new Genotype(_xValue, _bitStringLength);
     }
 }
