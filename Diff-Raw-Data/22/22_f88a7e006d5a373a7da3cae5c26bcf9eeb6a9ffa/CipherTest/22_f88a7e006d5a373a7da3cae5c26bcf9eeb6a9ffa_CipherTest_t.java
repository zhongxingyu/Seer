 import org.bouncycastle.crypto.modes.CBCBlockCipher;
 import org.bouncycastle.crypto.engines.AESEngine;
 import org.bouncycastle.crypto.params.ParametersWithIV;
 import org.bouncycastle.crypto.params.KeyParameter;
 import java.util.Arrays;
 import java.security.SecureRandom;
 import org.bouncycastle.util.encoders.Hex;
 
 class CipherTest {
   static byte[] key = "abcdefhijklmnopq".getBytes();
 
   public static void main(String[] args) {
     byte[] input = "This is a test. Under 32.".getBytes();
 
     byte[] padded = nullPad(input);
 
     byte[] encrypted = encryptTest(padded);
     byte[] decrypted = decryptTest(encrypted);
 
     byte[] unpadded = unPad(decrypted);
 
     if(Arrays.equals(input,unpadded)) {
       System.out.println("Success!");
     }
     else {
       System.out.println(new String(unpadded));
     }
   }
 
   // not needed anymore
   static byte[] concat(byte[] left, byte[] right) {
     byte[] out = new byte[left.length + right.length];
     System.arraycopy(left, 0, out, 0, left.length);
     System.arraycopy(right, 0, out, left.length, right.length);
 
     return out;
   }
 
  static byte[] nullPad(byte[] in) throws RuntimeException {
     if(32 < in.length) {
      throw new RuntimeException("we don't support length > 32");
     }
 
    return Arrays.copyOf(in, 32);
   }
 
   static byte[] unPad(byte[] in) {
     // assumes input has no null bytes
     int i = findIndex(in, (byte) 0);
    if(i == -1) {
      return in;
    }
 
    return Arrays.copyOf(in, i);
   }
 
   static int findIndex(byte[] array, byte elt) {
    for(int i = 0; i < array.length; i++) {
      if(array[i] == elt) {
         return i;
       }
     }
    return -1;
   }
 
 
   static byte[] encryptTest(byte[] in) {
   // use this, or Buffered / Padded cipher?
     CBCBlockCipher cipher = new CBCBlockCipher(new AESEngine());
 
     byte[] out = new byte[32];
 
     // we need it to be deterministic, so use default 0 IV
     cipher.init(true, new KeyParameter(key));
     cipher.processBlock(in, 0, out, 0);
     cipher.processBlock(in, 16, out, 16);
 
     return out;
   }
 
 
   static byte[] decryptTest(byte[] in) {
     CBCBlockCipher cipher = new CBCBlockCipher(new AESEngine());
 
     byte[] out = new byte[32];
 
     cipher.init(false, new KeyParameter(key));
     cipher.processBlock(in, 0, out, 0);
     cipher.processBlock(in, 16, out, 16);
 
     return out;
   }
 }
