 package net.blockscape;
 
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
import java.net.URL;
 
 import net.blockscape.block.Block;
 import net.blockscape.helper.DrawingAndLogicHelper;
 import net.blockscape.helper.IconHelper;
 import net.blockscape.helper.TerrainGenerationHelper;
 import net.blockscape.registry.GameRegistry;
 import net.blockscape.world.World;
 import processing.core.PApplet;
 
 public class BlockScape extends PApplet{
 	public static final int maxReachDistance = 5;
 	public static float distBetweenPlayerAndMouse;
 	public static boolean canPlaceOrRemoveBlock;
 	private static final long serialVersionUID = -1390024970025652247L;
 	public boolean ground;
 	/**
 	 * Called on game start
 	 */
 	public void setup(){
 	    
 		World.init();
 		Player.initPlayer(width/2, 0, this);
 		ground = false;
 		size(1280,720);
		if(frame!=null){
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			URL icon = classLoader.getResource("data/block/grass.png");
			frame.setTitle("BlockScape");
			frame.setIconImage(getToolkit().getImage(icon));
		}
 		IconHelper.init(this);
 		GameRegistry.initialize();
 		Block.blockInit();
 		frameRate(60);
 		TerrainGenerationHelper.generateWorld(this);
 		addMouseWheelListener(new MouseWheelListener() { 
 		    public void mouseWheelMoved(MouseWheelEvent mwe) { 
 		      mouseWheel(mwe.getWheelRotation());
 		  }});
 	}
 	/**
 	 * main game loop
 	 */
 	public void draw(){
 		background(100,100,255);
 		DrawingAndLogicHelper.draw(this);
 		
 	}
 	/**
 	 * called when a key is pressed
 	 */
 	public void keyPressed() {
 		  if (key==' ' && ground) {
 		    Player.setYvelocity(-3);
 		    ground = false;
 		  }
 		  if (key=='a')Player.left = true;
 		  if (key=='d')Player.right = true;
 		  if (key==ENTER){
 			  Player.setX(mouseX);
 			  Player.setY(mouseY);
 		  }
 		}
 	/**
 	 * called when a key is released
 	 */
 	public void keyReleased() {
 		  if (key=='a')Player.left = false;
 		  if (key=='d')Player.right = false;
 		  if (key=='r')setup();
 		}
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		PApplet.main(new String[]{  "net.blockscape.BlockScape"});
 
 	}
 	//Mouse Wheel Constants
 	public final int mwUP = -1;
 	public final int mwDWN = 1;
 	public int selectedBlockID = 1;
 	public Block selectedBlock = Block.blockStone;
 	/**
 	 * called when the mouse wheel is used
 	 * @param delta up (-1) down (1)
 	 */
 	public void mouseWheel(int delta) {
 		  
 		  if (delta==mwUP) {
 		    selectedBlockID++;
 		    if (selectedBlockID>GameRegistry.getBlockRegistrySize())selectedBlockID=1;
 		  }
 		  if (delta==mwDWN) {
 		    selectedBlockID--;
 		    if (selectedBlockID<1)selectedBlockID=GameRegistry.getBlockRegistrySize();
 		  }
 		  selectedBlock = GameRegistry.getBlock(selectedBlockID);
 		}
 
 }
