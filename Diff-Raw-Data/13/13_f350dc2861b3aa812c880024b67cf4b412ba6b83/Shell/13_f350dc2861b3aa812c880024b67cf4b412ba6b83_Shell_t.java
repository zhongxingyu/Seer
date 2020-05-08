 package org.jackie.tools;
 
 import org.jackie.utils.Assert;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Patrik Beno
  */
 public class Shell {
 
 	static public void main(String[] args) {
 
 		Option<File> sources = Option.create("sources", File.class);
 		Option<File> jarfile = Option.create("jarfile", File.class);
 
		try {
			CmdLineSupport cmdline = new CmdLineSupport(sources, jarfile);
			cmdline.parse(args);


		} catch (Throwable t) {
			System.out.println(t);
			System.exit(-1);
		}
 
 		Shell shell = new Shell(sources.get(), jarfile.get());
 		shell.run();
 	}
 
 	File sources;
 	File jarfile;
 
 	boolean quit;
 
 	List<String> commands = getCommands();
 
 	Shell(File sources, File jarfile) {
 		this.sources = sources;
 		this.jarfile = jarfile;
 	}
 
 	void run() {
 		System.out.println("(-: Welcome to Jackie Shell! :-)");
 		System.out.println();
 		System.out.println("Configuration:");
 		System.out.printf("\tsources=%s%n", sources);
 		System.out.printf("\tjarfile=%s%n", jarfile);
 		System.out.println();
 
 		try {
 			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 			while (!quit) {
 				System.out.print("jackie$> ");
 				String command = br.readLine();
 				execute(command);
 			}
 		} catch (IOException e) {
 			throw Assert.notYetHandled(e);
 		}
 	}
 
 	void execute(String command) {
 		if (command.length() == 0) {
 			help();
 			return;
 		}
 		try {
 			if (!commands.contains(command)) {
 				System.out.printf("Unrecognized command: \"%s\"%n", command);
 				help();
 				return;
 			}
 
 			Method m = getClass().getMethod(command);
 			m.invoke(this);
 			
 		} catch (NoSuchMethodException e) {
 			throw Assert.notYetHandled(e);
 		} catch (IllegalAccessException e) {
 			throw Assert.notYetHandled(e);
 		} catch (InvocationTargetException e) {
 			e.printStackTrace(System.out);
 		}
 	}
 
 	List<String> getCommands() {
 		List<String> commands = new ArrayList<String>();
 		for (Method m : getClass().getDeclaredMethods()) {
 			if (Modifier.isPublic(m.getModifiers()) && !Modifier.isStatic(m.getModifiers())) {
 				commands.add(m.getName());
 			}
 		}
 		return commands;
 	}
 
 
 	/// commands
 
 	public void help() {
 		System.out.printf("Commands: %s%n", getCommands());
 	}
 
 	public void quit() {
 		System.out.println("Good bye!");
 		quit = true;
 	}
 
 	Main main;
 
 	public void compile() {
 		long started = System.currentTimeMillis();
 		System.out.printf("Compiling %s to %s%n", sources, jarfile);
 
 		if (main == null) {
 			main = new Main(sources, jarfile);
 		}
 		main.run();
 
 		long elapsed = System.currentTimeMillis() - started;
 		System.out.printf("Compilation finished in %s ms%n", elapsed);
 	}
 }
