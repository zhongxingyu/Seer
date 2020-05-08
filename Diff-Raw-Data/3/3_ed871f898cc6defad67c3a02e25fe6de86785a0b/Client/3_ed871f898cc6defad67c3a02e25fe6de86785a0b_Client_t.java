 package Main;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.Socket;
 
 /**
  * Provides an extensible framework to do public and private calculations Can be
  * extended to provide degrees within this black or white type casting
  * 
  * @author Cole Christie
  * 
  */
 public class Client {
 	private Logging mylog;
 	private Networking network;
 	private Auth subject;
 	private Crypto crypt;
 
 	/**
 	 * s CONSTRUCTOR
 	 */
 	public Client(Logging passedLog, Auth passedSubject) {
 		mylog = passedLog;
 		subject = passedSubject;
 	}
 
 	/**
 	 * Handles the creation and main thread of client activity
 	 * 
 	 * @param passedPort
 	 * @param passedTarget
 	 */
 	public void StartClient(int passedPort, String passedTarget) {
 		// Start up client networking
 		network = new Networking(mylog, passedPort, passedTarget);
 		// Bring the created socket into this scope
 		Socket MySock = network.PassBackClient();
 		// Bind I/O to the socket
 		network.BringUp(MySock);
 
 		// Prep and begin interface
 		String UserInput = null;
 		String ServerResponse = null;
 		System.out.println("======================================================================");
 		System.out.println("Connected to server [" + passedTarget + "] on port [" + passedPort + "]");
 		System.out.println("Commands are:");
 		System.out.println("QUIT  - Closes connection with the server and quits");
 		System.out.println("REKEY - Rekeys encryption between the client and the server");
 		System.out.println("JOB   - Requests a job from the server");
 		System.out.println("*     - Anything else is sent to the server and echo'ed back");
 		System.out.println("======================================================================");
 
 		// Activate crypto
 		crypt = new Crypto(mylog, subject.GetPSK());
 
 		// Test bi-directional encryption is working
 		String rawTest = "Testing!!!12345";
 		byte[] testdata = crypt.encrypt(rawTest); // Craft test
 		network.Send(testdata); // Send test
 		byte[] fetched = network.ReceiveByte(); // Receive return response
 		String dec = crypt.decrypt(fetched); // Decrypt
 		if (dec.equals(rawTest + "<S>")) {
 			mylog.out("INFO", "Functional bi-directional encryption established.");
 		} else {
 			mylog.out("ERROR", "Failed to establish a functional encrypted channel!");
 			mylog.out("ERROR", "Expected [" + rawTest + "<S>" + "] but recieved [" + dec + "]");
 			network.BringDown();
 			try {
 				MySock.close();
 			} catch (IOException e) {
 				mylog.out("ERROR", "Failed to close client socket");
 			}
 			System.exit(0);
 		}
 
 		// Use DH to change encryption key
 		DHrekey();
 
 		// First prompt
 		UserInput = readUI();
 
 		// Begin UI loop
 		int MaxBeforeREKEY = 100;
 		int Current = 0;
 		boolean serverUp = true;
 		while ((UserInput != null) && (UserInput.compareToIgnoreCase("quit") != 0) && (MySock.isConnected())) {
 			network.Send(crypt.encrypt(UserInput));
 			fetched = network.ReceiveByte();
 			ServerResponse = crypt.decrypt(fetched);
 			if (ServerResponse == null) {
 				mylog.out("WARN", "Server disconected");
 				serverUp = false;
 				break;
 			}
 			System.out.println(ServerResponse);
 			UserInput = readUI();
 			// Check input for special commands
 			if ((UserInput.compareToIgnoreCase("rekey") == 0) && serverUp) {
 				UserInput = "Rekey executed.";
 				DHrekey();
 				Current = 0;
			} else if ((UserInput.compareToIgnoreCase("job") == 0) && serverUp) {				
 				// TODO Implement this feature
 			}
 
 			// Check for forced rekey interval
 			if (Current == MaxBeforeREKEY) {
 				DHrekey();
 				Current = 0;
 			} else {
 				Current++;
 			}
 		}
 
 		if ((UserInput.compareToIgnoreCase("quit") == 0) && serverUp) {
 			network.Send(crypt.encrypt("quit"));
 		}
 
 		// Client has quit or server shutdown
 		network.BringDown();
 		try {
 			MySock.close();
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
 	private void DHrekey() {
 		// Prep
 		byte[] fetched = null;
 		String ServerResponse = null;
 
 		// Create a DH instance and generate a PRIME and BASE
 		DH myDH = new DH(mylog);
 
 		// Share data with the server
 		network.Send(crypt.encrypt("<REKEY>"));
 		RecieveACK(); // Wait for ACK
 		network.Send(crypt.encrypt("<PRIME>"));
 		RecieveACK(); // Wait for ACK
 		network.Send(crypt.encrypt(myDH.GetPrime(16)));
 		RecieveACK(); // Wait for ACK
 		network.Send(crypt.encrypt("<BASE>"));
 		RecieveACK(); // Wait for ACK
 		network.Send(crypt.encrypt(myDH.GetBase(16)));
 		RecieveACK(); // Wait for ACK
 
 		// Validate server agrees with what has been sent
 		fetched = network.ReceiveByte();
 		SendACK(); // Send ACK
 		ServerResponse = crypt.decrypt(fetched);
 		if (ServerResponse.compareToIgnoreCase("<REKEY-STARTING>") != 0) {
 			mylog.out("ERROR", "Server has failed to acknowledge re-keying!");
 		}
 
 		// Phase 1 of DH
 		myDH.DHPhase1();
 
 		// Send my public DH key to SERVER
 		network.Send(crypt.encrypt("<PUBLICKEY>"));
 		RecieveACK(); // Wait for ACK
 		network.Send(crypt.encrypt(myDH.GetPublicKeyBF()));
 		RecieveACK(); // Wait for ACK
 
 		// Validate server agrees with what has been sent
 		fetched = network.ReceiveByte();
 		SendACK(); // Send ACK
 		ServerResponse = crypt.decrypt(fetched);
 		if (ServerResponse.compareToIgnoreCase("<PubKey-GOOD>") != 0) {
 			mylog.out("ERROR", "Server has failed to acknowledge client public key!");
 		}
 
 		// Receive server public DH key
 		byte[] serverPublicKey = null;
 		fetched = network.ReceiveByte();
 		SendACK(); // Send ACK(); //Send ACK
 		ServerResponse = crypt.decrypt(fetched);
 		if (ServerResponse.compareToIgnoreCase("<PUBLICKEY>") != 0) {
 			mylog.out("ERROR", "Server has failed to send its public key!");
 		} else {
 			fetched = network.ReceiveByte();
 			SendACK(); // Send ACK(); //Send ACK
 			serverPublicKey = crypt.decryptByte(fetched);
 			network.Send(crypt.encrypt("<PubKey-GOOD>"));
 			RecieveACK(); // Wait for ACK
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
 	private void SendACK() {
 		network.Send(crypt.encrypt("<ACK>"));
 		if (crypt.decrypt(network.ReceiveByteACK()).compareToIgnoreCase("<ACK>") != 0) {
 			mylog.out("ERROR", "Partner failed to ACK");
 		}
 	}
 
 	/**
 	 * Provides message synchronization
 	 */
 	private void RecieveACK() {
 		if (crypt.decrypt(network.ReceiveByteACK()).compareToIgnoreCase("<ACK>") != 0) {
 			mylog.out("ERROR", "Partner failed to ACK");
 		}
 		network.Send(crypt.encrypt("<ACK>"));
 	}
 }
