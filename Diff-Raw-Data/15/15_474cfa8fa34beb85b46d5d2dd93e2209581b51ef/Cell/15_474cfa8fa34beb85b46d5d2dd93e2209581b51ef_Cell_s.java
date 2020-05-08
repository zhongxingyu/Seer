 package game;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLDecoder;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 public class Cell {
 
 	public enum CellState {FLAGGED,REVEALED,UNTOUCHED};
 
 	// Flag determining if the cell is a mine or not
 	private boolean mine;
 
 	// Integer to store the number of mines adjacent to this cell
 	private int minesAdjacent;
 
 	// Stores the location of the cell on the board, can be used to draw the cells
 	private int row;
 	private int column;
 	private Game game;
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

 	}
 
 	// Draws the cell based on the cellState and the row/column of the cell
 	public void draw(Graphics g) {
 		String cellSize = "30";
 		String fileStr = "none";
 		BufferedImage img = null;
 		switch(cellState){
 		case FLAGGED:
 			fileStr = "/image/flagged"+cellSize+"x"+cellSize+".jpg";
 			break;
 		case UNTOUCHED:
 			fileStr = "/image/untouched"+cellSize+"x"+cellSize+".jpg";
 			break;
 		case REVEALED:
			fileStr = "/image/revealed"+cellSize+"x"+cellSize+".jpg";
 			break;
 		default:
 			System.out.println(cellState.toString());
 			System.err.println("Cell in unknown state!");  
 			break;      	
 		}
 		URL imageSrc = this.getClass().getResource(fileStr);
 		try {
 			img = ImageIO.read(imageSrc);
 		} catch (IOException e) {
 			System.err.print("Could not open file "+imageSrc.toString());
 		}
 		int x = this.getColumn()*img.getWidth();
 		int y = this.getRow()*img.getHeight();
 		g.drawImage(img,x,y,null);
		if(cellState == CellState.REVEALED && getMinesAdjacent() > 0) {
 			g.setColor(Color.WHITE);
 			g.drawString(String.valueOf(getMinesAdjacent()), x + img.getWidth()/2, y + img.getHeight()/2);
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
 	public void setRow(int row) {
 		this.row = row;
 	}
 	public void setColumn(int column) {
 		this.column = column;
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
