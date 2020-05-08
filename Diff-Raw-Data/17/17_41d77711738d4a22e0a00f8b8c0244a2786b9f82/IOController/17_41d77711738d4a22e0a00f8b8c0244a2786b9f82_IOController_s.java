 import java.util.*;
 public class IOController {
 
 	/**
 	 * @param args
 	 */
 	
 	private static boolean isExitGame = false;
 	//private static int command = 0;
 	private static String command = new String();
 	private static Scanner input = new Scanner(System.in);
 	private static boolean hasWon = false;
 	private static boolean hasLost = false;
	private static LevelController level = new LevelController(); //why initialize thrice?
 	private static int levelNo = 1;
 	private static Animals animals = new Animals();
 	private static Weapons weapons = new Weapons();
 	private static GameTimer timer = new GameTimer();
 	private static ArrayList<String> animalsKilled = new ArrayList<String>();
 	private static ArrayList<String> weaponsUsed = new ArrayList<String>();
 	public static HashMap<Integer,String> resultMap = new HashMap<Integer,String>();
 	
 	
 	public IOController(){
 		resultMap.put(Constants.CONTINUE, "Continue");
 		resultMap.put(Constants.WON, "You Win!");
 		resultMap.put(Constants.LOST, "You Lose!");
 		resultMap.put(Constants.WON_WITH_TIME, "You have won a medal, you are the �King of the Jungle�");
 		resultMap.put(Constants.WEAPON_NOT_FOUND, "The weapon you selected cannot be used to kill this animal! Select another weapon:");
 		resultMap.put(Constants.WEAPON_USED, "The weapon has been used! Choose another weapon.");
 		resultMap.put(Constants.ANIMAL_KILLED, "The animal has been killed!");
 	}
 	
 	public static void main(String[] args) {
         // TODO code application logic here
         IOController disp = new IOController();
         
         while(!isExitGame)
         {
         	boolean isNotReentry = true;
         	animalsKilled.clear();
         	weaponsUsed.clear();
         	hasWon = false;
         	hasLost = false;
             do
             {
             	//levelNo = 1; //not needed
             	try
                 {
 	                if(levelNo != 1 && levelNo != 2 && levelNo!=3)
 	                {
 	                    System.out.print("Enter Game Level 1, 2 or 3\n");
 	                }
 	                else
 	                {
 	                    System.out.print("Enter Game Level\n");                
 	                                    
 	                }
 	                levelNo = Integer.parseInt(input.next());
                 }
                 catch(Exception e)
                 {
                     levelNo = 0;
                 }
             }while(levelNo != 1 && levelNo != 2 && levelNo!=3);
 
            //dont need the next line
            level = new LevelController();
             level = LevelFactory.getController(levelNo);
             
             //testing
             //disp.displayList(animals.getNameToIndexList());
             //disp.displayList(weapons.getNameToIndexList());
             boolean startedHunting = false;
             
             command = input.nextLine();
             hasLost = false;
             hasWon = false;
             while(!hasLost && !hasWon){
 //            	System.out.println("Enter 1 to list animals");
 //            	System.out.println("Enter 2 to list weapons");
 //            	System.out.println("Enter 3 to hunt");
 //            	System.out.println("Enter 4 to exit");
             	//command = input.nextInt();
             	
             	System.out.print("\nEnter Command (list animals, list weapons, hunt, or exit):");
             	System.out.println("\t");
             	
             	//Hack to make the phrases work
             	//Used to capture trailing newline character
             	if(!isNotReentry){
             		command = input.nextLine();
             	}
             	command = input.nextLine();
             	
                 if(command.equalsIgnoreCase("list animals"))
             	//if(command == 1)
                 {
                     disp.displayCurrList(new ArrayList<String> (animals.getNameToIndexList().keySet()),animalsKilled);
                 }
                 else if(command.equalsIgnoreCase("list weapons"))
             	//else if(command == 2)
                 {
                     disp.displayCurrList(new ArrayList<String>( weapons.getNameToIndexList().keySet()),weaponsUsed);
                 }
                 else if(command.equalsIgnoreCase("hunt"))
             	//else if(command == 3)
                 {
                     if (!startedHunting)
                     {
                         startedHunting = true;
                         timer.start(); // started every time but not always displayed
                     }
                     disp.hunt();
                     isNotReentry = false;
                 }
             	//else if(command == 4)
                 else if(command.equalsIgnoreCase("exit"))
             	{ 
                 	System.out.println("The hunter left the forest!");
             		System.exit(1);
             	}
                 else
                 {
                 	System.out.println("Invalid.");
                 	isNotReentry = true;
                 }
             } 
             System.out.println("Replay the game?[Y/N]");
             String exitComm = new String();
             
             do{
             	exitComm = disp.inputString();
 	            if(exitComm.equalsIgnoreCase("n")){
 	            	isExitGame = true;
 	            	System.out.println("The hunter left the forest!");
 	            }
 	            else if(exitComm.equalsIgnoreCase("y")){
 	            	startedHunting = false;
 	            }
 	            else{
 	            	System.out.println("Enter Y/N");
 	            	//exitComm = disp.inputString();
 	            }
             }while(!exitComm.equalsIgnoreCase("y")&& !exitComm.equalsIgnoreCase("n"));
         }
     }
 
     //display values which only exist in first argument
     public void displayCurrList(ArrayList<String> list,ArrayList<String> exList){
     	System.out.println();
         for(String s: list)
         {
         	if(!exList.contains(s)){
         		System.out.println(s);
         	}
         }
         System.out.println();
     }
 
     //display all non-empty values in the map
     public void displayList(ArrayList<String> map){
         System.out.println();
         for(String s: map)
         {
         	if(!s.isEmpty()){
         		System.out.println(s);
         	}
         }
         System.out.println();
     }
     
     
     public void hunt(){
     	String animal = new String();
     	String weapon = new String();
     	boolean isDone = false;
     	int turnCount = 0;
     	int resultKey;
     	boolean isAnimalSelected = false;
     	long timeInSecs = 0;
     	//while(true){
     	displayHuntGrid();
     	//test
     	System.out.print("Please select an animal:");
     	animal = inputString();
     	
     	//check for valid animal entry
     	do{
     		if(!level.huntMap.keySet().contains(animal)){
     			if (animals.getAnimalList().containsValue(animal)){
     				System.out.print("The animal has been killed! Select another animal: ");
     			}
     			else if (!animals.getAnimalList().containsValue(animal)){
     				System.out.print("This animal is not in the forest. Select another animal: ");
     			}
     			animal = inputString();
     		}
     		else
     		{
     			isAnimalSelected = true;
     			System.out.println("Weapon checklist for "+animal+":");
     			displayList(level.huntMap.get(animal));
     		}
     	}while(!isAnimalSelected); 
     	
     	turnCount = 0;
     	System.out.print("Please select a weapon:");
     	weapon = inputString();
     	
     	do{
     		timeInSecs = timer.getElapsedTimeSecs();
     		++turnCount;
     		//let the level controller determine the outcome
     		resultKey = level.hunt(animal, weapon, timeInSecs, turnCount);
     		if(resultKey == Constants.WEAPON_NOT_FOUND || resultKey == Constants.WEAPON_USED){
     			isDone = false;
     			System.out.println(resultMap.get(resultKey));
     			weapon = inputString();
     		}
     		else{
     			isDone = true;
     		}
     	}while(!isDone);
 
     	if(resultKey == Constants.CONTINUE){
     		animalsKilled.add(animal);
     		weaponsUsed.add(weapon);
     		System.out.println("Animal killed: "+animal);
     		System.out.println("Weapon used: "+ weapon);
     		System.out.println("Number of animals left: " + level.animalsLeft);
     		//displayList(level.huntMap.get(animal));
     	}
     	else
     	{
     		if(resultKey == Constants.LOST){
     			hasLost = true;
     			System.out.println(resultMap.get(Constants.LOST));
     		}
     		else if (resultKey == Constants.WON || resultKey == Constants.WON_WITH_TIME){
     			hasWon = true;
    			timer.stop();
    			if(level.isTimeNeeded){
    				System.out.println("Time taken to complete(in seconds): " + timeInSecs);
    			}
     			System.out.println(resultMap.get(Constants.WON));
     			if (resultKey == Constants.WON_WITH_TIME){
     				System.out.println(resultMap.get(Constants.WON_WITH_TIME));
     			}
     		}
     		return;
     	}
     	//}
     }
     
   
     public void displayHuntGrid(){
     	HashMap<String,Integer> animalToIndex = new HashMap<String, Integer>();
     	animalToIndex = animals.getNameToIndexList();
         for(String s: level.huntMap.keySet()){
         	//always check whether the animal exists in animalWeaponMap
         	if(level.animalWeaponMap.keySet().contains(animalToIndex.get(s))){  
         		if(s.length()<=7)
         		{
         			System.out.print("\t"+s+"\t");
         		}
         		else
         		{
         			System.out.print("\t"+s);
         		}
         	}
         	else
         	{
         		System.out.print("\t\t");
         	}
         }
         System.out.println("\n\t-------------------------------------------------------------------------");
         //weapon display for current animal
         for(int i = 0; i<weapons.getWeaponCount();++i){
         	for(String s: level.huntMap.keySet()){
         		//always check whether the animal exists in animalWeaponMap
             	if(level.animalWeaponMap.keySet().contains(animalToIndex.get(s))){
             		if(level.huntMap.get(s).get(i).length()<=7)
             		{
             			System.out.print("\t"+level.huntMap.get(s).get(i)+"\t");
             		}
             		else
             		{
             			System.out.print("\t"+level.huntMap.get(s).get(i));
             		}
             	}
             	else
             	{
             		System.out.print("\t\t");
             	}
             } 
         	System.out.println("\n\n");
         }
     }
     
     private String inputString(){
     	try{
     		return input.next().toLowerCase();
     	}
     	catch(Exception e){
     		return "x";
     	}
     }
 }
