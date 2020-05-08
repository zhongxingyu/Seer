 package com.tnes;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.lang.reflect.Method;
 import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
import java.util.List;
 import java.util.Map;
 
 /*
  * Singleton class, use 'getInstance(boolean, logFileName)'
  */
 public class Debugger {
 	private boolean debugging = false;
 	private File logFile;
 	private Map<String, DebuggerCommand> commands = new HashMap<String, DebuggerCommand>();
 	private static Debugger debugger;
 
 	public static String LOG_FILE_NAME = "tnes.log";
 	public static String PARAM_DELIM = ",";
 
 	private Debugger(boolean debugging, String logFileName) throws IOException {
 		this.debugging = debugging;
 		this.commands = new HashMap<String, DebuggerCommand>();
 		this.logFile = new File(logFileName);
 		if (this.logFile.exists()) {
 			SimpleDateFormat formatter = new SimpleDateFormat("_hhmmss_MMddyyyy");
 			this.logFile.renameTo(new File(logFileName + formatter.format(new Date())));
 		}
 		this.logFile.createNewFile();
 	}
 
 	public static Debugger getInstance() {
 		if (debugger == null) {
 			try {
 				debugger = new Debugger(false, Debugger.LOG_FILE_NAME);
 			} catch (IOException e) {
 				System.out.println(e.getMessage());
 				debugger = null;
 			}
 		}
 
 		return debugger;
 	}
 
 	public boolean isDebugging() {
 		return debugging;
 	}
 
 	public void setDebugging(boolean debugging) {
 		this.debugging = debugging;
 	}
 
 	public void enableDebugging() {
 		this.debugging = true;
 	}
 
 	public void disableDebugging() {
 		this.debugging = false;
 	}
 
 	public void debugPrint(String text) {
 		if (debugging) {
			System.out.print(text);
 		}
 	}
 
 	public void debugLog(String text) {
 		try {
 			OutputStream outStream = new FileOutputStream(logFile, true);
 			Writer writer = new OutputStreamWriter(outStream);
 			writer.write(text);
 			writer.close();
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	/*
 	 * Add a command to the list of available commands.
 	 */
 	public void addCommand(String name, Method method, Object contextObject) {
 		commands.put(name, new DebuggerCommand(method, contextObject));
 	}
 
 	public boolean readCommand() {
 		// Variable indicating whether a command was received
 		boolean result = false;
 		if (!debugging)
 			return result;
 
		System.out.print("\nCommand>>");
 		InputStreamReader converter = new InputStreamReader(System.in);
 		BufferedReader reader = new BufferedReader(converter);
 		try {
 			String commandLine = reader.readLine();
 			if ("?".equals(commandLine)) {
 				System.out.println("Commands: ");
 				StringBuffer buffer = new StringBuffer("");
				List<String> keys = new ArrayList<String>(commands.keySet());
				Collections.sort(keys);
				for (String key : keys) {
 					buffer.append(key).append("\n");
 				}
 				System.out.println(buffer.toString());
 				result = true;
 			} else if (commandLine != null && !commandLine.isEmpty()) {
 				String[] commandPieces = commandLine.split(" ");
 				String command = commandPieces[0];
 				String param = (commandPieces.length > 1) ? commandPieces[1] : "";
 				execCommand(command, param);
 				result = true;
 			}
 		} catch (IOException e) {
 			debugPrint("Error reading from input.");
 		}
 
 		return result;
 	}
 
 	public void readCommands() {
 		boolean commandReceived = true;
 		while (commandReceived) {
 			commandReceived = readCommand();
 		}
 	}
 
 	/*
 	 * Execute a named command.
 	 */
 	public void execCommand(String name, String param) {
 		DebuggerCommand command = commands.get(name);
 		Object contextObject = command.getContextObject();
 		Method method = command.getMethod();
 
 		try {
 			method.invoke(contextObject, param);
 		} catch (Exception e) {
 			debugPrint(e.getMessage());
 		}
 	}
 
 	/*
 	 * Static utility methods
 	 */
 	public static String intToHex(int value) {
 		return String.format("%x", value);
 	}
 
 	public static String shortToHex(short value) {
 		return String.format("%x", value);
 	}
 
 	public static int hexToInt(String value) {
 		return Integer.parseInt(value, 16);
 	}
 
 	public static short hexToByte(String value) {
 		return Byte.parseByte(value, 16);
 	}
 
 	/*
 	 * Debugger command object. Methods should receive one parameter... 1)
 	 * <param> a String, which may be delimited by Debugger.PARAM_DELIM
 	 */
 	public class DebuggerCommand {
 		private Method method;
 		private Object contextObject;
 
 		public DebuggerCommand(Method method, Object contextObject) {
 			this.method = method;
 			this.contextObject = contextObject;
 		}
 
 		public Method getMethod() {
 			return method;
 		}
 
 		public Object getContextObject() {
 			return contextObject;
 		}
 	}
 }
