 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 
 
 
 public class ProcessManager {
 	
 	private Queue processes;
 	private int numProcesses;
 	
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
 	
 	public void addProcess(String newProcess) {
 		processes.enqueue(newProcess);
 	}
 }
