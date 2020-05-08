 package net.evtr.hungergames;
 
 import java.util.Random;
 import java.util.Vector;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.InventoryHolder;
 import org.bukkit.inventory.ItemStack;
 
 public class InventoryManager {
 	public Random random;
 	
 	private int lastChance = 0;
 
 	public class VirtualChest 
 	{
 		public Chest chest;
 		
 		public class Element 
 		{
 			
 			public Inventory inventory;
 			public HungerPlayer player;
 			
 			public Element(HungerPlayer player, Inventory inventory) 
 			{
 				this.player = player;
 				this.inventory = inventory;
 			}
 		}
 		
 		public Vector<Element> playerChests;
 		
 		public Inventory getInventory(HungerPlayer player)
 		{
 			for ( int i = 0; i < playerChests.size(); i++ ) 
 			{
 				if ( playerChests.get(i).player == player ) 
 				{
 					return playerChests.get(i).inventory;
 				}
 			}
 			Inventory inventory = randomInventory(chest);
 			Element ele = new Element(player, inventory);
 			playerChests.add(ele);
 			return ele.inventory;
 		}
 		
 		public VirtualChest(Chest chest) 
 		{
 			this.chest = chest;
 			playerChests = new Vector<Element>();
 		}
 	}
 	private Vector<VirtualChest> chests;
 	
 	public Inventory getInventory(HungerPlayer player, Chest chest) {
 		return getChest(chest).getInventory(player);
 	}
 	
 	public VirtualChest getChest(Chest chest) {
 		for ( int i = 0; i < chests.size(); i++ ) {
 			Block c1 = chests.get(i).chest.getBlock(), c2 = chest.getBlock(); 
 			// chest == other chest doesn't work?
 			if ( c1.getX() == c2.getX() && c1.getY() == c2.getY() && c1.getZ() == c2.getZ() ) {
 				return chests.get(i);
 			}
 		}
 		
 		VirtualChest newChest = new VirtualChest(chest);
 		chests.add(newChest);
 		return newChest;
 	}
 	
 	private Vector<InventoryTemplate> templates;
 	
 	public void addNewTemplate(InventoryTemplate template)
 	{
 		templates.add(template);
 	}
 	
 	public void addNewTemplate(ItemStack[] items, int chance, int max) 
 	{
 		templates.add(new InventoryTemplate(items, chance, max));
 	}
 	
 	public void addNewTemplate(Material[] items, int num) 
 	{
 		ItemStack[] stacks = new ItemStack[items.length];
 		for ( int i = 0; i < items.length; i++ ) {
 			stacks[i] = new ItemStack(items[i], 1);
 		}
 		addNewTemplate(stacks, lastChance, lastChance + num);
 		lastChance += num;
 	}
 	
 	private Inventory newInventory(InventoryHolder holder, InventoryTemplate template) {
 		
 		Inventory inventory = Bukkit.createInventory(holder, InventoryType.CHEST);
 		
 		Vector<Integer> slots = new Vector<Integer>();
 		for ( int i = 0; i < inventory.getSize(); i++ ) {
 			slots.add(i);
 		}
 		
 		for ( int i = 0; i < template.items.size(); i++ ) {
 			if ( slots.size() == 0 ) break;
 			
 			inventory.setItem(slots.remove(random.nextInt(slots.size())), template.items.get(i));
 		}
 		
 		return inventory;
 	}
 	
 	private InventoryTemplate getTemplate(int startingPoint)
 	{
 		//grab the first so we never return null
 		InventoryTemplate templateToReturn = templates.get(0);
 		for(int i = 0; i < templates.size(); i++)
 		{
 			InventoryTemplate temp = templates.get(i);
			if(temp.minRange <= startingPoint && temp.maxRange < startingPoint)
 			{
 				return templates.get(i);
 			}
 		}
 		return templateToReturn;
 	}
 	
 	public Inventory randomInventory(InventoryHolder holder)
 	{
 		return newInventory(holder, getTemplate(random.nextInt(lastChance)));
 	}
 	
 	public InventoryManager() {
 		chests = new Vector<VirtualChest>();
 		random = new Random(System.currentTimeMillis());
 		templates = new Vector<InventoryTemplate>();
 	}
 }
