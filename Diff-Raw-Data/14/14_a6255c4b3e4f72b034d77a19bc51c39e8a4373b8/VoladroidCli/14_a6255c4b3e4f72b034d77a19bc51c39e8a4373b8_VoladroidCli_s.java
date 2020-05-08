 package com.voladroid.ui.cli;
 
 import java.io.IOException;
 import java.util.Scanner;
 
 import org.jboss.jreadline.console.Console;
 
 import com.voladroid.service.Services;
 import com.voladroid.ui.ProjectInspector;
 import com.voladroid.ui.cli.args.Scope;
 import com.voladroid.ui.cli.args.Stack;
 
 public class VoladroidCli {
 
 	private Stack stack = Stack.getInstance();
 	private Scanner in = new Scanner(System.in);
 	private ProjectInspector inspector = new ProjectInspector();
 	private Console console = null;
 
 	private Scope root = new RootScope(this);
 
 	public VoladroidCli() throws IOException {
		 console = new Console();
 	}
 
 	public ProjectInspector inspector() {
 		return inspector;
 	}
 
 	public String in(String msg) throws IOException {
 		if (console != null) {
 			return console.read(msg);
 		} else {
 			System.out.print("dbg>> ");
 			return in.nextLine();
 		}
 	}
 
 	public void error(Exception e) {
 		Boolean debug = stack.local().get("debug");
 		if (debug != null && debug) {
 			System.out.print(Services.getStackTrace(e));
 		} else {
 			System.out.println(e);
 		}
 	}
 
 	public void error(String msg, Object... args) {
 		System.out.println(String.format(msg, args));
 	}
 
 	public void out(String msg, Object... args) {
 		System.out.println(String.format(msg, args));
 	}
 
 	public void out(Object o) {
 		System.out.println(o);
 	}
 
 	public void push(Scope e) {
 		if (e.getCompleters() != null && console != null)
 			console.addCompletions(e.getCompleters());
 		stack.push(e);
 	}
 
 	public void start() {
 		new Thread(inspector).start();
 		push(root);
 		while (true) {
 			try {
 				String[] cmd = in(">> ").split("\\s+");
 				Scope executor = stack.local().execute(cmd);
 				if (executor != null) {
 					out("Entering %s", executor.name());
 					push(executor);
 				} else if (stack.empty()) {
 					break;
 				}
 			} catch (Exception e) {
 				error(e);
 			}
 		}
 	}
 
 }
