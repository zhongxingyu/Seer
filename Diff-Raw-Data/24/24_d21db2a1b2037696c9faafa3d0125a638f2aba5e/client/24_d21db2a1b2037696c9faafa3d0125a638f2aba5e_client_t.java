 package sdfs;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStreamWriter;
 import java.nio.ByteBuffer;
 import java.security.KeyManagementException;
 import java.security.KeyStore;
 import java.security.KeyStoreException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.Principal;
 import java.security.PrivateKey;
 import java.security.Provider;
 import java.security.SecureRandom;
 import java.security.Security;
 import java.security.Signature;
 import java.security.UnrecoverableKeyException;
 import java.security.cert.CertificateException;
 import java.util.Properties;
 
 import javax.net.ssl.KeyManagerFactory;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLSession;
 import javax.net.ssl.SSLSocket;
 import javax.net.ssl.SSLSocketFactory;
 
 import sun.misc.BASE64Encoder;
 
 public class client {
 	private SSLSocket Socket;
 	private String fileName;
 	private BufferedWriter bufferedWriter = null;
 	private KeyStore ks;
	public String ksName;
 	//This method is requests a file from the server
 	//based on the user provided filename
 	public void GetFile(String UID){
 		fileName = UID;
 		String localfileName = "../ClientFile/" + fileName;
 		BufferedWriter w;
 		try {
 			w = new BufferedWriter(
 					new OutputStreamWriter(Socket.getOutputStream()));
 			w.write("g");
 			int fileNameSize = fileName.length();
 
 			ByteBuffer filename_size = ByteBuffer.allocate(4);
 			filename_size.putInt(fileNameSize);
 			char[] char_fileName = new String(filename_size.array()).toCharArray();
 			w.write(char_fileName,0,4);
 			w.write(fileName);
 			w.flush();
 
 
 			BufferedReader r = new BufferedReader(
 					new InputStreamReader(Socket.getInputStream()));
 			create_File(localfileName, r);
 			w.close();
 
 		}catch(FileNotFoundException fe){
 			System.out.println("File not found ");
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	//This method saves the file on the client side.
 	public void create_File(String file_name, BufferedReader br){
 		try {
 			ByteBuffer file_size = ByteBuffer.allocate(8);
 			// creates buffer
 			char[] cbuf = new char[8];
 			br.read(cbuf, 0, 8);
 			for(int i=0; i<8 ;i++){
 				System.out.println((int)cbuf[i]);
 			}
 			
 
 			for (char c:cbuf)
 			{
 				file_size.put((byte) c);
 			}
 			file_size.rewind();
 			long int_size = file_size.getLong();
 
 			StringBuffer fileData = new StringBuffer();
 			System.out.println(int_size);
 			for(long i=0; i< int_size ; i++){
 				fileData.append((char) br.read());
 			}
 			System.out.println(fileData.toString());
 			
 			//Assume default encoding.
 			FileWriter fileWriter = new FileWriter(file_name);
 
 			// Always wrap FileWriter in BufferedWriter.
 			bufferedWriter = new BufferedWriter(fileWriter);
 
 			// Note that write() does not automatically
 			// append a newline character.
 			bufferedWriter.write(fileData.toString());
 
 			// Always close files.
 			bufferedWriter.close();
 
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}  
 	}
 
 
 
 	//This method tries to read a file and
 	//send it over the server
 	public void PutFile(String UID){
 
 		fileName = UID;					//need to see how the filename and Id are related
 		String localFileName = "../ClientFile/" + fileName;
 		System.out.println(localFileName);
 		BufferedWriter w;
 		try {
 			w = new BufferedWriter(
 					new OutputStreamWriter(Socket.getOutputStream()));
 			w.write("p");
 			int fileNameSize = fileName.length();
 
 			ByteBuffer filename_size = ByteBuffer.allocate(4);
 			filename_size.putInt(fileNameSize);
 			char[] char_fileName = new String(filename_size.array()).toCharArray();
 			w.write(char_fileName,0,4);
 			w.write(fileName);
 
 			File file = new File(localFileName);
 			FileInputStream fis = new FileInputStream(file);
 			long fileSize = fis.available();
 			ByteBuffer file_size = ByteBuffer.allocate(8);
 			file_size.putInt((int)fileSize);
 			char[] char_file = new String(file_size.array()).toCharArray();
 			w.write(char_file,0,8);
 
 			byte[] b = new byte[fis.available()];
 			fis.read(b);
 			String text = new String(b);
 			w.write(text);
 
 			//				w.write(fis.available());
 			//				byte[] b = new byte[fis.available()];
 			//				w.write(b, 0, len);
 			//				fis.read(b);
 			//				String text = new String(b);
 			//				
 			//				w.write(text);
 			w.flush();
 			w.close();
 		}
 		catch(FileNotFoundException fe){
 			System.out.println("File not found ");
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void create_delegation_files(DelegationToken token){
 		byte[] serialToken = serializeToken(token);
 		byte[] signature = signDelegationToken(serialToken);
 		writeFile(serialToken, "default" + ".token");
 		writeFile(signature, "default"+ ".sig");
 		System.out.println("Written delegation token as default.token and default.sig");
 	}
 	
 	public void send_delegation_token(){
 		byte[] serialToken = readFile("default.token");
 		byte[] serialTokenLength = integerToByteArray(serialToken.length);
 		byte[] signature = readFile("default.sig");
 		byte[] signatureLength = integerToByteArray(signature.length);
 		BufferedWriter w;
 		try {
 			w = new BufferedWriter(
 					new OutputStreamWriter(Socket.getOutputStream()));
 			char[] char_Token = new String(serialToken, "UTF-8").toCharArray();
 			char[] char_TokenLength = new String(serialTokenLength, "UTF-8").toCharArray();
 			char[] char_TokenSign = new String(signature, "UTF-8").toCharArray();
 			char[] char_TokenSignLegth = new String(signatureLength, "UTF-8").toCharArray();
 			w.write('d');
 
 			w.write(char_TokenLength, 0, char_TokenLength.length);
 			w.write(char_Token, 0, serialToken.length);
 			w.write(char_TokenSignLegth, 0, char_TokenSignLegth.length);
 			w.write(char_TokenSign, 0, char_TokenSign.length);
 			
 			w.flush();
 			w.close();
 		}
 		catch(Exception ex){
 			
 		}
 		//send all 4 in that order to the server 
 	}
 	
 	public byte[] integerToByteArray(int input){
 		//given an integer, return the 4-byte representation of the integer
 		return ByteBuffer.allocate(4).putInt(input).array();
 	}
 	
 	//given a token object, return a byte[] representation of the token
 	public byte[] serializeToken(DelegationToken token){
 		// get filename, recipientname, rights, numdays, propagate from the client
 		// store them inside a new DelegationToken(...)
 		byte[] output =null;
 		ByteArrayOutputStream bo = new ByteArrayOutputStream(2048);
 		try {
 			ObjectOutputStream o = new ObjectOutputStream(bo);
 			
 			o.writeObject(token);
 			
 			output = bo.toByteArray();
 			
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return output;
 	}
 	
 	//given a token in byte[] format, sign it with the client's key
 	public byte[] signDelegationToken(byte[] token){
 		byte[] signature =null;
 		
 		try{
           
 			PrivateKey signingKey;
 			signingKey = (PrivateKey) ks.getKey("client", "cs6238-ca".toCharArray());
 			String b64 = new BASE64Encoder().encode(signingKey.getEncoded());
 			System.out.println("-----BEGIN PRIVATE KEY-----");
 			System.out.println(b64);
 			System.out.println("-----END PRIVATE KEY-----");
 
 			//hash first.
 			MessageDigest digest = null;
 			try {
 				digest = MessageDigest.getInstance("SHA-256");
 			} catch (NoSuchAlgorithmException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			digest.update(token);
 			byte[] hashValue = digest.digest();
 			
 			//now, sign by decrypting.
 			Provider[] p = Security.getProviders();
 			for(int i = 0; i < p.length; i++){
 				System.out.println(p[i].getName());
 				
 			}
 			try {
 				
 				// get an RSA cipher object and print the provider
 				final Signature signer = Signature.getInstance("SHA1withRSA");
 				// sign using the private key
 				SecureRandom random = new SecureRandom();
 				signer.initSign(signingKey, random);
 				signer.update(token);
 				signature = signer.sign();
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
 		} catch (UnrecoverableKeyException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		return signature;
 	}
 
 	public void writeFile(byte[] file, String fileName){
 		try {
 			FileOutputStream foWrite = new FileOutputStream(fileName);
 			ObjectOutputStream oosWrite;
 		
 			oosWrite = new ObjectOutputStream(foWrite);
 			oosWrite.write(file);
 			oosWrite.close();
 			
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 	
 	public byte[] readFile(String fileName){
 		byte[] bytes_data = null;
 		try {
 			FileInputStream fis = new FileInputStream(fileName);
 			ObjectInputStream ois_read = new ObjectInputStream(fis);
 			bytes_data = new byte[ois_read.available()];
 			ois_read.read(bytes_data, 0, bytes_data.length);
 			ois_read.close();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return bytes_data;
 	}
 	
 	
 	//This method starts a new session with the server.
 	public void Start_FS_session(){
 		int port = 3000;
 		try {
 			// Use the public key from the AIDAP server as the trust store for this client.
 			//   (note: created this keystore using InstallCerts.java from sun.com)
 			Properties systemProps = System.getProperties();
 			systemProps.put( "javax.net.ssl.trustStore", "../CS-6238/myTrustStore");
 			System.setProperties(systemProps);
 
 			System.out.println("Locating server socket factory for SSL...");
 			String ksName = "../CS-6238/keystore.jks";
 			char ksPass[] = "cs6238-ca".toCharArray();
 			char ctPass[] = "cs6238-ca".toCharArray();
 			ks = KeyStore.getInstance("JKS");
 			ks.load(new FileInputStream(ksName), ksPass);
 
 			KeyManagerFactory kmf = 
 					KeyManagerFactory.getInstance("SunX509");
 
 			kmf.init(ks, ctPass);
 			SSLContext sc = SSLContext.getInstance("TLS");
 			sc.init(kmf.getKeyManagers(), null, null);
 			SSLSocketFactory ssf = sc.getSocketFactory();
 			//				SSLSocket Socket = (SSLSocket) ssf.createSocket("127.0.0.1",port);
 			Socket = (SSLSocket) ssf.createSocket("127.0.0.1",port);
 			//		    ---------------------
 			System.out.println("Locating server socket factory for SSL...");
 			//		      SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
 
 			System.out.println("Creating a server socket on port " + port);
 			//		      SSLSocket Socket = (SSLSocket) factory.createSocket("143.215.116.52",port);
 
 			String[] suites = Socket.getSupportedCipherSuites();
 			System.out.println("Support cipher suites are:");
 			for (int i = 0; i < suites.length; i++) {
 				System.out.println(suites[i]);
 			}
 			
 			//set the cipher suite to only dhe rsa
 			String[] dhe_rsa_aes_256 = new String[1];
 			dhe_rsa_aes_256[0] = new String("TLS_DHE_RSA_WITH_AES_256_CBC_SHA");
 			Socket.setEnabledCipherSuites(dhe_rsa_aes_256);
 
 			
 			System.out.println("Support protocols are:");
 			String[] protocols = Socket.getSupportedProtocols();
 			for (int i = 0; i < protocols.length; i++) {
 				System.out.println(protocols[i]);
 			}
 
 //			System.out.println("Waiting for client...");
 			//		      SSLSocket socket = (SSLSocket) Socket.sta
 
 			System.out.println("Starting handshake...");
 			Socket.startHandshake();
 			SSLSession session = Socket.getSession();
 			Principal serverID = session.getPeerPrincipal();
 			System.out.println("The principal of the peer is " + serverID.getName() + " and should read as server");
 			//		      BufferedWriter w = new BufferedWriter(
 			//		              new OutputStreamWriter(Socket.getOutputStream()));
 			//		      w.write("hello");
 			//		      w.flush();
 			//		      w.close();
 
 			System.out.println("Just connected to " + Socket.getRemoteSocketAddress());
 
 			//		      Socket.close();
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
 
 	public void Exit() {
 		BufferedWriter w;
 		try {
 			w = new BufferedWriter(
 					new OutputStreamWriter(Socket.getOutputStream()));
 			w.write("e");
 			w.flush();
 			w.close();
 		}
 		catch(Exception ex){
 			
 		}
 		
 	}
 }
