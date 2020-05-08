 package com.ladushki.lineiki;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import org.anddev.andengine.entity.text.ChangeableText;
 
 import android.graphics.Point;
 
 public class GameLogic implements IGameEvent {
 	
 	private enum GameState {
 		SELECT_BALL,
 		SELECT_DESTINATION,
 		MOVING_BALL,
 		DROPPING_NEW_BALLS
 	}
 
 	
 	GameState mGameState;
 	BallDispencer mDispencer; 
 	PlayingField mPlayingField;
 	private static final Random RANDOM = new Random();
 
 	private ChangeableText mScoreField;
 	private int mScore;
 
 	
 	
 	Point mSelectedSource;
 	Point mSelectedDestination;
 
 
 	public GameLogic(PlayingField pPlayingField, BallDispencer pDispencer) {
 		mPlayingField = pPlayingField;
 		mDispencer = pDispencer;
 	}
 
 	public void startGame() {
 		
 		mScore = 0;
 		this.mGameState = GameState.SELECT_BALL;
 		this.mSelectedSource = new Point(-1, -1);
 		this.mSelectedDestination = new Point(-1, -1);
 
 		try {
 			dropNextBalls();
 		} catch (GameOverException e) {
 			// TODO
 		}
 	}
 
 	private void dropNextBalls() throws GameOverException {
 		BallColor[] next_colors = mDispencer.getNextBalls();
 		for (int i = 0; i < next_colors.length; i++) {
 			MapTile free_tile = getFreeTile();
 			if (free_tile == null) {
 				throw new GameOverException();
 			}
 			mPlayingField.addBall(free_tile, next_colors[i], i);
 		}
 		removeLines();
 
 	}
 	
 	private boolean moveBall(Point pSource, Point pDest) {
 		/*
 		 * Returns true if the ball is successfully moved, false if
 		 * it can't be moved
 		 */
 		
 		Point[] path = findPath(pSource, pDest);
 		
 		if (path == null) {
 			return false;
 		}
 		
 		mPlayingField.animateMovingBall(pSource, pDest, path);
 		
 		return true;
 	}
 	
 	public void onMovingBallFinished() {
 		removeLines();			
 		this.mGameState = GameState.SELECT_BALL;
 		try {
 			dropNextBalls();
 		} catch (GameOverException e) {
 			//gameOver(); TODO: Game over
 		}
 	}
 
 	MapTile getFreeTile() {
 		ArrayList<MapTile> freePoints = new ArrayList<MapTile>();
 		for (int j = 0; j < mPlayingField.FIELD_HEIGHT; j++) {
 			for (int i = 0; i < mPlayingField.FIELD_WIDTH; i++) {
 				final MapTile tile = mPlayingField.getTileAt(i, j);
 				if (tile.getBall() == null) {
 					freePoints.add(tile);
 				}
 			}
 		}
 		if (freePoints.size() == 0) {
 			return null;
 		}
 		
 		return freePoints.get(RANDOM.nextInt(freePoints.size()));
 	}
 		
 	Point[] findPath(Point pSource, Point pDest) {
 		/*
 		 * Finds a path from pSource to pDest, if no path exists returns null
 		 */
 		
 		PathFinder finder = new PathFinder(mPlayingField.FIELD_WIDTH, mPlayingField.FIELD_HEIGHT);
 		for (int y = 0; y < mPlayingField.FIELD_HEIGHT; y++) {
 			for (int x = 0; x < mPlayingField.FIELD_WIDTH; x++) {
 				MapTile tile = mPlayingField.getTileAt(x, y);
 				finder.setPassable(x, y, !tile.isOccupied());
 			}
 		}
 		return finder.findPath(pSource.x, pSource.y, pDest.x, pDest.y);
 	}
 	
 	void removeLines() {
 		SequenceChecker checker = new SequenceChecker();
 		
 		/// check horizontals
 		for (int j = 0; j < mPlayingField.FIELD_HEIGHT; j++) {
 			checker.startRow();
 			for (int i = 0; i < mPlayingField.FIELD_WIDTH; i++) {
 				final MapTile tile = mPlayingField.getTileAt(i, j);
 				checker.check(tile);
 			}
 		}
 		
 		/// check verticals
 		for (int j = 0; j < mPlayingField.FIELD_WIDTH; j++) {
 			checker.startRow();
 			for (int i = 0; i < mPlayingField.FIELD_HEIGHT; i++) {
				final MapTile tile = mPlayingField.getTileAt(i, j);
 				checker.check(tile);
 			}
 		}
 
 		/// check diagonals (top-right to bottom-left)
 		for (int j = 0; j < mPlayingField.FIELD_WIDTH; j++) {
 			checker.startRow();
 			for (int i = 0; i < mPlayingField.FIELD_HEIGHT; i++) {
 				int y = i; 
 				int x = j + 4 - y;
 				if (x < 0) continue;
 				if (x >= mPlayingField.FIELD_WIDTH) continue;
 				
 				final MapTile tile = mPlayingField.getTileAt(x, y);
 				checker.check(tile);
 			}
 		}
 
 		/// check diagonals (top-left to bottom-right)
 		for (int j = 0; j < mPlayingField.FIELD_WIDTH; j++) {
 			checker.startRow();
 			for (int i = 0; i < mPlayingField.FIELD_HEIGHT; i++) {
 				int y = i; 
 				int x = j - 4 + y;
 				if (x < 0) continue;
 				if (x >= mPlayingField.FIELD_WIDTH) continue;
 				
 				final MapTile tile = mPlayingField.getTileAt(x, y);
 				checker.check(tile);
 			}
 		}
 
 		MapTile[] tiles_to_remove = checker.getMatchedTiles();
 		
 		if (tiles_to_remove != null) {
 			
 			/// score stuff
 			this.addScore(tiles_to_remove.length);
 			
 			for (MapTile tile : tiles_to_remove) {
 				mPlayingField.clearTile(tile);
 			}
 		}
 	}
 
 	
 	public void setScoreField(ChangeableText pField) {
 		this.mScoreField = pField;
 		this.mScoreField.setText("!!!");
 		
 	}
 	
 	public void setScore(int pScore) {
 		this.mScore = pScore;
 		this.mScoreField.setText(Integer.toString(mScore));
 	}
 	
 	public void addScore(int pScore) {
 		setScore(this.mScore + pScore);
 	}
 
 	
 	public void onTileTouched(int x, int y) {
 		
 		final MapTile tile = mPlayingField.getTileAt(x, y);
 		final BallSprite ball = tile.getBall();
 
 		switch(this.mGameState) {
 		case SELECT_BALL:
 			if (ball != null) {
 				this.mSelectedSource.set(x,y);
 				this.mGameState = GameState.SELECT_DESTINATION;
 				tile.startBlinking();
 			}
 			break;
 		case SELECT_DESTINATION:
 			if (ball == null) {
 				/// selected an empty cell, move the ball there if possible
 				this.mSelectedDestination.set(x,y);
 				boolean ballMoved = moveBall(this.mSelectedSource, this.mSelectedDestination);
 				MapTile prevTile = mPlayingField.getTileAt(mSelectedSource.x, mSelectedSource.y);
 				prevTile.stopBlinking();
 			} else {
 				/// selected another ball, deselect the current one and select the new one
 				MapTile prevTile = mPlayingField.getTileAt(mSelectedSource.x, mSelectedSource.y);
 				prevTile.stopBlinking();
 				this.mSelectedSource.set(x,y);
 				this.mGameState = GameState.SELECT_DESTINATION;
 				tile.startBlinking();
 			}
 			break;
 		}	
 	}
 }
