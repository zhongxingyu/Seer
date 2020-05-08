 package graphics;
 
 import java.awt.Graphics2D;
 import java.awt.event.KeyEvent;
 import java.awt.image.BufferedImage;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import org.jnbt.*;
 
 
 import utils.BufferedImageUtils;
 import utils.KeyboardInput;
 
 
 
 
 public class Character implements Runnable{
 	static Thread t;
 	private int x;
 	private int y;
 	private int velX;
 	private int velY;
 	boolean noclip;
 	boolean fly;
 	GameWindow parent;
 	BufferedImage sprite;
 	
 	public Character(GameWindow owner) {
 		x=0;
 		y=0;
 		noclip=false;
 		fly=false;
 		
 		velX=0;
 		velY=-1;
 		parent=owner;
 		sprite=BufferedImageUtils.loadImage(getClass().getResource("/graphics/player.png"));
 		
 		t = new Thread(this);
 	    t.start();
 	}
 	
 	public int getX() {
 		return x;
 	}
 	
 	public int getY() {
 		return y;
 	}
 	
 	@Override
 	public void run() {
 		while(true)
 		{
 			BlockWorld world = parent.world;
 			KeyboardInput keyboard = parent.keyboard;
 			
 			// Player input
 			if (keyboard.keyDown( KeyEvent.VK_SPACE )) { // Player wants to jump
 				if (fly || noclip) {
 					velY-=1;
				} else if (!world.isBlockTraversable(x,y+1) && !fly && !noclip) { // You're not jumping in empty air 
 					velY-=1;
 				}
 			}
 			if (keyboard.keyDown( KeyEvent.VK_A )) { // Player wants to walk to the left
 				velX-=1;
 			}
 			if (keyboard.keyDown( KeyEvent.VK_D )) { // Player wants to walk to the right
 				velX+=1;
 			}
 			if (keyboard.keyDown( KeyEvent.VK_S )) { // Player wants to climb down
 				velY+=1;
 			}
 			
			if (world.isBlockTraversable(x,y+1) && velY==0 && !fly && !noclip) { // Gravity
 				velY+=1;
 			}
 			
 			while (velX!=0 || velY!=0) {
 				if (velX>0) { // You're going right
 					x+=1;
 					velX-=1;
 					
 					// X-Axis collision check
 					if (!world.isBlockTraversable(x, y) && !noclip) { // You're going right, and an object is in the way
 						velX=0;
 						x-=1;
 					}
 				} else if (velX<0) { // You're going left
 					x-=1;
 					velX+=1;
 					
 					// X-Axis collision check
 					if (!world.isBlockTraversable(x, y) && !noclip) { // You're going left, and an object is in the way
 						velX=0;
 						x+=1;
 					}
 				} else {
 				}
 				
 				if (velY>0) { // You're going down
 					y+=1;
 					velY-=1;
 
 					// Y-Axis collision check
 					if (!world.isBlockTraversable(x, y) && !noclip) { // You're down, and an object is in the way
 						velY=0;
 						y-=1;
 					}
 				} else if (velY<0) { // You're going up
 					y-=1;
 					velY+=1;
 					
 					// Y-Axis collision check
 					if (!world.isBlockTraversable(x, y) && !noclip) { // You're going up, and an object is in the way
 						velY=0;
 						y+=1;
 					}
 				} else {
 				}
 			}
 			
 			try {
 				Thread.sleep(1000/10);
 			} catch (InterruptedException e) { ; }
 		}
 	}
 	
 	public void draw(Graphics2D g) {
 		g.drawImage(sprite, x*25, y*25, 25, 25, parent);
 	}
 	
 	public void save(String filename) {
 		try {
 			NBTOutputStream out = new NBTOutputStream(new FileOutputStream(filename));
 			HashMap<String, Tag> data = new HashMap<String, Tag>();
 			data.put("x", new IntTag("x", getX()));
 			data.put("y", new IntTag("y", getY()));
 			data.put("noclip", new StringTag("noclip", Boolean.toString(noclip)));
 			data.put("fly", new StringTag("fly", Boolean.toString(fly)));
 			out.writeTag(new CompoundTag("player", data));
 			out.close();
 			
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void load(String filename) {
 		try {
 			NBTInputStream in = new NBTInputStream(new FileInputStream(filename));
 			Tag tag = in.readTag();
 			System.out.println(tag.getValue());
 			System.out.println(tag.getName());
 			@SuppressWarnings("unchecked")
 			Map<String, Tag> data = (Map<String, Tag>) tag.getValue();
 			x = (Integer) data.get("x").getValue();
 			y = (Integer) data.get("y").getValue();
 			noclip = Boolean.parseBoolean((String) data.get("noclip").getValue());
 			fly = Boolean.parseBoolean((String) data.get("fly").getValue());
 			
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
