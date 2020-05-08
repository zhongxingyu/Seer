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
 import com.github.tell.arithmetic.integer.JavaInteger;
 import com.github.tell.arithmetic.integer.JavaResidueRing;
 import com.github.tell.arithmetic.integer.ResidueRing;
 import com.github.tell.arithmetic.integer.gmp.MPZ;
 import com.github.tell.arithmetic.integer.gmp.MPZResidueRing;
 import com.github.tell.cryptography.interfaces.Ciphertext;
 import com.github.tell.cryptography.interfaces.Plaintext;
 import com.github.tell.cryptography.keymanagement.KeyLength;
 import com.github.tell.cryptography.keymanagement.KeyUtils;
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
 public class PaillierTest {
 
     static {
         PropertyConfigurator.configure("log4j.properties");
     }
 
     private static final Logger logger = LoggerFactory.getLogger(PaillierTest.class);
 
     private SecureRandom secureRandom;
     private PaillierEncryptor enc;
     private PaillierDecryptor dec;
     private ResidueRing ring;
    private ResidueRing ringWithBigInteger;
 
     public static final int numberOfLoop = 1000;
 
     @Before
     public void setUp() throws Exception {
         secureRandom = SecureRandom.getInstance("SHA1PRNG");
         PaillierKeyPairGenerator keyPairGenerator = new PaillierKeyPairGenerator("");
         keyPairGenerator.initialize(KeyLength.SEC40.compositeNumber, secureRandom);
         logger.info("key pair generator is {}", keyPairGenerator);
         KeyPair keyPair = keyPairGenerator.generateKeyPair();
         logger.info("generated key is {}", KeyUtils.keyPairToString(keyPair));
        //noinspection unchecked
        enc = new PaillierEncryptor(keyPair.getPublic(), MPZ.class, MPZResidueRing.class);
        //noinspection unchecked
        dec = new PaillierDecryptor(keyPair.getPublic(), keyPair.getPrivate(), MPZ.class,
                MPZResidueRing.class);
        ring = new MPZResidueRing(enc.getPublicKey().getN());
        ringWithBigInteger = new JavaResidueRing(enc.getPublicKey().getN());
        Assert.assertEquals(ring.getModulus().toBigInteger(), ringWithBigInteger.getModulus().toBigInteger());
     }
 
     @Test
     public void encryptionAndDecryption() throws Exception {
         for (int i = 0; i < numberOfLoop; i++) {
             final Integer m = ring.nextRandomInteger(secureRandom);
             final Plaintext plaintext = new PaillierPlaintext(m);
             final Ciphertext ciphertext = enc.encrypt(plaintext, secureRandom);
             final Plaintext decrypted = dec.decrypt(ciphertext);
             Assert.assertEquals(plaintext, decrypted);
         }
     }
 
     @Test
     public void reRandomization() throws Exception {
         for (int i = 0; i < numberOfLoop; i++) {
             final Integer m = ring.nextRandomInteger(secureRandom);
             final Plaintext plaintext = new PaillierPlaintext(m);
             final Ciphertext ciphertext = enc.encrypt(plaintext, secureRandom);
             final Ciphertext ciphertext1 = enc.reRandomize(ciphertext, secureRandom);
             final Plaintext decrypted = dec.decrypt(ciphertext1);
             Assert.assertEquals(m, decrypted.getRawData());
         }
     }
 
     @Test
     public void additionOnEncryptedSmallData() throws Exception {
         for (int i = 0; i < numberOfLoop; i++) {
             final Integer m1 = ring.makeElement(1);
             final Plaintext plaintext1 = new PaillierPlaintext(m1);
             final Integer m2 = ring.makeElement(2);
             final Plaintext plaintext2 = new PaillierPlaintext(m2);
 
             @SuppressWarnings("unchecked") final Integer mAdd = ring.add(m1, m2);
             final Ciphertext ciphertext1 = enc.encrypt(plaintext1, secureRandom);
             final Ciphertext ciphertext2 = enc.encrypt(plaintext2, secureRandom);
             final Ciphertext ciphertext3 = enc.add(ciphertext1, ciphertext2);
             final Plaintext plaintext3 = dec.decrypt(ciphertext3);
             Assert.assertEquals(mAdd, plaintext3.getRawData());
         }
     }
 
     @Test
     public void additionOnEncryptedData() throws Exception {
         for (int i = 0; i < numberOfLoop; i++) {
             final Integer m1 = ring.nextRandomInteger(secureRandom);
             final Integer m1BI = new JavaInteger(m1.toBigInteger());
             Assert.assertEquals(m1.toBigInteger(), m1BI.toBigInteger());
             final Plaintext plaintext1 = new PaillierPlaintext(m1);
             final Integer m2 = ring.nextRandomInteger(secureRandom);
             final Integer m2BI = new JavaInteger(m2.toBigInteger());
             Assert.assertEquals(m2.toBigInteger(), m2BI.toBigInteger());
             final Plaintext plaintext2 = new PaillierPlaintext(m2);
 
             @SuppressWarnings("unchecked") final Integer mAdd = ring.add(m1, m2);
            @SuppressWarnings("unchecked") final Integer mAddBI = ringWithBigInteger.add(m1BI, m2BI);
            Assert.assertEquals(mAdd.toBigInteger(), mAddBI.toBigInteger());
 
             final Ciphertext ciphertext1 = enc.encrypt(plaintext1, secureRandom);
             final Ciphertext ciphertext2 = enc.encrypt(plaintext2, secureRandom);
             final Ciphertext ciphertext3 = enc.add(ciphertext1, ciphertext2);
             final Plaintext plaintext3 = dec.decrypt(ciphertext3);
             Assert.assertEquals(mAdd, plaintext3.getRawData());
         }
     }
 
     @Test
     public void multiplicationOnEncryptedData() throws Exception {
         for (int i = 0; i < numberOfLoop; i++) {
             final Integer m1 = ring.nextRandomInteger(secureRandom);
             final Integer m2 = ring.nextRandomInteger(secureRandom);
             @SuppressWarnings("unchecked") final Integer mMul = ring.mul(m1, m2);
             final Plaintext plaintext = new PaillierPlaintext(m1);
             final Plaintext plaintext2 = new PaillierPlaintext(m2);
             final Ciphertext ciphertext = enc.encrypt(plaintext, secureRandom);
             final Ciphertext ciphertext2 = enc.mul(ciphertext, plaintext2);
             final Plaintext d = dec.decrypt(ciphertext2);
             Assert.assertEquals(mMul, d.getRawData());
         }
     }
 }
