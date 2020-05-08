 package com.android.icecave.general;
 
 import android.graphics.Point;
 import com.android.icecave.R;
 
 public class Consts
 {
 	public final static String PREFS_FILE_TAG = "prefsFile";
 	public final static String LEVEL_SELECT_TAG = "levelSelect";
 	public final static String PLAYER_SELECT_TAG = "playerSelect";
 	public final static String THEME_SELECT = "themeSelect";
 	public final static int DEFAULT_PLAYER = R.drawable.default_player;
 	public final static int DEFAULT_TILES = R.drawable.default_tiles;
	public final static Point DEFAULT_START_POS = new Point(1, 1);
	public final static int DEFAULT_WALL_WIDTH = 1;
 	public final static int DEFAULT_BOULDER_NUM = 30;
 	public final static int DEFAULT_BOARD_SIZE_X = 10;
 	public final static int DEFAULT_BOARD_SIZE_Y = 10;
 	public final static int DEFAULT_TILES_BMP_COLUMNS = 6;
 	public final static int DEFAULT_TILES_BMP_ROWS = 3;
 	public static final int DEFAULT_PLAYER_BMP_ROWS = 4;
 	public static final int DEFAULT_PLAYER_BMP_COLUMNS = 3;
 	public final static Point EMPTY_TILE_IN_SPRITE = new Point(4, 0);
 	public final static Point FLAG_TILE_IN_SPRITE = new Point(3, 0);
 	public final static Point WALL_TILE_IN_SPRITE = new Point(4, 2);
 	public final static Point BOULDER_TILE_IN_SPRITE = new Point(3, 1);
 }
