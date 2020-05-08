 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.math.BigInteger;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.charset.Charset;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.security.spec.InvalidKeySpecException;
 import java.security.spec.InvalidParameterSpecException;
 import java.security.spec.KeySpec;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import javax.crypto.Cipher;
 import javax.crypto.CipherInputStream;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.SecretKey;
 import javax.crypto.SecretKeyFactory;
 import javax.crypto.spec.IvParameterSpec;
 import javax.crypto.spec.PBEKeySpec;
 import javax.crypto.spec.SecretKeySpec;
 import java.util.Date;
 import java.text.SimpleDateFormat;
 
 //Consider saving files with the public key
 public class Model {
 
 	private static final String PWDFILE = "pwdFile.dat";
 	private static final String LOGFILE = "auditLog.dat";
 	private static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
 
 	private static final File ROOTDIR = new File(
 			System.getProperty("user.home"), "CMS_Files");
 
 	//Singleton
 	//private static final Model instance = new Model();
 	private SecretKey secret;
 	private Cipher aesCipher;
 	private byte[] iv;
 
 	private Map<String,char[][]> pwdStore;
 	
 	// Time format
     SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
 
 	public Model(char[] password) {
 		try {
 			aesCipher = Cipher.getInstance("AES");
 			keySetup(password);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		try{
 			this.pwdStoreSetup(password);
 		} catch(Exception e){
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 		}
 
 	}
 
 	private static char[] concat(char[] a, char b[]){
 		char [] c = new char[a.length+b.length];
 		for (int i=0; i<a.length; i++)
 			c[i]=a[i];
 		for (int i=0; i<b.length; i++)
 			c[i+a.length]=b[i];
 		return c;
 	}
 
 	private static char[] sha256(char[] input) {
 		MessageDigest mDigest;
 		byte[] hashBytes = null;
 		char[] hash = null;
 		try {
 			mDigest = MessageDigest.getInstance("SHA-256");
 			byte[] inputBytes = Charset.forName("UTF-8")
 				.encode(CharBuffer.wrap(input)).array();
 			mDigest.update(inputBytes);
 			hashBytes = mDigest.digest();
 			hash = Charset.forName("UTF-8")
 				.decode(ByteBuffer.wrap(hashBytes)).array();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return hash;
 	}
 
 	private static char[] getSalt() {
 		int size = 16;
 		byte[] bytes = new byte[size];
 		new SecureRandom().nextBytes(bytes);
 		return Charset.forName("UTF-8").decode(ByteBuffer.wrap(bytes)).array();
 	}
 
 	private static boolean authenticate(char[] salt, char[] storedHash, char[] plainPass) {
 		char[] saltyPass = sha256(concat(salt,plainPass));
 		return Arrays.equals(saltyPass,storedHash);
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
 
 	private void pwdStoreSetup(char[] adminpass) 
 		throws Exception{
 		try{
 			FileInputStream fin = new FileInputStream(PWDFILE);
 			ObjectInputStream ois = new ObjectInputStream(fin);
 			Object obj = ois.readObject();
 			ois.close();
 			if (obj instanceof HashMap<?,?>)
 				pwdStore = (HashMap<String,char[][]>) obj;
 			else
 				throw new Exception("Type Mismatch on password file");
 		} catch (FileNotFoundException e){
 			pwdStore = new HashMap<String,char[][]>();
 			char[] tempPass = this.createUser("admin","admin".toCharArray());
 			this.changePassword("admin",tempPass,adminpass);
 		}
 
 	}
 
 	private synchronized void pwdStoreUpdate(String username, char[] password, char[] role){	
 		if(pwdStore==null)
 			pwdStore = new HashMap<String,char[][]>();
 
 		char[]   salt = getSalt();
 		char[]   pwdHash = sha256(concat(salt,password));
 		char[][] value = { salt, pwdHash, role };
 		pwdStore.put(username,value);
 
 		try{
 			FileOutputStream fout = new FileOutputStream(PWDFILE);
 			ObjectOutputStream oos = new ObjectOutputStream(fout);
 			oos.writeObject( pwdStore );
 			oos.close();
 		} catch (Exception e){
 			e.printStackTrace();
 		}
 
 	}
 
 	private static void copy(InputStream input, OutputStream output, int size)
 		throws IOException {
 		byte[] buffer = new byte[1024*4];
 		int countdown = size;
 		int n = 0;
		while (countdown >= 200) {
 			n = input.read(buffer, 0, 1024*4);
 			output.write(buffer, 0, n);
 			countdown -= n;
 		}
 	}
 
 	private static long copy(InputStream input, OutputStream output)
 		throws IOException {
 		byte[] buffer = new byte[1024 * 4];
 		long count = 0;
 		int n = 0;
 		int lastn = 0;
 		while (-1 <= (n = input.read(buffer))) {
 			output.write(buffer, 0, n);
 			count += n;
 			if (lastn > n)
 				break;
 			lastn = n;
 		}
 		return count;
 	}
 
 	private void storeFileHash(String filename) {
 		//TODO
 		//Need to store hash of original stream contents and not the hash of the encrypted file
 	}
 
 	public String[] login(String username, char[] password)
 		throws Exception {
 		String identity = null;
 		char [] salt = null;
 		char [] storedHash = null;
 		char [] role = null;
 		char [][] storedValue = pwdStore.get(username);
 
 		try{
 			salt = storedValue[0];
 			storedHash = storedValue[1];
 
 		} catch (NullPointerException e){
 			throw new Exception("No such user");
 		}
 
 		if( authenticate(salt, storedHash, password) ){
 			identity = username; //TODO can something better be done here?
 			role = storedValue[2];
 			auditLogUpdate(username, "login");
 		}
 		else
 			throw new Exception("invalpass");
 
 		String[] returnVal = {identity,new String(role)};
 		return returnVal;
 
 	}
 
 	public char[] createUser(String username, char[] role){
 		//char[] newpass = Long.toHexString(Double.doubleToLongBits(
 		//			new SecureRandom().nextDouble())).toCharArray();
 		//byte[] newpassbytes = new BigInteger(100,new SecureRandom())
 		//	.toByteArray();
 
 		//char[] newpass  = Charset.forName("UTF-8")
 		//	.decode(ByteBuffer.wrap(newpassbytes)).array();
 		String newpassStr = new BigInteger(50,new SecureRandom()).toString(32);
 		char[] newpass = newpassStr.toCharArray();
 		pwdStoreUpdate(username, newpass, role);
 
 		new File(ROOTDIR,username).mkdirs();
 		
 		auditLogUpdate("Admin", "createUser: " + username);
 
 		return newpass;
 
 	}
 
 	public void changePassword(String username, char[] oldPassword, char[] newPassword)
 		throws Exception{
 		String[] credentials = login(username,oldPassword);
 		pwdStoreUpdate(username,newPassword,credentials[1].toCharArray());
 		
 		auditLogUpdate(username, "change password");
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
 
 	//For Debugging Only
 	public void decrypt(String infilename, String outfilename, String username) {
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
 	
 	// Audit log
 	private void auditLogUpdate(String username, String info) {
 		try {
 			File auditLog = new File(LOGFILE);
 			FileOutputStream os = new FileOutputStream(auditLog, true);
 			
 			Date date = new Date();
 			String result = "User: " + username + '\n' + info + '\n' + sdf.format(date) + '\n' + '\n';
 
 			os.write(result.getBytes());
 			
 			os.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public static void main(String[] args) {
 		Model model = new Model("123456".toCharArray());
 
 		String[] credentials= null;
 		String   identity = null;
 		String	 role = null;
 		try{
 			credentials = model.login("admin", "123456".toCharArray());
 			role = credentials[1];
 			identity = credentials[0];
 		} catch (Exception e) {
 			if (e.getMessage().equals("invalpass") || e.getMessage().equals("No such user"))
 				System.out.println("Failed successfully");
 			else{
 				System.out.println("Failed at failing");
 				e.printStackTrace();
 			}
 		}
 
 		System.out.println("Identitiy was: "+identity);
 		System.out.println("Role was: "+role);
 
 		try{
 			File testFile = new File("test.pdf");
 			model.saveFile(new FileInputStream(testFile),"test.out.pdf",(int)testFile.length(),"admin");
 		} catch (Exception e){
 			e.printStackTrace();
 		}
 
 	}
 
 }
