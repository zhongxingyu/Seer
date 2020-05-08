 /*
  * Jamm
 * Copyright (C) 2004 Dave Dribin and Keith Garner
  *  
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
/*
 * Portions Copyright (C) 2004 Dave Terrell
 */

 package jamm.ldap;
 
 import java.io.ByteArrayOutputStream;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 /**
  * Provides utility methods for the different LDAP password hashing
  * schemes.
  *
  * @see PasswordScheme
  */
 public abstract class LdapPassword
 {
     /**
      * Hashes a password using a given scheme.
      *
      * @param scheme Password scheme to use
      * @param password Clear test password
      * @return Hashed password
      */
     public static String hash(PasswordScheme scheme, String password)
     {
         LdapPassword    method;
 
         if (mSchemeLookup == null)
         {
             initSchemeLookup();
         }
         
         method = (LdapPassword) mSchemeLookup.get(scheme);
         if (method == null)
         {
             method = new ClearPassword();
         }
 
         return method.doHash(password);
     }
 
     /**
      * Initializes the scheme lookup table.  Should be called only
      * once.
      */
     private static void initSchemeLookup()
     {
         mSchemeLookup = new HashMap();
 //        mSchemeLookup.put(PasswordScheme.CRYPT_SCHEME,
 //                          new CryptPassword());
         mSchemeLookup.put(PasswordScheme.MD5_SCHEME,
                           new Md5Password());
         mSchemeLookup.put(PasswordScheme.SHA_SCHEME,
                           new ShaPassword());
         mSchemeLookup.put(PasswordScheme.SSHA_SCHEME,
                           new SaltedShaPassword());
     }
 
     /**
      * Checks a hashed password against a clear text password.  This
      * is not yet implemented.
      *
      * @param hashedPassword The hashed password
      * @param password The clear text password
      * @return Always returns <code>false</code>
      */
     public static boolean check(String hashedPassword, String password)
     {
         return false;
     }
 
     /**
      * Sets the random class to use for all future uses of random
      * numbers.  By default, it uses
      * <code>java.security.SecureRandom</code>, but any subclass of
      * <code>Random</code> will work.
      *
      * @param randomClass Class name of the random class.
      *
      * @see java.util.Random
      * @see java.security.SecureRandom
      */
     public static void setRandomClass(String randomClass)
     {
         mRandomClass = randomClass;
     }
 
     /**
      * Creates a new random class using the supplied class name.
      *
      * @return A new instance of a random number generator.
      */
     protected static Random createRandom()
     {
         Random  random;
 
         try
         {
             random = (Random) Class.forName(mRandomClass).newInstance();
         }
         catch (ClassNotFoundException e)
         {
             random = new Random();
         }
         catch (InstantiationException e)
         {
             random = new Random();
         }
         catch (IllegalAccessException e)
         {
             random = new Random();
         }
 
         return random;
     }
 
     /**
      * Hashes a clear text password.  Should be overridden by
      * subclasses for each password scheme.
      *
      * @param password A clear text password
      * @return A hashed password
      */
     protected abstract String doHash(String password);
 
     /**
      * Checks a hashed password against a clear text password.  Should
      * be overridden by subclasses for each password scheme.
      *
      * @param hashedPassword A hashed password
      * @param password A clear text password
      * @return A hashed password
      */
     protected abstract boolean doCheck(String hashedPassword, String password);
 
     /**
      * Base-64 encodes an array of bytes.
      *
      * @param bytes Array of bytes to encode
      * @return Base-64 encoded representation of the bytes
      * @throws UnsupportedOperationException If an error occured.
      */
     protected String encodeBase64(byte[] bytes)
         throws UnsupportedOperationException
     {
         ByteArrayOutputStream   baos;
         String  encoded;
 
         baos = new ByteArrayOutputStream();
         int carry = 0;
        for (int i = 0; i < bytes.length; i++)
         {
             int val;
             int b = (bytes[i]) & 255;
             switch (i % 3)
             {
                 case 0:
                     baos.write(BASE64_MAP[b / 4]);
                     carry = b & 3;
                     break;
                 case 1:
                     val = (carry << 8) + b;
                     baos.write(BASE64_MAP[val / 16]);
                     carry = b & 15;
                     break;
                 case 2:
                     val = (carry << 8) + b;
                     baos.write(BASE64_MAP[val / 64]);
                     baos.write(BASE64_MAP[val & 63]);
                     carry = 0;
             }
         }
         switch (bytes.length % 3)
         {
             case 1:
                 baos.write(BASE64_MAP[carry << 4]);
                 baos.write('=');
                 baos.write('=');
                 break;
             case 2:
                 baos.write(BASE64_MAP[carry << 2]);
                 baos.write('=');
         }
         encoded = new String(baos.toByteArray());
         
         return encoded;
     }
 
     /**
      * Create a new byte array by appending two byte arrays.
      *
      * @param first The first byte array
      * @param second The second byte array
      * @return A new byte array which is the first appended by the
      * second.
      */
     protected final byte[] append(byte[] first, byte[] second)
     {
         byte[] appended;
 
         appended = new byte[first.length + second.length];
         System.arraycopy(first, 0, appended, 0, first.length);
         System.arraycopy(second, 0, appended, first.length, second.length);
 
         return appended;
     }
     
     /**
      * Creates a new byte array by appending the byte representation
      * of a string and a byte array.
      *
      * @param first A string, which is converted to bytes
      * @param second The second byte array
      * @return A new byte array which is the first appended by the
      * second.
      */
     protected final byte[] append(String first, byte[] second)
     {
         return append(first.getBytes(), second);
     }
 
     /**
      * A lookup table from a password scheme to an instance of
      * LdapPassword for that scheme.
      */
     private static Map mSchemeLookup;
 
     /**
      * The class name of the random class.  By default, it is
      * <code>java.security.SecureRandom</code>
      */
     private static String mRandomClass = "java.security.SecureRandom";

    /** The map to use for Base64 encoding. */
     private static final byte[] BASE64_MAP = {
         'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
         'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b',
         'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
         'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3',
         '4', '5', '6', '7', '8', '9', '+', '/'
     };
 }
