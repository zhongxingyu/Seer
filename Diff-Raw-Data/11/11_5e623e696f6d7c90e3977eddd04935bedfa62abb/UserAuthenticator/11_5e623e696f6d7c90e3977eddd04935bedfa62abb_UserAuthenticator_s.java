 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.math.BigInteger;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.charset.Charset;
 import java.security.MessageDigest;
 import java.security.SecureRandom;
 import java.util.Arrays;
 import java.util.HashMap;
 
 public class UserAuthenticator {
 
 	private static final String PWDFILE = "pwdFile.dat";
 
 	private HashMap<String,char[][]> pwdStore;
 
 	public UserAuthenticator(char[] password) {
 
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
 		char[][] value = pwdStore.get(username);
 		boolean isOneTime = false;
 		try{
 			isOneTime = (value[3][0]=='t');
 		} catch (NullPointerException e){
 			//NOP
 		}
 		pwdStoreUpdate(username,password,role,isOneTime);
 	}
 
 	private synchronized void pwdStoreUpdate(String username, char[] password, char[] role,
 			boolean isOneTime){	
 		char[]   salt = getSalt();
 		char[]   pwdHash = sha256(concat(salt,password));
 		char[] isOTchar = "f".toCharArray();
 		if ( isOneTime )
 			isOTchar = "t".toCharArray();
 		char[][] value = { salt, pwdHash, role, isOTchar };
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
 	public String[] login(String username, char[] password)
 		throws Exception {
 		String identity = null;
 		char [] salt = null;
 		char [] storedHash = null;
 		char [] role = null;
 		char [] isOneTime = null;
 		char [][] storedValue = pwdStore.get(username);
 
 		try{
 			salt = storedValue[0];
 			storedHash = storedValue[1];
 			isOneTime = storedValue[3];
 
 			if( authenticate(salt, storedHash, password) ){
 				identity = username;
 				role = storedValue[2];
 			}
 			else {
 				throw new Exception("invalpass");
 			}
 		} catch (Exception e){
 			Thread.sleep(1000);
 			throw new Exception("Login Failed");
 		}


 		String[] returnVal = {identity,new String(role),new String(isOneTime)};
 		return returnVal;
 
 	}
 
 	public char[] createUser(String username, char[] role){
 		String newpassStr = new BigInteger(50,new SecureRandom()).toString(32);
 		char[] newpass = newpassStr.toCharArray();
 		pwdStoreUpdate(username, newpass, role,true);
 
 		return newpass;
 
 	}
 
 	public void changePassword(String username, char[] oldPassword, char[] newPassword)
 		throws Exception{
 		String[] credentials = login(username,oldPassword);
 		pwdStoreUpdate(username,newPassword,credentials[1].toCharArray(),false);
 
 	}
 
 	public static void main(String[] args) {
 		UserAuthenticator model = new UserAuthenticator("123456".toCharArray());
 		String[] login_out;
 
 		try{
 			model.login("admin", "123456".toCharArray());
 
 			char[] alicepass = model.createUser("alice","Student".toCharArray());
 			model.changePassword("alice",alicepass,"alicepass".toCharArray());
 			login_out = model.login("alice","alicepass".toCharArray());
 			System.out.println(login_out[2]);
 
 		} catch (Exception e) {
 			if (e.getMessage().equals("invalpass") || e.getMessage().equals("No such user"))
 				System.out.println("Failed successfully");
 			else{
 				System.out.println("Failed at failing");
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 }
