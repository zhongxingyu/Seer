 package no.progark.a18.towerdefence.level;
 
 import org.andengine.engine.handler.IUpdateHandler;
 import org.andengine.entity.IEntity;
 import org.andengine.entity.scene.background.Background;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.opengl.font.Font;
 import org.andengine.opengl.font.FontFactory;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.ui.activity.BaseGameActivity;
 import org.andengine.util.color.Color;
 
 import android.util.Log;
 
 import no.progark.a18.towerdefence.R;
 import no.progark.a18.towerdefence.TowerDefenceActivity;
 import no.progark.a18.towerdefence.gameContent.Cell;
 import no.progark.a18.towerdefence.gameContent.Direction;
 import no.progark.a18.towerdefence.gameContent.Creep;
 import no.progark.a18.towerdefence.gameContent.KillListener;
 import no.progark.a18.towerdefence.gameContent.TouchListener;
 import no.progark.a18.towerdefence.gameContent.Tower;
 import no.progark.a18.towerdefence.gameContent.TowerDefenceSprite;
 
 /**
  * A static scene mainly for testing purposes.
  */
 class StaticLevel1 extends TowerDefenceScene  implements KillListener{
 	private final static String TAG = StaticLevel1.class.getName();
 	private final TowerDefenceActivity TDA;
 	private Tower towerToAdd;
 
 	private float scale = 1.25f;
 	
 	private int numRows, numColls;
 	
 	private Cell startCell;
 	private Cell[][] backgroundTiles;
 	private Tower[][] towers;
 	private Sprite menuTower;
 
 	private Font exitFont;
 	private BitmapTextureAtlas fontTexture;
 
 	private ITextureRegion creepTextureregion;
 	private ITextureRegion towerTextureregion;
 	private ITextureRegion brownTextureRegion;
 	private ITextureRegion greenTextureRegion;
 
 	private Creep creep;
 	private Text exitToMain;
 	private float screenWidth;
 	private float screenHeight;
 
 	/**
 	 * A static scene mainly for testing purposes.
 	 * 
 	 * @param tda
 	 *            the games {@linkplain BaseGameActivity}
 	 */
 	StaticLevel1(TowerDefenceActivity tda) {
 		super();
 		this.TDA = tda;
 		numColls = 16;
 		numRows = 11;
 		backgroundTiles = new Cell[numRows][numColls];
 		towers = new Tower[numRows][numColls];
 		
 		screenWidth = TDA.getWindowManager().getDefaultDisplay().getWidth();
 		screenHeight = TDA.getWindowManager().getDefaultDisplay().getHeight();
 		
 		loadResourses();
 
 		setBackground(new Background(Color.RED));
 		
 		addBackgCells();
 		addText();
 		addMenue();
 		addTower();
 		addCreeps();
 	}
 
 	private void addMenue() {
 		exitToMain.setX(screenWidth - exitToMain.getWidth());
 		exitToMain.setY(screenHeight -exitToMain.getHeight() + 5);
 		registerTouchArea(exitToMain);
 		attachChild(exitToMain);
 		
 		menuTower = new Sprite(screenWidth - 64, 32, 32, 32, towerTextureregion, TDA.getVertexBufferObjectManager()){
 			@Override
 			public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
 					float pTouchAreaLocalX, float pTouchAreaLocalY) {
 				towerToAdd = new Tower(0, 0, 32, 32, towerTextureregion, getVertexBufferObjectManager(), backgroundTiles);
 				Log.d(TAG, "User selected a new tower");
 				return true;
 			}
 		};
 
 		menuTower.setScale(3f);
 		registerTouchArea(menuTower);
 		attachChild(menuTower);
 	}
 
 	private void addTower() {
 		Tower tower = new Tower(numColls-2, 1, 32, 32, towerTextureregion, TDA.getVertexBufferObjectManager(), backgroundTiles);
 		addTower(numColls-2, 1, tower);
 	}
 
 	private void addBackgCells() {
 		// Add brown columns(midle)
 		boolean dir = true;
 		for (int y = 0; y < numRows; y += 2) {
 			for(int x = 1; x < numColls -1; x++){
 				backgroundTiles[y][x] = new Cell(scale*x*32, scale*y*32, 32, 32, brownTextureRegion, TDA.getVertexBufferObjectManager(), true);
 				backgroundTiles[y][x].setScale(scale);
 				backgroundTiles[y][x].setDirectionToNextRoad(dir?Direction.LEFT : Direction.RIGHT);
 			
 				attachChild(backgroundTiles[y][x]);
 			}
 			dir = !dir;
 		}
 		// Add brown columns(corners) 
 		dir = true;
 		for (int y = 0; y < numRows; y += 2) {
 			int x = backgroundTiles[y].length-1;
 			backgroundTiles[y][0] = new Cell(scale*0*32, scale*y*32, 32, 32, brownTextureRegion, TDA.getVertexBufferObjectManager(), true);
 			backgroundTiles[y][0].setScale(scale);
 			backgroundTiles[y][0].setDirectionToNextRoad(dir? Direction.DOWN : Direction.RIGHT);
 			backgroundTiles[y][x] = new Cell(scale*x*32, scale*y*32, 32, 32, brownTextureRegion, TDA.getVertexBufferObjectManager(), true);
 			backgroundTiles[y][x].setScale(scale);
 			backgroundTiles[y][x].setDirectionToNextRoad(dir? Direction.LEFT : Direction.DOWN);
 			
 			attachChild(backgroundTiles[y][0]);
 			attachChild(backgroundTiles[y][x]);
 			dir = !dir;
 		}
 		// Add brown center row
 		dir = true;
 		for (int y = 1; y < numRows; y += 2) {
 			int x = dir ? 0 : backgroundTiles[y].length-1;
 			backgroundTiles[y][x] = new Cell(scale*x*32, scale*y*32, 32, 32, brownTextureRegion, TDA.getVertexBufferObjectManager(), true);
 			backgroundTiles[y][x].setScale(scale);
 			backgroundTiles[y][x].setDirectionToNextRoad(Direction.DOWN);
 			
 			attachChild(backgroundTiles[y][x]);
 			dir = !dir;
 		}
 
 		// Add green to the rest of the board
 		for (int y = 0; y < numRows; y++) {
 			for (int x = 0; x < numColls; x++) {
 				final int posY = y;
 				final int posX = x;
 				if (backgroundTiles[y][x] == null) {
 					backgroundTiles[y][x] = new Cell(scale * 32 * x, scale * 32
 							* y, 32f, 32f, greenTextureRegion,
 							TDA.getVertexBufferObjectManager(), false);
 					backgroundTiles[y][x].setScale(scale);
 					backgroundTiles[y][x].setTouchListener(new TouchListener() {
 						
 						public boolean handleTouch(IEntity entity) {
 							Log.d(TAG, "THEY TOUCHED ME!!!!");
 							if(towerToAdd == null || towers[posY][posX] != null)
 								return false;
 							towerToAdd.setPosition(backgroundTiles[posY][posX]);
 							towerToAdd.setGridPosX(posX);
 							towerToAdd.setGridPosY(posY);
 							towers[posY][posX] = towerToAdd;
 							attachChild(towerToAdd);
 							towerToAdd = null;
 							Log.d(TAG, "User placed a new tower at x:"+posX+" y:"+posY);
 							return true;
 						}
 					});
 					registerTouchArea(backgroundTiles[y][x]);
 					attachChild(backgroundTiles[y][x]);
 				}
 			}
 		}
 		
 		startCell = backgroundTiles[0][backgroundTiles[0].length-1];
 		
 	}
 
 	private void addCreeps() {
 		creep = new Creep(scale* (numColls-1) * 32, 0f, 32f, 32f, creepTextureregion,
 				TDA.getVertexBufferObjectManager(), this, this);
 		creep.setSpeed(-300f, 0f);
 		creep.setScale(scale - 0.1f);
 		creep.registerUpdateHandler(new PathFinder(backgroundTiles[0].length-1, 0, creep));
 		startCell.addCreep(creep);
 		attachChild(creep);
 		Log.d(TAG, "Added creep sprite");
 	}
 
 	private void addText() {
 
 		// Level 1 text
 		exitToMain = new Text(200, 50, this.exitFont, TDA.getResources()
 				.getString(R.string.returnToMain),
 				TDA.getVertexBufferObjectManager()) {
 			@Override
 			public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
 					float pTouchAreaLocalX, float pTouchAreaLocalY) {
 				Log.d(TAG, "Exiting StaticLevel1");
 				TDA.popState();
 				return true;
 			}
 		};
 	}
 
 	public void loadResourses() {
 		// Load font texture
 		this.fontTexture = new BitmapTextureAtlas(TDA.getTextureManager(), 256,
 				256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 
 		this.exitFont = FontFactory.createFromAsset(TDA.getFontManager(),
 				this.fontTexture, TDA.getAssets(), "Sabatica-regular.ttf", 48,
 				true, Color.BLACK.getABGRPackedInt());
 		TDA.getEngine().getTextureManager().loadTexture(this.fontTexture);
 		TDA.getFontManager().loadFont(this.exitFont);
 
 		// load creep texture
 		creepTextureregion = loadTexture("gfx/", "creep1.png", 32, 32);
 		
 		// load creep texture
 		towerTextureregion = loadTexture("gfx/", "tower1.png", 32, 32);
 
 		// load brown background images
 		brownTextureRegion = loadTexture("sprites/", "brown.png", 32, 32);
 
 		// load green background image
 		greenTextureRegion = loadTexture("sprites/", "green.png", 32, 32);
 
 	}
 
 	private ITextureRegion loadTexture(String path, String fileName, int x,
 			int y) {
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath(path);
 
 		BitmapTextureAtlas brownTexture = new BitmapTextureAtlas(
 				TDA.getTextureManager(), x, y,
 				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 
 		ITextureRegion textureRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(brownTexture, TDA, fileName, 0, 0);
 
 		TDA.getEngine().getTextureManager().loadTexture(brownTexture);
 
 		Log.d(TAG, "Loaded " + fileName + " texture");
 
 		return textureRegion;
 	}
 
	public void reatchedtTargt(final TowerDefenceSprite sprite) {
 		System.out.println("Weeeehoooooo");
 		//TODO:
		TDA.runOnUpdateThread(new Runnable() {
			public void run() {
				detachChild(sprite);
			}
		});
 	}
 	
 	public boolean addTower(int x, int y, Tower tower){
 		if(backgroundTiles[y][x].isRoad() || towers[y][x] != null)
 			return false;
 		
 		towers[y][x] = tower;
 		tower.setX(x*scale*32);
 		tower.setY(y*scale*32);
 		attachChild(tower);
 		return true;
 	}
 	
 	public boolean removeTower(int x, int y){
 		Tower tower = towers[y][x];
 		boolean datatched = detachChild(tower);
 
 		return datatched;
 	}
 	
 	public void wasKilled(final Creep creep) {
		removeCreep(creep);
	}

	private void removeCreep(final Creep creep) {
 		for(Cell[] row : backgroundTiles)
 			for(Cell cell : row)
 				if(cell.containsCreep(creep))
 					cell.removeCreep(creep);
 		
 		TDA.runOnUpdateThread(new Runnable() {
 			public void run() {
 				detachChild(creep);
 			}
 		});
 		
 		Log.d(TAG, "Creep detaqtched");
 	}
 
 	private class PathFinder implements IUpdateHandler{
 		private Creep creep;
 		private int posX, posY;
 		
 		public PathFinder(int posX, int posY, Creep creep){
 			this.creep = creep;
 			this.posX = posX;
 			this.posY = posY;
 		}
 	
 		public void onUpdate(float pSecondsElapsed) {
 			switch(backgroundTiles[posY][posX].getDirectionToNextRoad()){
 			case LEFT :
 				float boundryLeft = (posX-1) * scale * 32;
 				if(creep.getX() < boundryLeft){
 					backgroundTiles[posY][posX--].removeCreep(creep);
 					backgroundTiles[posY][posX].addCreep(creep);
 					changeDir(backgroundTiles[posY][posX].getDirectionToNextRoad());
 				}
 				break;
 			case DOWN :
 				float boundryDown = (posY+1) * scale * 32;
 				if(creep.getY() > boundryDown){
 					backgroundTiles[posY++][posX].removeCreep(creep);
 					backgroundTiles[posY][posX].addCreep(creep);
 					changeDir(backgroundTiles[posY][posX].getDirectionToNextRoad());
 				}
 				break;
 			case RIGHT :
 				float boundryRight = (posX+1) * scale * 32;
 				if(creep.getX() > boundryRight){
 					backgroundTiles[posY][posX++].removeCreep(creep);
 					backgroundTiles[posY][posX].addCreep(creep);
 					changeDir(backgroundTiles[posY][posX].getDirectionToNextRoad());
 				}
 				break;
 			case UP :
 				float boundryUp = (posY-1) * scale * 32 - 16;
 				if(creep.getY() > boundryUp){
 					backgroundTiles[posY--][posX].removeCreep(creep);
 					backgroundTiles[posY][posX].addCreep(creep);
 					changeDir(backgroundTiles[posY][posX].getDirectionToNextRoad());
 				}
 				break;
 			default:
 			}			
 		}
 	
 		private void changeDir(Direction dirToNextRoad) {
 			float speed = Math.max(Math.abs(creep.getSpeedX()), Math.abs(creep.getSpeedY()));
 			switch(dirToNextRoad){
 			case LEFT :
 				creep.setSpeed(-speed, 0);
 				break;
 			case DOWN :
 				creep.setSpeed(0, speed);
 				break;
 			case RIGHT :
 				creep.setSpeed(speed, 0);
 				break;
 			case UP :
 				creep.setSpeed(0, -speed);
 				break;
 			default:
 			}
 		}
 	
 		public void reset() { }
 	}
 }
