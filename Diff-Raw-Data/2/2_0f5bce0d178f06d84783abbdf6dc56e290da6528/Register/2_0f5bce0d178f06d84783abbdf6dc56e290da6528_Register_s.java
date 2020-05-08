 package org.vfsutils.shell.commands;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.ListIterator;
 
 import org.apache.commons.vfs.FileName;
 import org.apache.commons.vfs.FileObject;
 import org.apache.commons.vfs.FileSystemException;
 import org.apache.commons.vfs.FileUtil;
 import org.vfsutils.shell.Arguments;
 import org.vfsutils.shell.CommandException;
 import org.vfsutils.shell.CommandInfo;
 import org.vfsutils.shell.CommandProvider;
 import org.vfsutils.shell.Engine;
 import org.vfsutils.shell.Arguments.Argument;
 import org.vfsutils.shell.Arguments.Flag;
 import org.vfsutils.shell.Arguments.Option;
 
 public class Register extends AbstractCommand {
 
 	/**
 	 * Inner class to store reference to a script. When executed the script will
 	 * be read.
 	 * 
 	 */
 	protected class Script extends AbstractCommand {
 
 		private String type = "vfs";
 		private String path;
 
 		public Script(String cmd, String description, String usage,
 				String type, FileObject file) {
 			super(cmd, description, usage);
 			this.type = type;
 			this.path = file.getName().toString();
 		}
 
 		public void execute(Arguments args, Engine engine)
 				throws IllegalArgumentException, CommandException,
 				FileSystemException {
 
 			Arguments largs = copyArgs(this, args);
 			engine.handleCommand(largs);
 
 		}
 
 		protected String getType() {
 			return this.type;
 		}
 
 	}
 
 	protected class CachedScript extends Script {
 
 		private byte[] buffer = new byte[0];
 
 		public CachedScript(String cmd, String description, String usage,
 				String type, FileObject file) throws IOException {
 			super(cmd, description, usage, type, file);
 			setContent(file);
 		}
 
 		private void setContent(FileObject file) throws IOException {
 			this.buffer = FileUtil.getContent(file);
 		}
 
 		public void execute(Arguments args, Engine engine)
 				throws IllegalArgumentException, CommandException,
 				FileSystemException {
 
 			executeCachedScript(this.getType(), this.buffer, args, engine);
 		}
 
 	}
 
 	private String vfsCommand = "load";
 	private String bshCommand = "bsh";
 
 	public Register() {
 		super(
 				"register",
 				new CommandInfo(
 						"Registers a command",
						"<path>|<class> [--type={class|vfs|bsh}] [--name=<name>] [--description=<descr>] [--usage=<usage>] [--unregister]"));
 	}
 
 	public void execute(Arguments args, Engine engine)
 			throws IllegalArgumentException, CommandException,
 			FileSystemException {
 
 		args.assertSize(1);
 
 		String type = args.getOption("type");
 
 		String target = args.getArgument(0);
 		String name = args.getOption("name");
 		String description = args.getOption("description");
 		String usage = args.getOption("usage");
 		boolean cache = args.hasFlag("cache");
 
 		if ((type != null && type.equals("class"))
 				|| (type == null && isClassName(target))) {
 			registerClass(target, name, description, usage, engine);
 		} else {
 			FileObject[] files = engine.pathToFiles(target);
 
 			if (files.length == 0) {
 				engine.println("No files selected");
 			} else if (files.length == 1) {
 				registerScript(files[0], name, description, usage, type, cache,
 						engine);
 			} else {
 				registerScripts(files, type, cache, engine);
 			}
 		}
 	}
 
 	protected void registerScripts(FileObject[] files, String type,
 			boolean cache, Engine engine) throws CommandException {
 		for (int i = 0; i < files.length; i++) {
 			registerScript(files[i], type, cache, engine);
 		}
 	}
 
 	protected void registerScript(FileObject file, String type, boolean cache,
 			Engine engine) throws CommandException {
 		registerScript(file, null, null, null, type, cache, engine);
 	}
 
 	protected void registerScript(FileObject file, String name,
 			String description, String usage, String type, boolean cache,
 			Engine engine) throws CommandException {
 
 		FileName fileName = file.getName();
 
 		String extension = fileName.getExtension();
 
 		if (name == null) {
 			String baseName = fileName.getBaseName();
 			name = (extension.length() == 0 ? baseName : baseName.substring(0,
 					baseName.length() - extension.length() - 1));
 		}
 
 		if (description == null) {
 			description = "script " + fileName.toString();
 		}
 		if (usage == null) {
 			usage = "";
 		}
 
 		if (type == null && extension.equals("bsh")) {
 			type = "bsh";
 		} else if (type == null){
 			// the default
 			type = "vfs";
 		}
 
 		doRegisterScript(file, name, description, usage, type, cache, engine);
 	}
 
 	protected void doRegisterScript(FileObject file, String name,
 			String description, String usage, String type, boolean cache,
 			Engine engine) throws CommandException {
 
 		Script script;
 		if (cache) {
 			try {
 				script = new CachedScript(name, description, usage, type, file);
 			} catch (IOException e) {
 				throw new CommandException(e);
 			}
 		} else {
 			script = new Script(name, description, usage, type, file);
 		}
 
 		script.register(engine.getCommandRegistry());
 		engine.println("Registered " + type + " script " + file.getName().toString()
 				+ " as " + name);
 
 	}
 
 	public void registerClass(String className, String name,
 			String description, String usage, Engine engine)
 			throws CommandException {
 		try {
 			Class commandClass = Class.forName(className);
 			if (CommandProvider.class.isAssignableFrom(commandClass)) {
 				CommandProvider command = (CommandProvider) commandClass
 						.newInstance();
 
 				if (command instanceof AbstractCommand) {
 
 					AbstractCommand abstractCommand = (AbstractCommand) command;
 					if (name != null) {
 						abstractCommand.setCommand(name);
 					}
 
 					if (description != null) {
 						abstractCommand.setDescription(description);
 					}
 
 					if (usage != null) {
 						abstractCommand.setUsage(usage);
 					}
 				}
 
 				command.register(engine.getCommandRegistry());
 				engine.println("Registered class " + className + " as "
 						+ command.getCommand());
 
 			} else {
 				throw new CommandException("Class " + className
 						+ " is not a valid Command");
 			}
 		} catch (CommandException e) {
 			throw e;
 		} catch (Exception e) {
 			throw new CommandException("Error while registering class "
 					+ className, e);
 		}
 	}
 
 	protected Arguments copyArgs(Script script, Arguments args) {
 		Arguments result = new Arguments();
 
 		if (script.type.equals("bsh")) {
 			result.setCmd(this.bshCommand);
 		} else {
 			result.setCmd(this.vfsCommand);
 		}
 
 		result.addArgument(script.path);
 
 		ListIterator argsIterator = args.getArguments().listIterator();
 		while (argsIterator.hasNext()) {
 			Argument arg = (Argument) argsIterator.next();
 			result.addArgument(arg);
 		}
 
 		Iterator flagIterator = args.getFlags().iterator();
 		while (flagIterator.hasNext()) {
 			Flag flag = (Flag) flagIterator.next();
 			result.addFlag(flag);
 		}
 
 		Iterator optionIterator = args.getOptions().keySet().iterator();
 		while (optionIterator.hasNext()) {
 			String key = (String) optionIterator.next();
 			if (key.equals("flags")) {
 				Option option = (Option) args.getOptions().get(key);
 				char[] flags = option.getValue().toCharArray();
 				// each flag should be added individually
 				for (int i = 0; i < flags.length; i++) {
 					result.addFlag(String.valueOf(flags[i]));
 				}
 			} else {
 				Option option = (Option) args.getOptions().get(key);
 				result.addOption(option);
 			}
 		}
 
 		return result;
 	}
 
 	protected boolean isClassName(String input) {
 
 		// test for slashes indicating it is a filename
 		if (input.indexOf("/") > -1)
 			return false;
 
 		// test for wildcards
 		if (input.indexOf("*") > -1)
 			return false;
 
 		// more expensive: try to find class
 		try {
 			Class.forName(input);
 		} catch (ClassNotFoundException e) {
 			return false;
 		}
 
 		// is classname
 		return true;
 
 	}
 
 	public void setBshCommand(String cmd) {
 		this.bshCommand = cmd;
 	}
 
 	public void setVfsCommand(String cmd) {
 		this.vfsCommand = cmd;
 	}
 
 	protected void executeCachedScript(String type, byte[] buffer,
 			Arguments args, Engine engine) throws CommandException,
 			FileSystemException {
 		
 		CommandProvider cpr = this.getCommand(type, engine);
 
 		if ("bsh".equals(type)) {
 			Bsh bsh = (Bsh) cpr;
 			bsh.bsh(new ByteArrayInputStream(buffer), args, engine);
 		} else {
 			Load load = (Load) cpr;
 			load.load(new ByteArrayInputStream(buffer), null, args, engine, true, false);
 		}
 	}
 	
 	private CommandProvider getCommand(String type, Engine engine) {
 
 		String cmd = ("bsh".equals(type) ? bshCommand : vfsCommand);
 
 		CommandProvider cpr = engine.getCommandRegistry().getCommand(cmd);
 		if (cpr == null) {
 			if ("bsh".equals(type)) {
 				cpr = new Bsh();
 			} else {
 				cpr = new Load();
 			}
 		}
 		return cpr;
 	}
 
 }
