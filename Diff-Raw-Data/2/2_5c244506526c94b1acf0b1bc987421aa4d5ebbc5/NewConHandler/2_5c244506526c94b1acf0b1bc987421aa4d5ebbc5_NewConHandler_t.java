 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package tareaiiiredes;
 
 import java.io.*;
 import java.net.Socket;
 import java.net.URLDecoder;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.query.ResultSetFormatter;
 import java.net.URLEncoder;
 
 /**
  *
  * @author Rodrigo
  */
 public class NewConHandler implements Runnable {
 
     Socket m_connection = null;
     int m_clientID = -1;
     boolean m_bRunThread = true;
 
     NewConHandler(Socket NewCon, int clientID) throws IOException {
         m_connection = NewCon;
         m_clientID = clientID;
         System.out.println("server, client id is: " + m_clientID);
 
     }
 
     private ResultSet getQueryResults(String host, String query) {
         ResultSet rs = null;
         QueryExecution qexec = QueryExecutionFactory.sparqlService(host, query);
         try {
             rs = qexec.execSelect();
         } catch (Exception e) {
             System.out.println("Excepción en ResultSet QueryRemoteSparql.ResultSet en NewConHandler, client id: " + m_clientID + ":");
             System.out.printf(e.toString());
         }
         return rs;
     }
 
     private void sendHTTP200OKResponse(BufferedWriter out, String body) {
         try {
             out.write("HTTP/1.1 200 OK\r\n");
             out.write("\r\n");
             out.write(body);
             out.write("\r\n");
         } catch (Exception e) {
         }
     }
 
     private void sendHTTP500InternalServerErrorResponse(BufferedWriter out) {
         try {
             out.write("HTTP/1.1 500 Internal Server Error\r\n");
             out.write("\r\n");
             out.flush();
         } catch (Exception e) {
         }
     }
 
     private String resultToString(String format, ResultSet rscopy) {
         ByteArrayOutputStream so = new ByteArrayOutputStream();
 
         if (format.equals("CSV")) {
             ResultSetFormatter.outputAsCSV(so, rscopy);
         } else if (format.equals("XML")) {
             ResultSetFormatter.outputAsXML(so, rscopy);
         } else if (format.equals("TSV")) {
             ResultSetFormatter.outputAsTSV(so, rscopy);
         } else if (format.equals("RDF/XML")) {
             ResultSetFormatter.outputAsRDF(so, format, rscopy);
         } else if (format.equals("JSON")) {
             ResultSetFormatter.outputAsJSON(so, rscopy);
         }
 
         String s = so.toString();
         return s;
     }
 
     @Override
     public void run() {
         BufferedReader in = null;
         try {
             in = new BufferedReader(new InputStreamReader(m_connection.getInputStream()));
             //OutputStream out =  new BufferedOutputStream(m_connection.getOutputStream());
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(m_connection.getOutputStream(), "UTF8"));
             //PrintStream pout =  new PrintStream(out);
 
             // read first line of request (ignore the rest)
             String request = in.readLine();
 
            if (request.substring(0, 4).equals("POST")) {
                 System.out.println("Mensaje POST recibido, client id is: " + m_clientID);
                 int i = 0;
                 int blankLineCounter = 0;
 
                 int indexPrimerEspacio = request.indexOf(' ');
                 int indexSegundoEspacio = request.indexOf(' ', indexPrimerEspacio + 1);
                 String host = request.substring(indexPrimerEspacio + 1, indexSegundoEspacio);
                 String format = "XML";  // default
                 
                 while (true) {
                     //Read the http data message
                     String misc = in.readLine();
 
                     if (misc == null) {
                         break;
                     }
 
                     if (misc.length() == 0) {
                         blankLineCounter++;
                         if (blankLineCounter == 2) {
                             break;
                         }
                     }
                     
                     if (misc.substring(0, misc.indexOf(':')).toLowerCase().equals("content-type"))
                     {
                         int indexOfFirstSlash = misc.indexOf('/');
                         format = misc.substring(indexOfFirstSlash + 1);
                     }
                     
                     if (misc.length() != 0 && blankLineCounter == 1) {
                         // El cuerpo de un request HTTP enviado como POST se
                         // encuentra después de una línea en blanco
                         
                         // Se asume que por la codificacion (URLEncoded) está todo en una línea
                         misc = URLDecoder.decode(misc, "UTF-8");
                         ResultSet r = this.getQueryResults(host, misc);
 
                         if (r == null) {
                             this.sendHTTP500InternalServerErrorResponse(out);
                             break;
                         } else {
                             String body = URLEncoder.encode(this.resultToString(format, r), "UTF-8");
                             this.sendHTTP200OKResponse(out, body);
                         }
 
                     }
                     System.out.println("[i = " + i + "]\t" + misc);
                     i++;
                 }
             } else if (request.substring(0, 2).equals("GET")) {
                 System.out.println("Mensaje GET recibido, client id is: " + m_clientID);
             }
         } catch (IOException ex) {
             Logger.getLogger(NewConHandler.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             try {
                 in.close();
             } catch (IOException ex) {
                 Logger.getLogger(NewConHandler.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 }
