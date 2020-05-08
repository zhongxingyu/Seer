 /**
  * Bristlecone Test Tools for Databases
  * Copyright (C) 2006-2007 Continuent Inc.
  * Contact: bristlecone@lists.forge.continuent.org
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of version 2 of the GNU General Public License as
  * published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA
  *
  * Initial developer(s): Robert Hodges and Ralph Hannus.
  * Contributor(s):
  */
 
 package com.continuent.bristlecone.benchmark.impl;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.Set;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 
 import com.continuent.bristlecone.benchmark.BenchmarkException;
 
 public class PropertyManager
 {
   private static Logger logger = Logger.getLogger(PropertyManager.class);
 
   /** Create a manager instance. */
   public PropertyManager()
   {
   }
   
   /** 
    * Create a set of property instances from a single property file that 
    * follows the conventions described below:<p>
    * <ol>
    * <li>include property.  This property if present contains the relative
    * path from the current properties file to another properties file that 
    * should be included. 
    * <li>property value cross products.  The pipe symbol within a property 
    * value creates a cross product of property files.  
    *   
   * @param propFile A file containing properties
    */
   public Vector<Properties> propertiesCrossProduct(Properties props, File defaultDir, 
       Vector<String> splitPropertyNames)
   {
     // Prepare an array of key values. 
     String keys[] = this.getKeyArray(props);
     
     // Split properties into a tree.   
     Vector<Properties> propertyInstances = new Vector<Properties>();
     PropertyTreeNode head = new PropertyTreeNode(null); 
     splitProperties(props, keys, 0, head, propertyInstances, splitPropertyNames);
    
     // Look for "include" keys and merge in any properties files that are 
     // so referenced. 
     HashMap<String, Properties> includedPropertiesMap = new HashMap<String, Properties>();
     Iterator propertyIterator = propertyInstances.iterator();
     while (propertyIterator.hasNext())
     {
       // See if we have a value for the "include" property. 
       Properties p = (Properties) propertyIterator.next();
       String include = p.getProperty("include");
       if (include != null)
       {
         // We have a value, which corresponds to a file.  Try to load the 
         // properties from the map of previously read files.
         File baseDir = defaultDir; 
         File includePath = new File(include);
         if (includePath.isAbsolute())
           baseDir = null;
         
         File includeFile = new File(baseDir, include);
         String includeFilePath = includeFile.getAbsolutePath();
         Properties includedProperties = (Properties) includedPropertiesMap.get(includeFilePath);
         if (includedProperties == null)
         {
           // We have not see this file before, so we need to read it. 
           includedProperties = loadProperties(includeFile);
           includedPropertiesMap.put(includeFilePath, includedProperties);
           
           // Any property names in this file should be included as "split" 
           // names if the include file was split.  The test shown below 
           // ensures names are added once and only once.  
           if (splitPropertyNames.remove("include"))
           {
             Enumeration includedPropertyNames = includedProperties.propertyNames();
             while (includedPropertyNames.hasMoreElements())
             {
               String name = (String) includedPropertyNames.nextElement();
               splitPropertyNames.add(name);
             }
           }
         }
         
         // Now that we have the included properties, fill in any values for
         // which keys are missing in the original properties instance.
         String includedKeys[] = getKeyArray(includedProperties);
         for (int i = 0; i < includedKeys.length; i++)
         {
           if (p.getProperty(includedKeys[i]) == null)
           {
             p.setProperty(includedKeys[i], 
                 includedProperties.getProperty(includedKeys[i]));
           }
         }
       }
     }
     
     return propertyInstances;
   }
 
   /** 
    * Wrapper method to generate a properties cross product from a file. 
    * @param propFile A file containing properties
    * @return A list of property instances
    */
   public Vector<Properties> propertiesCrossProduct(File propFile, Vector<String> splitPropertyNames) 
     throws BenchmarkException
   {
     //    Load properties.  
     Properties props        = loadProperties(propFile);
     return propertiesCrossProduct(props, propFile.getParentFile(), 
         splitPropertyNames);
   }
 
   // Return an array containing property keys.
   private String[] getKeyArray(Properties props)
   {
     Set keySet = props.keySet();
     String keys[] = new String[keySet.size()];
     Iterator ksIterator = keySet.iterator();
     for (int i = 0; i < keys.length; i++)
     {
       keys[i] = (String) ksIterator.next();
     }
     return keys;
   }
 
   // Split up properties into a tree.  
   private void splitProperties(Properties props, String[] keys, int index, 
       PropertyTreeNode node, Vector<Properties> propertyInstances, 
       Vector<String> splitPropertyNames)
   {
     if (index < keys.length)
     {
       // We are still within the chain of key values.  Extract the 
       // corresponding value from the properties instance. 
       String key = keys[index];
       String value = props.getProperty(key);
       
       // Split up the value and create a child node for each.  
       String values[] = value.split("\\|");
       if (values.length > 1)
         splitPropertyNames.add(key);
       for (int i = 0; i < values.length; i++)
       {
         PropertyEntry propsEntry = new PropertyEntry(key, values[i]);
         PropertyTreeNode child = new PropertyTreeNode(propsEntry);
         node.addChild(child);
         splitProperties(props, keys, index + 1, child, propertyInstances, 
             splitPropertyNames);
       }
     }
     else
     {
       // We're at a leaf, so we can compute a properties set by looking
       // up the chain of parents. 
       Properties newProps = new Properties();
       PropertyTreeNode currentNode = node;
       while (currentNode != null && currentNode.getEntry() != null)
       {
         String key = currentNode.getEntry().getKey();
         String value = currentNode.getEntry().getValue();
         newProps.setProperty(key, value);
         currentNode = currentNode.getParent();
       }
       propertyInstances.add(newProps);
     }
   }
   
   /**
    * Load a properties file, returning a properties instance. 
    * @param propFile A file containing property definitions
    * @return Instantiated properties file
    * @throws BenchmarkException if the file cannot be loaded
    */
   public Properties loadProperties(File propFile) throws BenchmarkException
   {
     Properties bProperties = new Properties();
     InputStream inStream = null;
     if (propFile.canRead())
     {
       try 
       {
         logger.debug("Reading properties file: " + propFile.toString());
         inStream = new FileInputStream(propFile);
         bProperties.load(inStream);
         return bProperties;
       }
       catch (IOException e)
       {
         throw new BenchmarkException("Unable to read properties file: " 
             + propFile.toString(), e);
       }
       finally 
       {
         if (inStream != null)
         {
           try {
             inStream.close();
           }
           catch (IOException e) {}
         }
       }
     }
     else
     {
       String msg = "Properties file not found or not readable: " 
           + propFile.toString();
       throw new BenchmarkException(msg);
     }
   }
 }
