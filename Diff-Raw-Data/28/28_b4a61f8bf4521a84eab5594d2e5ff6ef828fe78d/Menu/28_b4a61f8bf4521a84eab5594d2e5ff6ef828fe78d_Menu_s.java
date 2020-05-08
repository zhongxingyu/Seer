 package rps.client.ui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 
 import rps.client.UIController;
 
 public class Menu {
 
 	private final JFrame frame;
 	private final UIController controller;
 	private final JFrame aboutFrame = new JFrame("About");
 	private final JMenuBar menuBar = new JMenuBar();
 	private final JMenu menuGame = new JMenu("Game");
 	private final JMenuItem menuGameNew = new JMenuItem("New");
 	private final JMenuItem menuGameSurrender = new JMenuItem("Surrender");
 	private final JMenuItem menuGameExit = new JMenuItem("Exit");
 	
 	private final JMenu menuTheme = new JMenu("Theme");
 	private final JMenuItem themeDefault = new JMenuItem("Default");
	private final JMenuItem themeFancy = new JMenuItem("Minimal");
 	
 	private final JMenu menuInfo = new JMenu("Info");
 	private final JMenuItem infoAbout = new JMenuItem("About");
 	
 	public Menu(JFrame frame, UIController controller) {
 
 		this.frame = frame;
 		this.controller = controller;
 
 		buildMenuStructure();
 		bindMenuActions();
 	}
 
 	private void buildMenuStructure() {
 		menuGame.setMnemonic(KeyEvent.VK_G);
 
 		menuBar.add(menuGame);
 		menuBar.add(menuTheme);
 		menuBar.add(menuInfo);
 		
 		menuInfo.add(infoAbout);
 		
 		menuTheme.add(themeDefault);
		menuTheme.add(themeFancy);
 		
 		menuGame.add(menuGameNew);
 		menuGame.add(menuGameSurrender);
 		menuGame.addSeparator();
 		menuGame.add(menuGameExit);
 		frame.setJMenuBar(menuBar);
 
 		menuGameSurrender.setEnabled(false);
 		menuGameNew.setEnabled(false);
 	}
 
 	private void bindMenuActions() {
 		menuGameSurrender.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				controller.handleSurrender();
 
 			}
 		});
 		menuGameNew.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				controller.handleNewGame();
 
 			}
 		});
 		menuGameExit.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				controller.handleExit();
 			}
 		});
 		themeDefault.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				controller.changeTheme("default");
 				controller.updateTheme();
 			}
 		});
		themeFancy.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				controller.changeTheme("minimal");
 				controller.updateTheme();
 			}
 		});
 		infoAbout.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				JOptionPane.showMessageDialog(aboutFrame, 
 							"Rock Paper Scissors Game written in Java\n\n"+
 							"Developer Team:\n"+
 							"Felix Breidenstein\n"+
 							"Sebastian Bechtel\n"+
 							"Wilhelm Werner\n"+
 							"Robert Respondek\n\n"+
 							"Sourcecode can be found at:\n"+
 							"https://github.com/f-breidenstein/rock-paper-scissors/"
 							);
 			}
 		});
 	}
 
 	public void gameStarted() {
 		menuGameNew.setEnabled(true);
 		menuGameSurrender.setEnabled(true);
 	}
 
 	public void gameEnded() {
 		menuGameNew.setEnabled(true);
 		menuGameSurrender.setEnabled(false);
 	}
 
 	public void reset() {
 		menuGameNew.setEnabled(false);
 		menuGameSurrender.setEnabled(false);
 	}
 }
