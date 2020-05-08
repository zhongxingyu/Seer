 
 import java.io.*;
 import java.net.*;
 import java.sql.*;
 import java.util.*;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.safety.Whitelist;
 import org.jsoup.select.Elements;
 
 public class Crawler implements Runnable
 {
    public final static String TABLE_URLS = "URLS";
    public final static String TABLE_IMAGES = "IMAGES";
    public final static String TABLE_WORD = "WORD";
    public final static String TABLE_IMGWORD = "IMGWORD";
   public static final int nThread = 1;
    private Connection connection;
    private int NextURLID, NextImageURLID, NextURLIDScanned, urlIndex, MaxURLs;
    private String domain;
    private ArrayList<String> urlList;
    private Properties props;
    private boolean reset = true;
    
   final Object monitor = new Object();
 
    Crawler(int MaxURLs, String domain, boolean reset, ArrayList<String> urlList) throws IOException
    {
       readProperties();
       setVariables();
       this.MaxURLs = MaxURLs;
       this.domain = domain;
       this.urlList = urlList;
       this.reset = reset;
    }
    
    Crawler(Connection connection)
    {
       this.connection = connection;
    }
    
    public void run()
    {
       try
       {
          crawl();
       }
       catch (Exception e) {}
    }
 
    /**
     * Open the properties file to read necessary data such as database 
     * information and counters
     * @throws IOException
     */
    public void readProperties() throws IOException
    {
       props = new Properties();
       FileInputStream in = new FileInputStream("WebContent/WEB-INF/database.properties");
       props.load(in);
       in.close();
    }
    
    /**
     * Save constantly changing counters to properties file to save crawler state
     * @throws IOException
     */
    public synchronized void  setProperties() throws IOException
    {
       props.setProperty("crawler.NextURLID",""+NextURLID);
       props.setProperty("crawler.NextURLIDScanned",""+NextURLIDScanned);
       props.setProperty("crawler.NextImageURLID",""+NextImageURLID);
       FileOutputStream out = new FileOutputStream("WebContent/WEB-INF/database.properties");
       props.store(out, null);
    }
    
    /**
     * Get data stored in properties file and save them to the global variables 
     */
    public synchronized void setVariables()
    {
       NextURLID = Integer.parseInt(props.getProperty("crawler.NextURLID"));
       NextURLIDScanned = Integer.parseInt(props.getProperty("crawler.NextURLIDScanned"));
       NextImageURLID = Integer.parseInt(props.getProperty("crawler.NextImageURLID"));
    }
 
    /**
     * Create connection the the database using DB url, username, and password
     * stored in the properties file
     * @throws SQLException
     * @throws IOException
     */
    public void openConnection() throws SQLException, IOException
    {
       String drivers = props.getProperty("jdbc.drivers");
       if (drivers != null)
          System.setProperty("jdbc.drivers", drivers);
 
       String url = props.getProperty("jdbc.url");
       String username = props.getProperty("jdbc.username");
       String password = props.getProperty("jdbc.password");
 
       connection = DriverManager.getConnection(url, username, password);
    }
 
    /**
     * Drop all previously created tables in the DB that the crawler will use
     * and create new ones. 
     * @throws SQLException
     * @throws IOException
     */
    public void createDB() throws SQLException, IOException
    {
       System.out.println("Connecting to Database...");
       openConnection();
 
       Statement stat = connection.createStatement();
 
       // Delete the tables first if any
       try
       {
          System.out.println("Removing Previously Created Tables...");
          stat.executeUpdate("DROP TABLE URLS");
          stat.executeUpdate("DROP TABLE IMAGES");
          stat.executeUpdate("DROP TABLE WORD");
          stat.executeUpdate("DROP TABLE IMGWORD");
       }
       catch (Exception e)
       {
       }
 
       // Create the tables
       System.out.println("Creating Tables...");
       stat.executeUpdate("CREATE TABLE URLS (urlid INT, url VARCHAR(20000), rank INT, title VARCHAR(200), description VARCHAR(200))");
       stat.executeUpdate("CREATE TABLE IMAGES (urlid INT, url VARCHAR(20000), rank INT)");
       stat.executeUpdate("CREATE TABLE WORD (word VARCHAR(2000), urllist VARCHAR(18000))");
       stat.executeUpdate("CREATE TABLE IMGWORD (word VARCHAR(2000), urllist VARCHAR(18000))");
       stat.close();
    }
 
    /**
     * Check if urlFound is already in the given table
     * @param urlFound
     * @param table
     * @return 
     * @throws SQLException
     * @throws IOException
     */
    public synchronized boolean urlInDB(String urlFound, String table) throws SQLException, IOException
    {
       PreparedStatement pstmt = connection
             .prepareStatement("SELECT * FROM " + table + " WHERE url LIKE ?");
       pstmt.setString(1, urlFound);
       ResultSet result = pstmt.executeQuery();
 
       if (result.next())
       {
          //System.out.println("URL " + urlFound + " already in DB");
          pstmt.close();
          return true;
       }
       // System.out.println("URL "+urlFound+" not yet in DB");
       pstmt.close();
       return false;
    }
 
    /**
     * Insert a url into the given table with the specified urlid 
     * @param url
     * @param urlid
     * @param table
     * @throws SQLException
     * @throws IOException
     */
    public synchronized void insertURLInDB(String url, int urlid, String table)
          throws SQLException, IOException
    {
       PreparedStatement pstmt;
       if (table.equals(TABLE_URLS))
          pstmt = connection.prepareStatement("INSERT INTO " + table
                + " VALUES (?, ?, 1, '', '')");
       else
          pstmt = connection.prepareStatement("INSERT INTO " + table
                + " VALUES (?, ?, 1)");
       pstmt.setInt(1, urlid);
       pstmt.setString(2, url);
       pstmt.executeUpdate();
       pstmt.close();
    }
 
    /**
     * Check if a word is found in the specified table 
     * @param word
     * @param table
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public synchronized boolean wordInDB(String word, String table) throws SQLException, IOException
    {  
       PreparedStatement pstmt = connection
             .prepareStatement("SELECT * FROM " + table + " WHERE word LIKE ?");
       pstmt.setString(1, word);
       ResultSet result = pstmt.executeQuery();
 
       if (result.next())
       {
          //System.out.println("WORD " + word + " already in DB");
          pstmt.close();
          return true;
       }
       // System.out.println("URL "+urlFound+" not yet in DB");
       pstmt.close();
       return false;
    }
    
    /**
     * Insert a new word into the given table with the specified url list
     * @param word
     * @param urllist
     * @param table
     * @throws SQLException
     * @throws IOException
     */
    public synchronized void insertWordInDB(String word, String urllist, String table) throws SQLException, IOException
    {  
       PreparedStatement pstmt = connection
             .prepareStatement("INSERT INTO " + table + " VALUES (?, ?)");
       pstmt.setString(1, word);
       pstmt.setString(2, urllist);
       pstmt.executeUpdate();
       pstmt.close();
    }
    
    /**
     * Get the url list of a word from the specified table
     * @param word
     * @param table
     * @return urllist
     * @throws SQLException
     * @throws IOException
     */
    public synchronized String getURLListFromDB(String word, String table) throws SQLException, IOException
    {  
       PreparedStatement pstmt = connection
             .prepareStatement("SELECT * FROM " + table + " WHERE word LIKE ?");
       pstmt.setString(1, word);
       ResultSet result = pstmt.executeQuery();
       result.next();
       //System.out.println("WORD " + word + ": " + result.getString(2));
       String list = result.getString(2);
       pstmt.close();
       return list;
    }
    
    /**
     * Get the page rank of the given url in the specified table 
     * @param url
     * @param table
     * @return rank
     * @throws SQLException
     * @throws IOException
     */
    public synchronized int getURLRankFromDB(String url, String table) throws SQLException, IOException
    {  
       PreparedStatement pstmt = connection
             .prepareStatement("SELECT * FROM " + table + " WHERE url LIKE ?");
       pstmt.setString(1, url);
       ResultSet result = pstmt.executeQuery();
       result.next();
       //System.out.println("WORD " + word + ": " + result.getString(2));
       int rank = result.getInt(3);
       pstmt.close();
       return rank;
    }
    
    /**
     * Check to see if the given url actually exists and is found on the web.
     * This is an important check so the html parser is not given a url that is
     * no longer available or found. 
     * @param link
     * @return
     */
    public boolean isHTML(String link)
    {
       URL url;
       HttpURLConnection urlc = null;
       try
       {
          url = new URL(link);
          urlc = (HttpURLConnection) url.openConnection();
          urlc.setAllowUserInteraction(false);
          urlc.setDoInput(true);
          urlc.setDoOutput(false);
          urlc.setUseCaches(true);
          
          // Only get the head of the document to save space and time
          urlc.setRequestMethod("HEAD");
          urlc.connect();
          
          // Check the content type to make sure the document is HTML
          String mime = urlc.getContentType();
          if (mime.contains("text/html"))
          {
             return true;
          }
       }
       catch (Exception e)
       {
          //e.printStackTrace();
       }
       finally
       {
          if(urlc != null)
             urlc.disconnect();
       }
 
       return false;
    }
    
    /**
     * Check to see that an image actually exists given a url and has not been 
     * removed. 
     * @param url
     * @return
     */
    public boolean imageExists(String url){
       
       HttpURLConnection con = null;
       
       try {
         HttpURLConnection.setFollowRedirects(false);
         
         // Only get the head of the document to save space and time
         con =
            (HttpURLConnection) new URL(url).openConnection();
         con.setRequestMethod("HEAD");
         return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
       }
       catch (Exception e) {
          //e.printStackTrace();
          return false;
       }
       finally {
          if(con != null)
             con.disconnect();
       }
     }
 
    /**
     * Get the contents of a specified meta tag. This is used to get the 
     * document description in the main crawler method. 
     * @param document
     * @param attr
     * @return
     */
    public String getMetaTag(Document document, String attr)
    {
       // Try to see if there is content in the name variable 
       Elements elements = document.select("meta[name=" + attr + "]");
       for (Element element : elements)
       {
          final String s = element.attr("content");
          if (s != null)
             return s;
       }
       
       // Try to see if there is content in the property variable 
       elements = document.select("meta[property=" + attr + "]");
       for (Element element : elements)
       {
          final String s = element.attr("content");
          if (s != null)
             return s;
       }
       return null;
    }
 
    /**
     * An initializing method for the crawl. Opens a database connection, creates
     * the DB and sets the necessary variables to their appropriate values. 
     * @throws SQLException
     * @throws IOException
     */
    public void startCrawl() throws SQLException, IOException
    {
       /* If the reset flag is not on, no need to recreate the DB. Just open the
        * connection. If there are no urls scanned, reset database anyways. 
        */
       if(!reset && NextURLIDScanned > 0) 
       {
          openConnection();
          return;
       }
       
       createDB();
       
       // Reset variables
       NextURLID = 0;
       NextURLIDScanned = 0;
       NextImageURLID = 0;
       
       /* For each url in the entered urlList from either the properties file or 
        * command arguments, add them to the database and increment the counter
        */
       int urlID;
       for(String url : urlList)
       {
          urlID = NextURLID;
          insertURLInDB(url, urlID, TABLE_URLS);
          NextURLID++;
       }
       
       // Save the counters to the properties file
       setProperties();
       
       return;
       
    }
    
    /**
     * Get the next url from the specified table to be parsed
     * @param table
     * @return url
     * @throws SQLException
     * @throws IOException
     */
    public synchronized String getNextURL(String table) throws SQLException, IOException
    {
       // Get the variables from the properties file
       setVariables();
       
       // Create a query to get the required row
       urlIndex = NextURLIDScanned;
       Statement stat = connection.createStatement();
       ResultSet result = stat
             .executeQuery("SELECT * FROM " + table + " WHERE urlid = " + urlIndex);
       result.next();
       
       // Get the url from the url column 
       String url1 = result.getString(2);
       stat.close();
       
       NextURLIDScanned++;
       
       // Save the counters to the properties file
       setProperties();
       
       return url1;
    }
    
    /**
     * Open connection to url1 and create a DOM document from the Jsoup parser.
     * @param url1
     * @return document
     * @throws IOException
     */
    public synchronized Document parseURL(String url1) throws IOException
    {
       InputStream in = null;
       Document doc = null;
       try
       {
          System.out.println("[" + Thread.currentThread().getName() +"] [" + urlIndex + "] " + url1);
          in = new URL(url1).openStream();
          doc = Jsoup.parse(in, "ISO-8859-1", url1);
       }
       catch (Exception e)
       {
          //e.printStackTrace();
          System.out.println("Could Not Obtain Document...");
          System.out.println("Skipped.");
          System.out.println("-------------------------------------------");
          doc = null;
       }
       finally
       {
          if (in != null)
             in.close();
       }
       
       return doc;
    }
 
    /**
     * Extract all href links from the given document and add them to the 
     * database
     * @param doc
     * @throws SQLException
     * @throws IOException
     */
    public synchronized void extractDocumentURLs(Document doc)
          throws SQLException, IOException
    {
       Elements links;
       PreparedStatement pstmt;
 
       System.out.println("Extracting URLs...");
       links = doc.select("a");
 
       for (Element e : links)
       {
          // Get the absolute url
          String urlFound = e.attr("abs:href");
          urlFound = urlFound.trim();
          boolean found = urlInDB(urlFound, TABLE_URLS);
 
          if (found)
          {
             // Increment the current url's rank and update its value in the DB
             int rank = getURLRankFromDB(urlFound, TABLE_URLS);
             rank++;
 
             // Increment rank in url found
             pstmt = connection
                   .prepareStatement("UPDATE urls SET rank = ? WHERE url = ?");
             pstmt.setInt(1, rank);
             pstmt.setString(2, urlFound);
             pstmt.executeUpdate();
             pstmt.close();
          }
 
          /* For a url to be added to the DB it must be http, contain the domain,
           * and must be an HTML document
           */
         if (!found && urlFound.contains("http://")
                && urlFound.contains(domain) && !urlFound.contains("#")
                && isHTML(urlFound))
          {
             insertURLInDB(urlFound, NextURLID, TABLE_URLS);
             NextURLID++;
             
             // Save the counters to the properties file
             setProperties();
          }
       }
 
    }
 
    /**
     * Extract all images from the given document 
     * @param doc
     * @throws SQLException
     * @throws IOException
     */
    public synchronized void extractDocumentImages(Document doc)
          throws SQLException, IOException
    {
       Elements images;
       PreparedStatement pstmt;
 
       System.out.println("Extracting Images...");
       images = doc.select("img");
 
       for (Element e : images)
       {
          // Get the absolute image url
          String imageFound = e.attr("abs:src");
          imageFound = imageFound.trim();
          boolean found = urlInDB(imageFound, TABLE_IMAGES);
 
          if (found)
          {
             // Increment the current url's rank and update its value in the DB
             int rank = getURLRankFromDB(imageFound, TABLE_IMAGES);
             rank++;
 
             // Increment rank in url found
             pstmt = connection
                   .prepareStatement("UPDATE images SET rank = ? WHERE url = ?");
             pstmt.setInt(1, rank);
             pstmt.setString(2, imageFound);
             pstmt.executeUpdate();
             pstmt.close();
          }
          /* For an image to be added to the DB it must be http and must exist
           */
          if (!found && imageFound.contains("http://") && imageExists(imageFound))
          {
             insertURLInDB(imageFound, NextImageURLID, TABLE_IMAGES);
             NextImageURLID++;
             
             // Save the counters to the properties file
             setProperties();
          }
       }
    }
 
    /**
     * The main crawl method. This is where all the magic happens.
     * Uses Breadth-First Searching algorithm to get urls and images, parse 
     * documents, and populate the database
     * @throws SQLException
     * @throws IOException
     * @throws InterruptedException
     */
    public void crawl() throws SQLException, IOException, InterruptedException
    {
       
       Document doc;
       String text, urllist;
       int imgIndexStart = 0;
       int imgIndexEnd = 0;
       String[] words;
       PreparedStatement pstmt;
       setVariables();
             
       // Let the first thread do some work before starting the rest of the threads.
       if(!Thread.currentThread().getName().equals("0"))
       {
          while(NextURLIDScanned < nThread)
             Thread.sleep(100);
       }
          
             
       while(NextURLIDScanned < NextURLID)
       {
          // Get the next url to be parsed
          String url1 = getNextURL(TABLE_URLS);
          doc = parseURL(url1);
          if(doc == null)
             continue;
          
          synchronized (monitor)
          {
             // Get title
             String title = doc.title();
             
             /* Get Description by either the description meta tag, first 100 
              * characters of the body or just the first 100 characters
              */
             String description = getMetaTag(doc, "description");
             if (description == null && doc.body() != null)
                description = doc.body().text();
             else if(description == null)
                description = doc.text();
             if (description.length() > 100)
                description = description.substring(0, 100);
             
             System.out.println(description);
 
             // Update database with description and title
             pstmt = connection
                   .prepareStatement("UPDATE urls SET title = ?, description = ? WHERE url like ?");
             pstmt.setString(1, title);
             pstmt.setString(2, description);
             pstmt.setString(3, url1);
             pstmt.executeUpdate();
             pstmt.close();
             
             // Custom checks for cs.purdue.edu 
             if(url1.contains("#") || url1.contains("&oldid=") || url1.contains("calendar/webevent.cgi?"))
                continue;
          }
 
          // Get all link elements
          if (NextURLID < MaxURLs)
          {
             extractDocumentURLs(doc);
          }
          
          /* Get the start and end indices to get the total number of images in
           * the document
           */
          imgIndexStart = NextImageURLID;
          extractDocumentImages(doc);
          imgIndexEnd = NextImageURLID;
  
          // Get document parsed text (no tags)
          System.out.println("Parsing Document...");
          // Remove html white spaces
          doc.select(":containsOwn(\u00a0)").remove();
          text = doc.text();
          // Remove remaining HTML code
          text = Jsoup.clean(text, Whitelist.relaxed());
          
          // Get each word of the document
          words = text.split("\\s+"); // split the string by white spaces to get individual words
          for(String word : words)
          {
             word = word.toLowerCase(); // Lower case all words
             word = word.replaceAll("[^A-Za-z0-9]", ""); // Remove punctuation
             if (word.matches("[a-zA-Z0-9]+")) // If the word is letters and numbers only
             {
                // Add words to urls table
                if (!wordInDB(word, TABLE_WORD))
                {
                   // If the word is not in the table, create a new entry
                   insertWordInDB(word, urlIndex + "", TABLE_WORD);           
                }
                else
                {
                   // Else, update the current entry with the current urlIndex
                   urllist = getURLListFromDB(word, TABLE_WORD);
                   if(!urllist.contains(String.valueOf(urlIndex))) // Check to not add duplicates
                      urllist += "," + urlIndex;
                   synchronized (monitor)
                   {
                      pstmt = connection
                            .prepareStatement("UPDATE word SET urllist = ? WHERE word = ?");
                      pstmt.setString(1, urllist);
                      pstmt.setString(2, word);
                      pstmt.executeUpdate();
                      pstmt.close();
                   }
                }
                
                // Add words to image table
                if (!wordInDB(word, TABLE_IMGWORD))
                {
                   if(imgIndexStart != imgIndexEnd)
                   {
                      /*
                       * Since every document can have multiple images, each word
                       * must contain all image urlids found in that document
                       */
                      urllist = imgIndexStart + "";
                      for (int i = imgIndexStart + 1; i < imgIndexEnd; i++)
                         urllist = urllist + "," + i;
                      insertWordInDB(word, urllist, TABLE_IMGWORD);
                   }
                }
                else
                {            
                   /* add the words to the imgWord table as well for each image 
                    * found in the current doc
                    */
                   urllist = getURLListFromDB(word, TABLE_IMGWORD);
                   for (int i = imgIndexStart + 1; i < imgIndexEnd; i++)
                   {
                      if (!urllist.contains(String.valueOf(i))) // Check to not add duplicates
                         urllist += "," + i;
                   }
                   synchronized (monitor)
                   {
                      pstmt = connection
                            .prepareStatement("UPDATE imgword SET urllist = ? WHERE word = ?");
                      pstmt.setString(1, urllist);
                      pstmt.setString(2, word);
                      pstmt.executeUpdate();
                      pstmt.close();
                   }
                }
             }
          }
          
          System.out.println("Done.");
          System.out.println("-------------------------------------------");
          
          setProperties();
         //setVariables();
       }
    }
    
    /**
     * Create the Crawler object with initial command line arguments
     * @param args
     * @return
     * @throws IOException
     */
    public static Crawler crawlerWithArguments(String[] args) throws IOException
    {
       // Default values
       String domain = "cs.purdue.edu";
       String root = "http://www.cs.purdue.edu/";
       int MaxURLs = 1000;
       boolean reset = false;
       ArrayList<String> urlList = new ArrayList<String>();
       
       // Parse arguments
       for(int i = 0; i < args.length; i++)
       {
          if(args[i].equals("-u")) // Max URLs
          {
             MaxURLs = Integer.parseInt(args[++i]);
          }
          else if(args[i].equals("-d")) // Domain
          {
             domain = args[++i];
          }
          else if(args[i].equals("-r")) // Reset flag
          {
             reset = true;
          }
          else
          {
             urlList.add(args[i]); // URL List
          }
       }
       
       if(urlList.size() == 0)
          urlList.add(root);
       
       System.out.println("ARGS: webcrawl -u " + MaxURLs + " -d " + domain + " -r " + reset + " " + urlList);
       Crawler crawler = new Crawler(MaxURLs, domain, reset, urlList);
       
       return crawler;
    }
    
    /**
     * Create the Crawler object with the properties file if available
     * @return
     * @throws IOException
     */
    public static Crawler crawlerWithProperties() throws IOException
    {
       // Open the properties file
       Properties props = new Properties();
       FileInputStream in = new FileInputStream("WebContent/WEB-INF/database.properties");
       props.load(in);
       in.close();
       
       // Get the necessary arguments
       String domain = props.getProperty("crawler.domain");
       String root = props.getProperty("crawler.root");
       int MaxURLs = Integer.parseInt(props.getProperty("crawler.maxurls"));
       boolean reset;
       if(props.getProperty("crawler.reset").equals("YES"))
          reset = true;
       else
          reset = false;
       ArrayList<String> urlList = new ArrayList<String>();
       urlList.add(root);
       
       // Create the crawler
       System.out.println("PROPS: webcrawl -u " + MaxURLs + " -d " + domain + " -r " + reset + " " + urlList);
       Crawler crawler = new Crawler(MaxURLs, domain, reset, urlList);
       
       return crawler;
       
    }
    
    /**
     * Attempt to create a Crawler from either the properties file, command line
     * arguments, or default values. Which ever one is available.
     * @param args
     * @return
     * @throws IOException
     */
    public static Crawler createCrawler(String[] args) throws IOException
    {
       // Default Values
       String domain = "cs.purdue.edu";
       String root = "http://www.cs.purdue.edu/";
       int MaxURLs = 1000;
       boolean reset = true;
       ArrayList<String> urlList = new ArrayList<String>();
       
       File propertiesFile = new File("WebContent/WEB-INF/database.properties");
       Crawler crawler;
       
       // Get the necessary variables from args, props file, or defaults 
       // Arguments given
       if(args.length > 0) 
          crawler = crawlerWithArguments(args);
       // No arguments given but properties file is available
       else if(args.length == 0 || propertiesFile.exists())
          crawler = crawlerWithProperties();
       // Default values
       else
       {
          urlList.add(root);
          System.out.println("DEFAULT: webcrawl -u " + MaxURLs + " -d " + " -r " + reset + " " + domain + " " + urlList);
          crawler = new Crawler(MaxURLs, domain, reset, urlList);
       }  
             
       return crawler;
    }
 
    public static void main(String[] args) throws IOException
    {   
       // Create the crawler
       Crawler crawler = createCrawler(args);
       
       //Create the threads
       Thread threads[] = new Thread[nThread];
       for(int i = 0; i < threads.length; i++)
      {
         threads[i] = new Thread(crawler, "" + i);
      }      
       
       // Crawl that sucker
       try
       {
          System.out.println("Initializing Crawl Sequence...");
          crawler.startCrawl();
          System.out.println("Crawling...");
          System.out.println("-------------------------------------------");
          for(int i = 0; i < threads.length; i++)
         {
             threads[i].start();
         }
          
          for(int i = 0; i < threads.length; i++)
             threads[i].join();                          
       }
       catch (Exception e)
       {
          e.printStackTrace();
       }
       System.out.println("Crawling Complete.");
    }
 
 }
