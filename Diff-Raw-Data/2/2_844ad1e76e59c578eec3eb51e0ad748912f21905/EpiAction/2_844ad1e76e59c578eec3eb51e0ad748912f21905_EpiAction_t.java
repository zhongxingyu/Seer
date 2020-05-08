 package gov.nih.nci.eagle.web.struts;
 
 import gov.nih.nci.caintegrator.application.cache.PresentationCacheManager;
 import gov.nih.nci.caintegrator.application.lists.ListType;
 import gov.nih.nci.caintegrator.application.lists.UserList;
 import gov.nih.nci.caintegrator.application.lists.UserListBeanHelper;
 import gov.nih.nci.caintegrator.exceptions.FindingsQueryException;
 import gov.nih.nci.caintegrator.service.task.Task;
 import gov.nih.nci.caintegrator.studyQueryService.FindingsManager;
 import gov.nih.nci.caintegrator.studyQueryService.dto.epi.EPIQueryDTO;
 import gov.nih.nci.caintegrator.studyQueryService.dto.epi.EducationLevel;
 import gov.nih.nci.caintegrator.studyQueryService.dto.epi.ExposureLevel;
 import gov.nih.nci.caintegrator.studyQueryService.dto.epi.Gender;
 import gov.nih.nci.caintegrator.studyQueryService.dto.epi.MaritalStatus;
 import gov.nih.nci.caintegrator.studyQueryService.dto.epi.Relative;
 import gov.nih.nci.caintegrator.studyQueryService.dto.epi.Religion;
 import gov.nih.nci.caintegrator.studyQueryService.dto.epi.ResidentialArea;
 import gov.nih.nci.caintegrator.studyQueryService.dto.epi.SmokingStatus;
 import gov.nih.nci.caintegrator.studyQueryService.dto.epi.TobaccoType;
 import gov.nih.nci.caintegrator.studyQueryService.dto.epi.SmokingExposure;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.actions.DispatchAction;
 import org.apache.struts.util.LabelValueBean;
 
 /**
  * all instance variables (objects) in this action are injected by the Spring
  * container (application-context-services.xml). The struts action itself is
  * managed by spring by use of the
  * org.springframework.web.struts.DelegatingActionProxy class. The action path
  * can then be referenced by Spring in application-context-struts.xml) All
  * struts and spring config files can be found in the WEB-INF directory.
  * 
  * @author landyr
  */
 
 public class EpiAction extends DispatchAction {
     private FindingsManager findingsManager;
     private PresentationCacheManager presentationCacheManager;
 
     public ActionForward submit(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response)
             throws IOException {
         EPIQueryDTO dto = new EPIQueryDTO();
         dto.setQueryName(((EpiForm)form).getQueryName());
         try {
             Task task = findingsManager.submitQuery(dto);
             presentationCacheManager.addNonPersistableToSessionCache(request
                     .getSession().getId(), task.getId(), task);
         } catch (FindingsQueryException e) {
             ActionErrors errors = new ActionErrors();
             errors.add("queryErrors", new ActionMessage(
                     "caintegrator.error.query"));
             saveMessages(request, errors);
             return (mapping.findForward("failure"));
         }
 
         return (mapping.findForward("success"));
     }
 
     public ActionForward setup(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response)
             throws Exception {
 
     	EpiForm eform = (EpiForm) form;
     	//set the group names
     	UserListBeanHelper helper = new UserListBeanHelper(request.getSession());
         List<UserList> patientLists = helper.getLists(ListType.PatientDID);
         List<LabelValueBean> lvbeans = new ArrayList<LabelValueBean>();
         for(UserList patientList: patientLists){
         	//sampleGroups.add(new LabelValueBean(patientList.getName(),patientList.getClass().getCanonicalName() + "#" + patientList.getName()));
         	lvbeans.add(new LabelValueBean(patientList.getName(),patientList.getName()));
         }
         eform.setExistingGroups(lvbeans);        
 
         //set the smoking status
         lvbeans = new ArrayList<LabelValueBean>();
         for(SmokingStatus s : SmokingStatus.values())	{
         	lvbeans.add(new LabelValueBean(s.getName(), s.toString()));
         }
         eform.setExistingSmokingStatus(lvbeans);
         
         //set the gender
         lvbeans = new ArrayList<LabelValueBean>();
         for(Gender s : Gender.values())	{
         	lvbeans.add(new LabelValueBean(s.getName(), s.toString()));
         }
         eform.setExistingGender(lvbeans);
  
         
         //set the educationLevel
         lvbeans = new ArrayList<LabelValueBean>();
         for(EducationLevel s : EducationLevel.values())	{
         	lvbeans.add(new LabelValueBean(s.getName(), s.toString()));
         }
         eform.setExistingEducationLevel(lvbeans);
 
         //set the residentialArea
         lvbeans = new ArrayList<LabelValueBean>();
         for(ResidentialArea s : ResidentialArea.values())	{
         	lvbeans.add(new LabelValueBean(s.getName(), s.toString()));
         }
         eform.setExistingResidentialArea(lvbeans);
  
         //set the maritalStatus
         lvbeans = new ArrayList<LabelValueBean>();
         for(MaritalStatus s : MaritalStatus.values())	{
         	lvbeans.add(new LabelValueBean(s.getName(), s.toString()));
         }
         eform.setExistingMaritalStatus(lvbeans);
         
         //set the religion
         lvbeans = new ArrayList<LabelValueBean>();
         for(Religion s : Religion.values())	{
         	lvbeans.add(new LabelValueBean(s.getName(), s.toString()));
         }
         eform.setExistingReligion(lvbeans);
         
         //set the relatives
         lvbeans = new ArrayList<LabelValueBean>();
         for(Relative s : Relative.values())	{
        	lvbeans.add(new LabelValueBean(s.getName(), s.toString()));
         }
         eform.setExistingRelatives(lvbeans);
  
         //set the smokiness
         lvbeans = new ArrayList<LabelValueBean>();
         for(ExposureLevel s : ExposureLevel.values())	{
         	lvbeans.add(new LabelValueBean(s.getName(), s.toString()));
         }
         eform.setExistingSmokiness(lvbeans);
 
         //set the tobacco type
         lvbeans = new ArrayList<LabelValueBean>();
         for(TobaccoType s : TobaccoType.values())	{
         	lvbeans.add(new LabelValueBean(s.getName(), s.toString()));
         }
         eform.setExistingTobaccoType(lvbeans);
 
         //set the smokingAreas
         lvbeans = new ArrayList<LabelValueBean>();
         for(SmokingExposure s : SmokingExposure.values())	{
         	lvbeans.add(new LabelValueBean(s.getName(), s.toString()));
         }
         eform.setExistingSmokingAreas(lvbeans);
         
         return mapping.findForward("success");
     }
 
     public FindingsManager getFindingsManager() {
         return findingsManager;
     }
 
     public void setFindingsManager(FindingsManager findingsManager) {
         this.findingsManager = findingsManager;
     }
 
     public PresentationCacheManager getPresentationCacheManager() {
         return presentationCacheManager;
     }
 
     public void setPresentationCacheManager(
             PresentationCacheManager presentationCacheManager) {
         this.presentationCacheManager = presentationCacheManager;
     }
 }
