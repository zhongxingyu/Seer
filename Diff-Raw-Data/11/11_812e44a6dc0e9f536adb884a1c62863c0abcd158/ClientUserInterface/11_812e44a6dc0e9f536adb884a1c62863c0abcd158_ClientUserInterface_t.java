 package herbstJennrichLehmannRitter.ui.impl;
 
 import herbstJennrichLehmannRitter.engine.Globals;
 import herbstJennrichLehmannRitter.engine.model.Card;
 import herbstJennrichLehmannRitter.engine.model.Data;
 import herbstJennrichLehmannRitter.server.GameServer;
 import herbstJennrichLehmannRitter.ui.UserInterface;
 import herbstJennrichLehmannRitter.ui.GUI.ClientMenuGUI;
 import herbstJennrichLehmannRitter.ui.GUI.HostMenuGUI;
 import herbstJennrichLehmannRitter.ui.GUI.MainMenuGUI;
 import herbstJennrichLehmannRitter.ui.GUI.PlayGameGUI;
 
 import java.rmi.RemoteException;
 import java.util.Collection;
 
 import org.eclipse.swt.widgets.Display;
 
 public class ClientUserInterface implements UserInterface {
 
	private static final long serialVersionUID = 4502825312932565802L;
	
 	private MainMenuGUI mainMenuGUI;
 	private PlayGameGUI playGameGUI;
 	private HostMenuGUI hostMenuGUI;
 	private ClientMenuGUI clientMenuGUI;
 
 	public void setMainMenuGUI(MainMenuGUI mainMenuGUI) {
 		this.mainMenuGUI = mainMenuGUI;
 	}
 	
 	public void setPlayGameGUI(PlayGameGUI playGameGUI) {
 		this.playGameGUI = playGameGUI;
 	}
 	
 	public void setHostMenuGUI(HostMenuGUI hostMenuGUI) {
 		this.hostMenuGUI = hostMenuGUI;
 	}
 	
 	public void setClientMenuGUI(ClientMenuGUI clientMenuGUI) {
 		this.clientMenuGUI = clientMenuGUI;
 	}
 	
 	@Override
 	public void setData(final Data data) {
 		Display.getDefault().asyncExec(new Runnable() {
 			
 			@Override
 			public void run() {
 				ClientUserInterface.this.playGameGUI.setPlayerDungeonLevel(data.getOwnPlayer().getDungeon().getLevel());
 				ClientUserInterface.this.playGameGUI.setPlayerDungeonStock(data.getOwnPlayer().getDungeon().getStock());
 				ClientUserInterface.this.playGameGUI.setPlayerMagicLabLevel(data.getOwnPlayer().getMagicLab().getLevel());
 				ClientUserInterface.this.playGameGUI.setPlayerMagicLabStock(data.getOwnPlayer().getMagicLab().getStock());
 				ClientUserInterface.this.playGameGUI.setPlayerMineLevel(data.getOwnPlayer().getMine().getLevel());
 				ClientUserInterface.this.playGameGUI.setPlayerMineStock(data.getOwnPlayer().getMine().getStock());
 				ClientUserInterface.this.playGameGUI.setPlayerTower(data.getOwnPlayer().getTower().getActualPoints());
 				ClientUserInterface.this.playGameGUI.setPlayerWall(data.getOwnPlayer().getWall().getActualPoints());
 				ClientUserInterface.this.playGameGUI.setPlayerHandCards(data.getOwnPlayer().getDeck().getAllCards());
 			}
 		});
 	}
 	
 	@Override
 	public void twoPlayerFound() {
 		if (this.hostMenuGUI != null) {
 			this.hostMenuGUI.cancelTimerAndOpenPlayGameGUI();
 		} else if (this.clientMenuGUI != null) {
 			this.clientMenuGUI.cancelTimerAndOpenPlayGameGUI();
 		} else {
 			GameServer gameServer = Globals.getLocalGameServer();
 			try {
 				gameServer.start(this.mainMenuGUI.getGameType());
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	@Override
 	public void nextTurn() {
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				ClientUserInterface.this.playGameGUI.nextTurnPlayer();
 			}
 		});
 	}
 
 	@Override
 	public void playAnotherCard() {
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				ClientUserInterface.this.playGameGUI.playAnotherCardPlayer();
 			}
 		});
 	}
 
 	@Override
 	public void enemyPlayedCard(final Card card) {
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				ClientUserInterface.this.playGameGUI.setEnemyChoosenCardName(card.getName());
 			}
 		});
 	}
 	
 	@Override
 	public void onPlayCard(final Card card) {
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				ClientUserInterface.this.playGameGUI.playerPlayedCard(card.getName());
 			}
 		});
 	}
 	
 	@Override
 	public void onDiscardCard(final Card card) {
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				ClientUserInterface.this.playGameGUI.playerDiscardCard(card.getName());
 			}
 		});
 	}
 	
 	
 	@Override
 	public void youLost() {
 		Display.getDefault().asyncExec(new Runnable() {
 			
 			@Override
 			public void run() {
 				ClientUserInterface.this.playGameGUI.setGameStateToLoose();
 			}
 		});
 	}
 
 	@Override
 	public void youWon() {
 		Display.getDefault().asyncExec(new Runnable() {
 			
 			@Override
 			public void run() {
 				ClientUserInterface.this.playGameGUI.setGameStateToWon();
 			}
 		});
 	}
 
 	@Override
 	public void abort(final String reason) {
 		if (this.playGameGUI != null) {
 			Display.getDefault().asyncExec(new Runnable() {
 				
 				@Override
 				public void run() {
 					ClientUserInterface.this.playGameGUI.setGameStateToAbort(reason);
 				}
 			});
 		} else if (this.hostMenuGUI != null) {
 			this.hostMenuGUI.displayMessageBox(reason);
 		}
 	}
 
 	@Override
 	public String getName() {
 		return this.mainMenuGUI.getPlayerName();
 	}
 
 	@Override
 	public Collection<String> getCards() {
 		return this.mainMenuGUI.getPlayerCards();
 	}
 }
