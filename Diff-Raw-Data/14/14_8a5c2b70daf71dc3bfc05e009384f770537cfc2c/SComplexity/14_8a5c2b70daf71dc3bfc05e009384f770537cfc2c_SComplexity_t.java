 package org.kornicameister.crypto.schnorr;
 
 /**
  * @author kornicameister
  * @since 0.0.1
  */
 public enum SComplexity {
     S_320(1024, 160),
     S_448(2048, 224),
     S_512(3072, 256);
 
     private final int pBitLength;
     private final int qBitLength;
 
     SComplexity(int i, int i1) {
         this.pBitLength = i;
        this.qBitLength = i1;
     }
 
     public int getPBitLength() {
         return pBitLength;
     }
 
     public int getQBitLength() {
         return qBitLength;
     }
 }
