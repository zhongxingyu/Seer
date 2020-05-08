 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Scanner;
 
 import javax.swing.JOptionPane;
 
 
 /**
  * Project #3
  * CS 2334, Section 010
  * Oct 1, 2013
  * <P>
  * This class will allow you to read a file of publication information, search through the data,
  *  write data onto a file, and display bar graphs of the data.
  * </P>
  *@version 1.0
  */
 public class Driver implements Serializable {
 	
 	/**
 	 * serial ID
 	 */
 	private static final long serialVersionUID = 5481149857232568707L;
 
 	/**
 	 * list of publishers
 	 */
 	static HashMap<String, Publication> pubList;
 	
 	/**
 	 * list of authors
 	 */
 	static HashMap<String, Author> authorList;
 	
 	public static void main (String[] args) throws IOException{
 		
 	//Scanner for user input data
 	Scanner in = new Scanner(System.in);
 	
 	//For loop for repeating search
 	for(int i=0; i==0;){
 	
 		//Ask for and read in the file
 		String filename = JOptionPane.showInputDialog ( "Please enter the name of the file" );
 		
 		if(filename == null)
 			System.exit(-1);
 		
 		Parser p = new Parser(filename);
 		
 			//Save input as the choice
 			String choice = JOptionPane.showInputDialog ( "Choose a criteria to search by entering the coressponding digit: \n\n1. Name of Author \n2. Name of Paper/Article \n3. Exit the Program" );
 			
 			if(choice == null)
 				System.exit(-1);
 			
 			int num = Integer.valueOf(choice);
 	
 			//Manages choice using if statements
 			
 			String search = "";
 			int testIfAuthor = 0;		//Tests to make sure search string is an author name, for use later when creating graph
 			if(num==1){ testIfAuthor = 1; search = JOptionPane.showInputDialog ( "You are searching by Name of Author. Please enter a name to search for" );}
 			else if(num==2){testIfAuthor = 0;search = JOptionPane.showInputDialog ( "You are searching by Name of Article/Paper. Please enter a name to search for");}
 			else if(num==3){i=1; System.exit(-1);}
 			else JOptionPane.showMessageDialog(null, "Please enter a valid option number.");
 			
 			//Search for data here using specified criteria type
 			ArrayList<Publication> results = new ArrayList<Publication>();
 			//the results of the search
 			Boolean resultsBool = false;
 			
 			//ArrayList of Publications made from Parser
 			pubList = new HashMap<String, Publication>();
 			pubList = p.getPublications();
 			
 			authorList = new HashMap<String, Author>();
 			authorList = p.getAuthors();
 			
 			//Search through publications
 			if(num==1)
 			{
 				Author foundAuthor = authorList.get(search);
 				if(foundAuthor != null)
 				{
 					ArrayList<Publication> foundAuthorPapers = foundAuthor.getPublishedPapers();
 					
 					for(Publication paper : foundAuthorPapers)
 					{
 						results.add(pubList.get(paper.getTitlePaper()));
 					}
 					
 					resultsBool = true;
 				}
 			}
 			else if(num==2)
 			{
 				Publication foundPub = pubList.get(search);
 				
 				if(foundPub != null)
 					
 				{
					results.add(pubList.get(foundPub));
 					resultsBool = true;
 				}
 			}
 			
 			//JOptionPane that displays search results
 			if(resultsBool==true){
 				
 				Object[] options = {"Author",
 	                    "Paper Title",
 	                    "Date"};
 				int sortingMethod = JOptionPane.showOptionDialog(null,
 						"Sort by Author or Paper Title?",
 						"What Sorting Method to Use?", 
 						JOptionPane.YES_NO_CANCEL_OPTION,
 					    JOptionPane.QUESTION_MESSAGE,
 					    null,
 					    options,
 					    options[1]);
 				
 				results.get(0);
 				Publication.compareMethod = sortingMethod;
 				
 				Collections.sort(results);
 				
 				String out="";
 				
 				for(Publication Pub : results)
 				{
 					out+=Pub.toString() + "\n";
 				}
 				
 				JOptionPane.showMessageDialog(null, out);
 				
 				//Yes and No JOptionPane used to continue searching or quit program
 				if (JOptionPane.showConfirmDialog(null, "Would you like to save the results?", "WARNING", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
 				    //Yes option
 					//Write to file
 					writePublications("Results.txt", out);
 					JOptionPane.showMessageDialog(null, "The results were written to the file Results.txt");
 				} else 
 				{
 					//No option
 					JOptionPane.showMessageDialog(null, "The results were not saved.");
 				}
 				
 				
 				//test to see if user searched for author, if not ask for author to use in graphs
 				if(testIfAuthor == 0){
 					search = JOptionPane.showInputDialog ( "Please enter an Author name to use in the graphs" );
 				}
 				
 				//graphing
 				for(int l=0;l==0;){
 					//Yes and No JOptionPane used to show a graph
 					Object[] graphOptions = {"TP",
 						"PY",
 						"CPY",
 						"JAY",
 						"NC",
 						"None"};
 					int graphChoice = JOptionPane.showOptionDialog(null,
 						"Would you like to graph information for this author? \nIf yes, please choose one of the following types of graph: \n\nTP: Type of Publication \nPY: Publications per Year \nCPY: Conference Papers per Year \nJAY: Journal Articles per Year \nNC: Number of co-authors per publication",
 						"Would you like to create a graph using this information?", 
 						JOptionPane.YES_NO_CANCEL_OPTION,
 						JOptionPane.QUESTION_MESSAGE,
 						null,
 						graphOptions,
 						graphOptions[5]);
 				
 					if(graphChoice<5){
 						Graph newGraph = new Graph((String) graphOptions[graphChoice],search, authorList);
 						newGraph.displayGraph();
 						
 						if (JOptionPane.showConfirmDialog(null, "Would you like to see another graph?", "WARNING", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
 							l=0;
 						}
 						else {
 							l=1;
 						}
 					}
 					else if(graphChoice==5){
 						l=1;
 					}
 				}
 			}
 			else if(resultsBool==false) JOptionPane.showMessageDialog(null, "There were no publications with that matched your search.");
 			
 			//Yes and No JOptionPane used to continue searching or quit program
 			if (JOptionPane.showConfirmDialog(null, "Do you wish to search again?", "WARNING", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
 			    //Yes option
 			} else 
 			{
 				//No option
 				in.close();
 				System.exit(-1);
 			}
 		
 		}
 		
 		
 		//Yes and No JOptionPane used to search using a different file or quit program
 		if (JOptionPane.showConfirmDialog(null, "Do you wish to search a different file?", "WARNING", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
 		    //Yes option
 		} else 
 		{
 			//No option
 			in.close();
 			System.exit(-1);
 		}
 	}
 	
 	/**
 	 * writes resultant database to disk
 	 * 
 	 * @throws IOException
 	 */
 	public static void writePublications(String filename, String results) throws IOException {
 		FileOutputStream fileOutputStream = new FileOutputStream(filename);
 		ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
 		objectOutputStream.writeObject(results);
 		objectOutputStream.close();
 	}
 	
 	/**
 	 * Writes text file to disk
 	 */
 	public static void writeFileString(){
 		
 	}
 	
 	/**
 	 * Writes binary file to disk
 	 */
 	public static void writeFileBin(){
 		
 	}
 	
 }
