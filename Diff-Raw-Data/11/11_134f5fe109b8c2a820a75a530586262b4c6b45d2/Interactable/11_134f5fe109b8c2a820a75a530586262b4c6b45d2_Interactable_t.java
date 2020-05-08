 package main.interactables;
 
 import java.io.IOException;
 import org.lwjgl.opengl.GL11;
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.opengl.TextureLoader;
 import main.Game;
 import main.Main;
 import main.ResourceManager;
 import main.world.World;
 
 public abstract class Interactable {
 	
 	private int x, y, sceneX, sceneY;
 	protected double tx, tw;
 	
 	protected Texture texture;
 	
	protected int spriteID;
	
 	public Interactable(int x, int y, int sceneX, int sceneY, int spriteID) {
 		
 		this(spriteID);
 		
 		this.x = x;
 		this.y = y;
 		this.sceneX = sceneX;
 		this.sceneY = sceneY;
 		
 	}
 	
 	public Interactable(int spriteID) {
 		
		this.spriteID = spriteID;
		
 		tw = 1/8.0;
 		
 		try {
 			texture = TextureLoader.getTexture("PNG", ResourceManager.getResourceAsStream("../res/img/interactables.png"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	public void draw() {
 		
 		if (World.currentMap().getSceneX() == sceneX && World.currentMap().getSceneY() == sceneY) {
			
			tx = spriteID/8.0;
			
 			texture.bind();
 			GL11.glColor3d(255, 255, 255);
 			GL11.glBegin(GL11.GL_POLYGON);
 			{
 				GL11.glTexCoord2d(tx, 0);	 	GL11.glVertex2i(x, y);
 				GL11.glTexCoord2d(tx + tw, 0); 		GL11.glVertex2i(x + Main.gridSize, y);
 				GL11.glTexCoord2d(tx + tw, 1); 		GL11.glVertex2i(x + Main.gridSize, y + Main.gridSize);
 				GL11.glTexCoord2d(tx, 1); 		GL11.glVertex2i(x, y + Main.gridSize);
 			}
 			GL11.glEnd();
 		}
 	}
 	
 	public abstract void activate();
 	
 	public boolean checkPlayer() {
 		
 		if (Game.player.getSceneX() == sceneX
 				&& Game.player.getSceneY() == sceneY
 				&& getX()-Game.player.getX() < Main.gridSize 
 				&& Game.player.getX()-getX() < Main.gridSize
 				&& Game.player.getY()-getY() < Main.gridSize
 				&& getY()-Game.player.getY() < Main.gridSize) {
 			
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public int getX() {
 		return x;
 	}
 
 	public int getY() {
 		return y;
 	}
 
 	public void setCoordinates(int x, int y) {
 		this.x = x;
 		this.y = y;
 	}
 
 	public int getSceneX() {
 		return sceneX;
 	}
 
 	public int getSceneY() {
 		return sceneY;
 	}
 
 	public void setScene(int sceneX, int sceneY) {
 		this.sceneX = sceneX;
 		this.sceneY = sceneY;
 	}
 	
 }
