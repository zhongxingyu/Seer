 package uk.org.sappho.code.change.management.data;
 
import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 public class IssueData {
 
     private String key;
     private String type;
     private String summary;
     private Date createdDate;
     private Date lastUpdatedDate;
     private String assignee;
    private String project;
     private List<String> components;
     private List<String> releases;
     private final List<String> subTaskKeys = new ArrayList<String>();
 
     public IssueData() {
     }
 
     public IssueData(String key, String type, String summary, Date createdDate, Date lastUpdatedDate, String assignee,
            String project, List<String> components, List<String> releases) {
 
         this.key = key;
         this.type = type;
         this.summary = summary;
         this.createdDate = createdDate;
         this.lastUpdatedDate = lastUpdatedDate;
         this.assignee = assignee;
        this.project = project;
         this.components = components;
         this.releases = releases;
     }
 
     public String getKey() {
 
         return key;
     }
 
     public void setType(String type) {
 
         this.type = type;
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
 
     public String getAssignee() {
 
         return assignee;
     }
 
    public String getProject() {

        return project;
    }

     public List<String> getComponents() {
 
         return components;
     }
 
     public void setReleases(List<String> releases) {
 
         this.releases = releases;
     }
 
     public List<String> getReleases() {
 
         return releases;
     }
 
     public void putSubTaskKey(String issueKey) {
 
         if (!subTaskKeys.contains(issueKey)) {
             subTaskKeys.add(issueKey);
         }
     }
 
     public List<String> getSubTaskKeys() {
 
         return subTaskKeys;
     }
 }
