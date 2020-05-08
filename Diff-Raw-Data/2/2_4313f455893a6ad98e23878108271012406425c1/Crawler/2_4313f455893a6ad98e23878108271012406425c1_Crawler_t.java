 package pagecrawler.main;
 
 import java.util.HashMap;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 
 import pagecrawler.db.DBFunctions;
 import pagecrawler.logic.HandleTagCount;
 import pagecrawler.logic.HandleWordCount;
 
 
 public class Crawler 
 {
 	/**
 	 * @param args
 	 * @throws Exception 
 	 */
 	public static void main(String[] args) throws Exception 
 	{
 		// Verify the correct argument length
 		if (args.length != 1)
 		{
 			System.err.println("Please enter the URL of one site to be analyzed, in full http:// format.");
 		}
 		else
 		{
 			String inputUrl = args[0];
 			
 			// Pull the webpage source code with JSoup.
 			Document doc = Jsoup.connect(inputUrl).get(); 
 			
 			// Use JSoup to select all of the HTML tags from the document before converting the result to a String.
 			String allTags = doc.select("body").toString().toLowerCase();
 		
 			// Create a HashMap of the tags included in the document and their count with the handleAllTags function.
 			HashMap<String, Integer> tagCount = HandleTagCount.handleAllTags(allTags);
 			
 			// Write the results of the HashMap to the popshops.htmlTags database.
 			DBFunctions.writeHtmlTagsToDb(inputUrl, tagCount);
 			
 			// Use JSoup to pull the non-HTML text from the source before replacing any non-letter character with whitespace.
			String text = doc.body().text().replaceAll("[^A-Za-z']", " ").replaceAll("'", "''");
 			
 			// Create a HashMap of the words included in the document and their count with the countWords function.
 			HashMap<String, Integer> wordCount = HandleWordCount.countWords(text);
 			
 			// Write the results of the HashMap to the popshops.wordCount database.
 			DBFunctions.writeWordCountToDb(inputUrl, wordCount);
 			
 			//Rejoice.
 		}		
 	}
 }
