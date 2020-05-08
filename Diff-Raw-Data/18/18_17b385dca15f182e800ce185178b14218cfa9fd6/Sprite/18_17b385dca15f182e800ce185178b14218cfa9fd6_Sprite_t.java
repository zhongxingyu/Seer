 import java.awt.Graphics;
 
 
 public class Sprite {
 	private final int FRAMES;
 	
 	private ImageWrapper image;
 	private int height;
 	private int width;
 	private int x;
 	private int y;
 	
 	private Hitbox movementBox, shootBox;
 	
 	private boolean direction = true;
 	private SpriteSheet sprites;
 	private int moveDistance = 8;
 	private int currentImage = 0;
 	
 	private boolean isWalking = false;
 	
 	public Sprite(int x, int y, int width, int height, String runCycle, int frames){
		setMovementBox(new Hitbox(x,y,height,width));
 		setX(x);
 		setY(y);
 		setHeight(96);
 		setWidth(64);
 		setSpriteSheet(runCycle);
 		this.FRAMES = frames;
 	}
 	
 	public void move(Map map){
 		if(isWalking){
 			int movement = moveDistance;
 			if(!getDirection()){
 				movement = -moveDistance;
 			}
 			
 			setX(getX()+movement);
 			getMovementBox().setX(getX());
 			int x = getX();
 			if(getDirection())
 				x+=getWidth();
 			for(int y=getY()+5; y<getY()+getHeight()-10; y++)
 				if(getMovementBox().checkCollision(map.getMap()[x/32][y/32].getHitbox())){
 					if(getDirection())
 						 setX(getX()/32*32);
 					else
 						setX(getX()/32*32+32);
 					break;
 				}
 			getMovementBox().setX(getX());
 			isWalking=false;
 		}
 	}
 	
 	public void draw(Graphics g){
 		image.draw(g, x, y, width, height);
 	}
 	
 	public void updateImage(ImageWrapper i){
 		if(!this.direction)
 			i.reverse();
 		this.image = i;
 	}
 	
 	public void walk(boolean direction){
 		this.currentImage += this.width/Map.BLOCK_SIZE;
 		if(this.currentImage>=(FRAMES-1)*(this.width/Map.BLOCK_SIZE))
 			this.currentImage=0;
 		updateImage(new ImageWrapper(currentImage, getWidth(), getHeight(), getSpriteSheet()));
 		setDirection(direction);
 		this.isWalking = true;
 	}
 	
 	//-------------------SETTERS---------------------------
	public void setX(int x) {
		this.x = x;
		this.movementBox.setX(x);
	}
	public void setY(int y) {
		this.y = y;
		this.movementBox.setY(y);
	}
 	public void setHeight(int h) {this.height = h;}
 	public void setWidth(int w) {this.width = w;}
 	public void setImage(ImageWrapper image) {this.image = image;}
 	public void setMovementBox(Hitbox movementBox) {this.movementBox = movementBox;}
 	public void setShootBox(Hitbox shootBox) {this.shootBox = shootBox;}
 	public void setDirection(boolean d) {this.direction = d;}
 	public void setSpriteSheet(String s) {this.sprites = new SpriteSheet(s);}
 	public void setSpriteSheet(SpriteSheet s) {this.sprites = s;}
 	public void setMoveDistance(int m) {this.moveDistance = m;}
 	public void setCurrentImage(int c) {this.currentImage = c;}
 	public void setWalking(boolean w) {this.isWalking = w;}
 	
 	//------------------GETTERS----------------------------
 	public int getY() {return this.y;}
 	public int getX() {return this.x;}
 	public int getWidth(){return this.width;}
 	public int getHeight(){return this.height;}
 	public ImageWrapper getImage() {return this.image;}
 	public Hitbox getMovementBox() {return this.movementBox;}
 	public Hitbox getShootBox() {return this.shootBox;}
 	public boolean getDirection() {return this.direction;}	
 	public SpriteSheet getSpriteSheet() {return this.sprites;}
 	public int getMoveDistance() {return this.moveDistance;}
 	public int getCurrentImage() {return this.currentImage;}
 	public boolean isWalking() {return this.isWalking;}
 }
