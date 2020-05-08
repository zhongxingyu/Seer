 package game.gui;
 
 import game.logic.Game;
 import game.ui.GameOptions;
 import game.ui.GameOutput;
 import game.ui.MazePictures;
 import general_utilities.MazeInput;
 
 import java.awt.Dimension;
 import java.awt.Frame;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.JDialog;
 import javax.swing.JToolBar;
 import java.awt.BorderLayout;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Iterator;
 
import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.ImageIcon;
 import javax.swing.JFileChooser;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import maze_objects.Dragon;
 import maze_objects.Maze;
 import maze_objects.Movable;
 import maze_objects.Sword;
 import maze_objects.Tile;
 
 public class MazeEditorPanel extends JDialog {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -981182364507201188L;
 	public MazeObjectToDraw currentObject = new MazeObjectToDraw();
 	public Game game;
 	public GameOptions options = new GameOptions(false);
 	public MazePictures pictures;
 	
 	//This boolean indicates if the user clicked on a new dragon or is repeating the same
 	public boolean newDragon; 
 
 	private int maze_rows;
 	private int maze_columns;
 	private int dragonType;
 	private boolean useMultipleDragons;
 
 	public MazeEditorPanel(Frame parent, final Game game, MazePictures pictures) {
 		super(parent, "Maze Editor", true);
		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
 		this.game = game;
 		this.pictures = pictures;
 
 		createMenuBar();
 
 		createToolBar();
 
 		if(askNewGameOptions() == 1)
 			return;
 		
 		initializeNewGame();
 
 		MazePainterPanel mazePainter = new MazePainterPanel(this);
		getContentPane().add(mazePainter);
 
 		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 		setResizable(false);
		pack();
		setLocationRelativeTo(null);
 		setVisible(true);
 	}
 
 	private void initializeNewGame() {
 		this.game = new Game(options);
 		this.game.setMaze(new Maze(options.rows, options.columns, true));
 		this.game.getHero().print = false;
 		this.game.getSword().print = false;
 	}
 
 	private int askNewGameOptions() {
 		String rows;
 		String columns;
 
 		do {
 			rows = JOptionPane.showInputDialog(this, "Number of rows? (Min. 6!)");
 		}
 		while(!MazeInput.isInteger(rows) && rows != null);
 
 		if(rows == null)
 			return 1;
 
 		do {
 			columns = JOptionPane.showInputDialog(this, "Number of columns? (Min. 6!)");
 		}
 		while(!MazeInput.isInteger(columns) && columns != null);
 
 		if(columns == null)
 			return 1;
 
 		if(Integer.parseInt(rows)  < 6 || Integer.parseInt(columns) < 6) {
 			JOptionPane.showMessageDialog(this,
 					"Invalid row and/or column number detected, using 10 for both!",
 					"Invalid input error",
 					JOptionPane.ERROR_MESSAGE);
 
 			maze_rows = 10;
 			maze_columns = 10;
 		}
 		else {
 			maze_rows = Integer.parseInt(rows);
 			maze_columns = Integer.parseInt(columns);
 		}
 
 		String[] possibilities = {"Randomly sleeping", "Always awake", "Static"};
 		String dragonOption = (String)JOptionPane.showInputDialog(
 				this,
 				"Dragon type:",
 				"Dragon type",
 				JOptionPane.QUESTION_MESSAGE,
 				null,
 				possibilities,
 				possibilities[0]);
 
 		if(dragonOption == null)
 			return 1;
 		if(dragonOption.equals( "Randomly sleeping" ))
 			dragonType = Dragon.SLEEPING;
 		else if(dragonOption.equals( "Always awake" ))
 			dragonType = Dragon.NORMAL;
 		else
 			dragonType = Dragon.STATIC;
 
 		int multipleDragonsOption = JOptionPane.showConfirmDialog(
 				this,
 				"Create a number of dragons proportional to the maze size?",
 				"Multiple dragons",
 				JOptionPane.YES_NO_OPTION);
 
 		if(multipleDragonsOption == JOptionPane.YES_OPTION)
 			useMultipleDragons = true;
 		else
 			useMultipleDragons = false;
 
 		updateOptions();
 		return 0;
 	}
 
 	private void createToolBar() {
 		JToolBar toolBar = new JToolBar();
 		toolBar.setFloatable(false);
		getContentPane().add(toolBar);
 
 		JButton btnFloor = new JButton("Floor");
 		toolBar.add(btnFloor);
 
 		btnFloor.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				currentObject.set(Tile.empty);
 			}
 		});
 
 		JButton btnWall = new JButton("Wall");
 		toolBar.add(btnWall);
 
 		btnWall.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				currentObject.set(Tile.wall);
 			}
 		});
 
 		JButton btnExit = new JButton("Exit");
 		toolBar.add(btnExit);
 
 		btnExit.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				currentObject.set(Tile.exit);
 			}
 		});
 
 		JButton btnDragon = new JButton("Dragon");
 		toolBar.add(btnDragon);
 
 		btnDragon.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				currentObject.set(new Dragon(0, 0, options.dragonType));
 				newDragon = true;
 			}
 		});
 
 		JButton btnSword = new JButton("Sword");
 		toolBar.add(btnSword);
 
 		btnSword.addActionListener(new SetSword());
 
 		JButton btnHero = new JButton("Hero");
 		toolBar.add(btnHero);
 
 		btnHero.addActionListener(new SetHero());
 	}
 
 	private void createMenuBar() {
 		JMenuBar menuBar = new JMenuBar();
 
 
 		JMenu fileMenu = new JMenu("File");
 		fileMenu.setMnemonic(KeyEvent.VK_F);
 		fileMenu.getAccessibleContext().setAccessibleDescription(
 				"File menu");
 		menuBar.add(fileMenu);
 
 		JMenuItem saveGameMenuItem = new JMenuItem("Save Maze", KeyEvent.VK_S);
 		saveGameMenuItem.getAccessibleContext().setAccessibleDescription(
 				"Saves the current maze to a file");
 		fileMenu.add(saveGameMenuItem);
 
 		saveGameMenuItem.addActionListener(new SaveMaze());
 
 		setJMenuBar(menuBar);
 	}
 
 	private void updateOptions() {
 
 		options.randomMaze = true;
 		options.rows = maze_rows;
 		options.columns = maze_columns;
 
 		options.dragonType = dragonType;
 
 		options.multipleDragons = useMultipleDragons;
 
 		options.randomSpawns = false;
 	}
 
 	class SetHero implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			currentObject.set(game.getHero());
 		}
 
 	}
 
 	class SetSword implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			currentObject.set(game.getSword());
 		}
 
 	}
 
 	class SaveMaze implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			JFileChooser fileChooser = new JFileChooser();
 			if (fileChooser.showSaveDialog(fileChooser) == JFileChooser.APPROVE_OPTION) {
 				File file = fileChooser.getSelectedFile();
 				// save to file
 				GameOutput.save(game, file);
 			}
 		}
 		
 	}
 }
 
 class MazePainterPanel extends JPanel implements MouseListener {
 
 	private static final long serialVersionUID = -5533602405577612408L;
 
 	MazeEditorPanel parent;
 
 	private ArrayList<Movable> movableObjects = new ArrayList<Movable>();
 
 	public MazePainterPanel(MazeEditorPanel parent) {
 
 		this.parent = parent;
 
 		setFocusable(true);
 		addMouseListener(this);
 
 		setPreferredSize(new Dimension(parent.options.columns * GUInterface.SPRITESIZE,
 				parent.options.rows * GUInterface.SPRITESIZE));
 	}
 
 	@Override
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		GameOutput.printGame(parent.game, g, parent.pictures);
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		return;
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		return;
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		return;
 
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		int printRow = e.getY() / 32;
 		int printColumn = e.getX() / 32;
 
 		System.out.println("mouse pressed!");
 		if(parent.currentObject.isMovable) {
 			movableObjects.remove(parent.currentObject.movable);
 			
 			if( (parent.currentObject.movable instanceof Dragon) && !parent.newDragon )
 				parent.game.removeDragon((Dragon) parent.currentObject.movable);
 			
 			deleteObjectOn(printRow, printColumn);
 			parent.currentObject.movable.setRow(printRow);
 			parent.currentObject.movable.setColumn(printColumn);
 			parent.currentObject.movable.print = true;
 			movableObjects.add(parent.currentObject.movable);
 			
 			if(parent.currentObject.movable instanceof Dragon) {
 				parent.game.addDragon((Dragon) parent.currentObject.movable);
 				parent.newDragon = false;
 			}
 		}
 		else
 			parent.game.getMaze().getPositions()[printRow][printColumn] = parent.currentObject.tile;
 
 		repaint();
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		return;
 	}
 
 	private void deleteObjectOn(int row, int column) {
 		
 		parent.game.getMaze().getPositions()[row][column] = Tile.empty;
 
 		Iterator<Movable> iter = movableObjects.iterator(); 
 
 		if(movableObjects != null)
 			while(iter.hasNext()) {
 				Movable movable = iter.next();
 
 				if(movable.getRow() == row && movable.getColumn() == column) {
 
 					if(movable instanceof Dragon) {
 						Iterator<Dragon> dragonIter = parent.game.getDragons().iterator(); 
 
 						while(dragonIter.hasNext()) {
 							Dragon dragon = dragonIter.next();
 
 							if(dragon.getRow() == row && dragon.getColumn() == column)
 								dragonIter.remove();
 						}
 					}
 
 					movable.setRow(0);
 					movable.setColumn(0);
 					movable.print = false;
 					iter.remove();
 				}
 			}
 
 	}
 
 }
