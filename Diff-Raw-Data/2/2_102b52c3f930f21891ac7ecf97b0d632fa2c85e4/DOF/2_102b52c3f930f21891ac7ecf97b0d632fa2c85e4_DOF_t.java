 package org.doframework;
 
 // Released under the Eclipse Public License-v1.0
 
 
 import java.io.*;
 import java.util.*;
 import java.util.regex.*;
 
 
 /**
  This class is the engine of the Dependent Object Framework.<p>
  <p/>
  The general problem this framework solves is how does a JUnit test ensure that a persistent object needed for a test
  exists in the database (or any persistent store). Alternative solutions to this problem include running SQL scripts to
  populate the database and restoring database backups. Both methods are inconvenient.
  <p/>
  The Dependent Object Framework allows the test writer to specify what objects are required for a test. The test writer
  provides:
  <p/>
  1. An appropriate handler class for each object type. The handler class implements the interface
  DepedendentObjectHandler and needs to be specfied in a file called handler_mappings.properties that exists somewhere in
  the classpath. This class knows how to create, get, and delete objects of a given type and given a format for the
  description files. Note, the description files can be of any form because the test writer is responsible for writing the
  code that processes the description files.<p> 2. A data file containing information to create the object, including the
  specification of any object dependencies. For example a "product" record might specify what manufacturer record is
  required. In order to be located by the framework, all DOF data files must exist in the classpath.
  <p/>
  There are only 2 main methods to use: <b>require</b>(fileToLoad) and <b>delete</b>(fileToLoad).<p>
  <p/>
  The fileToLoad encodes the object type, the object primary key, and the file type.
  <p/>
  Example:
  <pre>
  public void testNewInvoiceSubtotal()
  {
      // Get objects needed for test
      Customer johnSmith = (Customer) DOF.require("customer.25.xml");
      Product coffee = (Product) DOF.require("product.13.xml");
      Product tea = (Product) DOF.require("product.14.xml");
      .... rest of the test
  }
  </pre>
  The product file may specify the manufacturer required, note in the XML comment using the form
  <b>@require("fileToLoad")</b> that indicates the dependency on manufacturer 35.
  <p/>
  <pre>
  product.13.xml
  &lt;!-- @require("manufacturer.35.xml") --&gt;
  &lt;product&gt;
      &lt;id&gt;13&lt;/id&gt;
      &lt;name&gt;coffee&lt;/name&gt;
      &lt;price&gt;8.99&lt;/price&gt;
      &lt;manufacturer_id&gt;35&lt;/manufacturer_id&gt;
  &lt;/product&gt;
  </pre>
  The user of the DOF is responsible for having a file named "handler_mappings.properties" located in the classpath. The
  format of the properties file is:
  <p/>
  <code>objectType.fileSuffix=DependentObjectHandlerImplementationClassName</code>
  <p/>
  This is an example of a line in the mappings file:
  <p/>
  <code>customer.xml=dof_xml_handler.CustomerXmlFactory</code>
  <p/>
  It states that a customer.PK.xml file maps to the handler class dof_xml_handler.CustomerXmlFactory Note, the
  CustomerXmlFactory class must implement interface <b>DependentObjectHandler</b>. Even though fileToLoad uses the period
  as the delimiter, the primary key may contain periods because the first and last periods are used to find the object
  type and the file suffix. This also means that object types may NOT contain a period.
 
  @author Justin Gordon
  @date January, 2008
  @see DependentObjectHandler
 
 
 
  */
 public class DOF
 {
 
     //////////////////////////////////////////////////////////////////////////////////////////////
     // public API ////////////////////////////////////////////////////////////////////////////////
     //////////////////////////////////////////////////////////////////////////////////////////////
 
     /**
      The principal entry point for this class. If the object was previously requested, it is returned from map of the
      file name to the object. Otherwise, the database is then checked. If the object exists in the database (or whatever
      persistent store used by the handler classes), then it is simply returned. If the object does not exist, then it is
      created. During the creation process, the object definition file may specify other objects that are "required" and
      those will get loaded recursively depth first. Thus, if object A depends on object B that depends on object C, then
      a request for object A invokes the request for object B which invokes the request for object C.
      <p/>
      Note that the fileToLoad uses a specific format to define the file to load which encodes the:
      <pre>
      1. Object Type
      2. Object PK
      3. Object File Type (like xml)
      </pre>
      <p/>
      The file processor looks for dependencies in the definition files using this pattern:<p>
      <b>@require("{object_type.object_pk.extension}")</b>
      <p/>
      where {object_type.object_pk.extension} might be something like "manufacturer.35.xml".
      <p/>
      Thus, in an XML file, one would use the commented form:<p>
      <code> &lt;!-- @require("manufacturer.35.xml") --&gt; </code><p>
      Even though fileToLoad uses the period as the delimiter, the primary key may contain periods because the first and
      last periods are used to find the object type and the file suffix. This also means that object types may NOT
      contain a period.
 
      @param fileToLoad File name in form: {objectType}.{objectPk}.{fileType}
 
      @return The Object requested
      */
     public static Object require(String fileToLoad)
     {
         return requireWorker(fileToLoad);
     }
 
 
     /**
      Use this method to delete an object. This method will opportunistically try to delete all of the parent
      dependencies. Note, the deletion is greedy. Even if the object requested to be deleted does not exist, then it's
      parent objects will still try to be deleted. This method is useful when setting up tests, as the object definition
      files often need frequent tweaking. Note, it is critical that objects created with the require method be deleted
      using this method because the require method caches the created objects by the file name.
      <p/>
      Note, this method takes the same parameter as the require method to facilitate copying and pasting the require line.
 
      @param fileToLoad File name in form: {objectType}.{objectPk}.{fileType}
 
      @return true if requested object deleted successfully, false if object could not be deleted, maybe because another
      object depends upon it. Note, the return value from deleting dependency objectsfor the requested object is
      discarded. For example, if you request an invoice to be deleted, the return value only reflects if that
      requested invoice was deleted, and not if the parent customer record of that invoice is deleted.
      */
     public static boolean delete(String fileToLoad)
     {
         Set<String> processedDeletions = new HashSet<String>();
         return deleteObjectWorker(fileToLoad, processedDeletions);
     }
 
     /**
      Clears the cache (map) of {file loaded} to {object returned}.
      <p/>
      The below example demonstrates how the objects returned on successive calls to <b>require</b> with the same
      fileToLoad return the same object, unless the object is deleted, or the file cache is cleared. A reason this might
      be done is that code changes the object returned and persists it, and you want to fetch the object cleanly from the
      database again using a DOF.require call. Typically, you won't need to use this method, as the file cache speeds up
      performance of your tests.
      <pre>
      public void testDeleteManufacturer()
      {
          Manufacturer m1 = (Manufacturer) DOF.require("manufacturer.20.xml");
          assertTrue(DOF.delete("manufacturer.20.xml"));
          Manufacturer m2 = (Manufacturer) DOF.require("manufacturer.20.xml");
          assertNotSame(m1, m2);
          Manufacturer m3 = (Manufacturer) DOF.require("manufacturer.20.xml");
          assertSame(m2, m3);
          //DOF.clearFileCache();
          Manufacturer m4 = (Manufacturer) DOF.require("manufacturer.20.xml");
          assertNotSame(m3, m4);
      }
      </pre>
      */
     public static void clearFileCache()
     {
         m_pathToLoadedObject.clear();
     }
 
     //////////////////////////////////////////////////////////////////////////////////////////////
     // private members and methods ///////////////////////////////////////////////////////////////
     //////////////////////////////////////////////////////////////////////////////////////////////
 
     /**
      we keep a cache of the loaded objects to avoid searching the DB every time.
      */
     private static Map<String, Object> m_pathToLoadedObject = new HashMap<String, Object>();
 
     private static Pattern repIncludeDependency;
 
     private static void checkRepIncludeDependencyInitialized()
     {
         // Original pattern -- use the @require("type.pk.ext")
         //repIncludeDependency = Pattern.compile("\\$require\\s*\\(\\s*\"(.*)\"\\s*\\);");
 
         // Changed 3/20/2008 to use annotation format:
         // Leading @ and removed the trailing semicolon
         repIncludeDependency = Pattern.compile("@require\\s*\\(\\s*\"(.*)\"\\s*\\)");
     }
 
 
     /**
      No need for constructor.
      */
     private DOF()
     {
     }
 
     /**
      @param fileToLoad File name in form: {objectType}.{objectPk}.{fileType}
      */
 
     private static Object requireWorker(String fileToLoad)
     {
         // First check local cache of loaded files
         Object resultObject = m_pathToLoadedObject.get(fileToLoad);
         if (resultObject == null)
         {
             // Get handler class for object
             String[] fileNameParts = getFileNameParts(fileToLoad);
             String objectType = fileNameParts[0];
             String pk = fileNameParts[1];
             String fileType = fileNameParts[2].toLowerCase();
             DependentObjectHandler dbJUnitHandler = getHandlerForObject(objectType, fileType);
 
             loadDependencies(fileToLoad);
 
             // Now check if object exists in DB
             resultObject = dbJUnitHandler.get(pk);
             if (resultObject == null)
             {
                 resultObject = dbJUnitHandler.create(fileToLoad);
             }
             if (resultObject == null)
             {
                 throw new RuntimeException("DbJUnitHandler failed to create object with pk " + pk);
             }
 
             m_pathToLoadedObject.put(fileToLoad, resultObject);
         }
         return resultObject;
     }
 
     static String[] getFileNameParts(String fileToLoad)
     {
         final String period = ".";
         int firstPeriodIndex = fileToLoad.indexOf(period);
         int lastPeriodIndex = fileToLoad.lastIndexOf(period);
         String[] fileNameParts = new String[3];
         fileNameParts[0] = fileToLoad.substring(0, firstPeriodIndex);
         fileNameParts[1] = fileToLoad.substring(firstPeriodIndex + 1, lastPeriodIndex);
         fileNameParts[2] = fileToLoad.substring(lastPeriodIndex + 1);
         return fileNameParts;
     }
 
 
     /**
      @param fileToLoad
      @param processedDeletions
 
      @return true if requested object is deleted. Note, the return value from deleting dependency objects for the
      requested object is discarded. For example, if you request an invoice to be deleted, the return value only
      reflects if that requested invoice was deleted, and not if the parent customer record of that invoice is
      deleted.
      */
     private static boolean deleteObjectWorker(String fileToLoad, Set<String> processedDeletions)
     {
         String[] fileNameParts = getFileNameParts(fileToLoad);
         String objectType = fileNameParts[0];
         String pk = fileNameParts[1];
         String fileType = fileNameParts[2].toLowerCase();
         DependentObjectHandler dbJUnitHandler = getHandlerForObject(objectType, fileType);
 
         // delete parent object first
         boolean deletedParent = dbJUnitHandler.delete(pk);
         if (deletedParent)
         {
             processedDeletions.add(fileToLoad);
         }
 
         m_pathToLoadedObject.remove(fileToLoad);
 
         // then delete the dependencies
         deleteDependencies(fileToLoad, processedDeletions);
         return deletedParent;
     }
 
 
     private static void deleteDependencies(String fileToLoad, Set<String> processedDeletions)
     {
         String textForFile = getResourceAsString(fileToLoad);
 
         ArrayList<String> dependencies = getRequiredDependecies(textForFile);
         for (int i = dependencies.size() - 1; i >= 0; i--)
         {
             String requiredPath = dependencies.get(i);
             try
             {
                 if (!processedDeletions.contains(requiredPath))
                 {
                     deleteObjectWorker(requiredPath, processedDeletions);
                 }
                 //else
                 //{
                 //    System.out
                 //            .println("Duplicate dependency caught on deleting: " +
                 //                     requiredPath +
                 //                     ", already processed = " +
                 //                     processedDeletions);
                 //}
             }
             catch (Exception e)
             {
                 System.out
                         .println("Could not delete path = " + requiredPath +
                                  ", Possibly other objects depend on this object. " + e);
             }
         }
     }
 
 
     private static void loadDependencies(String fileName)
     {
         String textForFile = getResourceAsString(fileName);
 
         ArrayList<String> dependencies = getRequiredDependecies(textForFile);
         for (Iterator<String> iterator = dependencies.iterator(); iterator.hasNext();)
         {
             String requiredPath = iterator.next();
             if (!m_pathToLoadedObject.containsKey(requiredPath))
             {
                 requireWorker(requiredPath);
             }
             //else
             //{
             //    System.out
             //            .println("Duplicate dependency caught on loading: " +
             //                     requiredPath +
             //                     ", already processed");
             //}
         }
 
     }
 
 
     private static ArrayList<String> getRequiredDependecies(String requireText)
     {
         checkRepIncludeDependencyInitialized();
         Matcher matcher = repIncludeDependency.matcher(requireText);
         int pos = 0;
         ArrayList<String> matches = new ArrayList<String>();
         while (pos < requireText.length() && matcher.find(pos))
         {
             String requireMatch = matcher.group(1);
             pos = matcher.end(0);
             matches.add(requireMatch);
         }
         return matches;
     }
 
 
     private static String getResourceAsString(String resourceName)
     {
         InputStream sqlInputStream = ClassLoader.getSystemResourceAsStream(resourceName);
         InputStreamReader isr = new InputStreamReader(sqlInputStream);
         BufferedReader br = new BufferedReader(isr);
         String line;
         StringBuffer sb = new StringBuffer();
 
         try
         {
             while ((line = br.readLine()) != null)
             {
                 sb.append(line + "\n");
             }
 
         }
         catch (IOException e)
         {
             throw new RuntimeException(e);
         }
         return sb.toString();
     }
 
     private static DependentObjectHandler getHandlerForObject(String objectType, String fileType)
     {
         String className = HandlerMappings.getHandlerClassNameForObject(objectType, fileType);
         // todo -- store instance in a map
         try
         {
             Class<? extends DependentObjectHandler> handlerClass =
                     (Class<? extends DependentObjectHandler>) Class.forName(className);
            return handlerClass.newInstance();
         }
         catch (ClassNotFoundException e)
         {
             throw new RuntimeException(e);
         }
         catch (IllegalAccessException e)
         {
             throw new RuntimeException(e);
         }
         catch (InstantiationException e)
         {
             throw new RuntimeException(e);
         }
     }
 }
