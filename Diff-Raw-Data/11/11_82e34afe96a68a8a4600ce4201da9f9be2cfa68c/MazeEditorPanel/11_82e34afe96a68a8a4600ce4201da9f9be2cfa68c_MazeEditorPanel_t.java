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
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JToolBar;
 
 import maze_objects.Dragon;
 import maze_objects.Hero;
 import maze_objects.Maze;
 import maze_objects.Movable;
 import maze_objects.Sword;
 import maze_objects.Tile;
 
 public class MazeEditorPanel extends JDialog {
 
 	private static final long serialVersionUID = -981182364507201188L;
 
 	public MazeObjectToDraw currentObject = new MazeObjectToDraw();
 	public Game game;
 	public GameOptions options = new GameOptions(false);
 	public MazePictures pictures;
 
 	//This boolean indicates if the user clicked on a new dragon or is repeating the same
 	public boolean newDragon; 
 
 	public int numberOfExits = 0;
 	public boolean createdHero;
 	public boolean createdSword;
 
 	private int maze_rows;
 	private int maze_columns;
 	private int dragonType;
 
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
 
 		Dimension mazeEditorDimension = new Dimension(options.columns * GUInterface.SPRITESIZE, options.rows * GUInterface.SPRITESIZE);
 
 		JScrollPane scrollPane = new JScrollPane();
 		scrollPane.setViewportView(mazePainter);
 		Dimension scrollPaneDimension = new Dimension(mazeEditorDimension.width + GUInterface.SCROLLBAR_PIXELS, mazeEditorDimension.height + GUInterface.SCROLLBAR_PIXELS);
 		scrollPane.setPreferredSize(GUInterface.getFormattedPreferredDimension(scrollPaneDimension, GUInterface.MAXIMUM_WINDOW_SIZE));
 
 		getContentPane().add(scrollPane);
 		
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
 			rows = JOptionPane.showInputDialog(this, "Number of rows? (Min. 6, Max. 500 but clipping will occur!)");
 		}
 		while(!MazeInput.isInteger(rows) && rows != null);
 
 		if(rows == null)
 			return 1;
 
 		do {
 			columns = JOptionPane.showInputDialog(this, "Number of columns? (Min. 6, Max. 500 but clipping will occur!)");
 		}
 		while(!MazeInput.isInteger(columns) && columns != null);
 
 		if(columns == null)
 			return 1;
 
 		if(Integer.parseInt(rows)  < 6 || Integer.parseInt(columns) < 6
 				|| Integer.parseInt(columns) > 500 || Integer.parseInt(columns) > 500) {
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
 
 		updateOptions();
 		return 0;
 	}
 
 	private void createToolBar() {
 		JToolBar toolBar = new JToolBar();
 		toolBar.setFloatable(false);
 		getContentPane().add(toolBar);
 
 		JButton btnFloor = new JButton("");
 		btnFloor.setIcon(new ImageIcon(InfoPanel.class.getResource("/images/empty.png")));
 		toolBar.add(btnFloor);
 
 		btnFloor.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				currentObject.set(Tile.empty);
 			}
 		});
 
 		JButton btnWall = new JButton("");
 		btnWall.setIcon(new ImageIcon(InfoPanel.class.getResource("/images/wall.png")));
 		toolBar.add(btnWall);
 
 		btnWall.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				currentObject.set(Tile.wall);
 			}
 		});
 
 		JButton btnExit = new JButton("");
 		btnExit.setIcon(new ImageIcon(InfoPanel.class.getResource("/images/exit.png")));
 		toolBar.add(btnExit);
 
 		btnExit.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				currentObject.set(Tile.exit);
 			}
 		});
 
 		JButton btnDragon = new JButton("");
 		btnDragon.setIcon(new ImageIcon(InfoPanel.class.getResource("/images/dragon.png")));
 		toolBar.add(btnDragon);
 
 		btnDragon.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				currentObject.set(new Dragon(0, 0, options.dragonType));
 				newDragon = true;
 			}
 		});
 
 		JButton btnSword = new JButton("");
 		btnSword.setIcon(new ImageIcon(InfoPanel.class.getResource("/images/sword.png")));
 		toolBar.add(btnSword);
 
 		btnSword.addActionListener(new SetSword());
 
 		JButton btnHero = new JButton("");
 		btnHero.setIcon(new ImageIcon(InfoPanel.class.getResource("/images/hero.png")));
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
 
		JMenuItem saveGameMenuItem = new JMenuItem("Save maze", KeyEvent.VK_S);
 		saveGameMenuItem.getAccessibleContext().setAccessibleDescription(
 				"Saves the current maze to a file");
 		fileMenu.add(saveGameMenuItem);
 
 		saveGameMenuItem.addActionListener(new SaveMaze());
 		
		JMenuItem exitGameMenuItem = new JMenuItem("Exit editor",
 				KeyEvent.VK_E);
 		exitGameMenuItem.getAccessibleContext().setAccessibleDescription(
				"Exits the editor");
 		fileMenu.add(exitGameMenuItem);
 
 		exitGameMenuItem.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				int option = JOptionPane.showConfirmDialog(
 						MazeEditorPanel.this,
						"Do you really want to exit the editor?\nDon't forget to save!",
 						"Confirm exit",
 						JOptionPane.YES_NO_OPTION);
 				if(option == JOptionPane.YES_OPTION)
 					dispose();
 			}
 		});
 		
 		JMenu helpMenu = new JMenu("Help");
 		helpMenu.setMnemonic(KeyEvent.VK_H);
 		helpMenu.getAccessibleContext().setAccessibleDescription(
 				"Help menu");
 		menuBar.add(helpMenu);
 
 		JMenuItem keysHelpMenuItem = new JMenuItem("Help", KeyEvent.VK_H);
 		keysHelpMenuItem.getAccessibleContext().setAccessibleDescription(
 				"Explains how to use the editor");
 		helpMenu.add(keysHelpMenuItem);
 
 		keysHelpMenuItem.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				JOptionPane.showMessageDialog(MazeEditorPanel.this, "\nPlace the objects on the toolbar to create a custom game.\n" +
 						"If you want to place multiple dragons, click the dragon icon again after placing one, and place the new one.\n" +
 						"You cannot place anything on the corners, and only exits or walls on the map borders.\n" +
 						"You can't create a game that doesn't have one hero and one sword and at least an exit.\n" +
 						"You can, however, create an enemyless game, but, come on, where's the fun in that?\n\n",
 								"Help",
 								JOptionPane.PLAIN_MESSAGE);
 			}
 		});
 
 		setJMenuBar(menuBar);
 	}
 
 	private void updateOptions() {
 
 		options.randomMaze = true;
 		options.rows = maze_rows;
 		options.columns = maze_columns;
 
 		options.dragonType = dragonType;
 
 		options.multipleDragons = true;
 
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
 
 			if(numberOfExits == 0) {
 				JOptionPane.showMessageDialog(MazeEditorPanel.this,
 						"You need at least one exit on your maze!",
 						"No exits found",
 						JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 
 			if(!createdHero) {
 				JOptionPane.showMessageDialog(MazeEditorPanel.this,
 						"You need to place your hero!",
 						"No hero found",
 						JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 
 			if(!createdSword) {
 				JOptionPane.showMessageDialog(MazeEditorPanel.this,
 						"You need to place a sword!",
 						"No sword found",
 						JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 
 			GameOutput.showSaveGameDialog(game);
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
 
 		Dimension defaultPreferred = new Dimension(parent.options.columns * GUInterface.SPRITESIZE,
 				parent.options.rows * GUInterface.SPRITESIZE);
 
 		setMaximumSize(GUInterface.MAXIMUM_WINDOW_SIZE);
 
 		setPreferredSize(defaultPreferred);
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
 
 		if( checkIfAtCorner(printRow, printColumn)
 				|| ( checkIfAtMargin(printRow, printColumn) && checkIfNotWallOrExitTile() ) )
 			return;
 
 		if(parent.game.getMaze().getPositions()[printRow][printColumn] == Tile.exit)
 			parent.numberOfExits--;
 
 		deleteObjectOn(printRow, printColumn);
 
 		if(parent.currentObject.isMovable) {
 			movableObjects.remove(parent.currentObject.movable);
 
 			if( (parent.currentObject.movable instanceof Dragon) && !parent.newDragon )
 				parent.game.removeDragon((Dragon) parent.currentObject.movable);
 
 			parent.currentObject.movable.setRow(printRow);
 			parent.currentObject.movable.setColumn(printColumn);
 			parent.currentObject.movable.print = true;
 			movableObjects.add(parent.currentObject.movable);
 
 			if(parent.currentObject.movable instanceof Dragon) {
 				parent.game.addDragon((Dragon) parent.currentObject.movable);
 				parent.newDragon = false;
 			}
 		}
 		else {
 			parent.game.getMaze().getPositions()[printRow][printColumn] = parent.currentObject.tile;
 			if(parent.currentObject.tile == Tile.exit)
 				parent.numberOfExits++;
 		}
 
 		checkIfCreatedHeroAndSword();
 
 		repaint();
 	}
 
 	private boolean checkIfNotWallOrExitTile() {
 		return parent.currentObject.isMovable
 				|| ( parent.currentObject.tile != Tile.exit
 				&& parent.currentObject.tile != Tile.wall );
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
 
 							if(dragon.getRow() == row && dragon.getColumn() == column) {
 								boolean dragonWasDead;
 
 								if(dragon.getState() == Dragon.DEAD)
 									dragonWasDead = true;
 								else
 									dragonWasDead = false;
 
 								dragonIter.remove();
 
 								parent.game.removeDragon(dragonWasDead);
 							}
 						}
 					}
 
 					movable.setRow(0);
 					movable.setColumn(0);
 					movable.print = false;
 					iter.remove();
 				}
 			}
 
 	}
 
 	private void checkIfCreatedHeroAndSword() {
 		parent.createdHero = false;
 		parent.createdSword = false;
 
 		for(Movable movable : movableObjects) {
 			if(movable instanceof Hero)
 				parent.createdHero = true;
 			else if(movable instanceof Sword)
 				parent.createdSword = true;
 		}
 	}
 
 	private boolean checkIfAtMargin(int printRow, int printColumn) {
 		return printRow <= 0 || printRow >= (parent.options.rows - 1) || printColumn <= 0 || printColumn >= (parent.options.columns - 1);
 	}
 
 	private boolean checkIfAtCorner(int printRow, int printColumn) {
 		if(printRow == 0)
 			if(printColumn == 0 || printColumn == (parent.options.columns - 1))
 				return true;
 
 		if(printRow == (parent.options.rows - 1))
 			if(printColumn == 0 || printColumn == (parent.options.columns - 1))
 				return true;
 
 		return false;
 	}
 
 }
