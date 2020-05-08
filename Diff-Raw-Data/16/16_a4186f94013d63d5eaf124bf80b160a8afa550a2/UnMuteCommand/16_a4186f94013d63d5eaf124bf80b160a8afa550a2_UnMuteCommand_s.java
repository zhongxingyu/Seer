 package no.runsafe.nchat.command;
 
 import no.runsafe.framework.command.RunsafeCommand;
 import no.runsafe.framework.configuration.IConfiguration;
 import no.runsafe.framework.output.IOutput;
 import no.runsafe.framework.server.RunsafeServer;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import no.runsafe.nchat.Constants;
 import no.runsafe.nchat.handlers.MuteHandler;
 import org.bukkit.ChatColor;
 
 public class UnMuteCommand extends RunsafeCommand
 {
 	public UnMuteCommand(IConfiguration configuration, IOutput console, MuteHandler muteHandler)
 	{
		super("unmute", null);
 		this.configuration = configuration;
 		this.console = console;
 		this.muteHandler = muteHandler;
 	}
 
 	@Override
 	public boolean Execute(RunsafePlayer player, String[] args)
 	{
 		if (args.length == 1)
 		{
 			String unMutePlayerName = args[0].trim();
 
 			if (!unMutePlayerName.isEmpty())
 			{
 				if (unMutePlayerName.equalsIgnoreCase("server"))
 				{
 					if (player.hasPermission("nChat.commands.muteServer"))
 					{
 						this.configuration.setConfigValue("spamControl.muteChat", false);
 						player.sendMessage(Constants.DEFAULT_MESSAGE_COLOR + Constants.COMMAND_CHAT_UNMUTED);
 						console.write(ChatColor.YELLOW + String.format("%s un-muted server chat.", player.getName()));
 					}
 					else
 					{
 						player.sendMessage(Constants.COMMAND_NO_PERMISSION);
 					}
 				}
 				else
 				{
 					if (player.hasPermission("nChat.commands.mutePlayer"))
 					{
 						RunsafePlayer unMutePlayer = RunsafeServer.Instance.getPlayer(unMutePlayerName);
 
 						if (unMutePlayer != null)
 						{
 							if (!unMutePlayer.hasPermission("nChat.muteExempt"))
 							{
 								console.write(ChatColor.YELLOW + String.format(
 										"%s un-muted %s",
 										player.getName(),
 										unMutePlayer.getName()
 								));
 								this.muteHandler.unMutePlayer(unMutePlayer);
 							}
 							else
 							{
 								player.sendMessage(Constants.COMMAND_TARGET_EXEMPT);
 							}
 						}
 						else
 						{
 							player.sendMessage(Constants.COMMAND_TARGET_NO_EXISTS);
 						}
 					}
 					else
 					{
 						player.sendMessage(Constants.COMMAND_NO_PERMISSION);
 					}
 				}
 			}
 			else
 			{
 				player.sendMessage(Constants.COMMAND_ENTER_PLAYER);
 			}
 
 			return true;
 		}
 		return false;
 	}
 
 	private IConfiguration configuration;
 	private IOutput console;
 	private MuteHandler muteHandler;
 }
