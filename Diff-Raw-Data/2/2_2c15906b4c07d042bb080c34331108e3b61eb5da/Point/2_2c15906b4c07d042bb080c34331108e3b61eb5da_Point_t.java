 package backgammon.model.board;
 
 import backgammon.model.interfaces.ICheckerList;
 import backgammon.model.player.Player;
 
 public class Point implements ICheckerList {
 	
 	private Player owner;
 	private int size = 0;
 	
 	public int addChecker(Player player) {
 		
 		this.size++;
 		if (this.owner == null) {
 			this.owner = player;
 		}
 		
 		return this.size;
 	}
 
 	public int removeChecker(Player player) {
 		
 		if (this.size > 0) {
 			this.size--;
 			if (this.size == 0) {
 				this.owner = null;
 			}
 		}
 		
 		return this.size;
 	}
 
 	public int getTopCheckerIndex() {
 		return (this.size - 1);
 	}
 	
 	public int getTopCheckerIndexForPlayer(Player player) {
 		
 		if (player.equals(this.owner)) {
 			return this.size - 1;
 		}
 		
 		return -1;
 	}
 	
 	public int getCheckerCount() {
 		return (this.size);
 	}
 	
 	public int getCheckerCountForPlayer(Player player) {
 		return ((player.equals(this.owner)) ? (this.size) : (0));
 	}
 	
 	public boolean hasCheckersOfPlayer(Player player) {
 		return (player.equals(this.owner) && this.size > 0);
 	}
 
 	public boolean isEmpty() {
 		return (this.size == 0);
 	}
 
 	public boolean isBlot() {
 		return (this.size == 1);
 	}
 	
 	public boolean isBlotOfPlayer(Player player) {
 		return (this.isBlot() && player.equals(this.owner));
 	}
 	
 	public boolean isBlockedForPlayer(Player player) {
		return (this.size > 1 && player.equals(this.owner) == false);
 	}
 }
