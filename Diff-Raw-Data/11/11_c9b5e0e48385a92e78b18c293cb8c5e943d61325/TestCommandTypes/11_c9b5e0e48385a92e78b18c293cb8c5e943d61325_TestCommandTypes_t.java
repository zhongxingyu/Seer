 package org.zend.sdk.test.sdkcli;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNotSame;
 import static org.junit.Assert.assertSame;
 
 import org.junit.Test;
 import org.zend.sdkcli.CommandType;
 import org.zend.sdkcli.ParseError;
 import org.zend.sdkcli.internal.commands.CommandLine;
 
 public class TestCommandTypes {
 
 	@Test
 	public void testValidCommandType() throws ParseError {
 		CommandLine line = new CommandLine(new String[] { "create", "project" });
 		CommandType type = CommandType.byCommandLine(line);
 		assertNotNull(type);
		assertNotSame(CommandType.HELP, type);
 		assertSame("create", type.getVerb());
 		assertSame("project", type.getDirectObject());
 	}
 
 	@Test
 	public void testInvalidCommandType() throws ParseError {
 		CommandLine line = new CommandLine(new String[] { "random", "123" });
 		CommandType type = CommandType.byCommandLine(line);
 		assertNotNull(type);
		assertSame(CommandType.HELP, type);
 	}
 
 	@Test
 	public void testInvalidCommandType2() throws ParseError {
 		CommandLine line = new CommandLine(new String[] { "random" });
 		CommandType type = CommandType.byCommandLine(line);
 		assertNotNull(type);
		assertSame(CommandType.HELP, type);
 	}
 
 	@Test
 	public void testInvalidCommandType3() throws ParseError {
 		CommandLine line = new CommandLine(new String[] {});
 		CommandType type = CommandType.byCommandLine(line);
 		assertNotNull(type);
		assertSame(CommandType.HELP, type);
 	}
 
 }
