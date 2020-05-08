 package prototype;
 
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.List;
 import javax.imageio.ImageIO;
 import player.PlayerIO;
 
 
 
 public class NPC {
 	private int id;
 	private float f_NPCpos_x;
 	private float f_NPCpos_y;
 	private Rectangle bounding;
 	private static BufferedImage NPCSHOP; 
 	private static BufferedImage Storynpc; 
 	public boolean dialogAn=true;
 
 	
 	static {
 		try {
 			
 			NPCSHOP = ImageIO.read(Zauber.class.getClassLoader().getResourceAsStream("gfx/NPCSHOP.png"));
 			Storynpc = ImageIO.read(Zauber.class.getClassLoader().getResourceAsStream("gfx/Storynpc.png"));
 
 
 
 			} catch (IOException e) {e.printStackTrace();}
         }
 
 
 	public NPC(float x, float y, int id, List<NPC> npcs) {
 		this.id=id;
 		this.f_NPCpos_x = x;
 		this.f_NPCpos_y = y;
 	    bounding = new Rectangle((int)x, (int)y, NPCSHOP.getWidth(), NPCSHOP.getHeight());
 
 	}
 	public void update(float timeSinceLastFrame){
 		float entfernung=(float) Math.sqrt((PlayerIO.getBounding().x-f_NPCpos_x)*(PlayerIO.getBounding().x-f_NPCpos_x)+(PlayerIO.getBounding().y-f_NPCpos_y-128)*(PlayerIO.getBounding().y-f_NPCpos_y-128));
        if (entfernung<100){
     	   dialogAn=true;
  
 
     	   
        }else{dialogAn=false;	}
 	}
 	
 	
 
 
 
 
 	public Rectangle getBounding() {
 		return bounding;
 	}
 	public  BufferedImage getLook() {
 	    if(id==2){
 	    	return Storynpc;
	    }else
 			return NPCSHOP;
     }
 
 
 	public int getX() {
 
 		return (int) f_NPCpos_x;
 	}
 
 	public int getY() {
 		return (int) f_NPCpos_y;
 	}
 
 	public int getid() {
 		return (int) id;
 	}
 	
 }
