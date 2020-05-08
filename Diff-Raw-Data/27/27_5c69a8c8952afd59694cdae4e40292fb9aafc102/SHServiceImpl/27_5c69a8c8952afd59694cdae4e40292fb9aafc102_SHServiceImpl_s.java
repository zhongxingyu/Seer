 package fr.esir.sh.server;
 
 import java.rmi.RemoteException;
 import java.util.Random;
 import fr.esir.sh.client.SHService;
 
 public class SHServiceImpl extends java.rmi.server.UnicastRemoteObject
 		implements SHService {
 	
 	private static final long serialVersionUID = 1L;
 	
 	private int cellSize = 20;
 	
 	private int gridSize = 20;
 	
 	private int numberOfSweets = 10;
 	
 	boolean[][] gameMap = addSweetsIntoLandscape();
 	
 	Player player = new Player(0, 0);
 	
 	public SHServiceImpl() throws java.rmi.RemoteException {
 	
 		super();
 		
 	}
 	
 	@Override
 	public int getCellSize() throws RemoteException {
 	
 		return cellSize;
 	
 	}
 
 	@Override
 	public int getGridSize() throws RemoteException {
 	
 		return gridSize;
 	
 	}
 	
 	@Override
 	public int getNumberOfSweets() throws RemoteException {
 		
 		return this.numberOfSweets;
 		
 	}
 	
 	public boolean[][] addSweetsIntoLandscape(){
 		
 		boolean[][] gameMap = new boolean[gridSize][gridSize];
 		
 	    Random rand = new Random();
 		
 		for(int i = 0; i < numberOfSweets; i++) {
 			
 			int j, k;	      
 			
 			do {
 	    	  
 				j = rand.nextInt(gridSize);
 	        
 				k = rand.nextInt(gridSize);
 	        
 			} while (gameMap[j][k] == true);
 			
 			gameMap[j][k] = true;
 			
 	    }
 		
 		return gameMap;
 		
 	}
 	
 	@Override
 	public boolean[][] getLogicGameMap()  throws RemoteException{
 		
 		return this.gameMap;
 		
 	}
 
 	@Override
 	public int[] movePlayerToRight()
 			throws RemoteException {
 	
 		int x = this.player.getX();
 		
 		x = (x + 1) % this.getGridSize();
 		
 		this.player.setX(x);
 		
 		this.verifyIfStrawberryCollected();
 		
 		int y = this.player.getY();
 		
 		int result[] = {x, y};
 		
 		return result;
 		
 	}
 
 	@Override
 	public int[] movePlayerToLeft()
 			throws RemoteException {
 	
 		int x = this.player.getX();
 		
 		x = (x - 1) % this.getGridSize();
 
 		if(x == -1) x = 19;
 		
 		this.player.setX(x);
 		
 		this.verifyIfStrawberryCollected();
 		
 		int y = this.player.getY();
 		
 		int result[] = {x, y};
 		
 		return result;
 		
 	}
 
 	@Override
 	public int[] movePlayerToDown()
 			throws RemoteException {
 	
 		int y = this.player.getY();
 		
 		y = (y + 1) % this.getGridSize();
 		
 		this.player.setY(y);
 		
 		this.verifyIfStrawberryCollected();
 		
 		int x = this.player.getX();
 		
 		int result[] = {x, y};
 		
 		return result;
 		
 	}
 	
 	@Override
 	public int[] movePlayerToUp()
 			throws RemoteException {
 	
 		int y = this.player.getY();
 		
 		y = (y - 1) % this.getGridSize();
 
 		if(y == -1) y = 19;
 		
 		this.player.setY(y);
 		
 		this.verifyIfStrawberryCollected();
 		
 		int x = this.player.getX();
 		
 		int result[] = {x, y};
 		
 		return result;
 		
 	}
 	
 	@Override
 	public int getPlayerXPos()
 			throws RemoteException {
 	
 		int x = this.player.getX();
 		
 		return x;
 
 	}
 
 	@Override
 	public int getPlayerYPos()
 			throws RemoteException {
 	
 		int y = this.player.getY();
 		
 		return y;
 		
 	}
 	
 	private void verifyIfStrawberryCollected(){
 		
 		int x = this.player.getX();
 		
 		int y = this.player.getY();
 		
 		if(this.gameMap[x][y] == true){
 			
 			this.gameMap[x][y]= false;
 			
			System.out
					.println("--------------"+x+" "+y);
		}
 			
 	}
 }
