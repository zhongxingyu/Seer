 import java.util.Map;
 import java.util.HashMap;
 import java.io.*;
 import java.security.*;
 import javax.net.ssl.*;
 import java.net.*;
 import java.util.Arrays;
 
 public class Client {
 	private static final String LINE_UI = "> ";
 	private Map<String, CommandFactory> factories;
 	private BufferedReader buffReader;
 	private static InetAddress serverIP;
 	private static int serverPort = 1024;
 	private String user;
 	private String keystorePassword;
 	private String passwordKey;
     private String truststorePassword;
 	private static String[] validUsers = new String[] {"patient", "doctor", "nurse", "agency"};
 	private PrintWriter out;
 	private InputStream in;
     private SSLSocket socket = null;
     private boolean isConnect = false;
 
 	public static void main(String args[]) {
 		Client client = null;
 		boolean error = false;
 		if (args.length == 0) {
 			System.out.println("No user specified. Running with user \"patient\"");
 			client = new Client();
 		} else if (args.length == 1) {
 			if (Arrays.asList(validUsers).contains(args[0])) {
 				client = new Client(args[0]);
 			} else {
 				error = true;
 			}
 		} else {
 			error = true;
 		}
 
 		if (error) {
 			System.err.print("Specify none or one user to run the program as. Valid users are: ");
 			for (String user : validUsers) {
 				System.err.print(user + " ");
 			}
 			System.err.println(".");
 			System.exit(1);
 
 		} else {
 			client.run();
 		}
 	}
 
 	public Client() {
 		this("patient");
 	}
 
 	public Client(String user) {
 		this.user = user;	
 		buffReader = new BufferedReader(new InputStreamReader(System.in));
 
 		factories = new HashMap<String, CommandFactory>();
 		factories.put(ReadFactory.COMMAND_NAME,   new ReadFactory());
 		factories.put(ListFactory.COMMAND_NAME,   new ListFactory());
 		factories.put(AppendFactory.COMMAND_NAME, new AppendFactory());
 		factories.put(CreateFactory.COMMAND_NAME, new CreateFactory());
 		factories.put(DeleteFactory.COMMAND_NAME, new DeleteFactory());
 
 		try {
 			serverIP = InetAddress.getByAddress(new byte[] {(byte) 192,(byte) 168, 0, 1});
 		} catch (UnknownHostException uhe) {
 			uhe.printStackTrace();
 		}
 	}
 
 	public void run() {
 		System.out.println("Started secure client.");
 		System.out.println("Quit by sending an EOF.");
 		try {
 			readPassword();
 			connectServer();
 			
 			System.out.println("Available commands are:");
 			for (CommandFactory<Command> factory : factories.values()) {
 				System.out.println(factory.helpText());
 			}
 			System.out.println("-------");
 
 			System.out.print(LINE_UI);
 			String inputLine;
 			while ((inputLine = buffReader.readLine()) != null && isConnect) {
 				String[] parts = inputLine.split("\\s+");
 				if (parts.length > 0 && parts[0].length() > 0) {
 					if (parts[0].equals("q")) {
 						break; // I found my self trying to quit with q so often...
 					}
 
 					CommandFactory<Command> factory = factories.get(parts[0]);
 					if (factory == null) {
 						System.err.println("Not a valid command.");
 					} else {
 						try {
 							Command command = factory.makeCommand(parts);
 							// Send command to server!
 							String protoString = command.protocolString();
 							out.print(protoString.toCharArray().length);
 							out.print(protoString);
 
 							String serverResponse;
 							byte[] message = new byte[Integer.MAX_VALUE];					
 							int amtRead = 0;
 							
 							while((amtRead = in.read(message, amtRead, 4 - amtRead) )!= 4) { 
 							    System.out.println("Reading bytestream...");
 							    System.out.println(new String(message));
 							}
 							
 							int size = 0;
 							for (int i = 0; i < 4; i++) {
 							    size |= ((int) message[i]) << 3 - i;    
 							}
 							
 							
 						} catch (BadCommandParamException bcpe) {
 							System.err.println(bcpe.getMessage());
 						} catch (IOException ioe) {
 						    killConnection();
 						    
 						    System.err.println(ioe.getMessage());
 						    ioe.printStackTrace();
 						}
 				     
 					}
 				}
 				System.out.print(LINE_UI);
 			}
 		} catch (IOException ioe) {
 			System.err.println("Error reading input.");
 		}
 		System.out.println("\nTerminating...");
 	}
 
 	private void connectServer() {
 	  
 		try {
 			SSLContext sslContext = SSLContext.getInstance("TLS");
 			
 
 			KeyManagerFactory keyFactory = KeyManagerFactory.getInstance("SunX509");
 			KeyStore keyStore = KeyStore.getInstance("JKS");
 			keyStore.load(new FileInputStream("users/" + user + "/keystore"), keystorePassword.toCharArray());
			keyFactory.init(keyStore, passwordKey.toCharArray());
 
 			TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
 			KeyStore trustStore = KeyStore.getInstance("JKS");
 			trustStore.load(new FileInputStream("users/" + user + "/truststore"), truststorePassword.toCharArray());
 			trustFactory.init(trustStore);
 
 			trustFactory.init(keyStore);
 			sslContext.init(keyFactory.getKeyManagers(), trustFactory.getTrustManagers(), null);
 			SSLSocketFactory sslfactory = sslContext.getSocketFactory();
 
 			socket = (SSLSocket)sslfactory.createSocket(serverIP, serverPort);
 
 			socket.setUseClientMode(true);
 			socket.startHandshake();
 
 			out = new PrintWriter(
 						new OutputStreamWriter(
 								       socket.getOutputStream()));
 
 			
 			if(out.checkError()) {
 			    System.err.println("SSLSocketClient: java.io.PrintWriter error");
 			}
 
 			in = socket.getInputStream();
 			isConnect = true;
 			
 
 		} catch (GeneralSecurityException gse) {
 			gse.printStackTrace();
 			return;
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 
 		} 
 
 	}
 
     private void killConnection() {
 	try {
 	in.close();
 	out.close();
 	socket.close();
 	} catch (IOException ioe) {
 	    System.err.println("Disconnection failed.");
 	    ioe.printStackTrace();
 	}
     }
 
 	private void readPassword() throws IOException {
 		while (keystorePassword == null || keystorePassword.length() == 0) {
 			System.out.print("Keystore password:");
 			keystorePassword = new String(System.console().readPassword());
 		}
 
 		while (passwordKey == null || passwordKey.length() == 0) {
 			System.out.print("Private key access password:");
 			passwordKey = new String(System.console().readPassword());
 		}
 
 		while (truststorePassword == null || truststorePassword.length() == 0) {
 			System.out.print("Truststore password:");
 			truststorePassword = new String(System.console().readPassword());
 		}
 
 	}
 }
