 import java.io.*;
 import java.util.Properties;
 import java.util.logging.*;
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import com.vexsoftware.votifier.Votifier;
 import com.vexsoftware.votifier.model.*;
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.economy.EconomyResponse;
 import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
 
 
 /**
  * VaultListener is a listener class for Votifier 1.4 using Vault as a common API to various economy plugins and allows
  * admins to reward their players with virtual cash for voting. The original intent of this class was to provide missing
  * iConomy 6 support to Votifier; however, by using Vault it has the capabilities of using iConomy 4, iConomy 5, iConomy 6,
  * BOSEconomy 6, BOSEconomy 7, EssentialEcon, 3Co, and MultiConomy. For more economy options, please see the Vault plugin on
  * BukkitDev - http://dev.bukkit.org/server-mods/vault/.
  * 
  * For installation and configuration, please see the accompanying README file.
  * 
  * The methodology and approach used herein is largely based upon the original iConomyListener written by Blake Beaupin.
  * VaultListener improves thereupon by incorporating the Vault API, customized/colored messages, additional fault tolerances
  * and checks, and file resource optimizations to improve the general stability and reportability of this listener.
  * 
  * @author frelling
  * 
  */
 public class VaultListener implements VoteListener {
	private static String	version		= "1.1.4";
 	private static Logger	logger		= Logger.getLogger( "VaultListener" );
 	private static String	PROP_FILE	= "VaultListener.properties";
 	private static String	VL_ID		= "[Votifier][VaultListener " + version + "]";
 
 	// Reward amount
 	private static String	PK_AMT		= "reward_amount";
 	private static String	DEF_AMT		= "30.00";
 	private double			amount		= 30.0;
 	private double			paid		= amount;
 
 	// Reward adornment
 	private static String	PK_PREFIX	= "reward_prefix";
 	private String			prefix		= "";
 
 	private static String	PK_SUFFIX	= "reward_suffix";
 	private String			suffix		= "";
 
 	// Reward type
 	private static String	PK_TYPE		= "reward_type";
 	private static String	TYPE_FIXED	= "fixed";
 	private static String	TYPE_RATE	= "rate";
 	private boolean	isRate				= false;
 
 	// Reward rate
 	private static String	PK_RATE		= "reward_rate";
 	private static String	DEF_RATE	= "0.01";
 	private double			rate		= 0.01;
 
 	// Vote confirmation message
 	private static String	PK_VMSG		= "confirm_msg";
 	private String			confirmMsg	= "Thanks {IGN}, for voting on {SERVICE}!";
 
 	// Payment confirmation message
 	private static String	PK_PMSG		= "payment_msg";
 	private String			paymentMsg	= "{AMOUNT} has been added to your {ECONOMY} balance.";
 
 	// Debug Flag
 	private static String	PK_DEBUG	= "debug";
 	private boolean			debug		= false;
 
 	// Broadcast message
 	private static String	PK_BCAST	= "broadcast";
 	private boolean			bCastFlag	= true;
 	private static String	PK_BCASTMSG	= "broadcast_msg";
 	private String			bCastMsg	= "The server was voted for by {IGN}!";
 
 	private Economy			econ		= null;
 	private Votifier		plugin		= null;
 
 
 	/**
 	 * Constructor - Initialize properties and economy API
 	 */
 	public VaultListener() {
 		plugin = Votifier.getInstance();
 		if ( plugin != null ) {
 			initializeProperties();
 			initializeEconomyAPI();
 		}
 		else
 			log( Level.SEVERE, "Cannot find reference to Votifier plugin!" );
 	}
 
 
 	/**
 	 * Initialize VaultListener properties. Property file is expected to reside in Votifier's data directory. If not found, a
 	 * default property file is generated.
 	 */
 	private void initializeProperties() {
 		Properties props = new Properties();
 		File propFile = new File( plugin.getDataFolder(), PROP_FILE );
 
 		if ( propFile.exists() ) {
 			/*
 			 * Read property file if found.
 			 */
 			try {
 				FileReader freader = new FileReader( propFile );
 				props.load( freader );
 				freader.close();
 			}
 			catch ( IOException ex ) {
 				log( Level.WARNING,
 						"Error loading VaultListener properties. Using default messages and reward of "
 								+ DEF_AMT );
 			}
 		}
 		else {
 			/*
 			 * Create property file if it wasn't found.
 			 */
 			logInfo( "No VaultListener properties file found, creating default file." );
 			try {
 				propFile.createNewFile();
 				props.setProperty( PK_AMT, Double.toString( amount ) );
 				props.setProperty( PK_TYPE, TYPE_FIXED );
 				props.setProperty( PK_RATE, Double.toString( rate ) );
 				props.setProperty( PK_VMSG, confirmMsg );
 				props.setProperty( PK_PMSG, paymentMsg );
 				props.setProperty( PK_BCAST, "" + bCastFlag );
 				props.setProperty( PK_BCASTMSG, bCastMsg );
 				props.setProperty( PK_PREFIX, prefix );
 				props.setProperty( PK_SUFFIX, suffix );
 				props.setProperty( PK_DEBUG, "" + debug );
 
 				FileWriter fwriter = new FileWriter( propFile );
 				props.store( fwriter, "Vault Listener Properties" );
 				fwriter.close();
 
 			}
 			catch ( IOException ex ) {
 				log( Level.WARNING, "Error creating VaultListener properties." );
 			}
 		}
 
 		// Read reward amount. Use default amount if illegal number.
 		try {
 			amount = Double.parseDouble( props.getProperty( PK_AMT, DEF_AMT ) );
 		}
 		catch ( NumberFormatException ex ) {
 			amount = Double.parseDouble( DEF_AMT );
 			log( Level.WARNING, "Illegal reward_amount! Using default reward of " + DEF_AMT );
 		}
 
 		isRate = props.getProperty( PK_TYPE, TYPE_FIXED ).toLowerCase().equals( TYPE_RATE );
 
 		// Read reward rate. Use default rate if illegal number.
 		if ( isRate ) {
 			try {
 				rate = Double.parseDouble( props.getProperty( PK_RATE, DEF_RATE ) );
 			}
 			catch ( NumberFormatException ex ) {
 				rate = Double.parseDouble( DEF_RATE );
 				log( Level.WARNING, "Illegal reward_rate! Using default rate of " + DEF_RATE );
 			}
 		}
 
 		prefix = props.getProperty( PK_PREFIX, prefix );
 		suffix = props.getProperty( PK_SUFFIX, suffix );
 		confirmMsg = props.getProperty( PK_VMSG, confirmMsg );
 		paymentMsg = props.getProperty( PK_PMSG, paymentMsg );
 		debug = Boolean.parseBoolean( props.getProperty( PK_DEBUG, "false" ) );
 		bCastFlag = Boolean.parseBoolean( props.getProperty( PK_BCAST, "true" ) );
 		bCastMsg = props.getProperty( PK_BCASTMSG, bCastMsg );
 	}
 
 
 	/**
 	 * Initialize economy API. Note: Because this listener is just a simple class and has no control over how Votifier loads,
 	 * it is impossible to specify a soft/hard dependency to ensure that Vault loads before Votifier. Fortunately, Bukkit's
 	 * two pass class loading approach should take care of this.
 	 */
 	private void initializeEconomyAPI() {
 		RegisteredServiceProvider<Economy> economyProvider = null;
 		try {
 			economyProvider = plugin.getServer().getServicesManager()
 					.getRegistration( net.milkbowl.vault.economy.Economy.class );
 
 			if ( economyProvider != null ) {
 				econ = economyProvider.getProvider();
 				logInfo( "Using economy plugin: " + econ.getName() );
 			}
 			else {
 				econ = null;
 				log( Level.WARNING,
 						"Vault cannot detect a valid economy plugin. No payments will be made!" );
 			}
 		}
 		catch ( NoClassDefFoundError ex ) {
 			log( Level.SEVERE,
 					"Could not find Vault API. Please make sure Vault is installed and enabled!" );
 		}
 	}
 
 
 	@Override
 	public void voteMade( Vote vote ) {
 		String ign = vote.getUsername();
 		logInfo( ign );
 
 		if ( debug ) {
 			voteRecordDump( vote );
 			configDump();
 		}
 
 		// Try to pay vote IGN
 		if ( econ != null ) {
 			logDebug( "Using " + econ.getName() + " to pay IGN -> " + ign );
 
 			/*
 			 * If reward_type is 'rate' calculate percentage of player's balance. If it is less than the fixed amount, pay
 			 * the fixed amount instead.
 			 */
 			if ( isRate ) {
 				double balance = econ.getBalance( ign );
 				logDebug( "IGN balance (if 0.00, player may not have economy account): " + balance );
 
 				paid = balance * rate;
 				logDebug( "Calculated reward: " + paid );
 
 				if ( paid < amount ) {
 					paid = amount;
 					logDebug( "Calculated reward less than fixed amount. Paying fixed amount: " + paid );
 				}
 			}
 			else {
 				paid = amount;
 				logDebug( "Paying fixed amount: " + paid );
 			}
 
 			paid = Math.round( 100.0 * paid ) / 100.0;
 			EconomyResponse eres = econ.depositPlayer( ign, paid );
 			if ( eres.type == ResponseType.FAILURE )
 				logInfo( eres.errorMessage );
 		}
 		else {
 			paid = 0;
 			logDebug( "No economy plugin found" );
 		}
 
 		Player player = plugin.getServer().getPlayerExact( ign );
 
 		// Thank player, if online
 		if ( player != null ) {
 			sendMessage( player, vote, confirmMsg );
 			logDebug( "Found online player -> " + player.getName() );
 			for ( String s : insertTokenData( vote, confirmMsg ) )
 				logDebug( "Confirmation message -> " + s );
 			if ( econ != null ) {
 				sendMessage( player, vote, paymentMsg );
 				for ( String s : insertTokenData( vote, paymentMsg ) )
 					logDebug( "Payment message -> " + s );
 			}
 			else
 				logDebug( "No economy plugin found. No payment message sent." );
 		}
 		else
 			logDebug( "No online player found for -> " + ign );
 
 		if ( bCastFlag ) {
 			broadcastMessage( plugin.getServer(), vote, bCastMsg );
 			for ( String s : insertTokenData( vote, bCastMsg ) )
 				logDebug( "Broadcast message -> " + s );
 		}
 		else
 			logDebug( "Broadcast disabled. No broadcast message sent." );
 	}
 
 
 	private void log( Level lvl, String msg ) {
 		logger.log( lvl, VL_ID + " " + msg );
 	}
 
 
 	private void logInfo( String msg ) {
 		log( Level.INFO, msg );
 	}
 
 
 	private void logDebug( String msg ) {
 		if ( debug )
 			logInfo( msg );
 	}
 
 	
 	private void sendMessage( Player player, Vote vote, String msg ) {
 		for ( String s : insertTokenData( vote, msg ) )
 			player.sendMessage( s );
 	}
 	
 	private void broadcastMessage( Server server, Vote vote, String msg ) {
 		for ( String s : insertTokenData( vote, msg ) )
 			server.broadcastMessage( s );
 	}
 
 	/*
 	 * Replace token values in given string with actual data and split resulting string in
 	 * multi-lines.
 	 */
 	private String[] insertTokenData( Vote vote, String str ) {
 		String msg = str.replace( "{SERVICE}", vote.getServiceName() );
 		msg = msg.replace( "{IGN}", vote.getUsername() );
 		msg = msg.replace( "{AMOUNT}", prefix + Double.toString( paid ) + suffix );
 		msg = msg.replace( "{ECONOMY}", (econ != null) ? econ.getName()
 				: "UNKNOWN" );
 		msg = msg.replaceAll( "(?i)&([0-9A-FK-OR])", "\u00A7$1" );
 		return msg.split( "\n" );
 	}
 
 
 	private void voteRecordDump( Vote vote ) {
 		logInfo( "Vote notification received. Vote record dump..." );
 		logInfo( "Voting service name -> " + vote.getServiceName() );
 		logInfo( "Voting service address -> " + vote.getAddress() );
 		logInfo( "Voting IGN -> " + vote.getUsername() );
 		logInfo( "Voting timestamp -> " + vote.getTimeStamp() );
 	}
 
 
 	private void configDump() {
 		logInfo( PK_AMT + " -> " + Double.toString( amount ) );
 		logInfo( PK_TYPE + " -> " + (isRate ? TYPE_RATE : TYPE_FIXED) );
 		logInfo( PK_RATE + " -> " + Double.toString( rate ) );
 		logInfo( PK_VMSG + " -> " + confirmMsg );
 		logInfo( PK_PMSG + " -> " + paymentMsg );
 		logInfo( PK_BCAST + " -> " + bCastFlag );
 		logInfo( PK_BCASTMSG + " -> " + bCastMsg );
 		logInfo( PK_PREFIX + " -> " + prefix );
 		logInfo( PK_SUFFIX + " -> " + suffix );
 		logInfo( PK_DEBUG + " -> " + debug );
 	}
 
 }
