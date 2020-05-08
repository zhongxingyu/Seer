 package thompson.core;
 
 import java.util.*;
 import java.util.regex.*;
 
 public class BaseExponent {
   private static final Pattern RE_LEFT_PAREN =   Pattern.compile("\\(");
   private static final Pattern RE_X_UNDERSCORE = Pattern.compile("x_");
   private static final Pattern RE_BASE =         Pattern.compile("\\d+");
   private static final Pattern RE_CARET =        Pattern.compile("\\^");
   private static final Pattern RE_EXPONENT =     Pattern.compile("\\-?\\d+");
   private static final Pattern RE_RIGHT_PAREN =  Pattern.compile("\\)");
 
   public int[] bases, exponents;
 
   public BaseExponent(int[] bases, int[] exponents) {
     if (!(bases.length == exponents.length)) {
       throw new IllegalArgumentException();
     }
     this.bases = bases;
     this.exponents = exponents;
   }
 
   public int numTerms() {
     return this.bases.length;
   }
 
   private static String checkNext(Parser parser, Pattern pattern, String expected) {
     String str;
     if ((str = parser.next(pattern)) == null) {
      throw new IllegalArgumentException("expected " + expected + ", got '" + parser.rest() + "'");
     }
     return str;
   }
 
   public static BaseExponent fromString(String input) {
     Parser parser = new Parser(input);
     ArrayList<Integer> bases = new ArrayList<Integer>();
     ArrayList<Integer> exponents = new ArrayList<Integer>();
 
     if (parser.isEnd()) {
       throw new IllegalArgumentException("empty input");
     } else {
       while (!parser.isEnd()) {
         checkNext(parser, RE_LEFT_PAREN, "'('");
         checkNext(parser, RE_X_UNDERSCORE, "'x_'");
         String baseStr = checkNext(parser, RE_BASE, "base");
         checkNext(parser, RE_CARET, "'^'");
         String exponentStr = checkNext(parser, RE_EXPONENT, "exponent");
         checkNext(parser, RE_RIGHT_PAREN, "')'");
         bases.add(Integer.valueOf(baseStr));
         exponents.add(Integer.valueOf(exponentStr));
       }
     }
     return new BaseExponent(Util.toIntArray(bases),
                             Util.toIntArray(exponents));
   }
 
   public String toString() {
     StringBuilder buf = new StringBuilder();
     for (int i = 0; i < this.numTerms(); i++) {
       int base = this.bases[i];
       int exponent = this.exponents[i];
       buf.append("(x_" + base + "^" + exponent + ")");
     }
     return buf.toString();
   }
   
   public boolean equals(Object obj) {
     if (!(obj instanceof BaseExponent)) {
       return false;
     } else {
       BaseExponent be = (BaseExponent) obj;
       return (Arrays.equals(this.bases, be.bases) &&
               Arrays.equals(this.exponents, be.exponents));
     }
   }
   
   private BaseExponent coalesce() {
     int[] coalBases = new int[this.numTerms()];
     int[] coalExponents = new int[this.numTerms()];
     int i = 0;
     int j = 0;
     while (i < this.numTerms()) {
       if (this.exponents[i] == 0) {
         i++;
       } else {
         int coalBase = this.bases[i];
         int coalExponent = this.exponents[i];
         i++;
         while ((i < this.numTerms()) && (this.bases[i] == coalBase)) {
           if (!(this.exponents[i] == 0)) {
             coalExponent += this.exponents[i];
           }
           i++;
         }
         if (coalExponent != 0) {
           coalBases[j] = coalBase;
           coalExponents[j] = coalExponent;
           j++;
         }
       }
     }
     int[] coalBasesTight = new int[j];
     int[] coalExponentsTight = new int[j];
     System.arraycopy(coalBases, 0, coalBasesTight, 0, j);
     System.arraycopy(coalExponents, 0, coalExponentsTight, 0, j);
     return new BaseExponent(coalBasesTight, coalExponentsTight);
   }
   
   public BaseExponent toNormalForm() {
     int[] bases = Arrays.copyOf(this.bases, this.bases.length);
     int[] exponents = Arrays.copyOf(this.exponents, this.exponents.length);
       
     // for i < j, a positive, b anything
     //   (x_j^b)(x_i^a) = (x_i^a)(x_j+a^b)      shuffle x_i^a to left   (inc j base by i exp)
     //   (x_i^-a)(x_j^b) = (x_j+a^b)(x_i^-a)    shuffle x_i^-a to right (inc j base by i exp abs)
     boolean needsShuffle = true;
     while (needsShuffle) {
       needsShuffle = false;
       // find term to shuffle
       for (int i = 0; i < bases.length - 1; i++) {
         int leftBase = bases[i];
         int leftExponent = exponents[i];
         int rightBase = bases[i+1];
         int rightExponent = exponents[i+1];
         if ((leftExponent == 0) || (rightExponent == 0)) {
           // =~ 1 term, no shuffle
         } else if (leftBase == rightBase) {
           // need to coalesce these
           BaseExponent coal = new BaseExponent(bases, exponents).coalesce();
           bases = coal.bases;
           exponents = coal.exponents;
           needsShuffle = true;
           break;
         } else if ((leftExponent > 0) && (rightExponent < 0)) {
           // pos->neg transition, no shuffle
         } else if ((leftExponent < 0) && (rightExponent > 0)) {
           // neg->pos transition
           if (leftBase < rightBase) {
             // shuffle x_i^-a to right
             bases[i+1] = leftBase;
             exponents[i+1] = leftExponent;
             bases[i] = rightBase - leftExponent;
             exponents[i] = rightExponent;
           } else {
             // shuffle x_i^a to left
             bases[i] = rightBase;
             exponents[i] = rightExponent;
             bases[i+1] = leftBase + rightExponent;
             exponents[i+1] = leftExponent;
           }
           needsShuffle = true;
           break;
         } else if ((leftExponent < 0) && (rightExponent < 0)) {
           if (leftBase > rightBase) {
             // desc neg, no shuffle
           } else {
             // asc neg, shuffle x_i^-a to right
             bases[i+1] = leftBase;
             exponents[i+1] = leftExponent;
             bases[i] = rightBase - leftExponent;
             exponents[i] = rightExponent;
             needsShuffle = true;
             break;
           }
         } else if ((leftExponent > 0) && (rightExponent > 0)) {
           if (leftBase < rightBase) {
             // asc pos, no shuffle
           } else {
             // desc pos, shuffle x_i^a to left
             bases[i] = rightBase;
             exponents[i] = rightExponent;
             bases[i+1] = leftBase + rightExponent;
             exponents[i+1] = leftExponent;
             needsShuffle = true;
             break;
           }
         } else {
           throw new RuntimeException("unreachable: " + leftExponent + ", " + rightExponent);
         }
       }
     }
     return new BaseExponent(bases, exponents).coalesce();
   }
   
   private static int[][] splitTerms(BaseExponent be) {
     int firstNI = be.numTerms();
     for (int i = (be.numTerms() - 1); i >= 0; i--) {
       if (be.exponents[i] < 0) {
         firstNI = i;
       }
     }
 
     int pLength = firstNI;
     int nLength = be.numTerms() - firstNI;
     int[] pBases =     new int[pLength];
     int[] pExponents = new int[pLength];
     int[] nBases =     new int[nLength];
     int[] nExponents = new int[nLength];
     for (int i = 0; i < pLength; i++) {
       pBases[i]     = be.bases[firstNI - 1 - i]; 
       pExponents[i] = be.exponents[firstNI - 1 - i];
     }
     for (int i = 0; i < nLength; i++) {
       nBases[i] =     be.bases[firstNI + i];
       nExponents[i] = be.exponents[firstNI + i];
     }
     
     int[][] ret = new int[4][];
     ret[0] = pBases; ret[1] = pExponents; ret[2] = nBases; ret[3] = nExponents;
     return ret;
   }
   
   public BaseExponent toUniqueNormalForm() {
     // assume we are working from normal form
     if (!this.isNormalForm()) { throw new IllegalArgumentException(this.toString()); }
     
     int[][] ret = splitTerms(this);
     int[] pBases =     ret[0];
     int[] pExponents = ret[1];
     int[] nBases =     ret[2];
     int[] nExponents = ret[3];
     int pLength = pBases.length;
     int nLength = nBases.length;
     
     // uniqueify mirror terms as neccessary
     int pAt = 0;
     int nAt = 0;
     while ((pAt < pLength) && (nAt < nLength)) {
       if (pBases[pAt] == nBases[nAt]) {
         // {p,n}Gap is number of moves needed to get an x_i+1 adjacent
         int pGap = (pAt == 0) ? Integer.MAX_VALUE :
                                 pBases[pAt-1] - (pBases[pAt] + 1);
         int nGap = (nAt == 0) ? Integer.MAX_VALUE :
                                 nBases[nAt-1] - (nBases[nAt] + 1);
         if ((pGap == 0) || (nGap == 0)) {
           // unique condition met
           pAt++;
           nAt++;
         } else {
           // {p,n}Gap2 is number of moves needed to eliminate x_i on >=1 side
           int pGap2 = pExponents[pAt];
           int nGap2 = -nExponents[nAt];
           int minGap = Math.min(pGap, Math.min(nGap, Math.min(pGap2, nGap2)));
           // decrement middle exponents regardless of whether eliminating or not
           for (int i = 0; i < pAt; i++) {
             pBases[i] -= minGap;
           }
           for (int i = 0; i < nAt; i++) {
             nBases[i] -= minGap;
           } 
           // consider elimination
           if (pGap2 == minGap) {
             for (int i = (pAt + 1); i < pLength; i++) {
               pBases[i-1] = pBases[i];
               pExponents[i-1] = pExponents[i];
             }
             pLength--;
           } else {
             pExponents[pAt] -= minGap;
             pAt++;
           }
           if (nGap2 == minGap) {
             for (int i = (nAt + 1); i < nLength; i++) {
               nBases[i-1] = nBases[i];
               nExponents[i-1] = nExponents[i];
             }
             nLength--;
           } else {
             nExponents[nAt] += minGap;
             nAt++;
           }
         }
       } else if (pBases[pAt] > nBases[nAt]) {
         pAt++;
       } else if (pBases[pAt] < nBases[nAt]) {
         nAt++;
       } else {
         throw new RuntimeException();
       }
     }
     
     // pack remaining terms into unified arrays
     int[] bases =     new int[pLength + nLength];
     int[] exponents = new int[pLength + nLength];
     for (int i = 0; i < pLength; i++) {
       bases[i]     = pBases[pLength - 1 - i];
       exponents[i] = pExponents[pLength - 1 - i];
     }
     for (int i = 0; i < nLength; i++) {
       bases[pLength + i] = nBases[i];
       exponents[pLength + i] = nExponents[i];
     }
     return new BaseExponent(bases, exponents).coalesce();
   }
 
   // Returns true of this instance is in normal form. We consider the general
   // normal form, not the unique normal form. See isUnique.
   public boolean isNormalForm() {
     for (int i = 0; i < (this.numTerms() - 1); i++) {
       int prevBase = this.bases[i];
       int prevExponent = this.exponents[i];
       int nextBase = this.bases[i+1];
       int nextExponent = this.exponents[i+1];
       if ((prevExponent > 0) && (nextExponent > 0)) {
         if (!(nextBase > prevBase)) { return false; }
       } else if ((prevExponent < 0) && (nextExponent < 0)) {
         if (!(nextBase < prevBase)) { return false; }
       } else if ((prevExponent > 0) && (nextExponent < 0)) {
         // ok
       } else {
         return false;
       }
       prevBase = nextBase;
       prevExponent = nextExponent;
     }
     return true;
   }
   
   // Returns true if this in unique normal form. See isNormalForm.
   public boolean isUniqueNormalForm() {
     if (!this.isNormalForm()) {
       return false;
     } else {
       return (this.toUniqueNormalForm().equals(this));
     }
   }
 
   // Returns a TreePair corresponding to this instance.
   // OPTIMIZE: linear tree construction
   public TreePair toTreePair() {
     TreePair[] factors = new TreePair[this.numTerms()];
     for (int i = 0; i < this.numTerms(); i++) {
       factors[i] = TreePair.fromTerm(this.bases[i], this.exponents[i]);
     }
     return TreePair.product(factors);
   }
   
   // Returns the inverse of this element
   public BaseExponent invert() {
     int[] bases = new int[this.numTerms()];
     int[] exponents = new int[this.numTerms()];
     for (int i = 0; i < this.numTerms(); i++) {
       bases[this.numTerms() - i - 1] = this.bases[i];
       exponents[this.numTerms() - i - 1] = -this.exponents[i];
     }
     return new BaseExponent(bases, exponents);
   }
 
   // Returns fg in normal form
   public static BaseExponent multiply(BaseExponent f, BaseExponent g) {
     BaseExponent[] factors = {f, g};
     return product(factors);
   }
   
   // Returns the product of the given factors, in normal form.
   public static BaseExponent product(BaseExponent[] factors) {
     int totalTerms = 0;
     for (BaseExponent factor : factors) {
       totalTerms += factor.numTerms();
     }
     int[] bases = new int[totalTerms];
     int[] exponents = new int[totalTerms];
     int i = 0;
     for (BaseExponent factor : factors) {
       System.arraycopy(factor.bases,     0, bases,     i, factor.numTerms());
       System.arraycopy(factor.exponents, 0, exponents, i, factor.numTerms());
       i += factor.numTerms();
     }
     return new BaseExponent(bases, exponents).toNormalForm().toUniqueNormalForm();
   }
 }
