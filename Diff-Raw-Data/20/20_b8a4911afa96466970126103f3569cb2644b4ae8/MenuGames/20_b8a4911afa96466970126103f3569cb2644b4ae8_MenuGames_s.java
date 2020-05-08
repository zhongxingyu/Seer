 package pruebas.Controllers;
 
 import pruebas.Networking.ServerDriver;
 import pruebas.Renders.MenuGamesRender;
 import pruebas.Renders.MenuRender;
 
 public class MenuGames extends Menu {
 
 	public static MenuGames getInstance() {
		// TODO Auto-generated method stub
		return null;
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
 }
