 /*
  *   Copyright 2009-2010 Rustici Software. Licensed under the
  *   Educational Community License, Version 2.0 (the "License"); you may
  *   not use this file except in compliance with the License. You may
  *   obtain a copy of the License at
  *   
  *   http://www.osedu.org/licenses/ECL-2.0
  *
  *   Unless required by applicable law or agreed to in writing,
  *   software distributed under the License is distributed on an "AS IS"
  *   BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  *   or implied. See the License for the specific language governing
  *   permissions and limitations under the License.
  */
 
 package com.rusticisoftware.scormcloud.logic;
 
 import java.io.File;
import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.UUID;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.sakaiproject.assignment.api.Assignment;
 import org.sakaiproject.assignment.api.AssignmentSubmission;
 import org.sakaiproject.assignment.cover.AssignmentService;
 import org.sakaiproject.entity.api.Reference;
 import org.sakaiproject.event.api.Event;
 import org.sakaiproject.genericdao.api.search.Restriction;
 import org.sakaiproject.genericdao.api.search.Search;
 
 import org.w3c.dom.Document;
 
 import com.rusticisoftware.hostedengine.client.Configuration;
import com.rusticisoftware.hostedengine.client.datatypes.LaunchInfo;
import com.rusticisoftware.hostedengine.client.datatypes.RegistrationSummary;
 import com.rusticisoftware.hostedengine.client.ScormEngineService;
 import com.rusticisoftware.hostedengine.client.Utils;
 import com.rusticisoftware.hostedengine.client.XmlUtils;
