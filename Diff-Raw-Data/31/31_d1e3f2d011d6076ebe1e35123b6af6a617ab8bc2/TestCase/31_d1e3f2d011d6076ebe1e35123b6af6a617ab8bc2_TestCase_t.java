 package jcolin.commands.testcase;
 
 import java.util.Arrays;
 import java.util.Collection;
 
 import jcolin.commands.Command;
 import jcolin.commands.Container;
 
 public class TestCase extends Container {
 	private static final String[] COMMAND_NAMES = { "testcase" };
 
 	static private Command[] m_options = {
 		new Run(),
 		new RunAll(),
 		new List(),
 	};
 
 	public TestCase() { super(COMMAND_NAMES); }
 	public TestCase(Command command) { super(COMMAND_NAMES, command); }
 
 	public Collection<Command> getSubCommands() { return Arrays.asList(m_options); }
	protected String getDescription() { return "Testcase related commands"; }
 	protected Command clone(Command command) { return new TestCase(command); }
 }
