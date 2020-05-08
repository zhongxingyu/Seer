 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package cz.cvut.keyczar.homework;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author pborky
  */
 public class HmacCracker implements Runnable {
 
     private final OutputStream outputStream;
     private final InputStream inputStream;
     byte[] message;
     int hmacPtr = 0;
     byte[] hmac;
     int maxIterations;
     int totalIterations = 0;
 
     public HmacCracker(OutputStream outputStream, InputStream inputStream, int iMax, byte[] signature, char[] msg) {
 		this.outputStream = outputStream;
         this.inputStream =  inputStream;
         try {
             for (int r=this.inputStream.read(); r!='\n'; r=this.inputStream.read()) { }
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
         this.message = new byte[msg.length];
         for (int i=0; i<msg.length; i++) {
             this.message[i] = (byte)msg[i];
         }
         this.hmac = new byte[26];
         Arrays.fill(this.hmac, (byte)0xff);
         this.hmac[this.hmacPtr++] = 0; // separator
         
         maxIterations = iMax;
         if (null != signature) {
             if (signature.length > 25) {
                 throw new IllegalArgumentException("You may give up to 25 HMAC digits.");
             }
             System.arraycopy(signature, 0, this.hmac, 1, signature.length);
             hmacPtr += signature.length;
             if (hmacPtr > this.hmac.length) {
                 hmacPtr = this.hmac.length - 1;
             }
         }
     }
 
     public void run() {
         try {
             dfs(hmac, hmacPtr);
         } catch (Success s) {
             System.out.println("\nSuccess in "+totalIterations+" iterations!");
             System.out.print("HMAC: ");
             System.out.println(bytesToHexStr(s.getHMAC()));
             return;
         }
         System.out.println("\nFailed to find HMAC! Try raise iteration limit.");
     }
     
     private boolean dfs(byte[] hmac, int ptr) {
        int[] guessed = null;
         if (ptr <= this.hmac.length) {
             guessed = guessNext(message, hmac, ptr, maxIterations, inputStream, outputStream);
             if (guessed==null) {
                 System.out.println("\nDead end!");
                 return false;
             }
             System.out.print("\nPossible choices: ");
             System.out.println(bytesToHexStr(guessed));
             for (int g: guessed) {
                 System.out.print("\nGuessing: ");
                 System.out.println(Integer.toHexString((0xff & g)));
                 byte[] hm = Arrays.copyOf(hmac, hmac.length);
                 hm[ptr] = (byte) (0xff & g);
                 if (dfs(hm,ptr+1)) {
                     return true;
                 }
             }
         }
         return false;
     }
 
    private int[] guessNext(byte[] msg, byte[] hmac, int ptr, int maxIt, InputStream is, OutputStream os) {
 
         List<long[]> statsPool = new ArrayList<long[]>();
 
         System.out.print("\nHMAC: ");
         System.out.println(bytesToHexStr(hmac));
         System.out.print("Cracking");
         int bin = 4;
         
         while (maxIt>0) {
             for (int j = 0 ; j < bin; j++ ) {
                 long[] stats = new long[256];
 
                 for (int b = 0; b<256; b++) {
                     if (ptr < hmac.length) {
                         hmac[ptr] = (byte)(0xff&b);
                     }
 
                     int resp =  sendData(b, is, os, msg, hmac, stats);
                     if (resp == 'N') {
                         //
                     } else if (resp == 'O') {
                         throw new Success(hmac);
                     } else {
                         throw new UnexpectedContent();
                     }
                     totalIterations++;
                 }
                 System.out.write('.');System.out.flush();
                 statsPool.add(Arrays.copyOf(stats, stats.length));
                 maxIt--;
             }
            int[] bestM = CrackerCore.select(statsPool);
             if (bestM == null) {
                 bin = bin*2;
                 System.out.print("#"); // we need to collect more stats
                 continue;
             }
             return bestM;
         }
         return null;
     }
 
     private String bytesToHexStr(byte[] hmac) {
         if (hmac==null) return "";
         StringBuilder sb = new StringBuilder();
         for (byte x:hmac) {
             sb.append(Integer.toHexString(0xff&x)).append(' ');
         }
         return sb.toString();
     }
     private String bytesToHexStr(int[] hmac) {
         if (hmac==null) return "";
         StringBuilder sb = new StringBuilder();
         for (int x:hmac) {
             sb.append(Integer.toHexString(0xff&x)).append(' ');
         }
         return sb.toString();
     }
 
 
     private int sendData(int b, InputStream is, OutputStream os, byte[] msg, byte[] hmac, long[] stats) {
         try { //[101, 99, 104, 111, 95, 97, 104, 111, 106, 0, 0, 215, 137, 187, 50, 234, 124, 87, 103, 149, 169, 195, 50, 10, 79, 183, 36, 166, 199, 0, 11, 157, 207, 34, 228]
             os.write(msg);            
             os.write(hmac);
             long start,stop;
             start = System.nanoTime();
             os.flush();
             int s = is.read();
             stop = System.nanoTime();
             for (int r=is.read(); r!='\n'; r=is.read()) { }
             if (stats != null) {
                 stats[b] = stop - start;
             }            
             return s;
         } catch (IOException ex) {
             Logger.getLogger(HmacCracker.class.getName()).log(Level.SEVERE, null, ex);
             throw new RuntimeException(ex);
         }
     }
 
     private class Success extends RuntimeException {
         private byte [] hmac;
         public Success(byte[] hmac) {
             super(bytesToHexStr(hmac));
             this.hmac = hmac;
         }
         public byte[] getHMAC() {
             return this.hmac;
         }
     }
 
     private class UnexpectedContent extends RuntimeException {
         public UnexpectedContent() {
             super();
         }
         public UnexpectedContent(String msg) {
             super(msg);
         }
     }
 }
