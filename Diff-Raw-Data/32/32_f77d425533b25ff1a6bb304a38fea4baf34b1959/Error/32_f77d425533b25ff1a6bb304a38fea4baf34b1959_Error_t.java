 package org.vfsutils.shell.commands;
 
 import org.apache.commons.vfs.FileSystemException;
 import org.vfsutils.shell.Arguments;
 import org.vfsutils.shell.CommandException;
 import org.vfsutils.shell.CommandInfo;
 import org.vfsutils.shell.Engine;
 
 public class Error extends AbstractCommand {
 
 	public Error() {
		super("error", new CommandInfo("Info on last error", "[-p]"));
 	}

 	public void execute(Arguments args, Engine engine)
			throws IllegalArgumentException, CommandException,
			FileSystemException {

 		if (args.hasFlag('p')) {
 			printError(engine);
		} else {
 			lastError(engine);
 		}
 	}
 
 	public void lastError(Engine engine) {
		if (engine.getLastError() != null) {
			engine.println(engine.getLastError().getMessage());
		} else {
			engine.println("no error");
		}
 	}

 	public void printError(Engine engine) {
		if (engine.getLastError() != null) {
			engine.getLastError().printStackTrace(engine.getConsole().getOut());
		} else {
			engine.println("no error");
		}
 	}
 }
