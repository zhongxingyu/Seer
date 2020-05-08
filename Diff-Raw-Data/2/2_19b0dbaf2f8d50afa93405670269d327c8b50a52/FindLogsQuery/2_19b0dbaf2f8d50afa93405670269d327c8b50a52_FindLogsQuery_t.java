 /*
  * Copyright (c) 2010 Brookhaven National Laboratory
  * Copyright (c) 2010 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
  * Subject to license terms and conditions.
  */
 package edu.msu.nscl.olog;
 
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.Multimap;
 import java.io.UnsupportedEncodingException;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.ws.rs.core.MultivaluedMap;
 
 import javax.jcr.RepositoryException;
 import javax.ws.rs.core.Response;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.ibatis.exceptions.PersistenceException;
 import org.apache.ibatis.session.ResultContext;
 import org.apache.ibatis.session.ResultHandler;
 import org.apache.ibatis.session.SqlSession;
 import org.apache.ibatis.session.SqlSessionFactory;
 
 /**
  *  JDBC query to retrieve logs from the directory .
  *
  * @author Eric Berryman taken from Ralph Lange <Ralph.Lange@bessy.de>
  */
 public class FindLogsQuery {
 
     private enum SearchType {
 
         LOG, TAG
     };
     private Multimap<String, String> value_matches = ArrayListMultimap.create();
     private Multimap<String, String> logPaginate_matches = ArrayListMultimap.create();
     private Multimap<String, String> date_matches = ArrayListMultimap.create();
     private List<String> log_matches = new ArrayList();
     private List<Long> logId_matches = new ArrayList();
     private List<String> logbook_matches = new ArrayList();
     private List<String> tag_matches = new ArrayList();
     private List<String> tag_patterns = new ArrayList();
     private List<Long> jcr_search_ids = new ArrayList();
     private static SqlSessionFactory ssf = MyBatisSession.getSessionFactory();
 
     /**
      * Creates a new instance of FindLogsQuery, sorting the query parameters.
      * Logbook matches and tag string matches go to the first inner query,
      * tag pattern matches are queried separately,
      * name matches go to the outer query.
      * Logbook and tag names are converted to lowercase before being matched.
      *
      * @param matches  the map of matches to apply
      */
     private FindLogsQuery(MultivaluedMap<String, String> matches) throws RepositoryException {
         for (Map.Entry<String, List<String>> match : matches.entrySet()) {
             String key = match.getKey().toLowerCase();
             if (key.equals("search")) {
                 log_matches.addAll(match.getValue());
                 JcrSearch js = new JcrSearch();
                 jcr_search_ids = js.searchForIds(match.getValue().get(0));
             } else if (key.equals("tag")) {
                 addTagMatches(match.getValue());
             } else if (key.equals("logbook")) {
                 addLogbookMatches(match.getValue());
             } else if (key.equals("page")) {
                 logPaginate_matches.putAll(key, match.getValue());
             } else if (key.equals("limit")) {
                 logPaginate_matches.putAll(key, match.getValue());
             } else if (key.equals("start")) {
                 date_matches.putAll(key, match.getValue());
             } else if (key.equals("end")) {
                 date_matches.putAll(key, match.getValue());
             } else {
                 value_matches.putAll(key, match.getValue());
             }
         }
     }
 
     private FindLogsQuery(SearchType type, Collection<String> matches) {
         if (type == SearchType.LOG) {
             log_matches.addAll(matches);
         } else {
             addTagMatches(matches);
         }
     }
 
     private FindLogsQuery(SearchType type, String name) {
         if (type == SearchType.LOG) {
             log_matches.add(name);
         } else {
             addTagMatches(Collections.singleton(name));
         }
     }
 
     private FindLogsQuery(SearchType type, Long logId) {
         if (type == SearchType.LOG) {
             logId_matches.add(logId);
         }
     }
 
     private void addLogbookMatches(Collection<String> matches) {
         for (String m : matches) {
             logbook_matches.add(m);
         }
         if (logbook_matches.size() == 1) {
             String match = logbook_matches.get(0);
             logbook_matches.clear();
             logbook_matches.addAll(Arrays.asList(match.split(",")));
         }
     }
 
     private void addTagMatches(Collection<String> matches) {
         for (String m : matches) {
             if (m.contains("?") || m.contains("*")) {
                 tag_patterns.add(m);
             } else {
                 tag_matches.add(m);
             }
         }
     }
 
     /**
      * Creates and executes the logbook and tag string match subquery using GROUP.
      *
      * @param con connection to use
      * @return a set of log ids that match
      */
     //TODO:  need to add search params like olog; logs between dates, search all fields, files, etc.
     private Set<Long> getIdsFromLogbookAndTagMatch() throws CFException {
         SqlSession ss = ssf.openSession();
 
         try {
             Set<Long> ids = new HashSet<Long>();           // set of matching log ids
             List<String> params = new ArrayList<String>(); // parameter list for this query
 
             for (String tag : tag_matches) {
                 params.add(tag);
             }
             int size = tag_matches.size();
 
             HashMap<String, Object> hm = new HashMap<String, Object>();
             hm.put("list", params);
             hm.put("size", size);
 
             ArrayList<XmlLog> logs = (ArrayList<XmlLog>) ss.selectList("mappings.LogMapping.getIdsFromLogbookAndTagMatch", hm);
             if (logs != null) {
                 Iterator<XmlLog> iterator = logs.iterator();
                 while (iterator.hasNext()) {
                     XmlLog log = iterator.next();
                     ids.add(log.getId());
                 }
             }
 
             return ids;
         } catch (PersistenceException e) {
             throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                     "MyBatis exception: " + e);
         } finally {
             ss.close();
         }
     }
 
     /**
      * Creates and executes the properties string match subquery using GROUP.
      *
      * @param con connection to use
      * @return a set of log ids that match
      */
     private Set<Long> getIdsFromPropertiesMatch() throws CFException {
         SqlSession ss = ssf.openSession();
 
         try {
             Set<Long> ids = new HashSet<Long>();           // set of matching log ids
 
             HashMap<String, Object> hm = new HashMap<String, Object>();
 
             for (Map.Entry<String, String> match : value_matches.entries()) {
                 // Key is coming in as property[attribute] so lets split those two out
                Pattern p = Pattern.compile("((?:[A-Za-z0-9]+))(\\[)((?:[A-Za-z0-9]+))(\\])");
                 Matcher m = p.matcher(match.getKey());
                 if (m.matches()) {
                     hm.clear();
                     hm.put("property", m.group(1));
                     hm.put("attribute", m.group(3));
                     hm.put("value", convertFileGlobToSQLPattern(match.getValue()));
                     ArrayList<Long> logIds = (ArrayList<Long>) ss.selectList("mappings.PropertyMapping.getIdsFromPropertiesMatch", hm);
                     if (logIds != null) {
                         if (ids.isEmpty()) {
                             ids.addAll(logIds);
                         }
                         else { // For AND operations
                             ids.retainAll(logIds);
                         }
                     }
                 }
             }
 
             return ids;
         } catch (PersistenceException e) {
             throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                     "MyBatis exception: " + e);
         } finally {
             ss.close();
         }
 
 
     }
 
     /**
      * Creates and executes the tag string match subquery using GROUP.
      *
      * @param con connection to use
      * @return a set of log ids that match
      */
     private Set<Long> getIdsFromTagMatch(String match) throws CFException {
         SqlSession ss = ssf.openSession();
 
         try {
             Set<Long> ids = new HashSet<Long>();
 
             ArrayList<XmlLog> logs = (ArrayList<XmlLog>) ss.selectList("mappings.LogMapping.getIdsFromTagMatch", match);
             if (logs != null) {
                 Iterator<XmlLog> iterator = logs.iterator();
                 while (iterator.hasNext()) {
                     XmlLog log = iterator.next();
                     ids.add(log.getId());
                 }
             }
 
             return ids;
         } catch (PersistenceException e) {
             throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                     "MyBatis exception: " + e);
         } finally {
             ss.close();
         }
     }
 
     /**
      * Creates and executes the logbook string match subquery using GROUP.
      *
      * @return a set of log ids that match
      */
     private Set<Long> getIdsFromLogbookMatch(String match) throws CFException {
         SqlSession ss = ssf.openSession();
 
         try {
             Set<Long> ids = new HashSet<Long>();
 
             ArrayList<XmlLog> logs = (ArrayList<XmlLog>) ss.selectList("mappings.LogMapping.getIdsFromLogbookMatch", match);
             if (logs != null) {
                 Iterator<XmlLog> iterator = logs.iterator();
                 while (iterator.hasNext()) {
                     XmlLog log = iterator.next();
                     ids.add(log.getId());
                 }
             }
 
             return ids;
         } catch (PersistenceException e) {
             throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                     "MyBatis exception: " + e);
         } finally {
             ss.close();
         }
     }
 
     /**
      * Creates and executes the pagination subquery using GROUP BY.
      *
      * @param con connection to use
      * @return a set of log ids that match
      */
     Set<Long> getIdsFromPagination() throws CFException {
         SqlSession ss = ssf.openSession();
 
         try {
             Set<Long> idsList = new HashSet<Long>();
             Set<Long> idsSearchList = new HashSet<Long>();
             Set<String> valuesList = new HashSet<String>();
             Set<Long> returnIds = new HashSet<Long>();
             HashMap<String, Object> hm = new HashMap<String, Object>();
 
             if (!tag_matches.isEmpty()) {
                 for (String tag : tag_matches) {
                     Set<Long> ids = getIdsFromTagMatch(tag);
                     if (ids.isEmpty()) {
                         return null;
                     }
                     idsList.addAll(ids);
                 }
             }
 
             if (!value_matches.isEmpty()) {
                 Set<Long> ids = getIdsFromPropertiesMatch();
                 if (ids.isEmpty()) {
                     return null;
                 }
                 idsList.addAll(ids);
             }
 
             if (!tag_patterns.isEmpty()) {
                 for (String p : tag_patterns) {
                     Set<Long> ids = getIdsFromTagMatch(p);
                     if (ids.isEmpty()) {
                         return null;
                     }
                     idsList.addAll(ids);
                 }
             }
 
             if (!logbook_matches.isEmpty()) {
                 if (idsList.isEmpty()) {
                     for (String logbook : logbook_matches) {
                         Set<Long> ids = getIdsFromLogbookMatch(logbook);
                         if (ids.isEmpty()) {
                             return null;
                         }
                         idsList.addAll(ids);
                     }
                 } else {
                     Set<Long> id_results = new HashSet<Long>();
                     for (String logbook : logbook_matches) {
                         Set<Long> ids = getIdsFromLogbookMatch(logbook);
                         if (ids.isEmpty()) {
                             return null;
                         }
                         id_results.addAll(ids);
                     }
                     Set<Long> temp_set = new HashSet<Long>();
                     for (Long id : idsList) {
                         if (id_results.contains(id)) {
                             temp_set.add(id);
                         }
                     }
                     idsList.clear();
                     idsList.addAll(temp_set);
                 }
             }
 
             if (!date_matches.isEmpty()) {
                 String start = null, end = null;
                 for (Map.Entry<String, Collection<String>> match : date_matches.asMap().entrySet()) {
                     if (match.getKey().toLowerCase().equals("start")) {
                         start = match.getValue().iterator().next();
                     }
                     if (match.getKey().toLowerCase().equals("end")) {
                         end = match.getValue().iterator().next();
                     }
                 }
 
                 if (start != null && end == null) {
                     hm.put("start", Long.valueOf(start));
                     hm.put("end", Long.valueOf(Calendar.getInstance().getTime().getTime() / 1000L));
                 } else if (start == null && end != null) {
                     hm.put("start", 0L);
                     hm.put("end", Long.valueOf(end));
                 } else {
                     hm.put("start", Long.valueOf(start));
                     hm.put("end", Long.valueOf(end));
                 }
             }
             if (!logId_matches.isEmpty()) {
                 for (long i : logId_matches) {
                     idsList.add(i);
                 }
             }
             if (!log_matches.isEmpty()) {
                 for (String value : log_matches) {
                     valuesList.add(convertFileGlobToSQLPattern(value));
                 }
             }
 
             if (!jcr_search_ids.isEmpty()) {
                 for (long i : jcr_search_ids) {
                     idsSearchList.add(i);
                 }
             }
 
             if (!logPaginate_matches.isEmpty()) {
                 String limit = null, offset = null;
                 for (Map.Entry<String, Collection<String>> match : logPaginate_matches.asMap().entrySet()) {
                     if (match.getKey().toLowerCase().equals("limit")) {
                         limit = match.getValue().iterator().next();
                     }
                     if (match.getKey().toLowerCase().equals("page")) {
                         offset = match.getValue().iterator().next();
                     }
                 }
                 if (limit != null && offset != null) {
                     Long longOffset = Long.valueOf(offset) * Long.valueOf(limit) - Long.valueOf(limit);
                     hm.put("limit", Long.valueOf(limit));
                     hm.put("offset", longOffset);
                 }
             }
 
             if (idsSearchList.size() > 0) {
                 hm.put("idsSearchList", idsSearchList);
             }
 
             if (idsList.size() > 0) {
                 hm.put("idsList", idsList);
             }
             if (valuesList.size() > 0) {
                 hm.put("valuesList", valuesList);
             }
 
             ArrayList<XmlLog> logs = (ArrayList<XmlLog>) ss.selectList("mappings.LogMapping.getIdsFromPagination", hm);
             if (logs != null) {
                 Iterator<XmlLog> iterator = logs.iterator();
                 while (iterator.hasNext()) {
                     XmlLog log = iterator.next();
                     returnIds.add(log.getId());
                 }
             }
 
             return returnIds;
         } catch (PersistenceException e) {
             throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                     "MyBatis exception: " + e);
         } finally {
             ss.close();
         }
     }
 
     /**
      * Creates and executes a JDBC based query using subqueries for
      * logbook and tag matches.
      *
      * @param con  connection to use
      * @return result set with columns named <tt>log</tt>, <tt>logbook</tt>,
      *         <tt>value</tt>, null if no results
      * @throws CFException wrapping an SQLException
      */
     private ArrayList<XmlLog> executeQuery() throws CFException {
         SqlSession ss = ssf.openSession();
 
         try {
 
             List<Long> idsList = new ArrayList<Long>();
             Set<Long> paginate_result = new HashSet<Long>();
 
             Set<Long> ids = getIdsFromPagination();
 
             if (ids == null || ids.isEmpty()) {
                 return null;
             } else {
                 paginate_result.addAll(ids);
             }
             if (!paginate_result.isEmpty()) {
                 for (long i : paginate_result) {
                     idsList.add(i);
                 }
             }
 
 //            final ArrayList<XmlLog> logs = new ArrayList<XmlLog>();
 //            ss.select("mappings.LogMapping.getLogsFromIds", idsList, new ResultHandler() {
 //
 //                @Override
 //                public void handleResult(ResultContext rc) {
 //                    XmlLog log = (XmlLog) rc.getResultObject();
 //
 //                    // All of the logs current properties minus attributes
 //                    Collection<XmlProperty> props = log.getXmlProperties();
 //
 //                    // New collection of properties which will contain the log entries attributes
 //                    Collection<XmlProperty> newProps = new ArrayList<XmlProperty>();
 //                    
 //                    HashMap<String, Object> hm = new HashMap<String, Object>();
 //
 //                    // Iterate through each current property
 //                    for (XmlProperty prop : props) {
 //                        // Create a new property object to copy data from the current property
 //                        XmlProperty newProp = new XmlProperty(prop.getName());
 //                        newProp.setGroupingNum(prop.getGroupingNum());
 //                        newProp.setId(prop.getId());
 //
 //                        Map<String, String> attributes = new HashMap<String, String>();
 //
 //                        // Get the attributes
 //                        hm.clear();
 //                        hm.put("lid", log.getId());
 //                        hm.put("pid", prop.getId());
 //                        hm.put("gnum", prop.getGroupingNum());
 //                        ArrayList<HashMap> attrs = (ArrayList<HashMap>) ss.selectList("mappings.PropertyMapping.attributesForLog", hm);
 //
 //                        // Iterate through the returned hashmaps and populate the new attributes hashmap
 //                        for (HashMap hash : attrs) {
 //                            attributes.put(hash.get("name").toString(), hash.get("value").toString());
 //                        }
 //                        // Associate the attributes with the new property object
 //                        newProp.setAttributes(attributes);
 //
 //                        // Add to the collection
 //                        newProps.add(newProp);
 //                    }
 //                    // Set the logs new properties
 //                    log.setXmlProperties(newProps);
 //                    
 //                    // Add to logs list
 //                    logs.add(log);
 //                }
 //            });
 
             ArrayList<XmlLog> logs = (ArrayList<XmlLog>) ss.selectList("mappings.LogMapping.getLogsFromIds", idsList);
             HashMap<String, Object> hm = new HashMap<String, Object>();
 
             // Iterate through all the logs so we can add attributes
             for (XmlLog log : logs) {
 
                 // All of the logs current properties minus attributes
                 Collection<XmlProperty> props = log.getXmlProperties();
 
                 // New collection of properties which will contain the log entries attributes
                 Collection<XmlProperty> newProps = new ArrayList<XmlProperty>();
 
                 // Iterate through each current property
                 for (XmlProperty prop : props) {
                     // Create a new property object to copy data from the current property
                     XmlProperty newProp = new XmlProperty(prop.getName());
                     newProp.setGroupingNum(prop.getGroupingNum());
                     newProp.setId(prop.getId());
 
                     Map<String, String> attributes = new HashMap<String, String>();
 
                     // Get the attributes
                     hm.clear();
                     hm.put("lid", log.getId());
                     hm.put("pid", prop.getId());
                     hm.put("gnum", prop.getGroupingNum());
                     ArrayList<HashMap> attrs = (ArrayList<HashMap>) ss.selectList("mappings.PropertyMapping.attributesForLog", hm);
 
                     // Iterate through the returned hashmaps and populate the new attributes hashmap
                     for (HashMap hash : attrs) {
                         attributes.put(hash.get("name").toString(), hash.get("value").toString());
                     }
                     // Associate the attributes with the new property object
                     newProp.setAttributes(attributes);
 
                     // Add to the collection
                     newProps.add(newProp);
                 }
                 // Set the logs new properties
                 log.setXmlProperties(newProps);
             }
 
             return logs;
         } catch (PersistenceException e) {
             throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                     "MyBatis exception: " + e);
         } finally {
             ss.close();
         }
     }
 
     /* Regexp for this pattern: "((\\\\)*)((\\\*)|(\*)|(\\\?)|(\?)|(%)|(_))"
      * i.e. any number of "\\" (group 1) -> same number of "\\"
      * then any of        "\*" (group 4) -> "*"
      *                    "*"  (group 5) -> "%"
      *                    "\?" (group 6) -> "?"
      *                    "?"  (group 7) -> "_"
      *                    "%"  (group 8) -> "\%"
      *                    "_"  (group 9) -> "\_"
      */
     private static Pattern pat = Pattern.compile("((\\\\\\\\)*)((\\\\\\*)|(\\*)|(\\\\\\?)|(\\?)|(%)|(_))");
     private static final int grp[] = {4, 5, 6, 7, 8, 9};
     private static final String rpl[] = {"*", "%", "?", "_", "\\%", "\\_"};
 
     /**
      * Translates the specified file glob pattern <tt>in</tt>
      * into the corresponding SQL pattern.
      *
      * @param in  file glob pattern
      * @return  SQL pattern
      */
     private static String convertFileGlobToSQLPattern(String in) {
         StringBuffer out = new StringBuffer();
         Matcher m = pat.matcher(in);
 
         while (m.find()) {
             StringBuilder rep = new StringBuilder();
             if (m.group(1) != null) {
                 rep.append(m.group(1));
             }
             for (int i = 0; i < grp.length; i++) {
                 if (m.group(grp[i]) != null) {
                     rep.append(rpl[i]);
                     break;
                 }
             }
             m.appendReplacement(out, rep.toString());
         }
         m.appendTail(out);
         return out.toString();
     }
 
     /**
      * Finds logs by matching logbook/tag values and/or log and/or tag names.
      *
      * @param matches MultiMap of query parameters
      * @return XmlLogs container with all found logs and their logbooks/tags
      */
     public static XmlLogs findLogsByMultiMatch(MultivaluedMap<String, String> matches) throws CFException, RepositoryException, UnsupportedEncodingException, NoSuchAlgorithmException {
         FindLogsQuery q = new FindLogsQuery(matches);
         XmlLogs xmlLogs = new XmlLogs();
 
         ArrayList<XmlLog> logs = q.executeQuery();
         if (logs != null) {
             Iterator<XmlLog> iterator = logs.iterator();
             while (iterator.hasNext()) {
                 XmlLog log = iterator.next();
                 if (log.getMD5Entry().equals(CreateLogQuery.getmd5Entry(log.getId(), log))) {
                     xmlLogs.addXmlLog(log);
                 }
             }
         }
 
         return xmlLogs;
 
     }
 
     /**
      * Returns logs found by matching logbook/tag and/or log names.
      *
      * @param name query to be used for matching
      * @return XmlLogs container with all found logs and their logbooks/tags
      */
     public static XmlLogs findLogsByLogbookName(String name) throws CFException {
         FindLogsQuery q = new FindLogsQuery(SearchType.TAG, name);
         XmlLogs xmlLogs = null;
 
         ArrayList<XmlLog> logs = q.executeQuery();
         if (logs != null) {
             Iterator<XmlLog> iterator = logs.iterator();
             while (iterator.hasNext()) {
                 xmlLogs.addXmlLog(iterator.next());
             }
         }
 
         return xmlLogs;
     }
 
     /**
      * Return single log found by log id.
      *
      * @param logId id to look for
      * @return XmlLog with found log and its logbooks
      * @throws CFException on SQLException
      */
     public static XmlLog findLogById(Long logId) throws CFException, UnsupportedEncodingException, NoSuchAlgorithmException {
         FindLogsQuery q = new FindLogsQuery(SearchType.LOG, logId);
         XmlLog xmlLog = null;
 
         ArrayList<XmlLog> logs = q.executeQuery();
         if (logs != null) {
             Iterator<XmlLog> iterator = logs.iterator();
             while (iterator.hasNext()) {
                 XmlLog log = iterator.next();
                 if (log.getMD5Entry().equals(CreateLogQuery.getmd5Entry(log.getId(), log))) {
                     xmlLog = log;
                 }
             }
         }
 
         return xmlLog;
     }
 
     /**
      * Return single log found by log id without checking for md5
      *
      * @param logId id to look for
      * @return XmlLog with found log and its logbooks
      * @throws CFException on SQLException
      */
     public static XmlLog findLogByIdNoMD5(Long logId) throws CFException, UnsupportedEncodingException, NoSuchAlgorithmException {
         FindLogsQuery q = new FindLogsQuery(SearchType.LOG, logId);
         XmlLog xmlLog = null;
 
         ArrayList<XmlLog> logs = q.executeQuery();
         if (logs != null) {
             Iterator<XmlLog> iterator = logs.iterator();
             while (iterator.hasNext()) {
                 xmlLog = iterator.next();
             }
         }
 
         return xmlLog;
     }
 }
