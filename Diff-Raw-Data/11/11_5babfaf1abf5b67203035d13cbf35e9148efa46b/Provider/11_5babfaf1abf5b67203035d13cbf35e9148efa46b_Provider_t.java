 package providerPackage;
 
 import java.io.*;
 import java.net.*;
 import java.security.KeyStore;
 import java.security.NoSuchAlgorithmException;
 import java.util.Arrays;
 
 import javax.crypto.*;
 import javax.crypto.spec.PBEKeySpec;
 import javax.crypto.spec.PBEParameterSpec;
 import javax.net.*;
 import javax.net.ssl.*;
 import javax.security.cert.X509Certificate;
 import sun.misc.Queue;
 
 public class Provider extends ProviderServer {
 
 	//Top level storage directory
 	public static String path = "./storage";
 	
 	//Logged in User's current working directory
 	public static String cwd = "./storage";
 	
 	//Default port on which the User contacts the provider
 	private static int DefaultServerPort = 32770;
 	
	private static String currentUser = "";
	private static String currentPwd="";
	
 	// A series of error codes for returning errors in a consistent manner
 	private static final int AUTHORIZATION_ERROR = 1;
 	private static final int FILE_ERROR = 2;
 	private static final int BANK_ERROR = 4;
 
 	/**
 	 * Constructs a ClassFileServer.
 	 * 
 	 * @param path
 	 *            the path where the server locates files
 	 */
 	public Provider(ServerSocket ss) throws IOException {
 		super(ss);
 	}
 
 	/**
 	 * Main method to create the Provider server. To start up the server: <br>
 	 * <br>
 	 * 
 	 * <code>java Provider</code><br>
 	 * <br>
 	 */
 	public static void main(String args[]) {
 		int port = DefaultServerPort;
 
 		try {
 			SSLServerSocketFactory ssf = null;
 			try {
 				// set up key manager to do server authentication
 				SSLContext ctx;
 				KeyManagerFactory kmf;
 				KeyStore ks;
 				char[] passphrase = "123456".toCharArray();
 
 				ctx = SSLContext.getInstance("TLS");
 				kmf = KeyManagerFactory.getInstance("SunX509");
 				ks = KeyStore.getInstance("JKS");
 
 				ks.load(new FileInputStream("src/providerPackage/keystore.jks"),
 						passphrase);
 				kmf.init(ks, passphrase);
 				ctx.init(kmf.getKeyManagers(), null, null);
 
 				ssf = ctx.getServerSocketFactory();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 			ServerSocket ss = ssf.createServerSocket(port);
 			((SSLServerSocket) ss).setNeedClientAuth(true);
 
 			new Provider(ss);
 		} catch (IOException e) {
 			System.out.println("Unable to start Provider: " + e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Handles all the I/O with a connected User client.
 	 * 
 	 * @param s
 	 *            connected socket
 	 */
 	@Override
 	public void handleUser(Socket s) throws IOException {
 		PrintWriter out = new PrintWriter(s.getOutputStream(), true);
 		BufferedReader in = new BufferedReader(new InputStreamReader(
 				s.getInputStream()));
 
 		boolean createSession = false, enterPwd = false, reEnterPwd = false;
 		boolean loginSession = false, loginPwd = false, loggedIn = false;
 		boolean sending = false, removing = false;
 
 		String tempPwd = "", username = "", filename="";
 		String inputLine, outputLine;
 
 		outputLine = "Welcome to the Cloud Bank. Login or Create Account?";
 		out.println(outputLine);
 
 		while ((inputLine = in.readLine()) != null) {
 			System.out.println(inputLine);
 			if(inputLine.equals("") || inputLine.equals(" "))
 			{
 				continue;
 			}
 			// Creating an account
 			
 			if (inputLine.equalsIgnoreCase("create")
 					|| inputLine.equalsIgnoreCase("create account")) {
 				outputLine = "Enter Username:";
 				createSession = true;
 			} else if (createSession) {
 				if (checkFolderExists(inputLine)) {
 					outputLine = "Username " + inputLine
 							+ " already exists. Pick another username: ";
 				} else {
 					cwd = cwd + "/" + inputLine;
 					createSession = false;
 					enterPwd = true;
 					outputLine = "Enter Password:";
 				}
 			} else if (enterPwd) {
 				if (inputLine.length() > 5) {
 					outputLine = "Confirm Password:";
 					tempPwd = inputLine;
 					enterPwd = false;
 					reEnterPwd = true;
 				} else {
 					outputLine = "Password too short. Must be minimum of 6 characters.";
 				}
 			} else if (reEnterPwd) {
 				reEnterPwd = false;
 				/*
 				 * If passwords match, new user is created by placing a folder
 				 * with the username in the root directory and a .pwdfile in
 				 * that folder with the user's password
 				 */
 				if (tempPwd.equals(inputLine)) {
 					new File(cwd).mkdir();
 					FileOutputStream f = new FileOutputStream(cwd + "/.pwdfile");
 					try {
 						//encrypted password stored in file
 						f.write(PBE(tempPwd.toCharArray(), tempPwd.getBytes(),
 								true));
 						f.close();
 						tempPwd = "";
 						outputLine = "Account Created. Login or Create another account?";
 						cwd = path;
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				} else {
 					outputLine = "Passwords do not match. Re-enter password.";
 					enterPwd = true;
 				}
 			}
 
 			if (inputLine.equalsIgnoreCase("login")) {
 				outputLine = "Enter Username:";
 				loginSession = true;
 			} else if (loginSession) {
 				outputLine = "Enter password:";
 				username = inputLine;
 				loginPwd = true;
 				loginSession = false;
 			} else if (loginPwd) {
 				loginPwd = false;
 				if (checkPassword(username, inputLine)) {
 					cwd += "/" + username;
 					outputLine = "Welcome " + username + "!\t\t" + username
 							+ " ~";
 					loggedIn = true;
					currentUser = username;
					currentPwd = inputLine;
 				} else {
 					outputLine = "Incorrect username or password. Login or Create account?";
 					username = "";
 				}
 			}
 
 			if (loggedIn && inputLine.length()>1) {
 				outputLine = "";
 				String newFolder = "";
 				
 				if (inputLine.equalsIgnoreCase("ls")) {
 					String files[] = ls();
 					for (int i = 0; i < files.length; i++) {
 						outputLine += files[i];
 
 						if (i != files.length - 1)
 							outputLine += '\t';
 					}
 				} else if (inputLine.substring(0, 2).equalsIgnoreCase("rm"))
 				{
 					
 					boolean worked=new File(cwd+"/"+inputLine.substring(3, inputLine.length())).delete();
 					if(worked)
 					{
 						outputLine="File was deleted";
 					} else {
 						outputLine="File was not found";
 					}
 				}else if (inputLine.substring(0, 2).equalsIgnoreCase("cd")) {
 					// TODO: implement ..
 					if (inputLine.equalsIgnoreCase("cd")) {
 						cwd = path + "/" + username;
 					} else if (checkFolderExists(inputLine.substring(3,
 							inputLine.length()))) {
 						cwd += "/" + inputLine.substring(3, inputLine.length());
 					} else {
 						outputLine = newFolder + ": Not a valid directory";
 					}
 				} else if (inputLine.substring(0, 6).equalsIgnoreCase("mkdir ")) {
 					//TODO: implement check for invalid folder name
 					new File(cwd+"/"+inputLine.substring(6, inputLine.length())).mkdirs();
 				} else if (inputLine.substring(0, 6).equalsIgnoreCase("fetch ")) {
 					filename = inputLine.substring(6, inputLine.length());
 					if (filename.equalsIgnoreCase("")) {
 						outputLine = "Invalid file.";
 					} else {
 						if(checkFileExists(filename)){
 							outputLine = "Sent file: " + filename;
 							System.out.println("Sending: " + filename);
 							out.println("Sending: " + filename);
 							
 							deliver(s, filename);
 						
 						} else {
 							outputLine = "No such file.";
 						}
 					}
 					//TODO Bank interactions, encryption
				} else if(inputLine.substring(0,4).equals("put ") || (inputLine.length()>= 8 && inputLine.substring(0,8).equals("replace ")) )
 				{
 					if(inputLine.substring(0,4).equals("put ") )
 					{
 						filename = inputLine.substring(4, inputLine.length());
 					} else {
 						filename = inputLine.substring(8, inputLine.length());
 					}
 					if (filename.equalsIgnoreCase("")  ) {
 						outputLine = "Invalid file.";
 					} else {
 						if(!checkFileExists(filename) || inputLine.substring(0,8).equals("replace ") ){
 							outputLine = "Received file: " + filename;
 							System.out.println("Receiving: " + filename);
 							out.println("Receiving: " + filename);
 							sending = false;
 							recieve(s, filename);
 							sending=true;
 						} else {
 							outputLine = "File already exists";
 						}
 					}
 				}
 
 
 				// TODO: shouldn't print full cwd, chop off ./storage/<username>
 				outputLine += "\t\t" + username + " ~" + cwd;
 			}
 
 			if (inputLine.equals("Bye."))
 				break;
 			System.out.println(outputLine);
 			out.println(outputLine);
 		}
 	}
 
 	/**
 	 * Handles receiving a file from the User
 	 * @param s - socket over which the file is sent
 	 * @param filename - Name of the file to be sent
 	 */
 	private void recieve(Socket s, String filename)
 	{
 		try {
 			int bytesRead;
 			InputStream incoming;
 			incoming = s.getInputStream();
 		
 			DataInputStream fileData = new DataInputStream(incoming);   
 		
 			String fileName = fileData.readUTF();    
 			OutputStream output = new FileOutputStream(cwd+"/"+fileName);     
 			long size = fileData.readLong();     
 			byte[] buffer = new byte[1024];  
         
 			while (size > 0 && (bytesRead = fileData.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1)     
 			{     
				//buffer = PBE(currentPwd, buffer, true)
 				output.write(buffer, 0, bytesRead);     
 				size -= bytesRead;
             
 			}  
 			output.close();
 		} catch(Exception e)
 		{
 			e.printStackTrace();
 		}
         
 	}
 	
 	/**
 	 * Algorithm for encrypting or decrypting bytes using the user's password
 	 * @param password 	user's password
 	 * @param text		'cleartext' bytes to be encrypted/decrypted
 	 * @param encrypt	encrypt if true, decrypt if false
 	 * @return			ciphered bytes
 	 */
 	private byte[] PBE(char[] password, byte[] text, boolean encrypt) {
 		PBEKeySpec pbeKeySpec;
 		PBEParameterSpec pbeParamSpec;
 		SecretKeyFactory keyFac;
 
 		// Salt
 		byte[] salt = { (byte) 0xc7, (byte) 0x73, (byte) 0x21, (byte) 0x8c,
 				(byte) 0x7e, (byte) 0xc8, (byte) 0xee, (byte) 0x99 };
 
 		// Iteration count
 		int count = 20;
 
 		// Create PBE parameter set
 		pbeParamSpec = new PBEParameterSpec(salt, count);
 		pbeKeySpec = new PBEKeySpec(password);
 
 		try {
 			keyFac = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
 			SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);
 
 			// Create PBE Cipher
 			Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
 
 			if (encrypt) {
 				pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);
 			} else {
 				pbeCipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);
 			}
 
 			// Encrypt or decrypt the cleartext
 			return pbeCipher.doFinal(text);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	/**
 	 * Implements the ls method
 	 * @return ls as a string
 	 */
 	private String[] ls() {
 
 		File dir = new File(cwd);
 
 		String[] children = dir.list();
 		if (children == null) {
 			// Either dir does not exist or is not a directory
 		} else {
 			for (int i = 0; i < children.length; i++) {
 				// Get filename of file or directory
 				String filename = children[i];
 			}
 		}
 
 		// It is also possible to filter the list of returned files.
 		// This example does not return any files that start with `.'.
 		FilenameFilter filter = new FilenameFilter() {
 			public boolean accept(File dir, String name) {
 				return !name.startsWith(".");
 			}
 		};
 		children = dir.list(filter);
 
 		for (int j = 0; j < children.length; j++) {
 			System.out.println(children[j]);
 		}
 
 		return children;
 	}
 
 	private boolean checkFolderExists(String folder) {
 		File f = new File(cwd + "/" + folder);
 		return f.exists() && f.isDirectory();
 	}
 
 	private boolean checkFileExists(String file) {
 		File f = new File(cwd + "/" + file);
 		return f.exists();
 	}
 
 	private boolean checkPassword(String uid, String pwd) {
 		FileInputStream fileInputStream = null;
 		File file = new File(path + "/" + uid + "/.pwdfile");
 
 		byte[] bFile = new byte[(int) file.length()];
 
 		try {
 			// convert file into array of bytes
 			fileInputStream = new FileInputStream(file);
 			fileInputStream.read(bFile);
 			fileInputStream.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 
 		return Arrays.equals(pwd.getBytes(), PBE(pwd.toCharArray(), bFile, false));
 	}
 
 	// Still needs to be implemented
 	private String generate_checksum(String f) throws Exception {
 		return MD5Checksum.getMD5Checksum(f);
 	}
 
 	public void deliver(Socket s, String filename) {
 		try {
 			
 			File f= new File(cwd+"/"+filename);
 			//long size = f.length();
 			byte[] mybytearray = new byte[(int) f.length()];  
 	          
 	        FileInputStream fis = new FileInputStream(cwd+"/"+filename);  
 	        BufferedInputStream bis = new BufferedInputStream(fis);  
 	        //bis.read(mybytearray, 0, mybytearray.length);  
 	          
 	        DataInputStream dis = new DataInputStream(bis);     
 	        dis.readFully(mybytearray, 0, mybytearray.length);
 	        
 	        dis.close();
 	        OutputStream os = s.getOutputStream();  
 	          
 	        //Sending file name and file size to the server  
 	        DataOutputStream dos = new DataOutputStream(os);     
 	        dos.writeUTF(filename);     
 	        dos.writeLong(mybytearray.length);     
 	        dos.write(mybytearray, 0, mybytearray.length); 
 	        dos.writeChar('\n');
 	        dos.flush();
 	        
 	        
     	} catch (Exception e) {
     		e.printStackTrace();
     	}
 	}
 
 	public String verify(String userid, String pwd, String filename) {
 		File f = new File(path + "/" + userid + "/" + filename);
 		boolean file_exists = f.exists();
 		boolean verified = checkPassword(userid, pwd);
 		if (file_exists && verified) {
 			try {
 				return generate_checksum(path + "/" + userid + "/" + filename);
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} // THIS NEEDS TO BE FIXED
 		}
 		if (!verified) {
 			System.err.println("Access Denied");
 			return null;
 		}
 		if (!f.exists()) {
 			System.err.println("file does not exist on provider side");
 			return null;
 		}
 
 		return null;
 	}
 
 	public boolean delete(String userid, String pwd, String filename) {
 		File f = new File(path + "/" + userid + "/" + filename);
 		boolean file_exists = f.exists();
 		boolean verified = checkPassword(userid, pwd);
 		if (file_exists && verified) {
 			f.delete();
 			return true;
 		}
 		if (!verified) {
 			System.err.println("Access Denied");
 			return false;
 		}
 		if (!f.exists()) {
 			System.err.println("file does not exist on provider side");
 			return false;
 		}
 		return false;
 	}
 
 	public int create_file(String filename, String userid, String pwd,
 			byte[] data) {
 		int error_accumulation = 0;
 		File f = new File(path + "/" + userid + "/" + filename);
 		boolean file_exists = f.exists();
 		boolean verified = checkPassword(userid, pwd);
 		// TODO Actually verify the payment
 		boolean verify_payment = true;
 		if (!file_exists && verified && verify_payment) {
 			try {
 				OutputStream out = new FileOutputStream(path + "/" + userid
 						+ "/" + filename);
 				out.write(data);
 				out.close();
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				System.out.println("Could not write to file");
 				return FILE_ERROR;
 			}
 
 		}
 
 		if (file_exists)
 			error_accumulation += FILE_ERROR;
 		if (!verified)
 			error_accumulation += AUTHORIZATION_ERROR;
 		if (!verify_payment)
 			error_accumulation += BANK_ERROR;
 		return error_accumulation;
 
 	}
 }
