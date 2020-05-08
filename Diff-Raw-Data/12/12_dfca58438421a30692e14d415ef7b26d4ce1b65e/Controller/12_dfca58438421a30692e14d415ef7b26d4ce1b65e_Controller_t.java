 import java.util.ArrayList;
 
 import books.*;
 
 public class Controller {
 
 	/**
 	 * @param args
 	 * @throws Exception 
 	 */
 	
 	public static void main(String[] args) throws Exception {
 	
		BookSearch bs = new BookSearch("Project2.db");
		ArrayList<Author> authors = bs.searchAuthor("Ramez", null, null, null, ""+2, null, ""+1964);
		for (Author a : authors)
			System.out.println(a);
 	}
 }
