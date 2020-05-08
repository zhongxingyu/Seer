 package openzork;
 
 class Player {
 
 	public Player() {
 
 	}
 
 	public void addGold(int goldToAdd) {
 		gold += goldToAdd;
 	}
 
 	public int getGold() {
 		return gold;
 	}
 
 	public Location getCurrent() {
 		return currentLoc;
 	}
 
 	public void switchLoc(Location newLoc) {
 		currentLoc = newLoc;
		System.out.print(newLoc.getNameAndDesc());
 	}
 
 	private int gold = 0;
 	public Location currentLoc;
 }
