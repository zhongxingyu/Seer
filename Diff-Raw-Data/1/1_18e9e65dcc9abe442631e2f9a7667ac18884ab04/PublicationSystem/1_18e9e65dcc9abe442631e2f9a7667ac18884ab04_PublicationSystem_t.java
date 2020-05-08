 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.swing.JOptionPane;
 
 /**
  * Project #2
  * CS 2334, Section 011
  * 9/24/2013
  * <P>
  * This class represents a publication system, which allows for importing, printing, sorting, and searching of lists of publications.
  * </P>
  * @version 1.0
  */
 public class PublicationSystem {
 	/* Instance Variables */
 	
 	/**A list of all publications in the system*/
 	private PublicationList publicationList;
 
 	/**
      * Creates a publication system with no publications
      */
 	public PublicationSystem(){
 		publicationList = new PublicationList();
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
 	public void importPublication(){
 		String fileName = "";
 		try{
 			fileName = JOptionPane.showInputDialog("What is the file name?");
 			BufferedReader fileReader = new BufferedReader(new FileReader(new File(fileName)));
 			readPaper(fileReader);
 			fileReader.close();
 		}
 		catch(Exception e){e.printStackTrace();}
 	}
 	
 	private void readPaper(BufferedReader fileReader) throws IOException{
 		String bro = fileReader.readLine();
 		if(bro.equals("Conference Paper")){
 			String authorString = fileReader.readLine();
 			String[] authorList = authorString.split("; ");
 			String paperTitle = fileReader.readLine();
 			String serialTitle = fileReader.readLine();
 			int[] pageNumbers = new int[2];
 			String pagesLine = fileReader.readLine();
 			String[] pages = pagesLine.split("-");
 			pageNumbers[0] = Integer.parseInt(pages[0]);
 			pageNumbers[1] = Integer.parseInt(pages[1]);
 			String date = fileReader.readLine();
 			String digId = fileReader.readLine();
 			if(digId != null){
 				publicationList.add(new ConferencePaper(authorList, paperTitle, serialTitle, pageNumbers, date, digId));
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
 			int[] pageNumbers = new int[2];
 			String[] locInfo = fileReader.readLine().split(":");
 			String[] pages = locInfo[1].split("-");
 			String[] journalInfo = locInfo[0].split("\\(");
 			int volume = Integer.parseInt(journalInfo[0]);
 			int issue = Integer.parseInt(journalInfo[1].substring(0,1));
 			pageNumbers[0] = Integer.parseInt(pages[0]);
 			pageNumbers[1] = Integer.parseInt(pages[1]);
 			String date = fileReader.readLine();
 			String digId = fileReader.readLine();
 			if(digId != null){
 				publicationList.add(new ConferencePaper(authorList, paperTitle, serialTitle, pageNumbers, date, digId));
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
 		}
 		for(String line: lines){
 			writer.write(line);
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
 		int finalIndex=-1;
 		int lower=0;
 		int higher=publicationList.size()-1;
 		while(finalIndex==-1)
 		{
 			currentIndex=(higher+lower)/2;
 			if(publicationList.get(currentIndex).getTitle().compareTo(title)==0)
 				finalIndex=currentIndex;
 			else if(publicationList.get(currentIndex).getTitle().compareTo(title)>0)
 				higher=currentIndex;
 			else
 				lower=currentIndex;
 		}
 		return publicationList.get(finalIndex);
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
 		int index=0;
 		for(int i = 0; i < publicationList.size(); i++)
 		{
 			if(publicationList.get(i).getTitle().equals(title)){
 				return publicationList.get(i);
 			}
 		}
 		return null;
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
 		int finalIndex=-1;
 		int lower=0;
 		int higher=publicationList.size()-1;
 		while(finalIndex==-1)
 		{
 			count++;
 			currentIndex=(higher+lower)/2;
 			if(publicationList.get(currentIndex).getTitle().compareTo(title)==0)
 				finalIndex=currentIndex;
 			else if(publicationList.get(currentIndex).getTitle().compareTo(title)>0)
 				higher=currentIndex;
 			else
 				lower=currentIndex;
 		}
 		return count;
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
 		int index=0;
 		int finalIndex=-1;
 		while(finalIndex==-1 && index<publicationList.size())
 		{
 			count++;
 			if(publicationList.get(index).getTitle().equals(title))
 				finalIndex=index;
 			index++;
 		}
 		return count++;
 	}
 	
 }
