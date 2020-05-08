 package com.xenosync.strongbag;
 
 import java.io.*;
 import java.math.BigInteger;
 import java.security.*;
 import java.security.cert.CertificateEncodingException;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 
 import com.typesafe.config.Config;
 import com.typesafe.config.ConfigFactory;
 import org.bouncycastle.jce.X509Principal;
 import org.bouncycastle.x509.X509V3CertificateGenerator;
 import org.joda.time.DateTime;
 
 import javax.crypto.SecretKey;
 
 public class KeystoreManager {
     private KeyStore rsakey;
     private Config conf;
     private String passphrase;
 
     KeystoreManager() throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, SignatureException, NoSuchProviderException, InvalidKeyException {
         Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
         conf = ConfigFactory.parseFile(new File("application.conf"));
         passphrase = requestPassword();
 
         if(!rsaExists()){
             createRsaStore();
         }
     }
 
     public PrivateKey getPrivateKey() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
         return (PrivateKey) rsakey.getKey(conf.getString("rsa.alias"), passphrase.toCharArray());
     }
 
     public X509Certificate getCertificate() throws KeyStoreException {
         return (X509Certificate) rsakey.getCertificate(conf.getString("rsa.alias"));
     }
 
     public KeyPair getKeypair() throws KeyStoreException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, IOException {
         rsakey = KeyStore.getInstance("JKS");
         File store = new File(conf.getString("homedir.home"), conf.getString("rsa.store_name"));
         rsakey.load(new FileInputStream(store), passphrase.toCharArray());
         return new KeyPair(getCertificate().getPublicKey(), getPrivateKey());
     }
 
     private boolean rsaExists(){
         File f = new File(conf.getString("homedir.home"), conf.getString("rsa.store_name"));
         return f.exists();
     }
 
     private boolean aesExists(){
         File f = new File(conf.getString("homedir.home"), conf.getString("aes.store_name"));
         return f.exists();
     }
 
     private void createRsaStore() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, InvalidKeyException, NoSuchProviderException, SignatureException {
         System.out.println("Creating new RSA keystore");
         KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
         gen.initialize(conf.getInt("rsa.key_length"));
         KeyPair pair = gen.genKeyPair();
         PrivateKey key = pair.getPrivate();
         X509Certificate cert = generateCertificate(pair);
         X509Certificate[] chain = {cert};
 
         File store = new File(conf.getString("homedir.home"), conf.getString("rsa.store_name"));
         store.createNewFile();
         KeyStore ks = KeyStore.getInstance("JKS");
         ks.load(null,null);
         ks.setKeyEntry(conf.getString("rsa.alias"), key, passphrase.toCharArray(), chain);
         ks.store(new FileOutputStream(store), passphrase.toCharArray());
         System.out.println("The Password entered is: " + passphrase);
         System.out.println("You should keep the password somewhere safe, it is not recoverable");
     }
 
     private void createAesStore() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
         System.out.println("Creating new AES keystore");
         File store = new File(conf.getString("homedir.home"), conf.getString("aes.store_name"));
         store.createNewFile();
     }
 
     private X509Certificate generateCertificate(KeyPair keyPair) throws NoSuchAlgorithmException, CertificateEncodingException, NoSuchProviderException, InvalidKeyException, SignatureException {
         DateTime dt = new DateTime();
 
         X509V3CertificateGenerator cert = new X509V3CertificateGenerator();
         cert.setSerialNumber(BigInteger.valueOf(1));
         cert.setSubjectDN(new X509Principal("CN=localhost"));
         cert.setIssuerDN(new X509Principal("CN=localhost"));
         cert.setPublicKey(keyPair.getPublic());
         cert.setNotBefore(dt.toDate());
         cert.setNotAfter(dt.plusYears(10).toDate());
         cert.setSignatureAlgorithm("SHA256WithRSA");
         PrivateKey signingKey = keyPair.getPrivate();
         return cert.generate(signingKey, "BC");
     }
 
     private String requestPassword() throws IOException {
         System.out.println("Enter a password for the keystore: ");
         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
         String pass = br.readLine();
         return pass;
     }
 
     public void writeKey(SecretKey key, String alias) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
         if(!aesExists()){
             createAesStore();
         }
 
        File store = new File(conf.getString("homedir.home"), conf.getString("aes.store_name"));
        KeyStore ks = KeyStore.getInstance("JCEKS");
        ks.load(new FileInputStream(store), passphrase.toCharArray());
         ks.setKeyEntry(alias, key.getEncoded(), null);
         ks.store(new FileOutputStream(store), passphrase.toCharArray());
     }
 }
