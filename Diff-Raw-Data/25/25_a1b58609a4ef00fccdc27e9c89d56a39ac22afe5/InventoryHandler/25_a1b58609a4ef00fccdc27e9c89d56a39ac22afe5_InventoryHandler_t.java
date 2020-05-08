 package no.runsafe.UniqueInventories;
 
 import no.runsafe.framework.database.IRepository;
 import no.runsafe.framework.server.RunsafeWorld;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.enchantments.EnchantmentWrapper;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.MaterialData;
 
 import java.util.*;
 
 public class InventoryHandler
 {
 
 	private IRepository<InventoryStorage, RunsafePlayer> repository = null;
 	private IRepository<InventoryStorage, String> templates = null;
 
 	public InventoryHandler(
 		IRepository<InventoryStorage, RunsafePlayer> repository,
 		IRepository<InventoryStorage, String> templates
 	)
 	{
 		this.repository = repository;
 		this.templates = templates;
 	}
 
 	public void saveInventory(RunsafePlayer player, RunsafeWorld theWorld)
 	{
 		InventoryStorage storage;
 		if (repository instanceof InventoryRepository)
 			storage = ((InventoryRepository) repository).get(player, theWorld.getName());
 		else
 			storage = new InventoryStorage();
 		storage.setPlayerName(player.getName());
 		storage.setWorldName(theWorld.getName());
 		storage.setInventory(this.flatPackInventory(player));
 		storage.setExperience(player.getXP());
 		storage.setLevel(player.getLevel());
 		storage.setArmor(this.flatPackArmor(player));
 		storage.setSaved(true);
 		this.repository.persist(storage);
 	}
 
 	public void resetPlayersInventory(RunsafePlayer player)
 	{
 		if (player == null || player.getInventory() == null)
 			return;
 
 		player.setXP(0.0F);
 		player.setLevel(0);
 		player.getInventory().clear();
 		player.updateInventory();
 	}
 
 	public void loadInventory(RunsafePlayer player)
 	{
 		InventoryStorage stored = this.repository.get(player);
 
 		if (stored != null && !stored.getSaved())
 			return;
 
 		if (stored != null)
 		{
 			player.setXP(stored.getExperience());
 			player.setLevel(stored.getLevel());
 			this.unPackToInventory(stored.getInventory(), stored.getArmor(), player);
 			stored.setSaved(false);
 			this.repository.persist(stored);
 		}
 		else
 		{
 			loadTemplateInventory(player);
 		}
 	}
 
 	public void loadTemplateInventory(RunsafePlayer player)
 	{
 		InventoryStorage stored = this.templates.get(player.getWorld().getName());
 
 		if (stored != null)
 		{
 			player.setXP(stored.getExperience());
 			player.setLevel(stored.getLevel());
 			this.unPackToInventory(stored.getInventory(), stored.getArmor(), player);
 		}
 	}
 
 	public void saveTemplateInventory(RunsafePlayer player)
 	{
 		InventoryStorage storage;
 		storage = new InventoryStorage();
 		storage.setWorldName(player.getWorld().getName());
 		storage.setInventory(this.flatPackInventory(player));
 		storage.setExperience(player.getXP());
 		storage.setLevel(player.getLevel());
 		storage.setArmor(this.flatPackArmor(player));
 		this.templates.persist(storage);
 	}
 
 	public void PushInventory(RunsafePlayer player)
 	{
 		if (repository instanceof InventoryRepository)
 		{
 			saveInventory(player, player.getWorld());
 			((InventoryRepository) repository).addStack(player);
 			loadInventory(player);
 		}
 	}
 
 	public void PopInventory(RunsafePlayer player)
 	{
 		InventoryStorage inventory = repository.get(player);
 		if (inventory.getStack() > 0)
 			repository.delete(inventory);
 		loadInventory(player);
 	}
 
 	private String flattenEnchants(Map<Enchantment, Integer> enchants)
 	{
 		if (!enchants.isEmpty())
 		{
 			ArrayList<String> enchantStrings = new ArrayList<String>();
 			Set<Enchantment> enchantKeys = enchants.keySet();
 
 			for (Enchantment theEnchant : enchantKeys)
 			{
 				int enchantData = enchants.get(theEnchant);
 				enchantStrings.add(String.format("%s#%s", theEnchant.getId(), enchantData));
 			}
 
 			return Join(enchantStrings, "@");
 		}
 		else
 		{
 			return "0";
 		}
 	}
 
 	private Map<Enchantment, Integer> unpackEnchants(String enchantString)
 	{
 		Map<Enchantment, Integer> returnEnchants = new HashMap<Enchantment, Integer>();
 		String enchantSplit[] = enchantString.split("@");
 		for (String anEnchantSplit : enchantSplit)
 		{
 			String enchantData[] = anEnchantSplit.split("#");
 			Enchantment enchant = new EnchantmentWrapper(Integer.parseInt(enchantData[0]));
 			returnEnchants.put(enchant, Integer.parseInt(enchantData[1]));
 		}
 
 		return returnEnchants;
 	}
 
 	private void unPackToInventory(String packedInventory, String packedArmor, RunsafePlayer player)
 	{
 		Inventory playerInventory = player.getRawPlayer().getInventory();
 		playerInventory.clear();
 		//index, ID, amount, dura, data, helmet, chestplate, leggings, boots
 		HashMap<Integer, ItemStack> itemStacks = new HashMap<Integer, ItemStack>();
 
 		if (packedInventory != null && !packedInventory.isEmpty())
 		{
 			String[] items = packedInventory.split(",");
 
 			for (String item : items)
 			{
 				//0&61:22:0:0:0
 				if (!item.isEmpty())
 				{
 					String[] itemObject = item.split("&");
 					itemStacks.put(Integer.parseInt(itemObject[0]), this.unpackItem(itemObject[1]));
 				}
 			}
 		}
 
 		this.unpackArmorToPlayer(packedArmor, player);
 
 		Iterator<ItemStack> itemStackIterator = playerInventory.iterator();
 
 		int currentIndex = 0;
 		while (itemStackIterator.hasNext())
 		{
 			itemStackIterator.next();
 
 			if (itemStacks.containsKey(currentIndex))
 			{
 				playerInventory.setItem(currentIndex, itemStacks.get(currentIndex));
 			}
 
 			currentIndex++;
 		}
 		player.updateInventory();
 	}
 
 	private ItemStack unpackItem(String itemString)
 	{
 		String[] itemData = itemString.split(":");
 		int itemID = Integer.parseInt(itemData[0]);
 		ItemStack itemStack = new ItemStack(itemID);
 		itemStack.setAmount(Integer.parseInt(itemData[1]));
 		itemStack.setDurability(Short.parseShort(itemData[2]));
 		itemStack.setData(new MaterialData(itemID, Byte.parseByte(itemData[3])));
 
 		// Backwards compatibility check
 		if (itemData.length > 4 && !itemData[4].equals("0"))
 			itemStack.addEnchantments(unpackEnchants(itemData[4]));
 
 		return itemStack;
 	}
 
 	private String flattenItem(ItemStack theItem)
 	{
 		//0&61:22:0:0:0
 		//SlotID & ItemID:Amount:Durability:Data:Enchants
 
 		int itemID = theItem.getTypeId();
 		int itemAmount = theItem.getAmount();
 		int durability = theItem.getDurability();
 		byte data = theItem.getData().getData();
 		String enchants = flattenEnchants(theItem.getEnchantments());
 
 		return String.format("%s:%s:%s:%s:%s", itemID, itemAmount, durability, data, enchants);
 	}
 
 	private String flatPackArmor(RunsafePlayer player)
 	{
 		ItemStack[] armor = player.getRawPlayer().getInventory().getArmorContents();
 		ArrayList<String> prePackedArmor = new ArrayList<String>();
 
 		for (int i = 0; i < armor.length; i++)
 		{
 			prePackedArmor.add(String.format("%s&%s", i, flattenItem(armor[i])));
 		}
 
 		return Join(prePackedArmor, ",");
 	}
 
 	private void unpackArmorToPlayer(String armorImport, RunsafePlayer player)
 	{
 		if(armorImport == null || armorImport.isEmpty())
 			armorImport = "0&0:0:-1:0:0,1&0:0:-1:0:0,2&0:0:-1:0:0,3&0:0:-1:0:0";
 
 		ItemStack[] armorPack = new ItemStack[4];
 		String[] armorItems = armorImport.split(",");
 		for (String armorItem : armorItems)
 		{
 			String[] armorObject = armorItem.split("&");
 			armorPack[Integer.parseInt(armorObject[0])] = this.unpackItem(armorObject[1]);
 		}
 		player.getRawPlayer().getInventory().setArmorContents(armorPack);
 	}
 
 	private String flatPackInventory(RunsafePlayer player)
 	{
 		Inventory playerInventory = player.getRawPlayer().getInventory();
 
 		ArrayList<String> itemData = new ArrayList<String>();
 		Iterator<ItemStack> itemStackIterator = playerInventory.iterator();
 
 		int currentIndex = 0;
 		while (itemStackIterator.hasNext())
 		{
 			ItemStack theItem = itemStackIterator.next();
 
 			if (theItem != null)
 			{
 				itemData.add(String.format("%s&%s", currentIndex, flattenItem(theItem)));
 			}
 			currentIndex++;
 		}
 
 		return Join(itemData, ",");
 	}
 
 	private String Join(ArrayList<String> coll, String delimiter)
 	{
 		if (coll.isEmpty())
 			return "";
 
 		StringBuilder sb = new StringBuilder();
 
 		for (String x : coll)
 			sb.append(x).append(delimiter);
 
 		sb.delete(sb.length() - delimiter.length(), sb.length());
 
 		return sb.toString();
 	}
 }
