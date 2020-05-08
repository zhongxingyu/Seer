 package StaffDrops.republica.devteam;
 
 import net.minecraft.server.NBTBase;
 import net.minecraft.server.NBTTagCompound;
 import net.minecraft.server.NBTTagList;
 import net.minecraft.server.NBTTagString;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.craftbukkit.inventory.CraftItemStack;
 import org.bukkit.inventory.ItemStack;
 
 public class Legendarys {
 	
	private CraftItemStack LegendaryItem;
 	private String name = "THERESNOFUCKINGNAMEDIPSHIT";
 	private String lore = "THERESNOFUCKINGLOREDIPSHIT";
 	private NBTTagCompound tags;
	private NBTTagCompound display = null;
 	private NBTTagString _lore;
 	private NBTTagList thisIsFuckingStupid;
 
 	public Legendarys(ItemStack is){
 		this(new CraftItemStack(is));
 	}
 	
 	public Legendarys(CraftItemStack cis){
		LegendaryItem = cis;
 		
 		tags = cis.getHandle().tag;
 		
 		if(tags == null){
 			tags = new NBTTagCompound();
 		}
 		
 		if(!tags.hasKey("ench")){
 			//tags.set("ench", null);
 		}
 		if(!tags.hasKey("RepairCost")){
 			tags.setInt("RepairCost", 10000);
		}
		if(!tags.hasKey("display")){
			//display.set("Lore", null);
			//display.setString("Name", "NoName :(");
			
			//tags.setCompound("display", display);
		}
		
 		if(tags.hasKey("display")){
 			display = tags.getCompound("display");
 		}
 		else{
 			display = new NBTTagCompound();
 		}
 		
 		thisIsFuckingStupid = new NBTTagList();
 	}
 	
 	public void setItemName(String _name){	
 		Bukkit.broadcastMessage("Item name was called");
 		itemInfos(_name, "Name");
 	}
 	
 	public void setItemRarity(int i){
 		if(display != null){
 			if(display.hasKey("Name")){
 				switch(i){
 				case 1:
 					name = ChatColor.GREEN + name;
 					setItemName(name);
 					break;
 				case 2:
 					name = ChatColor.BLUE + name;
 					setItemName(name);
 					break;
 				case 3:
 					name = ChatColor.RED + name;
 					setItemName(name);
 					break;
 				case 4: 
 					name =  ChatColor.YELLOW + name;
 					setItemName(name);
 					break;
 				}
 			}
 		}
 	}
 	
 	public void setItemLore(String _lore_){
 		Bukkit.broadcastMessage("Item lore was called");
 		itemInfos(_lore_, "Lore");
 	}
 	
 	public void itemInfos(String s, String type){	
 		if(display != null){
 			Bukkit.broadcastMessage("It was actually called if that helps...");
 			if(type == "Lore"){
 				lore = ChatColor.DARK_PURPLE + s;
 				_lore = new NBTTagString(lore);
 				thisIsFuckingStupid.add(_lore);
 				thisIsFuckingStupid.setName("Lore");
 				if(display.hasKey("Lore")){//test
 					Bukkit.broadcastMessage("THE LORE IS WORKING DIPSHIT");
 					displayNeedsToGetItsShitTogether(thisIsFuckingStupid, "Lore");
 				}
 				else{
 					display.set("Lore", thisIsFuckingStupid);
 				}
 				
 			}
 			if(type == "Name"){
 				name = s;
 				if(display.hasKey("Name")){//test
 					Bukkit.broadcastMessage("THE NAME IS WORKING DIPSHIT");
 					displayNeedsToGetItsShitTogether(name, "Name");
 				}
 				else{
 					display.setString("Name", name);
 				}
 			}
 			tags.setCompound("display", display);
 		}
 		else{
 			Bukkit.broadcastMessage("Display is null");
 		}
 	}
 	
 	public void displayNeedsToGetItsShitTogether(Object o, String type){
 		if(type == "Lore"){
 			display.set("Lore", thisIsFuckingStupid);
 		}
 		if(type == "Name"){
 			display.setString("Name", name);
 		}
 	}
 	
 	public ItemStack getItemStack(){
		LegendaryItem.getHandle().tag = tags;
		return LegendaryItem;
 	}
 	
 }
