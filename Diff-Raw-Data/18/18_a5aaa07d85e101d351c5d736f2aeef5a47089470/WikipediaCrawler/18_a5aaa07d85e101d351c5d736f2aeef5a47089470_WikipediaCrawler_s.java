 package de.jaide.wire;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 
 import org.apache.commons.lang.WordUtils;
 import org.apache.log4j.Logger;
 
 import com.gargoylesoftware.htmlunit.BrowserVersion;
 import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.gargoylesoftware.htmlunit.util.NameValuePair;
 
 import de.jaide.exception.CrawlerFailedException;
 import de.jaide.exception.FileOperationFailedException;
 
 /**
  * A Wikipedia WikipediaCrawler. Expects a list of URLs to scan. Considers the linked words to be related to the topic of the URL and writes
  * them
  * into a file.
  * 
  * @author Janarthanan Ramar (janarthanan.r@gmail.com)
  * @author Rias A. Sherzad (rias.sherzad@jaide.de)
  */
 public class WikipediaCrawler extends FileOperation implements ICrawler {
   /**
    * Our logger.
    */
   private static Logger LOG = Logger.getLogger(WikipediaCrawler.class);
   protected WebClient webClient = null;
 
   /**
    * The list of URLs to crawl.
    */
   ArrayList<String> urlList = new ArrayList<String>();
 
   /**
    * The list of words to ignore.
    */
   ArrayList<String> bannedWordList = new ArrayList<String>();
 
   /**
    * The list of sections in an article to ignore.
    */
   ArrayList<String> bannedSectionList = new ArrayList<String>();
 
   /**
    * The resulting list of words and and URLs.
    */
   HashSet<String> wordList = new LinkedHashSet<String>();
 
   HtmlPage page = null;
   HtmlPage crawlPage = null;
   HtmlAnchor anchor = null;
 
   /**
    * Currently configured languages:
    * English, Arabic, Farsi, French, German, Bahasa Indonesia, Spanish, Turkish, Urdu
    */
   String[] lang = { "en", "ar", "fa", "fr", "de", "id", "es", "tr", "ur" };
 
   /**
    * Reads the three input files and starts the crawling process.
    * 
    * @param args Up to three parameter files are expected.
    * @throws CrawlerFailedException Thrown if the crawling failed.
    * @throws FileOperationFailedException Thrown if saving the resulting file failed.
    */
   public WikipediaCrawler(String args[]) throws CrawlerFailedException, FileOperationFailedException {
     /*
      * The list of URLs
      */
     if (args.length >= 1)
       urlList = readFile(args[0]);
 
     /*
      * The sections within a Wikipedia article that should not be scanned
      */
     if (args.length >= 2)
       bannedSectionList = readFile(args[1]);
 
     /*
      * The list of words that should be disregarded
      */
     if (args.length == 3)
       bannedWordList = readFile(args[2]);
 
     /*
      * Set up the crawler.
      */
     connect();
 
     /*
      * Start the crawling.
      */
     crawl();
 
     /*
      * Shut down the crawler.
      */
     disconnect();
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see de.jaide.wire.ICrawler#connect()
    */
   @Override
   public void connect() {
     webClient = new WebClient(BrowserVersion.FIREFOX_3_6);
     webClient.setJavaScriptEnabled(false);
     webClient.setCssEnabled(false);
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see de.jaide.wire.ICrawler#crawl()
    */
   @SuppressWarnings("unchecked")
   @Override
   public void crawl() throws CrawlerFailedException, FileOperationFailedException {
     String content = "";
     String[] value = null;
     String host = "";
     String[] inputLang = null;
     List<HtmlAnchor> frAnchors = null;
 
     /*
      * Reading the webpage for all the URLs
      */
     for (int i = 0; i < urlList.size(); i++) {
       try {
         content = urlList.get(i);
         value = content.split(",");
         outputFileName = value[0].toString();
         url = new URL(value[1]);
 
         /*
          * Get the locale of the given URL
          */
         host = url.getHost();
         inputLang = host.split("\\.");
         locale = inputLang[0];
         crawlInfo(url);
         page = (HtmlPage) webClient.getPage(url);
 
         /*
          * Read the webpage for other languages
          */
         for (int j = 0; j < lang.length; j++) {
           locale = lang[j].toString().trim();
           frAnchors = (List<HtmlAnchor>) page.getByXPath("//div[@id='p-lang']//a[@lang='" + locale + "']");
 
           if (frAnchors.size() > 0) {
             url = new URL(URLDecoder.decode("http:" + frAnchors.get(0).getHrefAttribute(), "UTF-8"));
             crawlInfo(url);
           }
 
           /*
            * Don't bomb Wikipedia. Wait for a few milliseconds.
            */
           Thread.sleep(100);
         }
 
         Thread.sleep(100);
       } catch (MalformedURLException e) {
         throw new CrawlerFailedException(
             "The server tried to connect to or was redirected to a host that could not be resolved. This could be a firewall or a DNS issue.",
             e.fillInStackTrace());
       } catch (UnsupportedEncodingException e) {
         throw new CrawlerFailedException("UTF-8 not supported", e.fillInStackTrace());
       } catch (FailingHttpStatusCodeException e) {
         throw new CrawlerFailedException(
             "The server tried to connect to or was redirected to a host that could not be resolved. This could be a firewall or a DNS issue.",
             e.fillInStackTrace());
       } catch (InterruptedException e) {
         throw new CrawlerFailedException(e.fillInStackTrace());
       } catch (IOException e) {
         throw new CrawlerFailedException("Some general problem occured. Please see the stacktrace for more information.",
             e.fillInStackTrace());
       }
     }
 
   }
 
   /*
    * Extract the content from the webpage
    */
   private void crawlInfo(URL url) throws CrawlerFailedException, FileOperationFailedException {
     ArrayList<String> pathList = new ArrayList<String>();
     wordList = new LinkedHashSet<String>();
 
     try {
 
       setOutputFile();
       crawlPage = (HtmlPage) webClient.getPage(url);
 
       /*
        * Print the response headers - in trace mode only.
        */
       if (LOG.isTraceEnabled()) {
         List<NameValuePair> responseHeaders = crawlPage.getWebResponse().getResponseHeaders();
         for (NameValuePair nameValuePair : responseHeaders)
           LOG.trace(nameValuePair.getName() + " --> " + nameValuePair.getValue());
       }
 
       /*
        * XPath for the actual article with its sections
        */
       pathList.add("//div[@id='mw-content-text']//p//a");
 
       /*
        * XPath for "References"
        */
       if (!bannedSectionList.contains("References"))
         pathList.add("//ol[@class='references']//li//span[@class='reference-text']//span//a[@class='external text']");
 
       /*
        * XPath for "Bibliography"
        */
       if (!bannedSectionList.contains("Bibliography"))
         pathList.add("//div[@class='refbegin']//ul//li//a[@class='external text']");
 
       /*
        * XPath for "See Also"
        */
       if (!bannedSectionList.contains("See also")) {
         pathList.add("//div[@class='column-count column-count-2']//ul//li//a");
         pathList.add("//table[@class='multicol']//tbody//tr//td[@valign='top' and @align='left']//ul//li//a");
       }
 
       /*
        * XPath for "Further reading"
        */
       else if (!bannedSectionList.contains("Further reading"))
         pathList.add(".//*[@id='mw-content-text']//ul//li//span//a");
 
       /*
        * XPath for "External links"
        */
       if (!bannedSectionList.contains("External links"))
         pathList.add("//ul//li//a[@class='external text']");
 
       /*
        * Requesting the webpage for the given XPath
        */
       for (int i = 0; i < pathList.size(); i++)
         dataIterator(pathList.get(i).toString());
 
       closeOutputFile();
     } catch (FailingHttpStatusCodeException e) {
       throw new CrawlerFailedException(
           "The server tried to connect to or was redirected to a host that could not be resolved. This could be a firewall or a DNS issue.",
           e.fillInStackTrace());
     } catch (IOException e) {
       throw new CrawlerFailedException("Some general problem occured. Please see the stacktrace for more information.",
           e.fillInStackTrace());
     }
   }
 
   /**
    * Checks if the given word is a string.
    * 
    * @param string The String to check.
    * @return True if it's a String, false if it's not.
    */
   public boolean isString(String string) {
     try {
       Integer.parseInt(string);
     } catch (NumberFormatException ex) {
       return true;
     }
 
     return false;
   }
 
   /**
    * Crawl the webpage for the given XPath.
    * 
    * @param path The XPath to scan for.
    * @throws CrawlerFailedException Thrown if the crawling failed.
    * @throws FileOperationFailedException Thrown if saving the resulting file failed.
    */
   @SuppressWarnings("unchecked")
   private void dataIterator(String path) throws CrawlerFailedException, FileOperationFailedException {
     Iterator<HtmlAnchor> iterator = null;
     List<HtmlAnchor> anchors = (List<HtmlAnchor>) crawlPage.getByXPath(path);
     iterator = anchors.iterator();
 
     String word = "";
     String anc = "";
 
     try {
       /*
        * Extracting the url and word from the webpage
        */
       while (iterator.hasNext()) {
         anchor = (HtmlAnchor) iterator.next();
         word = WordUtils.capitalize(anchor.asText().toString().trim());
 
         boolean isString = isString(word);
         anc = anchor.getHrefAttribute();
 
         if (!anc.contains("http"))
           value = "http://" + locale + ".wikipedia.org" + anc;
         else
           value = anc;
 
         anc = URLDecoder.decode(value, "UTF-8");
 
         if (!anc.toLowerCase().contains("jpg") && !anc.toLowerCase().contains("png") && !anc.toString().toLowerCase().contains("edit")
             && !anc.toString().toLowerCase().contains("gif") && !anc.toString().toLowerCase().contains("svg")
             && !bannedWordList.contains(word.toLowerCase()) && !word.equalsIgnoreCase("") && !anc.contains("cite_note")
             && !anc.contains("cite_ref") && !word.contains("http") && word.length() > 1 && !word.contains("?") && !word.contains("[")
            && !word.contains("]") && !word.contains(":") && !wordList.contains(word) && isString) {
 
           /*
            * Added to a LinkedHashSet to remove duplicates while preserving the sort order.
            */
           wordList.add(word);
           writeOutput(word, anc);
         }
 
       }
     } catch (UnsupportedEncodingException e) {
       throw new CrawlerFailedException("UTF-8 not supported", e.fillInStackTrace());
     }
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see de.jaide.wire.ICrawler#disconnect()
    */
   @Override
   public void disconnect() {
     webClient.closeAllWindows();
   }
 
   /**
    * Crawls the given URLs and saved the linked words in each article in a file, under the name preceeding the listed URL. Does that for
    * several languages.
    * 
    * @param args Expects up to three input files: list of URLs to crawl, list of banned sections and list of banned words. Only the first
    *          one is obligatory.
    */
   public static void main(String args[]) {
     try {
       new WikipediaCrawler(args);
     } catch (CrawlerFailedException e) {
       e.printStackTrace();
     } catch (FileOperationFailedException e) {
       e.printStackTrace();
     }
   }
 }
