 package edu.mines.csci598.recycler.frontend.utils;
 
 import edu.mines.csci598.recycler.frontend.RecyclableType;
 import edu.mines.csci598.recycler.frontend.RecycleBin;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Marshall
  * Date: 10/28/12
  * Time: 2:15 PM
  * To change this template use File | Settings | File Templates.
  */
 public final class GameConstants {
 	/**
 	 * The number of item types which are generated at the start of the game
 	 */
     public static final int INITIAL_NUMBER_OF_ITEM_TYPES = 1;
     /**
      *  The total number of item types available in the game
      */
     public static final int TOTAL_NUMBER_OF_ITEM_TYPES = 4; // TODO this needs to be 5 eventually
     /**
      *  The minimum amount of time between object generations, at the beginning of the game
      */
     public static final double INITIAL_ITEM_GENERATION_DELAY_SECONDS = 3;
 
     /**
      * If an item is not generated, this is the minimum delay before it can be attempted again.  As item
      * generation possibly happens many times per second, this keeps a non-generation from still showing
      * up immediately as far as the users are concerned.
      */
     public static final double ITEM_GENERATION_DELAY = 0.25;
     /**
      * The probability, at the start of the game, that an item will be generated
      */
     public static final double START_ITEM_GENERATION_PROB = 0.5;
 
     // TODO these three should become an enum, so that ADD_SPRITE != BOTTOM_PATH_START_X
     public static final int ADD_SPRITE = 0;
     public static final int REMOVE_SPRITE = 1;
     public static final int UPDATE_SPRITES = 2;
 
     public static final int BOTTOM_PATH_START_X = 0;
     public static final int BOTTOM_PATH_START_Y = 880;
     public static final int BOTTOM_PATH_END_X = 445;
     public static final int BOTTOM_PATH_END_Y = BOTTOM_PATH_START_Y;
     public static final double BOTTOM_PATH_START_TIME = 5;
     public static final double BOTTOM_PATH_END_TIME = 5;
 
     public static final int VERTICAL_PATH_START_X = BOTTOM_PATH_END_X;
     public static final int VERTICAL_PATH_START_Y = BOTTOM_PATH_END_Y;
     public static final int VERTICAL_PATH_END_X = BOTTOM_PATH_END_X;
     public static final int VERTICAL_PATH_END_Y = 40;
     public static final double VERTICAL_PATH_START_TIME = 15;
     public static final double VERTICAL_PATH_END_TIME = 1.5;
 
     public static final int TOP_PATH_START_X = VERTICAL_PATH_END_X;
     public static final int TOP_PATH_START_Y = VERTICAL_PATH_END_Y;
     public static final int TOP_PATH_END_X = 750;
     public static final int TOP_PATH_END_Y = VERTICAL_PATH_END_Y;
     public static final double TOP_PATH_START_TIME = BOTTOM_PATH_START_TIME;
     public static final double TOP_PATH_END_TIME = BOTTOM_PATH_END_TIME;
 
     public static final int ITEM_PATH_END = 250;
     public static final int ITEM_PATH_TIME = 5;
 
     public static final int SPRITE_BECOMES_TOUCHABLE = BOTTOM_PATH_END_Y - 50;
     public static final int SPRITE_BECOMES_UNTOUCHABLE = TOP_PATH_START_Y + 20;
     public static final int HORIZONTAL_VELOCITY = 2;
 
     public static final int MIN_HAND_VELOCITY = 10;
     public static final int SPRITE_X_OFFSET = 50;
     public static final int SPRITE_Y_OFFSET = 50;
 
     public static final int IN_BIN_OFFSET = 150;
 
     public static final RecyclableType BIN_1_TYPE = RecyclableType.GLASS;
     public static final RecycleBin.ConveyorSide BIN_1_SIDE = RecycleBin.ConveyorSide.LEFT;
     public static final double BIN_1_MIN_Y = 438;
     public static final double BIN_1_MAX_Y = 838;
 
     public static final RecyclableType BIN_2_TYPE = RecyclableType.PAPER;
     public static final RecycleBin.ConveyorSide BIN_2_SIDE = RecycleBin.ConveyorSide.LEFT;
     public static final double BIN_2_MIN_Y = 40;
     public static final double BIN_2_MAX_Y = 440;
 
     public static final RecyclableType BIN_3_TYPE = RecyclableType.PLASTIC;
     public static final RecycleBin.ConveyorSide BIN_3_SIDE = RecycleBin.ConveyorSide.RIGHT;
     public static final double BIN_3_MIN_Y = 140;
     public static final double BIN_3_MAX_Y = 540;
 
     public static final RecyclableType BIN_4_TYPE = RecyclableType.PLASTIC;
     public static final RecycleBin.ConveyorSide BIN_4_SIDE = RecycleBin.ConveyorSide.RIGHT;
     public static final double BIN_4_MIN_Y = 538;
     public static final double BIN_4_MAX_Y = 938;

    public static final RecyclableType BIN_5_TYPE = RecyclableType.TRASH;
    public static final RecycleBin.ConveyorSide BIN_5_SIDE = RecycleBin.ConveyorSide.RIGHT;
    public static final double BIN_5_MIN_Y = 0;
    public static final double BIN_5_MAX_Y = 140;


     
     /**
      * The amount of time, in seconds from the game start, that it takes before a second item is added to the game
      */
 	public static final int ITEM_TYPE_2_ACTIVATION_TIME = 15;
     /**
      * The amount of time, in seconds from the game start, that it takes before a third item is added to the game
      */
 	public static final int ITEM_TYPE_3_ACTIVATION_TIME = 30;
     /**
      * The amount of time, in seconds from the game start, that it takes before a fourth item is added to the game
      */
 	public static final int ITEM_TYPE_4_ACTIVATION_TIME = 10000; // TODO reduce this when we have a reasonable graphic
 	/**
 	 * The amount of time it takes from the start of the game, in seconds, to reach its maximum difficulty
 	 */
 	public static final int TIME_TO_MAX_DIFFICULTY = 300;
 
 }
 
