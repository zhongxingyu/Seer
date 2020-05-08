 /**
  * 
  */

 package org.geoserver.hibernate;
 
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import org.geoserver.config.util.XStreamPersister;
 import org.geoserver.config.util.XStreamPersisterFactory;
 
 /**
  * @author Francesco
  *
  */
 public class HibXStreamPersisterFactory extends XStreamPersisterFactory {
 
    @Override
    public XStreamPersister createXMLPersister() {
         return new HibXStreamPersister();
     }
 
    /**
     * Creates an instance configured to persist JSON.
     */
    @Override
    public XStreamPersister createJSONPersister() {
        return new HibXStreamPersister(new JettisonMappedXmlDriver());
    }
 }
