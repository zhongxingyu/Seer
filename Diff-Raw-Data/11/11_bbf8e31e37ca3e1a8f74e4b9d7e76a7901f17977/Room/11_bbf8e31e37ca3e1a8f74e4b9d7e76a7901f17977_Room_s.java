 
 public class Room {
 	private int foodInRoom;
 	int batHitPoints;
 	private boolean doorUnlockingStatus = false;
 	
 	// batHitPointsNumber is for future upgrades regarding adjusting difficulty
 	public Room(int food, int batHitPointsNumber){
 		
 	}
 	//everytime animal eats this method should follow
 	public int animalEats(){
 		foodInRoom --;
 		return foodInRoom;
 	}
 	//method to be used only if the player has the key
 	public void unlockDoor(){
 		doorUnlockingStatus = true;
 	}
 	
 	public boolean openDoor(){
		if (doorUnlockingStatus=true){
 			System.out.println("Congratulations! You have opened the door and");
 			return true;
 		}
 		else
 		{
 			System.out.println("Sorry the door is locked");
 			return false;
 		}
 	}
 	
 	public void roomDescription(){
 		System.out.println("You are in... guess what!! ... a rooom");
 		System.out.println("Not so much to look at - just a door, an old TV, a couch, wardrobe and the food bowl.");
 		System.out.println("...Oh and did I forget to mention the cage with an evil bat? You did not think it would be too easy now did you?");
 	}
 	
 	public void cageDescription(){
 		System.out.println("You look at the magnificent, huge, golden cage, and... ahhh whom am I kidding.. what you're interested in is the front door key which lies at the bottom of the cage.");
 		System.out.println("Unfortunately in order to get it you will have to fight the EVIIIIILLLLL bat. Up to the task?");
 	}
 	
 	public boolean cabinetDescription(){
 	System.out.println("Among the worthless rags, you can see an amazingly looking scarf, which can protect you from the Evil Bat's deadly bite");
 	System.out.println("Soo you take it or not? Actually it was a rethorical quesiton - of course you take it - what kind of player would you be if you did not collect all the junk you see???");
 	boolean scarfTaken = true;
 	return scarfTaken;
 	}
 		
 
 }
