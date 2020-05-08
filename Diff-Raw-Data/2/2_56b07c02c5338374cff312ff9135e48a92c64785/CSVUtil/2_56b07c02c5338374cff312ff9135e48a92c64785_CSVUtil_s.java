 package org.youthnet.export.util;
 
 import org.youthnet.export.domain.CSVable;
 import org.youthnet.export.domain.vb25.ContainsOid;
 import org.youthnet.export.domain.vb25.ContainsOrgid;
 import org.youthnet.export.domain.vb25.ContainsVid;
 import org.youthnet.export.domain.vb3.ContainsDiscriminator;
 import org.youthnet.export.domain.vb3.ContainsValue;
 import org.youthnet.export.domain.vb3.ContainsVb2id;
 import org.youthnet.export.io.CSVFileReader;
 
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * User: karl
  * Date: 06-Jul-2010
  */
 public class CSVUtil {
     private CSVUtil() {
     }
 
     public static <T extends CSVable> List<T> createDomainList(String filePath, Class<T> type) {
         List<T> domainObjects = null;
         CSVFileReader csvFileReader = null;
 
         try {
             csvFileReader = new CSVFileReader(new FileReader(filePath));
             domainObjects = new ArrayList<T>();
 
             String record = "";
             T domainObject = null;
             while ((record = csvFileReader.readRecord()) != null) {
                 try {
                     domainObject = type.newInstance();
                     domainObject.init(record);
                     domainObjects.add(domainObject);
                 } catch (InstantiationException e) {
                     System.out.println("Could not instantiate " + type.getName() + ". Error: " + e.getMessage());
                     break;
                 } catch (IllegalAccessException e) {
                     System.out.println("Could not access " + type.getName() + ". Error: " + e.getMessage());
                     break;
                 }
             }
 
         } catch (IOException e) {
             System.out.println("File " + filePath + " not found. Error: " + e.getMessage());
         } finally {
             if (csvFileReader != null) {
                 try {
                     csvFileReader.close();
                 } catch (IOException e) {
                     System.out.println("Could not close file stream. Error: " + e.getMessage());
                 }
             }
         }
 
         return domainObjects;
     }
 
     public static <T extends ContainsVid & CSVable> Map<Long, List<T>> createVidMap(String filePath, Class<T> type) {
         Map<Long, List<T>> vidMap = null;
         CSVFileReader csvFileReader = null;
 
         try {
             csvFileReader = new CSVFileReader(new FileReader(filePath));
             vidMap = new HashMap<Long, List<T>>();
 
             String record = "";
             T domainObject = null;
             while ((record = csvFileReader.readRecord()) != null) {
                 try {
                     domainObject = type.newInstance();
                     domainObject.init(record);
                     if (vidMap.get(domainObject.getVid()) == null)
                         vidMap.put(domainObject.getVid(), new ArrayList<T>());
                     vidMap.get(domainObject.getVid()).add(domainObject);
                 } catch (InstantiationException e) {
                     System.out.println("Could not instantiate " + type.getName() + ". Error: " + e.getMessage());
                     break;
                 } catch (IllegalAccessException e) {
                     System.out.println("Could not access " + type.getName() + ". Error: " + e.getMessage());
                     break;
                 }
             }
 
         } catch (IOException e) {
             System.out.println("File " + filePath + " not found. Error: " + e.getMessage());
         } finally {
             if (csvFileReader != null) {
                 try {
                     csvFileReader.close();
                 } catch (IOException e) {
                     System.out.println("Could not close file stream. Error: " + e.getMessage());
                 }
             }
         }
 
         return vidMap;
     }
 
     public static <T extends ContainsOid & CSVable> Map<Long, List<T>> createOidMap(String filePath, Class<T> type) {
         Map<Long, List<T>> oidMap = null;
         CSVFileReader csvFileReader = null;
 
         try {
             csvFileReader = new CSVFileReader(new FileReader(filePath));
             oidMap = new HashMap<Long, List<T>>();
 
             String record = "";
             T domainObject = null;
             while ((record = csvFileReader.readRecord()) != null) {
                 try {
                     domainObject = type.newInstance();
                     domainObject.init(record);
                     if (oidMap.get(domainObject.getOid()) == null)
                         oidMap.put(domainObject.getOid(), new ArrayList<T>());
                     oidMap.get(domainObject.getOid()).add(domainObject);
                 } catch (InstantiationException e) {
                     System.out.println("Could not instantiate " + type.getName() + ". Error: " + e.getMessage());
                     break;
                 } catch (IllegalAccessException e) {
                     System.out.println("Could not access " + type.getName() + ". Error: " + e.getMessage());
                     break;
                 }
             }
 
         } catch (IOException e) {
             System.out.println("File " + filePath + " not found. Error: " + e.getMessage());
         } finally {
             if (csvFileReader != null) {
                 try {
                     csvFileReader.close();
                 } catch (IOException e) {
                     System.out.println("Could not close file stream. Error: " + e.getMessage());
                 }
             }
         }
 
         return oidMap;
     }
 
     public static <T extends ContainsOrgid & CSVable> Map<Long, List<T>> createOrgidMap(String filePath, Class<T> type) {
         Map<Long, List<T>> orgidMap = null;
         CSVFileReader csvFileReader = null;
 
         try {
             csvFileReader = new CSVFileReader(new FileReader(filePath));
             orgidMap = new HashMap<Long, List<T>>();
 
             String record = "";
             T domainObject = null;
             while ((record = csvFileReader.readRecord()) != null) {
                 try {
                     domainObject = type.newInstance();
                     domainObject.init(record);
                     if (orgidMap.get(domainObject.getOrgid()) == null)
                         orgidMap.put(domainObject.getOrgid(), new ArrayList<T>());
                     orgidMap.get(domainObject.getOrgid()).add(domainObject);
                 } catch (InstantiationException e) {
                     System.out.println("Could not instantiate " + type.getName() + ". Error: " + e.getMessage());
                     break;
                 } catch (IllegalAccessException e) {
                     System.out.println("Could not access " + type.getName() + ". Error: " + e.getMessage());
                     break;
                 }
             }
 
         } catch (IOException e) {
             System.out.println("File " + filePath + " not found. Error: " + e.getMessage());
         } finally {
             if (csvFileReader != null) {
                 try {
                     csvFileReader.close();
                 } catch (IOException e) {
                     System.out.println("Could not close file stream. Error: " + e.getMessage());
                 }
             }
         }
 
         return orgidMap;
     }
 
     public static <T extends ContainsVb2id & CSVable> Map<Long, List<T>> createVb2idListMap(String filePath, Class<T> type) {
         Map<Long, List<T>> orgidMap = null;
         CSVFileReader csvFileReader = null;
 
         try {
             csvFileReader = new CSVFileReader(new FileReader(filePath));
             orgidMap = new HashMap<Long, List<T>>();
 
             String record = "";
             T domainObject = null;
             while ((record = csvFileReader.readRecord()) != null) {
                 try {
                     domainObject = type.newInstance();
                     domainObject.init(record);
                     if (orgidMap.get(domainObject.getVbase2Id()) == null)
                         orgidMap.put(domainObject.getVbase2Id(), new ArrayList<T>());
                     orgidMap.get(domainObject.getVbase2Id()).add(domainObject);
                 } catch (InstantiationException e) {
                     System.out.println("Could not instantiate " + type.getName() + ". Error: " + e.getMessage());
                     break;
                 } catch (IllegalAccessException e) {
                     System.out.println("Could not access " + type.getName() + ". Error: " + e.getMessage());
                     break;
                 }
             }
 
         } catch (IOException e) {
             System.out.println("File " + filePath + " not found. Error: " + e.getMessage());
         } finally {
             if (csvFileReader != null) {
                 try {
                     csvFileReader.close();
                 } catch (IOException e) {
                     System.out.println("Could not close file stream. Error: " + e.getMessage());
                 }
             }
         }
 
         return orgidMap;
     }
 
     public static <T extends ContainsVb2id & CSVable> Map<Long, T> createVb2idMap(String filePath, Class<T> type) {
         Map<Long, T> orgidMap = null;
         CSVFileReader csvFileReader = null;
 
         try {
             csvFileReader = new CSVFileReader(new FileReader(filePath));
             orgidMap = new HashMap<Long, T>();
 
             String record = "";
             T domainObject = null;
             while ((record = csvFileReader.readRecord()) != null) {
                 try {
                     domainObject = type.newInstance();
                     domainObject.init(record);
                     orgidMap.put(domainObject.getVbase2Id(), domainObject);
                 } catch (InstantiationException e) {
                     System.out.println("Could not instantiate " + type.getName() + ". Error: " + e.getMessage());
                     break;
                 } catch (IllegalAccessException e) {
                     System.out.println("Could not access " + type.getName() + ". Error: " + e.getMessage());
                     break;
                 }
             }
 
         } catch (IOException e) {
             System.out.println("File " + filePath + " not found. Error: " + e.getMessage());
         } finally {
             if (csvFileReader != null) {
                 try {
                     csvFileReader.close();
                 } catch (IOException e) {
                     System.out.println("Could not close file stream. Error: " + e.getMessage());
                 }
             }
         }
 
         return orgidMap;
     }
 
     public static <T extends ContainsDiscriminator & ContainsValue & CSVable>
     Map<String, Map<String, T>> createDiscriminatorValueMap(String filePath, Class<T> type) {
         Map<String, Map<String, T>> discValMap = null;
         CSVFileReader csvFileReader = null;
 
         try {
             csvFileReader = new CSVFileReader(new FileReader(filePath));
             discValMap = new HashMap<String, Map<String, T>>();
 
             String record = "";
             T domainObject = null;
             while ((record = csvFileReader.readRecord()) != null) {
                 try {
                     domainObject = type.newInstance();
                     domainObject.init(record);
                    if (discValMap.get(domainObject.getDiscriminator()) == null)
                         discValMap.put(domainObject.getDiscriminator().toLowerCase(), new HashMap<String, T>());
                     discValMap.get(domainObject.getDiscriminator().toLowerCase()).put(
                             domainObject.getValue().toLowerCase(), domainObject);
                 } catch (InstantiationException e) {
                     System.out.println("Could not instantiate " + type.getName() + ". Error: " + e.getMessage());
                     break;
                 } catch (IllegalAccessException e) {
                     System.out.println("Could not access " + type.getName() + ". Error: " + e.getMessage());
                     break;
                 }
             }
 
         } catch (IOException e) {
             System.out.println("File " + filePath + " not found. Error: " + e.getMessage());
         } finally {
             if (csvFileReader != null) {
                 try {
                     csvFileReader.close();
                 } catch (IOException e) {
                     System.out.println("Could not close file stream. Error: " + e.getMessage());
                 }
             }
         }
 
         return discValMap;
     }
 }
