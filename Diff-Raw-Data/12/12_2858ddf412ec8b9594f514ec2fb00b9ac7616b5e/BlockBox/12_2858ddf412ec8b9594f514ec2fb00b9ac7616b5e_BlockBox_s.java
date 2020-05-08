 package tetrix.core;
 
 /**
  * 
  * @author Magnus Huttu
  *
  */
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.newdawn.slick.SlickException;
 
 import tetrix.core.tetrominos.I;
 import tetrix.core.tetrominos.J;
 import tetrix.core.tetrominos.L;
 import tetrix.core.tetrominos.O;
 import tetrix.core.tetrominos.S;
 import tetrix.core.tetrominos.Square;
 import tetrix.core.tetrominos.Tetromino;
 import tetrix.core.tetrominos.Z;
 import tetrix.util.Util;
 
 public class BlockBox {
 
 	private int nbrOfColumns;
 	private int nbrOfRows;
 	private List<Tetromino> minoes;
 	private boolean isInUse;
 	private boolean rowFilled;
 	private TetrominoFactory tF;
 	private int level;
 	private Player player;
 	private boolean gameOver;
 
 	public BlockBox(Player player) throws SlickException {
 		this(10, 20, player);
 	}
 
 	public BlockBox(int nbrOfColumns, int nbrOfRows, Player player) throws SlickException{
 		this.nbrOfColumns = nbrOfColumns;
 		this.nbrOfRows = nbrOfRows;
 		isInUse = false;
 		minoes = new ArrayList<Tetromino>();
 		rowFilled = false;
 		tF = new TetrominoFactory();
 		level = 0;
 		this.player = player;
 		gameOver = false;
 		clearBoard();
 	}
 
 
 	public void clearBoard() {
 		minoes.clear();
 	}
 
 	public void clearRow(final int y){		
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				try{
 					for(Tetromino t : minoes){
 						for(Square s : t.getSquares()){
 							if(s.getY() == y+Util.SQUARE_SIZE){
 								s.destroy();
 							}
 							if(!s.destroyed() && !s.isMoving())
 								s.rowFall();
 						}
 					}
 					player.increaseScore();
 					Thread.sleep(150);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}).start();
 	}
 
 	private void search4FullRows(){		
 
 		for(int y = Util.B4_BOX_HEIGHT-Util.SQUARE_SIZE; y < Util.WINDOW_HEIGHT-Util.B4_BOX_HEIGHT; y+=Util.SQUARE_SIZE){
 			int amountFilled = 0;
 			for(int x = Util.B4_BOX_WIDTH; x < Util.WINDOW_WIDTH-Util.B4_BOX_WIDTH; x+=Util.SQUARE_SIZE){
 				if(isPainted(x, y)){
 					amountFilled++;
 				}
 			}
 			if(amountFilled == 10){
 				clearRow(y);
 			}
 		}
 	}
 
 
 	public void update() throws SlickException{
 		search4FullRows();
 		for(int i = minoes.size(); i > 0; i--){
 			if(minoes.get(i-1).isMoving()){
 				minoes.get(i-1).update();
 			}
 		}
 	}
 
 	public boolean isPainted(Tetromino t){ 
 		Square[] s = t.getSquares();
 		for(int i = 3; i > -1; i--){
 			if(s[i].getY() >= Util.B4_BOX_HEIGHT+Util.BOX_HEIGHT-Util.SQUARE_SIZE && !s[i].destroyed()){
 				return true;
 			}
 			if(i == 0){
 				if(isPainted(s[0].getX(), s[0].getY()+Util.SQUARE_SIZE)){
 					if(!s[i].destroyed())
 						return true;
 				}
				
 			}
			
 			if(isPainted(s[i].getX(), s[i].getY())){
 				if(!s[i].destroyed())
 					return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean isPainted(int x, int y) {
 		int size = minoes.size();
 		for(int i = 0; i < size; i++){
 			Square[] s = minoes.get(i).getSquares();
 			for(int h = 3; h > -1; h--){
 				if(!s[h].destroyed()){
 					if(s[h].getX() == x){
 						if(s[h].getY() == y + Util.SQUARE_SIZE){
 							if(!s[h].isMoving())
 								return true;
 						}
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	public List<Tetromino> getTetroList() {
 		List<Tetromino> temp = new ArrayList<Tetromino>();
 		temp.addAll(minoes);
 		return temp;
 	}
 
 	public void newBlock() throws SlickException{	
 		isInUse = true;
 		tF.createRandomTetromino(this);
 	}
 
 	public void newBlock(int i) throws SlickException{
 		isInUse = true;
 		tF.createTetromino(this, i);
 	}
 
 	/**
 	 * Create a new block with more data.
 	 *
 	 * @param i represents which type of block that is supposed to be created
 	 * @param sqrDestroyed represens which square that has been destroyed
 	 * @param pos represents where the block should start
 	 * @throws SlickException
 	 */
 	public void newBrokenBlock(int newSqr, Tetromino t, Position pos, int x) throws SlickException{
 		isInUse = true;
 
 		tF.createBrokenTetromino(this, t, newSqr, pos, x);
 	}
 
 	public void addMino(Tetromino t){
 		minoes.add(t);
 	}
 
 	public Position[][] getPos(){
 		Position[][] pos = new Position[minoes.size()][4];
 
 		int h = 0;
 		int size = minoes.size();
 		for(int i = 0; i < size; i++){
 			pos[h] = minoes.get(i).getPos();
 			h++;
 		}
 
 		return pos.clone();
 	}
 
 	public boolean isInUse(){
 		return isInUse;
 	}
 
 	public int getColumns() {
 		return nbrOfColumns;
 	}
 
 	public int getRows() {
 		return nbrOfRows;
 	}
 
 	public boolean isRowFilled() {
 		return rowFilled;
 	}
 
 	public void setLevel(int i){
 		level = i;
 	}
 
 	public int level(){
 		return level;
 	}
 
 	public void gameIsOver(){
 		gameOver = true;
 		System.out.println("gameOver");
 	}
 
 	public boolean gameOver(){
		return gameOver;
 	}
 
 }
