 package client;
 
 import org.apache.log4j.Logger;
 
 public class MessageParser {
 
 	private static final Logger LOG = Logger.getLogger(MessageParser.class);
 	
 	private final String STR_NEWBID = "!new-bid"; 
 	private final String STR_ENDED = "!auction-ended";
 	
 	public void parseMsg(String msg) {
 		//args: [0] = cmd, [1] = description
 		if(msg.startsWith(STR_NEWBID)) {
 			String description = msg.substring(STR_NEWBID.length());
 			System.out.println("You have been overbid on '" + description + "'");
 			LOG.info("server response 'new-bid' finished");
 		} 
 		//args: [0] = cmd, [1] = winner, [2] = amount, [3] = description
 		else if(msg.startsWith(STR_ENDED)) {
 			String[] args = msg.split("\\s");
 			String winner = args[1];
 			String amount = args[2];
			// + 3 because of the whitespaces
			String description = msg.substring(STR_ENDED.length() + winner.length() + amount.length() + 3);
 			//TODO fallunterscheidung ich - du
 			System.out.println("The auction '" + description + "' has ended. " + winner + " won with " + amount);
 			LOG.info("server response 'auction-ended' finished");
 		} else {
 			System.out.println("server response couldn't be identified");
 		}
 	}
 
 }
