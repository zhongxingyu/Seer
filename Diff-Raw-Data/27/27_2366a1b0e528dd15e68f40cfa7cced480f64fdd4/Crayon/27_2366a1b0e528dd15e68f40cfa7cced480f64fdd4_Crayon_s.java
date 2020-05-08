 package pxlJam;
 import java.awt.Graphics;
 
 import cells.Cell;
 import cells.Map;
 
 public class Crayon {
 	private ImageWrapper image;
 	
 	private int x;
 	private int y;
 	public static final int CRAYON_WIDTH  = 50;
 	public static final int CRAYON_HEIGHT = 50;
 	
 	private int direction;  //-1 for left, 0 for still, 1 for right
 	private int xSpeed;
 	private int ySpeed;
 	
 	private boolean falling;
 	private static final int gravity = 1;
 	
 	private String filename = "Sprites/crayonMovementNew.png";
 	
 	private Hitbox hitbox;
 	private Hitbox feetBox;
 	private Map theMap;
 	
 	private int spriteStage;
 	long jumpTime = System.currentTimeMillis();
 	boolean jumping = false;
 	boolean moving = false;
 
 	public Crayon(int x, int y, Map map){
 		this.x = x;
 		this.y = y;
 		image = new ImageWrapper(filename);
 		hitbox = new Hitbox(x+12, y, CRAYON_WIDTH -30, CRAYON_HEIGHT);
 		feetBox = new Hitbox(x+15, y+(int)((CRAYON_HEIGHT*2)/3), CRAYON_WIDTH -30, (int)(CRAYON_HEIGHT/3));
 		spriteStage = 0;
 		ySpeed = 0;
 		theMap = map;
 	}
 	
 	public void move(Cell[][] tiles){
 		boolean sideHit = false;
 		if(!checkFeetCollision(tiles))
 			falling = true;
 		else{
 			falling = false;
 		}
 		if(direction == 1){
 			xSpeed = 4;
 			spriteStage++;
 		}
 		else if(direction == -1){
 			xSpeed = -4;
 			spriteStage++;
 		}
 		else if (!jumping && !moving){ 
 			xSpeed = 0;
 		}
 		if (System.currentTimeMillis() - jumpTime > 800 ){
 			jumping = false;
 		}
 		
 		Hitbox sideHitBox = hitbox;
 		sideHitBox.setHitbox(sideHitBox.getX()+xSpeed,sideHitBox.getY(),sideHitBox.getWidth(),sideHitBox.getHeight());
 		if (checkSideCollision(sideHitBox,tiles)){
 			sideHit = true;
 		}
 		sideHitBox.setHitbox(sideHitBox.getX()-xSpeed,sideHitBox.getY(),sideHitBox.getWidth(),sideHitBox.getHeight());
 		if (checkSideCollision(sideHitBox,tiles)){
 			sideHit = true;
 		}
 		if ((this.x+xSpeed)/Cell.CELL_WIDTH> 0 && !(sideHit)){
 			if (theMap.getHeight() - (((this.x)/Cell.CELL_WIDTH))>4){
 				this.x += xSpeed;
 			}
 		}
 		
 		if(spriteStage == image.getNumSprites()){
 			spriteStage = 0;
 			//xSpeed = 0;
 			direction = 0;
 		}
 		
 		int collideY = 0;
 		boolean collided = false;
 		
 		if(falling){
 			Hitbox newFeet = feetBox;
 			newFeet.setHitbox(this.x, this.y+ySpeed, CRAYON_WIDTH, CRAYON_HEIGHT);
 			for (int i = (feetBox.getY()/Cell.CELL_HEIGHT); i<tiles.length;i++){
 				if (i > 0){
 					for (int a = 0; a < 6; a++){
 						if (!collided&&tiles[i][(feetBox.getX()/Cell.CELL_WIDTH)+a] !=null){
 							collideY = i;
 							collided = true;
 							break;
 						}
 					}
 				}
 			}
 			if ((newFeet.getY()/Cell.CELL_HEIGHT)>=(collideY)){
 				if (collided){
 					this.y=(collideY*Cell.CELL_HEIGHT)-CRAYON_HEIGHT-1;
 				}
 				else{
 					this.y += ySpeed;
 					ySpeed += gravity;
 				}
 			}
 			else if (this.y+CRAYON_HEIGHT + ySpeed > collideY*Cell.CELL_HEIGHT){
 				if ((this.y+CRAYON_HEIGHT) - (collideY*Cell.CELL_HEIGHT) < 15){
 					this.y=(collideY*Cell.CELL_HEIGHT)-CRAYON_HEIGHT-1;
 				}
 			}
 			else{
 				this.y += ySpeed;
 				ySpeed += gravity;
 			}
 		}
 		hitbox.setHitbox(this.x+15, this.y, CRAYON_WIDTH-30, CRAYON_HEIGHT);
 		feetBox.setHitbox(x+15, y+(CRAYON_HEIGHT*2)/3, CRAYON_WIDTH -30, CRAYON_HEIGHT/3);
 
 	}
 	
 	public void jump(){
 		if (System.currentTimeMillis() - jumpTime > 800 ){
 			ySpeed = -8;
 			this.y -= 2;
 			falling = true;
 			jumpTime = System.currentTimeMillis();
 			jumping = true;
 		}
 	}
 	
 	public boolean checkCollision(Cell[][] tiles){
 		for(int i = 0; i < tiles.length; i++){
 			for(int j = 0; j < tiles[i].length; j++){
 				if(tiles[i][j] != null && hitbox.checkCollision(tiles[i][j].getHitbox())){
 					return true;
 				}
 				else if(tiles[i][j] != null && feetBox.checkCollision(tiles[i][j].getHitbox())){
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	private boolean checkSideCollision(Hitbox prospective, Cell[][] tiles){
 		int prospectiveY = prospective.getY()/Cell.CELL_HEIGHT;
 		System.out.println("PROSPECTIVE X = "+prospectiveY);
 		System.out.println("tiles.length = "+tiles.length);
 		if (prospectiveY > 0 && prospectiveY < tiles.length){
 			for(int i = 0; i < tiles[0].length; i++){
 				if(tiles[prospectiveY][i] != null && tiles[prospectiveY][i].getHitbox() != null && prospective.checkCollision(tiles[prospectiveY][i].getHitbox())){
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	private boolean checkFeetCollision(Cell[][] tiles){
 		int feetX = feetBox.getX()/Cell.CELL_WIDTH;
 		
 		for(int i = 0; i < tiles.length; i++){
 			if(tiles[i][feetX] != null && feetBox.checkCollision(tiles[i][feetX].getHitbox())){
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	public void draw(Graphics g){
 		if(direction == -1)
 			image.draw(g, x, y, CRAYON_WIDTH, CRAYON_HEIGHT, spriteStage, true);
 		else
 			image.draw(g, x, y, CRAYON_WIDTH, CRAYON_HEIGHT, spriteStage, false);
 		hitbox.draw(g);
 		feetBox.draw(g);
 	}
 
 	public int getX() {
 		return x;
 	}
 
 	private void setX(int x) {
 		this.x = x;
 	}
 
 	public int getY() {
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
 
 	public void setDirection(int direction) {
 		if(direction < 0){
 			this.direction = -1;
 		}else if(direction > 0){
 			this.direction = 1;
 		}else
 			direction = 0;
 		
 	}
 
 	public boolean isFalling() {
 		return falling;
 	}
 	
 	
 
 	public void setFalling(boolean falling) {
 		this.falling = falling;
 	}
 	
 }
