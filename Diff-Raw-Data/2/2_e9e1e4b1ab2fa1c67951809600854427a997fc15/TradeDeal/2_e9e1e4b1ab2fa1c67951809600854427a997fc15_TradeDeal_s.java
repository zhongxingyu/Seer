 package oving5;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 public class TradeDeal implements Serializable {
 	private static final long serialVersionUID = -6273972612945569502L;
 	
 	//Money to trade for item
 	private final double tradeMoney;
 	//Items to trade for item
 	private final List<TradableItem> tradeItems = new ArrayList<TradableItem>();
 	//Item trading for
 	private final TradableItem item;
 	
 	private final String trader;
 	private final String buyer;
 	
 	public TradeDeal(TradableItem item, double money, List<TradableItem> items, String seller, String buyer){
 		this.item = item;
 		this.tradeMoney = money;
 		this.tradeItems.addAll(items);
 		this.trader = seller;
 		this.buyer = buyer;
 	}
 	
 	public TradeDeal(TradableItem item, double money, String seller, String buyer){
 		this(item, money, new ArrayList<TradableItem>(), seller, buyer);
 	}
 
 	public double getTradeMoney() {
 		return tradeMoney;
 	}
 
 	public List<TradableItem> getTradeItems() {
 		return tradeItems;
 	}
 
 	public TradableItem getItem() {
 		return item;
 	}
 
 	public String getTrader() {
 		return trader;
 	}
 
 	public String getBuyer() {
 		return buyer;
 	}
 	
 	/**
 	 * Pretty print the current deal, this should only be used when displaying
 	 * to user
 	 * @return - String representation of this deal
 	 */
 	public String pPrint(){
 		return this.buyer + " wants to buy " + this.item + " from " + this.trader +
 				" for " + this.tradeMoney + " and these items " + this.tradeItems;
 	}
 	
 	/**
 	 * Parse a string which should contain a deal into an TradeDeal object
 	 * @param deal - The string representing the deal, must come from the
 	 * toString() method of TradeDeal
 	 * @return - A TradeDeal
 	 */
 	public static TradeDeal parseDeal(String deal){
 		String[] splited = deal.trim().split(";");
 		TradableItem item = TradableItem.parseTradeItem(splited[0]);
 		double money = Double.parseDouble(splited[3]);
		String[] li = splited[4].replaceAll("\\[\\]", "").split(",");
 		List<TradableItem> list = new ArrayList<TradableItem>();
 		for(String i : li){
 			list.add(TradableItem.parseTradeItem(i));
 		}
 		if(list.isEmpty())
 			return new TradeDeal(item, money, splited[2], splited[1]);
 		return new TradeDeal(item, money, list, splited[2], splited[1]);
 	}
 	
 	public String toString(){
 		return this.item + ";" + this.buyer + ";" + this.trader + ";" + this.tradeMoney
 				+ ";" + this.tradeItems;
 	}
 }
