 package no.niths.application.rest.school;
 
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import no.niths.application.rest.AbstractRESTControllerImpl;
 import no.niths.application.rest.RESTConstants;
 import no.niths.application.rest.exception.DuplicateEntryCollectionException;
 import no.niths.application.rest.exception.NotInCollectionException;
 import no.niths.application.rest.exception.QRCodeException;
 import no.niths.application.rest.helper.QRCodeDecoder;
 import no.niths.application.rest.lists.FadderGroupList;
 import no.niths.application.rest.lists.ListAdapter;
 import no.niths.application.rest.lists.StudentList;
 import no.niths.application.rest.school.interfaces.FadderGroupController;
 import no.niths.common.AppConstants;
 import no.niths.common.SecurityConstants;
 import no.niths.common.ValidationHelper;
 import no.niths.domain.school.FadderGroup;
 import no.niths.domain.school.Student;
 import no.niths.services.interfaces.GenericService;
 import no.niths.services.school.interfaces.FadderGroupService;
 import no.niths.services.school.interfaces.StudentService;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.multipart.MultipartHttpServletRequest;
 import org.springframework.web.multipart.commons.CommonsMultipartFile;
 /**
  * Controller for subjects
  *
  */
 @Controller
 @RequestMapping(AppConstants.FADDER)
 public class FadderGroupControllerImpl
         extends AbstractRESTControllerImpl<FadderGroup>
         implements FadderGroupController{
 
     private static final Logger logger = LoggerFactory
             .getLogger(FadderGroupControllerImpl.class);
 
     @Autowired
     private FadderGroupService service;
 
     @Autowired
     private StudentService studService;
 
     private FadderGroupList fadderGroupList = new FadderGroupList();
 
     private StudentList studentList = new StudentList();
     
     /**
      * {@inheritDoc}
      */
     @Override
     @PreAuthorize(SecurityConstants.ADMIN_SR_FADDER_LEADER)
     public void create(
             @RequestBody FadderGroup domain,
             HttpServletResponse res) {
         super.create(domain, res);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @PreAuthorize(SecurityConstants.ADMIN_SR_FADDER_LEADER)
     public void update(@RequestBody FadderGroup domain) {
         super.update(domain);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @PreAuthorize(SecurityConstants.ADMIN_SR_FADDER_LEADER)
     public void hibernateDelete(@PathVariable long id) {
         super.hibernateDelete(id);
     }
 
     /**
      * @return FadderGroup the fadder group in which the student resides
      */
     @Override
     @RequestMapping(
             value   = "getGroupBelongingTo/{studentId}",
             method  = RequestMethod.GET,
             headers = RESTConstants.ACCEPT_HEADER)
     @ResponseBody
     public FadderGroup getGroupBelongingToStudent(
             @PathVariable Long studentId) {
         Student s = studService.getById(studentId);
         ValidationHelper.isObjectNull(s, Student.class);
         FadderGroup g = service.getGroupBelongingToStudent(studentId);
         ValidationHelper.isObjectNull(g, FadderGroup.class);
         g.setFadderChildren(null);
         g.setLeaders(null);
 
         return g;
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
     @PreAuthorize(SecurityConstants.ADMIN_SR_FADDER_LEADER)
     @RequestMapping(
             value  = "addLeader/{groupId}/{studId}",
             method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "Leader added")
     public void addLeaderToAGroup(
             @PathVariable Long groupId,
             @PathVariable Long studId) {
         FadderGroup group = getGroup(groupId);
         Student stud = getStudent(studId);
 
         group.getLeaders().add(stud);
         service.update(group);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @PreAuthorize(SecurityConstants.ADMIN_SR_FADDER_LEADER)
     @RequestMapping(
             value  = "removeLeader/{groupId}/{studId}",
             method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "Leader removed")
     public void removeLeaderFromAGroup(
             @PathVariable Long groupId,
             @PathVariable Long studId) {
         FadderGroup group = getGroup(groupId);
         Student stud = getStudent(studId);
   
         
         if(group.getLeaders().contains(stud)){
             group.getLeaders().remove(stud);
             service.update(group);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @PreAuthorize(SecurityConstants.ADMIN_SR_FADDER_LEADER)
     @RequestMapping(
             value  = "addChild/{groupId}/{studId}",
             method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "Child added")
     public void addChildToAGroup(
             @PathVariable Long groupId,
             @PathVariable Long studId) {
         FadderGroup group = getGroup(groupId);
         Student stud = getStudent(studId);
         
         if(stud.getFadderGroup() != null){
             throw new DuplicateEntryCollectionException(
                     "Student is already a child");
         }
 
         group.getFadderChildren().add(stud);
         service.update(group);       
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @PreAuthorize(SecurityConstants.ADMIN_SR_FADDER_LEADER)
     @RequestMapping(
             value  = "removeChild/{groupId}/{studId}",
             method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "Child removed")
     public void removeChildFromGroup(
             @PathVariable Long groupId,
             @PathVariable Long studId) {
         FadderGroup group = getGroup(groupId);
         Student stud = getStudent(studId);
 
         if(group.getFadderChildren().contains(stud)){
             group.getFadderChildren().remove(stud);
             service.update(group);
         }else{
             throw new NotInCollectionException(
                     "Student not a child in that group");
         }
         
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @PreAuthorize(SecurityConstants.ADMIN_SR_FADDER_LEADER)
     @RequestMapping(
             value  = { "{groupId}/remove-children/{studentIds}" },
             method = RequestMethod.DELETE)
     @ResponseStatus(value = HttpStatus.OK, reason = "Children removed")
     public void removeChildrenFromGroup(
             @PathVariable Long groupId,
             @PathVariable Long[] studentIds) {
         for (Long studentId : studentIds) {
             removeChildFromGroup(groupId, studentId);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @PreAuthorize(SecurityConstants.ADMIN_SR_FADDER_LEADER)
     @RequestMapping(
             value  = "removeAllChildren/{groupId}",
             method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "All children removed")
     public void removeAllChildrenFromGroup(@PathVariable Long groupId) {
         FadderGroup group = getGroup(groupId);
         
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
     @PreAuthorize(SecurityConstants.ADMIN_AND_SR)
     @RequestMapping(
             value  = "removeAllLeaders/{groupId}",
             method = RequestMethod.PUT)
     @ResponseStatus(value = HttpStatus.OK, reason = "All leaders removed")
     public void removeAllLeadersFromGroup(@PathVariable Long groupId) {
         FadderGroup group = getGroup(groupId);
         
         if(!group.getLeaders().isEmpty()){
             group.getLeaders().clear();
             service.update(group);
         } else {
             logger.debug("list was empty no need for update");
         }
     }
 
     /**
      * {@inheritDoc}
      * @throws QRCodeException an exception describing what went wrong during
      * scanning
      */
     @Override
     @RequestMapping(value  = "scan-qr-code", method = RequestMethod.POST)
     @ResponseStatus(value  = HttpStatus.OK,  reason = "Scanned QR code")
     public void scanImage(
             HttpServletRequest req,
             HttpServletResponse response) throws QRCodeException {
         
             if (req instanceof MultipartHttpServletRequest) {
                 Map<String, MultipartFile> files =
                         ((MultipartHttpServletRequest) req).getFileMap();
 
             // Iterate over maps like a boss
             for (Map.Entry<String, MultipartFile> entry : files.entrySet()) {
 
                 CommonsMultipartFile file =
                         (CommonsMultipartFile) entry.getValue();
                 response.setHeader(
                         "location",
                         AppConstants.FADDER + '/'
                             + new QRCodeDecoder().decodeFadderGroupQRCode(
                                     file.getBytes()));
             }
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
 
         FadderGroup fadderGroup = getGroup(id);
 
         // Adds the current FadderGroups children to the list.
         studentList.addAll(fadderGroup.getFadderChildren());
         studentList.setData(studentList); // for XML marshalling
         ValidationHelper.isListEmpty(studentList);
 
         for (int i = 0; i < studentList.size(); i++) {
             studentList.get(i).setFeeds(null);
             studentList.get(i).setCommittees(null);
             studentList.get(i).setCourses(null);
         }
 
         return studentList; 
     }
 
     @ExceptionHandler(QRCodeException.class)
     @ResponseStatus(
             value  = HttpStatus.BAD_REQUEST,
             reason = "Scan failed")
     public void catchNotFoundException(
             QRCodeException e,
             HttpServletResponse response) {
         response.setHeader(ERROR, e.getMessage());
     }
 
     private Student getStudent(Long studId) {
         Student stud = studService.getById(studId);
         ValidationHelper.isObjectNull(stud, Student.class);
         return stud;
     }
 
     private FadderGroup getGroup(Long groupId) {
        FadderGroup group = service.getById(groupId);
         ValidationHelper.isObjectNull(group, FadderGroup.class);
         return group;
     }
 }
