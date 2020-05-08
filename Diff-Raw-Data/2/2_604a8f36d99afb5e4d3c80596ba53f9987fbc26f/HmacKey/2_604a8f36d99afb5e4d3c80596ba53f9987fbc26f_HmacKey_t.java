 /*
  * Copyright 2008 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package cz.cvut.keyczar;
 
 import com.google.gson.annotations.Expose;
 import cz.cvut.keyczar.enums.KeyType;
 import cz.cvut.keyczar.exceptions.KeyczarException;
 import cz.cvut.keyczar.interfaces.SigningStream;
 import cz.cvut.keyczar.interfaces.Stream;
 import cz.cvut.keyczar.interfaces.VerifyingStream;
 import cz.cvut.keyczar.util.Base64Coder;
 import cz.cvut.keyczar.util.Util;
 
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;
 import java.nio.ByteBuffer;
 import java.security.GeneralSecurityException;
 import java.security.Key;
 
 /**
  * Wrapping class for HMAC-SHA1 keys
  *
  * @author steveweis@gmail.com (Steve Weis)
  * @author arkajit.dey@gmail.com (Arkajit Dey)
  *
  */
 public class HmacKey extends KeyczarKey {
   private static final String MAC_ALGORITHM = "HMACSHA1";
 
   @Expose private String hmacKeyString;
 
   private Key hmacKey;
   private byte[] hash = new byte[Keyczar.KEY_HASH_SIZE];
 
   static HmacKey generate() throws KeyczarException {
     return generate(KeyType.HMAC_SHA1.defaultSize());
   }
 
   static HmacKey generate(int keySize) throws KeyczarException {
     HmacKey key = new HmacKey();
     key.size = keySize;
     byte[] keyBytes = Util.rand(key.size() / 8);
     key.hmacKeyString = Base64Coder.encode(keyBytes);
     key.init();
     return key;
   }
 
   void init() throws KeyczarException {
     byte[] keyBytes = Base64Coder.decode(hmacKeyString);
     byte[] fullHash = Util.hash(keyBytes);
     System.arraycopy(fullHash, 0, hash, 0, hash.length);
     hmacKey = new SecretKeySpec(keyBytes, MAC_ALGORITHM);
   }
 
   /*
    * This method is for AesKey to grab the key bytes to compute an identifying
    * hash.
    */
   byte[] keyBytes() {
     return hmacKey.getEncoded();
   }
 
   @Override
   Stream getStream() throws KeyczarException {
     return new HmacStream();
   }
 
   @Override
   KeyType getType() {
     return KeyType.HMAC_SHA1;
   }
 
   @Override
   byte[] hash() {
     return hash;
   }
 
   static HmacKey read(String input) throws KeyczarException {
     HmacKey key = Util.gson().fromJson(input, HmacKey.class);
     key.init();
     return key;
   }
 
   private class HmacStream implements VerifyingStream, SigningStream {
     private Mac hmac;
       private long milis = 0;
       private int nanos = 0;
       private boolean delay = false;
 
       public HmacStream() throws KeyczarException {
 
       if ((System.getProperty("milis") != null) && (System.getProperty("nanos") != null)) {
           this.milis = Long.parseLong(System.getProperty("milis"));
           this.nanos = Integer.parseInt(System.getProperty("nanos"));
          if (milis > 0 || nanos > 0) delay = true;
       }
       if (delay) System.out.println("Delay setting: milis="+milis+", nanos="+nanos);
       else System.out.println("Delay setting: none");
       
       try {
         hmac = Mac.getInstance(MAC_ALGORITHM);
       } catch (GeneralSecurityException e) {
         throw new KeyczarException(e);
       }
     }
 
     public int digestSize() {
       return getType().getOutputSize();
     }
 
     public void initSign() throws KeyczarException {
       try {
         hmac.init(hmacKey);
       } catch (GeneralSecurityException e) {
         throw new KeyczarException(e);
       }
     }
 
     public void initVerify() throws KeyczarException {
       initSign();
     }
 
     public void sign(ByteBuffer output) {
       output.put(hmac.doFinal());
     }
 
     public void updateSign(ByteBuffer input) {
       hmac.update(input);
     }
 
     public void updateVerify(ByteBuffer input) {
       updateSign(input);
     }
 
     public boolean verify(ByteBuffer signature) {
       byte[] sigBytes = new byte[signature.remaining()];
       signature.get(sigBytes);
       byte[] macResult = hmac.doFinal();
 
       if (macResult.length != sigBytes.length) {
 	      return false;
       }
       for (int pos = 0; pos < macResult.length; pos++) {
         if (macResult[pos] != sigBytes[pos]) {
 	      return false;
         }
         if (delay) {
           try { Thread.sleep(milis, nanos); } catch (InterruptedException e) {}
         }
 
       }
       return true;
     }
   }
 }
