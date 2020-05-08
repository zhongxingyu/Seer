 package com.noobathon.minesweeper.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 public class MinesweeperMainFrame extends JFrame 
 {
 	private static final long serialVersionUID = -5099377961864547691L;
 	private final static int DEFAULT_GRID_ROWS = 15;
 	private final static int DEFAULT_GRID_COLUMNS = 15;
 	
 	public MinesweeperMainFrame()
 	{
 		super("Minesweeper");
 		setLayout(new BorderLayout());
		setupBorders();
 		setupGrid();
 		setupFrame();
 	}
 	
	private void setupBorders()
	{
		this.add(new JLabel("This is MINESWEEPER!", JLabel.CENTER), BorderLayout.NORTH);
		this.add(new JLabel("<HTML>H<br>E<br>L<br>L<br>O</HTML>"), BorderLayout.WEST);
		this.add(new JLabel("<HTML>H<br>E<br>L<br>L<br>O</HTML>"), BorderLayout.EAST);
		this.add(new JLabel("south", JLabel.CENTER), BorderLayout.SOUTH);
	}
	
 	private void setupGrid()
 	{
 		this.add(new MinesweeperGridFrame(DEFAULT_GRID_ROWS, DEFAULT_GRID_COLUMNS), BorderLayout.CENTER);
 	}
 	
 	private void setupFrame()
 	{
 		this.setVisible(true);
 		this.pack();
 		this.setMinimumSize(new Dimension(400, 400));
 		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
 	}
 }
