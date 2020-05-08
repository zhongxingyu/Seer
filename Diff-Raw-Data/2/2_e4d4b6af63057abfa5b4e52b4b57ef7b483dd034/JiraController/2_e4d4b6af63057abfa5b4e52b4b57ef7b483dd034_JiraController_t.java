 package org.trecena.modules.jira;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.stereotype.Repository;
 import org.springframework.web.bind.annotation.*;
 import org.trecena.ApplicationException;
 import org.trecena.ApplicationHttpException;
 import org.trecena.Constants;
 import org.trecena.ModuleManager;
 import org.trecena.beans.*;
 import org.trecena.controller.UserRequestContext;
 import org.trecena.dto.ResultDTO;
 import org.trecena.dto.ResultMessageDTO;
 import org.trecena.modules.jira.dto.*;
 
 import javax.annotation.security.RolesAllowed;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.List;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
 
 /**
  * JiraModule controller.
  *
  * @author Ivan Latysh <ivanlatysh@gmail.com>
  * @version 0.1
  * @since 22/11/11 8:15 PM
  */
 @Controller
 @RequestMapping(value = "modules/jira")
 @Repository
 public class JiraController {
   // Error prefix JIRA00031
   /** 
    * Errors:
    * JIRA00404 - Item not found
    * JIRA00101 - Unable to parse a number.
    * JIRA00102 - Missing required parameter.
    * JIRA00103 - Unable to get logged-in user.
    * JIRA00104 - User with login name is not found.
    * JIRA00110 - Not valid server ID
    * JIRA00151 - Unable to load JIRA server
    * JIRA00160 - Unable to connect to JIRA server
    */
   // 
 
   protected Logger logger = LoggerFactory.getLogger(this.getClass().getName());
 
   /** URL this jiraModule is mapped to */
   protected static final String MAPPED_URL = "modules/jira";
 
   @Autowired
   protected JiraModule jiraModule;
 
   @Autowired
   protected ModuleManager moduleManager;
 
   /* User request context */
   @Autowired
   protected UserRequestContext userRequestContext;
 
   /**
    * Exception handler, send response error code and the message from given ApplicationException
    *
    * @param apphex application exception
    * @param request request
    * @param response response
    * @return <tt>null</tt>
    */
   @ExceptionHandler(ApplicationHttpException.class)
   public String handleException(ApplicationHttpException apphex, HttpServletRequest request, HttpServletResponse response) {
     try {
       // check accept header
       if (null!=request.getHeader("Accept") && request.getHeader("Accept").contains("application/json")) {
         // set content type
         response.setContentType("application/json");
         // set response code
         response.setStatus(apphex.getResponse_code());
         // set error headers
         response.addHeader("Application-Error-Code", apphex.getCode());
         response.addHeader("Application-Error-Message", apphex.getMessage());
         // write response body
         response.getWriter().print("{");
         response.getWriter().print("\"response-code\":\"");
         response.getWriter().print(apphex.getResponse_code());
         response.getWriter().print("\",");
         response.getWriter().print("\"error-code\":\"");
         response.getWriter().print(apphex.getCode());
         response.getWriter().print("\",");
         response.getWriter().print("\"error-message\":\"");
         response.getWriter().print(apphex.getMessage().replaceAll("\"", "'"));
         response.getWriter().print("\"");
         response.getWriter().print("}");
       } else {
         // set content type
         response.setContentType("application/xml");
         // set response code
         response.setStatus(apphex.getResponse_code());
         // set error headers
         response.addHeader("Application-Error-Code", apphex.getCode());
         response.addHeader("Application-Error-Message", apphex.getMessage());
         // write response body
         response.getWriter().print("<exception>");
         response.getWriter().print("<response-code>");
         response.getWriter().print(apphex.getResponse_code());
         response.getWriter().print("</response-code>");
         response.getWriter().print("<error-code>");
         response.getWriter().print(apphex.getCode());
         response.getWriter().print("</error-code>");
         response.getWriter().print("<error-message>");
         response.getWriter().print(apphex.getMessage());
         response.getWriter().print("</error-message>");
         response.getWriter().print("</exception>");
       }
       // flush response
       response.flushBuffer();
     } catch (IOException e) {
       logger.error( "[JIRA00001] Unable to handle application exception {"+ apphex +"}.", e);
     }
     return null;
   }
 
   /**
    * Return configured jira servers
    *
    * @return list of configured jira servers
    * @throws ApplicationHttpException when failed
    */
   @RequestMapping(value = "/servers", method = RequestMethod.GET, produces = {"application/xml", "application/json"})
   @ResponseBody
   public ResultDTO<JiraServerDTO> getServers() throws ApplicationHttpException {
     ResultDTO<JiraServerDTO> reslt = null;
     try {
       List<JiraServerBean> _servers = jiraModule.getServers();
       if (null==_servers) {
         logger.error( "Returned null server list.");
         return ResultDTO.EMPTY_RESULT;
       }
       reslt = new ResultDTO<JiraServerDTO>(null, 0, _servers.size(), _servers.size());
       for (JiraServerBean bean: _servers) {
         JiraServerDTO serverDTO = null;
         // return full server info for admins (see issue trecena-jira #3)
         if (userRequestContext.isUserInRole("ADMIN")) {
           serverDTO = new JiraServerDTO(bean);
         } else {
           serverDTO = getSlimServerDTO(bean);
         }
         reslt.getEntities().add(serverDTO);
       }
       // return result
       return reslt;
     } catch (ApplicationException appex) {
       throw new ApplicationHttpException("JIRA00002", appex.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     } catch (Exception ex) {
       logger.error( "Unable to load jira servers.", ex);
       throw new ApplicationHttpException("JIRA00002", "Unable to load jira servers.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
   }
 
   /**
    * Get a jira server with given ID
    *
    * @param serverId id of the server to return
    * @return server
    * @throws ApplicationHttpException when failed
    */
   @RequestMapping(value = "/servers/{serverId}", method = RequestMethod.GET, produces = {"application/xml", "application/json"})
   @ResponseBody
   @RolesAllowed("ROLE_ADMIN")
   public JiraServerDTO getServer(@PathVariable("serverId") String serverId) throws ApplicationHttpException {
     try {
       // get server
       JiraServerBean jiraServerBean = getServerBean(serverId);
       // return DTO
       return new JiraServerDTO(jiraServerBean);
     } catch (ApplicationHttpException apphex) {
       throw apphex;
     } catch (ApplicationException appex) {
       throw new ApplicationHttpException("JIRA00003", appex.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     } catch (Exception ex) {
       logger.error( "[JIRA00003] Unable to return jira server with id {"+ serverId +"}.", ex);
       throw new ApplicationHttpException("JIRA00003", "Unable to return jira server.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
   }
 
   /**
    * Save or update a server
    *
    * @param jiraServerDTO server to save
    * @return saved server
    * @throws ApplicationHttpException when failed
    */
   @RequestMapping(value = "/servers", method = RequestMethod.PUT, produces = {"application/xml", "application/json"})
   @ResponseBody
   @RolesAllowed("ROLE_ADMIN")
   public JiraServerDTO saveServer(@RequestBody JiraServerDTO jiraServerDTO) throws ApplicationHttpException {
     try {
       // sanity check
       if (null==jiraServerDTO) {
         throw new ApplicationHttpException("JIRA00102", "Request body must contain a valid mapping DTO.", HttpServletResponse.SC_BAD_REQUEST);
       }
       return new JiraServerDTO(jiraModule.saveServer(jiraServerDTO.toBean(null)));
     } catch (ApplicationHttpException apphex) {
       throw apphex;
     } catch (ApplicationException appex) {
       throw new ApplicationHttpException("JIRA00004", appex.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     } catch (Exception ex) {
       logger.error( "[JIRA00004] Unable to save jira server {"+ jiraServerDTO +"}.", ex);
       throw new ApplicationHttpException("JIRA00004", "Unable to save jira server.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
   }
 
   /**
    * Delete a server
    *
    * @param serverId server id to delete
    * @return success message
    * @throws ApplicationHttpException when failed
    */
   @RequestMapping(value = "/servers/{serverId}", method = RequestMethod.DELETE, produces = {"application/xml", "application/json"})
   @ResponseBody
   @RolesAllowed("ROLE_ADMIN")
   public ResultMessageDTO deleteServer(@PathVariable("serverId") String serverId) throws ApplicationHttpException {
     try {
       // get server
       JiraServerBean jiraServerBean = getServerBean(serverId);
       // delete server
       jiraModule.deleteServer(jiraServerBean);
       // return result message
       return ResultMessageDTO.SUCCESS;
     } catch (ApplicationHttpException apphex) {
       throw apphex;
     } catch (ApplicationException appex) {
       throw new ApplicationHttpException("JIRA00005", appex.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     } catch (Exception ex) {
       logger.error( "[JIRA00005] Unable to delete jira server with id {"+ serverId +"}.", ex);
       throw new ApplicationHttpException("JIRA00005", "Unable to delete jira server", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
   }
 
   /**
    * Return user mapping
    *
    * @param serverId server id to return user mappings for
    * @return list of user mappings
    * @throws ApplicationHttpException when failed
    */
   @RequestMapping(value = "/servers/{serverId}/mappings/users", method = RequestMethod.GET, produces = {"application/xml", "application/json"})
   @ResponseBody
   public ResultDTO<JiraUserAccountMappingDTO> getUserMappings(@PathVariable("serverId") String serverId,
                                                               @RequestParam (value = "index", required = false) String index,
                                                               @RequestParam (value = "max", required = false) String max,
                                                               @RequestParam (value = "mine", required = false) Boolean mine) throws ApplicationHttpException {
     try {
 
       // parse pagination parameters
       Long _firstResult = parseLongValue(index);
       Long _maxResults = parseLongValue(max);
 
       // get server
       JiraServerBean _server = getServerBean(serverId);
       UserBean userBean = null;
      if (null!=mine && (mine || !userRequestContext.isUserInRole("ADMIN"))) {
         userBean = getLoggedUserBean(true);
       }
         // get mappings
       Result<JiraUserAccountMappingBean> mappings = jiraModule.getUserMappings(
           _server,
           userBean,
           null!=_firstResult ?_firstResult.intValue() :0,
           null!=_maxResults ?_maxResults.intValue() : Constants.RESULTS_MAX);
       // prepare result DTO
       ResultDTO<JiraUserAccountMappingDTO> reslt = new ResultDTO<JiraUserAccountMappingDTO>(mappings);
       // transfer beans
       for (JiraUserAccountMappingBean mappingBean : mappings.getEntities()) {
         reslt.getEntities().add(new JiraUserAccountMappingDTO(mappingBean, JiraUserAccountMappingDTO.USER));
       }
       // return result DTO
       return reslt;
     } catch (ApplicationHttpException apphex) {
       throw apphex;
     } catch (ApplicationException appex) {
       throw new ApplicationHttpException("JIRA00006", appex.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     } catch (Exception ex) {
       logger.error( "[JIRA00006] Unable to return user mappings for server {"+serverId+"}.", ex);
       throw new ApplicationHttpException("JIRA00006", "Unable to return user mappings.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
   }
 
   /**
    * Return user mapping
    *
    * @param serverId server id to return user mappings for
    * @return list of user mappings
    * @throws ApplicationHttpException when failed
    */
   @RequestMapping(value = "/servers/{serverId}/mappings/users/{mappingid}", method = RequestMethod.GET, produces = {"application/xml", "application/json"})
   @ResponseBody
   public JiraUserAccountMappingDTO getUserMapping(@PathVariable("serverId") String serverId,
                                                   @PathVariable("mappingid") String mappingid) throws ApplicationHttpException {
     try {
       // get mapping
       JiraUserAccountMappingBean mappingBean = jiraModule.getUserMapping(parseLongValue(mappingid));
       if (null==mappingBean) {
         throw new ApplicationHttpException("JIRA00404", "No mapping with id {"+mappingid+"} has been found.", HttpServletResponse.SC_NOT_FOUND);
       }
       // get server
       JiraServerBean serverBean = getServerBean(serverId);
       // make sure that mapping belong to the server
       if (!serverBean.equals(mappingBean.getServer())) {
         throw new ApplicationHttpException("JIRA00404", "No mapping with id {"+mappingid+"} has been found.", HttpServletResponse.SC_NOT_FOUND);
       }
       if (!userRequestContext.isUserInRole("ADMIN")) {
         // make sure that mapping belong to the user
         if (!mappingBean.getUser().getId().equals(getLoggedUserBean(true))) {
           throw new ApplicationHttpException("JIRA00404", "No mapping with id {"+mappingid+"} has been found.", HttpServletResponse.SC_NOT_FOUND);
         }
       }
       // return dto
       return new JiraUserAccountMappingDTO(mappingBean, JiraUserAccountMappingDTO.USER);
     } catch (ApplicationHttpException apphex) {
       throw apphex;
     } catch (ApplicationException appex) {
       throw new ApplicationHttpException("JIRA00007", "Unable to return user mapping with id {"+mappingid+"}.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     } catch (Exception ex) {
       logger.error( "[JIRA00007] Unable to return user mapping with id {"+mappingid+"}.", ex);
       throw new ApplicationHttpException("JIRA00007", "Unable to return user mapping.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
   }
 
   /**
    * Save or update user account mapping
    *
    * @param serverId server id
    * @param mappingDTO user mapping
    * @return saved mappingBean
    * @throws ApplicationHttpException when failed
    */
   @RequestMapping(value = "/servers/{serverId}/mappings/users", method = RequestMethod.PUT, produces = {"application/xml", "application/json"})
   @ResponseBody
   public JiraUserAccountMappingDTO saveUserMapping(@PathVariable("serverId") String serverId,
                                                    @RequestBody JiraUserAccountMappingDTO mappingDTO) throws ApplicationHttpException {
     try {
       if (null== mappingDTO) {
         throw new ApplicationHttpException("JIRA00102", "Request body must contain a valid mapping DTO.", HttpServletResponse.SC_BAD_REQUEST);
       }
 
       // create a bean
       JiraUserAccountMappingBean mappingBean = mappingDTO.toBean(null, JiraUserAccountMappingDTO.USER);
       // set server
       mappingBean.setServer(getServerBean(serverId));
       // make sure that regular user does not override someone else mapping
       if (!userRequestContext.isUserInRole("ADMIN")) {
         // get current user
         UserBean userBean = getLoggedUserBean(true);
         if (null!=mappingBean.getId()) {
           JiraUserAccountMappingBean _mappingBean = jiraModule.getUserMapping(mappingBean.getId());
           if (null!=_mappingBean && !_mappingBean.getUser().getId().equals(userBean.getId())) {
             // reset ID, so new mapping will be created
             mappingBean.setId(null);
           }
         }
         // reset user
         mappingBean.setUser(userBean);
       }
       // save mapping bean
       mappingBean = jiraModule.saveUserAccountMapping(mappingBean);
       // and return dto
       return new JiraUserAccountMappingDTO(mappingBean, JiraUserAccountMappingDTO.USER);
     } catch (ApplicationHttpException apphex) {
       throw apphex;
     } catch (ApplicationException appex) {
       throw new ApplicationHttpException("JIRA00008", appex.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     } catch (Exception ex) {
       logger.error( "[JIRA00008] Unable to save user mapping {"+ mappingDTO +"}.", ex);
       throw new ApplicationHttpException("JIRA00008", "Unable to save user mapping.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
   }
 
   /**
    * Delete user account mapping bean
    *
    * @param serverId server id
    * @param mappingid mapping id
    * @return result message
    * @throws ApplicationHttpException when failed
    */
   @RequestMapping(value = "/servers/{serverId}/mappings/users/{mappingid}", method = RequestMethod.DELETE, produces = {"application/xml", "application/json"})
   @ResponseBody
   public ResultMessageDTO deleteUserMapping(@PathVariable("serverId") String serverId,
                                             @PathVariable("mappingid") String mappingid) throws ApplicationHttpException {
     try {
       // get mapping
       JiraUserAccountMappingBean mappingBean = jiraModule.getUserMapping(parseLongValue(mappingid));
       if (null==mappingBean) {
         throw new ApplicationHttpException("JIRA00404", "No mapping with id {"+mappingid+"} has been found.", HttpServletResponse.SC_NOT_FOUND);
       }
       // get server
       JiraServerBean serverBean = getServerBean(serverId);
       // make sure that mapping belong to the server
       if (!serverBean.equals(mappingBean.getServer())) {
         throw new ApplicationHttpException("JIRA00404", "No mapping with id {"+mappingid+"} has been found.", HttpServletResponse.SC_NOT_FOUND);
       }
       // make sure that regular user deleted only his mappings
       if (!userRequestContext.isUserInRole("ADMIN")) {
         UserBean userBean = getLoggedUserBean(true);
         if (!mappingBean.getUser().getId().equals(userBean.getId())) {
           throw new ApplicationHttpException("JIRA00404", "No mapping with id {"+mappingid+"} has been found.", HttpServletResponse.SC_NOT_FOUND);
         }
       }
       // delete mapping
       jiraModule.deleteUserAccountMapping(mappingBean);
       // return result message
       return ResultMessageDTO.SUCCESS;
 
     } catch (ApplicationHttpException apphex) {
       throw apphex;
     } catch (ApplicationException appex) {
       throw new ApplicationHttpException("JIRA00009", appex.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     } catch (Exception ex) {
       logger.error( "[JIRA00009] Unable to delete user mappings with id {"+mappingid+"}.", ex);
       throw new ApplicationHttpException("JIRA00009", "Unable to delete user mapping.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
   }
 
   /**
    * Return type mapping
    *
    * @param serverId server id to return mappings for
    * @return list of type mappings
    * @throws ApplicationHttpException when failed
    */
   @RequestMapping(value = "/servers/{serverId}/mappings/types", method = RequestMethod.GET, produces = {"application/xml", "application/json"})
   @ResponseBody
   @RolesAllowed("ROLE_ADMIN")
   public ResultDTO<JiraIssueTypeMappingDTO> getIssueTypeMappings(@PathVariable("serverId") String serverId) throws ApplicationHttpException {
     try {
       // get server
       JiraServerBean _server = getServerBean(serverId);
       // get user mappings
       Result<JiraIssueTypeMappingBean> mappings = jiraModule.getIssueTypeMappings(_server);
       // prepare result DTO
       ResultDTO<JiraIssueTypeMappingDTO> reslt = new ResultDTO<JiraIssueTypeMappingDTO>(mappings);
       // transfer beans
       for (JiraIssueTypeMappingBean bean: mappings.getEntities()) {
         reslt.getEntities().add(new JiraIssueTypeMappingDTO(bean, JiraIssueTypeMappingDTO.TYPE));
       }
       // return result DTO
       return reslt;
     } catch (ApplicationHttpException apphex) {
       throw apphex;
     } catch (ApplicationException appex) {
       throw new ApplicationHttpException("JIRA00010", "Unable to return issue type mappings for server {"+serverId+"}.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
   }
 
   /**
    * Return issue type mapping
    *
    * @param serverId server id to return mappings for
    * @return list of type mappings
    * @throws ApplicationHttpException when failed
    */
   @RequestMapping(value = "/servers/{serverId}/mappings/types/{mappingId}", method = RequestMethod.GET, produces = {"application/xml", "application/json"})
   @ResponseBody
   @RolesAllowed("ROLE_ADMIN")
   public JiraIssueTypeMappingDTO getIssueTypeMapping(@PathVariable("serverId") String serverId,
                                                      @PathVariable("mappingId") String mappingId) throws ApplicationHttpException {
     try {
       // get server
       JiraServerBean serverBean = getServerBean(serverId);
       // get user mappings
       JiraIssueTypeMappingBean typeBean = jiraModule.getIssueTypeMapping(parseLongValue(mappingId));
       // make sure that mapping belong to the server
       if (!serverBean.equals(typeBean.getServer())) {
         throw new ApplicationHttpException("JIRA00404", "No mapping with id {"+ mappingId +"} has been found.", HttpServletResponse.SC_NOT_FOUND);
       }
       // return new DTO
       return new JiraIssueTypeMappingDTO(typeBean, JiraIssueTypeMappingDTO.TYPE);
     } catch (ApplicationHttpException apphex) {
       throw apphex;
     } catch (ApplicationException appex) {
       throw new ApplicationHttpException("JIRA00020", "Unable to return issue type mapping with id {"+mappingId+"}.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     } catch (Exception ex) {
       logger.error( "[JIRA00020] Unable to return issue type mapping with id {"+ mappingId +"}", ex);
       throw new ApplicationHttpException("JIRA00020", "Unable to return type mapping.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
   }
 
   /**
    * Save given issue type mapping
    *
    * @param serverId server id
    * @param mappingDTO issue type mapping
    * @return saved issue type mapping
    * @throws ApplicationHttpException when failed
    */
   @RequestMapping(value = "/servers/{serverId}/mappings/types", method = RequestMethod.PUT, produces = {"application/xml", "application/json"})
   @ResponseBody
   @RolesAllowed("ROLE_ADMIN")
   public JiraIssueTypeMappingDTO saveIssueTypeMapping(@PathVariable("serverId") String serverId,
                                                       @RequestBody JiraIssueTypeMappingDTO mappingDTO) throws ApplicationHttpException {
     try {
       if (null== mappingDTO) {
         throw new ApplicationHttpException("JIRA00102", "Request body must contain a valid mapping DTO.", HttpServletResponse.SC_BAD_REQUEST);
       }
       // create a bean
       JiraIssueTypeMappingBean mappingBean = mappingDTO.toBean(null, JiraIssueTypeMappingDTO.TYPE);
       // set server
       mappingBean.setServer(getServerBean(serverId));
       // save mapping bean
       mappingBean = jiraModule.saveIssueTypeMapping(mappingBean);
       // and return dto
       return new JiraIssueTypeMappingDTO(mappingBean, JiraIssueTypeMappingDTO.TYPE);
     } catch (ApplicationHttpException apphex) {
       throw apphex;
     } catch (ApplicationException appex) {
       throw new ApplicationHttpException("", appex.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     } catch (Exception ex) {
       logger.error( "[JIRA00011] Unable to save issue type mapping {"+ mappingDTO +"}.", ex);
       throw new ApplicationHttpException("JIRA00011", "Unable to save issue type mapping.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
   }
 
   /**
    * Delete issue type mapping
    *
    * @param serverId server id
    * @param mappingid mapping id
    * @return result message
    * @throws ApplicationHttpException when failed
    */
   @RequestMapping(value = "/servers/{serverId}/mappings/types/{mappingid}", method = RequestMethod.DELETE, produces = {"application/xml", "application/json"})
   @ResponseBody
   @RolesAllowed("ROLE_ADMIN")
   public ResultMessageDTO deleteIssueTypeMapping(@PathVariable("serverId") String serverId,
                                                  @PathVariable("mappingid") String mappingid) throws ApplicationHttpException {
     try {
       // get mapping
       JiraIssueTypeMappingBean mappingBean = jiraModule.getIssueTypeMapping(parseLongValue(mappingid));
       if (null==mappingBean) {
         throw new ApplicationHttpException("JIRA00404", "No mapping with id {"+mappingid+"} has been found.", HttpServletResponse.SC_NOT_FOUND);
       }
       // get server
       JiraServerBean serverBean = getServerBean(serverId);
       // make sure that mapping belong to the server
       if (!serverBean.equals(mappingBean.getServer())) {
         throw new ApplicationHttpException("JIRA00404", "No mapping with id {"+mappingid+"} has been found.", HttpServletResponse.SC_NOT_FOUND);
       }
       // delete mapping
       jiraModule.deleteIssueTypeMapping(mappingBean);
       // return result message
       return ResultMessageDTO.SUCCESS;
 
     } catch (ApplicationHttpException apphex) {
       throw apphex;
     } catch (ApplicationException appex) {
       throw new ApplicationHttpException("JIRA00012", appex.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     } catch (Exception ex) {
       logger.error( "[JIRA00012] Unable to delete issue type mappings with id {"+mappingid+"}.", ex);
       throw new ApplicationHttpException("JIRA00012", "Unable to delete issue type mapping.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
   }
 
   /**
    * Return status mappings
    *
    * @param serverId server id to return mappings for
    * @return list of status mappings
    * @throws ApplicationHttpException when failed
    */
   @RequestMapping(value = "/servers/{serverId}/mappings/statuses", method = RequestMethod.GET, produces = {"application/xml", "application/json"})
   @ResponseBody
   @RolesAllowed("ROLE_ADMIN")
   public ResultDTO<JiraIssueStatusMappingDTO> getIssueStatusMappings(@PathVariable("serverId") String serverId) throws ApplicationHttpException {
     try {
       // get server
       JiraServerBean _server = getServerBean(serverId);
       // get user mappings
       Result<JiraIssueStatusMappingBean> mappings = jiraModule.getIssueStatusMappings(_server);
       // prepare result DTO
       ResultDTO<JiraIssueStatusMappingDTO> reslt = new ResultDTO<JiraIssueStatusMappingDTO>(mappings);
       // transfer beans
       for (JiraIssueStatusMappingBean bean: mappings.getEntities()) {
         reslt.getEntities().add(new JiraIssueStatusMappingDTO(bean, JiraIssueStatusMappingDTO.STATUS));
       }
       // return result DTO
       return reslt;
     } catch (ApplicationHttpException apphex) {
       throw apphex;
     } catch (ApplicationException appex) {
       throw new ApplicationHttpException("JIRA00013", "Unable to return issue type mappings for server {"+serverId+"}.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
   }
 
   /**
    * Return issue status mapping
    *
    * @param serverId server id
    * @param mappingId mapping id
    * @return priority mapping
    * @throws ApplicationHttpException when failed
    */
   @RequestMapping(value = "/servers/{serverId}/mappings/statuses/{mappingId}", method = RequestMethod.GET, produces = {"application/xml", "application/json"})
   @ResponseBody
   @RolesAllowed("ROLE_ADMIN")
   public JiraIssueStatusMappingDTO getIssueStatusMapping(@PathVariable("serverId") String serverId,
                                                          @PathVariable("mappingId") String mappingId) throws ApplicationHttpException {
     try {
       // get server
       JiraServerBean serverBean = getServerBean(serverId);
       // get issue priority mapping
       JiraIssueStatusMappingBean mappingBean = jiraModule.getIssueStatusMapping(parseLongValue(mappingId));
       // make sure that mapping belong to the server
       if (!serverBean.equals(mappingBean.getServer())) {
         throw new ApplicationHttpException("JIRA00404", "No mapping with id {"+ mappingId +"} has been found.", HttpServletResponse.SC_NOT_FOUND);
       }
       // return DTO
       return new JiraIssueStatusMappingDTO(mappingBean, JiraIssueStatusMappingDTO.STATUS);
     } catch (ApplicationHttpException apphex) {
       throw apphex;
     } catch (ApplicationException appex) {
       throw new ApplicationHttpException("JIRA00021", "Unable to return issue status mapping with id {"+ mappingId +"}.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     } catch (Exception ex) {
       logger.error( "[JIRA00021] Unable to return issue status mapping with id {"+ mappingId +"}", ex);
       throw new ApplicationHttpException("JIRA00021", "Unable to return issue status mapping.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
   }
 
   /**
    * Save given issue status mapping
    *
    * @param serverId server id
    * @param mappingDTO issue status mapping
    * @return saved issue status mapping
    * @throws ApplicationHttpException when failed
    */
   @RequestMapping(value = "/servers/{serverId}/mappings/statuses", method = RequestMethod.PUT, produces = {"application/xml", "application/json"})
   @ResponseBody
   @RolesAllowed("ROLE_ADMIN")
   public JiraIssueStatusMappingDTO saveIssueStatusMapping(@PathVariable("serverId") String serverId,
                                                           @RequestBody JiraIssueStatusMappingDTO mappingDTO) throws ApplicationHttpException {
     try {
       if (null== mappingDTO) {
         throw new ApplicationHttpException("JIRA00102", "Request body must contain a valid mapping DTO.", HttpServletResponse.SC_BAD_REQUEST);
       }
       // create a bean
       JiraIssueStatusMappingBean mappingBean = mappingDTO.toBean(null, JiraIssueStatusMappingDTO.STATUS);
       // set server
       mappingBean.setServer(getServerBean(serverId));
       // save mapping bean
       mappingBean = jiraModule.saveIssueStatusMapping(mappingBean);
       // and return dto
       return new JiraIssueStatusMappingDTO(mappingBean, JiraIssueStatusMappingDTO.STATUS);
     } catch (ApplicationHttpException apphex) {
       throw apphex;
     } catch (ApplicationException appex) {
       throw new ApplicationHttpException("", appex.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     } catch (Exception ex) {
       logger.error( "[JIRA00014] Unable to save issue type mapping {"+ mappingDTO +"}.", ex);
       throw new ApplicationHttpException("JIRA00014", "Unable to save issue type mapping.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
   }
 
   /**
    * Delete issue Status mapping
    *
    * @param serverId server id
    * @param mappingId mapping id
    * @return result message
    * @throws ApplicationHttpException when failed
    */
   @RequestMapping(value = "/servers/{serverId}/mappings/statuses/{mappingId}", method = RequestMethod.DELETE, produces = {"application/xml", "application/json"})
   @ResponseBody
   @RolesAllowed("ROLE_ADMIN")
   public ResultMessageDTO deleteIssueStatusMapping(@PathVariable("serverId") String serverId,
                                                    @PathVariable("mappingId") String mappingId) throws ApplicationHttpException {
     try {
       // get mapping
       JiraIssueStatusMappingBean mappingBean = jiraModule.getIssueStatusMapping(parseLongValue(mappingId));
       if (null==mappingBean) {
         throw new ApplicationHttpException("JIRA00404", "No mapping with id {"+ mappingId +"} has been found.", HttpServletResponse.SC_NOT_FOUND);
       }
       // get server
       JiraServerBean serverBean = getServerBean(serverId);
       // make sure that mapping belong to the server
       if (!serverBean.equals(mappingBean.getServer())) {
         throw new ApplicationHttpException("JIRA00404", "No mapping with id {"+ mappingId +"} has been found.", HttpServletResponse.SC_NOT_FOUND);
       }
       // delete mapping
       jiraModule.deleteIssueStatusMapping(mappingBean);
       // return result message
       return ResultMessageDTO.SUCCESS;
 
     } catch (ApplicationHttpException apphex) {
       throw apphex;
     } catch (ApplicationException appex) {
       throw new ApplicationHttpException("JIRA00015", appex.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     } catch (Exception ex) {
       logger.error( "[JIRA00015] Unable to delete issue type mappings with id {"+ mappingId +"}.", ex);
       throw new ApplicationHttpException("JIRA00015", "Unable to delete issue type mapping.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
   }
 
   /**
    * Return issue priorities mapping
    *
    * @param serverId server id to return mappings for
    * @return list of issue prioroties mappings
    * @throws ApplicationHttpException when failed
    */
   @RequestMapping(value = "/servers/{serverId}/mappings/priorities", method = RequestMethod.GET, produces = {"application/xml", "application/json"})
   @ResponseBody
   @RolesAllowed("ROLE_ADMIN")
   public ResultDTO<JiraIssuePriorityMappingDTO> getIssuePriorityMappings(@PathVariable("serverId") String serverId) throws ApplicationHttpException {
     try {
       // get server
       JiraServerBean _server = getServerBean(serverId);
       // get user mappings
       Result<JiraIssuePriorityMappingBean> mappings = jiraModule.getIssuePriorityMappings(_server);
       // prepare result DTO
       ResultDTO<JiraIssuePriorityMappingDTO> reslt = new ResultDTO<JiraIssuePriorityMappingDTO>(mappings);
       // transfer beans
       for (JiraIssuePriorityMappingBean bean: mappings.getEntities()) {
         reslt.getEntities().add(new JiraIssuePriorityMappingDTO(bean, JiraIssuePriorityMappingDTO.PRIORITY));
       }
       // return result DTO
       return reslt;
     } catch (ApplicationHttpException apphex) {
       throw apphex;
     } catch (ApplicationException appex) {
       throw new ApplicationHttpException("JIRA00016", "Unable to return issue priority mappings for server {"+serverId+"}.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     } catch (Exception ex) {
       logger.error( "[JIRA00016] Unable to return issue priority mappings for server {"+serverId+"}", ex);
       throw new ApplicationHttpException("JIRA00016", "Unable to return issue priority mappings.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
   }
 
   /**
    * Return issue priority mapping
    *
    * @param serverId server id
    * @param mappingId mapping id
    * @return priority mapping
    * @throws ApplicationHttpException when failed
    */
   @RequestMapping(value = "/servers/{serverId}/mappings/priorities/{mappingId}", method = RequestMethod.GET, produces = {"application/xml", "application/json"})
   @ResponseBody
   @RolesAllowed("ROLE_ADMIN")
   public JiraIssuePriorityMappingDTO getIssuePriorityMapping(@PathVariable("serverId") String serverId,
                                                              @PathVariable("mappingId") String mappingId) throws ApplicationHttpException {
     try {
       // get server
       JiraServerBean serverBean = getServerBean(serverId);
       // get issue priority mapping
       JiraIssuePriorityMappingBean mappingBean = jiraModule.getIssuePriorityMapping(parseLongValue(mappingId));
       // make sure that mapping belong to the server
       if (!serverBean.equals(mappingBean.getServer())) {
         throw new ApplicationHttpException("JIRA00404", "No mapping with id {"+ mappingId +"} has been found.", HttpServletResponse.SC_NOT_FOUND);
       }
       // return DTO
       return new JiraIssuePriorityMappingDTO(mappingBean, JiraIssuePriorityMappingDTO.PRIORITY);
     } catch (ApplicationHttpException apphex) {
       throw apphex;
     } catch (ApplicationException appex) {
       throw new ApplicationHttpException("JIRA00017", "Unable to return issue priority mapping with id {"+ mappingId +"}.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     } catch (Exception ex) {
       logger.error( "[JIRA00017] Unable to return issue priority mapping with id {"+ mappingId +"}", ex);
       throw new ApplicationHttpException("JIRA00017", "Unable to return issue priority mapping.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
   }
 
   /**
    * Save given issue priority mapping
    *
    * @param serverId server id
    * @param mappingDTO mapping dto
    * @return saved mapping
    * @throws ApplicationHttpException when failed
    */
   @RequestMapping(value = "/servers/{serverId}/mappings/priorities", method = RequestMethod.PUT, produces = {"application/xml", "application/json"})
   @ResponseBody
   @RolesAllowed("ROLE_ADMIN")
   public JiraIssuePriorityMappingDTO saveIssuePriorityMapping(@PathVariable("serverId") String serverId,
                                                               @RequestBody JiraIssuePriorityMappingDTO mappingDTO) throws ApplicationHttpException {
     try {
       if (null== mappingDTO) {
         throw new ApplicationHttpException("JIRA00102", "Request body must contain a valid mapping DTO.", HttpServletResponse.SC_BAD_REQUEST);
       }
       // create a bean
       JiraIssuePriorityMappingBean mappingBean = mappingDTO.toBean(null, JiraIssuePriorityMappingDTO.PRIORITY);
       // set server
       mappingBean.setServer(getServerBean(serverId));
       // save mapping bean
       mappingBean = jiraModule.saveIssuePriorityMapping(mappingBean);
       // and return dto
       return new JiraIssuePriorityMappingDTO(mappingBean, JiraIssuePriorityMappingDTO.PRIORITY);
     } catch (ApplicationHttpException apphex) {
       throw apphex;
     } catch (ApplicationException appex) {
       throw new ApplicationHttpException("JIRA00018", appex.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     } catch (Exception ex) {
       logger.error( "[JIRA00018] Unable to save issue priority mapping {"+ mappingDTO +"}.", ex);
       throw new ApplicationHttpException("JIRA00018", "Unable to save issue priority mapping.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
   }
 
   /**
    * Delete issue priority mapping
    *
    * @param serverId server id
    * @param mappingId mapping id
    * @return result message
    * @throws ApplicationHttpException when failed
    */
   @RequestMapping(value = "/servers/{serverId}/mappings/priorities/{mappingId}", method = RequestMethod.DELETE, produces = {"application/xml", "application/json"})
   @ResponseBody
   @RolesAllowed("ROLE_ADMIN")
   public ResultMessageDTO deleteIssuePriorityMapping(@PathVariable("serverId") String serverId,
                                                      @PathVariable("mappingId") String mappingId) throws ApplicationHttpException {
     try {
       // get mapping
       JiraIssuePriorityMappingBean mappingBean = jiraModule.getIssuePriorityMapping(parseLongValue(mappingId));
       if (null==mappingBean) {
         throw new ApplicationHttpException("JIRA00404", "No mapping with id {"+ mappingId +"} has been found.", HttpServletResponse.SC_NOT_FOUND);
       }
       // get server
       JiraServerBean serverBean = getServerBean(serverId);
       // make sure that mapping belong to the server
       if (!serverBean.equals(mappingBean.getServer())) {
         throw new ApplicationHttpException("JIRA00404", "No mapping with id {"+ mappingId +"} has been found.", HttpServletResponse.SC_NOT_FOUND);
       }
       // delete mapping
       jiraModule.deleteIssuePriorityMapping(mappingBean);
       // return result message
       return ResultMessageDTO.SUCCESS;
 
     } catch (ApplicationHttpException apphex) {
       throw apphex;
     } catch (ApplicationException appex) {
       throw new ApplicationHttpException("JIRA00019", appex.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     } catch (Exception ex) {
       logger.error( "[JIRA00019] Unable to delete issue type mappings with id {"+ mappingId +"}.", ex);
       throw new ApplicationHttpException("JIRA00019", "Unable to delete issue type mapping.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
   }
 
   /**
    * Return issue priorities from a remote server
    *
    * @param serverId server id
    * @return jira priorities
    * @throws ApplicationHttpException when failed
    */
   @RequestMapping(value = "/servers/{serverId}/remote/priorities", method = RequestMethod.GET, produces = {"application/xml", "application/json"})
   @ResponseBody
   @RolesAllowed("ROLE_ADMIN")
   public ResultDTO<JiraIssuePriorityDTO> getRemotePriorities(@PathVariable("serverId") String serverId) throws ApplicationHttpException {
     try {
       // get server
       JiraServerBean serverBean = getServerBean(serverId);
       // get and return remote priorities
       return jiraModule.getRemotePriorities(serverBean);
     } catch (ApplicationHttpException apphex) {
       throw apphex;
     } catch (ApplicationException appex) {
       throw new ApplicationHttpException("JIRA00022", appex.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     } catch (Exception ex) {
       logger.error( "[JIRA00022] Unable to return issue priorities from remote JIRA server {"+serverId+"}.", ex);
       throw new ApplicationHttpException("JIRA00022", "Unable to return issue priorities from remote JIRA server.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
   }
 
   /**
    * Return issue statuses from a remote server
    *
    * @param serverId server id
    * @return jira statuses
    * @throws ApplicationHttpException when failed
    */
   @RequestMapping(value = "/servers/{serverId}/remote/statuses", method = RequestMethod.GET, produces = {"application/xml", "application/json"})
   @ResponseBody
   @RolesAllowed("ROLE_ADMIN")
   public ResultDTO<JiraIssueStatusDTO> getRemoteStatuses(@PathVariable("serverId") String serverId) throws ApplicationHttpException {
     try {
       // get server
       JiraServerBean serverBean = getServerBean(serverId);
       // get and return remote priorities
       return jiraModule.getRemoteStatuses(serverBean);
     } catch (ApplicationHttpException apphex) {
       throw apphex;
     } catch (ApplicationException appex) {
       throw new ApplicationHttpException("JIRA00023", appex.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     } catch (Exception ex) {
       logger.error( "[JIRA00023] Unable to return issue statuses from remote JIRA server {"+serverId+"}.", ex);
       throw new ApplicationHttpException("JIRA00023", "Unable to return issue statuses from remote JIRA server.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
   }
 
   /**
    * Return issue types from a remote server
    *
    * @param serverId server id
    * @return jira types
    * @throws ApplicationHttpException when failed
    */
   @RequestMapping(value = "/servers/{serverId}/remote/types", method = RequestMethod.GET, produces = {"application/xml", "application/json"})
   @ResponseBody
   @RolesAllowed("ROLE_ADMIN")
   public ResultDTO<JiraIssueTypeDTO> getRemoteTypes(@PathVariable("serverId") String serverId) throws ApplicationHttpException {
     try {
       // get server
       JiraServerBean serverBean = getServerBean(serverId);
       // get and return remote priorities
       return jiraModule.getRemoteTypes(serverBean);
     } catch (ApplicationHttpException apphex) {
       throw apphex;
     } catch (ApplicationException appex) {
       throw new ApplicationHttpException("JIRA00024", appex.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     } catch (Exception ex) {
       logger.error( "[JIRA00024] Unable to return issue types from remote JIRA server {"+serverId+"}.", ex);
       throw new ApplicationHttpException("JIRA00024", "Unable to return issue types from remote JIRA server.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
   }
 
   /**
    * Return available filters
    *
    * @param serverId server id
    * @return filters
    * @throws ApplicationHttpException when failed
    */
   @RequestMapping(value = "/servers/{serverId}/remote/filters", method = RequestMethod.GET, produces = {"application/xml", "application/json"})
   @ResponseBody
   @RolesAllowed("ROLE_ADMIN")
   public ResultDTO<JiraFilterDTO> getRemoteFilters(@PathVariable("serverId") String serverId) throws ApplicationHttpException {
     try {
       // get server
       JiraServerBean serverBean = getServerBean(serverId);
       // get and return remote priorities
       return jiraModule.getRemoteFilters(serverBean);
     } catch (ApplicationHttpException apphex) {
       throw apphex;
     } catch (ApplicationException appex) {
       throw new ApplicationHttpException("JIRA00025", appex.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     } catch (Exception ex) {
       logger.error( "[JIRA00025] Unable to return filters from remote JIRA server {"+serverId+"}.", ex);
       throw new ApplicationHttpException("JIRA00025", "Unable to return filters from remote JIRA server.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
   }
 
   /**
    * Get remote server info
    *
    * @param url server URL
    * @param login login name
    * @param password password
    * @return Server info
    * @throws ApplicationHttpException when failed
    */
   @RequestMapping(value = "/servers/remote/info", method = RequestMethod.GET, produces = {"application/xml", "application/json"})
   @ResponseBody
   @RolesAllowed("ROLE_ADMIN")
   public JiraServerInfoDTO getRemoteServerInfo(@RequestParam("url") String url,
                                                @RequestParam("login") String login,
                                                @RequestParam("password") String password) throws ApplicationHttpException {
     // create a dummy server bean
     JiraServerBean serverBean = new JiraServerBean();
     serverBean.setUrl(url);
     serverBean.setLogin(login);
     serverBean.setPassword(password);
     try {
       return jiraModule.getRemoteServerInfo(serverBean);
     } catch (ApplicationException appex) {
       throw new ApplicationHttpException("JIRA00160", appex.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     } catch (Exception ex) {
       logger.error("[JIRA00026] Unable to return remote server info {"+serverBean+"}.", ex);
       throw new ApplicationHttpException("JIRA00026", "Unable to return remote server info.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
   }
 
   /**
    * Parse given value as Long
    * 
    * @param value value to parse
    * @return Long value or null
    * @throws ApplicationHttpException when failed
    */
   protected Long parseLongValue(String value) throws ApplicationHttpException {
     if (null==value || value.trim().length()==0) return null;
     try {
       return Long.valueOf(value);
     } catch (NumberFormatException e) {
       logger.warn( "[JIRA00101] Unable to parse {"+value+"} as Long.", e);
       throw new ApplicationHttpException("JIRA00101", "Unable to parse {"+value+"} as Long.", HttpServletResponse.SC_BAD_REQUEST);
     }
   }
   
   /**
    * Return server bean with given id
    *
    * @param serverId server id
    * @return server bean or null if server with given ID is not found
    * @throws ApplicationHttpException when server bean can not be loaded
    */
   protected JiraServerBean getServerBean(String serverId) throws ApplicationHttpException {
     Long _serverId = null;
     // parse server
     try {
       // sanity check
       if (null==serverId || serverId.trim().length()==0) throw new Exception("Server id can not be blank.");
       // parse id
       _serverId = Long.valueOf(serverId);
       // get server bean
       JiraServerBean jiraServerBean = jiraModule.getServer(_serverId);
       if (null==jiraServerBean) {
         throw new ApplicationHttpException("JIRA00404", "No server with id {"+ serverId +"} has been found.", HttpServletResponse.SC_NOT_FOUND);
       }
       return jiraServerBean;
     } catch (ApplicationHttpException apphex) {
       throw apphex;
     } catch (ApplicationException appex) {
       throw new ApplicationHttpException("JIRA00151", appex.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     } catch (Exception ex) {
       logger.warn( "Unable to parse server id {"+serverId+"}.", ex);
       throw new ApplicationHttpException("JIRA00110", "Not valid server id {"+serverId+"}.", HttpServletResponse.SC_BAD_REQUEST);
     }
   }
 
   /**
    * Return Trecena user bean for logged in user
    *
    * @param failOnUserNotFound when set, method will throw an exception if user has not been found
    * @return user bean
    * @throws ApplicationHttpException when failed
    */
   protected UserBean getLoggedUserBean(Boolean failOnUserNotFound) throws ApplicationHttpException {
     // get user
     if (null==userRequestContext) {
       throw new ApplicationHttpException("JIRA00103", "Unable to get user request context.", HttpServletResponse.SC_BAD_REQUEST);
     }
     try {
       // load user bean
       String loginName = userRequestContext.getLoginName();
       UserBean userBean = jiraModule.getUserBean(loginName);
       if (null==userBean && failOnUserNotFound) {
         throw new ApplicationHttpException("JIRA00104", "No user with loginName {"+loginName+"} has been found.", HttpServletResponse.SC_BAD_REQUEST);
       }
       return userBean;
     } catch (ApplicationHttpException apphex) {
       throw apphex;
     } catch (ApplicationException appex) {
       throw new ApplicationHttpException("JIRA00028", appex.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     } catch (Exception ex) {
       logger.error("[JIRA00028] Unable to load user bean for context {"+userRequestContext+"}");
       throw new ApplicationHttpException("JIRA00028", "Unable to load user.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     }
   }
 
   /**
    * Create and return a slim server DTO
    *
    * @param bean bean
    * @return dto
    */
   protected JiraServerDTO getSlimServerDTO(JiraServerBean bean) {
     if (null==bean) return null;
     // create slim server dto
     JiraServerDTO slimServerDto = new JiraServerDTO();
     slimServerDto.setId(bean.getId());
     slimServerDto.setTitle(bean.getTitle());
     slimServerDto.setUrl(bean.getUrl());
     slimServerDto.setEnabled(bean.getEnabled());
     return slimServerDto;
   }
 
 }
