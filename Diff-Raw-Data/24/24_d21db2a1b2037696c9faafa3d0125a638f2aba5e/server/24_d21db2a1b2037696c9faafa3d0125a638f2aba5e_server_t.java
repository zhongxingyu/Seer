 package sdfs;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.nio.ByteBuffer;
 import java.security.InvalidAlgorithmParameterException;
 import java.security.InvalidKeyException;
 import java.security.Key;
 import java.security.KeyManagementException;
 import java.security.KeyStore;
 import java.security.KeyStoreException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.Principal;
 import java.security.SecureRandom;
 import java.security.UnrecoverableKeyException;
 import java.security.cert.Certificate;
 import java.security.cert.CertificateException;
 import java.util.Properties;

 import javax.crypto.BadPaddingException;
 import javax.crypto.Cipher;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.spec.IvParameterSpec;
 import javax.crypto.spec.SecretKeySpec;
 import javax.net.ssl.KeyManagerFactory;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLServerSocket;
 import javax.net.ssl.SSLServerSocketFactory;
 import javax.net.ssl.SSLSession;
 import javax.net.ssl.SSLSocket;
 
 import sun.misc.BASE64Encoder;
 
 
 public class server {
 
 	private String mode = null;
 	private BufferedWriter bufferedWriter = null;
 	private BufferedWriter bufferedWriter_key = null;
 	SSLSocket socket;
 	private byte[] iv;
 	byte[] key;
 	private String ksName;
 	private char ksPass[];
 	private char ctPass[];
 	private String file_name = null;
 	private Principal clientID;
 
 	//This is the method that tries to listen to the client 
 	//on the given port number.
 	public void listen(){
 		int port = 3000;
 		try {
 			Properties systemProps = System.getProperties();
 			systemProps.put( "javax.net.ssl.trustStore", "../../CS-6238/myTrustStore");
 			System.setProperties(systemProps);
 
 			System.out.println("Locating server socket factory for SSL...");
 			ksName = "../../CS-6238/keystoreServer.jks";
 			ksPass = "cs6238-ca".toCharArray();
 			ctPass = "cs6238-ca".toCharArray();
 			KeyStore ks = KeyStore.getInstance("JKS");
 			ks.load(new FileInputStream(ksName), ksPass);
 
 			KeyManagerFactory kmf = 
 					KeyManagerFactory.getInstance("SunX509");
 
 			kmf.init(ks, ctPass);
 			SSLContext sc = SSLContext.getInstance("TLS");
 			sc.init(kmf.getKeyManagers(), null, null);
 			SSLServerSocketFactory ssf = sc.getServerSocketFactory();
 			SSLServerSocket serverSocket = (SSLServerSocket) ssf.createServerSocket(port);
 			printServerSocketInfo(serverSocket);
 
 
 			//		    --------------------
 			//			SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
 
 			//			System.out.println("Creating a server socket on port " + port);
 			//			SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(port);
 
 			String[] suites = serverSocket.getSupportedCipherSuites();
 			System.out.println("Support cipher suites are:");
 			for (int i = 0; i < suites.length; i++) {
 				System.out.println(suites[i]);
 			}
 
 			//set the cipher suite to only dhe rsa
 			String[] dhe_rsa_aes_256 = new String[1];
 			dhe_rsa_aes_256[0] = new String("TLS_DHE_RSA_WITH_AES_256_CBC_SHA");
 			serverSocket.setEnabledCipherSuites(dhe_rsa_aes_256);
 
 			//require client authentication
 			serverSocket.setNeedClientAuth(true);
 
 			System.out.println("Support protocols are:");
 			String[] protocols = serverSocket.getSupportedProtocols();
 			for (int i = 0; i < protocols.length; i++) {
 				System.out.println(protocols[i]);
 			}
 
 			System.out.println("Waiting for client...");
 			socket = (SSLSocket) serverSocket.accept();
 
 			System.out.println("Starting handshake...");
 			socket.startHandshake();
 			SSLSession session = socket.getSession();
 			this.clientID = session.getPeerPrincipal();
 			System.out.println("The principal of the peer is " + clientID.getName() + " and should read as client");
 
 			BufferedReader r = new BufferedReader(
 					new InputStreamReader(socket.getInputStream()));
 
 			//			System.out.println(r.readLine());
 			//			char m;
 			//			String fileName = null;
 			int choice;			
 			while(1 != 2){
 				if((choice = r.read())!= -1){
 					byte b_choice = (byte)choice;
 					System.out.println("got inside th loop........");
 					System.out.println(b_choice);
 					if(b_choice == 'p'){
 						System.out.println("got inside th loop......put..");
 						file_name = get_fileName(r);
 						String local_FileName = "../../ServerFile/" + file_name;
 						create_File(local_FileName, r);
 					}
 					else if(b_choice == 'g'){
 						System.out.println("got inside th loop.....get");
 						file_name = get_fileName(r);
 						String local_FileName = "../../ServerFile/" + file_name;
 
 						handle_get(local_FileName);
 					}
 					else if(b_choice == 'd'){
 						// handle_d, then handle as per the g case
 						// extract out token and token signature that client sends
 						// as byte[] arrays
 						handle_del(r);
 						file_name = get_fileName(r);
 						String local_FileName = "../../ServerFile/" + file_name;
 						handle_get(local_FileName);
 					}
 					else if(b_choice == 'e'){
 						System.out.println("THis is exit.............");
 						r.close();
 						socket.close();
 						return;
 					}
 				}
 //				System.out.println("Just connected to " + socket.getRemoteSocketAddress());
 			}	
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (KeyManagementException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnrecoverableKeyException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (KeyStoreException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (CertificateException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	//This method handles the delegation provided by the user.
 	private void handle_del(BufferedReader br) {
 		try {
 			//--------Reading token
 			ByteBuffer token_size = ByteBuffer.allocate(4);
 			// creates buffer
 			char[] cbuf = new char[4];
 			br.read(cbuf, 0, 4);
 
 			for (char c:cbuf)
 			{
 				token_size.put((byte) c);
 			}
 			token_size.rewind();
 			int int_Tokensize = token_size.getInt();
 
 			ByteBuffer token = ByteBuffer.allocate(int_Tokensize);
 			for(int i=0; i< int_Tokensize ; i++){
 				token.put((byte) br.read());
 			}
 
 			//-------------Reading Signature
 
 			ByteBuffer sign_size = ByteBuffer.allocate(4);
 			// creates buffer
 			char[] signbuf = new char[4];
 			br.read(signbuf, 0, 4);
 
 			for (char c:signbuf)
 			{
 				sign_size.put((byte) c);
 			}
 			sign_size.rewind();
 			int int_SignSize = sign_size.getInt();
 
 			ByteBuffer signature = ByteBuffer.allocate(int_SignSize);
 			for(int i=0; i< int_SignSize ; i++){
 				signature.put((byte) br.read());
 			}
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private static void printServerSocketInfo(SSLServerSocket s) {
 		System.out.println("Server socket class: "+s.getClass());
 		System.out.println("   Socker address = "
 				+s.getInetAddress().toString());
 		System.out.println("   Socker port = "
 				+s.getLocalPort());
 		System.out.println("   Need client authentication = "
 				+s.getNeedClientAuth());
 		System.out.println("   Want client authentication = "
 				+s.getWantClientAuth());
 		System.out.println("   Use client mode = "
 				+s.getUseClientMode());
 	} 
 
 	//This method creates a file with the server
 	//after the put request by the client
 	public void create_File(String file_name, BufferedReader br){
 		try {
 			ByteBuffer file_size = ByteBuffer.allocate(8);
 			// creates buffer
 			char[] cbuf = new char[8];
 			br.read(cbuf, 0, 8);
 
 			for (char c:cbuf)
 			{
 				file_size.put((byte) c);
 			}
 			file_size.rewind();
 			int int_size = file_size.getInt();
 
 			StringBuffer fileData = new StringBuffer();
 			for(int i=0; i< int_size ; i++){
 				fileData.append((char) br.read());
 			}
 
 			//Encrypting the file content
 			String data = this.clientID.getName() + fileData.toString();
 			byte [] EncryptedData = EncryptFileContent(data);
 			FileOutputStream foWrite = new FileOutputStream(file_name);
 			ObjectOutputStream oosWrite = new ObjectOutputStream(foWrite);
 			oosWrite.write(EncryptedData);
 			oosWrite.close();
 
 			//			//--------Encrypting key-------
 			//
 			byte[] encryptedkey = EncryptKey();
 			//			//Assume default encoding.
 			FileOutputStream fo = new FileOutputStream(file_name + ".key");
 			ObjectOutputStream oos = new ObjectOutputStream(fo);
 			oos.write(encryptedkey);
 			oos.close();
 
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}  
 	}
 
 	//This method checks if the file is with the server
 	//If not then it returns an error otherwise it will return 
 	//the file to the client
 	public void handle_get(String file_name){
 		try{
 			BufferedWriter w;
 			w = new BufferedWriter(
 					new OutputStreamWriter(socket.getOutputStream()));
 
 			FileInputStream fis = new FileInputStream(file_name);
 			ObjectInputStream ois_read = new ObjectInputStream(fis);
 			byte[] bytes_data = new byte[ois_read.available()];
 			int numRead;
 			numRead = ois_read.read(bytes_data, 0, bytes_data.length);
 			ois_read.close();
 			//			fis.read(bytes_data);
 			//			String encrypted_text = new String(b);
 
 			String decryptedText = decrptText(bytes_data);
 
 			//check if the client is the owner or posssess a valid
 			//delagation token for the owner
 
 			if(checkPermission(decryptedText) == false){
 				System.out.println("Invalid get request, you do not own this file");
 				return;
 			}
 
 			decryptedText = decryptedText.substring(this.clientID.getName().length());
 
 			//			File file = new File(file_name);
 			long fileSize = decryptedText.length();
 			ByteBuffer file_size = ByteBuffer.allocate(8);
 			file_size.putLong(fileSize);
 			char[] char_file = new String(file_size.array()).toCharArray();
 			w.write(char_file,0,8);
 			w.write(decryptedText);
 			w.flush();
 		} 
 		catch(FileNotFoundException fe){
 			System.out.println("File not found ");
 		} 
 		catch (IOException e) {
 			System.out.println("The requested file is not there. Please try again with a valid name.");
 			e.printStackTrace();
 
 		}
 
 	}
 
 	//check for permissions to get this file
 	private boolean checkPermission(String decryptedText) {
 		if(decryptedText.indexOf(this.clientID.getName()) == 0){
 			return true;
 		} 
 		if(false){
 			return true;
 			//check for delegation
 		}
 		return false;
 	}
 
 	private String decrptText(byte[] encrypted_text) {
 		byte[] decryptedText = null;
 		String decrypted_Text = null;
 		//		key = new byte[16];
 		byte[] decryption_key = decrptKey();
 
 		// setup AES cipher in CBC mode with PKCS #5 padding
 		Cipher cipher = null;
 		try {
 			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchPaddingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		iv = new byte[cipher.getBlockSize()];
 
 		//		new SecureRandom().nextBytes(iv);
 
 		IvParameterSpec ivSpec = new IvParameterSpec(iv);
 
 		// hash keyString with SHA-256 and crop the output to 128-bit for key
 		MessageDigest digest = null;
 		try {
 			digest = MessageDigest.getInstance("SHA-256");
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		//		digest.update(decryption_key.getBytes());         //check if this is going to work (keystring.tobytes())
 		//		byte[] key = new byte[16];
 		//		System.arraycopy(digest.digest(), 0, key, 0, key.length);
 		SecretKeySpec keySpec = new SecretKeySpec(decryption_key, "AES");
 
 		// decrypt
 		try {
 			cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
 		} catch (InvalidKeyException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvalidAlgorithmParameterException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		//	        byte[] decrypted = null;
 		try {
 			decryptedText = cipher.doFinal(encrypted_text);
 		} catch (IllegalBlockSizeException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (BadPaddingException e1) {
 			// TODO Auto-generated catch block
 			System.out.println("Decryption error. Aborting decryption.");
 			System.exit(1);
 			//e1.printStackTrace();
 		}try {
 			decrypted_Text = new String(decryptedText, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 
 		return decrypted_Text;
 
 	}
 
 
 	private byte[] decrptKey() {
 		// TODO Auto-generated method stub
 		byte[] cipherText = null;
 		byte[] decrypt_key = null;
 		try{
 			final String alias = "server";
 			KeyStore ks;
 			ks = KeyStore.getInstance("jks");
 			ks.load(new FileInputStream(ksName), ksPass);
 			Key serverkey;
 			serverkey = ks.getKey(alias, ksPass);
 
 			String b64 = new BASE64Encoder().encode(serverkey.getEncoded());
 			System.out.println("-----BEGIN PRIVATE KEY-----");
 			System.out.println(b64);
 			System.out.println("-----END PRIVATE KEY-----");
 
 			try {
 				FileInputStream fi = new FileInputStream("../../ServerFile/" + file_name + ".key");
 				ObjectInputStream ois = new ObjectInputStream(fi);
 				cipherText = new byte[ois.available()];
 				ois.read(cipherText, 0 , cipherText.length);
 				ois.close();
 
 			} catch (FileNotFoundException e2) {
 				// TODO Auto-generated catch block
 				e2.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			try {
 				// get an RSA cipher object and print the provider
 				final Cipher cipher = Cipher.getInstance("RSA");
 				// encrypt the plain text using the public key
 				SecureRandom random = new SecureRandom();
 				cipher.init(Cipher.DECRYPT_MODE, serverkey, random);
 				decrypt_key = cipher.doFinal(cipherText);
 				//				decrypt_key = new String(cipherText, "UTF-8");
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 		}catch (KeyStoreException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (CertificateException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnrecoverableKeyException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 		return decrypt_key;
 
 	}
 
 	//This method does the encryption needed for storing the
 	//file with the server.
 	public byte[] EncryptFileContent(String fileData){
 		byte[] encryptedDataBytes = null;
 		final String keyString = fileData;
 		//		String encryptedData = null;
 
 		// setup AES cipher in CBC mode with PKCS #5 padding
 		Cipher cipher = null;
 		try {
 			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchPaddingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// setup an IV (initialization vector) that should be
 		// randomly generated for each input that's encrypted
 		iv = new byte[cipher.getBlockSize()];
 
 		//		new SecureRandom().nextBytes(iv);
 
 		IvParameterSpec ivSpec = new IvParameterSpec(iv);
 
 		// hash keyString with SHA-256 and crop the output to 128-bit for key
 		MessageDigest digest = null;
 		try {
 			digest = MessageDigest.getInstance("SHA-256");
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		digest.update(keyString.getBytes());						//Check with this line
 		key = new byte[16];
 		System.arraycopy(digest.digest(), 0, key, 0, key.length);
 		SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
 
 		// encrypt
 		try {
 			cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
 		} catch (InvalidKeyException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvalidAlgorithmParameterException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		//		byte[] encrypted = null;
 		try {
 			encryptedDataBytes = cipher.doFinal(fileData.getBytes("UTF-8"));
 
 		} catch (IllegalBlockSizeException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (BadPaddingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 		catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		//		try {
 		//			encryptedData = new String(encryptedDataBytes, "UTF-8");
 		//		} catch (UnsupportedEncodingException e) {
 		//			// TODO Auto-generated catch block
 		//			e.printStackTrace();
 		//		}
 
 
 		return encryptedDataBytes;
 	}
 
 	public byte[] EncryptKey(){
 		byte[] cipherText = null;
 		String cipher_key = null;
 		try{
 			final String alias = "server";
 			KeyStore ks;
 			ks = KeyStore.getInstance("jks");
 			ks.load(new FileInputStream(ksName), ksPass);
 			Certificate serverkey;
 			serverkey = ks.getCertificate(alias);
 
 			String b64 = new BASE64Encoder().encode(serverkey.getEncoded());
 			System.out.println("-----BEGIN CERTIFICATE-----");
 			System.out.println(b64);
 			System.out.println("-----END CERTIFICATE-----");
 
 			try {
 				// get an RSA cipher object and print the provider
 				final Cipher cipher = Cipher.getInstance("RSA");
 				// encrypt the plain text using the public key
 				SecureRandom random = new SecureRandom();
 				cipher.init(Cipher.ENCRYPT_MODE, serverkey, random);
 				cipherText = cipher.doFinal(key);
 				//				cipher_key = new String(cipherText, "UTF-8");
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 		}catch (KeyStoreException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (CertificateException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return cipherText;
 
 	}
 
 	public String get_fileName(BufferedReader br){
 		String file_name = null;
 		try {
 			ByteBuffer file_Namesize = ByteBuffer.allocate(4);
 			// creates buffer
 			char[] cbuf = new char[4];
 			br.read(cbuf, 0, 4);
 
 			for (char c:cbuf)
 			{
 				file_Namesize.put((byte) c);
 				System.out.print(c);
 			}
 			file_Namesize.rewind();
 			int int_size = file_Namesize.getInt();
 
 			StringBuffer name = new StringBuffer();
 			for(int i=0; i< int_size ; i++){
 				name.append((char) br.read());
 			}
 			return name.toString();
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}  
 		return file_name;
 
 	}
 
 }
