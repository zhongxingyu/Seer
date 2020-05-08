 package org.codehaus.xfire.gen;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 
 /**
  * @author <a href="mailto:tsztelak@gmail.com">Tomasz Sztelak</a>
  *
  */
 public class WSDLInputStreamLoader
 {
     public InputStream getInputStream(String wsdlUrl)
         throws IOException
     {
        URL url;
        if(wsdlUrl.toLowerCase().startsWith("file://")){
            wsdlUrl = wsdlUrl.substring(7);
        }
         File file = new File(wsdlUrl);
        if(file.exists()){
             return new FileInputStream(wsdlUrl);
         }
        url = new URL(wsdlUrl);
        InputStream inStream = url.openStream();
        return inStream;

     }
     
 }
