 package com.episparq.remotemailfilter;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.io.Writer;
 import java.math.BigInteger;
 import java.net.InetAddress;
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.security.KeyStore;
 import java.security.Security;
 import java.security.cert.Certificate;
 import java.security.cert.X509Certificate;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Properties;
 import javax.crypto.Cipher;
 import javax.crypto.CipherInputStream;
 import javax.crypto.CipherOutputStream;
 import javax.mail.Flags;
 import javax.mail.Folder;
 import javax.mail.FolderNotFoundException;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.NoSuchProviderException;
 import javax.mail.Session;
 import javax.mail.event.MessageCountEvent;
 import javax.mail.event.MessageCountListener;
 import javax.mail.search.FlagTerm;
 import javax.security.auth.x500.X500Principal;
 
 import com.sun.mail.imap.IMAPFolder;
 import com.sun.mail.imap.IMAPStore;
 import groovy.lang.Binding;
 import groovy.lang.GroovyShell;
 import org.apache.log4j.Logger;
 import org.bouncycastle.jce.provider.BouncyCastleProvider;
 import org.bouncycastle.x509.X509V1CertificateGenerator;
 
 public class RemoteFilter extends Thread {
     // http://svn.assembla.com/svn/SampleCode/gep/src/gep/minmax/UpsideDownShell.groovy
     /*
       * public static Reader getScript() { try { return new FileReader(new
       * File("/Users/thatha/a.groovy")); } catch (Exception e) { return null; } }
       */
 
     private static final Logger logger = Logger.getLogger(RemoteFilter.class);
 
     private IMAPStore store;
 
     public RemoteFilter(String rules, String hostname, String user,
                         String password) throws RemoteFilterException {
         this(rules, hostname, 143, user, password);
     }
 
     public RemoteFilter(String rules, String hostname, int port,
                         String user, String password) throws RemoteFilterException {
         this(rules, hostname, port, user, password, port == 993);
     }
 
     public RemoteFilter(String rules, String hostname, String user,
                         String password, boolean ssl) throws RemoteFilterException {
         this(rules, hostname, (ssl ? 993 : 143), user, password);
     }
 
     private String rules, hostname, user, password;
     private int port;
     private boolean ssl;
 
     public RemoteFilter(String rulesSource, String hostname, int port,
                         String user, String password, boolean ssl)
             throws RemoteFilterException {
         this.setDaemon(true);
 
         rules = rulesSource;
         this.hostname = hostname;
         this.port = port;
         this.user = user;
         this.password = password;
         this.ssl = ssl;
        store = getRemoteStore();
     }
 
     private IMAPStore getRemoteStore() throws RemoteFilterException {
         Session session = Session.getDefaultInstance(new Properties());
         try {
             IMAPStore store = (IMAPStore) session.getStore((ssl ? "imaps"
                     : "imap"));
             store.connect(hostname, port, user, password);
             return store;
         } catch (NoSuchProviderException e) {
             throw new RemoteFilterException(e);
         } catch (MessagingException e) {
             throw new RemoteFilterException(e);
         }
     }
 
     public void processMessage(Message m) {
 
         try {
             logger.info("From: " + m.getFrom()[0] + " Message: " + m.getSubject());
 
             Binding bindings = new Binding();
             bindings.setVariable("m", m);
             GroovyShell groovy = new GroovyShell(bindings);
 
             groovy.evaluate(rules);
 
             String target = (String) groovy.getVariable("target");
 
 
             if (target != null) {
                 logger.info("Moving to " + target);
                 // BEGIN TRANSACTION
                 IMAPFolder f = ((IMAPFolder) store.getFolder(target));
                 if (!f.exists()) {
                     logger.info("Creating " + target);
                     f.create(Folder.HOLDS_FOLDERS | Folder.HOLDS_MESSAGES);
                 }
                 int max = 10;
                 for (int i = 0; i < max; i++) {
                     try {
                         m.getFolder().copyMessages(new Message[]{m}, f);
                         break;
                     } catch (FolderNotFoundException e) {
                         logger.warn("folder appears not to exist retrying " + (i + 1) + " of " + max, e);
                         Thread.sleep(100 * (i + 1));
                     }
                 }
                 // Is there a way to verify the message was copied in order to
                 // fake transactions?
                 m.setFlag(Flags.Flag.DELETED, true);
                 Folder ff = m.getFolder();
 
                 ff.expunge();
                 // COMMIT
                 // ff.open(Folder.READ_WRITE);
             }
         } catch (Exception e) {
             logger.error("error processing message", e);
         }
     }
 
     public boolean processUnread = false;
 
     public boolean processRead = false;
 
     public void run() {
         assert (store != null);
         IMAPFolder inbox;
         try {
             inbox = (IMAPFolder) store.getFolder("INBOX");
             inbox.open(Folder.READ_WRITE);
         } catch (MessagingException e) {
             throw new FatalRemoteFilterError(e);
         }
 
         if (processUnread) {
             // Process unread messages already in the folder.
             try {
                 Message[] unreadMessages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
                 for (Message m : unreadMessages) {
                     processMessage(m);
                 }
             } catch (MessagingException e) {
                 logger.error("error processing unread", e);
             }
         }
 
         if (processRead) {
             // Process unread messages already in the folder.
             try {
                 Message[] readMessages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), true));
                 for (Message m : readMessages) {
                     processMessage(m);
                 }
             } catch (MessagingException e) {
                 logger.error("error processing unread", e);
             }
         }
 
         inbox.addMessageCountListener(new MessageCountListener() {
             public void messagesAdded(MessageCountEvent messageCountEvent) {
                 Message[] messages = messageCountEvent.getMessages();
                 for (Message m : messages) {
                     processMessage(m);
                 }
             }
 
             public void messagesRemoved(MessageCountEvent messageCountEvent) {
                 // Intentionally left empty.
             }
         });
 
         while (true) {
             try {
                 inbox.idle();
             } catch (MessagingException e) {
                 logger.error("error idling, will reconnect", e);
                 try {
                     Thread.sleep(500);
                 } catch (InterruptedException ex) {
                     logger.error("error", ex);
                 }
                 try {
                     store = getRemoteStore();
                 } catch (RemoteFilterException ex) {
                     logger.error("error reconnecting...", ex);
                 }
             } catch (Exception e) {
                 logger.error("error idling, exiting...", e);
                 break;
             }
         }
     }
 
     private static KeyStore.PrivateKeyEntry generateMyCert() throws IOException {
         try {
             Calendar calendar = Calendar.getInstance();
             Date startDate = new Date();
             calendar.setTime(startDate);
             calendar.roll(Calendar.YEAR, true);
             Date expiryDate = calendar.getTime();
 
             logger.info("Generating new RSA1024 keypair...");
 
             Security.addProvider(new BouncyCastleProvider());
             KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
             keyGen.initialize(1024);
             KeyPair keyPair = keyGen.generateKeyPair();
 
             X509V1CertificateGenerator certGen = new X509V1CertificateGenerator();
             X500Principal dnName = new X500Principal(
                     "CN=com.episparq.remotemailfilter."
                             + InetAddress.getLocalHost().getHostName());
 
             certGen.setSerialNumber(BigInteger.valueOf(System
                     .currentTimeMillis()));
             certGen.setIssuerDN(dnName);
             certGen.setNotBefore(startDate);
             certGen.setNotAfter(expiryDate);
             certGen.setSubjectDN(dnName); // note: same as issuer
             certGen.setPublicKey(keyPair.getPublic());
             certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");
 
             X509Certificate cert = certGen.generate(keyPair.getPrivate(), "BC");
             logger.info("generating new self-signed certificate that certifies the keypair");
 
             return new KeyStore.PrivateKeyEntry(keyPair.getPrivate(), new Certificate[]{cert});
 
         } catch (Exception e) {
             logger.error("error", e);
             return null;
         }
     }
 
     private static String fetchPasswordOrAsk() throws IOException {
         // http://stackoverflow.com/questions/727812/storing-username-password-on-mac-using-java
         String password = null;
         KeyStore.PrivateKeyEntry key = null;
 
         try {
             KeyStore keyStore = KeyStore.getInstance("KeychainStore", "Apple");
             keyStore.load(null, null);
 
             key = (KeyStore.PrivateKeyEntry) keyStore.getEntry(
                     "com.episparq.remotemailfilter",
                     new KeyStore.PasswordProtection(" ".toCharArray()));
             if (key == null) {
                 key = generateMyCert();
                 keyStore.setEntry("com.episparq.remotemailfilter", key,
                         new KeyStore.PasswordProtection("-".toCharArray()));
                 keyStore.store(null, null);
             } else {
                 logger.info("Private key loaded: " + key.getPrivateKey().getAlgorithm());
             }
 
             /*
                 * After some experimentation, I was able to access
                 * private-key certificate entries in the KeychainStore. However,
                 * passwords in my Keychain did not show up (no alias was listed),
                 * and when I tried to add a KeyStore.SecretKeyEntry (which is what
                 * you'd need to hold a password) it failed with the message,
                 * "Key is not a PrivateKey". Clearly, Apple has not supported
                 * SecretKeyEntry.
                 */
 
             // Attempt to read the password from the file
             try {
                 Cipher rsa = Cipher.getInstance("RSA");
                 rsa.init(Cipher.DECRYPT_MODE, key.getPrivateKey());
                 BufferedReader is = new BufferedReader(new InputStreamReader(
                         new CipherInputStream(new FileInputStream(
                                 SECRETS_FILENAME), rsa)));
                 password = is.readLine();
                 is.close();
             } catch (Exception e) {
                 logger.error("cannot read password from file ", e);
             }
 
             /**
              * Cipher rsa = Cipher.getInstance("RSA");
              *
              * rsa.init(Cipher.ENCRYPT_MODE, pk); OutputStream os = new
              * CipherOutputStream( new FileOutputStream("encrypted.rsa"), rsa);
              *
              * Writer out = new OutputStreamWriter(os);
              * out.write("Hello World!!"); out.close(); os.close();
              **/
         } catch (Exception e) {
             logger.error("error", e);
         }
 
         if (password == null) {
             password = new String(System.console().readPassword("Password? "));
 
             Cipher rsa;
             try {
                 rsa = Cipher.getInstance("RSA");
                 rsa.init(Cipher.ENCRYPT_MODE, key.getCertificate());
                 Writer os = new OutputStreamWriter(new CipherOutputStream(
                         new FileOutputStream(SECRETS_FILENAME), rsa));
                 os.write(password);
                 os.close();
             } catch (Exception e) {
                 logger.error("cannot store password", e);
             }
 
         }
         return password;
     }
 
     private final static String USER_HOME = System.getProperty("user.home") + "/";
     private final static String PROPERTIES_FILENAME = USER_HOME
             + ".remotefilter.properties";
 
     private final static String SECRETS_FILENAME = USER_HOME
             + ".remotefilter.secrets";
 
     public static void main(String[] args) throws Exception {
 
         Properties properties = new Properties();
         try {
             FileInputStream propertiesStream = new FileInputStream(new File(PROPERTIES_FILENAME));
             properties.load(propertiesStream);
             propertiesStream.close();
         } catch (IOException e) {
             // Unable to load properties from the file. Ask the user.
             // properties = updateSettings(properties);
             throw new RuntimeException("Could not read " + PROPERTIES_FILENAME);
         }
 
         String password = fetchPasswordOrAsk();
 
         InputStream rulesStream = new FileInputStream(new File(USER_HOME + properties.get("rules")));
 
         final char[] buffer = new char[0x10000];
         StringBuilder out = new StringBuilder();
         Reader in = new InputStreamReader(rulesStream, "UTF-8");
         int read;
         do {
             read = in.read(buffer, 0, buffer.length);
             if (read > 0) {
                 out.append(buffer, 0, read);
             }
         } while (read >= 0);
 
         RemoteFilter filter = new RemoteFilter(
                 out.toString(),
                 properties.getProperty("host"),
                 Integer.parseInt((String) properties.get("port")),
                 properties.getProperty("username"),
                 password);
 
         filter.processRead = Boolean.parseBoolean(properties.getProperty("process_read", "false"));
         filter.processUnread = Boolean.parseBoolean(properties.getProperty("process_unread", "false"));
 
         filter.run();
     }
 }
