 package edu.northwestern.bioinformatics.studycalendar.web.template;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
 import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
 import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
 import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.web.bind.ServletRequestUtils;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.Controller;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.*;
 
 import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER;
 
 /**
  * @author Saurabh Agrawal
  */
 public class SelectStudyController implements Controller, PscAuthorizedHandler {
     private DeltaService deltaService;
     private StudyDao studyDao;
     private static final Logger log = LoggerFactory.getLogger(SelectStudyController.class.getName());
 
     public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
         return ResourceAuthorization.createCollection(STUDY_CALENDAR_TEMPLATE_BUILDER);
     }
 
     @SuppressWarnings({"unchecked"})
     public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
         int id = ServletRequestUtils.getRequiredIntParameter(request, "study");
         Study study = studyDao.getById(id);
         Study theRevisedStudy = null;
 
         if (study.isReleased()) {
             //user has selected the releaesd template so dont revise the study
             theRevisedStudy = study;
         } else if (study.isInDevelopment() && study.getDevelopmentAmendment() != null) {
             theRevisedStudy = deltaService.revise(study, study.getDevelopmentAmendment());
         } else {
             theRevisedStudy = study;
         }
 
         List<Epoch> epochs = theRevisedStudy.getPlannedCalendar().getEpochs();
         List<Epoch> displayEpochs = new LinkedList<Epoch>();
         for (Epoch epoch : epochs) {
             List<StudySegment> studySegments = epoch.getStudySegments();
             for (StudySegment studySegment : studySegments) {
                 if (!studySegment.getPeriods().isEmpty()) {
                     displayEpochs.add(epoch);
                     break;
                 }
             }
         }
         Map model = new HashMap();
        model.put("study", theRevisedStudy);
         model.put("epochs", displayEpochs);
         return new ModelAndView("template/ajax/displayEpochs", model);
     }
 
 
     @Required
     public void setDeltaService(DeltaService deltaService) {
         this.deltaService = deltaService;
     }
 
     @Required
     public void setStudyDao(final StudyDao studyDao) {
         this.studyDao = studyDao;
     }
 }
 
