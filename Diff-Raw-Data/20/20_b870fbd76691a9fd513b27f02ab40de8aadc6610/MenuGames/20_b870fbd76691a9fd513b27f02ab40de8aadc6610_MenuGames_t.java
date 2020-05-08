 package pruebas.Controllers;
 
 import pruebas.Networking.ServerDriver;
 import pruebas.Renders.GameEngine;
 import pruebas.Renders.MenuGamesRender;
 import pruebas.Renders.MenuRender;
 
 public class MenuGames extends Menu {
 
 	public static MenuGames instance;
 
 	public static MenuGames getInstance() {
 		if (instance == null)
 			instance = new MenuGames();
 		return instance;
 	}
 
 	@Override
 	public void update(float delta) {
 	}
 
 	@Override
 	public MenuRender getRender() {
 		return MenuGamesRender.getInstance(this);
 	}
 
 	public void enableRandom() {
 		ServerDriver.enableRandom(GameController.getInstancia().getUser()
 				.getId());
 	}
 
 	public void listGamesSuccess(String[][] games) {
 		((MenuGamesRender) getRender()).listGamesSuccess(games);
 	}
 
 	public void listGamesError(String message) {
 		((MenuGamesRender) getRender()).listGamesError(message);
 	}
 
 	public void enableRandomSuccess() {
 		((MenuGamesRender) getRender()).enableRandomSuccess();
 	}
 
 	public void enableRandomError(String message) {
 		((MenuGamesRender) getRender()).enableRandomError(message);
 	}
 
 	public void logOut() {
 		MenuMaster.changeMenuToLogIn();
 	}
 
 	public void getGameTurn(String gameId) {
 		ServerDriver.getGameTurn(GameController.getInstancia().getUser()
 				.getId(), gameId);
 	}
 
 	public void gameTurnSuccess(String data) {
		GameEngine.getInstance().openMenu();
 	}
 
 	public void getGameTurnSuccess(int player, String data) {
 		GameEngine.getInstance().openGame(player, data);
 	}
 
 	public void postGameTurnError(String message) {
 		// ((MenuGamesRender) getRender()).listGamesError(message);
 	}
 }
