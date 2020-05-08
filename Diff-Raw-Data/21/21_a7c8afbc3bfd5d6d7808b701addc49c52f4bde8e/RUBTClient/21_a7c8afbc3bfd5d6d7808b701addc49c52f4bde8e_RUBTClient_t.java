 import java.util.Scanner;
 
 import bt.Model.Bittorrent;
 import bt.Utils.CommandParser;
 
 /**
  * This is the main entry point for the client. It will just simply
  * initialize the Bittorrent client.
  * 
  * @author Ike, Robert and Fernando
  *
  */
 
 public class RUBTClient {
 	
 	private static void clientLoop() {
 		Scanner sc = new Scanner(System.in);
 		System.out.println("Input help for commands");
 		while(true) {
 			try {
 				System.out.print("%> ");
 				CommandParser.execute(sc.nextLine());
 			} catch (Exception e) {
 				System.out.println(e.getMessage());
 			}
 		}
 	}
 	
 	/**
 	 * Client entry point.
 	 * @param String[] command-line arguments.
 	 * 	(e.g. somefile.torrent, picture.jpg)
 	 */
 	public static void main(String[] args) {
 		
 		Scanner sc = new Scanner(System.in);
 		Bittorrent bittorrent = null;
 		if(args.length >= 2) {
 			bittorrent = Bittorrent.getInstance(args[0], args[1]);
 		} else {
 			System.err.println("ERROR: Insuficient arguments");
 			System.out.println("usage: RUBTClient <torrent filename> <output filename>");
 			System.exit(-1);
 		}
 		
 		// Program's loop
 		System.out.println("Connection Successfull, welcome");
 		// client's loop OR connect directly to a client (for project 0)
 		//RUBTClient.clientLoop(); /* <- Client's loop */
 		try {
 			bittorrent.connectToPeer("128.6.171.3:6916");
			// THIS GUYS SHOULD BE EITHER EXECUTED AFTER A SLEEP OR PUT SOMEPLACE ELSE.
			// this thread executes too fast.
 			//bittorrent.simpleDownloadAlgorithm();
 			//bittorrent.saveFile();
 		} catch (Exception e) { 
 			bittorrent.disposePeers();
 			System.out.println(e.getMessage());	}
 		
 	}
 }
