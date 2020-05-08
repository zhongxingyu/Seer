 package com.github.desmaster.Devio.realm.world;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 import org.lwjgl.opengl.Display;
 
import com.github.desmaster.Devio.cons.Console;
 import com.github.desmaster.Devio.gfx.Screen;
 import com.github.desmaster.Devio.realm.Realm;
 import com.github.desmaster.Devio.realm.entity.Player;
 
 public class World {
 
 	public final int BLOCK_SIZE = 32;
 	public final int MAP_WIDTH = Display.getWidth() / BLOCK_SIZE;
 	public final int MAP_HEIGHT = Display.getHeight() / BLOCK_SIZE;
 	public final int WORLD_WIDTH = 1024;
 	public final int WORLD_HEIGHT = 1024;
 
 	Tile[][] worldtiles = new Tile[WORLD_WIDTH][WORLD_HEIGHT];
 
 	public World() {
 		for (int x = 0; x < WORLD_WIDTH; x++) {
 			for (int y = 0; y < WORLD_HEIGHT; y++) {
 				worldtiles[x][y] = Tile.GRASS;
 			}
 		}
 	}
 
 	public Tile getTile(int x, int y) {
 		return worldtiles[x][y];
 	}
 
 	public void setTile(int x, int y, Tile tile) {
 		worldtiles[x][y] = tile;
 	}
 
 	public Tile[][] getSubArea(int x, int y, int width, int height) {
 		Tile[][] SubArea = new Tile[width][height];
 
 		for (int x2 = 0; x2 < width; x2++) {
 			for (int y2 = 0; y2 < height; y2++) {
 				SubArea[x2][y2] = worldtiles[x + x2][y + y2];
 			}
 		}
 		
 		return SubArea;
 	}
 
 	public Tile[][] getVisibleMap(Player player) {
		// TODO stukje map die je kunt zien
		return getSubArea(player.x - (int) Math.ceil(Realm.MAP_WIDTH/2),player.y - (int) Math.ceil(Realm.MAP_HEIGHT/2),Realm.MAP_WIDTH,Realm.MAP_HEIGHT);
 	}
 
 	public void render() {
 		Tile[][] SubArea = getVisibleMap(Screen.getPlayer());
 		for(int x = 0; x < SubArea.length; x++) {
 			for (int y = 0; y < SubArea[0].length; y++) {
 				renderTile(x, y, SubArea[x][y]);
 			}
 		}
 	}
 	
 	public void renderTile(int x, int y, Tile tile) {
 		tile.getTexture().bind();
 		x *= BLOCK_SIZE / 2;
 		y *= BLOCK_SIZE / 2;
 		int width = BLOCK_SIZE;
 		int height = BLOCK_SIZE;
 		glLoadIdentity();
 		glTranslatef(x, y, 0);
 		glBegin(GL_QUADS);
 			glTexCoord2f(0, 0);
 			glVertex2f(x, y);
 			glTexCoord2f(1, 0);
 			glVertex2f(x + width, y);
 			glTexCoord2f(1, 1);
 			glVertex2f(x + width, y + height);
 			glTexCoord2f(0, 1);
 			glVertex2f(x, y + height);
 		glEnd();
 		glLoadIdentity();
 	}
 	
 	public void saveWorld(String filename, Tile[][] output_veld) {
 	     try {
 	        FileOutputStream fos = new FileOutputStream(filename);
 	        GZIPOutputStream gzos = new GZIPOutputStream(fos);
 	        ObjectOutputStream out = new ObjectOutputStream(gzos);
 	        out.writeObject(output_veld);
 	        out.flush();
 	        out.close();
 	     }
 	     catch (IOException e) {
 	         System.out.println(e); 
 	     }
 	  }
 
 	public Tile[][] loadWorld(String filename) {
 	      try {
 	        FileInputStream fis = new FileInputStream(filename);
 	        GZIPInputStream gzis = new GZIPInputStream(fis);
 	        ObjectInputStream in = new ObjectInputStream(gzis);
 	        Tile[][] gelezen_veld = (Tile[][])in.readObject();
 	        in.close();
 	        return gelezen_veld;
 	      }
 	      catch (Exception e) {
 	          System.out.println(e);
 	      }
 	      return null;
 	  }
 
 }
