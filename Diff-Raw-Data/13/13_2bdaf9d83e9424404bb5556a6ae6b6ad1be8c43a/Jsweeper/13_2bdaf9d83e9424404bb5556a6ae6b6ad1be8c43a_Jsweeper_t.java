 package org.kroqgar78.jsweeper;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Random;
 import javax.swing.*;
 
 public class Jsweeper
 {
 	
 	private JFrame frame;
 	private int[] size;
 	private Cell[][] cells;
 	private Random rand;
 	private int numMines;
 	
 	public Jsweeper( int width, int height, int numMines )
 	{
 		frame = new JFrame("Jsweeper");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		size = new int[] {height,width}; // dimensions of the field; rows by columns
 		
 		rand = new Random(System.currentTimeMillis());
 		
 		this.numMines = numMines;
 		
 		generateField();
 		
 		JMenuBar menubar = new JMenuBar();
 		JMenu menu = new JMenu("JSweeper");
 		JMenuItem startItem = new JMenuItem("Start over");
 		startItem.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0)
 			{
 				restartGame();
 			}
 		});
 		
 		menu.add(startItem);
 		menubar.add(menu);
 		
 		frame.setJMenuBar(menubar);
 				
 		frame.pack();
 		frame.setVisible(true);
 	}
 	
 	private void recountCells()
 	{
 		// calculate values of the cells by iterating through mines and incrementing values around them
 		for( int i = 0; i < cells.length; i++ )
 		{
 			for( int j = 0; j < cells[i].length; j++ )
 			{
 				if(!cells[i][j].isMine()) continue;
 
 				Cell[] adjCells = getAdjacentCells(i, j);
 				for( int k = 0; k < adjCells.length; k++ )
 				{
 					cells[adjCells[k].getPosition()[0]][adjCells[k].getPosition()[1]].incrementValue();
 				}
 			}
 		}
 	}
 	
 	public Cell[][] getCells() { return this.cells; }
 	
 	/**
 	 * Get the cells adjacent to cell (x,y)
 	 * 
 	 * @param x The distance from the top
 	 * @param y The distance from the left
 	 * @return All cells adjacent to (x,y)
 	 */
 	public Cell[] getAdjacentCells(int x, int y)
 	{
 		ArrayList<Cell> cellTmp = new ArrayList<Cell>();
 		
 		if( x<(size[0]-1) ) cellTmp.add(cells[x+1][y]); //  below
 		if( y<(size[0]-1) ) cellTmp.add(cells[x][y+1]); // right
 		if( x<(size[0]-1) && y<(size[0]-1) ) cellTmp.add(cells[x+1][y+1]); // below & right
 		if( x>0 ) cellTmp.add(cells[x-1][y]); // above
 		if( x>0 && y<(size[0]-1) ) cellTmp.add(cells[x-1][y+1]); // above & right 
 		if( y>0 ) cellTmp.add(cells[x][y-1]); // left
 		if( x>0 && y>0 ) cellTmp.add(cells[x-1][y-1]); // above & left
 		if( x<(size[0]-1) && y>0 ) cellTmp.add(cells[x+1][y-1]); // down & left
 		
 		return cellTmp.toArray(new Cell[cellTmp.size()]);
 	}
 	
 	/**
 	 * Get the number of flags adjacent to cell (x,y)
 	 * 
 	 * @param x The distance from the top
 	 * @param y The distance from the left
 	 * @return The number of adjacent flags
 	 */
 	public int getAdjacentFlagCount(int x, int y)
 	{
 		int count = 0;
 		Cell[] adjCells = getAdjacentCells(x,y);
 		for( int i = 0; i < adjCells.length; i++ )
 		{
 			if(adjCells[i].flagged) count++;
 		}
 		return count;
 	}
 	
 	/**
 	 * Get all clicked cells in the field
 	 * 
 	 * @return All clicked cells in an array
 	 */
 	public Cell[] getClickedCells()
 	{
 		ArrayList<Cell> cellTmp = new ArrayList<Cell>();
 		
 		for( int i = 0; i < cells.length; i++ )
 		{
 			for( int j = 0; j < cells[i].length; j++ )
 			{
 				if(cells[i][j].clicked) cellTmp.add(cells[i][j]);
 			}
 		}
 		
 		return cellTmp.toArray(new Cell[cellTmp.size()]);
 	}
 	
 	/**
 	 * Return only the clicked cells in an ArrayList.
 	 * This is the opposite of getUnclickedCells(orig).
 	 * 
 	 * @param orig The original ArrayList of cells
 	 * @return all clicked cells in ArrayList orig
 	 */
 	public static ArrayList<Cell> getClickedCells(ArrayList<Cell> orig)
 	{
 		ArrayList<Cell> cellTmp = new ArrayList<Cell>(orig);
		for( Cell tmp : orig )
 		{
			if(tmp.clicked) cellTmp.add(tmp);
 		}
 		return cellTmp;
 	}
 	
 	/**
 	 * Return only the clicked cells in an array.
 	 * This is the opposite of getUnclickedCells(orig).
 	 * 
 	 * @param orig The original array of cells
 	 * @return all clicked cells in orig[]
 	 */
 	public static Cell[] getClickedCells(Cell[] orig)
 	{
 		ArrayList<Cell> cellTmp = getClickedCells(new ArrayList<Cell>(java.util.Arrays.asList(orig)));
 		return cellTmp.toArray(new Cell[cellTmp.size()]);
 	}
 	
 	/**
 	 * Return only the unclicked cells in an ArrayList.
 	 * This is the opposite of getClickedCells(orig).
 	 * 
 	 * @param orig The original ArrayList of cells
 	 * @return all unclicked cells in ArrayList orig[]
 	 */
 	public static ArrayList<Cell> getUnclickedCells(ArrayList<Cell> orig)
 	{
		ArrayList<Cell> cellTmp = new ArrayList<Cell>();
		for( Cell tmp : orig )
 		{
			if(!tmp.clicked) cellTmp.add(tmp);
 		}
 		return cellTmp;
 	}
 	
 	/**
 	 * Return only the unclicked cells in an array.
 	 * This is the opposite of getClickedCells(orig).
 	 * 
 	 * @param orig The original array of cells
 	 * @return all unclicked cells in orig[]
 	 */
 	public static Cell[] getUnclickedCells(Cell[] orig)
 	{
 		ArrayList<Cell> cellTmp = getUnclickedCells(new ArrayList<Cell>(java.util.Arrays.asList(orig)));
 		return cellTmp.toArray(new Cell[cellTmp.size()]);
 	}
 	
 	public Cell getRandomCell()
 	{
 		return getCells()[rand.nextInt(getSize()[0])][rand.nextInt(getSize()[1])];
 	}
 	
 	public boolean isAnyCellClicked()
 	{
 		for( int i = 0; i < getSize()[0]; i++ )
 		{
 			for( int j = 0; j < getSize()[1]; j++ )
 			{
 				if( getCells()[i][j].clicked ) return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	public JFrame getFrame() { return this.frame; }
 	
 	public int[] getSize() { return this.size; }
 	
 	public int getNumMines() { return this.numMines; }
 	
 	public void generateField()
 	{
 		Container contentPane = frame.getContentPane();
 		contentPane.removeAll(); // wipe the content pane just in case it already had stuff
 		contentPane.setLayout(new GridLayout(size[0], size[1]));
 		cells = new Cell[size[0]][size[1]]; // position is (dist from top, dist from left)
 		for( int i = 0; i < cells.length; i++ )
 		{
 			for( int j = 0; j < cells[i].length; j++ )
 			{
 				//if(cells[i][j] != null) contentPane.remove(cells[i][j]); // remove old buttons if regenerating
 				cells[i][j] = new Cell(i, j, this);
 				cells[i][j].setPreferredSize(new Dimension(42, 32));
 				contentPane.add(cells[i][j]);
 			}
 		}
 		
 		// place mines
 		for( int i = 0; i < numMines; i++ )
 		{
 			// keep generating random positions until it finds one that isn't a mine
 			int x, y;
 			do
 			{
 				x = rand.nextInt(cells.length);
 				y = rand.nextInt(cells[0].length);
 			}
 			while(cells[x][y].isMine());
 			
 			cells[x][y].setMine(true);
 			System.out.println("Placed mine at (" + x + "," + y + ")");
 		}
 		
 		recountCells();
 	}
 	
 	public void restartGame()
 	{
 		generateField();
 		frame.pack();
 	}
 	
 	public void gameOver()
 	{
 		//JOptionPane.showMessageDialog(frame, "", "GAME OVER", JOptionPane.INFORMATION_MESSAGE);
 		int userOption = JOptionPane.showConfirmDialog(frame, "Game Over!!!\nWould you like to play again?\n... you suck btw");
 		if(userOption == JOptionPane.YES_OPTION)
 		{
 			restartGame();
 		}
 		else if(userOption == JOptionPane.NO_OPTION)
 		{
 			this.frame.setVisible(false);
 			System.exit(0);
 		}
 	}
 	
 	public void gameWin()
 	{
 		int userOption = JOptionPane.showConfirmDialog(frame, "Good job. You won.\n... you still suck btw");
 		if(userOption == JOptionPane.YES_OPTION)
 		{
 			restartGame();
 		}
 		else if(userOption == JOptionPane.NO_OPTION)
 		{
 			this.frame.setVisible(false);
 			System.exit(0);
 		}
 	}
 	
 	public void update()
 	{
 		// victory conditions: all non-mine cells are clicked
 		// iterate through all non-mine cells to see if the conditions are true
 		boolean allClicked = true;
 		for( int i = 0; i < cells.length; i++ )
 		{
 			for( int j = 0; j < cells[i].length; j++ )
 			{
 				
 				if( !cells[i][j].clicked && !cells[i][j].isMine() )
 				{
 					allClicked = false;
 					break;
 				}
 			}
 		}
 		if(allClicked)
 		{
 			gameWin();
 		}
 	}
 	
 	public static void main(String[] args)
 	{
 		Jsweeper inst = new Jsweeper(16, 16, 40);
 	}
 }
