 /**********************************************************************************
  * $URL:https://source.sakaiproject.org/svn/osp/trunk/reports/api-impl/src/java/org/theospi/portfolio/reports/model/impl/ReportsManagerImpl.java $
  * $Id:ReportsManagerImpl.java 9134 2006-05-08 20:28:42Z chmaurer@iupui.edu $
  ***********************************************************************************
  *
  * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
  *
  * Licensed under the Educational Community License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *       http://www.osedu.org/licenses/ECL-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  **********************************************************************************/
 package org.sakaiproject.reports.logic.impl;
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.hibernate.HibernateException;
 import org.hibernate.Transaction;
 import org.jdom.CDATA;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.XMLOutputter;
 import org.jdom.transform.JDOMResult;
 import org.jdom.transform.JDOMSource;
 import org.quartz.*;
 import org.sakaiproject.api.app.scheduler.JobBeanWrapper;
 import org.sakaiproject.api.app.scheduler.SchedulerManager;
 import org.sakaiproject.authz.api.SecurityService;
 import org.sakaiproject.authz.cover.FunctionManager;
 import org.sakaiproject.component.app.scheduler.jobs.SpringJobBeanWrapper;
 import org.sakaiproject.component.cover.ComponentManager;
 import org.sakaiproject.component.cover.ServerConfigurationService;
 import org.sakaiproject.content.api.ContentCollection;
 import org.sakaiproject.content.api.ContentCollectionEdit;
 import org.sakaiproject.content.api.ContentHostingService;
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.entity.api.ResourceProperties;
 import org.sakaiproject.entity.api.ResourcePropertiesEdit;
 import org.sakaiproject.event.cover.NotificationService;
 import org.sakaiproject.exception.*;
 import org.sakaiproject.metaobj.security.AuthenticationManager;
 import org.sakaiproject.metaobj.security.AuthorizationFacade;
 import org.sakaiproject.metaobj.security.AuthorizationFailedException;
 import org.sakaiproject.metaobj.security.Authorization;
 import org.sakaiproject.metaobj.security.model.AuthZMap;
 import org.sakaiproject.metaobj.shared.mgt.IdManager;
 import org.sakaiproject.metaobj.shared.model.Agent;
 import org.sakaiproject.metaobj.shared.model.Id;
 import org.sakaiproject.metaobj.shared.model.IdImpl;
 import org.sakaiproject.metaobj.shared.model.MimeType;
 import org.sakaiproject.metaobj.worksite.mgt.WorksiteManager;
 import org.sakaiproject.site.api.Site;
 import org.sakaiproject.site.cover.SiteService;
 import org.sakaiproject.tool.api.Session;
 import org.sakaiproject.tool.api.ToolSession;
 import org.sakaiproject.tool.cover.SessionManager;
 import org.sakaiproject.tool.cover.ToolManager;
 import org.sakaiproject.user.api.User;
 import org.sakaiproject.user.api.UserDirectoryService;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.BeanFactoryAware;
 import org.springframework.beans.factory.ListableBeanFactory;
 import org.springframework.beans.factory.xml.XmlBeanFactory;
 import org.springframework.core.io.ByteArrayResource;
 import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
 import org.sakaiproject.metaobj.security.impl.AllowAllSecurityAdvisor;
 import org.sakaiproject.reports.service.ReportExecutionException;
 import org.sakaiproject.reports.service.ReportFunctions;
 import org.sakaiproject.reports.service.ReportsManager;
 import org.sakaiproject.reports.service.ResultProcessor;
 import org.sakaiproject.reports.model.*;
 
 import javax.faces.context.FacesContext;
 import javax.servlet.http.HttpServletResponse;
 import javax.sql.DataSource;
 import javax.xml.transform.*;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import java.io.*;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.sakaiproject.time.api.Time;
 import org.sakaiproject.util.Validator;
 
 import java.lang.reflect.Method;
 
 /**
  * This class is a singleton that manages the reports on a general basis
  * <p/>
  * <p/>
  * When getting the reports a user can run this class checks the
  * "sakai.reports.useWarehouse" sakai.properties property.  0 is no reports. 1 is
  * the warehouse reports. 2 is live data reports.  and 3 is both warehouse and
  * live data reports.  The default is 1.  The default report has a setting of
  * operating on the warehouse.
  * <p/>
  * The dataSource is for the data warehouse data source.  If it is set then that
  * source is used.  If it is not set then the code tries to load in the data warehouse
  * dataSource.  It does this because that is the default dw dataSource.  The
  * data warehouse is not deployed then the dw dataSource won't exist.  If it is referenced
  * in the components.xml then there would be errors at startup.  Thus we don't reference
  * it there and programmatically pull it.  This way it could be null (when dw is not
  * deployed) and then the dataSource falls back to the sakai dataSource.
  * <p/>
  * the sakai.properties property "sakai.reports.forceColumnLabelUppercase" is used to standardize
  * the column label.  MySQL will keep the column titles exactly as specified in the query.
  * Oracle on the other hand seems to make all the column labels uppercase.  This makes writing
  * a query and an xsl that works in both databases more difficult.  This defaults to 1 (otherwise know as true)
  * Set this property to 0 and the column titles will pass through like the old behavior
  *
  * @author andersjb
  */
 public class ReportsManagerImpl extends HibernateDaoSupport implements ReportsManager, BeanFactoryAware {
     /**
      * Enebles logging
      */
 	
     private AuthenticationManager authnManager;
     /**
      * The global list of reports
      */
     private List reportDefinitions;
 
     /**
      * Class for converting a Id string to an Id class
      */
     private IdManager idManager = null;
 
     /**
      * the sakai class that manages permissions
      */
     private AuthorizationFacade authzManager;
 
     /**
      * The class that generates the database connection.  it is the data warehouse data source
      */
     private DataSource dataSource = null;
 
     /**
      * The class that generates the database connection.  it is the sakai data source
      */
     private DataSource sakaiDataSource = null;
 
     /**
      * an internal variable for whether or not the database connection should be closed after its use
      */
     private boolean canCloseConnection = true;
 
     /**
      * used to hash a reference so the hash isn't straight from the reference
      */
     private String secretKey = "sakai_reports";
 
     /**
      * used to allow artifacts to be downloaded (through adding an advisor)
      */
     private SecurityService securityService;
 
     private JobBeanWrapper jobBeanWrapper = null;
     /**
      * This is used to standardize the case of the column labels. This is helpful because
      * MySQL uses the same case as determined by the query.  Oracle makes them all uppercase.
      * This makes it easier to write the xsl in a database agnostic way
      */
     private Boolean forceColumnLabelUppercase;
 
     private SchedulerManager schedulerManager;
 
    private boolean autoDdl = true;
    
    private boolean upgrade24 = false;
 
     protected BeanFactory beanFactory;
     public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
         this.beanFactory = beanFactory;
     }
 
     /**
      * convert between the user formatted date and the database formatted date
      */
     private static SimpleDateFormat userDateFormat = new SimpleDateFormat("MM/dd/yyyy");
     private static SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd");
 
     /**
      * Tells us if the global database reportDefinitions was loaded
      */
     private ContentHostingService contentHosting;
 
     public List definedDefintions;
     /**
      * the name of key in the session into which the result is saved into
      */
     private static final String CURRENT_RESULTS_TAG = "org.sakaiproject.reports.service.ReportsManager.currentResults";
 
     /**
      * Called on after the startup of the singleton.  This sets the global
      * list of functions which will have permission managed by sakai
      *
      * @throws Exception
      */
     protected void init() throws Exception {
       logger.info("init() ReportsManagerImpl");
       // register functions
       FunctionManager.registerFunction(ReportFunctions.REPORT_FUNCTION_CREATE);
       FunctionManager.registerFunction(ReportFunctions.REPORT_FUNCTION_RUN);
       FunctionManager.registerFunction(ReportFunctions.REPORT_FUNCTION_VIEW);
       FunctionManager.registerFunction(ReportFunctions.REPORT_FUNCTION_EDIT);
       FunctionManager.registerFunction(ReportFunctions.REPORT_FUNCTION_DELETE);
       FunctionManager.registerFunction(ReportFunctions.REPORT_FUNCTION_SHARE);
       
       if (isUpgrade24()) {
          convert24to25Reports();
       }
       
     }
 
 
    public void clientInit() {
       if (isAutoDdl()) {
          initDefinedReportDefinitions();
       }
    }
 
    public AuthenticationManager getAuthnManager() {
        return authnManager;
    }
 
     public void setAuthnManager(AuthenticationManager authnManager) {
         this.authnManager = authnManager;
     }
 
     public BeanFactory getBeanFactory() {
         return beanFactory;
     }
 
     public void setParentBeanFactory(BeanFactory beanFactory) {
         this.beanFactory = beanFactory;
     }
 
     /**
      * {@inheritDoc}
      */
     public void setReportDefinitions(List reportDefinitions) {
         List reportDefs = new ArrayList();
         Iterator iter = reportDefinitions.iterator();
         while (iter.hasNext()) {
             ReportDefinition rd = (ReportDefinition) iter.next();
             rd.finishLoading();
             reportDefs.add(rd);
         }
         this.reportDefinitions = reportDefs;
     }
 
 
     public List getReportDefinitions() {
 
         //load any reportDefinitions in the database
         List reportsDefs = loadReportsFromDB();
         setReportDefinitions(reportsDefs);
         return this.reportDefinitions;
     }
 
     public boolean isValidRole(String roleStr) {
         if (roleStr != null && roleStr.length() > 0) {
             String currentRole = getCurrentSite().getMember(SessionManager.getCurrentSessionUserId()).getRole().getId().toString();
             String []roles = roleStr.split(",");
             for (int i = 0; i < roles.length; i++) {
                 String role = roles[i];
                 if (role.trim().equals(currentRole)) {
                     return true;
                 }
             }
         } else {
             return true;
         }
         return false;
 
     }
 
     public boolean isValidWorksiteType(String typesStr) {
         if (typesStr != null && typesStr.length() > 0) {
             String []types = typesStr.split(",");
             for (int i = 0; i < types.length; i++) {
                 String type = types[i];
                 if (type.trim().equals(getCurrentSiteType())) {
                     return true;
                 }
                else if (type.trim().equals(getCurrentSite().getId())) {
                    return true;
                 }
             }
         } else {
             return true;
         }
         return false;
 
     }
 
     /**
      * Given the param of whether or not the report is using the warehouse, should it be displayed is returned.
      * This works off the "sakai.reports.useWarehouse" sakai.properties property.  If there is no property
      * then we will only show the warehouse reports.
      * <p/>
      * If the input is null, then we automatically assume that it is using the warehouse (set to true).
      * <p/>
      * If the property is 0 then we don't show any report.  If bit 0 of the property is set then show
      * the data warehouse reports.  If bit 1 of the property is set then show the direct reports.  aka.
      * 0=no reports, 1= warehouse reports, 2= live data reports, 3= warehouse and live data reports
      *
      * @param usesWarehouse
      * @return true if report should be displayed
      */
     protected boolean hasWarehouseSetting(Boolean usesWarehouse) {
         int warehousePref = ServerConfigurationService.getInt("sakai.reports.useWarehouse", 1);
 
        if (warehousePref == 0) {
           return false;
        }
 
        if (usesWarehouse == null) {
           usesWarehouse = Boolean.TRUE;
        }
 
         // if bit 0 is set, show warehouse reports
        if ((warehousePref & 1) != 0 && usesWarehouse.booleanValue() == true) {
           return true;
        }
 
        if ((warehousePref & 2) != 0 && usesWarehouse.booleanValue() == false) {
           return true;
        }
 
         return false;
     }
 
     /**
      * This is the setter for the idManager
      */
     public void setIdManager(IdManager idManager) {
         this.idManager = idManager;
     }
 
 
     /**
      * This is the getter for the idManager
      *
      * @return IdManager
      */
     public IdManager getIdManager() {
         return idManager;
     }
 
 
     /**
      * This is the setter for the idManager
      */
     public void setDataSource(DataSource dataSource) {
         this.dataSource = dataSource;
     }
 
 
     /**
      * {@inheritDoc}
      */
     public DataSource getDataSource() {
         configureDataSource();
 
         int warehousePref = ServerConfigurationService.getInt("sakai.reports.useWarehouse", 1);
 
        if (warehousePref == 1) {
           return dataSource;
        } else if (warehousePref == 2) {
           return sakaiDataSource;
        }
 
         throw new RuntimeException("Tried to get the report data source but the source was ambiguous.");
     }
 
 
     /**
      * {@inheritDoc}
      */
     public DataSource getDataSourceUseWarehouse(boolean useWarehouse) {
         configureDataSource();
 
        if (useWarehouse) {
           return dataSource;
        } else {
           return sakaiDataSource;
        }
     }
 
 
     /**
      * This function sets up the data warehouse data source.  If the dataSource exists then nothing changes.
      * Thus if the dataSource is set in the components.xml then it will use that for the data warehouse
      * data source.  Also if the dataSource has already been set up then this is skipped.
      * <p/>
      * So, if there is no dataSource set in the components.xml then we will default to the data warehouse
      * defined data source.  This is needed because the data warehouse may not be loaded and thus the dataSource
      * bean wouldn't be defined.  If we reference the dw dataSource when it doesn't exist then problems can
      * happen during startup.  Thus we load the dw dataSource dynamically.  If the dw dataSource doesn't
      * exist then we skip to the sakai dataSource.
      */
     private void configureDataSource() {
         if (dataSource == null) {
             dataSource = (DataSource) ComponentManager.get("org.sakaiproject.warehouse.service.DataWarehouseManager.dataSource");
            if (dataSource == null) {
               dataSource = sakaiDataSource;
            }
         }
     }
 
 
     /**
      * {@inheritDoc}
      */
     public List getCurrentUserResults() {
         Session s = SessionManager.getCurrentSession();
 
         boolean runReports = can(ReportFunctions.REPORT_FUNCTION_RUN);
         boolean viewReports = can(ReportFunctions.REPORT_FUNCTION_VIEW);
 
         List returned = new ArrayList();
 
         if (viewReports | runReports) {
             List results = getHibernateTemplate().findByNamedQuery("findResultsByUser", s.getUserId());
 
             Iterator iter = results.iterator();
             while (iter.hasNext()) {
                 ReportResult r = (ReportResult) iter.next();
 
                 r.setIsSaved(true);
                 r.setOwner(true);
             }
             returned.addAll(results);
         }
 
         if (runReports) {
             List liveReports = getHibernateTemplate().findByNamedQuery("findReportsByUser", s.getUserId());
 
             Iterator iter = liveReports.iterator();
             while (iter.hasNext()) {
                 Report r = (Report) iter.next();
 
                 r.getReportParams().size();
 
                 r.connectToDefinition(getReportDefinitions());
                 r.setIsSaved(true);
             }
 
             returned.addAll(liveReports);
         }
 
         return returned;
     }
 
 
     /**
      * Loads the global database reportDefinitions if they haven't been loaded yet
      * This is a stub.
      */
     private List loadReportsFromDB() {
         List reportDefArray = new ArrayList();
             List reportDefs = getHibernateTemplate().findByNamedQuery("findReportDefinitionFiles");
 
             for (Iterator i = reportDefs.iterator(); i.hasNext();) {
                 ReportDefinitionXmlFile xmlFile = (ReportDefinitionXmlFile) i.next();
 
                 ListableBeanFactory beanFactory = new XmlBeanFactory(new ByteArrayResource(xmlFile.getXmlFile()), getBeanFactory());
                 ReportDefinition repDef = getReportDefBean(beanFactory);
                 repDef.finishLoading();
                 repDef.setDbLoaded(true);
                 if (isValidWorksiteType(repDef.getSiteType()) && isValidRole(repDef.getRole()) && hasWarehouseSetting(repDef.getUsesWarehouse()))
                 {
                     reportDefArray.add(repDef);
                 }
             }
         return reportDefArray;
 
 
     }
 
 
     /**
      * {@inheritDoc}
      */
     public ReportResult loadResult(ReportResult result) {
         ReportResult reportResult =
                 (ReportResult) getHibernateTemplate().get(
                         ReportResult.class,
                         result.getResultId()
                 );
 
         //load the report too
         Report report = reportResult.getReport();
 
         String function = report.getIsLive() ?
                 ReportFunctions.REPORT_FUNCTION_RUN : ReportFunctions.REPORT_FUNCTION_VIEW;
 
         getAuthzManager().checkPermission(function,
                 getIdManager().getId(ToolManager.getCurrentPlacement().getId()));
 
         //set the report and report result to that of already been saved
         reportResult.setIsSaved(true);
         report.setIsSaved(true);
 
         //link the report deinition
         report.connectToDefinition(getReportDefinitions());
 
 
         reportResult.setReport(report);
 
         //give back the result
         return reportResult;
     }
 
 
     /**
      * {@inheritDoc}
      */
     public String getReportResultKey(ReportResult result, String ref) {
         String hashCode = DigestUtils.md5Hex(ref + getSecretKey());
 
         return hashCode;
     }
 
 
     /**
      * {@inheritDoc}
      */
     public void checkReportAccess(String id, String ref) {
         String hashCode = DigestUtils.md5Hex(ref + getSecretKey());
 
         if (!hashCode.equals(id)) {
             throw new AuthorizationFailedException();
         }
 
         getSecurityService().pushAdvisor(new AllowAllSecurityAdvisor());
     }
 
     public void setCurrentResult(ReportResult result) {
         ToolSession session = SessionManager.getCurrentToolSession();
         session.setAttribute(CURRENT_RESULTS_TAG, result);
     }
 
     /**
      * Pulls the ReportResults out of the session
      *
      * @return ReportResult
      */
     public ReportResult getCurrentResult() {
         ToolSession session = SessionManager.getCurrentToolSession();
         return (ReportResult) session.getAttribute(CURRENT_RESULTS_TAG);
     }
 
     /**
      * Given an id, this method finds and returns the ReportDefinition
      *
      * @param Id
      * @return ReportDefinition
      */
     public ReportDefinition findReportDefinition(String Id) {
         Iterator iter = getReportDefinitions().iterator();
 
         while (iter.hasNext()) {
             ReportDefinition rd = (ReportDefinition) iter.next();
            if (rd.getIdString().equals(Id)) {
               return rd;
            }
         }
         return null;
     }
 
 //	*************************************************************************
     //	*************************************************************************
     //			The process functions (non-getter/setter)
 
 
     /**
      * {@inheritDoc}
      */
     public void createReportParameters(Report report) {
         List reportDefParams = report.getReportDefinition().getReportDefinitionParams();
         ArrayList reportParams = new ArrayList(reportDefParams.size());
 
         Iterator iter = reportDefParams.iterator();
 
         while (iter.hasNext()) {
             ReportDefinitionParam rdp = (ReportDefinitionParam) iter.next();
 
             ReportParam rp = new ReportParam();
 
             rp.setReportDefinitionParam(rdp);
             rp.setReport(report);
 
             //	if the parameter is static then copy the value, otherwise it is filled by user
            if (rdp.getValueType().equals(ReportDefinitionParam.VALUE_TYPE_STATIC)) {
               rp.setValue(replaceSystemValues(rdp.getValue()));
            }
             reportParams.add(rp);
         }
         report.setReportParams(reportParams);
     }
 
     /**
      * Does a test to ensure that the parameters are valid
      * One can get to the parameter definitions through the
      * report parameter.
      *
      * @param parameters a Collection of ReportParam
      */
     public boolean validateParameters(Collection parameters) {
         return true;
     }
 
 
     /**
      * {@inheritDoc}
      */
     public Report createReport(ReportDefinition reportDefinition) {
         getAuthzManager().checkPermission(ReportFunctions.REPORT_FUNCTION_CREATE,
                 getIdManager().getId(ToolManager.getCurrentPlacement().getId()));
 
         Report report = new Report(reportDefinition);
 
         //Create the report parameters
         createReportParameters(report);
 
         Session s = SessionManager.getCurrentSession();
         report.setUserId(s.getUserId());
         report.setCreationDate(new Date());
 
         return report;
     }
 
     /**
      * This function generates the sql connection.
      * If the dataSource connection fails then we want to fail over to
      * the hibernate session connection.  If the usesWarehouse param is null then
      * the connection should default to use the warehouse
      *
      * @return Connection
      * @throws HibernateException
      * @throws SQLException
      */
     public Connection getConnection(Boolean useWarehouse) throws HibernateException, SQLException {
         Connection con = null;
 
        if (useWarehouse == null) {
           con = getDataSourceUseWarehouse(true).getConnection();
        } else {
           con = getDataSourceUseWarehouse(useWarehouse.booleanValue()).getConnection();
        }
 
         canCloseConnection = true;
 
         //fail over to the session connection
         if (con == null) {
             org.hibernate.Session session = getSession();
 
             con = session.connection();
             //as of hibernate 3.1 you must close your connections
             //http://www.hibernate.org/250.html
             canCloseConnection = true;
         }
 
         return con;
     }
 
     /**
      * This closes the database connection if it was pulled from the
      * data warehouse.  (IOW, doesn't close if the connection came from
      * the hibernate session)
      *
      * @param connection
      * @throws SQLException
      * @deprecated ? should this method even be used now that both sources require connections to be closed?
      */
     public void closeConnection(Connection connection) throws SQLException {
        if (canCloseConnection) {
           connection.close();
        }
     }
 
     protected String escapeCommas(String value) { 
         return value.replaceAll(",", "<<comma>>"); 
     } 
 
     /**
      * gathers the data for dropdown/list box.
      *
      * @return String
      */
     public String generateSQLParameterValue(ReportParam reportParam) {
         Connection connection = null;
         PreparedStatement stmt = null;
         ResultSet rs = null;
         String results = "[]";
         StringBuilder strbuffer = new StringBuilder();
         try {
             connection = getConnection(reportParam.getReportDefinitionParam().getReportDefinition().getUsesWarehouse());
             stmt = connection
                     .prepareStatement(replaceSystemValues(reportParam.getReportDefinitionParam().getValue()));
 
             rs = stmt.executeQuery();
             strbuffer.append("[");
             int columns = rs.getMetaData().getColumnCount();
             while (rs.next()) {
                if (columns >= 2) {
                   strbuffer.append("(");
                }
                if (columns >= 1) {
                   strbuffer.append(escapeCommas(rs.getString(1)));
                }
                 if (columns >= 2) {
                     strbuffer.append(";");
                     strbuffer.append(escapeCommas(rs.getString(2)));
                     strbuffer.append(")");
                 }
                 strbuffer.append(",");
             }
             strbuffer.append("]");
             results = strbuffer.toString();
         } catch (SQLException e) {
             logger.error("", e);
             throw new RuntimeException(e);
         } catch (HibernateException e) {
             logger.error("", e);
             throw new RuntimeException(e);
         }
         finally {
             //ensure that the results set is clsoed
             if (rs != null) {
                 try {
                     rs.close();
                 } catch (SQLException e) {
                     if (logger.isDebugEnabled()) {
                         logger.debug("loadArtifactTypes(String, Map) caught " + e);
                     }
                 }
             }
             //ensure that the stmt is closed
             if (stmt != null) {
                 try {
                     stmt.close();
                 } catch (SQLException e) {
                     if (logger.isDebugEnabled()) {
                         logger.debug("loadArtifactTypes(String, Map) caught " + e);
                     }
                 }
             }
             if (connection != null) {
                 try {
                     closeConnection(connection);
                 } catch (Exception e) {
                     if (logger.isDebugEnabled()) {
                         logger.error("", e);
                     }
                 }
             }
         }
         return results;
     }
 
     /**
      * gets the xsl tranform file, does the transform on the Results,
      * then applies the post processor.
      * <p/>
      * The result is a file being placed into the output Stream.
      *
      * @param params
      * @param out
      * @throws IOException
      */
     public String packageForDownload(Map params, OutputStream out) throws IOException {
         ReportResult result = getCurrentResult();
 
         String exportResultsId = ((String[]) params.get(EXPORT_XSL_ID))[0];
         ReportXsl xslt = result.getReport().getReportDefinition().findReportXslByRuntimeId(exportResultsId);
         xslt.setReportDefinition(result.getReport().getReportDefinition());
         String fileData = transform(result, xslt);
 
         if (xslt.getResultsPostProcessor() != null) {
             out.write(xslt.getResultsPostProcessor().postProcess(fileData));
         } else {
             out.write(fileData.getBytes());
         }
 
         //Blank filename for now -- no more dangerous, since the request is in the form of a filename
         return "";
 
     }
 
 
     /**
      * {@inheritDoc}
      */
     public ReportResult generateResults(Report report) throws ReportExecutionException {
         ReportResult rr = new ReportResult();
 
         ReportDefinition rd = report.getReportDefinition();
         if (rd == null){
             report.connectToDefinition(getReportDefinitions());
             rd = report.getReportDefinition();
         }
         rr.setCreationDate(new Date());
         Element reportElement = new Element("reportResult");
         Document document = new Document(reportElement);
 
         //	replace the parameters with the values
         List reportParams = report.getReportParams();
 
         int index = -1;
         for (Iterator i = rd.getQuery().iterator(); i.hasNext();) {
                 StringBuilder nextQuery = new StringBuilder(replaceSystemValues((String) i.next()));
             if (index > -1) {
                 // <extraReportResult index="0">, <extraReportResult index="1"> etc.
                 Element currentReportResult = new Element("extraReportResult");
                 currentReportResult.setAttribute("index", String.valueOf(index));
                 reportElement.addContent(currentReportResult);
                 executeQuery(nextQuery, reportParams, report, currentReportResult, rr, rd, false);
             } else {
                 executeQuery(nextQuery, reportParams, report, reportElement, rr, rd, true);
             }
             index++;
         }
 
         rr.setCreationDate(new Date());
         rr.setReport(report);
         rr.setTitle(report.getTitle());
         rr.setKeywords(report.getKeywords());
         rr.setDescription(report.getDescription());
         rr.setUserId(report.getUserId());
         rr.setXml((new XMLOutputter()).outputString(document));
 
         rr = postProcessResult(rd, rr);
 
         return rr;
 
     }
 
     /**
      * executes the query converting the results into xml and updating the report
      *
      * @param query
      * @param reportParams
      * @param report
      * @param reportElement
      * @param rr
      * @param rd
      * @param isFirstResult - true if this is the first sql query in the report, otherwise this should be false
      */
     protected void executeQuery(StringBuilder query, List reportParams, Report report, Element reportElement,
                                 ReportResult rr, ReportDefinition rd, boolean isFirstResult) {
         //     get the query from the Definition and replace the values
         //     no should be able to put in a system parameter into a report parameter and have it work
         //             so replace the system values before processing the report parameters
         Connection connection = null;
         PreparedStatement stmt = null;
 
         try {
             connection = getConnection(report.getReportDefinition().getUsesWarehouse());
 
             query = replaceForMultiSet(query, reportParams);
             stmt = connection.prepareStatement(query.toString());
             //If there are params, place them with values in the query
             if (reportParams != null) {
                 Iterator iter = reportParams.iterator();
                 int paramIndex = 0;
 
                 //	loop through all the parameters and find in query for replacement
                 while (iter.hasNext()) {
 
                     //	get the paremeter and associated parameter definition
                     ReportParam rp = (ReportParam) iter.next();
                     ReportDefinitionParam rdp = rp.getReportDefinitionParam();
                     if (ReportDefinitionParam.VALUE_TYPE_MULTI_OF_SET.equals(rdp.getValueType()) ||
                             ReportDefinitionParam.VALUE_TYPE_MULTI_OF_QUERY.equals(rdp.getValueType())) {
 
                         for (Iterator i = rp.getListValue().iterator(); i.hasNext();) {
                             stmt.setString(paramIndex + 1, i.next().toString());
                             paramIndex++;
                         }
 
                     } else if (rp.getValue() == null) {
                         throw new RuntimeException("The Report Parameter Value was blank.  Offending parameter: " + rdp.getParamName());
                     } else {
                         String value = rp.getValue();
 
                         //	Dates need to be formatted from user format to database format
                         if (ReportDefinitionParam.TYPE_DATE.equals(rdp.getType())) {
                             value = dbDateFormat.format(userDateFormat.parse(rp.getValue()));
                             if ("oracle".equals(rd.getVendor())){
                             SimpleDateFormat oracleFormat = new SimpleDateFormat("dd-MMM-yyyy");
                             value = oracleFormat.format(userDateFormat.parse(rp.getValue()));
                         }
                         }
                         stmt.setString(paramIndex + 1, value);
                         paramIndex++;
                     }
 
                 }
             }
 
             // run the query
             ResultSet rs = null;
             int resultSetIndex = 0;
 
 
             rs = stmt.executeQuery();
 
             boolean makeUppercase = true;
            if (forceColumnLabelUppercase != null) {
               makeUppercase = forceColumnLabelUppercase.booleanValue();
            }
 
             String forceProperty = ServerConfigurationService.getString("sakai.reports.forceColumnLabelUppercase");
            if (forceProperty != null && forceProperty.length() > 0) {
               makeUppercase = Integer.parseInt(forceProperty) == 1;
            }
 
             int columns = rs.getMetaData().getColumnCount();
 
             String []columnNames = new String[columns];
 
             for (int i = 0; i < columns; i++) {
                 columnNames[i] = rs.getMetaData().getColumnLabel(i + 1);
                if (makeUppercase) {
                   columnNames[i] = columnNames[i].toUpperCase();
                }
             }
 
             if (isFirstResult) {
                 Element docAttrNode = new Element("attributes");
                 Element attr = new Element("title");
                 attr.setText(report.getTitle());
                 reportElement.addContent(attr);
 
                 attr = new Element("description");
                 attr.setText(report.getDescription());
                 reportElement.addContent(attr);
 
                 attr = new Element("keywords");
                 attr.setText(report.getKeywords());
                 reportElement.addContent(attr);
 
                 attr = new Element("runDate");
                 attr.setText(rr.getCreationDate().toString());
                 reportElement.addContent(attr);
 
                 attr = new Element("isWarehouseReport");
                 attr.setText(report.getReportDefinition().getUsesWarehouse().toString());
                 reportElement.addContent(attr);
 
                 attr = new Element("isLiveReport");
                 attr.setText(Boolean.toString(report.getIsLive()));
                 reportElement.addContent(attr);
 
                 attr = new Element("isSavedReport");
                 attr.setText(Boolean.toString(report.getIsSaved()));
                 reportElement.addContent(attr);
 
                 attr = new Element("accessUrl");
                 attr.setText(ServerConfigurationService.getAccessUrl());
                 reportElement.addContent(attr);
                 reportElement.addContent(docAttrNode);
             }
 
             Element paramsNode = new Element("parameters");
 
             if (reportParams != null) {
                 Iterator iter = report.getReportParams().iterator();
 
                 //	loop through all the parameters
                 while (iter.hasNext()) {
 
                     //	get the paremeter and associated parameter definition
                     ReportParam rp = (ReportParam) iter.next();
                     ReportDefinitionParam rdp = rp.getReportDefinitionParam();
 
                     Element paramNode = new Element("parameter");
 
                     paramNode.setAttribute("title", rdp.getTitle());
                     paramNode.setAttribute("name", rdp.getParamName());
                     paramNode.setAttribute("type", rdp.getType());
 
                     paramNode.setText(rp.getValue());
 
                     paramsNode.addContent(paramNode);
                 }
             }
             reportElement.addContent(paramsNode);
 
 
             Element columnsNode = new Element("columns");
             for (int i = 0; i < columnNames.length; i++) {
 
                 Element column = new Element("column");
                 column.setAttribute("colIndex", "" + i);
                 column.setAttribute("title", columnNames[i]);
                 columnsNode.addContent(column);
             }
             reportElement.addContent(columnsNode);
 
             Element datarowsNode = new Element("data");
             while (rs.next()) {
 
                 Element dataRow = new Element("datarow");
 
                 dataRow.setAttribute("index", "" + resultSetIndex++);
                 datarowsNode.addContent(dataRow);
 
                 for (int i = 0; i < columns; i++) {
 
                     String data = rs.getString(i + 1);
 
                     Element columnNode = new Element("element");
 
                     dataRow.addContent(columnNode);
 
                     columnNode.setAttribute("colIndex", "" + i);
                     columnNode.setAttribute("colName", columnNames[i]);
 
                     if (data == null) {
                         columnNode.setAttribute("isNull", "true");
                         data = "";
                     }
                     columnNode.addContent(new CDATA(data));
                 }
             }
             reportElement.addContent(datarowsNode);
 
         } catch (SQLException e) {
             logger.error("", e);
             throw new RuntimeException(e);
         } catch (ParseException e) {
             logger.error("", e);
             throw new ReportExecutionException(e);
         } catch (HibernateException e) {
             logger.error("", e);
             throw new RuntimeException(e);
         } finally {
             try {
                 stmt.close();
             } catch (Exception e) {
                 logger.error("", e);
             }
             try {
                 closeConnection(connection);
             } catch (Exception e) {
                 logger.error("", e);
             }
         }
 
     }
 
     /**
      * applies all the post processing filters and returns the processed results
      *
      * @param rd
      * @param rr
      * @return results
      */
     protected ReportResult postProcessResult(ReportDefinition rd, ReportResult rr) {
         List resultProcessors = rd.getResultProcessors();
         if (resultProcessors != null) {
             for (Iterator i = resultProcessors.iterator(); i.hasNext();) {
                 ResultProcessor processor = (ResultProcessor) i.next();
                 rr = processor.process(rr);
             }
         }
         return rr;
     }
 
     public StringBuilder replaceForMultiSet(StringBuilder inQuery, List reportParams) {
         if (reportParams == null) {
             return inQuery;
         }
         Iterator iter = reportParams.iterator();
         //	loop through all the parameters and find in query for replacement
         while (iter.hasNext()) {
 
             //	get the paremeter and associated parameter definition
             ReportParam rp = (ReportParam) iter.next();
             ReportDefinitionParam rdp = rp.getReportDefinitionParam();
             if (ReportDefinitionParam.VALUE_TYPE_MULTI_OF_SET.equals(rdp.getValueType()) ||
                     ReportDefinitionParam.VALUE_TYPE_MULTI_OF_QUERY.equals(rdp.getValueType())) {
 
 
                 if (rp.getListValue().size() > 1) {
                     int index = inQuery.indexOf("(?)");
                     inQuery.delete(index, index + 3);
                     StringBuilder tempString = new StringBuilder("(");
                     for (int i = 0; i < rp.getListValue().size(); i++) {
                         tempString.append("?,");
                     }
                     tempString.delete(tempString.length() - 1, tempString.length());
                     tempString.append(") ");
                     inQuery.insert(index, tempString);
                 }
             }
         }
         return inQuery;
     }
     
     /**
      * {@inheritDoc}
      */
     public String replaceSystemValues(String inString) {
     	UserDirectoryService userDirectoryService = org.sakaiproject.user.cover.UserDirectoryService.getInstance();
     	
         Session s           = SessionManager.getCurrentSession();
         User    user        = userDirectoryService.getCurrentUser();
         String  worksiteId  = ToolManager.getCurrentPlacement().getContext();  // current site id
         Site    site        = getCurrentWorksite(worksiteId);
         
         Map map = new HashMap();
         map.put("{userid}", Validator.escapeSql(s.getUserId()));
         //system values are stored in session if report is scheduled through quartz
         if (s.getAttribute("toolid") == null){
             UserDirectoryService dirServ = org.sakaiproject.user.cover.UserDirectoryService.getInstance();
             User u = dirServ.getCurrentUser();
             map.put("{userdisplayname}", Validator.escapeSql(u.getDisplayName()));
             map.put("{useremail}", Validator.escapeSql(u.getEmail()));
             map.put("{userfirstname}", Validator.escapeSql(u.getFirstName()));
             map.put("{userlastname}", Validator.escapeSql(u.getLastName()));
             map.put("{worksiteid}", Validator.escapeSql(ToolManager.getCurrentPlacement().getContext()));
             map.put("{toolid}", Validator.escapeSql(ToolManager.getCurrentPlacement().getId()));
         }
         else {
             map.put("{userdisplayname}", Validator.escapeSql((String) s.getAttribute("userdisplayname")));
             map.put("{useremail}", Validator.escapeSql((String) s.getAttribute("useremail")));
             map.put("{userfirstname}", Validator.escapeSql((String) s.getAttribute("userfirstname")));
             map.put("{userlastname}", Validator.escapeSql((String) s.getAttribute("userlastname")));
             map.put("{worksiteid}", Validator.escapeSql((String) s.getAttribute("worksiteid")));
             map.put("{toolid}", Validator.escapeSql((String) s.getAttribute("toolid")));
         }
 
         Iterator iter = map.keySet().iterator();
         StringBuilder str = new StringBuilder(inString);
 
         //	loop through all the parameters and find in query for replacement
         while (iter.hasNext()) {
 
             //	get the parameter and associated parameter definition
             String key = (String) iter.next();
 
             int i = str.indexOf(key);
 
             //	Loop until no instances exist
             while (i != -1) {
 
                 //	replace the parameter with the value
                 str.delete(i, i + key.length());
                 str.insert(i, (String) map.get(key));
 
                 //	look for a second instance
                 i = str.indexOf(key);
             }
         }
 
         String string = str.toString();
         
         // create a list of the supported bean objects whose values can be replaced
         Map beans = new HashMap();
         beans.put("{session.attribute.", s);
         beans.put("{site.property."    , site);
         beans.put("{user.property."    , user);
         string = replaceSystemValues(string, beans);
 
         beans = new HashMap();
         beans.put("{session."          , s);
         beans.put("{site."             , site);
         beans.put("{user."             , user);
         string = replaceSystemValues(string, beans);
         
         return string;
     }
 
     private Site getCurrentWorksite(String worksiteId) {
         Site site = null;
         try {
             if (worksiteId != null && worksiteId.trim().length() != 0)
                site = SiteService.getSite(worksiteId);
         } catch (IdUnusedException ex) {
          // do nothing.  just return null.
         }
         return site;
     }
     
     public String replaceSystemValues(String string, Map beans) {
 
         StringBuffer buffer = new StringBuffer(string);
         Set beanNames = beans.keySet();
         for (Iterator it=beanNames.iterator(); it.hasNext(); ) {
              String beanName = (String)it.next();
             // see if the string contains reference(s) to the supported beans
             for (int i=buffer.indexOf(beanName), j=beanName.length(); i != -1; i=buffer.indexOf(beanName)) {
                 // if it does, parse the property of the bean the report query references
                 int k = buffer.indexOf("}", i+j);
                 if (k == -1)
                    throw new RuntimeException("Missing closing brace \"}\" in report query: " + string);
                 String property = buffer.substring(i+j, k);
 
                 // construct the bean property's corresponding "getter" method
                 String getter = null;
                 String param  = null;
                 if (beanName.indexOf(".attribute.") != -1) {
                     getter = "getAttribute";
                     param  = property;
                 } else if (beanName.indexOf(".property.") != -1) {
                     getter = "getProperties";
                     param  = null;
                 } else {
                     getter = "get" + Character.toUpperCase(property.charAt(0)) + property.substring(1);
                     param  = null;
                 }
 
                 try {
                    // use reflection to invoke the method on the bean
                    Object  bean   = beans.get(beanName);
                    Class   clasz  = bean.getClass();
                    Class[] args   = param == null ? (Class[])null : new Class[]{String.class};
                    Method  method = clasz.getMethod(getter, args);
                    Object  result = method.invoke(bean, (param == null ? (Object[])null : new Object[]{param}));
 
                    if (beanName.indexOf(".property.") != -1) {
                        clasz  = org.sakaiproject.entity.api.ResourceProperties.class;
                        getter = "getProperty";
                        args   = new Class[]{String.class};
                        param  = property;
                        method = clasz.getMethod(getter, args);
                        result = method.invoke(result, new Object[]{param});
                    }
 
                    // replace the bean expression in the report query with the actual value of calling the bean's corresponding getter method
                    buffer.delete(i, k+1);
                    buffer.insert(i, (result == null ? "null" : result instanceof Time ? ((Time)result).toStringSql() : result.toString().replaceAll("'","''")));
                 } catch (Exception ex) {
                    throw new RuntimeException(ex.getMessage(), ex);
                 }
             }
         }
         return buffer.toString();
     }
     
     /**
      * {@inheritDoc}
      */
     public String transform(ReportResult reportResult, ReportXsl reportXsl) {
         try {
 
             JDOMResult result = new JDOMResult();
             SAXBuilder builder = new SAXBuilder();
             StreamSource xsltSource;
             if (reportXsl.getResource() == null) {
                 xsltSource = new StreamSource(loadXslFromDB(reportXsl));
             } else {
                 xsltSource = new StreamSource(reportXsl.getResource().getInputStream());
             }
             Transformer transformer = TransformerFactory.newInstance()
                     .newTransformer(xsltSource);
             Document rootElement = builder.build(new StringReader(reportResult
                     .getXml()));
 
             ByteArrayOutputStream sourceOut = new ByteArrayOutputStream();
             StreamResult resultstream = new StreamResult(sourceOut);
 
             transformer.transform(new JDOMSource(rootElement), resultstream);
 
             return sourceOut.toString();
 
         } catch (Exception e) {
             logger.error("", e);
             throw new RuntimeException(e);
         }
     }
 
     public InputStream loadXslFromDB(ReportXsl reportXsl) {
         ReportDefinitionXmlFile reportDef = getReportDefinition(reportXsl.getReportDefinition().getIdString());
 
         ByteArrayInputStream inputStream = null;
         for (Iterator i = reportDef.getReportXslFiles().iterator(); i.hasNext();) {
             ReportXslFile xslFile = (ReportXslFile) i.next();
             if (xslFile.getReportXslFileRef().equals(reportXsl.getXslLink())) {
                 inputStream = new ByteArrayInputStream(xslFile.getXslFile());
                 return inputStream;
             }
         }
         throw new RuntimeException("can't find report xslfile: reportDef=[" +
                 reportXsl.getReportDefinition().getReportDefId().toString() +
                 "], xslLink=[" + reportXsl.getXslLink() + "]");
     }
 
     private void writeFile(String fileString, String fileName, String contentType) {
         FacesContext faces = FacesContext.getCurrentInstance();
         HttpServletResponse response = (HttpServletResponse) faces.getExternalContext().getResponse();
         protectAgainstInstantDeletion(response);
         response.setContentType(contentType);
         response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".csv");
         response.setContentLength(fileString.length());
         OutputStream out = null;
         try {
             out = response.getOutputStream();
             out.write(fileString.getBytes());
             out.flush();
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             try {
                if (out != null) {
                   out.close();
                }
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         faces.responseComplete();
     }
 
     /**
      * THIS IS TAKEN FROM GRADEBOOK: org.sakai.tool.gradebook.ui.ExportBean
      * <p/>
      * Try to head off a problem with downloading files from a secure HTTPS
      * connection to Internet Explorer.
      * <p/>
      * When IE sees it's talking to a secure server, it decides to treat all hints
      * or instructions about caching as strictly as possible. Immediately upon
      * finishing the download, it throws the data away.
      * <p/>
      * Unfortunately, the way IE sends a downloaded file on to a helper
      * application is to use the cached copy. Having just deleted the file,
      * it naturally isn't able to find it in the cache. Whereupon it delivers
      * a very misleading error message like:
      * "Internet Explorer cannot download roster from sakai.yoursite.edu.
      * Internet Explorer was not able to open this Internet site. The requested
      * site is either unavailable or cannot be found. Please try again later."
      * <p/>
      * There are several ways to turn caching off, and so to be safe we use
      * several ways to turn it back on again.
      * <p/>
      * This current workaround should let IE users save the files to disk.
      * Unfortunately, errors may still occur if a user attempts to open the
      * file directly in a helper application from a secure web server.
      * <p/>
      * TODO Keep checking on the status of this.
      */
     private static void protectAgainstInstantDeletion(HttpServletResponse response) {
         response.reset();    // Eliminate the added-on stuff
         response.setHeader("Pragma", "public");    // Override old-style cache control
         response.setHeader("Cache-Control", "public, must-revalidate, post-check=0, pre-check=0, max-age=0");    // New-style
     }
 
 
     /**
      * {@inheritDoc}
      */
     public void saveReportResult(ReportResult result) {
         getHibernateTemplate().saveOrUpdate(result.getReport());
         getHibernateTemplate().saveOrUpdate(result);
 
         //	the user can't save results that have already been saved
         result.getReport().setIsSaved(true);
         result.setIsSaved(true);
     }
 
 
     /**
      * {@inheritDoc}
      */
     public void saveReport(Report report) {
         getHibernateTemplate().saveOrUpdate(report);
 
         //	the user can't save reports that have already been saved
         report.setIsSaved(true);
     }
 
 
     /**
      * {@inheritDoc}
      */
     public void deleteReportResult(ReportResult result) {
 
         checkPermission(ReportFunctions.REPORT_FUNCTION_DELETE);
 
         getHibernateTemplate().delete(result);
 
         // if we are deleting the result, then if the report it came from is not on display then delete the report too
        if (!result.getReport().getDisplay() || !result.getReport().getIsLive()) {
           deleteReport(result.getReport(), false);
        }
     }
 
 
     /**
      * {@inheritDoc}
      */
     public void deleteReport(Report report, boolean deactivate) {
         boolean deleteAction = false, deactivateAction = false;
 
         checkPermission(ReportFunctions.REPORT_FUNCTION_DELETE);
 
         report = (Report) getHibernateTemplate().get(
                 Report.class,
                 report.getReportId()
         );
 
         List results = getHibernateTemplate().findByNamedQuery("findResultsByReport",
                 report);
 
         if (report.getIsLive()) {
            if (results.size() == 0) {
               deleteAction = true;
            } else if (deactivate) {
               deactivateAction = true;
            }
         } else { //the report is not live so delete any report results
             for (Iterator i = results.iterator(); i.hasNext();) {
                 getHibernateTemplate().delete(i.next());
             }
             deleteAction = true;
         }
 
         if (deleteAction) {
             getHibernateTemplate().delete(report);
         } else if (deactivateAction) {
             report.setDisplay(false);
             getHibernateTemplate().saveOrUpdate(report);
         }
     }
 
     public String getSecretKey() {
         return secretKey;
     }
 
     public void setSecretKey(String secretKey) {
         this.secretKey = secretKey;
     }
 
     public SecurityService getSecurityService() {
         return securityService;
     }
 
     public void setSecurityService(SecurityService securityService) {
         this.securityService = securityService;
     }
 
     private Site getCurrentSite() {
         try {
             if (ToolManager.getCurrentPlacement() != null) {
                 return SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
             }
             //if this job is running because of scheduler
             org.sakaiproject.tool.api.Session sakaiSession = SessionManager.getCurrentSession();
             return SiteService.getSite((String)sakaiSession.getAttribute("worksiteid"));
 
         } catch (IdUnusedException iue) {
             return null;
         }
     }
 
     /**
      * Returns the type of current worksite
      *
      * @return String
      */
     private String getCurrentSiteType() {
         return getCurrentSite() != null ? getCurrentSite().getType() : "";
     }
 
     public AuthorizationFacade getAuthzManager() {
         return authzManager;
     }
 
     public void setAuthzManager(AuthorizationFacade authzManager) {
         this.authzManager = authzManager;
     }
 
     protected void checkPermission(String function) {
         getAuthzManager().checkPermission(function, getIdManager().getId(ToolManager.getCurrentPlacement().getId()));
     }
 
 
     /**
      * {@inheritDoc}
      */
     public Map getAuthorizationsMap() {
         return new AuthZMap(getAuthzManager(), ReportFunctions.REPORT_FUNCTION_PREFIX,
                 getIdManager().getId(ToolManager.getCurrentPlacement().getId()));
     }
 
     protected boolean can(String function) {
         return new Boolean(getAuthzManager().isAuthorized(function,
                 getIdManager().getId(ToolManager.getCurrentPlacement().getId()))).booleanValue();
     }
 
 
     /**
      * {@inheritDoc}
      */
     public boolean isMaintaner() {
         return new Boolean(getAuthzManager().isAuthorized(WorksiteManager.WORKSITE_MAINTAIN,
                 getIdManager().getId(ToolManager.getCurrentPlacement().getContext()))).booleanValue();
     }
 
 
     /**
      * {@inheritDoc}
      */
     public void checkEditAccess() {
         checkPermission(ReportFunctions.REPORT_FUNCTION_EDIT);
     }
 
 
     public DataSource getSakaiDataSource() {
         return sakaiDataSource;
     }
 
 
     public void setSakaiDataSource(DataSource sakaiDataSource) {
         this.sakaiDataSource = sakaiDataSource;
     }
 
 
     public Boolean getForceColumnLabelUppercase() {
         return forceColumnLabelUppercase;
     }
 
 
     public void setForceColumnLabelUppercase(Boolean forceColumnLabelUppercase) {
         this.forceColumnLabelUppercase = forceColumnLabelUppercase;
     }
 
     private ReportDefinitionXmlFile importReport(ContentResource resource) {
         ReportDefinitionXmlFile bean = null;
 
         try {
 
             bean = new ReportDefinitionXmlFile(resource);
 
         } catch (Exception e) {
             e.printStackTrace();
         }
         return bean;
     }
 
     public ContentHostingService getContentHosting() {
         return contentHosting;
     }
 
     public void setContentHosting(ContentHostingService contentHosting) {
         this.contentHosting = contentHosting;
     }
 
     public List getDefinedDefintions() {
         return definedDefintions;
     }
 
     public void setDefinedDefintions(List definedDefintions) {
         this.definedDefintions = definedDefintions;
     }
 
     public boolean importResource(Id worksiteId, String nodeId) throws UnsupportedFileTypeException, ImportException, RuntimeException {
 
         String id = getContentHosting().resolveUuid(nodeId);
         try {
             ContentResource resource = getContentHosting().getResource(id);
             MimeType mimeType = new MimeType(resource.getContentType());
 
             if (mimeType.equals(new MimeType("application/xml")) ||
                     mimeType.equals(new MimeType("text/xml"))) {
                 ListableBeanFactory beanFactory = new XmlBeanFactory(new ByteArrayResource(resource.getContent()), getBeanFactory());
                 ReportDefinitionXmlFile bean = importReport(resource);
                 if (bean != null) {
                     saveReportDef(bean, beanFactory);
                 }
                 return bean != null;
             } else {
                 throw new UnsupportedFileTypeException("Unsupported file type");
             }
 
         } catch (ServerOverloadException soe) {
             logger.warn(soe);
 
         } catch (PermissionException pe) {
             logger.warn("Failed loading content: no permission to view file", pe);
         } catch (TypeException te) {
             logger.warn("Wrong type", te);
         } catch (IdUnusedException iue) {
             logger.warn("UnusedId: ", iue);
         }
         return false;
     }
 
     public void saveReportDef(ReportDefinitionXmlFile xmlFile, ListableBeanFactory beanFactory) throws RuntimeException {
 
         ReportDefinition reportDef = getReportDefBean(beanFactory);
         List reportDefList = new ArrayList();
         reportDefList.add(reportDef);
         xmlFile.setReportDefId(reportDef.getIdString());
         xmlFile.setReportXslFiles(processXSLFiles(reportDef, xmlFile));
         getHibernateTemplate().saveOrUpdate(xmlFile);
     }
 
     public Set processXSLFiles(ReportDefinition reportDef, ReportDefinitionXmlFile xmlFile) throws RuntimeException {
         Set xslsList = new HashSet();
 
         ReportXsl defaultXsl = reportDef.getDefaultXsl();
 
         if (defaultXsl == null) {
             return xslsList;
         } else {
             List xsls = reportDef.getXsls();
             for (Iterator i = xsls.iterator(); i.hasNext();) {
                 ReportXsl xsl = (ReportXsl) i.next();
                 ReportXslFile xslFile = new ReportXslFile(xsl, getContentHosting());
                 xslFile.setReportXslFileRef(xsl.getXslLink());
                 xslFile.setXslFileHash(DigestUtils.md5Hex(xslFile.getXslFile()));
 
                 xslsList.add(xslFile);
             }
 
         }
 
         return xslsList;
 
     }
 
     public void deleteReportDefXmlFile(ReportDefinition reportDef) {
 
         checkPermission(ReportFunctions.REPORT_FUNCTION_DELETE);
        List<ReportDefinitionXmlFile> defs = getHibernateTemplate().find("from ReportDefinitionXmlFile r where reportDefId = ?", reportDef.getIdString());
        for (ReportDefinitionXmlFile def : defs) {
            getHibernateTemplate().delete(def);
         }
        
        List<Report> reports = getHibernateTemplate().find("from Report r where reportDefIdMark = ?", reportDef.getIdString());
        for (Report report : reports) {
        	List<ReportResult> results = getHibernateTemplate().find("from ReportResult r where r.report.reportId = ?", report.getReportId());
        	for (ReportResult rr : results) {
        		getHibernateTemplate().delete(rr);
        	}
        	getHibernateTemplate().delete(report);
         }
     }
 
     public ReportDefinition getReportDefBean(ListableBeanFactory beanFactory) {
         Map beanMap = beanFactory.getBeansOfType(ReportDefinition.class);
         for (Iterator i = beanMap.values().iterator(); i.hasNext();) {
             return (ReportDefinition) i.next();
         }
         return null;
     }
 
     public void saveXslFile(ReportXslFile reportXslFile) {
         getHibernateTemplate().saveOrUpdate(reportXslFile);
     }
 
     protected void initDefinedReportDefinitions() {
         getSecurityService().pushAdvisor(new AllowAllSecurityAdvisor());
 
         org.sakaiproject.tool.api.Session sakaiSession = SessionManager.getCurrentSession();
         String userId = sakaiSession.getUserId();
         sakaiSession.setUserId("admin");
         sakaiSession.setUserEid("admin");
         List definitions = new ArrayList();
 
         try {
             for (Iterator i = getDefinedDefintions().iterator(); i.hasNext();) {
                ReportsDefinitionWrapper wrapper = (ReportsDefinitionWrapper)i.next();
                wrapper.setParentClass(getClass());
                 definitions.add(processDefinedDefinition(wrapper));
             }
 
         } finally {
             getSecurityService().popAdvisor();
             sakaiSession.setUserEid(userId);
             sakaiSession.setUserId(userId);
         }
 
     }
 
    protected void convert24to25Reports() {
       getSecurityService().pushAdvisor(new AllowAllSecurityAdvisor());
       
       org.sakaiproject.tool.api.Session sakaiSession = SessionManager.getCurrentSession();
       String userId = sakaiSession.getUserId();
       sakaiSession.setUserId("admin");
       sakaiSession.setUserEid("admin");
       
       Transaction tx = getSession().beginTransaction();
       try {           
          Transformer trans = createTemplate("/org/sakaiproject/reports/conversion/reports24to25.xsl").newTransformer();
          for (Iterator<ReportDefinitionXmlFile> i = get24Defintions().iterator(); i.hasNext();) {
             process24Definition(i.next(), trans);
          }
       
       } catch (MalformedURLException e) {
          tx.rollback();
          logger.error("error migrating 2.4 report definitions", e);
       } catch (TransformerConfigurationException e) {
          tx.rollback();
          logger.error("error migrating 2.4 report definitions", e);
       } catch (TransformerException e) {
          tx.rollback();
          logger.error("error migrating 2.4 report definitions", e);
       } finally {
          if (!tx.wasRolledBack()) {
             tx.commit();
          }
          getSecurityService().popAdvisor();
          sakaiSession.setUserEid(userId);
          sakaiSession.setUserId(userId);
       }
       
    }
 
    protected void process24Definition(ReportDefinitionXmlFile reportDefinitionXmlFile, Transformer trans) 
       throws TransformerException {
       
       ByteArrayOutputStream bos = new ByteArrayOutputStream();
       Document doc = reportDefinitionXmlFile.getXml();
       trans.transform(new JDOMSource(doc), new StreamResult(bos));
       reportDefinitionXmlFile.setXmlFile(bos.toByteArray());
       getHibernateTemplate().saveOrUpdate(reportDefinitionXmlFile);
    }
 
    protected List<ReportDefinitionXmlFile> get24Defintions() {
       return getHibernateTemplate().findByNamedQuery("find24ReportDefinitions");
    }
 
    protected Templates createTemplate(String transformPath)
       throws MalformedURLException, TransformerConfigurationException {
 
       InputStream stream = getClass().getResourceAsStream(
             transformPath);
       URL url = getClass().getResource(transformPath);
       String urlPath = url.toString();
       String systemId = urlPath.substring(0, urlPath.lastIndexOf('/') + 1);
       TransformerFactory transformerFactory = TransformerFactory.newInstance();
       Templates templates = transformerFactory.newTemplates(
          new StreamSource(stream, systemId));
       return templates;
    }
 
    public void addReportDefinition(ReportsDefinitionWrapper reportDef) {
       processDefinedDefinition(reportDef);
    }
 
    protected ReportDefinitionXmlFile processDefinedDefinition(ReportsDefinitionWrapper wrapper) {
         ReportDefinitionXmlFile definition = getReportDefinition(wrapper.getIdValue());
 
         if (definition == null) {
             definition = new ReportDefinitionXmlFile();
             definition.setReportDefId(wrapper.getIdValue());
         }
 
         updateDefinition(wrapper, definition);
         return definition;
     }
 
     protected void updateDefinition(ReportsDefinitionWrapper wrapper, ReportDefinitionXmlFile def) {
         try {
 
             InputStream stream =  wrapper.getParentClass().getResourceAsStream(wrapper.getDefinitionFileLocation());
             if (stream== null) {
                 throw new RuntimeException ("Loaded Report Definition failed: " + wrapper.getDefinitionFileLocation());
             }
             Set xslFilesToBeRemoved = new HashSet();
             
            if (def.getReportXslFiles() != null) {
               xslFilesToBeRemoved.addAll(def.getReportXslFiles());
            }
            else {
               def.setReportXslFiles(new HashSet());
            }
             def.setXmlFile(readStreamToBytes(wrapper.getParentClass().getResourceAsStream(wrapper.getDefinitionFileLocation())));
 
             ListableBeanFactory beanFactory = new XmlBeanFactory(new ByteArrayResource(
                readStreamToBytes(wrapper.getParentClass().getResourceAsStream(wrapper.getDefinitionFileLocation()))), getBeanFactory());
             ReportDefinition repDef = getReportDefBean(beanFactory);
             List xsls = repDef.getXsls();
             for (Iterator i = xsls.iterator(); i.hasNext();) {
                 ReportXsl xsl = (ReportXsl) i.next();
                 ReportXslFile xslFile = new ReportXslFile();
                 if (wrapper.getParentClass().getResourceAsStream(xsl.getXslLink()) != null){
                     xslFile.setXslFile(readStreamToBytes(wrapper.getParentClass().getResourceAsStream(xsl.getXslLink())));
                     xslFile.setXslFileHash(DigestUtils.md5Hex(xslFile.getXslFile()));
                 }
                 //xslFile.setReportDefId(repDef.getIdString());
                 xslFile.setReportXslFileRef(xsl.getXslLink());
                 if (def.getReportXslFiles().contains(xslFile)) {
                    xslFilesToBeRemoved.remove(xslFile);
                 }
                else {
                    def.getReportXslFiles().add(xslFile);
                 }
             }
             
             for (Iterator<ReportXslFile> i=xslFilesToBeRemoved.iterator();i.hasNext();) {
                ReportXslFile removedFile = i.next();
                def.getReportXslFiles().remove(removedFile);
             }
             getHibernateTemplate().saveOrUpdate(def);
         }
         catch (Exception e) {
             throw new RuntimeException("Loaded report def failed", e);
         }
     }
 
     public ReportDefinitionXmlFile getReportDefinition(String id) {
         return (ReportDefinitionXmlFile) getHibernateTemplate().get(ReportDefinitionXmlFile.class, id);
     }
 
     public Report getReportById(String id) {
         Report report = (Report) getHibernateTemplate().get(Report.class, new IdImpl(id, null));
         report.connectToDefinition(getReportDefinitions());
         return report;
     }
 
     private byte[] readStreamToBytes(InputStream inStream) throws IOException {
         ByteArrayOutputStream bytes = new ByteArrayOutputStream();
         byte data[] = new byte[10 * 1024];
 
         int count;
         while ((count = inStream.read(data, 0, 10 * 1024)) != -1) {
             bytes.write(data, 0, count);
         }
         byte[] tmp = bytes.toByteArray();
         bytes.close();
         return tmp;
     }
 
    // todo figure out how to impl this -- JDE
     public List getReportsByViewer() {
         Agent viewer = getAuthnManager().getAgent();
         Collection reportAuthzs = getAuthzManager().getAuthorizations(viewer,
                 ReportFunctions.REPORT_FUNCTION_VIEW, null);
 
         List results = new ArrayList();
         for (Iterator i = reportAuthzs.iterator(); i.hasNext();) {
             Id resultId = ((Authorization) i.next()).getQualifier();
             List result = getReportResults(resultId);
             for (Iterator iter = result.iterator(); iter.hasNext();) {
                 results.add(iter.next());
             }
         }
         return results;
     }
 
     List getReportResults(Id resultId) {
 
         boolean viewReports = can(ReportFunctions.REPORT_FUNCTION_VIEW);
 
         List returned = new ArrayList();
 
         if (viewReports) {
             List results = getHibernateTemplate().findByNamedQuery("findResultsById", resultId);
 
             Iterator iter = results.iterator();
             while (iter.hasNext()) {
                 ReportResult r = (ReportResult) iter.next();
 
                 r.setIsSaved(true);
             }
             returned.addAll(results);
         }
         return returned;
     }
 
 
     protected String createResource(ByteArrayOutputStream bos,
                                     String name, String description, String type) {
 
         ContentResource resource = null;
         ResourcePropertiesEdit resourceProperties = getContentHosting().newResourceProperties();
         resourceProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
         resourceProperties.addProperty(ResourceProperties.PROP_DESCRIPTION, description);
         resourceProperties.addProperty(ResourceProperties.PROP_CONTENT_ENCODING, "UTF-8");
 
         getSecurityService().pushAdvisor(new AllowAllSecurityAdvisor());
 
         org.sakaiproject.tool.api.Session sakaiSession = SessionManager.getCurrentSession();
         String userId = sakaiSession.getUserId();
         sakaiSession.setUserId(userId);
         sakaiSession.setUserEid(userId);
 
         try {
 
             ContentCollectionEdit groupCollection = getContentHosting().addCollection(getUserCollection().getId() + "savedReports/");
             groupCollection.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, "Saved Reports");
             groupCollection.getPropertiesEdit().addProperty(ResourceProperties.PROP_DESCRIPTION, "Folder for Saved Report Results");
             getContentHosting().commitCollection(groupCollection);
         }
         catch (IdUsedException e) {
             // ignore... it is already there.
             if (logger.isDebugEnabled()) {
                 logger.debug(e);
             }
         }
         catch (Exception e) {
             throw new RuntimeException(e);
         }
 
 
         try {
             String id = getUserCollection().getId() + "Saved Reports/" + name;
             getContentHosting().removeResource(id);
         }
         catch (TypeException e) {
             // ignore, must be new
             if (logger.isDebugEnabled()) {
                 logger.debug(e);
             }
         }
         catch (IdUnusedException e) {
             // ignore, must be new
             if (logger.isDebugEnabled()) {
                 logger.debug(e);
             }
         }
         catch (PermissionException e) {
             // ignore, must be new
             if (logger.isDebugEnabled()) {
                 logger.debug(e);
             }
         }
         catch (InUseException e) {
             // ignore, must be new
             if (logger.isDebugEnabled()) {
                 logger.debug(e);
             }
         }
 
         try {
             resource = getContentHosting().addResource(name, getUserCollection().getId() + "savedReports/", 100, type,
                     bos.toByteArray(), resourceProperties, NotificationService.NOTI_NONE);
         }
         catch (Exception e) {
             throw new RuntimeException(e);
         }
 
         return "savedReports/" + name;
     }
 
     public String processSaveResultsToResources(ReportResult reportResult) throws IOException {
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         Report report = reportResult.getReport();
         report.connectToDefinition(getReportDefinitions());
         ReportXsl xslt = report.getReportDefinition().getDefaultXsl();
         xslt.setReportDefinition(report.getReportDefinition());
         String fileData = transform(reportResult, xslt);
 
         if (xslt.getResultsPostProcessor() != null) {
             bos.write(xslt.getResultsPostProcessor().postProcess(fileData));
         } else {
             bos.write(fileData.getBytes());
         }
         return createResource(bos, reportResult.getTitle() + ".html", reportResult.getTitle(), "text/html");
 
     }
 
     protected ContentCollection getUserCollection() throws TypeException, IdUnusedException, PermissionException {
         User user = org.sakaiproject.user.cover.UserDirectoryService.getCurrentUser();
         String userId = user.getId();
         String wsId = SiteService.getUserSiteId(userId);
         String wsCollectionId = getContentHosting().getSiteCollection(wsId);
         ContentCollection collection = getContentHosting().getCollection(wsCollectionId);
         return collection;
     }
 
     public SchedulerManager getSchedulerManager() {
         return schedulerManager;
     }
 
     public void setSchedulerManager(SchedulerManager schedulerManager) {
         this.schedulerManager = schedulerManager;
     }
 
     public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
         getSecurityService().pushAdvisor(new AllowAllSecurityAdvisor());
         org.sakaiproject.tool.api.Session sakaiSession = SessionManager.getCurrentSession();
         String sessionUserId = sakaiSession.getUserId();
         try {
             Map details = jobExecutionContext.getJobDetail().getJobDataMap();
             String reportId = (String) details.get("reportId");
             String userId = (String) details.get("userId");
             sakaiSession.setAttribute("userdisplayname", (String) details.get("userdisplayname"));
             sakaiSession.setAttribute("useremail", (String) details.get("useremail"));
             sakaiSession.setAttribute("userfirstname", (String) details.get("userfirstname"));
             sakaiSession.setAttribute("userlastname", (String) details.get("userlastname"));
             sakaiSession.setAttribute("worksiteid", (String) details.get("worksiteid"));
             sakaiSession.setAttribute("toolid", (String) details.get("toolid"));
             sakaiSession.setUserId(userId);
             sakaiSession.setUserEid(userId);
             Report report = getReportById(reportId);
             if (report != null) {
                 ReportResult result = generateResults(report);
                 result.setIsSaved(true);
                 result.setTitle(result.getTitle() + " - " + result.getCreationDate());
                 processSaveResultsToResources(result);
                 saveReportResult(result);
             } else {
                 schedulerManager.getScheduler().deleteJob(jobExecutionContext.getJobDetail().getName(), reportGroup);
             }
         }
         catch (Exception e){
             e.printStackTrace();
         }
         finally {
             getSecurityService().popAdvisor();
             sakaiSession.setUserEid(sessionUserId);
             sakaiSession.setUserId(sessionUserId);
         }
 
     }
 
     public void processDeleteJobs(JobDetail jobDetail) {
         try {
             schedulerManager.getScheduler().deleteJob(
                     jobDetail.getName(), reportGroup);
         }
         catch (SchedulerException e) {
             logger.error("Scheduler Down");
         }
      }
 
     public JobDetail processCreateJob(Report report) {
         Scheduler scheduler = getSchedulerManager().getScheduler();
         UserDirectoryService dirServ = org.sakaiproject.user.cover.UserDirectoryService.getInstance();
         User u = dirServ.getCurrentUser();
         if (scheduler == null) {
             logger.error("Scheduler is down!");
         }
         JobDetail jd = null;
         try {
 
             JobBeanWrapper job = getJobBeanWrapper();
             if (job != null) {
 
                 jd = scheduler.getJobDetail(report.getReportId().toString(), reportGroup);
                 if (jd == null) {
                     jd = new JobDetail(report.getReportId().toString(), reportGroup,
                             job.getJobClass(), false, true, true);
                     jd.getJobDataMap().put(JobBeanWrapper.SPRING_BEAN_NAME, job.getBeanId());
                     jd.getJobDataMap().put(JobBeanWrapper.JOB_TYPE, job.getJobType());
                     jd.getJobDataMap().put("reportId", report.getReportId().getValue());
                     jd.getJobDataMap().put("userId", SessionManager.getCurrentSessionUserId());
                     jd.getJobDataMap().put("userdisplayname", u.getDisplayName());
                     jd.getJobDataMap().put("useremail", u.getEmail());
                     jd.getJobDataMap().put("userfirstname", u.getFirstName());
                     jd.getJobDataMap().put("userlastname", u.getLastName());
                     jd.getJobDataMap().put("worksiteid", ToolManager.getCurrentPlacement().getContext());
                     jd.getJobDataMap().put("toolid", ToolManager.getCurrentPlacement().getId());
 
                     scheduler.addJob(jd, false);
                 }
             } else {
                 jd = new JobDetail(report.getReportId().toString(), reportGroup,
                         SpringJobBeanWrapper.class, false, true, true);
                 scheduler.addJob(jd, false);
             }
         }
         catch (Exception e) {
             logger.error("Failed to create job");
         }
         return jd;
     }
 
     public JobBeanWrapper getJobBeanWrapper() {
         return jobBeanWrapper;
     }
 
     public void setJobBeanWrapper(JobBeanWrapper jobBeanWrapper) {
         this.jobBeanWrapper = jobBeanWrapper;
     }
 
    public boolean isAutoDdl() {
       return autoDdl;
    }
 
    public void setAutoDdl(boolean autoDdl) {
       this.autoDdl = autoDdl;
    }
 
    public boolean isUpgrade24() {
       return upgrade24;
    }
 
    public void setUpgrade24(boolean upgrade24) {
       this.upgrade24 = upgrade24;
    }
 
 }
 
