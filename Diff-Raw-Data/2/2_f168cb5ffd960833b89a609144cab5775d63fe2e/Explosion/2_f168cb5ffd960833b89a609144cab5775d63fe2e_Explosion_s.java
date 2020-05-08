 package nitrogene.util;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 
 public class Explosion extends Animation{
 	private float x, y;
 	private float scale;
 	private Sound hit;
 	
 	public Explosion(float x, float y, float scale, int duration) throws SlickException{
 		super();
 		hit = new Sound("res/sound/Explosionfinal.ogg");
 		this.setLooping(false);
 		this.x = x;
 		this.y = y;
 		this.scale = scale;
 		init(duration);
 		hit.play(1f, 0.5f);
 	}
 	
 	private void init(int duration) throws SlickException{
 		Image baseimage = new Image("res/explanim.png");
 		//Initializes explosion into memory, row = rows, col = columns
 		//baseimage.getSubImage(x,y,width,height)
 		for (int row=0;row<2;row++) {
             for(int col=0;col<6;col++){
                this.addFrame(baseimage.getSubImage((int) (col*32),row*32,32,32), duration);
             }
          }
 	}
 	
 	@Override
 	public void draw(){
		draw(x,y,getWidth()*scale,getHeight()*scale);
 	}
 	
 	public float getX(){
 		return x;
 	}
 	public float getY(){
 		return y;
 	}
 	
 }
