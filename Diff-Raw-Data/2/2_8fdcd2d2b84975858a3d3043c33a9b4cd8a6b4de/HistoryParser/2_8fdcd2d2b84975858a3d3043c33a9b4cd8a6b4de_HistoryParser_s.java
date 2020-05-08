 package primary;
 
 import java.io.*;
 import java.util.*;
 
 
 //Source of IO reading code: stackoverflow.com
 
 public class HistoryParser {
 	
 	//bundle gets the root name of our repository address from the config.properties file
 	private ResourceBundle bundle = ResourceBundle.getBundle("config");
 	
 	// args holds the string array of passed parameters from the command line
 	private String[] args;
 	
 	private int[] ceiling;
 	
 	private LinkedList<RevisionNode> initiallyRelevant = new LinkedList<RevisionNode>();
 	
 	/**
 	 * Constructor: allows for access in the main code
 	 * @param arg file names to be looked for in the code
 	 * @throws Exception 
 	 */
 	public HistoryParser(String[] arg) throws Exception {
 		args = arg;  // gets the user-requested files
 		ceiling = new int[args.length];
 		ceiling = checkOver(args, ceiling);
 		int i; //loop counter
 		for (i = 0; i < args.length; i++) {
 			if (args[i].endsWith("/")) { //removes a end slash to avoid confusion in the later statistics and output
 				args[i] = args[i].substring(0, args[i].lastIndexOf('/'));
 			}
 		}
 	}
 	
 	public int[] checkOver(String[] args, int[] ceiling) throws Exception {
 		if (args.length == 0) {
 			throw new Exception("No Files");
 		}
 		int i,j; //loops counters
 		
 		//the 2 for loops check each entry with every other in order to prevent duplicate entries from affecting the data
 		for (i=0; i < args.length; i++){
 			if (args[i].contains("(")) { //implying that the user has specified a starting point before the newest revision
 				ceiling[i] = Integer.parseInt(args[i].substring(args[i].indexOf('(') + 1, args[i].indexOf(')')));
 				args[i] = args[i].substring(0, args[i].indexOf('(')); //remove the number to prevent complications later
 			}
 			for(j = i+1; j < args.length; j++){
 				if (args[i].contains("(")) { //remove and store later revision filter data so that equal names are not missed
 					ceiling[i] = Integer.parseInt(args[i].substring(args[i].indexOf('(') + 1, args[i].indexOf(')')));
 					args[i] = args[i].substring(0, args[i].indexOf('('));  //remove the number to prevent complications later
 				}
 				if (args[i].equals(args[j])){ //2 identical file names found = error and exception
 					throw new Exception("2 queried files are the same file: " + args[i]); //gives a reason for the exception
 				}
 			}
 		}
 		return ceiling; //return the revision filters to be used in the creation of several processes 
 	}
 	
 	/**
 	 * nodeCycle goes through the svn log of a single file and collects the commit data by parsing the 
 	 * entries present on the log
 	 * @param exec process to be called and executed, concept based on examples from stackOverflow.com
 	 * @param argNum how many files were entered to be logged and examined
 	 * @throws Exception if the file does not exist in the repository at the current time, or specified time
 	 */
 	public void nodeCycle(Process exec, int argNum) throws Exception{
 		
 		//from stackOverflow.com example
 		BufferedReader  stdInput=  new  BufferedReader(new
 	              InputStreamReader(exec.getInputStream()));
 		
 		//holds the current line of input in String form
 		String s;
 		//holds the split string
 		String[] ss;
 		String userList = "";
 		//revision list
 		String rev = "thisIsNotARevision";
 		//list of revision dates
 		String date = "";
 		//counter for how many files are changed
 		int count = 0;
 		String comment = "";
 		String undesirable = bundle.getString("unwanted");
 		
 		// while there is input to process, execute this loop
 		while  ((s=  stdInput.readLine())  !=  null)  {
 			
 			if (s.startsWith("r") && s.contains("|")) {  //a line starting with a lower case r implies that we are at a new revision
 				if (count != 0) {  // check to see whether or not this is the first iteration
 					
 					//the if below checks to see if the the current revision has been explicitly rejected by the user in the config file
					if (!(undesirable.contains(" " + rev + " ") || undesirable.contains(":" + rev + " ") || undesirable.contains(" " + rev + "."))) {
 						RevisionNode thisNode = new RevisionNode(date, rev, args.length, userList, count, comment);
 						thisNode.newRelevantFile(args[argNum]);
 						sortedInsert(initiallyRelevant, thisNode);
 					}
 					comment = ""; //clear the revision comment
 					count = 0;  //reset the counter
 				}
 				ss = s.split(" "); //split the string along white spaces
 				rev = ss[0].substring(1); //gets the revision number at the very beginning of s, removing the r to just get the number
 				if (ss[4].equals("|")){
 					date = ss[5] + " " + ss[6]; 
 					userList = ss[2] + ss[3];
 				}
 				else {
 					date = ss[4] + " " + ss[5];  // gets both the date and time of the revision
 					userList = ss[2];
 				}
 			}
 			//indicates that the line details a change of some sort in the file
 			else if (s.startsWith("   M") || s.startsWith("   A") || s.startsWith("   D") || s.startsWith("   R")){
 				count++; //increase the counter for files changed in a certain revision
 			}
 			else if (!s.equals("Changed paths:") && !s.contains("-------") && !s.startsWith("CVS: ")) { //filters out generally useless lines
 				comment += s + " "; //enters the comment data
 			}
 		}	
 		if (rev.equals("thisIsNotARevision")) { //implies no revisions were ever found for this file and it therefore does not exist
 			throw new Exception("User did not enter the names or starting revision properly");
 		}
 		
 		//used to get the last revision that was cut off by the for loop
 		if (!(undesirable.contains(" " + rev + " ") || undesirable.contains(":" + rev + " ") || undesirable.contains(" " + rev + "."))) {
 			RevisionNode thisNode = new RevisionNode(date, rev, args.length, userList, count, comment);
 			thisNode.newRelevantFile(args[argNum]);
 			sortedInsert(initiallyRelevant, thisNode);
 		}
 	}
 	
 	/**
 	 * sortedInsert takes a RevisionNode and places it into the chosen RevisionNode linked list in such a way that
 	 * the descending order of the revision numbers is preserved
 	 * @param list the list that the new RevisionNode will be inserted into
 	 * @param node the RevisionNode to be inserted
 	 */
 	public void sortedInsert(LinkedList<RevisionNode> list, RevisionNode node){
 		
 		int i; //loop counter
 		if (list.peek() == null){ //checks if the list is empty
 			list.addFirst(node); //if the list is empty then simply place the node at the head
 		}
 		else { //otherwise the node must be compared to the node currently present in the list
 			i = 0; //set i to zero to get the full range of the list
 			while (i < list.size()){ //iterates through the whole list until the correct location is found
 				
 				/*
 				 * since compare returns 1 if the first argument is greater then the second, 
 				 * then it implies that node's revision is to small to be placed in front of this currently present node
 				 */
 				if (list.get(i).compare(node) > 0) { 
 					i++; //if this is the case, then prepare to check the next node in the list
 				}
 				else if (list.get(i).compare(node) == 0){  //if the node is equal to the other
 					RevisionNode thatNode = list.remove(i); //remove the node from its current place
 					int j; //loop counter
 					for (j = 0; j < node.getRelevantFiles().size(); j++) {
 						String file = node.getRelevantFiles().get(j);
 						if (node.getRelevantFiles().contains(file)) {
 							thatNode.newRelevantFile(file); //uses the functions built-in conflict checking to avoid putting in the same file twice
 						}
 					}
 					list.add(i, thatNode); // return the node to its original location
 					return; //end the function call
 				}
 				else {
 					break; //if the nodes revision is greater, then it should be placed at index i, so the loop ends early
 				}
 			}
 			list.add(i, node); //place the node in it proper place
 			//if the loop fully completes then it implies that node's revision number is the smallest and so it is placed at the end
 		}
 	}
 	
 	/**
 	 * prints out the boundary lines at regular lengths when needed for the output
 	 * @param line the line of characters to be printed
 	 */
 	public void linesAndBounds(String line, PrintWriter out) {
 		int j; //loop counter
 		System.out.print("\n"); //spacing to avoid conflict with valuable data
 		out.print("\n");
 		for (j = 0; j < Integer.parseInt(bundle.getString("lineLengths")); j++) { //create a line break to separate the query print out from the data table
 			System.out.print(line); //indicates the end of the list of queried files
 			out.print(line);
 		}
 		System.out.print("\n"); //provide spacing between output
 		out.print("\n");
 	}
 
 	/**
 	 * printHistoryInformation both processes the log information of a Subversion repository through the methods above
 	 * and then prints out the names of the queried files along with all of the information stored in the RevisionNode
 	 * linked list in a table-style format
 	 * @throws Exception for various input errors
 	 */
 	public void printHistoryInformation() throws Exception{
 		
 		int i;							//loop counter
 		long standard;					//holds the average time between every single revision in the repository
 		boolean inRange;				//records whether the current time interval is within the chosen range
 		boolean lastWas = false;		//Records whether the last time interval was within range of the configuration bound
 		
 		//based on example at http://www.abbeyworkshop.com/howto/java/writeText/index.html
 		FileWriter outFile = new FileWriter(bundle.getString("textFile"));
         PrintWriter out = new PrintWriter(outFile);
         
 		if (bundle.getString("queryToggle").equals("true")) {
 			System.out.println("Queried Files:"); //indicates the next lines show what was entered on the command line
 			out.println("Queried Files:");
 		}
 		
 		String p = bundle.getString(bundle.getString("repo")); //uses the config.properties file to get the path to the svn working copy being used
 		
 		for (i = 0; i < args.length; i++){  //loops for every specified file
 			if (!args[i].startsWith("/")){ //all command line arguments must start with a / so it is checked if that is the case
 				args[i] = "/" + args[i]; //if not then the / is added to the argument at runtime
 			}
 			String n = p + args[i];
 			Process exec;				//the command to be executed 
 			if (ceiling[i] == 0) { //if 0, then no revision was specified
 				if (bundle.getString("queryToggle").equals("true")) {
 					System.out.println("\n" + args[i] + "\t (the full log history)"); //prints the files name and path from the start of the working copy
 					out.println("\n" + args[i] + "\t (the full log history)");
 				}
 				exec = Runtime.getRuntime().exec("svn log -v " + n); //uses the svn's log command to get the history of the queried file
 			}
 			else {
 				if (bundle.getString("queryToggle").equals("true")) {
 					System.out.println("\n" + args[i] + "\t (starting at revision " + ceiling[i] + ")"); //prints the file name and the most recent selected revision
 					out.println("\n" + args[i] + "\t (starting at revision " + ceiling[i] + ")");
 				}
 				exec = Runtime.getRuntime().exec("svn log -v " + n + "@" + ceiling[i]); //svn command starting at a particular revision
 			}
 			nodeCycle(exec, i); //execute the command and collect the desired data
 		}
 		if (bundle.getString("revisionOverallToggle").equals("true")) {
 			standard = fullTimeAverage(); //gets the average time between all revisions in the chosen repository
 			System.out.println("\n Average Time Period Between ALL Revisions: " + standard + " hours"); //and then print that time
 			out.println("\n Average Time Period Between ALL Revisions: " + standard + " hours");
 		}
 		else {
 			standard = Long.MAX_VALUE-(long)Integer.parseInt(bundle.getString("consecutiveRange")); //else just use the percentage as a base for the time ranges
 		}
 		int j; //loop counter
 		if (bundle.getString("tableToggle").equals("true")) {
 			linesAndBounds("==========", out);
 			System.out.print("\n"); //provide spacing between output
 			System.out.println("Legend: "); //this explains the meanings behind the symbols and lines used in the table
 			System.out.println("\t: \t\tindicates the time between this revision and the one before it is not \n\t\t\tin the selected range from the overall average\n");
 			System.out.println("\t: \t\tindicates that this revision and the one above it are in the intrval while \n\t\t\tthe current revision and the one below \n\t\t\titself is also within the specified range");
 			System.out.println("\t: \t\tindicate the bottom revision of a pair that have a time period within the \n\t\t\tdesired range but not with the one below itself");
 			System.out.println("\tA Line Of #: \tthe revisions between two of these are within the user selected \n\t\t\t interval range, starting from the most recent revision\n");
 			System.out.println("\tA Line Of -: \tthe revisions separated by these are within the user selected \n\t\t\t interval range\n");
 			System.out.print("\n"); //provide spacing between output
 			System.out.print("commit \t date \t\t\t relevants \t     changed \t rating \t\t rating comment \t\t\t\t actual relevant files");
 			
 			out.print("\n"); //provide spacing between output
 			out.println("Legend: "); //this explains the meanings behind the symbols and lines used in the table
 			out.println("\t: \t\tindicates the time between this revision and the one before it is not \n\t\t\tin the selected range from the overall average\n");
 			out.println("\t: \t\tindicates that this revision and the one above it are in the intrval while \n\t\t\tthe current revision and the one below \n\t\t\titself is also within the specified range");
 			out.println("\t: \t\tindicate the bottom revision of a pair that have a time period within the \n\t\t\tdesired range but not with the one below itself");
 			out.println("\tA Line Of #: \tthe revisions between two of these are within the user selected \n\t\t\t interval range, starting from the most recent revision\n");
 			out.println("\tA Line Of -: \tthe revisions separated by these are within the user selected \n\t\t\t interval range\n");
 			out.print("\n"); //provide spacing between output
 			out.print("commit \t date \t\t\t relevants \t     changed \t rating \t\t rating comment \t\t\t\t actual relevant files");
 			linesAndBounds("##########", out); //the lines used to separate the information rows
 		}
 
 		int[] statArray = new int[10]; //array used for the rating scattering table
 		double interval = 0; //how long the current interval is
 		int n = 0; //counts how many revisions there are right now in the current interval
 		LinkedList<Integer> allN = new LinkedList<Integer>(); //holds the various interval information
 		LinkedList<RevisionNode> history = initiallyRelevant; //gets the relevant revision data
 		for (i = 0; i < history.size(); i++){ //iterates through the entire RevisionNode list to print out its collected data
 			RevisionNode current = history.get(i); //takes the next node to be printed
 			fillArray(statArray, current.getRating()); //takes the current rating and plots it in the table
 			double newSpace; //space between the current two observed revisions
 			if (i < history.size()-1) { //if there is a revision after the current one
 				newSpace = (double)current.getTimeSpace(history.get(i + 1).getThisTime()) / 1000 / 60 / 60.0; //newSpace becomes the interval between the two
 				interval += newSpace; //increase interval time counter by the new interval
 			}
 			else {
 				newSpace = 0; //null the space
 				interval = Long.MAX_VALUE; //and put the interval to its maximum size
 			}
 			if (bundle.getString("revisionOverallToggle").equals("true")) {
 				if (newSpace > (double)standard + Integer.parseInt(bundle.getString("consecutiveRange")) * standard / 100 ||  newSpace < (double)standard-Integer.parseInt(bundle.getString("consecutiveRange"))*standard/100) {
 					inRange = false; //if the node is outside the specified range based on the configuration file parameters
 				}
 				else {
 					inRange = true; //if it is within range
 				}
 			}
 			else {
 				inRange = false; //permanently set it to false due to branch conditions
 			}
 			
 			if (i+1 == history.size() && lastWas) { //indicate that there will only be one more revision so the next symbol should by a triangle
 				inRange = false; //prevents a diamond from appearing
 			}
 			else if (i+1 == history.size()) { //otherwise simply remove the chance of a symbol from appearing
 				inRange = false;
 				lastWas = false;
 			}
 			
 			if (bundle.getString("tableToggle").equals("true")) {
 				if (inRange && !lastWas) {
 					System.out.print(" "); //first revision in a time-pair
 					out.print(" ");
 					lastWas = true; //flags the next revision to note its relationship to the previous (ie: this) revision
 				}
 				else if (inRange && lastWas) { 
 					System.out.print(" "); //revision that shares a time relationship with revisions directly above and below it
 					out.print(" ");
 				}
 				else if (!inRange && lastWas) {
 					System.out.print(" "); //only has a relationship with the revision above it, not below it
 					out.print(" ");
 					lastWas = false; //indicate to the next revision that the last revision is not important to it
 				}
 				else {
 					System.out.print(" "); //indicate no relationships around this node
 					out.print(" ");
 				}
 				System.out.print(current.toString()); //prints the String representation of all the nodes data
 				out.print(current.toString());
 				if (interval <= Double.parseDouble(bundle.getString("intervalLength"))) {
 					linesAndBounds("----------", out); //the lines used to separate the information rows
 				}
 				else {
 					linesAndBounds("##########", out); //end of a desired interval
 				}
 			}
 			
 			if (interval <= Double.parseDouble(bundle.getString("intervalLength"))) {
 				n++; //indicates another revision was in the current interval range
 			}
 			else if (interval > Double.parseDouble(bundle.getString("intervalLength"))) {
 				n++; //counts the last revision
 				allN.addFirst(n); //adds the new interval readings
 				n = 0; //reset the counter
 				interval = 0; //put the interval back to it original count, zero
 			}
 		}
 		
 		System.out.println("\n"); //spacing
 		out.println("\n");
 		NodeStatistics stats = new NodeStatistics(history, args, allN); // prepares to process data held in the RevisionNode list
 		stats.statsOut(out); //output the statistics to the screen
 		
 		System.out.println("\n");
 		out.println("\n");
 		if (bundle.getString("ratingsToggle").equals("true")) {
 			System.out.println("Rating Graph: looking for grouping\n"); //title of the graph
 			out.println("Rating Graph: looking for grouping\n");
 			for (i = 1; i <= statArray.length; i++){
 				System.out.print("(" + (double) (i-1) / 10 + ", " + (double) i / 10 + "]:  "); //current range interval
 				out.print("(" + (double) (i-1) / 10 + ", " + (double) i / 10 + "]:  ");
 				for (j = 0; j < statArray[i-1]; j++){ //loop through the entire range of rating intervals
 					System.out.print("|"); // one '|' = one rating in this range
 					out.print("|");
 				}
 				System.out.print("  (" + statArray[i-1] + ")"); //print out the numerical representation of that interval for easier use
 				out.print("  (" + statArray[i-1] + ")");
 				linesAndBounds("==========", out); //indicates the end of the list of queried files
 			}
 		}
 
 		if (bundle.getString("occurrencesToggle").equals("true")) {
 			fullCount(out); //counts out how many times every file name occurs in the entire repository history
 		}
 		out.close();
 	}
 	
 	/**
 	 * gets the average time interval between all (relevant and irrelevant) revisions
 	 * @return the average time
 	 * @throws IOException input error while reading from the console
 	 */
 	public long fullTimeAverage() throws IOException {
 		String p = bundle.getString(bundle.getString("repo")); //get the repository URL
 		String s;//current input line
 		String[] ss; //breaking down the line to get the date information
 		String previous = ""; //the last line's date stamp
 		String current = ""; //the current lines date stamp
 		long totalTime = 0; //the sum of all interval times
 		int rev = 0; //how many revisions there were in total
 		Process exec = Runtime.getRuntime().exec("svn log " + p + " -q");
 		BufferedReader  stdInput =  new  BufferedReader(new
 	              InputStreamReader(exec.getInputStream()));
 		
 		while  ((s =  stdInput.readLine())  !=  null)  {
 			
 			if (s.startsWith("r")) {  //a line starting with a lower case r implies that we are at a new revision
 				
 				ss = s.split(" "); //split the string along white spaces
 				if (ss[4].equals("|")){
 					current = (ss[5] + " " + ss[6]); 
 				}
 				else {
 					current = (ss[4] + " " + ss[5]);  // gets both the date and time of the revision
 				}
 				Calendar thisTime = new GregorianCalendar(Integer.parseInt(current.split(" ")[0].split("-")[0]), Integer.parseInt(current.split(" ")[0].split("-")[1])-1, Integer.parseInt(current.split(" ")[0].split("-")[2]), Integer.parseInt(current.split(" ")[1].split(":")[0]), Integer.parseInt(current.split(" ")[1].split(":")[1]), Integer.parseInt(current.split(" ")[1].split(":")[2]));
 				if (!previous.equals("")){ //implies this is not the first iteration
 					Calendar lastTime = new GregorianCalendar(Integer.parseInt(previous.split(" ")[0].split("-")[0]), Integer.parseInt(previous.split(" ")[0].split("-")[1])-1, Integer.parseInt(previous.split(" ")[0].split("-")[2]), Integer.parseInt(previous.split(" ")[1].split(":")[0]), Integer.parseInt(previous.split(" ")[1].split(":")[1]), Integer.parseInt(previous.split(" ")[1].split(":")[2]));
 					long timeDiff = lastTime.getTimeInMillis()-thisTime.getTimeInMillis();
 					rev++;
 					totalTime += timeDiff;
 				}
 				previous = current;
 			}
 		}
 		return (((totalTime / rev) / 1000) / 60) / 60;
 	}
 	
 	public void fullCount(PrintWriter out) throws IOException {
 		String p = bundle.getString(bundle.getString("repo"));
 		String s;
 		String[] ss;
 		String current = "";
 		int count = 0;
 		Process exec = Runtime.getRuntime().exec("svn log " + p + " -q -v");
 		BufferedReader  stdInput=  new  BufferedReader(new
 	              InputStreamReader(exec.getInputStream()));
 		CounterList<String> counter = new CounterList<String>(true);
 		
 		while  ((s =  stdInput.readLine())  !=  null)  {
 			if (s.startsWith("   M") || s.startsWith("   A") || s.startsWith("   D") || s.startsWith("   R")){
 				ss = s.split(" ");
 				current = ss[4];
 				counter.newInput(current);
 				count++;
 			}
 		}
 		System.out.println("start");
 		out.println("start");
 		System.out.println(counter.toString());
 		out.println(counter.toString());
 	}
 	
 	/**
 	 * Takes the rating of a particular revision and uses it to place in an array
 	 * designed to store the rating ranges
 	 * @param array the array that stores how many ratings fall in specific value ranges
 	 * @param rating the current rating reached in  the code
 	 */
 	public void fillArray(int[] array, double rating){ 
 		int i = 0; // loop counter
 		while (i <= array.length){ //while in the bounds of the array...
 			if (rating <= (double) i / 10){ // checks if this is the interval that the rating falls in
 				array[i-1] = array[i-1] + 1; //if so the that interval section is incremented to indicate that change of the data
 				break; //end the loop since 1 rating is only in one interval
 			}
 			i++;
 		}
 	}
 }
