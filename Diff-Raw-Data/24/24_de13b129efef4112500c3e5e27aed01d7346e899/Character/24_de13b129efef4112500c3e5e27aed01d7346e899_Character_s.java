 import java.awt.Graphics;
 
 public class Character implements Drawable{
 	private ImageWrapper image;
 	private int height;
 	private int width;
 	private int x;
 	private int y;
 	
 	private int moveDistance = 8;
 	
 	private boolean isJumping = false;
 	private boolean isWalking = false;
 	private boolean isFalling = false;
 	private boolean direction = true;
 	private int maxJump;
 	private int startHeight;
 	
 	private Hitbox movementBox;
 	private Hitbox shootBox;
 	
 	private SpriteSheet sprite;
 	
 	public Character(int x, int y, SpriteSheet sprites)
 	{
 		this.setX(x);
 		this.setY(y);
 		
 		this.sprite = sprites;
 		this.height = 96;
 		this.width = 64;
		image = new ImageWrapper(1, width, height, sprites);	//-1 cause of offset initialization
 		this.movementBox = new Hitbox(x, y, width, height);
 	}
 	
 	public void draw(Graphics g)
 	{
 		image.draw(g, x, y, width, height);
 	}
 	
 	public void move(Tile[][] map)
 	{
 		if(isJumping){
 			y -= moveDistance;
 			this.movementBox.setHeight(y);
 			if(y<maxJump)
 			{
 				isJumping = false;  
 				isFalling = true;
 			}
 		}else{
 			boolean falling = true;
 			for(int x=this.x; x<this.x+this.width; x++){
 				if(this.movementBox.checkCollision(map[x/32][(this.y+this.height)/32].getHitbox())){
 					falling = false;
 					break;
 				}
 			}
 			if(falling){
 				isFalling = true;
 				this.y+=moveDistance;
 				this.movementBox.setY(y);
 			}else{
 				this.y = this.y/32*32;
 				isFalling = false;
 			}
 		}
 		
 		
 		if(isWalking){
 			int movement = moveDistance;
			boolean colliding = false;
 			if(!direction){movement = -moveDistance;}
			
			
			
			if(!colliding){
				x += movement;
				this.movementBox.setX(x);
			}	
 			isWalking=false;
 		}
 	}
 	
 	public void jump()
 	{
 		if(isJumping || isFalling){return;}
 		
 		isJumping = true;
 		maxJump = y - 32*4;  //4 blocks of 32
 	}
 	
 
 	public int getY() {
 		return y;
 	}
 
 	public void setY(int y) {
 		this.y = y;
 	}
 
 	public int getX() {
 		return x;
 	}
 	
 	public void walk(boolean direction){
 		this.direction = direction;
 		this.isWalking = true;
 	}
 	
 	public void setX(int x) {
 		this.x = x;
 	}
 
 	public ImageWrapper getImage() {
 		return image;
 	}
 
 	public void setImage(ImageWrapper image) {
 		this.image = image;
 	}
 
 	public Hitbox getMovementBox() {
 		return movementBox;
 	}
 
 	public void setMovementBox(Hitbox movementBox) {
 		this.movementBox = movementBox;
 	}
 
 	public Hitbox getShootBox() {
 		return shootBox;
 	}
 
 	public void setShootBox(Hitbox shootBox) {
 		this.shootBox = shootBox;
 	}
 }
