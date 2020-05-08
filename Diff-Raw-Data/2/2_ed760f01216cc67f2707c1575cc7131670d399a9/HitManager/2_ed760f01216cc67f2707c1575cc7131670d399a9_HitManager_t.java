 package edu.mit.cci.amtprojects;
 
 import com.amazonaws.mturk.requester.Assignment;
 import com.amazonaws.mturk.requester.AssignmentStatus;
 import com.amazonaws.mturk.requester.GetAssignmentResult;
 import com.amazonaws.mturk.requester.HIT;
 import com.amazonaws.mturk.requester.QualificationRequirement;
 import com.amazonaws.mturk.requester.QualificationType;
 import com.amazonaws.mturk.requester.QualificationTypeStatus;
 import com.amazonaws.mturk.requester.SearchQualificationTypesResult;
 import com.amazonaws.mturk.requester.SortDirection;
 import com.amazonaws.mturk.service.axis.RequesterService;
 import com.amazonaws.mturk.service.exception.ServiceException;
 import com.amazonaws.mturk.util.ClientConfig;
 import edu.mit.cci.amtprojects.kickball.cayenne.Batch;
 import edu.mit.cci.amtprojects.kickball.cayenne.Hits;
 import edu.mit.cci.amtprojects.kickball.cayenne.TurkerLog;
 import edu.mit.cci.amtprojects.util.CayenneUtils;
 import edu.mit.cci.amtprojects.util.FilePropertiesConfig;
 import edu.mit.cci.amtprojects.util.MturkUtils;
 import edu.mit.cci.amtprojects.util.Utils;
 import org.apache.cayenne.DataObjectUtils;
 import org.apache.log4j.Logger;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * User: jintrone
  * Date: 9/26/12
  * Time: 3:27 PM
  */
 public class HitManager {
     private static final long serialVersionUID = 1L;
 
     private static Logger log = Logger.getLogger(HitManager.class);
 
 
     private RequesterService requesterService;
    Set<TurkerLog> newHitResults = new HashSet<TurkerLog>();
     boolean isModified = false;
 
     private Long batch;
 
     boolean autoApprove = true;
 
 
     private static Map<Batch, HitManager> managerMap = new HashMap<Batch, HitManager>();
 
 
     public static HitManager get(Batch batch) {
         HitManager manager = managerMap.get(batch);
         if (manager == null) {
             managerMap.put(batch, manager = new HitManager(batch));
         }
         return manager;
     }
 
 
     private HitManager(Batch batch) {
         setBatch(batch);
     }
 
 
     private void setBatch(Batch batch) {
         this.batch = batch.getId();
         ClientConfig config;
         try {
             config = new FilePropertiesConfig(getClass().getResourceAsStream("/global.mturk.properties"));
         } catch (IOException e) {
             log.error("Could not read global properties file: global.mturk.properties");
             config = new ClientConfig();
         }
 
         config.setAccessKeyId(batch.getAwsId());
         config.setSecretAccessKey(batch.getAwsSecret());
 
         if (batch.getIsReal()) {
             config.setServiceURL(ClientConfig.PRODUCTION_SERVICE_URL);
 
         } else {
             config.setServiceURL(ClientConfig.SANDBOX_SERVICE_URL);
 
         }
 
         requesterService = new RequesterService(config);
         updateHits();
     }
 
 
     public boolean isModified() {
         return isModified;
     }
 
 
     public  void launch(String url, int height, DefaultEnabledHitProperties props) {
         launch(url,height,batch().getAutoApprove(),props);
 
     }
 
     public synchronized void launch(String url, int height, boolean autoApprove, DefaultEnabledHitProperties props) {
        launch(url,height,autoApprove,null,props);
     }
 
     public synchronized void launch(String url, int height, boolean autoApprove, List<String> screenWorkers, DefaultEnabledHitProperties props) {
         String annotation = getAnnotation("batchId", batch + "", props.getAnnotation(null));
         long lifetime = props.getLifetime(60 * 60 * 15);
         HIT h = requesterService.createHIT(
                 null, // hitTypeId
                 props.getTitle("No title"),
                 props.getDescription("No description"),
                 props.getKeywords(null), // keywords
                 MturkUtils.getExternalQuestion(url, height),
                 props.getRewardAmount(0),
                 props.getAssignmentDuration(60 * 5),
                 props.getAutoApprovalDelay(60 * 30),
                 lifetime,
                 props.getMaxAssignments(1),
                 annotation, // requesterAnnotation
                 props.getQualificationRequirements(new QualificationRequirement[0]), // qualificationRequirements
                 new String[]{"Minimal", "HITDetail", "HITQuestion", "HITAssignmentSummary"}, // responseGroup
                 null, // uniqueRequestToken
                 null, // assignmentReviewPolicy
                 null); // hitReviewPolicy
 
 
         Hits hit = CayenneUtils.createHit(DbProvider.getContext(), h, null, batch(), url, lifetime,autoApprove);
         if (screenWorkers!=null && !screenWorkers.isEmpty()) {
             hit.setScreen(Utils.join(screenWorkers, ";"));
         }
         hit.setHitTypeId(h.getHITTypeId());
         DbProvider.getContext().commitChanges();
 
 
         CayenneUtils.logEvent(DbProvider.getContext(), batch(), "LAUNCH", null, h.getHITId(), null, null, Collections.<String, Object>emptyMap());
 
 
     }
 
     public synchronized void reLaunch(String oldhit) {
         Hits hits = DataObjectUtils.objectForPK(DbProvider.getContext(), Hits.class, oldhit);
         if (hits == null) {
             log.error("No local record of hit; not relaunching (" + oldhit + ")");
             return;
         }
         if (hits.getStatusEnum() == Hits.Status.COMPLETE) {
             log.error("Hit is already complete");
             return;
         } else if (hits.getStatusEnum() == Hits.Status.MISSING) {
             log.error("Hit is missing");
         }
 
         HIT h = null;
         try {
             h = requesterService.getHIT(oldhit);
         } catch (ServiceException ex) {
             ex.printStackTrace();
             log.error("Error retrieving hit from service: " + oldhit);
             hits.setStatus(Hits.Status.MISSING.name());
             DbProvider.getContext().commitChanges();
             return;
         }
 
         if (h.getMaxAssignments().intValue() == h.getNumberOfAssignmentsCompleted()) {
             hits.setCompleted(h.getNumberOfAssignmentsCompleted());
             hits.setStatus(Hits.Status.COMPLETE.name());
             DbProvider.getContext().commitChanges();
             log.warn("Can only relaunch hits that have pending assignments; extend HIT before relaunching");
             return;
         }
 
 
         try {
             requesterService.forceExpireHIT(oldhit);
         } catch (ServiceException ex) {
             ex.printStackTrace();
 
             log.error("Could not delete hit, not relaunching " + oldhit);
             return;
         }
 
 
 
 
         HIT nhit = requesterService.createHIT(
                 null,
                 h.getTitle(),
                 h.getDescription(),
                 h.getKeywords(),
                 h.getQuestion(),
                 h.getReward().getAmount().doubleValue(),
                 h.getAssignmentDurationInSeconds(),
                 h.getAutoApprovalDelayInSeconds(),
                 hits.getLifetime(),
                 h.getMaxAssignments() - hits.getCompleted(),
                 h.getRequesterAnnotation(),
                 h.getQualificationRequirement(),
                 new String[]{"Minimal", "HITDetail", "HITQuestion", "HITAssignmentSummary"},
                 null,
                 null,
                 null);
 
         hits.setStatus(Hits.Status.RELAUNCHED.name());
         List<String> workers = new ArrayList<String>();
         for (TurkerLog l : hits.getLogs()) {
             if (l.getType().equals("RESULTS")) {
                 workers.add(l.getWorkerId());
             }
 
         }
 
         workers.add(hits.getScreen());
         String screen = Utils.join(workers, ";");
 
         Hits nhits = CayenneUtils.createHit(DbProvider.getContext(), nhit, oldhit, batch(), hits.getUrl(), hits.getLifetime(),hits.getAutoApprove());
         nhits.setScreen(screen);
         nhits.setHitTypeId(h.getHITTypeId());
         DbProvider.getContext().commitChanges();
 
         CayenneUtils.logEvent(DbProvider.getContext(), batch(), "LAUNCH", null, nhit.getHITId(), null, null, Collections.<String, Object>emptyMap());
     }
 
 
     private Batch batch() {
         return CayenneUtils.findBatch(DbProvider.getContext(), batch);
     }
 
 
     public List<TurkerLog> getNewHitResults(boolean clear) {
         List<TurkerLog> results = new ArrayList<TurkerLog>(newHitResults);
         if (clear) newHitResults.clear();
         return results;
     }
 
 
     public synchronized void approveAssignments(String[] ids, String feedback) {
         for (String id : ids) {
             GetAssignmentResult result = requesterService.getAssignment(id);
             if (result.getAssignment() != null) {
                 Assignment a = result.getAssignment();
                 if (a.getAssignmentStatus().equals(AssignmentStatus.Submitted)) {
                     requesterService.approveAssignment(a.getAssignmentId(), feedback);
                     CayenneUtils.logEvent(DbProvider.getContext(), batch(), "APPROVED", a.getWorkerId(), a.getHITId(), a.getAssignmentId(), null, Collections.<String, Object>singletonMap("response", feedback));
 
                 }
             }
         }
     }
 
     public synchronized void rejectAssignments(String[] ids, String feedback) {
         for (String id : ids) {
             GetAssignmentResult result = requesterService.getAssignment(id);
             if (result.getAssignment() != null) {
                 Assignment a = result.getAssignment();
                 if (a.getAssignmentStatus().equals(AssignmentStatus.Submitted)) {
                     requesterService.rejectAssignment(a.getAssignmentId(), feedback);
                     CayenneUtils.logEvent(DbProvider.getContext(), batch(), "REJECTED", a.getWorkerId(), a.getHITId(), a.getAssignmentId(), null, Collections.<String, Object>singletonMap("response", feedback));
 
                 }
             }
         }
     }
 
     public void bonusAssignments(String[] ids, String feedback, double amount) {
         for (String id : ids) {
             List<TurkerLog> l = CayenneUtils.getTurkerLogForAssignment(DbProvider.getContext(), id, "BONUSED");
             if (l != null && !l.isEmpty()) {
                 log.warn("Already bonused worker for this assignment. Refusing to do it again!");
                 continue;
             }
 
 
             GetAssignmentResult result = requesterService.getAssignment(id);
             if (result.getAssignment() != null) {
                 Assignment a = result.getAssignment();
 
 
                 double bonusAmount = Double.parseDouble(String.format("%.2f", amount));
                 if (bonusAmount < .01) {
                     log.warn("Invalid bonus amount; not bonusing");
                 }
                 requesterService.grantBonus(a.getWorkerId(), bonusAmount, a.getAssignmentId(), feedback);
                 CayenneUtils.logEvent(DbProvider.getContext(), batch(), "BONUSED", a.getWorkerId(), a.getHITId(), a.getAssignmentId(), null, Utils.mapify("response", feedback, "amount", String.format("%.2f", amount)));
 
 
             }
         }
     }
 
     protected String getAnnotation(String key, String value, String data) {
 
         if (!data.contains(key + "=")) {
             data = key + "=" + value + ";" + data;
         }
         return data;
     }
 
     /**
      * Check if there are any incomplete or unprocessed hits
      * If new results are found, increment count, and approve if auto approval is set
      * If all results are retrieved, set hit status to complete and store these results in the "new results" set
      * If hit expiry time is past, expire hit, otherwise make sure it's listed as still open
      */
     public synchronized void updateHits() {
         List<Hits> hitses = batch().getHits();
 
         for (Hits h : hitses) {
             if (h.getStatusEnum() == Hits.Status.MISSING || h.getStatusEnum() == Hits.Status.RELAUNCHED || (h.getStatusEnum() == Hits.Status.COMPLETE && h.getProcessed()))
                 continue;
             HIT ahit = requesterService.getHIT(h.getId());
             h.setAmtStatus(ahit.getHITStatus().getValue());
             if (ahit.getHITTypeId().equals(h.getHitTypeId())) h.setHitTypeId(ahit.getHITTypeId());
             int completed = ahit.getMaxAssignments() - ahit.getNumberOfAssignmentsAvailable();
             if (completed > h.getCompleted()) {
                 completed = 0;
                 Assignment[] result = requesterService.getAllAssignmentsForHIT(ahit.getHITId());
                 List<TurkerLog> results = new ArrayList<TurkerLog>();
                 for (Assignment a : result) {
                     if (a.getSubmitTime() == null) {
                         log.info("Got null submit time??");
                         continue;
                     }
                     if (CayenneUtils.getTurkerLogForAssignment(DbProvider.getContext(), a.getAssignmentId(), "RESULTS").isEmpty()) {
                         results.add(CayenneUtils.logEvent(DbProvider.getContext(), batch(), "RESULTS", a.getWorkerId(), ahit.getHITId(), a.getAssignmentId(), null, Collections.<String, Object>singletonMap("answer", a.getAnswer())));
                     }
                     completed++;
                     if (a.getAssignmentStatus() == AssignmentStatus.Approved) {
                         if (CayenneUtils.getTurkerLogForAssignment(DbProvider.getContext(), a.getAssignmentId(), "APPROVED").isEmpty()) {
 
                             CayenneUtils.logEvent(DbProvider.getContext(), batch(), "APPROVED", a.getWorkerId(), ahit.getHITId(), a.getAssignmentId(), null, Collections.<String, Object>emptyMap());
                         }
                     } else if (a.getAssignmentStatus() == AssignmentStatus.Rejected) {
                         if (CayenneUtils.getTurkerLogForAssignment(DbProvider.getContext(), a.getAssignmentId(), "REJECTED").isEmpty()) {
                             CayenneUtils.logEvent(DbProvider.getContext(), batch(), "REJECTED", a.getWorkerId(), ahit.getHITId(), a.getAssignmentId(), null, Collections.<String, Object>emptyMap());
                         }
                     } else if (h.getAutoApprove()) {
                         requesterService.approveAssignment(a.getAssignmentId(), "Thanks!");
                         CayenneUtils.logEvent(DbProvider.getContext(), batch(), "APPROVED", a.getWorkerId(), ahit.getHITId(), a.getAssignmentId(), null, Collections.<String, Object>singletonMap("feedback", "Thanks!"));
                     }
 
 
                 }
 
                 h.setCompleted(completed);
 
 
                 if (h.getCompleted().intValue() == h.getRequested()) {
                     h.setStatus(Hits.Status.COMPLETE.name());
                 }
             }
 
 
             if (h.getStatus().equals(Hits.Status.COMPLETE.name())) {
                 Hits loop = h;
 
                 while (loop != null) {
 
                     for (TurkerLog l : loop.getLogs()) {
                         if (l.getType().equals("RESULTS")) {
                             newHitResults.add(l);
                         }
                     }
                     loop = loop.getPreviousHit();
 
                 }
 
                 if (ahit.getExpiration().before(new GregorianCalendar())) {
                     h.setStatus(Hits.Status.HALTED.name());
                 } else if (h.getStatusEnum() == Hits.Status.HALTED) {
                     h.setStatus(Hits.Status.OPEN.name());
                 }
             }
 
             DbProvider.getContext().commitChanges();
         }
     }
 
 
     public void extendHits(Collection<String> hits, long duration) {
         try {
             requesterService.extendHITs(hits.toArray(new String[hits.size()]), null, duration, null);
         } catch (ServiceException ex) {
             ex.printStackTrace();
             log.error("Couldn't extend hits " + hits);
         }
     }
 
 
     public void extendBatch(Batch b, long duration) {
         updateHits();
         List<String> hits = new ArrayList<String>();
         for (Hits h : b.getHits()) {
             if (h.getStatusEnum() == Hits.Status.OPEN || h.getStatusEnum() == Hits.Status.HALTED) {
                 hits.add(h.getId());
             }
         }
 
         extendHits(hits, duration);
     }
 
     public void expireHits(List<String> hits) {
         for (String h : hits) {
             requesterService.forceExpireHIT(h);
         }
     }
 
     public void expireBatch() {
         List<String> hitids = new ArrayList<String>();
         for (Hits h : batch().getHits()) {
             if (h.getStatus().equals(Hits.Status.OPEN.name())) {
                 hitids.add(h.getId());
             }
         }
         expireHits(hitids);
     }
 
 
     //maybe go somewhere else?
     public QualificationType findQualificationNamed(String name) {
         SearchQualificationTypesResult result = requesterService.searchQualificationTypes(name, false, true, SortDirection.Ascending, null, null, null);
         if (result.getTotalNumResults() == 0) {
             return null;
         }
         for (QualificationType type : result.getQualificationType()) {
             if (type.getName().equalsIgnoreCase(name)) {
                 return type;
             }
         }
         return null;
 
     }
 
     public QualificationType createAssignableQualificationType(String name, String keywords, String description) {
         return requesterService.createQualificationType(name, keywords, description, QualificationTypeStatus.Active, null, null, null, null, false, null);
         //return requesterService.createQualificationType("Forum Analysis Qualification","forum,MIT,conversation","Allows you to work on HITs for the requester that require you to be able follow the conversation in a web forum");
     }
 
 }
