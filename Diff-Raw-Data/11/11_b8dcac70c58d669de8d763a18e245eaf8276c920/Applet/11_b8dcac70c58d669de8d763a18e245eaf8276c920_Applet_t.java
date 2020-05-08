 /*
  * Copyright 2008 Sven Strickroth <email@cs-ware.de>
  * 
  * This file is part of JBubbleBreaker.
  * 
  * JBubbleBreaker is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 3 as
  * published by the Free Software Foundation.
  * 
  * JBubbleBreaker is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with JBubbleBreaker. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.jbubblebreaker;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JApplet;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 
 /**
 * GUI for jBubbleBreaker (in applet mode)
  * @author Sven Strickroth
  */
 @SuppressWarnings("serial")
 public class Applet extends JApplet implements ActionListener, GUIIf {
 	private JPanel infoPanel = new JPanel();
 	private Game game;
 	private JLabel pointsLabel = new JLabel();
 	private JLabel gameModeLabel = new JLabel();
 	private JMenuItem menuHelpInfo,menuGameNew,menuGameNewDots;
 
 	@Override
 	public void init() {
 		JBubbleBreaker.registerDefault();
 		try {
 			SwingUtilities.invokeAndWait(new Runnable() {
 				public void run() {
 					createGUI();
 				}
 			});
 		} catch (Exception e) {
 			System.err.println("createGUI didn't successfully complete");
 		}
 	}
 
 	/**
 	 * Prepares the GUI in a separated thread
 	 */
 	private void createGUI() {
 		// insert Menu
 		JMenuBar menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 		JMenu menuGame = new JMenu(Localization.getString("MenuGame"));
 		menuGame.setMnemonic(Localization.getChar("MenuGameMnemonic"));
 		menuBar.add(menuGame);
 		JMenu menuHelp = new JMenu(Localization.getString("MenuHelp"));
 		menuHelp.setMnemonic(Localization.getChar("MenuHelpMnemonic"));
 		menuBar.add(menuHelp);
 		menuGameNew = new JMenuItem(Localization.getString("MenuNew"));
 		menuGameNew.addActionListener(this);
 		menuGameNew.setMnemonic(Localization.getChar("MenuNewMnemonic"));
 		menuGame.add(menuGameNew);
 		menuGameNewDots = new JMenuItem(Localization.getString("MenuNewDots"));
 		menuGameNewDots.addActionListener(this);
 		menuGameNewDots.setMnemonic(Localization.getChar("MenuNewDotsMnemonic"));
 		menuGame.add(menuGameNewDots);
 		menuHelpInfo = new JMenuItem(Localization.getString("MenuAbout"));
 		menuHelpInfo.addActionListener(this);
 		menuHelpInfo.setMnemonic(Localization.getChar("MenuAboutMnemonic"));
 		menuHelp.add(menuHelpInfo);
 
 		newGameDots();
 	}
 
 	/**
 	 * Ask the user for game details for a new game
 	 */
 	private void newGameDots() {
 		game=null;
 		menuGameNew.setEnabled(false);
 		menuGameNewDots.setEnabled(false);
 		NewGameAskUserPanel nGAuP = new NewGameAskUserPanel(this);
 		nGAuP.setVisible(false);
 		setContentPane(nGAuP);
 		nGAuP.setVisible(true);
 	}
 
 	public void actionPerformed(ActionEvent arg0) {
 		if (arg0.getSource() == menuHelpInfo) {
 			new AboutBox(null);
 		} else if (arg0.getSource() == menuGameNew) {
 			game.newGame();
 			pointsLabel.setText(Localization.getString("PointsZero"));
 		} else if (arg0.getSource() == menuGameNewDots) {
 			newGameDots();
 		}
 	}
 
 	public void startNewGame(Game game) {
 		this.game = game;
 		JPanel newContentPane = new JPanel();
 		newContentPane.setVisible(false);
 		setContentPane(newContentPane);
 		newContentPane.setVisible(true);
 		setLayout(new BorderLayout());
 		getContentPane().add(infoPanel, BorderLayout.SOUTH);
 		infoPanel.setSize(60, 60);
 		if (game != null) {
 			getContentPane().remove(game.getPanel());
 		}
 		gameModeLabel.setText(game.getMode());
 		getContentPane().add(game.getPanel(), BorderLayout.CENTER);
 		game.setPointsLabel(pointsLabel);
 
 		infoPanel.setLayout(new BorderLayout());
 
 		infoPanel.add(pointsLabel, BorderLayout.WEST);
 		
 		infoPanel.add(gameModeLabel, BorderLayout.EAST);
 		pointsLabel.setText(Localization.getString("PointsZero"));
 
 		menuGameNew.setEnabled(true);
 		menuGameNewDots.setEnabled(true);
 	}
 }
