 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath" 
  * exception as provided by Sun in the License file that accompanied 
  * this code.
  */
 package org.jdesktop.wonderland.common.cell.state;
 
 import java.io.Reader;
 import java.io.Serializable;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElementRef;
 import javax.xml.bind.annotation.XmlElementRefs;
 import javax.xml.bind.annotation.XmlElements;
 import javax.xml.bind.annotation.XmlTransient;
 import org.jdesktop.wonderland.common.AssetURIAdapter;
 import org.jdesktop.wonderland.common.utils.ScannedClassLoader;
 
 /**
  * The CellServerState class is the base class for all classes that represent
  * the setup information for specific cell types. This class must be overridden
  * by the cell-specific setup class.
  * <p>
  * In additional to the setup information defined here, the cell-specific setup
  * class may also add additional parameters and require one or more component
  * setup classes. It must define the getServerClassName() method to return the
  * fully-qualified name of the server-side cell class.
  * <p>
  * The subclass of this class must be annotated with @XmlRootElement that
  * defines the root element for all documents of that cell type. It must be
  * unique for all cell types.
  * 
  * @author Jordan Slott <jslott@dev.java.net>
  */
 public abstract class CellServerState implements Serializable {
 
     /* The name of the cell */
     @XmlElement(name="name")
     private String name = null;
 
     /* Arbitrary collection of key-value meta data */
     @XmlElements({ 
         @XmlElement(name="metadata")
     })
     private MetaDataHashMap metadata = new MetaDataHashMap();
 
     /* An array of server component states for the XML */
     @XmlElementRefs({
         @XmlElementRef()
     })
     private CellComponentServerState components[] = new CellComponentServerState[0];
 
     /* A hashmap of server components, used by API access to component states */
     @XmlTransient
     private Map<Class, CellComponentServerState> internalComponentMap = new HashMap();
 
     /*
      * The internal representation of the metadata as a hashed map. The HashMap
      * class is not supported by JAXB so we must convert it to a list for
      * serialization
      */
     @XmlTransient
     private Map<String, String> internalMetaData = new HashMap();
 
     /**
      * A wrapper class for hashmaps, because JAXB does not correctly support
      * the HashMap class.
      */
     private static class MetaDataHashMap implements Serializable {
         /* A list of entries */
         @XmlElements( {
             @XmlElement(name="entry")
         })
         public List<HashMapEntry> entries = new ArrayList<HashMapEntry>();
 
         /** Default constructor */
         public MetaDataHashMap() {
         }
     }
     
     /**
      * A wrapper class for hashmap entries, because JAXB does not correctly
      * support the HashMap class
      */
     private static class HashMapEntry implements Serializable {
         /* The key and values */
         @XmlAttribute public String key;
         @XmlAttribute public String value;
 
         /** Default constructor */
         public HashMapEntry() {
         }
     }
     
     /** Default constructor */
     public CellServerState() {
     }
     
     /**
      * Returns the fully-qualified class name for the server-side cell class
      * to instantiate.
      *
      * @return The FQCN of the server-side cell class
      */
     public abstract String getServerClassName();
 
     /**
      * Adds a component server state. If a server state of the same Class has
      * already been added, this replaces the existing server state.
      *
      * @param serverState The component server state to add
      */
     public void addComponentServerState(CellComponentServerState serverState) {
         internalComponentMap.put(serverState.getClass(), serverState);
     }
 
     /**
      * Returns a CellComponentServerState given its Class, or null if a server
      * state object is not present on the cell server state class of the given
      * Class type.
      *
      * @param clazz The Class of the component server-state object
      * @return The component server state object if it exist, null otherwise
      */
     public CellComponentServerState getComponentServerState(Class clazz) {
         return internalComponentMap.get(clazz);
     }
 
     /**
      * Returns a map of all of the CellComponentServerState objects.
      *
      * @return A Map of Class object to their component server-state objects
      */
     public Map<Class, CellComponentServerState> getComponentServerStates() {
         return new HashMap(internalComponentMap);
     }
     
     /**
      * Removes a CellComponentServerState given its Class. If a server state
      * for the given Class does not exist, this method does nothing.
      *
      * @param clazz The Class of the component server-state object
      */
     public void removeComponentServerState(Class clazz) {
         internalComponentMap.remove(clazz);
     }
     
     /**
      * Returns the cell metadata.
      * 
      * @return The cell metadata
      */
     @XmlTransient public Map<String, String> getMetaData() {
         return this.internalMetaData;
     }
     
     /**
      * Sets the cell's metadata. If null, then this property will not be
      * written out to the file.
      * 
      * @param metadata The new cell metadata
      */
     public void setMetaData(Map<String, String> metadata) {
         this.internalMetaData = metadata;
     }
 
     @XmlTransient public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
    
     /**
      * Takes the input reader of the XML data and instantiates an instance of
      * the CellServerState class
      * <p>
      * @param r The input reader of the XML data
      * @throw ClassCastException If the input data does not map to CellServerState
      * @throw JAXBException Upon error reading the XML data
      */
     public static CellServerState decode(Reader r) throws JAXBException {
         return decode(r, null, null);
     }
 
     /**
      * Takes the input reader of the XML file and instantiates an instance of
      * the CellServerState class. Also takes the class loader and server name
      * associated with the context.
      * <p>
      * @param r The input data of the version XML data
      * @param cl The class loader
      * @param server The name of the server
      * @throw ClassCastException If the input data does not map to CellServerState
      * @throw JAXBException Upon error reading the XML data
      */
     public static CellServerState decode(Reader r, ScannedClassLoader cl, String server) throws JAXBException {
         /*
          * De-serialize from XML. We set up an adapter to handle XML elements
          * of type AssetURI. This will properly decode them and also fill in
          * the name of the server context.
          */
         Unmarshaller u = CellServerStateFactory.getUnmarshaller(cl);
         if (server != null) {
             AssetURIAdapter adapter = new AssetURIAdapter(server);
             u.setAdapter(adapter);
         }
         CellServerState setup = (CellServerState)u.unmarshal(r);
         
         /* Convert metadata to internal representation */
         if (setup.metadata != null) {
             ListIterator<HashMapEntry> iterator = setup.metadata.entries.listIterator();
             setup.internalMetaData = new HashMap<String, String>();
             while (iterator.hasNext() == true) {
                 HashMapEntry entry = iterator.next();
                 setup.internalMetaData.put(entry.key, entry.value);
             }
         }
         else {
             setup.internalMetaData = null;
         }
 
         /* Convert components to internal representation */
         if (setup.components != null) {
             setup.internalComponentMap = new HashMap();
             for (CellComponentServerState state : setup.components) {
                 setup.internalComponentMap.put(state.getClass(), state);
             }
         }
         else {
             setup.internalComponentMap = null;
         }
         return setup;
     }
     
     /**
      * Writes the CellServerState class to an output writer.
      * <p>
      * @param w The output write to write to
      * @throw JAXBException Upon error writing the XML data
      */
     public void encode(Writer w) throws JAXBException {
         encode(w, null);
     }
     
     /**
      * Writes the CellServerState class to an output writer. Also takes the
      * class loader context.
      * <p>
      * @param w The output write to write to
      * @param cl The class loader
      * @throw JAXBException Upon error writing the XML data
      */   
     public void encode(Writer w, ScannedClassLoader cl) throws JAXBException {
         /* Convert internal metadata map to one suitable for serialization */
         if (this.internalMetaData != null) {
             this.metadata = new MetaDataHashMap();
             for (Map.Entry<String, String> e : this.internalMetaData.entrySet()) {
                 HashMapEntry entry = new HashMapEntry();
                 entry.key = e.getKey();
                 entry.value = e.getValue();
                 this.metadata.entries.add(entry);
             }
         }
         else {
             this.metadata = null;
         }
 
         /* Convert internal component map to one suitable for serialization */
         if (this.internalComponentMap != null) {
             this.components = new CellComponentServerState[this.internalComponentMap.size()];
             int i = 0;
             for (Map.Entry<Class, CellComponentServerState> e : this.internalComponentMap.entrySet()) {
                 this.components[i] = e.getValue();
                 i++;
             }
         }
         else {
             this.components = null;
         }
 
         /* Write out as XML */
         Marshaller m = CellServerStateFactory.getMarshaller(cl);
         m.marshal(this, w);
     }
     
     /**
      * Returns a string representation of this class
      *
      * @return The setup information as a string
      */
     @Override
     public String toString() {
        StringBuilder sb = new StringBuilder("[CellServerState] ");
         for (Map.Entry<Class, CellComponentServerState> e : internalComponentMap.entrySet()) {
             sb.append(e.getValue().toString());
         }
         return sb.toString();
     }
 }
