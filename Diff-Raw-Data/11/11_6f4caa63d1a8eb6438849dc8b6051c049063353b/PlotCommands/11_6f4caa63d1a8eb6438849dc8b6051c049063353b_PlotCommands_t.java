 package ch.minepvp.spout.kingdoms.command;
 
 import ch.minepvp.spout.kingdoms.Kingdoms;
 import ch.minepvp.spout.kingdoms.component.KingdomsComponent;
 import ch.minepvp.spout.kingdoms.component.SelectionComponent;
 import ch.minepvp.spout.kingdoms.database.table.Kingdom;
 import ch.minepvp.spout.kingdoms.database.table.Member;
 import ch.minepvp.spout.kingdoms.database.table.Plot;
 import ch.minepvp.spout.kingdoms.entity.KingdomRank;
 import ch.minepvp.spout.kingdoms.manager.KingdomManager;
 import ch.minepvp.spout.kingdoms.manager.MemberManager;
 import ch.minepvp.spout.kingdoms.manager.PlotManager;
 import org.spout.api.chat.ChatArguments;
 import org.spout.api.command.CommandContext;
 import org.spout.api.command.CommandSource;
 import org.spout.api.command.annotated.Command;
 import org.spout.api.command.annotated.CommandPermissions;
 import org.spout.api.entity.Player;
 import org.spout.api.exception.CommandException;
 import org.spout.api.geo.discrete.Point;
 import org.spout.api.lang.Translation;
 
 public class PlotCommands {
 
     private final Kingdoms plugin;
     private KingdomManager kingdomManager;
     private MemberManager memberManager;
     private PlotManager plotManager;
 
     public PlotCommands(Kingdoms instance) {
         plugin = instance;
 
         kingdomManager = plugin.getKingdomManager();
         memberManager = plugin.getMemberManager();
         plotManager = plugin.getPlotManager();
     }
 
     @Command(aliases = {"help"}, usage = "", desc = "List all Commands for Plot")
     @CommandPermissions("kingdoms.command.plot.help")
     public void help(CommandContext args, CommandSource source) throws CommandException {
 
         Player player = plugin.getEngine().getPlayer( source.getName(), true );
 
         if ( player == null ) {
             source.sendMessage( ChatArguments.fromFormatString(Translation.tr("You must be a Player!", source)) );
             return;
         }
 
         player.sendMessage( ChatArguments.fromFormatString("{{BLUE}}-----------------------------------------------------") );
         player.sendMessage( ChatArguments.fromFormatString("{{YELLOW}}Help") );
         player.sendMessage( ChatArguments.fromFormatString("{{BLUE}}-----------------------------------------------------") );
 
         if ( player.hasPermission("kingdoms.command.plot.list") ) {
             player.sendMessage(ChatArguments.fromFormatString(Translation.tr("{{YELLOW}}/%0 list", source, args.getCommand())));
             player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{GOLD}}-> {{WHITE}}List all Plots form Kingdoms", source)) );
         }
 
         if ( player.hasPermission("kingdoms.command.plot.info") ) {
             player.sendMessage(ChatArguments.fromFormatString(Translation.tr("{{YELLOW}}/%0 info", source, args.getCommand())));
             player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{GOLD}}-> {{WHITE}}Get the Info about the Plot", source)) );
         }
 
         if ( player.hasPermission("kingdoms.command.plot.create") ) {
 
             if ( player.get(KingdomsComponent.class).getMember().getRank().equals(KingdomRank.LEADER) ||
                  player.get(KingdomsComponent.class).getMember().getRank().equals( KingdomRank.CAPTAIN ) ) {
 
                 player.sendMessage(ChatArguments.fromFormatString(Translation.tr("{{YELLOW}}/%0 create", source, args.getCommand())));
                 player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{GOLD}}-> {{WHITE}}List all Plots form Kingdoms", source)) );
             }
         }
 
         if ( player.hasPermission("kingdoms.command.plot.delete") ) {
 
             if ( player.get(KingdomsComponent.class).getMember().getRank().equals(KingdomRank.LEADER) ||
                     player.get(KingdomsComponent.class).getMember().getRank().equals( KingdomRank.CAPTAIN ) ) {
 
                 player.sendMessage(ChatArguments.fromFormatString(Translation.tr("{{YELLOW}}/%0 delete <plotId>", source, args.getCommand())));
                 player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{GOLD}}-> {{WHITE}}Delete a Plot", source)) );
             }
         }
 
         if ( player.hasPermission("kingdoms.command.plot.setowner") ) {
 
             if ( player.get(KingdomsComponent.class).getMember().getRank().equals(KingdomRank.LEADER) ||
                     player.get(KingdomsComponent.class).getMember().getRank().equals( KingdomRank.CAPTAIN ) ) {
 
                 player.sendMessage(ChatArguments.fromFormatString(Translation.tr("{{YELLOW}}/%0 setowner <plotId> <player>", source, args.getCommand())));
                 player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{GOLD}}-> {{WHITE}}Set a Owner for the Plot", source)) );
             }
         }
 
         source.sendMessage( ChatArguments.fromFormatString("{{BLUE}}-----------------------------------------------------") );
 
     }
 
     @Command(aliases = {"list"}, usage = "", desc = "List all Plots from a Kingdom")
     @CommandPermissions("kingdoms.command.plot.list")
     public void list(CommandContext args, CommandSource source) throws CommandException {
 
         Player player = plugin.getEngine().getPlayer( source.getName(), true );
 
         if ( player == null ) {
             source.sendMessage( ChatArguments.fromFormatString(Translation.tr("You must be a Player!", source)) );
             return;
         }
 
         Member member = memberManager.getMemberByPlayer(player);
         Kingdom kingdom = kingdomManager.getKingdomByPlayer( player );
 
         if ( kingdom == null ) {
             player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}You are not a Member of a Kingdom!", player)) );
             return;
         }
 
         player.sendMessage( ChatArguments.fromFormatString("{{BLUE}}-----------------------------------------------------") );
         player.sendMessage( ChatArguments.fromFormatString("{{YELLOW}}List all Plots of the Kingdom") );
         player.sendMessage( ChatArguments.fromFormatString("{{BLUE}}-----------------------------------------------------") );
 
         player.sendMessage( ChatArguments.fromFormatString("{{YELLOW}}Id - Owner - Size") );
 
         if ( plotManager.getPlotsByKingdom(kingdom) != null ) {
 
             for ( Plot plot : plotManager.getPlotsByKingdom(kingdom) ) {
 
                 String size = "" + (plot.getCornerTwoX() - plot.getCornerOneX()) + " x " + (plot.getCornerTwoZ() - plot.getCornerOneZ()) + " / " + (plot.getCornerTwoY() - plot.getCornerOneY());
 
                 player.sendMessage( ChatArguments.fromFormatString( Translation.tr("{{YELLOW}}%0 - %1 - %2", player, plot.getId(), plot.getOwner(), size) ) );
 
             }
 
         }
 
         player.sendMessage( ChatArguments.fromFormatString("{{BLUE}}-----------------------------------------------------") );
 
     }
 
     @Command(aliases = {"info"}, usage = "", desc = "Get the Info about the Plot")
     @CommandPermissions("kingdoms.command.plot.info")
     public void info(CommandContext args, CommandSource source) throws CommandException {
 
         Player player = plugin.getEngine().getPlayer( source.getName(), true );
 
         if ( player == null ) {
             source.sendMessage( ChatArguments.fromFormatString(Translation.tr("You must be a Player!", source)) );
             return;
         }
 
         Member member = memberManager.getMemberByPlayer(player);
         Plot plot = plotManager.getPlotByPoint( player.getScene().getTransform().getPosition() );
 
         if ( plot != null ) {
 
             player.sendMessage( ChatArguments.fromFormatString("{{BLUE}}-----------------------------------------------------") );
             player.sendMessage( ChatArguments.fromFormatString("{{YELLOW}}Info") );
             player.sendMessage( ChatArguments.fromFormatString("{{BLUE}}-----------------------------------------------------") );
 
             player.sendMessage( ChatArguments.fromFormatString("{{YELLOW}}Id - Owner - Size") );
             player.sendMessage( ChatArguments.fromFormatString( Translation.tr("{{YELLOW}}%0 - %1 - Size", player, plot.getId(), plot.getOwner()) ) );
 
             player.sendMessage( ChatArguments.fromFormatString("{{BLUE}}-----------------------------------------------------") );
 
         } else {
 
             player.sendMessage( ChatArguments.fromFormatString( Translation.tr("{{RED}}There is no Plot at this Place", player) ) );
 
         }
 
     }
 
     @Command(aliases = {"create"}, usage = "", desc = "Create a Plot")
     @CommandPermissions("kingdoms.command.plot.create")
     public void create(CommandContext args, CommandSource source) throws CommandException {
 
         Player player = plugin.getEngine().getPlayer( source.getName(), true );
 
         if ( player == null ) {
             source.sendMessage( ChatArguments.fromFormatString(Translation.tr("You must be a Player!", source)) );
             return;
         }
 
         Member member = memberManager.getMemberByPlayer(player);
         Kingdom kingdom = kingdomManager.getKingdomByPlayer( player );
 
         if ( kingdom == null ) {
             player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}You are not a Member of a Kingdom!", player)) );
             return;
         }
 
         if ( !player.get(KingdomsComponent.class).getMember().getRank().equals(KingdomRank.LEADER) &&
              !player.get(KingdomsComponent.class).getMember().getRank().equals( KingdomRank.CAPTAIN ) ) {
             player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}You need to be a Captain oder Leader to do this!", player)) );
             return;
         }
 
         if ( player.get(SelectionComponent.class).getSelection().getPoint1() == null ) {
             player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}You need to select the first Corner!", player)) );
             return;
         }
 
         if ( player.get(SelectionComponent.class).getSelection().getPoint2() == null ) {
             player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}You need to select the second Corner!", player)) );
             return;
         }
 
         Point cornerOne = player.get(SelectionComponent.class).getSelection().getMinPoint();
         Point cornerTwo = player.get(SelectionComponent.class).getSelection().getMaxPoint();
 
         if ( kingdom.contains(cornerOne) == false ) {
             player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}The first Corner ist not in the Kingdom!", player)) );
             return;
         }
 
         if ( kingdom.contains(cornerTwo) == false ) {
             player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}The second Corner ist not in the Kingdom!", player)) );
             return;
         }
 
         if ( plotManager.getPlotFromKingdomByPoint(kingdom, cornerOne) != null ) {
             player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}The first Corner ist in a other Plot!", player)) );
             return;
         }
 
         if ( plotManager.getPlotFromKingdomByPoint(kingdom, cornerTwo) != null ) {
             player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}The second Corner ist in a other Plot!", player)) );
             return;
         }
 
         plotManager.createPlot(kingdom, cornerOne, cornerTwo);
 
         player.sendMessage( ChatArguments.fromFormatString( Translation.tr("{{GOLD}}The Plot is created!", player) ) );
     }
 
     @Command(aliases = {"setowner"}, usage = "", desc = "Set the Owner of a Plot")
     @CommandPermissions("kingdoms.command.plot.setowner")
     public void setOwner(CommandContext args, CommandSource source) throws CommandException {
 
         Player player = plugin.getEngine().getPlayer( source.getName(), true );
 
         if ( player == null ) {
             source.sendMessage( ChatArguments.fromFormatString(Translation.tr("You must be a Player!", source)) );
             return;
         }
 
         Member member = memberManager.getMemberByPlayer(player);
         Kingdom kingdom = kingdomManager.getKingdomByPlayer( player );
 
         if ( kingdom == null ) {
             player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}You are not a Member of a Kingdom!", player)) );
             return;
         }
 
        if ( !player.get(KingdomsComponent.class).getMember().getRank().equals(KingdomRank.LEADER) &&
              !player.get(KingdomsComponent.class).getMember().getRank().equals( KingdomRank.CAPTAIN ) ) {
             player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}You need to be a Captain oder Leader to do this!", player)) );
             return;
         }
 
         if ( args.length() < 2 ) {
             player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}/plot setowner <plotid> <player>", player)) );
             return;
         }
 
         Plot plot = plotManager.getPlotById( args.getInteger(0) );
 
         if ( plot == null ) {
             player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}There is no Plot with this Id!", player)) );
             return;
         }
 
         Member ownerMember = memberManager.getMemberByName( args.getString(1) );
         Player ownerPlayer = plugin.getEngine().getPlayer( args.getString(1), true );
 
         if ( ownerMember == null ) {
             player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}There is no Player with this Name!", player)) );
             return;
         }
 
         if ( member.getKingdom().equalsIgnoreCase( ownerMember.getKingdom() ) == false ) {
             player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}The Player is not in your Kingdom!", player)) );
             return;
         }
 
         plot.setOwner( ownerMember.getId() );
 
         if ( ownerPlayer.isOnline() ) {
             ownerPlayer.sendMessage( ChatArguments.fromFormatString( Translation.tr("{{GOLD}}You are now Owner of the Plot %0!", ownerPlayer, plot.getId()) ) );
         }
 
         player.sendMessage( ChatArguments.fromFormatString( Translation.tr("{{GOLD}}Owner set", player)) );
     }
 
     @Command(aliases = {"delete"}, usage = "", desc = "Delete a Plot")
     @CommandPermissions("kingdoms.command.plot.delete")
     public void delete(CommandContext args, CommandSource source) throws CommandException {
 
         Player player = plugin.getEngine().getPlayer( source.getName(), true );
 
         if ( player == null ) {
             source.sendMessage( ChatArguments.fromFormatString(Translation.tr("You must be a Player!", source)) );
             return;
         }
 
         Member member = memberManager.getMemberByPlayer(player);
         Kingdom kingdom = kingdomManager.getKingdomByPlayer( player );
 
         if ( kingdom == null ) {
             player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}You are not a Member of a Kingdom!", player)) );
             return;
         }
 
         if ( player.get(KingdomsComponent.class).getMember().getRank().equals(KingdomRank.LEADER) ||
                 player.get(KingdomsComponent.class).getMember().getRank().equals( KingdomRank.CAPTAIN ) ) {
             player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}You need to be a Captain oder Leader to do this!", player)) );
             return;
         }
 
         if ( args.length() == 1 ) {
             player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}/plot delete <plotid>", player)) );
             return;
         }
 
         Plot plot = plotManager.getPlotById( args.getInteger(0) );
 
         if ( plot == null ) {
             player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}There is no Plot with this Id!", player)) );
             return;
         }
 
         plotManager.deletePlot(plot);
         player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}The Plot was deleted!", player)) );
     }
 
 }
