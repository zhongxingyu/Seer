 package server;
 
 import java.net.InetAddress;
 import java.util.Date;
 
 import org.apache.log4j.Logger;
 
 /**
  * explicitely assigned to ONE client, because running in a seperate thread
  * @see ClientListener
  * @author Babz
  *
  */
 public class ClientRequestParser {
 
 	private static final Logger LOG = Logger.getLogger(ClientRequestParser.class);
 	private UserManagement userMgmt = UserManagement.getInstance();
 	private AuctionManagement auctionMgmt = AuctionManagement.getInstance();
 
 	private String currUserName = null;
 	private InetAddress clientIp = null;
 
 	public ClientRequestParser(InetAddress clientIp) {
 		this.clientIp = clientIp;
 	}
 
 	public String getResponse(String clientRequest) {
 		String[] request = clientRequest.split("\\s");
 		String response = "";
 
 		//args: [0] = command, [1] = username, [2] = uspport
 		if(clientRequest.startsWith("!login")) {
 			int expectedNoOfArgs = 3;
 			if(currUserName != null) {
 				LOG.info("another user already logged in");
 				response = "Log out first";
 			} else if(request.length != expectedNoOfArgs) {
 				LOG.info("wrong no of args");
 				response = "expected parameter: username";
 			} else {
 				String userName = request[1];
 				String udpPort = request[2];
 				response = login(userName, udpPort);
 				LOG.info("client request 'login' finished");
 			}
 		} 
 		//args: [0] = command
 		else if(clientRequest.startsWith("!logout")) {
 			int expectedNoOfArgs = 1;
 			if(request.length != expectedNoOfArgs) {
 				LOG.info("wrong no of args");
 				response = "expected parameter: none";
 			} else if(currUserName == null) {
 				response = "You have to log in first";
 			} else {
 				response = logout();
 			}
 			LOG.info("client request 'logout' finished");
 		}
 		//args: [0] = command; allowed for anonymus users
 		else if(clientRequest.startsWith("!list")) {
 			int expectedNoOfArgs = 1;
 			if(request.length != expectedNoOfArgs) {
 				LOG.info("wrong no of args");
 				response = "expected parameter: none";
 			} else {
 				//TODO sort
 				response = auctionMgmt.getAllActiveAuctions();
 			}
 			LOG.info("client request 'list' finished");
 		}
 		//args: [0] = cmd, [1] = duration, [2] = description
 		else if(clientRequest.startsWith("!create")) {
 			int minExpectedNoOfArgs = 3;
 			if(request.length < minExpectedNoOfArgs) {
 				LOG.info("wrong no of args");
 				response = "expected parameter: duration + description";
 			} else if(!isAuthorized()) {
 				response = "You have to log in first to use this request";
 			} else {
 				int duration = Integer.parseInt(request[1]);
 				String description = clientRequest.substring(request[0].length()+request[1].length()+2);
 				response = createBid(duration, description);
 			}
 			LOG.info("client request 'create' finished");
 		}
 		//args: [0] = cmd, [1] = auction-id, [2] = amount
 		else if(clientRequest.startsWith("!bid")) {
 			int expectedNoOfArgs = 3;
 			int auctionId = Integer.parseInt(request[1]);
 			if(request.length != expectedNoOfArgs) {
 				LOG.info("wrong no of args");
 				response = "expected parameter: auction-id + amount";
 			} else if(!isAuthorized()) {
 				response = "You have to log in first to use this request";
			} else if(currUserName.equals(auctionMgmt.getAuction(auctionId).getOwner())) {
 				response = "As the auction owner you are not allowed to bid at this auction";
 			} else {
 				double amount = Double.parseDouble(request[2]);
 				response = bid(auctionId, amount);
 			}
 			LOG.info("client request 'bid' finished");
 		}
 		else {
 			response = "request couldn't be identified";
 			LOG.info("unidentified request");
 		}
 		return response;
 	}
 
 	private String login(String userName, String udpPort) {
 		boolean loginSuccessful = userMgmt.login(userName, udpPort, clientIp);
 		if(!loginSuccessful) {
 			return "Already logged in";
 		} else {
 			currUserName = userName;
 			String pendingNotifications = userMgmt.getUserByName(userName).getPendingNotifications();
 			
 			if(!pendingNotifications.isEmpty()) {
 				auctionMgmt.sendUdpMsg(userName, pendingNotifications);
 			}
 			return "Successfully logged in as " + currUserName;
 		}
 	}
 
 	private String logout() {
 		boolean logoutSuccessful = userMgmt.logout(currUserName);
 		if(!logoutSuccessful) {
 			return "Log in first";
 		} else {
 			String loggedOutUser = currUserName;
 			currUserName = null;
 			return "Successfully logged out as " + loggedOutUser;
 		}
 	}
 
 	private String createBid(int duration, String description) {
 		int id = auctionMgmt.createAuction(currUserName, duration, description);
 		Date expiration = auctionMgmt.getExpiration(id);
 		return "An auction '" + description + "' with id " + id + " has been created and will end on " + expiration;
 	}
 
 	private String bid(int auctionId, double amount) {
 		int success = auctionMgmt.bid(auctionId, amount, currUserName);
 		if(success == -1) {
 			return "Auction not available";
 		} else {
 			Auction auction = auctionMgmt.getAuction(auctionId);
 			double currHighestBid = auction.getHighestBid();
 			if (success == 0) {
 				return "You unsuccesfully bid with " + amount + " on '" + auction.getDescription() + "'. Current highest bid is " + currHighestBid + ".";
 			} else {
 				return "You successfully bid with " + auction.getHighestBid() + " on '" + auction.getDescription() + "'.";
 			}
 		}
 	}
 
 	private boolean isAuthorized() {
 		if(currUserName == null) {
 			return false;
 		}
 		return true;
 	}
 
 }
