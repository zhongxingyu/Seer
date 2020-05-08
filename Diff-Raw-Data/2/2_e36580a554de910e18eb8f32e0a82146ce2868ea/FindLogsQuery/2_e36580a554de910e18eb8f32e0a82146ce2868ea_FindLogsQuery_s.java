 /*
  * Copyright (c) 2010 Brookhaven National Laboratory
  * Copyright (c) 2010 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
  * Subject to license terms and conditions.
  */
 package edu.msu.nscl.olog;
 
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.Multimap;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
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
     private List<String> tag_matches = new ArrayList();
     private List<String> tag_patterns = new ArrayList();
 
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
      * Creates a new instance of FindLogsQuery, sorting the query parameters.
      * Logbook matches and tag string matches go to the first inner query,
      * tag pattern matches are queried separately,
      * name matches go to the outer query.
      * Logbook and tag names are converted to lowercase before being matched.
      *
      * @param matches  the map of matches to apply
      */
     private FindLogsQuery(MultivaluedMap<String, String> matches) {
         for (Map.Entry<String, List<String>> match : matches.entrySet()) {
             String key = match.getKey().toLowerCase();
             if (key.equals("search")) {
                 log_matches.addAll(match.getValue());
             } else if (key.equals("tag")) {
                 addTagMatches(match.getValue());
             } else if (key.equals("logbook")){
                 addTagMatches(match.getValue());
             } else if (key.equals("page")) {
                 logPaginate_matches.putAll(key,match.getValue());
             } else if (key.equals("limit")) {
                 logPaginate_matches.putAll(key,match.getValue());
             } else if (key.equals("start")){
                 date_matches.putAll(key,match.getValue());
             } else if (key.equals("end")){
                 date_matches.putAll(key,match.getValue());
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
 
     /**
      * Creates and executes the logbook and tag string match subquery using GROUP.
      *
      * @param con connection to use
      * @return a set of log ids that match
      */
     //TODO:  need to add search params like olog; logs between dates, search all fields, files, etc.
     private Set<Long> getIdsFromLogbookAndTagMatch(Connection con) throws CFException {
         StringBuilder query = new StringBuilder("SELECT log.*, t.*, level.name, "+
                                                 "(SELECT logs.created FROM logs WHERE log.parent_id=logs.id) as parent_created, "+
                                                 "(SELECT COUNT(id) FROM logs WHERE parent_id=log.parent_id GROUP BY parent_id) as children "+
                                                 "FROM `logs` as log "+
                                                 "LEFT JOIN `logs` as parent ON log.id = parent.parent_id "+
                                                 "LEFT JOIN logs_logbooks as lt ON log.id = lt.log_id "+
                                                 "LEFT JOIN logbooks as t ON lt.logbook_id = t.id "+
                                                 "LEFT JOIN levels as level ON log.level_id = level.id "+
                                                 "LEFT JOIN statuses as status ON log.status_id = status.id "+
                                                 "LEFT JOIN statuses as ltstatus ON lt.status_id = status.id "+
                                                 "LEFT JOIN statuses as tstatus ON t.status_id = status.id "+
                                                 "WHERE (parent.parent_id IS NULL and log.parent_id IS NULL "+
                                                 "OR log.id IN (SELECT MAX(logs.id) FROM logs WHERE logs.parent_id=log.parent_id)) "+
                                                 "AND status.name = 'Active' "+
                                                 "AND ltstatus.name = 'Active' "+
                                                 "AND tstatus.name = 'Active' AND");
         Set<Long> ids = new HashSet<Long>();           // set of matching log ids
         List<String> params = new ArrayList<String>(); // parameter list for this query
 
         for (String tag : tag_matches) {
             params.add(convertFileGlobToSQLPattern(tag).toLowerCase());
             query.append(" LOWER(t.name) LIKE ? OR");
         }
 
         query.replace(query.length() - 2, query.length(),
                 " GROUP BY lt.id HAVING COUNT(log.id) = ? ORDER BY lt.log_id DESC, ifnull(parent_created,log.created) DESC");
 
         try {
             PreparedStatement ps = con.prepareStatement(query.toString());
             int i = 1;
             for (String p : params) {
                 ps.setString(i++, p);
             }
             ps.setLong(i++, tag_matches.size());
             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
                 // Add key to list of matching log ids
                 ids.add(rs.getLong(1));
             }
         } catch (SQLException e) {
             throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                     "SQL Exception while getting log ids in logbook match query: "+ query.toString(), e);
         }
         return ids;
     }
  /**
      * Creates and executes the properties string match subquery using GROUP.
      *
      * @param con connection to use
      * @return a set of log ids that match
      */
     private Set<Long> getIdsFromPropertiesMatch(Connection con) throws CFException {
         StringBuilder query = new StringBuilder("SELECT log.*, t.*, level.name, "+
                                                 "(SELECT logs.created FROM logs WHERE log.parent_id=logs.id) as parent_created, "+
                                                 "(SELECT COUNT(id) FROM logs WHERE parent_id=log.parent_id GROUP BY parent_id) as children "+
                                                 "FROM `logs` as log "+
                                                 "LEFT JOIN `logs` as parent ON log.id = parent.parent_id "+
                                                 "LEFT JOIN logs_logbooks as lt ON log.id = lt.log_id "+
                                                 "LEFT JOIN logbooks as t ON lt.logbook_id = t.id "+
                                                 "LEFT JOIN levels as level ON log.level_id = level.id "+
                                                 "LEFT JOIN statuses as status ON log.status_id = status.id "+
                                                 "LEFT JOIN statuses as ltstatus ON lt.status_id = status.id "+
                                                 "LEFT JOIN statuses as tstatus ON t.status_id = status.id "+
                                                 "LEFT JOIN properties as prop ON log.id = prop.log_id "+
                                                 "WHERE (parent.parent_id IS NULL and log.parent_id IS NULL "+
                                                 "OR log.id IN (SELECT MAX(logs.id) FROM logs WHERE logs.parent_id=log.parent_id)) "+
                                                 "AND status.name = 'Active' "+
                                                 "AND ltstatus.name = 'Active' "+
                                                 "AND tstatus.name = 'Active' AND (");
         Set<Long> ids = new HashSet<Long>();           // set of matching log ids
         List<String> params = new ArrayList<String>(); // parameter list for this query
 
         for (Map.Entry<String, Collection<String>> match : value_matches.asMap().entrySet()) {
             StringBuilder valueList = new StringBuilder("prop.value LIKE ");
             params.add(match.getKey().toLowerCase());
             for (String value : match.getValue()) {
                 valueList.append("? OR prop.value LIKE ");
                 params.add(convertFileGlobToSQLPattern(value));
             }
             query.append("(LOWER(prop.name) = ? AND ( "
                     + valueList.substring(0, valueList.length() - 20) + ")) OR ");
         }
 
 
         query.replace(query.length() - 3, query.length(),
                 ") GROUP BY log.id HAVING COUNT(log.id) = ? ORDER BY lt.log_id DESC, ifnull(parent_created,log.created) DESC");
 
         try {
             PreparedStatement ps = con.prepareStatement(query.toString());
             int i = 1;
             for (String p : params) {
                 ps.setString(i++, p);
             }
             ps.setLong(i++, value_matches.asMap().size());
             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
                 // Add key to list of matching log ids
                 ids.add(rs.getLong(1));
             }
         } catch (SQLException e) {
             throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                     "SQL Exception while getting log ids in logbook match query: "+ query.toString(), e);
         }
         return ids;
     }
 
     /**
      * Creates and executes the tag string match subquery using GROUP.
      *
      * @param con connection to use
      * @return a set of log ids that match
      */
     private Set<Long> getIdsFromTagMatch(Connection con, String match) throws CFException {
         String query = "SELECT log.id, "+
                        "(SELECT logs.created FROM logs WHERE log.parent_id=logs.id) as parent_created "+
                        "FROM `logs` as log "+
                        "LEFT JOIN `logs` as parent ON log.id = parent.parent_id "+
                        "LEFT JOIN logs_logbooks as lt ON log.id = lt.log_id "+
                        "LEFT JOIN logbooks as t ON lt.logbook_id = t.id "+
                        "LEFT JOIN statuses as status ON log.status_id = status.id "+
                        "LEFT JOIN statuses as ltstatus ON lt.status_id = status.id "+
                        "LEFT JOIN statuses as tstatus ON t.status_id = status.id "+
                        "WHERE (parent.parent_id IS NULL and log.parent_id IS NULL "+
                        "OR log.id IN (SELECT MAX(logs.id) FROM logs WHERE logs.parent_id=log.parent_id)) "+
                        "AND t.name LIKE ? "+
                        "AND status.name = 'Active' "+
                        "AND ltstatus.name = 'Active' "+
                        "AND tstatus.name = 'Active' "+
                        "GROUP BY lt.id ORDER BY lt.log_id, ifnull(parent_created,log.created) DESC, t.name";
         Set<Long> ids = new HashSet<Long>();
 
         try {
             PreparedStatement ps = con.prepareStatement(query);
             ps.setString(1, convertFileGlobToSQLPattern(match));
             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
                 ids.add(rs.getLong(1));
             }
         } catch (SQLException e) {
             throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                     "SQL Exception while getting log ids in tag match query", e);
         }
         return ids;
     }
 
     /**
      * Creates and executes the pagination subquery using GROUP BY.
      *
      * @param con connection to use
      * @return a set of log ids that match
      */
     Set<Long> getIdsFromPagination(Connection con) throws CFException {
         StringBuilder query = new StringBuilder("SELECT log.*, t.*, level.name, prop.name, prop.value, "+
                                                 "(SELECT logs.created FROM logs WHERE log.parent_id=logs.id) as parent_created, "+
                                                 "(SELECT COUNT(id) FROM logs WHERE parent_id=log.parent_id GROUP BY parent_id) as children "+
                                                 "FROM `logs` as log "+
                                                 "LEFT JOIN `logs` as parent ON log.id = parent.parent_id "+
                                                 "LEFT JOIN logs_logbooks as lt ON log.id = lt.log_id "+
                                                 "LEFT JOIN logbooks as t ON lt.logbook_id = t.id "+
                                                 "LEFT JOIN levels as level ON log.level_id = level.id "+
                                                 "LEFT JOIN statuses as status ON log.status_id = status.id "+
                                                 "LEFT JOIN statuses as ltstatus ON lt.status_id = status.id "+
                                                 "LEFT JOIN statuses as tstatus ON t.status_id = status.id "+
                                                 "LEFT JOIN properties as prop ON log.id = prop.log_id "+
                                                 "WHERE (parent.parent_id IS NULL and log.parent_id IS NULL "+
                                                 "OR log.id IN (SELECT MAX(logs.id) FROM logs WHERE logs.parent_id=log.parent_id)) "+
                                                 "AND status.name = 'Active' "+
                                                 "AND ltstatus.name = 'Active' "+
                                                 "AND tstatus.name = 'Active' ");
         List<Long> id_params = new ArrayList<Long>();       // parameter lists for the outer query
         List<String> name_params = new ArrayList<String>();
         List<Long> paginate_params = new ArrayList<Long>();
         List<Long> date_params = new ArrayList<Long>();
         Set<Long> tag_result = new HashSet<Long>();
         Set<Long> value_result = new HashSet<Long>();
         Set<Long> returnIds = new HashSet<Long>();
 
         if (!tag_matches.isEmpty()) {
             Set<Long> ids = getIdsFromLogbookAndTagMatch(con);
             if (ids.isEmpty()) {
                 return null;
             }
             tag_result.addAll(ids);
         }
 
         if (!value_matches.isEmpty()) {
             Set<Long> ids = getIdsFromPropertiesMatch(con);
             if (ids.isEmpty()) {
                 return null;
             }
             value_result.addAll(ids);
         }
 
         if (!tag_patterns.isEmpty()) {
             for (String p : tag_patterns) {
                 Set<Long> ids = getIdsFromTagMatch(con, p);
                 if (ids.isEmpty()) {
                     return null;
                 }
                 if (tag_result.isEmpty()) {
                     tag_result.addAll(ids);
                 } else {
                     tag_result.retainAll(ids);
                     if (tag_result.isEmpty()) {
                         return null;
                     }
                 }
             }
         }
         if (!date_matches.isEmpty()) {
             String start = null, end = null;
             for (Map.Entry<String, Collection<String>> match : date_matches.asMap().entrySet()) {
                 if (match.getKey().toLowerCase().equals("start")){
                     start = match.getValue().iterator().next();
                 }
                 if (match.getKey().toLowerCase().equals("end")){
                     end = match.getValue().iterator().next();
                 }
             }
             if(start!=null && end!=null){
                 query.append(" AND log.created > FROM_UNIXTIME(?) AND log.created <= FROM_UNIXTIME(?) ");
                 date_params.add(Long.valueOf(start));
                 date_params.add(Long.valueOf(end));
                 // needs to be Long not string!!!!
             }
         }
         if (!logId_matches.isEmpty()) {
             query.append(" AND (log.id IN (");
             for (long i : logId_matches) {
                 query.append("?,");
                 id_params.add(i);
             }
             query.replace(query.length() - 1, query.length(), ") OR log.parent_id IN (");
             for (long i : logId_matches) {
                 query.append("?,");
                 id_params.add(i);
             }
             query.replace(query.length() - 1, query.length(), ")) ");
         }
         if (!tag_result.isEmpty()) {
             query.append(" AND (log.id IN (");
             for (long i : tag_result) {
                 query.append("?,");
                 id_params.add(i);
             }
             query.replace(query.length() - 1, query.length(), ") OR log.parent_id IN (");
             for (long i : tag_result) {
                 query.append("?,");
                 id_params.add(i);
             }
             query.replace(query.length() - 1, query.length(), ")) ");
         }
         if (!value_result.isEmpty()) {
             query.append(" AND (log.id IN (");
             for (long i : value_result) {
                 query.append("?,");
                 id_params.add(i);
             }
             query.replace(query.length() - 1, query.length(), ") OR log.parent_id IN (");
             for (long i : value_result) {
                 query.append("?,");
                 id_params.add(i);
             }
             query.replace(query.length() - 1, query.length(), ")) ");
         }
 
         if (!log_matches.isEmpty()) {
             query.append(" AND (");
             for (String value : log_matches) {
                 query.append("log.subject LIKE ? OR ");
                 name_params.add(convertFileGlobToSQLPattern(value));
                 query.append("log.description LIKE ? OR ");
                 name_params.add(convertFileGlobToSQLPattern(value));
                 query.append("log.owner LIKE ? OR ");
                 name_params.add(convertFileGlobToSQLPattern(value));
             }
             query.replace(query.length() - 4, query.length(), ")");
         }
 
         query.append(" GROUP BY lt.log_id ORDER BY lt.log_id DESC, ifnull(parent_created,log.created) DESC, t.name");
 
         if (!logPaginate_matches.isEmpty()) {
             String limit = null, offset = null;
             for (Map.Entry<String, Collection<String>> match : logPaginate_matches.asMap().entrySet()) {
                 if (match.getKey().toLowerCase().equals("limit")){
                     limit = match.getValue().iterator().next();
                 }
                 if (match.getKey().toLowerCase().equals("page")){
                     offset = match.getValue().iterator().next();
                 }
             }
             if(limit!=null && offset!=null){
                 Long longOffset = Long.valueOf(offset)*Long.valueOf(limit)-Long.valueOf(limit);
                 query.append(" LIMIT ? OFFSET ?");
                 paginate_params.add(Long.valueOf(limit));
                 paginate_params.add(longOffset);
             }
         }
         try {
             PreparedStatement ps = con.prepareStatement(query.toString());
             int i = 1;
             for (long q : date_params) {
                 ps.setLong(i++, q);
             }
             for (long p : id_params) {
                 ps.setLong(i++, p);
             }
             for (String s : name_params) {
                 ps.setString(i++, s);
             }
             for (long l : paginate_params) {
                 ps.setLong(i++, l);
             }
             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
                 returnIds.add(rs.getLong(1));
             }
           //  StringBuilder returnString = new StringBuilder("{");
           //  Log logger = LogFactory.getLog(FindLogsQuery.class);
           //  for (long ir : returnIds) {
           //      returnString.append(String.valueOf(ir)).append(",");
           //  }
           //  returnString.append("}");
           //  logger.debug("SQL Result :"+returnString.toString());
 
         } catch (SQLException e) {
             
             throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                     "SQL Exception in logs query "+query.toString(), e);
         }
         return returnIds;
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
     private ResultSet executeQuery(Connection con) throws CFException {
         StringBuilder query = new StringBuilder("SELECT log.*, t.*, level.name, prop.name, prop.value, "+
                                                 "(SELECT logs.created FROM logs WHERE log.parent_id=logs.id) as parent_created, "+
                                                 "(SELECT COUNT(id) FROM logs WHERE parent_id=log.parent_id GROUP BY parent_id) as children "+
                                                 "FROM `logs` as log "+
                                                 "LEFT JOIN `logs` as parent ON log.id = parent.parent_id "+
                                                 "LEFT JOIN logs_logbooks as lt ON log.id = lt.log_id "+
                                                 "LEFT JOIN logbooks as t ON lt.logbook_id = t.id "+
                                                 "LEFT JOIN levels as level ON log.level_id = level.id "+
                                                 "LEFT JOIN statuses as status ON log.status_id = status.id "+
                                                 "LEFT JOIN statuses as ltstatus ON lt.status_id = status.id "+
                                                 "LEFT JOIN statuses as tstatus ON t.status_id = status.id "+
                                                 "LEFT JOIN properties as prop ON log.id = prop.log_id "+
                                                 "WHERE (parent.parent_id IS NULL and log.parent_id IS NULL "+
                                                 "OR log.id IN (SELECT MAX(logs.id) FROM logs WHERE logs.parent_id=log.parent_id)) "+
                                                 "AND status.name = 'Active' "+
                                                 "AND ltstatus.name = 'Active' "+
                                                 "AND tstatus.name = 'Active' ");
         List<Long> id_params = new ArrayList<Long>();
         Set<Long> paginate_result = new HashSet<Long>();
 
         Set<Long> ids = getIdsFromPagination(con);
        if (ids.isEmpty()) {
               return null;
         } else {
              paginate_result.addAll(ids);
         }
         if (!paginate_result.isEmpty()) {
             query.append(" AND (log.id IN (");
             for (long i : paginate_result) {
                 query.append("?,");
                 id_params.add(i);
             }
             query.replace(query.length() - 1, query.length(), ") OR log.parent_id IN (");
             for (long i : paginate_result) {
                 query.append("?,");
                 id_params.add(i);
             }
             query.replace(query.length() - 1, query.length(), ")) ");
         }
 
         query.append(" ORDER BY lt.log_id DESC, ifnull(parent_created,log.created) DESC, t.name");
 
         try {
             PreparedStatement ps = con.prepareStatement(query.toString());
             int i = 1;
             for (long p : id_params) {
                 ps.setLong(i++, p);
             }
 
             return ps.executeQuery();
         } catch (SQLException e) {
             throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                     "SQL Exception in logs query "+query.toString(), e);
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
             StringBuffer rep = new StringBuffer();
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
      * Adds a logbook or tag to an XmlLog.
      *
      */
     private static void addLogbook(XmlLog c, ResultSet rs) throws SQLException {
         if (rs.getString("t.name") != null) {
             if (rs.getBoolean("is_tag")) {
                 c.addXmlTag(new XmlTag(rs.getString("t.name")));
             } else {
                 c.addXmlLogbook(new XmlLogbook(rs.getString("t.name"),
                         rs.getString("t.owner")));
             }
         }
     }
 
     /**
      * Adds a property to an XmlLog.
      *
      */
     private static void addProperty(XmlLog c, HashMap<String, String> properties) throws SQLException {
         if (!properties.isEmpty()) {
                 for( Map.Entry entry : properties.entrySet()){
                     if(entry.getKey() != null || entry.getKey() != "")
                         c.addXmlProperty(new XmlProperty(entry.getKey().toString(),
                                                  entry.getValue().toString()));
             }
         }
             }
     /**
      * Finds logs by matching logbook/tag values and/or log and/or tag names.
      *
      * @param matches MultiMap of query parameters
      * @return XmlLogs container with all found logs and their logbooks/tags
      */
     public static XmlLogs findLogsByMultiMatch(MultivaluedMap<String, String> matches) throws CFException {
         FindLogsQuery q = new FindLogsQuery(matches);
         XmlLogs xmlLogs = new XmlLogs();
         XmlLog xmlLog = null;
         try {
             ResultSet rs = q.executeQuery(DbConnection.getInstance().getConnection());
             Long lastlog = 0L;
             Long lastlogp = 0L;
             Long lastlogl = 0L;
             String lastlogbook = null;
             HashMap<String, String> properties = new HashMap();
             if (rs != null) {
                 while (rs.next()) {
                     Long thislog = rs.getLong("log.id");
                     String thislogbook = rs.getString("t.name");
                     if(rs.getString("prop.name") != null)
                         properties.put(rs.getString("prop.name"), rs.getString("prop.value"));
 
                     if (!thislog.equals(lastlog) || rs.isFirst()) {
                         if (rs.getLong("log.parent_id")==0L || rs.getLong("log.id")==rs.getLong("log.parent_id")) {
                             xmlLog = new XmlLog(thislog, rs.getString("log.owner"));
                             xmlLog.setCreatedDate(rs.getTimestamp("log.created"));
                         } else {
                             xmlLog = new XmlLog(rs.getLong("log.parent_id"), rs.getString("log.owner"));
                             xmlLog.setVersion(rs.getInt("children"));
                             xmlLog.setCreatedDate(rs.getTimestamp("parent_created"));
                             xmlLog.setModifiedDate(rs.getTimestamp("log.created"));
                         }
                         xmlLog.setSubject(rs.getString("subject"));
                         xmlLog.setDescription(rs.getString("description"));
                         xmlLog.setLevel(rs.getString("level.name"));
                         xmlLogs.addXmlLog(xmlLog);
                         lastlog = thislog;
                     }
                     if(!thislog.equals(lastlogp) && !rs.isFirst() ){
                         addProperty(xmlLog,properties);
                         properties.clear();
                         lastlogp = thislog;
                     }
                     if (!thislog.equals(lastlogl) || !thislogbook.equals(lastlogbook) || rs.isFirst()) {
                         addLogbook(xmlLog, rs);
                         lastlogbook = thislogbook;
                         lastlogl = thislog;
                 }
             }
 
             }
             return xmlLogs;
         } catch (SQLException e) {
             throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                     "SQL Exception while parsing result of find logs request", e);
         }
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
         XmlLog xmlLog = null;
         try {
             ResultSet rs = q.executeQuery(DbConnection.getInstance().getConnection());
 
             Long lastlog = 0L;
             Long lastlogp = 0L;
             Long lastlogl = 0L;
             String lastlogbook = null;
             HashMap<String, String> properties = new HashMap();
             if (rs != null) {
                 xmlLogs = new XmlLogs();
                 while (rs.next()) {
                     Long thislog = rs.getLong("log.id");
                     String thislogbook = rs.getString("t.name");
                     if(rs.getString("prop.name") != null)
                         properties.put(rs.getString("prop.name"), rs.getString("prop.value"));
                     if (!thislog.equals(lastlog) || rs.isFirst()) {
                         if (rs.getLong("log.parent_id")==0L || rs.getLong("log.id")==rs.getLong("log.parent_id")) {
                             xmlLog = new XmlLog(thislog, rs.getString("log.owner"));
                             xmlLog.setCreatedDate(rs.getTimestamp("log.created"));
                         } else {
                             xmlLog = new XmlLog(rs.getLong("log.parent_id"), rs.getString("log.owner"));
                             xmlLog.setVersion(rs.getInt("children"));
                             xmlLog.setCreatedDate(rs.getTimestamp("parent_created"));
                             xmlLog.setModifiedDate(rs.getTimestamp("log.created"));
                         }
                         xmlLog.setSubject(rs.getString("subject"));
                         xmlLog.setDescription(rs.getString("description"));
                         xmlLog.setLevel(rs.getString("level.name"));
                         xmlLogs.addXmlLog(xmlLog);
                         lastlog = thislog;
                     }
                     if(!thislog.equals(lastlogp) && !rs.isFirst() ){
                         addProperty(xmlLog,properties);
                         properties.clear();
                         lastlogp = thislog;
                     }
                     if (!thislog.equals(lastlogl) || !thislogbook.equals(lastlogbook) || rs.isFirst()) {
                         addLogbook(xmlLog, rs);
                         lastlogbook = thislogbook;
                         lastlogl = thislog;
                 }
             }
             }
             return xmlLogs;
         } catch (SQLException e) {
             throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                     "SQL Exception while parsing result of find logs by logbook name request", e);
         }
     }
 
     /**
      * Return single log found by log id.
      *
      * @param logId id to look for
      * @return XmlLog with found log and its logbooks
      * @throws CFException on SQLException
      */
     public static XmlLog findLogById(Long logId) throws CFException {
         FindLogsQuery q = new FindLogsQuery(SearchType.LOG, logId);
         XmlLog xmlLog = null;
         try {
             ResultSet rs = q.executeQuery(DbConnection.getInstance().getConnection());
             String lastlogbook = null;
             HashMap<String, String> properties = new HashMap();
             if (rs != null) {
                 while (rs.next()) {
                     Long thislog = rs.getLong("log.id");
                     String thislogbook = rs.getString("t.name");
                     if(rs.getString("prop.name") != null)
                         properties.put(rs.getString("prop.name"), rs.getString("prop.value"));
                     if (rs.isFirst()) {
                         if (rs.getLong("log.parent_id")==0 || rs.getLong("log.id")==rs.getLong("log.parent_id")) {
                             xmlLog = new XmlLog(thislog, rs.getString("log.owner"));
                             xmlLog.setCreatedDate(rs.getTimestamp("log.created"));
                         } else {
                             xmlLog = new XmlLog(rs.getLong("log.parent_id"), rs.getString("log.owner"));
                             xmlLog.setVersion(rs.getInt("children"));
                             xmlLog.setCreatedDate(rs.getTimestamp("parent_created"));
                             xmlLog.setModifiedDate(rs.getTimestamp("log.created"));
                         }
                         xmlLog.setSubject(rs.getString("subject"));
                         xmlLog.setDescription(rs.getString("description"));
                         xmlLog.setLevel(rs.getString("level.name"));
                     }
                     if(rs.isLast() ){
                         addProperty(xmlLog,properties);
                         properties.clear();
                     }
                     if (!thislogbook.equals(lastlogbook) || rs.isFirst()) {
                         addLogbook(xmlLog, rs);
                         lastlogbook = thislogbook;
                     }
                 }
             }
         } catch (SQLException e) {
             throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                     "SQL Exception while parsing result of single log search request", e);
         }
         return xmlLog;
     }
 }
