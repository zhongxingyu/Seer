 /**
  * @author Tadanori TERUYA &lt;tadanori.teruya@gmail.com&gt; (2012)
  * @license: The MIT license &lt;http://opensource.org/licenses/MIT&gt;
  */
 /*
  * Copyright (c) 2012 Tadanori TERUYA (tell) <tadanori.teruya@gmail.com>
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation files
  * (the "Software"), to deal in the Software without restriction,
  * including without limitation the rights to use, copy, modify, merge,
  * publish, distribute, sublicense, and/or sell copies of the Software,
  * and to permit persons to whom the Software is furnished to do so,
  * subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
  * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
  * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  *
  * @license: The MIT license <http://opensource.org/licenses/MIT>
  */
 package com.github.tell.cryptography.paillier.algorithm;
 
 import com.github.tell.arithmetic.integer.Integer;
 import com.github.tell.arithmetic.integer.ResidueRing;
 import com.github.tell.arithmetic.integer.gmp.MPZ;
 import com.github.tell.arithmetic.integer.gmp.MPZResidueRing;
 import com.github.tell.cryptography.interfaces.Ciphertext;
 import com.github.tell.cryptography.keymanagement.KeyLength;
 import com.github.tell.cryptography.paillier.datastructure.PaillierPlaintext;
 import junit.framework.Assert;
 import org.apache.log4j.PropertyConfigurator;
 import org.junit.Before;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.security.KeyPair;
 import java.security.SecureRandom;
 
 /**
  * @author Tadanori TERUYA &lt;tadanori.teruya@gmail.com&gt; (2012)
  */
 public class PaillierCRTTest {
 
     static {
         PropertyConfigurator.configure("log4j.properties");
     }
 
     @SuppressWarnings("UnusedDeclaration")
     private static final Logger logger = LoggerFactory.getLogger(PaillierCRTTest.class);
 
     private SecureRandom secureRandom;
     private PaillierEncryptor enc;
     private PaillierCRTDecryptor dec;
     private ResidueRing ring;
 
     public static final int numberOfLoop = 1000;
 
     @SuppressWarnings("unchecked")
     @Before
     public void setUp() throws Exception {
         secureRandom = new SecureRandom();
 
         final PaillierCRTKeyPairGenerator crtKeyPairGenerator = new PaillierCRTKeyPairGenerator("");
         crtKeyPairGenerator.initialize(KeyLength.SEC40.compositeNumber, secureRandom);
         final KeyPair keyPair = crtKeyPairGenerator.generateKeyPair();
 
        enc = new PaillierEncryptor(keyPair.getPublic(), MPZ.class, MPZResidueRing.class);
        dec = new PaillierCRTDecryptor(keyPair.getPublic(), keyPair.getPrivate(), MPZ.class,
                MPZResidueRing.class);
        ring = new MPZResidueRing(enc.getPublicKey().getN());
     }
 
     @Test
     public void encryptionAndDecryption() throws Exception {
         for (int i = 0; i < numberOfLoop; i++) {
             final Integer m = ring.nextRandomInteger(secureRandom);
             final PaillierPlaintext plaintext = new PaillierPlaintext(m);
             final Ciphertext ciphertext = enc.encrypt(plaintext, secureRandom);
             final Integer d = dec.decrypt((Integer) ciphertext.getRawData());
             Assert.assertEquals(m, d);
         }
     }
 
     @Test
     public void reRandomization() throws Exception {
         for (int i = 0; i < numberOfLoop; i++) {
             final Integer m = ring.nextRandomInteger(secureRandom);
             final PaillierPlaintext plaintext = new PaillierPlaintext(m);
             final Integer c = enc.encrypt(plaintext, secureRandom).getRawData();
             final Integer cr = enc.reRandomize(c, secureRandom);
             final Integer d = dec.decrypt(cr);
             Assert.assertEquals(m, d);
         }
     }
 
     @Test
     public void additionOnEncryptedData() throws Exception {
         for (int i = 0; i < numberOfLoop; i++) {
             final Integer m1 = ring.nextRandomInteger(secureRandom);
             final PaillierPlaintext plaintext1 = new PaillierPlaintext(m1);
             final Integer m2 = ring.nextRandomInteger(secureRandom);
             final PaillierPlaintext plaintext2 = new PaillierPlaintext(m2);
             @SuppressWarnings("unchecked") final Integer mAdd = ring.add(m1, m2);
             final Ciphertext c1 = enc.encrypt(plaintext1, secureRandom);
             final Ciphertext c2 = enc.encrypt(plaintext2, secureRandom);
             final Integer cAdd = enc.add(c1, c2).getRawData();
             final Integer d = dec.decrypt(cAdd);
             Assert.assertEquals(mAdd, d);
         }
     }
 
     @Test
     public void multiplicationOnEncryptedData() throws Exception {
         for (int i = 0; i < numberOfLoop; i++) {
             final Integer m1 = ring.nextRandomInteger(secureRandom);
             final Integer m2 = ring.nextRandomInteger(secureRandom);
             @SuppressWarnings("unchecked") final Integer mMul = ring.mul(m1, m2);
             final PaillierPlaintext plaintext = new PaillierPlaintext(m1);
             final Ciphertext c1 = enc.encrypt(plaintext, secureRandom);
             final PaillierPlaintext plaintext2 = new PaillierPlaintext(m2);
             final Ciphertext cMul = enc.mul(c1, plaintext2);
             final Integer d = dec.decrypt((Integer) cMul.getRawData());
             Assert.assertEquals(mMul, d);
         }
     }
 }
