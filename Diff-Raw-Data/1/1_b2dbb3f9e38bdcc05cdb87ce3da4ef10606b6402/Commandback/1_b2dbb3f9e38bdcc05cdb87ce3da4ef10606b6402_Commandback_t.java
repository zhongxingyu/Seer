 package com.earth2me.essentials.commands;
 
 import static com.earth2me.essentials.I18n._;
 import com.earth2me.essentials.Trade;
 import com.earth2me.essentials.User;
 import org.bukkit.Server;
 
 
 public class Commandback extends EssentialsCommand
 {
 	public Commandback()
 	{
 		super("back");
 	}
 
 	@Override
 	protected void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception
 	{
 		final Trade charge = new Trade(this.getName(), ess);
 		charge.isAffordableFor(user);
 		user.sendMessage(_("backUsageMsg"));
 		user.getTeleport().back(charge);
		throw new NoChargeException();
 	}
 }
