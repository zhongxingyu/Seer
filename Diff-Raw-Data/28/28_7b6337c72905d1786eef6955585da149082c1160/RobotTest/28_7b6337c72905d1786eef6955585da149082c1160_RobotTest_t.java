 package ca.mitmaro.RoboTim.robot;
 
 import java.io.IOException;
 
 import org.mockito.Mockito;
 import org.testng.annotations.Test;
 
 import ca.mitmaro.RoboTim.irc.command.exception.InvalidCommandException;
 import ca.mitmaro.RoboTim.network.Connection;
 import ca.mitmaro.RoboTim.robot.command.Command;
 import ca.mitmaro.RoboTim.robot.command.exception.InvalidCommand;
 import ca.mitmaro.RoboTim.robot.responder.ResponderHandler;
 import ca.mitmaro.RoboTim.robot.responder.exception.InvalidResponse;
 
 public class RobotTest {
 
 	@Test
	public void handle() throws InvalidResponse, InvalidCommandException {
 		
 		Connection c = Mockito.mock(Connection.class);
 		ResponderHandler rh = Mockito.mock(ResponderHandler.class);
 		ca.mitmaro.RoboTim.irc.command.Command cmd
 			= Mockito.mock(ca.mitmaro.RoboTim.irc.command.Command.class);
 		Robot r = new Robot(c);
 		
 		r.registerResponder(rh);
 		
		r.handle(cmd);
		
 		Mockito.verify(rh).handle(r, cmd);
 	}
 	
 	@Test(expectedExceptions = {InvalidCommandException.class})
	public void handle_invalid() throws InvalidCommandException, InvalidResponse {
 		Connection c = Mockito.mock(Connection.class);
 		ResponderHandler rh = Mockito.mock(ResponderHandler.class);
 		ca.mitmaro.RoboTim.irc.command.Command cmd
 			= Mockito.mock(ca.mitmaro.RoboTim.irc.command.Command.class);
 		Robot r = new Robot(c);
 		
		Mockito.doThrow(new InvalidResponse("message")).when(rh).handle(r, cmd);
 		
 		r.registerResponder(rh);
 		
		r.handle(cmd);

 	}
 
 	@Test
 	public void sendMessage_string() throws IOException {
 		Connection c = Mockito.mock(Connection.class);
 		Robot r = new Robot(c);
 		
 		r.sendMessage("message");
 		
		Mockito.verify(c).sendMessage("message\r\n");
 	}
 
 	@Test
 	public void sendMessage_array() throws IOException {
 		Connection c = Mockito.mock(Connection.class);
 		Robot r = new Robot(c);
 		
 		r.sendMessage(new String[] {"message1", "message2", "message3"});
 
		Mockito.verify(c).sendMessage("message1\r\n");
		Mockito.verify(c).sendMessage("message2\r\n");
		Mockito.verify(c).sendMessage("message3\r\n");
 	}
 
 	@Test
 	public void shutdown() throws InvalidCommand {
 		Connection c = Mockito.mock(Connection.class);
 		Robot r = new Robot(c);
 		Command cmd = Mockito.mock(Command.class);
		r.registerShutdownCommand(cmd);
 		
 		r.shutdown();
 		
 		Mockito.verify(cmd).run(r);
 		
 		
 	}
 
 	@Test
 	public void startup() throws InvalidCommand {
 		Connection c = Mockito.mock(Connection.class);
 		Robot r = new Robot(c);
 		Command cmd = Mockito.mock(Command.class);
 		r.registerStartupCommand(cmd);
 		
 		r.startup();
 		
 		Mockito.verify(cmd).run(r);
 		
 	}
 }
