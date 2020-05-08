 import java.awt.Graphics;
 
 
 public class Character {
 	public static final int CHAR_WIDTH  = 32;
 	public static final int CHAR_HEIGHT = 60;
 	private int x;
 	private int y;
 	
 	private Sprite img;
 	
 	private int spriteStage;
 	private int direction;  //0 = down, 1 = left, 2 = up, 3 = right
 	
 	public Character(int x, int y){
 		this.x = x;
 		this.y = y;
 		
		img = new Sprite("Sprites/Character.png");
 		direction = 3;
 		spriteStage = 0;
 	}
 	
 	public void move(int x, int y, int direction){
 		this.x += x;
 		this.y += y;
 		this.direction = direction;
		if(spriteStage >= 0 && spriteStage < 5)
 			spriteStage++;
 		else
 			spriteStage = 0;
 	}
 
 	public int getX() {
 		return x;
 	}
 
 	public int getY() {
 		return y;
 	}
 	
 	public String toString(){
 		return "X: "+x+" Y: "+y;
 	}
 	
 	public void draw(Graphics g){
 		img.draw(g, x, y, CHAR_WIDTH, CHAR_HEIGHT, spriteStage, direction);
 	}
 }
