 package server.auction;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.math.BigInteger;
 import java.net.Socket;
 import java.net.SocketTimeoutException;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.security.InvalidKeyException;
 import java.security.Key;
 import java.security.NoSuchAlgorithmException;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.security.Signature;
 import java.security.SignatureException;
 import java.util.ConcurrentModificationException;
 import java.util.Date;
 
 import javax.crypto.Mac;
 import javax.crypto.spec.IvParameterSpec;
 import javax.crypto.spec.SecretKeySpec;
 
 import org.bouncycastle.util.encoders.Base64;
 import org.bouncycastle.util.encoders.Hex;
 
 import server.analytics.AnalyticsServerRMI;
 import tools.PropertiesParser;
 import tools.SuperSecureSocket;
 
 public class CommandHandler implements Runnable
 {
 	private User u = null;
 	private Socket sock = null;
 	private SuperSecureSocket ssock = null;
 	private BufferedReader br = null;
 	private BufferedWriter bw = null;
 	private AuctionServer main = null;
 	private boolean localShutdown = false;
 	private boolean clientBlocked = false;
 	private GroupBid blockingGroupBid = null;
 	private int timeout = 0;
 
 	//RMI Analytics Server
 	/*private AnalyticsServerRMI as = null;
 	private PropertiesParser ps = null;
 	private Registry reg = null;*/
 
 	public CommandHandler(Socket s, AuctionServer main) {
 		this.sock = s;
 		ssock = new SuperSecureSocket(sock, main.getServerPrivKey(), main.getClientsKeyDir());
 		this.main = main;
 		br = new BufferedReader(new InputStreamReader(ssock.getInputStream()));
 		bw = new BufferedWriter(new OutputStreamWriter(ssock.getOutputStream()));
 
 		/*try {
 			ps = new PropertiesParser("registry.properties");
 			int portNr = Integer.parseInt(ps.getProperty("registry.port"));
 			String host = ps.getProperty("registry.host");
 			reg = LocateRegistry.getRegistry(host, portNr);
 			as = (AnalyticsServerRMI) reg.lookup("RemoteAnalyticsServer");
 		} catch (FileNotFoundException e) {
 			System.out.println("properties file not found!");
 			e.printStackTrace();
 		} catch (NumberFormatException e) {
 			System.out.println("Port non-numeric!");
 			e.printStackTrace();
 		} catch (RemoteException e) {
 			System.out.println("Registry couln't be found!");
 			e.printStackTrace();
 		} catch (NotBoundException e) {
 			System.out.println("Specified remote object couldn't be found in registry!");
 			e.printStackTrace();
 		}*/
 	}
 
 	public void run() {
 		while(!main.getShutdown() && !localShutdown) {
 			if(clientBlocked) {
 				if(blockingGroupBid.isConfirmed()) {
 					try {
 						ssock.sendLine("!confirmed");
 						clientBlocked = false;
 						blockingGroupBid = null;
 					} catch (IOException e) {
 						System.err.println("I/O Error! Writing to Client!");
 						e.printStackTrace();
 					}
 				}
 				timeout++;
 				if(timeout>20) {
 					try {
 						ssock.sendLine("!rejected");
 						main.getGroupBid(blockingGroupBid.getAuctionId(), blockingGroupBid.getAmount(), blockingGroupBid.getUser()).unConfirm(u.getUsername());
 						clientBlocked = false;
 						blockingGroupBid = null;
 					} catch (IOException e) {
 						System.err.println("I/O Error! Writing to Client!");
 						e.printStackTrace();
 					}
 				}else {
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 
 			if(!clientBlocked) {
 				try {
 					//sock.setSoTimeout(1000);
 					String command = "";
 					command = ssock.readLine();
 					if (command == null)
 						command = "!end";
 					command = command.trim(); // remove leading and trailing whitespaces
 					String[] commandParts = command.split("\\s+");
 					if (commandParts[0].equals("!list")) {
 						listAuctions();
 						////////////////////////////////////////////
 						// !listGroupBids
 						////////////////////////////////////////////
 					} else if (commandParts[0].equals("!getGbList")) {
 						listGroupBids();
 						System.out.println(main.waitingBids);
 						////////////////////////////////////////////
 						// !login - Client logs in
 						////////////////////////////////////////////
 					}
 					else if(commandParts[0].equals("!login")) {
 						byte[] clientChallenge = Base64.decode(commandParts[3]);
 						PrivateKey privK = main.getServerPrivKey();
 						if (privK != null) {
 							byte[] clientChallengeB64 = ssock.decrypt(clientChallenge, "RSA/NONE/OAEPWithSHA256AndMGF1Padding", privK);
 							final String B64 = "a-zA-Z0-9/+";
 							String message1 = new String(commandParts[0]+" "+commandParts[1]+" "+commandParts[2]+" "+ new String(clientChallengeB64));
 							assert message1.matches("!login [a-zA-Z0-9_\\-]+ [0-9]+ ["+B64+"]{43}=") : "1st message";
 							clientChallenge = Base64.decode(clientChallengeB64);
 							if (commandParts.length != 4) {
 								ssock.sendLine("Invalid command! Should be !login <username>");
 							} else {
 								if (u != null && u.isLoggedIn()) { // already logged in?
 									ssock.sendLine("You are already logged in as " + u.getUsername() + "\nPlease logout first!");
 								} else {
 									String username = commandParts[1];
 									if (username.length() > 50) { // check if username is too long
 										ssock.sendLine("Username is too long! Limit is 50 characters!");
 									} else {
 										int udpPort = Integer.parseInt(commandParts[2]);
 										u = main.getUser(username);
 										if (u != null && u.isLoggedIn()) { // is user already logged in at another session?
 											u = null;
 											ssock.sendLine(username + " is already logged in at another session! Logout first!");
 										} else {
 											byte[] secretKey = ssock.generateSecureRandomNumber(32);
 											byte[] serverChallenge = ssock.generateSecureRandomNumber(32);
 											byte[] iv = ssock.generateSecureRandomNumber(16);
 											String message2 = new String(clientChallengeB64) + " " + new String(Base64.encode(serverChallenge)) +
 													" " + new String(Base64.encode(secretKey)) + " " + new String(Base64.encode(iv));
 											PublicKey pubK = ssock.getPEMPublicKey(main.getClientsKeyDir() + username + ".pub.pem");
 											if (pubK != null) {
 												byte[] message2B = ssock.encrypt(message2.getBytes(), "RSA/NONE/OAEPWithSHA256AndMGF1Padding", pubK); 
 												message2 = "!ok " + new String(Base64.encode(message2B));
 												ssock.sendLine(message2);
 												ssock.setSecretKey(new SecretKeySpec(secretKey, "AES"));
 												ssock.setIv(new IvParameterSpec(iv));
 												String message3 = ssock.readLine();
 												if (!message3.equals("")) {
 													//System.out.println("Third Message B64: " + new String(Base64.encode(message3.getBytes())));
 													System.out.println("Third Message Length: " + Base64.decode(message3).length);
 													System.out.println("Third Message B64-Length: " + message3.getBytes().length);
 													assert new String(message3.getBytes()).matches("["+B64+"]{43}=") : "3rd message";
 													if (new String(Base64.decode(message3)).equals(new String(serverChallenge))) {
 														System.out.println("Yes! serverChallengeMatch!");
 														if (u == null) { // new user?
 															u = new User(sock);
 															u.setUsername(username);
 															u.setUdpPort(udpPort);
 															u.setLoggedIn(true);
 															main.users.add(u);
 														} else { // user is known to the system
 															u.setUdpPort(udpPort);
 															u.setLoggedIn(true);
 															u.setSocket(sock);
 														}
 														ssock.sendLine(new String("Successfully logged in as " + u.getUsername()));
 														/*bw.write("ready");
 										bw.newLine();
 										bw.flush();*/
 														/*try{
 											UserEvent ue = new UserEvent();
 											ue.setType("USER_LOGIN");
 											ue.setUsername(u.getUsername());
 											ue.setTimestamp(System.currentTimeMillis());
 											as.processEvent(ue);
 										} catch (RemoteException e) {
 											System.err.println("Error: Couldn't create event! AnalyticsServer may be down!");
 											//e.printStackTrace();
 										}*/
 													}
 												} else {
 													System.err.println("Failed Login! Client probably couldn't read private key!");
 													ssock.setIv(null);
 													ssock.setSecretKey(null);
 												}
 											} else {
 												System.err.println("Error getting client's public key");
 												ssock.sendLine("Login failed! Client Public Key Error!");
 											}
 										}
 									}
 								}
 							}
 						} else {
 							System.err.println("Error getting the private key");
 							ssock.sendLine("Login failed! Server Private Key Error!");
 						}
 						////////////////////////////////////////////
 						// !logout - Client logs out
 						////////////////////////////////////////////
 					} else if(commandParts[0].equals("!logout")) {
 						if (u == null || !u.isLoggedIn()) {
 							ssock.sendLine("You have to login first!");
 						} else {
 							String username = u.getUsername();
 							u.setLoggedIn(false);
 							u.setUdpPort(0);
 							ssock.sendLine("Successfully logged out as " + username);
 							ssock.setIv(null);
 							ssock.setSecretKey(null);
 							/*try{
 							UserEvent ue = new UserEvent();
 							ue.setType("USER_LOGOUT");
 							ue.setUsername(u.getUsername());
 							ue.setTimestamp(System.currentTimeMillis());
 							as.processEvent(ue);
 						} catch (RemoteException e) {
 							System.err.println("Error: Couldn't create event! AnalyticsServer may be down!");
 							//e.printStackTrace();
 						}*/
 						}
 						////////////////////////////////////////////
 						// !create - Client creates an auction
 						////////////////////////////////////////////
 					} else if(commandParts[0].equals("!create")) {
 						if (u != null && u.isLoggedIn()) {
 							if (command.split("\\s+", 3).length < 3) {
 								ssock.sendLine("Invalid command! Should be !create <duration> <description>");
 							} else {
 								String description = command.split("\\s+", 3)[2];
 								if (description.length() > 1000) {
 									ssock.sendLine("Description is too long! Limit is 1000 characters!");
 								} else {
 									long duration = Long.parseLong(command.split("\\s+", 3)[1]);
 									main.setHighestAuctionID(main.getHighestAuctionID()+1);
 									Auction a = new Auction(main.getHighestAuctionID(), description, u);
 									Date date = new Date(new Date().getTime() + (duration*1000));
 									a.setDate(date);
 									main.auctions.add(a);
 									ssock.sendLine("An auction '" + description + "' with id " + a.getId() + " has been created and will end on " 
 											+ date.toString() + ".");
 									/*try{
 										AuctionEvent ae = new AuctionEvent();
 										ae.setType("AUCTION_STARTED");
 										ae.setAuctionID(a.getId());
 										ae.setTimestamp(System.currentTimeMillis());
 										ae.setDuration(duration);
 										as.processEvent(ae);
 									} catch (RemoteException e) {
 										System.err.println("Error: Couldn't create event! AnalyticsServer may be down!");
 										//e.printStackTrace();
 									} catch (ConcurrentModificationException e) {
 										;
 									}*/
 								}
 							}
 						} else {
 							ssock.sendLine("You have to login first!");
 						}
 						////////////////////////////////////////////
 						// !bid - Client bids on an auction
 						////////////////////////////////////////////
 					} else if(commandParts[0].equals("!bid")) {
 						if (commandParts.length != 3) {
 							ssock.sendLine("Invalid command! Should be !bid <auction-id> <amount>");
 						} else if (u != null && u.isLoggedIn()) {
 							int id = Integer.parseInt(commandParts[1]);
 							double amount = Double.parseDouble(commandParts[2]);
 							//DecimalFormat f = new DecimalFormat("#0.00");
 							//String amount_string = f.format(amount);
 							//amount = Double.parseDouble(amount_string); 
 							Auction a = main.getAuction(id);
 							if (a == null) {
 								ssock.sendLine("Error! Auction not found!");
 							} else {
 								if (amount > a.getHighestBid()) {
 									/*********************
 									 * no UDP in Lab 2
 									 *********************
 								if (a.getHighestBidder() != null && !a.getHighestBidder().getUsername().equals(u.getUsername())) {
 									main.sendNotification(a.getHighestBidder(), "!new-bid " + a.getDescription());
 								}
 									 */
 									if (a.getHighestBidder() != null) {
 										/*try {
 											BidEvent be = new BidEvent();
 											be.setType("BID_OVERBID");
 											be.setUsername(a.getHighestBidder().getUsername());
 											be.setAuctionId(a.getId());
 											be.setPrice(amount);
 											be.setTimestamp(System.currentTimeMillis());
 											as.processEvent(be);
 										} catch (RemoteException e) {
 											System.err.println("Error: Couldn't create event! AnalyticsServer may be down!");
 											//e.printStackTrace();
 										}*/
 									}
 
 									a.setHighestBid(amount);
 									a.setHighestBidder(u);
 									ssock.sendLine("You successfully bid with " + amount + " on '" + a.getDescription() + "'.");
 									/*try{
 										BidEvent be = new BidEvent();
 										be.setType("BID_PLACED");
 										be.setUsername(u.getUsername());
 										be.setAuctionId(a.getId());
 										be.setPrice(amount);
 										be.setTimestamp(System.currentTimeMillis());
 										as.processEvent(be);
 									} catch (RemoteException e) {
 										System.err.println("Error: Couldn't create event! AnalyticsServer may be down!");
 										//e.printStackTrace();
 									}*/
 
 								} else {
 									ssock.sendLine("You unsuccessfully bid with " + amount + " on '" + a.getDescription() + "'. Current highest bid is " + (a.getHighestBid()));
 								}
 							}
 						} else {
 							ssock.sendLine("You have to login first!");
 						}
 						////////////////////////////////////////////
 						// !groupBid - Client creates groupBid
 						////////////////////////////////////////////
 					}else if(commandParts[0].equals("!groupBid")) {
 						if (commandParts.length != 3) {
 							ssock.sendLine("!rejected Invalid command! Should be !groupBid <auction-id> <amount>");
 						} else if (u != null && u.isLoggedIn()) {
 							int id = Integer.parseInt(commandParts[1]);
 							double amount = Double.parseDouble(commandParts[2]); 
 							Auction a = main.getAuction(id);
 							if (a == null) {
 								ssock.sendLine("!rejected Error! Auction not found!");
 							} else {
 								if (amount > a.getHighestBid()) {
 									//check if allowed
 									GroupBid gb = new GroupBid(u.getUsername(), id, amount);
 									if(main.checkGroupBid(gb)) {
 										main.groupBids.add(gb);
 										ssock.sendLine("Your groupBid with " + amount + " on '" + a.getDescription() + "' needs to be confirmed by two other users.");
 									} else {
 										main.waitingBids.add(gb);
 										ssock.sendLine("!rejected The maximum  number of active auctions with group bids is reached! Your groupBid was added to the waiting list.");
 									}
 								} else {
 									ssock.sendLine("!rejected Please use a higher bid price. Current highest bid is " + (a.getHighestBid()));
 								}
 							}
 						} else {
 							ssock.sendLine("!rejected You have to login first!");
 						}
 						////////////////////////////////////////////
 						// !confirm - Client confirms groupBid
 						////////////////////////////////////////////
 					} else if(commandParts[0].equals("!confirm")) {
 						if (commandParts.length != 4) {
 							ssock.sendLine("Invalid command! Should be !confirm <auction-id> <amount> <bidder>");
 						} else if (u != null && u.isLoggedIn()) {
 							int id = Integer.parseInt(commandParts[1]);
 							double amount = Double.parseDouble(commandParts[2]);
 							String bidder = commandParts[3];
 							//DecimalFormat f = new DecimalFormat("#0.00");
 							//String amount_string = f.format(amount);
 							//amount = Double.parseDouble(amount_string); 
 							GroupBid gb = main.getGroupBid(id, amount, bidder);
 							if (gb == null) {
 								ssock.sendLine("Error! GroubBid not found!");
 							} else {
								gb.confirm(u.getUsername());
								if(gb.isConfirmed()) {
									Auction a = main.getAuction(gb.getAuctionId());
									if(a!=null) {
										a.setHighestBid(amount);
										a.setHighestBidder(main.getUser(gb.getUser()));
 									}
									ssock.sendLine("!confirmed");
									main.groupBids.remove(gb);
 								} else {
									clientBlocked = true;
									timeout = 0;
									blockingGroupBid = gb;
									ssock.sendLine("You have to wait for another user to confirm the groupBid");
 								}
 							}
 						} else {
 							ssock.sendLine("You have to login first!");
 						}
 						////////////////////////////////////////////
 						// !verify - Client requests list again
 						////////////////////////////////////////////
 					} else if(commandParts[0].equals("!verify")) {
 						verifyListAuctions();
 					} else if(commandParts[0].equals("!end")) {
 						localShutdown = true;
 						if (u != null && u.isLoggedIn()) {
 							/*try{
 								UserEvent ue = new UserEvent();
 								ue.setType("USER_LOGOUT");
 								ue.setUsername(u.getUsername());
 								ue.setTimestamp(System.currentTimeMillis());
 								as.processEvent(ue);
 							} catch (RemoteException e) {
 								System.err.println("Error: Couldn't create event! AnalyticsServer may be down!");
 								//e.printStackTrace();
 								//
 							}*/
 							u.setLoggedIn(false);
 							//u.setSocket(null);
 							u.setUdpPort(0);
 						}
 					} else if(commandParts[0].equals("!getClientList")) {
 						String result = null;
 						if (u != null && u.isLoggedIn()) {
 							for (User u: main.users) {
 								if (u.isLoggedIn()) {
 									result = u.getSocket().getInetAddress().getHostAddress() + ":" + u.getUdpPort() + ":" + u.getUsername();
 									ssock.sendLine(result);
 								}
 							}
 							if (result == null)
 								ssock.sendLine("No active clients available at the moment!");
 							ssock.sendLine("ready");
 						} else {
 							ssock.sendLine("You have to login first!");
 							ssock.sendLine("ready");
 						}
 					} else if(commandParts[0].equals("!signedBid")) {
 						if (u != null && u.isLoggedIn()) {
 							int id = Integer.parseInt(commandParts[1]);
 							double amount = Double.parseDouble(commandParts[2]);
 							String user1 = commandParts[3].split(":")[0];
 							String timestamp1 = commandParts[3].split(":")[1];
 							String signature1 = commandParts[3].split(":")[2];
 							String user2 = commandParts[4].split(":")[0];
 							String timestamp2 = commandParts[4].split(":")[1];
 							String signature2 = commandParts[4].split(":")[2];
 							Auction a = main.getAuction(id);
 							Signature sig = Signature.getInstance("SHA512withRSA");
 							sig.initVerify(ssock.getPEMPublicKey(main.getClientsKeyDir() + user1 + ".pub.pem"));
 							sig.update(new String("!timestamp " + id + " " + amount + " " + timestamp1).getBytes());
 							Signature sig2 = Signature.getInstance("SHA512withRSA");
 							sig2.initVerify(ssock.getPEMPublicKey(main.getClientsKeyDir() + user2 + ".pub.pem"));
 							sig2.update(new String("!timestamp " + id + " " + amount + " " + timestamp2).getBytes());
 							boolean verify1, verify2;
 							verify1 = sig.verify(Base64.decode(signature1));
 							verify2 = sig2.verify(Base64.decode(signature2));
 							if (verify1 && verify2) {
 								if (a == null) {
 									ssock.sendLine("Error! Auction not found!");
 								} else {
 									if (amount > a.getHighestBid()) {
 										a.setHighestBid(amount);
 										a.setHighestBidder(u);
 										ssock.sendLine("You successfully bid with " + amount + " on '" + a.getDescription() + "'.");
 									} else {
 										ssock.sendLine("You unsuccessfully bid with " + amount + " on '" + a.getDescription() + "'. Current highest bid is " + (a.getHighestBid()));
 									}
 								}
 							} else {
 								ssock.sendLine("Couldn't make signed bid! Signatures don't match!");
 							}
 						} else {
 							ssock.sendLine("You have to login first!");
 							ssock.sendLine("ready");
 						}
 					} else {
 						ssock.sendLine("Unknown command!");
 					}
 				} catch (SocketTimeoutException e) {
 					;
 				} catch (NumberFormatException e) {
 					try {
 						ssock.sendLine("Invalid format: Found non-number where number was expected!");
 					} catch (IOException ex) {
 						u.setLoggedIn(false);
 					}
 				} catch (IOException e) {
 					//try{
 					/*UserEvent ue = new UserEvent();
 						ue.setType("USER_DISCONNECTED");
 						ue.setUsername(u.getUsername());
 						ue.setTimestamp(System.currentTimeMillis());
 						as.processEvent(ue);*/
 					/*} catch (RemoteException e1) {
 						System.err.println("Error: Couldn't create event! AnalyticsServer may be down!");
 						//e1.printStackTrace();
 					}*/
 					//System.err.println("Error while communicating with the client!");
 					//e.printStackTrace();
 					if (u != null)
 						u.setLoggedIn(false);
 					ssock.setIv(null);
 					ssock.setSecretKey(null);
 					break;
 				} catch (NoSuchAlgorithmException e) {
 					System.err.println("Error: No such algorithm!");
 				} catch (InvalidKeyException e) {
 					System.err.println("Error: Invalid key!");
 				} catch (SignatureException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		///////////////////////////
 		// SHUTDOWN THIS CONNECTION
 		///////////////////////////
 		try {
 			sock.close();
 		} catch (IOException e) {
 			System.err.println("Error closing the connection!");
 			e.printStackTrace();
 		}
 	}
 
 	public void listGroupBids() {
 		try {
 			if (main.groupBids.size() > 0) {
 				for (GroupBid g : main.groupBids) {
 					ssock.sendLine(g.toString());
 				}
 				ssock.sendLine("ready");
 			} else {
 				ssock.sendLine("No groupBids available at the moment!");
 
 				ssock.sendLine("ready");
 			}
 		} catch (IOException e) {
 			System.err.println("Error while returning a groupBid list!");
 			e.printStackTrace();
 		} catch (ConcurrentModificationException e) {
 			;
 		}
 	}
 
 	public void listAuctions() {
 		try {
 			if (main.auctions.size() > 0) {
 				// wenn eingeloggt signieren sonst normal versenden
 				if(u != null && u.isLoggedIn()) {
 					String message = "";
 					for (Auction a : main.auctions) {
 						message += "\n" + a.toString();
 					}
 					ssock.sendLine(sign(message));
 				} else {
 					for (Auction a : main.auctions) {
 						ssock.sendLine(a.toString());
 					}
 				}
 				ssock.sendLine("ready");
 			} else {
 				if(u != null && u.isLoggedIn()) {
 					ssock.sendLine(sign("No auctions available at the moment!"));
 				} else {
 					ssock.sendLine("No auctions available at the moment!");
 				}
 				ssock.sendLine("ready");
 			}
 		} catch (IOException e) {
 			System.err.println("Error while returning an auction list!");
 			e.printStackTrace();
 		} catch (ConcurrentModificationException e) {
 			;
 		}
 	}
 
 	public void verifyListAuctions() {
 		try {
 			if (main.auctions.size() > 0) {
 				if(u != null && u.isLoggedIn()) {
 					for (Auction a : main.auctions) {
 						ssock.sendLine(sign(a.toString()));
 					}
 				}
 				ssock.sendLine("ready");
 			} else {
 				ssock.sendLine("No auctions available at the moment!");
 				ssock.sendLine("ready");
 			}
 		} catch (IOException e) {
 			System.err.println("Error while returning an auction list!");
 			e.printStackTrace();
 		} catch (ConcurrentModificationException e) {
 			;
 		}
 	}
 
 	private String sign(String s) {
 		String result = "";
 		try{			
 			byte[] keyBytes = new byte[1024];
 			String pathToSecretKey = main.getClientsKeyDir() + u.getUsername() + ".key";
 			FileInputStream fis = new FileInputStream(pathToSecretKey);
 			fis.read(keyBytes);
 			fis.close();
 			byte[] input = Hex.decode(keyBytes);
 			Key secretKey = new SecretKeySpec(input,"SHA512withRSA");
 			Mac mac = Mac.getInstance("HmacSHA256");
 			mac.init(secretKey);
 
 			byte[] b = s.getBytes("UTF-8");
 
 			byte[] digest = mac.doFinal(b);
 			byte[] encoded = Base64.encode(digest); 
 
 			String hash = new String(encoded);
 			result = s + " " + hash;
 		} catch (NoSuchAlgorithmException e) {
 			System.out.println("No Such Algorithm:" + e.getMessage());
 		} catch (UnsupportedEncodingException e) {
 			System.out.println("Unsupported Encoding:" + e.getMessage());
 		} catch (InvalidKeyException e) {
 			System.out.println("Invalid Key:" + e.getMessage());
 		} catch (IOException e) {
 			System.out.println("I/O Exception:" + e.getMessage());
 			result = "Attention: Following information couldn't be verified:\n" + s;
 		}
 		return result;
 	}
 }
