 import java.awt.BorderLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.KeyStroke;
 
 
 public class MahjongBoard extends JFrame {
 	private static final long serialVersionUID = -8006300036685327355L;
 
 	private static final int WIDTH = 1100;
 	private static final int HEIGHT = 800;
 	private GamePanel gamePanel;
 	
 	// File has New game, then a separator before these items
 	private final int UNDO_INDEX = 3;
 	private final int REDO_INDEX = UNDO_INDEX + 1;
 	private final int HINT_INDEX = REDO_INDEX + 1;
 	// Save menu item is after a separator
 	private final int SAVE_INDEX = HINT_INDEX + 2;
 	
 	public MahjongBoard() {
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		setSize(WIDTH, HEIGHT);
 		
		gamePanel = new GamePanel(WIDTH - 10, HEIGHT - 10, true);
 
 		JMenuBar menubar = new JMenuBar();
 		JMenu menu = new JMenu("File");
 		JMenuItem item = new JMenuItem("New Game");
 		item.setMnemonic(KeyEvent.VK_N);
 		item.setAccelerator(KeyStroke.getKeyStroke(
 				KeyEvent.VK_N, KeyEvent.CTRL_MASK));
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int selection = JOptionPane.showConfirmDialog(
 						(JMenuItem) e.getSource(), 
 						"Abandon current game and start a new one?",
 						"New Game",
 						JOptionPane.YES_NO_OPTION
 						); 
 				if (selection == 0) {
 					newGame();
 				}
 			}
 		});
 		menu.add(item);
 		
 		item = new JMenuItem("New Numbered Game");
 		item.setMnemonic(KeyEvent.VK_G);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				String number = JOptionPane.showInputDialog(
 						"Enter the game number");
 				if (Helper.isInteger(number) && number.length() == 5) {
 					System.out.println(number);
 				}
 				else if (number != null) {
 					JOptionPane.showMessageDialog((JMenuItem) e.getSource(), 
 							"Invalid input, must be a five-digit number",
 							"Bad Game Number",
 							JOptionPane.ERROR_MESSAGE);
 				}
 			}
 		});
 		menu.add(item);
 		
 		menu.addSeparator();
 		
 		item = new JMenuItem("Undo");
 		item.setMnemonic(KeyEvent.VK_U);
 		item.setAccelerator(KeyStroke.getKeyStroke(
 				KeyEvent.VK_Z, KeyEvent.CTRL_MASK));
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				gamePanel.undo();
 			}
 		});
 		item.setEnabled(gamePanel.canUndo());
 		menu.add(item, UNDO_INDEX);
 		
 		item = new JMenuItem("Redo");
 		item.setMnemonic(KeyEvent.VK_R);
 		item.setAccelerator(KeyStroke.getKeyStroke(
 				KeyEvent.VK_Y, KeyEvent.CTRL_MASK));
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				gamePanel.redo();
 			}
 		});
 		item.setEnabled(gamePanel.canRedo());
 		menu.add(item, REDO_INDEX);
 
 		item = new JMenuItem("Hint");
 		item.setMnemonic(KeyEvent.VK_H);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				gamePanel.hint(true);
 			}
 		});
 		menu.add(item, HINT_INDEX);
 		
 		menu.addSeparator();
 		
 		item = new JMenuItem("Save Game");
 		item.setMnemonic(KeyEvent.VK_S);
 		item.setAccelerator(KeyStroke.getKeyStroke(
 				KeyEvent.VK_S, KeyEvent.CTRL_MASK));
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				
 			}
 		});
 		item.setEnabled(false);
 		menu.add(item, SAVE_INDEX);
 		
 		item = new JMenuItem("Exit Game");
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				close();
 			}
 		});
 		menu.add(item);
 		
 		menu.setMnemonic(KeyEvent.VK_F);
 		
 		menubar.add(menu);
 		
 		menu = new JMenu("Options");
 		menu.setMnemonic(KeyEvent.VK_O);
 		
 		item = new JMenuItem("Rounded Corners");
 		item.setMnemonic(KeyEvent.VK_R);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				gamePanel.toggleRoundedCorners();
 			}
 		});
 		menu.add(item);
 		
 		menu.addSeparator();
 		
 		item = new JMenuItem("High Scores");
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				
 			}
 		});
 		menu.add(item);
 		
 		item = new JMenuItem("Removed Tiles");
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
				setSize(WIDTH + 200, HEIGHT);
 				JPanel removedPanel = new JPanel();
 				gamePanel.populateRemoved(removedPanel);
 				
 				add(new JScrollPane(removedPanel), BorderLayout.EAST);
 			}
 		});
 		menu.add(item);
 		
 		menubar.add(menu);
 		setJMenuBar(menubar);
 		
 		add(gamePanel);
 		
 		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
 		addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				int selection =
 						JOptionPane.showConfirmDialog(e.getComponent(),
 						"Do you want to quit the game?",
 						"Exit Game",
 						JOptionPane.YES_NO_OPTION);
 				if (selection == 0) {
 					System.exit(0);
 				}
 			}
 		});
 		
 		setTitle("Marsh-jong");
 		setResizable(false);
 		setVisible(true);
 	}
 	
 	/**
 	 * Calls up a dialog to check what action the user wants to perform after
 	 * the last possible tile set is removed
 	 */
 	public void checkEndGame() {
 		String[] options = {"New Game", "Cancel", "Exit Game"};
 		int selection = JOptionPane.showOptionDialog(this, "No more moves exist.\n" +
 				"Choose Cancel to go back to this game, otherwise " +
 				"choose New Game or Exit Game.", "End Game", 
 				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
 				null, options, options[0]);
 		
 		switch (selection) {
 		case 0:
 			newGame();
 			break;
 		case 2:
 			close();
 			break;
 		case 1:
 			// Intentional fall-through
 		default:
 			break;
 		}
 	}
 	/**
 	 * Called to enable the undo/redo menus, save menu, etc.
 	 */
 	public void checkEnabledMenus() {
 		JMenu menu = getJMenuBar().getMenu(0);
 		menu.getMenuComponent(UNDO_INDEX).setEnabled(gamePanel.canUndo());
 		menu.getMenuComponent(REDO_INDEX)
 				.setEnabled(gamePanel.canRedo());
 	}
 
 	private void close() {
 		WindowEvent event = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
 		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(event);
 	}
 	private void newGame() {
 		remove(gamePanel);
 		
		gamePanel = new GamePanel(WIDTH - 10, HEIGHT - 10, true);
 		add(gamePanel);
 
 		checkEnabledMenus();
 
 		repaint();
 	}
 	
 	public static void main(String[] args) {
 		new MahjongBoard();
 	}
 
 }
