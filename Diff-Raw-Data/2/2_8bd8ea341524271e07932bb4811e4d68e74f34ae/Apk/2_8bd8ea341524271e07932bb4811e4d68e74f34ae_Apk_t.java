 package uk.ac.cam.db538.dexter.apk;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.security.PrivateKey;
 import java.security.cert.X509Certificate;
 import java.util.Enumeration;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipOutputStream;
 
 import com.rx201.jarsigner.JarSigner;
 import com.rx201.jarsigner.KeyGenerator;
 
 public class Apk {
     private static final String ManifestFile = "AndroidManifest.xml";
     private static final String SignatureFile = "resources.arsc";
     private static final String ClassesDex = "classes.dex";
     private static final String MetaInfo = "META-INF";
 
     public static Manifest getManifest(File apkFile) throws IOException {
         JarFile apk = null;
         try {
             apk = new JarFile(apkFile);
             JarEntry entry = (JarEntry) apk.getEntry(ManifestFile);
             if (entry == null)
             	throw new FileNotFoundException("Manifest file not found inside APK");
             return new Manifest(apk.getInputStream(entry));
         } finally {
             if (apk != null)
                 apk.close();
         }
     }
     
     public static SignatureFile getSignatureFile(File apkFile) throws IOException {
         JarFile apk = null;
         try {
             apk = new JarFile(apkFile);
             JarEntry entry = (JarEntry) apk.getEntry(SignatureFile);
             if (entry == null)
             	throw new FileNotFoundException("Signature file not found inside APK");
             return new SignatureFile(entry, apk);
         } finally {
             if (apk != null)
                 apk.close();
         }
     }
 
     private static KeyGenerator keyGenerator = new KeyGenerator();
 
     public static void produceAPK(File originalFile, File destinationFile, Manifest newManifest, byte[] dexData) throws IOException {
 
         // originalFile ---(replacing content)--->  workingFile --(signing)--> destinationFile
         File workingFile = File.createTempFile("dexter-", ".apk");
 
         ZipFile originalAPK = null;
         ZipOutputStream workingAPK = null;
         try {
             byte[] buffer = new byte[16*1024];
 
             originalAPK = new ZipFile(originalFile);
             workingAPK = new ZipOutputStream(new FileOutputStream(workingFile));
 
             // Create intermediate apk with new classes.dex and AndroidManifest.xml, excluding
             // old signature data
             Enumeration<? extends ZipEntry> entries = originalAPK.entries();
             while (entries.hasMoreElements()) {
                 ZipEntry entry = (ZipEntry) entries.nextElement();
                 String name = entry.getName();
 
                 ZipEntry newEntry = new ZipEntry(name);
                 
                 InputStream data = null;
                 if (name.equals(ManifestFile) && newManifest != null) {
                     data = newManifest.getDataStream();
                 } else if (name.equals(ClassesDex) && dexData != null) {
                     data = new ByteArrayInputStream(dexData);
                 } else if (name.startsWith(MetaInfo)) {
                     newEntry = null;
                 } else {
                     // If original entry is not compressed, we need to maintain this.
                     // New entry needs  a few other fields accordingly as well.
                     // So just use the copy constructor.
                    if (entry.getMethod() == ZipEntry.STORED)
                         newEntry = new ZipEntry(entry);
                     
                     data = originalAPK.getInputStream(entry);
                 }
 
                 if (newEntry != null) {
                     workingAPK.putNextEntry(newEntry);
                     int len;
                     while ((len = data.read(buffer)) > 0) {
                         workingAPK.write(buffer, 0, len);
                     }
                     workingAPK.closeEntry();
                 }
             }
             workingAPK.close();
             workingAPK = null;
             originalAPK.close();
             originalAPK = null;
 
             X509Certificate[] certChain = keyGenerator.getCertificateChain();
             PrivateKey privateKey = keyGenerator.getPrivateKey();
 
             JarSigner.sign(workingFile, destinationFile, "DEXTER", certChain, privateKey);
         } catch (ZipException e) {
             throw new IOException(e);
         } finally {
             if (originalAPK != null)
                 originalAPK.close();
             if (workingAPK != null)
                 workingAPK.close();
             workingFile.delete();
         }
 
     }
 }
