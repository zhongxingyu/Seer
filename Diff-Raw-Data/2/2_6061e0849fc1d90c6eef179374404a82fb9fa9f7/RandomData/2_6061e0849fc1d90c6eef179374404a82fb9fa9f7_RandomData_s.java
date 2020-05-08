 package talkingnet.utils.random;
 
 /**
  *
  * @author Alexander Oblovatniy <oblovatniy@gmail.com>
  */
 public class RandomData {
 
     public static byte[] getRandomData(int lengthEdge) {
         int length = RandomNumbers.getRandom(lengthEdge);
         return getRandomDataFixedLength(length);
     }
 
    public static byte[] getRandomDataOddLength(int lengthEdge) {
         int length = RandomNumbers.getRandom(lengthEdge);
         length -= length % 2;
         return getRandomDataFixedLength(length);
     }
     
     public static byte[] getRandomDataFixedLength(int length) {
         byte[] data = new byte[length];
         for (int i = 0; i < data.length; i++) {
             data[i] = RandomNumbers.getRandomPositiveByte();
         }
         return data;
     }
 }
