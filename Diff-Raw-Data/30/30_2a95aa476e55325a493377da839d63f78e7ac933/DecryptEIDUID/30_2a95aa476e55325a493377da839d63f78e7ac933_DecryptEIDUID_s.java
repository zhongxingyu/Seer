 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.math.BigInteger;
 import java.nio.file.Path;
 import java.security.InvalidAlgorithmParameterException;
 import java.security.InvalidKeyException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Arrays;
 import java.util.zip.ZipInputStream;
 
 import javax.crypto.BadPaddingException;
 import javax.crypto.Cipher;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.spec.IvParameterSpec;
 import javax.crypto.spec.SecretKeySpec;
 
 import org.apache.commons.lang.exception.ExceptionUtils;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.bouncycastle.crypto.InvalidCipherTextException;
 import org.bouncycastle.crypto.digests.SHA256Digest;
 import org.bouncycastle.crypto.encodings.OAEPEncoding;
 import org.bouncycastle.crypto.engines.RSAEngine;
 import org.bouncycastle.crypto.params.RSAKeyParameters;
 
 import sun.security.rsa.RSAPublicKeyImpl;
 
 public class DecryptEIDUID extends Thread {
 
   private Path inFile;
   private Path outFile;
   private static final Logger LOG = LogManager.getLogger(DecryptEIDUID.class);
   private boolean done = false;
 
   public DecryptEIDUID(Path inFile) {
     this.inFile = inFile;
     int len;
     if (Main.mode.equalsIgnoreCase("OrganizedXmlDirectory")) {
       String fileName = inFile.getFileName().toString();
       len = fileName.length();
       String strYear, strMonth;
       strYear = fileName.substring(len - 14, len - 10);
       strMonth = fileName.substring(len - 9, len - 7);
       this.outFile = Main.organizedXmlDirectory.resolve(strYear + File.separator + strMonth + "_dec" + File.separator + inFile.getFileName());
     } else {
       String tmp = inFile.getFileName().toString();
       String cleaned = tmp.replaceAll(".zip", "");
       cleaned = cleaned.replaceAll(".csv", "");
       cleaned = cleaned.replaceAll(".xml", "");
 
       if (inFile.getFileName().toString().contains(".csv"))
         this.outFile = Main.outputDirectory.resolve(cleaned + ".csv");
       else if (inFile.getFileName().toString().contains(".xml"))
         this.outFile = Main.outputDirectory.resolve(cleaned + ".xml");
       else
         this.outFile = Main.outputDirectory.resolve(inFile.getFileName());
     }
 
     // One directory per file.
     if (Main.unzipFiles) {
       try {
         ZipInputStream zipFile = new ZipInputStream(new FileInputStream(inFile.toFile()));
         zipFile.close();
         FileUnzipper fuz = new FileUnzipper(inFile);
         for (File f : fuz.getOutFiles()) {
           if (f.getAbsolutePath().endsWith(".xml") || f.getAbsolutePath().endsWith(".csv") || f.getAbsolutePath().endsWith(".zip")) {
             Main.count.incrementAndGet();
             Main.tpe.execute(new DecryptEIDUID(Main.tmpDirectory.resolve(f.getAbsolutePath())));
             done = true;
           }
         }
         return;
       } catch (Exception e) {
         // Not a zip file. Do nothing.
         e.printStackTrace();
       }
     }
   }
 
   @Override
   public void run() {
     if (done) {
       Main.count.decrementAndGet();
       return;
     }
     decryptEidUidFile();
   }
 
   private void decryptEidUidFile() {
 
     long used = 0;
     boolean ERROR = false;
     String versionNumber;
     System.out.println("Start - " + outFile.getFileName());
     try {
 
       if (Main.mode.equalsIgnoreCase("OrganizedXmlDirectory")) {
         // Check if file is already decrypted before proceeding.
         String tmpFile = inFile.getFileName().toString();
         int len = tmpFile.length();
         String tmpYear = tmpFile.substring(len - 14, len - 10);
         String tmpMonth = tmpFile.substring(len - 9, len - 7);
         String key = tmpYear + File.separator + tmpMonth + "_dec" + File.separator + inFile.getFileName().toString();
         Path decryptCheck = Main.organizedXmlDirectory.resolve(key);
         Path parent = decryptCheck.getParent();
         parent.toFile().mkdirs();
         if (decryptCheck.toFile().exists()) {
           System.out.println("File (" + inFile.getFileName() + ") is already decrypted. Ignoring.");
           return;
         }
 
         // Check if file is unencrypted.
         FileInputStream tmpFileUnc = new FileInputStream(inFile.toFile());
         byte[] first11 = new byte[11];
         tmpFileUnc.read(first11, 0, 11);
         tmpFileUnc.close();
         versionNumber = new String(first11);
         if (versionNumber.equalsIgnoreCase("<?xml versi")) {
           // Move to decrypted folder.
           System.out.println("File is unencrypted. Ignoring.");
           return;
         }
       }
 
       // File must be encrypted
       FileInputStream fis = new FileInputStream(inFile.toFile());
       byte[] chunks = new byte[11];
       fis.read(chunks, 0, 11);
       versionNumber = new String(chunks);
       // System.out.println(versionNumber);
       if (!versionNumber.equalsIgnoreCase("VERSION_1.0")) {
         fis = new FileInputStream(inFile.toFile());
       } else {
         used += 11;
       }
 
       if (Main.KEY_SIZE == 1024) {
         chunks = new byte[162];
         fis.read(chunks, 0, 162);
         used += 162;
       } else if (Main.KEY_SIZE == 2048) {
         chunks = new byte[294];
         fis.read(chunks, 0, 294);
         used += 294;
       }
 
       byte[] publicKeyModulus = new RSAPublicKeyImpl(chunks).getModulus().toByteArray();
       if (!Utils.isEqualBytes(publicKeyModulus, new RSAPublicKeyImpl(Main.keyFile.publicKey.getEncoded()).getModulus().toByteArray())) {
         System.out.println("Keys have different Mod values");
         ERROR = true;
         throw new Exception("Keys have different Mod values");
       }
       // System.out.println(Arrays.toString(publicKeyModulus));
 
       byte[] padding = new byte[32];
       fis.read(padding, 0, 32);
       used += 32;
 
       if (Main.KEY_SIZE == 1024) {
         chunks = new byte[128];
         fis.read(chunks, 0, 128);
         used += 128;
       } else if (Main.KEY_SIZE == 2048) {
         chunks = new byte[256];
         fis.read(chunks, 0, 256);
         used += 256;
       }
 
       byte[] secret = new byte[chunks.length];
       System.arraycopy(chunks, 0, secret, 0, chunks.length);
 
       byte[] dec = Main.keyFile.decrypt(secret);
 
       OAEPEncoding cipher = new OAEPEncoding(new RSAEngine(), new SHA256Digest(), padding);
       RSAKeyParameters params = new RSAKeyParameters(false, new RSAPublicKeyImpl(Main.keyFile.publicKey.getEncoded()).getModulus(), BigInteger.ONE);
       cipher.init(false, params);
       byte[] aesKey = cipher.processBlock(dec, 0, dec.length);
 
       // Just write to file.
       Object[] tmp = decryptEidUidMessage(fis, padding, aesKey, used, 32);
       byte[] hash = (byte[]) tmp[0];
       fis.close();
 
       byte[] genHash = (byte[]) tmp[1];
       if (!Utils.isEqualBytes(hash, genHash)) {
         ERROR = true;
         System.out.println("Hash Check Failed.");
         outFile.toFile().delete();
       }
 
       System.out.println("Done - " + outFile.getFileName());
 
     } catch (FileNotFoundException e) {
       ERROR = true;
       e.printStackTrace();
       LOG.error(ExceptionUtils.getStackTrace(e));
     } catch (InvalidKeyException e) {
       ERROR = true;
       e.printStackTrace();
       LOG.error(ExceptionUtils.getStackTrace(e));
     } catch (NoSuchAlgorithmException e) {
       ERROR = true;
       e.printStackTrace();
       LOG.error(ExceptionUtils.getStackTrace(e));
     } catch (IOException e) {
       ERROR = true;
       e.printStackTrace();
       LOG.error(ExceptionUtils.getStackTrace(e));
     } catch (InvalidCipherTextException e) {
       ERROR = true;
       e.printStackTrace();
       LOG.error(ExceptionUtils.getStackTrace(e));
     } catch (Exception e) {
       ERROR = true;
       e.printStackTrace();
       LOG.error(ExceptionUtils.getStackTrace(e));
     } catch (Error e) {
       ERROR = true;
       e.printStackTrace();
       LOG.error(ExceptionUtils.getStackTrace(e));
     } finally {
       Main.count.decrementAndGet();
       if (ERROR) {
         LOG.error("Could not decrypt file: " + inFile.getFileName());
       }
     }
   }
 
   private Object[] decryptEidUidMessage(FileInputStream fis, byte[] initVector, byte[] aesKey, long used, int hashSize) {
 
     byte[] hash = new byte[hashSize];
 
     used = inFile.toFile().length() - used;
     byte[] chunks = new byte[Main.CHUNK_SIZE];
     MessageDigest md = null;
 
     try {
       if (outFile.toFile().exists())
         outFile.toFile().delete();
       outFile.toFile().createNewFile();
       FileOutputStream fos = new FileOutputStream(outFile.toFile());
       OutputStreamWriter out = new OutputStreamWriter(fos);
       IvParameterSpec spec = new IvParameterSpec(Arrays.copyOfRange(initVector, 0, 16));
       SecretKeySpec key = new SecretKeySpec(aesKey, "AES");
       Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
       cipher.init(Cipher.DECRYPT_MODE, key, spec);
 
       md = MessageDigest.getInstance("SHA-256");
 
       // Deal with hash.
       fis.read(hash, 0, hash.length);
       hash = cipher.update(hash);
       used -= hash.length;
 
       int x;
       while ((x = fis.read(chunks, 0, chunks.length)) != -1) {
         if (used != x) {
           byte[] dec = cipher.update(chunks, 0, x);
           md.update(dec);
           out.append(new String(dec));
         } else {
           byte[] dec = cipher.doFinal(chunks, 0, x);
           md.update(dec);
           out.append(new String(dec));
         }
         used -= x;
       }
       out.close();
       fos.close();
     } catch (InvalidKeyException e) {
       e.printStackTrace();
       LOG.error(ExceptionUtils.getStackTrace(e));
     } catch (NoSuchAlgorithmException e) {
       e.printStackTrace();
       LOG.error(ExceptionUtils.getStackTrace(e));
     } catch (NoSuchPaddingException e) {
       e.printStackTrace();
       LOG.error(ExceptionUtils.getStackTrace(e));
     } catch (IllegalBlockSizeException e) {
       e.printStackTrace();
       LOG.error(ExceptionUtils.getStackTrace(e));
     } catch (BadPaddingException e) {
       e.printStackTrace();
       LOG.error(ExceptionUtils.getStackTrace(e));
     } catch (InvalidAlgorithmParameterException e) {
       e.printStackTrace();
       LOG.error(ExceptionUtils.getStackTrace(e));
     } catch (IOException e) {
       e.printStackTrace();
       LOG.error(ExceptionUtils.getStackTrace(e));
     }
 
     return new Object[] { hash, md.digest() };
   }
 }
