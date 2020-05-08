 package com.eucsoft.beeper.handler;
 
 import static org.testng.Assert.assertEquals;
 import static org.mockito.Mockito.*;
 
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.testng.annotations.Test;
 
 import com.eucsoft.beeper.Beeper;
 import com.eucsoft.beeper.model.Room;
 import com.eucsoft.beeper.model.User;
 
 @PrepareForTest(ClientHandler.class)
 public class ClientHandlerTest {
 
 	@Test
 	public void onConnect() {
 		ClientHandler clientHandler = new ClientHandler();
 		clientHandler.onConnect(new User(), "some user information");
 
 		int expected = 1;
 		int actual = Beeper.getInstance().getUsers().size();
 		assertEquals(actual, expected);
 	}
 
 	@Test
 	public void onConnect_user_has_device_info() {
 		String expected = "some user information";
 
 		ClientHandler clientHandler = new ClientHandler();
 		clientHandler.onConnect(new User(), expected);
 
 		User user = Beeper.getInstance().getUsers().iterator().next();
 
 		String actual = user.getDeviceInfo();
 
 		assertEquals(actual, expected);
 	}
 
 	@Test
 	public void onGetRoom() {
 		ClientHandler clientHandler = new ClientHandler();
 		clientHandler.onGetRoom(new User());
 
 		int expected = 1;
 		int actual = Beeper.getInstance().getBench().size();
 		assertEquals(actual, expected);
 	}
 
 	@Test
 	public void onMessageBegin() {
 		ClientHandler clientHandler = new ClientHandler();
 		Room room = new Room();
 		User user1 = new User();
 		User user2 = new User();
 		room.addUser(user1);
 		room.addUser(user2);
 
 		ClientHandler clientHandler2 = mock(ClientHandler.class);
 		clientHandler.onMessageBegin(user1);
 		
 		verify(clientHandler2).sendMessageBegin(user2);
 	}
 
 	@Test
 	public void onMessage() {
 		ClientHandler clientHandler = new ClientHandler();
 		Room room = new Room();
 		User user1 = new User();
 		User user2 = new User();
 		room.addUser(user1);
 		room.addUser(user2);
 
 		ClientHandler clientHandler2 = mock(ClientHandler.class);
 		clientHandler.onMessage("{\"command\":\"message\",\"data\":ldjkfdlfjlfkj}".getBytes(), user1);
 		
 		verify(clientHandler2).sendMessage("{\"command\":\"message\",\"data\":ldjkfdlfjlfkj}".getBytes(), user2);
 	}
 
 	@Test
 	public void onMessageEnd() {
 		ClientHandler clientHandler = new ClientHandler();
 		Room room = new Room();
 		User user1 = new User();
 		User user2 = new User();
 		room.addUser(user1);
 		room.addUser(user2);
 
 		ClientHandler clientHandler2 = mock(ClientHandler.class);
 		clientHandler.onMessageEnd(user1);
 
 		verify(clientHandler2).sendMessageEnd(user2);
 	}
 
 	@Test
 	public void onDisconnect() {
 		User user = new User();
 
 		ClientHandler clientHandler = new ClientHandler();
 		clientHandler.onConnect(user, "some user information");
 
 		assertEquals(Beeper.getInstance().getUsers().size(), 1);
 		
 		clientHandler.onDisconnect(user);
 		assertEquals(Beeper.getInstance().getUsers().size(), 0);
 	}
 
 	@Test
 	public void sendMessageBegin() {
 		
 	}
 
 	@Test
 	public void sendMessage() {
 	}
 
 	@Test
 	public void sendMessageEnd() {
 	}
 
 	@Test
 	public void sendChangeRoom() {
 	}
 
 }
