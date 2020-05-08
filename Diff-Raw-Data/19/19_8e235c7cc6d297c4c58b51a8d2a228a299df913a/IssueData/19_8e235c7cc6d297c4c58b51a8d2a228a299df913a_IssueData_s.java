 package uk.org.sappho.code.change.management.data;
 
 import java.util.Date;
import java.util.HashMap;
 import java.util.List;
import java.util.Map;
import java.util.Set;
 
 public class IssueData {
 
     private final String key;
     private final String type;
     private final String summary;
     private final Date createdDate;
     private final Date lastUpdatedDate;
    private final Map<String, String> subTaskKeys = new HashMap<String, String>();
     private final List<String> releases;
 
     public IssueData(String key, String type, String summary, Date createdDate, Date lastUpdatedDate,
             List<String> releases) {
 
         this.key = key;
         this.type = type;
         this.summary = summary;
         this.createdDate = createdDate;
         this.lastUpdatedDate = lastUpdatedDate;
         this.releases = releases;
     }
 
     public String getKey() {
 
         return key;
     }
 
     public String getType() {
 
         return type;
     }
 
     public String getSummary() {
 
         return summary;
     }
 
     public Date getCreatedDate() {
 
         return createdDate;
     }
 
     public Date getLastUpdatedDate() {
 
         return lastUpdatedDate;
     }
 
     public void putSubTaskKey(String issueKey) {
 
        subTaskKeys.put(issueKey, issueKey);
     }
 
    public Set<String> getSubTaskKeys() {
 
        return subTaskKeys.keySet();
     }
 
     public List<String> getReleases() {
 
         return releases;
     }
 }
