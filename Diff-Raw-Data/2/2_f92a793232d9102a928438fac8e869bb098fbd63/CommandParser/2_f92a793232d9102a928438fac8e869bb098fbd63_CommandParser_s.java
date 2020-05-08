 package source;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class CommandParser
 {		
 	private List<Command> 	_commands;
 	private Command			_currentCmd;
 	private Command			_parent;
 	private LineNumber		_line;
 	private TheoremSet		_theorems;
 
 	public CommandParser(TheoremSet theorems)
 	{
 		_commands			= new ArrayList<Command>();
 		_currentCmd			= null;
 		_parent				= null;
 		_line				= new LineNumber(1);
 		_theorems			= theorems;
 	}
 
 	public Command parse(String line, LineNumber lineNumber) throws IllegalLineException
 	{
 		line = skipSpaces(line);
 		String command = parseCommand(line);
 		line = line.substring(command.length()); 
 		line = skipSpaces(line);
 		Command cmd = null;
 		if (command.equals("show") || command.equals("assume"))
 			cmd = this.parseNoArgCommand(line, command, lineNumber);
 		else if (command.equals("print"))
 		{
 			dumpTree(_commands);
 			return new DummyCommand(null, null, null);
 		}
 		else if (command.equals("ic") || command.equals("repeat"))
 			cmd = this.parseOneArgCommand(line, command, lineNumber);
 		else if (command.equals("mp") || command.equals("mt") || command.equals("mc"))
 			cmd = this.parseTwoArgsCommand(line, command, lineNumber);
 		else
			cmd = this.parseOneArgCommand(line, command, lineNumber);
 		if (cmd.getParent() == null)
 			cmd.setParent(_commands.size() != 0 ? _commands.get(0) : cmd);
 		return cmd;
 	}
 
 	private void dumpTree(List<Command> _commands)
 	{
 		for (Command c : _commands)
 		{
 			System.out.print(c.getLineNumber().toString() + "\t" + c.toString() + " ");
 			for (String arg : c.getArgs())
 				System.out.print(arg + " ");
 			System.out.println(c.getExpr().toString());
 			if (c.subcommands() != null)
 				dumpTree(c.subcommands());
 		}
 
 	}
 
 	public LineNumber nextLineNumber()
 	{
 		return _line;
 	}
 
 	public void updateLine(Command cmd, LineNumber lineNumber) throws IllegalArgumentException
 	{
 		if (cmd.toString() == "dummy")
 			return; // skip dummy command
 		_currentCmd = cmd;
 		try
 		{
 			insertCommand(_commands, cmd, lineNumber);
 		}
 		catch (IllegalLineException e)
 		{
 			throw new IllegalArgumentException("lineNumber is incorrect.");
 		} 
 		_line = new LineNumber(_currentCmd.getLineNumber());
 		if (_currentCmd == null)
 			_parent = _currentCmd; 
 		if (_currentCmd.toString().equals("show") && _line.compare(new LineNumber(1)) != 0)
 		{
 			_parent = _currentCmd;
 			_line.add(1); // Branch
 		}
 		else
 		{
 			if (_currentCmd.isComplete() && _currentCmd.getParent() != null)
 			{
 				_parent = _parent.getParent();
 				_currentCmd = _currentCmd.getParent(); // Unbranch
 				_line = new LineNumber(_currentCmd.getLineNumber());
 			}
 			int size = _line.number().size() - 1;
 			Integer currentValue = _line.number().get(size);
 			_line.set(size, currentValue + 1);
 		}
 	}
 
 	public Command currentCommand()
 	{
 		return _currentCmd;
 	}
 
 	public List<Command> getCommandsTree()
 	{
 		return _commands;
 	}
 
 	public Command getCurrentCommand()
 	{
 		return _currentCmd;
 	}
 
 	private static String skipSpaces(String line)
 	{
 		for (int i = 0; i < line.length(); i++)
 		{
 			if (Character.isWhitespace(line.charAt(i)) == false)
 				return line.substring(i);
 		}
 		return "";
 	}
 
 	private static String parseCommand(String line)
 	{
 		for (int i = 0; i < line.length(); ++i)
 			if (!Character.isLetterOrDigit(line.charAt(i)))
 				return line.substring(0, i);
 		return line;
 	}
 
 	private Command parseNoArgCommand(String line, String command, LineNumber nb) throws IllegalLineException
 	{
 		Expression e = new Expression(line);
 		if (command.equals("show"))
 			return new ShowCommand(nb, e, _parent);
 		if (_currentCmd == null || _currentCmd.toString() != "show")
 			throw new IllegalLineException("Assume must be after a show statement");
 		return new AssumeCommand(nb, e, _parent);
 	}
 
 	private Command parseOneArgCommand(String line, String command, LineNumber nb) throws IllegalLineException
 	{
 		LineNumber ln = parseLineNumber(line);
 		line = line.substring(ln.length());
 		line = skipSpaces(line);
 		if (!isScopeAllowed(nb, ln))
 			throw new IllegalLineException("Scope error (can't access line `" + ln.toString() + "' from line `"+ nb.toString() +"')");
 		if (command.equals("ic"))
 		{
 			Expression e = new Expression(line);
 			return new ICCommand(nb, e, _parent, ln.toString());
 		}
 		else if (command.equals("repeat"))
 		{
 			return new RepeatCommand(nb, null, _parent, ln.toString());
 		}
 		else
 		{
 			Expression e = new Expression(line);
 			Expression theoExp = _theorems.get(command);
 			if(theoExp == null)
 				throw new IllegalLineException("Theorem " + command + " not found");
 			return new TheoremCommand(nb, theoExp, e, _parent);
 		}
 	}
 
 	private Command parseTwoArgsCommand(String line, String command, LineNumber nb) throws IllegalLineException
 	{
 		LineNumber ln1 = parseLineNumber(line);
 		line = line.substring(ln1.length());
 		if (!isScopeAllowed(nb, ln1))
 			throw new IllegalLineException("Scope error (can't access line `" + ln1.toString() + "' from line `"+ nb.toString() +"')");
 		line = skipSpaces(line);
 		LineNumber ln2 = parseLineNumber(line);
 		line = line.substring(ln2.length());
 		if (!isScopeAllowed(nb, ln2))
 			throw new IllegalLineException("Scope error (can't access line `" + ln2.toString() + "' from line `"+ nb.toString() +"')");
 		line = skipSpaces(line);
 		Expression e = new Expression(line);
 		if (command.equals("mp"))
 			return new MPCommand(nb, e, _parent, ln1.toString(), ln2.toString());
 		else if (command.equals("mt"))
 			return new MTCommand(nb, e, _parent, ln1.toString(), ln2.toString());
 		return new COCommand(nb, e, _parent, ln1.toString(), ln2.toString());
 	}
 
 	private LineNumber parseLineNumber(String line) throws IllegalLineException
 	{
 		if (line.length() == 0)
 			throw new IllegalLineException("Line doesn't contain any line number");
 		return new LineNumber(line);
 	}
 
 	private boolean isScopeAllowed(LineNumber cmd, LineNumber target)
 	{
 		boolean isOk = true;
 		for (int i = 0; isOk && i < cmd.number().size() && i < target.number().size(); ++i)
 		{
 			int lhs = cmd.number().get(i);
 			int rhs = target.number().get(i);
 			if (rhs > lhs)
 				return false;
 		}
 		return true;
 	}
 
 	private void insertCommand(List<Command> commands, Command cmd, LineNumber nb) throws IllegalLineException
 	{
 		for (int i = 0; i < nb.number().size() - 1; ++i)
 		{
 			Integer number = nb.number().get(i) - 1;
 			if (number > commands.size())
 				throw new IllegalLineException("Line: `" + nb.toString() + "' doesn't exist");
 			if (commands.get(number).subcommands() != null)
 				commands = commands.get(number).subcommands();
 		}
 		Integer number = nb.number().get(nb.number().size() - 1) - 1;
 		if (number < commands.size())
 			throw new IllegalLineException("Line already exists");
 		commands.add(cmd);
 	}
 
 	public Command findNode(List<Command> commands, LineNumber nb) throws IllegalLineException
 	{
 		for (int i = 0; i < nb.number().size() - 1; ++i)
 		{
 			Integer number = nb.number().get(i) - 1;
 			if (number > commands.size())
 				throw new IllegalLineException("Line: `" + nb.toString() + "' doesn't exist");
 			if (commands.get(number).subcommands() != null)
 				commands = commands.get(number).subcommands();
 		}
 		Integer number = nb.number().get(nb.number().size() - 1) - 1;
 		if (number >= commands.size())
 			throw new IllegalLineException("Line: `" + nb.toString() + "' doesn't exist");
 		return commands.get(number);
 	}
 }
