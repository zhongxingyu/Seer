 package NotSoEasy;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.KeyStroke;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import State.StateManager;
 
 
 public class NotSoEasyFrame extends JFrame implements Observer {
 
 	private static final long	serialVersionUID	= 1L;
 
 	private GameTable			gameTable;
 	private StateManager		stateManager;
 	
 	private JMenuItem			newGameMenuItem;
 	private JMenuItem			resetGameMenuItem;
 	private JMenuItem			undoMoveMenuItem;
 	private JMenuItem			redoMoveMenuItem;
 	private JMenuItem			exitGameMenuItem;
 	
 	private JMenuItem			aboutMenuItem;
 
 	public NotSoEasyFrame() {
 		super("Bobonne's Challenge");
 
 		gameTable = new GameTable();
 		setContentPane(gameTable);
 		setMinimumSize(gameTable.getMinimumSize());
 		
 		stateManager = gameTable.getStateManager();
 		stateManager.addObserver(this);
		
 		initMenuBar();
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	}
 
 	private void initMenuBar() {
 		newGameMenuItem = new JMenuItem("New Game");
 		newGameMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
 		newGameMenuItem.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				stateManager.init();
 			}
 		});
 
 		resetGameMenuItem = new JMenuItem("Reset Game");
 		resetGameMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
 		resetGameMenuItem.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				stateManager.reset();
 			}
 		});
 
 		undoMoveMenuItem = new JMenuItem("Undo Move");
 		undoMoveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
 		undoMoveMenuItem.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				stateManager.undo();
 			}
 		});
 
 		redoMoveMenuItem = new JMenuItem("Redo Move");
 		redoMoveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
 		redoMoveMenuItem.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				stateManager.redo();
 			}
 		});
 
 		exitGameMenuItem = new JMenuItem("Exit");
 		exitGameMenuItem.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				exit();
 			}
 		});
 		
 		aboutMenuItem = new JMenuItem("About");
 		aboutMenuItem.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				about();
 			}
 		});
 
 		// Add the 'Game' MenuItems
 		JMenu gameMenu = new JMenu("Game");
 		gameMenu.add(newGameMenuItem);
 		gameMenu.add(resetGameMenuItem);
 		gameMenu.addSeparator();
 		gameMenu.add(undoMoveMenuItem);
 		gameMenu.add(redoMoveMenuItem);
 		gameMenu.addSeparator();
 		gameMenu.add(exitGameMenuItem);
 
 		// Add the 'Help' MenuItems
 		JMenu helpMenu = new JMenu("Help");
 		helpMenu.add(aboutMenuItem);
 		
 		// Add the menus to the menubar
 		JMenuBar menuBar = new JMenuBar();
 		menuBar.add(gameMenu);
 		menuBar.add(helpMenu);
 		
 		// Add the menubar to the window
 		this.setJMenuBar(menuBar);
 
 		updateMenu();
 	}
 
 	public void updateMenu() {
 		undoMoveMenuItem.setEnabled(stateManager.canUndo());
 		redoMoveMenuItem.setEnabled(stateManager.canRedo());
 	}
 
 	private void about() {
 		JDialog aboutDialog = new AboutDialog(this);
 		aboutDialog.setVisible(true);
 	}
 	
 	private void exit() {
 		setVisible(false);
 		System.exit(0);
 	}
 
 	public static void main(String[] args) {
 		try {
 			// Set System L&F
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		}
 		catch (UnsupportedLookAndFeelException e) {
 			// TODO: handle exception
 		}
 		catch (ClassNotFoundException e) {
 			// TODO: handle exception
 		}
 		catch (InstantiationException e) {
 			// TODO: handle exception
 		}
 		catch (IllegalAccessException e) {
 			// TODO: handle exception
 		}
 		NotSoEasyFrame game = new NotSoEasyFrame();
 		game.setVisible(true);
 	}
 
 	@Override
 	public void update(Observable stateManager, Object arg) {
 		updateMenu();
 	}
 
 }
