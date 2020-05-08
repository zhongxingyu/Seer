 package epicCrawl;
 
 //Imports
 import javax.swing.*;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.util.ArrayList;
 
 //HUGE 2d array containing "current level/world" map
 
 //maybe instead of setting town or house could create them at start up and store them into list of levels..
 
 //_grid is current view of the level/world
 
 //change later so when clicked the focus is on this panel
 
 //
 public class GameGrid extends JPanel{
 	private static final long serialVersionUID = 6942355989519710021L;
 
 	//public static final int _GridSize = 1000; // Size of an individual square
 	
 	private ArrayList<GridSquare> gridSquares;
 
 	private int _maxRows = 30, _maxColumns = 30; // Size of the available grid
 	private int _rows = 15, _columns = 15;
 	
 	private Action leftAction, rightAction, upAction, downAction;
 	
 	//   private char _numAdjGrids = 0; // Accesible rooms from current room
 	private GridSquare[][] _grid; // Grid for current map
 	
 	private int playerX, playerY, newX, newY; // --------------------------------------------------
 	private GridSquare gridSquareUnderPlayer;
 
 	// Room constructor
 	public GameGrid(){
 		//gridSquareUnderPlayer = GridSquareTypes.FLOOR;
 		
 		setFocusable(true); // Not needed?
 		
 		setUpKeyBindings();
 
 		// change to load in saved spot..??
 		_grid = new GridSquare[_maxRows][_maxColumns]; // Initialize the grid
 
 		// Populate the grid with all floor objects, can be changed later
 		for(int i = 0; i < _maxRows; ++i){
 			for(int j = 0; j < _maxColumns; ++j)
 				_grid[i][j] = GridSquareTypes.FLOOR;
 		}
 		
 		for(int i = 0; i < _maxRows; ++i){
 			_grid[i][0] = GridSquareTypes.WALL;
 			_grid[i][_maxRows - 1] = GridSquareTypes.WALL;
 		}
 		
 		for(int i = 0; i < _maxRows; ++i){
 			_grid[0][i] = GridSquareTypes.WALL;
 			_grid[_maxColumns - 1][i] = GridSquareTypes.WALL;
 		}
 	}
 	
 	public void setHome(){
 		for(int i = 3; i < 13; ++i)
 			_grid[3][i] = _grid[12][i] = GridSquareTypes.WALL;
 		
 		for(int j = 4; j < 12; ++j)
 			_grid[j][3] = _grid[j][12] = GridSquareTypes.WALL;
 		
 		for(int i = 4; i < 12; ++i){
 			for(int j = 4; j < 12; ++j)
 				_grid[i][j] = GridSquareTypes.FLOOR;
 		}
 		
 		_grid[7][6] = GridSquareTypes.LCHAIR;
 		_grid[8][6] = GridSquareTypes.TABLE;
 		_grid[9][6] = GridSquareTypes.RCHAIR;
 		
 		_grid[7][12] = GridSquareTypes.DOOR;
 		
 		_grid[20][20] = GridSquareTypes.CHEST;
 		
 		_grid[20][5] = GridSquareTypes.GIRL;
 		
 		playerX = 15;
 		playerY = 15;
 		gridSquareUnderPlayer = _grid[playerX][playerY];
 		_grid[playerX][playerY] = GridSquareTypes.CHARACTER;
 	}
 	
 	private void movePlayer(String direction){
 		
 		if(direction.equals("left")){
 			newX = playerX - 1;
 			newY = playerY;
 		}
 		else if(direction.equals("up")){
 			newX = playerX;
 			newY = playerY - 1;
 		}
 		else if(direction.equals("right")){
 			newX = playerX + 1;
 			newY = playerY;
 		}
 		else if(direction.equals("down")){
 			newX = playerX;
 			newY = playerY + 1;
 		}
 		else{
 			System.out.println("wtf");
 		}
 		
 		// check if move is out of bounds
 		if(newX < 0 || newX > _maxRows -1 || newY < 0 || newY > _maxColumns - 1){
 			System.out.println("Attempting to move out of bounds");
 			return;
 		}
 		
 		// grab square type 
 		GridSquare nextSquare = _grid[newX][newY];
 		
 		if(!nextSquare.isPassable()){ // check if move is passable
 			System.out.println("Attempting to move through solid object");
 			return;
 		}
 		// restore last grid square
 		_grid[playerX][playerY] = gridSquareUnderPlayer;
 		gridSquareUnderPlayer = _grid[newX][newY];
 		_grid[newX][newY] = GridSquareTypes.CHARACTER;
 		// save grid square type
 		
 		playerX = newX;
 		playerY = newY;
 		
 		repaint();
 		// do move
 	}
 	
 	@Override
 	public void paintComponent(Graphics g){
 		super.paintComponent(g); // Important to call super class method
 		g.clearRect(0, 0, getWidth(), getHeight()); // Clear the board
 
 		int recW = getWidth() / _rows; // Draw the grid
 		int recH = getHeight() / _columns;
 		
 		for (int i = 0; i <  _rows; i++){ // Determine what to draw for each grid square on grid
 			int viewX = (playerX - (7) + i);
 			int xCord = i * recW; // Upper left corner of this terrain rect
 			
 			for (int j = 0; j < _columns; j++){
 				int yCord = j * recH;
 				int viewY = (playerY - (7) + j);
 				
 				if(playerX < 8)
 					viewX = i;
 				if(playerY < 8)
 					viewY = j;
 				if(playerX > _maxColumns - 8)
 					viewX = _maxColumns - _columns + i;
 				if(playerY > _maxRows - 8)
 					viewY = _maxRows - _rows + j;
 				
 				System.out.println("(" + playerX + ", " + playerY + ")");
 				
				if(_grid[viewX][viewY] == GridSquareTypes.VOID){ // Paint black grid for void space
 					g.setColor(Color.BLACK); // All void space is black
 					g.fillRect(xCord, yCord, recW, recH); // Fill in the square with black
 				}
 				
				if(_grid[viewX][viewY] != GridSquareTypes.VOID){
					g.drawImage(GridSquareTypes.FLOOR.getImage(), xCord, yCord, recW, recH, null);
 					
					if(_grid[viewX][viewY] != GridSquareTypes.FLOOR)
						g.drawImage(_grid[viewX][viewY].getImage(), xCord, yCord, recW, recH, null);
 				}
 			}
 		}
 		
 		System.out.println("exit");
 	}
 
 	private void setUpKeyBindings(){
 		leftAction = new LeftAction();
 		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke( 'a' ), "doLeftAction");  
 		getActionMap().put( "doLeftAction", leftAction );
 		
 		upAction = new UpAction();
 		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke( 'w' ), "doUpAction");  
 		getActionMap().put( "doUpAction", upAction );
 		
 		rightAction = new RightAction();
 		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke( 'd' ), "doRightAction");  
 		getActionMap().put( "doRightAction", rightAction );
 		
 		downAction = new DownAction();
 		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke( 's' ), "doDownAction");  
 		getActionMap().put( "doDownAction", downAction );
 	}
 	
 	class LeftAction extends AbstractAction{
 
 		public void actionPerformed( ActionEvent ae ){
 			movePlayer("left");
 		}
 	}
 	
 	class UpAction extends AbstractAction{
 
 		public void actionPerformed( ActionEvent ae ){
 			movePlayer("up");
 		}
 	}
 	
 	class RightAction extends AbstractAction{
 
 		public void actionPerformed( ActionEvent ae ){
 			movePlayer("right");
 		}
 	}
 	
 	class DownAction extends AbstractAction{
 
 		public void actionPerformed( ActionEvent ae ){
 			movePlayer("down");
 		}
 	}
 
 	
 	public static void main(java.lang.String[] args){
 		JFrame frame = new JFrame();
 		GridSquareTypes GridSquareTypes = new GridSquareTypes();
 		
 		GameGrid grid = new GameGrid();
 		grid.setVisible(true);
 		grid.setHome();
 		grid.repaint();
 		
 		frame.add(grid);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close on exit
 		frame.setTitle("Epic Crawl"); // Game title
 		frame.setSize(700,700); // Size of play window on start, later support changing screen size
 		frame.setLocationRelativeTo(null);
 		frame.setVisible(true);
 		frame.repaint();
 	}
}
