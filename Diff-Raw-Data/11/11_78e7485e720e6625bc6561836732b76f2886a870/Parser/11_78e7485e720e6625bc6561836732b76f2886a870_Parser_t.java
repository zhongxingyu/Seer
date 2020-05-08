 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class Parser {
 	
 	/**
 	 * list of all publications
 	 */
 	private ArrayList<Publication> publications;
 	
 	/**
 	 * location of file to parse
 	 */
 	private String file_loc;
 	
 	/**
 	 * default constructor
 	 * 
 	 * @param file_loc location of file to parse
 	 */
 	public Parser(String file_loc) {
 		publications= new ArrayList<Publication>();
 		
 		if(isValidSearchName(file_loc))
 			setFileLoc(file_loc);
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
 	 * parses CSV file
 	 * 
 	 * @return true on success, false on error
 	 * @throws IOException
 	 */
 	public boolean parseFile() throws IOException {
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
 				
 				if(next_line.length() > 0)
 				{
 					if(partNum == 1)
 					{
 						type = next_line;
 					}
 					if(partNum == 2)
 					{
 						authors = parseAuthors(next_line);
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
 							publications.add(new Publication(authors,titlePaper, titleSerial, pageStart, pageEnd, Month, year, link));
 						if(type.toLowerCase().equals("journal article"))
 							publications.add(new JournalArticle(authors,titlePaper, titleSerial, pageStart, pageEnd, Month, year, link, volume, issue));
 					}
 					partNum++;
 				}
 			}
 			
 			DBReaderBuffered.close();
 			
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public boolean isType(String in) {
 		return in.toLowerCase().equals("conference paper") || 
 				in.toLowerCase().equals("journal article");
 	}
 	
 	public ArrayList<String> parseAuthors(String authorsStr) {
 		String[] parts = authorsStr.split("\\; ");
 		
 		ArrayList<String> out = new ArrayList<String>();
 		
 		for(String author : parts)
 		{
 			if(author != null)
 			{
 				out.add(author);
 			}
 		}
 		return out;
 	}
 	
 	/**
 	 * writes resultant file to disk
 	 * 
 	 * @return true on success, false on error 
 	 * @throws IOException
 	 */
 	public boolean writeFile() throws IOException {
 		return false;
 	}
 	
 	/**
 	 * searches CVS file for matching items
 	 * 
 	 * @param search_term term for which to search
 	 * @return string containing all results from search
 	 */
 	public String searchAuthor(String search_term) {
 		String results = "";
 		//for each from http://stackoverflow.com/questions/85190/how-does-the-java-for-each-loop-work with many modifications
 				for(Publication item : publications) {
 		//substring checking from 
 		//http://stackoverflow.com/questions/2275004/in-java-how-to-check-if-a-string-contains-a-substring-ignoring-the-case
 					
 					for(String author : item.getAuthors())
 					{
 						if(author.toLowerCase().contains(search_term.toLowerCase())) {
 							results+=item.toString() + "\n";
 						}
 					}
 				}
 				if(results == "")
 					return "No Results Found";
 				
 				return results;
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
 
 	public ArrayList<Publication> getPublications() {
 		return publications;
 	}
 
 	public void setPublications(ArrayList<Publication> publications) {
 		this.publications = publications;
 	}
 }
