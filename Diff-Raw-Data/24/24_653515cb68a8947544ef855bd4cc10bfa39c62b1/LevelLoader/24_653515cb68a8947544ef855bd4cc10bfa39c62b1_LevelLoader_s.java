 package com.detourgames.raw;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.Texture;
 
 public class LevelLoader {
 
 	/*
 	 * A class for accessing level files, parsing them, loading them, and
 	 * returning the contents of a level to the GameManager.
 	 * 
 	 * It should only need a few methods: createLevel(levelnumber), //should
 	 * possibly be called in the constructor getLevel(), and getLevelItem()
 	 * (like getTiles() or getEnemies()).
 	 */
 
 	Level mLevel;
 	SpriteFactory mSpriteFactory;
 	SpriteSheet mSpriteSheet;
 
 	int levelWidth = 0;
 	int levelHeight = 0;
 	int sprites = 0;
 
 	Tile[] tiles;
 
 	public LevelLoader(Level level) {
 		mLevel = level;
 		mSpriteFactory = new SpriteFactory(mLevel);
 		Texture texture = new Texture(Gdx.files.internal("sprite_tiles.png"));
 		mSpriteSheet = new SpriteSheet(texture, 3, new int[]{3,1,5}, new int[]{8,8,4,0,7,16,16,16,16}, new int[]{128,1024,64}, new int[]{128,320,64});
 		// if(lvln==0){
 		// createRandomTileMap(); TODO
 		// }else{
 		// createLevelFromFile(lvln, level);
 		// mSpriteFactory.createHero(2, 3);
 		// }
 
 		// sprites += HUD.HUD_SPRITES;
 	}
 	
 	public void createLevelFromFile(int ln) {
 		FileHandle fileId = getFileHandle(ln);
 		// int[][] pieceInfo = null;
 		// int[][] grid = new int[1][1];
 		levelWidth = 0;
 		levelHeight = 0;
 		// int[][][] levelInfo = null;
 		String line1 = null;
 		String line4 = null;
 		String line5 = null;
 		// String line6 = null;
 
 		try {
 			InputStream inputStream = fileId.read();
 			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream), inputStream.toString().length());
 			
 			String line = in.readLine();
 			int x = 1;
 			while (line != null) {
 				if(x==1){
 					line1 = line;
 				}else if(x==4){
 					line4 = line;
 				}else if(x==5){
 					line5 = line;
 				}/*else if(x==6){
 					line6 = read;
 				}*/
 				line = in.readLine();
 				x++;
 			}
 		} catch (Exception e) {
 			// Log.d("ERROR-readingLevelFile", "Could not read file: " +
 			// e.getLocalizedMessage());
 		}
 
 		if (line1 != null) {
 			String[] str = line1.split("[ ]");
 			levelWidth = Integer.parseInt(str[0]);
 			levelHeight = Integer.parseInt(str[1]);
 			// grid = new int[levelHeight][levelWidth];
 			/*
 			 * for(int y=0;y<levelHeight;y++){ for(int x=0;x<levelWidth;x++){
 			 * grid[y][x] = 0; } }
 			 */// Not necessary
 		}
 
 		// tiles = new Tile[levelHeight][levelWidth];
 
 		if (line4 != null) {
 			String[] str = line4.split("[ ]");
 			int[] gridArr = new int[(levelWidth * levelHeight)];
 			if (str.length != gridArr.length) {
 				// Log.d("Levels",
 				// "Not correctly parsing level file. Check split() method");
 			}
 			// int tile;
 			for (int x = 0; x < gridArr.length; x++) {
 				gridArr[x] = Integer.parseInt(str[x]);
 			}
 
 			sprites = 0;
 			int i = 0;
 			Tile[] ph_tiles = new Tile[gridArr.length];
 			for(int y=levelHeight-1;y>-1;y--){
 				for(int x=0;x<levelWidth;x++){
 					if(gridArr[i]!=0){
 						//ph_tiles[sprites] = createTile(gridArr[i], x, y, level);
 						Animation animation = new Animation();
 						animation.createFrame(mSpriteSheet, Animation.convertTileFrame(gridArr[i])-1);
						mSpriteFactory.createTile((float)x*32f, (float)y*32f, 0, animation);
 						sprites++;
 					}
 					i++;
 				}
 			}
 			
 			tiles = new Tile[sprites];
 			for(int i2 = 0;i2<sprites;i2++){
 				tiles[i2] = ph_tiles[i2];
 			}
 			ph_tiles = null;
 
 		}
 
 	}
 
 	private FileHandle getFileHandle(int ln) {
 		FileHandle levelID = null;
 
 		if (ln == 1) {
 			levelID = Gdx.files.internal("levels/rawtestlevel1");
 		}
 
 		return levelID;
 	}
 
 }
