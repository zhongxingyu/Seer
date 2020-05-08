 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.gadgetcontainer.application;
 
 import org.apache.commons.lang.Validate;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.List;
 import java.util.LinkedList;
 
 import com.globant.katari.core.application.Command;
 
 import com.globant.katari.gadgetcontainer.domain.ApplicationRepository;
 import com.globant.katari.gadgetcontainer.domain.GadgetGroupRepository;
 import com.globant.katari.gadgetcontainer.domain.ContextUserService;
 
 import com.globant.katari.shindig.domain.Application;
 import com.globant.katari.gadgetcontainer.domain.GadgetGroup;
 
 /** Lists all the registered applications that are not already included in the
  * gadget group.
  *
  * This command only lists applications for a customizable group.
  */
 public class ListApplicationsCommand implements Command<List<Application>> {
 
   /** The class logger.
    */
   private static Logger log = LoggerFactory.getLogger(
       ListApplicationsCommand.class);
 
   /** The repository for applications, never null.
    */
   private final ApplicationRepository applicationRepository;
 
   /** The repository for gadget groups.
    *
    * This is never null.
    */
   private final GadgetGroupRepository gadgetGroupRepository;
 
   /** Service used to obtain the currently logged on user.
    *
    * This is never null.
    */
   private final ContextUserService userService;
 
   /** The gadget group where to add the application to.
    */
   private String gadgetGroupName;
 
   /** The url to go back when the user 'closes' the application list.
    */
   private String returnUrl;
 
   /** Constructor.
    *
    * @param theApplicationRepository Cannot be null.
    *
    * @param theGroupRepository Cannot be null.
    *
    * @param theUserService Cannot be null.
    */
   public ListApplicationsCommand(
       final ApplicationRepository theApplicationRepository,
       final GadgetGroupRepository theGroupRepository,
       final ContextUserService theUserService) {
 
     Validate.notNull(theApplicationRepository,
         "application repository can not be null");
     Validate.notNull(theGroupRepository, "gadget repository can not be null");
     Validate.notNull(theUserService, "user service can not be null");
 
     applicationRepository = theApplicationRepository;
     gadgetGroupRepository = theGroupRepository;
     userService = theUserService;
   }
 
   /** Obtains all the applications that are not already included in the gadget
    * group.
    *
    * You must call setGadgetGroupName before this operation. The gadget group
    * must be customizable.
    *
    * @return a list of applications, never returns null.
    */
   public List<Application> execute() {
     log.trace("Entering execute");
     Validate.notNull(gadgetGroupName, "Set the gadget group name.");
     List<Application> applications = applicationRepository.findAll();
     List<Application> result = new LinkedList<Application>();
     long uid = userService.getCurrentUserId();
     GadgetGroup group;
     group = gadgetGroupRepository.findGadgetGroup(uid, gadgetGroupName);
     if (group == null) {
      throw new RuntimeException("Group " + group + " not found.");
     }
     if (!group.isCustomizable()) {
       throw new RuntimeException("Group " + group + " is not customizable.");
     }
 
     for (Application application: applications) {
       if (!group.contains(application)) {
         result.add(application);
       }
     }
     log.trace("Entering execute");
     return result;
   }
 
   /** Obtains the name of the group to add the application to.
    *
    * @return the name of the group.
    */
   public String getGadgetGroupName() {
     return gadgetGroupName;
   }
 
   /** Sets the name of the group to add the application to.
    *
    * @param name the name of the group.
    */
   public void setGadgetGroupName(final String name) {
     gadgetGroupName = name;
   }
 
   /** Obtains the url to return to when the user closes the application list.
    *
    * @return the url to return to.
    */
   public String getReturnUrl() {
     return returnUrl;
   }
 
   /** Sets the url to return to when the user closes the application list.
    *
    * @param url the url to return to.
    */
   public void setReturnUrl(final String url) {
     returnUrl = url;
   }
 }
 
