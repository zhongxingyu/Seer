 package herbstJennrichLehmannRitter.server.impl;
 
 import herbstJennrichLehmannRitter.engine.enums.GameType;
 import herbstJennrichLehmannRitter.engine.model.Card;
 import herbstJennrichLehmannRitter.engine.service.GameService;
 import herbstJennrichLehmannRitter.server.GameServer;
 import herbstJennrichLehmannRitter.ui.UserInterface;
 
 import java.util.Collection;
 
 public class GameServerImpl implements GameServer {
 
 	private GameService gameService;
 	
 	public GameServerImpl(GameService gameService) {
 		this.gameService = gameService;
 	}
 
 	@Override
 	public void start(GameType gameType) {
 		this.gameService.start(gameType);
 	}
 	
 	@Override
 	public void stop() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				GameServerImpl.this.gameService.stop();
			}
		}).start();
 	}
 
 	@Override
 	public void playCard(final Card card) {
 		final Thread thread = Thread.currentThread();
 		new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				GameServerImpl.this.gameService.playCard(thread, card);
 			}
 		}).start();
 	}
 
 	@Override
 	public void discardCard(final Card card) {
 		final Thread thread = Thread.currentThread();
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				GameServerImpl.this.gameService.discardCard(thread, card);
 			}
 		}).start();
 	}
 
 	@Override
 	public Collection<Card> getAllPossibleCards() {
 		return this.gameService.getAllPossibleCards();
 	}
 
 	@Override
 	public void register(final UserInterface userInterface) {
 		final Thread thread = Thread.currentThread();
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				GameServerImpl.this.gameService.register(thread, userInterface);
 			}
 		}).start();
 	}
 
 	@Override
 	public void unregister(final UserInterface userInterface) {
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				GameServerImpl.this.gameService.unregister(userInterface);
 			}
 		}).start();
 	}
 	
 }
