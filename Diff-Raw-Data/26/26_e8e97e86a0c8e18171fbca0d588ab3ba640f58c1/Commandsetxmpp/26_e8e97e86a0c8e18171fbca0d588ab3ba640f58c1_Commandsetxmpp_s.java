 package com.earth2me.essentials.xmpp;
 
 import com.earth2me.essentials.User;
 import com.earth2me.essentials.commands.EssentialsCommand;
 import com.earth2me.essentials.commands.NotEnoughArgumentsException;
 import org.bukkit.Server;
 
 
 public class Commandsetxmpp extends EssentialsCommand
 {
 	public Commandsetxmpp()
 	{
 		super("setxmpp");
 	}
 
 	@Override
 	protected void run(final Server server, final User user, final String commandLabel, final String[] args) throws NotEnoughArgumentsException
 	{
 		if (args.length < 1)
 		{
 			throw new NotEnoughArgumentsException();
 		}
 
 		EssentialsXMPP.getInstance().setAddress(user, args[0]);
 	}
 }
