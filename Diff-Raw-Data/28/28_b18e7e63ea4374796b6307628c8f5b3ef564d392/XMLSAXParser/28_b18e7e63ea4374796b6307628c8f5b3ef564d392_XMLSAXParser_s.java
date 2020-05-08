 
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
     private static final boolean useHashMap = true;
     private static final boolean useCombinedAuthorStatement = false;
     private static final boolean useParallel = true;
     //END Toggles
     private HashMap<String, Integer> genres;
     private HashMap<String, Integer> people;
     private HashMap<String, Integer> booktitle;
     private HashMap<String, Integer> publishers;
     private ExecutorService eservice;
     private String tempVal;
     private document tempDoc;
     private Connection connection;
 
     public XMLSAXParser() {
         genres = new HashMap<String, Integer>();
         people = new HashMap<String, Integer>();
         booktitle = new HashMap<String, Integer>();
         publishers = new HashMap<String, Integer>();
 
         String password = "testpass";
         String username = "testuser";
         String server = "localhost";
         String tablename = "bookdb";
         try {
             connection = DriverManager.getConnection("jdbc:mysql://" + server + ":3306/" + tablename, username, password);
         } catch (SQLException ex) {
             System.out.println(ex.getMessage());
         }
         int nrOfProcessors = Runtime.getRuntime().availableProcessors();
         eservice = Executors.newFixedThreadPool(nrOfProcessors);
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
 
             //parse the file and also register this class for call backs
             startTime = System.currentTimeMillis();
 //            sp.parse("final-data.xml", this);//=============SMALL
             sp.parse("dblp-data.xml", this);//================LARGE
             endTime = System.currentTimeMillis();
             System.out.println("Execution Time: " + (endTime - startTime));
 
 
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
             tempDoc = new document(getGenreID(qName));
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
 
                     Statement st = connection.createStatement();
                     String addDoc = "INSERT INTO tbl_dblp_document " + tempDoc.getColAndVal();
                     Future FDocID;
                     Integer docID = 0;
                     if (useParallel) {
                         FDocID = eservice.submit(new SqlInsertTask(this, connection, addDoc));
                     } else {
                         st.executeUpdate(addDoc);
                         docID = getLastID();
                     }
 
                     if (useCombinedAuthorStatement) {
                         String values = "";
                         if (useParallel) {
                             try {
                                 docID = (Integer) FDocID.get();
                             } catch (InterruptedException ex) {
                             } catch (ExecutionException ex) {
                             }
                         }
                         for (Integer author : tempDoc.getAuthorsIDs()) {
                             values += " ('" + docID + "', '" + author + "'),";
                         }
                         if (!values.isEmpty()) {
                             values = values.substring(0, values.length() - 1);
                             if (useParallel) {
                                 eservice.submit(new SQLTask(connection, "INSERT INTO tbl_author_document_mapping (doc_id, author_id) VALUES " + values));
                             } else {
                                 st = connection.createStatement();
                                 st.executeUpdate("INSERT INTO tbl_author_document_mapping (doc_id, author_id) VALUES " + values);
                             }
                         }
                     } else {
                         for (Integer author : tempDoc.getAuthorsIDs()) {
                             if (useParallel) {
                                 try {
                                     eservice.submit(new SQLTask(connection, "INSERT INTO tbl_author_document_mapping (doc_id, author_id) VALUES", FDocID, author));
                                 } catch (InterruptedException ex) {
                                 } catch (ExecutionException ex) {
                                 }
                             } else {
                                 st = connection.createStatement();
                                 st.executeUpdate("INSERT INTO tbl_author_document_mapping (doc_id, author_id) VALUES ('" + docID + "','" + author + "')");
                             }
                         }
                     }
                     st.close();
                 } catch (SQLException ex) {
                     System.out.println(ex.getMessage());
                 }
 
