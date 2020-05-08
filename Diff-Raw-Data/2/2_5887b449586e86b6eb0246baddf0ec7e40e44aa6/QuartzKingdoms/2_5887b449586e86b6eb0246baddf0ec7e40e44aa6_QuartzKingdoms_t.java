 package uk.co.quartzcraft.kingdoms;
 
 import java.sql.Connection;
 import java.util.Arrays;
 import java.util.logging.Logger;
 
 import com.sun.tools.javac.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import uk.co.quartzcraft.core.QuartzCore;
 import uk.co.quartzcraft.core.chat.ChatPhrase;
 import uk.co.quartzcraft.core.database.MySQL;
 import uk.co.quartzcraft.kingdoms.command.*;
 import uk.co.quartzcraft.kingdoms.listeners.*; 
 
 public class QuartzKingdoms extends JavaPlugin {
 	
 	public Plugin plugin = this.plugin;
 	
 	public static final Logger log = Logger.getLogger("Minecraft");
 	
 	public static String releaseVersion = QuartzCore.displayReleaseVersion();
 
 	public static Connection DBKing = null;
 	public static MySQL MySQLking = null;
 	
 	@Override
 	public void onDisable() {
 				
     	//Shutdown notice
 		log.info("[QK]The QuartzKingdoms Plugin has been disabled!");
 	}
 	
 	@Override
 	public void onEnable() {
 		
 		log.info("[QC]Running plugin configuration");
 		this.saveDefaultConfig();
 		
 		String SQLKingHost = this.getConfig().getString("database.kingdoms.host");
 		String SQLKingDatabase = this.getConfig().getString("database.kingdoms.database");
 		String SQLKingUser = this.getConfig().getString("database.kingdoms.username");
 		String SQLKingPassword = this.getConfig().getString("database.kingdoms.password");
 		MySQLking = new MySQL(plugin, SQLKingHost, "3306", SQLKingDatabase, SQLKingUser, SQLKingPassword);
 		
 		//Phrases
 		log.info("[QK][STARTUP]Creating Phrases");
 		ChatPhrase.addPhrase("kingdom_name_single_word", "&cA kingdoms name may only be a single word!");
 		ChatPhrase.addPhrase("created_kingdom_yes", "&aSuccessfully created kingdom: ");
 		ChatPhrase.addPhrase("created_kingdom_no", "&cFailed to create kingdom: ");
 		ChatPhrase.addPhrase("deleted_kingdom_yes", "&aSuccessfully deleted kingdom: ");
 		ChatPhrase.addPhrase("deleted_kingdom_no", "&cFailed to delete kingdom: ");
 		ChatPhrase.addPhrase("specify_kingdom_name", "&cPlease specify a name!");
 		ChatPhrase.addPhrase("kingdomname_already_used", "&cAnother kingdom is using that name! &aConsider using a different name and overtaking that kingdom!");
 		ChatPhrase.addPhrase("info_kingdom", "&bInfo on Kingdom: ");
 		
 		ChatPhrase.addPhrase("chunk_claimed_for_kingdom_yes", "&aChunk successfully claimed for Kingdom: ");
 		ChatPhrase.addPhrase("chunk_claimed_for_kingdom_no", "&aChunk was not successfully claimed for Kingdom: ");
 		ChatPhrase.addPhrase("chunk_unclaimed_for_kingdom_yes", "&aChunk successfully unclaimed for Kingdom: ");
 		ChatPhrase.addPhrase("chunk_unclaimed_for_kingdom_no", "&aChunk was not successfully unclaimed for Kingdom: ");
 		ChatPhrase.addPhrase("now_entering_the_land_of", "&aNow entering the land of ");
 		ChatPhrase.addPhrase("now_leaving_the_land_of", "&aNow entering the land of ");
 		
 		ChatPhrase.addPhrase("got_promoted_kingdom_yes", "&aYou were moved group by your king!");
 
         ChatPhrase.addPhrase("you_must_be_member_kingdom", "&cYou must be a member of a kingdom!");
         ChatPhrase.addPhrase("you_must_be_member_kingdom_leave", "&cYou must be a member of a kingdom to leave one!");
         ChatPhrase.addPhrase("you_are_already_in_a_Kingdom", "&cYou are already a member of a kingdom!");
 		ChatPhrase.addPhrase("successfully_joined_kingdom_X", "&aSuccessfully joined the kingdom ");
 		ChatPhrase.addPhrase("failed_join_kingdom", "&cFailed to join the specified kingdom. Please check that it is not invite only.");
 		ChatPhrase.addPhrase("successfully_left_kingdom_X", "&aSuccessfully left the kingdom ");
 		ChatPhrase.addPhrase("failed_leave_kingdom", "&cFailed to leave the specified kingdom.");
 
         ChatPhrase.addPhrase("kingdom_already_open", " &cThe kingdom is already open!");
         ChatPhrase.addPhrase("kingdom_now_open", " &aYour kingdom is now open!");
         ChatPhrase.addPhrase("failed_open_kingdom", " &cFailed to open the kingdom!");
         ChatPhrase.addPhrase("kingdom_already_closed", " &cThe kingdom is already closed!");
         ChatPhrase.addPhrase("kingdom_now_closed", " &aYour kingdom is now closed!");
         ChatPhrase.addPhrase("failed_close_kingdom", " &cFailed to close the kingdom!");
 
 		ChatPhrase.addPhrase("kingdom_is_now_at_war_with_kingdom", " &cis now at war with ");
 		ChatPhrase.addPhrase("kingdom_is_now_allied_with_kingdom", " &ais now allied with ");
 		ChatPhrase.addPhrase("kingdom_is_now_neutral_relationship_with_kingdom", " &6is now in a neutral relationship with ");
 		ChatPhrase.addPhrase("failed_to_ally_with_kingdom", "&cFailed to become an ally with ");
 		ChatPhrase.addPhrase("failed_to_neutral_with_kingdom", "&cFailed to become neutral with ");
 		ChatPhrase.addPhrase("failed_to_war_with_kingdom", "&cFailed to go to war with ");
 		
		ChatPhrase.addPhrase("could_not_create_kingdoms_player", "&cYour player data could not be added to the QuartzKingdoms database!");
 		
 		//Database
 		//logger.info("[STARTUP]Connecting to Database");
 		DBKing = MySQLking.openConnection();
 		
 		//Listeners
 		log.info("[QK][STARTUP]Registering Listeners");
 		//getServer().getPluginManager().registerEvents(new BlockListener(), this);
 		new BlockListener(this);
 		new PlayerCreationListener(this);
 		new PlayerMoveListener(this);
 		
 		//Commands
 		log.info("[QK][STARTUP]Registering Commands");
 		getCommand("kingdom").setExecutor(new CommandKingdom());
 		CommandKingdom.addCommand(Arrays.asList("info"), new KingdomInfoSubCommand());
 		CommandKingdom.addCommand(Arrays.asList("create"), new KingdomCreateSubCommand());
 		CommandKingdom.addCommand(Arrays.asList("delete"), new KingdomDeleteSubCommand());
 		CommandKingdom.addCommand(Arrays.asList("promote"), new KingdomPromoteSubCommand());
 		CommandKingdom.addCommand(Arrays.asList("claim"), new KingdomClaimSubCommand());
 		CommandKingdom.addCommand(Arrays.asList("unclaim"), new KingdomUnClaimSubCommand());
 		CommandKingdom.addCommand(Arrays.asList("war"), new KingdomWarSubCommand());
 		CommandKingdom.addCommand(Arrays.asList("ally"), new KingdomAllySubCommand());
 		CommandKingdom.addCommand(Arrays.asList("neutral"), new KingdomNeutralSubCommand());
 		CommandKingdom.addCommand(Arrays.asList("join"), new KingdomJoinSubCommand());
 		CommandKingdom.addCommand(Arrays.asList("leave"), new KingdomJoinSubCommand());
         CommandKingdom.addCommand(Arrays.asList("open"), new KingdomOpenSubCommand());
         CommandKingdom.addCommand(Arrays.asList("close"), new KingdomOpenSubCommand());
 	   	
         //Startup notice
 		log.info("[QK]The QuartzKingdoms Plugin has been enabled!");
 		log.info("[QK]Compiled using QuartzCore version " + releaseVersion);
 	}
 
 }
