 
 /* ===========================================================================
  *  Copyright (c) 2007 Serena Software. All rights reserved.
  *
  *  Use of the Sample Code provided by Serena is governed by the following
  *  terms and conditions. By using the Sample Code, you agree to be bound by
  *  the terms contained herein. If you do not agree to the terms herein, do
  *  not install, copy, or use the Sample Code.
  *
  *  1.  GRANT OF LICENSE.  Subject to the terms and conditions herein, you
  *  shall have the nonexclusive, nontransferable right to use the Sample Code
  *  for the sole purpose of developing applications for use solely with the
  *  Serena software product(s) that you have licensed separately from Serena.
  *  Such applications shall be for your internal use only.  You further agree
  *  that you will not: (a) sell, market, or distribute any copies of the
  *  Sample Code or any derivatives or components thereof; (b) use the Sample
  *  Code or any derivatives thereof for any commercial purpose; or (c) assign
  *  or transfer rights to the Sample Code or any derivatives thereof.
  *
  *  2.  DISCLAIMER OF WARRANTIES.  TO THE MAXIMUM EXTENT PERMITTED BY
  *  APPLICABLE LAW, SERENA PROVIDES THE SAMPLE CODE AS IS AND WITH ALL
  *  FAULTS, AND HEREBY DISCLAIMS ALL WARRANTIES AND CONDITIONS, EITHER
  *  EXPRESSED, IMPLIED OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY
  *  IMPLIED WARRANTIES OR CONDITIONS OF MERCHANTABILITY, OF FITNESS FOR A
  *  PARTICULAR PURPOSE, OF LACK OF VIRUSES, OF RESULTS, AND OF LACK OF
  *  NEGLIGENCE OR LACK OF WORKMANLIKE EFFORT, CONDITION OF TITLE, QUIET
  *  ENJOYMENT, OR NON-INFRINGEMENT.  THE ENTIRE RISK AS TO THE QUALITY OF
  *  OR ARISING OUT OF USE OR PERFORMANCE OF THE SAMPLE CODE, IF ANY,
  *  REMAINS WITH YOU.
  *
  *  3.  EXCLUSION OF DAMAGES.  TO THE MAXIMUM EXTENT PERMITTED BY APPLICABLE
  *  LAW, YOU AGREE THAT IN CONSIDERATION FOR RECEIVING THE SAMPLE CODE AT NO
  *  CHARGE TO YOU, SERENA SHALL NOT BE LIABLE FOR ANY DAMAGES WHATSOEVER,
  *  INCLUDING BUT NOT LIMITED TO DIRECT, SPECIAL, INCIDENTAL, INDIRECT, OR
  *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, DAMAGES FOR LOSS OF
  *  PROFITS OR CONFIDENTIAL OR OTHER INFORMATION, FOR BUSINESS INTERRUPTION,
  *  FOR PERSONAL INJURY, FOR LOSS OF PRIVACY, FOR NEGLIGENCE, AND FOR ANY
  *  OTHER LOSS WHATSOEVER) ARISING OUT OF OR IN ANY WAY RELATED TO THE USE
  *  OF OR INABILITY TO USE THE SAMPLE CODE, EVEN IN THE EVENT OF THE FAULT,
  *  TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY, OR BREACH OF CONTRACT,
  *  EVEN IF SERENA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.  THE
  *  FOREGOING LIMITATIONS, EXCLUSIONS AND DISCLAIMERS SHALL APPLY TO THE
  *  MAXIMUM EXTENT PERMITTED BY APPLICABLE LAW.  NOTWITHSTANDING THE ABOVE,
  *  IN NO EVENT SHALL SERENA'S LIABILITY UNDER THIS AGREEMENT OR WITH RESPECT
  *  TO YOUR USE OF THE SAMPLE CODE AND DERIVATIVES THEREOF EXCEED US$10.00.
  *
  *  4.  INDEMNIFICATION. You hereby agree to defend, indemnify and hold
  *  harmless Serena from and against any and all liability, loss or claim
  *  arising from this agreement or from (i) your license of, use of or
  *  reliance upon the Sample Code or any related documentation or materials,
  *  or (ii) your development, use or reliance upon any application or
  *  derivative work created from the Sample Code.
  *
  *  5.  TERMINATION OF THE LICENSE.  This agreement and the underlying
  *  license granted hereby shall terminate if and when your license to the
  *  applicable Serena software product terminates or if you breach any terms
  *  and conditions of this agreement.
  *
  *  6.  CONFIDENTIALITY.  The Sample Code and all information relating to the
  *  Sample Code (collectively "Confidential Information") are the
  *  confidential information of Serena.  You agree to maintain the
  *  Confidential Information in strict confidence for Serena.  You agree not
  *  to disclose or duplicate, nor allow to be disclosed or duplicated, any
  *  Confidential Information, in whole or in part, except as permitted in
  *  this Agreement.  You shall take all reasonable steps necessary to ensure
  *  that the Confidential Information is not made available or disclosed by
  *  you or by your employees to any other person, firm, or corporation.  You
  *  agree that all authorized persons having access to the Confidential
  *  Information shall observe and perform under this nondisclosure covenant.
  *  You agree to immediately notify Serena of any unauthorized access to or
  *  possession of the Confidential Information.
  *
  *  7.  AFFILIATES.  Serena as used herein shall refer to Serena Software,
  *  Inc. and its affiliates.  An entity shall be considered to be an
  *  affiliate of Serena if it is an entity that controls, is controlled by,
  *  or is under common control with Serena.
  *
  *  8.  GENERAL.  Title and full ownership rights to the Sample Code,
  *  including any derivative works shall remain with Serena.  If a court of
  *  competent jurisdiction holds any provision of this agreement illegal or
  *  otherwise unenforceable, that provision shall be severed and the
  *  remainder of the agreement shall remain in full force and effect.
  * ===========================================================================
  */
 
 /*
  * This experimental plugin extends Hudson support for Dimensions SCM repositories
  *
  * @author Tim Payne
  *
  */
 
 // Package name
 package hudson.plugins.dimensionsscm;
 
 // Hudson imports
 import hudson.FilePath;
 import hudson.plugins.dimensionsscm.DateUtils;
 import hudson.plugins.dimensionsscm.Logger;
 
 // Dimensions imports
 import com.serena.dmclient.api.Filter;
 import com.serena.dmclient.api.ItemRevision;
 import com.serena.dmclient.api.Project;
 import com.serena.dmclient.api.Baseline;
 import com.serena.dmclient.api.Request;
 
 import com.serena.dmclient.api.DimensionsRelatedObject;
 import com.serena.dmclient.api.SystemAttributes;
 import com.serena.dmclient.api.SystemRelationship;
 
 import com.serena.dmclient.api.DimensionsNetworkException;
 import com.serena.dmclient.api.DimensionsRuntimeException;
 import com.serena.dmclient.api.DimensionsResult;
 import com.serena.dmclient.api.DimensionsObjectFactory;
 
 import com.serena.dmclient.objects.DimensionsObject;
 import com.serena.dmclient.objects.RequestRelationshipType;
 
 import com.serena.dmclient.api.DimensionsDatabaseAdmin.CommandFailedException;
 import com.serena.dmclient.api.DimensionsConnection;
 import com.serena.dmclient.api.DimensionsConnectionDetails;
 import com.serena.dmclient.api.DimensionsConnectionManager;
 import com.serena.dmclient.api.BulkOperator;
 import com.serena.dmclient.api.ItemRevisionHistoryRec;
 import com.serena.dmclient.api.ActionHistoryRec;
 
 // Hudson imports
 import hudson.model.AbstractBuild;
 import com.serena.dmclient.api.Request;
 import hudson.model.AbstractProject;
 import com.serena.dmclient.api.DimensionsRelatedObject;
 
 // General imports
 import java.io.File;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 
 import java.io.Serializable;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.ParseException;
 import java.lang.IllegalArgumentException;
 import java.net.URLDecoder;
 import java.util.List;
 import java.util.TimeZone;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.text.Collator;
 
 import java.net.URI;
 import java.net.URL;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 
 import org.apache.commons.lang.StringEscapeUtils;
 
 
 /*
  * Main Dimensions API class
  */
 public class DimensionsAPI
 {
     private static final String MISSING_SOURCE_PATH = "The nested element needs a valid 'srcpath' attribute"; //$NON-NLS-1$
     private static final String MISSING_PROJECT = "The nested element needs a valid project to work on"; //$NON-NLS-1$
     private static final String MISSING_BASELINE = "The nested element needs a valid baseline to work on"; //$NON-NLS-1$
     private static final String MISSING_REQUEST = "The nested element needs a valid request to work on"; //$NON-NLS-1$
     private static final String BAD_BASE_DATABASE_SPEC = "The <dimensions> task needs a valid 'database' attribute, in the format 'dbname@dbconn'"; //$NON-NLS-1$
     private static final String NO_COMMAND_LINE = "The <run> nested element need a valid 'cmd' attribute"; //$NON-NLS-1$
     private static final String SRCITEM_SRCPATH_CONFLICT = "The <getcopy> nested element needs exactly one of the 'srcpath' or 'srcitem' attributes"; //$NON-NLS-1$
 
     // Dimensions server details
     private String dmServer;
     private String dmDb;
 
     private String dbName;
     private String dbConn;
 
     // Dimensions user details
     private String dmUser;
     private String dmPasswd;
 
     // Dimensions project details
     private String dmProject;
     private String dmDirectory;
     private String dmRequest;
     private String projectPath;
 
     private String dateType = "edit";
     private boolean allRevisions = false;
     private int version = -1;
 
     private DimensionsConnection connection = null;
 
     /*
      * Gets the user ID for the connection.
      * @return the user ID of the user as whom to connect
      */
     public final String getSCMUserID() {
         return this.dmUser;
     }
 
     /*
      * Gets the Dimensions version if set.
      * @return version
      */
     public final int getDmVersion() {
         if (version > 0)
             return this.version;
         else
             return 0;
     }
 
     /*
      * Gets the base database for the connection (as "NAME@CONNECTION").
      * @return the name of the base database to connect to
      */
     public final String getSCMDatabase() {
         return this.dmDb;
     }
 
     /*
      * Gets the base database for the connection
      * @return the name of the base database only
      */
     public final String getSCMBaseDb() {
         return this.dbName;
     }
 
     /*
      * Gets the database DNS for the connection
      * @return the name of the DSN only
      */
     public final String getSCMDsn() {
         return this.dbConn;
     }
 
     /*
      * Gets the server for the connection.
      * @return the name of the server to connect to
      */
     public final String getSCMServer() {
         return this.dmServer;
     }
 
     /*
      * Gets the project ID for the connection.
      * @return the project ID
      */
     public final String getSCMProject() {
         return this.dmProject;
     }
 
     /*
      * Gets the project path.
      * @return the project path
      */
     public final String getSCMPath() {
         return this.projectPath;
     }
 
     /*
      * Gets the repository connection class
      * @return the SCM repository connection
      */
     public final DimensionsConnection getCon() {
         return this.connection;
     }
 
     /**
      * Creates a Dimensions session using the supplied login credentials and
      * server details
      *
      * @param userID
      *            Dimensions user ID
      * @param password
      *            Dimensions password
      * @param database
      *            base database name
      * @param server
      *            hostname of the remote dimensions server
      * @return a boolean
      * @throws DimensionsNetworkException
      */
     public final boolean login(String userID, String password,
             String database, String server)
             throws IllegalArgumentException, DimensionsRuntimeException
     {
 
         if (connection == null)
             connection = DimensionsConnectionManager.getThreadConnection();
 
         if (connection == null)
         {
             dmServer = server;
             dmDb = database;
             dmUser = userID;
             dmPasswd = password;
 
 
             Logger.Debug("Checking Dimensions login parameters...");
 
             if (dmServer == null || dmServer.length() == 0 ||
                 dmDb == null || dmDb.length() == 0 ||
                 dmUser == null || dmUser.length() == 0 ||
                 dmPasswd  == null || dmPasswd.length() == 0)
                 throw new IllegalArgumentException("Invalid or not parameters have been specified");
 
             try {
                 // check if we need to pre-process the login details
                 String[] dbCompts = parseDatabaseString(dmDb);
                 dbName = dbCompts[0];
                 dbConn = dbCompts[1];
 
                 Logger.Debug("Logging into Dimensions: " + dmUser + " " + dmServer + " " + dmDb);
 
                 DimensionsConnectionDetails details = new DimensionsConnectionDetails();
                 details.setUsername(dmUser);
                 details.setPassword(dmPasswd);
                 details.setDbName(dbName);
                 details.setDbConn(dbConn);
                 details.setServer(dmServer);
                 Logger.Debug("Getting Dimensions connection...");
                 connection = DimensionsConnectionManager.getConnection(details);
                 if (connection!=null) {
                     Logger.Debug("Registering connection...");
                     DimensionsConnectionManager.registerThreadConnection(connection);
                     Logger.Debug("Registered connection...");
                 }
             } catch(Exception e) {
                 throw new DimensionsRuntimeException("Login to Dimensions failed - " + e.getMessage());
             }
         }
         return (connection != null);
     }
 
     /**
      * Disconnects from the Dimensions repository
      */
     public final void logout()
     {
         if (connection != null) {
             try {
                 if (DimensionsConnectionManager.getThreadConnection() != null) {
                     Logger.Debug("Unregistering connection...");
                     DimensionsConnectionManager.unregisterThreadConnection();
                 }
                 Logger.Debug("Closing connection to Dimensions...");
                 connection.close();
             } catch (DimensionsNetworkException dne) {
                 /* do nothing */
             } catch (DimensionsRuntimeException dne) {
                 /* do nothing */
             }
         }
         connection = null;
     }
 
 
     /**
      * Parses a base database specification
      * <p>
      * Valid patterns are dbName/dbPassword@dbConn or dbName@dbConn. Anything
      * else will cause a java.text.ParseException to be thrown. Returns an array
      * of either [dbName, dbConn, dbPassword] or [dbName, dbConn].
      *
      * @param database
      *            a base database specification
      * @return an array of base database specification components
      * @throws ParseException
      *             if the supplied String does not conform to the above rules
      */
     private static String[] parseDatabaseString(String database)
             throws ParseException {
         String[] dbCompts;
         int endName = database.indexOf('/');
         int startConn = database.indexOf('@');
         if (startConn < 1 || startConn == database.length() - 1) {
             throw new ParseException(BAD_BASE_DATABASE_SPEC, startConn);
         }
         String dbName = null;
         String dbConn = null;
         String dbPassword = null;
         if (endName < 0 || startConn <= endName) {
             // no '/' or '@' is before '/':
             dbName = database.substring(0, startConn);
             dbConn = database.substring(startConn + 1);
             dbCompts = new String[2];
             dbCompts[0] = dbName;
             dbCompts[1] = dbConn;
         } else if (endName == 0 || startConn == endName + 1) {
             // '/' at start or '/' immediately followed by '@':
             throw new ParseException(BAD_BASE_DATABASE_SPEC, endName);
         } else {
             dbName = database.substring(0, endName);
             dbPassword = database.substring(endName + 1, startConn);
             dbConn = database.substring(startConn + 1);
             dbCompts = new String[3];
             dbCompts[0] = dbName;
             dbCompts[1] = dbConn;
             dbCompts[2] = dbPassword;
         }
         return dbCompts;
     }
 
     /*
      *-----------------------------------------------------------------
      *  FUNCTION SPECIFICATION
      *  Name:
      *      hasRepositoryBeenUpdated
      *  Description:
      *      Has the repository had any changes made during a certain time?
      * Parameters:
      *      @param final String projectName
      *      @param final FilePath workspace
      *      @param final Calendar fromDate
      *      @param final Calendar toDate
      *      @param final TimeZone tz
      *  Return:
      *      @return boolean
      *-----------------------------------------------------------------
      */
     public boolean hasRepositoryBeenUpdated(final String projectName,
                                final FilePath workspace,
                                final Calendar fromDate,
                                final Calendar toDate,
                                final TimeZone tz)
                               throws IOException, InterruptedException
     {
         boolean bChanged = false;
 
         if (fromDate == null)
             return true;
 
         if (connection == null)
             throw new IOException("Not connected to an SCM repository");
 
         try
         {
             List items = calcRepositoryDiffs(projectName,null,null,workspace,fromDate,toDate, tz);
             if (items != null)
                 bChanged = (items.size() > 0);
         }
         catch(Exception e)
         {
             // e.printStackTrace();
             throw new IOException("Unable to run hasRepositoryBeenUpdated - " + e.getMessage());
         }
 
         return bChanged;
     }
 
 
     /*
      *-----------------------------------------------------------------
      *  FUNCTION SPECIFICATION
      *  Name:
      *      checkout
      *  Description:
      *      Get a copy of the code
      * Parameters:
      *      @param final String projectName
      *      @param final FilePath projectDir
      *      @param final FilePath workspaceName
      *      @param final Calendar fromDate
      *      @param final Calendar toDate
      *      @param final File changelogFile
      *      @param final TimeZone tz
      *      @param StringBuffer cmdOutput
      *      @param final String url
      *      @param final String baseline
      *      @param final String requests
      *      @param final boolean doFullUpdate
      *      @param final boolean doRevert
      *  Return:
      *      @return boolean
      *-----------------------------------------------------------------
      */
     public boolean checkout(final String projectName,
                             final FilePath projectDir,
                             final FilePath workspaceName,
                             final Calendar fromDate,
                             final Calendar toDate,
                             final File changelogFile,
                             final TimeZone tz,
                             StringBuffer cmdOutput,
                             final String url,
                             final String baseline,
                             final String requests,
                             final boolean doFullUpdate,
                             final boolean doRevert)
                     throws IOException, InterruptedException
     {
         boolean bRet = false;
 
         if (connection == null)
             throw new IOException("Not connected to an SCM repository");
 
         try
         {
             if (version < 0) {
                 version = 2009;
                 // Get the server version
                 List inf = getCon().getObjectFactory().getServerVersion(2);
                 if (inf == null)
                     Logger.Debug("Detection of server information failed");
 
                 if (inf != null)
                 {
                     Logger.Debug("Server information detected -" + inf.size());
                     for (int i = 0; i < inf.size(); ++i) {
                         String prop = (String) inf.get(i);
                         Logger.Debug(i + " - " + prop);
                     }
 
                     // Try and locate the server version.
                     // If not found, then get the schema version and use that
                     String server = (String)inf.get(2);
                     if (server == null)
                         server = (String)inf.get(0);
 
                     if (server != null)
                     {
                         Logger.Debug("Detected server version: " + server);
                         String[] tokens = server.split(" ");
                         server = tokens[0];
                         if (server.startsWith("10."))
                             version = 10;
                         else if (server.startsWith("2009"))
                             version = 2009;
                         else if (server.startsWith("201"))
                             version = 2010;
                         else
                             version = 2009;
                         Logger.Debug("Version to process set to " + version);
                     }
                     else
                         Logger.Debug("No server information found");
                 }
             }
 
             String coCmd = "UPDATE /BRIEF ";
             if (version == 10)
                 coCmd = "DOWNLOAD ";
 
             List items = calcRepositoryDiffs(projectName,baseline,requests,projectDir,fromDate,toDate,tz);
             if (items != null || doFullUpdate)
             {
                 File logFile = new File("a");
                 FileWriter logFileWriter = null;
                 PrintWriter fmtWriter = null;
                 File tmpFile = null;
 
                 if (items != null && !doFullUpdate) {
                     try {
                         tmpFile = logFile.createTempFile("dmCm"+toDate.getTimeInMillis(),null,null);
                         logFileWriter = new FileWriter(tmpFile);
                         fmtWriter = new PrintWriter(logFileWriter,true);
 
                         for (int i = 0; i < items.size(); ++i) {
                             ItemRevision item = (ItemRevision) items.get(i);
                             fmtWriter.println((String)item.getAttribute(SystemAttributes.OBJECT_SPEC));
                         }
                         fmtWriter.flush();
                     } catch (Exception e) {
                         //e.printStackTrace();
                         throw new IOException("Unable to write command log - " + e.getMessage());
                     } finally {
                         fmtWriter.close();
                     }
                 }
 
                 String cmd = coCmd;
                 String projDir = (projectDir!=null) ? projectDir.getRemote() : null;
 
                 Logger.Debug("Do full update : " + doFullUpdate);
                 Logger.Debug("CM Url : " + ((url != null) ? url : "(null)"));
 
                 if (!doFullUpdate && tmpFile != null)
                     cmd += "/USER_ITEMLIST=\"" + tmpFile.getPath() + "\"";
                 else {
                     if (projDir != null && !projDir.equals("\\") && !projDir.equals("/") && requests == null)
                         cmd += "/DIR=\"" + projDir + "\"";
                 }
 
                 if (requests != null) {
                     if (requests.indexOf(",")==0) {
                         cmd += "/CHANGE_DOC_IDS=(\"" + requests + "\") ";
                     } else {
                         cmd += "/CHANGE_DOC_IDS=("+ requests +") ";
                     }
                     cmd += "/WORKSET=\"" + projectName + "\" ";
                 }
                 else if (baseline != null) {
                     cmd += "/BASELINE=\"" + baseline + "\"";
                 } else {
                     cmd += "/WORKSET=\"" + projectName + "\" ";
                 }
 
                 cmd += "/USER_DIR=\"" + workspaceName.getRemote() + "\" ";
 
                 if (doRevert)
                     cmd += " /OVERWRITE";
 
                 DimensionsResult res = run(connection,cmd);
                 if (res != null )
                 {
                     cmdOutput = cmdOutput.append(res.getMessage());
                     String outputStr = new String(cmdOutput.toString());
                     Logger.Debug(outputStr);
 
                     if (items != null) {
                         if (tmpFile != null)
                             tmpFile.delete();
                         // Process the changesets...
                         List changes = createChangeList(items,tz,url);
                         Logger.Debug("Writing changeset to " + changelogFile.getPath());
                         DimensionsChangeLogWriter write = new DimensionsChangeLogWriter();
                         write.writeLog(changes,changelogFile);
                     }
                     else {
                         // No changes - just fake a log
                         DimensionsChangeLogWriter write = new DimensionsChangeLogWriter();
                         write.writeLog(null,changelogFile);
                     }
                     bRet = true;
 
                     // Check if any conflicts were identified
                     int confl = outputStr.indexOf("C\t");
                     if (confl > 0)
                         bRet = false;
                 }
             }
             else
                 bRet = true;
         }
         catch(Exception e)
         {
             //e.printStackTrace();
             throw new IOException(e.getMessage());
         }
 
         return bRet;
     }
 
 
     /*
      *-----------------------------------------------------------------
      *  FUNCTION SPECIFICATION
      *  Name:
      *  Name:
      *      calcRepositoryDiffs
      *  Description:
      *      Calculate any repository changes made during a certain time
      * Parameters:
      *      @param final String projectName
      *      @param final String baselineName
      *      @param final String requests
      *      @param final FilePath workspace
      *      @param final Calendar fromDate
      *      @param final Calendar toDate
      *      @param final TimeZone tz
      *  Return:
      *      @return boolean
      *-----------------------------------------------------------------
      */
     private List calcRepositoryDiffs(final String projectName,
                                final String baselineName,
                                final String requests,
                                final FilePath workspace,
                                final Calendar fromDate,
                                final Calendar toDate,
                                final TimeZone tz)
                               throws IOException, InterruptedException
     {
         if (connection == null)
             throw new IOException("Not connected to an SCM repository");
 
         if (fromDate == null && baselineName == null && requests == null)
             return null;
 
         try
         {
             // Get the dates for the last build
             int[] attrs = getItemFileAttributes(true);
             String dateAfter = (fromDate != null) ? formatDatabaseDate(fromDate.getTime(), tz) : "01-JAN-1970 00:00:00";
             String dateBefore = (toDate != null) ? formatDatabaseDate(toDate.getTime(), tz) : formatDatabaseDate(Calendar.getInstance().getTime(), tz);
 
             Filter filter = new Filter();
 
             filter.criteria().add(new Filter.Criterion(SystemAttributes.LAST_UPDATED_DATE, dateAfter, Filter.Criterion.GREATER_EQUAL));
             filter.criteria().add(new Filter.Criterion(SystemAttributes.LAST_UPDATED_DATE, dateBefore, Filter.Criterion.LESS_EQUAL));
             filter.criteria().add(new Filter.Criterion(SystemAttributes.CREATION_DATE, dateAfter, Filter.Criterion.GREATER_EQUAL));
             filter.criteria().add(new Filter.Criterion(SystemAttributes.CREATION_DATE, dateBefore, Filter.Criterion.LESS_EQUAL));
             filter.criteria().add(new Filter.Criterion(SystemAttributes.IS_EXTRACTED, "Y", Filter.Criterion.NOT)); //$NON-NLS-1$
             filter.orders().add(new Filter.Order(SystemAttributes.REVISION_COMMENT, Filter.ORDER_ASCENDING));
             filter.orders().add(new Filter.Order(SystemAttributes.ITEMFILE_DIR, Filter.ORDER_ASCENDING));
             filter.orders().add(new Filter.Order(SystemAttributes.ITEMFILE_FILENAME, Filter.ORDER_ASCENDING));
 
             Logger.Debug("Looking between " + dateAfter + " -> " + dateBefore);
             String projName;
 
             if (baselineName != null && requests == null) {
                 projName = baselineName.toUpperCase();
             } else {
                 projName = projectName.toUpperCase();
             }
 
             List items = null;
 
             if (requests != null) {
                 String[] reqStr = null;
                 if (requests.indexOf(",")>0) {
                     reqStr = requests.split(",");
                     Logger.Debug("User specified " + reqStr.length + " requests");
                 }
                 else {
                     reqStr = new String[1];
                     reqStr[0] = requests;
                 }
 
                 // setup filter for requests Name
                 List requestList = new ArrayList(1);
                 items = new ArrayList(1);
 
                 for(int ii=0;ii<reqStr.length;ii++) {
                     String xStr = reqStr[ii];
                     xStr.trim();
                     Logger.Debug("Request to process is \"" + xStr + "\"");
                     Request requestObj = connection.getObjectFactory().findRequest(xStr.toUpperCase());
 
                     if (requestObj != null) {
                         Logger.Debug("Request to process is \"" + requestObj.getName() + "\"");
                         requestList.add(requestObj);
                         // Get all the children for this request
                         if (!getDmChildRequests(requestObj, requestList))
                             throw new IOException("Could not process request \""+requestObj.getName()+"\" children in repository");
 
                         Logger.Debug("Request has "+requestList.size()+" elements to process");
                         Project projectObj = getCon().getObjectFactory().getProject(projName);
                         for (int i=0; i<requestList.size(); i++) {
                             Request req = (Request)requestList.get(i);
                             Logger.Debug("Request "+i+" is \"" + req.getName() + "\"");
                             if (!queryItems(getCon(), req, "/", items, filter, projectObj, true, allRevisions))
                                 throw new IOException("Could not process items for request \""+req.getName()+"\"");
                         }
 
                         if (items != null) {
                             Logger.Debug("Request has "+items.size()+" items to process");
                             BulkOperator bo = getCon().getObjectFactory().getBulkOperator(items);
                             bo.queryAttribute(attrs);
                         }
                     }
                 }
             } else if (baselineName != null) {
                 // setup filter for baseline Name
                 Filter baselineFilter = new Filter();
                 baselineFilter.criteria().add(new Filter.Criterion(SystemAttributes.OBJECT_SPEC,baselineName.toUpperCase(),Filter.Criterion.EQUALS));
 
                 List<Baseline> baselineObjects = connection.getObjectFactory().getBaselines(baselineFilter);
                 Logger.Debug("Baseline query for \"" + baselineName + "\" returned " + baselineObjects.size() + " baselines");
                 for (int i=0; i<baselineObjects.size(); i++) {
                     Logger.Debug("Baseline "+i+" is \"" + baselineObjects.get(i).getName() + "\"");
                 }
 
                 if (baselineObjects.size() == 0) throw new IOException("Could not find baseline \""+baselineName+"\" in repository");
                 if (baselineObjects.size() > 1) throw new IOException("Found more than one baseline named \""+baselineName+"\" in repository");
 
                 items = queryItems(getCon(), baselineObjects.get(0), workspace.getRemote(), filter, attrs, true, !allRevisions);
             } else {
                 Project projectObj = getCon().getObjectFactory().getProject(projName);
                 items = queryItems(getCon(), projectObj, workspace.getRemote(), filter, attrs, true, !allRevisions);
             }
             return items;
         }
         catch(Exception e)
         {
             //e.printStackTrace();
            throw new IOException("Unable to run calcRepositoryDiffs - " + e.getMessage());
         }
     }
 
     /**
      * Lock a project
      *
      * @param String
      * @return DimensionsResult
      * @throws DimensionsRuntimeException
      */
     public DimensionsResult lockProject(String projectId)
                             throws DimensionsRuntimeException
     {
         try {
             String cmd = "LCK WORKSET ";
             if (projectId != null) {
                 cmd += "\""+projectId+"\"";
                 DimensionsResult res = run(connection,cmd);
                 if (res != null ) {
                     Logger.Debug("Locking project - "+res.getMessage());
                     return res;
                 }
             }
             return null;
         } catch(Exception e) {
             throw new DimensionsRuntimeException(e.getMessage());
         }
     }
 
     /**
      * UnLock a project
      *
      * @param String
      * @return DimensionsResult
      * @throws DimensionsRuntimeException
      */
     public DimensionsResult unlockProject(String projectId)
                             throws DimensionsRuntimeException
     {
         try {
             String cmd = "ULCK WORKSET ";
             if (projectId != null) {
                 cmd += "\""+projectId+"\"";
                 DimensionsResult res = run(connection,cmd);
                 if (res != null ) {
                     Logger.Debug("Unlocking project - "+res.getMessage());
                     return res;
                 }
             }
             return null;
         } catch(Exception e) {
             throw new DimensionsRuntimeException(e.getMessage());
         }
     }
 
 
     /**
      * Build a baseline
      *
      * @param String area
      * @param String projectId
      * @param boolean batch
      * @param boolean buildClean
      * @param String buildConfig
      * @param String options
      * @param boolean capture
      * @param String requests
      * @param String targets
      * @param AbstractBuild build
      * @return DimensionsResult
      * @throws DimensionsRuntimeException
      */
     public DimensionsResult buildBaseline(String area, String projectId, boolean batch,
                                           boolean buildClean, String buildConfig,
                                           String options, boolean capture,
                                           String requests, String targets,
                                           AbstractBuild build)
                             throws DimensionsRuntimeException
     {
         try {
             String cmd = "BLDB ";
             if (projectId != null && build != null) {
                 cmd += "\""+projectId+"_"+build.getProject().getName()+"_"+build.getNumber()+"\"";
                 if (area != null && area.length() > 0) {
                     cmd += " /AREA=\""+area+"\"";
                 }
                 if (batch) {
                     cmd += " /NOWAIT";
                 } else {
                     cmd += " /WAIT";
                 }
                 if (capture) {
                     cmd += " /CAPTURE";
                 } else {
                     cmd += " /NOCAPTURE";
                 }
                 if (buildClean) {
                     cmd += " /BUILD_CLEAN";
                 }
                 if (buildConfig != null && buildConfig.length() > 0) {
                     cmd += " /BUILD_CONFIG=\""+buildConfig+"\"";
                 }
                 if (options != null && options.length() > 0) {
                     if (options.indexOf(",")==0) {
                         cmd += "/BUILD_OPTIONS=(\"" + options + "\") ";
                     } else {
                         cmd += "/BUILD_OPTIONS=("+ options +") ";
                     }
                 }
                 if (requests != null && requests.length() > 0) {
                     if (requests.indexOf(",")==0) {
                         cmd += "/CHANGE_DOC_IDS=(\"" + requests + "\") ";
                     } else {
                         cmd += "/CHANGE_DOC_IDS=("+ requests +") ";
                     }
                 }
                 if (targets != null && targets.length() > 0) {
                     if (targets.indexOf(",")==0) {
                         cmd += "/TARGETS=(\"" + targets + "\") ";
                     } else {
                         cmd += "/TARGETS=("+ targets +") ";
                     }
                 }
                 DimensionsResult res = run(connection,cmd);
                 if (res != null ) {
                     Logger.Debug("Building baseline - "+res.getMessage());
                     return res;
                 }
             }
             return null;
         } catch(Exception e) {
             throw new DimensionsRuntimeException(e.getMessage());
         }
     }
 
 
     /**
      * Upload files
      *
      * @param FilePath
      * @param String
      * @param File
      * @param AbstractBuild
      * @param String
      * @return DimensionsResult
      * @throws DimensionsRuntimeException
      */
     public DimensionsResult UploadFiles(FilePath rootDir, String projectId, File cmdFile, AbstractBuild build, String requests)
                             throws DimensionsRuntimeException
     {
         try {
             String ciCmd = "DELIVER /BRIEF /ADD /UPDATE /DELETE ";
             if (version == 10)
                 ciCmd = "UPLOAD ";
 
             if (projectId != null && build != null) {
                 ciCmd += " /USER_FILELIST=\""+cmdFile.getAbsolutePath()+"\"";
                 ciCmd += " /WORKSET=\""+projectId+"\"";
                 ciCmd += " /COMMENT=\"Build artifacts saved by Hudson for job '"+build.getProject().getName()+"' - build "+build.getNumber()+"\"";
                 ciCmd += " /USER_DIRECTORY=\""+rootDir.getRemote()+"\"";
                 if (requests != null && requests.length() > 0) {
                     if (requests.indexOf(",")==0) {
                         ciCmd += "/CHANGE_DOC_IDS=(\"" + requests + "\") ";
                     } else {
                         ciCmd += "/CHANGE_DOC_IDS=("+ requests +") ";
                     }
                 }
                 DimensionsResult res = run(connection,ciCmd);
                 if (res != null ) {
                     Logger.Debug("Saving artifacts - "+res.getMessage());
                     return res;
                 }
             }
             return null;
         } catch(Exception e) {
             throw new DimensionsRuntimeException(e.getMessage());
         }
     }
 
     /**
      * Create a project tag
      *
      * @param String
      * @param AbstractBuild
      * @return DimensionsResult
      * @throws DimensionsRuntimeException
      */
     public DimensionsResult createBaseline(String projectId, AbstractBuild build)
                             throws DimensionsRuntimeException
     {
         try {
             String cmd = "CBL ";
             if (projectId != null && build != null) {
                 cmd += "\""+projectId+"_"+build.getProject().getName()+"_"+build.getNumber()+"\"";
                 cmd += " /SCOPE=WORKSET /WORKSET=\""+projectId+"\"";
                 cmd += " /DESCRIPTION=\"Project Baseline created by Hudson for job '"+build.getProject().getName()+"' - build "+build.getNumber()+"\"";
                 DimensionsResult res = run(connection,cmd);
                 if (res != null ) {
                     Logger.Debug("Tagging project - "+res.getMessage());
                     return res;
                 }
             }
             return null;
         } catch(Exception e) {
             throw new DimensionsRuntimeException(e.getMessage());
         }
     }
 
     /**
      * Deploy a baseline
      *
      * @param String
      * @param String
      * @param AbstractBuild
      * @return DimensionsResult
      * @throws DimensionsRuntimeException
      */
     public DimensionsResult deployBaseline(String projectId, AbstractBuild build, String state)
                             throws DimensionsRuntimeException
     {
         try {
             String cmd = "DPB ";
             if (projectId != null && build != null) {
                 cmd += "\""+projectId+"_"+build.getProject().getName()+"_"+build.getNumber()+"\"";
                 cmd += " /WORKSET=\""+projectId+"\"";
                 if (state != null && state.length() > 0) {
                     cmd += " /STAGE=\""+state+"\"";
                 }
                 cmd += " /COMMENT=\"Project Baseline deployed by Hudson for job '"+build.getProject().getName()+"' - build "+build.getNumber()+"\"";
                 DimensionsResult res = run(connection,cmd);
                 if (res != null ) {
                     Logger.Debug("Deploying baseline - "+res.getMessage());
                     return res;
                 }
             }
             return null;
         } catch(Exception e) {
             throw new DimensionsRuntimeException(e.getMessage());
         }
     }
 
     /**
      * Action a baseline
      *
      * @param String
      * @param String
      * @param AbstractBuild
      * @return DimensionsResult
      * @throws DimensionsRuntimeException
      */
     public DimensionsResult actionBaseline(String projectId, AbstractBuild build, String state)
                             throws DimensionsRuntimeException
     {
         try {
             String cmd = "ABL ";
             if (projectId != null && build != null) {
                 cmd += "\""+projectId+"_"+build.getProject().getName()+"_"+build.getNumber()+"\"";
                 cmd += " /WORKSET=\""+projectId+"\"";
                 if (state != null && state.length() > 0) {
                     cmd += " /STATUS=\""+state+"\"";
                 }
                 cmd += " /COMMENT=\"Project Baseline action by Hudson for job '"+build.getProject().getName()+"' - build "+build.getNumber()+"\"";
                 DimensionsResult res = run(connection,cmd);
                 if (res != null ) {
                     Logger.Debug("Actioning baseline - "+res.getMessage());
                     return res;
                 }
             }
             return null;
         } catch(Exception e) {
             throw new DimensionsRuntimeException(e.getMessage());
         }
     }
 
     /**
      * Construct the change list
      *
      * @param List
      * @param TimeZone
      * @param url
      * @return List
      * @throws DimensionsRuntimeException
      */
      private List createChangeList(List items, TimeZone tz, String url)
                             throws DimensionsRuntimeException
      {
         items = getSortedItemList(items);
         List changeSet = new ArrayList(items.size());
         String key = null;
         DimensionsChangeSet cs = null;
         for (int i = 0; i < items.size(); ++i) {
             Logger.Debug("Processing change set " + i + "/" + items.size());
             ItemRevision item = (ItemRevision) items.get(i);
             int x = 0;
 
             if (item.getAttribute(SystemAttributes.FULL_PATH_NAME)==null) {
                 // Came from another project or something - not in here
                 continue;
             }
 
             Integer fileVersion = (Integer)item.getAttribute(SystemAttributes.FILE_VERSION);
             String operation;
             if (fileVersion != null)
                 x = fileVersion.intValue();
 
             Logger.Debug("Creating a change set (" + x + ") " + i);
             if (x < 2)
                 operation = "add";
             else
                 operation = "edit";
 
             String spec = (String)item.getAttribute(SystemAttributes.OBJECT_SPEC);
             String revision = (String)item.getAttribute(SystemAttributes.REVISION);
             String fileName = (String)item.getAttribute(SystemAttributes.FULL_PATH_NAME) + ";" + revision;
             String author = (String)item.getAttribute(SystemAttributes.LAST_UPDATED_USER);
             String comment = (String)item.getAttribute(SystemAttributes.REVISION_COMMENT);
             String date = (String)item.getAttribute(getDateTypeAttribute(operation));
 
             if (date == null)
                 date = (String)item.getAttribute(getDateTypeAttribute("edit"));
 
             String urlString = constructURL(spec,url,getSCMDsn(),getSCMBaseDb());
             if (urlString == null)
                 urlString = "";
             if (comment == null)
                 comment = "(None)";
 
             Logger.Debug("Change set details -" + comment + " " + revision + " " + fileName + " " + author +
                          " " + spec  + " " + date + " " + operation + " " + urlString);
 
             Calendar opDate = Calendar.getInstance();
             opDate.setTime(DateUtils.parse(date,tz));
 
             if (key == null) {
                 cs = new DimensionsChangeSet(fileName,author,operation,revision,comment,urlString,opDate);
                 key = comment + author;
                 changeSet.add(cs);
             } else {
                 String key1 = comment + author;
                 if (key.equals(key1)) {
                     cs.add(fileName,operation,urlString);
                 } else {
                     cs = new DimensionsChangeSet(fileName,author,operation,revision,comment,urlString,opDate);
                     key = comment + author;
                     changeSet.add(cs);
                 }
             }
 
             // at this point we have a valid DimensionsChangeSet (cs) that has already been added
             // to the list (changeSet).  So now we will add all requests to the DimensionsChangeSet.
             List itemRequests = item.getChildRequests(null);
 
             for (int j = 0; j < itemRequests.size(); ++j) {
                 DimensionsRelatedObject obj = (DimensionsRelatedObject) itemRequests.get(j);
                 DimensionsObject relType = obj.getRelationship();
                 if (SystemRelationship.IN_RESPONSE.equals(relType)) {
                     Request req = (Request) obj.getObject();
                     req.queryAttribute(new int[]{SystemAttributes.TITLE,SystemAttributes.DESCRIPTION,SystemAttributes.OBJECT_SPEC});
                     String objectId = (String)req.getAttribute(SystemAttributes.OBJECT_SPEC);
                     String titlex = (String)req.getAttribute(SystemAttributes.TITLE);
                     String requestUrl = constructRequestURL(objectId,url,getSCMDsn(),getSCMBaseDb());
                     cs.addRequest(objectId,requestUrl,titlex);
                     Logger.Debug("Child Request Details IRT -" + objectId + " " + requestUrl + " " + titlex);
                 } else {
                     Logger.Debug("Child Request Details Ignored");
                 }
 
             }
         }
 
         return changeSet;
     }
 
     /**
      * Sort the item list
      *
      * @param List
      * @return List
      * @throws DimensionsRuntimeException
      */
     private static List getSortedItemList(List items)
                             throws DimensionsRuntimeException
     {
         Collections.sort(items, new Comparator()
         {
             public int compare(Object oa1, Object oa2)
             {
                 int result = 0;
                 try
                 {
                     ItemRevision o1 = (ItemRevision)oa1;
                     ItemRevision o2 = (ItemRevision)oa2;
 
                     String a1 = (String)o1.getAttribute(SystemAttributes.REVISION_COMMENT);
                     String a2 = (String)o2.getAttribute(SystemAttributes.REVISION_COMMENT);
 
                     a1 += (String)o1.getAttribute(SystemAttributes.LAST_UPDATED_USER);
                     a2 += (String)o2.getAttribute(SystemAttributes.LAST_UPDATED_USER);
 
                     result = a1.compareTo(a2);
                 }
                 catch (Exception e)
                 {
                     //e.printStackTrace();
                     throw new DimensionsRuntimeException("Unable to sort item list - " + e.getMessage());
                 }
                 return result;
             }
         });
         return items;
     }
 
     /**
      * Sets the current project for the current user, which is deduced from the
      * current thread.
      *
      * @param connection
      *            the connection for which to set the current project.
      * @param projectName
      *            the project to switch to, in the form PRODUCT NAME:PROJECT
      *            NAME.
      * @throws DimensionsRuntimeException
      */
     private static void setCurrentProject(DimensionsConnection connection,
             String projectName) {
         connection.getObjectFactory().setCurrentProject(projectName, false, "",
                 "", null, true);
     }
 
     private static Project getCurrentProject(DimensionsConnection connection) {
         return connection.getObjectFactory().getCurrentUser()
                 .getCurrentProject();
     }
 
 
     static int[] getItemFileAttributes(boolean isDirectory) {
         if (isDirectory) {
             final int[] attrs = { SystemAttributes.OBJECT_SPEC,
                     SystemAttributes.PRODUCT_NAME, SystemAttributes.OBJECT_ID,
                     SystemAttributes.VARIANT, SystemAttributes.TYPE_NAME,
                     SystemAttributes.REVISION, SystemAttributes.FULL_PATH_NAME,
                     SystemAttributes.ITEMFILE_FILENAME,
                     SystemAttributes.LAST_UPDATED_USER,
                     SystemAttributes.FILE_VERSION,
                     SystemAttributes.REVISION_COMMENT,
                     SystemAttributes.LAST_UPDATED_DATE,
                     SystemAttributes.CREATION_DATE};
             return attrs;
         }
         final int[] attrs = { SystemAttributes.PRODUCT_NAME,
                 SystemAttributes.OBJECT_ID, SystemAttributes.VARIANT,
                 SystemAttributes.TYPE_NAME, SystemAttributes.REVISION,
                 SystemAttributes.ITEMFILE_FILENAME,
                 SystemAttributes.LAST_UPDATED_USER,
                 SystemAttributes.FILE_VERSION,
                 SystemAttributes.LAST_UPDATED_DATE,
                 SystemAttributes.CREATION_DATE};
         return attrs;
     }
 
     private static String preProcessSrcPath(String srcPath) {
         String path = srcPath.equals("/") ? "" : srcPath; //$NON-NLS-1$ //$NON-NLS-2$
         if (!path.endsWith("/") & !path.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
             path += "/"; //$NON-NLS-1$
         }
         if (path.equals("\\/") || path.equals("/"))
             path = "";
         return path;
     }
 
     // URL encode a webclient path + spec for opening
     private static String constructURL(String spec, String url, String dsn, String db) {
         String urlString = "";
         if (spec != null && spec.length() > 0 &&
             url != null && url.length() > 0) {
             String host = url;
             if (host.endsWith("/"))
                 host = host.substring(0,host.length()-1);
 
             if (host.startsWith("http:"))
                 host = host.substring(7,host.length());
 
             String page = "/dimensions/";
             String urlQuery = "jsp=api&command=openi&object_id=";
             urlQuery += spec;
             urlQuery += "&DB_CONN=";
             urlQuery += dsn;
             urlQuery += "&DB_NAME=";
             urlQuery += db;
             try {
                 Logger.Debug("Host URL - " + host + " " + page + " " + urlQuery);
                 String urlStr = encodeUrl(host,page,urlQuery);
                 Logger.Debug("Change URL - " + urlStr);
                 urlString = urlStr;
             }   catch (Exception e) {
                 //e.printStackTrace();
                 return null;
             }
         }
 
         return urlString;
     }
 
     // URL encode a webclient path + spec for opening
     private static String constructRequestURL(String spec, String url, String dsn, String db) {
         String urlString = "";
         if (spec != null && spec.length() > 0 &&
             url != null && url.length() > 0) {
             String host = url;
             if (host.endsWith("/"))
                 host = host.substring(0,host.length()-1);
 
             if (host.startsWith("http:"))
                 host = host.substring(7,host.length());
 
             String page = "/dimensions/";
             String urlQuery = "jsp=api&command=opencd&object_id=";
             urlQuery += spec;
             urlQuery += "&DB_CONN=";
             urlQuery += dsn;
             urlQuery += "&DB_NAME=";
             urlQuery += db;
             try {
                 Logger.Debug("Request Host URL - " + host + " " + page + " " + urlQuery);
                 String urlStr = encodeUrl(host,page,urlQuery);
                 Logger.Debug("Request Change URL - " + urlStr);
                 urlString = urlStr;
             }   catch (Exception e) {
                 //e.printStackTrace();
                 return null;
             }
         }
 
         return urlString;
     }
 
     // Encode a URL correctly - handles spaces as %20
     // @param String
     // @param String
     // @param String
     // @return String
     // @throws MalformedURLException
     private static String encodeUrl(String host,String page,String query)
                         throws MalformedURLException, URISyntaxException {
         String urlStr = "";
         if (page != null && page.length() > 0 &&
             host != null && host.length() > 0 &&
             query != null && query.length() > 0) {
             URI uri = new URI("http",host,page,query,null);
             urlStr = uri.toASCIIString();
         }
         return urlStr;
     }
 
     // find items given a directory spec
     static List queryItems(DimensionsConnection connection, Project srcProject,
             String srcPath, Filter filter, int[] attrs, boolean isRecursive, boolean isLatest) {
         // check srcPath validity check srcPath trailing slash do query
         if (srcPath == null) {
             throw new IllegalArgumentException(MISSING_SOURCE_PATH);
         }
         if (srcProject == null) {
             throw new IllegalArgumentException(MISSING_PROJECT);
         }
 
         String path = preProcessSrcPath(srcPath);
         if (!(isRecursive && path.equals(""))) { //$NON-NLS-1$
             filter.criteria().add(
                     new Filter.Criterion(SystemAttributes.ITEMFILE_DIR,
                             (isRecursive ? path + '%' : path), 0));
         }
 
         if (isLatest) {
             filter.criteria().add(
                     new Filter.Criterion(SystemAttributes.IS_LATEST_REV,
                             Boolean.TRUE, 0));
         }
 
         //
         // Catch any exceptions that may be thrown by the Java API and
         // for now return no changes. Going forward it would be good to
         // trap all the possible exception types and do something about them
         //
         try {
             Logger.Debug("Looking for changed files in '" + path + "' in project: " + srcProject.getName());
             List rels = srcProject.getChildItems(filter);
             Logger.Debug("Found " + rels.size());
             if (rels.size()==0)
                 return null;
 
             List items = new ArrayList(rels.size());
             for (int i = 0; i < rels.size(); ++i) {
                 DimensionsRelatedObject rel = (DimensionsRelatedObject) rels.get(i);
                 items.add(rel.getObject());
             }
             BulkOperator bo = connection.getObjectFactory().getBulkOperator(items);
             bo.queryAttribute(attrs);
             return items;
         } catch (Exception e) {
             // e.printStackTrace();
             Logger.Debug("Exception detected from the Java API: " + e.getMessage());
             return null;
         }
     }
 
     // find items given a baseline/directory spec
     static List queryItems(DimensionsConnection connection, Baseline srcBaseline,
             String srcPath, Filter filter, int[] attrs, boolean isRecursive, boolean isLatest) {
         // check srcPath validity check srcPath trailing slash do query
         if (srcPath == null) {
             throw new IllegalArgumentException(MISSING_SOURCE_PATH);
         }
         if (srcBaseline == null) {
             throw new IllegalArgumentException(MISSING_BASELINE);
         }
 
         String path = preProcessSrcPath(srcPath);
         if (!(isRecursive && path.equals(""))) { //$NON-NLS-1$
             filter.criteria().add(
                     new Filter.Criterion(SystemAttributes.ITEMFILE_DIR,
                             (isRecursive ? path + '%' : path), 0));
         }
 
         if (isLatest) {
             filter.criteria().add(
                     new Filter.Criterion(SystemAttributes.IS_LATEST_REV,
                             Boolean.TRUE, 0));
         }
 
         //
         // Catch any exceptions that may be thrown by the Java API and
         // for now return no changes. Going forward it would be good to
         // trap all the possible exception types and do something about them
         //
         try {
             Logger.Debug("Looking for changed files in '" + path + "' in project: " + srcBaseline.getName());
             List rels = srcBaseline.getChildItems(filter);
             Logger.Debug("Found " + rels.size());
             if (rels.size()==0)
                 return null;
 
             List items = new ArrayList(rels.size());
             for (int i = 0; i < rels.size(); ++i) {
                 DimensionsRelatedObject rel = (DimensionsRelatedObject) rels.get(i);
                 items.add(rel.getObject());
             }
             BulkOperator bo = connection.getObjectFactory().getBulkOperator(items);
             bo.queryAttribute(attrs);
             return items;
         } catch (Exception e) {
             // e.printStackTrace();
             Logger.Debug("Exception detected from the Java API: " + e.getMessage());
             return null;
         }
     }
 
     // find items given a request/directory spec
     static boolean queryItems(DimensionsConnection connection, Request request,
             String srcPath, List items, Filter filter, Project srcProject, boolean isRecursive, boolean isLatest) {
         // check srcPath validity check srcPath trailing slash do query
         if (srcPath == null) {
             throw new IllegalArgumentException(MISSING_SOURCE_PATH);
         }
         if (request == null) {
             throw new IllegalArgumentException(MISSING_REQUEST);
         }
 
         Logger.Debug("Looking for items against request "+request.getName());
 
         String path = preProcessSrcPath((srcPath.equals("") ? "/" : srcPath));
         if (!(isRecursive && path.equals(""))) { //$NON-NLS-1$
             filter.criteria().add(
                     new Filter.Criterion(SystemAttributes.ITEMFILE_DIR,
                             (isRecursive ? path + '%' : path), 0));
         }
 
         if (isLatest) {
             filter.criteria().add(
                     new Filter.Criterion(SystemAttributes.IS_LATEST_REV,
                             Boolean.TRUE, 0));
         }
 
         //
         // Catch any exceptions that may be thrown by the Java API and
         // for now return no changes. Going forward it would be good to
         // trap all the possible exception types and do something about them
         //
         try {
             Logger.Debug("Looking for changed files in '" + path + "' in request: " + request.getName());
             request.queryChildItems(filter,srcProject);
             List rels = request.getChildItems(filter);
             Logger.Debug("Found " + rels.size());
             if (rels.size()==0)
                 return true;
 
             for (int i = 0; i < rels.size(); ++i) {
                 Logger.Debug("Processing " + i + "/" + rels.size());
                 DimensionsRelatedObject child = (DimensionsRelatedObject) rels.get(i);
                 if (child != null && child.getObject() instanceof ItemRevision) {
                     Logger.Debug("Found an item");
                     DimensionsObject relType = child.getRelationship();
                     if (SystemRelationship.IN_RESPONSE.equals(relType)) {
                         items.add(child.getObject());
                     }
                 }
             }
             return true;
         } catch (Exception e) {
             // e.printStackTrace();
             Logger.Debug("Exception detected from the Java API: " + e.getMessage());
             return false;
         }
     }
 
 
     /**
      * Flatten the list of related requests
      *
      * @param Request
      * @param List
      * @return boolean
      * @throws DimensionsRuntimeException
      */
      private boolean getDmChildRequests(Request request, List requestList)
                             throws DimensionsRuntimeException {
         try {
             request.flushRelatedObjects(Request.class, true);
             request.queryChildRequests(null);
             List rels = request.getChildRequests(null);
             Logger.Debug("Found " + rels.size());
             if (rels.size()==0)
                 return true;
 
             for (int i=0; i<rels.size(); i++) {
                 Logger.Debug("Processing " + i + "/" + rels.size());
                 DimensionsRelatedObject child = (DimensionsRelatedObject) rels.get(i);
                 if (child != null && child.getObject() instanceof Request) {
                     Logger.Debug("Found a request");
                     DimensionsObject relType = child.getRelationship();
                     if (SystemRelationship.DEPENDENT.equals(relType)) {
                         Logger.Debug("Found a dependent request");
                         requestList.add(child.getObject());
                         if (!getDmChildRequests((Request)child.getObject(), requestList))
                             return false;
                     }
                 } else {
                     Logger.Debug("Related object was null or not a request " + (child != null));
                 }
             }
             return true;
         } catch (Exception e) {
             // e.printStackTrace();
             Logger.Debug("Exception detected from the Java API: " + e.getMessage());
             throw new DimensionsRuntimeException("getDmChildRequests - encountered a Java API exception");
         }
     }
 
     /**
      * Runs a Dimensions command.
      *
      * @param connection
      *            the connection for which to run the command
      * @param cmd
      *            the command line to run
      * @throws Exception
      *             if the command failed
      * @throws IllegalArgumentException
      *             if the command string was null or an emptry dtring
      *
      */
     static DimensionsResult run(DimensionsConnection connection, String cmd)
             throws IllegalArgumentException, DimensionsRuntimeException {
         if (cmd == null || cmd.equals("")) { //$NON-NLS-1$
             throw new IllegalArgumentException(NO_COMMAND_LINE);
         }
         Logger.Debug("Running the command '" + cmd + "'...");
 
         try {
             DimensionsObjectFactory dof = connection.getObjectFactory();
             DimensionsResult res = dof.runCommand(cmd);
             return res;
         } catch (Exception e) {
             Logger.Debug("Command failed to run");
             throw new DimensionsRuntimeException("Dimension command failed - " + e.getMessage());
         }
     }
 
 
     /**
      * Convert the human-readable <code>dateType</code> into a DMClient
      * attribute name.
      * <p>
      * Defaults to
      * {@link com.serena.dmclient.api.SystemAttributes#CREATION_DATE} if it is
      * not recognized.
      *
      * @param dateType
      *            created, updated, revised or actioned.
      * @return the corresponding field value from
      *         {@link com.serena.dmclient.api.SystemAttributes}
      */
     static int getDateTypeAttribute(String dateType) {
         int ret = SystemAttributes.CREATION_DATE;
         if (dateType != null) {
             if (dateType.equalsIgnoreCase("edit")) { //$NON-NLS-1$
                 ret = SystemAttributes.LAST_UPDATED_DATE;
             } else if (dateType.equalsIgnoreCase("actioned")) { //$NON-NLS-1$
                 ret = SystemAttributes.LAST_ACTIONED_DATE;
             } else if (dateType.equalsIgnoreCase("revised")) { //$NON-NLS-1$
                 ret = SystemAttributes.UTC_MODIFIED_DATE;
             } else if (dateType.equalsIgnoreCase("add")) { //$NON-NLS-1$
                 ret = SystemAttributes.CREATION_DATE;
             }
         }
         return ret;
     }
 
     // database times are in Oracle format, in a specified timezone
     static String formatDatabaseDate(Date date, TimeZone timeZone) {
         return (timeZone == null) ? DateUtils.format(date) : DateUtils.format(date, timeZone);
     }
 
     // database times are in Oracle format, in a specified timezone
     static Date parseDatabaseDate(String date, TimeZone timeZone) {
         return (timeZone == null) ? DateUtils.parse(date) : DateUtils.parse(date, timeZone);
     }
 }
