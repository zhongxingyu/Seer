 package game;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.net.URL;
 
 import javax.imageio.ImageIO;
 
 public class Cell {
 
 	public enum CellState {FLAGGED,REVEALED,UNTOUCHED};
	private static BufferedImage flagged,revealed,revealedMine,untouched;
 
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
 		this.row=row;
 		this.column=column;
 		this.mine=false;
 		this.cellState=CellState.UNTOUCHED;
 		if(flagged==null){
 			loadCellImages();
 		}
 	}
 
 	// Draws the cell based on the cellState and the row/column of the cell
 	public void draw(Graphics g) {
 		// Declare the variables
 	
 		BufferedImage img = null;
 
 		// Grab the proper image url based on the state of the cell
 		switch(cellState) {
 		case FLAGGED:
 			img = flagged;
 			break;
 		case UNTOUCHED:
 			img = untouched;
 			break;
 		case REVEALED:
			img = (isMine())?revealedMine:revealed;
 			break;
 		default:
 			System.out.println(cellState.toString());
 			System.err.println("Cell in unknown state!");  
 			break;      	
 		}
 
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
 
 		// Declare the variables
 		String cellSize = String.valueOf(Board.cellSize);
 		String fileStr = "none";
 
 		// Grab the proper image url based on the state of the cell
 		fileStr = "/image/flagged"+cellSize+"x"+cellSize+".jpg";
 		URL imageSrc = this.getClass().getResource(fileStr);
 		try {
 			flagged = ImageIO.read(imageSrc);
 		} catch (IOException e) {
 			System.err.print("Could not open file "+imageSrc.toString());
 		}
 
 		fileStr = "/image/untouched"+cellSize+"x"+cellSize+".jpg";
 		imageSrc = this.getClass().getResource(fileStr);
 		try {
 			untouched = ImageIO.read(imageSrc);
 		} catch (IOException e) {
 			System.err.print("Could not open file "+imageSrc.toString());
 		}
 
 		fileStr = "/image/revealed"+((this.mine)?"Mine":"")+cellSize+"x"+cellSize+".jpg";
 		imageSrc = this.getClass().getResource(fileStr);
 		try {
 			revealed = ImageIO.read(imageSrc);
 		} catch (IOException e) {
 			System.err.print("Could not open file "+imageSrc.toString());
 		}

		fileStr = "/image/revealedMine"+((this.mine)?"Mine":"")+cellSize+"x"+cellSize+".jpg";
		imageSrc = this.getClass().getResource(fileStr);
		try {
			revealedMine = ImageIO.read(imageSrc);
		} catch (IOException e) {
			System.err.print("Could not open file "+imageSrc.toString());
		}
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
 		case REVEALED:
 			break;
 		}
 	}
 
 	// This will reveal a cell if it is presently untouched
 	public void revealCell() {
 		if (this.cellState == CellState.UNTOUCHED) {
 			this.cellState = CellState.REVEALED;
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
