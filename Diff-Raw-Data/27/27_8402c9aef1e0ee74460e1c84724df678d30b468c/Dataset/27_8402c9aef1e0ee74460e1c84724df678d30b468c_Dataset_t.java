 package org.renci.databridge.util;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileInputStream;
 import java.io.BufferedOutputStream;
import java.util.*;
 
 /**
  * This class holds the data for a dataset in
  * the DataBridge system.
  * 
  * @author Howard Lander -RENCI (www.renci.org)
  * 
  */
 public class Dataset {
 
      /** The URI of the data set */
      private String URI;
 
      /** The hdl "handle" unique identifier for the dataset */
      private String handle;
 
      /** The similarity matrix stored as a Compressed row format.Note that
          we only need the top half of the matrix though we are currently storing it all.
          Also note that the x and y dimensions of original matrix have to be equal */
      private String name;
 
      /** The databridge-specific identifier, unique within the databridge space  */
      private String dbID;
 
      /** A map to store properties such as the name of the dataset.
          Automatically includes URI, handle, and name - additional properties
 	 added via addProperty(String, String) */
      private HashMap<String, String> properties;
 
 
      /**
       * Dataset constructor with no arguments.
       */
      public Dataset() {
      }
 
      /**
       * Dataset constructor with URI, handle and name
       *
       *  @param  URI The URI for the dataset.
       *  @param  handle The unique handle for the dataset.
       *                  (duplicated as dbID)
       *  @param  name The name for the dataset.
       */
     public Dataset(String URI, String handle, String name) {
 	 this(URI, handle, name, handle);
      }
 
      /**
       * Dataset constructor with URI, handle and name
       *
       *  @param  URI The URI for the dataset.
       *  @param  handle The unique handle for the dataset.
       *  @param  name The name for the dataset.
       *  @param  dbID The unique databridge ID for the dataset.
       */
      public Dataset(String URI, String handle, String name, String dbID) {
          this.URI = URI;
          this.handle = handle;
          this.name = name;
 	 this.dbID = dbID;
 	 properties = new HashMap<String, String>();
 	 properties.put("URI", URI);
 	 properties.put("handle", handle);
 	 properties.put("name", name);
      }
 
 
      /**
       * Add Property: additional properties not provided for in the constructor
       *   are added via this method
       *
       * @return URI as String.
       */
      public void addProperty(String key, String val)
      {
          properties.put(key, val);
      }
 
      
      /**
       * Get URI.
       *
       * @return URI as String.
       */
      public String getURI()
      {
          return URI;
      }
      
      /**
       * Set URI.
       *
       * @param URI the value to set.
       */
      public void setURI(String URI)
      {
          this.URI = URI;
      }
      
      /**
       * Get handle.
       *
       * @return handle as String.
       */
      public String getHandle()
      {
          return handle;
      }
      
      /**
       * Set handle.
       *
       * @param handle the value to set.
       */
      public void setHandle(String handle)
      {
          this.handle = handle;
      }
      
      /**
       * Get name.
       *
       * @return name as String.
       */
      public String getName()
      {
          return name;
      }
      
      /**
       * Set name.
       *
       * @param name the value to set.
       */
      public void setName(String name)
      {
          this.name = name;
      }
 
      /**
       * Get dbID.
       *
       * @return dbID as String.
       */
      public String getDbID()
      {
          return dbID;
      }
 
      /**
       * Set dbID.
       *
       * @param dbID the value to set.
       */
      public void setDbID(String dbID)
      {
          this.dbID = dbID;
      }
 
      /**
       * Get properties.
       *
       * @return name as HashMap<String, String>.
       */
     public HashMap<String, String> getProperties()
      {
          return properties;
      }
      
      /**
       * Return a string representing the object
       *
       * @return object as String.
       */
      public String toString()
      {
          return ("URL: " + this.URI + " handle: " + this.handle + " name: " + this.name);
      }
      
 }
