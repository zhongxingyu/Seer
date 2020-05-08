 package com.shval.jnpgame;
 
 import java.util.ArrayList;
 
 import com.shval.jnpgame.BoardConfig;
 import static com.shval.jnpgame.Globals.*;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
 import com.badlogic.gdx.physics.box2d.EdgeShape;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.utils.Disposable;
 import com.badlogic.gdx.utils.TimeUtils;
 import com.badlogic.gdx.utils.Timer;
 
 public class Board implements Disposable {
 	
 	private static final String TAG = Board.class.getSimpleName();
 	private final int ROWS, COLS;
 	private final int REVERT_DEPTH = 3;
 	private Cell cells[][];
 	private Cell initialBoard[][];
 	private Cell boardStateStack[][][];
 	private int boardStateIndex;
 	private int boardDynamicState;
 	float cellWidth;
 	float cellHeight;
 	PlayScreen screen;
 	static int renderMode; // 0 - physical, 1 - debug physical 2 - static 
 	
 	private Sound[] sounds;
 	private float soundVolume; // in [0,1]
 	private World world; // box2d world
 	Box2DDebugRenderer debugRenderer;
 	private ArrayList<MergeEffect> mergeEffects;
 	
 	// dummy "out of scope" cell, make it a wall
 	//static final Cell outOfScopeCell = new Cell(null, 0, 0 ,null , WALL, NONE);
 	static Cell outOfScopeCell;
 	boolean boardLocked = false;
 	
 
 	private class UnlockTask extends Timer.Task {
 		@Override
 		public void run() {
 			boardLocked = false;
 		}
 	}
 	
 	private class WinTask extends Timer.Task {
 		@Override
 		public void run() {
 			boardLocked = true; // take control of the board
 			screen.win();
 		}
 	}
 
 	private class SetNeighboursTask extends Timer.Task {
 		@Override
 		public void run() {
 			setNeighbours();
 		}
 	}
 	
 	private class DelayedSoundPlay extends Timer.Task {
 		
 		Sound sound;
 		
 		DelayedSoundPlay(Sound sound) {
 			this.sound = sound;
 		}
 		
 		@Override
 		public void run() {
 			sound.play(soundVolume);
 		}
 	}
 
 	private class AttemptEmergeTask extends Timer.Task {
 		
 		@Override
 		public void run() {
 			boardDynamicState = attemptEmerge();
 			if (boardDynamicState == STABLE)
 				boardLocked = false;
 		}
 	}
 		
 	private class MergeEffect {
 		static final float MAX_TTL = 0.35f;
 		float ttl1; // time to live
 		float ttl2; // time to live
 		boolean horisontal;
 		Sprite star1, star2, star3;
 		Sprite flash;
 		float gX, gY;
 		float gX2, gY2;
 		float gX3, gY3;
 		float w = 720;
 
 		MergeEffect(boolean horisontal, int x, int y) {
 			Timer.schedule(new DelayedSoundPlay(sounds[SOUND_MERGE_START]), 0f);
 			Timer.schedule(new DelayedSoundPlay(sounds[SOUND_MERGE_FINISH]), MergeEffect.MAX_TTL * 1.5f);
 
 			// effect just above (if horizontal) or just to the right
 			// of cell (x, y)
 			this.horisontal = horisontal;
 			this.ttl1 = MAX_TTL;
 			this.ttl2 = MAX_TTL;
 			this.star1 = new Sprite(Assets.getStarTexture());
 			this.star2 = new Sprite(Assets.getStarTexture());
 			this.flash = new Sprite(Assets.getFlashTexture(), 26, 0, 20, 60);
 
 			this.star3 = new Sprite(Assets.getStarTexture());
 
 
 			
 			star1.setScale(cellWidth / (80), cellWidth / (80));
 			star2.setScale(cellWidth / (80), cellWidth / (80));
 			
 			star3.setScale(cellWidth / (160), cellWidth / (160));
 
 			
 			//star1.setSize(cellWidth / 3, cellHeight / 3);
 			//star2.setSize(cellWidth / 3, cellHeight / 3);
 			if (horisontal) {
 				this.gX = cellWidth * ((float) x + 0.5f) - 16;
 				this.gY = cellHeight * ((float) y + 1f) - 16;
 				//flash.setPosition(cellWidth * (x - 0.2f),  cellHeight * (y + 0.9f));
 				//flash.setSize(cellWidth * 1.4f, cellHeight / 6);
 				
 				flash.setSize(cellHeight / 6, cellWidth * 1.4f);
 				flash.setPosition(cellWidth * (x + 0.6f),  cellHeight * (y + 0.7f));
 				flash.rotate(90);
 				gX2 = cellWidth * x - 16;
 				gY2 = cellHeight * (y + 1) - 16;
 			}
 			else {
 				this.gX = cellWidth * ((float) x + 1f) - 16;
 				this.gY = cellHeight * ((float) y + 0.5f) - 16;
 				flash.setPosition(cellWidth * (x + 0.9f),  cellHeight * (y - 0.2f));
 				flash.setSize(cellWidth / 6, cellHeight * 1.4f);
 				gX2 = cellWidth * (x + 1) - 16;
 				gY2 = cellHeight * y - 16;
 			}
 			
 			gX3 = cellWidth * (x + 1) - 16;
 			gY3 = cellHeight * (y + 1) - 16;
 		}
 		
 		void update(float delta) {
 			
 			if (ttl1 > 0) {
 				ttl1 -= delta;
 					
 				float dx = 0;
 				float dy = 0;
 				
 				if (horisontal)
 					dx = 0.5f * cellWidth * (MAX_TTL - 2 * ttl1 ) / MAX_TTL;
 				else
 					dy = 0.5f * cellHeight * (MAX_TTL - 2 * ttl1 ) / MAX_TTL;
 				
 				star1.rotate(w * delta);
 				star2.rotate(- w * delta);
 				star1.setPosition(gX + dx, gY + dy);
 				star2.setPosition(gX - dx, gY - dy);
 			}
 			else {
 				ttl2 -= delta;
 				star3.rotate(- w * delta);
 			}
 		}
 		
 		void render(SpriteBatch batch) {
 			if (ttl1 > 0) {
 				flash.setColor(1, 1, 1, ttl1 / MAX_TTL);
 				flash.draw(batch);
 				star1.draw(batch);
 				star2.draw(batch);
 			}
 			else {
 				if (ttl2 < 3 * MAX_TTL / 4) {
 					float d = ttl2 / MAX_TTL;
 					star3.setColor(1, 1, 1, ttl2 / MAX_TTL);
 					d = d * d;
 					float dx = 0.4f * cellWidth * (1 - d * d);
 					float dy = 0.4f * cellHeight * (1 - d * d);
 
 
 					star3.setPosition(gX3 + dx, gY3 + dy);
 					star3.draw(batch);
 					star3.setPosition(gX3 - dx, gY3 + dy);
 					star3.draw(batch);
 					star3.setPosition(gX3 + dx, gY3 - dy);
 					star3.draw(batch);
 					star3.setPosition(gX3 - dx, gY3 - dy);
 					star3.draw(batch);
 					star3.setPosition(gX2 + dx, gY2 + dy);
 					star3.draw(batch);
 					star3.setPosition(gX2 - dx, gY2 + dy);
 					star3.draw(batch);
 					star3.setPosition(gX2 + dx, gY2 - dy);
 					star3.draw(batch);
 					star3.setPosition(gX2 - dx, gY2 - dy);
 					star3.draw(batch);					
 				}
 			}
 		}
 	}
 	
 	public Board(BoardConfig config, PlayScreen screen) {
 		this.ROWS = config.ROWS;
 		this.COLS = config.COLS;
 		this.screen = screen;
 		
 		soundVolume = config.getSoundVolume();
 		sounds = new Sound[MAX_BOARD_SOUNDS];
 		for (int i = 0; i < MAX_BOARD_SOUNDS; i++)
 			sounds[i] = config.getSound(i);
 		
 		
 		initialBoard = new Cell[COLS][ROWS];
 		for (int x = 0; x < COLS; x++) {
 			for (int y = 0; y < ROWS; y++) {
 				initialBoard[x][y] = Cell.createCell(x, y, config);
 			}
 		}
 		
 		cells = new Cell[COLS][ROWS];
 		boardStateStack = new Cell[REVERT_DEPTH][COLS][ROWS];
 		for (int i = 0; i <REVERT_DEPTH; i++)
 			boardStateStack[i] = new Cell[COLS][ROWS];
 		
 		createWorld();
 				
 		outOfScopeCell = Cell.createCell(-1, -1, null);
 		mergeEffects = new ArrayList<MergeEffect>();
 	}
 
 	private void copyBoardState(Cell dst[][], Cell src[][]) {
 		for (int x = 0; x < COLS; x++) {
 			for (int y = 0; y < ROWS; y++) {
 				Cell cell = src[x][y];
 				if (cell != null) {
 					//Gdx.app.debug(TAG, "Coping cell " + x + ", " + y);
 					dst[x][y] = new Cell(cell);
 				} else {
 					dst[x][y] = null;
 				}
 			}
 		}		
 	}
 	
 	private void pushCurrentBoardState() {
 		pushBoardState(cells);
 	}
 	
 	private void pushBoardState(Cell[][] state) {
 		Gdx.app.debug(TAG, "Pushing: boardStateIndex = " + boardStateIndex);
 		if (boardStateIndex == REVERT_DEPTH) {
 			// a circular spin
 			Cell[][] tmp = boardStateStack[0];
 			for (int i = 0; i < REVERT_DEPTH - 1; i++) {
 				boardStateStack[i] = boardStateStack[i+1];
 			}
 			boardStateStack[REVERT_DEPTH - 1] = tmp;
 			boardStateIndex = REVERT_DEPTH - 1;
 		}
 		
 		copyBoardState(boardStateStack[boardStateIndex], state);
 		boardStateIndex++;
 	}
 	
 
 	// returns null if stack empty
 	private Cell[][] popBoardState() {
 		if (boardStateIndex == 0)
 			return null;
 		boardStateIndex--; 
 		return boardStateStack[boardStateIndex];
 	}
 	
 	private Cell[][] peekBoardState() {
 		if (boardStateIndex == 0)
 			return null;
 		return boardStateStack[boardStateIndex - 1];
 	}
 
 	public boolean revert() {
 		Gdx.app.debug(TAG, "Reverting: boardStateIndex = " + boardStateIndex);
 		if (boardLocked)
 			return false;
 		Cell[][] state = popBoardState();
 		if (state != null) {
 			startFrom(state);
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean start() {
 		if (boardLocked)
 			return false;
 		Gdx.app.debug(TAG, "Starting");
 		renderMode = 2;
 		boardStateIndex = 0; // flush state stack
 		startFrom(initialBoard);
 		return true;
 	}
 	
 	private void startFrom(Cell[][] state) {
 		//renderMode = 0;
 		destoyPhysical();
 		copyBoardState(cells, state);
 		boardDynamicState = STABLE;
 		jellifyBoard();
 		attemptMerge(cells, null, true); // dont trigger merge effect
 		createPhysicalCells();
 		setNeighbours();
 		//updateBoardPhysics(); //TODO: do we really need levels who need this call?
 	}
 
 	private void destoyPhysical() {
 		for (int x = 0; x < COLS; x++) {
 			for (int y = 0; y < ROWS; y++) {
 				Cell cell = cells[x][y];
 				if (cell != null)
 					cell.destroyPhysical();
 			}
 		}
 	}
 	
 
 	public int getRows() {
 		return ROWS;
 	}
 
 	public int getCols() {
 		return COLS;
 	}
 	
 	float getSpriteHeight() {
 		return cellHeight;
 	}
 	
 	float getSpriteWidth() {
 		return cellWidth;
 	}
 	
 	public void jellifyBoard() {
 		jellify(cells);
 	}
 	// create jelly for each cell
 	public void jellify(Cell[][] cellState)
 	{
 		for (int x = 0; x < COLS; x++) {
 			for (int y = 0; y < ROWS; y++) {
 				Cell cell = cellState[x][y];
 				if (cell == null || cell.getType() == NONE)
 					continue;
 				Jelly jelly = new Jelly(this);
 				cell.setJelly(jelly);
 				jelly.join(cell);
 			}
 		}
 		
 		// jellify static out of scope cell iff not jellified alreasy
 		Cell cell = outOfScopeCell;
 		if (cell.getJelly() == null) {
 			Jelly jelly = new Jelly(this);
 			cell.setJelly(jelly);
 			jelly.join(cell);
 		}
 		
 	}
 	
 	private boolean isWinPosition() {
 		// we'll need a new copy for this
 		Cell[][] checkState = new Cell[COLS][ROWS];
 		copyBoardState(checkState, cells);
 		jellify(checkState);
 		attemptMerge(checkState, null, false); // don't merge anchored when looking for a win
 												// also dont trigger merge effect
 		
 		// win iff all non-black jellies
 		Jelly jellies[] = new Jelly[MAX_COLORED_JELLY_TYPES];
 
 		for (int x = 0; x < COLS; x++) {
 			for (int y = 0; y < ROWS; y++) {
 				Cell cell = checkState[x][y];
 				// we check for NONE although it isn't there
 				if (cell == null || cell.getType() == NONE)
 					continue;
 				
 				if (cell.emerging != null) 
 					return false;
 				int type = cell.getType();
 				// walls and blacks are not in the game
 				if (type == WALL || Cell.isBlack(type))
 					continue;
 
 				if (jellies[type] != null) {
 					if (jellies[type] != cell.getJelly())
 						return false;
 				} else {
 					jellies[type] = cell.getJelly();
 				}
 			}
 		}
 		return true;
 	}
 	
 	public void setResolution(float boardWidth, float boardHeight) {
 		// not necessarily square
 		// TODO: try using floats
 		this.cellWidth = (boardWidth / (float)COLS);
 		this.cellHeight = (boardHeight / (float)ROWS);
 		
 		// make sprite w/h even
 		//this.cellWidth += this.cellWidth % 2;
 		//this.cellHeight += this.cellHeight % 2;
 			
 		Gdx.app.debug(TAG, "Sprite size = " + cellWidth + " x " + cellHeight);
 		Gdx.app.debug(TAG, "Board size = " + boardWidth + " x " + boardHeight);
 		
 		// set resolution to cells in initialBoard
 		Cell.setResolution(cellWidth, cellHeight);
 		/*
 		for (int x = 0; x < COLS; x++) {
 			for (int y = 0; y < ROWS; y++) {
 				if (cells[x][y] != null)
 					cells[x][y].setNeighbours();
 			}
 		}
 		*/
 		
 	}
 		
 
 	public Cell getCell(int x, int y) {
 		if (x < 0 || x >= COLS || y < 0 || y >= ROWS) {
 			/* error */
 			//Gdx.app.debug(TAG, "Out of scope");
 			return outOfScopeCell;
 		}
 		return cells[x][y];
 	}
 	
 	public void resetAllScanFlags () {
 		for (int x = 0; x < COLS; x++) {
 			for (int y = 0; y < ROWS; y++) {
 				if (cells[x][y] != null)
 					cells[x][y].resetScanFlag();
 			}
 		}
 	}
 	
 	
 	// returns true if moving
 	boolean attemptSlide(int dir, int x, int y) {
 		Cell cell;
 
 		// dont allow slides while not stable
 		if (/*boardDynamicState != STABLE ||*/ boardLocked) {
 			Gdx.app.debug(TAG, "Attempting to slide. Board is locked");
 			return false;
 		}
 
 		if (x < 0 || x >= COLS || y < 0 || y >= ROWS) {
 			// error
 			Gdx.app.debug(TAG, "attemptSlide: Out of scope");
 			return false;
 		}
 				
 		cell = cells[x][y];
 		// if no cell return
 		if (cell == null)
 			return false;
 		
 		boolean ret = attemptMove(dir, cell);
 		
 		if (ret) {
 			sounds[SOUND_SLIDE].play(soundVolume / 2); // sliding is quieter
 			boardLocked = true;
 		}
 		
 		return ret;
 	}
 	
 	void render(float delta, SpriteBatch spriteBatch) {		
 
 		// update the world with a fixed time step
 		world.step(delta, 3, 3);
 		//float updateTime = (TimeUtils.nanoTime() - startTime) / 1000000000.0f;
 		//Gdx.app.debug(TAG, "delta = " + delta_t);
 		
 
 		//startTime = TimeUtils.nanoTime();
 		// clear the screen and setup the projection matrix
 
 
 		if (renderMode == 1 /* DEBUG */) {
 			Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
 			Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 			debugRenderer.render(world, this.screen.camera.combined);
 			return;
 		}
 		// render cells
 		
 		if (renderMode == 0 /* Physical */) {
 			Gdx.gl10.glEnable(GL10.GL_BLEND);
 		}
 
 		for (int x = 0; x < COLS; x++) {
 			for (int y = 0; y < ROWS; y++) {
 				Cell cell = cells[x][y];
 				if (cell == null)
 					continue;
 				cell.render(spriteBatch, 0);
 			}
 		}
 		
 		for (int x = 0; x < COLS; x++) {
 			for (int y = 0; y < ROWS; y++) {
 				Cell cell = cells[x][y];
 				if (cell == null)
 					continue;
 				cell.render(spriteBatch, 1);
 			}
 		}
 		
 		// render anchors
 		
 		for (int x = 0; x < COLS; x++) {
 			for (int y = 0; y < ROWS; y++) {
 				Cell cell = cells[x][y];
 				if (cell == null)
 					continue;
 				cell.render(spriteBatch, 2);
 			}
 		}
 		
 		if (renderMode == 2 /* static */) {
 			// render merge effects
 			for (MergeEffect c : mergeEffects) {
 				c.render(spriteBatch);
 			}
 		}
 	}
 
 	
 	private boolean isNeighbour(Cell cell1, int dir, Cell cell2) {
 		if (cell1 == null || cell2 == null)
 			return false;
 		
 		int dx = 0, dy = 0;
 		switch (dir) {
 		case LEFT:
 			dx = -1;
 			break;
 		case RIGHT:		
 			dx = 1;
 			break;
 		case DOWN:
 			dy = -1;
 			break;
 		case UP:
 			dy = 1;
 			break;
 		case NONE:
 			return false;
 		default:
 			// should never be here
 			Gdx.app.error(TAG, "isNeighbor(...): Invalid direction");
 			return false;
 		}
 		
 		return ((cell1.getX() + dx == cell2.getX()) && (cell1.getY() + dy == cell2.getY()) );
 	}
 	
 	private void mergeCells(Cell cell, Cell neighbor) {
 		neighbor.getJelly().merge(cell.getJelly());
 	}
 	
 	private boolean attemptMerge(Cell cell, Cell neighbor, boolean mergeAnchord) {
 		
 		if (neighbor == null)
 			return false;
 
 		//Gdx.app.debug(TAG, "attempting to merge " + cell.getX() + ", " + cell.getY() + 
 			//	   " and " + neighbor.getX() + ", " + neighbor.getY());
 		
 		if (cell.getJelly() == neighbor.getJelly())
 			return false;
 		
 		if (cell.getType() == neighbor.getType()) {
 			mergeCells(cell, neighbor);
 			return true;
 		}
 		
 		// when merging for checking win
 		if (!mergeAnchord)
 			return false;
 		
 		// anchored cells
 		if (isNeighbour(cell, cell.anchoredTo, neighbor) ||
 			isNeighbour(neighbor, neighbor.anchoredTo, cell) ) {
 				mergeCells(cell, neighbor);
 				return true;				
 		}
 		
 		return false;
 	}
 	
 	/*
 	private boolean attemptMerge() {
 		// for compatibility
 		return attemptMerge(cells, true, true); // merge anchord 
 	}
 	*/
 	
 	private boolean attemptMerge(Cell[][] postition, Cell[][] oldPostition, boolean mergeAnchord) {
 		boolean ret = false;
 				
 		for (int x = 0; x < COLS; x++) {
 			for (int y = 0; y < ROWS; y++) {
 				Cell cell = postition[x][y];
 								
 				if (cell == null)
 					continue;
 				
 				 if (cell.isMoving())
 					 Gdx.app.error(TAG, "Attempting to merge while moving");
 				
 				 
 				Cell neighbor;
 				// try to merge with right neighbor
 				if (x < COLS - 1)
 					neighbor = postition[x + 1][y];
 				else
 					neighbor = outOfScopeCell; // this is helpful for border rendering
 				
 				ret |= attemptMerge(cell, neighbor, mergeAnchord);
 				if (oldPostition != null && neighbor != null && cell.getType() == neighbor.getType()) {
 					Cell oldCell = oldPostition[x][y];
 					Cell oldNeighbor = (x < COLS - 1) ? oldPostition[x + 1][y] : null;
 					if (oldNeighbor != null && oldCell.getJelly() != oldNeighbor.getJelly()) {
 						mergeEffects.add(new MergeEffect(false, x, y));
 					}
 				}
 				// try to merge with up neighbor
 				if (y < ROWS - 1)
 					neighbor = postition[x][y+1];
 				else
 					neighbor = null;
 				
 				ret |= attemptMerge(cell, neighbor, mergeAnchord);
 				if (oldPostition != null && neighbor != null && cell.getType() == neighbor.getType()) {
 					Cell oldCell = oldPostition[x][y];
 					Cell oldNeighbor = (y < ROWS - 1) ? oldPostition[x][y + 1] : null;
 					if (oldNeighbor != null && oldCell.getJelly() != oldNeighbor.getJelly()) {
 						mergeEffects.add(new MergeEffect(true, x, y));
 					}
 				}
 				
 				
 			}
 		}
 		
 		return ret;
 	}
 
 	private int attemptEmerge() {
 		
 		for (int x = 0; x < COLS; x++) {
 			for (int y = 0; y < ROWS; y++) {
 				Cell cell = cells[x][y];
 				
 				if (cell == null)
 					continue;
 				
 				 if (cell.isMoving())
 					 Gdx.app.debug(TAG, "Attempting to emerge while moving (it may be ok)");
 				
 				 Cell emerging = cell.emerging;
 				 if (emerging == null)
 					continue;
 					
 				int dx = 0;
 				int dy = 0;
 				switch (emerging.emergingTo) {
 				case UP:
 					dy = 1;
 					break;
 				case LEFT:
 					dx = -1;
 					break;
 				case RIGHT:
 					dx = 1;
 					break;
 				default:
 					 Gdx.app.error(TAG, "Attempting to emerge to" + emerging.emergingTo);
 				}
 				
 				Cell neighbor = cells[x+dx][y+dy];
 				if (neighbor == null)
 					continue;
 				
 				if(emerging.getType() == neighbor.getType())
 					if (attemptMove(emerging.emergingTo, neighbor)) {
 						cell.emerge();
 						//mergeCells(cell.emerging, neighbor);
 						int emerge = emerging.emergingTo;
 						boardLocked = true;
 						return 1 << emerge; // let them emerge one by one
 					}
 			}
 		}
 
 		return STABLE;
 	}
 
 	private int attemptMergeEmerge() {
 		
 		for (int x = 0; x < COLS; x++) {
 			for (int y = 0; y < ROWS; y++) {
 				Cell cell = cells[x][y];
 				
 				if (cell == null)
 					continue;
 				
 				 if (cell.isMoving())
 					 Gdx.app.debug(TAG, "Attempting to merge-emerge while moving (it may be ok)");
 				
 				 Cell emerging = cell.emerging;
 				 if (emerging == null)
 					continue;
 					
 				int dx = 0;
 				int dy = 0;
 				switch (emerging.emergingTo) {
 				case UP:
 					dy = 1;
 					break;
 				case LEFT:
 					dx = -1;
 					break;
 				case RIGHT:
 					dx = 1;
 					break;
 				default:
 					 Gdx.app.error(TAG, "Attempting to emerge to" + emerging.emergingTo);
 				}
 				
 				Cell neighbor = cells[x+dx][y+dy];
 				if (neighbor == null)
 					continue;
 				
 				if(emerging.getType() == neighbor.getType())
 					if (attemptMove(emerging.emergingTo, neighbor)) {
 						cell.emerge();
 						//mergeCells(cell.emerging, neighbor);
 						int emerge = emerging.emergingTo;
 						mergeCells(emerging, neighbor);
 						mergeEffects.add(
 								new MergeEffect(emerging.emergingTo == UP, Math.min(x,  x+dx), Math.min(y, y+dy)));  
 						return 1 << emerge;// let them emerge one by one
 					}
 			}
 		}
 
 		return STABLE;
 	}
 
 	
 	private void setNeighbours() {
 		Gdx.app.debug(TAG, "Setting neighbors");
 		for (int x = 0; x < COLS ; x++) {
 			for (int y = 0; y < ROWS ; y++) {
 				Cell cell = cells[x][y];
 				
 				// moving cells do not merge
 				if (cell == null || cell.isMoving())
 					continue;
 				
 				cell.setNeighbours();
 			}
 		}
 	}
 	
 	// returns true if moving
 	private boolean attemptMove(int dir, Cell cell) {
 		//Gdx.app.debug(TAG, "Attempt to move (" + cell.getX() + ", " + cell.getY() + ") in direction: " + dir);
 		resetAllScanFlags(); // reset all cells scan flag
 		if(cell.canMove(dir)) {
 			resetAllScanFlags();
 			if (boardDynamicState == STABLE) { // just swithched from stable to non stable
 				pushCurrentBoardState();
 				boardDynamicState = 1 << dir;
 			}
 			cell.move(dir);
 			return true;
 		}
 		return false;
 	}
 	
 	void update(float delta) {
 		
 		boolean isMilestone;
 		//Gdx.app.debug(TAG, "Updating game state. delta = " + delta);
 		
 		ArrayList<MergeEffect> mergeEffectsNew = new ArrayList<MergeEffect>();
 		for (MergeEffect c : mergeEffects) {
 			c.update(delta);
 			if (c.ttl2 > 0)
 				mergeEffectsNew.add(c);
 		}
 		mergeEffects = mergeEffectsNew;
 		
 		if (boardDynamicState == STABLE)
 			return;
 		
 		isMilestone = false;
 		for (int x = 0; x < COLS; x++) {
 			for (int y = 0; y < ROWS; y++) {
 				Cell cell = cells[x][y];
 				if (cell == null)
 					continue;
 				isMilestone |= cell.update(delta);
 			}
 		}
 		
 		if(!isMilestone)
 			return;
 
 		// new milestone (N.Z) is reached - update board
 		Gdx.app.debug(TAG, "Reached milestone. Rebuilding board");
 		Cell cellsOld[][] = cells;
 		cells = new Cell[COLS][ROWS];
 		for (int x = 0; x < COLS; x++) {
 			for (int y = 0; y < ROWS; y++) {
 				Cell cell = cellsOld[x][y];
 				if (cell == null)
 					continue;
 				cells[cell.getX()][cell.getY()] = cell;
 				if(cell.emerging != null) {
 					Cell newCell = cell.emerging;
 					if (newCell.emergingTo == NONE) { // just emerged
 						cells[newCell .getX()][newCell .getY()] = newCell;
 						cell.emerging = null;
 					}
 				}
 			}
 		}
 		
 		Timer.schedule(new SetNeighboursTask(), 0);
 		
 		// trigger board dynamics (not to be confused with on tick update) 
 //		int oldBoardState = boardState;
 		
 		int newState = updateBoardPhysics();
 		if (newState == STABLE) {// milestone is stable
 			boolean merged;
 			Cell oldCells[][] = new Cell[COLS][ROWS];
 			copyBoardState(oldCells, cells);
 			merged = attemptMerge(cells, oldCells , true);
 			newState = attemptMergeEmerge();
 			merged = merged || newState != STABLE;
 			if (merged) {
 				//Timer.schedule(new DelayedSoundPlay(sounds[SOUND_MERGE_START]), 0f);
 				//Timer.schedule(new DelayedSoundPlay(sounds[SOUND_MERGE_FINISH]), MergeEffect.MAX_TTL * 1.5f);
 				Timer.schedule(new SetNeighboursTask(), MergeEffect.MAX_TTL * 2f);
 			}
			if (/*merged&&*/ isWinPosition()) {
 				Gdx.app.debug(TAG, "You win!");
 				Timer.schedule(new DelayedSoundPlay(sounds[SOUND_WIN]), 0.7f);
 				Timer.schedule(new WinTask(), 1f);
 				return;
 			}
 			createPhysicalCells();
 			
 			if (merged) {
 				//Timer.schedule(new AttemptEmergeTask(), MergeEffect.MAX_TTL * 3f);
 				Timer.schedule(new UnlockTask(), MergeEffect.MAX_TTL * 3f);
 			}
 			else {
 				//Timer.schedule(new AttemptEmergeTask(), 0);
 				Timer.schedule(new UnlockTask(), 0);
 			}
 
 		}
 		//else {
 			boardDynamicState = newState;
 		//}
 		
 		Gdx.app.debug(TAG, "Updating done. board dynamic state = " + boardDynamicState);
 	}	
 	
 	
 	private void createPhysicalCells() {
 		if (!PHYSICS_SUPPORTED)
 			return;
 		Gdx.app.debug(TAG, "Creating phy cells");
 		for (int x = 0; x < COLS; x++) {
 			for (int y = 0; y < ROWS; y++) {
 				Cell cell = cells[x][y];
 				if (cell == null)
 					continue;
 				cell.createPhysicalCell();
 			}
 		}
 		
 		// fix/anchor cells physically
 		for (int x = 0; x < COLS; x++) {
 			for (int y = 0; y < ROWS; y++) {
 				Cell cell = cells[x][y];
 				
 				if (cell == null)
 					continue;
 				
 				Cell neighbor;
 				// right neighbor
 				if (x < COLS - 1) {
 					neighbor = cells[x + 1][y];
 					
 					// walls already fixed
 					// we need to check type since multiple colors can compose one jelly  
 					// we also check jelly since cells may be same type  neighbors without
 					// 		belonging to the same jelly (in unstable state)
 					if (neighbor != null && neighbor.getType() != WALL && 
 						cell.getType() == neighbor.getType() &&
 						cell.getJelly() == neighbor.getJelly()) {
 						Gdx.app.debug(TAG, x + ", " + y + " - fixing right peer");
 						cell.fixPeer(neighbor, RIGHT);
 					}
 					if (neighbor != null && 
 						(cell.anchoredTo == RIGHT || neighbor.anchoredTo == LEFT) ) {
 						Gdx.app.debug(TAG, x + ", " + y + " - anchoring right peer");
 						cell.anchorPeer(neighbor, RIGHT);
 					}
 				}
 				
 				// up neighbor
 				if (y < ROWS - 1) {
 					
 					neighbor = cells[x][y + 1];
 					Gdx.app.debug(TAG, x + ", " + y + " type = " + cell.getType());
 					if (neighbor != null)
 						Gdx.app.debug(TAG, x + ", " + y + " up neighbor type = " + neighbor.getType());
 					// walls already fixed
 					if (neighbor != null && neighbor.getType() != WALL && 
 						cell.getType() == neighbor.getType() &&
 						cell.getJelly() == neighbor.getJelly()) {
 						Gdx.app.debug(TAG, x + ", " + y + " - fixing up peer");
 						cell.fixPeer(neighbor, UP);
 					}
 					if (neighbor != null && 
 						(cell.anchoredTo == UP || neighbor.anchoredTo == DOWN) ) {
 						Gdx.app.debug(TAG, x + ", " + y + " - anchoring up peer");
 						cell.anchorPeer(neighbor, UP);
 					}
 				}
 			}
 		}		
 		
 		//setNeighbours(); // align graphical properties of cells
 		Timer.schedule(new SetNeighboursTask(), MergeEffect.MAX_TTL * 2f);
 	}
 
 	// returns new board state
 	private int updateBoardPhysics() {
 		// this function is called iff a milestone was reached
 		// also, board motion is either vertical or horizontal, but not both 
 		
 		int newBoardState = STABLE; // until proven otherwise
 		
 		Gdx.app.debug(TAG, "Updating physics, dynamic state = " + boardDynamicState);
 		// horizontal motion
 		if (boardDynamicState == RIGHT_MOTION || boardDynamicState == LEFT_MOTION) {
 			//  stop all horizontal motion
 			for (int x = 0; x < COLS; x++) {
 				for (int y = 0; y < ROWS; y++) {
 					Cell cell = cells[x][y];
 					if (cell == null)
 						continue;
 					
 					cell.stopHorizontal();
 				}
 			}
 			
 			createPhysicalCells();
 			
 			// try to fall
 			for (int x = 0; x < COLS; x++) {
 				for (int y = 0; y < ROWS; y++) {
 					Cell cell = cells[x][y];
 					if (cell == null)
 						continue;
 					
 					if (attemptMove(DOWN, cell))
 						newBoardState = DOWN_MOTION;
 				}
 			}
 			
 		}
 		else if (boardDynamicState == DOWN_MOTION) {
 
 			// continue to fall?
 			for (int x = 0; x < COLS; x++) {
 				for (int y = 0; y < ROWS; y++) {
 					Cell cell = cells[x][y];
 					if (cell == null)
 						continue;
 					
 					// already falling cell ?
 					if (cell.isMoving()) {
 						if (!attemptMove(DOWN, cell)) {
 							// just hit ground
 							cell.stopVertical();
 							sounds[SOUND_FALL].play(soundVolume);
 						} else {
 							newBoardState = DOWN_MOTION;
 						}
 					} else if (attemptMove(DOWN, cell)) {
 						// cell begins to fall
 						newBoardState = DOWN_MOTION;
 					}
 				}
 			}
 		} 
 		else if (boardDynamicState == UP_MOTION) {
 			for (int x = 0; x < COLS; x++) {
 				for (int y = 0; y < ROWS; y++) {
 					Cell cell = cells[x][y];
 					if (cell == null)
 						continue;
 					
 					cell.stopVertical();
 				}
 			}
 		}
 		else {
 			Gdx.app.error(TAG, "Something is terribly wrong ...");
 		}			
 		
 		// update state
 		Gdx.app.debug(TAG, "Updating physics done, returning new dynamic state = " + newBoardState);
 		return newBoardState;
 	}
 	
 	
 	public boolean isRevertable() {
 		return (boardStateIndex > 0);
 	}
 	
 	public World getWorld() {
 		return world;
 	}
 	
 	public void createWorld () {
 		// setup the camera. In Box2D we operate on a
 		// meter scale, pixels won't do it. So we use
 		// an orthographic camera with a viewport of
 		// 48 meters in width and 32 meters in height.
 		// We also position the camera so that it
 		// looks at (0,16) (that's where the middle of the
 		// screen will be located).
 		//camera = new OrthographicCamera(48, 32);
 		//camera.position.set(0, 15, 0);
 
 		// create the world
 		world = new World(new Vector2(0, -1000), true);
 		debugRenderer = new Box2DDebugRenderer();
 		
 		//float k_restitution = 0.0f;
 		//Body ground;
 
 	}
 
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 		for (int x = 0; x < COLS; x++) {
 			for (int y = 0; y < ROWS; y++) {
 				Cell cell = cells[x][y];
 				if (cell != null)
 					cell.dispose();
 			}
 		}
 		world.dispose();
 	}
 
 }
