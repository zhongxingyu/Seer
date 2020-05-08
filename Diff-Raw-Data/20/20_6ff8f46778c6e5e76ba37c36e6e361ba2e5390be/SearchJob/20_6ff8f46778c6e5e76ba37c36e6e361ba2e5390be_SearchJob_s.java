 package dat255.grupp06.bibbla.backend.tasks;
 
 
 import java.util.ArrayList;
 import java.util.List;
 import org.jsoup.Connection.Method;
 import org.jsoup.Connection.Response;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import dat255.grupp06.bibbla.model.Book;
 import dat255.grupp06.bibbla.utils.Message;
 import dat255.grupp06.bibbla.utils.Error;
 
 public class SearchJob {
 	
 	private String searchPhrase = null;
 	private Message message = new Message();
 	private Document resultsDocument;
 	private int pageNumber;
 
 	/**
 	 * Creates a new SearchJob, which returns the first page of the search results.
 	 * @param s - The string to search for.
 	 */
 	public SearchJob(String s){
 		this(s, 0);
 	}
 	
 	public SearchJob(String s, int pageNumber){
 		searchPhrase = s;
 		message = new Message();
 		this.pageNumber = pageNumber;
 	}
 	
 	public Message run(){
 					
 		try {
 			System.out.print("\n****** SearchJob\n");
 			System.out.print("* step1(): ");
 			step1();
 			System.out.print("succeeded! *\n");
 			System.out.print("* step2(): ");
 			step2();
 			System.out.print("succeeded! *\n*");
 			System.out.print("****** SearchJob done \n");
 		}
 		catch (Exception e) {
 			System.out.print("failed: "+e.getMessage()+" *** \n");
 			message.error = Error.SEARCH_FAILED;
 		}
 
 		return message;
 	}
 
 	private void step1() throws Exception {
 		// Fetch first page?
 		if (pageNumber == 0) {
 			String url = "http://www.gotlib.goteborg.se/search*swe/X?searchtype=X&searcharg="+searchPhrase+"&searchscope=6&SUBMIT=S%C3%B6k";
 			Response response = Jsoup.connect(url)
 					.method(Method.GET)
 					.timeout(5000)
 					.execute();
 			resultsDocument = response.parse();
 		}
 		// Fetch any other page.
 		else {
 			String url = "http://www.gotlib.goteborg.se/search~S6*swe?/X"+searchPhrase+"&searchscope=6&SORT=D/X"+searchPhrase+"&searchscope=6&SORT=D&SUBKEY="+searchPhrase+"/"+(((pageNumber-1)*50)+ 1)+"%2C1000000%2C1000000%2CB/browse";
 			Response response = Jsoup.connect(url)
 			.method(Method.GET)
 			.execute();
 			resultsDocument = response.parse();
 		}
 		
 		
 	}
 	
 	private void step2() {		
 		
 		List<Book> results = new ArrayList<Book>();
 		Elements searchResults = resultsDocument.select("table.breifCitTable");
 		for(Element e : searchResults){
 			Book book = new Book();
 			book.setName(e.select("a").get(1).text());
 			book.setAuthor(e.select("strong").first().text());
 			book.setType(e.select("td.sokresultat").get(4).getElementsByTag("img").first().attr("alt"));
 			book.setUrl(e.select("a").get(1).attr("abs:href")); 
 			book.setReserveUrl(e.select("div.reserverapadding").select("a").attr("abs:href"));
 			results.add(book);
 		}
 		
 		message.obj = results;
 	}
 
 }
