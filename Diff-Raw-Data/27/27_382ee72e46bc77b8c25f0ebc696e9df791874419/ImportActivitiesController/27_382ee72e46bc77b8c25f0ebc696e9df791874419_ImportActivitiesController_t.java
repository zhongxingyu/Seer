 package edu.northwestern.bioinformatics.studycalendar.web.template;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Source;
 import edu.northwestern.bioinformatics.studycalendar.service.ImportActivitiesService;
 import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
 import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
 import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
 import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.validation.BindException;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.Collection;
 import java.util.Map;
 import java.util.List;
 
 import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.BUSINESS_ADMINISTRATOR;
 
 public class ImportActivitiesController extends PscSimpleFormController implements PscAuthorizedHandler {
     private ImportActivitiesService importActivitiesService;
     private SourceDao sourceDao;
 
     public ImportActivitiesController() {
         setCommandClass(ImportActivitiesCommand.class);
         setValidator(new ValidatableValidator());
         setFormView("template/importActivities");
         setSuccessView("redirectToActivities");
     }
 
     public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
         return ResourceAuthorization.createCollection(BUSINESS_ADMINISTRATOR);
     }
 
     @Override
     protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
         ImportActivitiesCommand command = (ImportActivitiesCommand) oCommand;
         List<Source> sources = sourceDao.getAll();
         Source s = null;
         try {
             s = command.apply();
         } catch (Exception e) {
             errors.reject("error.problem.importing.file", new String[]{e.getMessage()}, e.getMessage());
 
         }
         Map<String, Object> model = errors.getModel();
         if (errors.hasErrors()) {
             return showForm(request, response, errors);
         } else {
             List<Source> sourcesAfterAdding = sourceDao.getAll();
             //default sourse to display if there is an error
             Source sourceToDisplay = sourcesAfterAdding.get(0);
             if(s!= null) {
                 sourceToDisplay = s;
             } else {
                 for (Source source : sourcesAfterAdding) {
                     if (!sources.contains(source)) {
                         sourceToDisplay = source;
                         break;
                     }
                 }
             }
             model.put("sourceId", sourceToDisplay.getId());
            model.put("sourceName", sourceToDisplay.getName());
             return new ModelAndView(getSuccessView(),model);
 
         }
     }
 
 
     protected ImportActivitiesCommand formBackingObject(HttpServletRequest httpServletRequest) throws Exception {
         ImportActivitiesCommand command = new ImportActivitiesCommand();
         command.setImportActivitiesService(importActivitiesService);
         return command;
     }
 
     //// CONFIGURATION
     @Required
     public void setImportActivitiesService(ImportActivitiesService importActivitiesService) {
         this.importActivitiesService = importActivitiesService;
     }
 
 
     @Required
     public void setSourceDao(SourceDao sourceDao) {
         this.sourceDao = sourceDao;
     }
 
 }
