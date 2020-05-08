 package org.pentaho.pac.server;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.Driver;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Collection;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.NameValuePair;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.params.HttpMethodParams;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.pentaho.pac.client.PacService;
 import org.pentaho.pac.client.PacServiceException;
 import org.pentaho.pac.client.PentahoSecurityException;
 import org.pentaho.pac.client.datasources.Constants;
 import org.pentaho.pac.client.datasources.DataSourceManagementException;
 import org.pentaho.pac.client.datasources.IDataSource;
 import org.pentaho.pac.client.roles.ProxyPentahoRole;
 import org.pentaho.pac.client.users.DuplicateUserException;
 import org.pentaho.pac.client.users.NonExistingUserException;
 import org.pentaho.pac.client.users.ProxyPentahoUser;
 import org.pentaho.pac.server.datasources.DataSourceManagerFacade;
 import org.pentaho.pac.server.datasources.IDataSourceManager;
 import org.pentaho.pac.server.datasources.NamedParameter;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 public class PacServiceImpl extends RemoteServiceServlet implements PacService {
 
   private IUserRoleMgmtService userRoleMgmtService = new UserRoleMgmtService();
   private static final String PROPERTIES_FILE_NAME = "pac.properties"; //$NON-NLS-1$
   private static final Log logger = LogFactory.getLog(PacServiceImpl.class);
   
   private String jmxHostName = null;
   private String jmxPortNumber = null;
   private String password = null;
   private String username = null;
   private String pciContextPath = null;
   private String biServerBaseURL = null;
   
   public PacServiceImpl()
   {
     initConfiguration();
   }
   
   public boolean createUser( ProxyPentahoUser proxyUser ) throws DuplicateUserException, PentahoSecurityException, PacServiceException
   {
     boolean result = false;
     IPentahoUser user = new PentahoUser(proxyUser.getName());
     user.setDescription(proxyUser.getDescription());
     user.setPassword(proxyUser.getPassword());
     user.setEnabled(proxyUser.getEnabled()) ;
     try {
       userRoleMgmtService.beginTransaction();
       userRoleMgmtService.createUser( user );
       userRoleMgmtService.commitTransaction();
       result = true;
     } catch ( DAOException e) {
       throw new PacServiceException( 
           Messages.getErrorString( "PacService.ERROR_0004_USER_CREATION_FAILED", proxyUser.getName() ), e ); //$NON-NLS-1$
     }
     finally {
       if (!result) {
         rollbackTransaction();
       }
       userRoleMgmtService.closeSession();
     }
     return result;
   }
 
   public boolean deleteUsers( ProxyPentahoUser[] users ) throws NonExistingUserException, PentahoSecurityException, PacServiceException
   {
     boolean result = false;
     IPentahoUser[] persistedUsers;
     try {
       persistedUsers = new IPentahoUser[users.length];
       for (int i = 0; i < users.length; i++) {
         persistedUsers[i] = userRoleMgmtService.getUser(users[i].getName());
         if ( null == persistedUsers[i] )
         {
           throw new NonExistingUserException(users[i].getName());
         }
       }
       userRoleMgmtService.beginTransaction();
       for (int i = 0; i < persistedUsers.length; i++) {
         userRoleMgmtService.deleteUser( persistedUsers[i] );
       }
       userRoleMgmtService.commitTransaction();
       result = true;
     } catch (DAOException e) {
       throw new PacServiceException(
           Messages.getString("PacService.USER_DELETION_FAILED", e.getMessage())); //$NON-NLS-1$
     }
     finally {
       if (!result) {
         rollbackTransaction();
       }
       userRoleMgmtService.closeSession();
     }
     return result;
   }
 
   public ProxyPentahoUser getUser(String userName) throws PacServiceException
   {
     ProxyPentahoUser proxyPentahoUser = null;
     try {
       IPentahoUser user = userRoleMgmtService.getUser( userName );
       if ( null != user )
       {
         proxyPentahoUser = new ProxyPentahoUser();
         proxyPentahoUser.setName(user.getName());
         proxyPentahoUser.setDescription(user.getDescription());
         proxyPentahoUser.setEnabled(user.getEnabled());
         proxyPentahoUser.setPassword(user.getPassword());
       }
     } catch (DAOException e) {
       throw new PacServiceException(
           Messages.getString("PacService.FAILED_TO_FIND_USER", userName ), e ); //$NON-NLS-1$
     }
     finally {
       userRoleMgmtService.closeSession();
     }
     return proxyPentahoUser;
   }
 
   public ProxyPentahoUser[] getUsers() throws PacServiceException
   {
     ProxyPentahoUser[] proxyUsers = new ProxyPentahoUser[0];
     try {
       List<IPentahoUser> users = userRoleMgmtService.getUsers();
       proxyUsers = new ProxyPentahoUser[users.size()];
       int i = 0;
       for (IPentahoUser user : users) {       
         ProxyPentahoUser proxyPentahoUser = new ProxyPentahoUser();
         proxyPentahoUser.setName(user.getName());
         proxyPentahoUser.setDescription(user.getDescription());
         proxyPentahoUser.setEnabled(user.getEnabled());
         proxyPentahoUser.setPassword(user.getPassword());
         proxyUsers[i++] = proxyPentahoUser;
       }
     } catch (DAOException e) {
       throw new PacServiceException(
           Messages.getString("PacService.FAILED_TO_GET_USER_NAME" ), e ); //$NON-NLS-1$
     }
     finally {
       userRoleMgmtService.closeSession();
     }
     return proxyUsers;
   }
   
   public boolean updateUser(ProxyPentahoUser proxyPentahoUser) throws NonExistingUserException, PentahoSecurityException, PacServiceException
   {
     boolean result = false;
     try {
       IPentahoUser user = userRoleMgmtService.getUser(proxyPentahoUser.getName());
       if ( null == user )
       {
         throw new NonExistingUserException(proxyPentahoUser.getName());
       }
       userRoleMgmtService.beginTransaction();
       user.setPassword(proxyPentahoUser.getPassword());
       user.setEnabled(proxyPentahoUser.getEnabled());
       user.setDescription(proxyPentahoUser.getDescription());
       userRoleMgmtService.updateUser( user );
       userRoleMgmtService.commitTransaction();
       result = true;
     } catch (DAOException e) {
      throw new PacServiceException(
          Messages.getString("PacService.USER_UPDATE_FAILED_NO_PERMISSION", proxyPentahoUser.getName() ), e ); //$NON-NLS-1$
     }
     finally {
       if (!result) {
         rollbackTransaction();
       }
       userRoleMgmtService.closeSession();
     }
     return result;
   }
   
 
   public void updateRole( String roleName, String description, List<String> userNames ) throws PacServiceException
   {
     try {
       IPentahoRole role = userRoleMgmtService.getRole( roleName );
       if ( null == role )
       {
         throw new PacServiceException(
             Messages.getString("PacService.ROLE_UPDATE_FAILED", roleName ) ); //$NON-NLS-1$
       }
       userRoleMgmtService.beginTransaction();
       role.setDescription( description );
       role.setUsers( userNames );
       userRoleMgmtService.updateRole( role );
       userRoleMgmtService.commitTransaction();
     } catch (DAOException e) {
       rollbackTransaction();
       throw new PacServiceException(
           Messages.getString("PacService.ROLE_UPDATE_FAILED", roleName ), e ); //$NON-NLS-1$
     } catch (PentahoSecurityException e) {
       rollbackTransaction();
       throw new PacServiceException(
           Messages.getString("PacService.ROLE_UPDATE_FAILED_NO_PERMISSION", roleName ), e ); //$NON-NLS-1$
     } catch (NonExistingRoleException e) {
       rollbackTransaction();
       throw new PacServiceException(
           Messages.getString("PacService.ROLE_UPDATE_FAILED_DOES_NOT_EXIST", roleName ), e );  //$NON-NLS-1$
     } catch (NonExistingUserException e) {
       rollbackTransaction();
       throw new PacServiceException( 
           Messages.getString("PacService.ROLE_UPDATE_FAILED_USER_DOES_NOT_EXIST", roleName, /*user name*/e.getMessage() ), e ); //$NON-NLS-1$
     }
     finally {
       userRoleMgmtService.closeSession();
     }
   }
 
   public boolean createDataSource(IDataSource dataSource) throws PacServiceException {
     boolean result = false;
     try {
       IDataSourceManager dsMgr = getDataSourceMgr();
       dataSource.setParameter( Constants.PATH, this.getPciContextPath() );
       dsMgr.addDataSource(dataSource);
       dsMgr.commitChanges();
       result = true;
     } catch (DataSourceManagementException e) {
       throw new PacServiceException(e);
     }
     return result;
   }
   
   public boolean deleteDataSources(IDataSource[] dataSources) throws PacServiceException {
     boolean result = false;
     try {
       IDataSourceManager dsMgr = getDataSourceMgr();
       for (int i = 0; i < dataSources.length; i++) {
         if (dsMgr.getDataSource(dataSources[i].getJndiName()) == null) {
           throw new PacServiceException("Data Source does not exist : " + dataSources[i].getJndiName());
         }
       }
       for (int i = 0; i < dataSources.length; i++) {
         dataSources[i].setParameter( Constants.PATH, this.getPciContextPath() );
         dsMgr.deleteDataSource(dataSources[i]);
         dsMgr.commitChanges();
       }
       result = true;
     } catch (DataSourceManagementException e) {
       throw new PacServiceException(e);
     }
     return result;
   }
   
   public boolean updateDataSource(IDataSource dataSource) throws PacServiceException {
     boolean result = false;
     try {
       IDataSourceManager dsMgr = getDataSourceMgr();
       dataSource.setParameter( Constants.PATH, this.getPciContextPath() );
       dsMgr.updateDataSource(dataSource);
       dsMgr.commitChanges();
       result = true;
     } catch (DataSourceManagementException e) {
      e.printStackTrace();
     }
     return result;
   }
   
   public IDataSource[] getDataSources() throws PacServiceException {
     IDataSource[] dataSources;
     try {
       IDataSourceManager dataSourceManager = getDataSourceMgr();
       Collection<String> dataSourceNames = dataSourceManager.getDataSources();
       dataSources = new IDataSource[dataSourceNames.size()];
       int i = 0;
       for (String datasourceName : dataSourceNames) {
         dataSources[i++] = dataSourceManager.getDataSource(datasourceName);
       }
     } catch (DataSourceManagementException e) {
       throw new PacServiceException(e);
     }
     return dataSources;
   }
   
   private IDataSourceManager getDataSourceMgr() throws DataSourceManagementException
   {
     IDataSourceManager dsMgr = null;
 
     NamedParameter[] parms = new NamedParameter[2];
     parms[0] = new NamedParameter(Constants.JMX_HOST, this.getJmxHostName() );
     parms[1] = new NamedParameter(Constants.JMX_PORT, this.getJmxPortNumber() );
     dsMgr = new DataSourceManagerFacade().getDSManager(DataSourceManagerFacade.Type.TOMCAT, parms);
     return dsMgr;
   }
   
   /**
    * NOTE: caller is responsible for closing connection
    * 
    * @param ds
    * @return
    * @throws DataSourceManagementException
    */
    private static Connection getDataSourceConnection(IDataSource ds) throws DataSourceManagementException {
      Connection conn = null;
 
      String driverClass = ds.getDriverClass();
      if (StringUtils.isEmpty(driverClass)) {
        throw new DataSourceManagementException("Connection attempt failed, no driver class available: " + driverClass);
      }
      Class<?> driverC = null;
 
      try {
        driverC = Class.forName(driverClass);
      } catch (ClassNotFoundException e) {
        throw new DataSourceManagementException("Connection attempt failed, driver class " + driverClass
            + " not in classpath: ");
      }
      if (!Driver.class.isAssignableFrom(driverC)) {
        throw new DataSourceManagementException("Connection attempt failed, driver class " + driverClass
            + " not in classpath: ");
      }
 
      Driver driver = null;
 
      try {
        driver = driverC.asSubclass(Driver.class).newInstance();
      } catch (InstantiationException e) {
        throw new DataSourceManagementException("Connection attempt failed, unable to create driver class instance: "
            + driverClass, e);
      } catch (IllegalAccessException e) {
        throw new DataSourceManagementException("Connection attempt failed, unable to create driver class instance: "
            + driverClass, e);
      }
 
      try {
        DriverManager.registerDriver(driver);
        conn = DriverManager.getConnection(ds.getUrl(), ds.getUserName(), ds.getPassword());
        return conn;
      } catch (SQLException e) {
        throw new DataSourceManagementException("Connection attempt failed. " + e.getMessage(), e);
      }
    }
 
    public boolean testDataSourceConnection(IDataSource ds) throws PacServiceException {
      Connection conn = null;
      try {
        conn = getDataSourceConnection(ds);
      } catch (DataSourceManagementException dme) {
        throw new PacServiceException("Test connection failed.", dme);
      } finally {
        try {
          if (conn != null) {
            conn.close();
          }
        } catch (SQLException e) {
          throw new PacServiceException(e);
        }
      }
      return true;
    }
 
    public boolean testDataSourceValidationQuery(IDataSource ds) throws PacServiceException {
      Connection conn = null;
      Statement stmt = null;
      ResultSet rs = null;
      try {
        conn = getDataSourceConnection(ds);
 
        if (!StringUtils.isEmpty(ds.getValidationQuery())) {
          stmt = conn.createStatement();
          rs = stmt.executeQuery(ds.getValidationQuery());
        } else {
          throw new PacServiceException("Data Source configuration does not contain a validation query.");
        }
      } catch (DataSourceManagementException dme) {
 
        throw new PacServiceException("Data Source validation query failed.", dme);
      } catch (SQLException e) {
        throw new PacServiceException("Data Source validation query failed.", e);
      } finally {
        try {
          if (rs != null) {
            rs.close();
          }
          if (stmt != null) {
            stmt.close();
          }
          if (conn != null) {
            conn.close();
          }
        } catch (SQLException e) {
          throw new PacServiceException(e);
        }
      }
      return true;
    }
 
   private void rollbackTransaction()
   {
     try {
       userRoleMgmtService.rollbackTransaction();
    } catch (DAOException e) {
     }
   }
   
   public String refreshSolutionRepository() throws PacServiceException {
     return executePublishRequest("org.pentaho.core.solution.SolutionPublisher", getUserName(), getPassword()); //$NON-NLS-1$
   }
   
   public String cleanRepository() throws PacServiceException {
     return executeXAction("admin", "", "clean_repository.xaction", getUserName(), getPassword()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
   }
   
 
   public String clearMondrianDataCache() throws PacServiceException {
     return executeXAction("admin", "", "clear_mondrian_data_cache.xaction", getUserName(), getPassword());
   }
   
 
   public String clearMondrianSchemaCache() throws PacServiceException {
     return executeXAction("admin", "", "clear_mondrian_schema_cache.xaction", getUserName(), getPassword());
   }
   
 
   public String scheduleRepositoryCleaning() throws PacServiceException {
     return executeXAction("admin", "", "schedule-clean.xaction", getUserName(), getPassword()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
   }
   
 
   public String resetRepository() throws PacServiceException {
     return resetSolutionRepository(getUserName(), getPassword());
   }
   
 
   public String refreshSystemSettings() throws PacServiceException {
       return executePublishRequest("org.pentaho.core.system.SettingsPublisher", getUserName(), getPassword()); //$NON-NLS-1$
   }
   
 
   public String executeGlobalActions() throws PacServiceException {
     return executePublishRequest("org.pentaho.core.system.GlobalListsPublisher", getUserName(), getPassword()); //$NON-NLS-1$
   }
   
 
   public String refreshReportingMetadata() throws PacServiceException {
     return executePublishRequest("org.pentaho.plugin.mql.MetadataPublisher", getUserName(), getPassword()); //$NON-NLS-1$
   }
   
   private void initConfiguration()
   {
     InputStream s = this.getClass().getResourceAsStream( "/" + PROPERTIES_FILE_NAME ); //$NON-NLS-1$
     Properties p = new Properties();
     if ( null != s ) {
       try {
         p.load( s );
       } catch (IOException e) {
         logger.error( Messages.getString( "PacService.LOAD_PROPS_FAILED", PROPERTIES_FILE_NAME ) ); //$NON-NLS-1$
       }
     } else {
       logger.warn( Messages.getString( "PacService.OPEN_PROPS_FAILED", PROPERTIES_FILE_NAME ) ); //$NON-NLS-1$
     }
     jmxHostName = StringUtils.defaultIfEmpty( p.getProperty("jmxHostName"), System.getProperty("jmxHostName") ); //$NON-NLS-1$ //$NON-NLS-2$
     jmxPortNumber = StringUtils.defaultIfEmpty( p.getProperty("jmxPortNumber"), System.getProperty("jmxPortNumber") ); //$NON-NLS-1$ //$NON-NLS-2$
     password = StringUtils.defaultIfEmpty( p.getProperty("pentaho.platform.password"), System.getProperty("pentaho.platform.password") ); //$NON-NLS-1$ //$NON-NLS-2$
     username = StringUtils.defaultIfEmpty( p.getProperty("pentaho.platform.username"), System.getProperty("pentaho.platform.username") ); //$NON-NLS-1$ //$NON-NLS-2$
     pciContextPath = StringUtils.defaultIfEmpty( p.getProperty("pciContextPath"), System.getProperty("pciContextPath") ); //$NON-NLS-1$ //$NON-NLS-2$
     biServerBaseURL = StringUtils.defaultIfEmpty( p.getProperty("biServerBaseURL"), System.getProperty("biServerBaseURL") ); //$NON-NLS-1$ //$NON-NLS-2$
   }
 
   public String getJmxHostName() {
     return jmxHostName;
   }
 
   public String getJmxPortNumber() {
     return jmxPortNumber;
   }
 
   public String getPassword() {
     return password;
   }
 
   public String getUserName() {
     return username;
   }
   
   public String getPciContextPath() {
     return pciContextPath;
   }
   
   public String getBIServerBaseUrl() {
     return biServerBaseURL;
   }
   
   private String executeXAction(String solution, String path, String xAction, String userid, String password) throws PacServiceException{
     
     String result = "Action Failed";
     GetMethod method = null;
     try {
       HttpClient client = new HttpClient();
 
       // Create a method instance.
       method = new GetMethod(getBIServerBaseUrl() + "/ViewAction"); //$NON-NLS-1$
       NameValuePair nvp1 = new NameValuePair("solution", solution); //$NON-NLS-1$
       NameValuePair nvp3 = new NameValuePair("path", path); //$NON-NLS-1$
       NameValuePair nvp2 = new NameValuePair("action", xAction); //$NON-NLS-1$
       NameValuePair nvp4 = new NameValuePair("userid", userid); //$NON-NLS-1$
       NameValuePair nvp5 = new NameValuePair("password", password); //$NON-NLS-1$
 
       method.setQueryString(new NameValuePair[] { nvp1, nvp2, nvp3, nvp4, nvp5 });
       method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
 
       if (client.executeMethod(method) != HttpStatus.SC_OK) {
         result = method.getStatusLine().toString();
       } else {
         result = "Action Complete";
       }
 
     } catch (Exception e) {
       throw new PacServiceException(e);
     } finally {
       method.releaseConnection();
     }
     
     return result;
   }
   
   private String executePublishRequest(String publisherClassName, String userid, String password) throws PacServiceException {
     // Create an instance of HttpClient.
     HttpClient client = new HttpClient();
     String result = "Action Failed";
 
     // Create a method instance.
     GetMethod method = new GetMethod(getBIServerBaseUrl() + "/Publish"); //$NON-NLS-1$
     NameValuePair nvp1 = new NameValuePair("publish", "now"); //$NON-NLS-1$ //$NON-NLS-2$
     NameValuePair nvp3 = new NameValuePair("style", "popup"); //$NON-NLS-1$ //$NON-NLS-2$
     NameValuePair nvp2 = new NameValuePair("class", publisherClassName); //$NON-NLS-1$
     NameValuePair nvp4 = new NameValuePair("userid", userid); //$NON-NLS-1$
     NameValuePair nvp5 = new NameValuePair("password", password); //$NON-NLS-1$
 
     method.setQueryString(new NameValuePair[] { nvp1, nvp2, nvp3, nvp4, nvp5 });
     // Provide custom retry handler is necessary
     method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
 
     try {
       // Execute the method.
       int statusCode = client.executeMethod(method);
 
       if (client.executeMethod(method) != HttpStatus.SC_OK) {
         result = method.getStatusLine().toString();
       } else {
         result = "Action Complete";
       }
     } catch (Exception e) {
       throw new PacServiceException(e);
     } finally {
       // Release the connection.
       method.releaseConnection();
     }
 
     return result;
   }
   
   private String resetSolutionRepository(String userid, String password) throws PacServiceException {
     HttpClient client = new HttpClient();
     String result = "Action Failed";
 
     GetMethod method = new GetMethod(getBIServerBaseUrl()+"/ResetRepository"); //$NON-NLS-1$
     NameValuePair nvp1 = new NameValuePair("userid", userid); //$NON-NLS-1$
     NameValuePair nvp2 = new NameValuePair("password", password); //$NON-NLS-1$
 
     method.setQueryString(new NameValuePair[] { nvp1, nvp2 });
     method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
 
     try {
       int statusCode = client.executeMethod(method);
 
       if (client.executeMethod(method) != HttpStatus.SC_OK) {
         result = method.getStatusLine().toString();
       } else {
         result = "Action Complete";
       }
 
     } catch (Exception e) {
       throw new PacServiceException(e);
     } finally {
       method.releaseConnection();
     }
 
     return result;
   }
 
   public boolean createRole(ProxyPentahoRole proxyRole) throws PacServiceException {
     boolean result = false;
     IPentahoRole role = new PentahoRole(proxyRole.getName());
     role.setDescription(proxyRole.getDescription());
     try {
       userRoleMgmtService.beginTransaction();
       userRoleMgmtService.createRole(role);
       userRoleMgmtService.commitTransaction();
       result = true;
     } catch ( DAOException e) {
       rollbackTransaction();
       throw new PacServiceException( 
           Messages.getErrorString( "PacService.ERROR_0001_ROLE_CREATION_FAILED", proxyRole.getName() ), e ); //$NON-NLS-1$
     } catch (PentahoSecurityException e) {
       rollbackTransaction();
       throw new PacServiceException( 
           Messages.getErrorString( "PacService.ERROR_0002_NO_CREATE_ROLE_PERMISSION", proxyRole.getName() ), e ); //$NON-NLS-1$
     }
     finally {
       userRoleMgmtService.closeSession();
     }
     return result;
   }
 
   public boolean deleteRoles(ProxyPentahoRole[] roles) throws PacServiceException {
     boolean result = false;
     IPentahoRole[] persistedRoles;
     try {
       persistedRoles = new IPentahoRole[roles.length];
       for (int i = 0; i < roles.length; i++) {
         persistedRoles[i] = userRoleMgmtService.getRole(roles[i].getName());
         if ( null == persistedRoles[i] )
         {
           throw new PacServiceException(
               Messages.getString("PacService.ROLE_DELETION_FAILED_NO_ROLE", roles[i].getName() ) ); //$NON-NLS-1$
         }
       }
       userRoleMgmtService.beginTransaction();
       for (int i = 0; i < persistedRoles.length; i++) {
         userRoleMgmtService.deleteRole( persistedRoles[i] );
       }
       userRoleMgmtService.commitTransaction();
       result = true;
     } catch (NonExistingRoleException e) {
       rollbackTransaction();
       throw new PacServiceException(
           Messages.getString("PacService.ROLE_DELETION_FAILED_NO_ROLE", e.getMessage())); //$NON-NLS-1$
     } catch (DAOException e) {
       rollbackTransaction();
       throw new PacServiceException(
           Messages.getString("PacService.ROLE_DELETION_FAILED", e.getMessage())); //$NON-NLS-1$
     } catch (PentahoSecurityException e) {
       rollbackTransaction();
       throw new PacServiceException(
           Messages.getString("PacService.USER_DELETION_FAILED_NO_PERMISSION",  e.getMessage())); //$NON-NLS-1$
     }
     finally {
       userRoleMgmtService.closeSession();
     }
     return result;
   }
 
   public ProxyPentahoRole[] getRoles() throws PacServiceException {
     ProxyPentahoRole[] proxyRoles = new ProxyPentahoRole[0];
     try {
       List<IPentahoRole> roles = userRoleMgmtService.getRoles();
       proxyRoles = new ProxyPentahoRole[roles.size()];
       int i = 0;
       for (IPentahoRole role : roles) {       
         ProxyPentahoRole proxyPentahoRole = new ProxyPentahoRole();
         proxyPentahoRole.setName(role.getName());
         proxyPentahoRole.setDescription(role.getDescription());
         proxyRoles[i++] = proxyPentahoRole;
       }
     } catch (DAOException e) {
       throw new PacServiceException(
           Messages.getString("PacService.FAILED_TO_GET_ROLE_NAME" ), e ); //$NON-NLS-1$
     }
     finally {
       userRoleMgmtService.closeSession();
     }
     return proxyRoles;
   }
 
   public boolean updateRole(ProxyPentahoRole proxyPentahoRole) throws PacServiceException {
     boolean result = false;
     try {
       IPentahoRole role = userRoleMgmtService.getRole(proxyPentahoRole.getName());
       if ( null == role )
       {
         throw new PacServiceException(
             Messages.getString("PacService.ROLE_UPDATE_FAILED_DOES_NOT_EXIST", proxyPentahoRole.getName()) ); //$NON-NLS-1$
       }
       userRoleMgmtService.beginTransaction();
       role.setDescription(proxyPentahoRole.getDescription());
       userRoleMgmtService.updateRole( role );
       userRoleMgmtService.commitTransaction();
       result = true;
     } catch (DAOException e) {
       rollbackTransaction();
       throw new PacServiceException(
           Messages.getString("PacService.ROLE_UPDATE_FAILED", proxyPentahoRole.getName() ), e ); //$NON-NLS-1$
     } catch (PentahoSecurityException e) {
       rollbackTransaction();
       throw new PacServiceException(
           Messages.getString("PacService.ROLE_UPDATE_FAILED_NO_PERMISSION", proxyPentahoRole.getName() ), e );  //$NON-NLS-1$
     } catch (NonExistingRoleException e) {
       rollbackTransaction();
       throw new PacServiceException(
           Messages.getString("PacService.ROLE_UPDATE_FAILED_USER_DOES_NOT_EXIST", proxyPentahoRole.getName(), /*role name*/e.getMessage() ), e ); //$NON-NLS-1$
     }
     finally {
       userRoleMgmtService.closeSession();
     }
     return result;
   }
   
   public String getHomePage(String url) throws PacServiceException {
 	    String html = null;
 	    HttpClient client = new HttpClient();
 	    HttpMethod method = new GetMethod(url);
 	    method.getParams().setParameter("http.socket.timeout", new Integer(15000));
 	    
 	    try{
 	      int statusCode = client.executeMethod(method);
 
 	      if (statusCode != HttpStatus.SC_OK) {
 	    	  throw new PacServiceException(Messages.getString("PacService.ERROR_ACCESSING_URL", url )); //$NON-NLS-1$	        
 	      }
 	      
 	      byte[] responseBody = method.getResponseBody();
 	      html = new String(responseBody);
 	      html = html.substring(html.indexOf("<body>")+6);
 	      html = html.substring(0, html.indexOf("</body>"));
 	    } catch(Exception e){
 	        return showStatic();
 	    } finally {
 	      method.releaseConnection();
 	    }
 	    
 	    return html.toString();
 	  }
 	  
 private String showStatic(){
   InputStream flatFile = getClass().getResourceAsStream("defaultHome.ftl");
   StringBuffer sb = new StringBuffer(8096);
   int i = 0;
   try {
   	while (i != -1) {
   		i = flatFile.read();
   		sb.append((char) i);
       }
   } catch (IOException io) {
             	                  	  
   }
   return sb.toString();
 }
 
   
 
   
 }
