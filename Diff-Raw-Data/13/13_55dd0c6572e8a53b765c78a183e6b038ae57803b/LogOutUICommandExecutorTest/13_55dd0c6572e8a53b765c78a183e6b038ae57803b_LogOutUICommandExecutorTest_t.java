 package pl.edu.agh.two.mud.server.command.executor;
 
 import org.junit.Before;
 import org.junit.Test;
 import pl.edu.agh.two.mud.common.Player;
 import pl.edu.agh.two.mud.common.command.dispatcher.Dispatcher;
 import pl.edu.agh.two.mud.common.command.exception.FatalException;
 import pl.edu.agh.two.mud.common.command.provider.CommandProvider;
 import pl.edu.agh.two.mud.server.Service;
 import pl.edu.agh.two.mud.server.ServiceRegistry;
 import pl.edu.agh.two.mud.server.command.LogOutUICommand;
 import pl.edu.agh.two.mud.server.command.SendMessageToUserCommand;
 import pl.edu.agh.two.mud.server.command.util.AvailableCommands;
 import pl.edu.agh.two.mud.server.world.exception.NoPlayerWithSuchNameException;
 import pl.edu.agh.two.mud.server.world.model.Board;
 import pl.edu.agh.two.mud.server.world.model.SampleBoard;
 
 import java.io.IOException;
 
 import static org.mockito.Mockito.*;
 import static pl.edu.agh.two.mud.common.message.MessageType.INFO;
 
 public class LogOutUICommandExecutorTest {
 
     LogOutUICommandExecutor executor = new LogOutUICommandExecutor();
     Board board;
     ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
     Service service = mock(Service.class);
     Dispatcher dispatcher = mock(Dispatcher.class);
     CommandProvider commandProvider = mock(CommandProvider.class);
 
     @Before
     public void before() {
         board = new SampleBoard();
         when(serviceRegistry.getCurrentService()).thenReturn(service);
         executor.setBoard(board);
         executor.setServiceRegistry(serviceRegistry);
         executor.setDispatcher(dispatcher);
 
         AvailableCommands availableCommands = AvailableCommands.getInstance();
         availableCommands.setCommandProvider(commandProvider);
     }
 
     @Test
     public void shouldSuccessfullyExecuteLogOutCommand() throws NoPlayerWithSuchNameException, FatalException {
         // given
         LogOutUICommand command = mock(LogOutUICommand.class);
 
         Player player = createPlayer("krzyho", "correctPassword");
         board.addPlayer(player);
         when(serviceRegistry.getPlayer(service)).thenReturn(player);
 
         // when
         executor.execute(command);
 
         // then
         verify(serviceRegistry).unbindPlayer(player);
     }
 
     @Test
     public void shouldNotLogoutWhenNoPlayer() throws IOException, FatalException {
         // given
         LogOutUICommand command = mock(LogOutUICommand.class);
 
         // when
         executor.execute(command);
 
         // then
         verify(dispatcher).dispatch(new SendMessageToUserCommand("Nie jestes zalogowany!", INFO));
     }
 
     private Player createPlayer(String name, String password) {
         Player player = new Player();
         player.setName(name);
         player.setPassword(password);
         return player;
     }
 }
