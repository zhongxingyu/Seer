 package org.codehaus.xfire.client.http;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamWriter;
 
 /**
  * Common functionality for the SOAP and Rest HTTP clients.
  * 
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  * @since Oct 26, 2004
  */
 public abstract class AbstractHttpClient
 {
     public final static String SOAP11_ENVELOPE_NS = "http://schemas.xmlsoap.org/soap/envelope";
     
     public final static String SOAP12_ENVELOPE_NS = "http://www.w3.org/2003/05/soap-envelope";
     
     private String username;
     private String password;
     private String charset;
     private String urlString;
 
     public void invoke() 
         throws IOException
     {
         try
         {
             URL url = new URL(urlString);
             URLConnection urlConn = url.openConnection();
             
             urlConn.setDoInput(true);
             urlConn.setDoOutput(true);
             urlConn.setUseCaches(false);
 
             // Specify the content type.
             urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
             
             // Specify content type and encoding
             // If content encoding is not explicitly specified
             // ISO-8859-1 is assumed
             urlConn.setRequestProperty("Content-type", "text/xml; charset=" + charset);
 
             writeHeaders( urlConn );
             
             // Send POST output.
             DataOutputStream printout = new DataOutputStream(urlConn.getOutputStream());
 
             writeRequest(printout);
             
             printout.flush();
             printout.close();
 
             // Get response data.
             DataInputStream input = new DataInputStream(urlConn.getInputStream());
 
             readResponse( input );
 
             input.close();
         }
         catch (MalformedURLException me)
         {
            throw new RuntimeException("Bad URL.", me);
         }
     }
     
     protected void writeHeaders(URLConnection urlConn)
     {
         
     }
 
     /**
      * @return
      */
     protected void writeRequest(OutputStream out)
     {
         XMLOutputFactory factory = XMLOutputFactory.newInstance();
         
         try
         {
              XMLStreamWriter writer = factory.createXMLStreamWriter(out);
              
              writeRequest( writer );
         }
         catch (XMLStreamException e)
         {
             throw new RuntimeException("Couldn't parse stream.", e);
         }
     }
 
     protected abstract void writeRequest(XMLStreamWriter writer) throws XMLStreamException;
 
     /**
      * @param reader
      */
     protected void readResponse(InputStream is)
     {
         // Read in Envelope and then delegate header and Body
         XMLInputFactory factory = XMLInputFactory.newInstance();
         
         try
         {
             readResponse( factory.createXMLStreamReader(is) );
         }
         catch (XMLStreamException e)
         {
             throw new RuntimeException("Couldn't parse stream.", e);
         }
     }
 
     protected abstract void readResponse(XMLStreamReader reader) throws XMLStreamException;
     
     /**
      * @return Returns the url.
      */
     public String getUrl()
     {
         return urlString;
     }
     
     /**
      * @param url The url to set.
      */
     public void setUrl(String url)
     {
         this.urlString = url;
     }
     
     /**
      * @return Returns the charset.
      */
     public String getCharset()
     {
         return charset;
     }
     /**
      * @param charset The charset to set.
      */
     public void setCharset(String charset)
     {
         this.charset = charset;
     }
     /**
      * @return Returns the password.
      */
     public String getPassword()
     {
         return password;
     }
     /**
      * @param password The password to set.
      */
     public void setPassword(String password)
     {
         this.password = password;
     }
     /**
      * @return Returns the username.
      */
     public String getUsername()
     {
         return username;
     }
     /**
      * @param username The username to set.
      */
     public void setUsername(String username)
     {
         this.username = username;
     }
 }
