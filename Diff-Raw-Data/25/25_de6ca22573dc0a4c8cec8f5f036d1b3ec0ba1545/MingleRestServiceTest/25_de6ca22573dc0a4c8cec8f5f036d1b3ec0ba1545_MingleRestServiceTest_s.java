 package mingleplugin;
 
 import static org.mockito.Mockito.doReturn;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.spy;
 import static org.mockito.Mockito.when;
 import hudson.MarkupText;
 import hudson.model.AbstractProject;
 import hudson.model.FreeStyleBuild;
 import java.io.IOException;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Scanner;
 import java.net.URL;
 import java.util.Collections;
 import java.util.regex.Pattern;
 import hudson.Extension;
 import hudson.Util;
 import hudson.model.AbstractDescribableImpl;
 import hudson.model.AbstractProject;
 import hudson.model.Descriptor;
 import hudson.model.Hudson;
 
 import java.net.MalformedURLException;
 import java.net.HttpURLConnection;
 import java.net.ProtocolException;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 import java.util.Scanner;
 import java.util.NoSuchElementException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.Arrays;
 import java.util.List;
 import java.util.ArrayList;
 import java.lang.IllegalArgumentException;
 import java.lang.IllegalStateException;
 
 /**
  * @author Birk Brauer
  */
puclic class MingleRestServiceTest {
   
  @Overrides
   private String doMingleCall(String url_s, String method, String xml) throws MalformedURLException {
     if (xml == null) {
       // used to get some information out of the server
       if (url_s.indexOf("card")) {
         // trying to get a card, mock the result, skip actually HTTP call:
         String result = ""
        File file = new File("MingleXmlCard411.xml");
         
         try {
           Scanner scanner = new Scanner(file);
           while (scanner.hasNextLine()) {
             result += scanner.nextLine();
           }
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         }
     	return result;
    }//..
   }
 }
