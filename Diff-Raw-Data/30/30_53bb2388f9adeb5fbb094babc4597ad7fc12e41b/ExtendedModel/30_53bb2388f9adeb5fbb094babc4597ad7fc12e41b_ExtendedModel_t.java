 package com.atex.confluence.plugin.nexus;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import org.apache.maven.model.Model;
 
 /**
  *
  * @author pau
  */
 public class ExtendedModel extends Model{
     
     /**
      * 
      */
     private static final long serialVersionUID = 6495594790600528473L;
     private List<Artifact> artifacts = new ArrayList<Artifact>();
 
     public ExtendedModel(Model model, List<Artifact> artifacts) {
         setParent(model.getParent());
         setPackaging(model.getPackaging());
         
         setArtifactId(model.getArtifactId());
         setGroupId(model.getGroupId());
         setVersion(model.getVersion());
         
         setDependencies(model.getDependencies());
         setBuild(model.getBuild());
         setCiManagement(model.getCiManagement());
         setScm(model.getScm());
         setContributors(model.getContributors());
         setDependencyManagement(model.getDependencyManagement());
         setDescription(model.getDescription());
         setDevelopers(model.getDevelopers());
         setDistributionManagement(model.getDistributionManagement());
         setInceptionYear(model.getInceptionYear());
         setIssueManagement(model.getIssueManagement());
         setLicenses(model.getLicenses());
         setMailingLists(model.getMailingLists());
         setModelEncoding(model.getModelEncoding());
         setModelVersion(model.getModelVersion());
         setModules(model.getModules());
         setName(model.getName());
         setOrganization(model.getOrganization());
         setPluginRepositories(model.getPluginRepositories());
         setPomFile(model.getPomFile());
         setPrerequisites(model.getPrerequisites());
         setProfiles(model.getProfiles());
         setProperties(model.getProperties());
         setReporting(model.getReporting());
         setReports(model.getReports());
         setRepositories(model.getRepositories());
         setUrl(model.getUrl());
         
         setArtifacts(artifacts);
         
     }
     public List<Artifact> getArtifacts() {
         artifacts = safe(artifacts);
         return Collections.unmodifiableList(artifacts);
     }
 
     public void setArtifacts(List<Artifact> artifacts) {
         artifacts = safe(artifacts);
         this.artifacts = safe(this.artifacts);
         // only add artifact in the same group
         for(Artifact artifact: artifacts) {
             if(artifact != null) {
                if(safeEquals(getArtifactId(), artifact.getArtifactId()) 
                        && safeEquals(getGroupId(), artifact.getGroupId()) 
                         && !this.artifacts.contains(artifact)) {
                     this.artifacts.add(artifact);
                 }
             }
         }
         
         Collections.sort(this.artifacts, new Comparator<Artifact>() {
             @Override
             public int compare(Artifact o1, Artifact o2) {
                 try {
                     Float v1 = Float.parseFloat(o1.getVersion());
                     Float v2 = Float.parseFloat(o2.getVersion());
                     return -v1.compareTo(v2);
                 } catch (NumberFormatException e) {
                     return 0;
                 }
             }
         });
     }
     
    private boolean safeEquals(String s1, String s2) {
        if(s1 != null && s2 != null) {
            return s1.equals(s2);
        }
        
        return false;
    }
    
     @Override
     public String getDescription() {
         return safe(super.getDescription());
     }
     
     @Override
     public boolean equals(Object obj) {
         if(obj instanceof ExtendedModel) {
             ExtendedModel another = (ExtendedModel) obj;
             return getId().equals(another.getId());
         }
         return false;
     }
 
     @Override
     public int hashCode() {
         return getId().hashCode();
     }
     
     private String safe(String value) {
         return value == null?
                 ""
                 : value;
     }
     
     private <T> List<T> safe(List<T> list) {
         if(list == null) {
             list = new ArrayList<T>();
         }
         
         return list;
     }
     
 }
