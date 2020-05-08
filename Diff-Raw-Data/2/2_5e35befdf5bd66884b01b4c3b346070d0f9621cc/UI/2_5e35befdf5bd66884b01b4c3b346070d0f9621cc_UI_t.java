 package com.diycomputerscience.minesweeper.view;
 
 import java.awt.Color;
 import java.awt.EventQueue;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.MissingResourceException;
 import java.util.Properties;
 import java.util.ResourceBundle;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import javax.swing.plaf.ColorUIResource;
 
 import com.diycomputerscience.minesweeper.Board;
 import com.diycomputerscience.minesweeper.FilePersistenceStrategy;
 import com.diycomputerscience.minesweeper.PersistenceException;
 import com.diycomputerscience.minesweeper.PersistenceStrategy;
 import com.diycomputerscience.minesweeper.RandomMineInitializationStrategy;
 import com.diycomputerscience.minesweeper.Square;
 import com.diycomputerscience.minesweeper.UncoveredMineException;
 
 public class UI extends JFrame {
 	
 	private Board board;
 	private OptionPane optionPane;
 	private PersistenceStrategy peristenceStrategy;
 	private JPanel panel;
 	
 	
 	public UI(Board board, OptionPane optionPane, PersistenceStrategy persistenceStrategy) {		
 		
 		// set this.board to the injected Board
 		this.board = board;
 		this.optionPane = optionPane;
 		this.peristenceStrategy = persistenceStrategy;
 		
 		// Set the title to "Minesweeper"
 		this.setTitle("title");
 				
 		this.panel = new JPanel();
 		
 		// Set the name of the panel to "MainPanel" 
 		panel.setName("MainPanel");
 		
 		// Set the layout of panel to GridLayout. Be sure to give it correct dimensions
 		panel.setLayout(new GridLayout(Board.MAX_ROWS, Board.MAX_COLS));
 		
 		// add squares to the panel
 		this.layoutSquares(panel);
 		// add panel to the content pane
 		this.getContentPane().add(this.panel);
 		
 		// set the menu bar
 		this.setJMenuBar(buildMenuBar());
 		
 		// validate components
 		//this.validate();
 	}
 	
 	public void load(Board board) {
 		this.board = board;
 		
 		this.getContentPane().removeAll();
 		this.invalidate();
 		
 		this.panel = new JPanel();		
 		// Set the name of the panel to "MainPanel" 
 		panel.setName("MainPanel");		
 		// Set the layout of panel to GridLayout. Be sure to give it correct dimensions
 		panel.setLayout(new GridLayout(Board.MAX_ROWS, Board.MAX_COLS));
 		
 		// add squares to the panel
 		this.layoutSquares(panel);
 		// add panel to the content pane
 		
 		this.getContentPane().add(this.panel);
 		this.validate();		
 	}
 	
 	private void layoutSquares(JPanel panel) {		
 		final Square squares[][] = this.board.getSquares();
 					
 		for(int row=0; row<Board.MAX_ROWS; row++) {
 			for(int col=0; col<Board.MAX_COLS; col++) {
 				final JButton squareUI = new JButton();
 				squareUI.setName(row+","+col);
 				final int theRow = row;
 				final int theCol = col;
 				squareUI.addMouseListener(new MouseAdapter() {
 					@Override
 					public void mouseClicked(MouseEvent me) {
 						// invoke the appropriate logic to affect this action (left or right mouse click)
 						if(SwingUtilities.isLeftMouseButton(me)) {
 							try {
 								UI.this.board.getSquares()[theRow][theCol].uncover();												
 							} catch(UncoveredMineException ume) {
 								squareUI.setBackground(Color.RED);
 								String gameOverTitle = "Game Over Title";
 								String gameOverMsg = "Game Over Message";
 								int answer = optionPane.userConfirmation(UI.this, gameOverMsg, gameOverTitle, JOptionPane.YES_NO_OPTION);
 								if(answer == JOptionPane.YES_OPTION) {
 									Board board = new  Board(new RandomMineInitializationStrategy());
 									load(board);
 								} else {
 									UI.this.dispose();
 								}
 							}
 						} else if(SwingUtilities.isRightMouseButton(me)) {
 							UI.this.board.getSquares()[theRow][theCol].mark();
 						}
 						// display the new state of the square
 						updateSquareUIDisplay(squareUI, UI.this.board.getSquares()[theRow][theCol]);						
 					}
 				});
 				updateSquareUIDisplay(squareUI, UI.this.board.getSquares()[row][col]);
 				panel.add(squareUI);
 			}
 		}
 	}
 	
 	private void updateSquareUIDisplay(JButton squareUI, Square square) {
 		if(square.getState().equals(Square.SquareState.UNCOVERED)) {
 			if(square.isMine()) {
 				squareUI.setBackground(ColorUIResource.RED);
 			} else {
 				squareUI.setText(String.valueOf(square.getCount()));
 			}							
 		} else if(square.getState().equals(Square.SquareState.MARKED)) {
 			squareUI.setText("");
 			squareUI.setBackground(ColorUIResource.MAGENTA);
 		} else if(square.getState().equals(Square.SquareState.COVERED)) {
 			squareUI.setText("");
 			squareUI.setBackground(new ColorUIResource(238, 238, 238));
 		}
 	}
 	
 	private JMenuBar buildMenuBar() {
 		JMenuBar menuBar = new JMenuBar();
 		
 		JMenu file = new JMenu("Menu File");
 		file.setName("file");
 		JMenuItem fileSave = new JMenuItem("Menu Item Save");
 		fileSave.setName("file-save");
 		fileSave.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				try {
 					UI.this.peristenceStrategy.save(UI.this.board);
 				} catch(PersistenceException pe) {
 					System.out.println("Could not save the game" + pe);
 				}
 			}		
 		});
 		JMenuItem fileLoad = new JMenuItem("Menu Item Load");
 		fileLoad.setName("file-load");
 		fileLoad.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				try {
 					UI.this.load(UI.this.board = UI.this.peristenceStrategy.load());
 				} catch(PersistenceException pe) {
 					//TODO: error dialogue
 					//TODO: This button should be enabled only if a previously saved state exists
 					System.out.println("Could not load game from previously saved state");
 				}
 			}			
 		});
 		JMenuItem close = new JMenuItem("Menu Item Close");
 		close.setName("file-close");
 		close.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {			
 				System.exit(0);
 			}
 		});
 		file.add(fileSave);
 		file.add(fileLoad);
 		file.add(close);
 		menuBar.add(file);
 		
 		JMenu help = new JMenu("Menu Help");
 		help.setName("help");
 		JMenuItem helpAbout = new JMenuItem("Menu Item About");
 		help.add(helpAbout);
		helpAbout.setName("help-about");
 		menuBar.add(help);
 		
 		return menuBar;
 	}
 
 	
 	public static UI build(Board board, OptionPane optionPane, PersistenceStrategy persistenceStrategy) {
 		UI ui = new UI(board, optionPane, persistenceStrategy);
 		ui.setSize(300, 400);
 		ui.setVisible(true);
 		ui.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 		return ui;
 	}
 
 	public static void main(String[] args) {
 		InputStream configIS = null;
 		try {
 			configIS = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
 			final Properties configProperties = new Properties();
 			configProperties.load(configIS);
 			
 			EventQueue.invokeLater(new Runnable() {
 
 				@Override
 				public void run() {
 					build(new Board(new RandomMineInitializationStrategy()), 
 									new SwingOptionPane(), 
 									new FilePersistenceStrategy(configProperties.getProperty("persistence.filename")));
 				}
 						
 			});			
 		} catch(IOException ioe) {
 			System.out.println("Quitting: Could not load filename for persistence... " + ioe);
 		}
 	}
 
 }
