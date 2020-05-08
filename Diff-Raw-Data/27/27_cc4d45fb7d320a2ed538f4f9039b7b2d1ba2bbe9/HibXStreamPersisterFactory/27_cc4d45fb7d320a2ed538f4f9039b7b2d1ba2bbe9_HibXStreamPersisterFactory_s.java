 /**
  * 
  */
 package org.geoserver.hibernate;
 
 import org.geoserver.config.util.XStreamPersisterFactory;
 
 /**
  * @author Francesco
  *
  */
 public class HibXStreamPersisterFactory extends XStreamPersisterFactory {
 
	public HibXStreamPersister createXMLPersister() {
         return new HibXStreamPersister();
     }
 
 }
