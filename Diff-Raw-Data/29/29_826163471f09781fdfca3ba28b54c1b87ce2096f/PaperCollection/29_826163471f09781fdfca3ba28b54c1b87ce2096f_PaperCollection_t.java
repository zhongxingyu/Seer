 import java.io.BufferedReader;
import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Random;
 
 
 public class PaperCollection {
 	
 	private ArrayList<Paper> paperCollection = new ArrayList<Paper>();
 	
 	/**
 	 * Default constructor for the class.
 	 */
 	public PaperCollection() {}
 	
 	/**
 	 * Preferred constructor for the class.
 	 * Reads in a text file and constructs the proper paper based on the information given.
 	 * @param filepath File path of the text file that contains the list of papers and their details.
 	 * @throws IOException Thrown if a readLine error occurs.
 	 */
 	public PaperCollection(String filepath) throws IOException
 	{
 		FileReader fr = null;
 		String line = "";
 		
 		try {
 			fr = new FileReader(filepath);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 
 		BufferedReader br = new BufferedReader(fr);
 
 		line = br.readLine(); //Initial line grab
 		String[] paperInfo = new String[7]; //Holds the info for the current paper. Will be used in the construction of a new paper object.
 		int i = 0;
 
 		while (line != null) { //Until the end of the file
 			do { //Loops until a new magazine is found.
 				paperInfo[i++] = line;
 				line = br.readLine();
 				if (line == null) //End of file, break from the while loop since it's difficult to conditional this.
 					break;
 			} while (!(line.equalsIgnoreCase(""))); //Detection of a new paper.
 			if (paperInfo[0].equalsIgnoreCase("Journal Article")) //Create the appropriate constructor
 				paperCollection.add(new JournalArticle(paperInfo[0], paperInfo[1], paperInfo[2], paperInfo[3], paperInfo[4], paperInfo[5], paperInfo[6]));
 			else if (paperInfo[0].equalsIgnoreCase("Conference Paper"))
 				paperCollection.add(new ConferencePaper(paperInfo[0], paperInfo[1], paperInfo[2], paperInfo[3], paperInfo[4], paperInfo[5], paperInfo[6]));
 			else System.out.println("There's a major problem!"); //Should never be reached or we have a problem.
 			paperInfo = new String[7]; //Reset the information arrays
 			i = 0;
 			line = br.readLine();
 		}
 	}
 	
 	/**
 	 * Sorts the collection by a certain criteria.
 	 * 
 	 * May require a few smaller methods, but we'll see in implementation.
 	 * @param method Which element to sort by (ex. BI for bibliographic, AU for author, etc.)
 	 */
 	public void sort(String method)
 	{
 		Paper.setSortCriteria(method.toUpperCase());
 		if (method.equalsIgnoreCase("R"))
 			Collections.shuffle(paperCollection, new Random(System.nanoTime()));
 		else
 			Collections.sort(paperCollection);
 	}
 	
 	/**
 	 * Prints the data in the collection to a file on the drive
 	 * @param filepath Where you want to print the file to
 	 * @throws IOException Error in reading the file, in our out.
 	 */
 	public void printToFile(String filepath) throws IOException
 	{
		//Make a file to print to and open a buffer
		FileWriter outputFile = new FileWriter(filepath);
		BufferedWriter bw = new BufferedWriter(outputFile);
 
		//Go through each paper in the collection and print it to the file
		for(Paper eachPaper: paperCollection){
			bw.write(eachPaper.toString().replace(" // null", "").replace(" // ", "\n"));
			bw.newLine();
			bw.newLine();
		}
		//Close the buffer
		bw.close(); 
 	}
 	
 	/**
 	 * Prints the data in the collection to the screen for the user to view
 	 */
 	public void printToScreen()
 	{
 		for(Paper eachPaper:paperCollection)
 			System.out.println(eachPaper.toString().replace(" // null", "").replace(" // ", "\n") + "\n");
 	}
 	
 	/**
 	 * Searches the collection for a keyword.
 	 * @param searchCriteria The search query
 	 * @return A string containing the results of the search
 	 */
 	public Paper search(String searchTitle)
 	{
 		//Have to be certain the list is sorted.
 		sort("PT");
 		//Code from lab4.
 		int left  = 0;
 		int right = paperCollection.size() - 1;
 		
 		// While there are elements in the range [left,.., right].
 		while( right - left + 1 > 0) {
 			
 			// Pick the middle point of the range [left, ... , right].
 			int middleIndex = ( left + right ) / 2;
 			String middleElement = paperCollection.get(middleIndex).toString().split(" // ")[2];
 			int comparisonValue = middleElement.compareTo(searchTitle);
 			
 			if (comparisonValue < 0) {
 				left = middleIndex + 1;
 			}
 			else if (comparisonValue > 0) {
 				right = middleIndex - 1;				
 			}
 			else {
 				return paperCollection.get(middleIndex);
 			}
 		}
 		
 		// If the element was not found.
 		return null;	
 	}
 
 	/**
 	 * Gets the size of the list that contains the papers.
 	 * @return The size of the arrayList that the papers are stored in.
 	 */
 	public int size() {
 		return paperCollection.size();
 	}
 }
