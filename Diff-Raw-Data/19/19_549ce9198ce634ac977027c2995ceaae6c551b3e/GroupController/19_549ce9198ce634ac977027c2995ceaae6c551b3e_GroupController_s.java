 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.abada.cleia.rest.identity.group;
 
 import com.abada.cleia.dao.GroupDao;
 import com.abada.cleia.entity.user.Group;
 import com.abada.cleia.entity.user.User;
 import com.abada.cleia.entity.user.Views;
 import com.abada.extjs.ExtjsStore;
 import com.abada.extjs.Success;
 import com.abada.springframework.web.servlet.command.extjs.gridpanel.GridRequest;
 import com.abada.springframework.web.servlet.command.extjs.gridpanel.factory.GridRequestFactory;
 import com.abada.springframework.web.servlet.view.JsonView;
 import java.util.ArrayList;
 import java.util.List;
 import javax.annotation.security.RolesAllowed;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 /**
  *
  * @author mmartin
  */
 @Controller
 @RequestMapping("/rs/group")
 public class GroupController {
 
     private static final Log logger = LogFactory.getLog(GroupController.class);
     @Autowired
     private GroupDao groupDao;
 
     /**
      * Returns all users groups (service in clinical definition)
      *
      * @return Return a list of groups.
      */
     @RolesAllowed(value = {"ROLE_ADMIN", "ROLE_ADMINISTRATIVE"})
     @RequestMapping(value = "/list", method = RequestMethod.GET)
     public void getAllGroups(Model model) {
         ExtjsStore aux = new ExtjsStore();
         List<Group> listgroups = groupDao.getAllGroups();
         aux.setTotal(listgroups.size());
         aux.setData(listgroups);
         model.addAttribute(JsonView.JSON_VIEW_RESULT, aux);
         model.addAttribute(JsonView.JSON_VIEW_CLASS, Views.Public.class);
     }
 
     /**
      * Returns one user group by id
      *
      * @param idgroup Group id.
      * @return Return a user group.
      */
     @RolesAllowed(value = {"ROLE_ADMIN", "ROLE_ADMINISTRATIVE"})
     @RequestMapping(value = "/{idgroup}", method = RequestMethod.GET)
     public void getGroupById(@PathVariable Long idgroup, Model model) {
 
         Group group = new Group();
         try {
             group = groupDao.getGroupById(idgroup);
         } catch (Exception e) {
             logger.error(e);
         }
         model.addAttribute(JsonView.JSON_VIEW_RESULT, group);
         model.addAttribute(JsonView.JSON_VIEW_CLASS, Views.Public.class);
     }
 
     /**
      * Returns a list of all users from a group
      *
      * @param idgroup Group id.
      * @return Return a list of users.
      * @throws Exception
      */
     @RolesAllowed(value = {"ROLE_ADMIN", "ROLE_USER", "ROLE_ADMINISTRATIVE"})
     @RequestMapping(value = "/{idgroup}/users", method = RequestMethod.GET)
    public void getGroupUsers(@PathVariable Long idgroup, Model model) throws Exception {
 
 
         List<User> lusers = new ArrayList<User>();
         ExtjsStore aux = new ExtjsStore();
         try {
             lusers = this.groupDao.getUsersByIdGroup(idgroup);
         } catch (Exception e) {
             logger.error(e);
         }
 
         aux.setData(lusers);
         aux.setTotal(lusers.size());
 
         model.addAttribute(JsonView.JSON_VIEW_RESULT, aux);
        model.addAttribute(JsonView.JSON_VIEW_CLASS, Views.Public.class);
     }
 
     /**
      * Search a list of groups by params
      *
      * @param filter Filter conditions of results. Set in JSON by an array of
      * FilterRequestPriv
      * @param sort Order of results. Set in JSON by an array of OrderByRequest
      * @param limit Set the limit of the results. Use it for pagination
      * @param start Set the start of the results. Use it for pagination
      * @return Return a list of results.
      */
     @RolesAllowed(value = {"ROLE_ADMIN", "ROLE_ADMINISTRATIVE"})
     @RequestMapping(value = "/search", method = RequestMethod.GET)
     public void getSearchGroup(String filter, String sort, Integer limit, Integer start,Model model) {
 
         List<Group> lgroup;
         ExtjsStore aux = new ExtjsStore();
         try {
             GridRequest grequest = GridRequestFactory.parse(sort, start, limit, filter);
             lgroup = this.groupDao.getAll(grequest);
             aux.setData(lgroup);
             aux.setTotal(this.groupDao.loadSizeAll(grequest).intValue());
         } catch (Exception e) {
             logger.error(e);
         }
 
         model.addAttribute(JsonView.JSON_VIEW_RESULT, aux);
         model.addAttribute(JsonView.JSON_VIEW_CLASS, Views.Public.class);
     }
 
     /**
      * Insert a group
      *
      * @param group Group1 structure. Must set in JSON in the Http body request.
      * @return Return success structure.
      */
     @RolesAllowed(value = {"ROLE_ADMIN", "ROLE_ADMINISTRATIVE"})
     @RequestMapping(method = RequestMethod.POST)
     public Success postGroup(@RequestBody Group group) {
 
         Success result = new Success(Boolean.FALSE);
         try {
             groupDao.postGroup(group);
             result.setSuccess(Boolean.TRUE);
         } catch (Exception e) {
             result.setErrors(new com.abada.extjs.Error(e.getMessage()));
             logger.error(e);
         }
 
         return result;
     }
 
     /**
      * Modify a group by id
      *
      * @param idgroup Group id.
      * @param group Group1 structure. Must set in JSON in the Http body request.
      * @return Return success structure.
      */
     @RolesAllowed(value = {"ROLE_ADMIN", "ROLE_ADMINISTRATIVE"})
     @RequestMapping(value = "/{idgroup}", method = RequestMethod.PUT)
     public Success putGroup(@PathVariable Long idgroup, @RequestBody Group group) {
 
         Success result = new Success(Boolean.FALSE);
         try {
             groupDao.putGroup(idgroup, group);
             result.setSuccess(Boolean.TRUE);
         } catch (Exception e) {
             result.setErrors(new com.abada.extjs.Error(e.getMessage()));
             logger.error(e);
         }
 
         return result;
     }
 
     /**
      * Delete a group by id
      *
      * @param idgroup Group id.
      * @return Return a success structure.
      */
     @RolesAllowed(value = {"ROLE_ADMIN", "ROLE_ADMINISTRATIVE"})
     @RequestMapping(value = "/{idgroup}", method = RequestMethod.DELETE)
     public Success deleteGroup(@PathVariable Long idgroup) {
 
         Success result = new Success(Boolean.FALSE);
         try {
             groupDao.deleteGroup(idgroup);
             result.setSuccess(Boolean.TRUE);
         } catch (Exception e) {
             result.setErrors(new com.abada.extjs.Error(e.getMessage()));
             logger.error(e);
         }
 
         return result;
     }
 }
