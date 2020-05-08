 package sat.cli;
 
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Scanner;
 import java.util.Vector;
 
 /**
  * The SAT command line framework
  */
 public abstract class CLI implements Runnable {
 	protected Scanner in;
 	protected PrintStream out;
 	
 	private String prompt;
 	private String prompt_default;
 	
 	private HashMap<String, Method> api = new HashMap<String, Method>();
 	
 	private boolean exit = false;
 	
 	public CLI(InputStream in, PrintStream out, String prompt) {
 		this.in = new Scanner(in);
 		this.out = out;
 		
 		this.prompt = this.prompt_default = prompt;
 		
 		buildAPI(this.getClass());
 	}
 	
 	public CLI(InputStream in, PrintStream out) {
 		this(in, out, "> ");
 	}
 	
 	private void buildAPI(Class<?> root) {
 		Method[] methods = root.getDeclaredMethods();
 		
 		for(Method method : methods) {
 			if(!api.containsKey(method.getName()))
 				api.put(method.getName(), method);
 		}
 		
 		Class<?> superclass = root.getSuperclass();
 		
 		if(superclass != null && superclass != CLI.class && superclass != Object.class)
 			buildAPI(superclass);
 	}
 
 	public void run() {
 		while(!exit) {
 			out.print(prompt);
 			
 			if(!in.hasNextLine()) break;
 			
 			String line = in.nextLine();
 			eval(line);
 		}
 	}
 	
 	public Thread runInNewThread() {
 		Thread thread = new Thread(this);
 		thread.start();
 		return thread;
 	}
 	
 	private String[] split(String line) {
 		Vector<String> parts = new Vector<String>();
 		StringBuffer buffer = new StringBuffer();
 		
 		boolean string_mode = false;
 		boolean escape_mode = false;
 		
 		for(int i = 0; i < line.length(); i++) {
 			char c = line.charAt(i);
 			
 			if(escape_mode) {
 				buffer.append(c);
 				escape_mode = false;
 				continue;
 			}
 			
 			switch(c) {
 				case '\\':
 					escape_mode = true;
 					break;
 					
 				case '"':
 					string_mode = !string_mode;
 					break;
 					
 				case ' ':
 				case '\t':
 					if(!string_mode) {
 						if(buffer.length() > 0) {
 							parts.add(buffer.toString());
 							buffer.setLength(0);
 						}
 						
 						break;
 					}
 					
 				default:
 					buffer.append(c);
 			}
 		}
 		
 		if(buffer.length() > 0) {
 			parts.add(buffer.toString());
 		}
 		
 		return parts.toArray(new String[parts.size()]);
 	}
 	
 	private void eval(String line) {
 		String[] parts = split(line);
 		
 		if(parts.length == 0) {
 			// Input is empty
 			return;
 		}
 		
 		int args_counts = parts.length-1; // Ignore command
 		
 		String cmd = parts[0];
 		
 		if(!api.containsKey(cmd)) {
 			out.println("Unknow command: " + cmd);
 			return;
 		}
 		
 		Method method = api.get(cmd);
 		
 		int args_required = method.getParameterTypes().length;
 		
 		String[] args = new String[args_required];
 		for(int i = 0; i < args_required; i++) {
 			if(i >= args_counts) {
 				args[i] = "";
 			} else {
 				args[i] = parts[i+1];
 			}
 		}
 		
 		try {
 			method.invoke(this, (Object[]) args);
 		} catch(InvocationTargetException e) {
 			// The call-stack for InvocationTargetException is always the
 			// same. So, ignore it and print TargetException.
 			e.getTargetException().printStackTrace(out);
 		} catch(Exception e) {
 			e.printStackTrace(out);
 		}
 	}
 	
 	public String prompt() {
 		return prompt;
 	}
 	
 	public void setPrompt(String newPrompt) {
 		prompt = newPrompt;
 	}
 	
 	public void restorePrompt(String newPrompt) {
 		prompt = prompt_default;
 	}
 	
 	public void println(String x) {
 		out.println(x);
 	}
 	
 	public void exit() {
 		exit = true;
 	}
 }
