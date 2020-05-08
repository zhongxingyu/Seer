 package monopoly;
 
 import java.awt.Color;
 
 public class Player
 {
 
 	private String username;
 	private int money;
 	private int value;
 	private int index;
 	private Color color;
 	private int colorCode;
 	private boolean inJail;
 	
 	public Player()
 	{
 		
 	}
 
 	public Player(String user, int colorCode)
 	{
 		username = user;
 		index = 0;
 		money = 200000;
 		value = money;
 		inJail = false;
 	}
 	
 	public void setJail(boolean b) {
 		inJail = b;
 	}
 	
 	public void setUsername(String user)
 	{
 		username = user;
 	}
 	
 	public void setColor(int code)
 	{
 		color = new Color(code);
 		colorCode = code;
 	}
 	
 	public void setIndex(int i)
 	{
 		index = i;
 	}
 
	/**
	 * Adds ' to the money to make the cash values more easily readable
	 * @return The money in a string manner with '
	 */
 	public String MoneyString()
 	{
 		String sMoney = Integer.toString(money);
		for (int i = sMoney.length(), j = 0; i > 0; i--, j++) {
			if (j % 3 == 0 && i != sMoney.length()) {
 				sMoney = sMoney.substring(0, i) + "'" + sMoney.substring(i);
 			}
 		}
 		return sMoney;
 	}
 
 	public void incrementMoney(int amount)
 	{
 		money += amount;
 		value += amount;
 	}
 
 	public void buyProperty(int cost)
 	{
 		money -= cost;
 		value += cost;
 	}
 
 	public String getUsername()
 	{
 		return username;
 	}
 	
 	public void incrementIndex(int i)
 	{
 		index += i;
 	}
 	
 	public int getIndex()
 	{
 		return index;
 	}
 
 	public int getMoney()
 	{
 		return money;
 	}
 
 	public int getValue()
 	{
 		return value;
 	}
 
 	public Color getColor()
 	{
 		return color;
 	}
 	
 	public int getColorCode()
 	{
 		return colorCode;
 	}
 	
 	public boolean isInJail() {
 		return inJail;
 	}
 }
