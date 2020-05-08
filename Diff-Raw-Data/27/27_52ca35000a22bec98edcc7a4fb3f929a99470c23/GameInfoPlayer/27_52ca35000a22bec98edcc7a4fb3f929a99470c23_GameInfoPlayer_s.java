 /**
  * Project : TowerDefense
  * By Aurélie Beauprez, Thomas Demenat, Keven Akyurek and Cecilia Lejeune
  * Copyright IMAC 2013 - All Rights Reserved
  *
  * File created on 26 avr. 2013
  */
 package View;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Point;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import javax.imageio.ImageIO;
 import javax.swing.JLabel;
 import javax.swing.SwingUtilities;
 
 import GameEngine.Player.PlayerType;
 
 /**
  * Project - TowerDefense</br>
  * <b>Class - GameInfoPanel</b></br>
  * <p>The GameInfoPanel class is used to display a user friendly interface while playing the game.
  * The player would be allowed to make actions thanks to buttons and informations hold in the
  * GameInfoPanel.
  * The GameMenuBar is created and managed by the ViewManager.</br>
  * </p> 
  * <b>Creation :</b> 26/04/2013</br>
  * @author K. Akyurek, A. Beauprez, T. Demenat, C. Lejeune - <b>IMAC</b></br>
  * @see ViewManager
  */
 @SuppressWarnings("serial")
 public class GameInfoPlayer extends MainViews{
     private ArrayList<Sprite> sprites;
     private JLabel jMoney;
     
     private PlayerType humanType;
     private int money;
    
     private Image img;
 	
 	/**
 	 * Constructor of the GameInfoPlayer class
 	 * @param view
 	 * @param position
 	 * @param width
 	 * @param height
 	 */
 	public GameInfoPlayer(ViewManager view, Point position, int width, int height) {
 		super(view, position, width,height);
 		
 		sprites = new ArrayList<Sprite>();
 		
 		jMoney = new JLabel();
 		jMoney.setForeground(Color.BLACK);
 	
 		//Laying the components on the Panel
 		setLayout(null);
		jMoney.setBounds(10, 10, 50,15);
 		add(jMoney);
 		
 		setBackground(Color.gray); 
 	}
 	
 	/**
 	 * Reset the SceneView
 	 * @see ViewManager#initiateGameView(ArrayList)
 	 */
 	public void initiate(SceneView scene, final int money){
 		this.money = money;
 		SwingUtilities.invokeLater(new Runnable(){
 		public void run() {	
 			//Removing all the Sprites		
 			Iterator<Sprite> it = sprites.iterator();
 			while (it.hasNext()) {
 				Sprite element = it.next();
 				it.remove();
 				remove(element);
 			}
 			
 			String fileName ="img/";
 			
 			if(humanType == PlayerType.ELECTRIC){
 				fileName +="Electric/";
 			}
 			else if(humanType == PlayerType.WATER){
 				fileName +="Water/";
 			}
 			else if(humanType == PlayerType.GRASS){
 				fileName +="Grass/";
 			}
 			else if(humanType == PlayerType.FIRE){
 				fileName +="Fire/";
 			}
 			
 			fileName += "playerInfo.png";
 			
 			//Loading the background
 			try {
 				 img = ImageIO.read(new File(fileName));
 			  
 			} catch (IOException e) {
 			      e.printStackTrace();
 			}
 		
 			jMoney.setText(money+" P$");
 	        //Repaint the panel
 	    	revalidate();
 	    	repaint();	
 		}});
 	}
 	
 	/**
 	 * Setter - humanType
 	 * @param humanType - id of the human player
 	 * @see ViewManager#play(int)
 	 */
 	public void setHumanType(PlayerType humanType) {
 		this.humanType = humanType;
 	}
 	
 	/**
 	 * 
 	 * @param money
 	 * @param playerType
 	 */
 	public void setMoney(int money, PlayerType playerType){
 		if(playerType == humanType){
 			this.money = money;
 			jMoney.setText(money+" P$");
 		}	
 	}
 	
     /**
      * Draw the SceneView Panel
      */
     @Override
 	public void paintComponent(Graphics g){
 		super.paintComponent(g);
 	    g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), this);
 	 }   
 
 }
