 package au.com.jaylin.testpgp;
 
 import java.io.File;
 
 import org.bouncycastle.openpgp.PGPCompressedData;
 import org.bouncycastle.openpgp.PGPEncryptedData;
 
 /*
  * Test class enables file-based PGP encryption using the 'bouncy castle library'.
  * 
  * This class enables you to specify a PGP public key file and a plaintext file
  * for encryption and also a PGP private key file (and password) and cyphertext
  * file for decryption.
  * 
  * Comment-out the parts not required on each run.
  * 
  * REQUIREMENTS: PGPFileProcessor, PGPUtils (from same package). 
  */
 public class TestPGP {
     private static final String PASSPHRASE = "test";   //this is the private key password
 
     private static final String DE_INPUT = "cyphertext.dat";
     private static final String DE_OUTPUT = "decrypted.dat";
     private static final String DE_KEY_FILE = "testPGPPrivate.txt";
 
     private static final String E_INPUT = "cleartext.xml";
     private static final String E_OUTPUT = "cyphertext.dat";
     private static final String E_KEY_FILE = "testPGPPublic.txt"; //from project
 
 
     /*
      * Currently only setup for the decrypt method to work - need to sort out the files for encrypt!
      */
     public static void main(String[] args) {
         String de_input = "";
         String de_output = "";
         String e_input = "";
         String e_output = "";
         String secret_key_file = "";
         String public_key_file = "";
         String passphrase = PASSPHRASE;
 
        System.out.println("testing PGP encryption...");	
 
         File directory = new File (".");
         try {
             System.out.println ("Current directory's canonical path: " + directory.getCanonicalPath()); 
             System.out.println ("Current directory's absolute  path: " + directory.getAbsolutePath());
 
             de_input = directory.getCanonicalPath() + "/" + DE_INPUT;
             de_output = directory.getCanonicalPath() + "/" + DE_OUTPUT;
             secret_key_file = directory.getCanonicalPath() + "/" + DE_KEY_FILE;
             e_input = directory.getCanonicalPath() + "/" + E_INPUT;
             e_output = directory.getCanonicalPath() + "/" + E_OUTPUT;
             public_key_file = directory.getCanonicalPath() + "/" + E_KEY_FILE;
         }
         catch (Exception e) {
             System.out.println("Exception is ="+e.getMessage());
         }
 
         try {
             System.out.println("Encyrpting:\t\t" + e_input);
             testEncrypt(e_input, e_output, public_key_file, passphrase);
 
             System.out.println("Decrypting...");
             System.out.println("input file:\t\t" + de_input);
             System.out.println("output file:\t\t" + de_output);
             System.out.println("secret key file:\t" + secret_key_file);
 
             testDecrypt(de_input, de_output, secret_key_file);
         }
         catch (Exception e) {
             e.printStackTrace();
         }
     }
	
     public static void testDecrypt(String fileIn, String fileOut, String secretKeyFile) throws Exception {
         PGPFileProcessor p = new PGPFileProcessor();
         p.setInputFileName(fileIn);
         p.setOutputFileName(fileOut);
         p.setPassphrase(PASSPHRASE);
         p.setSecretKeyFileName(secretKeyFile);
         System.out.println(p.decrypt());
     }
 
     public static void testEncrypt(String fileIn, String fileOut, String publicKeyFile, String passphrase) throws Exception {
         PGPFileProcessor p = new PGPFileProcessor();
         p.setInputFileName(fileIn);
         p.setOutputFileName(fileOut);
         p.setPassphrase(passphrase);
         p.setPublicKeyFileName(publicKeyFile);
        p.setAsciiArmored(true);  //Makese the cyphertext asicc instead of binary
         System.out.println(p.encrypt(PGPCompressedData.UNCOMPRESSED, PGPEncryptedData.CAST5));  //PGPCompressedData.ZIP
     }
 }
