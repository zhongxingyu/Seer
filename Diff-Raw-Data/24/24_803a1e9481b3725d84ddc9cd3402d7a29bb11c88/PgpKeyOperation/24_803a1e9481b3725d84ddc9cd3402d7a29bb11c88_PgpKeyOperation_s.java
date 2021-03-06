 /*
  * Copyright (C) 2012-2013 Dominik Schürmann <dominik@dominikschuermann.de>
  * Copyright (C) 2010 Thialfihar <thi@thialfihar.org>
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
 
 package org.sufficientlysecure.keychain.pgp;
 
 import java.io.IOException;
 import java.math.BigInteger;
 import java.security.InvalidAlgorithmParameterException;
 import java.security.KeyPairGenerator;
 import java.security.NoSuchAlgorithmException;
 import java.security.NoSuchProviderException;
 import java.security.SecureRandom;
 import java.security.SignatureException;
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.spongycastle.bcpg.CompressionAlgorithmTags;
 import org.spongycastle.bcpg.HashAlgorithmTags;
 import org.spongycastle.bcpg.SymmetricKeyAlgorithmTags;
 import org.spongycastle.bcpg.sig.KeyFlags;
 import org.spongycastle.jce.provider.BouncyCastleProvider;
 import org.spongycastle.jce.spec.ElGamalParameterSpec;
 import org.spongycastle.openpgp.PGPEncryptedData;
 import org.spongycastle.openpgp.PGPException;
 import org.spongycastle.openpgp.PGPKeyPair;
 import org.spongycastle.openpgp.PGPKeyRingGenerator;
 import org.spongycastle.openpgp.PGPPrivateKey;
 import org.spongycastle.openpgp.PGPPublicKey;
 import org.spongycastle.openpgp.PGPPublicKeyRing;
 import org.spongycastle.openpgp.PGPSecretKey;
 import org.spongycastle.openpgp.PGPSecretKeyRing;
 import org.spongycastle.openpgp.PGPSignature;
 import org.spongycastle.openpgp.PGPSignatureGenerator;
 import org.spongycastle.openpgp.PGPSignatureSubpacketGenerator;
 import org.spongycastle.openpgp.PGPSignatureSubpacketVector;
 import org.spongycastle.openpgp.PGPUtil;
 import org.spongycastle.openpgp.operator.PBESecretKeyDecryptor;
 import org.spongycastle.openpgp.operator.PBESecretKeyEncryptor;
 import org.spongycastle.openpgp.operator.PGPContentSignerBuilder;
 import org.spongycastle.openpgp.operator.PGPDigestCalculator;
 import org.spongycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
 import org.spongycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
 import org.spongycastle.openpgp.operator.jcajce.JcaPGPKeyPair;
 import org.spongycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
 import org.spongycastle.openpgp.operator.jcajce.JcePBESecretKeyEncryptorBuilder;
 import org.sufficientlysecure.keychain.Constants;
 import org.sufficientlysecure.keychain.Id;
 import org.sufficientlysecure.keychain.R;
 import org.sufficientlysecure.keychain.pgp.exception.PgpGeneralException;
 import org.sufficientlysecure.keychain.provider.ProviderHelper;
 import org.sufficientlysecure.keychain.util.Log;
 import org.sufficientlysecure.keychain.util.Primes;
 import org.sufficientlysecure.keychain.util.ProgressDialogUpdater;
 
 import android.content.Context;
 
 public class PgpKeyOperation {
     private Context mContext;
     private ProgressDialogUpdater mProgress;
 
     private static final int[] PREFERRED_SYMMETRIC_ALGORITHMS = new int[] {
             SymmetricKeyAlgorithmTags.AES_256, SymmetricKeyAlgorithmTags.AES_192,
             SymmetricKeyAlgorithmTags.AES_128, SymmetricKeyAlgorithmTags.CAST5,
             SymmetricKeyAlgorithmTags.TRIPLE_DES };
     private static final int[] PREFERRED_HASH_ALGORITHMS = new int[] { HashAlgorithmTags.SHA1,
             HashAlgorithmTags.SHA256, HashAlgorithmTags.RIPEMD160 };
     private static final int[] PREFERRED_COMPRESSION_ALGORITHMS = new int[] {
             CompressionAlgorithmTags.ZLIB, CompressionAlgorithmTags.BZIP2,
             CompressionAlgorithmTags.ZIP };
 
     public PgpKeyOperation(Context context, ProgressDialogUpdater progress) {
         super();
         this.mContext = context;
         this.mProgress = progress;
     }
 
     public void updateProgress(int message, int current, int total) {
         if (mProgress != null) {
             mProgress.setProgress(message, current, total);
         }
     }
 
     public void updateProgress(int current, int total) {
         if (mProgress != null) {
             mProgress.setProgress(current, total);
         }
     }
 
     /**
      * Creates new secret key. The returned PGPSecretKeyRing contains only one newly generated key
      * when this key is the new masterkey. If a masterkey is supplied in the parameters
      * PGPSecretKeyRing contains the masterkey and the new key as a subkey (certified by the
      * masterkey).
      * 
      * @param algorithmChoice
      * @param keySize
      * @param passPhrase
      * @param masterSecretKey
      * @return
      * @throws NoSuchAlgorithmException
      * @throws PGPException
      * @throws NoSuchProviderException
      * @throws PgpGeneralException
      * @throws InvalidAlgorithmParameterException
      */
     public PGPSecretKeyRing createKey(int algorithmChoice, int keySize, String passPhrase,
             PGPSecretKey masterSecretKey) throws NoSuchAlgorithmException, PGPException,
             NoSuchProviderException, PgpGeneralException, InvalidAlgorithmParameterException {
 
         if (keySize < 512) {
             throw new PgpGeneralException(mContext.getString(R.string.error_key_size_minimum512bit));
         }
 
         if (passPhrase == null) {
             passPhrase = "";
         }
 
         int algorithm = 0;
         KeyPairGenerator keyGen = null;
 
         switch (algorithmChoice) {
         case Id.choice.algorithm.dsa: {
             keyGen = KeyPairGenerator.getInstance("DSA", Constants.BOUNCY_CASTLE_PROVIDER_NAME);
             keyGen.initialize(keySize, new SecureRandom());
             algorithm = PGPPublicKey.DSA;
             break;
         }
 
         case Id.choice.algorithm.elgamal: {
             if (masterSecretKey == null) {
                 throw new PgpGeneralException(
                         mContext.getString(R.string.error_master_key_must_not_be_el_gamal));
             }
             keyGen = KeyPairGenerator.getInstance("ElGamal", Constants.BOUNCY_CASTLE_PROVIDER_NAME);
             BigInteger p = Primes.getBestPrime(keySize);
             BigInteger g = new BigInteger("2");
 
             ElGamalParameterSpec elParams = new ElGamalParameterSpec(p, g);
 
             keyGen.initialize(elParams);
             algorithm = PGPPublicKey.ELGAMAL_ENCRYPT;
             break;
         }
 
         case Id.choice.algorithm.rsa: {
             keyGen = KeyPairGenerator.getInstance("RSA", Constants.BOUNCY_CASTLE_PROVIDER_NAME);
             keyGen.initialize(keySize, new SecureRandom());
 
             algorithm = PGPPublicKey.RSA_GENERAL;
             break;
         }
 
         default: {
            throw new PgpGeneralException(mContext.getString(R.string.error_unknown_algorithm_choice));
         }
         }
 
         // build new key pair
         PGPKeyPair keyPair = new JcaPGPKeyPair(algorithm, keyGen.generateKeyPair(), new Date());
 
         // define hashing and signing algos
         PGPDigestCalculator sha1Calc = new JcaPGPDigestCalculatorProviderBuilder().build().get(
                 HashAlgorithmTags.SHA1);
 
         // Build key encrypter and decrypter based on passphrase
         PBESecretKeyEncryptor keyEncryptor = new JcePBESecretKeyEncryptorBuilder(
                 PGPEncryptedData.CAST5, sha1Calc)
                 .setProvider(Constants.BOUNCY_CASTLE_PROVIDER_NAME).build(passPhrase.toCharArray());
         PBESecretKeyDecryptor keyDecryptor = new JcePBESecretKeyDecryptorBuilder().setProvider(
                 Constants.BOUNCY_CASTLE_PROVIDER_NAME).build(passPhrase.toCharArray());
 
         PGPKeyRingGenerator ringGen = null;
         PGPContentSignerBuilder certificationSignerBuilder = null;
         if (masterSecretKey == null) {
             certificationSignerBuilder = new JcaPGPContentSignerBuilder(keyPair.getPublicKey()
                     .getAlgorithm(), HashAlgorithmTags.SHA1);
 
             // build keyRing with only this one master key in it!
             ringGen = new PGPKeyRingGenerator(PGPSignature.POSITIVE_CERTIFICATION, keyPair, "",
                     sha1Calc, null, null, certificationSignerBuilder, keyEncryptor);
         } else {
             PGPPublicKey masterPublicKey = masterSecretKey.getPublicKey();
             PGPPrivateKey masterPrivateKey = masterSecretKey.extractPrivateKey(keyDecryptor);
             PGPKeyPair masterKeyPair = new PGPKeyPair(masterPublicKey, masterPrivateKey);
 
             certificationSignerBuilder = new JcaPGPContentSignerBuilder(masterKeyPair
                     .getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA1);
 
             // build keyRing with master key and new key as subkey (certified by masterkey)
             ringGen = new PGPKeyRingGenerator(PGPSignature.POSITIVE_CERTIFICATION, masterKeyPair,
                     "", sha1Calc, null, null, certificationSignerBuilder, keyEncryptor);
 
             ringGen.addSubKey(keyPair);
         }
 
         PGPSecretKeyRing secKeyRing = ringGen.generateSecretKeyRing();
 
         return secKeyRing;
     }
 
     public void changeSecretKeyPassphrase(PGPSecretKeyRing keyRing, String oldPassPhrase,
             String newPassPhrase) throws IOException, PGPException, PGPException,
             NoSuchProviderException {
 
         updateProgress(R.string.progress_building_key, 0, 100);
         if (oldPassPhrase == null) {
             oldPassPhrase = "";
         }
         if (newPassPhrase == null) {
             newPassPhrase = "";
         }
 
         PGPSecretKeyRing newKeyRing = PGPSecretKeyRing.copyWithNewPassword(
                 keyRing,
                 new JcePBESecretKeyDecryptorBuilder(new JcaPGPDigestCalculatorProviderBuilder()
                         .setProvider(Constants.BOUNCY_CASTLE_PROVIDER_NAME).build()).setProvider(
                         Constants.BOUNCY_CASTLE_PROVIDER_NAME).build(oldPassPhrase.toCharArray()),
                 new JcePBESecretKeyEncryptorBuilder(keyRing.getSecretKey()
                         .getKeyEncryptionAlgorithm()).build(newPassPhrase.toCharArray()));
 
         updateProgress(R.string.progress_saving_key_ring, 50, 100);
 
         ProviderHelper.saveKeyRing(mContext, newKeyRing);
 
         updateProgress(R.string.progress_done, 100, 100);
 
     }
 
     public void buildSecretKey(ArrayList<String> userIds, ArrayList<PGPSecretKey> keys,
             ArrayList<Integer> keysUsages, long masterKeyId, String oldPassPhrase,
             String newPassPhrase) throws PgpGeneralException, NoSuchProviderException,
             PGPException, NoSuchAlgorithmException, SignatureException, IOException {
 
         Log.d(Constants.TAG, "userIds: " + userIds.toString());
 
         updateProgress(R.string.progress_building_key, 0, 100);
 
         if (oldPassPhrase == null) {
             oldPassPhrase = "";
         }
         if (newPassPhrase == null) {
             newPassPhrase = "";
         }
 
         updateProgress(R.string.progress_preparing_master_key, 10, 100);
 
         int usageId = keysUsages.get(0);
         boolean canSign = (usageId == Id.choice.usage.sign_only || usageId == Id.choice.usage.sign_and_encrypt);
         boolean canEncrypt = (usageId == Id.choice.usage.encrypt_only || usageId == Id.choice.usage.sign_and_encrypt);
 
         String mainUserId = userIds.get(0);
 
         PGPSecretKey masterKey = keys.get(0);
 
         // this removes all userIds and certifications previously attached to the masterPublicKey
         PGPPublicKey tmpKey = masterKey.getPublicKey();
         PGPPublicKey masterPublicKey = new PGPPublicKey(tmpKey.getAlgorithm(),
                 tmpKey.getKey(new BouncyCastleProvider()), tmpKey.getCreationTime());
 
         // already done by code above:
         // PGPPublicKey masterPublicKey = masterKey.getPublicKey();
         // // Somehow, the PGPPublicKey already has an empty certification attached to it when the
         // // keyRing is generated the first time, we remove that when it exists, before adding the
         // new
         // // ones
         // PGPPublicKey masterPublicKeyRmCert = PGPPublicKey.removeCertification(masterPublicKey,
         // "");
         // if (masterPublicKeyRmCert != null) {
         // masterPublicKey = masterPublicKeyRmCert;
         // }
 
         PBESecretKeyDecryptor keyDecryptor = new JcePBESecretKeyDecryptorBuilder().setProvider(
                 Constants.BOUNCY_CASTLE_PROVIDER_NAME).build(oldPassPhrase.toCharArray());
         PGPPrivateKey masterPrivateKey = masterKey.extractPrivateKey(keyDecryptor);
 
         updateProgress(R.string.progress_certifying_master_key, 20, 100);
 
        //TODO: if we are editing a key, keep old certs, don't remake certs we don't have to.
 
         for (String userId : userIds) {
             PGPContentSignerBuilder signerBuilder = new JcaPGPContentSignerBuilder(
                     masterPublicKey.getAlgorithm(), HashAlgorithmTags.SHA1)
                     .setProvider(Constants.BOUNCY_CASTLE_PROVIDER_NAME);
             PGPSignatureGenerator sGen = new PGPSignatureGenerator(signerBuilder);
 
             sGen.init(PGPSignature.POSITIVE_CERTIFICATION, masterPrivateKey);
 
             PGPSignature certification = sGen.generateCertification(userId, masterPublicKey);
 
             masterPublicKey = PGPPublicKey.addCertification(masterPublicKey, userId, certification);
         }
 
         PGPKeyPair masterKeyPair = new PGPKeyPair(masterPublicKey, masterPrivateKey);
 
         PGPSignatureSubpacketGenerator hashedPacketsGen = new PGPSignatureSubpacketGenerator();
         PGPSignatureSubpacketGenerator unhashedPacketsGen = new PGPSignatureSubpacketGenerator();
 
         int keyFlags = KeyFlags.CERTIFY_OTHER | KeyFlags.SIGN_DATA;
         if (canEncrypt) {
             keyFlags |= KeyFlags.ENCRYPT_COMMS | KeyFlags.ENCRYPT_STORAGE;
         }
         hashedPacketsGen.setKeyFlags(true, keyFlags);
 
         hashedPacketsGen.setPreferredSymmetricAlgorithms(true, PREFERRED_SYMMETRIC_ALGORITHMS);
         hashedPacketsGen.setPreferredHashAlgorithms(true, PREFERRED_HASH_ALGORITHMS);
         hashedPacketsGen.setPreferredCompressionAlgorithms(true, PREFERRED_COMPRESSION_ALGORITHMS);
 
         // TODO: this doesn't work quite right yet (APG 1)
         // if (keyEditor.getExpiryDate() != null) {
         // GregorianCalendar creationDate = new GregorianCalendar();
         // creationDate.setTime(getCreationDate(masterKey));
         // GregorianCalendar expiryDate = keyEditor.getExpiryDate();
         // long numDays = Utils.getNumDaysBetween(creationDate, expiryDate);
         // if (numDays <= 0) {
         // throw new GeneralException(
         // context.getString(R.string.error_expiryMustComeAfterCreation));
         // }
         // hashedPacketsGen.setKeyExpirationTime(true, numDays * 86400);
         // }
 
         updateProgress(R.string.progress_building_master_key, 30, 100);
 
         // define hashing and signing algos
         PGPDigestCalculator sha1Calc = new JcaPGPDigestCalculatorProviderBuilder().build().get(
                 HashAlgorithmTags.SHA1);
         PGPContentSignerBuilder certificationSignerBuilder = new JcaPGPContentSignerBuilder(
                 masterKeyPair.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA1);
 
         // Build key encrypter based on passphrase
         PBESecretKeyEncryptor keyEncryptor = new JcePBESecretKeyEncryptorBuilder(
                 PGPEncryptedData.CAST5, sha1Calc)
                 .setProvider(Constants.BOUNCY_CASTLE_PROVIDER_NAME).build(
                         newPassPhrase.toCharArray());
 
         PGPKeyRingGenerator keyGen = new PGPKeyRingGenerator(PGPSignature.POSITIVE_CERTIFICATION,
                 masterKeyPair, mainUserId, sha1Calc, hashedPacketsGen.generate(),
                 unhashedPacketsGen.generate(), certificationSignerBuilder, keyEncryptor);
 
         updateProgress(R.string.progress_adding_sub_keys, 40, 100);
 
         for (int i = 1; i < keys.size(); ++i) {
             updateProgress(40 + 50 * (i - 1) / (keys.size() - 1), 100);
 
             PGPSecretKey subKey = keys.get(i);
             PGPPublicKey subPublicKey = subKey.getPublicKey();
 
             PBESecretKeyDecryptor keyDecryptor2 = new JcePBESecretKeyDecryptorBuilder()
                     .setProvider(Constants.BOUNCY_CASTLE_PROVIDER_NAME).build(
                             oldPassPhrase.toCharArray());
             PGPPrivateKey subPrivateKey = subKey.extractPrivateKey(keyDecryptor2);
 
             // TODO: now used without algorithm and creation time?! (APG 1)
             PGPKeyPair subKeyPair = new PGPKeyPair(subPublicKey, subPrivateKey);
 
             hashedPacketsGen = new PGPSignatureSubpacketGenerator();
             unhashedPacketsGen = new PGPSignatureSubpacketGenerator();
 
             keyFlags = 0;
 
             usageId = keysUsages.get(i);
             canSign = (usageId == Id.choice.usage.sign_only || usageId == Id.choice.usage.sign_and_encrypt);
             canEncrypt = (usageId == Id.choice.usage.encrypt_only || usageId == Id.choice.usage.sign_and_encrypt);
            if (canSign) { //TODO: ensure signing times are the same, like gpg
                 keyFlags |= KeyFlags.SIGN_DATA;
                //cross-certify signing keys
                 PGPContentSignerBuilder signerBuilder = new JcaPGPContentSignerBuilder(
                    subPublicKey.getAlgorithm(), PGPUtil.SHA1)
                    .setProvider(Constants.BOUNCY_CASTLE_PROVIDER_NAME);
                 PGPSignatureGenerator sGen = new PGPSignatureGenerator(signerBuilder);
                 sGen.init(PGPSignature.PRIMARYKEY_BINDING, subPrivateKey);
                PGPSignature certification = sGen.generateCertification(masterPublicKey, subPublicKey);
                 unhashedPacketsGen.setEmbeddedSignature(false, certification);
             }
             if (canEncrypt) {
                 keyFlags |= KeyFlags.ENCRYPT_COMMS | KeyFlags.ENCRYPT_STORAGE;
             }
             hashedPacketsGen.setKeyFlags(false, keyFlags);
 
             // TODO: this doesn't work quite right yet (APG 1)
             // if (keyEditor.getExpiryDate() != null) {
             // GregorianCalendar creationDate = new GregorianCalendar();
             // creationDate.setTime(getCreationDate(masterKey));
             // GregorianCalendar expiryDate = keyEditor.getExpiryDate();
             // long numDays = Utils.getNumDaysBetween(creationDate, expiryDate);
             // if (numDays <= 0) {
             // throw new GeneralException(
             // context.getString(R.string.error_expiryMustComeAfterCreation));
             // }
             // hashedPacketsGen.setKeyExpirationTime(true, numDays * 86400);
             // }
 
             keyGen.addSubKey(subKeyPair, hashedPacketsGen.generate(), unhashedPacketsGen.generate());
         }
 
         PGPSecretKeyRing secretKeyRing = keyGen.generateSecretKeyRing();
         PGPPublicKeyRing publicKeyRing = keyGen.generatePublicKeyRing();
 
         updateProgress(R.string.progress_saving_key_ring, 90, 100);
 
         ProviderHelper.saveKeyRing(mContext, secretKeyRing);
         ProviderHelper.saveKeyRing(mContext, publicKeyRing);
 
         updateProgress(R.string.progress_done, 100, 100);
     }
 
     public PGPPublicKeyRing signKey(long masterKeyId, long pubKeyId, String passphrase)
             throws PgpGeneralException, NoSuchAlgorithmException, NoSuchProviderException,
             PGPException, SignatureException {
        if (passphrase == null || passphrase.length() <= 0) {
             throw new PgpGeneralException("Unable to obtain passphrase");
         } else {
             PGPPublicKeyRing pubring = ProviderHelper
                     .getPGPPublicKeyRingByKeyId(mContext, pubKeyId);
 
             PGPSecretKey signingKey = PgpKeyHelper.getCertificationKey(mContext, masterKeyId);
             if (signingKey == null) {
                 throw new PgpGeneralException(mContext.getString(R.string.error_signature_failed));
             }
 
             PBESecretKeyDecryptor keyDecryptor = new JcePBESecretKeyDecryptorBuilder().setProvider(
                     Constants.BOUNCY_CASTLE_PROVIDER_NAME).build(passphrase.toCharArray());
             PGPPrivateKey signaturePrivateKey = signingKey.extractPrivateKey(keyDecryptor);
             if (signaturePrivateKey == null) {
                 throw new PgpGeneralException(
                         mContext.getString(R.string.error_could_not_extract_private_key));
             }
 
             // TODO: SHA256 fixed?
             JcaPGPContentSignerBuilder contentSignerBuilder = new JcaPGPContentSignerBuilder(
                     signingKey.getPublicKey().getAlgorithm(), PGPUtil.SHA256)
                     .setProvider(Constants.BOUNCY_CASTLE_PROVIDER_NAME);
 
             PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(
                     contentSignerBuilder);
 
             signatureGenerator.init(PGPSignature.DIRECT_KEY, signaturePrivateKey);
 
             PGPSignatureSubpacketGenerator spGen = new PGPSignatureSubpacketGenerator();
 
             PGPSignatureSubpacketVector packetVector = spGen.generate();
             signatureGenerator.setHashedSubpackets(packetVector);
 
             PGPPublicKey signedKey = PGPPublicKey.addCertification(pubring.getPublicKey(pubKeyId),
                     signatureGenerator.generate());
             pubring = PGPPublicKeyRing.insertPublicKey(pubring, signedKey);
 
             return pubring;
         }
     }
 }
