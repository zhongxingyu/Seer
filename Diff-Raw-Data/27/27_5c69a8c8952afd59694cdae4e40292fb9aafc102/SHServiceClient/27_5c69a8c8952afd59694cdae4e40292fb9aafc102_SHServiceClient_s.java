 package fr.esir.sh.client;
 
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.net.MalformedURLException;
 import java.rmi.Naming;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 
 import javax.swing.JFrame;
 
 import fr.esir.sh.guinterface.Circle;
 import fr.esir.sh.guinterface.Rectangle;
 
 public class SHServiceClient extends JFrame implements KeyListener{
 	
 	//Attributes//////////////////////////////////////////////////////////////////////////////////////
 	private static final long serialVersionUID = 1L;
 
 	private SHService shService  = initializeService();
 	
 	private int cellSize  = initializeCellSize();
 	
 	private int gridSize  = initializeGridSize();
 	
 	private int numberOfSweets  = initializeNumberOfSweets();
 	
 	Rectangle myRectangle = new Rectangle(this) ;
 	
 	Container myContainer ;
 	
 	Circle[][] gameMap;
 	
 	public SHServiceClient(){
 		
 		super();
 		
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	    
 		setResizable(false);
 	    
 		setLocation(30, 30);
 	    
 		myContainer = getContentPane();
 	
 	    myContainer.setPreferredSize(new Dimension(cellSize * (gridSize + 1), cellSize * (gridSize + 1) ));
 	    
 	    pack();
 	    
 	    gameMap = addSweetsIntoGUI();
 	    	
 	    setVisible(true);
 	    
 	    addKeyListener(this);
 	
 	}
 	
 	//Methods/////////////////////////////////////////////////////////////////////////////////////////
 	private SHService initializeService(){
 		
 		SHService shService;
 		
 		try {
 			
 			shService = (SHService) Naming.lookup("rmi://localhost:8090/SHService");
 			
 		}
 		catch (MalformedURLException e) {
 			
 			throw new IllegalStateException(
 					"MalformedURLException occured. Please check if the host name and/or the port number and/or the name of the service is/are correct",
 					e);
 			
 		}
 		catch (RemoteException e) {
 			
 			throw new IllegalStateException(
 					"MalformedURLException occured. Maybe an exception occured during the execution of a remote method call",
 					e);
 			
 		}
 		catch (NotBoundException e) {
 			
 			throw new IllegalStateException(
 					"NotBoundException occured while lookimg up or unbinding in the registry a name that has no associated binding.",
 					e);
 			
 		}
 		
 		return shService;
 		
 	}
 	
 	private int initializeCellSize(){
 		
 		int cellSize= 0;
 		
 		try {
 			
 			cellSize = shService.getCellSize();
 		
 		} 
 		catch (RemoteException e) {
 			
 			e.printStackTrace();
 		
 			System.err.println("RemoteException occured. " +
 					"the cell size is not loaded, this number is initialized to 20");
 			
 		}
 		
 		return cellSize;
 		
 	}
 	
 	private int initializeGridSize(){
 		
 		int gridSize= 0;
 		
 		try {
 			
 			gridSize = shService.getGridSize();
 		
 		} 
 		catch (RemoteException e) {
 		
 			System.err.println("RemoteException occured. " +
 					"the grid size is not loaded, this number is initialized to 20");
 			
 		}
 		
 		return gridSize;
 		
 	}
 	
 	private int initializeNumberOfSweets(){
 		
 		int numberOfSweets= 0;
 		
 		try {
 			
 			numberOfSweets = shService.getNumberOfSweets();
 		
 		} 
 		catch (RemoteException e) {
 		
 			System.err.println("RemoteException occured. the number of the sweets is not loaded");
 			
 		}
 		
 		return numberOfSweets;
 		
 	}
 	
 	private boolean[][] initializeGameMap(){
 		
 		boolean[][] gameMap = null;
 		
 		try{
 			
 			gameMap = shService.getLogicGameMap();
 			
 		}
 		catch (RemoteException e) {
 			
 			System.err.println("RemoteException occured. the sweets are not loaded in the game map." +
 					"Consequently, this player would not have no sweet in his/her map");
 			
 		}
 		
 		return gameMap;
 		
 	}
 	
 	private Circle[][] addSweetsIntoGUI(){
 		
 		boolean[][] logicGameMap = initializeGameMap();
 		
 		Circle[][] gameMap = new Circle[gridSize][gridSize];
 	    
 	    for(int i= 0; i< gridSize; ++i){
 	    	
 	    	for(int j= 0; j< gridSize; ++j){
 	    		
 	    		if(logicGameMap[i][j] == true){
 	    			
 	    		      gameMap[i][j] = new Circle(this);
 	    		      
 	    		      gameMap[i][j].setGridPos(i,j);
 	    			
 	    		}
 	    		
 	    	}
 	    	
 	    }
 	    
 	    return gameMap;
 		
 	}
 	
 	public int getCellSize() {
 		
 		return this.cellSize;
 		
 	}
 	
 	public int getGridSize() {
 		
 		return this.gridSize;
 		
 	}
 	
 	public int getNumberOfSweets(){
 		
 		return this.numberOfSweets;
 		
 	}
 	
 	public static void main(String[] args) {
 		
 		SHServiceClient shService = new SHServiceClient();
 		
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		
 		  int keyCode = e.getKeyCode();
 		  
 		  int[] coord = {0, 0};
 		  		  
 		  try{
 			  
 			  if (keyCode == KeyEvent.VK_RIGHT)	coord= this.shService.movePlayerToRight();
 			  
 			  else if (keyCode == KeyEvent.VK_LEFT) coord= this.shService.movePlayerToLeft();
 			  
 			  else if (keyCode == KeyEvent.VK_DOWN) coord= this.shService.movePlayerToDown();
 			  
 			  else if (keyCode == KeyEvent.VK_UP) coord= this.shService.movePlayerToUp();
 			  
 		  }
 		  catch(RemoteException re){
 			  
 			  System.err.println("RemoteException occured. Could not reach the server to move the player. " +
 			  					 "Consequently, the player will not move.");
 			  
 		  }
 		  
 		  changeRectanglePos();
 		  
 		  int x= coord[0];
 		  
 		  int y= coord[1];
 		  
 		  if(this.gameMap[x][y] != null){
 			  
 			  Circle c = gameMap[myRectangle.x][myRectangle.y];
 		      
 			  myContainer.remove(c);
 	      
 			  pack();
 			  
 		  }
			  
			  
		  
 	}
 
 	private void changeRectanglePos(){
 		
 		  try{
 			  
 			  myRectangle.x = this.shService.getPlayerXPos();
 			  
 			  myRectangle.y = this.shService.getPlayerYPos();
 			  
 			  myRectangle.repaint();
 
 		  }
 		  catch(RemoteException re){
 			  
 			  System.err.println("RemoteException occured. Could not get the coordinates of the player.");
 			  
 		  }
 		
 	}
 	
 	@Override
 	public void keyReleased(KeyEvent e) {}
 	@Override
 	public void keyTyped(KeyEvent e) {}
 
 }
