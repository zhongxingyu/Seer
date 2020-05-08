 package ch.andefgassm.adventuregame.game.state;
 
 import java.util.List;
 import java.util.Map.Entry;
 import ch.andefgassm.adventuregame.combat.IStat;
import ch.andefgassm.adventuregame.game.Stat;
import ch.andefgassm.adventuregame.game.assets.AssetLoadException;
import ch.andefgassm.adventuregame.game.assets.ItemLoader;
 import ch.andefgassm.adventuregame.game.inventory.Item;
 import ch.andefgassm.adventuregame.game.inventory.ItemType;
 import ch.andefgassm.adventuregame.game.inventory.Player;
 
 public class InventoryMenuState extends AbstractConsoleGameState {
 	
 	private GameStateContext context;
 	private Player p = Player.getInstance();
 	List<Item> inventory = p.getInventory();
 	//character mit items ausruesten	
 	
 	public InventoryMenuState() {
 //		//testItem
 //		Item testItem = new Item();
 //		testItem.setName("Todes-Messer-NunJackOh");
 //		testItem.setType(ItemType.WEAPON);
 //		testItem.getStats().put(Stat.STRENGTH, 10);
 //		testItem.getStats().put(Stat.BASMATI, 99);
 //		inventory.add(testItem);		
 //		//testItem
 //		
 //		//testItem
 //		Item testItem2 = new Item();
 //		testItem2.setName("Affenschaedel-Helm");
 //		testItem2.setType(ItemType.HEAD);
 //		testItem2.getStats().put(Stat.STRENGTH, 69);
 //		testItem2.getStats().put(Stat.SPEED, 3);
 //		testItem2.getStats().put(Stat.AGILITY, 19);
 //		inventory.add(testItem2);		
 //		//testItem
 		
 //		Item item;
 //		try {
 //			item = ItemLoader.load();
 //			inventory.add(item);
 //		} catch (AssetLoadException e) {
 //			e.printStackTrace();
 //		}
 		
 				
 	}
 	
 	public void init(GameStateContext context) {
 		this.context = context;
 		
 	    clear();
 		println("Inventory Menu");
 		println("=========");
 		println("");
 		println("Inventory (press button to equip item)");
 		println("--------------------------------------");
 					
 		for(int i=1; i <= inventory.size(); i++)
 		{
 			Item item = inventory.get(i-1);
 			StringBuilder sb = new StringBuilder(i);
 			sb.append(String.format("%s ) ", i));
 			sb.append(String.format("[%s] ", isItemEquipped(item)));
 			sb.append(item.getType());
 			sb.append(" ");
 			sb.append(item.getName());
 			sb.append(" ");
 			
 			for (Entry<IStat, Integer> entry : item.getStats().entrySet()) {
 				sb.append(String.format("[%s %s] ", entry.getKey(), entry.getValue()));
 			}
 			
 			println(sb.toString());
 		}
 		
 		println("--------------------------------------");
 		println("0 ) Back to main menu");
 		
 	}
 
 	private String isItemEquipped(Item item) {
 		boolean hasMatch = false;
 		for (Entry<ItemType, Item> entry : p.getEquipment().entrySet()) {
 			if(entry.getValue() == item)
 				hasMatch = true;
 		}
 
 		if(hasMatch)
 			return "X";
 		else
 			return "";
 	}
 
 	@Override
 	public void handleInput(int input) {
 		if(input == 0){
 			context.changeState(GameStateContext.MAIN_MENU);
 		}
 		else if(input <= inventory.size()){
 			p.equip(inventory.get(input-1));
 			init(context);
 		}
 	}
 }
