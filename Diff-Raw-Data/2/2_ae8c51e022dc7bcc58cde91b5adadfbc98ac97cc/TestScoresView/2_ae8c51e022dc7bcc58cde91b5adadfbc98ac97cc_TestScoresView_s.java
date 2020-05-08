 package com.earth2me.school.casestudy;
 
 import java.util.Scanner;
 
 final class TestScoresView implements Runnable
 {
 	private final TestScoresModel model;
 
 	public TestScoresView(final TestScoresModel model)
 	{
 		this.model = model;
 	}
 
 	// Menu-driven command loop
 	public void run()
 	{
 		for (;;)
 		{
 			System.out.println("start of run method");
 			System.out.println("Number of students: " + model.size());
 			System.out.println("Index of current student: " + model.currentPosition());
 
 			final int quit = displayMenu();
 			final int command = getCommand("Enter a number [1-11]: ", 1, quit);
 			if (command == quit)
 			{
 				return;
 			}
 			runCommand(command);
 		}
 	}
 
 	private int displayMenu()
 	{
 		System.out.println("MAIN MENU");
 
 		final String[] menu = {
 				"Display the current student.",
 				"Display the class average.",
 				"Display the student with the highest grade.",
 				"Display all of the students.",
 				"Edit the current student.",
 				"Add a new student.",
 				"Move to the first student.",
 				"Move to the last student.",
 				"Move to the next student.",
 				"Move to the previous student.",
 				"Quit the program",
 		};
 
 		// Print each menu item.
 		for (int i = 0; i < menu.length; i++)
 		{
 			// Print it in the format " 1. Blah", ... "12. Blah", etc.
 			System.out.println(String.format("%2d. %s", i + 1, menu[i]));
 		}
 
 		return menu.length;
 	}
 
 	// Prompts the user for a command number and runs until
 	// the user enters a valid command number
 	// Parameters: prompt is the string to display
 	// low is the smallest command number
 	// high is the largest command number
 	// Returns: a valid command number
 	private int getCommand(String prompt, int low, int high)
 	{
 		final Scanner reader = new Scanner(System.in);
 		for (int command;;)
 		{
 			System.out.print(prompt);
 			command = reader.nextInt();
 			if (command < low || command > high)
 			{
 				System.out.println("Error: command must be between " + low + " and " + high);
 			}
 			else
 			{
 				return command;
 			}
 		}
 	}
 
 	// Selects a command to run based on a command number,
 	// runs the command, and asks the user to continue by
 	// pressing the Enter key
 	private void runCommand(int command)
 	{
 		switch (command)
 		{
 		case 1:
 			displayStudent();
 			break;
 
 		case 2:
 			displayClassAverage();
 			break;
 
 		case 3:
 			displayHighScore();
 			break;
 
 		case 4:
 			displayAllStudents();
 			break;
 
 		case 5:
 			editStudent();
 			break;
 
 		case 6:
 			addStudent();
 			break;
 
 		case 7:
 			moveToFirst();
 			break;
 
 		case 8:
 			moveToLast();
 			break;
 
 		case 9:
 			moveToNext();
 			break;
 
 		case 10:
 			moveToPrevious();
 			break;
 		}
 	}
 
 	/**
 	 * Outputs a list of all students.
 	 * 
 	 * @author Paul Buonopane
 	 */
 	private void displayAllStudents()
 	{
 		System.out.println(model);
 	}
 
 	/**
 	 * Moves to the first element in the model.
 	 * 
 	 * @author Paul Buonopane
 	 */
 	private void moveToFirst()
 	{
 		model.first();
 	}
 
 	/**
 	 * Moves to the last element in the model.
 	 * 
 	 * @author Paul Buonopane
 	 */
 	private void moveToLast()
 	{
 		model.last();
 	}
 
 	/**
 	 * Moves to the next element in the model.
 	 * 
 	 * @author Paul Buonopane
 	 */
 	private void moveToNext()
 	{
 		model.next();
 	}
 
 	/**
 	 * Moves to the previous element in the model.
 	 * 
 	 * @author Paul Buonopane
 	 */
 	private void moveToPrevious()
 	{
 		model.previous();
 	}
 
 	/**
 	 * Outputs the class average to the console.
 	 * 
 	 * @author Paul Buonopane
 	 */
 	private void displayClassAverage()
 	{
 		System.out.println("Average Score: " + model.getClassAverage());
 	}
 
 	private void displayStudent()
 	{
 		Student s = model.currentStudent();
 		if (s == null)
 		{
 			System.out.println("No student is currently available");
 		}
 		else
 		{
 			System.out.println(s);
 		}
 	}
 
 	/**
 	 * Outputs the student with the highest grade, or
 	 * "No students have been added yet." if students have yet to be added to
 	 * the model.
 	 */
 	private void displayHighScore()
 	{
 		Student student = model.getHighScore();
 		if (student == null)
 		{
 			System.out.println("No students have been added yet.");
 		}
 		else
 		{
 			System.out.println(student);
 		}
 	}
 
 	/**
 	 * Prompts the user to add a student to the model.
 	 */
 	private void addStudent()
 	{
 		final Student student = new Student();
 
 		changeName(student);
 		changeAllScores(student);
 
 		// Validate the data. Output error if invalid.
 		String error = student.validateData();
 		if (error != null)
 		{
 			System.out.println(error);
 			return;
 		}
 
 		// Attempt to add student to model. Output error if impossible.
 		error = model.add(student);
 		if (error != null)
 		{
 			System.out.println(error);
 			return;
 		}
 
 		System.out.println("Student added.");
 	}
 
 	private void editStudent()
 	{
 		Student student = model.currentStudent();
 		if (student == null)
 		{
 			System.out.println("No student is currently available.");
 			return;
 		}
 
 		// Work on a temporary copy
 		final Student temp = student.memberwiseClone();
 
 		final String menu = "EDIT MENU\n" +
 				"1. Change the name.\n" +
 				"2. Change all scores.\n" +
 				"3. Change individual score.\n" +
 				"4. View the student.\n" +
 				"5. Quit this menu.\n";
 
 		loop:
 		for (;;)
 		{
 			System.out.print(menu);
 
 			final int command = getCommand("Enter a number [1-4]: ", 1, 4);
 			switch (command)
 			{
 			case 1:
 				changeName(student);
 				break;
 
 			case 2:
 				changeAllScores(student);
 				break;
 
 			case 3:
 				changeIndividualScore(student);
 				break;
 
 			case 4:
 				displayStudent(student);
 				break;
 
 			case 5:
 				break loop;
 			}
 		}
 
 		// Check for valid data before writing to database
 		final String message = temp.validateData();
 		if (message != null)
 		{
 			System.out.println(message);
 		}
 		else
 		{
 			model.replace(temp);
 		}
 	}
 
 	/**
 	 * Prompts the user to change the name of a student.
 	 * 
 	 * @param student
 	 *            The student to change.
 	 */
 	private void changeName(final Student student)
 	{
 		System.out.print("Enter the name of the student: ");
 		
 		// Ignore blank lines.
 		final Scanner reader = new Scanner(System.in);
 		String line;
 		do
 		{
 			line = reader.nextLine();
 		}
 		while (line == null || line.length() < 1);
 		
 		student.setName(line);
 	}
 
 	/**
 	 * Prompts the user to change all of the score for a student's tests.
 	 * 
 	 * @param student
 	 *            The student to change.
 	 */
 	private void changeAllScores(final Student student)
 	{
 		// Iterate through the tests and request scores.
 		final Scanner reader = new Scanner(System.in);
		for (int i = 1; i < -student.getNumberOfTests(); i++)
 		{
 			System.out.printf("Score on test %d: ", i);
 			
 			// Read the score.
 			final int score = reader.nextInt();
 			
 			// Limit the score to the range 0 <= score <= 100.
 			student.setScore(i, Math.max(0, Math.min(score, 100)));
 		}
 	}
 
 	/**
 	 * Prompts a user to change an individual test score.
 	 * 
 	 * @param student
 	 *            The student to change.
 	 */
 	private void changeIndividualScore(final Student student)
 	{
 		// Ensure there is at least one test to change.
 		final int testCount = student.getNumberOfTests();
 		if (testCount < 1)
 		{
 			System.out.println("This student has no test scores.");
 			return;
 		}
 
 		// Loop until we have a valid test number.
 		int testIndex;
 		final Scanner reader = new Scanner(System.in);
 		for (;;)
 		{
 			System.out.printf("Test score to change [1-%d]: ", testCount);
 			testIndex = reader.nextInt(); // Read the test number
 			if (testIndex < 1 || testIndex > testCount)
 			{
 				// Test number is invalid.  Loop again.
 				System.out.println("That test does not exist.");
 			}
 			else
 			{
 				// Test number is valid.  Break loop.
 				break;
 			}
 		}
 		
 		System.out.printf("Score on test %d: ", testIndex);
 		
 		// Read the score.
 		final int score = reader.nextInt();
 		
 		// Limit the score to the range 0 <= score <= 100.
 		student.setScore(testIndex, Math.max(0, Math.min(score, 100)));
 	}
 
 	/**
 	 * Outputs a student to the console.
 	 * 
 	 * @param student
 	 *            The student to output.
 	 */
 	private void displayStudent(final Student student)
 	{
 		System.out.println(student);
 	}
 }
