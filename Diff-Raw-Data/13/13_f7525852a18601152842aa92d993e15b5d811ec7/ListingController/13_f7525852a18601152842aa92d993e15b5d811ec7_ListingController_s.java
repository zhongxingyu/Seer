 package ccnmtl.vital3.controllers;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.HashSet;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.springframework.dao.DataRetrievalFailureException;
 import org.springframework.validation.BindException;
 import org.springframework.web.servlet.mvc.Controller;
 import org.springframework.web.servlet.ModelAndView;
 
 import ccnmtl.utils.OmniComparator;
 import ccnmtl.vital3.*;
 import ccnmtl.vital3.commands.BasicAdminCommand;
 import ccnmtl.vital3.commands.Vital3Command;
 import ccnmtl.vital3.dao.Vital3DAO;
 import ccnmtl.vital3.ucm.*;
 import ccnmtl.vital3.utils.*;
 
 /**
  * ListingController is intended to be used when a controller's job is basically just to return a list of something.
  * The "mode" parameter is used to distinguish each type of listing that this controller is used for. It is assumed
  * that each listing mode will be different and so the code branches by mode.
  * Future directions: paginated listings?
  */
 public class ListingController extends Vital3CommandController {
 
     /**
      * Protect this controller, varies depending on mode:
      */
     protected Integer getMinAccessLevel(Vital3Command command) {
 
         String mode = command.getMode();
         if (mode.equals("user")) return UserCourseManager.ADMIN_ACCESS;
         else return UserCourseManager.TA_ACCESS;
     }
 
     /**
      * Derive worksite from different parameters in different cases...
      */
     protected VitalWorksite getRequestedWorksite(Vital3Command commandObj) throws Exception {
 
         VitalWorksite worksite = null;
 
         BasicAdminCommand command = (BasicAdminCommand) commandObj;
         String mode = command.getMode();
 
         if (!mode.equals("user")) {
 
             if (mode.equals("roster") || mode.equals("customField")) {
                 worksite = command.getWorksite();
                 ucm.decorateWorksite(worksite, true, false);
             } else {
 
                 if (mode.equals("amAssoc") || mode.equals("glQuestions")) {
                     worksite = command.getAssignment().getRelatedWorksite();
 
                 } else if (mode.equals("qmAssoc")) {
                     worksite = command.getQuestion().getRelatedWorksite();
 
                 } else if (mode.equals("umAssoc")) {
                     worksite = command.getUnit().getWorksite();
                 } else {
                     throw new RuntimeException("invalid mode");
                 }
                 if (worksite == null) throw new RuntimeException("worksite was null. something went wrong.");
                 // decorate worksite:
                 ucm.decorateWorksite(worksite, false, false);
             }
 
         }
 
         return worksite;
     }
 
 
     public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Vital3Command commandObj, BindException errors) throws Exception {
 
         BasicAdminCommand command = (BasicAdminCommand) commandObj;
 
         String message = request.getParameter("message");
         String mode = command.getMode();
         Long id = command.getId();
 
         // This will be the model which is returned
         Map model = new HashMap();
         // This will be the view name which is returned
         String viewName = null;
 
         UserContextInfo userInfo = getUserContextInfo(request);
         VitalUser currentUser = userInfo.getUser();
         VitalWorksite worksite = userInfo.getWorksite();
         boolean admin = userInfo.hasPermission(UserCourseManager.CAN_ADMINISTRATE_WORKSITE_CURRICULUM);
         boolean globalAdmin = userInfo.hasPermission(UserCourseManager.CAN_ADMINISTRATE_ADMINISTRATORS);
         
         if (mode.equals("roster")) {
 
             // must be admin!
             if (!admin) throw new Vital3AuthViolationException(true);
 
             // Get the worksite, init courseAffils and participants:
 
             logger.debug("ListingController decorating worksite...");
             ucm.decorateWorksite(worksite, true, true);
             logger.debug("ListingController getting participants collection...");
             // Sort the participants by Last name:
             ArrayList participants = new ArrayList(worksite.getParticipants());
             OmniComparator participantComp = new OmniComparator(VitalParticipant.class, "getLastName");
             Collections.sort(participants, participantComp);
 
             model.put("participants", participants);
 
             viewName = "rosterManagement";
 
         } else if (mode.equals("customField")) {
 
             // must be admin!
             if (!admin) throw new Vital3AuthViolationException(true);
 
             ArrayList customFields = new ArrayList(worksite.getCustomFields());
             Collections.sort(customFields, new OmniComparator(CustomField.class, "getOrdinalValue"));
             model.put("customFields", customFields);
             viewName = "customFieldManagement";
 
 
         } else if (mode.equals("amAssoc") || mode.equals("qmAssoc") || mode.equals("umAssoc")) {
 
 
             model.put("mode", mode);
 
             // must be admin!
             if (!admin) throw new Vital3AuthViolationException(true);
 
             viewName = "assocManagement";
 
             ArrayList materialList = new ArrayList();
             Set associatedMaterials = null;
 
             if (mode.equals("amAssoc")) {
                 Assignment assignment = command.getAssignment();
                 model.put("assignment", assignment);
                 associatedMaterials = Vital3Utils.initM2MCollection(Assignment.class, assignment, Material.class);
 
             } else if (mode.equals("qmAssoc")) {
                 Question question = command.getQuestion();
                 model.put("question", question);
                 associatedMaterials = Vital3Utils.initM2MCollection(Question.class, question, Material.class);
 
             } else {
                 Unit unit = command.getUnit();
                 model.put("unit", unit);
                 associatedMaterials = Vital3Utils.initM2MCollection(Unit.class, unit, Material.class);
             }
 
             // need to put hashmaps wrapping each material into arraylist:
            // keys: "material" holds Material, "isAssoc" holds Boolean true if its currently associated.
             ArrayList worksiteMaterials = worksite.getMaterialsSortedByTitle();
             Iterator matIter = worksiteMaterials.iterator();
             while (matIter.hasNext()) {
                 Material material = (Material) matIter.next();
                 HashMap wrapper = new HashMap();
                 wrapper.put("material", material);
                 if (associatedMaterials.contains(material)) wrapper.put("isAssoc", Boolean.TRUE);
                 else wrapper.put("isAssoc", null);
                 materialList.add(wrapper);
             }
             model.put("materials", materialList);
 
         } else if (mode.equals("glQuestions")) {
 
             // must be admin!
             if (!admin) throw new Vital3AuthViolationException(true);
 
             // Get questions and sort by ordinalvalue
             Assignment assignment = command.getAssignment();
             Set questions = assignment.getQuestions();
             // init:
             questions.size();
 
             model.put("questions", questions);
             model.put("assignment", assignment);
             viewName = "questionManagement";
 
         } else if (mode.equals("user")) {
 
             if (!admin) throw new Vital3AuthViolationException(true);
            List users = ucm.findAllUsers(false);
 
             // sort the users in the order of their userIdString
             OmniComparator userComp = new OmniComparator(VitalUser.class, "getUserIdString");
             Collections.sort(users, userComp);

            model.put("users", users);
             viewName="userManagement";
 
         }
 
         model.put("worksite", worksite);
         model.put("currentUser", currentUser);
         model.put("admin", new Boolean(admin));
         model.put("globalAdmin", new Boolean(globalAdmin));
         if (message != null) model.put("message", message);
 
         return new ModelAndView(viewName, model);
     }
 }
