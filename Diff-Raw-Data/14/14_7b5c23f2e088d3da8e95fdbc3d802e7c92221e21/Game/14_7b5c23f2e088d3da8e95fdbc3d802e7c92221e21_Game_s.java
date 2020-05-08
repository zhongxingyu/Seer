 package Java;
 
 import java.util.Scanner;
 
 public class Game implements Play{
 	private static Scanner scanner = new Scanner(System.in);
 	private static int playerTotal,dealerTotal;
 	private static Deck deck;
 	private static String input;
 	
 	@Override
 	public void explainRules() {
 		// TODO Auto-generated method stub
 		System.out.println("Stuff");
 	}
 
 	@Override
 	public void setUp() {
 		// TODO Auto-generated method stub
 		deck = new Deck();
 		deck.shuffle();
 		playerTotal = dealerTotal = 0;
 	}
 
 	@Override
 	public void playGame() {
 		// TODO Auto-generated method stub
 		setUp();
 		
 		while(true){
 			System.out.println("Dealers hand:");
			dealerTotal = deck.deal();
 			System.out.println("\nYour hand:");
			playerTotal = deck.deal()+deck.deal();
 			while(true){
 				System.out.println("\nHit or stay?");
 				input = scanner.nextLine();
 				if(input.toLowerCase().equals("hit")){
					playerTotal+=deck.deal();
 				}else if(input.toLowerCase().equals("stay")){
 					break;
 				}else{
 					System.out.println("Not a valid response.");
 				}
 				if(playerTotal>21){
 					break;
 				}System.out.println("Your Total: "+playerTotal);
 			}
 			if(playerTotal>21){
 				System.out.println("Your Total: "+playerTotal);
 				System.out.println("Dealer Total: "+dealerTotal);
 				System.out.println("You busted!");
 				break;
 			}
 			System.out.print("\n");
			dealerTotal+=deck.deal();
 			System.out.println("Dealer Total:"+dealerTotal+"\n");
 			while(dealerTotal<=16){
 				System.out.println("Dealer hits:");
				dealerTotal+=deck.deal();
 				System.out.println("Dealer Total: "+dealerTotal+"\n");
 			}
 			if(dealerTotal>21){
 				System.out.println("Your Total: "+playerTotal);
 				System.out.println("Dealer Total: "+dealerTotal);
 				System.out.println("Dealer busted!");
 				break;
 			}
 			if(playerTotal>dealerTotal){
 				System.out.println("Your Total: "+playerTotal);
 				System.out.println("Dealer Total: "+dealerTotal);
 				System.out.println("You win!");
 				break;
 			}
 			else if(playerTotal<dealerTotal){
 				System.out.println("Your Total: "+playerTotal);
 				System.out.println("Dealer Total: "+dealerTotal);
 				System.out.println("You lost!");
 				break;
 			}
 			else{
 				System.out.println("Your Total: "+playerTotal);
 				System.out.println("Dealer Total: "+dealerTotal);
 				System.out.println("Push!");
 				break;
 			}
 		}
 	}
 
 	@Override
 	public void gameOver(String winner) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 }
