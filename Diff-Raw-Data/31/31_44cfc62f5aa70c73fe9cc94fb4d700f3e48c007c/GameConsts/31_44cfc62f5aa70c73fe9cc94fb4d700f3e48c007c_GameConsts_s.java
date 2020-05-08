 package sfs2x.extensions.projectsasha.game;
 
 import sfs2x.extensions.projectsasha.game.entities.software.*;
 
 public class GameConsts 
 {
 	public static final String USER_TABLE 			= "users";
 	public static final boolean DEBUG 				= false;
	
 	public static final int MONEY_UPDATE_TIME		= 5000;
 	public static final int BONUS_MONEY_PER_REGION	= 10;
 	
 	/* Gateway parameters */
 	public static final String[] STARTING_SPOT		= {"iceland","","","",""};
 	
 	public static final String BASE_GATEWAY			= "Base";
 	public static final int BASE_PAYMENT_AMOUNT 	= 5;
 	public static final int BASE_DEFENCE_LEVEL		= 5;
 	public static final int BASE_ATTACK_LEVEL		= 5;
 	
 	public static final String BANK_GATEWAY			= "Bank";
 	public static final int BANK_BASE_PAYMENT_AMOUNT= 20;
 	public static final int BANK_BASE_DEFENCE_LEVEL	= 10;
 	public static final int BANK_BASE_ATTACK_LEVEL	= 5;
 	
 	public static final String EDU_GATEWAY			= "Educational";
 	public static final int EDU_BASE_PAYMENT_AMOUNT	= 5;
 	public static final int EDU_BASE_DEFENCE_LEVEL	= 10;
 	public static final int EDU_BASE_ATTACK_LEVEL	= 5;
 	
 	public static final String GOV_GATEWAY			= "Government";
 	public static final int GOV_BASE_PAYMENT_AMOUNT	= 15;
 	public static final int GOV_BASE_DEFENCE_LEVEL	= 10;
 	public static final int GOV_BASE_ATTACK_LEVEL	= 10;
 	
 	public static final String MIL_GATEWAY			= "Military";
 	public static final int MIL_BASE_PAYMENT_AMOUNT = 10;
 	public static final int MIL_BASE_DEFENCE_LEVEL	= 10;
 	public static final int MIL_BASE_ATTACK_LEVEL	= 15;
 	
 	public static final String SCI_GATEWAY			= "Scientific";
 	public static final int SCI_BASE_PAYMENT_AMOUNT = 10;
 	public static final int SCI_BASE_DEFENCE_LEVEL	= 15;
 	public static final int SCI_BASE_ATTACK_LEVEL	= 5;
 	
 	//***********************//
 
 	public static final int MAX_SOFTWARE_INSTALLED	= 3;
 	
 	/* Defensive Software */
 	public static final String FIREWALL 				= "FIREWALL";
 	public static final String FIREWALL_NAME 			= "FireWool";
 	public static final int FIREWALL_DEFENCE_LEVEL 		= 10;
 	public static final int FIREWALL_MAX_LEVEL			= 3;
 	public static final boolean FIREWALL_CUMULATIVE		= true;
 	
 	public static final String IDS						= "IDS";
 	public static final String IDS_NAME 				= "Prelude Detection System";
 	public static final int IDS_DETECTION 				= 100;
 	public static final int IDS_MAX_LEVEL				= 3;
 	public static final boolean IDS_CUMULATIVE			= false;
 	
 	public static final String LOGCLEANER				= "LOGCLEANER";
 	public static final String LOGCLEANER_NAME 			= "LCleaner";
 	public static final int LOGCLEANER_MAX_LEVEL		= 3;
 	public static final boolean LOGCLEANER_CUMULATIVE	= false;
 	
 	public static final String ANTIVIRUS				= "ANTIVIRUS";
 	public static final String ANTIVIRUS_NAME 			= "Morton Antivirus";
 	public static final int ANTIVIRUS_MAX_LEVEL			= 3;
 	public static final boolean ANTIVIRUS_CUMULATIVE	= false;
 	
 	public static final String VIRUS					= "VIRUS";
 	public static final String VIRUS_NAME 				= "Blaster";
 	public static final int VIRUS_MAX_LEVEL				= 3;
 	public static final boolean VIRUS_CUMULATIVE		= false;
 	
 	public static final String DEEPTHROAT				= "DEEPTHROAT";
 	public static final String DEEPTHROAT_NAME 			= "Deep Throat";
 	public static final int DEEPTHROAT_MAX_LEVEL		= 1;
 	public static final boolean DEEPTHROAT_CUMULATIVE	= false;
 	
 	/* Offensive Software */
 	public static final String SNIFFER					= "SNIFFER";
 	public static final String SNIFFER_NAME 			= "WireBass";
 	public static final int SNIFFER_MAX_LEVEL			= 3;
 	public static final boolean SNIFFER_CUMULATIVE		= false;
 	
 	public static final String BRUTEFORCER				= "BRUTEFORCER";
 	public static final String BRUTEFORCER_NAME 		= "Brutus";
 	public static final int BRUTEFORCER_ATTACK_LEVEL	= 10;
 	public static final int BRUTEFORCER_MAX_LEVEL		= 3;
 	public static final boolean BRUTEFORCER_CUMULATIVE	= true;
 	
 	public static final String DICTIONARY				= "DICTIONARY";
 	public static final String DICTIONARY_NAME 			= "John The Rapper";
 	public static final int DICTIONARY_ATTACK_LEVEL		= 10;
 	public static final int DICTIONARY_MAX_LEVEL		= 3;
 	public static final boolean DICTIONARY_CUMULATIVE	= true;
 	
 	public static final String PROXY					= "PROXY";
 	public static final String PROXY_NAME 				= "Thor Garlic Proxy";
 	public static final int PROXY_ATTACK_LEVEL			= 10;
 	public static final int PROXY_MAX_LEVEL				= 3;
 	public static final boolean PROXY_CUMULATIVE		= false;
 	
 	//***********************//
 	
 	/* Police constants */
 	public static final int IA_THREAD_SLEEP_TIME 			= 50;
 	public static final int DEFAULT_POLICE_RELEVANCE_THRS	= 4;
 	public static final int POLICE_SLEEP_TIME				= 10*20;
 }
