 package pl.edu.agh.two.mud.server.executor;
 
 import java.io.IOException;
 
 import pl.edu.agh.two.mud.common.IPlayer;
 import pl.edu.agh.two.mud.common.command.executor.CommandExecutor;
 import pl.edu.agh.two.mud.server.IServiceRegistry;
 import pl.edu.agh.two.mud.server.Service;
 import pl.edu.agh.two.mud.server.command.LogInCommand;
 import pl.edu.agh.two.mud.server.world.exception.NoPlayerWithSuchNameException;
 import pl.edu.agh.two.mud.server.world.model.Board;
 
 public class LogInCommandExecutor implements CommandExecutor<LogInCommand> {
 
 	private Board board;
 	private IServiceRegistry serviceRegistry;
 
 	@Override
 	public void execute(LogInCommand command) {
 		String login = command.getLogin();
 		String password = command.getPassword();
 		try {
 			IPlayer player = board.getPlayerByName(login);
 			if (player.getPassword().equals(password)) {
				Service service = serviceRegistry.getCurrentService();
 				serviceRegistry.bindPlayerToService(service, player);
 				try {
 					service.writeObject("Witaj, " + login);
 					service.writeObject(player);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		} catch (NoPlayerWithSuchNameException e) {
			e.printStackTrace();
 		}
 
 	}
 
 	public void setBoard(Board board) {
 		this.board = board;
 	}
 
 	public void setServiceRegistry(IServiceRegistry serviceRegistry) {
 		this.serviceRegistry = serviceRegistry;
 	}
 
 }
