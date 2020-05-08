 package ch.minepvp.spout.kingdoms.command.admin;
 
 import ch.minepvp.spout.kingdoms.Kingdoms;
 import org.spout.api.chat.ChatArguments;
 import org.spout.api.command.CommandContext;
 import org.spout.api.command.CommandSource;
 import org.spout.api.command.annotated.Command;
 import org.spout.api.command.annotated.CommandPermissions;
 import org.spout.api.command.annotated.NestedCommand;
 import org.spout.api.entity.Player;
 import org.spout.api.exception.CommandException;
 import org.spout.api.lang.Translation;
 
 public class AdminCommands {
 
     private final Kingdoms plugin;
 
     public AdminCommands( Kingdoms instance) {
         plugin = instance;
     }
 
     @Command(aliases = {"kingdom", "king"}, usage = "", desc = "Kingdom Commands", min = 1, max = 4)
     @NestedCommand(AdminKingdomsCommands.class)
     public void kingdom(CommandContext args, CommandSource source) throws CommandException {
 
     }
 
     @Command(aliases = {"member"}, usage = "", desc = "Kingdom Commands", min = 1, max = 4)
     @NestedCommand(AdminMemberCommans.class)
     public void member(CommandContext args, CommandSource source) throws CommandException {
 
     }
 
     @Command(aliases = {"plot"}, usage = "", desc = "Kingdom Commands", min = 1, max = 4)
     @NestedCommand(AdminPlotCommands.class)
     public void plot(CommandContext args, CommandSource source) throws CommandException {
 
     }
 
     @Command(aliases = {"zone"}, usage = "", desc = "Kingdom Commands", min = 1, max = 4)
     @NestedCommand(AdminZoneCommands.class)
     public void zone(CommandContext args, CommandSource source) throws CommandException {
 
     }
 
     @Command(aliases = {"economy", "money"}, usage = "", desc = "Kingdom Commands", min = 1, max = 4)
     @NestedCommand(AdminEconomyCommands.class)
     public void economy(CommandContext args, CommandSource source) throws CommandException {
 
     }
 
     @Command(aliases = {"help"}, usage = "", desc = "List all Commands for /economy /money")
    @CommandPermissions("kingdoms.command.economy.help")
     public void help(CommandContext args, CommandSource source) throws CommandException {
 
         Player player = plugin.getEngine().getPlayer( source.getName(), true );
 
         if ( player == null ) {
             source.sendMessage( ChatArguments.fromFormatString(Translation.tr("You must be a Player!", source)) );
             return;
         }
 
         player.sendMessage( ChatArguments.fromFormatString("{{BLUE}}-----------------------------------------------------") );
         player.sendMessage( ChatArguments.fromFormatString("{{YELLOW}}Help") );
         player.sendMessage( ChatArguments.fromFormatString("{{BLUE}}-----------------------------------------------------") );
 
         if ( player.hasPermission("kingdoms.command.admin.kingdom") ) {
             player.sendMessage(ChatArguments.fromFormatString(Translation.tr("{{YELLOW}}/%0 king", source, args.getCommand())));
             player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{GOLD}}-> {{WHITE}}Admin Commands for the Kingdoms", source)) );
         }
 
         if ( player.hasPermission("kingdoms.command.admin.member") ) {
             player.sendMessage(ChatArguments.fromFormatString(Translation.tr("{{YELLOW}}/%0 member", source, args.getCommand())));
             player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{GOLD}}-> {{WHITE}}Admin Commands for the Members", source)) );
         }
 
         if ( player.hasPermission("kingdoms.command.admin.plot") ) {
             player.sendMessage(ChatArguments.fromFormatString(Translation.tr("{{YELLOW}}/%0 plot", source, args.getCommand())));
             player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{GOLD}}-> {{WHITE}}Admin Commands for the Plots", source)) );
         }
 
         if ( player.hasPermission("kingdoms.command.admin.zone") ) {
             player.sendMessage(ChatArguments.fromFormatString(Translation.tr("{{YELLOW}}/%0 zone", source, args.getCommand())));
             player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{GOLD}}-> {{WHITE}}Admin Commands for the Zones", source)) );
         }
 
         if ( player.hasPermission("kingdoms.command.admin.economy") ) {
             player.sendMessage(ChatArguments.fromFormatString(Translation.tr("{{YELLOW}}/%0 economy", source, args.getCommand())));
             player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{GOLD}}-> {{WHITE}}Admin Commands for the Economy", source)) );
         }
 
 
 
     }
 
 
 }
