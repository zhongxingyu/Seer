 package pl.edu.agh.two.mud.server.command.executor;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import pl.edu.agh.two.mud.common.IPlayer;
 import pl.edu.agh.two.mud.common.command.dispatcher.Dispatcher;
 import pl.edu.agh.two.mud.common.command.exception.CommandExecutingException;
 import pl.edu.agh.two.mud.common.command.type.Text;
 import pl.edu.agh.two.mud.common.message.MessageType;
 import pl.edu.agh.two.mud.server.IServiceRegistry;
 import pl.edu.agh.two.mud.server.Service;
 import pl.edu.agh.two.mud.server.command.SendMessageToUserCommand;
 import pl.edu.agh.two.mud.server.command.WhisperUICommand;
 import pl.edu.agh.two.mud.server.command.exception.ClientAwareException;
 import pl.edu.agh.two.mud.server.world.exception.NoPlayerWithSuchNameException;
 import pl.edu.agh.two.mud.server.world.model.Board;
 import pl.edu.agh.two.mud.server.world.model.Field;
 
 public class WhisperUICommandExecutorTest {
 
 	private static final String MESSAGE = "Some message";
 	private static final String TARGET_USER = "user";
 
 	private WhisperUICommandExecutor executor;
 	private Board board;
 	private IServiceRegistry serviceRegistry;
 	private IPlayer currentPlayer;
 	private Service currentService;
 	private Dispatcher dispatcher;
 	private WhisperUICommand command;
	private Dispatcher dispatcher;
 
 	@Before
 	public void prepareTest() {
 		board = mock(Board.class);
 		currentService = mock(Service.class);
 		currentPlayer = mock(IPlayer.class);
 		dispatcher = mock(Dispatcher.class);
 
 		serviceRegistry = mock(IServiceRegistry.class);
 		when(serviceRegistry.getCurrentService()).thenReturn(currentService);
 		when(serviceRegistry.getPlayer(currentService)).thenReturn(currentPlayer);
 
 		executor = new WhisperUICommandExecutor();
 		executor.setBoard(board);
 		executor.setDispatcher(dispatcher);
 		executor.setServiceRegistry(serviceRegistry);
 		executor.setDispatcher(dispatcher);
 
 		command = mock(WhisperUICommand.class);
 		when(command.getContent()).thenReturn(new Text(MESSAGE));
 		when(command.getTarget()).thenReturn(TARGET_USER);
 	}
 
 	@Test
 	public void userDoesNotExist() {
 		when(serviceRegistry.getPlayer(any(Service.class))).thenReturn(null);
 		try {
 			executor.execute(command);
 			fail("Exception expected");
 		} catch (ClientAwareException e) {
 			assertEquals("Jestes niezalogowany.", e.getErrorMessage());
 
 		} catch (CommandExecutingException e) {
 			fail("Other exception expected");
 		}
 	}
 
 	@Test
 	public void noFieldForUser() {
 		when(board.getPlayersPosition(currentPlayer)).thenReturn(null);
 
 		try {
 			executor.execute(command);
 			fail("Exception expected");
 		} catch (ClientAwareException e) {
 			assertEquals("Nieznany blad.", e.getErrorMessage());
 		} catch (CommandExecutingException e) {
 			fail("Other exception expected");
 		}
 	}
 
 	@Test
 	public void noSuchPlayerOnTheField() {
 		Field field = mock(Field.class);
 
 		when(board.getPlayersPosition(currentPlayer)).thenReturn(field);
 		try {
 			when(field.getPlayerByName(TARGET_USER)).thenThrow(new NoPlayerWithSuchNameException(TARGET_USER));
 		} catch (NoPlayerWithSuchNameException e) {
 			e.printStackTrace();
 		}
 
 		try {
 			executor.execute(command);
 			fail("Exception expected");
 		} catch (ClientAwareException e) {
 			assertEquals("Nie ma takiego gracza na tym polu.", e.getErrorMessage());
 		} catch (CommandExecutingException e) {
 			fail("Other exception expected");
 		}
 	}
 
 	@Test
 	public void whisper() {
 		Field field = mock(Field.class);
 		IPlayer targetPlayer = mock(IPlayer.class);
 
 		when(board.getPlayersPosition(currentPlayer)).thenReturn(field);
 		try {
 			when(field.getPlayerByName(TARGET_USER)).thenReturn(targetPlayer);
 		} catch (NoPlayerWithSuchNameException e) {
 			e.printStackTrace();
 		}
 
 		try {
 			executor.execute(command);
 			verify(dispatcher).dispatch(
 					new SendMessageToUserCommand(targetPlayer, String.format("%s szepcze: %s", currentPlayer.getName(),
 							MESSAGE), MessageType.INFO));
 
 		} catch (CommandExecutingException e) {
 			fail("Exception unexpected");
 		}
 	}
 }
