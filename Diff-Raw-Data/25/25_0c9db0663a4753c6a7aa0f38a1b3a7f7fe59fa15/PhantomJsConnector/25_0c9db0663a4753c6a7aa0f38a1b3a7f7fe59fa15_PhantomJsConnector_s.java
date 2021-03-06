 /**
  * This file was automatically generated by the Mule Development Kit
  */
 package com.espn.mule.connector.phantomjs;
 
 import org.mule.api.annotations.Connector;
 import org.mule.api.annotations.Connect;
 import org.mule.api.annotations.ValidateConnection;
 import org.mule.api.annotations.ConnectionIdentifier;
 import org.mule.api.annotations.Disconnect;
 import org.mule.api.annotations.param.ConnectionKey;
 import org.mule.api.ConnectionException;
import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.Processor;
 
 import com.espn.phantomjs.PhantomJs;
 import com.espn.phantomjs.client.*;
 import org.mule.api.annotations.param.Default;
 import org.mule.api.annotations.param.Optional;
 
 /**
 * Cloud Connector
  *
 * @author MuleSoft, Inc.
  */
 @Connector(name="phantomjs", schemaVersion="1.0-SNAPSHOT")
 public class PhantomJsConnector
 {
     /**
     * locaion of the phantomjs executable file
      */
     @Configurable
     @Optional
     @Default("/usr/local/bin/phantomjs")
     String phantomjsBinary = "/usr/local/bin/phantomjs";
 
     public String getPhantomjsBinary() {
         return phantomjsBinary;
     }
 
     public void setPhantomjsBinary(String phantomjsBinary) {
         this.phantomjsBinary = phantomjsBinary;
     }
     
     /**
      * default phantomjs timeout
      */
     @Configurable
     @Optional
     @Default("60")
     int timeout = 60;
 
     public int getTimeout() {
         return timeout;
     }
 
     public void setTimeout(int timeout) {
         this.timeout = timeout;
     }
 
     private PhantomJs phantomJsClient = null;
 
     private synchronized PhantomJs getPhantomJs() {
         if (this.phantomJsClient == null) {
             this.phantomJsClient = new PhantomJs(new PhantomJsWebDriverClient(getPhantomjsBinary(), getTimeout()));
         }
         return this.phantomJsClient;
     }
     
     /**
     * screenshot
      *
      * {@sample.xml ../../../doc/PhantomJs-connector.xml.sample phantomjs:screenshot}
     * @param url String
     * @return image bytes
      * @throws Exception for url
      */
     @Processor
     public byte[] screenshot( String url ) throws Exception {
         return getPhantomJs().screenshot(url);
     }
 }
