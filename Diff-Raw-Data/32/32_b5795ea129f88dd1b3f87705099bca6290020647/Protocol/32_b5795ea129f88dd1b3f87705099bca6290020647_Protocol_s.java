 package dslab.auctionserver;
 
 import java.math.BigDecimal;
 import java.net.InetAddress;
 import java.util.Date;
 
 import dslab.analyticsserver.AuctionEvent;
 import dslab.analyticsserver.BidEvent;
 import dslab.analyticsserver.EventNotFoundException;
 import dslab.analyticsserver.UserEvent;
 import dslab.channels.Base64Channel;
 import dslab.channels.ChannelDecorator;
 import dslab.channels.TcpChannel;
 import java.io.IOException;
 import java.security.InvalidAlgorithmParameterException;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.crypto.BadPaddingException;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 import java.security.Key;
 import javax.crypto.Mac; 
 import org.bouncycastle.util.encoders.Base64;
 
 public class Protocol {
 
     public Protocol() {
     }
 
     public String processInput(String inputWhole, tcpRequestCommunication request) throws UnknownParameterException {
 	
	//System.out.println("got: "+inputWhole);
 	
 	if (inputWhole != null) {
 	    String[] input = inputWhole.split(" ");
 	    if (input.length > 0) {
 		if (input[0].equals("!list")) {
 		    return this.list(input, request);
 		} 
 		else if (input[0].equals("!logout")) {
 		    return this.logout(input, request);
 		}
 		else if (input[0].equals("!create")) {
 		    return this.create(input, request);
 		}
 		else if (input[0].equals("!end")) {
 		    return this.end(input, request);
 		}
 		else if (input[0].equals("!bid")) {
 		    return this.bid(input, request);
 		}
 		else {
 		    //check if the user is logged in
 		    if (request.getCurrentUser() != null) {
 			//user is logged in
 		    } else {
 			//shake hands
 			try {
 			    return request.shakeHands(inputWhole);
 
 
 			} catch (InvalidAlgorithmParameterException ex) {
 			    Logger.getLogger(Protocol.class.getName()).log(Level.SEVERE, null, ex);
 			} catch (NoSuchAlgorithmException ex) {
 			    Logger.getLogger(Protocol.class.getName()).log(Level.SEVERE, null, ex);
 			} catch (NoSuchPaddingException ex) {
 			    Logger.getLogger(Protocol.class.getName()).log(Level.SEVERE, null, ex);
 			} catch (InvalidKeyException ex) {
 			    Logger.getLogger(Protocol.class.getName()).log(Level.SEVERE, null, ex);
 			} catch (IllegalBlockSizeException ex) {
 			    Logger.getLogger(Protocol.class.getName()).log(Level.SEVERE, null, ex);
 			} catch (BadPaddingException ex) {
 			    Logger.getLogger(Protocol.class.getName()).log(Level.SEVERE, null, ex);
 			}
 		    }
 		}
 		
 		return "Command not recognized! Use !login, !logout, !list, !create, !bid or !end!";
 	    }
 	    return "Empty input!";
 	}
 	return "No input!";
     }
 
     private String logout(String[] input, tcpRequestCommunication request) {
 	if (input.length == 1) {
 	    if (request.getCurrentUser() != null) {
 		String username = request.getCurrentUser().getUsername();		
 		synchronized(request){
 		request.getChannel().send("logging out CODE");
 		request.resetChannel();
 		request.getCurrentUser().logout();
 		request.setCurrentUser(null);
 		}
 		try {
 		    AnalyticsServerProtocol.getInstance().processEvent(new UserEvent(UserEvent.logout, new Date().getTime(), username));
 		} catch (EventNotFoundException e) {
 		    // TODO Auto-generated catch block
 		    e.printStackTrace();
 		}
 		System.out.println("Success");
 		return "!logout" + System.getProperty("line.separator") + "Successfully logged out as " + username + "!";
 	    }
 	    return "You have to log in first!";
 	}
 	return "Wrong command: !logout requires no additional argument! Usage: !logout";
     }
 
     private String create(String[] input, tcpRequestCommunication request) {
 	if (input.length >= 3) {
 	    if (request.getCurrentUser() != null) {
 		long duration;
 		try {
 		    duration = Long.parseLong(input[1]);
 		} catch (NumberFormatException e) {
 		    return "duration must be a long! Please try again!";
 		}
 		String description = input[2];
 		for (int i = 3; i < input.length; i++) {
 		    description += " " + input[i];
 		}
 		Auction auction = new Auction(description, duration, new Date(), request.getCurrentUser());
 		synchronized (Data.getInstance()) {
 		    Data.getInstance().addAuction(auction);
 		    try {
 			int i = Data.getInstance().getAuctionIndex(auction);
 			AnalyticsServerProtocol.getInstance().processEvent(new AuctionEvent(AuctionEvent.started, new Date().getTime(), Auction.getCounter() - 1));
 		    } catch (EventNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		    }
 		}
 		return "An auction '" + auction.getDescription() + "' with id " + auction.getId() + " has been created and will end on " + auction.getEndDate() + ".";
 	    }
 	    return "You have to log in to create an auction item!";
 	}
 	return "Wrong command: !create requires at least two additional arguments! Usage: !create <duration> <description>";
 
     }
 
     private String end(String[] input, tcpRequestCommunication request) {
 
 	if (input.length == 1) {
 	    if (request.getCurrentUser() != null) {
 		request.getCurrentUser().logout();
 		request.setCurrentUser(null);
 		return "!end";
 	    }
 	    return "!end";
 	}
 	return "Wrong command: !end requires no additional argument! Usage: !end";
     }
 
     private String bid(String[] input, tcpRequestCommunication request) {
 	if (input.length == 3) {
 	    if (request.getCurrentUser() != null) {
 		int id;
 		try {
 		    id = Integer.parseInt(input[1]);
 		} catch (NumberFormatException e) {
 		    return "auctionid must be an integer! Please try again!";
 		}
 		double amount;
 		try {
 		    amount = Double.parseDouble(input[2]);
 		} catch (NumberFormatException e) {
 		    return "amount must be a BigDecimal! Please try again!";
 		}
 		Auction auction = new Auction(id);
 		int index = Data.getInstance().getAuctionIndex(auction);
 		if (index == -1) {
 		    return "No such id exists! Please check !list again!";
 		}
 		auction = Data.getInstance().getAuction(index);
 		String bidOut = auction.bid(request.getCurrentUser(), amount);
 		if (bidOut.equals("You can't bid any more!")) {
 		    return bidOut;
 		}
 		boolean success = Boolean.parseBoolean(bidOut);
 		if (success) {
 		    try {
 			AnalyticsServerProtocol.getInstance().processEvent(new BidEvent(BidEvent.placed, new Date().getTime(), request.getCurrentUser().getUsername(), id, amount));
 		    } catch (EventNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		    }
 		    return "You successfully bid with " + amount + " on '" + auction.getDescription() + "'.";
 		}
 		return "You unsuccessfully bid with " + amount + " on '" + auction.getDescription() + "'. Current highest bid is " + auction.getHighestBid() + ".";
 	    }
 	    return "You have to log in to bid on an auction item!";
 	}
 	return "Wrong command: !bid requires two additional arguments! Usage: bid <auction-id> <amount>";
     }
 
     private String list(String[] input, tcpRequestCommunication request) {
 	if (input.length == 1) {
 		if (request.getCurrentUser() == null) {
 	    return Data.getInstance().listAuctions();
 		}
 		else {
 			try{
 			String auctions = Data.getInstance().listAuctions();
 			Key secretKey = request.getCurrentUser().getSecretKey();
 					Mac hMac = Mac.getInstance("HmacSHA256");
 					hMac.init(secretKey);
 					hMac.update(auctions.getBytes());
 					byte[] hash = hMac.doFinal();
 					byte[] hashencoded = Base64.encode(hash);
 					System.out.println(auctions + System.getProperty("line.separator") + hashencoded);
 					return auctions + System.getProperty("line.separator") + hashencoded;
 			}
 			catch (NoSuchAlgorithmException e) {
 			    // TODO Auto-generated catch block
 			    e.printStackTrace();
 			}
 			catch (InvalidKeyException e) {
 			    // TODO Auto-generated catch block
 			    e.printStackTrace();
 			}
 		}
 	}
 	return "Wrong command: !list requires no additional argument! Usage: !list";
     }
 }
