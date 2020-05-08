 package euler;
 
 import java.io.*;
 import java.text.DecimalFormat;
 import java.util.*;
 
 import euler.problems.Problem;
 
 public class EulerMain {
 
 	public static void main(String[] args) {
 		ArrayList<Problem> problemList = new ArrayList<Problem>();
 		HashMap<Problem, Boolean> solutionFound = new HashMap<Problem, Boolean>();
 		HashMap<Problem, Long> executionTime = new HashMap<Problem, Long>();
 		String debugProblem = "000"; //Ignore if 000.
 		System.out.println("#############################");
 		System.out.println("#  Welcome to Euler-admin.  #");
 		System.out.println("#          made by          #");
 		System.out.println("#  Jacob Bang (2011->2012)  #");
 		System.out.println("#############################");
 		System.out.println();
 
 		if (!debugProblem.contentEquals("000")) {
 			ArrayList<String> temp = new ArrayList<String>();
 			temp.add("euler.problems.Problem" + debugProblem);
 			
 			problemList = getProblems(temp);
 			
 		} else if (args.length < 1) {
 			System.out.println("This program requires one or more parameters:");
 			System.out.println("* To run all problems the parameter 'all' is used without the '-character.");
 			System.out.println();
 			System.out.println("* A single problem can be run by typing the ID of the problem as a\n" + 
 							   "  parameter. (Like 001).");
 			System.out.println();
 			System.out.println("* Several problems can be run by typing the ID of each problem in the\n" + 
 						       "  order they should be run (like 002 001 003)");
 			
 			System.exit(0);
 		} else if (args[0].contentEquals("all")) {
 			problemList = getProblems("euler/problems");
 
 		} else {
 			ArrayList<String> fileNames = new ArrayList<String>();
 
 			for (String arg : args) {
 				fileNames.add("euler.problems.Problem" + arg);
 			}
 			
 			problemList = getProblems(fileNames);
 		}
 
 		for (Problem problem : problemList) {
 			long startTime, endTime;
 			
 			System.out.println("--------------------------------------------------------------------------------");
 			System.out.printf("PROBLEM:\t%03d\t\n\n", problem.getID());
 			System.out.print("DESCRIPTION:\t");
 
 			String [] lines = wrapText(problem.getDescription(), 50);
 			for (String line : lines) {
 				System.out.println(line.replaceAll("", " "));
 				System.out.print("\t\t");
 			}
 			System.out.println();
 			System.out.println("----------[STARTED]----------");
 			startTime = System.currentTimeMillis();
 			double answer = problem.test(true);
 			endTime = System.currentTimeMillis();
 			System.out.println("----------[STOPPED]----------");
 
 			System.out.println();
 			System.out.println("Time taken: " + (endTime - startTime) + " msec");
 			
 			//Thanks to: http://ubuntuforums.org/showpost.php?p=7945847&postcount=6
 			DecimalFormat df = new DecimalFormat("#.######"); 
 			System.out.printf("Output: " + df.format(answer)+"\n",answer);
 			System.out.printf("Answer: " + df.format(problem.getAnswer())+"\n",problem.getAnswer());
 			System.out.print("Equal: ");
 
 			if (answer == problem.getAnswer()) {
 				solutionFound.put(problem, true);
 				System.out.println("[TRUE]");
 			} else {
 				solutionFound.put(problem, false);
 				System.out.println("[FALSE]");
 			}
 			
 			executionTime.put(problem, (endTime - startTime));
 		}
 		
 		if (problemList.size() > 1) {
 			System.out.println();
 			System.out.println("----------------------------------");
 			System.out.println("------[Summary of Execution]------");
 			System.out.println("----------------------------------");
 			System.out.println("| Nr. | Correct | Execution time |");
 			
 			for (Problem problem : problemList) {
 				if (solutionFound.get(problem)) {
 					System.out.printf("| %03d |   YES   | %,9d msec |\n", problem.getID(), executionTime.get(problem));
 				} else {
 					System.out.printf("| %03d |  ERROR  | %,9d msec |\n", problem.getID(), executionTime.get(problem));
 				}
 			}
 			System.out.println("------------------------------------------------");
 			
 			int sum = 0;
 			for (long value : executionTime.values()) {
 				sum += value;
 			}
 			System.out.printf("| Execution time of problems: %,11d msec |\n", sum);
 			System.out.println("------------------------------------------------");
 		}
 	}
 	
 	static ArrayList<String> getClassNames(String folderPath) {
 		ArrayList<String> fileNames = new ArrayList<String>();
 		File folder = new File(folderPath);
 		File[] listOfFiles = folder.listFiles();
 		
 		System.out.println(folder.getAbsolutePath());
 		
 		for (File file : listOfFiles) {
 			//Check if file is a file and filename matches regx "Problem???.class" where ? can be any number
 			if (file.isFile() && file.getName().matches("Problem\\d\\d\\d.class")) {
 				fileNames.add("euler.problems." + file.getName().replaceFirst(".class", ""));
 			}
 		}
 		
 		return fileNames;
 	}
 	
 	static ArrayList<Problem> getProblems(String folderPath) {
 		return getProblems(getClassNames(folderPath));
 	}
 	
 	static ArrayList<Problem> getProblems(List<String> classNames) {
 		ArrayList<Problem> problems = new ArrayList<Problem>();
 		
 		for (String problemClassName : classNames) {
 			try {
 				System.out.println("Loading: " + problemClassName);
 				problems.add((Problem)ClassLoader.getSystemClassLoader().loadClass(problemClassName).newInstance());
 			} catch (ClassNotFoundException e) {
 				System.err.println("Class not found: " + problemClassName);
 				e.printStackTrace();
 				System.exit(1);
 			} catch (Exception e) {
 				System.out.println("Unknown problem with class: " + problemClassName);
 				e.printStackTrace();
 				System.exit(1);
 			}
 		}
 		
 		return problems;
 	}
 
 	static String[] wrapText(String text, int len) {
 		// return empty array for null text
 		if (text == null)
 			return new String[] {};
 		
 		//get rip of newlines
 		if (text.contains("\n")) {
 			ArrayList<String> lines = new ArrayList<String>();
 
 			for (String line : text.split("\n")) {
 				if (line.contentEquals("")) {
 					lines.add("");
 				} else {
 					lines.addAll(Arrays.asList(wrapText(line, len)));
 				}
 			}
 
 			return lines.toArray(new String[] { });
 			
 		} else {
 			// return text if len is zero or less
 			if (len <= 0)
 				return new String[] { text };
 
 			// return text if less than length
 			if (text.length() <= len)
 				return new String[] { text };
 
 			char[] chars = text.toCharArray();
 			Vector<String> lines = new Vector<String>();
 			StringBuffer line = new StringBuffer();
 			StringBuffer word = new StringBuffer();
 
 			for (int i = 0; i < chars.length; i++) {
 				word.append(chars[i]);
 
 				if (chars[i] == ' ') {
					if ((line.length() + word.length()) > len && line.length() > 0) {
 						lines.add(line.toString());
 						line.delete(0, line.length());
 					}
 
 					line.append(word);
 					word.delete(0, word.length());
 				}
 			}
 
 			// handle any extra chars in current word
 			if (word.length() > 0) {
 				if ((line.length() + word.length()) > len) {
 					lines.add(line.toString());
 					line.delete(0, line.length());
 				}
 				line.append(word);
 			}
 
 			// handle extra line
 			if (line.length() > 0) {
 				lines.add(line.toString());
 			}
 
 			String[] ret = new String[lines.size()];
 			int c = 0; // counter
 			for (Enumeration<String> e = lines.elements(); e.hasMoreElements(); c++) {
 				ret[c] = (String) e.nextElement();
 			}
 
 			return ret;
 		}
 	}
 
 }
