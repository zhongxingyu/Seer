 package edu.gatech.CS2340.GrandTheftPoke.backend;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 
 import edu.gatech.CS2340.GrandTheftPoke.backend.Items.Item;
 
 /**
  * represents a marketPlace
  * 
  * @author Team Rocket
  */
 @XStreamAlias("Market")
 public class MarketPlace {
 	// @XStreamImplicit(itemFieldName="Item")
 	HashMap<Item, MarketPlaceItem> stock;
 
 	/**
 	 * Constructor that creates the market place for each town
 	 * 
 	 * @param stock
	 *            a hash map of Items to their MarketPlaceItem economic models
 	 */
 	public MarketPlace(HashMap<Item, MarketPlaceItem> stock) {
 		this.stock = stock;
 	}
 
 	/**
 	 * checks if two markets are equal
 	 * 
 	 * @param market
 	 *            the current market
 	 * @return boolean
 	 */
 	public boolean equals(MarketPlace market) {
 		if (market == null)
 			return false;
 		for (Entry<Item, MarketPlaceItem> entry : stock.entrySet())
 			if (!(market.stock.containsKey(entry.getKey()) && market.stock.get(
 					entry.getKey()).equals(entry.getValue())))
 				return false;
 		return true;
 	}
 
 	/**
 	 * buys an item
 	 * 
 	 * @param good
 	 *            the good to be bought
 	 * @param quantity
 	 *            quantity to be bought
 	 * @return float
 	 */
 	public float buy(Item good, int quantity) {
 		if (stock.containsKey(good)) {
 			MarketPlaceItem model = stock.get(good);
 			return model.buy(quantity);
 		}
 		return 0;
 	}
 
 	/**
 	 * sells an item
 	 * 
 	 * @param good
 	 *            good to be sold
 	 * @param quantity
 	 *            quantity to be sold
 	 * @return float
 	 */
 	public float sell(Item good, int quantity) {
 		if (stock.containsKey(good)) {
 			MarketPlaceItem model = stock.get(good);
 			return model.sell(quantity);
 		}
 		return 0;
 
 	}
 
 	/**
 	 * getsStock
 	 * 
 	 * @return stock the stock of the market
 	 */
 	public HashMap getStock() {
 		return stock;
 	}
 
 	/**
 	 * marketPlace Iterator
 	 * 
 	 * @return Iterator iterator
 	 */
 	public Iterator iterator() {
 		return stock.entrySet().iterator();
 
 	}
 }
