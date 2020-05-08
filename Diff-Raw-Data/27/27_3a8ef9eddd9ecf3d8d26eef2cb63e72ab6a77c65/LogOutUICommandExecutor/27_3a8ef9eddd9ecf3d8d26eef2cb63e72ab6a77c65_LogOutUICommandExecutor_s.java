 package pl.edu.agh.two.mud.server.command.executor;
 
 import pl.edu.agh.two.mud.common.IPlayer;
 import pl.edu.agh.two.mud.common.command.dispatcher.Dispatcher;
 import pl.edu.agh.two.mud.common.command.exception.FatalException;
 import pl.edu.agh.two.mud.common.command.executor.CommandExecutor;
 import pl.edu.agh.two.mud.server.IServiceRegistry;
 import pl.edu.agh.two.mud.server.Service;
 import pl.edu.agh.two.mud.server.command.LogOutUICommand;
 import pl.edu.agh.two.mud.server.command.SendAvailableCommandsCommand;
 import pl.edu.agh.two.mud.server.command.SendMessageToUserCommand;
 import pl.edu.agh.two.mud.server.command.util.AvailableCommands;
 import pl.edu.agh.two.mud.server.world.model.Board;
 
 import java.io.IOException;
 
 import static pl.edu.agh.two.mud.common.message.MessageType.INFO;
 
 public class LogOutUICommandExecutor implements CommandExecutor<LogOutUICommand> {
 
     private Board board;
     private IServiceRegistry serviceRegistry;
     private Dispatcher dispatcher;
 
     @Override
     public void execute(LogOutUICommand command) throws FatalException {
         Service service = serviceRegistry.getCurrentService();
         IPlayer currentPlayer = serviceRegistry.getPlayer(service);
 
        if (currentPlayer != null) {
            serviceRegistry.unbindPlayer(currentPlayer);
             try {
                 dispatcher.dispatch(new SendMessageToUserCommand("Zegnaj, " + currentPlayer.getName(), INFO));
                 dispatcher.dispatch(new SendAvailableCommandsCommand(currentPlayer, AvailableCommands.getInstance().getUnloggedCommands()));
                 board.getPlayersPosition(currentPlayer).removePlayer(currentPlayer);
                 board.removePlayer(currentPlayer);
                 service.writeObject((IPlayer) null);
             } catch (IOException e) {
                 throw new FatalException(e);
             }
         } else {
             dispatcher.dispatch(new SendMessageToUserCommand("Nie jestes zalogowany!", INFO));
         }
 
     }
 
     public void setBoard(Board board) {
         this.board = board;
     }
 
     public void setServiceRegistry(IServiceRegistry serviceRegistry) {
         this.serviceRegistry = serviceRegistry;
     }
 
     public void setDispatcher(Dispatcher dispatcher) {
         this.dispatcher = dispatcher;
     }
 
 }
