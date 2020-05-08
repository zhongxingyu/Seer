 package gameObjects;
 
 import java.util.Random;
 import java.util.Stack;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.me.mygdxgame.Mule;
 
 /**
  * The Map which holds the tile elements.
  * @author DavidW
  *
  */
 public class Map {
 	
 	private final int TOWN_X = 4;
 	private final int TOWN_Y = 2;
 	
 	Tile[][] tiles;
 	/*
 	 * The tile list starts at 0,0 at the bottom left hand corner
 	 *
 	 *	0,3		1,3		2,3		3,3
 	 *	0,2		1,2		2,2		3,2
 	 *	0,1		1,1		2,1		3,1
 	 *	0,0		1,0		2,0		3,0
 	 *
 	 *	This formating takes the column number as the x and the row number as the y
 	 */
 	
 	public PlayerToken playerT;
 	
 	public boolean drawPlayer;
 	
 	private float ppuX, ppuY;
 	private Random r = new Random();
 	private boolean randomOn = false; //Change this based on whether or not we want a random map
 	private boolean highlighting = true;
 	
 	public Map(boolean randomOn){
 		this.randomOn = randomOn;
 		if(!randomOn){
 			defaultMap();
 		} else {
 			generateRandomMap();
 		}
 		loadTextures();
 		drawPlayer = true;
 	}
 	
 	public Map(boolean randomOn, boolean drawP){
 		this(randomOn);
 		drawPlayer = drawP;
 	}
 	
 	public void defaultMap(){
 		tiles = new Tile[9][5];
 		
 		//Creates Tiles and defaults them to Plains
 		for(int i = 0; i < tiles.length; i++){
 			for(int c = 0; c < tiles[i].length; c++){
 				tiles[i][c] = new Tile(i,c);
 			}
 		}
 		//River
 		for(int i = 0; i < tiles[0].length; i ++){
 			tiles[4][i].setType(1);
 		}
 		//Town
 		tiles[4][2].setType(2);
 		
 		//One Ridge Mountains
 		tiles[2][0].setType(3);
 		tiles[2][3].setType(3);
 		tiles[7][1].setType(3);
 		
 		//Two Ridge Mountains
 		tiles[3][2].setType(4);
 		tiles[6][2].setType(4);
 		tiles[8][4].setType(4);
 		tiles[0][4].setType(4);
 		
 		//Three Ridge Mountains
 		tiles[2][1].setType(5);
 		tiles[5][0].setType(5);
 		tiles[5][3].setType(5);
 	}
 	
 	private void generateRandomMap(){
 		Stack<Integer> tileTypes = new Stack<Integer>();
 		tiles = new Tile[9][5];
 		//Using set number of tiles here. For bigger maps this will hae to be changed
 		for(int i = 0; i < 45; i++){
 			double roll = r.nextDouble() * 100;
 			if(roll > 30.){
 				tileTypes.add(0);
 			} else if( 30. >= roll && roll > 20.){
 				tileTypes.add(3);
 			} else if( 20. >= roll && roll > 10.){
 				tileTypes.add(4);
 			} else if( 10. >= roll && roll >= 0.){
 				tileTypes.add(5);
 			}
 		}
 		
 		tiles = new Tile[9][5];
 		for(int i = 0; i < tiles.length; i++){
 			for(int c = 0; c < tiles[i].length; c++){
 				tiles[i][c] = new Tile(i,c);
 				tiles[i][c].setType(tileTypes.pop());
 			}
 		}
 		
 		//River
 		for(int i = 0; i < tiles[0].length; i ++){
 			tiles[4][i].setType(1);
 		}
 		//Town
 		tiles[4][2].setType(2);
 	}
 	
 	/**
 	 * Updates both the map and any potential objects inside the map
 	 * @param delta
 	 */
 	private float count = 0;
 	public void update(float delta){
 		if(playerT == null){
 			this.playerT = new PlayerToken(Mule.pm.getCurrentPlayer(),0,0);
 			drawPlayer = true;
 		}
 	}
 	
 	public void loadTextures(){
 		Tile.loadTextures();
 	}
 	
 	public void draw(SpriteBatch sprites){
 		ShapeRenderer sr = new ShapeRenderer();
 		
 		/*
 		 * Drawing black grid along with tiles
 		 */
 		sr.begin(ShapeRenderer.ShapeType.Line);
 		sr.setColor(Color.BLACK);
 		for(int i = 0; i < tiles.length; i++){
 			sr.line(i * ppuX, 0, i * ppuX, 5 * ppuY);
 			for(int c = 0; c < tiles[i].length; c++){
 				tiles[i][c].draw(sprites);
 			}
 		}
 
 		for(int i = 0; i < tiles[i].length; i++){
 			sr.line(0, i * ppuY, 9 * ppuX, i * ppuY);
 		}
 		sr.end();
 		
 		
 		/*
 		 * Draw ALL COLORS for ownership
 		 */
 		for(int i = 0; i < tiles.length; i++){
 			for(int c = 0; c < tiles[i].length; c++){
 				tiles[i][c].drawOwner(sprites);
 				if(!drawPlayer) tiles[i][c].drawHighlight(sprites);
 			}
 		}
 
 		/*
 		 * Player has to be drawn over the sprites so he doens't "walk" under them.
 		 */
 		if(this.playerT != null && drawPlayer){
 			this.playerT.draw(sprites, 1);
 		}
 	}
 	
 	public void setDrawPlayer(boolean b){
 		drawPlayer = b;
 	}
 	
 	
 	public Tile getMouseClickedTile(int x, int y){
 		int a = (int) (x / ppuX);
 		int b = (int) ((Mule.HEIGHT - y) / ppuY);
 		if(tiles[a].length <= b){
 			return null;
 		}
 		return tiles[a][b];
 	}
 	
 	public void moveUp(){
 		if(playerT.getY()<=Mule.HEIGHT-135)
 		this.playerT.moveUp();
 	}
 
 	public void moveRight(){
 		if(playerT.getX()<=Mule.WIDTH-55)
 		this.playerT.moveRight();
		System.out.println(playerT.getX() + ", " + playerT.getY());
 	}
 	public void moveDown(){
 		if(playerT.getY()>=5)
 		this.playerT.moveDown();
 	}
 	public void moveLeft(){
 		if(playerT.getX()>=5)
 		this.playerT.moveLeft();
 	}
 	
 	/**
 	 * True if the playerToken and Town tile are overlapping
 	 */
 	public boolean changeToTown(){
 		if(this.playerT == null){
 			this.playerT = new PlayerToken(Mule.pm.getCurrentPlayer(), 0, 0);
 			System.out.println("MAKING NEW PLAYER TOKEN");
 		} else if (!this.playerT.compareTo(Mule.pm.getCurrentPlayer())){
 			playerT = new PlayerToken(Mule.pm.getCurrentPlayer(), 0, 0);
 			System.out.println("MAKING NEW PLAYER TOKEN");
 		}
 		float tX = 4.5f * ppuX;	//In order to get center of tile use .5 more than tile number
 		float tY = 2.5f * ppuY;
 		float pX = playerT.getX() + 25;	//Center of player
 		float pY = playerT.getY() + 25;
 		
 		float a = (tX - pX) * (tX - pX);
 		float b = (tY - pY) * (tY - pY);
 		float c = (float)Math.sqrt((a * a) + (b * b));
 		if(c < 1000) return true;
 		return false;
 	}
 
 	
 	public void setPPU(float x, float y){
 		ppuX = x;
 		ppuY = y;
 	}
 	
 	public String toString(){
 		String s = "";
 		s += "RANDOM : " + randomOn;
 		s += "\n";
 		for(int i = 0 ; i < tiles.length ; i ++){
 			for(int j = 0 ; j < tiles[i].length ; j++){
 				s += tiles[i][j];
 			}
 			s += "\n";
 		}
 		return s;
 	}
 	
 	public void putBelowTown(){
 		playerT.setX((int)ppuX * TOWN_X);
 		playerT.setY((int)ppuY * (TOWN_Y - 1));
 	}
 	
 	public Tile[][] getTiles(){
 		return tiles;
 	}
 	
 	public PlayerToken getToken(){
 		return playerT;
 	}
 }
