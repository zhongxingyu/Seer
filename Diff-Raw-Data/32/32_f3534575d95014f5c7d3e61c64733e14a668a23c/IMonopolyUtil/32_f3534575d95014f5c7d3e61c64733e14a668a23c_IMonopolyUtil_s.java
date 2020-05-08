 package de.htwg.monopoly.util;
 
 /**
  * Interface to avoid magic numbers
  * @author RuprechtT
  *
  */
 public interface IMonopolyUtil {
 	
 	
 	/**
 	 * CONFIGURATIONS
 	 */
 	int FIELD_SIZE = 10;
 	
 	
 	/**
 	 *  defines to avoid magic numbers 
 	 *  */
 	int NULL = 0;
 	int ONE = 1;
 	int DICE = 6;
 	int FIRST_PLAYER = 0;
 	int MIN_NUMBER_OF_PLAYER = 2;
 	int MAX_NUMBER_OF_PLAYER = 6;
 	int INITIAL_MONEY = 6000;
 	int MAX_PRISON_ROUND = 4;
 	int LOS_MONEY = 2000;
 
 	int ZUSATZSTEUER = 1000;
 	int TUI_FIELD_SIZE = 28;
 	int TUI_HIGH = 5;
 	int FREIKAUFEN = 500;
 	int OPTION_FINISH = 3;
 
 	
 	/***
 	 * Magic number for Tests
 	 */
 	int TEST_PRICE_ONE = 250;
 	int TEST_PRICE_TWO = 230;
 	int TEST_PRICE_THREE = 50;
 	int TEST_PLAYFIELD_SIZE = 3;
 
 	/* Strings */
 	String GAME_NAME = "HTWG Monopoly";
 	String Q_NUMBER_OF_PLAYER = "Bitte Anzahl der Spieler eingeben: ";
 	String Q_NAME_PLAYER = "bitte Name eingeben: ";
 	String START = "Los gehts!";
 	
 	String ERR_NUMBER_OF_PLAYER = "Ungltige Anzahl von Spieler eingegeben (2 - 6)";
 	String ERR_NAME_OF_PLAYER = "Kann das passieren?";
 	String TEST_INPUT_STREAM = "2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n2\n";
 	
 	/* GUI */
 	int COLORSIZE = 20;
 	int WEST = 270;
 	int SOUTH = 180;
 	int EAST = 90;
 
 
 	int MAX_NUMBER_OF_STEPS = 5;
 
 	
 
 }
