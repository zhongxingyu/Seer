 /*
  * @author <a href="mailto:oliver@wehrens.de">Oliver Wehrens</a>
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
 *
 * The Class provides easy access to marshal/unmarshal objects to XML files
  */
 package org.gridlab.gridsphere.core.persistence.castor;
 
 import org.apache.log4j.Category;
 import org.exolab.castor.mapping.Mapping;
 import org.exolab.castor.mapping.MappingException;
 import org.exolab.castor.xml.MarshalException;
 import org.exolab.castor.xml.Marshaller;
 import org.exolab.castor.xml.Unmarshaller;
 import org.exolab.castor.xml.ValidationException;
 import org.gridlab.gridsphere.core.persistence.*;
 
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.List;
 import java.util.ArrayList;
 

 public class PersistenceManagerXml implements PersistenceManagerInterface  {
 
     static Category cat = Category.getInstance(PersistenceManagerXml.class.getName());
 
     private static PersistenceManagerXml instance;
 
     private String URL = null;
     private String MappingFile = null;
 
     /**
      * PersistenceManagerXml default constructor
      */
     private PersistenceManagerXml() {}
 
     private static synchronized void doSync() {}
 
     public static PersistenceManagerXml getInstance() {
         if (instance == null) {
             doSync();
             instance = new PersistenceManagerXml();
         }
         return instance;
     }
 
     /**
      * checks if all setting for marshalling xml data are done
      *
      * @throws ConfigurationsExcpetion if one variable is not set
      */
     private void checkSetting() throws ConfigurationException {
 
         // Mapping can be null
         if (getConnectionURL().equals(null)) throw new ConfigurationException("Configuration Error");
 
     }
 
     /**
      * return the connectionURL, which is here the XMLFilename
      *
      * @return filename
      */
     public String getConnectionURL() {
         return URL;
     }
 
     /**
      * sets the xml outputfile
      *
      * @param url where to write the file
      *
      */
     public void setConnectionURL(String url) {
         URL = url;
     }
 
     /**
      * sets the mapping file
      *
      * @param mappingfile file containing the mapping
      */
     public void setMappingFile(String mappingFile) {
         MappingFile = mappingFile;
     }
 
 
     /**
      * gets the filename of the mappingfile
      *
      * @return name of the mappingfile
      */
     public String getMappingFile() {
         return MappingFile;
     }
 
     /**
      * updates an object in the xml file (same as create)
      *
      * @throws UpdateExcpetion if the update failed
      * @param object update 'object'
      */
     public void update(Object object) throws UpdateException {
 
         try {
             create(object);
         } catch (CreateException e) {
             throw new UpdateException("Update Error");
         } catch (ConfigurationException e) {
             throw new UpdateException("ConfigurationExcpetion");
         }
     }
 
     /**
      * marshals the given object to an xml file
      *
      * @throws CreateException if the creation failed
      * @throws ConfiguratonExcpetion if the configuration was wrong
      * @param object object to be marshalled
      */
     public void create(Object object) throws CreateException, ConfigurationException {
 
         checkSetting();
 
         try {
             FileWriter filewriter = new FileWriter(getConnectionURL());
 
             Marshaller marshal = new Marshaller(filewriter);
 
             Mapping map = new Mapping();
             map.loadMapping(getMappingFile());
             marshal.setMapping(map);
 
             marshal.marshal(object);
             filewriter.close();
             Class cl = object.getClass();
             cat.debug("Wrote object of type " + cl.getName() + " to XMLFile " + getConnectionURL());
 
         } catch (IOException e) {
             cat.error("Exception " + e);
             throw new CreateException("IO Error");
         } catch (ValidationException e) {
             cat.error("Exception " + e);
             throw new CreateException("Validation Error");
         } catch (MarshalException e) {
             cat.error("Exception " + e);
             throw new CreateException("Marshal Error");
         } catch (MappingException e) {
             cat.error("Exception " + e);
             throw new CreateException("Mapping Error");
         }
 
     }
 
     /**
      * restores an object from an xml file
      *
      * @param qobject instance of object
      * @throws RestoreExcpetion if restore was not succsessful
      * @throws ConfigurationException if there was a configurationerror
      * @return object which was unmarshalled
      */
     public Object restoreObject() throws RestoreException, ConfigurationException {
 
         checkSetting();
 
         Object object = null;
 
         try {
             FileReader filereader = new FileReader(getConnectionURL());
             Mapping mapping = new Mapping();
             mapping.loadMapping(getMappingFile());
 
             Unmarshaller unmarshal = new Unmarshaller(mapping);
             object = unmarshal.unmarshal(filereader);
         } catch (IOException e) {
             cat.debug("Exception " + e);
             throw new RestoreException("IO Error");
         } catch (MappingException e) {
             cat.debug("Exception " + e);
             throw new RestoreException("Mapping Error");
         } catch (MarshalException e) {
             cat.debug("Exception " + e);
             throw new RestoreException("Marshal Error");
         } catch (ValidationException e) {
             cat.debug("Exception " + e);
             throw new RestoreException("Validation Error");
         }
 
         return object;
     }
 
     /**
      * restore objects from xml file, just here to justify interface could be empty
      * since it makes no sense in xml files
      *
      * @throws RestoreExcpetion if restore failed
      * @throws ConfigurationExcpetion if config was wrong
      * @returns list of objects (here just with one object)
      */
     public List restoreList() throws RestoreException, ConfigurationException {
 
         List list = new ArrayList();
         list.add(restoreObject());
         return list;
 
     }
 
     /**
      * deletes object in xml file storage (<b>not implemented yet</b>)
      * deletes the file
      *
      * @param object object to be deleted
      */
     public void delete(Object object) {
 
 
     }
 }
 
 
