 
 import java.io.IOException;
 import java.sql.*;
 import java.util.HashMap;
 import java.util.concurrent.*;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 
 import org.xml.sax.helpers.DefaultHandler;
 
 public class XMLSAXParser extends DefaultHandler {
 
     private long startTime;
     private long endTime;
     //Toggle optimizations
    public static final boolean useHashMap = false;
     private static final boolean useParallel = true;
     //END Toggles
     private HashMap<String, Integer> genres;
     private HashMap<String, Integer> people;
     private HashMap<String, Integer> booktitle;
     private HashMap<String, Integer> publishers;
     private ExecutorService eservice;
     private String tempVal;
     private document tempDoc;
     //Connection Pool
     private Connection[] connection;
     private int conNum;
     private static final int maxCon = 6;
 
     public XMLSAXParser() {
         genres = new HashMap<String, Integer>();
         people = new HashMap<String, Integer>();
         booktitle = new HashMap<String, Integer>();
         publishers = new HashMap<String, Integer>();
 
 
         conNum = 0;
         connection = new Connection[maxCon];
 
         String password = "testpass";
         String username = "testuser";
         String server = "localhost";
         String tablename = "bookdb";
         try {
             for (int i = 0; i < maxCon; i++) {
                 connection[i] = DriverManager.getConnection("jdbc:mysql://" + server + ":3306/" + tablename, username, password);
 //                connection[i].setAutoCommit(false);//TESTING
             }
         } catch (SQLException ex) {
             System.out.println(ex.getMessage());
         }
         int nrOfProcessors = Runtime.getRuntime().availableProcessors();
         //NUMBER OF PROCS
         eservice = Executors.newFixedThreadPool(Math.max(nrOfProcessors, maxCon) * 2);
 
     }
 
     public static void main(String[] args) {
         XMLSAXParser spe = new XMLSAXParser();
         spe.runExample();
         System.exit(0);
     }
 
     public void runExample() {
         parseDocument();
     }
 
     private void parseDocument() {
 
         //get a factory
         SAXParserFactory spf = SAXParserFactory.newInstance();
         try {
 
             //get a new instance of parser
             SAXParser sp = spf.newSAXParser();
 
             //Make side DBs unique
             try {
                 Statement st = connection[conNum].createStatement();
                 nextConnection();
                 st.execute("ALTER TABLE tbl_genres ADD UNIQUE (genre_name)");
 
                 st = connection[conNum].createStatement();
                 nextConnection();
                 st.execute("ALTER TABLE tbl_people  ADD UNIQUE (name)");
 
                 st = connection[conNum].createStatement();
                 nextConnection();
                 st.execute("ALTER TABLE tbl_booktitle ADD UNIQUE (title)");
 
                 st = connection[conNum].createStatement();
                 nextConnection();
                 st.execute("ALTER TABLE tbl_publisher ADD UNIQUE (publisher_name)");
             } catch (SQLException ex) {
                 System.out.println(ex.getMessage());
             }
 
 
             //parse the file and also register this class for call backs
             startTime = System.currentTimeMillis();
 //            sp.parse("final-data.xml", this);//=============SMALL
             sp.parse("dblp-data.xml", this);//================LARGE
 
             if (useParallel) {
                 System.out.println("========== Parsing Complete (" + (System.currentTimeMillis() - startTime) + " ms): Waiting for MYSQL to finish... ==========");
                 try {
                     //Stop addition of new parallels
                     eservice.shutdown();
                     int i = 0;
                     //Wait for processes to finish
                     while (!eservice.awaitTermination(1, TimeUnit.SECONDS)) {
                         //Wait till threads are done, check each second
                         i++;
 
                         if (i % 10 == 0) {
                             System.out.print("|");
                         } else {
                             System.out.print(".");
                         }
 
                         if (i % 60 == 0) {
                             System.out.println();
                         }
 
                     }
                 } catch (InterruptedException ex) {
                     System.out.println(ex.getMessage());
                 }
             }
             endTime = System.currentTimeMillis();
             System.out.println("\n========== Total Execution Time: " + (endTime - startTime) + " ms ==========");
 
             //remove special conditions for parsing
             try {
                 Statement st = connection[conNum].createStatement();
                 nextConnection();
                 st.execute("ALTER TABLE tbl_genres DROP INDEX genre_name;");
 
                 st = connection[conNum].createStatement();
                 nextConnection();
                 st.execute("ALTER TABLE tbl_people DROP INDEX name");
 
                 st = connection[conNum].createStatement();
                 nextConnection();
                 st.execute("ALTER TABLE tbl_booktitle DROP INDEX title");
 
                 st = connection[conNum].createStatement();
                 nextConnection();
                 st.execute("ALTER TABLE tbl_publisher DROP INDEX publisher_name");
 
 
 //                for (int i = 0; i < maxCon; i++) {
 //                    connection[i].commit();//TESTING
 //                    connection[i].close();
 //                }
             } catch (SQLException ex) {
                 System.out.println(ex.getMessage());
             }
 
         } catch (SAXException se) {
             System.out.println(se.getMessage());
         } catch (ParserConfigurationException pce) {
             System.out.println(pce.getMessage());
         } catch (IOException ie) {
             System.out.println(ie.getMessage());
         }
     }
 
     //Event Handlers
     public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
         //reset
         tempVal = "";
         if (isGenreElement(qName)) {
             if (useParallel) {
                 tempDoc = new document(eservice.submit(new SqlGetIDTask(connection[conNum], genres, "tbl_genres", "genre_name", qName)));
                 nextConnection();
             } else {
                 tempDoc = new document(getGenreID(qName));
             }
         }
     }
 
     public void characters(char[] ch, int start, int length) throws SAXException {
         String value = new String(ch, start, length);
         tempVal += value.replaceAll("\n", "");
     }
 
     public void endElement(String uri, String localName, String qName) throws SAXException {
         try {
             if (isGenreElement(qName)) {
                 try {
                     //add book to the db;
 
                     Statement st = null;
                     Future FDocID;
                     Integer docID = 0;
                     if (useParallel) {
                         FDocID = eservice.submit(new SqlInsertTask(connection[conNum], tempDoc));
                         nextConnection();
                         for (Future FAuthor : tempDoc.getAuthorsIDsFuture()) {
                             eservice.submit(new SqlTask(connection[conNum], "INSERT INTO tbl_author_document_mapping (doc_id, author_id) VALUES ", FDocID, FAuthor));
                             nextConnection();
                         }
                     } else {
                         st = connection[conNum].createStatement();
                         st.executeUpdate("INSERT INTO tbl_dblp_document " + tempDoc.getColAndVal());
                         docID = getLastID(connection[conNum]);
                         nextConnection();
                         for (Integer author : tempDoc.getAuthorsIDs()) {
                             st = connection[conNum].createStatement();
                             st.executeUpdate("INSERT INTO tbl_author_document_mapping (doc_id, author_id) VALUES ('" + docID + "','" + author + "')");
                         }
                     }
 
                     tempDoc = null;
 
                     if (st != null) {
                         st.close();
                     }
                 } catch (SQLException ex) {
                     System.out.println(ex.getMessage());
                 }
 
             } else if (qName.equalsIgnoreCase("Author")) {
                 if (useParallel) {
                     tempDoc.addAuthorsIDsFuture(eservice.submit(new SqlGetIDTask(connection[conNum], people, "tbl_people", "name", tempVal.substring(0, Math.min(tempVal.length(), 61)).trim())));
                     nextConnection();
                 } else {
                     tempDoc.addAuthorsIDs(getPersonID(tempVal.trim()));
                 }
             } else if (qName.equalsIgnoreCase("Editor")) {
                 if (useParallel) {
                     Future err = tempDoc.setEditor_idFuture(eservice.submit(new SqlGetIDTask(connection[conNum], people, "tbl_people", "name", tempVal.substring(0, Math.min(tempVal.length(), 61)).trim())));
                     nextConnection();
                     if (err != null) {
                         eservice.submit(new printTask("Multiple editor_id: ", err));
                     }
                 } else {
                     tempDoc.setEditor_id(getPersonID(tempVal.substring(0, Math.min(tempVal.length(), 61)).trim()));
                 }
             } else if (qName.equalsIgnoreCase("Booktitle")) {
                 if (useParallel) {
                     Future err = tempDoc.setBooktitle_idFuture(eservice.submit(new SqlGetIDTask(connection[conNum], booktitle, "tbl_booktitle", "title", tempVal.substring(0, Math.min(tempVal.length(), 300)).trim())));
                     nextConnection();
                     if (err != null) {
                         eservice.submit(new printTask("Multiple booktitle_id: ", err));
                     }
                 } else {
                     tempDoc.setBooktitle_id(getBooktitleID(tempVal.substring(0, Math.min(tempVal.length(), 300)).trim()));
                 }
             } else if (qName.equalsIgnoreCase("Publisher")) {
                 if (useParallel) {
                     Future err = tempDoc.setPublisher_idFuture(eservice.submit(new SqlGetIDTask(connection[conNum], publishers, "tbl_publisher", "publisher_name", tempVal.substring(0, Math.min(tempVal.length(), 300)).trim())));
                     nextConnection();
                     if (err != null) {
                         eservice.submit(new printTask("Multiple publisher_id: ", err));
                     }
                 } else {
                     tempDoc.setPublisher_id(getPublisherID(tempVal.substring(0, Math.min(tempVal.length(), 300)).trim()));
                 }
             } else if (qName.equalsIgnoreCase("Title")) {
                 tempDoc.setTitle(tempVal.substring(0, Math.min(tempVal.length(), 300)).trim());
             } else if (qName.equalsIgnoreCase("Pages")) {
                 tempDoc.setPages(tempVal.trim());
             } else if (qName.equalsIgnoreCase("Year")) {
                 tempDoc.setYear(Integer.parseInt(tempVal.trim()));
             } else if (qName.equalsIgnoreCase("Volume")) {
                 tempDoc.setVolume(Integer.parseInt(tempVal.trim()));
             } else if (qName.equalsIgnoreCase("Number")) {
                 tempDoc.setNumber(Integer.parseInt(tempVal.trim()));
             } else if (qName.equalsIgnoreCase("Url")) {
                 tempDoc.setUrl(tempVal.substring(0, Math.min(tempVal.length(), 200)).trim());
             } else if (qName.equalsIgnoreCase("ee")) {
                 tempDoc.setEe(tempVal.substring(0, Math.min(tempVal.length(), 100)).trim());
             } else if (qName.equalsIgnoreCase("CDrom")) {
                 tempDoc.setCdrom(tempVal.substring(0, Math.min(tempVal.length(), 75)).trim());
             } else if (qName.equalsIgnoreCase("Cite")) {
                 tempDoc.setCite(tempVal.substring(0, Math.min(tempVal.length(), 75)).trim());
             } else if (qName.equalsIgnoreCase("Crossref")) {
                 tempDoc.setCrossref(tempVal.substring(0, Math.min(tempVal.length(), 75)).trim());
             } else if (qName.equalsIgnoreCase("ISBN")) {
                 tempDoc.setIsbn(tempVal.substring(0, Math.min(tempVal.length(), 21)).trim());
             } else if (qName.equalsIgnoreCase("Series")) {
                 tempDoc.setSeries(tempVal.substring(0, Math.min(tempVal.length(), 100)).trim());
             }
         } catch (NumberFormatException e) {
             System.out.println("Invalid Number: " + e.getMessage());
         }
     }
 
     private boolean isGenreElement(String qName) {
         if (qName.equalsIgnoreCase("article")
                 || qName.equalsIgnoreCase("inproceedings")
                 || qName.equalsIgnoreCase("proceedings")
                 || qName.equalsIgnoreCase("book")
                 || qName.equalsIgnoreCase("incollection")
                 || qName.equalsIgnoreCase("phdthesis")
                 || qName.equalsIgnoreCase("mastersthesis")
                 || qName.equalsIgnoreCase("www")) {
             return true;
         }
         return false;
     }
 
     private Integer getGenreID(String genreName) {
         Integer ret = 0;
         if (useHashMap && genres.containsKey(genreName)) {
             return genres.get(genreName);
         }
         try {
             Statement st = connection[conNum].createStatement();
             nextConnection();
             ResultSet genreID = st.executeQuery("SELECT * FROM tbl_genres WHERE genre_name = '" + cleanSQL(genreName) + "'");
             if (genreID.next()) {
                 int id = genreID.getInt("id");
                 st.close();
                 if (useHashMap) {
                     genres.put(genreName, id);
                 }
                 return id;
             } else {
                 int id;
                 st = connection[conNum].createStatement();
                 //Sync all uses of getLastID()
                 st.executeUpdate("INSERT INTO tbl_genres (genre_name) VALUE ('" + cleanSQL(genreName) + "')");
                 id = getLastID(connection[conNum]);
                 nextConnection();
                 st.close();
                 if (useHashMap) {
                     genres.put(genreName, id);
                 }
                 return id;
             }
         } catch (SQLException ex) {
             System.out.println(ex.getMessage());
         }
         return ret;
     }
 
     private Integer getPersonID(String personName) {
         Integer ret = 0;
         if (useHashMap && people.containsKey(personName)) {
             return people.get(personName);
         }
         try {
             Statement st = connection[conNum].createStatement();
             nextConnection();
             ResultSet personID = st.executeQuery("SELECT * FROM tbl_people WHERE name = '" + cleanSQL(personName) + "'");
             if (personID.next()) {
                 int id = personID.getInt("id");
                 st.close();
                 if (useHashMap) {
                     people.put(personName, id);
                 }
                 return id;
             } else {
                 int id;
                 st = connection[conNum].createStatement();
                 //Sync all uses of getLastID()
                 st.executeUpdate("INSERT INTO tbl_people (name) VALUE ('" + cleanSQL(personName) + "')");
                 id = getLastID(connection[conNum]);
                 nextConnection();
                 st.close();
                 if (useHashMap) {
                     people.put(personName, id);
                 }
                 return id;
             }
         } catch (SQLException ex) {
             System.out.println(ex.getMessage());
         }
         return ret;
     }
 
     private Integer getBooktitleID(String booktitleName) {
         Integer ret = 0;
         if (useHashMap && booktitle.containsKey(booktitleName)) {
             return booktitle.get(booktitleName);
         }
         try {
             Statement st = connection[conNum].createStatement();
             nextConnection();
             ResultSet booktitleID = st.executeQuery("SELECT * FROM tbl_booktitle WHERE title = '" + cleanSQL(booktitleName) + "'");
             if (booktitleID.next()) {
                 int id = booktitleID.getInt("id");
                 st.close();
                 if (useHashMap) {
                     booktitle.put(booktitleName, id);
                 }
                 return id;
             } else {
                 int id;
                 st = connection[conNum].createStatement();
                 //Sync all uses of getLastID()
                 st.executeUpdate("INSERT INTO tbl_booktitle (title) VALUE ('" + cleanSQL(booktitleName) + "')");
                 id = getLastID(connection[conNum]);
                 nextConnection();
                 st.close();
                 if (useHashMap) {
                     booktitle.put(booktitleName, id);
                 }
                 return id;
             }
         } catch (SQLException ex) {
             System.out.println(ex.getMessage());
         }
         return ret;
     }
 
     private Integer getPublisherID(String publisherName) {
         Integer ret = 0;
         if (useHashMap && publishers.containsKey(publisherName)) {
             return publishers.get(publisherName);
         }
         try {
             Statement st = connection[conNum].createStatement();
             nextConnection();
             ResultSet publisherID = st.executeQuery("SELECT * FROM tbl_publisher WHERE publisher_name = '" + cleanSQL(publisherName) + "'");
             if (publisherID.next()) {
                 int id = publisherID.getInt("id");
                 st.close();
                 if (useHashMap) {
                     publishers.put(publisherName, id);
                 }
                 return id;
             } else {
                 int id;
                 st = connection[conNum].createStatement();
                 //Sync all uses of getLastID()
                 st.executeUpdate("INSERT INTO tbl_publisher (publisher_name) VALUE ('" + cleanSQL(publisherName) + "')");
                 id = getLastID(connection[conNum]);
                 nextConnection();
                 st.close();
                 if (useHashMap) {
                     publishers.put(publisherName, id);
                 }
                 return id;
             }
         } catch (SQLException ex) {
             System.out.println(ex.getMessage());
         }
         return ret;
     }
 
     public static String cleanSQL(String arg) {
         String rtn = arg.replace("\\", "\\\\");
         return rtn.replace("'", "''");
     }
 
     private Integer getLastID(Connection conn) throws SQLException {
         //MUST SYNC all uses of getLastID()
         Statement st = conn.createStatement();
         ResultSet lastIDQ = st.executeQuery("SELECT LAST_INSERT_ID()");
         if (lastIDQ.next()) {
             int id = lastIDQ.getInt(1);
             st.close();
             return id;
         } else {
             return null;
         }
     }
 
     private void nextConnection() {
         conNum = ++conNum % maxCon;
     }
 }
