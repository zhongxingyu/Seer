 package View;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import javax.imageio.ImageIO;
 
 import GameEngine.Player.PlayerType;
 
 /**
  * Project - TowerDefense</br>
  * <b>Class - SceneView</b></br> 
  * <p>The SceneView class displays the map and its Sprites</p>
  * <b>Creation :</b> 22/04/2013</br>
  * @author K. Akyurek, A. Beauprez, T. Demenat, C. Lejeune - <b>IMAC</b></br>
  * @see Sprite
  * @see ViewManager
  * 
  */
 
 @SuppressWarnings("serial")
 public class SceneView extends MainViews{   
     private Image map;
     private ArrayList<Sprite> sprites;
     
     private PlayerType humanType;
     
     private Color color;
     
     private boolean towerClicked;
     private Point clickedTowerPosition;
     
     private boolean addTowerClicked;
     private Point addTowerPosition;
     
     private boolean baseClicked;
     private boolean attackBase;
     private Point basePosition;
     private Point mousePosition;
     
     /**
      * Constructor of the SceneView class
      * @param view - ViewManager
      * @param position - SceneView panel position
      * @param width - SceneView panel width
      * @param height - SceneView panel height
      */
 	public SceneView(ViewManager view, Point position, int width, int height){
 		super(view, position, width,height);
 	
 		sprites = new ArrayList<Sprite>();
 		towerClicked = false;
 		baseClicked = false;
 		attackBase = false;	
 		mousePosition = new Point(0,0);
 		clickedTowerPosition = new Point(0,0);
 		humanType = PlayerType.ELECTRIC;
 
 		//Loading the image map
 		try {
 		      map = ImageIO.read(new File("img/map/Map.jpg"));
 		  
 		} catch (IOException e) {
 		      e.printStackTrace();
 		}
 		
         //Add a mouse listener on the map
     	addMouseListener(new MouseAdapter() {
 			public void mousePressed(MouseEvent me) { 
 	             myMousePressed(me);
 	            }
          });
     	
     	//Add a mouse motion listener on the map
     	addMouseMotionListener(new MouseAdapter() {
 			public void mouseMoved(MouseEvent e) {
 				myMouseMoved(e);
 			}
 			//TODO : 
 			public void mouseDragged(MouseEvent e) {
 			}
     	 });
 		
 		//Suppress the layout manager of the SceneView
 		setLayout(null);
 	    setBackground(Color.gray);
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
 	 * Getter - retrieve humanType
 	 * @return PlayerType
 	 */
 	public PlayerType getHumanType() {
 		return humanType;
 	}
 
 	/**
 	 * Setter - set the map image displayed by the ScenView Panel
 	 * @param filename - new map filename
 	 */
 	public void setMap(String filename){
 		
 		//Loading the image map
 		try {
 		      map = ImageIO.read(new File(filename));
 		  
 		} catch (IOException e) {
 		      e.printStackTrace();
 		}
 		
 		//Repaint the window
 		revalidate();
 		repaint();
 	}
 
 	/**
 	 * Add a Sprite in the ScenView ArrayList
 	 * @param sprite
 	 * @see ViewManager#initiateGameView(ArrayList)
 	 */
 	public void addSprite(Sprite sprite){
 		sprites.add(sprite);
 		
 		//TO DO : retrieve the last element added...
 		Iterator<Sprite> it = sprites.iterator();
 		while (it.hasNext()) {
 			Sprite element = it.next();
 			element.setBounds(element.getPosition().x -(element.getWidth()/2), element.getPosition().y -(element.getHeight()/2), element.getWidth(),element.getHeight());
 			add(element);
 		}
 		
         //Repaint the panel
     	revalidate();
     	repaint();	
 	}
 	
 	/**
 	 * Reset the SceneView
 	 * @see ViewManager#initiateGameView(ArrayList)
 	 */
 	public void initiate(){
 
 		//Setting the color
 		if(humanType == PlayerType.ELECTRIC){
 			color = new Color(255,255,0,100);
 		}
 		else if(humanType == PlayerType.WATER){
 			color = new Color(0,0,255,100);
 		}
 		else if(humanType == PlayerType.GRASS){
 			color = new Color(0,255,0,100);
 		}
 		else if(humanType == PlayerType.FIRE){
 			color = new Color(255,0,0,100);
 		}
 		
 		//Removing all the Sprites		
 		Iterator<Sprite> it = sprites.iterator();
 		while (it.hasNext()) {
 			Sprite element = it.next();
 			it.remove();
 			remove(element);
 		}
 		
 		//Add the AddTower Attack Sprite on the panel
 		addSprite(new AddTowerSprite(this, new Point(30,310), true, humanType, 55, 55, 1));
 		addSprite(new AddTowerSprite(this, new Point(30,370), true, humanType, 55, 55, 2));
 		
 		if (addTowerClicked) {
 			addTowerFailed();
 		}
 		if (baseClicked) baseClicked = false;
 		if(towerClicked) hideTowerInfo();
 		
         //Repaint the panel
     	revalidate();
     	repaint();	
 	}
 	
 	/**
 	 * Event "the mouse has been pressed in the zone" handler
 	 * @param me - MouseEvent
 	 */
 	private void myMousePressed(MouseEvent me) {
 		
 		//Click on the map to add the tower
 		if (addTowerClicked) {
 			addTowerClicked = false;
 			//Retrieve the add tower Sprite
 			Iterator<Sprite> it = sprites.iterator();
 			while (it.hasNext()) {
 				Sprite element = it.next();
 				if(element.getPosition().equals(addTowerPosition)){
 					//Tell the engine to check if the tower can be add there
 					view.towerToAdd(element.getPosition(), humanType,((TowerSprite) element).getTowerType());
 				}
 			}		
 		}
 		
 		//Click on the map when a tower is selected
 		if (towerClicked){
 			hideTowerInfo();
 		}
 		
 		//Click on the map when a base is selected
 		if (baseClicked){
 	    	baseClicked = false;
 			
 	    	//Repaint the Panel
 	    	revalidate();
 	    	repaint();	
 		}
 	}
 	
 	/**
 	 * Event "the mouse has moved in the zone" handler
 	 * @param e - MouseEvent
 	 */
 	private void myMouseMoved(MouseEvent e) {
 		if (baseClicked){
 			//Retrieve the current mouse position
 			mousePosition = new Point(e.getPoint());
 	    	//Repaint the Panel
 	    	revalidate();
 	    	repaint();	
 		}
 		if(addTowerClicked){			
 			//Retrieve the the add tower Sprite
 			Iterator<Sprite> it = sprites.iterator();
 			while (it.hasNext()) {
 				Sprite element = it.next();
 				if(element.getPosition().equals(addTowerPosition)){
 					//Reset the tower Sprite Position according to the mouse one
 					addTowerPosition = new Point(e.getPoint());
 					element.setPosition(addTowerPosition);
 					element.setBounds(element.getPosition().x -(element.getWidth()/2), element.getPosition().y -(element.getHeight()/2), element.getWidth(),element.getHeight());
 					add(element);
 				}
 			}		
 			//Repaint the Panel
 	    	revalidate();
 	    	repaint();
 		}
 	}
 	
 	/**
 	 * Display the TowerInfoSprites of a clicked tower
 	 * @param position - Point : position of the tower
 	 * @param playerType - PlayerType
 	 * @see TowerSprite#myMousePressed(MouseEvent)
 	 */
 	public void towerClicked(Point position, PlayerType playerType){
 		if (baseClicked) baseClicked = false;
 		
 		if (addTowerClicked) {
 			addTowerFailed();
 		}
 		//If a tower was already clicked
 		if(towerClicked){
 			hideTowerInfo();
 		}
 		
 		//Display the  TowerInfoSprites of the clicked tower on the map
 		clickedTowerPosition = new Point(position);
 		
 		Point positionSprite = new Point(position);
 		positionSprite.translate((50/2) + (16/2),(50/2) + (16/2));
 		
 		//Add the TowerInfoSprite
 		sprites.add(new TowerInfoSprite(this, positionSprite, true, playerType, 16,16, 0, position));	
 		
 		//Retrieve the clicked tower position
 		Iterator<Sprite> it = sprites.iterator();
 		while (it.hasNext()) {
 			Sprite element = it.next();
 			//Set the Sprite and lay it on the panel
 			if(element.getPosition().equals(positionSprite)){
 				element.setBounds(element.getPosition().x -(element.getWidth()/2), element.getPosition().y -(element.getHeight()/2), element.getWidth(),element.getHeight());
 				add(element);
 			}
 		}		
 		towerClicked = true;
 		
 		//Repaint the panel
     	revalidate();
     	repaint();		
 	}
 	
 	/**
 	 * Remove the TowerInfoSprites of a clicked tower
 	 * @see #addTowerClicked(Point, PlayerType, int)
 	 * @see #baseClicked(Point, PlayerType)
 	 * @see #towerClicked(Point, PlayerType)
 	 * @see #suppressTower(Point, PlayerType)
 	 * @see #myMousePressed(MouseEvent)
 	 */
 	public void hideTowerInfo(){
 		//Removing the towerInfoSprite			
 		Iterator<Sprite> it = sprites.iterator();
 		while (it.hasNext()) {
 			Sprite element = it.next();
 			if(element instanceof TowerInfoSprite){
 				it.remove();
 				remove(element);
 			}
 		}
     	towerClicked = false;
 		
     	//Repaint the Panel
     	revalidate();
     	repaint();	
 	}
 	
 	/**
 	 * Display the territory map when the player want to add a tower
 	 * @param position - Position of the center of the AddTower button clicked
 	 * @param playerType
 	 * @param towerType
 	 */
 	public void addTowerClicked(Point position, PlayerType playerType, int towerType){
 		if(!addTowerClicked){
 			addTowerClicked = true;
 			//Display the territory map
			setMap("tmp/tm.png");
 			addTowerPosition = new Point(position.x+1, position.y+1);
 			
 			TowerSprite ts = new TowerSprite(this, addTowerPosition, false, humanType, 50, 50, towerType, 90);
 			
 			//Add the towerSprite in the sceneView list of Sprites
 			addSprite(ts);	
 		}
 		else{
 			addTowerFailed();
 		}
 		
 		if (baseClicked) baseClicked = false;
 		if(towerClicked) hideTowerInfo();
 		
 	}
 	
 	/**
 	 * Add tower on the SceneView
 	 * @param position
 	 * @param playerType
 	 * @see ViewManager#refresh()
 	 */
 	public void addTower(Point position, PlayerType playerType, int towerType){
 		
 		//If the tower to add is owned by the human player 
 		if(position.equals(addTowerPosition)){
 			addTowerSuccess();
 		}
 		
 		//TODO If the tower to add is owned by an AI player
 		
 	}
 	
 	/**
 	 * Add the tower the player wanted to add
 	 * @see #addTower(Point, PlayerType, int)
 	 */
 	public void addTowerSuccess(){
 		addTowerClicked = false;
 		//Display the simple map
 		setMap("img/map/Map.jpg");
 		
 		//Set the tower Sprite clickable attribute to true
 		Iterator<Sprite> it = sprites.iterator();
 		while (it.hasNext()) {
 			Sprite element = it.next();
 			if(element.getPosition().equals(addTowerPosition)){
 				((TowerSprite) element).setClickable(true);
 			}
 		}	
 
 	}
 	
 	/**
 	 * Remove the tower-to-add Sprite
 	 * @see #baseClicked(Point, PlayerType)
 	 * @see #initiate()
 	 * @see #towerClicked(Point, PlayerType)
 	 * @see #addTowerClicked(Point, PlayerType, int)
 	 */
 	public void addTowerFailed(){
 		addTowerClicked = false;
 		//Display the simple map
 		setMap("img/map/Map.jpg");
 		
 		//Suppress the tower-to-add Sprite
 		Iterator<Sprite> it = sprites.iterator();
 		while (it.hasNext()) {
 			Sprite element = it.next();
 			if(element.getPosition().equals(addTowerPosition)){
 				it.remove();
 				remove(element);
 			}
 		}
 		
 		//Repaint the panel
     	revalidate();
     	repaint();	
 	}
 	
 	/**
 	 * Display the line between the clicked base and the mouse cursor
 	 * @param position
 	 * @param playerType
 	 * @see BaseSprite#myMousePressed(MouseEvent)
 	 */
 	public void baseClicked(Point position, PlayerType playerType){
 		if (towerClicked){
 			hideTowerInfo();
 		}
 		
 		if (addTowerClicked) {
 			addTowerFailed();
 		}
 		
 		//If the player has clicked on one of his base
 		if((!baseClicked)&&(playerType == humanType)){
 			basePosition = new Point(position);
 			baseClicked = true;
 			mousePosition = new Point(position);
 		}
 		
 		//If the player has first clicked on one of his base, then clicked on an enemy base 
 		if((baseClicked)&&(playerType != humanType)){
 			attackBase = true;
 		}
 		
 		//Repaint the panel
     	revalidate();
     	repaint();	
 	}
 	
 	/**
 	 * Tell the dispatcher that the player want to attack a base
 	 * @param position
 	 * @param playerType
 	 * @see BaseSprite#myMouseReleased(MouseEvent)
 	 */
 	public void attackBase(Point position, PlayerType playerType){
 		
 		//If the player has first clicked on one of his base, then clicked on an enemy base 
 		if(attackBase&&(playerType != humanType)){
 			System.out.println("View - Attack !!");
 			//Remove the line between the two bases
 			baseClicked = false;
 			attackBase = false;
 			//TODO : attack !!! Number of unit increase according to the mouse 
 			//Will need basePosition (position of the first base) and position (position of the second base...)
 		}
 		
 		//Repaint the panel
     	revalidate();
     	repaint();		
 	}
 	
 	/**
 	 * Tell the view that a tower need to be suppressed
 	 * @param position of the tower to suppress
 	 * @param playerType
 	 * @see TowerInfoSprite#myMousePressed(MouseEvent)
 	 */
 	 public void towerToSupress(Point position, PlayerType playerType){
 		   view.towerToSupress(position, playerType);
 	   }
 	 
 	/**
 	 * Suppress a tower and its Sprite info
 	 * @param position
 	 * @param playerType
 	 * @see ViewManager#refresh()
 	 */
 	public void suppressTower(Point position, PlayerType playerType){
 		Iterator<Sprite> it = sprites.iterator();
 
 		while (it.hasNext()) {
 			Sprite element = it.next();
 			//Removing the towerSprite
 			if(element.getPosition().equals(position)){
 				it.remove();
 				remove(element);
 			}
 		}	
 		hideTowerInfo();		
 	}
 	
     /**
      * Draw the SceneView Panel
      */
     @Override
 	public void paintComponent(Graphics g){
 		super.paintComponent(g);
 	    g.drawImage(map, 0, 0, this.getWidth(), this.getHeight(), this);
 	    g.setColor(color);
 	    
 	    if(towerClicked){
 	    	//Retrieve the clicked tower
 			Iterator<Sprite> it = sprites.iterator();
 			while (it.hasNext()) {
 				Sprite s = it.next();
 				if(s.getPosition().equals(clickedTowerPosition)){
 		    		g.fillOval(s.getPosition().x-(((TowerSprite) s).getRange()/2), s.getPosition().y -(((TowerSprite) s).getRange()/2), ((TowerSprite) s).getRange(), ((TowerSprite) s).getRange());
 				}
 			}
 	    }
 	    
 	    if(baseClicked){
 	    	//g.setColor(Color.blue);
     		g.drawLine(basePosition.x, basePosition.y, mousePosition.x, mousePosition.y);
 	    }
 	  }              
 }
