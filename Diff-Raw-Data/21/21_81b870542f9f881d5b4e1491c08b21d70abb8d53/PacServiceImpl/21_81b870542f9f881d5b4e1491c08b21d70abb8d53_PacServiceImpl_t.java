 package org.pentaho.pac.server;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.Driver;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 
 import org.apache.commons.httpclient.params.HttpMethodParams;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.pentaho.pac.client.PacService;
 import org.pentaho.pac.common.PacServiceException;
 import org.pentaho.pac.common.PentahoSecurityException;
 import org.pentaho.pac.common.UserRoleSecurityInfo;
 import org.pentaho.pac.common.UserToRoleAssignment;
 import org.pentaho.pac.common.datasources.DataSourceManagementException;
 import org.pentaho.pac.common.datasources.DuplicateDataSourceException;
 import org.pentaho.pac.common.datasources.NonExistingDataSourceException;
 import org.pentaho.pac.common.datasources.PentahoDataSource;
 import org.pentaho.pac.common.roles.DuplicateRoleException;
 import org.pentaho.pac.common.roles.NonExistingRoleException;
 import org.pentaho.pac.common.roles.ProxyPentahoRole;
 import org.pentaho.pac.common.users.DuplicateUserException;
 import org.pentaho.pac.common.users.NonExistingUserException;
 import org.pentaho.pac.common.users.ProxyPentahoUser;
