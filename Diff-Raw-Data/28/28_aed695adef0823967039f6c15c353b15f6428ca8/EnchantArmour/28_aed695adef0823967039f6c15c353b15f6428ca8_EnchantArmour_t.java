 package no.runsafe.itemflangerorimega.commands;
 
 import no.runsafe.framework.api.command.argument.IArgumentList;
 import no.runsafe.framework.api.command.player.PlayerCommand;
 import no.runsafe.framework.api.player.IPlayer;
 import no.runsafe.framework.minecraft.Item;
 import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
 import no.runsafe.itemflangerorimega.armour.CustomArmourEnchantHandler;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class EnchantArmour extends PlayerCommand
 {
 	public EnchantArmour(CustomArmourEnchantHandler handler)
 	{
 		super(
				"armour", "Enchants armour using a magical custom enchant.", "runsafe.flangerorimega.enchant.armour",
 				new EnchantArmourArgument(handler)
 		);
 		this.handler = handler;
 	}
 
 	@Override
 	public String OnExecute(IPlayer executor, IArgumentList parameters)
 	{
 		String enchantType = parameters.get("enchant");
 		RunsafeMeta item = executor.getItemInHand();
 		if (item != null && armour.contains(item.getItemType()))
 		{
 			handler.enchantArmour(item, handler.getEnchant(enchantType));
			return "&2The armour has been enchanted!.";
 		}
		return "&cYou are not holding armour.";
 	}
 
 	private CustomArmourEnchantHandler handler;
 	private static List<Item> armour = new ArrayList<Item>(0);
 
 	static
 	{
 		armour.add(Item.Combat.Boots.Leather);
 		armour.add(Item.Combat.Helmet.Leather);
 		armour.add(Item.Combat.Chestplate.Leather);
 		armour.add(Item.Combat.Leggings.Leather);
 		armour.add(Item.Combat.Boots.Iron);
 		armour.add(Item.Combat.Helmet.Iron);
 		armour.add(Item.Combat.Chestplate.Iron);
 		armour.add(Item.Combat.Leggings.Iron);
 		armour.add(Item.Combat.Boots.Gold);
 		armour.add(Item.Combat.Helmet.Gold);
 		armour.add(Item.Combat.Chestplate.Gold);
 		armour.add(Item.Combat.Leggings.Gold);
 		armour.add(Item.Combat.Boots.Diamond);
 		armour.add(Item.Combat.Helmet.Diamond);
 		armour.add(Item.Combat.Chestplate.Diamond);
 		armour.add(Item.Combat.Leggings.Diamond);
 	}
 }