import com.rusticisoftware.hostedengine.client.datatypes.Enums.RegistrationResultsFormat;
import com.rusticisoftware.hostedengine.client.datatypes.Enums.ReportageNavPermission;
 import com.rusticisoftware.scormcloud.dao.ScormCloudDao;
 import com.rusticisoftware.scormcloud.logic.ExternalLogic;
 import com.rusticisoftware.scormcloud.logic.ScormCloudLogic;
 import com.rusticisoftware.scormcloud.logic.helpers.LaunchHistoryReportHelper;
 import com.rusticisoftware.scormcloud.model.ScormCloudConfiguration;
 import com.rusticisoftware.scormcloud.model.ScormCloudPackage;
 import com.rusticisoftware.scormcloud.model.ScormCloudRegistration;
 
 /**
  * This is the implementation of the business logic interface
  * 
  * @author Sakai App Builder -AZ
  */
 public class ScormCloudLogicImpl implements ScormCloudLogic, Observer {
 
     private static Log log = LogFactory.getLog(ScormCloudLogicImpl.class);
  
     private ScormCloudDao dao;
 
     public void setDao(ScormCloudDao dao) {
         this.dao = dao;
     }
 
     private ExternalLogic externalLogic;
 
     public void setExternalLogic(ExternalLogic externalLogic) {
         this.externalLogic = externalLogic;
     }
 
     /**
      * Place any code that should run when this class is initialized by spring
      * here
      */
     public void init() {
         log.debug("init");
         externalLogic.registerEventObserver(this);
     }
     
     
     
     //----------------- SCORM Cloud Configuration --------------------
     
     public boolean isPluginConfigured() {
         ScormCloudConfiguration configuration = 
             getScormCloudConfigurationInternal(externalLogic.getCurrentContext());
         return (configuration != null);
     }
 
     
     private ScormEngineService getScormEngineService(String context){
         ScormCloudConfiguration cloudConfig = getScormCloudConfigurationInternal(context);
         if(cloudConfig != null){
             log.debug("Found cloud config in database, returning scorm engine service");
             return new ScormEngineService(
                     new Configuration(cloudConfig.getServiceUrl(), 
                             cloudConfig.getAppId(),
                             cloudConfig.getSecretKey()));
         } else {
             log.debug("Couldn't find existing config, returning null");
             return null;
         }
     }
 
     
     public void saveScormCloudConfiguration(ScormCloudConfiguration config) throws Exception {
         if(!canConfigurePlugin()){
             log.error("saveScormCloudConfiguration called without canConfigurePlugin permission!");
             throw new Exception("saveScormCloudConfiguration called without canConfigurePlugin permission!");
         }
         log.debug("saveScormCloudConfiguration called w/ context = " + config.getContext() +
                   ", isMasterConfig = " + config.getIsMasterConfig());
         //If this is supposed to be the master config, make sure to overwrite any existing master
         if(config.getIsMasterConfig()){
             log.debug("Fullfilling request to create/update a master SCORM Cloud configuration");
             ScormCloudConfiguration existingMaster = getScormCloudMasterConfig();
             if(existingMaster != null){
                 existingMaster.copyFrom(config);
                 log.debug("Updating existing SCORM Cloud Master config w/ appId = " + config.getAppId());
                 dao.save(existingMaster);
             } else {
                 log.debug("Saving new SCORM Cloud Master config w/ appId = " + config.getAppId());
                 dao.save(config);
             }
         } else {
             log.debug("Saving new site specific cloud configuration for context = " + 
                     config.getContext() + " and appId = " + config.getAppId());
             dao.save(config);
         }
     }
     
     public ScormCloudConfiguration getScormCloudConfiguration() throws Exception {
         return getScormCloudConfiguration(externalLogic.getCurrentContext(), true);
     }
     
     private ScormCloudConfiguration getScormCloudConfigurationInternal(String context){
         try { return getScormCloudConfiguration(context, false); }
         catch (Exception e) {
             log.error("Exception thrown in getScormCloudConfigurationInternal", e);
             return null;
         }
     }
     
     private ScormCloudConfiguration getScormCloudConfiguration(String context, boolean isPublicCall) throws Exception {
         if(isPublicCall && !isCurrentUserPluginAdmin()){
             log.error("getScormCloudConfiguration called without admin permission!");
             throw new Exception("getScormCloudConfiguration called without admin permission!");
         }
         
         Search s = new Search();
         s.addRestriction(new Restriction("context", context));
         s.addRestriction(new Restriction("isMasterConfig", Boolean.FALSE));
         List<ScormCloudConfiguration> configs = dao.findBySearch(ScormCloudConfiguration.class, s);
         
         if(configs.size() == 0){
             log.warn("Could not find SCORM Cloud configuration for context = " + context + ", returning master config (if present)");
             return getScormCloudMasterConfig();
         }
         if(configs.size() > 1){
             log.warn("Found more than one SCORM Cloud Configuration object for context = " + context);
         }
         log.debug("Found site specific SCORM Cloud configuration with context = " + context);
         return configs.get(0);
     }
     
     private ScormCloudConfiguration getScormCloudMasterConfig() throws Exception {
         Search s = new Search();
         s.addRestriction(new Restriction("isMasterConfig", Boolean.TRUE));
         List<ScormCloudConfiguration> masterConfigs = dao.findBySearch(ScormCloudConfiguration.class, s);
         if(masterConfigs.size() == 0){
             log.warn("Found no master SCORM Cloud configurations");
             return null;
         }
         if(masterConfigs.size() > 1){
             log.warn("Found more than one SCORM Cloud master configuration");
         }
         log.debug("Found SCORM Cloud master configuration");
         return masterConfigs.get(0);
     }
     
     
     
     
     
     //--------------------- Packages -------------------------
     
     public ScormCloudPackage getPackageById(String id) {
         log.debug("Getting package by id: " + id);
         return dao.findById(ScormCloudPackage.class, id);
     }
 
     public void removePackage(ScormCloudPackage pkg) {
         log.debug("In removePackage with item:" + 
                   pkg.getId() + ":" + pkg.getTitle());
         
         try {
             //Delete all associated registrations first
             List<ScormCloudRegistration> regs = getRegistrationsByPackageId(pkg.getId());
             for (ScormCloudRegistration reg : regs){
                 removeRegistration(reg);
             }
             //Then delete the package from the cloud
             getScormEngineService(pkg.getContext())
                 .getCourseService()
                     .DeleteCourse(pkg.getScormCloudId(),
                                   false,
                                   externalLogic.getUserEmail(externalLogic.getCurrentUserId()));
         } catch (Exception e) {
             log.error(
                     "Exception occurred trying to delete package with id = "
                             + pkg.getId() + ", cloud id = "
                             + pkg.getScormCloudId(), e);
         }
         dao.delete(pkg);
         log.info("Deleted package: " + pkg.getId() + ":" + pkg.getTitle());
     }
 
     public void addNewPackage(ScormCloudPackage pkg, File packageZip)
             throws Exception {
         log.debug("In addNewPackage with package:" + pkg.getTitle());
         // set the owner and site to current if they are not set
         if (pkg.getOwnerId() == null) {
             pkg.setOwnerId(externalLogic.getCurrentUserId());
         }
         if (pkg.getLocationId() == null) {
             pkg.setLocationId(externalLogic.getCurrentLocationId());
         }
         if (pkg.getContext() == null) {
             pkg.setContext(externalLogic.getCurrentContext());
         }
         if (pkg.getDateCreated() == null) {
             pkg.setDateCreated(new Date());
         }
         // save pkg if new OR check if the current user can update the existing pkg
         if (pkg.getId() == null) {
             getScormEngineService(pkg.getContext())
                 .getCourseService()
                     .ImportCourse(pkg.getScormCloudId(), 
                                   packageZip.getAbsolutePath(),
                                   null,
                                   null,
                                   externalLogic.getUserEmail(externalLogic.getCurrentUserId()));
             dao.save(pkg);
             log.info("Saved package: " + pkg.getId() + ":" + pkg.getTitle());
         } else {
             throw new SecurityException("Current user cannot update package "
                     + pkg.getId() + " because they do not have permission");
         }
     }
 
     public void updatePackage(ScormCloudPackage pkg) {
         log.debug("In saveItem with item:" + pkg.getTitle());
         // set the owner and site to current if they are not set
         if (pkg.getOwnerId() == null) {
             pkg.setOwnerId(externalLogic.getCurrentUserId());
         }
         if (pkg.getLocationId() == null) {
             pkg.setLocationId(externalLogic.getCurrentLocationId());
         }
         if (pkg.getContext() == null){
             pkg.setContext(externalLogic.getCurrentContext());
         }
         if (pkg.getDateCreated() == null) {
             pkg.setDateCreated(new Date());
         }
         // save pkg if new OR check if the current user can update the existing
         // pkg
         dao.save(pkg);
         log.info("Saving package: " + pkg.getId() + ":" + pkg.getTitle());
     }
     
     public String getPackagePropertiesUrl(ScormCloudPackage pkg, String styleSheetUrl){
         try {
             return getScormEngineService(pkg.getContext())
                     .getCourseService()
                         .GetPropertyEditorUrl(pkg.getScormCloudId(), styleSheetUrl, null);
         } catch (Exception e) {
             log.error("Encountered exception while trying to get " +
                       "property editor url for package with SCORM " +
                       "cloud id = " + pkg.getScormCloudId(), e);
             return null;
         }
     }
     
     public String getPackagePreviewUrl(ScormCloudPackage pkg, String redirectOnExitUrl){
         try {
             return getScormEngineService(pkg.getContext())
                        .getCourseService()
                            .GetPreviewUrl(pkg.getScormCloudId(), redirectOnExitUrl);
         } catch (Exception e) {
             log.error("Encountered an exception while trying to get " +
                       "preview url from SCORM Cloud", e);
           return null;
        }
     }
 
 
     
     
 
     
     
     //---------------------- Registrations ---------------------------
     
     public ScormCloudRegistration addNewRegistration(ScormCloudPackage pkg,
             String userId, String assignmentKey) {
         String userDisplayName = externalLogic.getUserDisplayName(userId);
         String userDisplayId = externalLogic.getUserDisplayId(userId);
         String userEmail = externalLogic.getUserEmail(userId);
         
         Assignment assignment = null;
         if (assignmentKey != null && assignmentKey != ""){
             assignment = externalLogic.getAssignmentFromAssignmentKey(
                     pkg.getContext(), userId, assignmentKey);
         }
 
         String firstName = "sakai";
         String lastName = "learner";
         
         
         if (userDisplayName != null && userDisplayName.contains(" ")) {
             String[] nameParts = userDisplayName.split(" ");
             firstName = nameParts[nameParts.length - 2];
             lastName = nameParts[nameParts.length - 1];
         }
 
         try {
             String unclean = "sakai-reg-" + userDisplayId + "-" + UUID.randomUUID().toString();
             String cloudRegId = Utils.cleanRegistrationId(unclean);
             log.debug(String.format("Attempting to create registration with ID %1$s (%2$s before clean) for user %3$s %4$s <%5$s> (%6$s)", cloudRegId, unclean, firstName, lastName, userEmail, userId));
 
             getScormEngineService(pkg.getContext())
                 .getRegistrationService().CreateRegistration(
                     cloudRegId, pkg.getScormCloudId(), userId, firstName, lastName, userEmail);
             
             ScormCloudRegistration reg = new ScormCloudRegistration();
             reg.setDateCreated(new Date());
             reg.setOwnerId(userId);
             reg.setLocationId(pkg.getLocationId());
             reg.setContext(pkg.getContext());
             
             reg.setUserName(userDisplayId);
             reg.setUserDisplayName(userDisplayName);
             reg.setScormCloudId(cloudRegId);
             reg.setPackageId(pkg.getId());
             reg.setPackageTitle(pkg.getTitle());
             
             if(assignment != null){
                 reg.setAssignmentKey(assignmentKey);
                 reg.setAssignmentId(assignment.getId());
                 reg.setAssignmentName(assignment.getTitle());
                 reg.setContributesToAssignmentGrade(pkg.getContributesToAssignmentGrade());
                 int contributingResources = getNumberOfContributingResourcesForAssignment(assignment);
                 reg.setNumberOfContributingResources(contributingResources);
             } else {
                 reg.setAssignmentName("None");
             }
             
             dao.save(reg);
             return reg;
         } catch (Exception e) {
             log.error("Exception thrown creating reg", e);
             return null;
         }
     }
     
     
     public ScormCloudRegistration getRegistrationById(String id) {
         log.debug("Getting registration with id: " + id);
         return dao.findById(ScormCloudRegistration.class, id);
     }
     
     
     public List<ScormCloudRegistration> getRegistrationsByPackageId(String packageId) {
         log.debug("Getting registrations for package with id = " + packageId);
         ScormCloudPackage pkg = this.getPackageById(packageId);
         if(pkg == null){
             log.debug("Error finding registrations for package, " +
                       "no package found with id = " + packageId);
             return null;
         }
         Search s = new Search();
         s.addRestriction(new Restriction("packageId", packageId));
         return dao.findBySearch(ScormCloudRegistration.class, s);
     }
 
     public List<ScormCloudRegistration> getRegistrationsWherePropertiesEqual(Map<String, Object> propertyMap){
         return searchRegistrationsByPropertyMap(propertyMap, Restriction.EQUALS);
     }
     
     public List<ScormCloudRegistration> getRegistrationsWherePropertiesLike(Map<String, Object> propertyMap){
         return searchRegistrationsByPropertyMap(propertyMap, Restriction.LIKE);
     }
     
     public List<ScormCloudRegistration> searchRegistrationsByPropertyMap(Map<String, Object> propertyMap, int restrictionType){
         log.debug("Getting registrations by property map");
         boolean emptySearch = true;
         Search s = new Search();
         for (String propertyName : propertyMap.keySet()){
             Object propertyValue = propertyMap.get(propertyName);
             if(propertyValue != null){
                 emptySearch = false;
                 log.debug("Adding property " + propertyName + " = " + propertyValue.toString());
                 s.addRestriction(new Restriction(propertyName, propertyValue, restrictionType));
             }
         }
         if(emptySearch){
             return null;
         }
         return dao.findBySearch(ScormCloudRegistration.class, s);
     }
     
     
     
     
     public ScormCloudRegistration findRegistrationFor(ScormCloudPackage pkg, String userId, String assignmentKey) {
         log.debug("Finding registration with userId = " + userId + 
                   ", assignmentKey = " + assignmentKey);
                 
         
         Search s = new Search();
         s.addRestriction(new Restriction("ownerId", userId));
         if(pkg != null){
             s.addRestriction(new Restriction("packageId", pkg.getId()));
         }
         if(assignmentKey != null && assignmentKey != ""){
             s.addRestriction(new Restriction("assignmentKey", assignmentKey));
         }
         List<ScormCloudRegistration> regs = dao.findBySearch(ScormCloudRegistration.class, s);
         
         if (regs.size() >= 1) {
             if (regs.size() > 1) {
                 log.warn("Found more than one registration with userId = "
                         + userId + " and assignmentKey = " + assignmentKey);
             }
             return regs.get(0);
         }
         log.debug("Couldn't find any regs, returning null");
         return null;
     }
     
     private List<ScormCloudRegistration> findContributingRegistrationsFor(String userId, String assignmentId){
         Search s = new Search();
         s.addRestriction(new Restriction("ownerId", userId));
         s.addRestriction(new Restriction("assignmentId", assignmentId));
         s.addRestriction(new Restriction("contributesToAssignmentGrade", Boolean.TRUE));
         return dao.findBySearch(ScormCloudRegistration.class, s);
     }
 
 
     public void removeRegistration(ScormCloudRegistration reg) {
         log.debug("In removeRegistration with regId:" + reg.getId());
         // check if current user can remove this item
         try {
             getScormEngineService(reg.getContext())
                 .getRegistrationService()
                     .DeleteRegistration(reg.getScormCloudId());
         }
         catch (Exception e){
             log.error("Exception thrown trying to delete registration with id = " +
                       reg.getId() + ", cloud id = " + reg.getScormCloudId(), e);
         }
         dao.delete(reg);
         log.info("Removed reg with id: " + reg.getId());
     }
     
     public void resetRegistration(ScormCloudRegistration reg) {
         log.debug("In resetRegistration with regId:" + reg.getId());
         // check if current user can remove this item
         try {
             log.info("Resetting reg with id: " + reg.getId());
             getScormEngineService(reg.getContext())
                 .getRegistrationService()
                     .ResetRegistration(reg.getScormCloudId());
             reg.setComplete("unknown");
             reg.setSuccess("unknown");
             reg.setScore("unknown");
             reg.setTotalTime("0");
             dao.save(reg);
         }
         catch (Exception e){
             log.error("Exception thrown trying to reset registration with id = " +
                       reg.getId() + ", cloud id = " + reg.getScormCloudId(), e);
         }
     }
 
     /*public void updateRegistration(ScormCloudRegistration reg) {
         log.debug("In updateRegistration with reg id:" + reg.getId());
         // set the owner and site to current if they are not set
         if (reg.getOwnerId() == null) {
             reg.setOwnerId(externalLogic.getCurrentUserId());
         }
         if (reg.getLocationId() == null) {
             reg.setLocationId(externalLogic.getCurrentLocationId());
         }
         if (reg.getContext() == null) {
             reg.setContext(externalLogic.getCurrentContext());
         }
         if (reg.getDateCreated() == null) {
             reg.setDateCreated(new Date());
         }
         // save pkg if new OR check if the current user can update the existing pkg
         if (canWriteRegistration(reg, externalLogic.getCurrentLocationId(),
                 externalLogic.getCurrentUserId())) {
             dao.save(reg);
             log.info("Saving registration: " + reg.getId());
         } else {
             throw new SecurityException("Current user cannot update package "
                     + reg.getId() + " because they do not have permission");
         }
     }*/
     
     
 
     public void updateRegistrationResultsFromCloud(ScormCloudRegistration reg) {
         if (reg == null){
             log.debug("Error: registration passed to " +
                       "updateRegistrationResultsFromCloud was null!");
             return;
         }
         try {
             log.debug("Updating registration " + reg.getId() + " with results from SCORM Cloud...");
             RegistrationSummary sum = getScormEngineService(reg.getContext())
                                         .getRegistrationService()
                                         .GetRegistrationSummary(
                                                 reg.getScormCloudId());
             reg.setComplete(sum.getComplete());
             reg.setSuccess(sum.getSuccess());
             reg.setScore(sum.getScore());
             reg.setTotalTime(sum.getTotalTime());
             dao.save(reg);
         } catch (Exception e) {
             log.error("Exception getting registration results for " +
                       "reg with id = " + reg.getId() + 
                       " cloud id = " + reg.getScormCloudId(), e);
         }
     }
     
     
     public String getLaunchUrl(ScormCloudRegistration reg, String redirectOnExitUrl) {
         try {
             return getScormEngineService(reg.getContext())
                         .getRegistrationService()
                             .GetLaunchUrl(reg.getScormCloudId(), redirectOnExitUrl);
         } catch (Exception e) {
             log.error("Encountered an exception while trying to get " +
                       "launch url from SCORM Cloud, returning null", e);
             return null;
         }
     }
     
     public String getLaunchHistoryReport(ScormCloudRegistration reg){
         try {
             List<LaunchInfo> launchHistory = getScormEngineService(reg.getContext())
                                                 .getRegistrationService()
                                                     .GetLaunchHistory(reg.getScormCloudId());
             return (new LaunchHistoryReportHelper()).getLaunchLinks(reg.getId(), launchHistory);
         } catch (Exception e) {
             log.error("Caught exception while trying to retrieve launch history", e);
             return null;
         }
     }
     
     public String getLaunchInfoXml(ScormCloudRegistration reg, String launchId){
         try {
             List<LaunchInfo> launchHistory = getScormEngineService(reg.getContext())
                                                 .getRegistrationService()
                                                     .GetLaunchHistory(reg.getScormCloudId());
             
             LaunchInfo launchInfo = getScormEngineService(reg.getContext())
                                         .getRegistrationService()
                                             .GetLaunchInfo(launchId);
             
             return (new LaunchHistoryReportHelper())
                             .getLaunchInfoXml(reg.getId(), launchInfo, launchHistory);
         } catch (Exception e) {
             log.error("Caught exception while trying to retrieve launch info xml", e);
             return null;
         }
     }
     
     public Document getRegistrationReport(ScormCloudRegistration reg){
         try {
             String resultsXml = getScormEngineService(reg.getContext())
                                     .getRegistrationService()
                                         .GetRegistrationResult(
                                                 reg.getScormCloudId(),
                                                 RegistrationResultsFormat.FULL_DETAIL);
             return XmlUtils.parseXmlString(resultsXml);
         } catch (Exception e) {
             log.error("Encountered an exception while trying to get " +
                       "report xml from SCORM Cloud, returning null", e);
           return null;
         }
     }
     
     public Double getScoreFromRegistration(ScormCloudRegistration reg){
         Double score = null;
         try { 
             score = new Double(reg.getScore()); 
             //The need for this multiplication may disappear in the future
             //At which point the returned score will already be scaled to 100
             if(score <= 1.0){
                 score *= 100.0;
             }
         }
         catch (NumberFormatException nfe) {}
         return (score == null) ? 0.0 : score; 
     }
     
     
     public Double getCombinedScore(List<ScormCloudRegistration> regs){
         if (regs.size() == 0){
             log.debug("getCombinedScore called with no regs, returning 0.0");
             return 0.0;
         }
         
         //Grab number of contributing regs / resources for the assignment
         ScormCloudRegistration firstReg = regs.get(0);
         String assignmentId = firstReg.getAssignmentId();
         int numberOfContributingResources = firstReg.getNumberOfContributingResources();
         
         log.debug("In getCombinedScoreForAssignment, found " + regs.size() + 
                   " regs and " + numberOfContributingResources + " contributing " +
                   " resources associated with assignment = " + assignmentId);
 
         Double scoreSum = 0.0;
         for (ScormCloudRegistration r : regs){
             scoreSum += getScoreFromRegistration(r);
         }
         
         //This assumes all contributing registrations / resources are weighted equally
         return scoreSum / numberOfContributingResources;
     }
     
 
     
     //------------------------------ Reportage ------------------------------------
     
     public String getReportageAuth(String navPermission, boolean isAdmin){
         try {
             return getScormEngineService(externalLogic.getCurrentContext())
                         .getReportingService()
                             .GetReportageAuth(ReportageNavPermission.getValue(navPermission), isAdmin);
         } catch (Exception e) {
             log.warn("Exception getting reportage authentication token: " + e.getMessage(), e);
             return null;
         }
     }
     
     public String getReportUrl(String reportageAuth, String reportUrl){
             try {
                 return getScormEngineService(externalLogic.getCurrentContext())
                             .getReportingService()
                                 .GetReportUrl(reportageAuth, reportUrl);
             } catch (Exception e) {
                 log.warn("Exception calling getReportUrl: " + e.getMessage(), e);
                 return null;
             }
     }
     
     public String getReportDate(){
         try {
            SimpleDateFormat reportageDateFormat = new SimpleDateFormat("yyyy-M-d k:mm:ss");
            return reportageDateFormat.format(getScormEngineService(externalLogic.getCurrentContext())
                         .getReportingService()
                            .GetReportDate());
         } catch (Exception e) {
             log.warn("Exception calling getReportDate: " + e.getMessage(), e);
             return null;
         }
     }
     
     
     
     
     
     
     
     //------------------ Assignment Integration Methods --------------------
 
     public void update(Observable o, Object args){
         Event evt = (Event)args;
         log.debug("SCORM_CLOUD_LOGIC_EVENT_OBSERVATION: " + 
                   " event = " + evt.getEvent() + 
                   " userId = " + evt.getUserId() +
                   " resource = " + evt.getResource());
         
         try {
             if ("asn.submit.submission".equals(evt.getEvent())){
                 //This may not find any associated contributing registrations, 
                 //in which case, it won't update the score of the submission
                 AssignmentSubmission sub = AssignmentService.getSubmission(evt.getResource());
                 updateAssignmentScoreFromRegistrationScores(
                         sub.getAssignment(), externalLogic.getCurrentUserId(), true);
             }
         } catch (Exception e){
             log.error("Exception thrown in update", e);
         }
     }
     
     private int getNumberOfContributingResourcesForAssignment(Assignment asn){
         int count = 0;
         List attachments = asn.getContent().getAttachments();
         log.debug("\tIn getNumberOfActiveCloudPackageResourcesForAssignment, total attachments? = " + attachments.size());
         for (Object att : attachments){
             String pkgId = ((Reference)att).getEntity().getProperties().getProperty("packageId");
             ScormCloudPackage pkg = getPackageById(pkgId);
             if(pkg.getContributesToAssignmentGrade()){
                 count++;
             }
         }
         log.debug("Total number of contributing resources for this assignment? " + count);
         return count;
     }
     
     public void updateAssignmentScoreFromRegistrationScores(Assignment asn, String userId, boolean updateRegsFromCloud){
         String assignmentId = asn.getId();
 
         log.debug("Updating assignment score for assignment " + assignmentId);
         List<ScormCloudRegistration> regs = findContributingRegistrationsFor(userId, assignmentId);
         if(regs.size() == 0){
             log.debug("No contributing regisrations found for assignment = " + assignmentId +
                       " therefore not updating it's score!");
             return;
         }
         
         if(updateRegsFromCloud){
             for(ScormCloudRegistration reg : regs){
                 updateRegistrationResultsFromCloud(reg);
             }
         }
         
         double maxPoints = ((double)asn.getContent().getMaxGradePoint()/10.0);
         Double score = (getCombinedScore(regs) * maxPoints) / 100.0;
         String scoreStr = (score == null) ? null : score.toString();
         externalLogic.updateAssignmentScore(asn, userId, scoreStr);
     }
 
 
     //------------------ Security ---------------------
     
     public boolean isCurrentUserSakaiAdmin(){
         String currentUserId = externalLogic.getCurrentUserId();
         return externalLogic.isUserAdmin(currentUserId);
     }
 
     public boolean isCurrentUserPluginAdmin() {
         String currentUserId = externalLogic.getCurrentUserId();
         String currentLocation = externalLogic.getCurrentLocationId();
         return externalLogic.isUserAllowedInLocation(
                     currentUserId, ExternalLogic.SCORMCLOUD_ADMIN, currentLocation);
     }
     
 
     public boolean canConfigurePlugin() {
         //Sakai admins can do whatever they please...
         if(isCurrentUserSakaiAdmin()){
             return true;
         }
         String currentUserId = externalLogic.getCurrentUserId();
         String currentLocation = externalLogic.getCurrentLocationId();
         return externalLogic.isUserAllowedInLocation(
                 currentUserId, ExternalLogic.SCORMCLOUD_CONFIGURE, currentLocation);
     }
 
     public List<ScormCloudPackage> getAllSitePackages(){
         Search s = new Search();
         s.addRestriction(new Restriction("locationId", externalLogic.getCurrentLocationId()));
         return dao.findBySearch(ScormCloudPackage.class, s);
     }
 }
