 package edu.mharper.tp2;
 
 import java.awt.BasicStroke;
 import java.awt.BorderLayout;
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.plaf.FileChooserUI;
 
 public class View implements ActionListener {
 	
 	// View main components
 	public static JFrame frame;
 	public static GameView gameView;
 	public static InfoView infoView;
 	JMenuBar menuBar;
 	
 	// Menu bar items
 	JMenu fileMenu;
 	JMenu controlsMenu;
 	JMenu helpMenu;
 	
 	// File menu items
 	ArrayList<JMenuItem> fileMenuItems;
 	JMenuItem newGameMenuItem;
 	JMenuItem saveMenuItem;
 	JMenuItem loadMenuItem;
 	JMenuItem exitMenuItem;
 	
 	// Game menu items
 	ArrayList<JMenuItem> gameMenuItems;
 	JMenuItem endTurnItem;
 	JMenuItem settingsMenuItem;
 	
 	// Help Menu Items;
 	ArrayList<JMenuItem> helpMenuItems;
 	JMenuItem helpMenuItem;
 
 	
 	public View() {
 		gameView = new GameView();
 		infoView = new InfoView();
 		initWindow();
 	}
 	
 	private void initWindow() {
 		frame = new JFrame(Main.gameTitle);
 		frame.setLayout(new BorderLayout());
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setResizable(true);
 		
 		initMenuBar();
 		initGameDisplay();
 		initInfoDisplay();
 		frame.pack();
 		frame.setVisible(true);
 	}
 	
 	private void initMenuBar() {
 		// Init menu bar
 		menuBar = new JMenuBar();
 		
 		// Init menus
 		fileMenu = new JMenu("File");
 		controlsMenu = new JMenu("Game");
 		helpMenu = new JMenu("Help");
 		
 		// Init file menu
 		fileMenuItems = new ArrayList<JMenuItem>();
 		fileMenuItems.add(new JMenuItem("New game"));
 		fileMenuItems.add(new JMenuItem("Save game"));
 		fileMenuItems.add(new JMenuItem("Load game"));
 		fileMenuItems.add(new JMenuItem("Exit"));
 		for (JMenuItem item : fileMenuItems) {
 			item.addActionListener(this);
 			fileMenu.add(item);
 		}
 		
 		// Init game menu
 		gameMenuItems = new ArrayList<JMenuItem>();
 		gameMenuItems.add(new JMenuItem("End turn"));
 		//gameMenuItems.add(new JMenuItem("Settings"));
 		for (JMenuItem item : gameMenuItems) {
 			item.addActionListener(this);
 			controlsMenu.add(item);
 		}
 		
 		// Init help menu
 		helpMenuItems = new ArrayList<JMenuItem>();
 		helpMenuItems.add(new JMenuItem("Show rules"));
 		for (JMenuItem item : helpMenuItems) {
 			item.addActionListener(this);
 			helpMenu.add(item);
 		}
 		
 		menuBar.add(fileMenu);
 		menuBar.add(controlsMenu);
 		menuBar.add(helpMenu);
 		
 		frame.setJMenuBar(menuBar);
 	}
 	
 	private void initGameDisplay() {
 		Container pane = frame.getContentPane();
 		pane.add(gameView, BorderLayout.PAGE_START);	
 	}
 	
 	private void initInfoDisplay() {
 		Container pane = frame.getContentPane();
 		pane.add(infoView, BorderLayout.PAGE_END);
 	}
 	
 	private void showIntroScreen() {
 
 	}
 	
 	private void showRules() {
 		String windowTitle = "Fanorona Rules";
 		String rules = "Moving: White moves first.  Pieces are moved by sliding one " 
 				+ "space along one of the the lines.\n\tNote that some points lie on diagonal lines, "
 				+ "while others have only horizontal and vertical directions.\n\n"
 				+ "Capturing:  you can capture a line of your opponent's pieces by approach by " 
 				+ "moving toward them into the adjacent space,\n or by withdrawal by starting in the "
 				+ "adjacent space and moving directly away from your opponent's piece.\n "
 				+ "In some positions, you could capture either way, and you must choose one or the other.\n\n"
 				+ "A Turn:  consists of either a single, non-capturing move, or a sequence of capturing moves.\n "
 				+ "If any capturing moves are possible anywhere on the board, then a capturing move must be made.\n"
 				+ "If multiple captures are possible, you can choose which to do.  Subsequent captures on the same turn are optional.\n"
 				+ "Second and subsequent captures in the same turn are subject to some restrictions:\n"
 				+ "\t       you must keep moving the same piece\n"
 				+ "\t       you cannot return to any space twice\n"
 				+ "\t       you can't move in the same direction twice in a row\n";
 		
 		JOptionPane.showMessageDialog(frame, rules, windowTitle, JOptionPane.PLAIN_MESSAGE);
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent event) {
 		if (event.getActionCommand().equals("New game")) {
 			// Tell the game manager to reset the board
 			gameView.newGame();
 			//repaint();
 		}
 		else if (event.getActionCommand().equals("Exit")) {
 			System.exit(0);
 		}
 		else if (event.getActionCommand().equals("Save game")) {
 			gameView.saveGame();
 			JOptionPane.showMessageDialog(frame, "Game saved");
 		}
 		else if (event.getActionCommand().equals("Load game")) {
 			JFileChooser fileChooser = new JFileChooser();
 			int retVal = fileChooser.showOpenDialog(frame);
 			if (retVal == JFileChooser.APPROVE_OPTION) {
 				File file = fileChooser.getSelectedFile();
 				String fileName = file.getAbsolutePath();
 				gameView.loadGame(fileName);
 			}
			gameView.repaint();
 		}
 		else if (event.getActionCommand().equals("End turn")) {
 			
 		}
 		else if (event.getActionCommand().equals("Settings")) {
 			SettingsView set = new SettingsView();
 		}
 		else if (event.getActionCommand().equals("Show rules")) {
 			// Pop up a window with the rules
 			showRules();
 		}
 	}
 	
 	public static void gameOver() {
 		frame.setVisible(false);
 		infoView.timer.stop();
 		GameOverScreen gs = new GameOverScreen();
 	}
 }
