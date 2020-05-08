 package edu.cci.amtprojects;
 
 import com.amazonaws.mturk.requester.Assignment;
 import com.amazonaws.mturk.requester.AssignmentStatus;
 import com.amazonaws.mturk.requester.GetAssignmentResult;
 import com.amazonaws.mturk.requester.HIT;
 import com.amazonaws.mturk.requester.HITStatus;
 import com.amazonaws.mturk.requester.QualificationRequirement;
 import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.service.exception.ServiceException;
 import com.amazonaws.mturk.util.ClientConfig;
 import edu.mit.cci.amtprojects.DbProvider;
 import edu.mit.cci.amtprojects.kickball.cayenne.Batch;
 import edu.mit.cci.amtprojects.kickball.cayenne.TurkerLog;
 import edu.mit.cci.amtprojects.util.CayenneUtils;
 import edu.mit.cci.amtprojects.util.FilePropertiesConfig;
 import edu.mit.cci.amtprojects.util.MturkUtils;
 import edu.mit.cci.amtprojects.util.Utils;
 import org.apache.log4j.Logger;
 import org.apache.wicket.ajax.json.JSONObject;
 
 import java.io.IOException;
 import java.util.*;
 
 /**
  * User: jintrone
  * Date: 9/26/12
  * Time: 3:27 PM
  */
 public class HitManager {
     private static final long serialVersionUID = 1L;
 
     private static Logger log = Logger.getLogger(HitManager.class);
 
 
     private RequesterService requesterService;
     List<HIT> hits = new ArrayList<HIT>();
     boolean isModified = false;
 
     private Long batch;
 
     private static Map<Batch, HitManager> managerMap = new HashMap<Batch, HitManager>();
 
 
     public HitManager(Batch batch) {
         setBatch(batch);
 
 
     }
 
     public void setBatch(Batch batch) {
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
 
 
     public void launch(String url, int height, DefaultEnabledHitProperties props) {
         String annotation = getAnnotation("batchId",batch+"",props.getAnnotation(null));
         HIT h = requesterService.createHIT(
                 null, // hitTypeId
                 props.getTitle("No title"),
                 props.getDescription("No description"),
                 props.getKeywords(null), // keywords
                 MturkUtils.getExternalQuestion(url, height),
                 props.getRewardAmount(0),
                 props.getAssignmentDuration(60 * 5),
                 props.getAutoApprovalDelay(60 * 30),
                 props.getLifetime(60 * 60 * 15),
                 props.getMaxAssignments(1),
                 annotation, // requesterAnnotation
                 props.getQualificationRequirements(new QualificationRequirement[0]), // qualificationRequirements
                 new String[]{"Minimal", "HITDetail", "HITQuestion", "HITAssignmentSummary"}, // responseGroup
                 null, // uniqueRequestToken
                 null, // assignmentReviewPolicy
                 null); // hitReviewPolicy
 
 
         CayenneUtils.logEvent(DbProvider.getContext(), batch(), "LAUNCH", null, h.getHITId(), null, url, Collections.singletonMap("properties", (Object) props.toJSONString()));
 
 
     }
 
      public void launch(String url, int height, DefaultEnabledHitProperties props, String oldhit) {
 
          String annotation = getAnnotation("batchId",batch+"",props.getAnnotation(null));
          annotation = getAnnotation("previousHit",oldhit,annotation);
 
         HIT h = requesterService.createHIT(
                 null, // hitTypeId
                 props.getTitle("No title"),
                 props.getDescription("No description"),
                 props.getKeywords(null), // keywords
                 MturkUtils.getExternalQuestion(url, height),
                 props.getRewardAmount(0),
                 props.getAssignmentDuration(60 * 5),
                 props.getAutoApprovalDelay(60 * 30),
                 props.getLifetime(60 * 60 * 15),
                 props.getMaxAssignments(1),
                 annotation, // requesterAnnotation
                 props.getQualificationRequirements(new QualificationRequirement[0]), // qualificationRequirements
                 new String[]{"Minimal", "HITDetail", "HITQuestion", "HITAssignmentSummary"}, // responseGroup
                 null, // uniqueRequestToken
                 null, // assignmentReviewPolicy
                 null); // hitReviewPolicy
 
 
         CayenneUtils.logEvent(DbProvider.getContext(), batch(), "LAUNCH", null, h.getHITId(), null, url, Collections.singletonMap("properties", (Object) props.toJSONString()));
 
 
     }
 
 
     public List<HIT> getAllHits() {
 
         return hits;
 
     }
 
     private Batch batch() {
         return CayenneUtils.findBatch(DbProvider.getContext(), batch);
     }
 
     public void extendByTime(Collection<String> hits, long duration) {
        try {
            requesterService.extendHITs(hits.toArray(new String[hits.size()]), null, duration, null);
        } catch (ServiceException ex) {
            ex.printStackTrace();
            log.error("Couldn't extend hits "+hits);
        }
     }
 

     public void extendBatch(Batch b, long duration) {
         populateResults(false);
         List<TurkerLog> logs = b.getToLogs();
         Set<String> hits = new HashSet<String>();
         for (TurkerLog l : logs) {
             if (l.getType().equals("RESULTS") && Utils.getJsonInt(l.getData(), "remaining") > 0) {
                 hits.add(l.getHit());
             }
         }
         extendByTime(hits,duration);
     }
 
     public List<TurkerLog> getFilteredLogs(boolean allowIncomplete, boolean forceNonReviewable, String... type) {
         populateResults(forceNonReviewable);
         Batch b = CayenneUtils.findBatch(DbProvider.getContext(), batch);
         List<TurkerLog> logs = new ArrayList<TurkerLog>(b.getToLogs());
         List<String> accept = Arrays.asList(type);
         if (accept == null || accept.isEmpty()) return logs;
         for (Iterator<TurkerLog> i = logs.iterator(); i.hasNext(); ) {
             TurkerLog log = i.next();
             if (!accept.contains(log.getType())) {
                 i.remove();
 
             } else {
                 Integer remaining = Utils.getJsonInt(log.getData(), "remaining");
                 if (remaining != null && remaining > 0 && !allowIncomplete) {
                     i.remove();
                 }
             }
 
         }
         return logs;
 
     }
 
     public void populateResults(boolean force) {
         updateHits();
         for (HIT h : getAllHits()) {
             //only process completed hits
             //TODO do a better job with this, so that clients can choose completed hits or not
             if (!force) {
                 if (h.getHITStatus() != HITStatus.Reviewable) continue;
 
             }
             int remaining = (h.getMaxAssignments() - h.getNumberOfAssignmentsCompleted());
             Assignment[] assignments = requesterService.getAllAssignmentsForHIT(h.getHITId());
             for (Assignment a : assignments) {
                 List<TurkerLog> logs = CayenneUtils.getTurkerLogForAssignment(DbProvider.getContext(), a.getAssignmentId(), "RESULTS");
                 if (logs == null || logs.isEmpty()) {
                     TurkerLog nlog = DbProvider.getContext().newObject(TurkerLog.class);
                     nlog.setAssignmentId(a.getAssignmentId());
                     nlog.setHit(a.getHITId());
                     nlog.setDate(a.getSubmitTime().getTime());
                     nlog.setToBatch(CayenneUtils.findBatch(DbProvider.getContext(), batch));
                     nlog.setType("RESULTS");
                     nlog.setWorkerId(a.getWorkerId());
                     Map<String, String> result = new HashMap<String, String>();
                     result.put("answer", a.getAnswer());
                     result.put("remaining", remaining + "");
                     JSONObject obj = new JSONObject(result);
                     nlog.setData(obj.toString());
 
                 } else {
                     for (TurkerLog log : logs) {
                         log.setData(Utils.updateJSONProperty(log.getData(), "remaining", remaining + ""));
                     }
                 }
                 if (a.getAssignmentStatus().equals(AssignmentStatus.Approved)) {
                     logs = CayenneUtils.getTurkerLogForAssignment(DbProvider.getContext(), a.getAssignmentId(), "APPROVED");
                     if (logs == null || logs.isEmpty()) {
                         TurkerLog nlog = DbProvider.getContext().newObject(TurkerLog.class);
                         nlog.setAssignmentId(a.getAssignmentId());
                         nlog.setHit(a.getHITId());
                         nlog.setDate(a.getSubmitTime().getTime());
                         nlog.setToBatch(CayenneUtils.findBatch(DbProvider.getContext(), batch));
                         nlog.setType("APPROVED");
                         nlog.setWorkerId(a.getWorkerId());
                         Map<String, String> result = new HashMap<String, String>();
                         result.put("answer", a.getAnswer());
                         result.put("response", a.getRequesterFeedback());
                         result.put("remaining", remaining + "");
                         JSONObject obj = new JSONObject(result);
                         nlog.setData(obj.toString());
 
                     }
                 } else if (a.getAssignmentStatus().equals(AssignmentStatus.Rejected)) {
                     logs = CayenneUtils.getTurkerLogForAssignment(DbProvider.getContext(), a.getAssignmentId(), "REJECTED");
                     if (logs == null || logs.isEmpty()) {
                         TurkerLog nlog = DbProvider.getContext().newObject(TurkerLog.class);
                         nlog.setAssignmentId(a.getAssignmentId());
                         nlog.setHit(a.getHITId());
                         nlog.setDate(a.getApprovalTime().getTime());
                         nlog.setToBatch(CayenneUtils.findBatch(DbProvider.getContext(), batch));
                         nlog.setType("REJECTED");
                         nlog.setWorkerId(a.getWorkerId());
                         Map<String, String> result = new HashMap<String, String>();
                         result.put("answer", a.getAnswer());
                         result.put("response", a.getRequesterFeedback());
                         result.put("remaining", remaining + "");
                         JSONObject obj = new JSONObject(result);
                         nlog.setData(obj.toString());
                     }
 
                 }
 
                 DbProvider.getContext().commitChanges();
 
 
             }
         }
     }
 
     public void approveAssignments(String[] ids, String feedback) {
         for (String id : ids) {
             GetAssignmentResult result = requesterService.getAssignment(id);
             if (result.getAssignment() != null) {
                 Assignment a = result.getAssignment();
                 if (a.getAssignmentStatus().equals(AssignmentStatus.Submitted)) {
                     requesterService.approveAssignment(a.getAssignmentId(), feedback);
                     TurkerLog nlog = DbProvider.getContext().newObject(TurkerLog.class);
                     nlog.setAssignmentId(a.getAssignmentId());
                     nlog.setHit(a.getHITId());
                     nlog.setDate(new Date());
                     nlog.setToBatch(CayenneUtils.findBatch(DbProvider.getContext(), batch));
                     nlog.setType("APPROVED");
                     nlog.setWorkerId(a.getWorkerId());
                     Map<String, String> data = new HashMap<String, String>();
                     data.put("response", feedback);
                     JSONObject obj = new JSONObject(data);
                     nlog.setData(obj.toString());
                     DbProvider.getContext().commitChanges();
                 }
             }
         }
     }
 
     public void rejectAssignments(String[] ids, String feedback) {
         for (String id : ids) {
             GetAssignmentResult result = requesterService.getAssignment(id);
             if (result.getAssignment() != null) {
                 Assignment a = result.getAssignment();
                 if (a.getAssignmentStatus().equals(AssignmentStatus.Submitted)) {
                     requesterService.rejectAssignment(a.getAssignmentId(), feedback);
                     TurkerLog nlog = DbProvider.getContext().newObject(TurkerLog.class);
                     nlog.setAssignmentId(a.getAssignmentId());
                     nlog.setHit(a.getHITId());
                     nlog.setDate(new Date());
                     nlog.setToBatch(CayenneUtils.findBatch(DbProvider.getContext(), batch));
                     nlog.setType("REJECTED");
                     nlog.setWorkerId(a.getWorkerId());
                     Map<String, String> data = new HashMap<String, String>();
                     data.put("response", feedback);
                     JSONObject obj = new JSONObject(data);
                     nlog.setData(obj.toString());
                     DbProvider.getContext().commitChanges();
                 }
             }
         }
     }
 
     public void bonusAssignments(String[] ids, String feedback, double amount) {
         for (String id : ids) {
            List<TurkerLog> l =  CayenneUtils.getTurkerLogForAssignment(DbProvider.getContext(), id, "BONUSED");
             if (l !=null) {
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
                 TurkerLog nlog = DbProvider.getContext().newObject(TurkerLog.class);
                 nlog.setAssignmentId(a.getAssignmentId());
                 nlog.setHit(a.getHITId());
                 nlog.setDate(new Date());
                 nlog.setToBatch(CayenneUtils.findBatch(DbProvider.getContext(), batch));
                 nlog.setType("BONUSED");
                 nlog.setWorkerId(a.getWorkerId());
                 Map<String, String> data = new HashMap<String, String>();
                 data.put("response", feedback);
                 data.put("amount", String.format("%.2f", amount));
                 JSONObject obj = new JSONObject(data);
                 nlog.setData(obj.toString());
                 DbProvider.getContext().commitChanges();
 
             }
         }
     }
 
     protected String getAnnotation(String key, String value, String data) {
 
         if (!data.contains(key+"=")) {
             data = key+"=" + value + ";" + data;
         }
         return data;
     }
 
 
     public void updateHits() {
 
         List<HIT> hits = new ArrayList<HIT>(Arrays.asList(requesterService.searchAllHITs()));
         for (Iterator<HIT> hitsI = hits.iterator(); hitsI.hasNext(); ) {
             HIT h = hitsI.next();
 
             if (!Long.valueOf(MturkUtils.parseBatchId(h)).equals(batch)) {
                 hitsI.remove();
             }
         }
         isModified = !(hits.size() == this.hits.size() && hits.containsAll(this.hits));
         if (isModified) {
             this.hits = hits;
 
         }
 
     }
 
     public static HitManager get(Batch batch) {
         HitManager manager = managerMap.get(batch);
         if (manager == null) {
             managerMap.put(batch, manager = new HitManager(batch));
         }
         return manager;
     }
 
     public void expireHits(List<String> hits) {
         for (String h:hits) {
             requesterService.forceExpireHIT(h);
         }
     }
 
     public void expireBatch() {
         List<String> hits = new ArrayList<String>();
         for (TurkerLog log:getFilteredLogs(true,false,"LAUNCH")) {
             hits.add(log.getHit());
         }
         expireHits(hits);
     }
 }
