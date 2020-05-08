 package org.codehaus.xfire.wsdl;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 
 import org.codehaus.xfire.util.Resolver;
 
 /**
  * Create a WSDL instance from a URI.
  *
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  */
 public class ResourceWSDL
 	implements WSDLWriter
 {
     private URL wsdlUrl;
    private String wsdlString;
    private String baseString;
 
     /**
      * @param wsdlUrl
      */
     public ResourceWSDL(String wsdlUrl) throws IOException
     {
        wsdlString = wsdlUrl;
        baseString = "";
     }
 
     public ResourceWSDL(String baseUri, String wsdlUrl) throws IOException
     {
        wsdlString=wsdlUrl;
        baseString=baseUri;
     }

     /**
      * @param wsdlUrl
      */
     public ResourceWSDL(URL wsdlUrl)
     {
         this.wsdlUrl = wsdlUrl;
     }
 
     public void write(OutputStream out) throws IOException
     {
        InputStream is;
         if (wsdlUrl != null)
             is = wsdlUrl.openStream();
        else
            is = new Resolver(baseString, wsdlString).getInputStream();

         copy(is, out, 8096 );
     }
 
     private void copy(final InputStream input,
                      final OutputStream output,
                      final int bufferSize)
         throws IOException
     {
         try
         {
             final byte[] buffer = new byte[bufferSize];
 
            int n;
             while (-1 != (n = input.read(buffer)))
             {
                 output.write(buffer, 0, n);
             }
         }
         finally
         {
             input.close();
         }
     }
 }
