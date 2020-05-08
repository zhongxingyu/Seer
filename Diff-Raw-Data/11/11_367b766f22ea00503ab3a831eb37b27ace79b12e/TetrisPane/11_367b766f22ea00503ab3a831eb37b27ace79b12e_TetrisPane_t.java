 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 
 @SuppressWarnings("serial")
 public class TetrisPane extends JPanel implements ActionListener, KeyListener{
 	private static Timer timer;
 	private static int blockSize = 20; //determines the size of the panel
 	private static int width;
 	private static int height;
	private static int speed; //length of one "turn", in milliseconds
 	private static boolean lockInDelay; //if true block wont lock in
 	private static Tetromino currentTetromino;
 	
 	//Integer represents coordinates, the 100 digits represent x coordinates 0-9
 	//10 and 1 digits represent y coordinates 0-19
 	//e.g. 812 represents the (x, y)-coordinate (8, 12)
 	//x = Integer / 100, y = Integer % 100
 	//grid contains only locked in blocks, not the active tetromino
 	private static Map<Integer, Boolean> grid = new HashMap<Integer, Boolean>();
 	
 	
 	public TetrisPane(){
 		super();
 		width = blockSize * 10;
 		height = blockSize * 20;
 		setPreferredSize(new Dimension(width, height));
 		setFocusable(true);
 		addKeyListener(this);
 		speed = 1000; 
 		timer = new Timer(speed, this);
 		timer.setInitialDelay(speed);
 		timer.start();
 		lockInDelay = true;
 		
 		currentTetromino = nextTetromino();
 		
 		//the boolean values in the map represent
 		//whether or not the space on the grid is occupied
 		for(int i = 0; i < 10; i++){
 			for(int j = 0; j < 20; j++){
 				Integer coords = (i * 100) + j;
 				Boolean occupied = false;
 				grid.put(coords, occupied);
 			}
 		}
 	}
 	
 	private void drawBlock(List<Integer> coords, Graphics g){
 		for(Integer coord : coords){
 			int x = coord / 100 * blockSize;
 			int y = coord % 100 * blockSize;
 			g.setColor(Color.BLUE);
 			g.fillRect(x, y, blockSize, blockSize);
 			g.setColor(Color.BLACK);
 			g.drawRect(x, y, blockSize, blockSize);
 		}
 	}
 	
 	public void paintComponent(Graphics g){
 		g.setColor(Color.WHITE);
 		g.fillRect(0, 0, width, height);
 		
 		List<Integer> occupiedSpaces = new ArrayList<Integer>();
 		for(Integer coord : grid.keySet()){
 			if(grid.get(coord) == true){
 				occupiedSpaces.add(coord);
 			}
 		}
 		drawBlock(occupiedSpaces, g);
 		drawBlock(currentTetromino.getBlockPositions(), g);
 	}
 
 	
 	private void clearFullRows(){
 		//check each of 20 rows
 		for(int y = 0; y < 20; y++){
 			boolean rowFull = true;
 			for(int x = 0; x < 10; x++){
 				Integer coord = x * 100 + y;
 				if(grid.get(coord) == false){
 					rowFull = false;
 					break; //breaks inner loop only
 				}
 			}
 			if(rowFull){
 				for(int x = 0; x < 10; x++){
 					Integer coord = x * 100 + y;
 					grid.put(coord, false);
 				}
 				for(int i = y - 1; i > 0; i--){
 					for(int x = 0; x < 10; x++){
 						Integer coord = x * 100 + i;
 						if(grid.get(coord) == true){
 							grid.put(coord, false);
 							grid.put(coord + 1, true);
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	private boolean gameOver(){
 		if(grid.get(300) == true || grid.get(400) == true || grid.get(500) == true || grid.get(600) == true){
 			return true;
 		}else{
 			return false;
 		}
 	}
 	
 	private Tetromino nextTetromino(){
 		//to do: make them spawn more 'evenly' over the short term
 		Random rand = new Random();
 		int next = rand.nextInt(7);
 		switch(next){
 			case 0: return new OShapedTetromino();
 			case 1: return new IShapedTetromino();
 			case 2: return new SShapedTetromino();
 			case 3: return new ZShapedTetromino();
 			case 4: return new TShapedTetromino();
 			case 5: return new LShapedTetromino();
 			case 6: return new JShapedTetromino();
 		}
 		return null;
 	}
 	
	//this method is called once every 'turn'
 	//locking blocks into place occurs in this method
 	@Override
 	public void actionPerformed(ActionEvent event) {
 		if(gameOver()){
 			//do some game over stuff
 		}
 		if(currentTetromino.checkLegalMove(currentTetromino.getNextPositions(), grid)){			
 			currentTetromino.moveDown(grid);
 		}else if(lockInDelay){
 			//delays lock in by one 'turn'
 			lockInDelay = false;
 		}else{
 			//this is where the current piece gets locked into place
 			for(Integer coord : currentTetromino.getBlockPositions()){
 				grid.put(coord, true);
 			}
 			currentTetromino = nextTetromino();
 			lockInDelay = true;
 		}
 		clearFullRows();
 		repaint();
 	}
 	
 	@Override
 	public void keyPressed(KeyEvent event) {
 		int keyCode = event.getKeyCode();
 		if(keyCode == KeyEvent.VK_UP){
 			currentTetromino.rotate(grid);
 			lockInDelay = true;
 			repaint();
 		}else if(keyCode == KeyEvent.VK_DOWN){
 			if(currentTetromino.checkLegalMove(currentTetromino.getNextPositions(), grid)){			
 				currentTetromino.moveDown(grid);
 				lockInDelay = false;
 				repaint();
 			}			
 		}else if(keyCode == KeyEvent.VK_LEFT){
 			currentTetromino.moveLeft(grid);
 			lockInDelay = true;
 			repaint();
 		}else if(keyCode == KeyEvent.VK_RIGHT){
 			currentTetromino.moveRight(grid);
 			lockInDelay = true;
 			repaint();
 		}
 	}
 
 	@Override
 	public void keyReleased(KeyEvent arg0) {
 		//unused
 	}
 
 	@Override
 	public void keyTyped(KeyEvent arg0) {
 		//Unused		
 	}
 }
