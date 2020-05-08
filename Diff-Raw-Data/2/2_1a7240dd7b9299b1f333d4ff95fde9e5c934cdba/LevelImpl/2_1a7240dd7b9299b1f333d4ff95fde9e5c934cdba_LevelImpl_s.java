 package tutorials.slickout.gameplay.level;
  
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
  
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.SpriteSheet;
 import org.newdawn.slick.geom.Circle;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Shape;
 import org.newdawn.slick.geom.Vector2f;
  
 public class LevelImpl implements ILevel {
  
 	protected ImageObject background;
 	protected CollidableImageObject rightBumper;
 	protected CollidableImageObject leftBumper;
 	protected CollidableImageObject topBumper;
  
 	protected List<Brick> bricks;
 	protected List<Ball> balls;
 	protected static List<PowerUp> powerUps;
 	public double powerUpP = 0.5;
  
 	protected Paddle paddle;
  
 	protected String[] ballArgs;
  
  
 	public static ILevel loadLevel(InputStream is) throws SlickException{
 		LevelImpl level = new LevelImpl();
  
 		BufferedReader br = new BufferedReader(new InputStreamReader(is));
 		
 		//powerUps
 		powerUps = new ArrayList<PowerUp>();
  
 		// background
 		level.setBackground( createImage( readNextValidLine( br ) ) );
  
 		// Left Bumper
 		level.setLeftBumper( createCollidableImage( readNextValidLine( br ) ) );
  
 		// Right Bumper
 		level.setRightBumper( createCollidableImage( readNextValidLine( br ) ) );
  
 		// top Bumper
 		level.setTopBumper( createCollidableImage( readNextValidLine( br ) ) );
  
 		// Paddle
 		level.setPaddle( createPaddle( readNextValidLine( br ) ) );
  
 		// Ball
 		level.setBallArgs( readNextValidLine( br ) );
  
 		// Bricks
 		try {
 			List<Brick> bricks = new ArrayList<Brick>();
  
 			while(br.ready()){
 				bricks.add( createBrick( readNextValidLine( br ) ) );
 			}
  
 			level.setBricks(bricks);
 		} catch (IOException e) {
 			throw new SlickException("Could not load Bricks", e);
 		}
 		return level;
 	}
  
 	private void setBallArgs(String[] ballArgs) {
 		this.ballArgs = ballArgs;
 	}
  
 	private static CollidableAnimationObject createCollidableAnimation(String[] args) throws SlickException{
 		// blue| ANIMATION; /data/brickanimation.png; 100,20,100 | 100, 100 | 1; RECTANGLE; 0,0, 50, 20
 		String name = args[0];
  
 		String[] imageData = args[1].split(";");
  
 		if(!imageData[0].trim().equalsIgnoreCase("ANIMATION")){
 			throw new SlickException("Animation tag is invalid");
 		}
  
 		String[] animationData = imageData[2].split(",");
  
 		SpriteSheet ss = new SpriteSheet(new Image(imageData[1]), Integer.parseInt(animationData[0]), Integer.parseInt(animationData[1]));
  
 		Animation animation = new Animation(ss, Integer.parseInt(animationData[2]));
  
 		String[] coords = args[2].split(",");
 		Vector2f position = new Vector2f(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));
  
 		String[] collisionData = args[3].split(";");
  
 		int collisionType = Integer.parseInt( collisionData[0] );
  
 		Shape shape = null;
  
 		if(collisionData[1].trim().equalsIgnoreCase("RECTANGLE")){
 			String[] size = collisionData[2].split(",");
 			shape = new Rectangle(Integer.parseInt(size[0]), Integer.parseInt(size[1]), Integer.parseInt(size[2]), Integer.parseInt(size[3]));
 		}else if(collisionData[1].trim().equalsIgnoreCase("CIRCLE")){
 			shape = new Circle(position.x, position.y, Float.parseFloat(collisionData[2])); 	
 		}
  
 		return new CollidableAnimationObject(name, animation, position, shape, collisionType);
 	}
  
 	private static Paddle createPaddle(String[] args) throws SlickException {
 		CollidableAnimationObject animation = createCollidableAnimation(args);
  
 		return new Paddle(animation.getName(), animation.getAnimation(), animation.getPosition(), animation.getNormalCollisionShape(), animation.getCollisionType());
 	}
  
 	public Ball addNewBall(){
  
 		Ball ball = null;
 		try {
 			ball = createBall( ballArgs);
 			balls.add( ball );
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
  
 		return ball;
 	}
  
 	private static Ball createBall(String[] args) throws SlickException {
  
 		CollidableImageObject image = createCollidableImage(args);
  
 		return new Ball(image.getName(), image.getImage(), image.getPosition(), Float.parseFloat(args[4]), new Vector2f(0,0), image.getNormalCollisionShape(), image.getCollisionType());
 	}
  
 	private static String[] readNextValidLine(BufferedReader br) throws SlickException {
 		boolean read = false;
  
 		String[] args = null;
  
 		while(!read){
 			String line = null;
 			try {
 				line = br.readLine();
 			} catch (IOException e) {
 				throw new SlickException("Could not read level file line", e);
 			}
  
 			if(!( line.startsWith("#") || line.isEmpty() )){
 				read = true;
  
 				args = line.split("\\|");
 			}
 		}
  
 		return args;
 	}
  
 	private static ImageObject createImage( String[] args ) throws SlickException {
 		// background| IMAGE; /data/background.jpg | 0, 0
 		String name = args[0];
 		String[] imageData = args[1].split(";");
  
 		if( !imageData[0].trim().equalsIgnoreCase("IMAGE") ){
 			throw new SlickException("Invalid image");
 		}
  
 		String path = imageData[1];
 		String[] coords = args[2].split(",");
 		Vector2f position = new Vector2f(Integer.parseInt(coords[0].trim()), Integer.parseInt(coords[1].trim()));
  
 		return new ImageObject(name, new Image(path), position);
 	}
  
 	private static CollidableImageObject createCollidableImage( String[] args ) throws SlickException{
 		ImageObject image = createImage(args); 
  
 		String[] collisionData = args[3].split(";");
  
 		int collisionType = Integer.parseInt( collisionData[0] );
  
 		Shape shape = null;
  
 		if(collisionData[1].trim().equalsIgnoreCase("RECTANGLE")){
 			String[] size = collisionData[2].split(",");
 			shape = new Rectangle(Integer.parseInt(size[0]), Integer.parseInt(size[1]), Integer.parseInt(size[2]), Integer.parseInt(size[3]));
 		}else if(collisionData[1].trim().equalsIgnoreCase("CIRCLE")){
 			String[] size = collisionData[2].split(",");
 			shape = new Circle(Integer.parseInt(size[0]), Integer.parseInt(size[1]), Integer.parseInt(size[2])); 	
 		}
  
 		return new CollidableImageObject(image.getName(), 
 										 image.getImage(), 
 										 image.getPosition(), 
 										 shape, 
 										 collisionType);
 	}
  
 	private static Brick createBrick(String[] args) throws SlickException{
  
 		CollidableAnimationObject brickAnimation = createCollidableAnimation(args);
  
 		String[] brickData = args[4].split(";");
 		String[] colorData = brickData[0].split(","); 
 		int numberOfHits = Integer.parseInt(brickData[1]);
 		Color color = new Color( Integer.parseInt(colorData[0]), Integer.parseInt(colorData[1]), Integer.parseInt(colorData[2]) );
  
 		return new Brick(brickAnimation.getName(), 
 						 brickAnimation.getAnimation(), 
 						 brickAnimation.getPosition(), 
 						 numberOfHits, 
 						 color, 
 						 brickAnimation.getNormalCollisionShape(), 
 						 brickAnimation.getCollisionType());
 	}
  
 	private LevelImpl(){
 		balls = new ArrayList<Ball>();
 	}
  
 	@Override
 	public final ImageObject getBackground() {
 		return background;
 	}
  
 	@Override
 	public final List<Ball> getBalls() {
 		return balls;
 	}
  
 	@Override
 	public final List<Brick> getBricks() {
 		return bricks;
 	}
  
 	@Override
 	public final CollidableImageObject getLeftBumper() {
 		return leftBumper;
 	}
  
 	@Override
 	public final Paddle getPaddle() {
 		return paddle;
 	}
  
 	@Override
 	public final CollidableImageObject getRightBumper() {
 		return rightBumper;
 	}
  
 	@Override
 	public final CollidableImageObject getTopBumper() {
 		return topBumper;
 	}
 	
 	public final List<PowerUp> getPowerUps() {
 		return powerUps;
 	}
  
  
 	public final void setBackground(ImageObject background) {
 		this.background = background;
 	}
  
 	public final void setRightBumper(CollidableImageObject rightBumper) {
 		this.rightBumper = rightBumper;
 	}
  
 	public final void setLeftBumper(CollidableImageObject leftBumper) {
 		this.leftBumper = leftBumper;
 	}
  
 	public final void setTopBumper(CollidableImageObject topBumper) {
 		this.topBumper = topBumper;
 	}
  
 	public final void setBricks(List<Brick> bricks) {
 		this.bricks = bricks;
 	}
  
 	public final void setBalls(List<Ball> balls) {
 		this.balls = balls;
 	}
  
 	public final void setPaddle(Paddle paddle) {
 		this.paddle = paddle;
 	}
 
 	public PowerUp addPowerUp(Vector2f pos){
 		 
 		PowerUp pu = null;
 		
 		try {
 			
 			String name = "pu"+powerUps.size();
 			Image image = new Image("data/paddlePU.png");
 			//position is passed in
 			float speed = 0.28f;
 			Vector2f initialDirection = new Vector2f(0,-1);	//set direction to be in negative y
			Shape collisionShape = new Rectangle(pos.x, pos.y, 50, 20);
 			int collisionType = 4;
 			int powerType = 1;
 			
 			pu = new PowerUp( name, image, pos, speed, initialDirection, collisionShape,  collisionType, powerType);
 			powerUps.add( pu );
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 		
 		return pu;
 		
 	}
 	
 	public double getPowerUpP(){
 		return powerUpP;
 	}
 	
 }
 
 	
 	
