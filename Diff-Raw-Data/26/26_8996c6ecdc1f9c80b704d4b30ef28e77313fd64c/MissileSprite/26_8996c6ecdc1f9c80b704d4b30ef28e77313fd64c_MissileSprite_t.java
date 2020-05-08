 /**
  * Project : TowerDefense
  * By Aurélie Beauprez, Thomas Demenat, Keven Akyurek and Cecilia Lejeune
  * Copyright IMAC 2013 - All Rights Reserved
  *
  * File created on 15 mai 2013
  */
 package View;
 
import java.awt.Graphics;
 import java.awt.Point;
 import java.io.File;
 import java.io.IOException;
import java.util.Iterator;
 
 import javax.imageio.ImageIO;
 
 import GameEngine.GameManager;
 import GameEngine.Player.PlayerType;
 
 /**
  * Project - TowerDefense</br>
  * <b>Class - MissileSprite</b></br>
  * <p>The MissileSprite class represents the units images displayed on the ScenView</p>
  * <b>Creation :</b> 03/05/2013</br>
  * @author K. Akyurek, A. Beauprez, T. Demenat, C. Lejeune - <b>IMAC</b></br>
  * @see MainViews
  * @see GameMenuBar
  * @see SceneView
  * @see GameManager
  */
 @SuppressWarnings("serial")
 public class MissileSprite extends Sprite{
 	private boolean isArea;
 
 	/**
 	 * 
 	 */
 	public MissileSprite(SceneView scene, int id, Point position, PlayerType playerType, boolean isArea) {
 		super(scene, id, position,false,playerType,16,16);
 		
 		//Loading the unit image (different one according the player)
 		String fileName ="img/";
 		
 		if(playerType == PlayerType.ELECTRIC){
 			fileName +="Electric/";
 		}
 		else if(playerType == PlayerType.WATER){
 			fileName +="Water/";
 		}
 		else if(playerType == PlayerType.GRASS){
 			fileName +="Grass/";
 		}
 		else if(playerType == PlayerType.FIRE){
 			fileName +="Fire/";
 		}
 		
 		if(isArea) fileName += "Missile.png";
 		else fileName += "Missile.png";
 		
 		try {	
 		      image = ImageIO.read(new File(fileName));
 		  
 		} catch (IOException e) {
 		      e.printStackTrace();
 		}
 	}
	
	 /**
     * Draw the SceneView Panel
     */
    @Override
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		//if(!isArea){
			g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), this);
		//}
		/*else{
			g.setColor(color);
			g.fillOval(s.getPosition().x-(((TowerSprite) s).getRange()), s.getPosition().y -(((TowerSprite) s).getRange()), 2*((TowerSprite) s).getRange(), 2*((TowerSprite) s).getRange());
	    }*/  
    }              
 
 }
