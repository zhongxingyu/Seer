 
 import java.io.IOException;
 import java.sql.*;
 import java.util.HashMap;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 
 import org.xml.sax.helpers.DefaultHandler;
 
 public class XMLSAXParser extends DefaultHandler {
 
     private long startTime;
     private long endTime;
     private static final boolean useHashMap = true;
     private HashMap<String, Integer> genres;
     private HashMap<String, Integer> people;
     private HashMap<String, Integer> booktitle;
     private HashMap<String, Integer> publishers;
     private String tempVal;
     //to maintain context
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
     }
 
     public static void main(String[] args) {
         XMLSAXParser spe = new XMLSAXParser();
         spe.runExample();
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
//            sp.parse("final-data.xml", this);//SMALL
            sp.parse("dblp-data.xml", this);//LARGE
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
         tempVal += value.replaceAll("\n", "").trim();
     }
 
     public void endElement(String uri, String localName, String qName) throws SAXException {
 //        System.out.println("===");
         try {
             if (isGenreElement(qName)) {
                 try {
                     //add it to the db;
                     Statement st = connection.createStatement();
                     st.executeUpdate("INSERT INTO tbl_dblp_document (" + tempDoc.getColumns() + ") VALUES (" + tempDoc.getValues() + ")");
 
                     int docID = 0;
                     if (tempDoc.getAuthorsIDs().size() > 0) {
                         st = connection.createStatement();
                         //TODO find a better way to get last added document
                         //largest id should be newest, unless someone manually input an id
                         ResultSet docIDQ = st.executeQuery("SELECT max(id) FROM tbl_dblp_document");
                         if (docIDQ.next()) {
                             docID = docIDQ.getInt(1);
                         }
                     }
                     for (Integer author : tempDoc.getAuthorsIDs()) {
                         st = connection.createStatement();
                         st.executeUpdate("INSERT INTO tbl_author_document_mapping (doc_id, author_id) VALUES ('" + docID + "','" + author + "')");
                         //TODO create one multi statement
                     }
 
                 } catch (SQLException ex) {
                     System.out.println(ex.getMessage());
                 }
 
 
             } else if (qName.equalsIgnoreCase("Author")) {
                 tempDoc.addAuthorsIDs(getPersonID(tempVal.trim()));
             } else if (qName.equalsIgnoreCase("Editor")) {
                 tempDoc.setEditor_id(getPersonID(tempVal.trim()));
             } else if (qName.equalsIgnoreCase("Booktitle")) {
                 tempDoc.setBooktitle_id(getBooktitleID(tempVal.trim()));
             } else if (qName.equalsIgnoreCase("Publisher")) {
                 tempDoc.setPublisher_id(getPublisherID(tempVal.trim()));
             } else if (qName.equalsIgnoreCase("Title")) {
                 tempDoc.setTitle(tempVal.trim());
             } else if (qName.equalsIgnoreCase("Pages")) {
                 tempDoc.setPages(tempVal.trim());
             } else if (qName.equalsIgnoreCase("Year")) {
                 tempDoc.setYear(Integer.parseInt(tempVal.trim()));
             } else if (qName.equalsIgnoreCase("Volume")) {
                 tempDoc.setVolume(Integer.parseInt(tempVal.trim()));
             } else if (qName.equalsIgnoreCase("Number")) {
                 tempDoc.setNumber(Integer.parseInt(tempVal.trim()));
             } else if (qName.equalsIgnoreCase("Url")) {
                 tempDoc.setUrl(tempVal.trim());
             } else if (qName.equalsIgnoreCase("ee")) {
                 tempDoc.setEe(tempVal.trim());
             } else if (qName.equalsIgnoreCase("CDrom")) {
                 tempDoc.setCdrom(tempVal.trim());
             } else if (qName.equalsIgnoreCase("Cite")) {
                 tempDoc.setCite(tempVal.trim());
             } else if (qName.equalsIgnoreCase("Crossref")) {
                 tempDoc.setCrossref(tempVal.trim());
             } else if (qName.equalsIgnoreCase("ISBN")) {
                 tempDoc.setIsbn(tempVal.trim());
             } else if (qName.equalsIgnoreCase("Series")) {
                 tempDoc.setSeries(tempVal.trim());
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
                 if (useHashMap) {
                     genres.put(genreName, id);
                 }
                 return id;
             } else {
                 st = connection.createStatement();
                 st.executeUpdate("INSERT INTO tbl_genres (genre_name) VALUE ('" + cleanSQL(genreName) + "')");
                 return getGenreID(genreName);
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
                 if (useHashMap) {
                     people.put(personName, id);
                 }
                 return id;
             } else {
                 st = connection.createStatement();
                 st.executeUpdate("INSERT INTO tbl_people (name) VALUE ('" + cleanSQL(personName) + "')");
                 return getPersonID(personName);
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
                 if (useHashMap) {
                     booktitle.put(booktitleName, id);
                 }
                 return id;
             } else {
                 st = connection.createStatement();
                 st.executeUpdate("INSERT INTO tbl_booktitle (title) VALUE ('" + cleanSQL(booktitleName) + "')");
                 return getBooktitleID(booktitleName);
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
                 if (useHashMap) {
                     publishers.put(publisherName, id);
                 }
                 return id;
             } else {
                 st = connection.createStatement();
                 st.executeUpdate("INSERT INTO tbl_publisher (publisher_name) VALUE ('" + cleanSQL(publisherName) + "')");
                 return getBooktitleID(publisherName);
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
 }
