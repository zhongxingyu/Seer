 package no.runsafe.itemflangerorimega.commands;
 
 import no.runsafe.framework.api.command.argument.IArgumentList;
 import no.runsafe.framework.api.command.player.PlayerCommand;
 import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.Item;
 import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
 import no.runsafe.itemflangerorimega.tools.CustomToolEnchantHandler;
 
 public class EnchantTool extends PlayerCommand
 {
 	public EnchantTool(CustomToolEnchantHandler handler)
 	{
 		super(
 			"tool", "Enchants a tool using a magical custom enchant.", "runsafe.flangerorimega.enchant.tool",
 			new EnchantToolArgument(handler)
 		);
 		this.handler = handler;
 	}
 
 	@Override
 	public String OnExecute(IPlayer executor, IArgumentList parameters)
 	{
 		String enchantType = parameters.get("enchant");
 		RunsafeMeta item = executor.getItemInHand();
		if (item != null && item.is(Item.Combat.Bow))
 		{
 			handler.enchantTool(item, handler.getEnchant(enchantType));
			return "&2Your bow has been enchanted with wizardry.";
 		}
		return "&cYou are not holding a bow.";
 	}
 
 	private CustomToolEnchantHandler handler;
 }
