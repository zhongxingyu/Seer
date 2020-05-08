 package btlshp.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.io.File;
 
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JTextArea;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 
 import btlshp.Btlshp;
 import btlshp.entities.Map;
 import btlshp.enums.AppState;
 
 
 public class MainUI {
 	private HelpScreen   helpScreen;
 	private JFrame       mainFrame;
 	private JMenuBar     mainMenuBar;
 	private FileMenu     fileMenu;
 	private GameGrid     gameGrid;
 	private OutputArea   outputConsole;
 	private JLabel       status;
 	private Font         proFont;
 	
 	public MainUI() {
 		try {
 			proFont = Font.createFont(Font.TRUETYPE_FONT, new File("fonts/ProFontWindows.ttf"));
 		} 
 		catch (Exception e) {
 			proFont = null;
 		}
 	}
 	
	
	/**
	 * Used to update what items are available in the file menu when the game state changes.
	 */
	public void updateMainMenu() {
		fileMenu.updateMenuItems();
	}
	
	
 	private void makeMainMenu() {
 		mainMenuBar = new JMenuBar();
 		mainMenuBar.setOpaque(true);
 		mainMenuBar.setPreferredSize(new Dimension(200, 20));
 		
 		fileMenu = new FileMenu();		
 		mainMenuBar.add(fileMenu);
 		
 		mainFrame.setJMenuBar(mainMenuBar);
 		
 		fileMenu.updateMenuItems();
 	}
 	
 	
 	private void makeStatusPane() {
 		status = new JLabel();
 		status.setPreferredSize(new Dimension(158, 100));
 		status.setFont(proFont.deriveFont(Font.PLAIN, 12.0f));
 		status.setForeground(new Color(6, 178, 48));
 		status.setBackground(new Color(3, 28, 9));
 		status.setOpaque(true);
 	}
 	
 	
 	/**
 	 * Builds the GUI for the main program.
 	 */
 	public void makeGUI() {
 		// Look and feel...
 		try {
 			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
 		}
 		catch(Exception e) {
 		}
 		
 		
 		// Main JFrame
 		mainFrame = new JFrame("BtlShp");
 		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		Dimension dims = new Dimension();
 		dims.width = 640;
 		dims.height = 580;
 		
 		mainFrame.setSize(dims);
 		mainFrame.setResizable(false);
 		
 		if(!(mainFrame.getLayout() instanceof BorderLayout))
 			mainFrame.setLayout(new BorderLayout());
 		
 		// Make the menu
 		makeMainMenu();
 		
 		// Make the game grid
 		gameGrid = new GameGrid();
 		mainFrame.add(gameGrid, BorderLayout.CENTER);
 		
 		// Right status pane
 		makeStatusPane();
 		mainFrame.add(status, BorderLayout.LINE_END);
 		
 		// Bottom console pane.
 		outputConsole = new OutputArea();
 		outputConsole.setFont(status.getFont());
 		mainFrame.add(outputConsole, BorderLayout.PAGE_END);
		outputConsole.addLine("Welcome to BtlShp! Please start or restore a game!");
 		outputConsole.addLine("BTLSHP by TeamTBD");
 		
 		// Make help screen
 		helpScreen = new HelpScreen();
 		helpScreen.buildUi();
 		
 		// Show everything
 		mainFrame.pack();
 		mainFrame.setVisible(true);
 	}
 	
 	
 	/**
 	 * Utility function to display a Yes/No/Cancel dialog for the user.
 	 * 
 	 * @param title    Title of the dialog window
 	 * @param message  Message to be displayed inside the dialog
 	 * @return         Yes, No, or Cancel depending on the users decision.
 	 */
 	public DialogResult yesNoCancelDialog(String title, String message) {
 		int result = JOptionPane.showConfirmDialog(mainFrame, message, title, JOptionPane.YES_NO_CANCEL_OPTION);
 		
 		switch(result) {
 		case 0:
 			return DialogResult.Yes;
 		case 1:
 			return DialogResult.No;
 		case 2:
 			return DialogResult.Cancel;
 		}
 		
 		return DialogResult.Cancel;
 	}
 	
 	
 	/**
 	 * Utility function to show an alert to the player
 	 * 
 	 * @param title    Title of the alert
 	 * @param message  Message inside the alert
 	 */
 	public void showAlert(String title, String message) {
 		JOptionPane.showMessageDialog(mainFrame, message, title, JOptionPane.INFORMATION_MESSAGE);
 	}
 	
 	
 	/**
 	 * Utility function to display a directory selection dialog and allow the user to select a directory.
 	 * 
 	 * @return    File representing the path the user selected, or null if the user canceled.
 	 */
 	public File selectDiretory() {
 		JFileChooser fc = new JFileChooser();
 		
 		fc.setDialogTitle("Choose a directory...");
 		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 		fc.setAcceptAllFileFilterUsed(false);
 		
 		if(fc.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
 			return fc.getSelectedFile();
 		}
 		else 
 			return null;
 	}
 	
 	
 	/**
 	 * UI action to display the help screen.
 	 */
 	public void showHelp() {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				helpScreen.show();
 			}
 		});
 	}
 	
 	
 	/**
 	 * Handles the UI event when the map changes.
 	 * @param map     New game map.
 	 */
 	public void setMap(Map map) {
 		gameGrid.setMap(map);
 	}
 }
