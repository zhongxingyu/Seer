 /*
  * Copyright (c) 2010 Brookhaven National Laboratory
  * Copyright (c) 2010 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
  * Subject to license terms and conditions.
  */
 package edu.msu.nscl.olog;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import javax.ws.rs.core.Response;
 import java.util.logging.Logger;
 import org.apache.ibatis.exceptions.PersistenceException;
 import org.apache.ibatis.session.SqlSession;
 import org.apache.ibatis.session.SqlSessionFactory;
 
 /**
  * JDBC query to add a logbook to log(s).
  * 
  * @author Eric Berryman taken from Ralph Lange <Ralph.Lange@bessy.de>
  */
 public class UpdateValuesQuery {
 
     private Logger audit = Logger.getLogger(this.getClass().getPackage().getName() + ".audit");
     private static SqlSessionFactory ssf = MyBatisSession.getSessionFactory();
 
     /**
      * Creates a new instance of UpdateValuesQuery.
      *
      * @param data logbook data (containing logs to add logbook to)
      */
     private UpdateValuesQuery() {
     }
     
     /**
      * Updates a logbook in the database with an incoming log
      *
      * @param name String the name of the logbook
      * @param logId Long the log to add to logbook
      * @throws CFException wrapping an SQLException
      */
     public static XmlLogbook updateLogbookWithLog(String name, Long logId) throws CFException {
         SqlSession ss = ssf.openSession();
 
         try {
             List<Long> ids = new ArrayList<Long>();
 
             // Get logbook id
             Long pid = FindLogbookIdsQuery.getLogbookId(name);
 
             if (pid == null) {
                 throw new CFException(Response.Status.NOT_FOUND, "A logbook named '" + name + "' does not exist");
             }
             
             // Add incoming id
             ids.add(logId);
 
             if (ids.isEmpty()) {
                 throw new CFException(Response.Status.NOT_FOUND,
                         "Logs specified in Logbook update do not exist");
             }
 
             HashMap<String, Object> hm = new HashMap<String, Object>();
             hm.put("logidsList", ids);
             hm.put("logbookid", pid);
             hm.put("state", null);
 
             ss.insert("mappings.LogMapping.logsLogbooksEntryFromList", hm);
 
             ss.commit();
             
             // Return the logbook now that the new log has been added
             return ListLogbooksQuery.findLogbook(name);
         } catch (PersistenceException e) {
             throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                     "MyBatis exception: " + e);
         } finally {
             ss.close();
         }
     }
 
     /**
      * Updates a logbook in the database.
      *
      * @param logbook XmlLogbook
      * @throws CFException wrapping an SQLException
      */
     public static XmlLogbook updateLogbook(String name, XmlLogbook logbook) throws CFException {
         SqlSession ss = ssf.openSession();
 
         try {
             List<Long> newestVersionIds = new ArrayList<Long>();
             List<Long> ids = new ArrayList<Long>();
 
             // Get logbook id
             Long pid = FindLogbookIdsQuery.getLogbookId(logbook.getName());
 
             if (pid == null) {
                 throw new CFException(Response.Status.NOT_FOUND, "A logbook named '" + logbook.getName() + "' does not exist");
             }
 
             XmlLogbook p = ListLogbooksQuery.findLogbook(logbook.getName());
             String logbookOwner = p.getOwner();
 
             if ((name != null && !name.equals(logbook.getName())) || (logbook.getOwner() != null && !logbookOwner.equals(logbook.getOwner()))) {
                 HashMap<String, Object> hm = new HashMap<String, Object>();
                 hm.put("name", logbook.getName());
                 hm.put("owner", logbook.getOwner());
                 hm.put("id", pid);
                 ss.update("mappings.LogbookMapper.updateLogbook", hm);
             }
             
             if (logbook.getXmlLogs() == null) {
                return ListLogbooksQuery.findLogbook(logbook.getName());
             }
 
             for (XmlLog log : logbook.getXmlLogs().getLogs()) {
                 if (log.getVersion() > 0) {
                     newestVersionIds.add(log.getId());
                 } else {
                     ids.add(log.getId());
                 }
             }
 
             if (!newestVersionIds.isEmpty()) {
                 ArrayList<XmlLog> logs = (ArrayList<XmlLog>) ss.selectList("mappings.LogMapping.getChildrenIds", newestVersionIds);
                 if (logs.isEmpty()) {
                     throw new CFException(Response.Status.NOT_FOUND,
                             "Logs do not exist in getChildrenIds query");
                 }
 
                 for (XmlLog log : logs) {
                     ids.add(log.getId());
                 }
             }
 
             if (ids.isEmpty()) {
                 throw new CFException(Response.Status.NOT_FOUND,
                         "Logs specified in Logbook update do not exist");
             }
 
             HashMap<String, Object> hm = new HashMap<String, Object>();
             hm.put("pid", pid);
             hm.put("list", ids);
 
             ss.update("mappings.LogMapping.updateAsInactive", hm);
 
             hm.clear();
 
             hm.put("logidsList", ids);
             hm.put("logbookid", pid);
             hm.put("state", null);
 
             ss.insert("mappings.LogMapping.logsLogbooksEntryFromList", hm);
 
             ss.commit();
             
             // Return the logbook now that all the changes have been made
             return ListLogbooksQuery.findLogbook(logbook.getName());
         } catch (PersistenceException e) {
             throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                     "MyBatis exception: " + e);
         } finally {
             ss.close();
         }
     }
 
     /**
      * Updates a tag in the database, adding it to all logs in <tt>tag</tt>.
      *
      * @param tag XmlTag
      * @throws CFException wrapping an SQLException
      */
     public static XmlTag updateTag(String name, XmlTag tag) throws CFException {
         SqlSession ss = ssf.openSession();
 
         try {
             List<Long> newestVersionIds = new ArrayList<Long>();
             List<Long> ids = new ArrayList<Long>();
 
             // Get logbook id
             Long pid = FindLogbookIdsQuery.getLogbookId(tag.getName());
            
            if (pid == null) {
                throw new CFException(Response.Status.NOT_FOUND, "A tag named '" + tag.getName() + "' does not exist");
            }
 
             if (name != null && !name.equals(tag.getName())) {
                 HashMap<String, Object> hm = new HashMap<String, Object>();
                 hm.put("name", tag.getName());
                 hm.put("id", pid);
                 ss.update("mappings.TagMapper.updateTag", hm);
             }
            
            XmlTag t = ListLogbooksQuery.findTag(tag.getName());
            if(t == null){
                 return null;
             }
            
            if (tag.getXmlLogs() == null) {
                return t;
            }
             if (tag.getXmlLogs().getLogs().isEmpty()) {
                return t;
             }
             
             for (XmlLog log : tag.getXmlLogs().getLogs()) {
                 if (log.getVersion() > 0) {
                     newestVersionIds.add(log.getId());
                 } else {
                     ids.add(log.getId());
                 }
             }
 
             if (!newestVersionIds.isEmpty()) {
                 ArrayList<XmlLog> logs = (ArrayList<XmlLog>) ss.selectList("mappings.LogMapping.getChildrenIds", newestVersionIds);
                 if (logs.isEmpty()) {
                     throw new CFException(Response.Status.NOT_FOUND,
                             "Logs do not exist in getChildrenIds query");
                 }
 
                 for (XmlLog log : logs) {
                     ids.add(log.getId());
                 }
             }
 
             if (ids.isEmpty()) {
                 throw new CFException(Response.Status.NOT_FOUND,
                         "Logs specified in Logbook update do not exist");
             }
 
             HashMap<String, Object> hm = new HashMap<String, Object>();
             hm.put("pid", pid);
             hm.put("list", ids);
 
             ss.update("mappings.LogMapping.updateAsInactive", hm);
 
             hm.clear();
 
             hm.put("logidsList", ids);
             hm.put("logbookid", pid);
             hm.put("state", null);
 
             ss.insert("mappings.LogMapping.logsLogbooksEntryFromList", hm);
 
             ss.commit();
             
             // Return new tag now that the logs have been added
             return ListLogbooksQuery.findTag(tag.getName());
         } catch (PersistenceException e) {
             throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                     "MyBatis exception: " + e);
         } finally {
             ss.close();
         }
     }
 
     /**
     * Updates the <tt>tag</tt> in the database, adding it to the single log <tt>logId</tt>.
      *
      * @param tag name of tag to add
      * @param logId id of log to add tag to
      * @throws CFException wrapping an SQLException
      */
     public static XmlTag updateTag(String tag, Long logId) throws CFException {
         SqlSession ss = ssf.openSession();
 
         try {
             List<Long> newestVersionIds = new ArrayList<Long>();
             List<Long> ids = new ArrayList<Long>();
             
             XmlLogs logs = new XmlLogs(new XmlLog(logId));
 
             // Get log id
             Long pid = FindLogbookIdsQuery.getLogbookId(tag);
            if (pid == null) {
                throw new CFException(Response.Status.NOT_FOUND, "A tag named '" + tag + "' does not exist");
            }
             
             if (logs == null) {
                 return null;
             }
             if (logs.getLogs().isEmpty()) {
                 return null;
             }
             
             for (XmlLog log : logs.getLogs()) {
                 if (log.getVersion() > 0) {
                     newestVersionIds.add(log.getId());
                 } else {
                     ids.add(log.getId());
                 }
             }
 
             if (!newestVersionIds.isEmpty()) {
                 ArrayList<XmlLog> logs_returned = (ArrayList<XmlLog>) ss.selectList("mappings.LogMapping.getChildrenIds", newestVersionIds);
                 if (logs_returned.isEmpty()) {
                     throw new CFException(Response.Status.NOT_FOUND,
                             "Logs do not exist in getChildrenIds query");
                 }
 
                 for (XmlLog log : logs_returned) {
                     ids.add(log.getId());
                 }
             }
 
             if (ids.isEmpty()) {
                 throw new CFException(Response.Status.NOT_FOUND,
                         "Logs specified in Logbook update do not exist");
             }
 
             HashMap<String, Object> hm = new HashMap<String, Object>();
 
             hm.put("logidsList", ids);
             hm.put("logbookid", pid);
             hm.put("state", null);
 
             ss.insert("mappings.LogMapping.logsLogbooksEntryFromList", hm);
 
             ss.commit();
             
             // Return new tag now that the new log have been added
             return ListLogbooksQuery.findTag(tag);
         } catch (PersistenceException e) {
             throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                     "MyBatis exception: " + e);
         } finally {
             ss.close();
         }
     }
 }
