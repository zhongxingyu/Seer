 package game;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.imageio.ImageIO;
 
 public class Cell {
 
 	public enum CellState {FLAGGED,REVEALED,REVEALEDMINE,REVEALEDFLAGGEDMINE,UNTOUCHED}; //images to enums are now 1:1
	public Map<CellState,BufferedImage> images;
 
 	// Flag determining if the cell is a mine or not
 	private boolean mine;
 
 	// Integer to store the number of mines adjacent to this cell
 	private int minesAdjacent;
 
 	// Stores the location of the cell on the board, can be used to draw the cells
 	private int row;
 	private int column;
 
 	// Stores the state of the cell
 	private CellState cellState;
 
 	// Default constructor
 	public Cell() {
 		this(0, 0);
 	}
 
 	// Sets the row and column, sets the mine flag to false, and sets cellState to UNTOUCHED
 	public Cell(int row, int column) {
 		if(images==null){
 			images = new HashMap<CellState,BufferedImage>();
 			loadCellImages();
 		}
 		this.row=row;
 		this.column=column;
 		this.mine=false;
 		this.cellState=CellState.UNTOUCHED;
 	}
 
 	// Draws the cell based on the cellState and the row/column of the cell
 	public void draw(Graphics g) {
 		// Declare the variables
 	
 		BufferedImage img = images.get(this.cellState);
 
 		// Set the proper x/y values
 		int x = this.column * Board.cellSize;
 		int y = this.row * Board.cellSize;
 
 		// Draw the image, and the adjacency numbers if needed
 		g.drawImage(img,x,y,null);
 		if(cellState == CellState.REVEALED && minesAdjacent > 0 && !this.mine) {
 			g.setColor(Color.WHITE);
 			g.drawString(String.valueOf(minesAdjacent), x + Board.cellSize/2-3, y + Board.cellSize/2+3);
 		}
 	}
 	public void loadCellImages(){
 		// Grab the proper image url based on the state of the cell
 		for(CellState c : CellState.values()){
 			images.put(c, loadImageOf(c));
 		}
 	}
 	public BufferedImage loadImageOf(CellState c){
 		BufferedImage img = null;
 		String cellSize = String.valueOf(Board.cellSize);
 		String fileStr = "none";
 		fileStr = "/image/"+c.toString().toLowerCase()+cellSize+"x"+cellSize+".jpg";
 		URL imageSrc = this.getClass().getResource(fileStr);
 		try {
 			img = ImageIO.read(imageSrc);
 		} catch (IOException e) {
 			System.err.print("Could not open file "+imageSrc.toString());
 		}
 		return img;
 		
 	}
 	// This will be called whenever a cell is right-clicked, to toggle the state of the cell
 	public void toggleFlag() {
 		switch(this.cellState) {
 		case UNTOUCHED:
 			this.cellState = CellState.FLAGGED;
 			break;
 		case FLAGGED:
 			this.cellState = CellState.UNTOUCHED;
 			break;
 		default:
 			break;
 		}
 	}
 
 	// This will reveal a cell if it is presently untouched
 	public void revealCell() {
 		if(this.cellState==CellState.FLAGGED && isMine()){
 			this.cellState=CellState.REVEALEDFLAGGEDMINE;
 		}
 		if (this.cellState == CellState.UNTOUCHED) {
 			this.cellState = (isMine())?CellState.REVEALEDMINE:CellState.REVEALED;
 		}
 	}
 
 	// Setters for the private variables
 	public void setMinesAdjacent(int minesAdjacent) {
 		this.minesAdjacent = minesAdjacent;
 	}	
 	public void setMine(boolean mine) {
 		this.mine = mine;
 	}
 
 
 	// Getters for the private variables
 	public int getMinesAdjacent() {
 		return minesAdjacent;
 	}
 	public int getRow() {
 		return row;
 	}
 	public int getColumn() {
 		return column;
 	}
 	public CellState getCellState() {
 		return cellState;
 	}
 	public boolean isMine() {
 		return mine;
 	}
}
