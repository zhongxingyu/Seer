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
 
 package dat255.grupp06.bibbla.utils;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import dat255.grupp06.bibbla.model.Book;
 import dat255.grupp06.bibbla.model.PhysicalBook;
 
 /**
  * Supplies common parsing methods to other classes.
  * 
  * @author Niklas Logren
  */
 public class CommonParsing {
 
	/**f
 	 * Parses the HTML from the "my loans"-view of gotlib.
 	 * 
 	 * @param rows - The table rows as parsed by Jsoup. Should be <tr class="trpatFuncEntry" ...> .
 	 * 
 	 * @returns a list of Books. Each book contains physicalBook, name, author, url and renewId.
 	 * 	The physicalBook may contain an error message, retrievable by getMessage().
 	 * 
 	 * @throws Exception - If there is a slash in the title of the book,
 	 * 	this code cannot differentiate between author and title.
 	 * @throws Exception also if parsing fails in any other way. 
 	 */
 	public static List<Book> parseMyBooks(Elements rows) throws Exception {
 		// false = parse my books; true = parse my reservations.
 		return parseRows(rows, false);		
 	}
 	
 	/**
 	 * Parses the HTML from the reservations-view of gotlib.
 	 * 
 	 * @param rows - The table rows as parsed by Jsoup. Should be <tr class="trpatFuncEntry" ...> .
 	 * 
 	 * @returns a list of Books. Each book contains physicalBook, name,
 	 * author, url and reservationId. The physicalBook contains a library, and 
 	 * may also contain an error message, retrievable by getMessage(). 
 	 * 
 	 * @throws Exception - If there is a slash in the title of the book,
 	 * 	this code cannot differentiate between author and title.
 	 * @throws Exception also if parsing fails in any other way. 
 	 */
 	public static List<Book> parseMyReservations(Elements rows) throws Exception {
 		// true = parse my reservations; false = parse my books.
 		return parseRows(rows, true);
 	}
 	
 	/**
 	 * Parses the supplied rows and treats them in one of two ways - 
 	 * As rows of the reservation view, or as rows of the My Books view. 
 	 * 
 	 * @param rows - the rows to be parsed.
 	 * @param parseReservations - true if we should parse My reservations,
 	 * 	false if we should parse My books. 
 	 * @returns a list of Books. Each book contains physicalBook, name, author and url.
 	 * 	Other information may be present, dependning on parsing mode.
 	 * 
 	 * @throws Exception - If there is a slash in the title of the book,
 	 * 	this code cannot differentiate between author and title.
 	 * @throws Exception also if parsing fails in any other way. 
 	 */
 	public static List<Book> parseRows(Elements rows, boolean parseReservations) throws Exception {
 		
 		List<Book> books = new ArrayList<Book>();
 	    for (Element row : rows) {
 	    	// Create our new Book.
 	    	Book book = new Book();
 	    	
 	    	// Create a new PhysicalBook.
 	    	PhysicalBook physicalBook = new PhysicalBook();
 	    	
 	    	// Does the PhysicalBook have an error?
 	    	if (row.select("font").size()>0) {
 	    		Element errorTag = row.select("font").first();
 	    		// Save error to PhysicalBook.
 	    		physicalBook.setError(true);
 	    		physicalBook.setMessage(errorTag.text());
 	    		// Font tag is located inside status td.
 	    		// Remove, so that .text()-call below returns only status.
 	    		errorTag.remove();
 	    	}
 	    	physicalBook.setStatus(row.select("td.patFuncStatus").first().text());
 	    	
 	    	if (parseReservations) { // Only reservations view displays library,
 	    		physicalBook.setLibrary(row.select("td.patFuncPickup").first().text());
 	    	} else { // and only My books view displays shelf.
 	    		physicalBook.setShelf(row.select("td.patFuncCallNo").first().text());
 	    	}
 	    	book.setPhysicalBook(physicalBook);
 
 	    	// Set name, author and URL.
 	    	String[] nameAndAuthor = row.select("td.patFuncTitle").text().split("/");
 	    	// Did we find more than one slash?
 	    	if (nameAndAuthor.length > 2) {
 	    		// Ignore the others. Temporary solution!
 	    		nameAndAuthor = new String[]{nameAndAuthor[0], nameAndAuthor[1]};
 	    	// Did we find no slash?
 	    	} else if (nameAndAuthor.length < 2) {
 	    		// Set author to "".
 	    		nameAndAuthor = new String[]{nameAndAuthor[0], ""};
 	    	}
 	    	book.setName(nameAndAuthor[0]);
 	    	book.setAuthor(nameAndAuthor[1]);
 	    	book.setUrl(row.select("td.patFuncTitle").first().select("a").first().attr("abs:href"));
 	    	if (parseReservations) {
 	    		book.setUnreserveId(row.select("td.patFuncMark").first().getElementsByTag("input").first().attr("name"));
 	    		if (row.select("td.patFuncFreeze").first().getElementsByTag("input").first() != null)
 	    			book.setFreezeId(row.select("td.patFuncFreeze").first().getElementsByTag("input").first().attr("name"));
 	    	} else {
 	    		book.setRenewId(row.select("td.patFuncMark").first().getElementsByTag("input").first().attr("value"));
 	    	}
 	    	
 	    	// Finally, add our new book to the list of results.
 	    	books.add(book);
 	    }
 		
 	    return books;
 	}
 	
 }
