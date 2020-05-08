 package me.frodenkvist.armoreditor;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Color;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.LeatherArmorMeta;
 
 public class Store
 {
 	private static List<EpicGear> inventory = new ArrayList<EpicGear>();
 	
 	public static boolean addItem(EpicGear item)
 	{
 		return inventory.add(item);
 	}
 	
 	public static void load(FileConfiguration fc)
 	{
 		inventory.clear();
 		String c = new String();
 		for(int i = 0;i < 10; ++i)
 		{
 			switch(i)
 			{
 			case 0:
 				c = "Black";
 				break;
 			case 1:
 				c = "Red";
 				break;
 			case 2:
 				c = "Green";
 				break;
 			case 3:
 				c = "Blue";
 				break;
 			case 4:
 				c = "White";
 				break;
 			case 5:
 				c = "Purple";
 				break;
 			case 6:
 				c = "Teal";
 				break;
 			case 7:
 				c = "Pink";
 				break;
 			case 8:
 				c = "Orange";
 				break;
 			case 9:
 				c = "Yellow";
 				break;
 			}
 			EpicGear eg = new EpicArmor();
 			if(eg.load(fc.getConfigurationSection(c), "Helmet", AEHandler.getColorByName(c)))
 				inventory.add(eg);
 			eg = new EpicArmor();
 			if(eg.load(fc.getConfigurationSection(c), "Chestplate", AEHandler.getColorByName(c)))
 				inventory.add(eg);
 			eg = new EpicArmor();
 			if(eg.load(fc.getConfigurationSection(c), "Leggings", AEHandler.getColorByName(c)))
 				inventory.add(eg);
 			eg = new EpicArmor();
 			if(eg.load(fc.getConfigurationSection(c), "Boots", AEHandler.getColorByName(c)))
 				inventory.add(eg);
 		}
 		for(int i = 0;i < 3; ++i)
 		{
 			switch(i)
 			{
 			case 0:
 				c = "Sword";
 				break;
 			case 1:
 				c = "Axe";
 				break;
 			case 2:
 				c = "Bow";
 				break;
 			}
 			for(int j=1;fc.contains("Weapons." + c + "s" + "." + j);++j)
 			{
 				EpicGear eg = new EpicWeapon();
 				if(eg.load(fc.getConfigurationSection("Weapons"), c, Color.fromRGB(j)))
 					inventory.add(eg);
 			}
 		}
 		Collections.sort(inventory, new EpicGearComparator());
 		ArmorEditor.plugin.logger.info("" + inventory.size());
 	}
 	
 	public static ItemStack getItem(String name)
 	{
 		for(EpicGear eg : inventory)
 		{
 			if(eg.getDisplayName().equalsIgnoreCase(name))
 				return eg.getItem();
 		}
 		return null;
 	}
 	
 	public static void addNameAndLore(ItemStack is)
 	{
 		LeatherArmorMeta lam = (LeatherArmorMeta)is.getItemMeta();
 		for(EpicGear eg : inventory)
 		{
 			if(!(eg instanceof EpicArmor))
 				continue;
 			EpicArmor ea = (EpicArmor)eg;
 			if(ea.getColor().equals(lam.getColor()))
 			{
 				if(is.getType().toString().replace("leather_", "").equalsIgnoreCase(ea.getType()))
 				{
 					Namer.setName(is, ea.getName());
 					Namer.setLore(is, ea.getLore());
 					return;
 				}
 			}
 		}
 	}
 	
 	public static boolean isEpicGear(ItemStack is)
 	{
 		if(is.getItemMeta() == null)
 			return false;
 		List<String> lore = is.getItemMeta().getLore();
 		if(lore == null || lore.isEmpty())
 			return false;
 		for(EpicGear eg : inventory)
 		{
 			if(eg.getCode().equalsIgnoreCase(lore.get(lore.size()-1)))
 				return true;
 		}
 		return false;
 	}
 	
 	public static boolean isEpicArmor(ItemStack is)
 	{
 		if(is.getItemMeta() == null)
 			return false;
 		List<String> lore = is.getItemMeta().getLore();
 		if(lore == null || lore.isEmpty())
 			return false;
 		for(EpicGear eg : inventory)
 		{
 			if(!(eg instanceof EpicArmor))
 				continue;
 			if(eg.getCode().equalsIgnoreCase(lore.get(lore.size()-1)))
 				return true;
 		}
 		return false;
 	}
 	
 	public static EpicArmor getEpicArmor(ItemStack is)
 	{
 		if(is.getItemMeta() == null)
 			return null;
 		List<String> lore = is.getItemMeta().getLore();
 		if(lore == null || lore.isEmpty())
 			return null;
 		for(EpicGear eg : inventory)
 		{
 			if(!(eg instanceof EpicArmor))
 				continue;
			//ArmorEditor.plugin.logger.info(Namer.removeTag(eg.getCode()));
			//ArmorEditor.plugin.logger.info(Namer.removeTag(Namer.removeTag(lore.get(lore.size()-1))));
 			if(Namer.addChatColor(eg.getCode()).equalsIgnoreCase(Namer.addChatColor(lore.get(lore.size()-1))))
 				return (EpicArmor)eg;
 		}
 		return null;
 	}
 	
 	public static EpicGear getEpicGear(ItemStack is)
 	{
 		if(is.getItemMeta() == null)
 			return null;
 		List<String> lore = is.getItemMeta().getLore();
 		if(lore == null || lore.isEmpty())
 			return null;
 		for(EpicGear eg : inventory)
 		{
 			//if(!(eg instanceof EpicArmor))
 			if(eg.getCode().equalsIgnoreCase(lore.get(lore.size()-1)))
 				return (EpicArmor)eg;
 		}
 		return null;
 	}
 	
 	public static EpicGear getEpicGear(String diplayname)
 	{
		//if(is.getItemMeta() == null)
		//	return null;
		//List<String> lore = is.getItemMeta().getLore();
		//if(lore == null || lore.isEmpty())
		//	return null;
 		for(EpicGear eg : inventory)
 		{
			//if(!(eg instanceof EpicArmor))
 			if(eg.getDisplayName().equalsIgnoreCase(diplayname))
 				return eg;
 		}
 		return null;
 	}
 	
 	public static void displayStoreList(Player player, int nr)
 	{
 		int size = inventory.size();
 		if(size <= 0)
 		{
 			player.sendMessage(ChatColor.RED + "The Is Nothing In The Store Right Now");
 			return;
 		}
 		int pages = size/8;
 		if((size%8) != 0)
 			++pages;
 		if(nr > pages)
 			nr = pages;
 		player.sendMessage(ChatColor.GOLD + "----" + ChatColor.YELLOW + " EpicGear Store " + ChatColor.GOLD + "----(pg. " + nr + "/" + pages + ")");
 		player.sendMessage(ChatColor.GOLD + "| " + ChatColor.AQUA + " Name " + ChatColor.GOLD + " | " + ChatColor.DARK_AQUA + " Buy Name " + ChatColor.GOLD + " | " + ChatColor.GRAY + " Cost " + ChatColor.GOLD + " |");
		for(int i = (8*pages-8);i<pages*8;++i)
 		{
 			EpicGear eg = inventory.get(i);
 			player.sendMessage(ChatColor.GOLD + "- " + ChatColor.AQUA + Namer.removeTag(eg.getName()) + " " + ChatColor.DARK_AQUA + eg.getDisplayName() + " " + ChatColor.GRAY + eg.getCost());
			if(inventory.size() >= i+1)
				return;
 		}
		/*else
		{
			player.sendMessage(ChatColor.GOLD + "----EpicGear Store----(pg. " + nr + "/" + pages + ")");
			player.sendMessage(ChatColor.GOLD + "| " + ChatColor.AQUA + " Name " + ChatColor.GOLD + " | " + ChatColor.DARK_AQUA + " Buy Name " + ChatColor.GOLD + " | " + ChatColor.GRAY + " Cost " + ChatColor.GOLD + " |");
			for(int i = (pages-8);i<pages;++i)
			{
				EpicGear eg = inventory.get(i);
				player.sendMessage(ChatColor.AQUA + eg.getName() + " " + ChatColor.DARK_AQUA + eg.getDisplayName() + " " + ChatColor.GRAY + eg.getCost());
				if(inventory.size() == i+1)
					return;
			}
		}*/
 	}
 }
