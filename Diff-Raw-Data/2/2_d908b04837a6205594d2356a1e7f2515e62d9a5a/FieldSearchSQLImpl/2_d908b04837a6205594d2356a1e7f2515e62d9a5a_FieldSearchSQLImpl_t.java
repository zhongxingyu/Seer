 package fedora.server.search;
 
 import java.io.InputStream;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import fedora.server.Logging;
 import fedora.server.ReadOnlyContext;
 import fedora.server.StdoutLogging;
 import fedora.server.errors.ObjectIntegrityException;
 import fedora.server.errors.QueryParseException;
 import fedora.server.errors.RepositoryConfigurationException;
 import fedora.server.errors.ServerException;
 import fedora.server.errors.StorageDeviceException;
 import fedora.server.errors.StreamIOException;
 import fedora.server.errors.UnknownSessionTokenException;
 import fedora.server.errors.UnrecognizedFieldException;
 import fedora.server.storage.ConnectionPool;
 import fedora.server.storage.BMechReader;
 import fedora.server.storage.DOReader;
 import fedora.server.storage.RepositoryReader;
 import fedora.server.storage.types.DatastreamXMLMetadata;
 import fedora.server.storage.types.Disseminator;
 import fedora.server.utilities.DateUtility;
 import fedora.server.utilities.SQLUtility;
 
 /**
  *
  * <p><b>Title:</b> FieldSearchSQLImpl.java</p>
  * <p><b>Description:</b> A FieldSearch implementation that uses a relational
  * database as a backend.</p>
  *
  * -----------------------------------------------------------------------------
  *
  * <p><b>License and Copyright: </b>The contents of this file are subject to the
  * Mozilla Public License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of the License
  * at <a href="http://www.mozilla.org/MPL">http://www.mozilla.org/MPL/.</a></p>
  *
  * <p>Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
  * the specific language governing rights and limitations under the License.</p>
  *
  * <p>The entire file consists of original code.  Copyright &copy; 2002, 2003 by The
  * Rector and Visitors of the University of Virginia and Cornell University.
  * All rights reserved.</p>
  *
  * -----------------------------------------------------------------------------
  *
  * @author cwilper@cs.cornell.edu
  * @version $Id$
  */
 public class FieldSearchSQLImpl
         extends StdoutLogging
         implements FieldSearch {
 
     private ConnectionPool m_cPool;
     private RepositoryReader m_repoReader;
     private int m_maxResults;
     private int m_maxSecondsPerSession;
     public static String[] DB_COLUMN_NAMES=new String[] {"pid", "label",
             "fType", "cModel", "state", "ownerId", "cDate", "mDate", "dcmDate",
             "bDef", "bMech", "dcTitle", "dcCreator", "dcSubject", "dcDescription",
             "dcPublisher", "dcContributor", "dcDate", "dcType", "dcFormat",
             "dcIdentifier", "dcSource", "dcLanguage", "dcRelation", "dcCoverage", "dcRights"};
     private static boolean[] s_dbColumnNumeric=new boolean[] {false, false,
             false, false, false, false, true, true, true, false, false,
             false, false, false, false, false,
             false, false, false, false, false,
             false, false, false, false, false};
 
     // a hash of token-keyed FieldSearchResultSQLImpls
     private HashMap m_currentResults=new HashMap();
 
     private static ReadOnlyContext s_nonCachedContext;
     static {
         HashMap h=new HashMap();
         h.put("useCachedObject", "false");
         s_nonCachedContext=new ReadOnlyContext(h);
     }
 
     /**
      * Construct a FieldSearchSQLImpl.
      *
      * @param cPool the ConnectionPool with connections to the db containing
      *        the fields
      * @param repoReader the RepositoryReader to use when getting the original
      *        values of the fields
      * @param maxResults the maximum number of results to return at a time,
      *        regardless of what the user might request
      * @param logTarget where to send log messages
      */
     public FieldSearchSQLImpl(ConnectionPool cPool, RepositoryReader repoReader,
             int maxResults, int maxSecondsPerSession, Logging logTarget) {
         super(logTarget);
         logFinest("Entering constructor");
         m_cPool=cPool;
         m_repoReader=repoReader;
         m_maxResults=maxResults;
         m_maxSecondsPerSession=maxSecondsPerSession;
         logFinest("Exiting constructor");
     }
 
     public void update(DOReader reader)
             throws ServerException {
         logFinest("Entering update(DOReader)");
         String pid=reader.GetObjectPID();
         Connection conn=null;
         Statement st=null;
         try {
             conn=m_cPool.getConnection();
             String[] dbRowValues=new String[26];
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
             v=reader.getOwnerId();
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
             // add bdef and bmech ids for each active disseminator
             // and if the object is a bMech, add the bDefPID (from the 
             // datastream input spec) to its list of bDefs
             Disseminator[] disses=reader.GetDisseminators(null, null);
             ArrayList bDefs=new ArrayList();
             ArrayList bMechs=new ArrayList();
             for (int i=0; i<disses.length; i++) {
                 bDefs.add(disses[i].bDefID);
                 bMechs.add(disses[i].bMechID);
             }
             if (dbRowValues[2].equals("m")) {
                 // get it as a BMechReader and add the bDefPID
                 BMechReader mechReader=m_repoReader.getBMechReader(
                         s_nonCachedContext,
                         reader.GetObjectPID());
                 bDefs.add(mechReader.getServiceDSInputSpec(null).bDefPID);
             }
             dbRowValues[9]=getDbValue(bDefs);
             dbRowValues[10]=getDbValue(bMechs);
             // do dc stuff if needed
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
                 dbRowValues[11]=getDbValue(dc.titles());
                 dbRowValues[12]=getDbValue(dc.creators());
                 dbRowValues[13]=getDbValue(dc.subjects());
                 dbRowValues[14]=getDbValue(dc.descriptions());
                 dbRowValues[15]=getDbValue(dc.publishers());
                 dbRowValues[16]=getDbValue(dc.contributors());
                 dbRowValues[17]=getDbValue(dc.dates());
                 // get any dc.dates strings that are formed such that they
                 // can be treated as a timestamp
                 List wellFormedDates=null;
                 for (int i=0; i<dc.dates().size(); i++) {
                     if (i==0) {
                         wellFormedDates=new ArrayList();
                     }
                     Date p=DateUtility.parseDate((String) dc.dates().get(i));
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
                 dbRowValues[18]=getDbValue(dc.types());
                 dbRowValues[19]=getDbValue(dc.formats());
                 dbRowValues[20]=getDbValue(dc.identifiers());
                 dbRowValues[21]=getDbValue(dc.sources());
                 dbRowValues[22]=getDbValue(dc.languages());
                 dbRowValues[23]=getDbValue(dc.relations());
                 dbRowValues[24]=getDbValue(dc.coverages());
                 dbRowValues[25]=getDbValue(dc.rights());
             }
             logFine("Formulating SQL and inserting/updating...");
             SQLUtility.replaceInto(conn, "doFields", DB_COLUMN_NAMES,
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
 
     public FieldSearchResult findObjects(String[] resultFields,
             int maxResults, FieldSearchQuery query)
             throws UnrecognizedFieldException, ObjectIntegrityException,
             RepositoryConfigurationException, StreamIOException,
             ServerException, StorageDeviceException {
         closeAndForgetOldResults();
         int actualMax=maxResults;
         if (m_maxResults<maxResults) {
             actualMax=m_maxResults;
         }
         try {
             return stepAndRemember(new FieldSearchResultSQLImpl(
                     m_cPool, m_repoReader, resultFields, actualMax,
                     m_maxSecondsPerSession, query, this));
         } catch (SQLException sqle) {
             throw new StorageDeviceException("Error querying sql db: "
                     + sqle.getMessage());
         }
     }
 
     public FieldSearchResult resumeFindObjects(String sessionToken)
             throws UnrecognizedFieldException, ObjectIntegrityException,
             RepositoryConfigurationException, StreamIOException,
             ServerException, UnknownSessionTokenException {
         closeAndForgetOldResults();
         FieldSearchResultSQLImpl result=(FieldSearchResultSQLImpl)
                 m_currentResults.remove(sessionToken);
         if (result==null) {
             throw new UnknownSessionTokenException("Session is expired "
                     + "or never existed.");
         }
         return stepAndRemember(result);
     }
 
     private FieldSearchResult stepAndRemember(FieldSearchResultSQLImpl result)
             throws UnrecognizedFieldException, ObjectIntegrityException,
             RepositoryConfigurationException, StreamIOException,
             ServerException, UnrecognizedFieldException {
         result.step();
         if (result.getToken()!=null) {
             m_currentResults.put(result.getToken(), result);
         }
         return result;
     }
 
     // erase and cleanup expired stuff
     private void closeAndForgetOldResults() {
         Iterator iter=m_currentResults.values().iterator();
         ArrayList toRemove=new ArrayList();
         while (iter.hasNext()) {
             FieldSearchResultSQLImpl r=(FieldSearchResultSQLImpl) iter.next();
             if (r.isExpired()) {
                 logFine("listSession " + r.getToken() + " expired; will forget it.");
                 toRemove.add(r.getToken());
             }
         }
         for (int i=0; i<toRemove.size(); i++) {
             String token=(String) toRemove.get(i);
             m_currentResults.remove(token);
         }
     }
 
     /**
      * Get the string that should be inserted for a repeating-value column,
      * given a list of values.  Turn each value to lowercase and separate them
      * all by space characters.  If the list is empty, return null.
      *
      * @param dcItem a list of dublin core values
      * @return String the string to insert
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
        out.append(" .");
         return out.toString();
     }
 
 }
