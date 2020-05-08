 package pl.edu.agh.two.mud.server.command.executor;
 
 import pl.edu.agh.two.mud.common.IPlayer;
 import pl.edu.agh.two.mud.common.command.exception.CommandExecutingException;
 import pl.edu.agh.two.mud.common.command.exception.FatalException;
 import pl.edu.agh.two.mud.common.command.executor.CommandExecutor;
 import pl.edu.agh.two.mud.server.IServiceRegistry;
 import pl.edu.agh.two.mud.server.Service;
 import pl.edu.agh.two.mud.server.command.RunUICommand;
import pl.edu.agh.two.mud.server.command.exception.ClientAwareException;
 import pl.edu.agh.two.mud.server.world.fight.Fight;
 import pl.edu.agh.two.mud.server.world.model.Board;
 import pl.edu.agh.two.mud.server.world.model.Direction;
 import pl.edu.agh.two.mud.server.world.model.Field;
 
 public class RunUICommandExecutor implements CommandExecutor<RunUICommand> {
 	private IServiceRegistry serviceRegistry;
 	private Fight fight;
 	private Board board;
 
 	@Override
 	public void execute(RunUICommand command) throws CommandExecutingException {
 		Service currentService = serviceRegistry.getCurrentService();
 		IPlayer currentPlayer = serviceRegistry.getPlayer(currentService);
 
 		if (currentPlayer != null) {
 			Direction direction = command.getDirection();
 			Field from = board.getPlayersPosition(currentPlayer);
 
 			if (!isDirectionValid(direction, from)) {
				throw new ClientAwareException("Nie mozesz isc w te strone.");
 			} else {
 				fight.runFromFight(currentPlayer, direction);
 			}
 		} else {
 			throw new FatalException(new RuntimeException());
 		}
 
 	}
 
 	private boolean isDirectionValid(Direction direction, Field from) {
 		return direction != null
 				&& board.getPossibleDirections(from).contains(direction);
 	}
 
 	public void setServiceRegistry(IServiceRegistry serviceRegistry) {
 		this.serviceRegistry = serviceRegistry;
 	}
 
 	public void setFight(Fight fight) {
 		this.fight = fight;
 	}
 
 	public void setBoard(Board board) {
 		this.board = board;
 	}
 
 }
