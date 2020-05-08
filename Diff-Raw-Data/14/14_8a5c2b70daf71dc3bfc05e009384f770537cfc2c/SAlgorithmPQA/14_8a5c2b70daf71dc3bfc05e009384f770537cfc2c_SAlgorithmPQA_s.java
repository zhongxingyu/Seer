 package org.kornicameister.crypto.schnorr;
 
 import org.apache.log4j.Logger;
 import org.kornicameister.crypto.utils.MathUtils;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.util.Properties;
 import java.util.Random;
 
 /**
  * @author kornicameister
  */
 public class SAlgorithmPQA {
     private final static Logger LOGGER = Logger.getLogger(SAlgorithmPQA.class);
     private static final int RADIX = 16;
     private final Random randomSeed;
     private final SComplexity complexity;
     private BigInteger p, q, a;
     private int certainty;
 
     private SAlgorithmPQA(SComplexity complexity) {
         this.p = null;
         this.q = null;
         this.a = null;
 
         this.complexity = complexity;
         this.certainty = 10;
 
         this.randomSeed = new Random(System.nanoTime());
 
         LOGGER.info(String.format("%s init variables pBitLength=%d, qBitLength=%d, certainty=%d",
                 SAlgorithmPQA.class.getSimpleName(),
                 this.complexity.getPBitLength(),
                 this.complexity.getQBitLength(),
                 this.certainty)
         );
     }
 
     public static SAlgorithmPQA generate(SComplexity complexity) {
         SAlgorithmPQA sAlgorithm = new SAlgorithmPQA(complexity);
 
         BigInteger q = generateQFactor(
                 sAlgorithm,
                 BigInteger.probablePrime(
                        sAlgorithm.complexity.getPBitLength(),
                         sAlgorithm.randomSeed
                 )
         );
         BigInteger p = generatePFactor(
                 sAlgorithm,
                 q
         );
         BigInteger a = generateAFactor(
                 sAlgorithm,
                 p,
                 q
         );
 
         if (q.equals(q.mod(p))) {
             sAlgorithm.q = q;
             sAlgorithm.p = p;
             sAlgorithm.a = a;
 
             LOGGER.info(String.format("Primers [p\n,q\n,a\n]=[%s\n,%s\n,%s\n]",
                     p.toString(RADIX),
                     q.toString(RADIX),
                     a.toString(RADIX))
             );
 
             return sAlgorithm;
         }
 
         return null;
     }
 
     private static BigInteger generateAFactor(SAlgorithmPQA sAlgorithm, BigInteger p, BigInteger q) {
         BigInteger a, h = MathUtils.random(p.subtract(BigInteger.ONE), sAlgorithm.randomSeed);
 //        BigInteger a, h = MathUtils.INTEGER_2;
         while (true) {
             a = MathUtils.powModFast(h, p.subtract(BigInteger.ONE).divide(q), p);
             if (a.compareTo(BigInteger.ONE) != 0) {
                 LOGGER.info(String.format("Found a\na=%s", a.toString(RADIX)));
                 sAlgorithm.a = a;
                 break;
             }
             h = h.add(BigInteger.ONE);
         }
         return a;
     }
 
     private static BigInteger generatePFactor(SAlgorithmPQA sAlgorithm, BigInteger q) {
         BigInteger m;
         BigInteger mR;
         BigInteger p;
         while (true) {
             m = new BigInteger(sAlgorithm.complexity.getPBitLength(), sAlgorithm.randomSeed);
            mR = m.mod(q.multiply(org.kornicameister.crypto.utils.MathUtils.INTEGER_2));
            p = m.subtract(mR).subtract(BigInteger.ONE);
             if (p.isProbablePrime(sAlgorithm.certainty)) {
                 LOGGER.info(String.format("Found p being prime\np=%s", p.toString(RADIX)));
                 break;
             }
         }
         return p;
     }
 
     private static BigInteger generateQFactor(SAlgorithmPQA sAlgorithm, BigInteger q) {
         while (!q.isProbablePrime(sAlgorithm.certainty)) {
             q = q.add(BigInteger.ONE);
         }
         LOGGER.info(String.format("Found q being prime\nq=%s", q.toString(RADIX)));
         return q;
     }
 
     public void toProperties(String pFile) throws IOException {
         Properties properties = new Properties();
         properties.load(new FileInputStream(pFile));
 
         LOGGER.info("Saving [p,g,a] to properties");
 
         properties.setProperty("qNumber", this.q.toString(RADIX));
         properties.setProperty("pNumber", this.p.toString(RADIX));
         properties.setProperty("aNumber", this.a.toString(RADIX));
 
         properties.store(new FileOutputStream(pFile), "Prime numbers generated");
     }
 
     public void fromProperties(String pFile) throws IOException {
         Properties properties = new Properties();
         properties.load(new FileInputStream(pFile));
 
         LOGGER.info("Retrieving [p,g] from properties");
 
         this.q = new BigInteger(properties.getProperty("qNumber"), RADIX);
         this.p = new BigInteger(properties.getProperty("pNumber"), RADIX);
         this.a = new BigInteger(properties.getProperty("aNumber"), RADIX);
 
         LOGGER.info(String.format("Loaded [p,q,a]=[%s,%s,%s]",
                 p.toString(RADIX),
                 q.toString(RADIX),
                 a.toString(RADIX))
         );
     }
 
     public static SAlgorithmPQA loadFromProperties(SComplexity complexity, String pFile) throws IOException {
         SAlgorithmPQA pqa = new SAlgorithmPQA(complexity);
         pqa.fromProperties(pFile);
         return pqa;
     }
 
     public BigInteger getP() {
         return p;
     }
 
     public BigInteger getQ() {
         return q;
     }
 
     public BigInteger getA() {
         return a;
     }
 
     public SComplexity getComplexity() {
         return complexity;
     }
 }
