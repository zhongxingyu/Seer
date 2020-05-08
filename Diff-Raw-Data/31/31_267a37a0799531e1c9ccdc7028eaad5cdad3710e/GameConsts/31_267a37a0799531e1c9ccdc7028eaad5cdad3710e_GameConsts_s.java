 package sfs2x.extensions.projectsasha.game;
 
 import sfs2x.extensions.projectsasha.game.entities.software.BruteForcer;
 import sfs2x.extensions.projectsasha.game.entities.software.Firewall;
 import sfs2x.extensions.projectsasha.game.entities.software.Software;
 
 public class GameConsts 
 {
 	public static final String USER_TABLE 			= "users";
 	public static final boolean TEST 				= true;
 	
 	public static final int MAX_SOFTWARE_INSTALLED	= 3;
 	
 	public static final String FIREWALL_NAME 		= "FireWool";
 	public static final int FIREWALL_DEFENCE_LEVEL 	= 10;
 	public static final int FIREWALL_MAX_LEVEL		= 3;
 
 	public static final String BRUTEFORCER_NAME 	= "Brutus";
 	public static final int BRUTEFORCER_ATTACK_LEVEL= 10;
 	public static final int BRUTEFORCER_MAX_LEVEL	= 3;
 	
 	public static final int BANK_BASE_PAYMENT_AMOUNT= 20;
 	public static final int BANK_BASE_DEFENCE_LEVEL	= 10;
 	public static final int BANK_BASE_ATTACK_LEVEL	= 5;
 	
 	public static final int EDU_BASE_PAYMENT_AMOUNT	= 5;
 	public static final int EDU_BASE_DEFENCE_LEVEL	= 10;
 	public static final int EDU_BASE_ATTACK_LEVEL	= 5;
 	
 	public static final int GOV_BASE_PAYMENT_AMOUNT= 15;
 	public static final int GOV_BASE_DEFENCE_LEVEL	= 10;
 	public static final int GOV_BASE_ATTACK_LEVEL	= 10;
 	
 	public static final int MIL_BASE_PAYMENT_AMOUNT = 10;
 	public static final int MIL_BASE_DEFENCE_LEVEL	= 10;
 	public static final int MIL_BASE_ATTACK_LEVEL	= 15;
 	
 	public static final int SCI_BASE_PAYMENT_AMOUNT = 10;
 	public static final int SCI_BASE_DEFENCE_LEVEL	= 15;
 	public static final int SCI_BASE_ATTACK_LEVEL	= 5;
 	
 	public static final Software FIREWALL 			= new Firewall();
 	public static final Software BRUTEFORCER		= new BruteForcer();
 	
 }
