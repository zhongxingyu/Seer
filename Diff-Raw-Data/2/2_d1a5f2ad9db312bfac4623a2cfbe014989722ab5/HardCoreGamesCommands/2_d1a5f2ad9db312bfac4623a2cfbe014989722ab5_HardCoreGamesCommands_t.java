 package ch.minepvp.spout.hardcoregames.commands;
 
 import ch.minepvp.spout.hardcoregames.Game;
 import ch.minepvp.spout.hardcoregames.config.GameDifficulty;
 import ch.minepvp.spout.hardcoregames.config.GameSize;
 import ch.minepvp.spout.hardcoregames.config.GameStatus;
 import ch.minepvp.spout.hardcoregames.manager.GameManager;
 import ch.minepvp.spout.hardcoregames.HardCoreGames;
 import org.spout.api.chat.ChatArguments;
 import org.spout.api.chat.style.ChatStyle;
 import org.spout.api.command.CommandContext;
 import org.spout.api.command.CommandSource;
 import org.spout.api.command.annotated.Command;
 import org.spout.api.command.annotated.CommandPermissions;
 import org.spout.api.entity.Player;
 import org.spout.api.exception.CommandException;
 import org.spout.api.lang.Translation;
 
 public class HardCoreGamesCommands {
 
     private final HardCoreGames plugin;
     private GameManager gameManager;
 
     public HardCoreGamesCommands( HardCoreGames instance ) {
         plugin = instance;
         gameManager = plugin.getGameManager();
     }
 
     @Command(aliases = {"help"}, usage = "", desc = "List all Messages.")
     @CommandPermissions("hcg.help")
     public void help(CommandContext args, CommandSource source) throws CommandException {
 
         source.sendMessage( ChatStyle.BLUE, "-----------------------------------------------------" );
         source.sendMessage( ChatStyle.YELLOW, "Help" );
         source.sendMessage( ChatStyle.BLUE, "-----------------------------------------------------" );
 
         if ( source.hasPermission("hcg.create") ) {
             source.sendMessage( ChatStyle.YELLOW, "/game create <easy|normal|hard|hardcore> <tiny|small|medium|big>" );
             source.sendMessage( ChatArguments.fromFormatString(Translation.tr("Create a new HardCore Game", source)) );
         }
 
         if ( source.hasPermission("hcg.list") ) {
             source.sendMessage( ChatStyle.YELLOW, "/game list" );
             source.sendMessage( ChatArguments.fromFormatString( Translation.tr("List all running Games", source) ) );
         }
 
         if ( source.hasPermission("hcg.join") ) {
             source.sendMessage( ChatStyle.YELLOW, "/game join <player>" );
             source.sendMessage( ChatArguments.fromFormatString( Translation.tr("Join a Game over a Player", source) ) );
         }
 
         if ( source.hasPermission("hcg.leave") ) {
             source.sendMessage( ChatStyle.YELLOW, "/game leave" );
             source.sendMessage( ChatArguments.fromFormatString( Translation.tr("Leave the Game", source) ) );
         }
 
         if ( source.hasPermission("hcg.start") ) {
             source.sendMessage( ChatStyle.YELLOW, "/game start" );
             source.sendMessage( ChatArguments.fromFormatString( Translation.tr("Start the Game", source) ) );
         }
 
         source.sendMessage( ChatStyle.BLUE, "-----------------------------------------------------" );
 
     }
 
     @Command(aliases = {"create"}, usage = "", desc = "Create a new Game")
     @CommandPermissions("hcg.create")
     public void create(CommandContext args, CommandSource source) throws CommandException {
 
         Player player = plugin.getEngine().getPlayer( source.getName(), true );
 
         if ( player == null ) {
             source.sendMessage( ChatArguments.fromFormatString(Translation.tr("You must be a Player!", source)) );
             return;
         }
 
         GameDifficulty difficutly = null;
 
         if ( args.getString(0).equalsIgnoreCase("easy") ) {
             difficutly = GameDifficulty.EASY;
         } else if ( args.getString(0).equalsIgnoreCase("normal") ) {
             difficutly = GameDifficulty.NORMAL;
         } else if ( args.getString(0).equalsIgnoreCase("hard") ) {
             difficutly = GameDifficulty.HARD;
         } else if ( args.getString(0).equalsIgnoreCase("hardcore") ) {
             difficutly = GameDifficulty.HARDCORE;
         } else {
             difficutly = GameDifficulty.NORMAL;
         }
 
         GameSize size = null;
 
         if ( args.getString(1).equalsIgnoreCase("tiny") ) {
             size = GameSize.TINY;
         } else if ( args.getString(1).equalsIgnoreCase("small") ) {
             size = GameSize.SMALL;
         } else if ( args.getString(1).equalsIgnoreCase("medium") ) {
             size = GameSize.MEDIUM;
         } else if ( args.getString(1).equalsIgnoreCase("big") ) {
             size = GameSize.BIG;
         } else {
             size = GameSize.MEDIUM;
         }
 
         Game game = new Game( player, difficutly, size );
         gameManager.addGame(game);
 
         source.sendMessage( ChatArguments.fromFormatString( Translation.tr("{{GOLD}}The Game is createt other Players can now Join the Game!", source) ) );
 
     }
 
     @Command(aliases = {"list"}, usage = "", desc = "")
     @CommandPermissions("hcg.list")
     public void list(CommandContext args, CommandSource source) throws CommandException {
 
         Player player = plugin.getEngine().getPlayer( source.getName(), true );
 
         if ( player == null ) {
             source.sendMessage( ChatArguments.fromFormatString(Translation.tr("You must be a Player!", source)) );
             return;
         }
 
         source.sendMessage( ChatStyle.BLUE, "-----------------------------------------------------" );
         source.sendMessage( ChatStyle.YELLOW, "List all Games" );
         source.sendMessage( ChatStyle.BLUE, "-----------------------------------------------------" );
 
         for ( Game game : gameManager.getGames() ) {
 
             source.sendMessage( ChatArguments.fromFormatString( Translation.tr("{{WHITE}}Owner : {{GOLD}}%0 {{WHITE}}Status : {{GOLD}}%1", source, game.getOwner().getName(), game.getStatus() ) ) );
             source.sendMessage( ChatArguments.fromFormatString( Translation.tr("{{WHITE}}Settings : {{WHITE}}Difficulty {{GOLD}}%0 {{WHITE}}Size {{GOLD}}%1", source, game.getDifficulty(), game.getSize() ) ) );
 
             String players = "";
 
             for ( Player player2 : game.getPlayers() ) {
 
                 players += player2.getName() + " ";
 
             }
 
             source.sendMessage( ChatArguments.fromFormatString( Translation.tr("{{WHITE}}Players : {{GOLD}}%0", source, players ) ) );
             source.sendMessage( ChatStyle.BLUE, "-----------------------------------------------------" );
         }
 
     }
 
     @Command(aliases = {"join"}, usage = "", desc = "")
     @CommandPermissions("hcg.join")
     public void join(CommandContext args, CommandSource source) throws CommandException {
 
         Player player = plugin.getEngine().getPlayer( source.getName(), true );
 
         if ( player == null ) {
             source.sendMessage( ChatArguments.fromFormatString(Translation.tr("You must be a Player!", source)) );
             return;
         }
 
         if ( gameManager.getGameByPlayer( player ) != null ) {
             source.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}You are allready in a Game!", source)) );
             return;
         }
 
         Player player2 = plugin.getEngine().getPlayer( args.getString(0), true );
 
         if (  player2 == null ) {
             source.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}The Player was not found!", source)) );
             return;
         }
 
         Game game = gameManager.getGameByPlayer( player2 );
 
         if ( game == null ) {
             source.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}The Player is not in a Game!!", source)) );
             return;
         }
 
         game.addPlayer( player );
 
         player.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{GOLD}}You joined the Game!", player)) );
 
         for ( Player toPlayer : game.getPlayers() ) {
            toPlayer.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{GOLD}}%0 has joined the Game!!", toPlayer, player.getName())) );
         }
 
     }
 
     @Command(aliases = {"leave"}, usage = "", desc = "")
     @CommandPermissions("hcg.leave")
     public void leave(CommandContext args, CommandSource source) throws CommandException {
 
         Player player = plugin.getEngine().getPlayer( source.getName(), true );
 
         if ( player == null ) {
             source.sendMessage( ChatArguments.fromFormatString(Translation.tr("You must be a Player!", source)) );
             return;
         }
 
         Game game = gameManager.getGameByPlayer( player );
 
         if ( game == null ) {
             source.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}You are not in a Game!", source)) );
             return;
         }
 
         game.removePlayer(player);
 
         if ( game.getPlayers().size() == 0 ) {
             gameManager.removeGame(game);
         }
 
     }
 
     @Command(aliases = {"start"}, usage = "", desc = "")
     @CommandPermissions("hcg.start")
     public void start(CommandContext args, CommandSource source) throws CommandException {
 
         Player player = plugin.getEngine().getPlayer( source.getName(), true );
 
         if ( player == null ) {
             source.sendMessage( ChatArguments.fromFormatString(Translation.tr("You must be a Player!", source)) );
             return;
         }
 
         Game game = gameManager.getGameByPlayer( player );
 
         if ( game == null ) {
             source.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}You are not in a Game!", source)) );
             return;
         }
 
         if ( game.getPlayers().size() == 1 ) {
             source.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}You are alone in the Game!", source)) );
             return;
         }
 
         if ( game.getStatus().equals( GameStatus.RUNNING ) ) {
             source.sendMessage( ChatArguments.fromFormatString(Translation.tr("{{RED}}The Game is allready running!", source)) );
             return;
         }
 
         game.startGame();
     }
 
     @Command(aliases = {"test"}, usage = "", desc = "")
     @CommandPermissions("hcg.test")
     public void test(CommandContext args, CommandSource source) throws CommandException {
 
         Player player = plugin.getEngine().getPlayer( source.getName(), true );
 
         Game game = new Game(player, GameDifficulty.EASY, GameSize.TINY);
         gameManager.addGame(game);
         game.startGame();
 
 
     }
 
     @Command(aliases = {"world"}, usage = "", desc = "")
     @CommandPermissions("hcg.world")
     public void world(CommandContext args, CommandSource source) throws CommandException {
 
         Player player = plugin.getEngine().getPlayer( source.getName(), true );
 
         player.sendMessage( "You are in World : " + player.getWorld().getName() );
         player.sendMessage( "Chunk : X " + player.getChunk().getX() + " Y " + player.getChunk().getY() + " Z " + player.getChunk().getZ() );
 
     }
 
 }
