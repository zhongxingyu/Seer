 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Scanner;
 
 import javax.swing.JOptionPane;
 
 
 /**
  * Project #2
  * CS 2334, Section 010
  * Oct 1, 2013
  * <P>
  * This class will allow you to read a file of publication information, search through the data, and write data onto a file.
  * </P>
  *@version 1.0
  */
 public class Driver {
 	
 	int count=0;
 	
 	public static void main (String[] args) throws IOException{
 		
 		//Scanner for user input data
 		Scanner in = new Scanner(System.in);
 		
 		//For loop for repeating search
 		for(int i=0; i==0;){
 		
 			//Ask for and read in the file
 			String filename = JOptionPane.showInputDialog ( "Please enter the name of the file" );
 			Parser p = new Parser(filename);
 			
 			//For loop for repeating search
 			for(int j=0; j==0;) {
 			
 				//Save input as the choice
 				
 				String choice = JOptionPane.showInputDialog ( "Choose a criteria to serach by entering the coressponding digit: \n\n1. Name of Author \n2. Name of Paper/Article \n3. Name of the Journal/Collection of Paper \n4. Exit the Program" );
 				int num = Integer.valueOf(choice);
 		
 				//Manages choice using if statements
 				
 				String search = "";
 				if(num==1){ search = JOptionPane.showInputDialog ( "You are searching by Name of Author. Please enter a name to search for" );}
 				else if(num==2){search = JOptionPane.showInputDialog ( "You are searching by Name of Article/Paper. Please enter a name to search for");}
 				else if(num==3){search = JOptionPane.showInputDialog ( "You are searching by Name of Journal/Collection of Papers. Please enter a name to search for");}
 				else if(num==4){i=1; System.exit(-1);}
 				else JOptionPane.showMessageDialog(null, "Please enter a valid option number.");
 				
 				//Search for data here using specified criteria type
 				String results = "";
 				//the results of the search
 				Boolean resultsBool = false;
 				
 				//ArrayList of Publications made from Parser
 				ArrayList<Publication> pubList = new ArrayList<Publication>();
 				pubList = p.getPublications();
 				
 				//Search through magazines. Check that all magazines are stored into one large arraylist
 				if(num==1)
 				{
 					for(Publication pub : pubList)
 					{
 						if(pub.getAuthorsString() != null && pub.getAuthorsString().contains(search))
 						{
 							results += pub.toString() + "\n" ;resultsBool = true;
 						}
 						else {resultsBool = false;}
 					}
 				}
 				else if(num==2)
 				{
 					Searcher searcher = new Searcher();
 					
 					int index = searcher.binarySearch(pubList, new Publication(search));
 					
 					if(index != -1)	
 					{	
 						results = pubList.get(index).toString() + "\n" ;
 						resultsBool = true;
 					}
 					else
 						resultsBool = false;
 				}
 				else if(num==3)
 				{
 					for(Publication pub : pubList)
 					{
 						if(pub.getTitleSerial() != null && pub.getTitleSerial().contains(search)){results += pub.toString() + "\n";resultsBool = true;}
 						else {resultsBool = false;}
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
				}
				else 
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
 	
 	public <T extends Comparable<? super T>> void sort(List<T> list, Comparator<? super T> c) {
 		Object[] a = list.toArray();
 		Arrays.sort(a, (Comparator)c);
 		count=0;
 		ListIterator<T> i = list.listIterator();
 		for (int j=0; j<a.length; j++) {
 			i.next();
 			i.set((T)a[j]);
 			count++;
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
