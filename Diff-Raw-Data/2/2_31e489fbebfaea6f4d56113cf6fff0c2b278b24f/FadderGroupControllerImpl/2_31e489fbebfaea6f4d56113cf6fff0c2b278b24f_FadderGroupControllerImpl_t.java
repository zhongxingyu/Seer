 package no.niths.application.rest;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import no.niths.application.rest.interfaces.FadderGroupController;
 import no.niths.application.rest.lists.FadderGroupList;
 import no.niths.application.rest.lists.ListAdapter;
 import no.niths.application.rest.lists.StudentList;
 import no.niths.common.AppConstants;
 import no.niths.common.ValidationHelper;
 import no.niths.domain.FadderGroup;
 import no.niths.domain.Student;
 import no.niths.services.interfaces.FadderGroupService;
 import no.niths.services.interfaces.GenericService;
 import no.niths.services.interfaces.StudentService;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 /**
  * Controller for subjects
  *
  */
 @Controller
 @RequestMapping(AppConstants.FADDER)
 public class FadderGroupControllerImpl extends AbstractRESTControllerImpl<FadderGroup> implements FadderGroupController{
 
     private static final Logger logger = LoggerFactory
             .getLogger(FadderGroupControllerImpl.class);
 
     @Autowired
     private FadderGroupService service;
     
     @Autowired
     private StudentService studService;
 
     private FadderGroupList fadderGroupList = new FadderGroupList();
 
     private StudentList studentList = new StudentList();
     
     
     @Override
     @RequestMapping(value = "{id}", method = RequestMethod.GET, headers = RESTConstants.ACCEPT_HEADER)
     @ResponseBody
     public FadderGroup getById(@PathVariable Long id) {
         FadderGroup group = super.getById(id);
         if(group != null){
             for(Student l: group.getLeaders()){
                 l.setCommittees(null);
                 l.setCourses(null);
             }
             for(Student c: group.getFadderChildren()){
                 c.setCommittees(null);
                 c.setCourses(null);
             }
         }
         return group;
     }
     /**
      * {@inheritDoc}
      * 
      * 
      */
     @Override
     @RequestMapping(method = RequestMethod.GET, headers = RESTConstants.ACCEPT_HEADER)
     @ResponseBody
     public ArrayList<FadderGroup> getAll(FadderGroup domain) {
        fadderGroupList = (FadderGroupList) super.getAll(domain);
         for (int i = 0; i < fadderGroupList.size(); i++){
             fadderGroupList.get(i).setFadderChildren(null);
             fadderGroupList.get(i).setLeaders(null);
         }
         return fadderGroupList;
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public GenericService<FadderGroup> getService() {
         return service;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public ListAdapter<FadderGroup> getList() {
         return fadderGroupList;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @RequestMapping(value = { "addLeader/{groupId}/{studId}" }, method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "Leader added")
     public void addLeaderToAGroup(@PathVariable Long groupId, @PathVariable Long studId) {
         FadderGroup group = service.getById(groupId);
         ValidationHelper.isObjectNull(group);
         Student stud = studService.getById(studId);
         ValidationHelper.isObjectNull(stud);
         
         group.getLeaders().add(stud);
         service.update(group);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @RequestMapping(value = { "removeLeader/{groupId}/{studId}" }, method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "Leader removed")
     public void removeLeaderFromAGroup(@PathVariable Long groupId, @PathVariable Long studId) {
         FadderGroup group = service.getById(groupId);
         ValidationHelper.isObjectNull(group);
         Student stud = studService.getById(studId);
         ValidationHelper.isObjectNull(stud);
   
         
         if(group.getLeaders().contains(stud)){
         	group.getLeaders().remove(stud);
         	service.update(group);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @RequestMapping(value = { "addChild/{groupId}/{studId}" }, method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "Child added")
     public void addChildToAGroup(@PathVariable Long groupId, @PathVariable Long studId) {
         FadderGroup group = service.getById(groupId);
         ValidationHelper.isObjectNull(group);
         Student stud = studService.getById(studId);
         ValidationHelper.isObjectNull(stud);
         
         
         group.getFadderChildren().add(stud);
         service.update(group);       
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @RequestMapping(value = { "removeChild/{groupId}/{studId}" }, method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "Child removed")
     public void removeChildFromAGroup(@PathVariable Long groupId, @PathVariable Long studId) {
         FadderGroup group = service.getById(groupId);
         ValidationHelper.isObjectNull(group);
         Student stud = studService.getById(studId);
         ValidationHelper.isObjectNull(stud);
         
         if(group.getFadderChildren().contains(stud)){
         	group.getFadderChildren().remove(stud);
         	service.update(group);
         }else{
         	logger.debug("student was not found in the group no update was performed");
         }
         
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @RequestMapping(value = { "removeAllChildren/{groupId}" }, method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "All children removed")
     public void removeAllChildrenFromAGroup(@PathVariable Long groupId) {
         FadderGroup group = service.getById(groupId);
         ValidationHelper.isObjectNull(group);
         
         if(!group.getFadderChildren().isEmpty()){
         	group.getFadderChildren().clear();
         	service.update(group);
         }else{
         	logger.debug("list was empty no need for update");
         }
       
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @RequestMapping(value = { "removeAllLeaders/{groupId}" }, method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "All leaders removed")
     public void removeAllLeadersFromAGroup(@PathVariable Long groupId) {
         FadderGroup group = service.getById(groupId);
         ValidationHelper.isObjectNull(group);
         
         if(!group.getLeaders().isEmpty()){
         	group.getLeaders().clear();
         	service.update(group);
         }else{
         	logger.debug("list was empty no need for update");
         }
         
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @RequestMapping(value = "{id}/get-all-children", method = RequestMethod.GET)
     @ResponseBody
     public List<Student> getAllStudentsFromFadderGroup(@PathVariable Long id) {
 
         // Clear the list as it is never newed up more than once.
         studentList.clear();
 
         FadderGroup fadderGroup = service.getById(id);
         ValidationHelper.isObjectNull(fadderGroup);
 
         // Adds the current FadderGroups children to the list.
         studentList.addAll(fadderGroup.getFadderChildren());
         studentList.setData(studentList); // for XML marshalling
         ValidationHelper.isListEmpty(studentList);
 
         for (int i = 0; i < studentList.size(); i++) {
             studentList.get(i).setCommittees(null);
             studentList.get(i).setCourses(null);
         }
 
         return studentList; 
     }
 }
