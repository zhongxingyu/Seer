 package bang.bang;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 
 
 /**
  * Hello world!
  *
  */
 public class App
 {
 	
 	static String[] printArray = {"empty",
 		"You-lily-livered-yellow-bellied-scoundrel",
 		"Butthead",
 		"You-smell-like-manure.",
 		"You're-as-ugly-as-a-burnt-boot.",
 		"You-couldn't-teach-a-hen-to-cluck.",
 		"You're-as-dull-as-a-dishwasher.",
 		"You-gutless,-yellow,-pie-slinger",
 		"So,-from-now-on,-you-better-watch-yor-back",
 		"You-are-uglier-than-a-new-sheared-sheep.",
 		"You-Irish-bug",
 		"Ouch!",
 		"You-got-me!",
 		"Barf!",
 		"Jackpot!",
 		"I'm-laughing-all-the-way-to-the-bank.",
 		"Bonus!",
 		"Mmmmm…beer.",
 		"Drinks-all-around!",
 		"Yay...-water",
 		"One-tequila,-two-tequlia,-three-tequila,-floor.",
 		"What-wiskey-will-not-cure,-there-is-no-cure-for.",
 		"Thank-heaven",
 		"INDIANS!!!",
 		"When-I'm-done-with-you,-there-won't-be-enough-left-of-you-to-snore.",
 		"Cause-of-death?-Lead poisoning.",
 		"Let's-settle-this-once-and-for-all,-runt!-Or-ain't-you-got-the-gumption?",
 		"I-aim-to-shoot-somebody-today-and-I'd-prefer-it'd-be-you.",
 		"Bang!",
 		"Bang,-Bang!",
 		"Dodge-this!",
 		"Not-as-deceiving-as-a-low-down,-dirty...-deceiver.",
 		"Make-like-a-tree-and-get-out-of-here.",
 		"That's-about-as-funny-as-a-screen-door-on-a-battleship.",
 		"Tim-is-a-saboteur.",
 		"Let's-go!-I-got-me-a-runt-to-kill!",
 		"Let-these-sissies-have-their-party.",
 		"Nobody-calls me-\"Mad Dog\",-especially-not-some-duded-up,-egg-suckin'-gutter-trash.",
 		"I-hate-manure.",
 		"What's-wrong,-McFly.-Chicken?",
 		"New-York-city!-Get-a-rope.",
 		"There's-a-snake-in-my-boot.",
 		"Gimme,-Gimme!",
 		"I'll-be-taking-that.",
 		"Mine!",
 		"Get-that-trash-out-of-here!",
 		"Go-back-to-where-you-came-from.",
 		"Yeah,-you-can-go-ahead-and-get-rid-of-that.",
 		"I'm-armed-and-dangerous.",
 		"Which-way-to-the-gun-show?",
 		"I'm-on-a-horse.",
 		"Ha,-you-can't-find me!",
 		"Saved-by-the-Barrel.",
 		"Setting-my-sights.",
 		"I-spy-with-my-little-eye.",
 		"Lets-make-this-a-little-more-interesting.",
 		"Kaboom!",
 		"I'm-locking-you-up!",
 		"Nobody-knows-the-trouble-I've-seen…",
 		"Thanks,-sucker!",
 		"I'm-getting-better.",
 		"You-Missed.",
 		"In-yo-face!",
 		"You-couldn't-hit-water-if-you-fell-out-of-a-boat.",
 		"I'm-Buford-'Pi'-Tannen",
 		"Blargh!-*DEATH*",
 		"I-call-my-gun-Vera",
 		"Trash-it!"};
 	
 	static Random rnd = new Random();
 	static int health = 4;
 	static int rangeGun = 1;
	static int rangeOther = 1;
 	static int myRange = rangeGun + rangeOther;
 	static List <String>	roles =  new ArrayList<String>();
 	static List <String>	hand =  new ArrayList<String>();
 	static List <String> 	bHand = new ArrayList<String>();
 	static List <GreenHand>	gHand =  new ArrayList<GreenHand>();
 	static String myRole = "";
 	static int inHand = 0;
 	static String lastCardPlayed = "";
 	static boolean isVolcanic = false;
 	static Long lastModifiedDate;
 	static boolean isFirstStart = true;
 	static boolean isGameOver = false;
 	static int bangs = 0;
 
 	
 	public static void main( String[] args )
 	{
 		boolean start = true;
 		File file = new File("bang.txt");
 		
 		//card holders
 		//    	String colorOfCard = "";
 		//    	String B1 = "";
 		//    	String B2 = "";
 
 		
         lastModifiedDate = file.lastModified();
 		BufferedReader reader = null;
 
 		try {
 			reader = new BufferedReader(new FileReader(file));
 			String line;
 			String[] action;
 
 
 			line = reader.readLine();
 			//Read one line at a time, printing it.
 			while (start) {
 
 
 				if(line.equals("start") || isFirstStart)
 				{  			  
 					start = true;
 					/*
 					 *  To do
 					 *  initialize the game state
 					 */			   
 
 					//Initilaize Myself;
 				//	health = 4;
 					System.out.println("start command: ");
 					isFirstStart = false;
 
 				}
 				else if(isGameOver)
 				{	  /*
 				 *  To do
 				 *  Cleanup
 				 */
 
 					roles = new ArrayList<String>();
 					hand =  new ArrayList<String>();
 					bHand = new ArrayList<String>();
 					gHand =  new ArrayList<GreenHand>();
 					myRole = "";
 				//	lastModifiedDate = 0L;
 					isVolcanic = false;
 					inHand = 0;
 					health = 4;
 					rangeGun = 1;
 					rangeOther = 0;
 					myRange = rangeGun + rangeOther;
 					lastCardPlayed = "";
 					isFirstStart = true;
 					isGameOver = false;
 					
 					
 					
 					//start = false;
 					System.out.println("Game over!!!");
 					
 				}
 
 
 				// close file and wait for new input
 				try {
 					reader.close();
 					while(lastModifiedDate.equals(file.lastModified()))
 					{
 						Thread.sleep(5000);
 					}	
 					lastModifiedDate = file.lastModified();
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 				reader = new BufferedReader(new FileReader(file));  	
 
 				line = reader.readLine();
 				
 				while(line.equals(null)){
 					try {
 						Thread.sleep(5000);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					line = reader.readLine();
 				}
 
 				while(line.split(",").length < 2){
 					try {
 						
 						reader.close();
 						Thread.sleep(5000);
 						reader = new BufferedReader(new FileReader(file)); 
 						line = reader.readLine();
 						lastModifiedDate = file.lastModified();
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				//read file for command to play the game
 				action = line.split(",");
 				System.out.println(action[0] + "," + action[1] + "," + action[2]);
 
 				if(action.length < 1){
 					// something is wrong
 
 				} else if (action[0].equals("b3")) {
 					/*
 					 * to do
 					 * add cards to correct hand
 					 */
 					//System.out.println("b3 command");
 					b3Button(action[2]);
 					
 					
 					
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
 							if (myRole.equals("sheriff")){
 								health++;
 								play("I-am-el-Sheriffo!"); // announce myself if sheriff
 							}
 						}
 						
 						System.out.println("starting a new game...  role has been set");
 					}
 					else { // Add to hand
 						if (action[1].equals("gren"))
 							addToHand(2, action[2]);
 						else if (action[1].equals("blue"))
 							addToHand(3, action[2]);
 						else 
 							addToHand(1, action[2]);
 					}
 					
 
 				} else if (action[0].equals("b2")){
 					String card = action[2];
 					String cardType = action[1];
 					
 					//b2 role action , one of the player is dead, reset role
 					if (action[1].equals("role")) {						
 						for (int i = 0; i < roles.size(); i++) {
 							if (roles.get(i).equals(action[2])){
 								roles.remove(i);
 								//print something
 								play("You-dead-player-" + i +  "who-is-the-champ-now?");
 							}							
 						}						
 					}
 					
 //					if(lastCardPlayed.equals("panic") || lastCardPlayed.equals("conestoga") || lastCardPlayed.equals("ragtime") 
 //							  || lastCardPlayed.equals("cancan") || lastCardPlayed.equals("catbalou") 
 //							     || lastCardPlayed.equals("brawl")) {
 //						//do action for taking a card away from my hand
 //						takeCardFromHand(cardType, card);
 //						lastCardPlayed = card;
 //					}
 					
 					
 					if(card.equals("panic") || card.equals("conestoga") || card.equals("ragtime") 
 							  || card.equals("cancan") || card.equals("cat") || card.equals("brawl")){
 						takeCardFromHand(cardType, card);
 						//lastCardPlayed = card;
 					}
 					else if(card.equals("bang") || card.equals("pepperbox") || card.equals("howitzer") || card.equals("Buffalorifle")
 							 || card.equals("punch") || card.equals("knife") || card.equals("derringer") 
 							     || card.equals("springfield") || card.equals("indians") || card.equals("duel")){
 						
 						// do action for to check for miss, no miss check for health, last health check for beer. if last health play beer
 						System.out.println("inside: someoneShottAtMe");
 						someoneShootAtMe(card);
 						lastCardPlayed = card;
 					}
 //					else if(card.equals("indian") || card.equals("duel")){
 //						// play bang , if no bang in hand, minus health, if last bullet, 
 //						someoneShootMe(card);
 //						//print something and announce finished from the game						
 //					}
 					else if(card.equals("saloon") || card.equals("tequila")){
 					   // heal me, check for health if full health skip	
 						healMe();
 						lastCardPlayed = card;
 					}
 					else if(card.equals("dynamite")){
 						ouch();
 					}
 					
 				}
 				
 				System.out.println("hands: " + hand );
 				System.out.println("myrole: " + myRole);
 				System.out.println("roles: " + roles);
 				System.out.println("blue hands: " + bHand);
 				for (int i = 0; i < gHand.size(); i++) {
 					System.out.println("green hands: " + gHand.get(i).getCard() + ":" + gHand.get(i).isActive());
 				}
 				System.out.println("health: " + health);
 
 			}    		    		  
 
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		} 
 
 	}
 
 	private static void ouch() {
 		boolean hld = false;
 		if(health <= 3){
 			for (int i = 0; i < hand.size(); i++) {
 				if (hand.get(i).equals("beer")) {
 					hand.remove(i);
 					health = 1;
 					play("Still Alive!");					
 				}
 				hld=true;	
 			}
 			if (!hld){
 				health = 0;
 				isGameOver = true;
 				play(" I have no health, it's game over for me ");
 			}
 		}
 		else{
 			health = health - 3;
 			play("ouch"  +  printArray[17]);
 			
 		}
 		
 	}
 
 	private static void healMe() {
 		// TODO Auto-generated method stub
 		if(health >= 4){
 			
 			//Print i have full health don't need to heal me
 			play("I-have-full-health,-don't-need-your-heal");
 		}
 		else
 		{
 			health = health + 1;
 			
 			//print my full health
 			play("my health-should be:-" + health);
 		}
 		
 	}
 
 	private static void someoneShootAtMe(String card) {
 		int idxOfHand = 0;
 		if(card.equals("indians") || card.equals("duel")){
 			//do i have a miss
 			if(hand.contains("bang")){
 				
 				idxOfHand = hand.indexOf("bang");
 				hand.remove(idxOfHand);
 				
 				//print something to pi hand + bang, index
 				play ("Hand:-" + String.valueOf(idxOfHand) +"-card-should-be-bang "  +  printArray[30].toString());
 				
 			}
 			else{
 				//print something is wrong
 				//lost one health, if health is <= 0 print out die message
 				if(health >=2){
 					health = health - 1;
 					play ("Almost-got-me" + printArray[11]  + "health-is:-" + health );
 				}
 				else
 				{
 					play ("I-am-dead-" + printArray[13]);
 				}
 			}
 		}
 		else 
 		{
 			GreenHand green;
 			for (int i = 0; i < gHand.size();i++){
 				green = gHand.get(i);
 				if (green.isActive() && (green.getCard().equals("sombrero") || green.getCard().equals("tengallonhat") 
 						|| green.getCard().equals("ironplate") || green.getCard().equals("bible"))) {
 					gHand.remove(i);
 					play("Play Green: " + i + "-you-can-do-better-than-this!");
 					i = gHand.size();
 				}
 			}
 			
 			
 //			if(gHand.contains("sombrero") || gHand.contains("tengallonhat") || gHand.contains("ironplate") 
 //					 || gHand.contains("bible")){
 //
 //				 Iterator<GreenHand> iter = gHand.iterator();
 //				 while (iter.hasNext()) {      // any more element
 //			         // Retrieve the next element, explicitly downcast from Object back to String
 //			         GreenHand gh = iter.next();
 //			         if(gh.Card.equals("sombrero") && gh.isActive()){
 //			        	idxOfHand = gHand.indexOf(gh.Card);
 //			        	gHand.remove(idxOfHand);
 //			        	//print something
 //			        	play("Hand:-" + idxOfHand + "-card-should-be-sombrero-" + "-you-can-do-better-than-this!");
 //			        	break;
 //			         }
 //			         else if(gh.Card.equals("tengallonhat") && gh.isActive()){
 //			        	idxOfHand = gHand.indexOf(gh.Card);
 //				        gHand.remove(idxOfHand);
 //				        //print something
 //				        play("hand:-" + idxOfHand + "-card-should-be-sombrero-" + "-Oh-my-ten-gallon-hat-is-so-heavy");
 //				        break;
 //			         }
 //			         else if(gh.Card.equals("ironplate") && gh.isActive()){
 //			        	idxOfHand = gHand.indexOf(gh.Card);
 //				        gHand.remove(idxOfHand);
 //				        //print something
 //				        play("hand:-" + idxOfHand + "-card-should-be-ironplate-" + "-nothing-can-come-through-with-my-iron-plate");
 //				        break;
 //			         }
 //			         else if(gh.Card.equals("bible") && gh.isActive()){
 //				        idxOfHand = gHand.indexOf(gh.Card);
 //					    gHand.remove(idxOfHand);
 //					        //print something
 //					    play("hand:-" + idxOfHand + "-card-should-be-bible-" + "-God-is-awesome!");
 //					    break;
 //				     }
 //			        
 //			      }				
 //			}
 //			else 
 				if(hand.contains("missed") ){
 				idxOfHand = hand.indexOf("missed");
 				hand.remove(idxOfHand);
 				//print something
 				System.out.println("hand:-" + idxOfHand + "-card-should-be-miss " + "-You-Missed.");
 				play (String.valueOf("hand:-" + idxOfHand + "-card-should-be-miss-" + "-You-Missed."));
 			}
 			else if(hand.contains("dodge")){
 				idxOfHand = hand.indexOf("dodge");
 				hand.remove(idxOfHand);
 				//print something
 				play("hand: " + idxOfHand + " card should be dodge " + " I dodged the bullet.");
 				
 			}
 			else{
 				//
 				if (health >= 2){
 					health = health - 1;
 					play("Ouch!");
 				}
 				else
 				{
 					if(hand.contains("beer")){
 						idxOfHand = hand.indexOf("beer");
 						hand.remove(idxOfHand);
 						//print last health but played beer
 						play("hand: " + idxOfHand + " card should be beer " + " I am dead, but I have beer");
 						
 					}
 					else{
 						//print I am dead, no miss or health
 						health = health - 1;
 						isGameOver = true;
 						play(" I have no health, it's game over for me ");
 					}			
 				}
 			}
 		}
 	}
 
 	private static void takeCardFromHand(String cardType,String card) {
 		
 		int tmp;
 		if (hand.size() >=1) {
 			tmp = randomCard();
 			hand.remove(tmp);
 			play("Remove from hand: " + tmp +  printArray[8]);
 		}
 		else if (bHand.size() >= 1) {
 			tmp = 0 + (int)(Math.random() * ((bHand.size() - 0) + 1));
 			bHand.remove(tmp);
 			play("Remove from Blue hand: " + tmp +  printArray[8]);
 		}
 		else if (gHand.size() >= 1) {
 			tmp = 0 + (int)(Math.random() * ((gHand.size() - 0) + 1));
 			gHand.remove(tmp);
 			play("Remove from Green hand: " + tmp +  printArray[8]);
 		}
 		else
 		{
 			//print something wrong
 			play("are you sure? if you are here, there is something wrong, you should start a new game. ");
 		}
 		
 		
 //		
 //		int idxOfHand = 0;
 //		if(cardType.equals("blue")){
 //			if(bHand.contains(card)){
 //				//do somehting
 //				idxOfHand = bHand.indexOf(card);
 //				bHand.remove(card);
 //				
 //				play("Hand: " + idxOfHand + "card should be: " + card +  printArray[8]);
 //				
 //			}
 //			else{
 //				//print something wrong
 //				play("are you sure? if you are here there is something wrong, you should start a new game. ");
 //			}
 //		}
 //		else if(cardType.equals("gren")){
 //			if(gHand.contains(card)){
 //				//do something
 //				idxOfHand = gHand.indexOf(card);
 //				gHand.remove(card);
 //				play("Hand: " + idxOfHand + "card should be: " + card +  printArray[8]);
 //			}
 //			else{
 //				
 //				//print something wrong
 //				play("are you sure? if you are here, there is something wrong, you should start a new game. ");
 //			}
 //		}
 //		else {
 //			if(hand.contains(card)){
 //				//do something
 //				idxOfHand = hand.indexOf(card);
 //				hand.remove(card);
 //				play("Hand: " + idxOfHand + "card should be: " + card +  printArray[8]);
 //			}
 //			else
 //			{
 //				//print something wrong
 //				play("are you sure? if you are here, there is something wrong, you should start a new game. ");
 //			}
 //		}
 		
 	}
 
 	
 
 	public static void addToHand(int handType, String card) {
 		// resulted from b3 - used to add initial hand and general store cards
 		switch (handType) {
 		case 1: hand.add(card);
 				inHand++;
 				break;
 		case 2:	GreenHand tmp = new GreenHand(card);
 				gHand.add(tmp);
 				inHand++;
 				break;
 		case 3: bHand.add(card);
 		        inHand++;
 		        break;
 		default: break;
 		}
 	}
 	
 	public static void b3Button(String card) {
 		// see excel sheet B3 table
 		String print = new String();
 		String currentCard = new String();
 		int playerIndex;
 		int sheriffPos = findSheriff();
 		bangs = 0;
 
 		
 		playBlueHand();
 		
 		for (int i = 0; i < gHand.size(); i++) {
 			GreenHand currentGreen = gHand.get(i);
 			
 			if (currentGreen.isInPlay() && !currentGreen.isActive()) {
 				currentGreen.activate();
 				gHand.set(i,currentGreen);
 			}
 		}
 		
 		playGreenHand();
 
 		for (int i = 0; i < gHand.size(); i++) {
 			GreenHand currentGreen = gHand.get(i);	
 //			String cGCard = currentGreen.getCard();
 			if (!currentGreen.isInPlay()) {
 				currentGreen.play();
 				gHand.set(i,currentGreen);
 				play("Green card: " + i +  "--" + printArray[10]);
 			}
 		}
 		
 		playHand();
 		
 	}
 	
 	public static void playBlueHand() {
 		String print = new String();
 		String currentCard = new String();
 		int playerIndex;
 		int sheriffPos = findSheriff();
 		
 		for (int i = 0; i < bHand.size(); i++) {
 		
 			currentCard = bHand.get(i);
 			
 			if (currentCard.equals("jail")) {
 				do
 					playerIndex = choosePlayerIndex(roles.size());
 				while (playerIndex == sheriffPos);
 				bHand.remove(i);
 				play("Blue card: " + i + " against player: " + playerIndex + "--" + printArray[57]);
 				playBlueHand();
 			}
 			if (currentCard.equals("dynamite")) {
 				//playerIndex = choosePlayerIndex(roles.size());
 				bHand.remove(i);
 				play("Blue card: " + i + "--" + printArray[55]);
 				playBlueHand();
 			}
 			if (currentCard.equals("binocular")) {
 				rangeOther++;
 				bHand.remove(i);
 				play("Blue card: " + i + "--" + printArray[54]);
 				playBlueHand();
 			}
 			if (currentCard.equals("scope")) {
 				rangeOther++;
 				bHand.remove(i);
 				play("Blue card: " + i + "--" + printArray[53]);
 				playBlueHand();
 			}
 			if (currentCard.equals("barrel")) {
 				bHand.remove(i);
 				play("Blue card: " + i + "--" + printArray[52]);
 				playBlueHand();
 			}
 			if (currentCard.equals("hideout")) {
 				bHand.remove(i);
 				play("Blue card: " + i + "--" + printArray[51]);
 				playBlueHand();
 			}
 			if (currentCard.equals("mustang")) {
 				bHand.remove(i);
 				play("Blue card: " + i + "--" + printArray[50]);
 				playBlueHand();
 			}
 			if (currentCard.equals("schofield") && rangeGun < 2) {
 				rangeGun = 2;
 				bHand.remove(i);
 				play("Blue card: " + i + "--" + printArray[48]);
 				playBlueHand();
 			}
 			if (currentCard.equals("remington") && rangeGun < 3){
 				rangeGun = 3;
 				bHand.remove(i);
 				play("Blue card: " + i + "--" + printArray[49]);
 			}
 			if (currentCard.equals("revcarabine") && rangeGun < 4){
 				rangeGun = 4;
 				bHand.remove(i);
 				play("Blue card: " + i + "--" + printArray[48]);
 				playBlueHand();
 			}
 			if (currentCard.equals("winchester") && rangeGun < 5){
 				rangeGun = 5;
 				bHand.remove(i);
 				play("Blue card: " + i + "--" + printArray[49]);
 				playBlueHand();
 			}
 			if (currentCard.equals("volcanic")) {
 				if (!(myRole.contains("outlaw") && sheriffPos > 1)) {
 					rangeGun = 1;
 					isVolcanic = true;
 					bHand.remove(i);
 					play("Blue card: " + i + "--" + printArray[66]);
 					playBlueHand();
 				}
 			}
 		}
 	}
 	
 	public static void playGreenHand() {
 		for (int i = 0; i < gHand.size(); i++) {	
 
 			GreenHand currentGreen = gHand.get(i);	
 			String cGCard = currentGreen.getCard();
 			
 			if (currentGreen.isActive()) {	
 				if (cGCard.equals("canteen") && (health < 4 || (myRole.equals("sheriff") && health < 5))) {
 					gHand.remove(i);
 					health++;
 					play("Green Hand: " + i + "--" + printArray[19]);
 					playGreenHand();
 				}
 				if (cGCard.equals("conestoga")) {
 					gHand.remove(i);
 					play(String.valueOf("Green Hand: " + i + " On player: " + choosePlayerIndex(myRange)) + "--" + printArray[42]);
 					playGreenHand();
 				}
 				if (cGCard.equals("cancan")) {
 					gHand.remove(i);
 					play(String.valueOf("Green Hand: " + i + " On player: " + choosePlayerIndex(myRange)) + "--" + printArray[67]);
 					playGreenHand();
 				}
 				if (cGCard.equals("pepperbox")) {
 					gHand.remove(i);
 					play(String.valueOf("Green Hand: " + i + " On player: " + choosePlayerIndex(myRange)) + "--" + printArray[27]);
 					playGreenHand();
 				}
 				if (cGCard.equals("howitzer")) {
 					gHand.remove(i);
 					play(String.valueOf("Green Hand: " + i + " On player: " + choosePlayerIndex(myRange)) + "--" + printArray[25]);
 					playGreenHand();
 				}
 				if (cGCard.equals("buffalorifle")) {
 					gHand.remove(i);
 					play(String.valueOf("Green Hand: " + i + " On player: " + choosePlayerIndex(myRange)) + "--" + printArray[32]);
 					playGreenHand();
 				}
 				if (cGCard.equals("knife")) {
 					gHand.remove(i);
 					play(String.valueOf("Green Hand: " + i + " On player: " + choosePlayerIndex(myRange)) + "--" + printArray[30]);
 					playGreenHand();
 				}
 				if (cGCard.equals("derringer")) {
 					gHand.remove(i);
 					play(String.valueOf("Green Hand: " + i + " On player: " + choosePlayerIndex(myRange)) + "--" + printArray[26]);
 					playGreenHand();
 				}
 				if (cGCard.equals("ponyexpress")) {
 					gHand.remove(i);
 					play("Green Hand: " + i + "--" + printArray[14]);
 					playGreenHand();
 				}
 			}
 			
 			
 		}
 	}
 	
 	public static void playHand() {
 		for (int i = 0; i < hand.size(); i++) {
 
 			String cBCard = hand.get(i);
 			
 			if (health < 4 || (myRole.equals("sheriff") && health < 5)) {
 				if (cBCard.equals("beer")) {
 					hand.remove(i);
 					health++;
 					play("Play card: " + i + "--" + printArray[17]);
 					playHand();
 				}
 				if (cBCard.equals("saloon")) {
 					hand.remove(i);
 					health++;
 					play("Play card: " + i + "--" + printArray[18]);
 					playHand();
 				}
 				if (cBCard.equals("whiskey")) {
 					if (hand.size() > 1) {					
 						hand.remove(i);
 						int second = randomCard();
 						hand.remove(second);	
 						health++;
 						if (health < 4 || (myRole.equals("sheriff") && health < 5))
 							health++;
 						play(String.valueOf("Play card: " + i + " and " + second + " (after shifting hand)--" + printArray[21]));
 						playHand();
 					}
 				}
 				if (cBCard.equals("tequila")) {
 					if (hand.size() > 1) {					
 						hand.remove(i);
 						health++;
 						int second = randomCard();
 						hand.remove(second);					
 						play(String.valueOf("Play card: " + i + " and " + second + " (after shifting hand)--" + printArray[20]));
 						playHand();
 					}
 				}
 			}
 			if (cBCard.equals("panic")) {
 				hand.remove(i);
 				play(String.valueOf("Play card: " + i + " On player: " + choosePlayerIndex(rangeOther)) +  "--" + printArray[43]);
 				playHand();
 			}
 			if (cBCard.equals("ragtime") || cBCard.equals("brawl")) {
 				if (hand.size() > 1) {					
 					hand.remove(i);
 					int second = randomCard();
 					hand.remove(second);					
 					play(String.valueOf("Play card: " + i + " and " + second + " (after shifting hand) On player: " + choosePlayerIndex(rangeOther)) + "--" + printArray[44]);
 					playHand();
 				}
 			}
 			if (cBCard.equals("cat")) {
 				hand.remove(i);
 				play(String.valueOf("Play card: " + i + " On player: " + choosePlayerIndex(roles.size())) + "--" + printArray[47]);
 				playHand();
 			}
 			if (cBCard.equals("bang")) {
 				if (isVolcanic || bangs == 0) {
 					bangs++;
 					hand.remove(i);
 					play(String.valueOf("Play card: " + i + " On player: " + choosePlayerIndex(myRange)) + "--" + printArray[28]);
 					playHand();
 				}
 			}
 			if (cBCard.equals("gatling")) {
 				hand.remove(i);
 				play("Play card: " + i + "--" + printArray[29]);
 				playHand();
 			}
 			if (cBCard.equals("punch")) {
 				hand.remove(i);
 				play(String.valueOf("Play card: " + i + " On player: " + choosePlayerIndex(rangeOther)) + "--" + printArray[30]);
 				playHand();
 			}
 			if (cBCard.equals("springfield")) {
 				if (hand.size() > 1) {					
 					hand.remove(i);
 					int second = randomCard();
 					hand.remove(second);					
 					play(String.valueOf("Play card: " + i + " and " + second + " (after shifting hand) On player: " + choosePlayerIndex(roles.size())) + "--" + printArray[8]);
 					playHand();
 				}
 			}
 			if (cBCard.equals("indians")) {
 				hand.remove(i);
 				play("Play card: " +  + i + "--" + printArray[23]);
 				playHand();
 			}
 			if (cBCard.equals("wellsfargo")) {
 				hand.remove(i);
 				play("Play card: " +  + i + "--" + printArray[15]);
 				playHand();
 			}
 			if (cBCard.equals("stagecoach")) {
 				hand.remove(i);
 				play("Play card: " +  + i + "--" + printArray[16]);
 				playHand();
 			}
 			if (cBCard.equals("generalstore")) {
 				hand.remove(i);
 				play("Play card: " + i + "--" + printArray[14]);
 				playHand();
 			}
 			if (cBCard.equals("duel")) {
 				hand.remove(i);
 				play(String.valueOf("Play card: " + i + " On player: " + choosePlayerIndex(roles.size())) + "--" + printArray[24]);
 				//someoneShootAtMe("duel");
 				playHand();
 			}
 		}
 	}
 	
 	public static int randomCard(int index) {
 		int min = 0;
 		int max = hand.size()-1;
 		int rand = min + (int)(Math.random() * ((max - min) + 1));
 		
 //		int rand = rnd.nextInt(hand.size());
 		if (rand == index)
 			rand = randomCard(index);
 		return rand;
 	}
 	
 	public static int randomCard() {
 		int min = 0;
 		int max = hand.size()-1;
 		int rand = min + (int)(Math.random() * ((max - min) + 1));
 
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
 		try {
 			String tmp = str.replace(" ", "-");
 			Runtime.getRuntime().exec("python scream.py '" + tmp + "'");
 			System.out.println("python scream.py \"" + tmp + "\"");
 			Thread.sleep(10000);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public static int choosePlayerIndex(int range) {
 		/*
 		 * Sheriff: shoot all
 		 * Outlaw: shoot sheriff if in range, else random
 		 * Deputy: shoot all except sheriff
 		 * Renegade: shoot all except sheriff, sheriff last
 		 * 
 		 */
 //		if (roles.size() < range) {
 //			range = roles.size() - 1;
 //		}
 		
 		int sheriffPos = findSheriff();
 		int direction = 0; // 0 = left, 1 = right
 		
 		if (!(sheriffPos == 0))
 			if (((double)sheriffPos/(double)roles.size()) < 0.5){
 				//System.out.println("go right");
 				direction = 1;
 			}
 		// size - index is the left index
 		
 		//int index = rnd.nextInt(Math.abs(range));
 		int index = 0 + (int)(Math.random() * (Math.abs(range) + 1));
 		//System.out.println("index: " + index);
 		if (index == 0) 
 			index++;
 		if (direction == 1) {
 			index = roles.size() - index;
 		}
 		
 		
 		if (myRole.equals("sheriff")) {
 			//System.out.println(index);
 			return index%roles.size();
 		}
 		else 
 		if (myRole.equals("renegade")) {
 			if (roles.get(index).equals("sheriff") && roles.size() > 2)
 				index = choosePlayerIndex(range);
 			return index%roles.size();
 		}
 		else if (myRole.equals("deputy1") || myRole.equals("deputy2")) {
 			if (roles.get(index).equals("sheriff"))
 				index = choosePlayerIndex(range);
 			return index%roles.size();
 		}
 		else if (myRole.equals("outlaw1") || myRole.equals("outlaw2") || myRole.equals("outlaw3")) {
 			if (sheriffPos <= myRange)
 				index = sheriffPos;
 			return index%roles.size();
 		}
 		if (direction == 1) {
 			//System.out.println(index);
 			return Math.abs(roles.size() - index);
 		}
 		else {
 			//System.out.println(index);
 			return index%roles.size();
 		}
 	}
 	
 	public static int findSheriff () {
 		for (int i = 0; i < roles.size(); i++)
 			if (roles.get(i).equals("sheriff"))
 				return i;
 		return 0;
 	}
 
 }
