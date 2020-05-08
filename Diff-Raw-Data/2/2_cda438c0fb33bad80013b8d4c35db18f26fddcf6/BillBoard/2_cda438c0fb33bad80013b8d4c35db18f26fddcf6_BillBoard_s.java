 package me.sd5.billboard;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 /**
  * 
  * @author sd5
  * 
  * This class represents
  * the billboard where the
  * advertisement will be
  * pinned on. Every player
  * can see the billboard
  * with "/bb show"
  *
  */
 public class BillBoard {
 
 	private static List<Advertising> board = new ArrayList<Advertising>();
 	
 	/**
 	 * Loads the billboard from the database.
 	 */
 	public static void load() {
 		
 		board = MySQLManager.getBillboard();
 		
 	}
 	
 	/**
 	 * Adds an advertising to the billboard.
 	 * @param advertising:
 	 *   The advertising to add.
 	 * @return:
 	 *   Whether the advertising was added to the
 	 *   billboard or whether the player has too
 	 *   many advertising on the billboard.
 	 */
 	public static boolean add(Advertising advertising) {
 		
 		if(BillBoard.get(advertising.getPlayer()).size() >= Config.maxPlayerAdvertising) {
 			return false;
 		}
 		
 		board.add(0, advertising);
 		
 		if(board.size() > Config.maxBillboardLength) {
 			board.remove(board.size() - 1);
 		}
 		
 		MySQLManager.saveBillboard(board);
 		return true;
 		
 	}
 	
 	/**
 	 * Returns the advertising which is
 	 * at the given index on the board.
 	 * @param index:
 	 *   The index.
 	 * @return:
 	 *   The Advertising.
 	 */
 	public static Advertising get(int index) {
 		
 		return board.get(index);
 		
 	}
 	
 	/**
 	 * Returns the size of the billboard.
 	 * @return:
 	 *   The size.
 	 */
 	public static int size() {
 		
 		return board.size();
 		
 	}
 	
 	/**
 	 * Clears the billboard.
 	 * @return:
 	 *   Whether the billboard was already empty or not.
 	 */
 	public static boolean clear() {
 		
 		if(!board.isEmpty()) {
 			board.clear();
 			MySQLManager.saveBillboard(board);
 			return true;
 		} else {
 			return false;
 		}
 		
 	}
 	
 	/**
 	 * Returns a list of Advertising created by this player.
 	 * @param player:
 	 *   The player.
 	 * @return:
 	 *   A list of Advertising.
 	 */
 	public static List<Advertising> get(String player) {
 		
 		List<Advertising> list = new ArrayList<Advertising>();
 		
 		for(Advertising a : board) {
			if(a.getPlayer() == player) {
 				list.add(a);
 			}
 		}
 		
 		return list;
 		
 	}
 	
 	/**
 	 * Returns a free advertisement number.
 	 * @return:
 	 *   A free advertisement number.
 	 */
 	public static int getFreeId() {
 		
 		int number = (new Random()).nextInt((int) Math.pow(Config.maxBillboardLength, 2));
 		
 		for(Advertising a : board) {
 			if(number == a.getId()) {
 				number = getFreeId();
 				break;
 			}
 		}
 		
 		return number;
 		
 	}
 	
 }
