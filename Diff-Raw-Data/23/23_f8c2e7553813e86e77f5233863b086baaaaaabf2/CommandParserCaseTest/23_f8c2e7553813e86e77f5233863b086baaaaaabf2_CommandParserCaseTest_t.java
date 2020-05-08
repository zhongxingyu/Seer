 package grouppractical.tests.client.commands;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import grouppractical.client.commands.ClientType;
 import grouppractical.client.commands.Command;
 import grouppractical.client.commands.CommandParser;
 import grouppractical.client.commands.ConnectCommand;
 import grouppractical.client.commands.MInitialiseCommand;
 import grouppractical.client.commands.MPositionCommand;
 import grouppractical.client.commands.RDistanceCommand;
 import grouppractical.client.commands.RLockCommand;
 import grouppractical.client.commands.RRotateCommand;
 import grouppractical.client.commands.RStatusCommand;
 import grouppractical.client.commands.RStopCommand;
 import grouppractical.client.commands.RUnlockCommand;
 import grouppractical.utils.map.Position;
 
 import java.util.Arrays;
 import java.util.Collection;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 
 @RunWith(Parameterized.class)
 public class CommandParserCaseTest {
 	@Parameters
 	public static Collection<Object[]> data() {
 		Object[][] data = new Object[][] {
 				//Test RDISTANCE(dist) command
 				{ "RDISTANCE(MIN)", new RDistanceCommand(RDistanceCommand.MIN) },
 				{ "RDISTANCE(0)", new RDistanceCommand((short) 0) },
 				{ "RDISTANCE(MAX)", new RDistanceCommand(RDistanceCommand.MAX) },
 				//Test RROTATE(rotate) command
 				{ "RROTATE(MIN)", new RRotateCommand(RRotateCommand.MIN_INT) },
 				{ "RROTATE(0)", new RRotateCommand((short) 0) },
 				{ "RROTATE(MAX)", new RRotateCommand(RRotateCommand.MAX_INT) },
 				//Test RSTOP() command
 				{ "RSTOP()", new RStopCommand() },
 				//Test MINITIALIZE() command
 				{ "MINITIALIZE()", new MInitialiseCommand() },
 				//Test RLOCK() command
 				{ "RLOCK()", new RLockCommand() },
 				//Test RLOCK() command
 				{ "RUNLOCK()", new RUnlockCommand() }
 		};
 		Object[][][] xss = new Object[][][] {
 				data,
 				//Test MPOSITION(position) command
 //				mPositionCommandData(),
 				//Test RSTATUS(position,voltage) command
				rStatusCommandData(),
 				//Test CONNECT(clientType) command
 				connectCommandData()
 		};
 		int size = 0;
 		for (Object[][] xs : xss)
 			size += xs.length;
 		Object[][] ys = new Object[size][];
 		int i = 0;
 		for (Object[][] xs : xss)
 			for (Object[] x : xs)
 				ys[i++] = x;
 		
 		return Arrays.asList(ys);
 	}
 	
 	protected static Object[][] mPositionCommandData() {
 		//Create lists of valid values to test
 		int[] xs = new int[] { MPositionCommand.MIN_X - 1, MPositionCommand.MIN_X,
 				(MPositionCommand.MIN_X + MPositionCommand.MAX_X) / 2, MPositionCommand.MAX_X,
 				MPositionCommand.MAX_X + 1 },
 			ys = new int[] { MPositionCommand.MIN_Y - 1, MPositionCommand.MIN_Y,
 				(MPositionCommand.MIN_Y + MPositionCommand.MAX_Y) / 2, MPositionCommand.MAX_Y,
 				MPositionCommand.MAX_Y + 1 };
 		short[] certs = new short[] { Position.MIN_CERTAINTY - 1, Position.MIN_CERTAINTY,
 				(Position.MIN_CERTAINTY + Position.MAX_CERTAINTY) / 2, Position.MAX_CERTAINTY };
 		boolean[] occupieds = new boolean[] { true, false };
 		
 		//Constructs list of (String,Command), which is the test name and command to test
 		Object[][] data = new Object[xs.length * ys.length * certs.length * 2][2];
 		
 		//Creates all commands
 		int i = 0;
 		for (int x : xs)
 			for (int y : ys)
 				for (short cert : certs)
 					for (boolean o : occupieds) {
 						Position p = new Position(x, y, o, cert);
 						MPositionCommand cmd = new MPositionCommand(p);
 						data[i][0] = cmd.toString();
 						data[i++][1] = cmd;
 					}
 		return data;
 	}
 	protected static Object[][] rStatusCommandData() {
 		//Create lists of valid values to test
 		int[] xs = new int[] { MPositionCommand.MIN_X - 1, MPositionCommand.MIN_X,
 				(MPositionCommand.MIN_X + MPositionCommand.MAX_X) / 2, MPositionCommand.MAX_X,
 				MPositionCommand.MAX_X + 1 },
 			ys = new int[] { MPositionCommand.MIN_Y - 1, MPositionCommand.MIN_Y,
 				(MPositionCommand.MIN_Y + MPositionCommand.MAX_Y) / 2, MPositionCommand.MAX_Y,
 				MPositionCommand.MAX_Y + 1 };
 		float[] volts = new float[] { RStatusCommand.MIN_BATT - 1, RStatusCommand.MIN_BATT,
 				(RStatusCommand.MIN_BATT + RStatusCommand.MAX_BATT) / 2, RStatusCommand.MAX_BATT,
 				RStatusCommand.MAX_BATT + 1};
 		short[] angles = new short[] { RStatusCommand.MIN_INT - 1, RStatusCommand.MIN_INT,
 				(RStatusCommand.MIN_INT + RStatusCommand.MAX_INT) / 2, RStatusCommand.MAX_INT,
 				(short) (RStatusCommand.MAX_INT + 1)};
 		
 		//Constructs list of (String,Command), which is the test name and command to test
 		Object[][] data = new Object[xs.length * ys.length * volts.length * angles.length][2];
 		
 		//Creates all commands
 		int i = 0;
 		for (int x : xs)
 			for (int y : ys)
 				for (float volt : volts) 
 					for (short angle : angles) {
 						Position p = new Position(x, y, false, (short) Position.MAX_CERTAINTY);
 						RStatusCommand cmd = new RStatusCommand(p,volt,angle);
 						data[i][0] = cmd.toString();
 						data[i++][1] = cmd;
 					}
 		return data;
 	}
 	public static Object[][] connectCommandData() {
 		ClientType[] clientTypes = ClientType.values();
 		
 		//Constructs list of (Command,Bool), which is the position and whether the position is valid
 		Object[][] data = new Object[clientTypes.length][2];
 		
 		//Creates all positions
 		int i = 0;
 		for (ClientType clientType : clientTypes) {
 			ConnectCommand cmd = new ConnectCommand(clientType);
 			data[i][0] = cmd.toString();
 			data[i++][1] = cmd;
 		}
 		return data;
 	}
 	protected final Command cmd;
 	protected final String testname;
 	
 	public CommandParserCaseTest(String testname, Command cmd) {
 		this.cmd = cmd;
 		this.testname = testname;
 	}
 	
 	@Test
 	public void testParse() throws InterruptedException {
 		char[] serialized = cmd.serialize();
 		assertNotNull(testname + " - serialize method is invalid");
 		
 		CommandParser cmdparse = new CommandParser();
 		cmdparse.enqueue(serialized);
 		
 		//Check that exactly all of the characters were parsed
 		assertEquals(testname + " - parse was incomplete",1,cmdparse.outputSize());
 		assertEquals(testname + " - parse had leftover input",0,cmdparse.inputSize());
 		
 		Command pcmd = cmdparse.dequeue();
 		
 		assertNotNull(testname + " - parsed command is null",pcmd);
 		assertEquals(testname + " - parsed command is not equal to original",cmd,pcmd);
 	}
 }
