 package cz.cuni.mff.odcleanstore.wsclient;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringReader;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 
 /**
  * Java client for ODCleanStore Input Webservice.
  * Provides programmatic access in Java to the ODCleanStore SOAP Input Webservice.
  * @author Petr Jerman
  */
 public final class ODCSService {
 
     private URL serviceURL;
 
     /**
      * Create new instance of Input Webservice java client.
      * 
      * @param serviceLocation location of the Input Webservice to send data to as a nURI
      * @throws MalformedURLException serviceLocation URL format error
      */
     public ODCSService(String serviceLocation) throws MalformedURLException {
         try {
             this.serviceURL = new URI(serviceLocation).toURL();
         } catch (IllegalArgumentException e) {
             throw new MalformedURLException();
         } catch (NullPointerException e) {
             throw new MalformedURLException();
         } catch (URISyntaxException e) {
             throw new MalformedURLException();
         }
     }
 
     /**
      * Insert data to ODCleanStore through the Input Webservice.
      * Inserted data will be stored to a single named graph.
      * Since only registered users may insert data, user credentials are required.
      * 
      * @param user username
      * @param password user password
      * @param metadata metadata associated with the inserted data
      * @param payload the actual data to be inserted serialized as RDF/XML or Turtle
      * @throws InsertException Exception returned from server or client
      */
    public void insert(String user, String password, Metadata metadata, String payload) throws InsertException {
         Insert insert = new Insert(serviceURL);
         insert.run(user, password, metadata, new StringReader(payload), new StringReader(payload));
     }
 
     /**
      * Insert data to ODCleanStore through the Input Webservice.
      * Inserted data will be stored to a single named graph.
      * Since only registered users may insert data, user credentials are required.
      * 
      * @param user username
      * @param password user password
      * @param metadata metadata associated with the inserted data
      * @param payloadFile file with the data to be inserted in RDF/XML or Turtle format
      * @param payloadFileEncoding encoding of payloadFile (e.g. UTF-8)
      * @throws InsertException Exception returned from server or client
      * @throws FileNotFoundException payload file not found
      * @throws UnsupportedEncodingException payload file encoding not supported
      */
     public void insert(String user, String password, Metadata metadata, File payloadFile, String payloadFileEncoding)
             throws InsertException, FileNotFoundException, UnsupportedEncodingException {
         Insert insert = new Insert(serviceURL);
         Reader payloadReader = new InputStreamReader(new FileInputStream(payloadFile), payloadFileEncoding);
         Reader payloadReaderForSize = new InputStreamReader(new FileInputStream(payloadFile), payloadFileEncoding);
         insert.run(user, password, metadata, payloadReader, payloadReaderForSize);
     }
 }
