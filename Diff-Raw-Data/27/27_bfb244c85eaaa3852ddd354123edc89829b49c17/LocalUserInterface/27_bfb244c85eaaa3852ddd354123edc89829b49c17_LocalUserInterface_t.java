 package herbstJennrichLehmannRitter.ui.impl;
 
 import herbstJennrichLehmannRitter.engine.Globals;
 import herbstJennrichLehmannRitter.engine.model.Card;
 import herbstJennrichLehmannRitter.engine.model.Data;
 import herbstJennrichLehmannRitter.server.GameServer;
 import herbstJennrichLehmannRitter.ui.UserInterface;
 import herbstJennrichLehmannRitter.ui.GUI.MainMenuGUI;
 import herbstJennrichLehmannRitter.ui.GUI.PlayGameGUI;
 
 import java.rmi.RemoteException;
 import java.util.Collection;
 
 import org.eclipse.swt.widgets.Display;
 
 public class LocalUserInterface implements UserInterface {
 
 	private MainMenuGUI mainMenuGUI;
 	private PlayGameGUI playGameGUI;
 
 	public void setMainMenuGUI(MainMenuGUI mainMenuGUI) {
 		this.mainMenuGUI = mainMenuGUI;
 	}
 	
 	public void setPlayGameGUI(PlayGameGUI playGameGUI) {
 		this.playGameGUI = playGameGUI;
 	}
 	
 	@Override
 	public void setData(final Data data) {
 		Display.getDefault().asyncExec(new Runnable() {
 			
 			@Override
 			public void run() {
 				LocalUserInterface.this.playGameGUI.setEnemyDungeonLevel(data.getOwnPlayer().getDungeon().getLevel());
 				LocalUserInterface.this.playGameGUI.setEnemyMagicLabLevel(data.getOwnPlayer().getMagicLab().getLevel());
 				LocalUserInterface.this.playGameGUI.setEnemyMineLevel(data.getOwnPlayer().getMine().getLevel());
 				LocalUserInterface.this.playGameGUI.setEnemyTower(data.getOwnPlayer().getTower().getActualPoints());
 				LocalUserInterface.this.playGameGUI.setEnemyWall(data.getOwnPlayer().getWall().getActualPoints());
 			}
 		});
 	}
 	
 	@Override
 	public void twoPlayerFound() {
 		GameServer gameServer = Globals.getLocalGameServer();
 		try {
 			gameServer.start(this.mainMenuGUI.getGameType());
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void nextTurn() {
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				playGameGUI.nextTurnEnemy();
 			}
 		});
 	}
 
 	@Override
 	public void playAnotherCard() {
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				playGameGUI.playAnotherCardEnemy();
 			}
 		});
 	}
 
 	@Override
 	public void enemyPlayedCard(final Card card) {
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				playGameGUI.setPlayerChoosenCardName(card.getName());
 			}
 		});
 	}
 	
 	public void onPlayCard(final Card card) {}
 	
 	@Override
 	public void onDiscardCard(final Card card) {}
 	
 	@Override
 	public void youLost() {}
 
 	@Override
 	public void youWon() {}
 
 	@Override
 	public void abort(final String reason) {}
 
 	@Override
 	public String getName() {
 		return this.mainMenuGUI.getEnemyName();
 	}
 
 	@Override
 	public Collection<String> getCards() {
 		return this.mainMenuGUI.getEnemyCards();
 	}
 }
