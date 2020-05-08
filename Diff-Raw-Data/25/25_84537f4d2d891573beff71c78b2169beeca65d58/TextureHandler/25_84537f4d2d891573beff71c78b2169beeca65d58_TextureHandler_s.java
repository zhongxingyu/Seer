 package org.noip.staticattic.fileutils;
 
 import java.io.File;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 
 public class TextureHandler {
 	
 	private static ImageIcon grass = new ImageIcon();
 	private static ImageIcon player = new ImageIcon();
 	
 	public static void loadAllTextures() {
 		
 		try {
 		    
			grass = (new ImageIcon(ImageIO.read(TextureHandler.class.getResource("/resources/img/tiles/grass.fw.png"))));
 		    
 		} catch (Exception e1) {
 		    
 			try {
 		     
				grass = (new ImageIcon(ImageIO.read(new File(System.getProperty("user.home")+"/village/grass.fw.png"))));
 		     
 		    } catch (Exception e2) {
 		     
 		         e2.printStackTrace();
 		     
 		    }
 		    
         }
 		
		
		
 	}
 
 	public static ImageIcon getGrass() {
 		
 		return grass;
 		
 	}
 		
 	public static ImageIcon getPlayer() {
 		
 		return player;
 		
 	}
 	
 }
