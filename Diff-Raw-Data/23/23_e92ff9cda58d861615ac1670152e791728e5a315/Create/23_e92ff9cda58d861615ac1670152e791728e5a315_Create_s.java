 package client.command;
 
 import server.logic.AuctionDATABASE;
 import network.tcp.server.TCPServerConnection;
 import command.ICommand;
 
 public class Create implements ICommand{
 	private TCPServerConnection connection;
 	
 	 public Create(TCPServerConnection connection) {
 		 this.connection=connection;
 	}
 	 
 	@Override
 	public int numberOfParams() {
 		return 2; // <duration> <description>
 	}
 
 	@Override
 	public String execute(String[] params) {
 		if(connection.getUser()==null)
 			return "You have to log in first";
 		
 		long duration;
 		try{
 			duration = Long.valueOf(params[0].trim());
 		}catch(NumberFormatException e){
 			return params[0]+" is not a number!"; 
 		}
 		String description = fixDescription(params[1]);
 		
 		
 		
 		int x= AuctionDATABASE.getInstance().createAuction(connection.getUserObject(), description, duration);
 		String answer="";
 		
 		switch(x){
 		
 		case AuctionDATABASE.AUCTION_EXPIRED:
 			answer= "!print "+"the selected auction has expired.";
 		case AuctionDATABASE.NEEDS_MORE_MONEY:
 			answer= "!print "+"more cash is required to overbid.";
 		case AuctionDATABASE.NO_AUCTION_WITH_ID_FOUND:
 			answer= "!print "+"no auction with this id.";
 		case AuctionDATABASE.SUCCESSFULLY_PLACED_BID:
 			//answer="!print "+ AuctionDATABASE.getInstance().getCreationString();
 		}
		return answer;
 	}
 
 	@Override
 	public boolean needsRegistration() {
 		return true;
 	}
 	
 	private String fixDescription(String input){
 		
 		return input.replace("!_!", " ");
 		
 	}
 
 	
 }
