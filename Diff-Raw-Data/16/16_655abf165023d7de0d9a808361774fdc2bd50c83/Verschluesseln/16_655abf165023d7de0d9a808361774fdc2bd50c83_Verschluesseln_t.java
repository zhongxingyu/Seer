 package gBriefe;
 
 import javax.crypto.*;
 import javax.crypto.spec.*;
 
 public class Verschluesseln {
 
   final private transient static String password = "jeLaengerJeBesserPasswortRehaVerwaltung";
   final private transient byte [] salt = { (byte) 0xc9, (byte) 0xc9,(byte) 0xc9,(byte) 0xc9,(byte) 0xc9,(byte) 0xc9,(byte) 0xc9,(byte) 0xc9};
   private final int iterations = 3;
 
   protected Verschluesseln() {
 //    java.security.Security.addProvider(new com.sun.crypto.provider.SunJCE()); // implizit bereits erledigt!
   }
 
   /** instance */
   private static Verschluesseln instance;
 
   /** Singleton Factory
    * @return instance
    */
   public static Verschluesseln getInstance () {
     if (instance == null) {
       instance = new Verschluesseln ();
     }
     return instance;
 
   }
 
 
   /** Notwendige Instanczen */
   private Cipher encryptCipher;
   private Cipher decryptCipher;
   private sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
   private sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
 
   /** Verwendete Zeichendecodierung */
   private String charset = "UTF16";
 
   /**
   * Initialisiert den Verschlüsselungsmechanismus
    * @param pass char[]
    * @param salt byte[]
    * @param iterations int
    * @throws SecurityException
    */
   public void init (final char[] pass, final byte[] salt, final int iterations) throws SecurityException {
     try {
       final PBEParameterSpec ps = new PBEParameterSpec(salt, 20);
       final SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
       final SecretKey k = kf.generateSecret(new PBEKeySpec(pass));
       encryptCipher = Cipher.getInstance("PBEWithMD5AndDES/CBC/PKCS5Padding");
       encryptCipher.init (Cipher.ENCRYPT_MODE, k, ps);
       decryptCipher = Cipher.getInstance("PBEWithMD5AndDES/CBC/PKCS5Padding");
       decryptCipher.init (Cipher.DECRYPT_MODE, k, ps);
     }
     catch (Exception e) {
       throw new SecurityException("Could not initialize CryptoLibrary: " +
                                   e.getMessage());
     }
   }
 
   /**
   * Verschlüsselt eine Zeichenkette
    *
    * @param str Description of the Parameter
    * @return String the encrypted string.
    * @exception SecurityException Description of the Exception
    */
   public synchronized String encrypt(String str) throws SecurityException {
     try {
       byte[] b = str.getBytes(this.charset);
       byte[] enc = encryptCipher.doFinal(b);
       return encoder.encode(enc);
     }
     catch (Exception e){
       throw new SecurityException("Could not encrypt: " + e.getMessage());
     }
 
   }
 
   /**
   * Entschlüsselt eine Zeichenkette, welche mit der Methode encrypt
   * verschlüsselt wurde.
    *
    * @param str Description of the Parameter
    * @return String the encrypted string.
    * @exception SecurityException Description of the Exception
    */
   public synchronized String decrypt(String str) throws SecurityException  {
     try {
       byte[] dec = decoder.decodeBuffer(str);
       byte[] b = decryptCipher.doFinal(dec);
       return new String(b, this.charset);
     }
     catch (Exception e) {
       throw new SecurityException("Could not decrypt: " + e.getMessage());
     }
   }
   /*
   public static void main (final String [] ignored) {
     Verschluesseln man = Verschluesseln.getInstance();
     man.init(man.password.toCharArray(), man.salt, man.iterations);
     final String encrypted = man.encrypt("Bastie");
    System.out.println ("Verschlüsselt :"+encrypted);
     final String decrypted = man.decrypt (encrypted);
    System.out.println("Entschlüsselt :"+decrypted);
   }
   */
 
 public byte [] getSalt() {
 	return salt;
 }
 
 public int getIterations() {
 	return iterations;
 }
 
 public static String getPassword() {
 	return password;
 }
 
 }
