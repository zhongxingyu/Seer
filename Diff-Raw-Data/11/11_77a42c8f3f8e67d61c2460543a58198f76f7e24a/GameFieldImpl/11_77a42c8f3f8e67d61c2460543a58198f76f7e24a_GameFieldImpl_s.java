 package jku.se.tetris.model;
 
 import java.util.ArrayList;
 
 import jku.se.tetris.model.exception.InvalidActionException;
 
 public class GameFieldImpl implements GameField {
 	public static final int MAX_LEVEL = 100;
 	public static final int LEVEL_THRESHOLD = 10000;
 
 	// ---------------------------------------------------------------------------
 
 	private int width;
 	private int height;
 
 	// ---------------------------------------------------------------------------
 
 	private long score;
 	private int level;
 
 	// ---------------------------------------------------------------------------
 
 	private boolean backToBack = false;
 
 	// ---------------------------------------------------------------------------
 
 	private Block[][] blocks;
 
 	// ---------------------------------------------------------------------------
 
 	private Stone activeStone;
 	private Stone nextStone;
 
 	// ---------------------------------------------------------------------------
 
 	private ArrayList<GameFieldChangedListener> fieldListeners;
 	private ArrayList<GameDataChangedListener> dataListeners;
 
 	// ---------------------------------------------------------------------------
 
 	private EGameState gameState;
 
 	// ---------------------------------------------------------------------------
 
 	public GameFieldImpl(int width, int height) {
 		this.width = width;
 		this.height = height;
 		// --
 		score = 0;
 		level = 1;
 		// --
 		blocks = new Block[height][width];
 		// --
 		fieldListeners = new ArrayList<GameFieldChangedListener>();
 		dataListeners = new ArrayList<GameDataChangedListener>();
 		// --
 		gameState = EGameState.INITIALIZED;
 	}
 
 	// ---------------------------------------------------------------------------
 
 	@Override
 	public void newGame() {
 		score = 0;
 		level = 1;
 		// --
 		blocks = new Block[height][width];
 		// --
 		gameState = EGameState.PLAYING;
 		// --
 		notifyGameStarted();
 		// --
 		notifyScoreChanged();
 		notifyLevelChanged();
 		notifyBlocksChanged();
 		// --
 		try {
 			newStone();
 		} catch (InvalidActionException e) {
 			// ignore
 		}
 	}
 
 	// ---------------------------------------------------------------------------
 
 	private void checkState() throws InvalidActionException {
 		if (gameState == EGameState.PAUSED || gameState == EGameState.GAMEOVER) {
 			throw new InvalidActionException("action not allowed");
 		}
 	}
 
 	// ---------------------------------------------------------------------------
 
 	@Override
 	public void newStone() throws InvalidActionException {
 		checkState();
 		// --
		activeStone = new Stone();
 		// --
 		activeStone.move((width / 2) + (activeStone.getWidth() > 1 ? -1 : 0), 0);
 		// --
 		if (checkCollision()) {
 			gameState = EGameState.GAMEOVER;
 			notifyGameOver();
 			return;
 		}
 		// --
 		notifyStoneAdded();
		// --
		nextStone = new Stone();
 		notifyAnnounceNextStone();
 	}
 	// ---------------------------------------------------------------------------
 
 	@Override
 	public long getScore() {
 		return this.score;
 	}
 
 	// ---------------------------------------------------------------------------
 
 	@Override
 	public int getLevel() {
 		return this.level;
 	}
 
 	// ---------------------------------------------------------------------------
 
 	@Override
 	public Block[][] getBlocks() {
 		return blocks.clone();
 	}
 
 	// ---------------------------------------------------------------------------
 
 	@Override
 	public Stone getCurrentStone() {
 		return this.activeStone;
 	}
 
 	// ---------------------------------------------------------------------------
 
 	@Override
 	public Stone getNextStone() {
 		return null;
 	}
 
 	// ---------------------------------------------------------------------------
 
 	@Override
 	public void rotateStoneClockwise() throws InvalidActionException {
 		checkState();
 		// --
 		synchronized (activeStone) {
 			activeStone.rotateClockwise();
 			// --
 			if (checkCollision()) {
 				activeStone.rotateCounterClockwise();
 			} else {
 				notifyStoneMoved(activeStone.getX(), activeStone.getY());
 			}
 		}
 	}
 
 	@Override
 	public void rotateStoneCounterClockwise() throws InvalidActionException {
 		checkState();
 		// --
 		synchronized (activeStone) {
 			this.activeStone.rotateCounterClockwise();
 			// --
 			if (checkCollision()) {
 				activeStone.rotateClockwise();
 			} else {
 				notifyStoneMoved(activeStone.getX(), activeStone.getY());
 			}
 		}
 	}
 
 	// ---------------------------------------------------------------------------
 
 	@Override
 	public void moveStoneLeft() throws InvalidActionException {
 		checkState();
 		// --
 		synchronized (activeStone) {
 			int x = activeStone.getX();
 			int y = activeStone.getY();
 			// --
 			activeStone.move(x - 1, y);
 			// --
 			if (checkCollision()) {
 				activeStone.move(x, y);
 			} else {
 				notifyStoneMoved(x, y);
 			}
 		}
 	}
 
 	// ---------------------------------------------------------------------------
 
 	@Override
 	public void moveStoneRight() throws InvalidActionException {
 		checkState();
 		// --
 		synchronized (activeStone) {
 			int x = activeStone.getX();
 			int y = activeStone.getY();
 			// --
 			activeStone.move(x + 1, y);
 			// --
 			if (checkCollision()) {
 				activeStone.move(x, y);
 			} else {
 				notifyStoneMoved(x, y);
 			}
 		}
 	}
 
 	// ---------------------------------------------------------------------------
 
 	@Override
 	public void moveStoneDown() throws InvalidActionException {
 		checkState();
 		// --
 		synchronized (activeStone) {
 			int x = activeStone.getX();
 			int y = activeStone.getY();
 			// --
 			activeStone.move(x, y + 1);
 			// if this move leads to a collision -> undo move and add to fixed
 			// blocks
 			if (checkCollision()) {
 				activeStone.move(x, y);
 				stoneToBlocks();
 				newStone();
 			} else {
 				notifyStoneMoved(x, y);
 			}
 		}
 	}
 
 	// ---------------------------------------------------------------------------
 
 	@Override
 	public void moveStoneToBottom() throws InvalidActionException {
 		checkState();
 		// --
 		synchronized (activeStone) {
 			int x = activeStone.getX();
 			int y = activeStone.getY();
 			// --
 			for (int i = 1; i <= height; i++) {
 				activeStone.move(x, y + i);
 				if (checkCollision()) {
 					activeStone.move(x, y + (i - 1));
 					break;
 				}
 			}
 			// --
 			notifyStoneMoved(x, y);
 			// --
 			calculateScoreFreefall(activeStone.getY() - y);
 			// --
 			stoneToBlocks();
 			newStone();
 		}
 	}
 
 	// ---------------------------------------------------------------------------
 
 	@Override
 	public void addFieldChangedListener(GameFieldChangedListener listener) {
 		if (!fieldListeners.contains(listener)) {
 			fieldListeners.add(listener);
 		}
 	}
 
 	@Override
 	public void removeFieldChangedListener(GameFieldChangedListener listener) {
 		if (fieldListeners.contains(listener)) {
 			fieldListeners.remove(listener);
 		}
 
 	}
 
 	@Override
 	public void addDataChangedListener(GameDataChangedListener listener) {
 		if (!dataListeners.contains(listener)) {
 			dataListeners.add(listener);
 		}
 	}
 
 	@Override
 	public void removeDataChangedListener(GameDataChangedListener listener) {
 		if (dataListeners.contains(listener)) {
 			dataListeners.remove(listener);
 		}
 
 	}
 
 	// ---------------------------------------------------------------------------
 
 	private void stoneToBlocks() {
 		for (Block b : activeStone.getBlocks()) {
 			blocks[activeStone.getY() + b.getY()][activeStone.getX() + b.getX()] = b;
 		}
 		// --
 		notifyBlocksChanged();
 		// --
 		checkForFullRows();
 	}
 
 	// ---------------------------------------------------------------------------
 
 	private boolean checkCollision() {
 		for (Block b : activeStone.getBlocks()) {
 			int x = b.getX() + activeStone.getX();
 			int y = b.getY() + activeStone.getY();
 			// --
 			if (y >= height) {
 				return true;
 			}
 			if (x >= width) {
 				return true;
 			}
 			if (x < 0) {
 				return true;
 			}
 			// --
 			if (blocks[y][x] != null) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	// ---------------------------------------------------------------------------
 
 	private void checkForFullRows() {
 		ArrayList<Integer> tmpRows = new ArrayList<Integer>();
 		// --
 		for (int i = 0; i < height; i++) {
 			boolean full = true;
 			// --
 			for (int j = 0; j < width; j++) {
 				if (blocks[i][j] == null) {
 					full = false;
 					break;
 				}
 			}
 			// --
 			if (full) {
 				tmpRows.add(i);
 			}
 		}
 		// --
 		if (tmpRows.size() > 0) {
 			// --
 			for (int row : tmpRows) {
 				for (int i = row - 1; i >= 0; i--) {
 					for (int j = 0; j < width; j++) {
 						blocks[i + 1][j] = blocks[i][j];
 					}
 				}
 			}
 			// --
 			calculateScore(tmpRows.size());
 		}
 	}
 
 	// ---------------------------------------------------------------------------
 
 	private void calculateScore(int removedRowCount) {
 		if (removedRowCount > 0) {
 			//@formatter:off
 			switch (removedRowCount) {
 				case 1: score += (100 * level); backToBack = false; break;
 				case 2: score += (300 * level); backToBack = false; break;
 				case 3: score += (500 * level); backToBack = false; break;
 				case 4:
 					if (backToBack) score += (800 * level) / 2;
 					score += (800 * level); 
 					backToBack = true; 
 					break;
 			}		
 			//@formatter:on			
 			notifyScoreChanged();
 			calculateLevel();
 		}
 	}
 
 	// ---------------------------------------------------------------------------
 
 	private void calculateScoreFreefall(int distance) {
 		score += 2 * distance;
 		// --
 		notifyScoreChanged();
 		calculateLevel();
 	}
 
 	// ---------------------------------------------------------------------------
 
 	private void calculateLevel() {
 		int tmpLevel = (int) (score / LEVEL_THRESHOLD) + 1;
 		// --
 		if (tmpLevel != this.level) {
 			if (tmpLevel <= MAX_LEVEL) {
 				this.level = tmpLevel;
 			} else {
 				this.level = MAX_LEVEL;
 			}
 			notifyLevelChanged();
 		}
 	}
 
 	// ---------------------------------------------------------------------------
 
 	private void notifyAnnounceNextStone() {
 		for (GameFieldChangedListener l : fieldListeners) {
 			l.announceNextStone(nextStone);
 		}
 	}
 	private void notifyStoneAdded() {
 		for (GameFieldChangedListener l : fieldListeners) {
 			l.newStone(activeStone);
 		}
 	}
 	private void notifyStoneMoved(int xOld, int yOld) {
 		for (GameFieldChangedListener l : fieldListeners) {
 			l.stoneMoved(activeStone, xOld, yOld);
 		}
 	}
 	private void notifyBlocksChanged() {
 		for (GameFieldChangedListener l : fieldListeners) {
 			l.blocksChanged(blocks);
 		}
 	}
 
 	// ---------------------------------------------------------------------------
 
 	private void notifyScoreChanged() {
 		for (GameDataChangedListener l : dataListeners) {
 			l.scoreChanged(score);
 		}
 	}
 	private void notifyLevelChanged() {
 		for (GameDataChangedListener l : dataListeners) {
 			l.levelChanged(level);
 		}
 	}
 	private void notifyGameStarted() {
 		for (GameDataChangedListener l : dataListeners) {
 			l.gameStarted();
 		}
 	}
 	private void notifyGameOver() {
 		for (GameDataChangedListener l : dataListeners) {
 			l.gameOver();
 		}
 	}
 }
