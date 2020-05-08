 import java.lang.Math;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.concurrent.Callable;
 import java.util.Random;
 
 
 public class HashWorker implements Callable<HashResult> {
     private Random random;
     private HashResult localMaxima;
     private byte[] target;
     private MessageDigest md;
     private int maxLength;
     private int reset;
 
     public HashWorker(Random initialRandom,
             String algorithm,
             int maxLength,
             byte[] target, 
             int reset) throws NoSuchAlgorithmException {
         this.random = new Random(initialRandom.nextLong());
         this.md = MessageDigest.getInstance(algorithm);
         this.target = target;
         this.maxLength = maxLength;
         this.reset = reset;
     }
 
     public HashResult call() {
         for (int reset = this.reset; reset > 0; reset--) {
             hashNext();
         }
         return localMaxima;
     }
 
     private void hashNext() { 
         int bufLength = Math.abs(this.random.nextInt() % maxLength);
         byte[] buf = new byte[bufLength];
         this.random.nextBytes(buf);
         this.md.reset();
         this.md.update(buf);
         byte[] digest = this.md.digest();
         int distance = hammingDistance(digest, target);
         if (localMaxima == null || distance > localMaxima.getDistance()) {
             localMaxima = new HashResult(distance, buf);
         }
     }
 
     private int hammingDistance(byte[] b1, byte[] b2) {
         assert b1.length == b2.length : "Cannot compute hamming distance";
         int result = 0;
         for (int i = 0; i < b1.length; i++){
             result += hammingDistance(b1[i], b2[i]);
         }
         return result;
     }
 
     private int hammingDistance(byte b1, byte b2) {
         int result = 0;
        result += (b1 & 0x1) ^ (b2 & 0x1);
        result += (b1 & 0x2) ^ (b2 & 0x2);
        result += (b1 & 0x4) ^ (b2 & 0x4);
        result += (b1 & 0x8) ^ (b2 & 0x8);
        result += (b1 & 0x10) ^ (b2 & 0x10);
        result += (b1 & 0x20) ^ (b2 & 0x20);
        result += (b1 & 0x40) ^ (b2 & 0x40);
        result += (b1 & 0x80) ^ (b2 & 0x80);
         return result;
     }
 }
 
 
 
