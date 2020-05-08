 package main.java;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Alan Derrick
  * Date: 10/18/12
  * Time: 4:57 PM
  */
 import org.apache.commons.io.IOUtils; // commons-io-2.4.jar file in lib supplies this
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.net.URL;
 
 // TODO analyze code says Probable Bugs (bugs in comments below):
 // 1) Variable 'inputStream' initializer 'null' is redundant
 // 2) Variable 'stringWriter' initializer 'null' is redundant
 public class GetStockQuote {
     public static String getQuote(String sym) throws IOException {
 
         String params = "snl1d1t1";
         String theUrl = "http://download.finance.yahoo.com/d/quotes.csv?s=" + sym + "&f=" + params;
        InputStream inputStream;
        StringWriter stringWriter;
         URL url = new URL(theUrl);
         inputStream = url.openStream();
         stringWriter = new StringWriter();
         IOUtils.copy(inputStream, stringWriter);
         return stringWriter.toString();
     }
     public static void main(String[] args) throws IOException{
         System.out.println("Making system call");
         System.out.println(getQuote("GOOG"));
         System.out.println("Program completed");
     }
 }
