 package uk.badger.bConomy.account;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 
 import org.bukkit.entity.Player;
 
 @SuppressWarnings("serial")
 public class PlayerAccounts extends ArrayList<Account> {
 	
 	/**
 	 * Removes the account for a given player.
 	 *
 	 * @param player the player who's account to remove
 	 * @return true, if successfully removed
 	 */
 	public boolean remove(Player player) {
 
 		Account account = get(player);
 		if (account != null) {
 			remove(account);
 			return true;
 		}
 
 		return false;
 	}
 	
 	/**
 	 * Removes the account for a given player name.
 	 *
 	 * @param player the players name who's account should be removed
 	 * @return true, if successfully removed
 	 */
 	public boolean remove(String player) {
 
 		Account account = get(player);
 		if (account != null) {
 			remove(account);
 			return true;
 		}
 
 		return false;
 	}
 	
 	/**
 	 * Gets an account from a given player.
 	 *
 	 * @param player the player
 	 * @return the account of the given player
 	 */
 	public Account get(Player player) {
 
 		Iterator<Account> itr = iterator();
 		while (itr.hasNext()) {
 			Account currentAccount = itr.next();
 			if (currentAccount.getPlayer().getName().equalsIgnoreCase(player.getName())) {
 				return currentAccount;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Gets an account from a player name.
 	 *
 	 * @param player the player name
 	 * @return the account of the given player name
 	 */
 	public Account get(String player) {
 
 		Iterator<Account> itr = iterator();
 		while (itr.hasNext()) {
 			Account currentAccount = itr.next();
 			if (currentAccount.getPlayer().getName().equalsIgnoreCase(player)) {
 				return currentAccount;
 			}
 		}
 
 		return null;
 	}
 	
 	public ArrayList<Account> getTop(int amount) {
 		
 		ArrayList<Account> sortedAccounts = this;
 		ArrayList<Account> topAccounts = new ArrayList<Account>();
 		
 		Collections.sort(sortedAccounts, new Comparator<Account>() {
             public int compare(Account account, Account otherAccount) {
                return (int)(account.getBalance() - otherAccount.getBalance());
             }
         });
 		
 		int start = sortedAccounts.size() - amount;
 		if (start < 0)
 			start = 0;
 		
 		for (int i = start; i < sortedAccounts.size(); ++i) {
 			topAccounts.add(sortedAccounts.get(i));
 		}
 		
 		return topAccounts;
 		
 	}
 	
 }
