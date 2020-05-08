 package digi.recipeManager.data;
 
 import java.util.*;
 
 import org.bukkit.ChatColor;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.inventory.ItemStack;
 
 public class Item extends ItemData
 {
 	private static final long			serialVersionUID	= -4358780777605623605L;
 	private int							amount				= 1;
 	private int							chance				= 100;
 	private Map<Enchantment, Integer>	enchantments		= new HashMap<Enchantment, Integer>();
 	
 	public Item(int type)
 	{
 		super(type);
 	}
 	
 	public Item(int type, int amount)
 	{
 		super(type);
 		this.amount = amount;
 	}
 	
 	public Item(int type, int amount, short data)
 	{
 		super(type, data);
 		this.amount = amount;
 	}
 	
 	public Item(int type, int amount, short data, int chance)
 	{
 		super(type, data);
 		this.amount = amount;
 		this.chance = chance;
 	}
 	
 	public Item(ItemStack item)
 	{
 		super(item.getTypeId(), item.getDurability());
 		amount = item.getAmount();
 	}
 	
 	@Override
 	public ItemStack getItemStack()
 	{
 		ItemStack item = new ItemStack(type, amount, data);
 		item.addUnsafeEnchantments(enchantments);
 		return item;
 	}
 	
 	public Map<Enchantment, Integer> getEnchantments()
 	{
 		return enchantments;
 	}
 	
 	public void setAmount(int amount)
 	{
 		this.amount = amount;
 	}
 	
 	public int getAmount()
 	{
 		return amount;
 	}
 	
 	public void setChance(int chance)
 	{
 		this.chance = chance;
 	}
 	
 	public int getChance()
 	{
 		return chance;
 	}
 	
 	public boolean compareItemStack(ItemStack item)
 	{
 		if(item == null)
 			return false;
 		
 		int dur = item.getDurability();
 		
		return (item.getTypeId() == type && (dur == -1 || dur == data));
 	}
 	
 	public boolean compareItemData(ItemData item)
 	{
 		return (item != null && item.type == type && (item.data == -1 || item.data == data));
 	}
 	
 	public String printAuto()
 	{
 		int enchants = enchantments.size();
 		
 		return (type == 0 && chance < 100 ? ChatColor.RED + "" + chance + "% failure chance" : (chance < 100 ? ChatColor.YELLOW + "" + chance + "% chance " + ChatColor.WHITE : "") + (amount > 1 ? amount + "x " : "") + getMaterial().toString().toLowerCase() + (data > 0 ? ":" + data : "") + (enchants > 0 ? " (" + enchants + " enchant" + (enchants == 1 ? "" : "s") + ")" : ""));
 	}
 	
 	@Override
 	public String toString()
 	{
 		return printAuto();
 	}
 }
