 package echode;
 
 import java.io.PrintStream;
 import java.lang.reflect.InvocationTargetException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Scanner;
 
 public class Echode {
 	static Scanner scan;
 	private static String in;
 	private static List<Class> loaded = new ArrayList<>();
 
 	/**
 	 * @param args
 	 * @throws ReflectiveOperationException 
 	 */
 	public static void main(String[] args) throws ReflectiveOperationException {
 		scan = new Scanner(System.in);
 		intro();
 	}
 
 	// welcome message
 	public static void intro() throws ReflectiveOperationException {
 		System.out.println("Welcome to ECHODE version 0.3\nLoading echode.programs...");
 		load();
 		mainLoop();
 	}
 
 	private static void load() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
 		Class c = Class.forName("echode.Test");
 		loaded.add(c);
 		
 	}
 
 	private static void mainLoop() throws ReflectiveOperationException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
 		while (true) {
 			System.out.print("-> ");
 			in = scan.nextLine();
 			parse(in);
 		}
 	}
 
 	private static void parse(String in2) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
 		/**
 		 * Commented this out, in case needed.
 		 * 
 		 * if (in2.equalsIgnoreCase("about")) { System.out.println(
 		 * "Echode version 0.2.2\nMade by Erik Konijn and Marks Polakovs"); }
 		 * else { if (in2.equalsIgnoreCase("kill")){
 		 * System.out.println("Echode shut down succesfully."); System.exit(0);
 		 * } else{ System.out.println("Not implemented yet."); } }
 		 **/
 		String[] result = in.split(" ", 2);
 		switch (result[0]) {
 		case "about":
 			System.out
 					.println("Echode version 0.3\nMade by Erik Konijn and Marks Polakovs");
 			break;
 		case "kill":
 			System.out.println("Echode shut down succesfully.");
 			System.exit(0);
 			break;
 		case "help":
 			System.out
 					.println("List of commands:\n"
 							+ "about ----------------------------------- Gives some info about ECHODE\n"
 							+ "help ---------------------------------------------- Lists all commands\n"
 							+ "kill ---------------------------------------- Quits the ECHODE console\n"
 							+ "version ------------------------ Outputs current Echode version number\n"
 							+ "time <all|date|digital> --------------------------------- Outputs time\n");
 			break;
 		case "version":
 			System.out.println("0.3");
 			break;
 		case "time":
 			try {
 				switch (result[1]) {
 				case "all":
 					String alltime = new SimpleDateFormat(
 							"yyyy-MM-dd HH:mm:ss").format(Calendar
 							.getInstance().getTime());
 					System.out.println(alltime);
 					break;
 				case "date":
 					String datetime = new SimpleDateFormat(
 							"yyyy-MM-dd").format(Calendar
 							.getInstance().getTime());
 					System.out.println(datetime);
 					break;
 				case "digital":
 					String digitaltime = new SimpleDateFormat(
 							"HH:mm:ss").format(Calendar
 							.getInstance().getTime());
 					System.out.println(digitaltime);
 					break;
 				default:
 					System.out.println("Usage: 'time <all|date|digital>'");
 					break;
 				}
 			} catch (ArrayIndexOutOfBoundsException e) {
 				System.out.println("Usage: 'time <all|date|digital>'");
 			}
 			break;
 		default:
 			for (Class c:loaded) {
 				if(c.getName().equals(result[0])) {
 					c.getMethod("run", PrintStream.class).invoke(Echode.class, System.out);
 				}
 			}
 			break;
 		}
 	}
 }
