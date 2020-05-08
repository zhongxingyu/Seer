 package RingMaster;
 
 import RingMaster.Cards.Card;
 import RingMaster.Cards.CardCollection;
 
 import java.util.Scanner;
 
 /**
  * Author:      Grant Kurtz
  */
 public class Console {
 
 	private Controller controller;
 	Scanner scan;
 
 	public Console(Controller c) {
 		controller = c;
 		scan = new Scanner(System.in);
 	}
 
 	public void processCommands() {
 		System.out.print("Connecting");
 		while (!controller.isGameRunning()) {
 			try {
 				Thread.sleep(200);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			System.out.print(".");
 		}
 		System.out.println("Connected!");
 
 		while (controller.isGameRunning()) {
 
 			if (controller.ourTurn()) {
 
 				// Show Board
 				printBoard();
 
 				// Input Caret
 				processCommand();
 			} else {
 				if(controller.sendCommand())
 					scan.nextLine();
 				printBoard();
 				System.out.println("Waiting...");
 				controller.awaitOurTurn();
 				scan.nextLine();
 			}
 		}
 
		controller.sendCommand();
 		if(controller.weWon()){
 			System.out.println("We have won! YOUR ENEMY IS DEAD, AND SO ARE " +
 					"ALL HIS FRIENDS!");
			scan.nextLine();
 		}
 		else{
 			System.out.println("YOU HAVE LOST BECAUSE YOU ARE BAD!");
 		}
 
 		controller.quit();
 	}
 
 	private void printBoard() {
 		// Print Opposing player's Ring
 		Ring theirRing = controller.getTheirRing();
 		System.out.println("    " + theirRing.getCard(2) + "     " +
 				theirRing.getCard(3));
 		System.out.println();
 		System.out.println("  " + theirRing.getCard(1) + "    " +
 				theirRing.getPlayerCard() + "    " + theirRing.getCard(4));
 		System.out.println();
 		System.out.println("       " + theirRing.getCard(0) + "      " +
 				theirRing.getRotationAmount());
 
 		// Print separator
 		System.out.println("---------------------");
 
 		// Print our Ring
 		Ring ourRing = controller.getOurRing();
 		System.out.println(ourRing.getRotationAmount() + "      " + ourRing.getCard(0));
 		System.out.println();
 		System.out.println("  " + ourRing.getCard(4) + "    " +
 				ourRing.getPlayerCard() + "    " + ourRing.getCard(1));
 		System.out.println();
 		System.out.println("    " + ourRing.getCard(3) + "     " +
 				ourRing.getCard(2));
 
 		// Print our Hand
 		CardCollection hand = controller.getHand();
 		System.out.println();
 		for (int i = 0; i < hand.getCardCount(); i++) {
 			Card c = hand.getCard(i);
 			System.out.println((i + 1) + ". " + c.getName() + ", " + c.getType());
 		}
 	}
 
 	private void processCommand() {
 		String command = "";
 		System.out.print("> ");
 		command = scan.nextLine();
 		while (!isValidCommand(command)) {
 			System.err.println("'" + command + "' is an invalid command!");
 			showCommandList();
 			System.out.print("> ");
 			command = scan.nextLine();
 		}
 	}
 
 	private boolean isValidCommand(String command) {
 		String[] args = command.split(" ");
 		if (args[0].equals("play")) {
 			int card = Integer.parseInt(args[1]) - 1;
 			int ringPos = Integer.parseInt(args[2]) - 1;
 			return controller.placeCard(card, ringPos);
 		} else if (args[0].equals("menu")) {
 			showCommandList();
 			scan.nextLine();
 			return true;
 		} else if (args[0].equals("quit")) {
 			controller.quit();
 			System.out.println("Game Ended...");
 			System.exit(1);
 		}
 		else if(args[0].equals("pass")){
 			controller.pass();
 			return true;
 		}
 		return false;
 	}
 
 	private void showCommandList() {
 		System.out.println(
 				"play card pos - Place a card on the Ring\n" +
 						"quit - Quit the game");
 	}
 }
