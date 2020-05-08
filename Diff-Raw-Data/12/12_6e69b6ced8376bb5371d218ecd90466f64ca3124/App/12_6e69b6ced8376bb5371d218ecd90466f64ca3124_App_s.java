 package client;
 
 import game.Game;
 
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.util.Scanner;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 
import server.MockServer;
 import server.Server;
 import server.ServerInterface;
 import view.ConsoleView;
 import view.IView;
 import view.graphicview.GraphicView;
 
 /**
  * La classe App se charge d'initialisé le programme avec les
  * arguments passés en paramètre.
  * @author Gregory Eric Sanderson
  */
 public class App {
 	
 	/**
 	 * L'adresse de l'hôte par défaut du régistraire RMI.
 	 */
 	final public static String 	DEFAULT_HOST = "localhost";
 	
 	/**
 	 * Le port de l'hôte par défaut du régistraire RMI.
 	 */
 	final public static int 	DEFAULT_PORT = 2222;
 	
 	/**
 	 * Le nom du programme
 	 */
 	final public static String 	PROGRAM_NAME = "Cartes500";
 	
 	/**
 	 * L'interface Stub du serveur
 	 */
 	private static ServerInterface server = null;
 	
 	/**
 	 * L'instance du régistraire RMI
 	 */
 	private static Registry registry = null;
 	
 	/**
 	 * L'instance du jeu
 	 */
 	private static Game game = null;
 	
 	/**
 	 * Cette méthode se charge de lire les arguments.
 	 * @param args Les arguments du programme
 	 * @return Retourne sous forme d'objet les arguments de la ligne
 	 *         de commande
 	 */
 	private static CommandLine parseArgs(String[] args) {
 		
 		Options options = new Options();		
 		
 		@SuppressWarnings("static-access")
 		Option port = OptionBuilder.withLongOpt("port")
 									.withArgName("PORT")
 									.hasArg()
 									.withDescription("Port of the rmi registry server")
 									.create("p");
 		
 		@SuppressWarnings("static-access")
 		Option host = OptionBuilder.withLongOpt("host")
 									.withArgName("HOST")
 									.hasArg()
 									.withDescription( "Address of the rmi registry server, a.k.a the game server" )
 									.create("H");
 
 		options.addOption("h", "help", false, "print this help message");
 		options.addOption("c", "console", false, "start console app");
 		options.addOption("g", "graphic", false, "start graphical app");
 		options.addOption( host );
 		options.addOption( port );
 										
 		CommandLineParser parser = new PosixParser();
 		
 		CommandLine cmd = null;
 		
 		try{
 			cmd = parser.parse( options, args );
 		} catch (ParseException e) {
 			System.out.println("ERROR: cannot parse arguments.");
 			System.out.println( e.getMessage() );
 		}
 		
 		if( cmd != null && cmd.hasOption("help") ) {
 		
 			HelpFormatter formatter = new HelpFormatter();
 			formatter.printHelp( App.PROGRAM_NAME , options );
 			return null;
 
 		}
 
 		return cmd;
 	}
 	
 	/**
 	 * Cette méthode se charge d'effectuer la connexion avec
 	 * les régistraire RMI.
 	 * @param host L'hôte du régistraire
 	 * @param port Le port du régistraire
 	 * @return Retourne vrai si la connexion a pu être effectuée; sinon faux.
 	 */
 	private static boolean connectToServer( String host, int port ) {
 		
 		App.server = new Server();
 			
 		try {
 			
 			System.out.println("Connecting to RMI Registry...");
 			App.registry = LocateRegistry.getRegistry( host, port );
 			App.server = (ServerInterface)registry.lookup( App.PROGRAM_NAME );
 			
 		} catch (RemoteException e) {
 			System.out.println("Error during connection to server. Stacktrace:");
 			e.printStackTrace();
 			return false;
 		} catch (NotBoundException e) {
 			System.out.println("Error during connection to server. Stacktrace:");
 			e.printStackTrace();
 			return false;
 		}
 		
 		System.out.println("Connected.");
 		
 		return true;
 		
 	}
 
 	/**
 	 * Le main du programme se charge d'initialiser le jeu et lire les
 	 * arguments du programme en les interprétant.
 	 * @param args Les arguments de la ligne de commande
 	 * @throws Exception 
 	 */
 	public static void main(String[] args) throws Exception {
 		
 		CommandLine cmd = null;
 		String host = App.DEFAULT_HOST;
 		int port = App.DEFAULT_PORT;
 		
 		cmd = App.parseArgs( args );
 		
 		if( cmd != null ) {
 			
 			if ( cmd.hasOption("host") ) {
 				host = cmd.getOptionValue("host");
 			}
 			
 			if ( cmd.hasOption("port") ) {
 				port = Integer.parseInt( cmd.getOptionValue("port") );
 			}
 			
 			//App.connectToServer( host, port );
 			
 			IView view;
 			if ( cmd.hasOption("g") ) {
 				view = new GraphicView();
 			}
 			else {
 				Scanner in = new Scanner( System.in );
 				view = new ConsoleView( in , System.out );
 			}
			MockServer server = new MockServer();
 
			Client client = new Client( server, view );
 			
			server.setClient( client );
			server.startGame();
 			
 		}
 
 	}
 	
 }
