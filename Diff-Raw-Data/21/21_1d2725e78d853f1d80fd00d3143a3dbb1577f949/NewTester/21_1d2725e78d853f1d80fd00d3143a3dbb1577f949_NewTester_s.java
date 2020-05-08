 import java.io.File;
 import java.io.IOException;
 import java.util.Scanner;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 
 /**
  * @args[0] Website we are making modules for
  * @args[1] Website for diff dept we are checking against
  * 
  * EXAMPLE:   CSE HIST
  * 
  * @author michael
  *
  */
 public class NewTester {
 
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 		// TODO Auto-generated method stub
 
 		/* department we are making modules for */
 		String department = args[0];
 		String url = "http://www.ucsd.edu/catalog/courses/"+department+".html";
 		
 		Webpage w = new Webpage(url);
 		
 		/* different department to check against */
 		String department2 = args[1];
 		String url2 = "http://www.ucsd.edu/catalog/courses/"+department2+".html";
 		
 		Webpage w2 = new Webpage(url2);
 		
 	}
 
 }
