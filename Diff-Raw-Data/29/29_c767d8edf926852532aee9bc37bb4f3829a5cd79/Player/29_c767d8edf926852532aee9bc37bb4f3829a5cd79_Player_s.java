 package entities.ships;
 
 import game.Variables;
 
 import java.awt.image.BufferedImage;
 import java.awt.image.BufferedImageOp;
 import java.awt.image.LookupOp;
 import java.awt.image.LookupTable;
 import java.awt.image.ShortLookupTable;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.World;
 import org.jbox2d.dynamics.joints.DistanceJointDef;
 import org.jbox2d.dynamics.joints.Joint;
 
 public class Player extends Ship {
 	
 	private final static int SLOW = 20;
 	
 	private BufferedImage[] loopingImage;
	BufferedImageOp colorizeFilter;
 	
 	//Utilise sa pour charger les images !!!!!!!!!!!!!!!!!!!!!!!!!
 	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 	//Class loader
 	//getRessourceAsStream(String name)
 	//FAIRE NOTRE CLASS LOADER
 	
 	
 	public enum Looping{
 		NONE,
 		LEFT,
 		RIGHT;
 		
 		BufferedImage currentImage;
 		int frame = 0;
 		int count = 0;
 	}
 	private Looping looping;
 	
 	
 	
 	public Player() throws IOException {
 		looping = Looping.NONE;
 		
		colorizeFilter = createColorizeOp((short)1, (short)0, (short)0);
 		loopingImage = new BufferedImage[12];
 		
 		for(int i=0; i<loopingImage.length; i++){
 			try {  
 				loopingImage[i] = ImageIO.read(new File("images/Player/Joueur"+(i+1)+".png"));				
 			} catch (IOException ex) {
 				throw new IOException("Ship initialisation fail: can't open images/Player/Joueur"+(i+1)+".png");
 			}
 		}
 	}
 
 	@Override
 	public void compute() {
 		Vec2 position = getScreenPostion();
 		if(position.y>Variables.SCREEN_HEIGHT/3){
 			setVelocity(getVelocity().x, -50);
 		}		
 	}
 
 	@Override
 	protected String getImageURL() {
 		return "images/Player/Joueur.png";
 	}
 
 	
 	
 	@Override
 	public void init(World world, float x, float y){
 		super.init(world, Variables.SCREEN_WIDTH/2, Variables.SCREEN_HEIGHT/5);
 		body.setFixedRotation(true);		
 	}
 	
 	
 	@Override
 	public BufferedImage getImage(){
 		switch(looping){
 			case NONE:		
 				return image;
 				
 			case LEFT:
 				looping.count = ++looping.count % SLOW;
 				if(looping.count==0)
 					looping.frame--;
 				return loopRender();
 				
 			case RIGHT:
 				looping.count = ++looping.count % SLOW;
 				if(looping.count==0)
 					looping.frame++;
				return colorizeFilter.filter(loopRender(), null);
 		}
 		return null;
 	}
 	
 	
 	private BufferedImage loopRender(){				
 		if(looping.frame<0 || looping.frame>=loopingImage.length){
 			looping.frame = 0;
 			looping = Looping.NONE;
 			return image;
 		}
 		
 		switch(looping){
 			case LEFT:
 				return loopingImage[looping.frame];
 			case RIGHT:
 				return loopingImage[looping.frame];
 		}
 		return image;
 		
 	}
 	
 	
 	
 	
 	/*
 	 * Actions
 	 */
 	public void looping(Looping loop){
 		looping = loop;
 		switch(looping){
 			case NONE:
 				looping.frame=0;
 				break;
 			case LEFT:
 				looping.frame= loopingImage.length-1;
 				break;
 			case RIGHT:
 				looping.frame = 0;
 				break;
 		} 
 	}
 		
 	public void touched(int i) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	
	
	protected LookupOp createColorizeOp(short R1, short G1, short B1) {
	    short[] alpha = new short[256];
	    short[] red = new short[256];
	    short[] green = new short[256];
	    short[] blue = new short[256];

	    for (short i = 0; i < 256; i++) {
	        alpha[i] = i;
	        red[i] = (short) ((R1 + i*.3)/2);
	        green[i] = (short) ((G1 + i*.59)/2);
	        blue[i] = (short) ((B1 + i*.11)/2);
	    }

	    short[][] data = new short[][] {
	            red, green, blue, alpha
	    };

	    LookupTable lookupTable = new ShortLookupTable(0, data);
	    return new LookupOp(lookupTable, null);
	}
	
 	/**
 	 * Used to join the Ship to the origin point
 	 * Non used
 	 */
 	private void initJoin(){
 		Joint joint;
 		Body ground = world.createBody(new BodyDef());
 		
 		DistanceJointDef jd = new DistanceJointDef();
 		Vec2 p1 = new Vec2();
 		Vec2 p2 = new Vec2();
 		Vec2 d = new Vec2();
 		
 		jd.frequencyHz = Variables.LINK_FREQUENCY;
 		jd.dampingRatio = Variables.LINK_DAMPING;
 		
 		jd.bodyA = ground;
 		jd.bodyB = body;
 		jd.localAnchorA.set(toWorldSize(Variables.SCREEN_WIDTH/2), toWorldSize(Variables.SCREEN_HEIGHT/5));
 		jd.localAnchorB.set(0,0);
 		p1 = jd.bodyA.getWorldPoint(jd.localAnchorA);
 		p2 = jd.bodyB.getWorldPoint(jd.localAnchorB);
 		d = p2.sub(p1);
 		jd.length = 0;//d.length();
 		joint = world.createJoint(jd);
 	}
 }
