 package modules;
 
 import static org.jibble.pircbot.Colors.*;
 
 import java.io.IOException;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 
 import main.Message;
 import main.NoiseModule;
 import static main.Utilities.*;
 
 /**
  * Beer Advocate module
  *
  * @author Michael Auchter
  *         Created Sweetmorn, the 22nd day of Bureaucracy in the YOLD 3178
  */
 public class BeerAdvocate extends NoiseModule {
   private static final String COLOR_ERROR = RED + REVERSE;
   private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1667.0 Safari/537.36";
 
   // Yeah, you wanna fight?
   public static String extract(Document page, String selector)
   {
     Element node = page.select(selector).first();
     if (node == null)
       return "";
     return node.text();
   }
 
   @Command(".*(http://beeradvocate.com/beer/profile/[0-9]+/[0-9]+).*")
   public void beer(Message message, String beerUrl)
   {
     Document page = null;
     try {
       page = Jsoup.connect(beerUrl)
                   .timeout(10000)
                   .userAgent(USER_AGENT)
                   .get();
     } catch (IOException e) {
       e.printStackTrace();
       this.bot.sendMessage(COLOR_ERROR + "Error retrieving BA page...");
     }
 
     String name = extract(page, ".titleBar");
     String score = extract(page, ".BAscore_big");
     String style = extract(page, "[href^=/beer/style/]");
 
     this.bot.sendMessage(name + " - " + style + " - " + score);
   }
 
   // Runs a search in BA and returns the first search result. Searches beer only.
   @Command("\\.beer (.*)")
   public void search(Message message, String toSearch)
   {
     Document searchResults = null;
     toSearch = toSearch.replaceAll(" ", "\\+");
               toSearch = "\""+toSearch+"\"";
     try {
      searchResults = Jsoup.connect("http://beeradvocate.com/search?q="+toSearch+"&qt=beer")
                                                                       .timeout(10000)
                                                                       .userAgent(USER_AGENT)
                                                                       .get();
                       //this.bot.sendMessage("DEBUG: " + "http://beeradvocate.com"+searchResults.getElementsByTag("li").html());
      String url = "http://beeradvocate.com"+searchResults
                                                                       .body()
                                                                       .getElementsByTag("li")
                                                                       .get(2)
                                                                       .getElementsByTag("a")
                                                                       .get(0)
                                                                       .attr("href");
       this.beer(null, url);
       this.bot.sendMessage(url);
     } catch(IOException e) {
       e.printStackTrace();
       this.bot.sendMessage(COLOR_ERROR + "Error retrieving BA page...");
     }
   }
 
   @Override
   public String getFriendlyName() {
     return "BeerAdvocate";
   }
 
   @Override
   public String getDescription() {
     return "Returns information about a beer when a BeerAdvocate link appears...";
   }
 
   @Override
   public String[] getExamples() {
     return new String[] { 
       "http://beeradvocate.com/beer/profile/192/83920/"
     };
   }
 }
