 package org.kroqgar78.jsweeper;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.ArrayList;
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
