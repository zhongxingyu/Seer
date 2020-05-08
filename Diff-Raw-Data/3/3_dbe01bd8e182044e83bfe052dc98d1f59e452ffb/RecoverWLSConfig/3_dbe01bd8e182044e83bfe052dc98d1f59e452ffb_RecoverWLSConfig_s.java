 package uitls;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.util.Arrays;
 import javax.crypto.SecretKeyFactory;
 import java.security.Security;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.crypto.Cipher;
 import javax.crypto.spec.IvParameterSpec;
 import javax.crypto.spec.PBEKeySpec;
 import javax.crypto.spec.SecretKeySpec;
 import org.bouncycastle.jce.provider.BouncyCastleProvider;
 import org.bouncycastle.util.encoders.Base64;
 
 /**
  *
 * @author Ted <xiaox.j AT gmail.com>
  * 
  */
 public class RecoverWLSConfig {
 
     public static final String PASSPHASE = "0xccb97558940b82637c8bec3c770f86fa3a391a56";
     //1+4+1+1+32=39
     public static final int FILESIZE = 39;
     public static final int SALT_SIZE = 4;
     public static final int SECURITY_KEY_SIZE = 32;
     //match string like {3DES}N90kGJYiVZ4JwgAAGuCHBA==
     protected static final String REGEX_FOR_DES = "\\{3DES\\}(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?";
     private Cipher decryptCipher;
 
     public RecoverWLSConfig(File key) throws Exception {
         decryptCipher = getDecryptCipher(key);
         if (decryptCipher == null) {
             throw new Exception("Invalid key file");
         }
     }
 
     private Cipher getDecryptCipher(File key) {
         if (key.length() != FILESIZE) {
             System.err.println("Invalid SerializedSystemIni file size");
             return null;
         }        
         byte[] data;
         try (FileInputStream fileinputstream = new FileInputStream(key)) {
             data = new byte[(int) key.length()];
             fileinputstream.read(data);
             fileinputstream.close();
             //validate  content
             if (data[0] != SALT_SIZE || data[SALT_SIZE + 1] != 1 || data[SALT_SIZE + 2] != SECURITY_KEY_SIZE) {
                 System.out.println("salt size:" + data[0] + "version:" + data[SALT_SIZE + 1] + "keysize:" + data[SALT_SIZE + 2]);
                 System.err.println("Invalid SerializedSystemIni file content");
                 return null;
             }
             byte[] salt = new byte[SALT_SIZE];
             System.arraycopy(data, 1, salt, 0, SALT_SIZE);
             byte[] secKey = Arrays.copyOfRange(data, 7, 39);
             char passphase[] = new char[PASSPHASE.length()];
             PASSPHASE.getChars(0, passphase.length, passphase, 0);
             //PBE/SHA1/RC2/CBC/PKCS12PBE-5-128
             String keyAlgo = "PBEWITHSHAAND128BITRC2-CBC";
             SecretKeyFactory keyFact = SecretKeyFactory.getInstance(keyAlgo);
             PBEKeySpec keySpec = new PBEKeySpec(passphase, salt, 5);
             Cipher cipher = Cipher.getInstance(keyAlgo);
             cipher.init(Cipher.DECRYPT_MODE, keyFact.generateSecret(keySpec));
             SecretKeySpec skeySpec = new SecretKeySpec(cipher.doFinal(secKey), "DES");
             //doule the salt for DES
             byte[] dessalt = new byte[8];
             System.arraycopy(salt, 0, dessalt, 0, 4);
             System.arraycopy(salt, 0, dessalt, 4, 4);
             IvParameterSpec ivSpec = new IvParameterSpec(dessalt);
             Cipher outCipher = Cipher.getInstance("DESEDE/CBC/PKCS5Padding");
             outCipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
             return outCipher;
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         }
     }
 
     public String decrypt(String passwd) {
         String plianText = "";
         try {
             byte[] input = Base64.decode(passwd);
             byte[] res = decryptCipher.doFinal(input);
             plianText = new String(res, "UTF-8");
         } catch (Exception e) {
             e.printStackTrace();
         }
         return plianText;
     }
 
     public static void main(String[] args) throws Exception {
         if (args.length < 2) {
             throw new Exception("Usage: [/path/SerializedSystemIni.dat] [/path/configFile]");
         }
         //add BouncyCastleProvider or set java.security file in jre/lib/security
         Security.addProvider(new BouncyCastleProvider());
         RecoverWLSConfig service = new RecoverWLSConfig(new File(args[0]));
         File configFile = new File(args[1]);
         BufferedReader in = new BufferedReader(new FileReader(configFile));
         String line;
         Pattern pattern = Pattern.compile(REGEX_FOR_DES);
         while ((line = in.readLine()) != null) {
             //System.out.println("line:" + line);
             Matcher m = pattern.matcher(line);
             while (m.find()) {
                 String encoded = line.substring(m.start(), m.end());
                 String decoded = service.decrypt(encoded.substring(6));
                 line = line.replace(encoded, decoded);
                 m.reset(line);
             }
             System.out.println(line);
         }
 
     }
 }
