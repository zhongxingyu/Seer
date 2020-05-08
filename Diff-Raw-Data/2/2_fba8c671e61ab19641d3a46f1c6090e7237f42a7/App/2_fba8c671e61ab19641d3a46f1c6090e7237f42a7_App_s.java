 package bang.bang;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 
 /**
  * Hello world!
  *
  */
 public class App
 {
 	
 	String[] print;
 	static Random rnd = new Random();
 	static int health = 0;
 	static int rangeGun = 1;
 	static int rangeOther = 0;
 	static int myRange = rangeGun + rangeOther;
 	static List <String>	roles =  new ArrayList<String>();
 	static List <String>	hand =  new ArrayList<String>();
 	static List <String> 	bHand = new ArrayList<String>();
 	static List <GreenHand>	gHand =  new ArrayList<GreenHand>();
 	static String myRole = "";
 	static int inHand = 0;
 	static boolean isVolcanic = false;
 	
 	public static void main( String[] args )
 	{
 		boolean start = true;
 		File file = new File("bang.txt");
 		
 		//card holders
 		//    	String colorOfCard = "";
 		//    	String B1 = "";
 		//    	String B2 = "";
 
 
 		BufferedReader reader = null;
 
 		try {
 			reader = new BufferedReader(new FileReader(file));
 			String line;
 			String[] action;
 
 
 			line = reader.readLine();
 			//Read one line at a time, printing it.
 			while (start) {
 
 
 				if(line.equals("start"))
 				{  			  
 					start = true;
 					/*
 					 *  To do
 					 *  initialize the game state
 					 */			   
 
 					//Initilaize Myself;
 					health = 4;
 					System.out.print("start command: ");
 
 				}
 				else if(line.equals("end"))
 				{	  /*
 				 *  To do
 				 *  Cleanup
 				 */
 
 					start = false;
 					System.out.print("end command");
 				}
 
 
 				// close file and wait for new input
 				try {
 					reader.close();
 					Thread.sleep(5000);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 				reader = new BufferedReader(new FileReader(file));  	
 
 				line = reader.readLine();
 
 				//read file for command to play the game
 				action = line.split(",");
 
 				if(action.length < 1){
 					// something is wrong
 
 				} else if (action[0].equals("b3")) {
 					/*
 					 * to do
 					 * add cards to correct hand
 					 */
 					b3Button(action[1]);
 					
 					System.out.print("b3 command");
 					
 				} else if (action[0].equals("b1")){
 
 					/*
 					 * to do
 					 * Add 2 cards to hand and play my turn
 					 * If role card
 					 * scan for roles (sheriff, deputy, outlaw, renegate)
 					 */
 					if (action[1].equals("role")) {						
 						roles.add(action[2]);
 						if(myRole.equals("")) {
 							myRole = roles.get(0);
 							if (myRole.equals("sheriff"))
 								printSomething(1); // announce myself if sheriff
 						}
 						
 						System.out.print("starting a new game...  role has been set");
 					}
 					else { // Add to hand
						if (action[1].equals("green"))
 							addToHand(3, action[2]);
 						else if (action[1].equals("blue"))
 							addToHand(2, action[2]);
 						else 
 							addToHand(1, action[2]);
 					}
 					
 
 				} else if (action[0].equals("b2"));
 
 				/*
 				 * to do 
 				 * respond to card b2
 				 * if role
 				 * Update player death
 				 */
 				if (action[1].equals("role")) {						
 					for (int i = 0; i < roles.size(); i++) {
 						if (roles.get(i).equals(action[2]))
 							roles.remove(i);
 					}
 					
 					printSomething(1); // Smack Talk dead person
 					
 					System.out.print("someone is finished");
 				}
 				else { // Respond!
 					b2Button(action[2]);
 				}
 
 			}    		    		  
 
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		} 
 
 	}
 
 	public static void addToHand(int handType, String card) {
 		// resulted from b3 - used to add initial hand and general store cards
 		switch (handType) {
 		case 1: hand.add(card);
 				inHand++;
 		case 2:	GreenHand tmp = new GreenHand(card);
 				gHand.add(tmp);
 				inHand++;
 		default: break;
 		}
 	}
 	
 	public static void b3Button(String card) {
 		// see excel sheet B3 table
 		String print = new String();
 		String currentCard = new String();
 		int playerIndex;
 		int sheriffPos = findSheriff();
 		
 		if (card.equals("jail"))
 			play("miss turn");
 		if (card.equals("dynamite")) {
 			health = health - 3;
 			if (health <=0) {
 				for (int i = 0; i < bHand.size(); i++) {
 					currentCard = hand.get(i);
 					if (currentCard.equals("beer")) {
 						play("still alive");
 						break;
 					}
 				}	
 			}
 			else
 				play("ouch!!!");
 		}
 		
 		for (int i = 0; i < bHand.size(); i++) {
 			currentCard = bHand.get(i);
 			
 			if (currentCard.equals("jail")) {
 				do
 					playerIndex = choosePlayerIndex(roles.size());
 				while (playerIndex != sheriffPos);
 			}
 			if (currentCard.equals("dynamite")) {
 				playerIndex = choosePlayerIndex(roles.size());
 			}
 			if (currentCard.equals("binocular") || currentCard.equals("scope")) {
 				rangeOther++;
 				play("Card: " + i);
 			}
 			if (currentCard.equals("barrel") || currentCard.equals("hideout") || currentCard.equals("mustang"))
 				play("Card: " + i);
 			if (currentCard.equals("schofield") && rangeGun < 2) {
 				rangeGun = 2;
 				play("Card: " + i);
 			}
 			if (currentCard.equals("remindton") && rangeGun < 3){
 				rangeGun = 3;
 				play("Card: " + i);
 			}
 			if (currentCard.equals("schofield") && rangeGun < 4){
 				rangeGun = 4;
 				play("Card: " + i);
 			}
 			if (currentCard.equals("schofield") && rangeGun < 5){
 				rangeGun = 5;
 				play("Card: " + i);
 			}
 			if (currentCard.equals("volcanic")) {
 				if (!(myRole.contains("outlaw") && sheriffPos > 1)) {
 					rangeGun = 1;
 					isVolcanic = true;
 					play("Card: " + i);
 				}
 			}
 		}
 		
 		for (int i = 0; i < gHand.size(); i++) {
 			GreenHand currentGreen = gHand.get(i);
 			
 			if (currentGreen.isInPlay() && !currentGreen.isActive()) {
 				currentGreen.activate();
 				gHand.set(i,currentGreen);
 			}
 		}
 		
 		for (int i = 0; i < gHand.size(); i++) {
 			GreenHand currentGreen = gHand.get(i);	
 			String cGCard = currentGreen.getCard();
 			
 			if (currentGreen.isActive()) {	
 				if (cGCard.equals("contestoga") || cGCard.equals("cancan") || cGCard.equals("pepperbox")
 						|| cGCard.equals("howitzer") || cGCard.equals("buffalorifle") || cGCard.equals("knife")
 						|| cGCard.equals("derringer")) {
 					play(String.valueOf(i + choosePlayerIndex(myRange)));
 				}
 				if (cGCard.equals("canteen") && health < 4)
 					play("canteen heal");
 				if (cGCard.equals("ponyexpress"))
 						play("ponyexpress" + i);
 			}
 			
 			if (!currentGreen.isInPlay()) {
 				currentGreen.play();
 				gHand.set(i,currentGreen);
 				play("green card" + i);
 			}
 		}
 		
 		for (int i = 0; i < hand.size(); i++) {
 			String cBCard = hand.get(i);
 			
 			if (cBCard.equals("panic")) {
 				play(String.valueOf(i + choosePlayerIndex(rangeOther)));
 			}
 			if (cBCard.equals("ragtime") || cBCard.equals("brawl")) {
 				play(String.valueOf(i + randomCard(i) + choosePlayerIndex(rangeOther)));
 			}
 			if (cBCard.equals("catbalou")) {
 				play(String.valueOf(i + choosePlayerIndex(roles.size())));
 			}
 			if (cBCard.equals("bang")) {
 				play(String.valueOf(i + choosePlayerIndex(myRange)));
 			}
 			if (cBCard.equals("gatling")) {
 				play("gatling" + i + choosePlayerIndex(myRange));
 			}
 			if (cBCard.equals("punch")) {
 				play(String.valueOf(i + choosePlayerIndex(rangeOther)));
 			}
 			if (cBCard.equals("springfield") || cBCard.equals("duel")) {
 				play(String.valueOf(i + randomCard(i) + choosePlayerIndex(roles.size())));
 			}
 			if (cBCard.equals("indians") || cBCard.equals("wellsfargo") || cBCard.equals("stagecoach")) {
 				play("card" + i);
 			}
 			if (health < 4) {
 				if (cBCard.equals("beer") || cBCard.equals("saloon")) {
 					play("beer" + i);
 				}
 				if (cBCard.equals("whiskey") || cBCard.equals("tequila")) {
 					play(String.valueOf("whiskey" + i + randomCard(i)));
 				}
 			}
 		}
 		
 		
 		
 		//
 			
 		
 //		switch (card) {
 //		case "panic":	
 //			break;
 //		default: break;
 //		}
 	}
 	
 	public static int randomCard(int index) {
 		int rand = rnd.nextInt(hand.size());
 		if (rand == index)
 			rand = randomCard(index);
 		return rand;
 	}
 	
 	public static void b2Button(String card) {
 		// see excel sheet B2 table
 		switch (1) {
 		case 1:
 			break;
 		default: break;
 		}
 	}
 	
 	public static void play(String str) {
 		
 	}
 	
 	public static int choosePlayerIndex(int range) {
 		/*
 		 * Sheriff: shoot all
 		 * Outlaw: shoot sheriff if in range, else random
 		 * Deputy: shoot all except sheriff
 		 * Renegade: shoot all except sheriff, sheriff last
 		 * 
 		 */
 		int sheriffPos = findSheriff();
 		int direction = 0; // 0 = left, 1 = right
 		
 		if (!(sheriffPos == 0))
 			if (((double)sheriffPos/(double)roles.size()) > 0.5)
 				direction = 1;
 		
 		
 		int index = rnd.nextInt(Math.abs(range));
 		if (index == 0) 
 			index++;
 		int sheriff = findSheriff();
 		
 //		if (myRole.equals("sheriff")) {
 //			return index;
 //		}
 //		else 
 		if (myRole.equals("renegade")) {
 			if (roles.get(index).equals("sheriff") && roles.size() > 2)
 			index = choosePlayerIndex(range);
 		}
 		else if (myRole.equals("deputy1") || myRole.equals("deputy2")) {
 			if (roles.get(index).equals("sheriff"))
 				index = choosePlayerIndex(range);
 		}
 		else if (myRole.equals("outlaw1") || myRole.equals("outlaw2") || myRole.equals("outlaw3")) {
 			if (sheriff <= myRange)
 				index = sheriff;
 		}
 		if (direction == 1)
 			return Math.abs(roles.size() - index);
 		else
 			return index;
 	}
 	
 	public static int findSheriff () {
 		for (int i = 0; i < roles.size(); i++)
 			if (roles.get(i).equals("sheriff"))
 				return i;
 		return 0;
 	}
 	
 	public static void printSomething(int index) {
 		/*
 		 * This function has all the LED prints
 		 * in a nice switch statement
 		 */
 		switch (index) {
 		case 1:  // print
 		case 2:  
 		case 3:  
 		case 4:  
 		case 5:  
 		case 6:  
 		case 7:  
 		case 8:  
 		case 9:  
 		case 10: 
 		case 11: 
 		case 12: 
 			break;
 		default: break;
 		}
 	}
 }
