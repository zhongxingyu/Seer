 package utilities;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.Path;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collections;
 import static java.nio.charset.Charset.defaultCharset;
 import static java.nio.file.Files.newBufferedReader;
 import static java.nio.file.Paths.get;
 
 public class RosterParser {
 	/**
 	 * This class will be used to read through the .lst file for each employee,
 	 * in order to determine what shifts they work.
 	 */
 	// -----------------------------------------------------------------------------
 
 	public ArrayList<String> getEmployeesOnShift(int shiftTime) {
 		ArrayList<String> Employees = new ArrayList<String>();
 		ArrayList<File> files = new ArrayList<File>();
 		Calendar cal = Calendar.getInstance();
 		String name, day;
 		int dayAsInt;
 		// Directory path here
 		String path = "PatrolScheduler/employee";
 		dayAsInt = cal.get(Calendar.DAY_OF_WEEK);
 		day = getDayAsString(dayAsInt);
 
 		File folder = new File(path);
 		Collections.addAll(files, folder.listFiles());
 
 		/*
 		 * run through each directory, getting the name if the employee is on
 		 * the shift and adding it to the array list.
 		 */
 		for (File file : files) {
 			// check if employee is on the shift
 			if (isOnShift(file, shiftTime, day)) {
 				// check if employee is on the employee list
 				name = getNameFromCNumber(file.getName());
 				// if so add him
 				if (name != null)
 					Employees.add(name);
 			}
 		}
 		return Employees;
 	}
 
 	// -----------------------------------------------------------------------------
 	public boolean isOnShift(File file, int shiftTime, String day) {
 		String filePath, shiftTimeAsString;
 		// check that file is a directory
 		if (!file.isDirectory())
 			return false;
 
 		filePath = file.getAbsolutePath() + "\\" + "regularschedule.lst";
 		shiftTimeAsString = ((Integer) shiftTime).toString();
 
 		/*
 		 * open the reader, read each line and check if the time and day match,
 		 * if so return true, else return false
 		 */
 		try (BufferedReader reader = getReader(filePath)) {
 			String line = null;
 			while ((line = reader.readLine()) != null) {
 				if (line.startsWith(shiftTimeAsString) && line.contains(day)) {
 					return true;
 				}
 			}
 		} catch (IOException e) {
 			System.out.println("Employee file " + file.getName()
 					+ " has no regular shifts");
 			// e.printStackTrace();
 		}
 		return false;
 	}
 
 	// -----------------------------------------------------------------------------
 
 	public String getNameFromCNumber(String Cnumber) {
 		// here filename is really the cnumber
 
 		String employeeFileName, name;
 		String[] splitName;
 		employeeFileName = System.getProperty("user.dir")
 				+ "\\PatrolScheduler\\employee\\employees.lst";
 
 		/*
 		 * Open the employee list file, check for a match with the cnumber, and
 		 * if one occurs, return the name
 		 */
 		try (BufferedReader reader = getReader(employeeFileName)) {
 			String line = null;
 			while ((line = reader.readLine()) != null) {
 				if (line.contains(Cnumber)) {
 					splitName = line.split("\t", 3);
 					name = splitName[0].concat(" ");
 					name = name.concat(splitName[1]);
 					return name;
 				}
 			}
 		} catch (IOException e) {
 			System.out.println("Employee list does not exist");
 			// e.printStackTrace();
 		}
 		return null;
 	}
 
 	// -----------------------------------------------------------------------------
 
 	private BufferedReader getReader(String filename) throws IOException {
 		return newBufferedReader(get(filename), defaultCharset());
 	}
 
 	// -----------------------------------------------------------------------------
 
 	private String getDayAsString(int dayAsInt) {
 		String day;
 
 		switch (dayAsInt) {
 		case 1:
 			return "Sunday";
 		case 2:
 			return "Monday";
 		case 3:
 			return "Tuesday";
 		case 4:
 			return "Wednesday";
 		case 5:
 			return "Thursday";
 		case 6:
 			return "Friday";
 		case 7:
 			return "Saturday";
 		default:
 			return null;
 		}
 	}
 }
