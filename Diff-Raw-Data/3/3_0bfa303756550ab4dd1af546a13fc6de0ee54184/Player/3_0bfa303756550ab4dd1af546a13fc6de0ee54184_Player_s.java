 /**
  * 
  */
 package monopoly;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 /**
  * @author thiemann
  *
  */
 public class Player {
 	private final String name;
 	private int position;
 	private int cash;
 	private Collection<IProperty> ownedProperty;
 	private Collection<IActionCard> cards;
 	
 	public JailState jailState;
 	
 	public Player(String name) {
 		this.name = name;
 		this.position = Constants.START_POSITION;
 		this.cash = Constants.START_CASH;
 		this.ownedProperty = new ArrayList<IProperty>();
 		this.cards = new ArrayList<IActionCard>();
 		this.jailState = JailState.FREE;
 	}
 
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @return the position
 	 */
 	public int getPosition() {
 		return position;
 	}
 
 	/**
 	 * @param position the position to set
 	 */
 	public void setPosition(int position) {
 		this.position = position;
 	}
 
 	/**
 	 * @return the cash
 	 */
 	public int getCash() {
 		return cash;
 	}
 
 	/**
 	 * @param cash the cash to set
 	 */
 	public void setCash(int cash) {
 		this.cash = cash;
 	}
 
 	/**
 	 * @return the inJail
 	 */
 	public boolean isInJail() {
 		return this.jailState != JailState.FREE;
 	}
 
	/**
	 * @param inJail the inJail to set
	 */
 	public void gotoJail() {
 		this.jailState = JailState.INJAIL;
 	}
 
 	public void addProperty(IProperty prop) {
 		this.ownedProperty.add(prop);
 	}
 	
 	public boolean earn(int amount) {
 		if (amount > 0) {
 			this.cash += amount;
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public boolean pay(int price) {
 		if ( (price > 0) && (price <= this.cash) ) {
 			this.cash -= price;
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public boolean ownsAllInGroup(Group colorGroup) {
 		int count = 0;
 		for (IProperty prop : this.ownedProperty) {
 			if (prop.inColorGroup(colorGroup)) {
 				count++;
 			}
 		}
 		return count == colorGroup.getNrInGroup();
 	}
 
 	public boolean anyMortgaged(Group group) {
 		for (IProperty prop : this.ownedProperty) {
 			if (prop.inColorGroup(group) && prop.isMortgaged()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 }
