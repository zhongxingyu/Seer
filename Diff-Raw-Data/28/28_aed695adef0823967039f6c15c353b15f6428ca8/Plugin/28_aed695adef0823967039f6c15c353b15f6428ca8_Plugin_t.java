 package no.runsafe.itemflangerorimega;
 
 import no.runsafe.framework.RunsafePlugin;
import no.runsafe.framework.api.command.Command;
 import no.runsafe.framework.features.Commands;
 import no.runsafe.framework.features.Events;
 import no.runsafe.itemflangerorimega.armour.CustomArmourEnchantHandler;
 import no.runsafe.itemflangerorimega.armour.enchants.Resilience;
 import no.runsafe.itemflangerorimega.bows.CustomBowEnchantHandler;
 import no.runsafe.itemflangerorimega.bows.enchants.ExplosiveCharge;
 import no.runsafe.itemflangerorimega.commands.EnchantArmour;
 import no.runsafe.itemflangerorimega.commands.EnchantBow;
 
 public class Plugin extends RunsafePlugin
 {
 	@Override
 	protected void pluginSetup()
 	{
 		// Framework features
 		addComponent(Events.class);
 		addComponent(Commands.class);
 
 		// Bow Enchants
 		addComponent(ExplosiveCharge.class);
 
 		// Armour enchants
 		addComponent(Resilience.class);
 
 		// Handlers
 		addComponent(CustomBowEnchantHandler.class);
 		addComponent(CustomArmourEnchantHandler.class);
 
 		// Commands
 		addComponent(EnchantBow.class);
 		addComponent(EnchantArmour.class);

		Command customEnchantCommand = new Command("customenchant", "Enchant an item with a custom enchant", null);
		addComponent(customEnchantCommand);

		customEnchantCommand.addSubCommand(getInstance(EnchantBow.class));
		customEnchantCommand.addSubCommand(getInstance(EnchantArmour.class));
 	}
 }
