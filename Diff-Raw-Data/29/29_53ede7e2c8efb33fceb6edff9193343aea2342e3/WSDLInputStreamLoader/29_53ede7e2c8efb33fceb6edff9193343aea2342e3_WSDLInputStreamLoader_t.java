 package org.codehaus.xfire.gen;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
import java.net.MalformedURLException;
 import java.net.URL;
 
import org.codehaus.xfire.XFireRuntimeException;

 /**
  * @author <a href="mailto:tsztelak@gmail.com">Tomasz Sztelak</a>
  *
  */
 public class WSDLInputStreamLoader
 {
     public InputStream getInputStream(String wsdlUrl)
         throws IOException
     {
         File file = new File(wsdlUrl);
        if(file.exists()) {
             return new FileInputStream(wsdlUrl);
         }
        
        try
        {
            URL url = new URL(wsdlUrl);
            InputStream inStream = url.openStream();
            return inStream;
        }
        catch (MalformedURLException e)
        {
            // it doesn't exist, and its not a good url, so there is probably a typo in the url
            throw new XFireRuntimeException("Could not find wsdl at location " + wsdlUrl);
        }
     }
     
 }
