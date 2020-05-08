 package tools;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
  
 /**
  * For more information about this algorithm, see
  * http://archives1.twoplustwo.com/showflat.php?Cat=0&Number=8513906&page=0&fpart=1&vc=1
  * @author Chris Oei http://www.linkedin.com/in/christopheroei
  */
 public class TwoPlusTwo {
  
 	
     /**
      * HandRanks.dat
      * http://static.eluctari.com/download/game/poker/HandRanks.dat<br/>
      * Size: 129951336<br/>
      * CRC32: 7808da57<br/>
      * MD5: 5de2fa6f53f4340d7d91ad605a6400fb<br/>
      * SHA1: f8467e36f470c9beea98c47d661c9b2c4a13e577<br/>
      * SHA256: ad00f3976ad278f2cfd8c47b008cf4dbdefac642d70755a9f20707f8bbeb3c7e<br/>
      */
     private static final String HAND_RANK_DATA_FILENAME = "HandRanks.dat";
     private static final int HAND_RANK_SIZE = 32487834;
     private static int HR[] = new int[HAND_RANK_SIZE];
     public static String[] HAND_RANKS = {"BAD!!", "High Card", "Pair", "Two Pair", "Three of a Kind",
     	"Straight", "Flush", "Full House", "Four of a Kind", "Straight Flush"};
  
     /**
      * Calculate the poker hand rank of seven cards.
      * @param cards		an integer array
      * @param offset	evaluate the next seven cards starting at this position
      * @return			the hand rank
      */
     public static int lookupHand7(int[] cards, int offset) {
         int pCards = offset;
         int p = HR[53 + cards[pCards++]];
         p = HR[p + cards[pCards++]];
         p = HR[p + cards[pCards++]];
         p = HR[p + cards[pCards++]];
         p = HR[p + cards[pCards++]];
         p = HR[p + cards[pCards++]];
         return HR[p + cards[pCards]];
     }
  
     /**
      * Calculate the poker hand rank of five cards.
      * @param cards		an integer array
      * @return			the hand rank
      */
     public static int lookupHand5(int[] cards) {
         int pCards = 0;
         int p = HR[53 + cards[pCards++]];
         p = HR[p + cards[pCards++]];
         p = HR[p + cards[pCards++]];
         p = HR[p + cards[pCards++]];
         p = HR[p + cards[pCards]];
         p = HR[p];
         return HR[p];
     }
     
     /**
      * Converts a little-endian byte array to a Java (big-endian) integer.
      * We need this because the HandRanks.dat file was generated using
      * a little-endian C program and we want to maintain compatibility.
      * @param b
      * @param offset
      * @return
      */
     private static final int littleEndianByteArrayToInt(byte[] b, int offset) {
         return (b[offset + 3] << 24) + ((b[offset + 2] & 0xFF) << 16)
                 + ((b[offset + 1] & 0xFF) << 8) + (b[offset] & 0xFF);
     }
  
     {
         int tableSize = HAND_RANK_SIZE * 4;
         byte[] b = new byte[tableSize];
         InputStream br = null;
         try {
             br = new BufferedInputStream(ClassLoader.getSystemResource("data/HandRanks.dat").openStream());
             int bytesRead = br.read(b, 0, tableSize);
             if (bytesRead != tableSize) {
             	System.out.println("TableError");
             }
         } catch (FileNotFoundException e) {
             System.out.println(e.getStackTrace());
         } catch (IOException e) {
         	System.out.println(e.getStackTrace());
         } finally {
             try {
                 br.close();
             } catch (IOException e) {
             	System.out.println(e.getStackTrace());
             }
         }
         for (int i = 0; i < HAND_RANK_SIZE; i++) {
             HR[i] = littleEndianByteArrayToInt(b, i * 4);
         }
 
     }
     
 }
