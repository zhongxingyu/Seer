 package com.android.icecave.general;
 
 import com.android.icecave.utils.Point;
 
 import com.android.icecave.R;
 
 public class Consts
 {
 	// Keys
 	public final static String PREFS_FILE_TAG = "prefsFile";
 	public final static String LEVEL_SELECT_TAG = "levelSelect";
 	public final static String PLAYER_SELECT_TAG = "playerSelect";
 	public final static String THEME_SELECT_TAG = "themeSelect";
 	public final static String SELECT_BOARD_SIZE_SIZE = "selectBoardSize";
 	public final static String MUSIC_MUTE_FLAG = "musicMuteFlag";
 	
 	public final static int DEFAULT_PLAYER = R.drawable.default_player;
 	public final static int DEFAULT_TILES = R.drawable.tileset1;
 	public final static Point DEFAULT_START_POS = new Point(1, 1);
 	
 	// Updates
 	public static final int PLAYER_FINISH_MOVE_UPDATE = 0;
 	public static final int LOADING_LEVEL_FINISHED_UPDATE = 1;
 	
 	/***
 	 * Level default values
 	 */
 	// Easy
 	public final static int DEFAULT_WALL_WIDTH = 1;
 	public final static int DEFAULT_BOULDER_RELATION = 7;
 	public final static int DEFAULT_BOARD_SIZE = 20;
 	
 	/***
 	 *  Standard tile values and positioning
 	 */
 	// Player tiles
 	public final static int PLAYER_ERROR_ROW = -1;
 	public final static int PLAYER_DOWN_ROW = 0;
 	public final static int PLAYER_LEFT_ROW = 1;
 	public final static int PLAYER_RIGHT_ROW = 2;
 	public final static int PLAYER_UP_ROW = 3;
 	public final static int PLAYER_MOVING_1 = 0;
 	public final static int PLAYER_STANDING = 1;
 	public final static int PLAYER_MOVING_2 = 2;
 	public final static int PLAYER_MOVEMENTS_SUM = 3; // TODO Might want to change these into an enum
 	
 	// Board tiles
 	public final static Point EMPTY_TILE_IN_SPRITE = new Point(0, 0);
 	public final static Point FLAG_TILE_IN_SPRITE = new Point(1, 0);
 	public final static Point BOULDER_TILE_IN_SPRITE = new Point(2, 0);
 	public final static Point WALL_TILE_IN_SPRITE = new Point(3, 0);
 	
 	// Columns and rows
	public final static int DEFAULT_TILES_BMP_COLUMNS = 4;
	public final static int DEFAULT_TILES_BMP_ROWS = 1;
 	public static final int DEFAULT_PLAYER_BMP_ROWS = 4;
 	public static final int DEFAULT_PLAYER_BMP_COLUMNS = 3;
 }
