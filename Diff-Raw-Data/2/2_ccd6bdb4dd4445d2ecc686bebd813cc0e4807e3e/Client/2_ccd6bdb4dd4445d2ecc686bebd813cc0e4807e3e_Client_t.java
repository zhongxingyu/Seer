 package Main;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.Socket;
 import java.util.ArrayList;
 
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
 	private Crypto cryptSVR;
 	private Crypto cryptDO;
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
 		// TODO Setup a parameter to put the client into an endless loop of job
 		// requests. In that loop, when jobs are missing sleep - after a certain
 		// number of cycles quit
 
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
		String UserInput = "";
 		String ServerResponse = null;
 
 		// Load client identification data
 		String OS = (String) clientSession.getAttribute("OS");
 		String SecLev = (String) clientSession.getAttribute("SecurityLevel");
 		String ClientID = (String) clientSession.getAttribute("ID");
 
 		// Display the UI boilerplate
 		DisplayMenu();
 
 		// Activate crypto
 		cryptSVR = new Crypto(mylog, subject.GetPSK(), "Server");
 		cryptDO = new Crypto(mylog, subject.GetPSK(), "Drop Off Point");
 
 		// Test bi-directional encryption is working
 		String rawTest = "Testing!!!12345";
 		byte[] testdata = cryptSVR.encrypt(rawTest); // Craft test
 		// Test the SERVER
 		ServerNetwork.Send(testdata); // Send test
 		byte[] fetched = ServerNetwork.ReceiveByte(); // Receive return response
 		String dec = cryptSVR.decrypt(fetched); // Decrypt
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
 		testdata = cryptDO.encrypt(rawTest); // Craft test
 		DropOffNetwork.Send(testdata); // Send test
 		fetched = DropOffNetwork.ReceiveByte(); // Receive return response
 		dec = cryptDO.decrypt(fetched); // Decrypt
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
 		DHrekey(ServerNetwork, cryptSVR, "Server");
 		DHrekey(DropOffNetwork, cryptDO, "Drop Off");
 
 		// Begin UI loop
 		int MaxBeforeREKEY = 100;
 		int Current = 0;
 		boolean serverUp = true;
 		boolean flagJob = false;
 		boolean noSend = false;
 		while ((UserInput.compareToIgnoreCase("quit") != 0) && (ServerSock.isConnected())
 				&& (DropOffSock.isConnected())) {
 			if (noSend) {
 				// We do not send anything to the server this time, but we will
 				// reset the boolean flag so we will next time
 				noSend = false;
 			} else {
 				// Communicate with the server
 				ServerNetwork.Send(cryptSVR.encrypt(UserInput));
 				fetched = ServerNetwork.ReceiveByte();
 				ServerResponse = cryptSVR.decrypt(fetched);
 				if (ServerResponse == null) {
 					mylog.out("WARN", "Server disconected");
 					serverUp = false;
 					break;
 				}
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
 
 						// Setup and Connect
 						ArrayList<String> ErrorData = new ArrayList<String>();
 						ArrayList<String> OutputData = new ArrayList<String>();
 						Runtime rt = Runtime.getRuntime();
 						Process proc = rt.exec(ServerResponse);
 
 						// Capture all STDERR
 						StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
 						errorGobbler.start();
 
 						// Capture all STDOUT
 						StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");
 						outputGobbler.start();
 
 						// Wait for the work to complete
 						int CheckExit = 0;
 						try {
 							CheckExit = proc.waitFor();
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 						if (CheckExit != 0) {
 							System.out.println("ExitValue: " + CheckExit);
 						} else {
 							ErrorData = errorGobbler.ReturnData();
 							OutputData = outputGobbler.ReturnData();
 
 							// Send the results to the Drop Off point
 							// TODO send completed work to drop off point
 							for (String line : ErrorData) {
 								System.out.println("Error:" + line);
 							}
 							for (String line : OutputData) {
 								System.out.println("Output:" + line);
 							}
 						}
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 
 					// Inform the Server that the work has been completed
 					UserInput = "workdone";
 					ServerNetwork.Send(cryptSVR.encrypt(UserInput));
 					fetched = ServerNetwork.ReceiveByte();
 					ServerResponse = cryptSVR.decrypt(fetched);
 					if (ServerResponse == null) {
 						mylog.out("WARN", "Server disconected");
 						serverUp = false;
 						break;
 					}
 					System.out.println(ServerResponse);
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
 				DHrekey(ServerNetwork, cryptSVR, "Server");
 				DHrekey(DropOffNetwork, cryptDO, "Drop Off");
 				Current = 0;
 			} else if (UserInput.contains("job")) {
 				flagJob = true; // Flags the use of a slightly different display
 				UserInput = "job" + ":" + ClientID + ":" + OS + ":" + SecLev;
 			} else if (UserInput.contains("help")) {
 				// Do not send anything, a help request stays local
 				noSend = true;
 				DisplayMenu();
 			}
 
 			// Check for forced rekey interval
 			if (Current == MaxBeforeREKEY) {
 				DHrekey(ServerNetwork, cryptSVR, "Server");
 				DHrekey(DropOffNetwork, cryptDO, "Drop Off");
 				Current = 0;
 			} else {
 				Current++;
 			}
 		}
 
 		if ((UserInput.compareToIgnoreCase("quit") == 0) && serverUp) {
 			ServerNetwork.Send(cryptSVR.encrypt("quit"));
 			DropOffNetwork.Send(cryptDO.encrypt("quit"));
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
 	private void DHrekey(Networking network, Crypto crypt, String ReKeyedWith) {
 		// Prep
 		byte[] fetched = null;
 		String ServerResponse = null;
 
 		// Create a DH instance and generate a PRIME and BASE
 		DH myDH = new DH(mylog);
 
 		// Share data with the server
 		network.Send(crypt.encrypt("<REKEY>"));
 		RecieveACK(network, crypt); // Wait for ACK
 		network.Send(crypt.encrypt("<PRIME>"));
 		RecieveACK(network, crypt); // Wait for ACK
 		network.Send(crypt.encrypt(myDH.GetPrime(16)));
 		RecieveACK(network, crypt); // Wait for ACK
 		network.Send(crypt.encrypt("<BASE>"));
 		RecieveACK(network, crypt); // Wait for ACK
 		network.Send(crypt.encrypt(myDH.GetBase(16)));
 		RecieveACK(network, crypt); // Wait for ACK
 
 		// Validate server agrees with what has been sent
 		fetched = network.ReceiveByte();
 		SendACK(network, crypt); // Send ACK
 		ServerResponse = crypt.decrypt(fetched);
 		if (ServerResponse.compareToIgnoreCase("<REKEY-STARTING>") != 0) {
 			mylog.out("ERROR", ReKeyedWith + " has failed to acknowledge re-keying!");
 		}
 
 		// Phase 1 of DH
 		myDH.DHPhase1();
 
 		// Send my public DH key to SERVER
 		network.Send(crypt.encrypt("<PUBLICKEY>"));
 		RecieveACK(network, crypt); // Wait for ACK
 		network.Send(crypt.encrypt(myDH.GetPublicKeyBF()));
 		RecieveACK(network, crypt); // Wait for ACK
 
 		// Validate server agrees with what has been sent
 		fetched = network.ReceiveByte();
 		SendACK(network, crypt); // Send ACK
 		ServerResponse = crypt.decrypt(fetched);
 		if (ServerResponse.compareToIgnoreCase("<PubKey-GOOD>") != 0) {
 			mylog.out("ERROR", ReKeyedWith + " has failed to acknowledge client public key!");
 		}
 
 		// Receive server public DH key
 		byte[] serverPublicKey = null;
 		fetched = network.ReceiveByte();
 		SendACK(network, crypt); // Send ACK(); //Send ACK
 		ServerResponse = crypt.decrypt(fetched);
 		if (ServerResponse.compareToIgnoreCase("<PUBLICKEY>") != 0) {
 			mylog.out("ERROR", ReKeyedWith + " has failed to send its public key!");
 		} else {
 			fetched = network.ReceiveByte();
 			SendACK(network, crypt); // Send ACK(); //Send ACK
 			serverPublicKey = crypt.decryptByte(fetched);
 			network.Send(crypt.encrypt("<PubKey-GOOD>"));
 			RecieveACK(network, crypt); // Wait for ACK
 		}
 
 		// Use server DH public key to generate shared secret
 		myDH.DHPhase2(myDH.CraftPublicKey(serverPublicKey), ReKeyedWith);
 
 		// Final verification
 		// System.out.println("Shared Secret (Hex): " +
 		// myDH.GetSharedSecret(10));
 		crypt.ReKey(myDH.GetSharedSecret(10), ReKeyedWith);
 	}
 
 	/**
 	 * Provides message synchronization
 	 */
 	private void SendACK(Networking network, Crypto crypt) {
 		network.Send(crypt.encrypt("<ACK>"));
 		if (crypt.decrypt(network.ReceiveByteACK()).compareToIgnoreCase("<ACK>") != 0) {
 			mylog.out("ERROR", "Partner failed to ACK");
 		}
 	}
 
 	/**
 	 * Provides message synchronization
 	 */
 	private void RecieveACK(Networking network, Crypto crypt) {
 		if (crypt.decrypt(network.ReceiveByteACK()).compareToIgnoreCase("<ACK>") != 0) {
 			mylog.out("ERROR", "Partner failed to ACK");
 		}
 		network.Send(crypt.encrypt("<ACK>"));
 	}
 }
 
 /**
  * This code is a variation of the code from the following URL
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
 	ArrayList<String> Collect;
 
 	StreamGobbler(InputStream is, String type) {
 		this.is = is;
 		this.type = type;
 		Collect = new ArrayList<String>();
 	}
 
 	public void run() {
 		try {
 			InputStreamReader isr = new InputStreamReader(is);
 			BufferedReader br = new BufferedReader(isr);
 			String line = null;
 			while ((line = br.readLine()) != null) {
 				Collect.add(line);
 			}
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		}
 	}
 
 	public ArrayList<String> ReturnData() {
 		return Collect;
 	}
 }
