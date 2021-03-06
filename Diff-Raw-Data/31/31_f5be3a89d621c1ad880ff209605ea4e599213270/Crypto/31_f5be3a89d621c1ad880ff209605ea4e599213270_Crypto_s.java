 /*
  *  This file is part of SWADroid.
  *
  *  Copyright (C) 2010 Juan Miguel Boyero Corral <juanmi1982@gmail.com>
  *
  *  SWADroid is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  SWADroid is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with SWADroid.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package es.ugr.swad.swadroid.utils;
 
 import java.security.MessageDigest;
 import java.security.SecureRandom;
 import javax.crypto.Cipher;
 import javax.crypto.KeyGenerator;
 import javax.crypto.SecretKey;
 import javax.crypto.spec.SecretKeySpec;
 
 /**
 * Cryptographic class for encription purposes.
  * @author Juan Miguel Boyero Corral <juanmi1982@gmail.com>
  */
 public class Crypto
 {
     public static String encrypt(String seed, String cleartext)
     {
         try
         {
            byte[] rawKey = getRawKey(seed.getBytes());
            byte[] result = encrypt(rawKey, cleartext.getBytes());
             return Base64.encodeBytes(result);
         }
         catch(Exception e)
         {
             e.printStackTrace();
         }
         return "error";
     }
 
     public static String decrypt(String seed, String encrypted)
     {
         try
         {
            byte[] rawKey = getRawKey(seed.getBytes());
             byte[] enc = Base64.decode(encrypted);
             byte[] result = decrypt(rawKey, enc);
            return new String(result);
         }
         catch(Exception e)
         {
             e.printStackTrace();
         }
         return "error";
     }
 
     private static byte[] getRawKey(byte[] seed) throws Exception
     {
         KeyGenerator kgen = KeyGenerator.getInstance("AES");
         SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
         sr.setSeed(seed);
         kgen.init(128, sr);
         SecretKey skey = kgen.generateKey();
         byte[] raw = skey.getEncoded();
         return raw;
     }
 
     private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception
     {
         SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
         Cipher cipher = Cipher.getInstance("AES");
         cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
         byte[] encrypted = cipher.doFinal(clear);
         return encrypted;
     }
 
     private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception
     {
         SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
         Cipher cipher = Cipher.getInstance("AES");
         cipher.init(Cipher.DECRYPT_MODE, skeySpec);
         byte[] decrypted = cipher.doFinal(encrypted);
         return decrypted;
     }
     
     public static final String md5(final String s) 
     {
         try
         {
             MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
             byte messageDigest[] = digest.digest();
 
             StringBuffer hexString = new StringBuffer();
             for(int i=0; i<messageDigest.length; i++)
             {
                 String h = Integer.toHexString(0xFF & messageDigest[i]);
                 while(h.length()<2)
                 {
                     h="0"+h;
                 }
                 hexString.append(h);
             }
             return hexString.toString();
         }
         catch(Exception e)
         {
             e.printStackTrace();
         }
         return "error";
     }
 }
