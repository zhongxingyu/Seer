 package musician101.itembank.util;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import musician101.itembank.ItemBank;
 
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * @author Musician101
  */
 public class ItemTranslator 
 {
 	/** HashMap storing all of the aliases and their respective ItemStacks.*/
 	private final Map<String, ItemStack> items = new HashMap<String, ItemStack>();
 	
 	/**
 	 * Loads up the translator for the plugin.
 	 * 
 	 * @param plugin References the plugin's main class.
 	 * @param values List of values used to determine the aliases and their IDs.
 	 */
 	public ItemTranslator(ItemBank plugin, Iterable<String[]> values)
 	{
 		for (String[] s : values)
 		{
 			if (!s[0].startsWith("#"))
 			{
 				if (s.length < 1) continue;
 				Material material;
 				ItemStack item;
				String alias = s[0].toLowerCase();
 				try
 				{
					material = Material.valueOf(s[1].toUpperCase());
 					item = new ItemStack(material);
 				}
 				catch (IllegalArgumentException e)
 				{
					plugin.getLogger().warning("Error with Material: " + s[1].toUpperCase());
 					continue;
 				}
 				
 				try
 				{
					short data = Short.valueOf(s[2]);
 					item.setDurability(data);
 				}
 				catch (NumberFormatException e)
 				{
					plugin.getLogger().info("Error with data: " + s[2]);
 					continue;
 				}
 				
 				synchronized (items)
 				{
 					items.put(alias, item);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Retrieves an ItemStack based an a given alias.
 	 * 
 	 * @param alias The alias used to match the ItemStack.
 	 * @return item
 	 */
 	public ItemStack getItemStackFromAlias(String alias)
 	{
 		boolean found = false;
 		String data = null;
 		if (alias.contains(":"))
 		{
 			String[] datas = alias.split(":");
 			data = (datas.length > 1) ? datas[1] : "";
 			alias = datas[0];
 		}
 		
 		if (items.keySet().contains(alias.toLowerCase())) found = true;
 		
 		if (!found) return null;
 		
 		ItemStack item = items.get(alias);
 		if (data != null && !data.isEmpty())
 		{
 			try
 			{
 				item.setDurability(Short.parseShort(data));
 			}
 			catch (NumberFormatException e){}
 		}
 		return item;
 	}
 	
 	/**
 	 * Get the list of aliases based on a given ItemStack.
 	 * 
 	 * @param item The item to search aliases for.
 	 * @return
 	 */
 	public String getAliases(ItemStack item)
 	{
 		List<String> aliases = new ArrayList<String>();
 		for (String alias : items.keySet())
 			if (items.get(alias).getType() == item.getType() && items.get(alias).getDurability() == item.getDurability())
 				aliases.add(alias);
 		
 		if (aliases.size() > 15)
 			aliases = aliases.subList(0, 14);
 		
 		return IBUtils.joinList(", ", aliases);
 	}
 }
