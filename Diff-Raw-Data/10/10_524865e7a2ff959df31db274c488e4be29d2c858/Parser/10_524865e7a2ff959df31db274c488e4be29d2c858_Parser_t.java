 package nl.starapple.starterbot;
 
 import java.util.Scanner;
 
 import nl.starapple.poker.PokerMove;
 
 public class Parser {
 	
 	final Scanner scan;
 	
 	final Bot bot;
 
 	public Parser(Bot bot) {
 		this.scan = new Scanner(System.in);
 		this.bot = bot;
 	}
 
 	public void run() {
 		PokerState currentState = new PokerState();
 		while( scan.hasNextLine() ) {
			String line = scan.nextLine().trim();
			if( line.length() == 0 ) { continue; }
 			String[] parts = line.split("\\s+");
 			if( parts.length == 2 && parts[0].equals("go") ) {
 				// we need to move
 				PokerMove move = bot.getMove(currentState, Long.valueOf(parts[1]));
 				System.out.printf("%s %d\n", move.getAction(), move.getAmount());
 				System.out.flush();
 			} else if( parts.length == 3 && parts[0].equals("Match") ) {
 				// update PokerState
 				currentState.updateMatch(parts[1], parts[2]);
 			} else if( parts.length == 3 && parts[0].equals("Settings") ) {
 				// update settings
 				currentState.updateSetting(parts[1], parts[2]);
 			} else if( parts.length == 3 ) {
 				// assume it's ``botX y z''
 				// also update PokerState
 				currentState.updateMove(parts[0], parts[1], parts[2]);
 			} else {
 				System.err.printf("Unable to parse line ``%s''\n", line);
 			}
 		}
 	}
 
 }
