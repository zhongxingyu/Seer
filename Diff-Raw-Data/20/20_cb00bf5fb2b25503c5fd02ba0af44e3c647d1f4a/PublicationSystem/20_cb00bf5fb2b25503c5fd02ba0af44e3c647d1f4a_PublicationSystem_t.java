 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import javax.swing.JOptionPane;
 
 /**
  * Project #3
  * CS 2334, Section 011
  * 10/9/2013
  * <P>
  * This class represents a publication system, which allows for importing, printing, sorting, and searching of lists of publications.
  * </P>
  * @version 2.0
  */
 public class PublicationSystem implements Serializable {
 	/* Instance Variables */
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 2L;
 
 	/**A list of all publications in the system*/
 	private PublicationList publicationList;
 	
 	/**A map of author names to authors*/
 	private AuthorMap authorMap;
 
 	/**
      * Creates a publication system with no publications
      */
 	public PublicationSystem(){
 		publicationList = new PublicationList();
 		authorMap = new AuthorMap();
 	}
 	
 	/*
      * Mutator Methods
      */
 	
 	 /**
      * This sorts papers by the date they were published
      * <P>
      * Algorithm:<br>
      * Algorithm not yet implemented.<br>
      * </P>
      * <dt><b>Conditions:</b>
      * <dd>POST -         papers is sorted by the date they were published
      */
 	public void sortByDate()
 	{
 		publicationList.sortByDate();
 	}
 	
 	/**
      * This sorts papers by the digital identifier associated with it alphanumerically
      * <P>
      * Algorithm:<br>
      * Algorithm not yet implemented.<br>
      * </P>
      * <dt><b>Conditions:</b>
      * <dd>POST -         papers is sorted by the digital identifier associated with it alphanumerically
      */
 	public void sortByDigitalIdentifier()
 	{
 		publicationList.sortByDigitalIdentifier();
 	}
 
 	/**
     * This methods sorts papers by serial title
     * <P>
     * Algorithm:<br>
     * Algorithm not yet implemented.<br>
     * </P>
     * <dt><b>Conditions:</b>
     * <dd>POST -         Papers are sorted by serial title
     */
 	public void sortBySerialTitle(){
 		publicationList.sortBySerialTitle();
 	}
 
 	/**
     * This methods sorts papers by paper title
     * <P>
     * Algorithm:<br>
     * Algorithm not yet implemented.<br>
     * </P>
     * <dt><b>Conditions:</b>
     * <dd>POST -         Papers are sorted by paper title
     */
 	public void sortByPaperTitle(){
 		publicationList.sortByPaperTitle();
 	}
 
 	/**
     * This methods sorts papers by author
     * <P>
     * Algorithm:<br>
     * Algorithm not yet implemented.<br>
     * </P>
     * <dt><b>Conditions:</b>
     * <dd>POST -         Papers are sorted by author
     */
 	public void sortByAuthor(){
 		publicationList.sortByAuthor();
 	}
 
 	/**
     * This methods sorts papers by bibliographic info
     * <P>
     * Algorithm:<br>
     * Algorithm not yet implemented.<br>
     * </P>
     * <dt><b>Conditions:</b>
     * <dd>POST -         Papers are sorted by bibliographic info
     */
 	public void sortByBibliographicInfo(){
 		publicationList.sortByBibliographicInfo();
 	}
 
 	/**
     * This methods sorts papers by title
     * <P>
     * Algorithm:<br>
     * Algorithm not yet implemented.<br>
     * </P>
     * <dt><b>Conditions:</b>
     * <dd>POST -         Papers are sorted by a random characteristic
     */
 	public void randomSort(){
 		publicationList.randomSort();
 	}
 
 	/**
 	 * Imports a publication from a file given by the user
 	 */
 	public void importPublications(){
 		String fileName = "";
 		try{
 			fileName = JOptionPane.showInputDialog("Where to import from?");
 			if(fileName == null){
 				return;
 			}
 			try{
 				String newFileName = fileName;
 				if(fileName.length() < 7 || !fileName.substring(fileName.length() - 7).equals(".pubsys")){
 					newFileName += ".pubsys";
 				}
 				ObjectInputStream objectReader = new ObjectInputStream(new FileInputStream(new File(newFileName)));
 				PublicationSystem newSys = (PublicationSystem)objectReader.readObject();
 				publicationList = newSys.getPublicationList();
 				authorMap = newSys.getAuthorMap();
 				objectReader.close();
 				return;
 			}
 			catch(Exception e){}
 			BufferedReader fileReader = new BufferedReader(new FileReader(new File(fileName)));
 			readPaper(fileReader);
 			fileReader.close();
 		}
 		catch(Exception e){
			//Changes
			JOptionPane.showMessageDialog(null, "There was an error while reading your file. \nImport failed.", "", JOptionPane.ERROR_MESSAGE);
			int choice = JOptionPane.showConfirmDialog(null, "Would you like to print the error stream?");
			if(choice == 0)
				e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Exports the publication list to a file given by the user
 	 */
 	public void exportPublicationList(){
 		String fileName = "";
 		try{
 			fileName = JOptionPane.showInputDialog("Where to save?");
 			if(fileName == null){
 				return;
 			}
 			if(fileName.length() < 7 || !fileName.substring(fileName.length() - 7).equals(".pubsys")){
 				fileName += ".pubsys";
 			}
 			ObjectOutputStream writer = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(new File(fileName))));
 			writer.writeObject(this);
 			writer.close();
 		}
 		catch(Exception e){
			//Changes
			JOptionPane.showMessageDialog(null, "There was an error while writing your file. \nExport failed.", "", JOptionPane.ERROR_MESSAGE);
			int choice = JOptionPane.showConfirmDialog(null, "Would you like to print the error stream?");
			if(choice == 0)
				e.printStackTrace();
 		}
 	}
 	
 	private void readPaper(BufferedReader fileReader) throws IOException{
 		try{
 			String bro = fileReader.readLine();
 			if(bro.equals("Conference Paper")){
 				String authorString = fileReader.readLine();
 				String[] authorList = authorString.split("; ");
 				String paperTitle = fileReader.readLine();
 				String serialTitle = fileReader.readLine();
 				String[] pageNumbers = new String[2];
 				String pagesLine = fileReader.readLine();
 				String[] pages = pagesLine.split("-");
 				for(int i = 0; i < Math.min(pages.length, pageNumbers.length); i++){
 					pageNumbers[i] = pages[i];
 				}
 				if(pages.length < 2){
 					pageNumbers[1] = "";
 				}
 				String date = fileReader.readLine();
 				String digId = fileReader.readLine();
 				if(digId != null){
 					ConferencePaper next = new ConferencePaper(authorList, paperTitle, serialTitle, pageNumbers, date, digId);
 					Author auth;
 					publicationList.add(next);
 					for(int i = 0; i < authorList.length; i++){
 						if(authorMap.containsKey(authorList[i])){
 							authorMap.get(authorList[i]).addConferenceProceeding(next);
 						}
 						else{
 							auth = new Author(authorList[i]);
 							auth.addConferenceProceeding(next);
 							authorMap.put(authorList[i], auth);
 						}
 					}
 					if(digId.equals("")){
 						readPaper(fileReader);
 						return;
 					}
 				}
 				else{
 					publicationList.add(new ConferencePaper(authorList, paperTitle, serialTitle, pageNumbers, date, ""));
 				}
 				if(fileReader.readLine().equals("")){
 					readPaper(fileReader);
 				}
 			}
 			else{
 				String authorString = fileReader.readLine();
 				String[] authorList = authorString.split("; ");
 				String paperTitle = fileReader.readLine();
 				String serialTitle = fileReader.readLine();
 				String[] pageNumbers = new String[2];
 				String[] locInfo = fileReader.readLine().split(":");
 				String[] pages = locInfo[1].split("-");
 				String[] journalInfo = locInfo[0].split("\\(");
 				String volume = journalInfo[0];
 				String issue = journalInfo[1].substring(0,1);
 				for(int i = 0; i < pages.length; i++){
 					pageNumbers[i] = pages[i];
 				}
 				if(pages.length < 2){
 					pageNumbers[1] = "";
 				}
 				String date = fileReader.readLine();
 				String digId = fileReader.readLine();
 				if(digId != null){
 					Article next = new Article(authorList, paperTitle, serialTitle, pageNumbers, volume, issue, date, digId);
 					Author auth;
 					publicationList.add(next);
 					for(int i = 0; i < authorList.length; i++){
 						if(authorMap.containsKey(authorList[i])){
 							authorMap.get(authorList[i]).addJournalArticle(next);
 						}
 						else{
 							auth = new Author(authorList[i]);
 							auth.addJournalArticle(next);
 							authorMap.put(authorList[i], auth);
 						}
 					}
 					if(digId.equals("")){
 						readPaper(fileReader);
 						return;
 					}
 				}
 				else{
 					publicationList.add(new Article(authorList, paperTitle, serialTitle, pageNumbers, volume, issue, date, ""));
 				}
 				if(fileReader.readLine() == ""){
 					readPaper(fileReader);
 				}
 			}
 		}
 		catch(NullPointerException e){
 			JOptionPane.showMessageDialog(null, "Error processing paper", "", JOptionPane.ERROR_MESSAGE);
			//Changes
			int choice = JOptionPane.showConfirmDialog(null, "Would you like to print the error stream?");
			if(choice == 0)
				e.printStackTrace();
 		}
 	}
 	
 	/*
 	 * Accessor Methods
 	 */
 	
 	/**
 	 * returns the publicationList
      * <P>
 	 */
 	public PublicationList getPublicationList(){
 		return publicationList;
 	}
 
 	 /**
     * This prints the publications to a file
     * <P>
     * Algorithm:<br>
     * Algorithm not yet implemented.<br>
     * </P>
     * <dt><b>Conditions:</b>
     * <dd>PRE  -         papers is not null
     * <dd>POST -         papers is correctly printed to the file <i>fileName</i>
 	 * @throws IOException 
     */
 	public void printPublicationsToFile(String fileName) throws IOException{
 		if(fileName == null){
 			return;
 		}
 		if(fileName.length() < 4 || !fileName.substring(fileName.length() - 4).equals(".txt")){
 			fileName += ".txt";
 		}
 		BufferedWriter writer = null;
 		writer = new BufferedWriter(new FileWriter(fileName));
 		
 		ArrayList<String> lines = new ArrayList<String>();
 		String[] lilLines;
 		for(Paper p: publicationList)
 		{
 			lilLines = p.toString().split("<br />");
 			for(String lilLine: lilLines){
 				lines.add(lilLine);
 			}
 			lines.add("");
 		}
 		for(String line: lines){
 			writer.write(line);
 			writer.newLine();
 		}
 		writer.close();
 	}
 	
 	/**
 	 * Searches for a paper by title
     * <P>
     * Algorithm:<br>
     * Algorithm not yet implemented.<br>
     * </P>
     * @return a Paper with the title given
     * <dt><b>Conditions:</b>
     * <dd>PRE  -		 publicationList is sorted
     * <dd>POST -         The correct paper is returned. A value of null is returned on failure.
 	 */
 	public Paper getPaperBinary(String title){
 		int currentIndex;
 		int lower=0;
 		int higher=publicationList.size()-1;
 		while(true)
 		{
 			currentIndex=(higher+lower)/2;
 			if(publicationList.get(currentIndex).getTitle().compareTo(title)==0)
 				return publicationList.get(currentIndex);
 			else if(publicationList.get(currentIndex).getTitle().compareTo(title)>0)
 				higher=currentIndex;
 			else
 				lower=currentIndex;
 			if(higher - lower == 1 && publicationList.get(currentIndex).getTitle().compareTo(title)!=0){
 				return null;
 			}
 		}
 	}
 	
 	/**
      * This searches publicationList linearly
      * <P>
      * Algorithm:<br>
      * For loop.<br>
      * @return a Paper with the given title
      * </P>
      */
 	public Paper getPaperLinear(String title){
 		for(int i = 0; i < publicationList.size(); i++)
 		{
 			if(publicationList.get(i).getTitle().equalsIgnoreCase(title)){
 				return publicationList.get(i);
 			}
 		}
 		return null;
 	}
 
 	/**
      * This searches publicationList linearly
      * <P>
      * Algorithm:<br>
      * For loop.<br>
      * @return A list of Papers containing the given title
      * </P>
      */
 	public ArrayList<Paper> getPapers(String title){
 		ArrayList<Paper> papers = new ArrayList<Paper>(); 
 		for(int i = 0; i < publicationList.size(); i++)
 		{
 			if(publicationList.get(i).getTitle().toLowerCase().contains(title.toLowerCase())){
 				papers.add(publicationList.get(i));
 			}
 		}
 		return papers;
 	}
 
 	/**
      * Returns how many comparisons must be performed to find a title searching using a binary search.
      * <P>
      * </P>
      * @return the number of comparisons performed
      * <dt><b>Conditions:</b>
      * <dd>PRE -         papers is sorted by their title
      */
 	public int getSearchComparisonsBI(String title)
 	{
 		int count=0;
 
 		int currentIndex;
 		int lower=0;
 		int higher=publicationList.size()-1;
 		while(true)
 		{
 			count++;
 			currentIndex=(higher+lower)/2;
 			if(publicationList.get(currentIndex).getTitle().compareTo(title)==0)
 				return count;
 			else if(publicationList.get(currentIndex).getTitle().compareTo(title)>0)
 				higher=currentIndex;
 			else
 				lower=currentIndex;
 			if(higher - lower == 1 && publicationList.get(currentIndex).getTitle().compareTo(title)!=0){
 				return count;
 			}
 		}
 	}
 
 	/**
      * Returns how many comparisons must be performed to find a title searching using a linear search.
      * <P>
      * @return the number of comparisons performed
      * </P>
      */
 	public int getSearchComparisonsLI(String title)
 	{
 		int count=0;
 		for(int i = 0; i < publicationList.size(); i++)
 		{
 			count++;
 			if(publicationList.get(i).getTitle().equals(title)){
 				return count;
 			}
 		}
 		return count;
 	}
 
 	public Author findAuthor(String auth){
 		return authorMap.get(auth);
 	}
 	
 	public AuthorMap getAuthorMap(){
 		return authorMap;
 	}
 }
