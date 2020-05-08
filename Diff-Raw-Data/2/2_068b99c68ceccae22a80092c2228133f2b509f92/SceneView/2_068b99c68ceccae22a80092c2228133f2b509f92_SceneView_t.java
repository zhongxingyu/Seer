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
 import javax.swing.JLabel;
 import javax.swing.SwingUtilities;
 
 import GameEngine.TowerManager;
 import GameEngine.Player.PlayerType;
 import GameEngine.TowerManager.TowerTypes;
 
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
 public class SceneView extends MainViews implements Runnable{   
     private Image map;
     private Image territoryMap;
     
     private ArrayList<Sprite> sprites;
     
     private PlayerType humanType;
     
     private Color color;
     
     private boolean towerClicked;
     private Point clickedTowerPosition;
     
     private boolean addTowerClicked;
     private Point addTowerPosition;
    
     private boolean baseClicked;
     private boolean attackBase;
     private int attackAmountPercent;
     private JLabel jAttackAmountPercent;
     private int idBaseSrc;
     private int idBaseDst;
     private Point basePosition;
     private Point baseToAttackPosition;
     private Point mousePosition;
 	private Thread thread;
 	
 	private int money;
 	
     
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
 		attackAmountPercent = 50;
 		mousePosition = new Point(0,0);
 		clickedTowerPosition = new Point(0,0);
 		humanType = PlayerType.ELECTRIC;
 		money = 0;
 
 		jAttackAmountPercent = new JLabel();
 		jAttackAmountPercent.setForeground(Color.BLACK);
 
 		
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
 	 * @see
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
 	 * Add a Sprite in the ScenView ArrayList
 	 * @param sprite
 	 * @see ViewManager#initiateGameView(ArrayList)
 	 */
 	public void addSprite(final Sprite sprite){
 		SwingUtilities.invokeLater(new Runnable(){
 		public void run() {
 			sprites.add(sprite);
 			
 			//TO DO : retrieve the last element added...
 			Iterator<Sprite> it = sprites.iterator();
 			while (it.hasNext()) {
 				Sprite element = it.next();
 				if(element instanceof UnitSprite){
 					element.setBounds(element.getPosition().x -(element.getWidth()/2), element.getPosition().y -(element.getHeight()), element.getWidth(),element.getHeight());
 				}
 				else element.setBounds(element.getPosition().x -(element.getWidth()/2), element.getPosition().y -(element.getHeight()/2), element.getWidth(),element.getHeight());
 				add(element);
 			}
 			
 	        //Repaint the panel
 	    	revalidate();
 	    	repaint();	
 		}});
 	}
 	
 	/**
 	 * Reset the SceneView
 	 * @see ViewManager#initiateGameView(ArrayList)
 	 */
 	public void initiate(int money){
 		
 		//resetting the money
 		this.money = money;
 
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
 		
 		SwingUtilities.invokeLater(new Runnable(){
 		public void run() {
 		//Resetting the attackAmount
 		attackAmountPercent = 50;
 
 		jAttackAmountPercent.setVisible(false);
 		remove(jAttackAmountPercent);
 		
 		
 		//Removing all the Sprites		
 		Iterator<Sprite> it = sprites.iterator();
 		while (it.hasNext()) {
 			Sprite element = it.next();
 			it.remove();
 			remove(element);
 		}
 		
 		//Loading the image map
 		try {
 		      map = ImageIO.read(new File("img/map/MapView.png"));
 		      territoryMap = ImageIO.read(new File("tmp/tm.png"));
 		  
 		} catch (IOException e) {
 		      e.printStackTrace();
 		}
 		
 		//Add the AddTower Attack Sprite on the panel
 		//addSprite(new AddTowerSprite(this, new Point(30,310), true, humanType, 55, 55, 1));
 		//addSprite(new AddTowerSprite(this, new Point(30,370), true, humanType, 55, 55, 2));
 		
 		
 		if (addTowerClicked) {
 			addTowerFailed();
 		}
 		if (baseClicked) baseClicked = false;
 		if(towerClicked) hideTowerInfo();
 		
         //Repaint the panel
     	revalidate();
     	repaint();	
 		}}); 
 	}
 	
 	/**
 	 * Reload the modified territory Map
 	 */
 	public void reloadTerritoryMap(){
 		try {
 		      territoryMap = ImageIO.read(new File("tmp/tm.png"));
 		  
 		} catch (IOException e) {
 		      e.printStackTrace();
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
 	private void myMouseMoved(final MouseEvent e) {	
 		SwingUtilities.invokeLater(new Runnable(){
 		public void run() {
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
					if((element.getPosition().equals(addTowerPosition))&&(element instanceof TowerSprite)&&(element.getId()==-1)){
 						//Reset the tower Sprite Position according to the mouse one						
 						if(e.getPoint().y<(height-10)){
 							addTowerPosition = new Point(e.getPoint());
 							element.setPosition(addTowerPosition);
 							element.setBounds(element.getPosition().x -(element.getWidth()/2), element.getPosition().y -(element.getHeight()/2), element.getWidth(),element.getHeight());
 							add(element);
 						}
 						else {
 							remove(element);
 						}
 					}
 				}		
 				//Repaint the Panel
 		    	revalidate();
 		    	repaint();
 			}
 		}});
 	}
 	
 	/**
 	 * Display the TowerInfoSprites of a clicked tower
 	 * @param position - Point : position of the tower
 	 * @param playerType - PlayerType
 	 * @see TowerSprite#myMousePressed(MouseEvent)
 	 */
 	public void towerClicked(int id, Point position, PlayerType playerType, TowerTypes towerType, ArrayList<TowerManager.TowerTypes> evolutions){
 
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
 		
 		final Point positionSprite = new Point(position);
 		//positionSprite.translate((50/2) + (16/2),(50/2) + (16/2));
 		positionSprite.translate(64/2,64/2);
 		
 		//Add the TowerInfoSprite
 		//TODO !Metre les sprites d'info des tours dans les tours elle-meme...
 		sprites.add(new TowerInfoSprite(this,id, positionSprite, true, playerType, 16,16, 0, position));	
 		
 		SwingUtilities.invokeLater(new Runnable(){
 		public void run() {
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
 		}});	
 		towerClicked = true;
 		
 		//Tell the ViewManager that a tower has been clicked
 		view.towerClicked(id, playerType, towerType, evolutions);
 		
 		//Repaint the panel
     	revalidate();
     	repaint();
 	}
 	
 	public void evolveTower(final int id, final TowerTypes towerType, final int range){
 		SwingUtilities.invokeLater(new Runnable(){
 		public void run() {
 			Iterator<Sprite> it = sprites.iterator();
 			while (it.hasNext()) {
 				Sprite element = it.next();
 				//Set the baseSprite amount
 				if((element.getId()==id)&&(element instanceof TowerSprite)){
 					System.out.println(range);
 					((TowerSprite)element).setTowerType(towerType);
 					((TowerSprite)element).setRange(range);
 				}
 			}
 		}});
 		//If the tower was clicked
 		if(towerClicked){
 			hideTowerInfo();
 		}
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
 		//Tell the viewManager that the tower info need to be hide in the GameInfoMenu
 		view.hideTowerInfo();
 		
 		SwingUtilities.invokeLater(new Runnable(){
 		public void run() {
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
 		}});
 	}
 	
 	/**
 	 * Display the territory map when the player want to add a tower
 	 * @param position - Position of the center of the AddTower button clicked
 	 * @param playerType
 	 * @param towerType
 	 */
 	public void addTowerClicked(Point position, PlayerType playerType, TowerTypes towerType){
 		if(!addTowerClicked){
 			addTowerClicked = true;
 			addTowerPosition = new Point(position.x+1, position.y+1);
 			int range;
 			if(towerType == TowerTypes.ATTACKTOWER){
 				range = 80;
 			}
 			else range = 35;
 			//TODO : change the id of the tower if it's add by the engine...
 			TowerSprite ts = new TowerSprite(this, -1, addTowerPosition, false, humanType, 64, 64, towerType, range);
 			
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
 	public void addTower(int id, PlayerType playerType, Point position, TowerTypes towerType, int range){
 		Point test = new Point(-1,-1);
 		
 		//If the position of the tower is (-1,-1), the tower can't be add :
 		if(position.equals(test)){
 			addTowerFailed();
 		}
 		else{
 		
 			//If the tower to add is owned by the human player 
 			if(position.equals(addTowerPosition)){
 				addTowerSuccess(id, range);
 			}
 			
 			//TODO If the tower to add is owned by an AI player
 			else{
 				addSprite(new TowerSprite(this, id, position, false, playerType,64, 64, towerType, range));
 			}
 			
 		}
 	}
 	
 	/**
 	 * Add a unit on the SceneView
 	 * @param position
 	 * @param playerType
 	 * @see ViewManager#refresh()
 	 */
 	public void addUnit(int id, int srcId, int amount){
 		Point position = null;
 		PlayerType playerType = null;
 		for(Sprite s: sprites){
 			if(s.getId()==srcId){
 				position = s.getPosition();
 				playerType = s.getPlayerType();
 				break;
 			}
 		}
 		if(position!=null){
 			UnitSprite unit = new UnitSprite(this,id, position, playerType, amount);
 			addSprite(unit);
 			addSprite(unit.getTextAmount());
 		}
 	}
 	
 	/**
 	 * Add a Missile on the SceneView
 	 * @param position
 	 * @param playerType
 	 * @see ViewManager#refresh()
 	 */	
 	public void addMissile(int id, PlayerType playerType, Point position, boolean isArea){
 		//If the missile is an area one, the matching tower need to be activated
 		if(isArea){
 			Iterator<Sprite> it = sprites.iterator();
 			while (it.hasNext()) {
 				Sprite element = it.next();
 				if((element.getPosition().equals(position))&&(element instanceof TowerSprite)){
 					((TowerSprite) element).setActivated(true);
 				}
 			}	
 		}
 		System.out.println("View - Add a Missile "+id);
 		MissileSprite unit = new MissileSprite(this,id, position, playerType, isArea);
 		addSprite(unit);
 	}
 	
 	/**
 	 * Add the tower the player wanted to add
 	 * @see #addTower(Point, PlayerType, int)
 	 */
 	public void addTowerSuccess(int id, int range){
 		addTowerClicked = false;
 		
 		//Set the tower Sprite clickable attribute to true
 		Iterator<Sprite> it = sprites.iterator();
 		while (it.hasNext()) {
 			Sprite element = it.next();
 			if(element.getPosition().equals(addTowerPosition)){
 				((TowerSprite) element).setClickable(true);
 				((TowerSprite) element).setId(id);
 				((TowerSprite) element).setRange(range);
 			}
 		}	
     	//Repaint the Panel
     	revalidate();
     	repaint();	
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
 		
 		SwingUtilities.invokeLater(new Runnable(){
 		public void run() {
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
 		}});
 	}
 	
 	/**
 	 * Display the line between the clicked base and the mouse cursor
 	 * @param position
 	 * @param playerType
 	 * @see BaseSprite#myMousePressed(MouseEvent)
 	 */
 	public void baseClicked(final int idBase, final Point position, PlayerType playerType){
 		if (towerClicked){
 			hideTowerInfo();
 		}
 		
 		if (addTowerClicked) {
 			addTowerFailed();
 		}
 		
 		//If the player has clicked on one of his base
 		if((!baseClicked)&&(playerType == humanType)){
 			this.idBaseSrc = idBase;
 			basePosition = new Point(position);
 			baseClicked = true;
 			mousePosition = new Point(position);
 		}
 		
 		//If the player has first clicked on one of his base, then clicked on an enemy base or on one of his other base
 		if((baseClicked)&&(idBase != idBaseSrc)){
 			SwingUtilities.invokeLater(new Runnable(){
 			public void run() {
 				//Set the amont percent
 				attackAmountPercent = 50;
 	
 				
 				baseToAttackPosition = new Point(position);
 				idBaseDst = idBase;
 				
 				if(baseToAttackPosition.x<=400){
 					jAttackAmountPercent.setBounds(baseToAttackPosition.x+20, baseToAttackPosition.y-10, 50,25);
 				}
 				else{
 					jAttackAmountPercent.setBounds(baseToAttackPosition.x-50, baseToAttackPosition.y-10, 50,25);
 				}
 				jAttackAmountPercent.setText(attackAmountPercent+"%");
 				jAttackAmountPercent.setVisible(true);
 				add(jAttackAmountPercent);
 			}});
 				//Start the thread
 				thread = new Thread(this);
 		        thread.start();
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
 		
 		//If the player has first clicked on one of his base, then clicked on an enemy base or one of on one of his other base
 		if(attackBase){
 			//Remove the line between the two bases
 			baseClicked = false;
 			//Stop the thread
 			attackBase = false;
 		
 			//Tell the engine that the player want to attack an other base
 			view.baseToAttack(idBaseSrc,idBaseDst, attackAmountPercent);
 
 			SwingUtilities.invokeLater(new Runnable(){
 			public void run() {
 				jAttackAmountPercent.setVisible(false);
 				remove(jAttackAmountPercent);
 			}});
 		}
 		
 		//Repaint the panel
     	revalidate();
     	repaint();		
 	}
 	
 	public void run()
 	{
 		 while(attackBase)
 		 {
 			 try{
 				Thread.sleep(50);
 				SwingUtilities.invokeLater(new Runnable(){
 				public void run() {
 					 if((attackAmountPercent+1)<=99)  attackAmountPercent+=1;
 					 jAttackAmountPercent.setText(attackAmountPercent+"%");
 				}});
 		 	}catch(Exception e){e.printStackTrace();}
 		 }
 	}
 	
 	/**
 	 * Tell the view that a tower need to be suppressed
 	 * @param position of the tower to suppress
 	 * @param playerType
 	 * @see TowerInfoSprite#myMousePressed(MouseEvent)
 	 */
 	 public void towerToSupress(int id){
 		   view.towerToSupress(id);
 	   }
 	 
 	/**
 	 * Suppress a tower and its Sprite info, or a Unit and its label, or
 	 * @param position
 	 * @param playerType
 	 * @see ViewManager#refresh()
 	 */
 	public void suppressObject(final int id){
 		SwingUtilities.invokeLater(new Runnable(){
 		public void run() {
 			Iterator<Sprite> it = sprites.iterator();	
 			while (it.hasNext()) {
 				Sprite element = it.next();
 				//Removing the towerSprite, the UnitSprite, the matching TextInfoSprite, or the MissileSprite
 				if(element.getId()==id){
 					System.out.println("View - Suppress the object "+id);
 					it.remove();
 					remove(element);
 					revalidate();
 					repaint();
 					if(element instanceof MissileSprite){
 						System.out.println("View - Suppress MissileSprite "+id);
 					}
 					
 					if(element instanceof TowerSprite){
 						hideTowerInfo();
 					}
 				}
 			}	
 
 		}});
 	}
 	
 	/**
 	 * Reset the base amount when the base or a unit is the source (or the destination) of an attack
 	 * @param position
 	 * @param playerType
 	 * @param newAmount
 	 * @see ViewManager#refresh()
 	 */
 	public void setAmount(final int id, final int newAmount){
 		SwingUtilities.invokeLater(new Runnable(){
 		public void run() {
 			Iterator<Sprite> it = sprites.iterator();
 			while (it.hasNext()) {
 				Sprite element = it.next();
 				//Set the baseSprite amount
 				if((element.getId()==id)&&(element instanceof BaseSprite)){
 					((BaseSprite)element).setAmount(newAmount);
 				}
 				if((element.getId()==id)&&(element instanceof UnitSprite)){
 					((UnitSprite)element).setAmount(newAmount);
 				}
 			}
 		}});
 	}
 	
 	/**
 	 * Reset the base owner when it has been taken
 	 * @param position
 	 * @param playerType
 	 * @param newAmount
 	 * @see ViewManager#refresh()
 	 */
 	public void setOwner(final int id, final PlayerType newPlayerType){
 		SwingUtilities.invokeLater(new Runnable(){
 		public void run() {
 			Iterator<Sprite> it = sprites.iterator();
 			while (it.hasNext()) {
 				Sprite element = it.next();
 				//Set the baseSprite amount
 				if((element.getId()==id)&&(element instanceof BaseSprite)){
 					((BaseSprite)element).setPlayerType(newPlayerType);
 				}
 				if((element.getId()==id)&&(element instanceof TowerSprite)){
 					//If the tower was the clicked one 
 					if(((TowerSprite)element).getPosition().equals(clickedTowerPosition)) hideTowerInfo();
 					//Reset the tower playerType et clickable attributs
 					boolean clickable = false;
 					if (newPlayerType==humanType) clickable = true;
 					((TowerSprite)element).setPlayerType(newPlayerType,clickable);
 				}
 			}
 		}});
 	}
 	
 	/**
 	 * 
 	 * @param money
 	 * @param playerType
 	 */
 	public void setMoney(int money, PlayerType playerType){
 		if(playerType == humanType){
 			this.money = money;
 		}	
 	}
 	
 	/**
 	 * Reset the unit (and its label) position 
 	 * @param position
 	 * @param playerType
 	 * @param newAmount
 	 * @see ViewManager#refresh()
 	 */
 	public void moveUnit(final int id, final Point newPosition){
 		
 		SwingUtilities.invokeLater(new Runnable(){
 		public void run() {
 			Iterator<Sprite> it = sprites.iterator();
 			
 			while (it.hasNext()) {
 				Sprite element = it.next();
 				//Set the baseSprite amount
 				if(element.getId()==id){
 					if(element instanceof UnitSprite){
 						
 						//If the Sprite have to move to the right, the image need to be flipped
 						if(((UnitSprite)element).getPosition().x<newPosition.x){((UnitSprite)element).setFlipped(true);}
 						else {((UnitSprite)element).setFlipped(false);}
 						
 						((UnitSprite)element).setPosition(newPosition);
 									
 						remove(element);
 						element.setBounds(element.getPosition().x -(element.getWidth()/2), element.getPosition().y -(element.getHeight()), element.getWidth(),element.getHeight());
 						add(element);
 					
 						revalidate();
 						repaint();
 					}
 					
 					if(element instanceof TextInfoSprite){
 						Point textPosition = new Point(newPosition.x, newPosition.y - 20);
 						
 						((TextInfoSprite)element).setPosition(textPosition);
 						remove(element);
 						element.setBounds(element.getPosition().x -(element.getWidth()/2), element.getPosition().y -(element.getHeight()), element.getWidth(),element.getHeight());
 						add(element);
 						revalidate();
 						repaint();
 					}
 				}		
 			}
 		}});
 	}
 	
 	
 	/**
 	 * Reset the missile position 
 	 * @param position
 	 * @param playerType
 	 * @param newAmount
 	 * @see ViewManager#refresh()
 	 */
 	public void moveMissile(final int id, final Point newPosition){
 		
 		SwingUtilities.invokeLater(new Runnable(){
 		public void run() {
 			Iterator<Sprite> it = sprites.iterator();
 			
 			while (it.hasNext()) {
 				Sprite element = it.next();
 				//Set the baseSprite amount
 				if(element.getId()==id){
 					if(element instanceof MissileSprite){
 						((MissileSprite)element).setPosition(newPosition);
 									
 						remove(element);
 						element.setBounds(element.getPosition().x -(element.getWidth()/2), element.getPosition().y -(element.getHeight()/2), element.getWidth(),element.getHeight());
 						add(element);
 					
 						revalidate();
 						repaint();
 					}
 				}		
 			}
 		}});
 	}
 	
     /**
      * Draw the SceneView Panel
      */
     @Override
 	public void paintComponent(Graphics g){
 		super.paintComponent(g);
 	    g.drawImage(map, 0, 0, this.getWidth(), this.getHeight(), this);
 	    g.setColor(color);
 	    
 	    if(addTowerClicked){
 	    	//Display the territoryMap
 		    g.drawImage(territoryMap, 0, 0, this.getWidth(), this.getHeight(), this);
 		    //TODO Displaying the range of the tower to add
 		    if(addTowerPosition.y<(height-15)){
 				Iterator<Sprite> it = sprites.iterator();
 				while (it.hasNext()) {
 					Sprite s = it.next();
 					if((s.getPosition().equals(addTowerPosition))&& (s instanceof TowerSprite)){
 						g.fillOval(s.getPosition().x-(((TowerSprite) s).getRange()), s.getPosition().y -(((TowerSprite) s).getRange()), 2*((TowerSprite) s).getRange(), 2*((TowerSprite) s).getRange());
 					}
 				}
 		    }
 	    }    
 	    
 	    if(towerClicked){
 	    	//Retrieve the clicked tower
 			Iterator<Sprite> it = sprites.iterator();
 			while (it.hasNext()) {
 				Sprite s = it.next();
 				if((s.getPosition().equals(clickedTowerPosition))&& (s instanceof TowerSprite)){
 					g.fillOval(s.getPosition().x-(((TowerSprite) s).getRange()), s.getPosition().y -(((TowerSprite) s).getRange()), 2*((TowerSprite) s).getRange(), 2*((TowerSprite) s).getRange());
 				}
 			}
 	    }
 	    
 	    if(baseClicked){
 	    	//g.setColor(Color.blue);
     		g.drawLine(basePosition.x, basePosition.y, mousePosition.x, mousePosition.y);
 	    }
 	    
 	    //Retrieving the activated tower
 		Iterator<Sprite> iter = sprites.iterator();
 		while (iter.hasNext()) {
 			Sprite element = iter.next();
 			if(((element instanceof TowerSprite))&&(((TowerSprite) element).isActivated())){
 				Color towerColor = null;
 				if(((TowerSprite) element).getPlayerType() == PlayerType.ELECTRIC){
 					towerColor = new Color(255,255,0,50);
 				}
 				else if(((TowerSprite) element).getPlayerType() == PlayerType.WATER){
 					towerColor = new Color(0,0,255,50);
 				}
 				else if(((TowerSprite) element).getPlayerType() == PlayerType.GRASS){
 					towerColor = new Color(0,255,0,50);
 				}
 				else if(((TowerSprite) element).getPlayerType()== PlayerType.FIRE){
 					towerColor= new Color(255,0,0,50);
 				}
 				g.setColor(towerColor);
 				g.fillOval((((TowerSprite) element).getPosition().x-(((TowerSprite) element).getRange())), element.getPosition().y -(((TowerSprite) element).getRange()), 2*((TowerSprite) element).getRange(), 2*((TowerSprite) element).getRange());
 			}
 		}	
 	 }              
 }
