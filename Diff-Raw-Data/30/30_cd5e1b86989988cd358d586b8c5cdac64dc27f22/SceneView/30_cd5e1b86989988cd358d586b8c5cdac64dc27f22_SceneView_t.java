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
     
     private int humanId;
     private boolean towerClicked;
     private int indexTowerClicked;
     
     
     private boolean baseClicked;
     private boolean attackBase;
     private Point basePosition;
     private Point mousePosition;
     
     /**
      * Constructor of the SceneView class
      * @param view
      * @param position
      * @param width
      * @param height
      */
 	public SceneView(ViewManager view, Point position, int width, int height){
 		super(view, position, width,height);
 	
 		sprites = new ArrayList<Sprite>();
 		towerClicked = false;
 		baseClicked = false;
 		attackBase = false;
 		indexTowerClicked = 0;		
 		mousePosition = new Point(0,0);
 		humanId = 0;
 		
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
 				//System.out.println("Hey !");
 			}
     	 });
 		
 		//Suppress the layout manager of the SceneView
 		setLayout(null);
 	    setBackground(Color.gray);
 	}
 	
 	
 	/**
 	 * Setter - humanId
 	 * @param humanId - id of the human player
 	 * @see ViewManager#play(int)
 	 */
 	public void setHumanId(int humanId) {
 		this.humanId = humanId;
 	}
 	
 	/**
 	 * Getter - retrieve humanId
 	 * @return
 	 */
 	public int getHumanId() {
 		return humanId;
 	}
 
 
 	/**
 	 * Add a Sprite on the map
 	 * @param tower 
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
 	 * Clear the Sprites list
 	 * @see ViewManager#initiateGameView(ArrayList)
 	 */
 	public void initiate(){
 		//Removing all the Sprites		
 		Iterator<Sprite> it = sprites.iterator();
 		while (it.hasNext()) {
 			Sprite element = it.next();
 			it.remove();
 			remove(element);
 		}
 		
         //Repaint the panel
     	revalidate();
     	repaint();	
 	}
 	
 	/**
 	 * Event "the mouse has been pressed in the zone" handler
 	 * @param me - MouseEvent
 	 */
 	private void myMousePressed(MouseEvent me) {
 		
 		System.out.println("Position on the Map ("+ me.getPoint().x+","+ me.getPoint().y+")");
 		
 		//Click on the map when a tower is selected
 		if (towerClicked){
 			//Removing from the view the towerInfoSprite			
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
 	}
 	
 	/**
 	 * Display the TowerInfoSprites of a clicked tower
 	 * @param position
 	 * @param idOwner
 	 * @see TowerSprite#myMousePressed(MouseEvent)
 	 */
 	public void towerClicked(Point position, int playerId){
 		//Display the  TowerInfoSprites of the clicked tower on the map
 		Point positionSprite = new Point(position);
 		positionSprite.translate((32/2) + (16/2),(32/2) + (16/2));
 		
 		System.out.println("Position Tower "+position.x);
 		System.out.println("Position Delete "+positionSprite.x);
 		
 		//Add the TowerInfoSprite
 		sprites.add(new TowerInfoSprite(this, positionSprite, true, playerId, 16,16, 0, position));	
 		
 		
 		//TODO Retrieve the the clicked tower
 		Iterator<Sprite> it = sprites.iterator();
 		while (it.hasNext()) {
 			Sprite element = it.next();
 			//Retrieve the index of the clicked tower
 			if(element.getPosition().equals(position)){
 				indexTowerClicked = sprites.indexOf(element);
 				System.out.println(indexTowerClicked);
 			}
 			element.setBounds(element.getPosition().x -(element.getWidth()/2), element.getPosition().y -(element.getHeight()/2), element.getWidth(),element.getHeight());
 			add(element);
 		}		
 		towerClicked = true;
 		
 		//Repaint the panel
     	revalidate();
     	repaint();	
 		
 	}
 	
 	/**
 	 * Display the line between the clicked base and the mouse cursor
 	 * @param position
 	 * @param playerId
 	 * @see BaseSprite#myMousePressed(MouseEvent)
 	 */
 	public void baseClicked(Point position, int playerId){
 		
 		//If the player has clicked on one of his base
 		if((!baseClicked)&&(playerId == humanId)){
 			basePosition = new Point(position);
 			baseClicked = true;
 			mousePosition = new Point(position);
 		}
 		
 		//If the player has first clicked on one of his base, then clicked on an enemy base 
 		if((baseClicked)&&(playerId != humanId)){
 			attackBase = true;
 		}
 		
 		//Repaint the panel
     	revalidate();
     	repaint();	
 	}
 	
 	/**
 	 * Tell the dispatcher that the player want to attack a base
 	 * @param position
 	 * @param playerId
 	 * @see BaseSprite#myMouseReleased(MouseEvent)
 	 */
 	public void attackBase(Point position, int playerId){
 		System.out.println("Position Base "+position.x);
 		
 		//If the player has first clicked on one of his base, then clicked on an enemy base 
 		if(attackBase&&(playerId != humanId)){
 			System.out.println("Attack !!");
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
 	 * @param position
 	 * @param playerId
 	 * @see TowerInfoSprite#myMousePressed(MouseEvent)
 	 */
 	 public void towerToSupress(Point position, int playerId){
 		   view.towerToSupress(position, playerId);
 	   }
 	 
 	/**
 	 * Suppress a tower and its Sprite info
 	 * @param position
 	 * @param playerId
 	 * @see ViewManager#refresh()
 	 */
 	public void suppressTower(Point position, int playerId){
 		Iterator<Sprite> it = sprites.iterator();
 		Point positionSuppress = new Point(position);
 		positionSuppress.translate((32/2) + (16/2),(32/2) + (16/2));
 
 		while (it.hasNext()) {
 			Sprite element = it.next();
 			if(element.getPosition().equals(position)){
 				it.remove();
 				remove(element);
 
 			}
 			//Sprite suppress
 			if(element.getPosition().equals(positionSuppress)){
 				it.remove();
 				remove(element);
 			}
 		}	
 		towerClicked =  false;
 		
 		//Repaint the panel
     	revalidate();
     	repaint();		
 	}
 	
     /**
      * Draw the SceneView Panel
      */
     @Override
 	public void paintComponent(Graphics g){
 		super.paintComponent(g);
 	    g.drawImage(map, 0, 0, this.getWidth(), this.getHeight(), this);
 	    if(towerClicked){
 	    	//Retrieve the clicked tower
 	    	Sprite s = sprites.get(indexTowerClicked);
 	    	if(s instanceof TowerSprite){
 	    		g.setColor(Color.blue);
 				System.out.println("Circle ! "+s.getPosition().x);
 	    		g.drawOval(s.getPosition().x-(((TowerSprite) s).getRange()/2), s.getPosition().y -(((TowerSprite) s).getRange()/2), ((TowerSprite) s).getRange(), ((TowerSprite) s).getRange());
 	    	}
 	    }
 	    if(baseClicked){
 	    	g.setColor(Color.blue);
     		g.drawLine(basePosition.x, basePosition.y, mousePosition.x, mousePosition.y);
 	    }
 	  }              
 }
