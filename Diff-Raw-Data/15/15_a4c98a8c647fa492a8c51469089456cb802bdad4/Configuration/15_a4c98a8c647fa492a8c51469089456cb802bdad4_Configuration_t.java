 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 public class Configuration extends TradeItem {
 	public final String name;
 
 	private ItemCollection items;
 	private Map<ProductGroup, Integer> productGroups;
 	
 	public Configuration(String name) {
 		super(0,0,0);
 		this.name = name;
 		items = new ItemCollection();
 		productGroups = new HashMap<ProductGroup, Integer>();
 	}
 	
 
	//post: `count` amount TradeItem is added to items comprising `this`
 	public void addTradeItem (TradeItem item, int count){
 		items.deposit(item, count);
 	}
 	
 	//Precondition: Productgroup pg exists, count >= 0
 	/*Postcondition: adds Product to ProductGroup, if this product is 
 					 already in the group, the amount is raised.*/
 	public void addProductGroup (ProductGroup pg, int count){
 		if (productGroups.containsKey(pg)) {
			productGroups.put(pg, productGroups.get(pg) + count);
 		} else {
 			productGroups.put(pg, count);
 		}
 	}
 	
 	//Precondition: returns itemcollection if configuration is in store, otherwise: null, Store exists
 	//Postcondition: lookup if there enough parts in the store in order to build this config
 	//if not>>return null
 	public ItemCollection checkItems(Store s, Integer amount) {
 		ItemCollection ret = new ItemCollection();
 		for (Entry<TradeItem, Integer> e : items.entrySet()) {
 			TradeItem item = e.getKey();
 			Integer i = e.getValue();
 			int storedAmount = s.getAmount(item);
 			ret.deposit(item, storedAmount);
 			if (storedAmount < i) {
 				if (item instanceof Configuration) {
 					ItemCollection subItems = ((Configuration) item).checkItems(s, i - storedAmount);
 					if (subItems == null) { return null; }
 					ret.deposit(subItems);
 				} else {
 					return null;
 				}
 			}
 		}
 		for (Entry<ProductGroup, Integer> e : productGroups.entrySet()) {
 			ItemCollection groupParts = s.getProductGroup(e.getKey(), e.getValue());
 			if (groupParts == null) { return null; }
 			ret.deposit(groupParts);
 		}
 		return ret;
 	}
 	
 	/* Build configuration with Items from Store
 	 * and store it back if some subconfiguration isn't stored,
 	 * it will be built too.
 	 */
 	
 	//Precondition: Store s exists
 	/*Postcondition: only if there's enough parts for it in the store, the configuration is built.
 					 and parts are withdrawed*/
 	public void buildConfiguration(Store s) {
 		ItemCollection parts = checkItems(s, 1);
 		if (parts == null) {
 			throw new IllegalArgumentException("Store has not enough parts");
 		}
 		s.withdraw(parts);
 		s.deposit(this, 1);
 	}
 
 	//TODO: solllen wir uns noch genauer anschauen lt fabian: - was?, check "bad"
 	/*Postcondition: returns all components of the configuration as a string;
 					 components can include: products and productgroups*/
 	//BAD: doesnt show explicit, which products are in which sub-ProductGroups
 	public String toString(){
 		StringBuilder productsInGroup = new StringBuilder();
 		StringBuilder ret = new StringBuilder("Konfiguration " + name + " beinhaltet:" + "\n"+ "\n");
 		
 		ret.append("-ProductGroups: " + "\n");
 		for( Entry<ProductGroup, Integer> entry : productGroups.entrySet() )
 		{
 		  ret.append(entry.getValue()+ "x");
 		  ret.append(entry.getKey().toString());
 		  ret.append("\n");
 		  
 		  productsInGroup.append(entry.getKey().toString(true) + "\n");
 		}
 		
 		ret.append("- Produkte:" + "\n");
 		for( Entry<TradeItem, Integer> entry : items.entrySet() )
 		{
 		  ret.append(entry.getValue()+ "x");
 		  ret.append(entry.getKey().toString());
 		  ret.append("\n");
 		  
 
 		  ret.append(productsInGroup.toString()); 
 		  ret.append("\n");
 		}
 
 		return ret.toString();
 	}
 }
