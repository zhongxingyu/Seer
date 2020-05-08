 package View;
 
 import GameEngine.*;
 import GameEngine.Player.PlayerType;
 import GameEngine.TowerManager.TowerTypes;
 import Dispatcher.*; 
 import Dispatcher.AddTowerOrder.ErrorType;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
 import javax.swing.JFrame;
 
 /**
  * Project - TowerDefense</br>
  * <b>Class - ViewManager</b></br>
  * <p>The ViewManager class is responsible for the display of all the items the game need. It 
  * concerns the game and the user interface but also the display of menus at the beginning and
  * the end of a game.</br>
  * The ViewManager is also responsible for the updating of the scene.</br>
  * The ViewManager communicates with the GameEngine trough the Dispatcher.
  * </p> 
  * <b>Creation :</b> 22/04/2013</br>
  * @author K. Akyurek, A. Beauprez, T. Demenat, C. Lejeune - <b>IMAC</b></br>
  * @see MainViews
  * @see GameMenuBar
  * @see SceneView
  * @see GameManager
  */
 
 @SuppressWarnings("serial")
 public class ViewManager extends JFrame implements Runnable{
 	//Thread managers
 	private boolean running;
 	private DispatcherManager dispatcher;
 	private ConcurrentLinkedQueue<Order> queue;
 	
 	//Windows setting
     public static final int WIDTH = 800 ;
     public static final int HEIGHT = 600 ;
     private Image icon;
     
     //Main Panels	
     private HomeMenu homeMenu;
     private PlayMenu playMenu;
    // private OptionsMenu optionsMenu;
     
     //Editor Panels
     private EditorScene editorScene;
     private EditorToolBar editorToolBar;
     
     //Game Panels
     private SceneView sceneView;
     private GameMenuBar gameMenuBar;
     private GameInfoPlayer gameInfoPlayer;
     private GameInfoMenu gameInfoMenu;
     
     private boolean editor;
    
 	
     /**
      * Constructor of the ViewManager class
      */
     public ViewManager() {
 		super("PokéWars");
 		
 		queue = new ConcurrentLinkedQueue<Order>();
 		running = false;
 		
 		editor = false;
 		
 		homeMenu = new HomeMenu(this, new Point(0,0), WIDTH, HEIGHT);	
 		playMenu = new PlayMenu(this, new Point(0,0), WIDTH, HEIGHT);	
 		
 		editorScene = new EditorScene(this, new Point(0,0), 800, 400);
 		editorToolBar = new EditorToolBar(this, new Point(0,400), 800, 200, editorScene);
 
 		
 		sceneView = new SceneView(this,new Point(0,25), 800,400);
 		gameMenuBar = new GameMenuBar(this,new Point(0,0),800, 25);
 		gameInfoPlayer = new GameInfoPlayer(this, new Point(0,425), 185,150);
 		gameInfoMenu = new GameInfoMenu(this, new Point(185,425), 615 ,175);
 		
 		//Creating a border for the gameMenuBar panel
 		gameMenuBar.setBorder(BorderFactory.createLineBorder(Color.darkGray));
 
 
 		
 		//Loading the map icon
 		try {
 		      icon = ImageIO.read(new File("img/icon.png"));
 		  
 		} catch (IOException e) {
 		      e.printStackTrace();
 		}
 		
         //Create and lay the component on the windows
 		initComponents();
 		layComponents();
 
         //Main settings of the window
 		centralization();
 		setIconImage(icon);
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
 		setSize(WIDTH,HEIGHT);
 		setBackground(Color.gray);
 		setResizable(false);
         setVisible(true);
 	}
     
     /**
      * Initiate the window components
      * @see #ViewManager()
      */	
     public void  initComponents(){
 		homeMenu.setPreferredSize(new Dimension(homeMenu.getWidth(), homeMenu.getHeight()));
 		playMenu.setPreferredSize(new Dimension(playMenu.getWidth(), playMenu.getHeight()));
 		
         editorScene.setPreferredSize(new Dimension(editorScene.getWidth(), editorScene.getHeight()));
         editorToolBar.setPreferredSize(new Dimension(editorToolBar.getWidth(), editorToolBar.getHeight()));
 				
         sceneView.setPreferredSize(new Dimension(sceneView.getWidth(), sceneView.getHeight()));
         gameMenuBar.setPreferredSize(new Dimension(gameMenuBar.getWidth(), gameMenuBar.getHeight()));
         gameInfoPlayer.setPreferredSize(new Dimension(gameInfoPlayer.getWidth(), gameInfoPlayer.getHeight()));
         gameInfoMenu.setPreferredSize(new Dimension(gameInfoMenu.getWidth(), gameInfoMenu.getHeight()));
 
     }
 
     /**
      * Lay the component on the window
      * @see #ViewManager()
      */	
     public void layComponents(){
     	//Remove the default layout manager of the windows
     	//Allow us to lay the components according to absolute coordinates on the windows
     	setLayout(null);
     	
         //Move and Resize the components
 		homeMenu.setBounds(homeMenu.getPosition().x, homeMenu.getPosition().y,homeMenu.getWidth(), homeMenu.getHeight());	   	
 		playMenu.setBounds(playMenu.getPosition().x, playMenu.getPosition().y,playMenu.getWidth(), playMenu.getHeight());	   	
 		
 		editorScene.setBounds(editorScene.getPosition().x, editorScene.getPosition().y,editorScene.getWidth(), editorScene.getHeight());	   	
 	    editorToolBar.setBounds(editorToolBar.getPosition().x, editorToolBar.getPosition().y,editorToolBar.getWidth(), editorToolBar.getHeight());	   	
 			
 		sceneView.setBounds(sceneView.getPosition().x, sceneView.getPosition().y,sceneView.getWidth(), sceneView.getHeight());	
         gameMenuBar.setBounds(gameMenuBar.getPosition().x, gameMenuBar.getPosition().y,gameMenuBar.getWidth(), gameMenuBar.getHeight());	
         gameInfoPlayer.setBounds(gameInfoPlayer.getPosition().x, gameInfoPlayer.getPosition().y,gameInfoPlayer.getWidth(), gameInfoPlayer.getHeight());	
         gameInfoMenu.setBounds(gameInfoMenu.getPosition().x, gameInfoMenu.getPosition().y,gameInfoMenu.getWidth(), gameInfoMenu.getHeight());	
        
         //add the homeMenu panel on the window
         add(homeMenu);
     }
     
 	/**
 	 * Lay the window at the center of the screen
 	 * @see #ViewManager()
 	 */
 	public void centralization(){
 		//Retrieve the screen size
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		pack();
 		setLocation((screenSize.width-WIDTH)/2,(screenSize.height-HEIGHT)/2);	
 	}
 
 	/**
 	 * Initiate the dispatcher attribute
 	 * @see TowerDefense.TowerDefense#main(String[])
 	 */
 	public void setDispatcher(DispatcherManager dispatcher){
 		this.dispatcher = dispatcher;
 	}
 	
 	/**
 	 * Initialize the running attribute
 	 * @param r boolean
 	 * @see Dispatcher.DispatcherManager#start()
 	 * @see Dispatcher.DispatcherManager#stop()
 	 */
     public void setRunning(boolean running){
     	this.running = running;
     }
 	
     /**
      * Launch the Map Editor (you don't say !)
      */
     public void launchMapEditor(){
     	//Remove the homeMenu panel from the window
     	remove(homeMenu);
     	
     	
     	//Add the editor panels on the window
     	add(editorScene);
     	add(editorToolBar);
     	
     	editor = true;
         
         //Repaint the window
     	validate();
     	repaint();	 
     	
     	//Launch the choose file editor 
     	editorToolBar.showChooseFile();
     }
     
 	/**
 	 * Initialize the view when the game is launched
 	 * @param towers - ArrayList of towers created by the engine during the game initialization
 	 * @param bases 
 	 * @see Dispatcher.DispatcherManager#initiateGameView(ArrayList)
 	 */	
     public void initiateGameView(ArrayList<Base> bases, int money){
    	
    	//Clear the orders queue 
    	queue.clear();
 
 		//Clear the Sprites list of the scene
 		sceneView.initiate(money);
 		gameInfoMenu.initiate(sceneView);
 		gameInfoPlayer.initiate(sceneView, money);
		gameMenuBar.setInfo("Welcome in PokéWars. Let's battle !");
		
 		
 		Iterator<Base> iter = bases.iterator();
 		while (iter.hasNext()) {
 			//Retrieve the tower
 			Base base = iter.next();
 			//Create the corresponding TowerSprit
 			BaseSprite bs = new BaseSprite(sceneView, base.getId(), base.getPosition(),true, base.getPlayerType(), 36, 36,  base.getAmount());
 
 			//Add the baseSprite in the sceneView list of Sprites
 			sceneView.addSprite(bs);
 			//Add the matching TextInfoSprite in the sceneView list of Sprites
 			sceneView.addSprite(bs.getTextAmount());
 		}
 				
 		//The view and engine initializations are done ! The game can start !
 		dispatcher.start();	
     }
 	
 	/**
 	 * Show the game settings panel
 	 * @see HomeMenu#jButtonPlayPerformed(ActionEvent)
 	 */	
     public void createGame(){
     	//Clear the Sprites list of the playMenu
     	playMenu.initiate();
     	
     	//Remove the homeMenu panel from the window
     	remove(homeMenu);
     	
     	//Add the play panels on the window
     	add(playMenu);
         
         //Repaint the window
     	validate();
     	repaint();	  	
     }
     
     /**
      * Tell the GameInfoMenu to display the information of a tower
      */
     public void towerClicked(int id, PlayerType playerType, TowerTypes towerType, ArrayList<TowerManager.TowerTypes> evolutions){
     	gameInfoMenu.towerClicked(id, playerType, towerType, evolutions);
     }
     /**
      * Tell the GameInfoMenu to hide the information of a tower
      */
     public void hideTowerInfo(){
     	gameInfoMenu.hideTowerInfo();
     }   
     
 	/**
 	 * Launch the game
 	 * @see HomeMenu#jButtonPlayPesrformed(ActionEvent)
 	 */	
     public void play(PlayerType humanType, int nbEnemies, ArrayList<PlayerType> enemiesType){
     	
 		//tell the scene and the gameInfoMenu the id of the human player
 		sceneView.setHumanType(humanType);
 		gameInfoMenu.setHumanType(humanType);
 		gameInfoPlayer.setHumanType(humanType);
     	
     	//Tell the engine (via the dispatcher) to initiate the game
     	dispatcher.initiateGame(humanType,nbEnemies, enemiesType);
 
     	//Remove the homeMenu panel from the window
     	remove(playMenu);
     	
     	//Add the game panels on the window
     	add(sceneView);
         add(gameMenuBar);
         add(gameInfoPlayer);
         add(gameInfoMenu);
         
         //Repaint the window
     	validate();
     	repaint();	  	
     }
 	/**
 	 * Stop the game and display the homeMenu
 	 * @see GameMenuBar#jButtonBackPerformed(ActionEvent) 
 	 */	
     public void homeMenu(){
     	//Tell the dispatcher to stop the game threads
     	if(running){
     		dispatcher.stop();
     	
 	    	//Remove the game panels from the window
 	    	remove(sceneView);
 	    	remove(gameMenuBar);
 	    	remove(gameInfoPlayer);
 	    	remove(gameInfoMenu);
     	}
     	else if(editor){
     		remove(playMenu);
     		remove(editorScene);
     		remove(editorToolBar);
     		editor =false;
     	}
     	else {
     		remove(playMenu);
     	}
 	    	//Add the homeMenu panel on the window
 	    	add(homeMenu);
         
     	//Repaint the window
     	validate();
     	repaint();	  	
     }
  
    /**
     * Tell the dispatcher a tower need to be suppress
     * @param position
     * @param playerType
     * @see SceneView#towerToSupress(Point, int)
     */
    public void towerToSupress(int id){
 	   dispatcher.addOrderToEngine(new SuppressOrder(id));
    }
    
 	/**
 	 * Tell the dispatcher a tower need to be add
 	 * @param position
 	 * @param humanType
 	 * @param towerTypes
 	 * @see SceneView#myMousePressed()
 	 */
 	public void towerToAdd(Point position, PlayerType playerType, TowerTypes towerTypes) {
 		   dispatcher.addOrderToEngine(new AddTowerOrder(-1, playerType, position, towerTypes, -1, ErrorType.NONE));
 	}
 	/**
 	 * Tell the dispatcher a player want to attack an other base
 	 * @param position
 	 * @param humanType
 	 * @param towerType
 	 * @see SceneView#myMousePressed()
 	 */
 	public void baseToAttack(int idBaseSrc, int idBaseDestination, int amount) {
 		   dispatcher.addOrderToEngine(new AddUnitOrder(-1, idBaseSrc, idBaseDestination, amount));
 	}
     
 	/**
 	 * Tell the dispatcher a player want to evolve one of his tower
 	 * @param position
 	 * @param humanType
 	 * @param towerType
 	 * @see SceneView#myMousePressed()
 	 */
 	public void evolveTower(int id, TowerTypes towerType){
 		   dispatcher.addOrderToEngine(new EvolveTowerOrder(id, towerType,-1));
 	}
 	
 	/**
 	 * Refresh the graphic component of the view
 	 * @see #run()
 	 */	
 	public void refresh(){
 		//Retrieve the current size of the queue
 		int nb = queue.size();
 		
 		//Execute and remove the "size" first orders of the queue
 		if(nb>0){
 			for(int i = 0;i<nb; i++){
 				//Retrieve and remove the head of the queue
 				Order o = queue.poll();
 				
 				//If the order is a SuppressOrder one
 				if(o instanceof SuppressOrder) {
 					sceneView.suppressObject(o.getId());
 				}
 				
 				//If the order is an AddTowerOrder one
 				if(o instanceof AddTowerOrder) {
 					sceneView.addTower(o.getId(),((AddTowerOrder) o).getPlayerType(), ((AddTowerOrder) o).getPosition(), ((AddTowerOrder) o).getTowerType(),((AddTowerOrder) o).getRange(), ((AddTowerOrder) o).getErrorType());
 				}
 							
 				//If the order is an EvolveTowerOrder one
 				if(o instanceof EvolveTowerOrder) {
 					sceneView.evolveTower(o.getId(),((EvolveTowerOrder) o).getType(),((EvolveTowerOrder) o).getRange());
 					//System.out.println("View - TODO : make the tower "+o.getId()+" evolve in "+((EvolveTowerOrder) o).getType());
 				}
 				
 				//If the order is an AddUnitOrder one
 				if(o instanceof AddUnitOrder) {
 					sceneView.addUnit(((AddUnitOrder) o).getId(), ((AddUnitOrder) o).getSrcId(),((AddUnitOrder) o).getAmount());
 					//System.out.println("View - TODO : Add  "+((AddUnitOrder) o).getAmount()+" units from "+((AddUnitOrder) o).getSrcId());
 				}
 				//if the order is an AddEffectOrder
 				if(o instanceof AddEffectOrder){
 					sceneView.addEffect(o.getId(),((AddEffectOrder) o).getType(), ((AddEffectOrder) o).getIdUnit() );
 				}
 				
 				if(o instanceof AddMissileOrder){
 					//int id, PlayerType playerType, Point position, boolean isArea
 					sceneView.addMissile(((AddMissileOrder) o).getId(), ((AddMissileOrder) o).getPlayerType(), ((AddMissileOrder) o).getPosition(),((AddMissileOrder) o).isArea());
 					//System.out.println("View - TODO : Add  "+((AddUnitOrder) o).getAmount()+" units from "+((AddUnitOrder) o).getSrcId());
 				}
 				
 				//If the order is an MoveUnitOrder one
 				if(o instanceof MoveUnitOrder) {
 					sceneView.moveUnit(((MoveUnitOrder) o).getId(),((MoveUnitOrder) o).getNewPosition());
 				}
 					
 				//If the order is an MoveMissileOrder one
 				if(o instanceof MoveMissileOrder) {
 					sceneView.moveMissile(((MoveMissileOrder) o).getId(),((MoveMissileOrder) o).getNewPosition());
 				}
 				
 				//If the order is an ChangeAmountOrder (base or unit) one
 				if(o instanceof ChangeAmountOrder){
 					sceneView.setAmount(((ChangeAmountOrder) o).getId(), ((ChangeAmountOrder) o).getAmount());
 				}
 				//If the order is a MoneyOrder one
 				if(o instanceof MoneyOrder){
 					sceneView.setMoney(((MoneyOrder) o).getAmount(),((MoneyOrder) o).getPlayerType());
 					gameInfoPlayer.setMoney(((MoneyOrder) o).getAmount(),((MoneyOrder) o).getPlayerType());
 					
 				}		
 				//If the order is a ChangeOwnerOrder
 				if(o instanceof ChangeOwnerOrder){
 					sceneView.setOwner(((ChangeOwnerOrder) o).getId(), ((ChangeOwnerOrder) o).getNewPlayerType());
 					//Reloading the modified territory map
 					sceneView.reloadTerritoryMap();
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Tell the gameMenuBar to set it's jInfo label text with the given String
 	 * @param text
 	 */
 	public void setInfoText(String text){
 		gameMenuBar.setInfo(text);
 	}
 	
 	/**
 	 * Add an order to the engine ConcurrentLinkedQueue queue
 	 * @see Dispatcher.DispatcherManager#addOrderToView(Order)
 	 */	
 	public void addOrder(Order order){
 		//Add the order to the queue
 		queue.add(order);
 	}
 
 	/**
 	 * run() method of the view thread
 	 */	
 	@Override
 	public void run() {
 		while(running){
 			/*try{
 				Thread.sleep(500);
 			}
 			catch(InterruptedException e){
 				System.out.println(e.getMessage());
 			}*/
 			
 			refresh();
 		}
 	}
 
 }
