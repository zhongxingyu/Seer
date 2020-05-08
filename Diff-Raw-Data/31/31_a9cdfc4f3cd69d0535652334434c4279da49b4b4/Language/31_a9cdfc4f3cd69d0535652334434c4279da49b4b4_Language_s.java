 package com.wolvencraft.promote;
 
 
 public class Language {
 	
 	public final String GENERAL_TITLE;
 	public final String GENERAL_SUCCESS;
 	public final String GENERAL_ERROR;
 	
 	public final String ERROR_COMMAND;
 	public final String ERROR_ARGUMENTS;
 	public final String ERROR_ACCESS;
 	public final String ERROR_SENDERISNOTPLAYER;
 	public final String ERROR_RANK_HIGHEST;
 	public final String ERROR_RANK_NOT_IN_LADDER;
 	public final String ERROR_NO_MONEY;
 
 	public final String RANKS_PROMOTED;
 	
 	
 	public Language(Promote plugin) {
		GENERAL_TITLE = plugin.getLanguageData().getString("general.title");
		GENERAL_SUCCESS = plugin.getLanguageData().getString("general.title-success");
		GENERAL_ERROR = plugin.getLanguageData().getString("general.title-error");
 		
		ERROR_COMMAND = plugin.getLanguageData().getString("error.command");
		ERROR_ARGUMENTS = plugin.getLanguageData().getString("error.arguments");
		ERROR_ACCESS = plugin.getLanguageData().getString("error.access");
		ERROR_SENDERISNOTPLAYER = plugin.getLanguageData().getString("error.sender-is-not-player");
		ERROR_RANK_HIGHEST = plugin.getLanguageData().getString("error.highest-rank-possible");
		ERROR_RANK_NOT_IN_LADDER = plugin.getLanguageData().getString("error.not-in-ladder");
		ERROR_NO_MONEY = plugin.getLanguageData().getString("error.not-enough-money");
 		
		RANKS_PROMOTED = plugin.getLanguageData().getString("ranks.promoted");
 	}
 }
