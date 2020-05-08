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
		GENERAL_TITLE = Promote.getLanguageData().getString("general.title");
		GENERAL_SUCCESS = Promote.getLanguageData().getString("general.title-success");
		GENERAL_ERROR = Promote.getLanguageData().getString("general.title-error");
 		
		ERROR_COMMAND = Promote.getLanguageData().getString("error.command");
		ERROR_ARGUMENTS = Promote.getLanguageData().getString("error.arguments");
		ERROR_ACCESS = Promote.getLanguageData().getString("error.access");
		ERROR_SENDERISNOTPLAYER = Promote.getLanguageData().getString("error.sender-is-not-player");
		ERROR_RANK_HIGHEST = Promote.getLanguageData().getString("error.highest-rank-possible");
		ERROR_RANK_NOT_IN_LADDER = Promote.getLanguageData().getString("error.not-in-ladder");
		ERROR_NO_MONEY = Promote.getLanguageData().getString("error.not-enough-money");
 		
		RANKS_PROMOTED = Promote.getLanguageData().getString("ranks.promoted");
 	}
 }
