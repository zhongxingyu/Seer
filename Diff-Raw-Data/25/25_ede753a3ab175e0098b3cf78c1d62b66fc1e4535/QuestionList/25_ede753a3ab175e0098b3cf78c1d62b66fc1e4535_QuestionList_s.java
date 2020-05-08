 package com.example.foundationsofsuccessfulprogramminginjava;
 
 import java.util.Random;
 
 public class QuestionList {
 	
 	Question q1 = new Question(0, "What is the hot-key combination for copying text?", "CTRL-Z", "CTRL-X", "CTRL-C", "CTRL-Y", 2);
 	Question q2 = new Question(1, "What is the hot-key combination for cutting text?", "CTRL-Y", "CTRL-C", "CTRL-Z", "CTRL-X", 3);
 	Question q3 = new Question(2, "What is the hot-key combination for undoing the most recent command?", "CTRL-Z", "CTRL-Y", "CTRL-C", "CTRL-A", 0);
 	Question q4 = new Question(3, "What is the hot-key combination for redoing the most recently undone command in Eclipse?", "CTRL-SHIFT-Z", "CTRL-Y", "CTRL-SHIFT-C", "CTRL-X", 0);
 	Question q5 = new Question(4, "What is the hot-key combination for organizing libraries (importing needed ones and deleting the unused ones)?", 
 			"CRTL-SHIFT-I", "CRTL-SHIFT-N", "CRTL-SHIFT-Z", "CRTL-SHIFT-O", 3);
 	Question q6 = new Question(5, "What is the hot-key combination for creating something new (classes, project, interfaces, etc)?", 
 			"SHIFT-ALT-N", "CRTL-SHIFT-N", "SHIFT-ALT-I", "SHIFT-ALT-O", 0);
 	Question q7 = new Question(6, "What is the hot-key combination for pasting text?", "CTRL-Z", "CTRL-X", "CTRL-C", "CTRL-V", 3);
 	Question q8 = new Question(7, "What is the hot-key combination for saving changes to ALL classes?", "CTRL-S", "SHIFT-ALT-S", "CTRL-SHIFT-C", "SHIFT-C", 1);
 	Question q9 = new Question(8, "Which of these is a hot-key combination for toggling comments text?", "CTRL-/ or CTRL-SHIFT-C", "CTRL-< or CTRL-SHIFT-E", 
 			"CTRL-* or CTRL-SHIFT-C", "CTRL-% or CTRL-SHIFT-E", 0);
 	Question q10 = new Question(9, "Which of these is a hot-key combination for deleting an entire line of code?", "CTRL-C", "CTRL-T", "CTRL-A", "CTRL-D", 3);
 	Question q11 = new Question(10, "Which of these is a fast way to select all of the text on a line?", "Home->SHIFT-End", "End->SHIFT-End", 
 			"Home->SHIFT-Home", "Home->SHIFT-Page Down", 0);
 	Question q12 = new Question(11, "What is the hot-key combination to move up and down a class by the methods in it?", "CTRL-(Up or Down)", 
 			"CTRL-SHIFT-(Up or Down)", "CTRL-ALT-(Up or Down)", "ALT-(Up or Down)", 0);
 	Question q13 = new Question(12, "What is the hot-key combinations to select all of the text in a class?", "CTRL-D", "CTRL-L", "CTRL-A", "CTRL-I", 2);
 	Question q14 = new Question(13, "What are the hot-key combinations to select all of the text in a class and then indent it properly?", 
 			"CTRL-I -> CTRL-A", "CTRL-A -> CTRL-I", "CTRL-I -> CTRL-O", "CTRL-O -> CTRL-I", 1);
 	Question q15 = new Question(14, "What is the hot-key combination for selecting whole words in a line of code?", "CTRL-(Left or Right)", "SHIFT-(Left or Right)", 
 			"CTRL-SHIFT-(Left or Right)", "ALT-(Left or Right)", 2);
 	Question q16 = new Question(15, "What is the hot-key combination for selecting finding specifics words?", "CTRL-F", "CTRL-R", "CTRL-U", "CTRL-A", 0);
 	Question q17 = new Question(16, "When a method is static, what does that mean?", "You need an instance of an object to call it.", "You do not need " +
 			"an instance of an object to call it.", "It returns a value.", "It does not return anything.", 1);
 	Question q18 = new Question(17, "What is the proper way to instantiate a Tiger object?", "Tiger = new Tiger();", "Tiger t = Tiger();", 
 			"t = new Tiger();", "Tiger t = new Tiger();", 3);
 	Question q19 = new Question(18, "Which of these is a proper int array declaration (of size 4)?", "int[] intArray = new array[4];", 
 			"int[4] intArray  = new int[];", "int intArray = new int[4];", "int[] intArray = new int[4];", 3);
 	Question q20 = new Question(19, "Which of for loops will traverse all of the elements of an array without throwing an exception?", 
 			"for(int i = 0; i < array.length; i++){}", "for(int i = 0; i <= array.length; i++){}", "for(int i = 1; i < array.length; i++){}", 
 			"for(int i = 0; i < array.length; i--){}", 0);
 	Question q21 = new Question(20, "What is the index of the last element in an array of size 10?", "10", "9", "11", "1", 1);
 	Question q22 = new Question(21, "What is the index of the first element in an array of size 10?", "0", "1", "9", "10", 0);
 	Question q23 = new Question(22, "If a String variable called \'word\'  holds the value \"sharp\", what will word.charAt(1), return?", 
 			"'s'", "'h'", "It will throw an error.", "Nothing.", 1);
 	Question q24 = new Question(23, "If a String variable called \'word\'holds the value \"sharp\", what will word.charAt(5), return?", 
 			"\'r\'", "\'p\'", "It will throw an error.", "Nothing.", 1);
 	Question q25 = new Question(24, "What is the proper way to declare a scanner to take in information from the keyboard?", 
 			"Scanner scan = new Scanner();", "Scanner scan = new Scanner(new File(filename));", "Scanner scan = new Scanner(System.out);", 
 			"Scanner scan = new Scanner(System.in);", 3);
 	Question q26 = new Question(25, "What is the proper way to declare a scanner to take in information from a file called \'filename\'?", 
 			"Scanner scan = new Scanner();", "Scanner scan = new Scanner(new File(filename));", "Scanner scan = new Scanner(System.out);", 
 			"Scanner scan = new Scanner(System.in);", 1);
 	Question q27 = new Question(26, "What will \"sample\".substring(3) return?", "\"mple\"", "\"sam\"", "\"samp\"", "\"ple\"", 3);
 	Question q28 = new Question(27, "What will \"sample\".substring(3, 5) return?", "\"pl\"", "\"ple\"", "\"mp\"", "\"mpl\"", 0);
 	Question q29 = new Question(28, "What does making a variable private mean?", "It can only be accessed by that class.", 
 			"It can only be accessed by classes in the same package.", "It can be accessed by classes in the same project.", 
 			"It can only be accessed by objects that extend that class.", 0);
 	Question q30 = new Question(29, "What does making a variable public mean?", "It can only be accessed by that class.", 
 			"It can only be accessed by classes in the same package.", "It can be accessed by classes in the same project.", 
 			"It can only be accessed by objects that extend that class.", 2);
 	Question q31 = new Question(31, "What does making a variable protected mean?", "It can only be accessed by that class.", 
 			"It can only be accessed by classes in the same package.", "It can be accessed by classes in the same project.", 
 			"It can only be accessed by objects that extend that class.", 3);
 	Question q32 = new Question(32, "What is the keyword for refering to your parent class?", "this", "that", "parent", "super", 3);
 	Question q33 = new Question(33, "What is the keyword for refering to your class (or yourself)?", "this", "that", "parent", "super", 0);
 	Question q34 = new Question(34, "What does overloading a method mean?", "Having the method defined multiple times, differing in the " +
 			"arguments that is takes in.", "Having the method defined multiple times, differing only in its return type.", "Definig a method " +
 			"differently than the way its parent has it implemented.", "Having the method defined multiple times, differing only in the code " +
 			"implemented in the method.", 0);
 	Question q35 = new Question(35, "What does overriding a method mean?", "Having the method defined multiple times, differing in the " +
 			"arguments that is takes in.", "Having the method defined multiple times, differing only in its return type.", "Definig a method " +
 			"differently than the way its parent has it implemented.", "Having the method defined multiple times, differing only in the code " +
 			"implemented in the method.", 2);
 	Question q36 = new Question(36, "What is wrong with the following code?\nint count = 0;\n Scanner scan = new Scanner(new File(filename));" +
			"\nwhile(scan.hasNextLine(){\ncount++;", "count should be decremented.", "There needs to be a scan.nextLine() to advance the scanner.", 
 			"There needs to be another scan.hasNextLine() to advance the scanner.", "Nothing is wrong.", 1);
 	Question q37 = new Question(37, "You first run your program and see an error that says \"Exception in thread \"main\" " +
 			"java.lang.ArrayIndexOutOfBoundsException: 0.\", yet you don't have any arrays declared in your class yet.  What is the likely " +
 			"cause of this error?", "Nothing, try it again.", 
 			"You probably did declare an array that you didn't know about.", "There isn't an argument in the run configurations.", 
 			"You have too many arguments in the run configurations.", 2);
 	Question q38 = new Question(38, "You are getting a null pointer exception error on a line where you are adding an Integer object to an" +
 			"arraylist called intArrayList.  What do you believe the problem could be?", "The ArrayList wasn't declared to take in Integers.", 
 			"The arraylist was declared but not initialized.", "The arraylist can't store any more values in it.", "Arraylists can't hold " +
 			"Integers in them.", 1);
 	Question q39 = new Question(39, "What does F5 do while in the debugger?", "Steps into a method that is on the line where the degugger" +
 			"currently is.", "Steps over that line and goes to the next.", "Step out of the current method.", "Goes to the next breakpoint.", 0);
 	Question q40 = new Question(40, "What does F6 do while in the debugger?", "Steps into a method that is on the line where the degugger" +
 			"currently is.", "Steps over that line and goes to the next.", "Step out of the current method.", "Goes to the next breakpoint.", 1);
 	Question q41 = new Question(41, "What does F7 do while in the debugger?", "Steps into a method that is on the line where the degugger" +
 			"currently is.", "Steps over that line and goes to the next.", "Step out of the current method.", "Goes to the next breakpoint.", 2);
 	Question q42 = new Question(42, "What does F8 do while in the debugger?", "Steps into a method that is on the line where the degugger" +
 			"currently is.", "Steps over that line and goes to the next.", "Step out of the current method.", "Goes to the next breakpoint.", 3);
 	Question q43 = new Question(43, "While inside of a method that has a void return type, there is a \'return\' statement within a loop.  " +
 			"What happens when this statement is hit?", "The current iteration of the loop stops and the conditional statement is checked.", 
 			"The loop is exited and the code after the loop is executed.", "The method is exited.", "Nothing happens.", 2);
 	Question q44 = new Question(44, "While inside of a method that has a void return type, there is a \'break\' statement within a loop.  " +
 			"What happens when this statement is hit?", "The current iteration of the loop stops and the conditional statement of the loop " +
 			"is checked.", "The loop is exited and the code after the loop is executed.", "The method is exited.", "Nothing happens.", 1);
 	Question q45 = new Question(45, "While inside of a method that has a void return type, there is a \'continue\' statement within a loop.  " +
 			"What happens when this statement is hit?", "The current iteration of the loop stops and the conditional statement of the loop " +
 			"is checked.", "The loop is exited and the code after the loop is executed.", "The method is exited.", "Nothing happens.", 0);
 	//Question q46 = new Question(46, "What is the difference between ")
 	
 	
 	
 	//array of questions, ripe for the plucking
 	Question[] qList = {q1, q2, q3, q4, q5, q6, q7, q8, q9, q10, q11, q12, q13, q14, q15, q16, q17, q18, q19, q20, q21, q22, q23, q24, q25, 
 			q26, q27, q28, q29, q30, q31, q32, q33, q34, q35, q36, q37, q38, q39, q40, q41, q42, q43, q44, q45};
 	
 	Random rand;
 
 	//returns a random question, yo
 	public Question getQuestion(){
 		rand = new Random();
 		long offset = System.currentTimeMillis() % 100;
 		long num = rand.nextInt() / 7;
 		num = Math.abs(num);
 		num += offset;
 		num %= qList.length;
 		return qList[(int)num];
 	}
 
 }
