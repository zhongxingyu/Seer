 package com.parasitefrog.colorfall.screens;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.InputListener;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.parasitefrog.colorfall.ColorFallGame;
 import com.parasitefrog.colorfall.game.AbstractToken;
 import com.parasitefrog.colorfall.game.Assets;
 import com.parasitefrog.colorfall.game.Brick;
 
 public class GameScreen extends AbstractScreen {
 	private Label lblScore = new Label(" - Score - ", getSkin());
 	private Label lblLevel = new Label(" - Level - ", getSkin());
 	
 	// pixel coordinates of the lower left corner
 	public static final int FIELD_CORNER_X = 89;
 	public static final int FIELD_CORNER_Y = 40;
 	// grid size in pixels
 	public static final int GRID_SIZE = 20;
 	
 	private static final int SCORE_PER_LINE = 5;
 	// initial tick time
 	private static final float TICK_INITIAL = 0.85f;
 	// tick level increase
 	private static final float TICK_INCREASE = 0.05f;
 	// tick time constant
 	private static float TICK_TIME = TICK_INITIAL - TICK_INCREASE * HighscoreScreen.currentScore.getLevel();
 	// ticktime value
 	private float tickDelta = 0.0f;
 	
 	private static final int NEXT_TOKEN_POS_X = 410;
 	private static final int NEXT_TOKEN_POS_Y = 350;
 	
 	private ArrayList<Integer> flushedLines = new ArrayList<Integer>();
 	
 	public GameScreen(ColorFallGame game) {
 		super(game);
 	}
 
 	AbstractToken activeToken;
 	AbstractToken nextToken;
 	Group gameField;
 	
 	@Override
 	public void show() {
 		super.show();
 		
 		Image bg = new Image(Assets.getTextureAtlas(Assets.TEXTUREATLAS_GAMEPACK).createSprite("game"));
 		stage.addActor(bg);
 		
 		HighscoreScreen.currentScore.setLevel(1);
 		HighscoreScreen.currentScore.setNickname("nickname");
 		HighscoreScreen.currentScore.setScore(0);
 		
 		newToken();
 		lblScore.setText(""+HighscoreScreen.currentScore.getScore());
 		lblLevel.setText(""+HighscoreScreen.currentScore.getLevel());
 		lblScore.setPosition(650, 380);
 		lblLevel.setPosition(650, 340);
 		stage.addActor(lblScore);
 		stage.addActor(lblLevel);
 		stage.addListener(new InputListener() {
 			
 			@Override
 			public boolean keyTyped(InputEvent event, char character) {
 				
 				if(character == ' ') {
 					AbstractToken actToken = activeToken;
 					while(actToken == activeToken) {
 						activeToken.moveDown();
 						checkCollision(activeToken, true);
 						tickDelta = 0f;
 					}
 					return true;
 				}
 				if(character == 'a') {
 					activeToken.moveLeft();
 					return checkCollision(activeToken, false);
 				}
 				if(character == 'd') {
 					activeToken.moveRight();
 					return checkCollision(activeToken, false);
 				}
 				if(character == 'w') {
 					activeToken.rotateClockwise();
 					return checkCollision(activeToken, false);
 				}
 				if(character == 'q') {
 					activeToken.rotateAntiClockwise();
 					checkCollision(activeToken, false);
 					return true;
 				}
 				if(character == 's') {
 					activeToken.moveDown();
 					Assets.playSound(Assets.SOUND_DROP);
 					checkCollision(activeToken, true);
 					tickDelta = 0f;
 					return true;
 				}
 				
 				return false;
 				
 			}
 
 			@Override
 			public boolean keyDown(InputEvent event, int keycode) {
 
 				if(keycode == Input.Keys.LEFT) {
 					activeToken.moveLeft();
 					return checkCollision(activeToken, false);
 				}
 				if(keycode == Input.Keys.RIGHT) {
 					activeToken.moveRight();
 					return checkCollision(activeToken, false);
 				}
 				if(keycode == Input.Keys.DOWN) {
 					activeToken.moveDown();
 					Assets.playSound(Assets.SOUND_DROP);
 					checkCollision(activeToken, true);
 					tickDelta = 0f;
 					return true;
 				}
 				if(keycode == Input.Keys.UP) {
 					activeToken.rotateClockwise();
 					checkCollision(activeToken, false);
 					return true;
 				}
 				
 				return false;
 			}
 			
 		});
 		
 		gameField = new Group();
 		gameField.setSize(200, 400);
 		gameField.setPosition(FIELD_CORNER_X, FIELD_CORNER_Y);
 		stage.addActor(gameField);
 	}
 	
 	/**
 	 * Check for collisions
 	 * @param token
 	 * @return a boolean indicating if no collision was detected; no collision -> true
 	 */
 	private boolean checkCollision(AbstractToken token, boolean moveDown) {
 		boolean shouldMove = true;
 		for(Actor child: token.getChildren().toArray()) {
 			Brick brick = (Brick) child;
 			Gdx.app.log(ColorFallGame.LOG, "Brick Position = "+brick.getGridPosition());
 			// too far to the right
 			while(brick.getGridPosition().x > 9) {
 				token.moveLeft();
 				shouldMove = false;
 			}
 			// too far to the left
 			while(brick.getGridPosition().x < 0) {
 				token.moveRight();
 				shouldMove = false;
 			}
 			// too far to the bottom
 			if(brick.getGridPosition().y > 19) {
 				token.moveUp();
 				newToken();
 				return false;
 			}
 			for(Actor fieldBrick : gameField.getChildren().toArray()) {
 				Gdx.app.log(ColorFallGame.LOG, "fieldbrick Position = "+((Brick) fieldBrick).getGridPosition());
 				if(((Brick) fieldBrick).getGridPosition().equals(brick.getGridPosition())) {
 					activeToken.resetPosition();
 					if(moveDown) newToken();
 					return false;
 				}
 			}
 		}
 		return shouldMove;
 	}
 	
 	private ArrayList<ArrayList<Actor>> getFullLines() {
 		ArrayList<ArrayList<Actor>> lines = new ArrayList<ArrayList<Actor>>();
 		for(int y = 0; y <= 19; y++) {
 			ArrayList<Actor> bricks = new ArrayList<Actor>(10);
 			for(int x = 0; x <= 9; x++) {
 				Vector2 coords = getPixelCoordinatesFromGridPosition(new Vector2(x, y));
 				Actor brick = null;
				if((brick = stage.hit(coords.x, coords.y, false)) != null && brick.getClass() == Brick.class && !brick.getParent().equals(stage.getRoot())) {
 					bricks.add(brick);
 				}
 			}
 			if(bricks.size() == 10) {
 				lines.add(bricks);
 				flushedLines.add(y);
 			}
 		}
 		return lines;
 	}
 	
 	/**
 	 * Places a new Token to the game field, sets the upcoming token and puts the bricks onto the gamefield
 	 */
 	private void newToken() {		
 		if(activeToken != null) {
 			for(Actor brick : activeToken.getChildren().toArray()) {
 				// get absolute pixel coords from relative pixel coords
 				Vector2 brickAbsolutePixelCoords = getPixelCoordinatesFromGridPosition(((Brick) brick).getGridPosition());
 				brick.setPosition(brickAbsolutePixelCoords.x - FIELD_CORNER_X, brickAbsolutePixelCoords.y - FIELD_CORNER_Y);
 				// pick the bricks of the activeToken and put them to the gameField
 				gameField.addActor(brick);
 				activeToken.removeActor(brick);
 			}
 			activeToken.remove();
 		} else {
 			nextToken = AbstractToken.getRandomToken();
 		}
 		
 		activeToken = nextToken;
 		Vector2 coordinate = getPixelCoordinatesFromGridPosition(new Vector2(activeToken.START_X, activeToken.START_Y));
  		activeToken.setPosition(coordinate.x, coordinate.y);
 		stage.addActor(activeToken);
 		stage.setKeyboardFocus(activeToken);
 		
 		nextToken = AbstractToken.getRandomToken();
 		nextToken.setPosition(NEXT_TOKEN_POS_X, NEXT_TOKEN_POS_Y);
 		stage.addActor(nextToken);
 	}
 
 	@Override
 	public void render(float delta) {
 		
 		// Tick implementation
 		tickDelta += delta;
 		if(tickDelta >= TICK_TIME) {
 			activeToken.moveDown();
 			Assets.playSound(Assets.SOUND_DROP);
 			checkCollision(activeToken, true);
 			tickDelta = 0f;
 			
 			// clear full lines
 			for(ArrayList<Actor> actors : getFullLines()) {
 				for(Actor brick : actors) {
 					Assets.playSound(Assets.SOUND_LINE);
 					brick.remove();
 				}
 			}
 			
 			if(flushedLines.size() > 0) {
 				System.out.println(flushedLines);
 				
 				HighscoreScreen.currentScore.setLines(HighscoreScreen.currentScore.getLines()+flushedLines.size());
 				HighscoreScreen.currentScore.setLevel(1+HighscoreScreen.currentScore.getLines()/10);
 				HighscoreScreen.currentScore.setScore(HighscoreScreen.currentScore.getScore()+flushedLines.size()*2*SCORE_PER_LINE);
 				
 				lblScore.setText(""+HighscoreScreen.currentScore.getScore());
 				lblLevel.setText(""+HighscoreScreen.currentScore.getLevel());
 				
 				TICK_TIME = TICK_INITIAL - TICK_INCREASE * HighscoreScreen.currentScore.getLevel();
 			}
 			// move bricks down if lines where flushed
 			for(Integer line : flushedLines) {
 				for(Actor brick : gameField.getChildren().toArray()) {
 					if(getGridPositionFromPixelCoordinates(brick.getX() + FIELD_CORNER_X, brick.getY() + FIELD_CORNER_Y).y < (float) line) {
 						brick.setY(brick.getY() - GameScreen.GRID_SIZE);
 					}
 				}
 			}
 			flushedLines.clear();
 		}
 		super.render(delta);
 	}
 	
 	public static Vector2 getPixelCoordinatesFromGridPosition(Vector2 gridCoords) {
 		if(gridCoords.x > 9 || gridCoords.y > 19) throw new RuntimeException("gridX[0,9], gridY[0,19] -> actual values: gridX = " + gridCoords.x + ", gridY = " + gridCoords.y);
 		// gridX = 0, gridY = 0 -> upper left corner
 		return new Vector2(FIELD_CORNER_X + (gridCoords.x * GRID_SIZE), FIELD_CORNER_Y + ((19 - gridCoords.y) * GRID_SIZE));
 	}
 	
 	public static Vector2 getGridPositionFromPixelCoordinates(float x, float y) {
 		return new Vector2((x - GameScreen.FIELD_CORNER_X) / GameScreen.GRID_SIZE, 19 - (y - GameScreen.FIELD_CORNER_Y) / GameScreen.GRID_SIZE);
 	}
 
 }
