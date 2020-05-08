 package org.trecena.modules.jira;
 
 import com.atlassian.jira.rpc.exception.RemoteAuthenticationException;
 import com.atlassian.jira.rpc.exception.RemotePermissionException;
 import com.atlassian.jira.rpc.soap.beans.*;
 import com.atlassian.jira.rpc.soap.jirasoapservice_v2.JiraSoapService;
 import com.atlassian.jira.rpc.soap.jirasoapservice_v2.JiraSoapServiceServiceLocator;
 import org.apache.axis.AxisFault;
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.oxm.xstream.XStreamMarshaller;
 import org.springframework.stereotype.Component;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 import org.trecena.ApplicationException;
 import org.trecena.ModuleManager;
 import org.trecena.UserManager;
 import org.trecena.beans.*;
 import org.trecena.dto.ResultDTO;
 import org.trecena.dto.UserDTO;
 import org.trecena.modules.jira.dto.*;
 
 import javax.annotation.PostConstruct;
 import javax.xml.rpc.ServiceException;
 import java.rmi.RemoteException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
 
 /**
  * Jira integration module.
  *
  * @author Ivan Latysh <ivanlatysh@gmail.com>
  * @version 0.1
  * @since 22/11/11 7:12 PM
  */
 @Transactional(propagation = Propagation.REQUIRED)
 @Component
 public class JiraModule {
   // Error prefix JIRA300032
 
   /** Logger */
   protected Logger logger = LoggerFactory.getLogger(this.getClass().getName());
 
   /** JiraModule name */
   public final static String MODULE_NAME = JiraModule.class.getName();
 
   /** JiraModule name */
   public final static String MODULE_TITLE = "Jira";
 
   /** Number of errors to occur before server is auto-disabled */
   public static final long ERROR_THRESHOLD = 3L;
 
   /** JiraModule mappingBean */
   protected ModuleBean moduleBean = new ModuleBean(null,
       MODULE_NAME,
       MODULE_TITLE,
       "jira/settings.html",
       "jira/userprofile.html",
       new Date());
 
   @Autowired
   protected ModuleManager moduleManager;
 
   /** User manager */
   @Autowired
   protected UserManager userManager;
 
   @Autowired
   protected SessionFactory sessionFactory;
 
   @Autowired
   protected XStreamMarshaller xstream;
 
   /**
    * Register jiraModule
    *
    * @throws ApplicationException when failed
    */
   @PostConstruct
   public void registerModule() throws ApplicationException {
     moduleBean = moduleManager.registerModule(moduleBean);
     // register annotated XStream DTO's
     xstream.setAnnotatedClasses(new Class[]{
         JiraServerDTO.class,
         JiraServerInfoDTO.class,
         JiraUserAccountMappingDTO.class,
         JiraIssueTypeMappingDTO.class,
         JiraIssueStatusMappingDTO.class,
         JiraIssuePriorityMappingDTO.class,
         JiraIssuePriorityDTO.class,
         JiraIssueStatusDTO.class,
         JiraIssueTypeDTO.class,
         JiraFilterDTO.class
     });
   }
 
   /**
    * Create and return JIRA SOAP service endpoint
    *
    * @param jiraServer jira server to get the service for
    * @return service endpoint
    * @throws javax.xml.rpc.ServiceException
    * @throws java.rmi.RemoteException
    */
   public JiraSoapService getService(JiraServerBean jiraServer) throws ServiceException, java.rmi.RemoteException {
     JiraSoapServiceServiceLocator jiraSoapServiceGetter = new JiraSoapServiceServiceLocator();
     // set endpoint URL
     jiraSoapServiceGetter.setEndpointAddress("JirasoapserviceV2", jiraServer.getUrl());
     // return service
     return jiraSoapServiceGetter.getJirasoapserviceV2();
   }
 
   /**
    * Login to given server
    * @param jiraServer server
    * @param jiraSoapService soap service
    * @throws ApplicationException when failed
    */
   public String login(JiraServerBean jiraServer, JiraSoapService jiraSoapService) throws ApplicationException {
     try {
       return jiraSoapService.login(jiraServer.getLogin(), jiraServer.getPassword());
     } catch (AxisFault axisex) {
       throw new ApplicationException("JIRA300003", "["+axisex.getFaultCode()+"] "+axisex.getFaultString(), axisex);
     } catch (RemoteException ex) {
       throw new ApplicationException("JIRA300003", ex.getMessage(), ex);
     }
   }
 
   /**
    * Return all configured JIRA servers
    *
    * @return server list
    * @throws ApplicationException when failed
    */
   public List<JiraServerBean> getServers() throws ApplicationException{
     try {
       Session session = sessionFactory.getCurrentSession();
       return session.createCriteria(JiraServerBean.class)
           .add(Restrictions.eq("enabled", true))
           .list();
     } catch (Exception ex) {
       logger.error( "[JIRA300003] Unable to load registered servers.", ex);
       throw new ApplicationException("JIRA300003", "Unable to load registered servers.", ex);
     }
   }
 
   /**
    * Return server with given ID
    *
    * @param id serve id
    * @return server or null
    * @throws ApplicationException when failes
    */
   public JiraServerBean getServer(Long id) throws ApplicationException {
     try {
       Session session = sessionFactory.getCurrentSession();
       return (JiraServerBean) session.get(JiraServerBean.class, id);
     } catch (Exception ex) {
       logger.error( "[JIRA300004] Unable to load server with id {"+id+"}", ex);
       throw new ApplicationException("JIRA300004", "Unable to load server with id {"+id+"}", ex);
     }
 
   }
 
   /**
    * Save given server
    *
    * @param serverBean server to save
    * @return saved server
    * @throws ApplicationException when failed
    */
   public JiraServerBean saveServer(JiraServerBean serverBean) throws ApplicationException {
     try {
       Session session = sessionFactory.getCurrentSession();
       // merge if required
       if (null!=serverBean.getId()) {
         serverBean = (JiraServerBean) session.merge(serverBean);
       } else {
         // set error threshold
         serverBean.setErrorThreshold(ERROR_THRESHOLD);
       }
       // save changes
       session.save(serverBean);
       // return saved server
       return serverBean;
     } catch (Exception ex) {
       logger.error( "[JIRA300005] Unable to save server {"+ serverBean +"}", ex);
       throw new ApplicationException("JIRA300005", "Unable to save server {"+ serverBean +"}", ex);
     }
   }
 
   /**
    * Delete server with given ID
    *
    * @param serverBean server mappingBean to delete
    * @throws ApplicationException when failed
    */
   public void deleteServer(JiraServerBean serverBean) throws ApplicationException {
     try {
       Session session = sessionFactory.getCurrentSession();
       session.delete(serverBean);
     } catch (Exception ex) {
       logger.error( "[JIRA300011] Unable to delete server {"+serverBean+"}.", ex);
       throw new ApplicationException("JIRA300011", "Unable to delete server {"+serverBean+"}.", ex);
     }
   }
 
   /**
    * Return Trecena user for given JIRA username
    *
    * @param username JIRA username
    * @param server server
    * @return Trecena user or null when no user has been found
    * @throws ApplicationException when failed
    */
   public UserBean getTrecenaUserForJiraUser(String username, JiraServerBean server) throws ApplicationException {
     try {
       Session session = sessionFactory.getCurrentSession();
 
       // check mappings
       List mappings = session.createCriteria(JiraUserAccountMappingBean.class)
           .add(Restrictions.eq("server", server))
           .add(Restrictions.eq("jiraUsername", username))
           .list();
       if (mappings.size()>0) {
         return ((JiraUserAccountMappingBean)mappings.get(0)).getUser();
       }
 
       // try direct mapping
       try {
         return userManager.getUserByLogin(username);
       } catch (Exception e) {
         logger.warn( "Unable to find Trecena user for JIRA user {"+username+"}.", e);
       }
 
       // unable to map the user
       return null;
     } catch (Exception ex) {
       logger.error( "[JIRA300009] Unable to find Trecena user for JIRA user {"+username+"}.", ex);
       throw new ApplicationException("JIRA300009", "Unable to find Trecena user for JIRA user {"+username+"}.", ex);
     }
   }
 
   /**
    * Return Trecena user bean for given username
    *
    * @param userName user name
    * @return Trecena user bean or null, if user has not ben found.
    * @throws ApplicationException when failed
    */
   public UserBean getUserBean(String userName) throws ApplicationException {
     return userManager.getUserByLogin(userName);
   }
 
   /**
    * Return user mappings for given server
    *
    *
    * @param server server
    * @param userBean user bean to get mappings for, or null to get mappings for all users
    * @return list of user mappings
    * @throws ApplicationException when failed
    */
   public Result<JiraUserAccountMappingBean> getUserMappings(JiraServerBean server, UserBean userBean, int firstResult, int maxResults) throws ApplicationException {
     Result<JiraUserAccountMappingBean> reslt = new Result<JiraUserAccountMappingBean>();
     try {
       Session session = sessionFactory.getCurrentSession();
       // count rows
       Criteria rowCountCriteria = session.createCriteria(JiraUserAccountMappingBean.class)
           .add(Restrictions.eq("server", server))
           .setProjection(Projections.rowCount());
       // set user restrictions if needed
       if (null!=userBean && null!=userBean.getId()) {
         rowCountCriteria.add(Restrictions.eq("user", userBean));
       }
       reslt.setAvailable(rowCountCriteria.uniqueResult());
       if (reslt.getAvailable() == 0) return reslt;
       // list records
       Criteria listCriteria = session.createCriteria(JiraUserAccountMappingBean.class)
           .add(Restrictions.eq("server", server))
           .setFirstResult(firstResult)
           .setMaxResults(maxResults);
       // set user restrictions if needed
       if (null!=userBean && null!=userBean.getId()) {
        listCriteria.add(Restrictions.eq("user", userBean));
       }
       List<JiraUserAccountMappingBean> mappings = listCriteria.list();
       for (JiraUserAccountMappingBean mappingBean : mappings) {
         reslt.getEntities().add(mappingBean);
       }
       // set returned
       reslt.setReturned(reslt.getEntities().size());
       // return result
       return reslt;
     } catch (Exception ex) {
       logger.error( "[JIRA300010] Unable to return user mappings.", ex);
       throw new ApplicationException("JIRA300010", "Unable to return user mappings.", ex);
     }
   }
 
   /**
    * Return jira user account mapping with given id
    * @param mappingId mapping id
    * @return mapping bean or null
    * @throws ApplicationException when failed
    */
   public JiraUserAccountMappingBean getUserMapping(Long mappingId) throws ApplicationException {
     try {
       Session session = sessionFactory.getCurrentSession();
       return (JiraUserAccountMappingBean) session.get(JiraUserAccountMappingBean.class, mappingId);
     } catch (Exception ex) {
       logger.error( "[JIRA300014] Unable to load user mapping with id {"+mappingId+"}", ex);
       throw new ApplicationException("JIRA300014", "Unable to load user mapping", ex);
     }
   }
 
   /**
    * Save user account mapping
    *
    * @param mappingBean mapping to save
    * @return saved mapping
    * @throws ApplicationException when failed
    */
   public JiraUserAccountMappingBean saveUserAccountMapping(JiraUserAccountMappingBean mappingBean) throws ApplicationException{
     try {
       // sanity check
       if (null==mappingBean) return null;
 
       Session session = sessionFactory.getCurrentSession();
       if (null!= mappingBean.getId()) {
         mappingBean = (JiraUserAccountMappingBean) session.merge(mappingBean);
       }
       session.saveOrUpdate(mappingBean);
       return mappingBean;
     } catch (Exception ex) {
       logger.error( "[JIRA300012] Unable to save user account mapping {"+mappingBean+"}", ex);
       throw new ApplicationException("JIRA300012", "Unable to save user account mapping.", ex);
     }
   }
 
   /**
    * Delete given mapping
    *
    * @param mappingBean mapping bean
    * @throws ApplicationException when failed
    */
   public void deleteUserAccountMapping(JiraUserAccountMappingBean mappingBean) throws ApplicationException {
     try {
       // sanity check
       if (null==mappingBean || null==mappingBean.getId()) return;
 
       Session session = sessionFactory.getCurrentSession();
       session.delete(mappingBean);
     } catch (Exception ex) {
       logger.error( "[JIRA300013] Unable to delete user account mapping {"+mappingBean+"}", ex);
       throw new ApplicationException("JIRA300013", "Unable to delete user account mapping", ex);
     }
   }
 
   /**
    * Return issue type mappings
    *
    * @param server server to get mappings for
    * @return type mappings
    * @throws ApplicationException when failed
    */
   public Result<JiraIssueTypeMappingBean> getIssueTypeMappings(JiraServerBean server) throws ApplicationException{
     Result<JiraIssueTypeMappingBean> reslt;
     try {
       Session session = sessionFactory.getCurrentSession();
       List<JiraIssueTypeMappingBean> _mappings = session.createCriteria(JiraIssueTypeMappingBean.class)
           .add(Restrictions.eq("server", server))
           .list();
       if (null==_mappings || _mappings.size()==0) return Result.EMPTY_RESULT;
       reslt = new Result<JiraIssueTypeMappingBean>();
       reslt.setIndex(0);
       reslt.setAvailable(_mappings.size());
       reslt.setReturned(_mappings.size());
       for (JiraIssueTypeMappingBean bean: _mappings) {
         reslt.getEntities().add(bean);
       }
       return reslt;
     } catch (Exception ex) {
       logger.error( "[JIRA300006] Unable to load issue type mappings for server {"+server+"}.", ex);
       throw new ApplicationException("JIRA300006", "Unable to load issue type mappings.", ex);
     }
   }
 
   /**
    * Return issue type mapping
    *
    * @param mappingId id of the mapping to return
    * @return type mapping
    * @throws ApplicationException when failed
    */
   public JiraIssueTypeMappingBean getIssueTypeMapping(Long mappingId) throws ApplicationException{
     try {
       Session session = sessionFactory.getCurrentSession();
       return (JiraIssueTypeMappingBean) session.get(JiraIssueTypeMappingBean.class, mappingId);
     } catch (Exception ex) {
       logger.error( "[JIRA300017] Unable to load issue type mapping with id {"+mappingId+"}.", ex);
       throw new ApplicationException("JIRA300017", "Unable to load issue type mapping.", ex);
     }
   }
 
   /**
    * Save given type mapping
    *
    * @param mappingBean mapping to save
    * @return saved mapping bean
    * @throws ApplicationException when failed
    */
   public JiraIssueTypeMappingBean saveIssueTypeMapping(JiraIssueTypeMappingBean mappingBean) throws ApplicationException{
     try {
       // sanity check
       if (null==mappingBean) return null;
 
       Session session = sessionFactory.getCurrentSession();
       if (null!=mappingBean.getId()) {
         mappingBean = (JiraIssueTypeMappingBean) session.merge(mappingBean);
       }
       session.saveOrUpdate(mappingBean);
       return mappingBean;
     } catch (Exception ex) {
       logger.error( "[JIRA300015] Unable to save issue type mapping bean {"+mappingBean+"}", ex);
       throw new ApplicationException("JIRA300015", "Unable to save issue type mapping bean.", ex);
     }
   }
 
   /**
    * Delete given issue type mapping bean
    *
    * @param mappingBean mapping bean to delete
    * @throws ApplicationException when failed
    */
   public void deleteIssueTypeMapping(JiraIssueTypeMappingBean mappingBean) throws ApplicationException {
     try {
       // sanity check
       if (null==mappingBean || null==mappingBean.getId()) return;
 
       Session session = sessionFactory.getCurrentSession();
       session.delete(mappingBean);
     } catch (Exception ex) {
       logger.error( "[JIRA300016] Unable to delete issue type mapping bean {"+mappingBean+"}", ex);
       throw new ApplicationException("JIRA300016", "Unable to delete issue type mapping bean.", ex);
     }
   }
 
   /**
    * Return issue status mappings
    *
    * @param server server to get mappings for
    * @return status mappings
    * @throws ApplicationException when failed
    */
   public Result<JiraIssueStatusMappingBean> getIssueStatusMappings(JiraServerBean server) throws ApplicationException{
     Result<JiraIssueStatusMappingBean> reslt;
     try {
       // sanity check
       if (null==server) return Result.EMPTY_RESULT;
 
       Session session = sessionFactory.getCurrentSession();
       List<JiraIssueStatusMappingBean> _mappings = session.createCriteria(JiraIssueStatusMappingBean.class)
           .add(Restrictions.eq("server", server))
           .list();
       if (null==_mappings || _mappings.size()==0) return Result.EMPTY_RESULT;
       reslt = new Result<JiraIssueStatusMappingBean>(null, 0, _mappings.size(), _mappings.size());
       for (JiraIssueStatusMappingBean bean: _mappings) {
         reslt.getEntities().add(bean);
       }
       return reslt;
     } catch (Exception ex) {
       logger.error( "[JIRA300007] Unable to load issue status mappings for server {"+server+"}.", ex);
       throw new ApplicationException("JIRA300007", "Unable to load issue status mappings.", ex);
     }
   }
 
   /**
    * Return issue status mapping with given ID
    *
    * @param mappingId mapping id
    * @return mapping with given id or null
    * @throws ApplicationException when failed
    */
   public JiraIssueStatusMappingBean getIssueStatusMapping(Long mappingId) throws ApplicationException{
     try {
       // sanity check
       if (null==mappingId) return null;
       Session session = sessionFactory.getCurrentSession();
       return (JiraIssueStatusMappingBean) session.get(JiraIssueStatusMappingBean.class, mappingId);
     } catch (Exception ex) {
       logger.error( "[JIRA300018] Unable to return issue status mapping with id {"+mappingId+"}", ex);
       throw new ApplicationException("JIRA300018", "Unable to return issue status mapping.", ex);
     }
   }
 
   /**
    * Save give issue status mapping
    *
    * @param mappingBean mapping to save
    * @return saved mapping
    * @throws ApplicationException when failed
    */
   public JiraIssueStatusMappingBean saveIssueStatusMapping(JiraIssueStatusMappingBean mappingBean) throws ApplicationException{
     try {
       // sanity check
       if (null==mappingBean) return null;
 
       Session session = sessionFactory.getCurrentSession();
       if (null!=mappingBean.getId()) {
         mappingBean = (JiraIssueStatusMappingBean) session.merge(mappingBean);
       }
       session.saveOrUpdate(mappingBean);
       return mappingBean;
     } catch (Exception ex) {
       logger.error( "[JIRA300019] Unable to save issue status mapping {"+mappingBean+"}", ex);
       throw new ApplicationException("JIRA300019", "Unable to save issue status mapping.", ex);
     }
   }
 
   /**
    * Delete given status mapping bean
    *
    * @param mappingBean bean to delete
    * @throws ApplicationException when failed
    */
   public void deleteIssueStatusMapping(JiraIssueStatusMappingBean mappingBean) throws ApplicationException {
     try {
       // sanity check
       if (null==mappingBean || null==mappingBean.getId()) return;
 
       Session session = sessionFactory.getCurrentSession();
       session.delete(mappingBean);
 
     } catch (Exception ex) {
       logger.error( "[JIRA300020] Unable to delete issue status mapping bean {"+mappingBean+"}", ex);
       throw new ApplicationException("JIRA300020", "Unable to delete issue status mapping bean.", ex);
     }
   }
 
   /**
    * Return issue priority mappings
    *
    * @param server server to get mappings for
    * @return priority mappings
    * @throws ApplicationException when failed
    */
   public Result<JiraIssuePriorityMappingBean> getIssuePriorityMappings(JiraServerBean server) throws ApplicationException{
     Result<JiraIssuePriorityMappingBean> reslt = null;
     try {
       // sanity check
       if (null==server) return Result.EMPTY_RESULT;
 
       Session session = sessionFactory.getCurrentSession();
       List<JiraIssuePriorityMappingBean> mappings = session.createCriteria(JiraIssuePriorityMappingBean.class)
           .add(Restrictions.eq("server", server))
           .list();
       // create new result
       reslt = new Result<JiraIssuePriorityMappingBean>(null, 0, mappings.size(), mappings.size());
       // copy beans
       for (JiraIssuePriorityMappingBean bean: mappings) {
         reslt.getEntities().add(bean);
       }
       return reslt;
     } catch (Exception ex) {
       logger.error( "[JIRA300008] Unable to load issue priority mappings for server {"+server+"}.", ex);
       throw new ApplicationException("JIRA300008", "Unable to load issue priority mappings.", ex);
     }
   }
 
   /**
    * Return issue priority mapping
    *
    * @param mappigId mapping id
    * @return priority mapping
    * @throws ApplicationException when failed
    */
   public JiraIssuePriorityMappingBean getIssuePriorityMapping(Long mappigId) throws ApplicationException{
     try {
       // sanity check
       if (null==mappigId) return null;
       Session session = sessionFactory.getCurrentSession();
       return (JiraIssuePriorityMappingBean) session.get(JiraIssuePriorityMappingBean.class, mappigId);
     } catch (Exception ex) {
       logger.error( "[JIRA300021] Unable to load issue priority mapping with id {"+mappigId+"}.", ex);
       throw new ApplicationException("JIRA300021", "Unable to load issue priority mapping.", ex);
     }
   }
 
   /**
    * Save given issue priority mapping
    *
    * @param mappingBean mapping to save
    * @return updated mapping
    * @throws ApplicationException when failed
    */
   public JiraIssuePriorityMappingBean saveIssuePriorityMapping(JiraIssuePriorityMappingBean mappingBean) throws ApplicationException {
     try {
       // sanity check
       if (null==mappingBean) return null;
 
       Session session = sessionFactory.getCurrentSession();
       if (null!=mappingBean.getId()) {
         mappingBean = (JiraIssuePriorityMappingBean) session.merge(mappingBean);
       }
       session.saveOrUpdate(mappingBean);
       return mappingBean;
     } catch (Exception ex) {
       logger.error( "[JIRA300022] Unable to save issue priority mapping {"+mappingBean+"}", ex);
       throw new ApplicationException("JIRA300022", "Unable to save issue priority mapping.", ex);
     }
   }
 
   /**
    * Delete issue priority mapping
    *
    * @param mappingBean mapping to delete
    * @throws ApplicationException when failed
    */
   public void deleteIssuePriorityMapping(JiraIssuePriorityMappingBean mappingBean) throws ApplicationException {
     try {
       // sanity check
       if (null==mappingBean || null==mappingBean.getId()) return;
       Session session = sessionFactory.getCurrentSession();
       session.delete(mappingBean);
     } catch (Exception ex) {
       logger.error( "[JIRA300023] Unable to delete issue priority mapping {"+mappingBean+"}", ex);
       throw new ApplicationException("JIRA300023", "Unable to delete issue priority mapping.", ex);
     }
   }
 
   /**
    * Return issue priorities from a remote JIRA server
    *
    * @param serverBean server to get priorities from
    * @return priorities list
    * @throws ApplicationException when failed
    */
   public ResultDTO<JiraIssuePriorityDTO> getRemotePriorities(JiraServerBean serverBean) throws ApplicationException {
     ResultDTO<JiraIssuePriorityDTO> reslt = null;
     // sanity check
     if (null==serverBean) return ResultDTO.EMPTY_RESULT;
     try {
       // get JIRA SOAP service
       JiraSoapService service = getService(serverBean);
       // login
       String authToken = login(serverBean, service);
       // get remote priorities
       RemotePriority[] remotePriorities = service.getPriorities(authToken);
       if (null==remotePriorities) return ResultDTO.EMPTY_RESULT;
       // create new result
       reslt = new ResultDTO<JiraIssuePriorityDTO>(null, 0, remotePriorities.length, remotePriorities.length);
       // fill it up
       for (RemotePriority rpr: remotePriorities) {
         reslt.getEntities().add(new JiraIssuePriorityDTO(rpr.getId(), rpr.getName()));
       }
       // return result
       return reslt;
     } catch (ApplicationException appex) {
       throw appex;
     } catch (com.atlassian.jira.rpc.exception.RemoteException e) {
       logger.error("[JIRA300024] Error while retrieving issue types from the server {"+serverBean+"}", e);
       throw new ApplicationException("JIRA300024", "["+e.getFaultCode()+"] "+e.getFaultString(), e);
     } catch (Exception e) {
       logger.error( "[JIRA300024] Error while retrieving issue types from the server {"+serverBean+"}", e);
       throw new ApplicationException("JIRA300024", "Error while retrieving issue types from the server {"+serverBean+"}", e);
     }
   }
 
   /**
    * Return issue statuses from a remote JIRA server
    *
    * @param serverBean server to get statuses from
    * @return issue statuses
    * @throws ApplicationException when failed
    */
   public ResultDTO<JiraIssueStatusDTO> getRemoteStatuses(JiraServerBean serverBean) throws ApplicationException {
     ResultDTO<JiraIssueStatusDTO> reslt = null;
     // sanity check
     if (null==serverBean) return ResultDTO.EMPTY_RESULT;
     try {
       // get JIRA SOAP service
       JiraSoapService service = getService(serverBean);
       // login
       String authToken = login(serverBean, service);
       // get remote priorities
       RemoteStatus[] remoteStatuses = service.getStatuses(authToken);
       if (null==remoteStatuses) return ResultDTO.EMPTY_RESULT;
       // create new result
       reslt = new ResultDTO<JiraIssueStatusDTO>(null, 0, remoteStatuses.length, remoteStatuses.length);
       // fill it up
       for (RemoteStatus rst: remoteStatuses) {
         reslt.getEntities().add(new JiraIssueStatusDTO(rst.getId(), rst.getName()));
       }
       // return result
       return reslt;
     } catch (ApplicationException appex) {
       throw appex;
     } catch (com.atlassian.jira.rpc.exception.RemoteException e) {
       logger.error("[JIRA300025] Error while retrieving issue statuses from the server {"+serverBean+"}", e);
       throw new ApplicationException("JIRA300025", "["+e.getFaultCode()+"] "+e.getFaultString(), e);
     } catch (Exception e) {
       logger.error( "[JIRA300025] Error while retrieving issue statuses from the server {"+serverBean+"}", e);
       throw new ApplicationException("JIRA300025", "Error while retrieving issue statuses from the server {"+serverBean+"}", e);
     }
   }
 
   /**
    * Return issue types from a remote JIRA server
    *                                   sc
    * @param serverBean server to get types from
    * @return issue types
    * @throws ApplicationException when failed
    */
   public ResultDTO<JiraIssueTypeDTO> getRemoteTypes(JiraServerBean serverBean) throws ApplicationException {
     ResultDTO<JiraIssueTypeDTO> reslt = null;
     // sanity check
     if (null==serverBean) return ResultDTO.EMPTY_RESULT;
     try {
       // get JIRA SOAP service
       JiraSoapService service = getService(serverBean);
       // login
       String authToken = login(serverBean, service);
       // get remote priorities
       RemoteIssueType[] remoteTypes = service.getIssueTypes(authToken);
       if (null==remoteTypes) return ResultDTO.EMPTY_RESULT;
       // create new result
       reslt = new ResultDTO<JiraIssueTypeDTO>(null, 0, remoteTypes.length, remoteTypes.length);
       // fill it up
       for (RemoteIssueType rst: remoteTypes) {
         reslt.getEntities().add(new JiraIssueTypeDTO(rst.getId(), rst.getName()));
       }
       // return result
       return reslt;
     } catch (ApplicationException appex) {
       throw appex;
     } catch (com.atlassian.jira.rpc.exception.RemoteException e) {
       logger.error("[JIRA300026] Error while retrieving issue types from the server {"+serverBean+"}", e);
       throw new ApplicationException("JIRA300026", "["+e.getFaultCode()+"] "+e.getFaultString(), e);
     } catch (Exception e) {
       logger.error("[JIRA300026] Error while retrieving issue types from the server {"+serverBean+"}", e);
       throw new ApplicationException("JIRA300026", "Error while retrieving issue types from the server {"+serverBean+"}", e);
     }
   }
 
   /**
    * Return filers from a remote server
    *
    * @param serverBean server to get filters from
    * @return filters
    * @throws ApplicationException when failed
    */
   public ResultDTO<JiraFilterDTO> getRemoteFilters(JiraServerBean serverBean) throws ApplicationException {
     ResultDTO<JiraFilterDTO> reslt = null;
     // sanity check
     if (null==serverBean) return ResultDTO.EMPTY_RESULT;
     try {
       // get JIRA SOAP service
       JiraSoapService service = getService(serverBean);
       // login
       String authToken = login(serverBean, service);
       // get remote priorities
       RemoteFilter[] remoteFilters = service.getFavouriteFilters(authToken);
       if (null==remoteFilters) return ResultDTO.EMPTY_RESULT;
       // create new result
       reslt = new ResultDTO<JiraFilterDTO>(null, 0, remoteFilters.length, remoteFilters.length);
       // fill it up
       for (RemoteFilter rst: remoteFilters) {
         reslt.getEntities().add(new JiraFilterDTO(rst.getId(), rst.getName()));
       }
       // return result
       return reslt;
     } catch (ApplicationException appex) {
       throw appex;
     } catch (com.atlassian.jira.rpc.exception.RemoteException e) {
       logger.error("[JIRA300027] Error while retrieving filters from the server {"+serverBean+"}", e);
       throw new ApplicationException("JIRA300027", "["+e.getFaultCode()+"] "+e.getFaultString(), e);
     } catch (Exception e) {
       logger.error( "[JIRA300027] Error while retrieving filters from the server {"+serverBean+"}", e);
       throw new ApplicationException("JIRA300027", "Error while retrieving filters from the server {"+serverBean+"}", e);
     }
   }
 
   /**
    * Return remote server info
    *
    * @param serverBean server bean
    * @return server info
    * @throws ApplicationException when failed
    */
   public JiraServerInfoDTO getRemoteServerInfo(JiraServerBean serverBean) throws ApplicationException {
     JiraServerInfoDTO serverInfoDTO = new JiraServerInfoDTO();
     try {
       // get JIRA SOAP service
       JiraSoapService service = getService(serverBean);
       // login
       String authToken = login(serverBean, service);
       // get server info
       RemoteServerInfo serverInfo = service.getServerInfo(authToken);
       // prepare DTO
       serverInfoDTO.setBaseUrl(serverInfo.getBaseUrl());
       serverInfoDTO.setBuildDate(serverInfo.getBuildDate().getTime());
       serverInfoDTO.setBuildNumber(serverInfo.getBuildNumber());
       serverInfoDTO.setServerTime(serverInfo.getServerTime().getServerTime());
       serverInfoDTO.setServerTimeZone(serverInfo.getServerTime().getTimeZoneId());
       serverInfoDTO.setVersion(serverInfo.getVersion());
       // return server info
       return serverInfoDTO;
     } catch (ApplicationException appex) {
       throw appex;
     } catch (com.atlassian.jira.rpc.exception.RemoteException e) {
       logger.error("[JIRA300028] Error while retrieving server info {"+serverBean+"}", e);
       throw new ApplicationException("JIRA300028", "["+e.getFaultCode()+"] "+e.getFaultString(), e);
     } catch (Exception e) {
       logger.error("[JIRA300028] Error while retrieving server info {"+serverBean+"}", e);
       throw new ApplicationException("JIRA300028", "Error while retrieving server info. Cause:"+e.getMessage(), e);
     }
   }
 
 }
