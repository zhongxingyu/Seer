 /**
     This file is part of Bibbla.
 
     Bibbla is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Bibbla is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Bibbla.  If not, see <http://www.gnu.org/licenses/>.    
  **/
 
 package dat255.grupp06.bibbla.backend.tasks;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jsoup.Connection.Method;
 import org.jsoup.Connection.Response;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import dat255.grupp06.bibbla.backend.Backend;
 import dat255.grupp06.bibbla.model.Book;
 import dat255.grupp06.bibbla.model.PhysicalBook;
 import dat255.grupp06.bibbla.utils.Error;
 import dat255.grupp06.bibbla.utils.Message;
 
 /**
  * Fetches the detailed view of a book, and
  * saves the additional information in a new Book.
  * 
  * @author Fahad Al-Khameesi
  */
 public class DetailedViewJob extends Job {
 	
 	private Book originalBook, newBook;
 	private Message message = new Message();
 	
 	public DetailedViewJob(Book book){
 		this.originalBook = book;
 		this.newBook = (Book)originalBook.clone();
 	}
 	
 	/**
 	 * Fetches the detailed view of a book.
 	 * 
 	 * @returns a new Book, containing the additional information present in its detailed view.
 	 */
 	public Message run(){
 		
 		try {
 			System.out.print("\n****** DetailedViewJob\n");
 			System.out.print("* step 1: fetch book details... ");
 			Response response = connectAndRetry();
 			System.out.print("* step 2: parse book details... ");
 			parseBookDetails(response);
 			System.out.print("succeeded! *\n*");
 			System.out.print("****** DetailedViewJob done \n");
 			
 		} catch (Exception e) {
 			message.error = (message.error!=null) ? message.error : Error.DETAILED_VIEW_FAILED;
 		}
 		
 		return message;
 	}
 	
 	@Override
 	/**
 	 * Fetches the details of the supplied book.
 	 * 
 	 * @throws Exception if HTTP connection failed.
 	 */
 	protected Response connect() throws Exception {
 		// Performs connection using Jsoup.
 	    Response r = Jsoup.connect(originalBook.getUrl())
 			    .method(Method.GET)
 			    .timeout(Backend.CONNECTION_TIMEOUT)
 			    .execute();
 	    return r;
 	}
 	
 	/**
 	 * Parses the HTML document fetched by fetchBookDetails().
 	 * 
 	 * @throws Exception if parsing fails.
 	 */
 	private void parseBookDetails(Response response) throws Exception{
 
 		// Prepare HTML for parsing.
 		Document html = response.parse();
 
 	    List<PhysicalBook> physicalBooks = new ArrayList<PhysicalBook>();
 	    Elements tableRows = html.select("table.bibItems").select("tr.bibItemsEntry");
 	    
 	    // Create all our PhysicalBooks.
 	    for (Element row : tableRows) {
 	    	Elements columns = row.getElementsByTag("td"); 
 	    	String library = columns.get(0).text();
 	    	String shelf = columns.get(1).text();
 	    	String status = columns.get(2).text();
 	    	String message = columns.get(3).text();
 	    	PhysicalBook physicalBook = new PhysicalBook(library, shelf, status, message);
 	    	physicalBooks.add(physicalBook);
 	    }
 
 	    // Select all rows containing detailed information.
 	    Elements rows = html.select("div#orders").select("table.bibDetail").select("tr");
 	    String description = "", notes = "", isbn = "";
 
 	    // Loop through all rows.
 	    for(int i=1;i<rows.size();i++) {
 	    	
 	    	// If we find a physical description,
 	    	if(rows.get(i).select("td.bibInfoLabel").text().equals((String) "Fysisk beskrivning")){
 	    		// Save the text present on row #1,
 	    		description += (rows.get(i)).select("td.bibInfoData").text();
 				int n = i+1;
 				// And if there are rows without labels following it,
 				// they are also part of the description. 
 				while((rows.get(n).select("td.bibInfoLabel").size() == 0)){
 					description += (rows.get(n)).select("td.bibInfoData").text();
 					n++;
 				}
 	    	}
 	    	
 	    	// If we find a note,
 	       	if(rows.get(i).select("td.bibInfoLabel").text().equals((String) "Anmrkning")) {
 	       		// Save the text on row #1
 	    		notes += (rows.get(i)).select("td.bibInfoData").text();
 				int n = i+1;
 				// And save text of possible following rows that has no label.
 				while((rows.get(n).select("td.bibInfoLabel").size() == 0)) {
 					notes += (rows.get(n)).select("td.bibInfoData").text();
 					n++;
 				}
 	    	}
 	       	
 	       	// If we find an ISBN, save it.
 	       	if(rows.get(i).select("td.bibInfoLabel").text().equals((String) "ISBN")){
 	    		isbn = (rows.get(i)).select("td.bibInfoData").text();
 	    	}
 	    }
 	    
		Elements table = html.select("table.bibDetail").first().select("tr");
		for(int i=0;i<table.size();i++){
			if( table.get(i).select("td.bibInfoLabel").text().equals((String) "Upphov")){
				newBook.setAuthor(table.get(i).select("td.bibInfoData").text());
				
			}
			if( table.get(i).select("td.bibInfoLabel").text().equals((String) "Titel")){
				String[] temp =(table.get(i).select("td.bibInfoData").text().split("/"));
				newBook.setName(temp[0]);
			}
			
			if( table.get(i).select("td.bibInfoLabel").text().equals((String) "Utgivning")){
				
				newBook.setPublisher(table.get(i).select("td.bibInfoData").text());
			}
		}
	    
 	    // Update newBook with this information.
 	    newBook.setPhysicalBooks(physicalBooks);
 	    newBook.setIsbn(isbn);
 	    newBook.setNotes(notes);
 	    newBook.setPhysicalDescription(description);
 	    
 	    message.obj = newBook;
 	}
 
 }
 
