 package Main;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.Socket;
 
 import org.apache.shiro.session.Session;
 
 /**
  * Provides an extensible framework to do public and private calculations Can be
  * extended to provide degrees within this black or white type casting
  * 
  * @author Cole Christie
  * 
  */
 public class Client {
 	private Logging mylog;
 	private Networking ServerNetwork;
 	private Networking DropOffNetwork;
 	private Auth subject;
 	private Crypto crypt;
 	private Session clientSession;
 
 	/**
 	 * s CONSTRUCTOR
 	 */
 	public Client(Logging passedLog, Auth passedSubject, Session passedSession) {
 		mylog = passedLog;
 		subject = passedSubject;
 		clientSession = passedSession;
 	}
 
 	/**
 	 * This displays the CLI menu and advertised commands
 	 */
 	private void DisplayMenu() {
 		System.out.println("======================================================================");
 		System.out.println("Commands are:");
 		System.out.println("QUIT  - Closes connection with the server and quits");
 		System.out.println("REKEY - Rekeys encryption between the client and the server");
 		System.out.println("JOB   - Requests a job from the server");
 		System.out.println("HELP  - Displays this menu");
 		System.out.println("*     - Anything else is sent to the server and echo'ed back");
 		System.out.println("======================================================================");
 	}
 
 	/**
 	 * Handles the creation and main thread of client activity
 	 * 
 	 * @param passedPort
 	 * @param passedTarget
 	 */
 	public void StartClient(int SERVERpassedPort, String SERVERpassedTarget, int DROPOFFpassedPort,
 			String DROPOFFpassedTarget) {
 		// Connect to the server
 		// Start up client networking
 		ServerNetwork = new Networking(mylog, SERVERpassedPort, SERVERpassedTarget);
 		// Bring the created socket into this scope
 		Socket ServerSock = ServerNetwork.PassBackClient();
 		// Bind I/O to the socket
 		ServerNetwork.BringUp(ServerSock);
		System.out.println("Connected to Server [" + SERVERpassedTarget + "] on port [" + SERVERpassedPort + "]");
 
 		// Connect to the drop off
 		// Start up client networking
		DropOffNetwork = new Networking(mylog, DROPOFFpassedPort, DROPOFFpassedTarget);
 		// Bring the created socket into this scope
 		Socket DropOffSock = DropOffNetwork.PassBackClient();
 		// Bind I/O to the socket
 		DropOffNetwork.BringUp(DropOffSock);
		System.out.println("Connected to Drop Off [" + DROPOFFpassedTarget + "] on port [" + DROPOFFpassedPort + "]");
 
 		// Prepare the interface
 		String UserInput = null;
 		String ServerResponse = null;
 
 		// Load client identification data
 		String OS = (String) clientSession.getAttribute("OS");
 		String SecLev = (String) clientSession.getAttribute("SecurityLevel");
 		String ClientID = (String) clientSession.getAttribute("ID");
 
 		// Display the UI boilerplate
		DisplayMenu();		
 
 		// Activate crypto
 		crypt = new Crypto(mylog, subject.GetPSK());
 
 		// Test bi-directional encryption is working
 		String rawTest = "Testing!!!12345";
 		byte[] testdata = crypt.encrypt(rawTest); // Craft test
 		// Test the SERVER
 		ServerNetwork.Send(testdata); // Send test
 		byte[] fetched = ServerNetwork.ReceiveByte(); // Receive return response
 		String dec = crypt.decrypt(fetched); // Decrypt
 		if (dec.equals(rawTest + "<S>")) {
 			mylog.out("INFO", "Functional bi-directional encryption established. (Server)");
 		} else {
 			mylog.out("ERROR", "Failed to establish a functional encrypted channel! (Server)");
 			mylog.out("ERROR", "Expected [" + rawTest + "<S>" + "] but recieved [" + dec + "] (Server)");
 			ServerNetwork.BringDown();
 			try {
 				ServerSock.close();
 			} catch (IOException e) {
 				mylog.out("ERROR", "Failed to close client socket (Server)");
 			}
 			System.exit(0);
 		}
 		// Test the DROPOFF
 		DropOffNetwork.Send(testdata); // Send test
 		fetched = DropOffNetwork.ReceiveByte(); // Receive return response
 		dec = crypt.decrypt(fetched); // Decrypt
 		if (dec.equals(rawTest + "<S>")) {
 			mylog.out("INFO", "Functional bi-directional encryption established. (Drop Off)");
 		} else {
 			mylog.out("ERROR", "Failed to establish a functional encrypted channel! (Drop Off)");
 			mylog.out("ERROR", "Expected [" + rawTest + "<S>" + "] but recieved [" + dec + "] (Drop Off)");
 			DropOffNetwork.BringDown();
 			try {
 				ServerSock.close();
 			} catch (IOException e) {
 				mylog.out("ERROR", "Failed to close client socket (Drop Off)");
 			}
 			System.exit(0);
 		}
 
 		// Use DH to change encryption key
 		DHrekey(ServerNetwork);
 		DHrekey(DropOffNetwork);
 
 		// First prompt
 		UserInput = readUI();
 
 		// Begin UI loop
 		int MaxBeforeREKEY = 100;
 		int Current = 0;
 		boolean serverUp = true;
 		boolean flagJob = false;
 		boolean noSend = false;
 		while ((UserInput != null) && (UserInput.compareToIgnoreCase("quit") != 0) && (ServerSock.isConnected())
 				&& (DropOffSock.isConnected())) {
 			if (!noSend) {
 				ServerNetwork.Send(crypt.encrypt(UserInput));
 				fetched = ServerNetwork.ReceiveByte();
 				ServerResponse = crypt.decrypt(fetched);
 				if (ServerResponse == null) {
 					mylog.out("WARN", "Server disconected");
 					serverUp = false;
 					break;
 				}
 			} else {
 				// We did not send anything to the server this time, but we will
 				// reset the boolean flag so we will next time
 				noSend = false;
 			}
 
 			// If this is the client receiving a job from the server
 			if (flagJob) {
 				if (ServerResponse.length() > 0) {
 					// Print out the job the server has passed us (the client)
 					System.out.println("JobIn:[" + ServerResponse + "]");
 
 					// Adjust the job so it can properly run (Windows clients
 					// require some padding at the front)
 					if (OS.contains("Windows")) {
 						// Pad the job with the required Windows shell
 						ServerResponse = "cmd /C " + ServerResponse;
 					}
 
 					try {
 						/*
 						 * Some of the code in this section is from the
 						 * following URL http://www.javaworld
 						 * .com/jw-12-2000/jw-1229-traps.html?page=4
 						 * 
 						 * It provides a simple way of calling external code
 						 * while still capturing all of the output (STD and
 						 * STDERR)
 						 * 
 						 * @author Michael C. Daconta
 						 */
 						Runtime rt = Runtime.getRuntime();
 						Process proc = rt.exec(ServerResponse);
 						// any error message?
 						StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
 
 						// any output?
 						StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");
 
 						// kick them off
 						errorGobbler.start();
 						outputGobbler.start();
 
 						// any error???
 						int exitVal = 0;
 						try {
 							exitVal = proc.waitFor();
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 						System.out.println("ExitValue: " + exitVal);
 						// TODO send completed work info to the drop off server
 						// Tell the SERVER that the work is done
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 					System.out.println("JobOut:[" + ServerResponse + "]");
 				} else {
 					System.out.println("Job:[No jobs available]");
 				}
 				flagJob = false;
 			} else {
 				System.out.println(ServerResponse);
 			}
 			UserInput = readUI().toLowerCase();
 			// Check input for special commands
 			if ((UserInput.contains("rekey")) && serverUp) {
 				UserInput = "Rekey executed.";
 				DHrekey(ServerNetwork);
 				DHrekey(DropOffNetwork);
 				Current = 0;
 			} else if (UserInput.contains("job")) {
 				// TODO Initial job requests are missing the meta data?
 				flagJob = true; // Flags the use of a slightly different display
 				UserInput = UserInput + ":" + ClientID + ":" + OS + ":" + SecLev;
 			} else if (UserInput.contains("help")) {
 				noSend = true; // Do not send anything, the help request stays
 								// local
 				DisplayMenu();
 			}
 
 			// Check for forced rekey interval
 			if (Current == MaxBeforeREKEY) {
 				DHrekey(ServerNetwork);
 				DHrekey(DropOffNetwork);
 				Current = 0;
 			} else {
 				Current++;
 			}
 		}
 
 		if ((UserInput.compareToIgnoreCase("quit") == 0) && serverUp) {
 			ServerNetwork.Send(crypt.encrypt("quit"));
 			DropOffNetwork.Send(crypt.encrypt("quit"));
 		}
 
 		// Client has quit or server shutdown
 		ServerNetwork.BringDown();
 		DropOffNetwork.BringDown();
 		try {
 			ServerSock.close();
 			DropOffSock.close();
 		} catch (IOException e) {
 			mylog.out("ERROR", "Failed to close client socket");
 		}
 	}
 
 	/**
 	 * Reads input provided by the user, returns a string
 	 * 
 	 * @return
 	 */
 	private String readUI() {
 		System.out.flush();
 		System.out.print("> ");
 		System.out.flush();
 		String data = null;
 		BufferedReader inputHandle = new BufferedReader(new InputStreamReader(System.in));
 		boolean wait = true;
 		while (wait) {
 			try {
 				if (inputHandle.ready()) {
 					wait = false;
 				} else {
 					try {
 						Thread.sleep(10);
 					} catch (InterruptedException e) {
 						mylog.out("ERROR", "Failed to sleep");
 					}
 				}
 			} catch (IOException err) {
 				mylog.out("ERROR", "Failed to check if buffered input was ready [" + err + "]");
 			}
 		}
 		try {
 			data = inputHandle.readLine();
 		} catch (IOException err) {
 			mylog.out("ERROR", "Failed to collect user input [" + err + "]");
 		}
 		return data;
 	}
 
 	/**
 	 * Starts a DH rekey between the client and the server
 	 */
 	private void DHrekey(Networking network) {
 		// Prep
 		byte[] fetched = null;
 		String ServerResponse = null;
 
 		// Create a DH instance and generate a PRIME and BASE
 		DH myDH = new DH(mylog);
 
 		// Share data with the server
 		network.Send(crypt.encrypt("<REKEY>"));
 		RecieveACK(network); // Wait for ACK
 		network.Send(crypt.encrypt("<PRIME>"));
 		RecieveACK(network); // Wait for ACK
 		network.Send(crypt.encrypt(myDH.GetPrime(16)));
 		RecieveACK(network); // Wait for ACK
 		network.Send(crypt.encrypt("<BASE>"));
 		RecieveACK(network); // Wait for ACK
 		network.Send(crypt.encrypt(myDH.GetBase(16)));
 		RecieveACK(network); // Wait for ACK
 
 		// Validate server agrees with what has been sent
 		fetched = network.ReceiveByte();
 		SendACK(network); // Send ACK
 		ServerResponse = crypt.decrypt(fetched);
 		if (ServerResponse.compareToIgnoreCase("<REKEY-STARTING>") != 0) {
 			mylog.out("ERROR", "Server has failed to acknowledge re-keying!");
 		}
 
 		// Phase 1 of DH
 		myDH.DHPhase1();
 
 		// Send my public DH key to SERVER
 		network.Send(crypt.encrypt("<PUBLICKEY>"));
 		RecieveACK(network); // Wait for ACK
 		network.Send(crypt.encrypt(myDH.GetPublicKeyBF()));
 		RecieveACK(network); // Wait for ACK
 
 		// Validate server agrees with what has been sent
 		fetched = network.ReceiveByte();
 		SendACK(network); // Send ACK
 		ServerResponse = crypt.decrypt(fetched);
 		if (ServerResponse.compareToIgnoreCase("<PubKey-GOOD>") != 0) {
 			mylog.out("ERROR", "Server has failed to acknowledge client public key!");
 		}
 
 		// Receive server public DH key
 		byte[] serverPublicKey = null;
 		fetched = network.ReceiveByte();
 		SendACK(network); // Send ACK(); //Send ACK
 		ServerResponse = crypt.decrypt(fetched);
 		if (ServerResponse.compareToIgnoreCase("<PUBLICKEY>") != 0) {
 			mylog.out("ERROR", "Server has failed to send its public key!");
 		} else {
 			fetched = network.ReceiveByte();
 			SendACK(network); // Send ACK(); //Send ACK
 			serverPublicKey = crypt.decryptByte(fetched);
 			network.Send(crypt.encrypt("<PubKey-GOOD>"));
 			RecieveACK(network); // Wait for ACK
 		}
 
 		// Use server DH public key to generate shared secret
 		myDH.DHPhase2(myDH.CraftPublicKey(serverPublicKey));
 
 		// Final verification
 		// System.out.println("Shared Secret (Hex): " +
 		// myDH.GetSharedSecret(10));
 		crypt.ReKey(myDH.GetSharedSecret(10));
 	}
 
 	/**
 	 * Provides message synchronization
 	 */
 	private void SendACK(Networking network) {
 		network.Send(crypt.encrypt("<ACK>"));
 		if (crypt.decrypt(network.ReceiveByteACK()).compareToIgnoreCase("<ACK>") != 0) {
 			mylog.out("ERROR", "Partner failed to ACK");
 		}
 	}
 
 	/**
 	 * Provides message synchronization
 	 */
 	private void RecieveACK(Networking network) {
 		if (crypt.decrypt(network.ReceiveByteACK()).compareToIgnoreCase("<ACK>") != 0) {
 			mylog.out("ERROR", "Partner failed to ACK");
 		}
 		network.Send(crypt.encrypt("<ACK>"));
 	}
 }
 
 /**
  * This code is from the following URL
  * http://www.javaworld.com/jw-12-2000/jw-1229-traps.html?page=4
  * 
  * It is useful in catching all of the output of an executed sub-process and has
  * not been altered from its initial state
  * 
  * @author Michael C. Daconta
  * 
  */
 class StreamGobbler extends Thread {
 	InputStream is;
 	String type;
 
 	StreamGobbler(InputStream is, String type) {
 		this.is = is;
 		this.type = type;
 	}
 
 	public void run() {
 		try {
 			InputStreamReader isr = new InputStreamReader(is);
 			BufferedReader br = new BufferedReader(isr);
 			String line = null;
 			while ((line = br.readLine()) != null)
 				System.out.println(type + ">" + line);
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		}
 	}
 }
