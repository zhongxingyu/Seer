 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.events.StartElement;
 import javax.xml.stream.events.XMLEvent;
 
 import database.Database;
 
 /**
  * Interactive user interface which allows a user to query and manage a database
  *
  */
 public class CommandLineInterface
 {
 	private PrintStream out;
 	private InputStream in;
 	
 	/**
 	 * Starts the SQL Query Command-line interface.
 	 * User can choose options using numeric input.
 	 * Data can be manipulated through alphanumeric input.
 	 */
 	public static void main(String[] args)
 	{
 		CommandLineInterface cli = new CommandLineInterface(System.out, System.in);
 		cli.start();
 	}
 	
 	private Scanner input;
 	private User user;
 	private Database DB;
 	private SQLParser parser;
 	
 	/**
 	 * Establish an interface using I/O streams.
 	 * @param out Output stream in which to display information
 	 * @param in Input stream by which the user can send information
 	 */
 	public CommandLineInterface(PrintStream out, InputStream in)
 	{
 		this.out = out;
 		this.in = in;
 		initializeScanner();
 	}
 
 	public void start()
 	{
 		DB = new Database();
 		parser = new SQLParser(DB);
 		out.println("SQL Command Line Interface");
 		mainMenu(true);
 	}
 	
 	/*
 	 *  Menus (Only accessible through user commands [main menu called initially])
 	 */
 	
 	private void mainMenu(boolean listHelp)
 	{
 		out.println("\nMain Menu");
 		/*
 		 * Options:
 		 * 1. Initialize Database
 		 * 2. Login to role
 		 * 3. Exit
 		 */
 		String[] options = {"Initialize Database", "Login to role", "Exit"};
 		if (listHelp) printOptions(options);
 		switch(getChoice(options.length))
 		{
 		case 1:
 			initializeDatabase();
 			mainMenu(true);
 			break;
 		case 2:
 			loginToRole(true);
 			break;
 		case 3:
 			// Exit
 			if (DB != null)
 				closeDatabase();
 			//input.close();
 			out.println("Goodbye!");
 			break;
 		default:
 			throw new ArrayIndexOutOfBoundsException();
 		}
 	}
 	
 	private void initializeDatabase()
 	{
 		/*
 		 * Options:
 		 * 1. Direct data entry
 		 * 2. Load table from file
 		 * 3. Save table to file
 		 */
 		String[] options = {"Direct data entry", "Load table from file", "Save table to CSV file"};
 		printOptions(options);
 		switch(getChoice(options.length))
 		{
 		case 1:
 			directDataEntry();
 			break;
 		case 2:
 			loadFile();
 			break;
 		case 3:
 			saveFile();
 			break;
 		default:
 			throw new ArrayIndexOutOfBoundsException();
 		}
 	}
 	
 	private void loadFile()
 	{
 		out.println("\nImport table from file");
 		/*
 		 * Options:
 		 * 1. Load from CSV file
 		 * 2. Load from XML file
 		 * 3. Back
 		 */
 		String[] options = {"Import CSV file", "Import XML file", "Back"};
 		printOptions(options);
 		switch(getChoice(options.length))
 		{
 		case 1:
 			loadFileCSV();
 			break;
 		case 2:
 			loadFileXML();
 			break;
 		case 3:
 			return;
 		default:
 			throw new ArrayIndexOutOfBoundsException();
 		}
 	}
 	
 	private void loadFileCSV()
 	{
 		out.println("\nImport CSV table");
 		/*
 		 * Options:
 		 * 1. Students table
 		 * 2. Courses table
 		 * 3. Grades table
 		 * 4. Back
 		 */
 		String[] options = {"Import student table", "Import course table", "Import grade table", "Back"};
 		printOptions(options);
 		String tableName = "";
 		switch(getChoice(options.length))
 		{
 		case 1:
 			tableName = "students";
 			break;
 		case 2:
 			tableName = "courses";
 			break;
 		case 3:
 			tableName = "grades";
 			break;
 		case 4:
 			return;
 		default:
 			throw new ArrayIndexOutOfBoundsException();
 		}
 		try
 		{
 			String filepath = prompt("Load from file");
 			if (!filepath.endsWith(".csv"))
 				filepath = filepath + ".csv";
 			Scanner inScanner = new Scanner(new File(filepath));
 			while(inScanner.hasNextLine())
 			{
 				String values = inScanner.nextLine();
 				String query = String.format("INSERT INTO %s VALUES (%s)",
 						tableName, values);
 				try{
 					parser.query(query);
 				}catch (Exception e)
 				{
 					out.println(e.getMessage());
 				}
 			}
 			out.println("Import successful.");
 			inScanner.close();
 		}
 		catch (FileNotFoundException e)
 		{
 			out.println("File not found.");
 			loadFileCSV();
 		}
 		catch (Exception e)
 		{
 			out.println(e.getMessage());
 		}
 	}
 	
 	private void loadFileXML()
 	{
 		out.println("\nImport XML table");
 		String inFilepath = prompt("Load from file");
 		if (inFilepath.equals(""))
 			return;
 		if (!inFilepath.endsWith(".xml"))
 			inFilepath += ".xml";
 		try
 		{
 			xmlToDatabase(inFilepath);
 			out.println("Table loaded.");
 		}
 		catch (IOException e)
 		{
 			if(e instanceof FileNotFoundException)
 				out.println("Error: File not found");
 			else
 				out.println("Error: File not valid");
 		}
 		catch (XMLStreamException e)
 		{
 			out.println("XML error");
 		}
 		catch (Exception e)
 		{
 			out.println(e.getMessage());
 		}
 	}
 	
 	private void saveFile()
 	{
 		/*
 		 * Options:
 		 * 1. Save students table
 		 * 2. Save courses table
 		 * 3. Save grades table
 		 * 4. Exit 
 		 */
 		String[] options = {"Save students table", "Save courses table", "Save grades table"};
 		printOptions(options);
 		switch(getChoice(options.length))
 		{
 			case 1:
 				save("students");
 				break;
 			case 2:
 				save("courses");
 				break;
 			case 3:
 				save("grades");
 				break;
 			case 4:
 				return;
 			default:
 				throw new ArrayIndexOutOfBoundsException();
 		}
 	}
 	
 	private void save(String tablename)
 	{
 		String outFilepath = prompt("Save to file");
 		if (!outFilepath.endsWith(".csv"))
 			outFilepath += ".csv";
 		try
 		{
 			String query = String.format("SELECT * FROM %s", tablename);
 			String tableString = parser.query(query);
 			BufferedWriter saveOut = new BufferedWriter(new FileWriter(outFilepath));
 			ArrayList<ArrayList<String>> table = asciiTableToArrayList(tableString);
 			for (ArrayList<String> row : table)
 			{
 				String rowString = "";
 				for (int i = 0; i < row.size()-1; i++)
 					rowString += row.get(i) + ",";
 				rowString += row.get(row.size()-1);
 				saveOut.write(rowString);
 				saveOut.newLine();
 			}
 			saveOut.close();
 			out.println("File saved.");
 		}
 		catch (Exception e)
 		{
 			out.println(e.getMessage());
 		}
 	}
 	
 	private ArrayList<ArrayList<String>> asciiTableToArrayList(String ascii)
 	{
 		Scanner scanner = new Scanner(ascii);
 		ArrayList<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();
 		scanner.nextLine();
 		scanner.nextLine();
 		scanner.nextLine();	// Advance scanner to first row of data
 		while(scanner.hasNextLine())
 		{
 			ArrayList<String> lineArrayList = new ArrayList<String>();
 			String line = scanner.nextLine();
 			if (!line.startsWith("|"))
 				continue;
 			while (line.length() > 1)
 			{
 				if (line.indexOf("|") >= line.length()-2)
 					break;
 				String cell = line = line.substring(line.indexOf("|") + 2);
 				int idx = cell.indexOf("|");
 				if (idx < 0)
 					continue;
 				cell = cell.substring(0, cell.indexOf("|")).trim();
 				lineArrayList.add(cell);
 			}
 			ret.add(lineArrayList);
 		}
 		scanner.close();
 		return ret;
 	}
 	
 	private void closeDatabase()
 	{
 		if (prompt("Save tables? (Y/N)").equalsIgnoreCase("Y"))
 			saveFile();
 	}
 	
 	private void loginToRole(boolean listHelp)
 	{
 		out.println("Login to role:");
 		/*
 		 * Options:
 		 * 1. Student
 		 * 2. Teacher
 		 * 3. Administrator
 		 * 4. Back to main menu
 		 */
 		String[] options = {"Student", "Teacher", "Administrator", "Back to main menu"};
 		if (listHelp) printOptions(options);
 		switch(getChoice(options.length))
 		{
 		case 1:
 			user = new Student();
 			makeSelection(true);
 			break;
 		case 2:
 			user = new Instructor();
 			makeSelection(true);
 			break;
 		case 3:
 			user = new Administrator();
 			makeSelection(true);
 			break;
 		case 4:
 			mainMenu(true);
 			break;
 		default:
 			throw new ArrayIndexOutOfBoundsException();
 		}
 	}
 	
 	private void makeSelection(boolean listHelp)
 	{
 		int highestIdx = 0;
 		int basicInfoIdx = 0, modifyGradesIdx = 0, putStudentsIdx = 0, createCoursesIdx = 0, seeStudentGradesIdx = 0,
 				seeCourseGradesIdx = 0, rmStudentsIdx = 0, helpIdx = 0, logoutIdx = 0;
 		
 		// make list of options based on user permissions, associating each option with a number
 		ArrayList<String> options = new ArrayList<String>(0);
 		if (user.canSeeBasicCourseInfo()){			basicInfoIdx = ++highestIdx;		options.add("See basic course info");}
 		if (user.canModifyGrades()){				modifyGradesIdx = ++highestIdx;		options.add("Modify grade");}
 		if (user.canPutStudentsInCourses()){		putStudentsIdx = ++highestIdx;		options.add("Put student in course");}
 		if (user.canCreateCourses()){				createCoursesIdx = ++highestIdx;	options.add("Create course");}
 		if (user.canSeeAllGradesForOneStudent()){	seeStudentGradesIdx = ++highestIdx;	options.add("See student grades");}
 		if (user.canSeeAllGradesForOneCourse()){	seeCourseGradesIdx = ++highestIdx;	options.add("See course grades");}
 		if (user.canRemoveStudentFromCourse()){		rmStudentsIdx = ++highestIdx;		options.add("Remove student from course");}
 		if (user.canGetHelp()){						helpIdx = ++highestIdx;				options.add("Help");}
 		if (user.canLogout()){						logoutIdx = ++highestIdx;			options.add("Logout");}
 		
 		String[] optionsArray = new String[options.size()];
 		for (int i = 0; i < options.size(); i++)
 			optionsArray[i] = options.get(i).toString();
 		out.printf("Logged in as %s. Please make a selection. (Type %s for help)%n", user.name, helpIdx);
 		if (listHelp) printOptions(optionsArray);
 		int choice = getChoice(options.size());
 		if (choice < 1)
 		{
 			out.println("Invalid selection.");
 			makeSelection(false);
 		}
 		else if (choice == basicInfoIdx) {
 			getBasicInfo();
 			makeSelection(false);
 		}
 		else if (choice == modifyGradesIdx) {
 			modifyGrade();
 			makeSelection(false);
 		}
 		else if (choice == putStudentsIdx) {
 			putStudentInCourse();
 			makeSelection(false);
 		}
 		else if (choice == createCoursesIdx) {
 			createCourse();
 			makeSelection(false);
 		}
 		else if (choice == seeStudentGradesIdx) {
 			seeStudentGrades();
 			makeSelection(false);
 		}
 		else if (choice == seeCourseGradesIdx) {
 			seeCourseGrades();
 			makeSelection(false);
 		}
 		else if (choice == rmStudentsIdx) {
 			removeStudentFromCourse();
 			makeSelection(false);
 		}
 		else if (choice == helpIdx)
 			help();
 		else if (choice == logoutIdx)
 			loginToRole(true);// Logout
 		else
 		{
 			out.println("Invalid selection.");
 			makeSelection(false);
 		}
 	}
 	
 	/*
 	 * Database methods
 	 */
 	
 	private void directDataEntry()
 	{
 		if (DB == null)
 		{
 			DB = new Database();
 			parser = new SQLParser(DB);
 		}
 		String query;
 		do
 		{
 			query = prompt("Input SQL");
 			try
 			{
 				out.println(parser.query(query));
 			}catch (Exception e)
 			{
 				out.println(e.getMessage());
 			}
 		} while (!query.equals(""));
 	}
 	
 	private void getBasicInfo()
 	{
 		String courseCode = prompt("Enter course code");
 		if (courseCode.equals(""))
 			return;
 		try
 		{
			String query = String.format("SELECT * FROM courses WHERE course='%s'", courseCode);
 			out.println(parser.query(query));
 		}catch (Exception e)
 		{
 			out.println(e.getMessage());
 		}
 	}
 	
 	private void modifyGrade()
 	{
 		String courseCode = prompt("Enter course code");
 		if (courseCode.equals(""))
 			return;
 		String studentID = prompt("Enter student ID");
 		if (studentID.equals(""))
 			return;
 		String grade = prompt("Enter new grade (0-100)");
 		if (grade.equals(""))
 			return;
 		try
 		{
			String query = String.format("UPDATE grades SET grade='%s' WHERE course='%s' AND student='%s'",
 					grade, courseCode, studentID);
 			out.println(parser.query(query));
 		}catch (Exception e)
 		{
 			out.println(e.getMessage());
 		}
 	}
 	
 	private void putStudentInCourse()
 	{
 		String courseCode = prompt("Enter course code");
 		if (courseCode.equals(""))
 			return;
 		String studentID = prompt("Enter Student ID");
 		if (studentID.equals(""))
 			return;
 		try
 		{
 			String query = String.format("INSERT INTO grades (student, course, grade, isFinal) VALUES (%s, %s, %d, %s)",
 					studentID, courseCode, 0, "false");
 			out.println(parser.query(query));
 		}catch (Exception e)
 		{
 			out.println(e.getMessage());
 		}
 	}
 	
 	private void createCourse()
 	{
 		String courseCode = prompt("Enter course code");
 		if (courseCode.equals(""))
 			return;
 		String courseName = prompt("Enter course name");
 		if (courseName.equals(""))
 			return;
 		String courseInstructor = prompt("Enter course instructor");
 		if (courseInstructor.equals(""))
 			return;
 		try
 		{
 			String query = String.format("INSERT INTO courses (course, name, instructor) VALUES (%s, %s, %s)",
 					courseCode, courseName, courseInstructor);
 			out.println(parser.query(query));
 		}catch (Exception e)
 		{
 			out.println(e.getMessage());
 		}
 	}
 	
 	private void seeStudentGrades()
 	{
 		String studentID = prompt("Enter student ID");
 		if (studentID.equals(""))
 			return;
 		try
 		{
			String query = String.format("SELECT * FROM grades WHERE student='%s'", studentID);
 			out.println(parser.query(query));
 		}catch (Exception e)
 		{
 			out.println(e.getMessage());
 		}
 	}
 	
 	private void seeCourseGrades()
 	{
 		String courseName = prompt("Enter course code");
 		if (courseName.equals(""))
 			return;
 		try
 		{
			String query = String.format("SELECT * FROM grades WHERE course='%s'", courseName);
 			out.println(parser.query(query));
 		}catch (Exception e)
 		{
 			out.println(e.getMessage());
 		}
 	}
 	
 	private void removeStudentFromCourse()
 	{
 		String courseCode = prompt("Enter course code");
 		if (courseCode.equals(""))
 			return;
 		String studentID = prompt("Enter Student ID");
 		if (studentID.equals(""))
 			return;
 		try
 		{
			String query = String.format("DELETE FROM grades WHERE course='%s' AND student='%s'", courseCode, studentID);
 			out.println(parser.query(query));
 		}catch (Exception e)
 		{
 			out.println(e.getMessage());
 		}
 	}
 
     private void help() {
         makeSelection(true);// queries current user information and prints permissions
     }
 	
     private void xmlToDatabase(String filepath) throws Exception
     {
     	XMLInputFactory inFactory = XMLInputFactory.newInstance();
     	InputStream inStream = new FileInputStream(filepath);
     	XMLEventReader eventReader = inFactory.createXMLEventReader(inStream);
     	while (eventReader.hasNext())
     	{
 	    	XMLEvent event = eventReader.nextEvent();
 	    	if (event.isStartElement())
 	    	{
 	    		StartElement startElement = event.asStartElement();
 	    		if (startElement.getName().getLocalPart().equals("StudentTable"))
 	    		{
 	    			xmlStudentTable(eventReader);
 	    			break;
 	    		}
 	    		else if (startElement.getName().getLocalPart().equals("CourseTable"))
 	    		{
 	    			xmlCourseTable(eventReader);
 	    			break;
 	    		}
 	    		else if (startElement.getName().getLocalPart().equals("GradeTable"))
 	    		{
 	    			xmlGradeTable(eventReader);
 	    			break;
 	    		}
 	    	}
     	}
     }
     
     private void xmlStudentTable(XMLEventReader eventReader) throws Exception
     {
     	String id = "", first = "", last = "", age = "", year = ""; 
     	while(eventReader.hasNext())
     	{
     		XMLEvent event = eventReader.nextEvent();
     		if (event.isStartElement())
     		{
     			StartElement startElement = event.asStartElement();
     			if (startElement.getName().getLocalPart().equalsIgnoreCase("id"))
     				id = eventReader.nextEvent().asCharacters().getData();
     			else if (startElement.getName().getLocalPart().equalsIgnoreCase("first"))
     				first = eventReader.nextEvent().asCharacters().getData();
     			else if (startElement.getName().getLocalPart().equalsIgnoreCase("last"))
     				last = eventReader.nextEvent().asCharacters().getData();
     			else if (startElement.getName().getLocalPart().equalsIgnoreCase("age"))
     				age = eventReader.nextEvent().asCharacters().getData();
     			else if (startElement.getName().getLocalPart().equalsIgnoreCase("year"))
     				year = eventReader.nextEvent().asCharacters().getData();
     		}
     		
     		// reach end of a record
     		if (event.isEndElement())
     			if (event.asEndElement().getName().getLocalPart() == ("student")) {
 					String query = String.format("INSERT INTO students VALUES (%s, %s, %s, %s, %s)",
 							id, first, last, age, year);
 					parser.query(query);
 				}
     	}
     }
     
     private void xmlCourseTable(XMLEventReader eventReader) throws Exception
     {
     	String id = "", name = "", instructor = ""; 
     	while(eventReader.hasNext())
     	{
     		XMLEvent event = eventReader.nextEvent();
     		if (event.isStartElement())
     		{
     			StartElement startElement = event.asStartElement();
     			if (startElement.getName().getLocalPart().equalsIgnoreCase("course"))
     				id = eventReader.nextEvent().asCharacters().getData();
     			else if (startElement.getName().getLocalPart().equalsIgnoreCase("name"))
     				name = eventReader.nextEvent().asCharacters().getData();
     			else if (startElement.getName().getLocalPart().equalsIgnoreCase("instructor"))
     				instructor = eventReader.nextEvent().asCharacters().getData();
     		}
     		
     		// reach end of a record
     		if (event.isEndElement())
     			if (event.asEndElement().getName().getLocalPart() == ("class")) {
 					String query = String.format("INSERT INTO courses VALUES (%s, %s, %s)",
 							id, name, instructor);
 					parser.query(query);
 				}
     	}
     }
     
     private void xmlGradeTable(XMLEventReader eventReader) throws Exception
     {
     	String student = "", course = "", grade = "", isFinal = "";
     	while(eventReader.hasNext())
     	{
     		XMLEvent event = eventReader.nextEvent();
     		if (event.isStartElement())
     		{
     			StartElement startElement = event.asStartElement();
     			if (startElement.getName().getLocalPart().equalsIgnoreCase("student"))
     				student = eventReader.nextEvent().asCharacters().getData();
     			else if (startElement.getName().getLocalPart().equalsIgnoreCase("class"))
     				course = eventReader.nextEvent().asCharacters().getData();
     			else if (startElement.getName().getLocalPart().equalsIgnoreCase("grades"))
     				grade = eventReader.nextEvent().asCharacters().getData();
     			else if (startElement.getName().getLocalPart().equalsIgnoreCase("isFinal"))
     				isFinal = eventReader.nextEvent().asCharacters().getData();
     		}
     		
     		// reach end of a record
     		if (event.isEndElement())
 	    		if (event.asEndElement().getName().getLocalPart() == ("grades")) {
 					String query = String.format("INSERT INTO grades VALUES (%s, %s, %s, %s)",
 							student, course, grade, isFinal);
 					parser.query(query);
 				}
     	}
     }
     
    
 	
 	/*
 	 * Internal Methods
 	 */
 	
 	private void initializeScanner()
 	{
 		input = new Scanner(in);
 	}
 	
 	private int getChoice(int numChoices)
 	{
 		try
 		{
 			int choice = Integer.parseInt(prompt("Selection"));
 			if (choice < 1 || choice > numChoices)
 				throw new ArrayIndexOutOfBoundsException(choice);
 			return choice;
 		}
 		catch (ArrayIndexOutOfBoundsException e)
 		{
 			out.printf("Please input an integer from 1 to %d.%n", numChoices);
 			return getChoice(numChoices);
 		}
 		catch (IllegalArgumentException e)
 		{
 			out.printf("Please input an integer from 1 to %d.%n", numChoices);
 			return getChoice(numChoices);
 		}
 	}
 	
 	private void printOptions(String[] options)
 	{
 		out.println("Options:");
 		for (int i = 0; i < options.length; i++)
 		{
 			out.printf("%d. %s%n", i+1, options[i]);
 		}
 	}
 	
 	private String prompt(String message)
 	{
 		out.printf("%s: ", message);
 		String res = input.nextLine();
 		out.println();
 		return res;
 	}
 	
 	abstract class User
 	{
 		String name;
 		boolean[] permissions;
 		User(boolean... permissions)
 		{
 			this.permissions = permissions;
 		}
 		boolean canSeeBasicCourseInfo()
 		{
 			return permissions[0];
 		}
 		boolean canModifyGrades()
 		{
 			return permissions[1];
 		}
 		boolean canPutStudentsInCourses()
 		{
 			return permissions[2];
 		}
 		boolean canCreateCourses()
 		{
 			return permissions[3];
 		}
 		boolean canSeeAllGradesForOneStudent()
 		{
 			return permissions[4];
 		}
 		boolean canSeeAllGradesForOneCourse()
 		{
 			return permissions[5];
 		}
 		boolean canRemoveStudentFromCourse()
 		{
 			return permissions[6];
 		}
 		boolean canGetHelp()
 		{
 			return permissions[7];
 		}
 		boolean canLogout()
 		{
 			return permissions[8];
 		}
 	}
 	
 	class Student extends User
 	{
 		Student()
 		{
 			super(true, false, false, false, true, false, false, true, true);
 			name = "Student";
 		}
 	}
 	
 	class Instructor extends User
 	{
 		Instructor()
 		{
 			super(true, true, false, false, false, true, false, true, true);
 			name = "Instructor";
 			
 		}
 	}
 	
 	class Administrator extends User
 	{
 		Administrator()
 		{
 			super(true, false, true, true, true, true, true, true, true);
 			name = "Administrator";
 		}
 	}
 }
