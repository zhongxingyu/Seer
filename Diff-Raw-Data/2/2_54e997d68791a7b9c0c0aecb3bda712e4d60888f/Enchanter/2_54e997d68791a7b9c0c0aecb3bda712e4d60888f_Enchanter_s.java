 package no.runsafe.toybox.handlers;
 
 import no.runsafe.framework.server.enchantment.RunsafeEnchantment;
 import no.runsafe.framework.server.enchantment.RunsafeEnchantmentType;
 import no.runsafe.framework.server.item.RunsafeItemStack;
 
 public class Enchanter
 {
 	public RunsafeItemStack createFullyEnchanted(int itemID)
 	{
 		RunsafeItemStack item = new RunsafeItemStack(itemID);
 		this.applyAllEnchants(item);
 		return item;
 	}
 
 	public void applyAllEnchants(RunsafeItemStack item)
 	{
		for (int enchantID : RunsafeEnchantmentType.enchants)
 		{
 			RunsafeEnchantment enchant = new RunsafeEnchantment(enchantID);
 			if (enchant.canEnchantItem(item))
 				item.addEnchantment(enchant, enchant.getMaxLevel());
 		}
 	}
 }
