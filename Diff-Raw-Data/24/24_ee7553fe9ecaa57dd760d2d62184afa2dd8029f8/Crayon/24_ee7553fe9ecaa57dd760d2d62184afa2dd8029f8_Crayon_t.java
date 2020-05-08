 package pxlJam;
 import java.awt.Graphics;
 
 import cells.Cell;
 
 
 public class Crayon {
 	private ImageWrapper image;
 	
 	private int x;
 	private int y;
 	public static final int CRAYON_WIDTH  = 50;
 	public static final int CRAYON_HEIGHT = 50;
 	
 	private String filename = "Sprites/crayonMovementNew.png";
 	
 	private Hitbox hitbox;
 	
 	private int spriteStage;
 
 	public Crayon(int x, int y){
 		this.x = x;
 		this.y = y;
 		image = new ImageWrapper(filename);
 		hitbox = new Hitbox(x, y, CRAYON_WIDTH, CRAYON_HEIGHT);
 		spriteStage = 0;
 	}
 	
 	public void move(int x){
 		this.x += x;
 		spriteStage++;
 		if(spriteStage == image.getNumSprites())
 			spriteStage = 0;
		
		hitbox.setHitbox(this.x, this.y, CRAYON_WIDTH, CRAYON_HEIGHT);
	}
	
	private void walk(){
		
 	}
 	
 	public void checkCollision(Cell[][] tiles){
 		for(int i = 0; i < tiles.length; i++){
 			for(int j = 0; j < tiles[i].length; j++){
 				hitbox.checkCollision(tiles[i][j].getHitbox());
 			}
 		}
 	}
 	
 	public void draw(Graphics g){
 		image.draw(g, x, y, CRAYON_WIDTH, CRAYON_HEIGHT, spriteStage);
 	}
 
 	private int getX() {
 		return x;
 	}
 
 	private void setX(int x) {
 		this.x = x;
 	}
 
 	private int getY() {
 		return y;
 	}
 
 	private void setY(int y) {
 		this.y = y;
 	}
 
 
 	private ImageWrapper getImage() {
 		return image;
 	}
 
 	private void setImage(ImageWrapper image) {
 		this.image = image;
 	}
 
 	public Hitbox getHitbox() {
 		return hitbox;
 	}
 
 	public void setHitbox(Hitbox hitbox) {
 		this.hitbox = hitbox;
 	}
 	
 }
