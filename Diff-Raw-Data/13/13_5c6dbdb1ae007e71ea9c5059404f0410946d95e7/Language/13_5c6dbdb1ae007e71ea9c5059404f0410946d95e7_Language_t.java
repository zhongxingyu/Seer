 package com.wolvencraft.prison.mines.settings;
 
 import com.wolvencraft.prison.mines.PrisonMine;
 
 public class Language extends com.wolvencraft.prison.settings.Language {
 	
 	public final String GENERAL_TITLE;
 	public final String GENERAL_SUCCESS;
 	public final String GENERAL_ERROR;
 	
 	public final String ERROR_MINENAME;
 	public final String ERROR_MINENOTSELECTED;
 	public final String ERROR_FUCKIGNNOOB;
 	
 	public final String MINE_SELECTED;
 	public final String MINE_DESELECTED;
 	
 	public final String RESET_MANUAL;
 	public final String RESET_WARNING;
 	public final String RESET_AUTOMATIC;
 	public final String RESET_TIMED;
 	public final String RESET_COMPOSITION;
 	public final String RESET_COOLDOWN;
 	
 	public final String SIGN_TITLE;
 	public final String SIGN_WITHDRAW;
	public final String SIGN_FUNDS;
 	
 	public final String MISC_TELEPORT;
 	
 	public Language(PrisonMine plugin) {
 		super(PrisonMine.getPrisonSuite());
 		GENERAL_TITLE = plugin.getLanguageData().getString("general.title");
 		GENERAL_SUCCESS = plugin.getLanguageData().getString("general.title-success");
 		GENERAL_ERROR = plugin.getLanguageData().getString("general.title-error");
 		
 		ERROR_MINENAME = plugin.getLanguageData().getString("error.mine-name");
 		ERROR_MINENOTSELECTED = plugin.getLanguageData().getString("error.mine-not-selected");
 		ERROR_FUCKIGNNOOB = plugin.getLanguageData().getString("error.removing-air");
 
 		MINE_SELECTED = plugin.getLanguageData().getString("editing.mine-selected-successfully");
 		MINE_DESELECTED = plugin.getLanguageData().getString("editing.mine-deselected-successfully");
 		
 		RESET_MANUAL = plugin.getLanguageData().getString("reset.manual-reset-successful");
 		RESET_WARNING = plugin.getLanguageData().getString("reset.automatic-reset-warning");
 		RESET_AUTOMATIC = plugin.getLanguageData().getString("reset.automatic-reset-successful");
 		RESET_TIMED = plugin.getLanguageData().getString("reset.timed-reset-successful");
 		RESET_COMPOSITION = plugin.getLanguageData().getString("reset.composition-reset-successful");
 		RESET_COOLDOWN = plugin.getLanguageData().getString("reset.mine-cooldown");
 		
 		SIGN_TITLE = plugin.getLanguageData().getString("sign.title");
 		SIGN_WITHDRAW = plugin.getLanguageData().getString("sign.withdraw");
		SIGN_FUNDS = plugin.getLanguageData().getString("sign.funds");
 		
 		MISC_TELEPORT = plugin.getLanguageData().getString("misc.mine-teleport");
 	}
 }
