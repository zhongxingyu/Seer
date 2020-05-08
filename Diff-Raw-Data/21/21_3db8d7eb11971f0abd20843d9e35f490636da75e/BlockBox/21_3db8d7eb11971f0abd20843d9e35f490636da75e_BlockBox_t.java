 package tetrix.core;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.newdawn.slick.SlickException;
 
 import tetrix.util.Util;
 
 public class BlockBox {
 
 	private int nbrOfColumns;
 	private int nbrOfRows;
 	private List<Tetromino> minoes;
 	private boolean isInUse;
 
 	public BlockBox() throws SlickException {
 		this(10,20);
 	}
 
 	public BlockBox(int nbrOfColumns, int nbrOfRows) throws SlickException{
 		this.nbrOfColumns = nbrOfColumns;
 		this.nbrOfRows = nbrOfRows;
 		isInUse = false;
 		minoes = new ArrayList<Tetromino>();
 		clearBoard();
 	}
 
 
 	public void clearBoard() {
 		minoes.clear();
 	}
 
 	public void update() throws SlickException{
 		for(Tetromino t : minoes){
 			for(Position p : t.getPos()){
 				if(isPainted(p.getX(), p.getY())){
 					t.stop();
 				}
 			}
 			if(t.isMoving())
 				t.update();
 		}
 	}
 
 	public void newBlock(int i) throws SlickException{
 		isInUse = true;
 
 		switch(i){
 
 		case 1:
 			minoes.add(new L((int)(Math.random()*8)));
 			break;
 
 		case 2:
 			minoes.add(new J((int)(Math.random()*8)));
 			break;
 
 		case 3:
 			minoes.add(new O((int)(Math.random()*9)));
 			break;
 
 		case 4:
 			minoes.add(new I((int)(Math.random()*7)));
 			break;
 
 		case 5:
 			minoes.add(new Z((int)(Math.random()*8)));
 			break;
 
 		case 6:
 			minoes.add(new S((int)(Math.random()*8)));
 			break;
 
 		}
 	}
 
 	public boolean isPainted(float x, float y) {
		if(y > 480){
 			return true;
 		}
 		for(Tetromino t : minoes){
 			Position[] p = t.getPos();
 			for(int i = 0; i < 4; i++)
 				if(p[0].getY() == y+Util.SQUARE_SIZE && p[i].getX() == x){
 					return true;
 				}
 		}
 		return false;
 	}
 
 	public Position[][] getPos(){
 		Position[][] pos = new Position[minoes.size()][4];
 
 		int h = 0;
 		for(Tetromino t : minoes){
 			pos[h] = t.getPos();
 			h++;
 		}

 		return pos.clone();
 	}
 
 	public void move(){
 		for(int i = 0; i < minoes.size(); i++){
 			minoes.get(i).update();
 		}
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
 }
