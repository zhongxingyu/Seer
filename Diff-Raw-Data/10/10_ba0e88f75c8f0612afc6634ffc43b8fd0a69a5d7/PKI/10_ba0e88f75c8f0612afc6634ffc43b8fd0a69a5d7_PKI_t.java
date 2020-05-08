 import java.math.BigInteger;
 import java.security.GeneralSecurityException;
 import java.security.interfaces.ECPublicKey;
 import java.security.interfaces.ECPrivateKey;
 import java.security.interfaces.RSAPublicKey;
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.security.InvalidKeyException;
 import java.security.Key;
 import java.security.MessageDigest;
 import java.security.PrivateKey;
 import java.security.Provider;
 import java.security.PublicKey;
 import java.security.SecureRandom;
 import java.security.Security;
 import java.security.Signature;
 import java.security.spec.AlgorithmParameterSpec;
 import java.security.spec.ECGenParameterSpec;
 import java.security.spec.ECPoint;
 import java.security.spec.RSAKeyGenParameterSpec;
 import java.util.Arrays;
 
 import javax.crypto.BadPaddingException;
 import javax.crypto.Cipher;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 
 import org.bouncycastle.crypto.CipherParameters;
 import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
 import org.bouncycastle.crypto.params.ECDomainParameters;
 import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
 import org.bouncycastle.crypto.params.ECPublicKeyParameters;
 import org.bouncycastle.crypto.signers.ECGOST3410Signer;
 import org.bouncycastle.jcajce.provider.asymmetric.ec.ECUtil;
 import org.bouncycastle.jce.spec.ECParameterSpec;
 //import org.bouncycastle.jce.spec.IEKeySpec;
 import org.bouncycastle.jce.spec.IESParameterSpec;
 import org.bouncycastle.jce.provider.BouncyCastleProvider;
 import org.bouncycastle.util.encoders.Hex;
 
 public class PKI {
     public static void main(String[] args) {
 
         Security.addProvider(new BouncyCastleProvider());
 
         KeyPairGenerator keyPairGenerator = null;
         try {
             keyPairGenerator = KeyPairGenerator.getInstance("ECGOST3410", "BC");
             AlgorithmParameterSpec spec =
                 new ECGenParameterSpec("GostR3410-2001-CryptoPro-A");
             keyPairGenerator.initialize(spec, new SecureRandom());
         } catch(GeneralSecurityException e) {
             System.out.println(e.toString());
         }
 
         KeyPair keyPair = null;
         if (keyPairGenerator != null) {
             keyPair = keyPairGenerator.generateKeyPair();
             PublicKey publicKey = keyPair.getPublic();
             System.out.println(publicKey.toString());
 
             if (publicKey instanceof ECPublicKey) {
                 ECPublicKey ecPublicKey = (ECPublicKey)publicKey;
                 ECPoint point = ecPublicKey.getW();
                 BigInteger affineX = point.getAffineX();
                 System.out.println("X\t" + affineX.toString(16));
                 BigInteger affineY = point.getAffineY();
                 System.out.println("Y\t" + affineY.toString(16));
             }
         }
 
         byte[] message = Hex.decode("1234567890abcdef");
         System.out.println("Message\t" + new BigInteger(message).toString(16));
 
         if (keyPair != null) {
             byte[] derivation = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };
             byte[] encoding = new byte[] { 8, 7, 6, 5, 4, 3, 2, 1 };
             IESParameterSpec iesSpec = new IESParameterSpec(derivation, encoding, 128);
             Key publicKey = keyPair.getPublic();
             //Key publicKey = new IEKeySpec(keyPair.getPrivate(), keyPair.getPublic());
             Key privateKey = keyPair.getPrivate();
             //Key privateKey = new IEKeySpec(keyPair.getPrivate(), keyPair.getPublic());
             try {
 
                 Cipher cipher1 = Cipher.getInstance("ECIES", "BC");
                 cipher1.init(Cipher.ENCRYPT_MODE, publicKey, iesSpec);
                 byte[] encrypted = cipher1.doFinal(message, 0, message.length);
 
                 System.out.println("Encrypted\t" + new BigInteger(encrypted).toString(16));
 
                 Cipher cipher2 = Cipher.getInstance("ECIES", "BC");
                 cipher2.init(Cipher.DECRYPT_MODE, privateKey, iesSpec);
                 byte[] decrypted = cipher2.doFinal(encrypted, 0, encrypted.length);
 
                 System.out.println("Decrypted\t" + new BigInteger(decrypted).toString(16));
             } catch(GeneralSecurityException e) {
                 System.out.println(e.toString());
             }
         }
 
         byte[] signatureBytes = null;
         try {
             Signature signature = Signature.getInstance("GOST3411withECGOST3410", "BC");
             signature.initSign(keyPair.getPrivate());
             signature.update(message);
             signatureBytes = signature.sign();
             System.out.println("Signature\t" + new BigInteger(signatureBytes).toString(16));
         } catch(GeneralSecurityException e) {
             System.out.println(e.toString());
         }
 
         byte[] hash = null;
         try {
             MessageDigest md = MessageDigest.getInstance("GOST3411", "BC");
             hash = md.digest(message);
             System.out.println("Hash\t" + new BigInteger(hash).toString(16));
         } catch(GeneralSecurityException e) {
             System.out.println(e.toString());
         }
 
         byte[] hashSignatureBytes = signHashECGOST3410(hash, keyPair.getPrivate());
         System.out.println("Hash signature\t" + new BigInteger(hashSignatureBytes).toString(16));
 
         try {
             Signature signature = Signature.getInstance("GOST3411withECGOST3410", "BC");
             signature.initVerify(keyPair.getPublic());

             signature.update(message);
             if (signature.verify(signatureBytes)) {
                 System.out.println("Signature\tvalid");
             } else {
                System.out.println("Signature\tinvalid");
             }

            signature.update(message);
             if (signature.verify(hashSignatureBytes)) {
                 System.out.println("Hash signature\tvalid");
             } else {
                System.out.println("Hash signature\tinvalid");
             }
         } catch(GeneralSecurityException e) {
             System.out.println(e.toString());
         }
     }
 
     public static byte[] signHashECGOST3410(byte[] hash, PrivateKey privateKey) {
         byte[] signature = null;
         ECGOST3410Signer gost3410Signer = new ECGOST3410Signer();
         try {
             CipherParameters param = ECUtil.generatePrivateKeyParameter(privateKey);
             gost3410Signer.init(true, param);
             BigInteger[] sigBigInts = gost3410Signer.generateSignature(hash);
             byte[] r = sigBigInts[0].toByteArray();
             byte[] s = sigBigInts[1].toByteArray();
 
             signature = new byte[64];
 
             int sOffset = (s[0] == 0) ? 1 : 0;
             System.arraycopy(s, sOffset, signature, 32 - (s.length - sOffset), s.length - sOffset);
 
             int rOffset = (r[0] == 0) ? 1 : 0;
             System.arraycopy(r, rOffset, signature, 64 - (r.length - rOffset), r.length - rOffset);
         } catch(GeneralSecurityException e) {
             System.out.println(e.toString());
         }
         return signature;
     }
 }
