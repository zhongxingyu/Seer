 package us.percept.pile.repo;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.vertx.java.core.Handler;
 import org.vertx.java.core.buffer.Buffer;
 import org.vertx.java.core.http.HttpClient;
 import org.vertx.java.core.http.HttpClientResponse;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import us.percept.pile.model.Paper;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import java.io.IOException;
 import java.io.StringReader;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * User: spartango
  * Date: 8/2/13
  * Time: 6:28 PM
  */
 public class ArxivSource extends AsyncPaperSource {
     private static final String ARXIV_HOST = "export.arxiv.org";
     private static final long   TIMEOUT    = 60000; // ms
 
     private static final Logger logger = LoggerFactory.getLogger(ArxivSource.class);
 
     private HttpClient client;
 
     public ArxivSource() {
         client = vertx.createHttpClient().setHost(ARXIV_HOST);
     }
 
     // Synchronous requests for arxiv materials
 
     @Override
     public Paper getPaper(final String identifier) {
         final Buffer body = new Buffer();
         synchronized (body) {
             client.get("/api/query?id_list=" + identifier, new Handler<HttpClientResponse>() {
                 @Override
                 public void handle(HttpClientResponse event) {
                     // If its not a good response, don't carry on
                     if (event.statusCode() != 200) {
                         logger.error("Arxiv returned " + event.statusCode() + " for " + identifier);
                         synchronized (body) {
                             body.notify();
                         }
                         return;
                     }
 
                     // Otherwise download the body
                     event.bodyHandler(new Handler<Buffer>() {
                         @Override
                         public void handle(Buffer event) {
                             logger.info("Arxiv request succeeded with body of ", event.length() + "b");
 
                             // Pass the data on for parsing
                             body.setBuffer(0, event);
                             synchronized (body) {
                                 // Notify the caller that we're done getting data
                                 body.notify();
                             }
                         }
                     });
 
                 }
             }).exceptionHandler(new Handler<Throwable>() {
                 @Override
                 public void handle(Throwable event) {
                     logger.error("Arxiv request failed with ", event);
                     synchronized (body) {
                         body.notify();
                     }
                 }
             }).end();
 
             try {
                 body.wait(TIMEOUT);
             } catch (InterruptedException e) {
                 logger.error("Arxiv request was interrupted by ", e);
             }
         }
 
         // Check that there's a body to parse
         if (body.length() == 0) {
             return null;
         }
 
         try {
             return parsePaper(parseAtomBody(body.toString()));
         } catch (ParserConfigurationException | IOException | SAXException e) {
             logger.error("Failed to parse body ", e);
         }
 
         return null;
     }
 
 
     @Override
     public Collection<Paper> findPapers(final String query) {
         final Buffer body = new Buffer();
         synchronized (body) {
             client.get("/api/query?search_query=" + query, new Handler<HttpClientResponse>() {
                 @Override
                 public void handle(HttpClientResponse event) {
                     // If its not a good response, don't carry on
                     if (event.statusCode() != 200) {
                         logger.error("Arxiv returned " + event.statusCode() + " for query " + query);
                         synchronized (body) {
                             body.notify();
                         }
                         return;
                     }
 
                     // Otherwise download the body
                     event.bodyHandler(new Handler<Buffer>() {
                         @Override
                         public void handle(Buffer event) {
                             logger.info("Arxiv query succeeded with body of ", event.length() + "b");
 
                             // Pass the data on for parsing
                             body.setBuffer(0, event);
                             synchronized (body) {
                                 // Notify the caller that we're done getting data
                                 body.notify();
                             }
                         }
                     });
 
                 }
             }).exceptionHandler(new Handler<Throwable>() {
                 @Override
                 public void handle(Throwable event) {
                     logger.error("Arxiv request failed with ", event);
                     synchronized (body) {
                         body.notify();
                     }
                 }
             }).end();
 
             try {
                 body.wait(TIMEOUT);
             } catch (InterruptedException e) {
                 logger.error("Arxiv request was interrupted by ", e);
             }
         }
 
         // Check that there's a body to parse
         if (body.length() == 0) {
             return null;
         }
 
         try {
             return parseResults(parseAtomBody(body.toString()));
         } catch (ParserConfigurationException | IOException | SAXException e) {
             logger.error("Failed to parse body ", e);
         }
 
         return null;
     }
 
 
     // Asynchronous requests for arxiv materials
 
     @Override
     public void requestPaper(final String identifier) {
         client.get("/api/query?id_list=" + identifier, new Handler<HttpClientResponse>() {
             @Override
             public void handle(HttpClientResponse event) {
                 // If its not a good response, don't carry on
                 if (event.statusCode() != 200) {
                     logger.error("Arxiv returned " + event.statusCode() + " for " + identifier);
                     // Notify that there's been an error
                     notifyPaperFailure(identifier, new Exception("Not OK Status code " + event.statusCode()));
                     return;
                 }
 
                 // Otherwise download the body
                 event.bodyHandler(new Handler<Buffer>() {
                     @Override
                     public void handle(Buffer event) {
                         logger.info("Arxiv request succeeded with body of ", event.length() + "b");
                         try {
                             Paper paper = parsePaper(parseAtomBody(event.toString()));
                             notifyPaperReceived(paper);
                         } catch (ParserConfigurationException | IOException | SAXException e) {
                             logger.error("Failed to parse body ", e);
                             notifyPaperFailure(identifier, e);
                         }
                     }
                 });
 
             }
         }).exceptionHandler(new Handler<Throwable>() {
             @Override
             public void handle(Throwable event) {
                 logger.error("Arxiv request failed with ", event);
                 // Notify that there's been an error
                 notifyPaperFailure(identifier, event);
             }
         }).end();
 
     }
 
     @Override
     public void requestSearch(final String query) {
         client.get("/api/query?search_query=" + query, new Handler<HttpClientResponse>() {
             @Override
             public void handle(HttpClientResponse event) {
                 // If its not a good response, don't carry on
                 if (event.statusCode() != 200) {
                     logger.error("Arxiv returned " + event.statusCode() + " for " + query);
                     // Notify that there's been an error
                     notifySearchFailure(query, new Exception("Not OK Status code " + event.statusCode()));
                     return;
                 }
 
                 // Otherwise download the body
                 event.bodyHandler(new Handler<Buffer>() {
                     @Override
                     public void handle(Buffer event) {
                         logger.info("Arxiv search succeeded with body of ", event.length() + "b");
                         try {
                             Collection<Paper> results = parseResults(parseAtomBody(event.toString()));
                             notifyResultsReceived(results);
                         } catch (ParserConfigurationException | IOException | SAXException e) {
                             logger.error("Failed to parse body ", e);
                             notifySearchFailure(query, e);
                         }
                     }
                 });
 
             }
         }).exceptionHandler(new Handler<Throwable>() {
             @Override
             public void handle(Throwable event) {
                 logger.error("Arxiv request failed with ", event);
                 // Notify that there's been an error
                notifySearchFailure(query, event);
             }
         }).end();
     }
 
     private Document parseAtomBody(String body) throws ParserConfigurationException, IOException, SAXException {
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
         return dBuilder.parse(new InputSource(new StringReader(body)));
     }
 
     private Paper parsePaper(Document document) {
         // Get the Entry tag, it's the only one
         Node entry = document.getElementsByTagName("entry").item(0);
         return parsePaper(entry);
     }
 
     private Paper parsePaper(Node entry) {
         NodeList children = entry.getChildNodes();
         Paper paper = new Paper();
 
         // Read the fields sequentially, as we can't get them by name
         for (int i = 0; i < children.getLength(); i++) {
             Node field = children.item(i);
             if(field.getNodeName().equals("author")) {
                 // First and only child node is a <name>
                 String name = field.getChildNodes().item(0).getTextContent();
                 paper.addAuthor(name);
             } else if(field.getNodeName().equals("published")) {
                 String dateString = field.getTextContent();
                 // Date is in yyyy-MM-ddTHH:mm:ssZ
                 DateFormat df = new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
                 try {
                     Date result =  df.parse(dateString);
                     paper.setDate(result);
                 } catch (ParseException e) {
                     logger.warn("Failed to parse publication date for "+dateString);
                 }
             } else if(field.getNodeName().equals("title")) {
                 paper.setTitle(field.getTextContent());
             } else if(field.getNodeName().equals("summary")) {
                 paper.setSummary(field.getTextContent());
             } else if(field.getNodeName().equals("link")) {
                 // Check if this is the PDF link
                 Node titleNode = field.getAttributes().getNamedItem("title");
                 Node hrefNode = field.getAttributes().getNamedItem("href");
                 if(titleNode != null && titleNode.getNodeValue().equals("pdf")) {
                     paper.setFileLocation(hrefNode.getNodeValue());
                 }
             }
 
             // Otherwise it's an extraneous field.
             // No, not everything in the arxiv metadata is actually useful. And who cares about DOIs?!
         }
 
 
         // Build a Paper
         return paper;
     }
 
     private Collection<Paper> parseResults(Document document) {
         NodeList entries = document.getElementsByTagName("entry");
         List<Paper> papers = new ArrayList<>(entries.getLength());
 
         for (int i = 0; i < entries.getLength(); i++) {
             papers.add(parsePaper(entries.item(i)));
         }
 
         return papers;
     }
 
 
 }
