 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.nio.CharBuffer;
 import java.nio.charset.Charset;
 import java.security.NoSuchAlgorithmException;
 import java.security.spec.InvalidKeySpecException;
 import java.security.spec.InvalidParameterSpecException;
 import java.security.spec.KeySpec;
 import javax.crypto.Cipher;
 import javax.crypto.CipherInputStream;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.SecretKey;
 import javax.crypto.SecretKeyFactory;
 import javax.crypto.spec.IvParameterSpec;
 import javax.crypto.spec.PBEKeySpec;
 import javax.crypto.spec.SecretKeySpec;
 
 public class FileStorage {
 
 	private static final File ROOTDIR = new File(
 			System.getProperty("user.home"), "CMS_Files");
 
 	private SecretKey secret;
 	private Cipher aesCipher;
 	private byte[] iv;
 
 	private UserAuthenticator userAuth;
 	private AuditLog auditLog;
 
 	// Time format
 
 	public FileStorage(char[] password) {
 		try {
 			aesCipher = Cipher.getInstance("AES");
 			keySetup(password);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	private void keySetup(char[] password) throws NoSuchAlgorithmException,
 			NoSuchPaddingException, InvalidKeySpecException,
 			InvalidParameterSpecException {
 
 				//Use password as salt effectively not using salt. Fix later. //TODO
 				byte[] salt = Charset.forName("UTF-8")
 					.encode(CharBuffer.wrap(password)).array();
 				SecretKeyFactory factory = SecretKeyFactory
 					.getInstance("PBKDF2WithHmacSHA1");
 				KeySpec spec = new PBEKeySpec(password, salt, 65536, 256);
 				SecretKey tmp = factory.generateSecret(spec);
 				secret = new SecretKeySpec(tmp.getEncoded(), "AES");
 				aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
 	}
 
 	private static void copy(InputStream input, OutputStream output, int size)
 		throws IOException {
 		byte[] buffer = new byte[1024*4];
 		int countdown = size;
 		int n = 0;
		while (countdown >= 20) {
 			n = input.read(buffer, 0, 1024*4);
 			output.write(buffer, 0, n);
 			countdown -= n;
 		}
 	}
 
 	private void storeFileHash(String filename) {
 		//TODO
 		//Need to store hash of original stream contents and not the hash of the encrypted file
 	}
 
 	public void saveFile(InputStream is, String filename, int filesize,
 			String username) {
 		//Initialize Cipher
 		try {
 			aesCipher.init(Cipher.ENCRYPT_MODE, secret);
 			iv = aesCipher.getParameters()
 				.getParameterSpec(IvParameterSpec.class).getIV();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		//Keep file in user directory
 		File userhome = new File(ROOTDIR, username);
 		//if the user home doesn't exist, something is wrong. get out.
 		if (!userhome.exists())
 			return;
 
 
 		try {
 			//file is just the filename in the user's home directory.
 			File outputFile = new File(userhome, new File(filename).getName());
 
 			//Set up streams
 			FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
 			CipherInputStream cis = new CipherInputStream(is, aesCipher);
 			//InputStream cis = is;
 
 			//Save the file
 			copy(cis, fileOutputStream, filesize);
 
 			//Save its hash
 			storeFileHash(filename);
 
 			//Close the streams
 			fileOutputStream.close();
 
 			this.decrypt(new File(filename).getName(),new File(filename).getName()+".decrypt",username);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public void createUser(String username){
 		new File(ROOTDIR,username).mkdirs();
 	}
 
 	//For Debugging Only
 	private void decrypt(String infilename, String outfilename, String username) {
 		try {
 			aesCipher
 				.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
 
 			File userHome = new File(ROOTDIR,username);			
 			File out = new File(userHome,outfilename);
 			File in = new File(userHome,infilename);
 			CipherInputStream is = new CipherInputStream(
 					new FileInputStream(in), aesCipher);
 			FileOutputStream os = new FileOutputStream(out);
 
 			copy(is, os, (int)in.length());
 
 			os.close();
 			is.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public static void main(String[] args) {
 
 	}
 
 
 }
