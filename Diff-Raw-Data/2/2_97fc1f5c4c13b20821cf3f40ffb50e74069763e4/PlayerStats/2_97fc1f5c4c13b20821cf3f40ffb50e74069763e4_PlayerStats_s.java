 package graindcafe.tribu;
 
 import graindcafe.tribu.Configuration.Constants;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class PlayerStats implements Comparable<PlayerStats> {
 	private boolean alive;
 	private int money;
 	private Player player;
 	private int points;
 
 	public PlayerStats(Player player) {
 		this.player = player;
 		alive = false;
 	}
 
 	public void addMoney(int amount) {
 		money += amount;
 	}
 
 	public void addPoints(int amount) {
 		points += amount;
 	}
 
 	// Order reversed to sort list desc
 	@Override
 	public int compareTo(PlayerStats o) {
 		if (o.getPoints() == points)
 			return 0;
 		else if (o.getPoints() > points)
 			return 1;
 		else
 			return -1;
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		if (!(o instanceof PlayerStats))
 			return false;
 		PlayerStats ps = (PlayerStats) o;
 		return ps.getPlayer().equals(player) && ps.getMoney() == money && ps.getPoints() == points;
 	}
 
 	public int getMoney() {
 		return money;
 	}
 
 	public Player getPlayer() {
 		return player;
 	}
 
 	public int getPoints() {
 		return points;
 	}
 
 	public boolean isalive() {
 		return alive;
 	}
 
 	public void kill() {
 		alive = false;
 	}
 
 	public void msgStats() {
		Tribu.messagePlayer(player, String.format(Constants.MessageMoneyPoints, String.valueOf(money), String.valueOf(points)),ChatColor.YELLOW);
 	}
 
 	public void resetMoney() {
 		money = 0;
 	}
 
 	public void resetPoints() {
 		points = 0;
 	}
 
 	public void revive() {
 		alive = true;
 	}
 
 	public boolean subtractmoney(int amount) {
 		if (money >= amount) {
 			money -= amount;
 			return true;
 		}
 		return false;
 	}
 
 	public void subtractPoints(int val) {
 		points -= val;
 		if (points < 0) {
 			points = 0;
 		}
 	}
 
 }
