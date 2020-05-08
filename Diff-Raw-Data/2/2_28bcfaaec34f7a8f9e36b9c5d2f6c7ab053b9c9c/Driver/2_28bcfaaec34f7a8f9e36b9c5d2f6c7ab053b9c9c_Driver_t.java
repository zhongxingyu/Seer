 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Scanner;
 
 import javax.swing.JOptionPane;
 
 
 /**
 * Project #3
  * CS 2334, Section 010
  * Oct 1, 2013
  * <P>
  * This class will allow you to read a file of publication information, search through the data, and write data onto a file.
  * </P>
  *@version 1.0
  */
 public class Driver {
 	
 	int count=0;
 	
 	static HashMap<String, Publication> pubList;
 	
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
 			
 			//For loop for repeating search
 			for(int j=0; j==0;) {
 			
 				//Save input as the choice
 				
 				String choice = JOptionPane.showInputDialog ( "Choose a criteria to serach by entering the coressponding digit: \n\n1. Name of Author \n2. Name of Paper/Article \n3. Exit the Program" );
 				
 				if(choice == null)
 					System.exit(-1);
 				
 				int num = Integer.valueOf(choice);
 		
 				//Manages choice using if statements
 				
 				String search = "";
 				if(num==1){ search = JOptionPane.showInputDialog ( "You are searching by Name of Author. Please enter a name to search for" );}
 				else if(num==2){search = JOptionPane.showInputDialog ( "You are searching by Name of Article/Paper. Please enter a name to search for");}
 				else if(num==3){i=1; System.exit(-1);}
 				else JOptionPane.showMessageDialog(null, "Please enter a valid option number.");
 				
 				//Search for data here using specified criteria type
 				String results = "";
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
 						ArrayList<String> foundAuthorPapers = foundAuthor.getPublishedPapers();
 						
 						for(String paper : foundAuthorPapers)
 						{
 							results += pubList.get(paper).toString() + "\n" ;
 						}
 						
 						resultsBool = true;
 					}
 				}
 				else if(num==2)
 				{
 					Publication foundPub = pubList.get(search);
 					
 					if(foundPub != null)
 						
 					{
 						results += foundPub.toString() + "\n" ;
 						resultsBool = true;
 					}
 				}
 				
 				//JOptionPane that displays search results
 				if(resultsBool==true){
 					JOptionPane.showMessageDialog(null, results);
 					
 					//Yes and No JOptionPane used to continue searching or quit program
 					if (JOptionPane.showConfirmDialog(null, "Would you like to save the results?", "WARNING", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
 					    //Yes option
 						//Write to file
 						writePublications("Results.txt", results);
 						JOptionPane.showMessageDialog(null, "The results were written to the file Results.txt");
 					} else 
 					{
 						//No option
 						JOptionPane.showMessageDialog(null, "The results were not saved.");
 					}
 					
 				}
 				else if(resultsBool==false) JOptionPane.showMessageDialog(null, "There were no publications with that matched your search.");
 				
 				//Yes and No JOptionPane used to continue searching or quit program
 				if (JOptionPane.showConfirmDialog(null, "Do you wish to search again?", "WARNING", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
 				    //Yes option
 					j=0;
 				} else 
 				{
 					//No option
 					j=1;
 					in.close();
 					System.exit(-1);
 				}
 			
 			}
 			
 			
 			//Yes and No JOptionPane used to search using a different file or quit program
 			if (JOptionPane.showConfirmDialog(null, "Do you wish to search a different file?", "WARNING", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
 			    //Yes option
 				i=0;
 			} else 
 			{
 				//No option
 				i=1;
 				in.close();
 				System.exit(-1);
 			}
 		}
 	}
 	
 	/**
 	 * writes resultant database to disk
 	 * 
 	 * @return true on success, false on error 
 	 * @throws IOException
 	 */
 	public static void writePublications(String filename, String results) throws IOException {
 		FileOutputStream fileOutputStream = new FileOutputStream(filename);
 		ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
 		objectOutputStream.writeObject(results);
 		objectOutputStream.close();
 	}
 }
