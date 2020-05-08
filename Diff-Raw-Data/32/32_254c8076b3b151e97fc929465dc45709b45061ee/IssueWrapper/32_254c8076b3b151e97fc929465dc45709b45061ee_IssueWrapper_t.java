 package uk.org.sappho.code.heatmap.issues;
 
import java.util.Set;

 public interface IssueWrapper {
 
     public String getKey();
 
     public String getSubTaskKey();
 
     public String getSummary();
 
    public Set<String> getReleases();

     public int getWeight();
 }