//TODO enforce string length limits
             } else if (qName.equalsIgnoreCase("Author")) {
                 tempDoc.addAuthorsIDs(getPersonID(tempVal.trim()));
             } else if (qName.equalsIgnoreCase("Editor")) {
                 tempDoc.setEditor_id(getPersonID(tempVal.trim()));
             } else if (qName.equalsIgnoreCase("Booktitle")) {
                 tempDoc.setBooktitle_id(getBooktitleID(tempVal.trim()));
             } else if (qName.equalsIgnoreCase("Publisher")) {
                 tempDoc.setPublisher_id(getPublisherID(tempVal.trim()));
             } else if (qName.equalsIgnoreCase("Title")) {
                tempDoc.setTitle(tempVal.substring(0, Math.min(tempVal.length(), 301) ).trim());
             } else if (qName.equalsIgnoreCase("Pages")) {
                 tempDoc.setPages(tempVal.trim());
             } else if (qName.equalsIgnoreCase("Year")) {
                 tempDoc.setYear(Integer.parseInt(tempVal.trim()));
             } else if (qName.equalsIgnoreCase("Volume")) {
                 tempDoc.setVolume(Integer.parseInt(tempVal.trim()));
             } else if (qName.equalsIgnoreCase("Number")) {
                 tempDoc.setNumber(Integer.parseInt(tempVal.trim()));
             } else if (qName.equalsIgnoreCase("Url")) {
                tempDoc.setUrl(tempVal.substring(0, Math.min(tempVal.length(), 201) ).trim());
             } else if (qName.equalsIgnoreCase("ee")) {
                tempDoc.setEe(tempVal.substring(0, Math.min(tempVal.length(), 101) ).trim());
             } else if (qName.equalsIgnoreCase("CDrom")) {
                tempDoc.setCdrom(tempVal.substring(0, Math.min(tempVal.length(), 76) ).trim());
             } else if (qName.equalsIgnoreCase("Cite")) {
                tempDoc.setCite(tempVal.substring(0, Math.min(tempVal.length(), 76) ).trim());
             } else if (qName.equalsIgnoreCase("Crossref")) {
                tempDoc.setCrossref(tempVal.substring(0, Math.min(tempVal.length(), 76) ).trim());
             } else if (qName.equalsIgnoreCase("ISBN")) {
                tempDoc.setIsbn(tempVal.substring(0, Math.min(tempVal.length(), 22) ).trim());
             } else if (qName.equalsIgnoreCase("Series")) {
                tempDoc.setSeries(tempVal.substring(0, Math.min(tempVal.length(), 101) ).trim());
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
             Statement st = connection.createStatement();
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
                 st = connection.createStatement();
                 //Sync all uses of getLastID()
                 synchronized (this) {
                     st.executeUpdate("INSERT INTO tbl_genres (genre_name) VALUE ('" + cleanSQL(genreName) + "')");
                     id = getLastID();
                 }
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
             Statement st = connection.createStatement();
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
                 st = connection.createStatement();
                 //Sync all uses of getLastID()
                 synchronized (this) {
                     st.executeUpdate("INSERT INTO tbl_people (name) VALUE ('" + cleanSQL(personName) + "')");
                     id = getLastID();
                 }
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
             Statement st = connection.createStatement();
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
                 st = connection.createStatement();
                 //Sync all uses of getLastID()
                 synchronized (this) {
                     st.executeUpdate("INSERT INTO tbl_booktitle (title) VALUE ('" + cleanSQL(booktitleName) + "')");
                     id = getLastID();
                 }
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
             Statement st = connection.createStatement();
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
                 st = connection.createStatement();
                 //Sync all uses of getLastID()
                 synchronized (this) {
                     st.executeUpdate("INSERT INTO tbl_publisher (publisher_name) VALUE ('" + cleanSQL(publisherName) + "')");
                     id = getLastID();
                 }
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
 
     private Integer getLastID() throws SQLException {
         //MUST SYNC all uses of getLastID()
         Statement st = connection.createStatement();
         ResultSet lastIDQ = st.executeQuery("SELECT LAST_INSERT_ID()");
         if (lastIDQ.next()) {
             int id = lastIDQ.getInt(1);
             st.close();
             return id;
         } else {
             return null;
         }
     }
 }
