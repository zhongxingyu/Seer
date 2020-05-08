 package com.shval.jnpgame;
 
 import static com.shval.jnpgame.Globals.*;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 
 public class BoardConfig {
 
 	int ROWS;
 	int COLS;
 	int LEVELS;
 	int level;
 	static private final String TAG = BoardConfig.class.getSimpleName();
 	private char cells[][];
 	boolean flipped;
 	
 	// level 1
 	private String levels[][] = { 
 			{ // level 0 - dev playground\
 /*
 			"xxxxxxxxxxxxxx",
 			"x   G4brg  rRx",
 			"xd b gygr    x",
 			"xd   0g1yB  Bx",
 			"xw   yry22   x",
 			"x    rybyB  Yx",
 			"x   Y33Rg    x",
 			"xxxxxxxxxxxxxx",
 			
 			*/
 			"WWW",
 			"R R",
 			"W y",
 			"WWW"
 			
 			},
 			{ // level 1			
 			"xxxxxxxxxxxxxx",
 //			"x            x",
 			"x            x",
 			"x            x",
 			"x            x",
 			"x       r    x",
 			"x      xx    x",
 			"x  g     r b x",
 			"xxbxxxg xxxxxx",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 2
 			"xxxxxxxxxxxxxx",
 			"x            x",
 			"x            x", // 			
 			"x            x",
 			"x            x",
			"x     y   y  x",
 			"x   r r   r  x",
 			"xxxxx x x xxxx",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 3
 			"xxxxxxxxxxxxxx",
 			"x            x",
 			"x            x", //			
 			"x            x",
			"x   by  x y  x",
 			"xxx xxxrxxx  x",
 			"x      b     x",
 			"xxx xxxrxxxxxx",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 4
 			"xxxxxxxxxxxxxx",
 //			"x            x",
 			"x       r    x",
 			"x       b    x",
 			"x       x    x",
 			"x b r        x",
 			"x b r      b x",
 			"xxx x      xxx",
 			"xxxxx xxxxxxxx",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 5
 			"xxxxxxxxxxxxxx",
 			"x            x",
 			"x            x",
 			"xrg  gg      x",
 			"xxx xxxx xx  x",
 			"xrg          x",
 			"xxxxx  xx   xx",
 			"xxxxxx xx  xxx",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 6
 			"xxxxxxxxxxxxxx",
 //			"xxxxxxx      x",
 			"xxxxxxx g    x",
 			"x       xx   x",
 			"x r   b      x",
 			"x x xxx x g  x",
 			"x         x bx",
 			"x       r xxxx",
 			"x   xxxxxxxxxx",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 7
 			"xxxxxxxxxxxxxx",
 			"x            x",
 			"x          r x",
 			"x          w x",
 			"x     b   b  x",
 			"x     w  rr  x",			
 			"x         w  x",
 			"x R  Bw w w  x",
 			"x W  Ww w w  x",			
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 8
 			"xxxxxxxxxxxxxx",
 			"xxxx x  x xxxx",
 			"xxx  g  b  xxx",
 			"xx   W  W   xx",
 			"xx   B  G   xx",
 			"xxg        bxx",
 			"xxxg      bxxx",
 			"xxxx      xxxx",			
 			"xxxxxxxxxxxxxx",
 			"xxxxxxxxxxxxxx",			
 			},
 			{ // level 9		
 			"xxxxxxxxxxxxxx",
 			"x            x",
 			"x            x",
 			"x            x",
 			"x            x",
 			"x          rbx",
 			"x    w     xxx",
 			"xb        ddxx",
 			"xx  Rx  x xxxx",			
 			"xxxxWxxxxxxxxx",
 			},
 			{ // level 10
 			"xxxxxxxxxxxxxx",
 			"x   gr       x",
 			"x   dd D     x",
 			"x    w w xxxxx",
 			"x            x",
 			"x  w  w      x",
 			"x        w  Rx",
 			"xx   w     GWx",
 			"x          Wxx",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 11		
 			"xxxxxxxxxxxxxx",
 			"x      YDDY yx",
 			"x       www xx",
 			"x           yx",
 			"xdd         xx",
 			"xxx          x",
 			"x       y    x",
 			"x   x xxx   Yx",
 			"x   xxxxxx xWx",			
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 12
 			"xxxxxxxxxxxxxx",
 			"xxr rr  rr rxx",
 			"xxx  w  w  xxx",
 			"x            x",
 			"xb          Bx",
 			"xx          Wx",
 			"x            x",
 			"x            x",
 			"x   xxxxxx   x",			
 			"xxxxxxxxxxxxxx",
 			},			
 			{ // level 13
 			"xxxxxxxxxxxxxx",
 			"xxxxxxxxxxxxxx",
 			"xxxxxxxxxxxxxx",
 			"xxxxx yr xxxxx",
 			"xxxxx rb xxxxx",
 			"xxxxx yr xxxxx",
 			"xxxxx by xxxxx",
 			"xxxxxxxxxxxxxx",
 			"xxxxxxxxxxxxxx",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 14
 			"xxxxxxxxxxxxxx",
 			"xxxxxxxxx   rx",
 			"xxxxxxxxx   gx",
 			"xxxxxxxxx   gx",
 			"x2200       gx",
 			"x2200       gx",
 			"x3311      xxx",
 			"x3311      xxx",
 			"xxR x Gxxx xxx",			
 			"xxWxxxWxxxxxxx",
 			},
 			{ // level 15
 			"xxxxxxxxxxxxxx",
 			"xr r r      rx",
 			"xg w w      gx",
 			"xB          bx",
 			"xWxxx     xxxx",
 			"xxxxxx   xxxxx",
 			"xxxxxx   xxxxx",
 			"xxxxxx   xxxxx",
 			"xxxxxWGgGWxxxx",			
 			"xxxxxx   xxxxx",
 			},
 			{ // level 16
 			"xxxxxxxxxxxxxx",
 			"xx   3332100rx",
 			"xx   3422100xx",
 			"xx   444211xxx",
 			"xW     xxxxxxx",
 			"xR     xxxxxxx",
 			"xx     xxxxxxx",
 			"xx     xxxxxxx",
 			"xx     xxxxxxx",			
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 17
 			"xxxxxxxxxxxxxx",
 			"xxxx000xxxgb x",
 			"xxxx0     bg x",
 			"xxxx0    ddxxx",
 			"xxxx000xxxxxxx",
 			"x 111  xxxxWxx",
 			"xxxx     xxGWx",
 			"xxxx   g    Bx",
 			"xxxx   x     x",			
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 18
 			"xxxxxxxxxxxxxx",
 			"x            x",
 			"xb23         x",
 			"xb2yy     y  x",
 			"xb210     ydBx",
 			"xxxxx y   xxWx",
 			"xxxxx yy  xxxx",
 			"xxxxx yyy xxxx",
 			"xxxxx yyyyxxxx",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 19
 			"xxxxxxxxxxxxxx",
 			"xG2    G0Gx  x",
 			"x 3G    0 x  x",
 			"x444    1 x  x",
 			"xG G   gGg   x",
 			"xxx     www  x",
 			"xxx     www  x",
 			"xxx     www  x",
 			"xxx          x",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 20
 			"xxxxxxxxxxxxxx",
 			"xrrrr   rggxxx",
 			"xxxb    xxxxWx",
 			"xxxx       xBx",
 			"xx           x",
 			"xx           x",
 			"xx     x     x",
 			"xx x         x",
 			"xx        x  x",
 			"xxxxxxxxxxxxxx",
 			},
 			
 			};
 
 	public BoardConfig() {
 		LEVELS = levels.length - 1; // zero level doesn't count
 		Gdx.app.debug(TAG, "Number of levels is " + LEVELS);
 	}
 	
 	public void setLevel(int level) {
 		Gdx.app.debug(TAG, "Attempting to define level " + level);
 		if (level > LEVELS || level < 0)
 			level = 0; // ha !!! will you pass this one?
 		
 		this.level = level;
 		String board[] = levels[level];
 		ROWS = board.length;
 		COLS = board[0].length();
 		cells = new char[COLS][ROWS];
 		Gdx.app.debug(TAG, "boardsize (" + COLS + "," + ROWS + ")");
 		transposeBoard(board);
 		Gdx.app.debug(TAG, "Level " + level + " defined");
 	}
 
 	int getLevels() {
 		return LEVELS;
 	}
 	
 	private void transposeBoard(String board[]) {
 		// in libgdx (0,0) is the lower left corner
 		// we'll stick to that
 		
 		// flip 
 		if (flipped) {
 			Gdx.app.error(TAG, "building flipped board");
 			for (int x = 0; x < COLS; x++) {
 				for (int y = 0; y < ROWS; y++) {
 					cells[x][y] = board[y].charAt(x);
 				}
 			}			
 			flipped = false; // flip is done only once
 		}
 		
 		else {
 			for (int x = 0; x < COLS; x++) {
 				for (int y = 0; y < ROWS; y++) {
 					cells[x][ROWS - 1 - y] = board[y].charAt(x);
 				}
 			}
 		}
 	}
 	
 	Texture getTexture(int x, int y) {
 		int type = getType(x, y);
 		return Assets.getTexture(type, level);
 	}
 	
 
 	/*
 	boolean isFixed(int x, int y) {
 		 char cell = cells[x][y];
 		 boolean ret;
 		 switch(cell) {
 		 	case 'r' :
 		 	case 'b' :
 		 	case 'g' :
 		 	case 'y' :
 		 	case 'd' :		 		
 		 		ret = false;
 		 		break;
 		 	default:
 		 		ret = true;
 		 		break;
 		 }
 		 return ret;
 	}	
 	*/
 	
 	int getAncoredTo(int x, int y) {
 		if (x < 0 || y < 0)
 			return getEmergingAnchordTo(-x, -y);
 		char cell = cells[x][y];
 		 		 
 		switch(cell) {
 		 	case 'R' :
 		 	case 'G' :		 		
 		 	case 'B' :		 		
 		 	case 'Y' :
 		 		break;
 		 	default :
 		 		return NONE;
 		 }
 		 
 		 // TODO: for now, anchor to W walls or blacks anchor to blacks
 
 		 if (cells[x][y-1] == 'W')
 			 return DOWN;
 
 		 if (cells[x][y+1] == 'W')
 			 return UP;
 
 		 if (cells[x+1][y] == 'W')
 			 return RIGHT;
 		 
 		 if (cells[x-1][y] == 'W')
 			 return LEFT;
 		 
 		 
 		 if (Cell.isBlack(getType(x-1, y)))
 			 return LEFT;
 
 		 if (Cell.isBlack(getType(x, y+1)))
 			 return UP;
 
 		 if (Cell.isBlack(getType(x+1, y)))
 			 return RIGHT;
 
 		 if (Cell.isBlack(getType(x, y-1)))
 			 return DOWN;
 
 		 // should not be here
 		 Gdx.app.error(TAG, "Could notanchor cell at " + x + ", " + y);
 		 return NONE;
 	}
 	
 	int getBlackType(char blackAscii) {
 		return blackAscii + JELLY_BLACK_MIN;
 	}
 	
 	int getType(int x, int y) {
 		 if (x < 0 || y < 0) {
 			 return getEmergingType(-x, -y);
 		 }
 		 char cell = cells[x][y];
 		 int ret;
 		 switch(cell) {
 		 	case 'W' :
 		 	case 'w' :		 		
 		 	case 'x' :		 		
 		 		ret = WALL;
 		 		break;
 		 	case 'r' :
 		 	case 'R' :
 		 		ret = JELLY_RED;
 		 		break;
 		 	case 'b' :
 		 	case 'B' :
 		 		ret = JELLY_BLUE;
 		 		break;
 		 	case 'g' :
 		 	case 'G' :
 		 		ret = JELLY_GREEN;
 		 		break;
 		 	case 'y' :
 		 	case 'Y' :
 		 		ret = JELLY_YELLOW;
 		 		break;		 		
 		 	case 'd' :
 		 	case 'D' :		 		
 		 	case '0' :
 		 	case '1' :
 		 	case '2' :
 		 	case '3' :
 		 	case '4' :
 		 	case '5' :
 		 	case '6' :
 		 	case '7' :
 		 	case '8' :
 		 	case '9' :
 		 		ret = getBlackType(cell);
 		 		break;
 		 	default :
 		 		ret = NONE;
 		 		break;
 		 }
 		 return ret;
 	}
 
 	private int getEmergingType(int x, int y) {
 		
 		if (level == 0) {
 			if (x == 1 && y == 0)
 				return JELLY_YELLOW;
 		}
 
		if (level == 11) {
 			if (x == 10 && y == 1)
 				return JELLY_RED;
 			if (x == 13 && y == 2)
 				return JELLY_BLUE;
 			if (x == 0 && y == 2)
 				return JELLY_GREEN;
 		}
 
 		return NONE;
 	}
 	
 	private int getEmergingAnchordTo(int x, int y) {
 
 		if (level == 0) {
 			if (x == 1 && y == 0)
 				return DOWN;
 		}
		if (level == 11) {
 			if (x == 10 && y == 1)
 				return DOWN;
 			if (x == 13 && y == 2)
 				return RIGHT;
 			if (x == 0 && y == 2)
 				return LEFT;
 		}
 
 		return NONE;
 
 	}
 
 	int getEmergingTo(int x, int y) {
 		x = -x;
 		y = -y;
 		if (level == 0) {
 			if (x == 1 && y == 0)
 				return UP;
 			
 		}
		if (level == 11) {
 			if (x == 10 && y == 1)
 				return UP;
 			if (x == 13 && y == 2)
 				return LEFT;
 			if (x == 0 && y == 2)
 				return RIGHT;
 		}
 		return NONE;
 	}
 	
 	Sprite getEmergingSprite(int x, int y) {
 	
 		int to = getEmergingTo(x, y);
 		int type = getEmergingType(-x, -y);
 		int anchoredTo = getEmergingAnchordTo(-x, -y);
 		
 		if (type == NONE)
 			return null;
 
 		int dx = 0;
 		switch(type) {
 		case JELLY_BLUE:
 			dx = 1 * 48;
 			break;
 		case JELLY_GREEN:
 			dx = 2 * 48;
 			break;
 		case JELLY_YELLOW:
 			dx = 3 * 48;
 			break;
 			
 		}
 
 		int dy = 0;
 		int dw = 0;
 		if (anchoredTo != NONE) {
 			dy = 48;
 			dx -= 16;
 			dw = 16;
 		}
 		
 		int xP = 28 + dx;
 		int yP = 9 + dy;
 		int wP = 8 + dw;
 		int hP = 46;
 		Sprite sp = new Sprite(Assets.getEmergingTexture(), xP, yP, wP, hP);
 		if (to == UP || to == LEFT)
 			sp.flip(true, true);
 		return sp;
 	}
 	
 	public Texture getResetButtonsTexture() {
 		return Assets.getButtonsTexture();
 	}
 
 	
 	// rocky mountains
 	private void addHighMountLayer(Background background) {
 		int height = (int) (256 * 0.5);
 		int width = (int) (256 * 3 / 8);
 		
 		background.addLayer(Assets.getBgTexture(1), 0 * width, 0, width, height);
 		background.addLayer(Assets.getBgTexture(1), 1 * width, 0, width, height);
 		background.addLayer(Assets.getBgTexture(1), 2 * width, 0, width, height);
 	}
 
 	private void addLowMountLayer(Background background) {
 		int height = (int) (256 * 0.4);
 		int width = (int) (256 * 3 / 10);
 		
 		background.addLayer(Assets.getBgTexture(1), 0 * width, 0, width, height);
 		background.addLayer(Assets.getBgTexture(1), 1 * width, 0, width, height);
 		background.addLayer(Assets.getBgTexture(1), 2 * width, 0, width, height);
 		background.addLayer(Assets.getBgTexture(1), 3 * width, 0, width, height);
 	}
 
 	private void addGreenMountLayer(Background background) {
 	
 		//background.addLayer(texture, x, y, vX, vY, wrapX, wrapY, width, height)
 		float xSpeed = -3f;
 		float overlap = 2;
 		int height = 96;
 		background.addLayer(Assets.getBgTexture(22), 0 * (64 - overlap), 0, xSpeed, 0, 54, 0, 64, height);
 		background.addLayer(Assets.getBgTexture(22), 1 * (64 - overlap), 0, xSpeed, 0, 54, 0, 64, height);
 		background.addLayer(Assets.getBgTexture(22), 2 * (64 - overlap), 0, xSpeed, 0, 54, 0, 64, height);
 		background.addLayer(Assets.getBgTexture(22), 3 * (64 - overlap), 0, xSpeed, 0, 54, 0, 64, height);
 		background.addLayer(Assets.getBgTexture(22), 4 * (64 - overlap), 0, xSpeed, 0, 54, 0, 64, height);
 	}	
 
 	private void addGrayMountLayer(Background background) {
 		
 		//background.addLayer(texture, x, y, vX, vY, wrapX, wrapY, width, height)
 		float xSpeed = -3f;
 		int overlap = 2;
 		int height = 96;
 		background.addLayer(Assets.getBgTexture(52), 0 * (64 - overlap), 0, xSpeed, 0, 54, 0, 64, height);
 		background.addLayer(Assets.getBgTexture(52), 1 * (64 - overlap), 0, xSpeed, 0, 54, 0, 64, height);
 		background.addLayer(Assets.getBgTexture(52), 2 * (64 - overlap), 0, xSpeed, 0, 54, 0, 64, height);
 		background.addLayer(Assets.getBgTexture(52), 3 * (64 - overlap), 0, xSpeed, 0, 54, 0, 64, height);
 		background.addLayer(Assets.getBgTexture(52), 4 * (64 - overlap), 0, xSpeed, 0, 54, 0, 64, height);
 	}	
 
 
 	private void addGrassLayer(Background background) {
 		
 		//background.addLayer(texture, x, y, vX, vY, wrapX, wrapY, width, height)
 		float xSpeed = -10f;
 		int overlap = 2;	
 		background.addLayer(Assets.getBgTexture(21), 0 * (96 - overlap), 0, xSpeed, 0, 120, 0, 96, 64);
 		background.addLayer(Assets.getBgTexture(21), 1 * (96 - overlap), 0, xSpeed, 0, 120, 0, 96, 64);
 		background.addLayer(Assets.getBgTexture(21), 2 * (96 - overlap), 0, xSpeed, 0, 120, 0, 96, 64);
 		background.addLayer(Assets.getBgTexture(21), 3 * (96 - overlap) - 256 - 120, 0, xSpeed, 0, 120, 0, 96, 64);
 	}
 
 	private void addSnowLayer(Background background) {
 		
 		//background.addLayer(texture, x, y, vX, vY, wrapX, wrapY, width, height)
 		float xSpeed = -10f;
 		int overlap = 2;	
 		background.addLayer(Assets.getBgTexture(51), 0 * (96 - overlap), 0, xSpeed, 0, 120, 0, 96, 64);
 		background.addLayer(Assets.getBgTexture(51), 1 * (96 - overlap), 0, xSpeed, 0, 120, 0, 96, 64);
 		background.addLayer(Assets.getBgTexture(51), 2 * (96 - overlap), 0, xSpeed, 0, 120, 0, 96, 64);
 		background.addLayer(Assets.getBgTexture(51), 3 * (96 - overlap) - 256 - 120, 0, xSpeed, 0, 120, 0, 96, 64);
 	}
 	
 	private void addStars(Background background) {
 		int stars = (int) ( 32 * (Math.random() + 2));
 		for (int i=0 ; i < stars; i++) {
 			int wrapX = (int) (16 * (1 + Math.random()));
 			int wrapY = (int) (16 * (1 + Math.random()));
 			float x = (float) (((double) (256 + wrapX)) * Math.random());
 			float y = (float) (((double) (256 + wrapY)) * Math.random());
 			int textureWidth = 16;
 			int textureHeight = 16;
 			float moment = (float) (75 * (4 + Math.random()));
 			float vX = -8f;
 			float vY = -32f;
 			int width = 4;
 			int height = 4;
 			background.addSprite(Assets.getSmallStarTexture(), x, y, textureWidth, textureHeight , vX, vY, wrapX, wrapY, width, height, moment);
 		}
 	}
 	
 	public Background getBackground() {
 		Background background = new Background();
 		
 		// background.color.set(Color.blue); TODO: why doesnt it work !!!		
 		switch (level) {
 		case 1:
 		case 2:
 		case 3:
 		case 4:
 			// blue sky + clouds
 			background.color.set(0.5f, 0.7f, 1f, 0);
 			
 			addHighMountLayer(background);
 			
 			// clouds
 			background.addLayer(Assets.getBgTexture(0), 0, 86, -15, 0, 128, 0, 100, 128);
 			background.addLayer(Assets.getBgTexture(0), 200, 86, -15, 0, 128, 0, 100, 128);
 			break;
 			
 		case 5:
 		case 6:
 		case 7:
 		case 8:
 		case 13:			
 		case 14:
 		case 15:
 		case 16:
 			// stars in the night
 			background.color.set(0.0f, 0.0f, 0f, 0);
 			addStars(background);
 			addLowMountLayer(background);			
 			break;
 
 		case 9:
 		case 10:
 		case 11:
 		case 12:
 			// bald alien on grass
 
 			background.color.set(0.5f, 0.7f, 1f, 0);
 			addGreenMountLayer(background);
 			
 			// bald thing
 			background.addLayer(Assets.getBgTexture(30), 0 * 64, 32, -5, 0, 64, 0, 96, 128);
 			background.addLayer(Assets.getBgTexture(30), 3 * 64, 32, -5, 0, 64, 0, 96, 128);
 
 			addGrassLayer(background);
 				
 			break;
 			
 		case 17:
 		case 18:
 		case 19:
 		case 20:			
 			// bald alien on snow
 
 			background.color.set(0.5f, 0.7f, 1f, 0);
 			
 			addGrayMountLayer(background);			
 						
 			// bald thing
 			background.addLayer(Assets.getBgTexture(30), 0 * 64, 32, -5, 0, 128, 0, 96, 128);
 			background.addLayer(Assets.getBgTexture(30), 2 * 64, 32, -5, 0, 128, 0, 96, 128);
 			background.addLayer(Assets.getBgTexture(30), 4 * 64, 32, -5, 0, 128, 0, 96, 128);
 
 			addSnowLayer(background);
 			
 			break;
 			
 		default:
 			
 		}
 		return background;
 	}
 
 	public Sound getSound(int soundID) {
 		return Assets.getSound(soundID);
 
 	}
 
 	public float getSoundVolume() {
 		return 0.5f; // in [0,1], TODO: get it ftom user
 	}
 
 	public int getLevel() {
 		return level;
 	}
 
 	public boolean firstLevel() {
 		return (level == 1);
 	}
 
 	public boolean lastLevel() {
 		return (level == LEVELS);
 	}
 	
 	public void setFlipped() {
 		if (level <= 20) // this will make thing simpler with emerging cells
 			flipped = ! flipped;
 	}
 
 }
