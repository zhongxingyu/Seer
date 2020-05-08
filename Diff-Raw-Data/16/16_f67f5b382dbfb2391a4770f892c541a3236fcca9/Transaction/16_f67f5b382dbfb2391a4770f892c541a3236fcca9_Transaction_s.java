 import java.util.Hashtable;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 /**
  * Represents a transaction of goods in the market.
  * 
  * @author TeamTroll
  * @version 1.0
  */
 @SuppressWarnings("serial")
 public class Transaction extends JPanel {
<<<<<<< HEAD
	private final JLabel transLabel;
	private Player player;
=======
 	private GameScreen screen;
 	private JLabel transLabel;
>>>>>>> branch 'master' of https://github.com/shawnchaudhry/TeamTroll.git
 	private Market market;
 	private int balance, change;
 	private Hashtable<String, Integer> trans;
 
 	/**
 	 * Creates the transaction panel.
 	 * 
 	 * @param GameScreen
 	 *            screen The screen of the game.
 	 */
 	public Transaction(final GameScreen screen, Market market) {
 		Player player = screen.game.getPlayer();
 		this.screen = screen;
 		this.market = market;
 		balance = player.getMoney();
 		trans = new Hashtable<String, Integer>();
 
 		for (String good : player.getInventory().keySet())
 			trans.put(good, 0);
 
 		transLabel = new JLabel("Transaction: $" + balance + " + (" + change
 				+ ") = $" + getNewBalance() + "      Cargo Space Remaining: "
 				+ (player.getShip().getCargoBay() - player.numItems()));
 
 		add(transLabel);
 	}
 
 	/**
 	 * Increments the number of items to be bought or sold in the market.
 	 * 
 	 * @param String
 	 *            good The name of the good.
 	 * @param int price The price of the good.
 	 */
 	public int add(String good, int price) {
 		Player player = screen.game.getPlayer();
 
 		if (market.buy() && 0 < balance + change - price
 				&& numItems() <= player.getShip().getCargoBay()) {
 			trans.put(good, trans.get(good) + 1);
 			change -= price;
 			// System.out.println("add called, change: " + change);
 		} else if (!market.buy()
 				&& trans.get(good) * -1 < player.getInventory().get(good)) {
 			trans.put(good, trans.get(good) - 1);
 			change += price;
 		}
 
 		refresh();
 
 		return trans.get(good);
 	}
 
 	/**
 	 * Decrements the number of items to be bought or sold in the market.
 	 * 
 	 * @param String
 	 *            good The name of the good.
 	 * @param int price The price of the good.
 	 */
 	public int remove(String good, int price) {
 		if (market.buy() && 0 < trans.get(good)) {
 			trans.put(good, trans.get(good) - 1);
 			change += price;
 		} else if (!market.buy() && trans.get(good) < 0) {
 			trans.put(good, trans.get(good) + 1);
 			change -= price;
 		}
 
 		refresh();
 
 		return trans.get(good);
 	}
 
 	/**
 	 * Gets the number of goods currently included in the active transaction.
 	 * 
 	 * @paran Integer The number of items in the current transaction.
 	 */
 	private Integer numItems() {
 		Integer total = 0;
 
 		for (Integer i : trans.values()) {
 			total += i;
 		}
 
 		return total;
 	}
 
 	/**
 	 * Calculates the new balance of the player's money should the transaction
 	 * occur.
 	 * 
 	 * @return int The new balance of the player's money should the transaction
 	 *         occur.
 	 */
 	private int getNewBalance() {
 		return balance + change;
 	}
 
 	/**
 	 * Refreshes the transaction's display label.
 	 */
 	private void refresh() {
 		transLabel.setText("Transaction: $"	+ balance + " + (" + change + ") = $" + getNewBalance()
 						+ "      Cargo Space Remaining: " + (screen.game.getPlayer().getShip().getCargoBay() 
 						- screen.game.getPlayer().numItems() - numItems()));
 	}
 
 	/**
 	 * Allows the player to confirm a transaction.
 	 * 
 	 * @return Transaction The results of the transaction.
 	 */
 	public Transaction confirm() {
<<<<<<< HEAD
		final Hashtable<String, Integer> playerInventory = player.getInventory();
=======
 		Player player = screen.game.getPlayer();
 		Hashtable<String, Integer> playerInventory = player.getInventory();
>>>>>>> branch 'master' of https://github.com/shawnchaudhry/TeamTroll.git
 
 		for (String good : trans.keySet())
 			playerInventory.put(good, playerInventory.get(good) + trans.get(good));
 
 		// System.out.println("Confirm method in transaction: ");
 		// printInventory(playerInventory);
 
 		player.setInventory(playerInventory);
 		player.setMoney(getNewBalance());
 		change = 0;
 
 		trans = new Hashtable<String, Integer>();
 		balance = player.getMoney();
 
 		for (String good : screen.game.getPlayer().getInventory().keySet())
 			trans.put(good, 0);
 
 		screen.game.setPlayer(player);
 		refresh();
 
 		return this;
 	}
 
 	public void printInventory(Hashtable<String, Integer> inventory) {
 		for (String good : inventory.keySet()) {
 			System.out.println(good + ": " + inventory.get(good));
 		}
 	}
}
