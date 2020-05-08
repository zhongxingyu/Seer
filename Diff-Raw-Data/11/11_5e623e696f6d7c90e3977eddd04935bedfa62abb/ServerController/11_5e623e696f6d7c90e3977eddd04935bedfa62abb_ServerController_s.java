 import javax.net.ssl.SSLServerSocket;
 import javax.net.ssl.SSLServerSocketFactory;
 import javax.net.ssl.SSLSocket;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.BufferedWriter;
 import java.util.Properties;
 
 //import java.security.KeyStore;
 
 public class ServerController {
 	private SSLServerSocket _serverSocket;
 	private Model model;
 
 	public ServerController(int port, String password, String keyStoreName) {
 
 		//Instantiate a Model. Architecture subject to change
 		model = new Model(password.toCharArray());
 
 		//Use password based encryption as basis for SSL
 		Properties systemProps = System.getProperties();
 		systemProps.setProperty("javax.net.ssl.keyStore", keyStoreName);
 		systemProps.setProperty("javax.net.ssl.keyStorePassword",
 				password.toString());
 		System.setProperties(systemProps);
 
 		//Attempt to create a Socket Factory with the specified security parameters
 		SSLServerSocketFactory sslServerFactory = (SSLServerSocketFactory) SSLServerSocketFactory
 			.getDefault();
 		try {
 			_serverSocket = (SSLServerSocket) sslServerFactory
 				.createServerSocket(port);
 			_serverSocket
 				.setEnabledCipherSuites(new String[] { "TLS_RSA_WITH_AES_128_CBC_SHA256" });
 			_serverSocket.setEnabledProtocols(new String[] { "TLSv1",
 				"TLSv1.1", "TLSv1.2" });
 
 			//Perpetually spawn a new socket and listen on it whenever one is accepted.
 			//Start a new thread to handle each connection.
 			for (;;) {
 				SSLSocket clientSocket = (SSLSocket) _serverSocket.accept();
 				Runnable connectionHandler = new ConnectionHandler(clientSocket);
 				new Thread(connectionHandler).start();
 			}
 
 		} catch (IOException e) {
 			System.err.println("Password Verification Failed");
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.err.println("Could not create socket on port " + port + ".");
 		}
 	}
 
 	public ServerController(String password) {
 		this(9999, password, "PrivKey");
 	}
 
 	private class ConnectionHandler implements Runnable {
 		private SSLSocket _sslSocket;
 		private String identity;
 		private String role;
 
 		public ConnectionHandler(SSLSocket sslSocket) {
 			_sslSocket = sslSocket;
 			identity = null;
 			role = null;
 		}
 
 		public void run() {
 			try {
 				InputStream inputStream = _sslSocket.getInputStream();
 				InputStreamReader inputStreamReader = new InputStreamReader(
 						inputStream);
 				BufferedReader bufferedReader = new BufferedReader(
 						inputStreamReader);
 
 				boolean forcePassChange=true;
 				//Always do a login first
 				while(identity==null){
 					String command;
 					try{ 
 						command = bufferedReader.readLine();
 						if(!command.equals("login"))
 							throw new Exception("notlogin");
 					} catch(Exception e){
 						System.err.println(e.getMessage());
 						if(e.getMessage().equals("notlogin"))
 							continue;
 						else
 							e.printStackTrace();
 					}
 					forcePassChange = this.login(bufferedReader);
 				}
 
 				//Look for new commands forever unless login failed.
 				while( identity != null ){
 					String command;
 					try {
 						command = bufferedReader.readLine();
 					} catch (Exception e) {
 						e.printStackTrace();
 						break;
 					}
 					
 					if ( forcePassChange ){
 						try{
 							if(!command.equals("changePassword"))
 								throw new Exception("Client refused to change password");
 							forcePassChange = false;
 						} catch (Exception e){
 							System.err.println(e.getMessage());
 							//Close Session
 							identity = null;
 							role = null;
 							return;
 						}
 					}
 							
 					switch (command) {
 						case "sendKeyboardMessages":
 							this.printKeyboardMessages(bufferedReader);
 							break;
 						case "sendFile":
 							this.receiveFile(inputStream, bufferedReader);
 							break;
 						case "changePassword":
 							this.changePassword(bufferedReader);
 							break;
 						case "createUser":
 							if(!role.equals("admin"))
 								break;
 							this.createUser(bufferedReader);
 							break;
 						default:
 							//System.out.println("Command not found");
 							// Above should throw an exception
 							break;
 					}
 				}
 
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 		private void printKeyboardMessages(BufferedReader bufferedReader)
 			throws IOException {
 			String string = null;
 			while ((string = bufferedReader.readLine()) != null) {
 				System.out.println(string);
 				System.out.flush();
 			}
 		}
 
 		private boolean login(BufferedReader bufferedReader) throws IOException {
 			String username = bufferedReader.readLine();
 			String password = bufferedReader.readLine();
			String isOneTime = null;
 
 			String [] login_output= {null, null, null};
 
 			try{
 				login_output = model.login(username, password.toCharArray());
 				identity = login_output[0];
 				role = login_output[1];
 				isOneTime = login_output[2];
 			} catch (Exception e) {
 				if (e.getMessage().equals("invalpass")){
 					identity = null;
 					role = null;
 				}
 				else if(e.getMessage().equals("No such user")){
 					identity = null;
 					role = null;
 				}
 				else
 					e.printStackTrace();
 			}
 
 			OutputStream outputStream = _sslSocket.getOutputStream();
 			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
 					outputStream);
 			BufferedWriter bufferedWriter = new BufferedWriter(
 					outputStreamWriter);
 
 			bufferedWriter.write(identity + '\n');
 			bufferedWriter.write(role + '\n');
 			bufferedWriter.write(isOneTime + '\n');
 			System.out.println(isOneTime);
 			bufferedWriter.flush();
 			return (isOneTime.equals("t"));
 
 		}
 
 		private void receiveFile(InputStream inputStream,
 				BufferedReader bufferedReader) throws IOException {
 
 			//Get filename and size
 			String filename = bufferedReader.readLine() + ".out";
 			int filesize = Integer.parseInt(bufferedReader.readLine());
 
 			//Tell the model to save the file. It will automatically be encrypted.
 			model.saveFile(inputStream,filename,filesize, identity);
 			//model.decrypt(filename, filename+".decrypt",filesize); //Debug
 
 			//Get output stream to send an ack.
 			OutputStream outputStream = _sslSocket.getOutputStream();
 			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
 			BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
 
 			//Send an ack.
 			bufferedWriter.write("succeed"+'\n');
 			bufferedWriter.flush();
 		}
 
 		private void changePassword(BufferedReader bufferedReader)
 			throws IOException {
 
 			String oldPassword = bufferedReader.readLine();
 			String newPassword = bufferedReader.readLine();
 
 			OutputStream os = _sslSocket.getOutputStream();
 			OutputStreamWriter osw = new OutputStreamWriter(os);
 			BufferedWriter bw = new BufferedWriter(osw);
 
 			try {
 				model.changePassword(identity,
 						oldPassword.toCharArray(),
 						newPassword.toCharArray());
 			} catch (Exception e) {
 				bw.write("fail"+'\n');
 			}
 
 			bw.write("succeed"+'\n'+role+'\n');
 			bw.flush();
 
 		}
 
 		private void createUser(BufferedReader bufferedReader)
 			throws IOException {
 
 			String username = bufferedReader.readLine();
 			String role = bufferedReader.readLine();
 			char[] newPassword = model.createUser(username,role.toCharArray());
 
 			//Get output stream to send the password back
 			OutputStream outputStream = _sslSocket.getOutputStream();
 			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
 			BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
 
 			//Send the password back.
 			bufferedWriter.write(new String(newPassword)+'\n');
 			bufferedWriter.flush();
 
 		}
 
 	}
 
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		System.out.println("Enter server password: ");
 		InputStreamReader inputStreamReader = new InputStreamReader(System.in);
 		BufferedReader reader = new BufferedReader(inputStreamReader);
 		String password = null;
 		try {
 			password = reader.readLine();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		new ServerController(password);
 
 	}
 
 }
