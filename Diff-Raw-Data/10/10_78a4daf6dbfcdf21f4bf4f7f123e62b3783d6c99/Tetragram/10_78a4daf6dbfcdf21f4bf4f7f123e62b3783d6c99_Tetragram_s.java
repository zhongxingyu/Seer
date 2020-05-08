 package adfgvx;
 
 import gnu.trove.TLongDoubleHashMap;
 import gnu.trove.TLongIntHashMap;
 import gnu.trove.TLongIntIterator;
import gnu.trove.TLongIntProcedure;
 
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 /**
  * This class delivers functions to solve a monoalphabetic substitution cipher.
  * It uses log frequencies of tetragrams to determine how fit a solution is. The
  * fitness function can be used in hill-climbing.
  * 
  * @author Ben Ruijl
  * 
  */
 public class Tetragram {
     /** Logger. */
     private static final Logger LOG = Logger.getLogger(Tetragram.class);
     /** Table of log tetragram frequencies. */
     private final TLongDoubleHashMap tetragrams;
 
     /**
      * Creates an instance of tetragram, which contains a reference frequency
      * table.
      * 
      * @param filename
      *            Filename of the reference frequency table
      * @throws IOException
      *             Read error
      */
     public Tetragram(final String filename) throws IOException {
         tetragrams = new TLongDoubleHashMap();
         readTetragrams(filename);
     }
 
     /**
      * Reads a log frequency table from a reference text.
      * 
      * @param filename
      *            Filename of the source text
      * @throws IOException
      *             Read error
      */
     public void readTetragrams(final String filename) throws IOException {
         final InputStream file = new FileInputStream(filename);
         final DataInputStream in = new DataInputStream(file);
 
         final int size = in.readInt();
 
         for (int i = 0; i < size; i++) {
             long tet = in.readLong();
             tetragrams.put(tet, in.readDouble());
         }
 
         LOG.info("tetragrams read.");
 
         in.close();
     }
 
     /**
      * Calculates the fitness of a certain substitution of text.
      * 
      * @param cipherText
      *            Cipher text
      * @param alphabet
      *            Alphabet map from cipher text to certain solution
      * @return Fitness expressed as a number
      */
     public double fitness(final String cipherText,
             final Map<Character, Character> alphabet) {
         final String newText = Encryption.transcribeCipherText(cipherText,
                 alphabet);
 
         final TLongIntHashMap ciphertetragrams = getTetragramCount(newText);
 
         double fitness = 0;
         final double sigmaSquared = 4.0;
 
         for (TLongIntIterator it = ciphertetragrams.iterator(); it.hasNext();) {
             it.advance();
             fitness += getContributionToFitness(it.key(), it.value(), newText,
                     sigmaSquared);
         }
 
         return fitness;
     }
 
     private double getContributionToFitness(final long tetragram,
             final int count, final String text, final double sigmaSquared) {
         double sourceLogFreq = tetragrams.get(tetragram);
 
         final double logFreq = Math.log((double) count
                 / (double) (text.length() - 3));
 
         /* TODO: check if the factor in front of the exp is required. */
         final double exponent = -(logFreq - sourceLogFreq)
                 * (logFreq - sourceLogFreq) / (2.0 * sigmaSquared);
 
         return 1.0 / Math.sqrt(2 * Math.PI * sigmaSquared)
                 * Utils.exp(exponent);
     }
 
     /**
      * Counts the appearance of a tetragram in a text.
      * 
      * @param text
      *            Text to scan
      * @return Map of number of appearances of each tetragram
      */
     private TLongIntHashMap getTetragramCount(final String text) {
         final TLongIntHashMap ciphertetragrams = new TLongIntHashMap();
 
         char[] c = text.toCharArray();
         long tetragram = ((long) c[0] << 48) | (((long) c[1]) << 32)
                 | (((long) c[2]) << 16) | ((long) c[3]);
 
         for (int i = 4, last = text.length(); i < last; i++) {
 
             tetragram = (tetragram << 16) | ((long) c[i]);
 
             ciphertetragrams.adjustOrPutValue(tetragram, 1, 1);
         }
 
         return ciphertetragrams;
     }
 }
