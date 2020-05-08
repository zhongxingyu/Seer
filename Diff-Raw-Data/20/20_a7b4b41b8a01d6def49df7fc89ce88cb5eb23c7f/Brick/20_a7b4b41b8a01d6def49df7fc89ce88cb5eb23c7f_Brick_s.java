 package com.parasitefrog.colorfall.game;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 
 public class Brick extends Actor {
 
 	private Color color;
 	private static final int SIZE = 20;
 	
 	public Brick(int gridPositionX, int gridPositionY) {
 		super();
 		this.color = Color.getRandomColor();
 		// initial settings
 		this.setSize(SIZE, SIZE);
 		this.setGridPositionX(gridPositionX);
 		this.setGridPositionY(gridPositionY);
 	}
 
 	@Override
 	public void draw(SpriteBatch batch, float parentAlpha) {
 		super.draw(batch, parentAlpha);
 		
 		TextureAtlas region = new TextureAtlas(Gdx.files.internal("game/game.pack"));
 		Sprite sprite = region.createSprite(color.toString().toLowerCase());
 		sprite.setSize(Brick.SIZE, Brick.SIZE);
 		sprite.draw(batch, parentAlpha);
 		
 	}
 	
 	/**
 	 * Sets the Position of the Brick in the Grid of the Token.<br>
 	 * x may be between 0 and the size of the grid minus 1, e.g. if the grid size is 4 the max value of x may be 3.
 	 */
 	public void setGridPositionX(int x) {
 		if(x < 0 || x > AbstractToken.GRID_SIZE) {
 			throw new RuntimeException("The value of x is outside of the Token-Grid (x=" + x + ", GRID_SIZE=" + AbstractToken.GRID_SIZE + ")");
 		}
 		
 		this.setX(x * SIZE);
 	}
 	
 	/**
 	 * Sets the Position of the Brick in the Grid of the Token.<br>
 	 * y may be between 0 and the size of the grid minus 1, e.g. if the grid size is 4 the max value of y may be 3.
 	 */
 	public void setGridPositionY(int y) {
 		if(y < 0 || y > AbstractToken.GRID_SIZE) {
 			throw new RuntimeException("The value of y is outside of the Token-Grid (y=" + y + ", GRID_SIZE=" + AbstractToken.GRID_SIZE + ")");
 		}
 		
 		this.setY(y * SIZE);
 	}
 	
 	public int getGridPositionsX() {
 		if(this.getX() < 1) {
 			return 0;
 		}
 		return (int)(this.getX() / SIZE);
 	}
 	
 	public int getGridPositionsY() {
 		if(this.getY() < 1) {
 			return 0;
 		}
 		return (int)(this.getY() / SIZE);
 	}
 }
