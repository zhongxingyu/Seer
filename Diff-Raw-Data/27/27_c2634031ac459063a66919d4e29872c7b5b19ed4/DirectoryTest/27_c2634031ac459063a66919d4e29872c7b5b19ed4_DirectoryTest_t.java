 /*
  * Copyright (c) 2010-2012 Matthias Klass, Johannes Leimer,
  *               Rico Lieback, Sebastian Gabriel, Lothar Gesslein,
  *               Alexander Rampp, Kai Weidner
  *
  * This file is part of the Physalix Enrollment System
  *
  * Foobar is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Foobar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package hsa.awp.common.naming;
 
 import hsa.awp.common.exception.ConfigurationException;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import javax.annotation.Resource;
 import javax.naming.NamingEnumeration;
 import javax.naming.directory.Attributes;
 import javax.naming.directory.BasicAttribute;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Map.Entry;
 import java.util.Properties;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 /**
  * Tests the Directory class with a dummy directory adapter.
  *
  * @author alex
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("classpath:config/spring/TestDirectory.xml")
 public class DirectoryTest {
   /**
    * File where the field name mapping is defined.
    */
   private static final String MAPPING_FILE = "config/fieldMapping.test.properties";
 
   /**
    * The class under test.
    */
   @Resource(name = "directory")
   private IAbstractDirectory directory;
 
   /**
    * The dummy adapter under the directory.
    */
   @Resource(name = "adapter")
   private DummyDirectoryAdapter adapter;
 
   /**
    * Defines the mapping from ldap fields to our field names.
    */
   private Properties mapping;
 
   /**
    * Sets up the mapping properties.
    *
    * @throws IOException if mapping file could not be loaded
    */
   public DirectoryTest() throws IOException {
 
     setupFieldMapping(MAPPING_FILE);
   }
 
   /**
    * This procedure reads <code>fieldMapping</code> from the properties file
    * and reverses it. This is needed because the properties file is in the
    * format [fieldname]=[ldapname] (more convenient to configure) but we need
    * them in format [ldapname]=[fieldname].
    *
    * @param propertyFile - The properties file to read from.
    */
   private void setupFieldMapping(String propertyFile) {
     // loading the properties file in a temporary object
     Properties tmpProps = new Properties();
     try {
       InputStream in = ClassLoader
           .getSystemResourceAsStream(propertyFile);
       if (in == null) {
         throw new ConfigurationException("PorpertyFile " + propertyFile
             + " is not in CLASSPATH");
       }
       tmpProps.load(in);
     } catch (IOException e) {
       throw new ConfigurationException(
           "Could not read configuration file: " + propertyFile, e);
     }
 
     // now we reverse the properties
     mapping = new Properties();
     for (Entry<Object, Object> currentEntry : tmpProps.entrySet()) {
       mapping.put(currentEntry.getValue(), currentEntry.getKey());
     }
   }
 
   /**
    * Tests if getFieldName() does the mapping right.
    *
    * @throws Exception if the field for the mapping could not be read.
    */
   @Test
   public void testGetFieldName() throws Exception {
 
     for (Object ldapName : mapping.keySet()) {
       String fieldName = mapping.getProperty((String) ldapName);
       String abstractName = (String) directory.getClass().getField(
           fieldName).get("");
       assertEquals(abstractName, directory
           .getAbstractFieldName((String) ldapName));
     }
   }
 
  @Test
  public void testGetLowLevelFieldName() {
    String fieldName = directory.getLowLevelFieldName(Directory.STUDYCOURSE);
    assertEquals(mapping.getProperty(Directory.STUDYCOURSE).toLowerCase(), fieldName);
  }

   /**
    * Properties that must be set.
    *
    * @throws Exception If something went wrong with Attributes
    */
 
   @Test
   public void testGetUserProperties() throws Exception {
 
     for (Attributes curAttrs : adapter.getUsers().values()) {
       Properties curProps = directory.getUserProperties((String) curAttrs.get("uid").get());
       // here we have to fix the semester field - ldap has a field like
       // 0079$8 but directory maps to 8
       // TODO refactor ldap abstraction to externalize field processing
       String curSemesterString = (String) curAttrs.get("term").get();
       curAttrs.put(new BasicAttribute("term", curSemesterString.split("\\$")[1]));
       assertTrue(isEqual(curAttrs, curProps));
     }
   }
 
   /**
    * Checks if the content of the Attributes and Properties is equal. In
    * properties it assumes the mapping from ldap names to our names.
    *
    * @param attributes Ldap Attribtues
    * @param properties Properties returned from Directory
    * @return True if they are equal, otherwise false
    * @throws Exception If someone went wron.
    */
   private boolean isEqual(Attributes attributes, Properties properties)
       throws Exception {
 
     if (attributes.size() != properties.size()) {
       return false;
     }
 
     NamingEnumeration<String> attribIds = attributes.getIDs();
     while (attribIds.hasMoreElements()) {
       String curAttribId = attribIds.nextElement();
       String currentAttribValue = (String) attributes.get(curAttribId)
           .get();
       String currentPropertyId = (String) directory.getClass().getField(
           mapping.getProperty(curAttribId)).get("");
       String currentPropertyValue = properties
           .getProperty(currentPropertyId);
       if (!currentAttribValue.equals(currentPropertyValue)) {
         return false;
       }
     }
 
     return true;
   }
 }
