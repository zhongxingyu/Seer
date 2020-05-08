 package queuebot;
 
 import java.io.IOException;
 
 import org.jibble.pircbot.IrcException;
 import org.jibble.pircbot.NickAlreadyInUseException;
 
 import queuebot.bot.QueueBot;
 
 /**
  * A driver class for the QueueBot. It reads in configurations from the INI
  * file, sets the appropriate values, and then starts the bot.
  * 
  * @author Winslow Dalpe
  * 
  */
 public class QueueBotMain {
 
 	private static String server = "";
 	private static int port = 0;
 	private static String chan = "";
 	private static String nick = "";
 	private static String superuser = "";
 	private static boolean debug = false;
 
 	/**
 	 * Parses the command line parameters for options
 	 * 
 	 * @param args an array of command line arguments
 	 */
 	private static void parseArgs(String[] args) {
 		boolean setServ = false;
 		boolean setPort = false;
 		boolean setChan = false;
 		boolean setNick = false;
 		boolean setSU = false;
		boolean setDebug = false;
 
 		for (String a : args) {
 			if (setServ) {
 				server = a;
 				setServ = false;
 			} else if (setPort) {
 				try {
 					port = Integer.parseInt(a);
 					setPort = false;
 				} catch (NumberFormatException e) {
 					System.out.println("Failed to set port to value: " + a);
 					System.exit(1);
 				}
 			} else if (setChan) {
 				chan = a;
 				setChan = false;
 			} else if (setNick) {
 				nick = a;
 				setNick = false;
 			} else if (setSU) {
 				superuser = a;
 				setSU = false;
 			} else {
 				if (a.equals("-s")) {
 					setServ = true;
 				} else if (a.equals("-p")) {
 					setPort = true;
 				} else if (a.equals("-c")) {
 					setChan = true;
 				} else if (a.equals("-n")) {
 					setNick = true;
 				} else if (a.equals("-su")) {
 					setSU = true;
 				} else if (a.equals("-d")) {
 					debug = true;
 				}
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * @param args
 	 *            Command line parameters. Failure to provide all the following
 	 *            parameters may cause unexpected program errors.
 	 * 
 	 *            -s SERVERNAME -p PORTNUMBER -c CHANNEL (including the #) -n
 	 *            NICK -su SUPERUSER -d (enable debug)
 	 */
 	public static void main(String[] args) {
 		parseArgs(args);
 
 		QueueBot bot = new QueueBot(chan, superuser);
 		bot.subSetName(nick);
 		bot.setAutoNickChange(true);
 		bot.setVerbose(debug);
 		try {
 			bot.connect(server, port);
 		} catch (NickAlreadyInUseException e) {
 
 		} catch (IrcException e) {
 			bot.log("SERVER WOULD NOT LET US JOIN: " + e.getMessage());
 		} catch (IOException e) {
 			bot.log("COULD NOT CONNECT TO SERVER: " + e.getMessage());
 		}
 		bot.joinChannel(chan);
 	}
 
 }
