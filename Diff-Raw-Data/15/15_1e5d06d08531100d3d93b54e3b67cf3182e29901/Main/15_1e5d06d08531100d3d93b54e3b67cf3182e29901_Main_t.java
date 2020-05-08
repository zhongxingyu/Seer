 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.zip.GZIPInputStream;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Scanner;
 import java.util.Set;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class Main {
 
 	public static void main(final String[] args) {
 
 		ErrorLogger logger = new ErrorLogger();
 		Scanner inputScanner = null;
 
 		try {
 			InputStream fileStream = new FileInputStream(args[0]);
 			InputStream gzipStream = new GZIPInputStream(fileStream);
 			inputScanner = new Scanner(gzipStream);
 		}
 
 		catch (IOException io) {
 			System.out.println("IOException");
 			System.exit(0);
 		}
 
 		Set notBornObjects = new HashSet();
 		Set deadObjects = new HashSet();
 		Set multipleObjects = new HashSet();
 		HashMap<String, ObjectEventRecord> hash = new HashMap();
 		long startOfProcess = System.currentTimeMillis();
 		int numberOfIllegalAccesses = 0;
 		int multipleCreations = 0;
 		int notBornErrors = 0;
 		int deadErrors = 0;
 
 		while (inputScanner.hasNextLine()) {
 
 			String nextLine = inputScanner.nextLine();
 
 			System.out.println(nextLine + " nextline");
 
 			Event event = new Event(nextLine);
 			String currentEventID = event.getID();
 			String currentEventStatus = event.getStatus();
 
 			boolean existsInHashtable = hash.get(event.getID()) != null;
 
 			// free and status "A"
 			if (!existsInHashtable && currentEventStatus.equalsIgnoreCase("A")) {
 
 				ObjectEventRecord record = new ObjectEventRecord(event);
 				hash.put(currentEventID, record);
 				System.out.println("Object " + currentEventID
 						+ " added to the records for the first time");
 				System.out.println();
 				System.out.println();
 			}
 
 			// free but not status "A"
 			else if (!existsInHashtable
 					&& !currentEventStatus.equalsIgnoreCase("A")) {
 
 				Error notBornError = new Error(
 						"The object is not born so you cannot update it!");
 				logger.getLogger().log(
 						Level.SEVERE,
 						"The object with id " + currentEventID
 								+ " is not born so you cannot update it!");
 				numberOfIllegalAccesses++;
 				notBornErrors++;
 				notBornObjects.add(currentEventID);
 
 				System.out.println();
 				System.out.println();
 			}
 
 			// occupied and alive
 			else if (existsInHashtable && hash.get(currentEventID).isAlive()) {
 
 				ObjectEventRecord record = hash.get(currentEventID);
 				record.updateRecord(event);
 				hash.put(currentEventID, record);
 
 				System.out.println("Event " + currentEventID
 						+ " was updated in the records");
 
 				System.out.println();
 				System.out.println();
 
 			}
 
 			if (existsInHashtable && currentEventStatus.equalsIgnoreCase("A")) {
 				Error multipleCreation = new Error("The object already exists");
 				logger.getLogger()
 						.log(Level.SEVERE,
 								"The object with id "
 										+ currentEventID
 										+ " is already created! Don't create it again!");
 				numberOfIllegalAccesses++;
 				multipleCreations++;
 				multipleObjects.add(currentEventID);
 
 				System.out.println();
 				System.out.println();
 
 			}
 
 			// occupied but dead
 			else if (existsInHashtable && !hash.get(currentEventID).isAlive()) {
 
 				Error deadError = new Error(
 						"The object is dead so you cannot update it!");
 				logger.getLogger().log(
 						Level.SEVERE,
 						"The object with id " + currentEventID
 								+ " is dead so you cannot update it!");
 				numberOfIllegalAccesses++;
 				deadErrors++;
 				deadObjects.add(currentEventID);
 
				System.out.println(
						"The object is dead so you cannot update it!");
 				System.out.println();
 			}
 
 		}
 		long endOfProcess = System.currentTimeMillis();
 		long timeTakenInMillisecs = endOfProcess - startOfProcess;
 		long timeTakenInSeconds = timeTakenInMillisecs / 1000;
 
		long linesPerSecond = 12782052 / timeTakenInSeconds;
 		int seconds = (int) (timeTakenInMillisecs / 1000) % 60;
 		int minutes = (int) ((timeTakenInMillisecs / (1000 * 60)) % 60);
 		int hours = (int) ((timeTakenInMillisecs / (1000 * 60 * 60)) % 24);
 		System.out
 				.printf("It took %d hours %d minutes %d seconds to process 11MB file\n",
 						hours, minutes, seconds);
 		System.out
 				.printf("Number of recorded errors, i.e. trying to access not initialised or dead object %d \n",
 						numberOfIllegalAccesses);
 		System.out.println("The program reads " + linesPerSecond
 				+ " lines per second");
		System.out.println("Number of unborn accesses " + notBornErrors);
 		System.out
 				.println("Objects accessed before creation " + notBornObjects);
		System.out.println("Number of dead accesses " + deadErrors);
 		System.out.println("Objects accessed after death " + deadObjects);
		System.out.println("Number of multiple creations " + multipleCreations);
 		System.out.println("Objects created more than once " + multipleObjects);
 
 
 	}
 
 	public static boolean testing(final String path) {
 		Scanner inputScanner = null;
 
 		try {
 			InputStream fileStream = new FileInputStream(path);
 			InputStream gzipStream = new GZIPInputStream(fileStream);
 			inputScanner = new Scanner(gzipStream);
 		}
 
 		catch (IOException io) {
 			System.out.println("IOException");
 			System.exit(0);
 		}
 
 		HashMap<String, ObjectEventRecord> hash = new HashMap();
 
 		while (inputScanner.hasNextLine()) {
 
 			String nextLine = inputScanner.nextLine();
 
 			System.out.println(nextLine + " nextline");
 
 			Event event = new Event(nextLine);
 			String currentEventID = event.getID();
 			String currentEventStatus = event.getStatus();
 
 			boolean existsInHashtable = hash.get(currentEventID) != null;
 
 			// free and status "A"
 			if (!existsInHashtable && currentEventStatus.equalsIgnoreCase("A")) {
 
 				ObjectEventRecord record = new ObjectEventRecord(event);
 				hash.put(currentEventID, record);
 
 			}
 
 			// free but not status "A"
 			else if (!existsInHashtable
 					&& !currentEventStatus.equalsIgnoreCase("A")) {
 
 				return false;
 
 			}
 
 			// occupied and alive
 			else if (existsInHashtable && hash.get(currentEventID).isAlive()) {
 
 				ObjectEventRecord record = hash.get(currentEventID);
 				record.updateRecord(event);
 				hash.put(currentEventID, record);
 
 			}
 
 			if (existsInHashtable && currentEventStatus.equalsIgnoreCase("A")) {
 
 				return false;
 
 			}
 
 			// occupied but dead
 			else if (existsInHashtable && !hash.get(currentEventID).isAlive()) {
 
 				return false;
 
 			}
 
 		}
 		return true;
 	}
 
 }
