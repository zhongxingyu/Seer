 package controller;
 
 
 import inputOutput.GameIO;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import model.GameModel;
 import model.MainModel;
 import model.save.SavePath;
 import resources.Translator;
 import view.Window;
 import view.menu.LoadingPanel;
 import view.menu.MenuButton;
 import view.menu.MenuPanel;
 import view.menu.subMenuPanels.SaveLoadGame;
 import view.menu.subMenuPanels.Score;
 import view.menu.subMenuPanels.Settings;
 
 /**
  * The controller which is responsible for managing the program's window and it's menus.
  * 
  * @author Vidar Eriksson
  *
  */
 public class MenuController{
 	private Window WINDOW = new Window();
 	private GameController gameController = null;
 	private MainModel mainModel = new MainModel();
 	private MenuActionButtons buttons = new MenuActionButtons();
 	
 	/**
 	 * Creates a new game window and starts the main menu for the game.
 	 */
 	public MenuController() {
 		showMainMenu();		
 	}
 	/**
 	 * Changes this controllers window to the main menu.
 	 */
 	public void showMainMenu(){
 		WINDOW.add(new MenuPanel(Translator.getMenuString("mainMenu"), buttons.getMainMenuButtons()));
 	}
 	private void startGame(GameModel m){
 		if (gameController != null){
 			gameController.stopThread();
 		}
 		if (m == null){
 			startNewGame();
 		}
 		
 		WINDOW.add(new LoadingPanel());
 		
 		gameController = new GameController(mainModel, this);
 		mainModel.setGameModel(m);
 		gameController.init();
 
 		WINDOW.add(gameController.getGamePanel());
 		new Thread(gameController).start();
 	}
 	private void startNewGame(){
 		startGame(GameIO.newGame());
 	}
 	private static void exitGame(){
 		System.exit(0);
 	}
 	private void showhighscore(){
		WINDOW.add(new Score(buttons.getMainMenuButton()));
 	}
 	private void showSettingsFromMainMenu(){
 		WINDOW.add(new Settings(buttons.getMainMenuButton()));
 	}
 	private void showSettingsFromPauseMenu(){
 		WINDOW.add(new Settings(buttons.getPauseMenuButton()));
 	}
 	private void showSaveLoadGame(){
 		WINDOW.add(new SaveLoadGame(new MenuButton[]{buttons.getLoadButton(),
 			buttons.getSaveButton(), buttons.getPauseMenuButton()}, true));
 	}
 	private void showLoadGame(){
 		WINDOW.add(new SaveLoadGame(new MenuButton[]{buttons.getLoadButton(),
 			buttons.getSaveButton(), buttons.getMainMenuButton()}, false));
 	}
 	/**
 	 * Changes this controllers window to the game over view.
 	 */
 	public void showGameOverPanel(){
		WINDOW.add(new Score(buttons.getMainMenuButton()));
 	}
 	/**
 	 * Changes this controllers window to the paused game menu view.
 	 */
 	public void showPauseMenu(){
 		WINDOW.add(new MenuPanel(Translator.getMenuString("pause"), buttons.getPauseMenuButtons()));
 	}
 	private void resumeGame() {
 		WINDOW.add(gameController.getGamePanel());
 		gameController.resumeThread();
 	}
 	
 	
 	private class MenuActionButtons {
 
 		private MenuButton getSaveButton() {
 			MenuButton b  = new MenuButton(Translator.getMenuString("save"));
 			b .addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					GameIO.saveGame(gameController.getGameModel(), e.getActionCommand());
 					WINDOW.repaint();
 				}
 			});
 				return b;
 		}
 		
 		private MenuButton getLoadButton() {
 			MenuButton b = new MenuButton(Translator.getMenuString("load"));
 			b.setEnabled(false);
 			b .addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					startGame(GameIO.loadGame(SavePath.getSaveFolder() + e.getActionCommand()));
 				}
 			});
 			return b;
 		}
 		
 		
 		private MenuButton getMainMenuButton() {
 			MenuButton b = new MenuButton(Translator.getMenuString("mainMenu"));
 			b .addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					showMainMenu();
 				}
 			});
 			return b;
 		}
 		
 		private MenuButton getPauseMenuButton() {
 			MenuButton b = new MenuButton(Translator.getMenuString("pauseMenu"));
 			b .addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					showPauseMenu();
 				}
 			});
 			return b;
 		}
 		
 		private MenuButton[] getMainMenuButtons() {
 				
 			MenuButton[] buttons = new MenuButton[5];
 			
 			buttons[0]= new MenuButton(Translator.getMenuString("newGame"));
 			buttons[0].addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					startNewGame();
 				}
 			});
 			
 			buttons[1]= new MenuButton(Translator.getMenuString("load"));
 			buttons[1].addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					showLoadGame();
 				}
 			});
 			
 			buttons[2]= new MenuButton(Translator.getMenuString("highScore"));
 			buttons[2].addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					showhighscore();
 				}
 			});
 			
 			buttons[3]= new MenuButton(Translator.getMenuString("settings"));
 			buttons[3].addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					showSettingsFromMainMenu();
 				}
 			});
 			
 			buttons[4]= new MenuButton(Translator.getMenuString("exitGame"));
 			buttons[4].addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					exitGame();
 				}
 			});
 		
 			return buttons;
 		}
 
 		private MenuButton[] getPauseMenuButtons() {
 
 			MenuButton buttons[] = new MenuButton[5];
 			
 			buttons[0]= new MenuButton(Translator.getMenuString("settings"));
 			buttons[0].addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					showSettingsFromPauseMenu();
 				}
 			});
 			
 			buttons[1]= new MenuButton(Translator.getMenuString("saveLoad"));
 			buttons[1].addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					showSaveLoadGame();
 				}
 			});
 			
 			buttons[2]= new MenuButton(Translator.getMenuString("mainMenu"));
 			buttons[2].addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					showMainMenu();
 				}
 			});
 			
 			buttons[3]= new MenuButton(Translator.getMenuString("exitGame"));
 			buttons[3].addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					exitGame();
 				}
 			});
 			
 			buttons[4]= new MenuButton(Translator.getMenuString("resume"));
 			buttons[4].addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					resumeGame();
 				}
 			});
 
 			return buttons;
 		}
 
 	}
 
 }
