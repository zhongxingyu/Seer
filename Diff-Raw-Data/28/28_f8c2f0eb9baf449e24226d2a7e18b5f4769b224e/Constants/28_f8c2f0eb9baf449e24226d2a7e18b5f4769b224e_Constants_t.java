 package com.cn.constants;
 
 public class Constants {
 	public static String CONNECT = "connect";
 	public static String DISCONNECT = "disconnect";
 	public static String ATTACK = "attack";
 	public static String ATTACK_MONSTER = "attackMonster";
 	public static String HEAL = "heal";
 	public static String REST = "rest";
 	public static String GET_HEALTH = "getHealth";
 	public static String GET_ENERGY = "getEnergy";
 	public static String GET_MONSTER = "getMonsters";
 	public static String DEATH = "death";
 	public static String LOOT = "loot";
 	public static String LOGIN = "login";
 	public static String PASSWORD = "password";
 	public static String CHARACTER = "character";
 	public static String LOGIN_NAME = "loginName";
 	public static String SAVE = "save";
 	public static String LOGGER_PROPERTIES = "dep/log4j.properties";
 	public static String CREATE_ACC = "createAcc";
 
 	public static String MONSTER_DOES_NOT_EXIST = "monsterDNE";
 	public static String MONSTER_WAS_KILLED = "monsterWasKilled";
 	public static String CANNOT_LOOT_MONSTER = "unableToLootMonster";
 	public static String SUCCESS = "success";
 
 	public static String NO_CHARS_CREATED = "noCharsCreated";
 	public static String USER_NOT_FOUND = "noUsersFound";
 	
 	public static final String NOT_ENOUGH_ENERGY = "notEnoughEnergy";
	public static final String LOGOUT = "logout";
 
 	
 	/**
 	 * Formats the message to a more desirable output within the server.
 	 * 
 	 * @param msg
 	 * @return formatted msg
 	 */
 	public static String formatOutput(String msg) {
 		return "\n==========================================\n"
 			                       +msg+"\n"
 			   + "==========================================\n";
 	}
 }
