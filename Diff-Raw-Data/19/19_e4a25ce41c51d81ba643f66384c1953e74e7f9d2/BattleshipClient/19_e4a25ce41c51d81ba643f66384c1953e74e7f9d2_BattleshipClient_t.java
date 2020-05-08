 package battlechallenge.client;
 
 import battlechallenge.bot.ClientPlayer;
 import battlechallenge.bot.DavidBot;
 import battlechallenge.bot.KevinBot;
 
 /**
  * The Class BattleshipClient.
  */
 public class BattleshipClient {
 	
 	/** The Constant IP. */
 	public static final String IP = "127.0.0.1";
 	
 	/** The Constant PORT. */
 	public static final int PORT = 3000;
 	
 	/**
 	 * Bot to play. Allows user to develop multiple AI bots.
 	 *
 	 * @param botName the bot name
 	 * @return the client player
 	 */
 	public static ClientPlayer botToPlay(String botName) {
 		if (botName.equals("DavidBot")) {
 			return new DavidBot(botName, 0, 0, 0);
 		}
 		else if (botName.equals("KevinBot")) {
 			return new KevinBot(botName, 0, 0, 0);
 		}
 		else {
 			return new ClientPlayer(botName, 0, 0, 0);
 		}
 	}
 	
 	
 	/**
 	 * The main method.
 	 *
	 * Example
	 * 		./runClient 'Name' 3000, '127.0.0.1' 'BotName'
	 *
 	 * @param args the arguments
 	 */
 	public static void main(String[] args) {
 		String name;
 		ClientPlayer bot;
 		if (args.length > 0)
 			name = args[0];
 		else
 			name = "player" + ((int)(Math.random()*Integer.MAX_VALUE));
 		int port = PORT;
 		if (args.length > 1) 
 			try {
 			port = Integer.parseInt(args[1]);
 			} catch (NumberFormatException e) { /* ignore exception */ }
 		String ip = IP;
 		if (args.length > 2)
 			ip = args[2];
 		if (args.length > 3) {
 			bot = botToPlay(args[3]);
 		}
 		else {
 			bot = botToPlay(name);
 		}		
 		System.out.println(port + " " + ip);
 		new ServerConnection(port, ip, name, bot);
 	}
 }
