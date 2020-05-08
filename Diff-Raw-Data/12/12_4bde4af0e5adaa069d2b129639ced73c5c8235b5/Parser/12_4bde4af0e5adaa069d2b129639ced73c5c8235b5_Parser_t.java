 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class Parser {
 	
 	/**
 	 * list of all publications by title
 	 */
 	private HashMap<String, Publication> publications;
 	
 	/**
 	 * list of what publications which authors published
 	 */
 	private HashMap<String, Author> authors;
 	
 	/**
 	 * location of file to parse
 	 */
 	private String file_loc;
 	
 	/**
 	 * default constructor
 	 * 
 	 * @param file_loc location of file to parse
 	 * @throws IOException 
 	 */
 	public Parser(String file_loc) throws IOException {
 		publications= new HashMap<String, Publication>();
 		
 		authors = new HashMap<String, Author>();
 		
 		if(isValidSearchName(file_loc))
 			setFileLoc(file_loc);
 		parsePublications();
 	}
 	
 	/**
 	 * decides whether or not the entered search name is valid
 	 *
 	 * @param file_loc location of file to parse
 	 * @return true if valid, false if not
 	 */
 	public boolean isValidSearchName(String file_loc) {
 		//ensure an program argument exists
 		if(file_loc.length() > 0)
 			return true;
 		else {
 			return false;
 		}
 	}
 	
 	/**
 	 * parses input file
 	 * 
 	 * @return true on success, false on error
 	 * @throws IOException
 	 */
 	public boolean parsePublications() throws IOException {
 		if(isValidSearchName(getFileLoc())) {
 			FileReader DBReader = new FileReader(getFileLoc());
 			
 			BufferedReader DBReaderBuffered = new BufferedReader(DBReader);
 			
 			String next_line="";
 			
 			int partNum = 1;
 			
 			String type = "";
 			
 			ArrayList<String> authors = new ArrayList<String>();
 			
 			String titlePaper = "";
 			
 			String titleSerial = "";
 			
 			int pageStart = 0;
 			
 			int pageEnd = 0;
 			
 			String Month = "";
 			
 			int year = 0;
 			
 			int volume = 0;
 			
 			int issue = 0;
 			
 			String link = "";
 			
 			while(DBReaderBuffered.ready())
 			{
 				next_line=DBReaderBuffered.readLine();
 				
 				if(next_line != null && next_line.length() > 0)
 				{
 					//TODO use modulus instead of partNum
 					if(partNum == 1)
 					{
 						type = next_line;
 					}
 					if(partNum == 2)
 					{
 						authors = parseAuthors(next_line);
 						
 						for(String author : authors)
 						{
 							if(this.authors.get(author) == null)
 							{
								this.authors.put(author, new Author(author));
 							}
 						}
 					}
 					if(partNum == 3)
 					{
 						titlePaper = next_line;
 					}
 					if(partNum == 4)
 					{
 						titleSerial = next_line;
 					}
 					if(partNum == 5)
 					{
 						if(type.toLowerCase().equals("conference paper")) {
 							pageStart = Integer.parseInt(next_line.split("\\-")[0]);
 						
 							pageEnd = Integer.parseInt(next_line.split("\\-")[1]);
 						} else if(type.toLowerCase().equals("journal article")) {
 							volume = Integer.parseInt(next_line.split("\\(")[0]);
 							
 							issue = Integer.parseInt(next_line.split("\\(")[1].split("\\)")[0]);
 							
 							pageStart = Integer.parseInt(next_line.split("\\:")[1].split("\\-")[0]);
 							
 							pageEnd = Integer.parseInt(next_line.split("\\:")[1].split("\\-")[1]);
 						}
 					}
 					
 					if(partNum == 6)
 					{
 						Month = next_line.split("\\ ")[0];
 						
 						year = Integer.parseInt(next_line.split("\\ ")[1]);
 					}
 					if(partNum == 7)
 					{
 						partNum=0;
 						if(!isType(next_line))
 						{
 							link = next_line;
 						}
 						
 						if(type.toLowerCase().equals("conference paper"))
 							publications.put(titlePaper, new Publication(authors,titlePaper, titleSerial, pageStart, pageEnd, Month, year, link));
 						if(type.toLowerCase().equals("journal article"))
 							publications.put(titlePaper, new JournalArticle(authors,titlePaper, titleSerial, pageStart, pageEnd, Month, year, link, volume, issue));
 					}
 					partNum++;
 				}
 			}
 			
 			if(type.toLowerCase().equals("conference paper"))	
 				publications.put(titlePaper, new Publication(authors,titlePaper, titleSerial, pageStart, pageEnd, Month, year, link));
 			if(type.toLowerCase().equals("journal article"))
 				publications.put(titlePaper, new JournalArticle(authors,titlePaper, titleSerial, pageStart, pageEnd, Month, year, link, volume, issue));
 			
 			DBReaderBuffered.close();
 			
 			parseAuthors();
 			
 			return true;
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * parses publications to assign the papers to the author that wrote them
 	 */
 	public void parseAuthors() {
 		
 		//Iterator from http://stackoverflow.com/questions/1066589/java-iterate-through-hashmap
 		for(Publication pub : publications.values())
 		{
 			for(String author : pub.getAuthors())
 			{
 				if(this.authors.get(author) != null)
 				{
 					if(this.authors.get(author).getPublishedPapers().contains(pub.getTitlePaper()) == false)
 					{
 						this.authors.get(author).addPublishedPaper(pub.getTitlePaper());
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * checks if the inputed String is the type of a paper 
 	 * @param in String to check
 	 * @return true is yes, false if no
 	 */
 	public boolean isType(String in) {
 		return in.toLowerCase().equals("conference paper") || 
 				in.toLowerCase().equals("journal article");
 	}
 	
 	/**
 	 * parses the authors line into an ArrayList
 	 * @param authorsStr line with all authors
 	 * @return ArrayList of authors
 	 */
 	public ArrayList<String> parseAuthors(String authorsStr) {
 		String[] parts = authorsStr.split("\\; ");
 		
 		ArrayList<String> out = new ArrayList<String>();
 		
 		for(String author : parts)
 		{
 			if(author != null)
 			{
 				out.add(author.split("\\, ")[1] + " " + author.split("\\, ")[0]);
 			}
 		}
 		return out;
 	}
 	
 	public String getFileLoc() {
 		if(isValidSearchName(file_loc)) {
 			return file_loc;
 		}
 		return "Invalid File Location";
 	}
 	
 	public boolean setFileLoc(String file_loc) {
 		if(isValidSearchName(file_loc)) {
 			this.file_loc = file_loc;
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public HashMap<String, Publication> getPublications() {
 		return publications;
 	}
 	
 	public void setPublications(HashMap<String, Publication> publications) {
 		this.publications = publications;
 	}
 
 	public HashMap<String, Author> getAuthors() {
 		return this.authors;
 	}
 
 	public void setAuthors(HashMap<String, Author> authors) {
 		this.authors = authors;
 	}
 }
