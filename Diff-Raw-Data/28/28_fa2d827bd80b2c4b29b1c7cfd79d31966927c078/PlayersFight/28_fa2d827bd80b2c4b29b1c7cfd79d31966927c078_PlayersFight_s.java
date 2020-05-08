 package pl.edu.agh.two.mud.server.world.fight.impl;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Random;
 
 import org.apache.log4j.Logger;
 
 import pl.edu.agh.two.mud.common.IPlayer;
 import pl.edu.agh.two.mud.common.command.UICommand;
 import pl.edu.agh.two.mud.common.command.dispatcher.Dispatcher;
 import pl.edu.agh.two.mud.common.command.exception.FatalException;
 import pl.edu.agh.two.mud.common.message.MessageType;
 import pl.edu.agh.two.mud.server.IServiceRegistry;
 import pl.edu.agh.two.mud.server.Service;
 import pl.edu.agh.two.mud.server.command.MoveUICommand;
 import pl.edu.agh.two.mud.server.command.SendAvailableCommandsCommand;
 import pl.edu.agh.two.mud.server.command.SendMessageToUserCommand;
 import pl.edu.agh.two.mud.server.command.util.AvailableCommands;
 import pl.edu.agh.two.mud.server.world.fight.Fight;
 import pl.edu.agh.two.mud.server.world.model.Direction;
 
 public class PlayersFight implements Fight {
 	private Dispatcher dispatcher;
 	private IServiceRegistry serviceRegistry;
 
 	@Override
 	public void startFight(IPlayer playerOne, IPlayer playerTwo) {
 		dispatcher.dispatch(new SendMessageToUserCommand(String.format("Zaatkowales gracza %s", playerTwo.getName()),
 				MessageType.INFO));
 		dispatcher.dispatch(new SendMessageToUserCommand(playerTwo, String.format(
 				"Zostales zaatakowany przez gracza %s", playerOne.getName()), MessageType.INFO));
 
 		int whoAttacksFirst = new Random().nextInt(2);
 		switch (whoAttacksFirst) {
 			case 0:
 				switchAttackingPlayer(playerTwo, playerOne);
 				break;
 			case 1:
 				switchAttackingPlayer(playerOne, playerTwo);
 				break;
 		}
 
 	}
 
 	@Override
 	public IPlayer getCurrentlyAttackingPlayer() {
 		return null;
 	}
 
 	@Override
 	public void hit(IPlayer playerWhoHits) throws FatalException {
 		IPlayer enemy = playerWhoHits.getEnemy();
 		int damage = new Random().nextInt(4) + 1;
 		enemy.subtractHealthPoints(damage);
 		Service enemyService = serviceRegistry.getService(enemy);
 
 		try {
 			enemyService.writeObject(enemy);
 
 			if (enemy.isAlive()) {
 				dispatcher.dispatch(new SendMessageToUserCommand(playerWhoHits, String.format(
 						"Zadales przeciwnikowi %d pkt obrazen.", damage), MessageType.INFO));
 				dispatcher.dispatch(new SendMessageToUserCommand(enemy, String.format(
 						"Krwawisz! Zadano ci %d pkt obrazen.", damage), MessageType.INFO));
 
 				switchAttackingPlayer(playerWhoHits, enemy);
 			} else {
 				dispatcher.dispatch(new SendMessageToUserCommand(playerWhoHits, "Wygrales!", MessageType.INFO));
 				dispatcher.dispatch(new SendMessageToUserCommand(enemy, "Zginales!", MessageType.INFO));
 
 				endFight(playerWhoHits, enemy);
 			}
 		} catch (IOException e) {
 			throw new FatalException(e, Logger.getLogger(getClass()));
 		}
 	}
 
 	@Override
 	public void runFromFight(IPlayer currentPlayer, Direction direction) {
 		IPlayer enemy = currentPlayer.getEnemy();
 		boolean canRun = Math.random() > 0.5;
 		if (!canRun) {
 			dispatcher.dispatch(new SendMessageToUserCommand("Nie udalo Ci sie uciec", MessageType.INFO));
 			dispatcher.dispatch(new SendMessageToUserCommand(enemy, "Przeciwnik probowal uciec lecz mu sie nie udalo",
 					MessageType.INFO));
 
 			switchAttackingPlayer(currentPlayer, enemy);
 		} else {
 			dispatcher.dispatch(new SendMessageToUserCommand("Udalo Ci sie uciec", MessageType.INFO));
 			dispatcher.dispatch(new SendMessageToUserCommand(enemy, "Przeciwnikowi udalo sie uciec", MessageType.INFO));
 
 			dispatcher.dispatch(new MoveUICommand(direction));
 			endFight(currentPlayer, enemy);
 		}
 	}
 
 	public void switchAttackingPlayer(IPlayer from, IPlayer to) {
 		dispatcher.dispatch(new SendMessageToUserCommand(from, "Tura przeciwnika", MessageType.INFO));
 		dispatcher.dispatch(new SendMessageToUserCommand(to, "Twoja tura", MessageType.INFO));
 
 		sendAvailableCommands(from, AvailableCommands.getInstance().getFightOpponentTurnCommands());
 		sendAvailableCommands(to, AvailableCommands.getInstance().getFightYouTurnCommands());
 	}
 
 	public void setServiceRegistry(IServiceRegistry serviceRegistry) {
 		this.serviceRegistry = serviceRegistry;
 	}
 
 	public void setDispatcher(Dispatcher dispatcher) {
 		this.dispatcher = dispatcher;
 	}
 
 	private void endFight(IPlayer player1, IPlayer player2) {
 		playersFightEnd(player1);
		playersFightEnd(player1);
 	}
 
 	private void playersFightEnd(IPlayer player) {
 		player.setEnemy(null);
 
 		if (player.isAlive()) {
 			sendAvailableCommands(player, AvailableCommands.getInstance().getGameCommands());
 		} else {
 			sendAvailableCommands(player, AvailableCommands.getInstance().getDeadPlayerCommands());
 		}
 	}
 
 	private void sendAvailableCommands(IPlayer player, Collection<UICommand> availableCommands) {
 		dispatcher.dispatch(new SendAvailableCommandsCommand(player, availableCommands));
 
 	}
 }
