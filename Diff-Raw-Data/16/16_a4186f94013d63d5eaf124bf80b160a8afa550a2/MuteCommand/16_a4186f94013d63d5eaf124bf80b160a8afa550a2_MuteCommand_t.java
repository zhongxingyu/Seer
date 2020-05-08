 package no.runsafe.nchat.command;
 
 import no.runsafe.framework.command.RunsafeCommand;
 import no.runsafe.framework.configuration.IConfiguration;
 import no.runsafe.framework.output.IOutput;
 import no.runsafe.framework.server.RunsafeServer;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import no.runsafe.nchat.Constants;
 import no.runsafe.nchat.handlers.MuteHandler;
 import org.bukkit.ChatColor;
 
 public class MuteCommand extends RunsafeCommand
 {
 
     public MuteCommand(IConfiguration configuration, IOutput console, MuteHandler muteHandler)
     {
        super("mute");
         this.configuration = configuration;
 		this.console = console;
 		this.muteHandler = muteHandler;
     }
 
     @Override
     public boolean Execute(RunsafePlayer player, String[] args)
     {
         if (args.length == 1)
         {
             String mutePlayerName = args[0].trim();
 
             if (!mutePlayerName.isEmpty())
             {
                 if (mutePlayerName.equalsIgnoreCase("server"))
                 {
                     if (player.hasPermission("nChat.commands.muteServer"))
                     {
 						this.configuration.setConfigValue("spamControl.muteChat", true);
 						player.sendMessage(Constants.DEFAULT_MESSAGE_COLOR + Constants.COMMAND_CHAT_MUTED);
 						console.write(ChatColor.YELLOW + String.format("%s muted server chat.", player.getName()));
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
                         RunsafePlayer mutePlayer = RunsafeServer.Instance.getPlayer(mutePlayerName);
 
 						if (mutePlayer != null)
 						{
 							if (!mutePlayer.hasPermission("nChat.muteExempt"))
 							{
 								console.write(ChatColor.YELLOW + String.format(
 										"%s muted %s",
 										player.getName(),
 										mutePlayer.getName()
 								));
 								this.muteHandler.mutePlayer(mutePlayer);
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