import org.pentaho.pac.server.biplatformproxy.xmlserializer.XActionXmlSerializer;
import org.pentaho.pac.server.biplatformproxy.xmlserializer.XmlSerializerException;
 import org.pentaho.pac.server.common.AppConfigProperties;
 import org.pentaho.pac.server.common.BiServerTrustedProxy;
 import org.pentaho.pac.server.common.DAOException;
 import org.pentaho.pac.server.common.PasswordServiceFactory;
 import org.pentaho.pac.server.common.ProxyException;
 import org.pentaho.pac.server.common.ThreadSafeHttpClient;
 import org.pentaho.pac.server.common.ThreadSafeHttpClient.HttpMethodType;
 import org.pentaho.pac.server.datasources.DataSourceMgmtService;
 import org.pentaho.pac.server.datasources.IDataSourceMgmtService;
 import org.pentaho.pac.server.i18n.Messages;
 import org.pentaho.platform.api.util.IPasswordService;
 import org.pentaho.platform.api.util.PasswordServiceException;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 public class PacServiceImpl extends RemoteServiceServlet implements PacService {
 
   /**
    * 
    */
   private static final long serialVersionUID = 420L;
 
   private IUserRoleMgmtService userRoleMgmtService = new UserRoleMgmtService();
 
   private IDataSourceMgmtService dataSourceMgmtService = new DataSourceMgmtService();
 
   private static final Log logger = LogFactory.getLog(PacServiceImpl.class);
   private static final int DEFAULT_CHECK_PERIOD = 30000; // 30 seconds
 
   private static BiServerTrustedProxy biServerProxy;
   static {
     biServerProxy = BiServerTrustedProxy.getInstance();
   }
   
   private String jmxHostName = null;
   private String jmxPortNumber = null;
   private String userName = null;
   private String pciContextPath = null;
   private String biServerBaseURL = null;
   private int biServerStatusCheckPeriod = -1;
   
   public PacServiceImpl()
   {
   }
 
   public void init(ServletConfig config) throws ServletException {
     super.init( config );
     initFromConfiguration();
   }
   
   public UserRoleSecurityInfo getUserRoleSecurityInfo() throws PacServiceException {
     UserRoleSecurityInfo userRoleSecurityInfo = new UserRoleSecurityInfo();
     try {
       List<IPentahoUser> users = userRoleMgmtService.getUsers();
       for (IPentahoUser user : users) {
         ProxyPentahoUser proxyPentahoUser = new ProxyPentahoUser();
         proxyPentahoUser.setName(user.getName());
         proxyPentahoUser.setDescription(user.getDescription());
         proxyPentahoUser.setEnabled(user.getEnabled());
         proxyPentahoUser.setPassword(user.getPassword());
         userRoleSecurityInfo.getUsers().add(proxyPentahoUser);
 
         Set<IPentahoRole> roles = user.getRoles();
         for (IPentahoRole role : roles) {
           userRoleSecurityInfo.getAssignments().add(new UserToRoleAssignment(user.getName(), role.getName()));
         }
       }
       userRoleSecurityInfo.getRoles().addAll(Arrays.asList(getRoles()));
     } catch (DAOException e) {
       throw new PacServiceException(
           Messages.getString("PacService.FAILED_TO_GET_USER_NAME" ), e ); //$NON-NLS-1$
     }    
     finally {
       userRoleMgmtService.closeSession();
     }
     return userRoleSecurityInfo;
   }
 
   public boolean createUser(ProxyPentahoUser proxyUser) throws DuplicateUserException, PentahoSecurityException,
       PacServiceException {
     boolean result = false;
     IPentahoUser user = new PentahoUser(proxyUser.getName());
     user.setDescription(proxyUser.getDescription());
     user.setPassword(proxyUser.getPassword());
     user.setEnabled(proxyUser.getEnabled());
     try {
       userRoleMgmtService.beginTransaction();
       userRoleMgmtService.createUser(user);
       userRoleMgmtService.commitTransaction();
       result = true;
     } catch (DAOException e) {
       String msg = Messages.getString("PacService.ERROR_0004_USER_CREATION_FAILED", proxyUser.getName()) //$NON-NLS-1$
           + " " + e.getMessage(); //$NON-NLS-1$
       throw new PacServiceException(msg, e);
     } finally {
       if (!result) {
         rollbackTransaction();
       }
       userRoleMgmtService.closeSession();
     }
     return result;
   }
 
   public boolean deleteUsers(ProxyPentahoUser[] users) throws NonExistingUserException, PentahoSecurityException,
       PacServiceException {
     boolean result = false;
     IPentahoUser[] persistedUsers;
     try {
       persistedUsers = new IPentahoUser[users.length];
       for (int i = 0; i < users.length; i++) {
         persistedUsers[i] = userRoleMgmtService.getUser(users[i].getName());
         if (null == persistedUsers[i]) {
           throw new NonExistingUserException(users[i].getName());
         }
       }
       userRoleMgmtService.beginTransaction();
       for (int i = 0; i < persistedUsers.length; i++) {
         userRoleMgmtService.deleteUser(persistedUsers[i]);
       }
       userRoleMgmtService.commitTransaction();
       result = true;
     } catch (DAOException e) {
       throw new PacServiceException(Messages.getString("PacService.USER_DELETION_FAILED", e.getMessage())); //$NON-NLS-1$
     } finally {
       if (!result) {
         rollbackTransaction();
       }
       userRoleMgmtService.closeSession();
     }
     return result;
   }
 
   public ProxyPentahoUser getUser(String pUserName) throws PacServiceException {
     ProxyPentahoUser proxyPentahoUser = null;
     try {
       IPentahoUser user = userRoleMgmtService.getUser(pUserName);
       if (null != user) {
         proxyPentahoUser = new ProxyPentahoUser();
         proxyPentahoUser.setName(user.getName());
         proxyPentahoUser.setDescription(user.getDescription());
         proxyPentahoUser.setEnabled(user.getEnabled());
         proxyPentahoUser.setPassword(user.getPassword());
       }
     } catch (DAOException e) {
       throw new PacServiceException(Messages.getString("PacService.FAILED_TO_FIND_USER", pUserName), e); //$NON-NLS-1$
     } finally {
       userRoleMgmtService.closeSession();
     }
     return proxyPentahoUser;
   }
 
   public ProxyPentahoUser[] getUsers() throws PacServiceException {
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
       throw new PacServiceException(Messages.getString("PacService.FAILED_TO_GET_USER_NAME"), e); //$NON-NLS-1$
     } finally {
       userRoleMgmtService.closeSession();
     }
     return proxyUsers;
   }
 
   public ProxyPentahoUser[] getUsers(ProxyPentahoRole proxyRole) throws NonExistingRoleException, PacServiceException {
     ArrayList<ProxyPentahoUser> users = new ArrayList<ProxyPentahoUser>();
     try {
       IPentahoRole role = userRoleMgmtService.getRole(proxyRole.getName());
       if (null != role) {
         for (IPentahoUser user : role.getUsers()) {
           ProxyPentahoUser proxyPentahoUser = new ProxyPentahoUser();
           proxyPentahoUser.setName(user.getName());
           proxyPentahoUser.setDescription(user.getDescription());
           proxyPentahoUser.setEnabled(user.getEnabled());
           proxyPentahoUser.setPassword(user.getPassword());
           users.add(proxyPentahoUser);
         }
       } else {
         throw new NonExistingRoleException(proxyRole.getName());
       }
     } catch (DAOException e) {
       throw new PacServiceException(Messages.getString("PacService.FAILED_TO_FIND_USER", proxyRole.getName()), e); //$NON-NLS-1$
     } finally {
       userRoleMgmtService.closeSession();
     }
     return users.toArray(new ProxyPentahoUser[0]);
   }
 
   public boolean updateUser(ProxyPentahoUser proxyUser) throws NonExistingUserException, PentahoSecurityException,
       PacServiceException {
     boolean result = false;
     try {
       IPentahoUser user = userRoleMgmtService.getUser(proxyUser.getName());
       if (null == user) {
         throw new NonExistingUserException(proxyUser.getName());
       }
       userRoleMgmtService.beginTransaction();
       user.setPassword(proxyUser.getPassword());
       user.setEnabled(proxyUser.getEnabled());
       user.setDescription(proxyUser.getDescription());
       userRoleMgmtService.updateUser(user);
       userRoleMgmtService.commitTransaction();
       result = true;
     } catch (DAOException e) {
       String msg = Messages.getString("PacService.USER_UPDATE_FAILED", proxyUser.getName()) //$NON-NLS-1$
           + " " + e.getMessage(); //$NON-NLS-1$
       throw new PacServiceException(msg, e);
     } finally {
       if (!result) {
         rollbackTransaction();
       }
       userRoleMgmtService.closeSession();
     }
     return result;
   }
   public void setRoles(ProxyPentahoUser proxyUser, ProxyPentahoRole[] assignedRoles) throws NonExistingRoleException, NonExistingUserException, PentahoSecurityException, PacServiceException{
     boolean result = false;
     try {
       IPentahoUser user = userRoleMgmtService.getUser( proxyUser.getName() );
       if ( null == user )
       {
         throw new NonExistingUserException(proxyUser.getName());
       }
       Set<IPentahoRole> currentRoleAssignments = user.getRoles();
       List<IPentahoRole> assignmentsToRemove = new ArrayList<IPentahoRole>();
       List<IPentahoRole> assignmentsToAdd = new ArrayList<IPentahoRole>();
       
       ArrayList<String> roleNames = new ArrayList<String>();
       for (int i = 0; i < assignedRoles.length; i++) {
         roleNames.add(assignedRoles[i].getName());
       }      
       for (IPentahoRole currentlyAssignedRole : currentRoleAssignments) {
         if (!roleNames.contains(currentlyAssignedRole.getName())) {
           assignmentsToRemove.add(currentlyAssignedRole);
         }
       }
       
       roleNames.clear();
       for (IPentahoRole currentlyAssignedRole : currentRoleAssignments) {
         roleNames.add(currentlyAssignedRole.getName());
       }
       for (int i = 0; i < assignedRoles.length; i++) {
         if (!roleNames.contains(assignedRoles[i].getName())) {
           IPentahoRole pentahoRole = userRoleMgmtService.getRole(assignedRoles[i].getName());
           if (pentahoRole != null) {
             assignmentsToAdd.add(pentahoRole);
           } else {
             throw new NonExistingRoleException(assignedRoles[i].getName());
           }
         }
       }      
       
       userRoleMgmtService.beginTransaction();
       for (IPentahoRole role : assignmentsToRemove) {
         role.removeUser(user);
         userRoleMgmtService.updateRole( role );
       }
       for (IPentahoRole role : assignmentsToAdd) {
         role.addUser(user);
         userRoleMgmtService.updateRole( role );
       }
       userRoleMgmtService.commitTransaction();
       result = true;
     } catch (DAOException e) {
       rollbackTransaction();
       throw new PacServiceException(
           Messages.getString("PacService.ROLE_UPDATE_FAILED", proxyUser.getName() ), e ); //$NON-NLS-1$
     }
     finally {
       if (!result) {
         rollbackTransaction();
       }
       userRoleMgmtService.closeSession();
     }
   }
   
   public void setUsers( ProxyPentahoRole proxyRole, ProxyPentahoUser[] assignedUsers ) throws NonExistingRoleException, NonExistingUserException, PentahoSecurityException, PacServiceException
   {
     boolean result = false;
     try {
       IPentahoRole role = userRoleMgmtService.getRole( proxyRole.getName() );
       if ( null == role )
       {
         throw new NonExistingRoleException(proxyRole.getName());
       }
       Set<IPentahoUser> currentUserAssignments = role.getUsers();
       List<IPentahoUser> assignmentsToRemove = new ArrayList<IPentahoUser>();
       List<IPentahoUser> assignmentsToAdd = new ArrayList<IPentahoUser>();
       
       ArrayList<String> userNames = new ArrayList<String>();
       for (int i = 0; i < assignedUsers.length; i++) {
         userNames.add(assignedUsers[i].getName());
       }      
       for (IPentahoUser currentlyAssignedUser : currentUserAssignments) {
         if (!userNames.contains(currentlyAssignedUser.getName())) {
           assignmentsToRemove.add(currentlyAssignedUser);
         }
       }
       
       userNames.clear();
       for (IPentahoUser currentlyAssignedUser : currentUserAssignments) {
         userNames.add(currentlyAssignedUser.getName());
       }
       for (int i = 0; i < assignedUsers.length; i++) {
         if (!userNames.contains(assignedUsers[i].getName())) {
           IPentahoUser pentahoUser = userRoleMgmtService.getUser(assignedUsers[i].getName());
           if (pentahoUser != null) {
             assignmentsToAdd.add(pentahoUser);
           } else {
             throw new NonExistingUserException(assignedUsers[i].getName());
           }
         }
       }      
       
       userRoleMgmtService.beginTransaction();
       for (IPentahoUser user : assignmentsToRemove) {
         role.removeUser(user);
       }
       for (IPentahoUser user : assignmentsToAdd) {
         role.addUser(user);
       }
       userRoleMgmtService.updateRole( role );
       userRoleMgmtService.commitTransaction();
       result = true;
     } catch (DAOException e) {
       rollbackTransaction();
       throw new PacServiceException(
           Messages.getString("PacService.ROLE_UPDATE_FAILED", proxyRole.getName() ), e ); //$NON-NLS-1$
     }
     finally {
       if (!result) {
         rollbackTransaction();
       }
       userRoleMgmtService.closeSession();
     }
   }
 
   public void updateRole(String roleName, String description, List<String> userNames) throws NonExistingRoleException,
       NonExistingUserException, PentahoSecurityException, PacServiceException {
     try {
       IPentahoRole role = userRoleMgmtService.getRole(roleName);
       if (null == role) {
         throw new PacServiceException(Messages.getString("PacService.ROLE_UPDATE_FAILED", roleName)); //$NON-NLS-1$
       }
       userRoleMgmtService.beginTransaction();
       role.setDescription(description);
       role.setUsers(userNames);
       userRoleMgmtService.updateRole(role);
       userRoleMgmtService.commitTransaction();
     } catch (DAOException e) {
       rollbackTransaction();
       throw new PacServiceException(Messages.getString("PacService.ROLE_UPDATE_FAILED", roleName), e); //$NON-NLS-1$
     } finally {
       userRoleMgmtService.closeSession();
     }
   }
 
   public boolean createDataSource(PentahoDataSource dataSource) throws PacServiceException {
     boolean result = false;
     try {
       dataSourceMgmtService.beginTransaction();
       // Get the password service
       IPasswordService passwordService = PasswordServiceFactory.getPasswordService();
       // Store the new encrypted password in the datasource object
       String encryptedPassword = passwordService.encrypt(dataSource.getPassword());
       dataSource.setPassword(encryptedPassword);
       dataSourceMgmtService.createDataSource(dataSource);
       dataSourceMgmtService.commitTransaction();
       result = true;
     } catch(PasswordServiceException pse) {
       throw new PacServiceException( pse.getMessage(), pse );
     } catch (DuplicateDataSourceException dde) {
       throw new PacServiceException(Messages.getString("PacService.ERROR_0009_DATASOURCE_ALREADY_EXIST", dataSource.getName()), dde);
 
     } catch (DAOException e) {
       throw new PacServiceException(Messages.getString("PacService.ERROR_0007_DATASOURCE_CREATION_FAILED", dataSource.getName()), e);
     } catch (PentahoSecurityException pse) {
       throw new PacServiceException(Messages.getString("PacService.ERROR_0008_NO_CREATE_DATASOURCE_PERMISSION", dataSource.getName()), pse);
 
     } finally {
       if (!result) {
         rollbackTransaction();
       }
       dataSourceMgmtService.closeSession();
     }
     return result;
 
   }
 
   public boolean deleteDataSources(PentahoDataSource[] dataSources) throws PacServiceException {
     boolean result = false;
     PentahoDataSource persistedDataSources = null;
     try {
       dataSourceMgmtService.beginTransaction();
       for (int i = 0; i < dataSources.length; i++) {
         persistedDataSources = dataSourceMgmtService.getDataSource(dataSources[i].getName());
         dataSourceMgmtService.deleteDataSource(persistedDataSources);
       }
       result = true;
       dataSourceMgmtService.commitTransaction();
     } catch (NonExistingDataSourceException neds) {
       String msg = Messages.getString(
           "PacService.DATASOURCE_DELETION_FAILED_NO_DATASOURCE", persistedDataSources.getName()) //$NON-NLS-1$
           + " " + neds.getMessage(); //$NON-NLS-1$
       throw new PacServiceException(msg, neds);
     } catch (DAOException e) {
       throw new PacServiceException(Messages.getString("PacService.DATASOURCE_DELETION_FAILED", persistedDataSources.getName()), e);
     } catch (PentahoSecurityException pse) {
       throw new PacServiceException(Messages.getString("PacService.DATASOURCE_DELETION_FAILED_NO_PERMISSION", persistedDataSources.getName()), pse);
     } finally {
       if (!result) {
         rollbackTransaction();
       }
       userRoleMgmtService.closeSession();
     }
     return result;
   }
 
   public boolean updateDataSource(PentahoDataSource dataSource) throws PacServiceException {
     boolean result = false;
     try {
       PentahoDataSource ds = dataSourceMgmtService.getDataSource(dataSource.getName());
       if (null == ds) {
         throw new NonExistingDataSourceException(dataSource.getName());
       }
       ds.setDriverClass(dataSource.getDriverClass());
       if(dataSource.getIdleConn() < 0) {
         ds.setIdleConn(0);
       } else {
         ds.setIdleConn(dataSource.getIdleConn());  
       }
       if(dataSource.getMaxActConn() < 0) {
         ds.setMaxActConn(0);
       } else {
         ds.setMaxActConn(dataSource.getMaxActConn());  
       }
       
       IPasswordService passwordService = PasswordServiceFactory.getPasswordService();
       // Store the new encrypted password in the datasource object
       ds.setPassword(passwordService.encrypt(dataSource.getPassword()));
       ds.setQuery(dataSource.getQuery());
       ds.setUrl(dataSource.getUrl());
       ds.setUserName(dataSource.getUserName());
       if(dataSource.getMaxActConn() < 0) {
         ds.setWait(0);
       } else {
         ds.setWait(dataSource.getWait());  
       }
       
       dataSourceMgmtService.beginTransaction();
       dataSourceMgmtService.updateDataSource(ds);
       dataSourceMgmtService.commitTransaction();
       result = true;
     } catch(PasswordServiceException pse) {
         throw new PacServiceException( pse.getMessage(), pse );
     } catch (NonExistingDataSourceException neds) {
       throw new PacServiceException(Messages.getString("PacService.DATASOURCE_UPDATE_FAILED_DOES_NOT_EXIST", dataSource.getName()), neds);
     } catch (DAOException e) {
       throw new PacServiceException(Messages.getString("PacService.DATASOURCE_UPDATE_FAILED", dataSource.getName()), e);
     } catch (PentahoSecurityException pse) {
       throw new PacServiceException(Messages.getString("PacService.DATASOURCE_UPDATE_FAILED_NO_PERMISSION", dataSource.getName()), pse);
 
     } finally {
       if (!result) {
         rollbackTransaction();
       }
       dataSourceMgmtService.closeSession();
     }
     return result;
   }
 
   public PentahoDataSource[] getDataSources() throws PacServiceException {
     List<PentahoDataSource> dataSources;
     try {
       dataSources = dataSourceMgmtService.getDataSources();
       for(PentahoDataSource pentahoDataSource: dataSources) {
         try {
               // Get the password service
           if(pentahoDataSource != null) {
             IPasswordService passwordService = PasswordServiceFactory.getPasswordService();
             String decryptedPassword = passwordService.decrypt(pentahoDataSource.getPassword());
             pentahoDataSource.setPassword(decryptedPassword);
           }
         } catch(PasswordServiceException pse) {
           throw new DAOException( pse.getMessage(), pse );
         }         
       }
 
     } catch (DAOException e) {
       // TODO need a way better error message here please, maybe include some information from the exception?
       throw new PacServiceException(Messages.getString("PacService.FAILED_TO_GET_DATASDOURCE"), e); //$NON-NLS-1$
     } finally {
       dataSourceMgmtService.closeSession();
     }
     return dataSources.toArray(new PentahoDataSource[0]);
 
   }
 
   /**
    * NOTE: caller is responsible for closing connection
    * 
    * @param ds
    * @return
    * @throws DataSourceManagementException
    */
   private static Connection getDataSourceConnection(PentahoDataSource ds) throws DataSourceManagementException {
     Connection conn = null;
 
     String driverClass = ds.getDriverClass();
     if (StringUtils.isEmpty(driverClass)) {
       throw new DataSourceManagementException(Messages.getString("PacService.CONNECTION_ATTEMPT_FAILED",driverClass)); //$NON-NLS-1$  
     }
     Class<?> driverC = null;
 
     try {
       driverC = Class.forName(driverClass);
     } catch (ClassNotFoundException e) {
       throw new DataSourceManagementException(Messages.getString("PacService.DRIVER_NOT_FOUND_IN_CLASSPATH",driverClass), e); //$NON-NLS-1$
     }
     if (!Driver.class.isAssignableFrom(driverC)) {
       throw new DataSourceManagementException(Messages.getString("PacService.DRIVER_NOT_FOUND_IN_CLASSPATH",driverClass)); //$NON-NLS-1$    }
     }
     Driver driver = null;
     
     try {
       driver = driverC.asSubclass(Driver.class).newInstance();
     } catch (InstantiationException e) {
       throw new DataSourceManagementException(Messages.getString("PacService.UNABLE_TO_INSTANCE_DRIVER",driverClass), e); //$NON-NLS-1$
     } catch (IllegalAccessException e) {
       throw new DataSourceManagementException(Messages.getString("PacService.UNABLE_TO_INSTANCE_DRIVER",driverClass), e); //$NON-NLS-1$    }
     }
     try {
       DriverManager.registerDriver(driver);
       conn = DriverManager.getConnection(ds.getUrl(), ds.getUserName(), ds.getPassword());
       return conn;
     } catch (SQLException e) {
       throw new DataSourceManagementException(Messages.getString("PacService.UNABLE_TO_CONNECT",e.getMessage()), e); //$NON-NLS-1$
     }
   }
 
   public boolean testDataSourceConnection(PentahoDataSource ds) throws PacServiceException {
     Connection conn = null;
     try {
       conn = getDataSourceConnection(ds);
     } catch (DataSourceManagementException dme) {
       throw new PacServiceException(dme.getMessage(), dme);
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
 
   public boolean testDataSourceValidationQuery(PentahoDataSource ds) throws PacServiceException {
     Connection conn = null;
     Statement stmt = null;
     ResultSet rs = null;
     try {
       conn = getDataSourceConnection(ds);
 
       if (!StringUtils.isEmpty(ds.getQuery())) {
         stmt = conn.createStatement();
         rs = stmt.executeQuery(ds.getQuery());
       } else {
         throw new PacServiceException(Messages.getString("PacService.QUERY_NOT_VALID")); //$NON-NLS-1$
       }
     } catch (DataSourceManagementException dme) {
       throw new PacServiceException(Messages.getString("PacService.QUERY_VALIDATION_FAILED",ds.getQuery()), dme); //$NON-NLS-1$
     } catch (SQLException e) {
       throw new PacServiceException(Messages.getString("PacService.QUERY_VALIDATION_FAILED",ds.getQuery()), e); //$NON-NLS-1$
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
     } catch (Exception e) {
       logger.error( Messages.getString( "PacService.rollbackFailed" ) );  //$NON-NLS-1$
     }
   }
   
   public String refreshSolutionRepository() throws PacServiceException {
     return executePublishRequest("org.pentaho.platform.engine.services.solution.SolutionPublisher" ); //$NON-NLS-1$
   }
   
   public String cleanRepository() throws PacServiceException {
     return executeXAction("admin", "", "clean_repository.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
   }
   
 
   public String clearMondrianDataCache() throws PacServiceException {
     return executeXAction("admin", "", "clear_mondrian_data_cache.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
   }
   
 
   public String clearMondrianSchemaCache() throws PacServiceException {
     return executeXAction("admin", "", "clear_mondrian_schema_cache.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
   }
   
 
   public String scheduleRepositoryCleaning() throws PacServiceException {
     return executeXAction("admin", "", "schedule-clean.xaction" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
   }
   
 
   public String resetRepository() throws PacServiceException {
     return resetSolutionRepository(getUserName() );
   }
   
 
   public String refreshSystemSettings() throws PacServiceException {
       return executePublishRequest("org.pentaho.core.system.SettingsPublisher" ); //$NON-NLS-1$
   }
   
 
   public String executeGlobalActions() throws PacServiceException {
     return executePublishRequest("org.pentaho.core.system.GlobalListsPublisher" ); //$NON-NLS-1$
   }
   
 
   public String refreshReportingMetadata() throws PacServiceException {
     return executePublishRequest("org.pentaho.plugin.mql.MetadataPublisher" ); //$NON-NLS-1$
   }
   
   private void initFromConfiguration()
   {
     AppConfigProperties appCfg = AppConfigProperties.getInstance();
     jmxHostName = StringUtils.defaultIfEmpty( appCfg.getProperty("jmxHostName"), System.getProperty("jmxHostName") ); //$NON-NLS-1$ //$NON-NLS-2$
     jmxPortNumber = StringUtils.defaultIfEmpty( appCfg.getProperty("jmxPortNumber"), System.getProperty("jmxPortNumber") ); //$NON-NLS-1$ //$NON-NLS-2$
     userName = StringUtils.defaultIfEmpty( appCfg.getProperty("pentaho.platform.userName"), System.getProperty("pentaho.platform.userName") ); //$NON-NLS-1$ //$NON-NLS-2$
     pciContextPath = StringUtils.defaultIfEmpty( appCfg.getProperty("pciContextPath"), System.getProperty("pciContextPath") ); //$NON-NLS-1$ //$NON-NLS-2$
     biServerBaseURL = StringUtils.defaultIfEmpty( appCfg.getProperty("biServerBaseURL"), System.getProperty("biServerBaseURL") ); //$NON-NLS-1$ //$NON-NLS-2$biServerBaseURL = StringUtils.defaultIfEmpty( p.getProperty("biServerBaseURL"), System.getProperty("biServerBaseURL") );
     biServerProxy.setBaseUrl( biServerBaseURL );
     String strBiServerStatusCheckPeriod = StringUtils.defaultIfEmpty( appCfg.getProperty("consoleToolBar.biServerStatusCheckPeriod"), System.getProperty("consoleToolBar.biServerStatusCheckPeriod") ); //$NON-NLS-1$ //$NON-NLS-2$
     try {
       biServerStatusCheckPeriod = Integer.parseInt( strBiServerStatusCheckPeriod );
     } catch( NumberFormatException e ) {
       logger.error( Messages.getString( "PacService.THREAD_SCHEDULING_FAILED" ), e ); //$NON-NLS-1$
       biServerStatusCheckPeriod = DEFAULT_CHECK_PERIOD;
     }
   }
 
   public String getJmxHostName() {
     return jmxHostName;
   }
 
   public String getJmxPortNumber() {
     return jmxPortNumber;
   }
 
   public String getUserName() {
     return userName;
   }
   
   public String getPciContextPath() {
     return pciContextPath;
   }
   
   public String getBIServerBaseUrl() {
     return biServerBaseURL;
   }
 
   private String executeXAction(String solution, String path, String xAction ) throws PacServiceException{
 
     Map<String,Object> params = new HashMap<String,Object>();
     params.put( "solution", solution ); //$NON-NLS-1$
     params.put( "path", path ); //$NON-NLS-1$
     params.put( "action", xAction ); //$NON-NLS-1$
     
     String strResponse;
     try {
       strResponse = biServerProxy.execRemoteMethod( "ServiceAction", HttpMethodType.GET, userName, params );//$NON-NLS-1$
     } catch (ProxyException e) {
       throw new PacServiceException( e.getMessage(), e );
     } 
     XActionXmlSerializer s = new XActionXmlSerializer();
     String errorMsg;
     try {
       errorMsg = s.getXActionResponseStatusFromXml( strResponse );
     } catch (XmlSerializerException e) {
       throw new PacServiceException( e.getMessage(), e );
     }
     if ( null != errorMsg ) {
       throw new PacServiceException( errorMsg );
     }
     
     return Messages.getString( "PacService.ACTION_COMPLETE" ); //$NON-NLS-1$
   }
   
   private String executePublishRequest(String publisherClassName ) throws PacServiceException {
     
     Map<String,Object> params = new HashMap<String,Object>();
     params.put( "publish", "now" ); //$NON-NLS-1$ //$NON-NLS-2$
     params.put( "style", "popup" ); //$NON-NLS-1$ //$NON-NLS-2$
     params.put( "class", publisherClassName ); //$NON-NLS-1$
     
     String strResponse;
     try {
       strResponse = biServerProxy.execRemoteMethod( "Publish", HttpMethodType.GET, userName, params );//$NON-NLS-1$
     } catch (ProxyException e) {
       throw new PacServiceException( e.getMessage(), e );
     } 
     XActionXmlSerializer s = new XActionXmlSerializer();
     String errorMsg = s.getPublishStatusFromXml( strResponse );
     if ( null != errorMsg ) {
       throw new PacServiceException( errorMsg );
     }
     
     return Messages.getString( "PacService.ACTION_COMPLETE" );//$NON-NLS-1$
   }
   
   private String resetSolutionRepository(String userid ) throws PacServiceException {
 
     try {
       String strResponse = biServerProxy.execRemoteMethod( "ResetRepository", HttpMethodType.GET, userName, /*params*/null );//$NON-NLS-1$
     } catch (ProxyException e) {
       throw new PacServiceException( e.getMessage(), e );
     } 
     return Messages.getString( "PacService.ACTION_COMPLETE" ); //$NON-NLS-1$
   }
 
   public boolean createRole(ProxyPentahoRole proxyRole) throws DuplicateRoleException, PentahoSecurityException, PacServiceException {
     boolean result = false;
     IPentahoRole role = new PentahoRole(proxyRole.getName());
     role.setDescription(proxyRole.getDescription());
     try {
       userRoleMgmtService.beginTransaction();
       userRoleMgmtService.createRole(role);
       userRoleMgmtService.commitTransaction();
       result = true;
     } catch ( DAOException e) {
       throw new PacServiceException(e.getClass().getName() + " " + e.getMessage()); //$NON-NLS-1$
     }
     finally {
       if (!result) {
         rollbackTransaction();
       }
       userRoleMgmtService.closeSession();
     }
     return result;
   }
 
   public boolean deleteRoles(ProxyPentahoRole[] roles) throws NonExistingRoleException, PentahoSecurityException, PacServiceException {
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
     } catch (DAOException e) {
       throw new PacServiceException(
           Messages.getString("PacService.ROLE_DELETION_FAILED", e.getMessage())); //$NON-NLS-1$
     }
     finally {
       if (!result) {
         rollbackTransaction();
       }
       userRoleMgmtService.closeSession();
     }
     return result;
   }
 
   public ProxyPentahoRole[] getRoles(ProxyPentahoUser proxyUser) throws NonExistingUserException, PacServiceException {
     ArrayList<ProxyPentahoRole> roles = new ArrayList<ProxyPentahoRole>();
     try {
       IPentahoUser user = userRoleMgmtService.getUser( proxyUser.getName());
       if ( null != user )
       {
         for (IPentahoRole role : user.getRoles()) {
           ProxyPentahoRole proxyPentahoRole = new ProxyPentahoRole();
           proxyPentahoRole.setName(role.getName());
           proxyPentahoRole.setDescription(role.getDescription());
           roles.add(proxyPentahoRole);
         }
       } else {
         throw new NonExistingUserException(proxyUser.getName());
       }
     } catch (DAOException e) {
       throw new PacServiceException(
           Messages.getString("PacService.FAILED_TO_FIND_USER", proxyUser.getName() ), e ); //$NON-NLS-1$
     }
     finally {
       userRoleMgmtService.closeSession();
     }
     return (ProxyPentahoRole[])roles.toArray(new ProxyPentahoRole[0]);
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
   
   public String getHomePageAsHtml(String url) {
     
     ThreadSafeHttpClient client = new ThreadSafeHttpClient( url );
 
     Map<String,Object> params = new HashMap<String,Object>();
     String timeOut = AppConfigProperties.getInstance().getProperty( "homePage.timeout" );//$NON-NLS-1$
     params.put( HttpMethodParams.SO_TIMEOUT, timeOut );
     
     String html = null;
     try {
       html = client.execRemoteMethod( null, HttpMethodType.GET, params, "text/html" );//$NON-NLS-1$
     } catch (ProxyException e) {
       html = showStatic();
     }
     final String BODY_TAG = "<body>"; //$NON-NLS-1$
     
     int afterBodyIdx = html.indexOf(BODY_TAG);
     if ( -1 != afterBodyIdx ) {
       html = html.substring( html.indexOf(BODY_TAG) + BODY_TAG.length() );
       html = html.substring(0, html.indexOf("</body>")); //$NON-NLS-1$
     }
       
     return html;
   }
     
   private String showStatic(){
     String templateFileName = "defaultHome.ftl"; //$NON-NLS-1$
     InputStream flatFile = getClass().getResourceAsStream( templateFileName );
     try {
       return IOUtils.toString(flatFile);
     } catch (IOException e) {
       String msg = Messages.getString( "PacService.ioError", templateFileName ); //$NON-NLS-1$
       logger.error( msg,e);
       return "<span>" + msg + "</span>"; //$NON-NLS-1$ //$NON-NLS-2$
     }
   }
   
   public void isBiServerAlive() throws PacServiceException {
     ThreadSafeHttpClient c = new ThreadSafeHttpClient( biServerBaseURL );
     try {
       String response = c.execRemoteMethod( "ping/alive.gif", HttpMethodType.GET, null );//$NON-NLS-1$
     } catch (ProxyException e) {
       throw new PacServiceException( e.getMessage(), e );
     } 
   }
   
   public int getBiServerStatusCheckPeriod() {
     return biServerStatusCheckPeriod;
   }
 }
