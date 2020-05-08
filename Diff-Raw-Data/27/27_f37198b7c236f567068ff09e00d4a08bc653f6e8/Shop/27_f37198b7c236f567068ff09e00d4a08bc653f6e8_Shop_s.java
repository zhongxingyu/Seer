 package TheMainGame.Classes;
 
 import java.util.Scanner;
 
 public class Shop implements GameConstants {
 	private int manaPot = 100;
 	private int healthPot = 100;
 	Scanner input;
 	
 	public Shop(){
 		input = new Scanner(System.in);
 	}
 	
 	public void enterShop(HumanCharacter theHumanCharacter){
 		System.out.println("Welcome to the shop!");
 		System.out.println("What would you like to buy?");
 		System.out.println("Type 'mana potion' for Mana Potions --20g each");
 		System.out.println("Type 'health potion' for Health Potions --15g each");
 		System.out.println("Type 'exit' to leave.");
 		System.out.println("Your gold: " + theHumanCharacter.getGold());
 		System.out.print("Command:");
 		String choice = input.nextLine();
 		if(choice.compareTo("")==0){
			System.out.println("wtf");
 		}
 		if(choice.compareTo("mana potion") == 0){
 			theHumanCharacter.buyManaPotions(this.sellPotions("mana potion"));
 		}
 		else if(choice.compareTo("health potion") == 0){
 			theHumanCharacter.buyHealthPotions(this.sellPotions("health potion"));
 		}
 		else if(choice.compareTo("exit") == 0){
 			System.out.println("Good bye!");
 		}
 		
 	}
 	
 	private int sellPotions(String potionType){
 		int amount;
 		if(potionType.compareTo("mana potion") == 0){
 			System.out.println("How many would you like to buy?");
 			amount = input.nextInt();
 			return amount;
 			
 		}else if(potionType.compareTo("health potion") == 0){
 			System.out.println("How many would you like to buy?");
 			amount = input.nextInt();
 			return amount;
 		}
 		
 		return 0;
 		
 	}
 }
