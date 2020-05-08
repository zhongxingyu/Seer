 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 
 public class ProcessManager {
 	
 	private Queue processes;
 	private int numProcesses;
 	private boolean isMaster = true;
 	
 	public String migrate() throws IOException {
 		MigratableProcess pToMigrate = processes.dequeue();
 		pToMigrate.suspend();
 		String fileName = "hmmKay";
         FileOutputStream fileOut = new FileOutputStream(fileName);
         ObjectOutputStream out = new ObjectOutputStream(fileOut);
         out.writeObject(pToMigrate);
         out.close();
         fileOut.close();
         return fileName;
 	}
 	
 	public void runProcess(String command, String[] args) {
 		Class<?> processClass = null;
 		Constructor<?> processCtr = null;
 		Thread t = null;
 		
 		try {
 			processClass = Class.forName(command);
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		
 		try {
 			processCtr = processClass.getConstructor();
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			e.printStackTrace();
 		}
 		
 		try {
 			t = new Thread((MigratableProcess) processCtr.newInstance(args));
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		}	
 	}
 	
 	public void receiveCommands() throws IOException {
		byte[] buffer = new byte[256];
		char[] string = new char[256];
 		
 		while(true) {
			buffer = new byte[256];
 			System.out.print("==> ");
			System.in.read(buffer);
			for (int i = 0; i < buffer.length; i++) {
				string[i] = (char)buffer[i];
			}
			System.out.print("\n"+ String.valueOf(string) + "\n");
			parseCommand(buffer.toString());
 		}
 	}
 	
 	public void parseCommand(String command) {
 		String[] words = command.split(" ");
 		String com = words[0];
 		String[] args = new String[words.length - 1];
 		
 		for (int i = 1; i < words.length; i++) {
 			args[i-1] = words[i];
 		}
 		
		if (com == "-c" && args.length == 1) {
 			
		} else if (com == "ps" && words.length == 1) {
			
		} else if (com == "quit" && words.length == 1) {
 			
 		} else {
 			//runProcess(com, args);
 		}
 	}
 	
 	public void addProcess(String newProcess) {
 	}
 }
