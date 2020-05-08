 package fedora.server.search;
 
 import java.io.InputStream;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.HashMap;
 import java.util.List;
 
 import fedora.server.Logging;
 import fedora.server.ReadOnlyContext;
 import fedora.server.StdoutLogging;
 import fedora.server.errors.ObjectIntegrityException;
 import fedora.server.errors.QueryParseException;
 import fedora.server.errors.RepositoryConfigurationException;
 import fedora.server.errors.ServerException;
 import fedora.server.errors.StorageDeviceException;
 import fedora.server.errors.StreamIOException;
 import fedora.server.errors.UnrecognizedFieldException;
 import fedora.server.storage.ConnectionPool;
 import fedora.server.storage.DOReader;
 import fedora.server.storage.RepositoryReader;
 import fedora.server.storage.types.DatastreamXMLMetadata;
 import fedora.server.utilities.SQLUtility;
 
 /**
  * A FieldSearch implementation that uses a relational database
  * as a backend.
  *
  * @author cwilper@cs.cornell.edu
  */ 
 public class FieldSearchSQLImpl
         extends StdoutLogging
         implements FieldSearch {
 
     private ConnectionPool m_cPool;
     private RepositoryReader m_repoReader;
     private int m_maxResults;
     private static String[] s_dbColumnNames=new String[] {"pid", "label", 
             "fType", "cModel", "state", "locker", "cDate", "mDate", "dcmDate",
             "dcTitle", "dcCreator", "dcSubject", "dcDescription", "dcPublisher",
             "dcContributor", "dcDate", "dcType", "dcFormat", "dcIdentifier",
             "dcSource", "dcLanguage", "dcRelation", "dcCoverage", "dcRights"};
     private static boolean[] s_dbColumnNumeric=new boolean[] {false, false,
             false, false, false, false, true, true, true,
             false, false, false, false, false,
             false, false, false, false, false,
             false, false, false, false, false};
             
             
     private static ReadOnlyContext s_nonCachedContext;
     static {
         HashMap h=new HashMap();
         h.put("useCachedObject", "false");
         s_nonCachedContext=new ReadOnlyContext(h);
     }
         
     public FieldSearchSQLImpl(ConnectionPool cPool, RepositoryReader repoReader, 
             int maxResults, Logging logTarget) {
         super(logTarget);
         logFinest("Entering constructor");
         m_cPool=cPool;
         m_repoReader=repoReader;
         m_maxResults=maxResults;
         logFinest("Exiting constructor");
     }
     
     private boolean isDCProp(String in) {
         for (int i=0; i<s_dbColumnNames.length; i++) {
             String n=s_dbColumnNames[i];
             if ( (n.startsWith("dc"))
                     && (n.toLowerCase().indexOf(in.toLowerCase())!=-1) ) {
                 return true;
             }
         }
         return false;
     }
     
     public void update(DOReader reader) 
             throws ServerException {
         logFinest("Entering update(DOReader)");
         String pid=reader.GetObjectPID();
         Connection conn=null;
         Statement st=null;
         try {
             conn=m_cPool.getConnection();
             String[] dbRowValues=new String[24];
             dbRowValues[0]=reader.GetObjectPID();
             String v;
             v=reader.GetObjectLabel();
             if (v!=null) v=v.toLowerCase();
             dbRowValues[1]=v;
             dbRowValues[2]=reader.getFedoraObjectType().toLowerCase();
             v=reader.getContentModelId();
             if (v!=null) v=v.toLowerCase();
             dbRowValues[3]=v;
             dbRowValues[4]=reader.GetObjectState().toLowerCase();
             v=reader.getLockingUser();
             if (v!=null) v=v.toLowerCase();
             dbRowValues[5]=v;
             Date date=reader.getCreateDate();
             if (date==null) {  // should never happen, but if it does, don't die
                 date=new Date();
             }
             dbRowValues[6]="" + date.getTime();
             date=reader.getLastModDate();
             if (date==null) {  // should never happen, but if it does, don't die
                 date=new Date();
             }
             dbRowValues[7]="" + date.getTime();
             DatastreamXMLMetadata dcmd=null;
             try {
                 dcmd=(DatastreamXMLMetadata) reader.GetDatastream("DC", null);
             } catch (ClassCastException cce) {
                 throw new ObjectIntegrityException("Object " + reader.GetObjectPID() 
                         + " has a DC datastream, but it's not inline XML.");
             }
             if (dcmd==null) {
                 logFine("Did not have DC Metadata datastream for this object.");
             } else {
                 logFine("Had DC Metadata datastream for this object.");
                 InputStream in=dcmd.getContentStream();
                 DCFields dc=new DCFields(in);
                 dbRowValues[8]="" + dcmd.DSCreateDT.getTime();
                 dbRowValues[9]=getDbValue(dc.titles()); 
                 dbRowValues[10]=getDbValue(dc.creators()); 
                 dbRowValues[11]=getDbValue(dc.subjects()); 
                 dbRowValues[12]=getDbValue(dc.descriptions()); 
                 dbRowValues[13]=getDbValue(dc.publishers()); 
                 dbRowValues[14]=getDbValue(dc.contributors()); 
                 dbRowValues[15]=getDbValue(dc.dates()); 
                 // get any dc.dates strings that are formed such that they
                 // can be treated as a timestamp
                 List wellFormedDates=null;
                 for (int i=0; i<dc.dates().size(); i++) {
                     if (i==0) {
                         wellFormedDates=new ArrayList();
                     }
                     Date p=parseDate((String) dc.dates().get(i));
                     if (p!=null) {
                         wellFormedDates.add(p);
                     }
                 }
                 if (wellFormedDates!=null && wellFormedDates.size()>0) {
                     // found at least one... so delete the existing dates
                     // in that table for this pid, then add these.
                     st=conn.createStatement();
                     st.executeUpdate("DELETE FROM dcDates WHERE pid='" + pid 
                             + "'");
                     for (int i=0; i<wellFormedDates.size(); i++) {
                         Date dt=(Date) wellFormedDates.get(i);
                         st.executeUpdate("INSERT INTO dcDates (pid, dcDate) "
                                 + "values ('" + pid + "', " 
                                 + dt.getTime() + ")");
                     }
                 }
                 dbRowValues[16]=getDbValue(dc.types()); 
                 dbRowValues[17]=getDbValue(dc.formats()); 
                 dbRowValues[18]=getDbValue(dc.identifiers()); 
                 dbRowValues[19]=getDbValue(dc.sources()); 
                 dbRowValues[20]=getDbValue(dc.languages()); 
                 dbRowValues[21]=getDbValue(dc.relations()); 
                 dbRowValues[22]=getDbValue(dc.coverages()); 
                 dbRowValues[23]=getDbValue(dc.rights()); 
             }
             logFine("Formulating SQL and inserting/updating...");
             SQLUtility.replaceInto(conn, "doFields", s_dbColumnNames,
                     dbRowValues, "pid", s_dbColumnNumeric, this);
         } catch (SQLException sqle) {
             throw new StorageDeviceException("Error attempting update of " 
                     + "object with pid '" + pid + ": " + sqle.getMessage());
         } finally {
             if (conn!=null) {
                 if (st!=null) {
                     try {
                         st.close();
                     } catch (Exception e) { }
                 }
                 m_cPool.free(conn);
             }
             logFinest("Exiting update(DOReader)");
         }
     }
     
     // delete from doFields where pid=pid, dcDates where pid=pid
     public boolean delete(String pid) 
             throws ServerException {
         logFinest("Entering delete(String)");
         Connection conn=null;
         Statement st=null;
         try {
             conn=m_cPool.getConnection();
             st=conn.createStatement();
             st.executeUpdate("DELETE FROM doFields WHERE pid='" + pid + "'");
             st.executeUpdate("DELETE FROM dcDates WHERE pid='" + pid + "'");
             return true;
         } catch (SQLException sqle) {
             throw new StorageDeviceException("Error attempting delete of " 
                     + "object with pid '" + pid + "': " 
                     + sqle.getMessage());
         } finally {
             if (conn!=null) {
                 if (st!=null) {
                     try {
                         st.close();
                     } catch (Exception e) { }
                 }
                 m_cPool.free(conn);
             }
             logFinest("Exiting delete(String)");
         }
     }
    
     public List search(String[] resultFields, String terms) 
             throws StorageDeviceException, QueryParseException, ServerException {
         Connection conn=null;
         try {
             logFinest("Entering search(String[], String)");
             if (terms.indexOf("'")!=-1) {
                 throw new QueryParseException("Query cannot contain the ' character.");
             }
             StringBuffer whereClause=new StringBuffer();
             if (!terms.equals("*") && !terms.equals("")) {
                 whereClause.append(" WHERE");
                 // formulate the where clause if the terms aren't * or ""
                 int usedCount=0;
                 boolean needsEscape=false;
                 for (int i=0; i<s_dbColumnNames.length; i++) {
                     String column=s_dbColumnNames[i];
                     // use only stringish columns in query
                     boolean use=column.indexOf("Date")==-1;
                     if (!use) {
                         if (column.equals("dcDate")) {
                             use=true;
                         }
                     }
                     if (use) {
                         if (usedCount>0) {
                             whereClause.append(" OR");
                         }
                         String qPart=toSql(column, terms);
                         if (qPart.charAt(0)==' ') {
                             needsEscape=true;
                         } else {
                             whereClause.append(" ");
                         }
                         whereClause.append(qPart);
                         usedCount++;
                     }
                 }
                 if (needsEscape) {
                     whereClause.append(" {escape '/'}");
                 }
             }
             logFinest("Doing word search using whereClause: '" 
                     + whereClause.toString() + "'");
             conn=m_cPool.getConnection();
             List ret=getObjectFields(conn, "SELECT pid FROM doFields" 
                     + whereClause.toString(), resultFields);
             return ret;
         } catch (SQLException sqle) {
             throw new StorageDeviceException("Error attempting word search: \"" 
                     + terms + "\": " + sqle.getMessage());
         } finally {
             if (conn!=null) {
                 m_cPool.free(conn);
             }
             logFinest("Exiting search(String[], String)");
         }
     }
 
 /**
  * = for dates and non-repeating fields
 
 ~ for all fields, excluding cDate mDate dcDate
 
 >, <, >=, <= for dates
 
 */
     public List search(String[] resultFields, List conditions) 
             throws ServerException {
         Connection conn=null;
         try {
             logFinest("Entering search(String[], List)");
             StringBuffer whereClause=new StringBuffer();
             boolean willJoin=false;
             if (conditions.size()>0) {
                 boolean needsEscape=false;
                 whereClause.append(" WHERE");
                 for (int i=0; i<conditions.size(); i++) {
                     Condition cond=(Condition) conditions.get(i);
                     if (i>0) {
                         whereClause.append(" AND");
                     }
                     String op=cond.getOperator().getSymbol();
                     String prop=cond.getProperty();
                     if (prop.toLowerCase().endsWith("date")) {
                         // deal with dates ... cDate mDate dcmDate date
                         if (op.equals("~")) {
                             if (prop.equals("date")) {
                                 // query for dcDate as string
                                 String sqlPart=toSql("doFields.dcDate", cond.getValue());
                                 if (sqlPart.startsWith(" ")) {
                                     needsEscape=true;
                                 } else {
                                     whereClause.append(' ');
                                 }
                                 whereClause.append(sqlPart);
                             } else {
                                 throw new QueryParseException("The ~ operator "
                                         + "cannot be used with cDate, mDate, "
                                         + "or dcmDate because they are not "
                                         + "string-valued fields.");
                             }
                         } else { // =, <, <=, >, >=
                             // property must be parsable as a date... if ok,
                             // do (cDate, mDate, dcmDate) 
                             // or (date) <- dcDate from dcDates table
                             Date dt=parseDate(cond.getValue());
                             if (dt==null) {
                                 throw new QueryParseException("When using "
                                         + "equality or inequality operators "
                                         + "with a date-based value, the date "
                                         + "must be in yyyy-MM-DD[Thh:mm:ss[Z]] "
                                         + "form.");
                             }
                             if (prop.equals("date")) {
                                 // do a left join on the dcDates table...dcDate
                                 // query will be of form: 
                                 // select pid 
                                 // from doFields 
                                 // left join dcDates on doFields.pid=dcDates.pid 
                                 // where...
                                 if (!willJoin) {
                                     willJoin=true;
                                     whereClause.insert(0, " LEFT JOIN dcDates "
                                             + "ON doFields.pid=dcDates.pid");
                                 }
                                 whereClause.append(" dcDates.dcDate" + op 
                                         + dt.getTime() );
                             } else {
                                 whereClause.append(" doFields." + prop + op
                                         + dt.getTime() );
                             }
                         }
                     } else {
                         if (op.equals("=")) {
                             if (isDCProp(prop)) {
                                 throw new QueryParseException("The = operator "
                                         + "can only be used with dates and "
                                         + "non-repeating fields.");
                             } else {
                                 // do a real equals check... do a toSql but
                                 // reject it if it uses "LIKE"
                                 String sqlPart=toSql("doFields." + prop, cond.getValue());
                                 if (sqlPart.indexOf("LIKE ")!=-1) {
                                     throw new QueryParseException("The = "
                                         + "operator cannot be used with "
                                         + "wildcards.");
                                 }
                                 if (sqlPart.startsWith(" ")) {
                                     needsEscape=true;
                                 } else {
                                     whereClause.append(' ');
                                 }
                                 whereClause.append(sqlPart);
                             }
                         } else if (op.equals("~")) {
                             if (isDCProp(prop)) {
                                 // prepend dc and caps the first char first...
                                 prop="dc" + prop.substring(0,1).toUpperCase() 
                                         + prop.substring(1);  
                             }
                             // the field name is ok, so toSql it
                             String sqlPart=toSql("doFields." + prop, 
                                     cond.getValue());
                             if (sqlPart.startsWith(" ")) {
                                 needsEscape=true;
                             } else {
                                 whereClause.append(' ');
                             }
                             whereClause.append(sqlPart);
                         } else {
                             throw new QueryParseException("Can't use >, >=, <, "
                                     + "or <= operator on a string-based field.");
                         }
                     }
                 }
                 if (needsEscape) {
                     whereClause.append(" {escape '/'}");
                 }
             }
             logFinest("Doing field search using whereClause: '" 
                     + whereClause.toString() + "'");
             conn=m_cPool.getConnection();
             List ret=getObjectFields(conn, "SELECT doFields.pid FROM doFields" 
                     + whereClause.toString(), resultFields);
             return ret;
         } catch (SQLException sqle) {
             throw new StorageDeviceException("Error from SQL DB while attempting field search: "
                     + sqle.getMessage());
         } finally {
             if (conn!=null) {
                 m_cPool.free(conn);
             }
             logFinest("Exiting search(String[], List)");
         }
     }
     
     /**
      * Get the string that should be inserted for a dublin core column,
      * given a list of values.  Turn each value to lowercase and separate them 
      * all by space characters.  If the list is empty, return null.
      */
     private static String getDbValue(List dcItem) {
         if (dcItem.size()==0) {
             return null;
         }
         StringBuffer out=new StringBuffer();
         for (int i=0; i<dcItem.size(); i++) {
             String val=(String) dcItem.get(i);
             out.append(" ");
             out.append(val.toLowerCase());
         }
         out.append(" ");
         return out.toString();
     }
 
     /**
      * Perform the given query for 'pid' using the given connection
      * and return the result as a List of ObjectFields objects
      * with resultFields populated.  The list will have a maximum size
      * of m_maxResults.
      */
     private List getObjectFields(Connection conn, String query,
             String[] resultFields) 
             throws SQLException, UnrecognizedFieldException, 
             ObjectIntegrityException, ServerException {
         Statement st=null;
         try {
             ArrayList fields=new ArrayList();
             st=conn.createStatement();
             ResultSet results=st.executeQuery(query);
             boolean tooMany=false;
             int numResults=0;
             while (results.next() && !tooMany) {
                 String pid=results.getString("pid");
                 fields.add(getObjectFields(pid, resultFields));
                 numResults++;
                 if (numResults>m_maxResults) {
                     tooMany=true;
                 }
             }
             return fields;
         } finally {
             if (st!=null) {
                 try {
                     st.close();
                 } catch (Exception e) { }
             }
         }
     }
 
     /**
      * For the given pid, get a reader on the object from the repository
      * and return an ObjectFields object with resultFields fields populated.
      */
     private ObjectFields getObjectFields(String pid, String[] resultFields) 
             throws UnrecognizedFieldException, ObjectIntegrityException,
             RepositoryConfigurationException, StreamIOException, 
             ServerException {
         DOReader r=m_repoReader.getReader(s_nonCachedContext, pid);
         ObjectFields f;
         // If there's a DC record available, use SAX to parse the most 
         // recent version of it into f.
         DatastreamXMLMetadata dcmd=null;
         try {
             dcmd=(DatastreamXMLMetadata) r.GetDatastream("DC", null);
         } catch (ClassCastException cce) {
             throw new ObjectIntegrityException("Object " + r.GetObjectPID() 
                     + " has a DC datastream, but it's not inline XML.");
         }
         if (dcmd!=null) {
             f=new ObjectFields(resultFields, dcmd.getContentStream());
             // add dcmDate if wanted
             for (int i=0; i<resultFields.length; i++) {
                 if (resultFields[i].equals("dcmDate")) {
                     f.setDCMDate(dcmd.DSCreateDT);
                 }
             }
         } else {
             f=new ObjectFields();
         }
         // add non-dc values from doReader for the others in resultFields[]
         for (int i=0; i<resultFields.length; i++) {
             String n=resultFields[i];
             if (n.equals("pid")) {
                 f.setPid(pid);
             }
             if (n.equals("label")) {
                 f.setLabel(r.GetObjectLabel());
             }
             if (n.equals("fType")) {
                 f.setFType(r.getFedoraObjectType());
             }
             if (n.equals("cModel")) {
                 f.setCModel(r.getContentModelId());
             }
             if (n.equals("state")) {
                 f.setState(r.GetObjectState());
             }
             if (n.equals("locker")) {
                 f.setLocker(r.getLockingUser());
             }
             if (n.equals("cDate")) {
                 f.setCDate(r.getCreateDate());
             }
             if (n.equals("mDate")) {
                 f.setMDate(r.getLastModDate());
             }
         }
         return f;
     }
     
     /**
      * Attempt to parse the given string of form: yyyy-MM-dd[Thh:mm:ss[Z]] 
      * as a Date.  If the string is not of that form, return null.
      */
     private static Date parseDate(String str) {
         if (str.indexOf("T")!=-1) {
             try {
                 return new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss").parse(str);
             } catch (ParseException pe) {
                 try {
                     return new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'").parse(str);
                 } catch (ParseException pe2) {
                     return null;
                 }
             }
         } else {
             try {
                 return new SimpleDateFormat("yyyy-MM-dd").parse(str);
             } catch (ParseException pe3) {
                 return null;
             }
         }
         
     }
     
     /**
      * Return a condition suitable for a SQL WHERE clause, given a column
      * name and a string with a possible pattern (using * and ? wildcards).
      * If the string has any characters that need to be escaped, it will
      * begin with a space, indicating to the caller that the entire WHERE
      * clause should end with " {escape '/'}".
      */
     public static String toSql(String name, String in) {
         in=in.toLowerCase();
        if (name.startsWith("dc")) {
             StringBuffer newIn=new StringBuffer();
             if (!in.startsWith("*")) {
                 newIn.append("* ");
             }
             newIn.append(in);
             if (!in.endsWith("*")) {
                 newIn.append(" *");
             }
             in=newIn.toString();
         }
         if (in.indexOf("\\")!=-1) {
             // has one or more escapes, un-escape and translate
             StringBuffer out=new StringBuffer();
             out.append("\'");
             boolean needLike=false;
             boolean needEscape=false;
             boolean lastWasEscape=false;
             for (int i=0; i<in.length(); i++) {
                 char c=in.charAt(i);
                 if ( (!lastWasEscape) && (c=='\\') ) {
                     lastWasEscape=true;
                 } else {
                     char nextChar='!';
                     boolean useNextChar=false;
                     if (!lastWasEscape) {
                         if (c=='?') {
                             out.append('_');
                             needLike=true;
                         } else if (c=='*') {
                             out.append('%');
                             needLike=true;
                         } else {
                             nextChar=c;
                             useNextChar=true;
                         }
                     } else {
                         nextChar=c;
                         useNextChar=true;
                     }
                     if (useNextChar) {
                         if (nextChar=='\"') {
                             out.append("\\\"");
                             needEscape=true;
                         } else if (nextChar=='\'') {
                             out.append("\\\'");
                             needEscape=true;
                         } else if (nextChar=='%') {
                             out.append("\\%");
                             needEscape=true;
                         } else if (nextChar=='_') {
                             out.append("\\_");
                             needEscape=true;
                         } else {
                             out.append(nextChar);
                         }
                     }
                     lastWasEscape=false;
                 }
             }
             out.append("\'");
             if (needLike) {
                 out.insert(0, " LIKE ");
             } else {
                 out.insert(0, " = ");
             }
             out.insert(0, name);
             if (needEscape) {
                 out.insert(0, ' ');
             }
             return out.toString();
         } else {
             // no escapes, just translate if needed
             StringBuffer out=new StringBuffer();
             out.append("\'");
             boolean needLike=false;
             boolean needEscape=false;
             for (int i=0; i<in.length(); i++) {
                 char c=in.charAt(i);
                 if (c=='?') {
                     out.append('_');
                     needLike=true;
                 } else if (c=='*') {
                     out.append('%');
                     needLike=true;
                 } else if (c=='\"') {
                     out.append("\\\"");
                     needEscape=true;
                 } else if (c=='\'') {
                     out.append("\\\'");
                     needEscape=true;
                 } else if (c=='%') {
                     out.append("\\%");
                     needEscape=true;
                 } else if (c=='_') {
                     out.append("\\_");
                     needEscape=true;
                 } else {
                     out.append(c);
                 }
             }
             out.append("\'");
             if (needLike) {
                 out.insert(0, " LIKE ");
             } else {
                 out.insert(0, " = ");
             }
             out.insert(0, name);
             if (needEscape) {
                 out.insert(0, ' ');
             }
             return out.toString();
         }
     }
 
 }
