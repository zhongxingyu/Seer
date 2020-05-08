 package ch.lepeit.brain2;
 
 public class VersionInfo {
 
    public String getStageName() {
         return stageKurzName;
     }
 
    public void setStageName(String stageName) {
         this.stageKurzName = stageName;
     }
 
     public String getArtefaktBudleId() {
         return artefaktBudleId;
     }
 
     public void setArtefaktBudleId(String artefaktBudleId) {
         this.artefaktBudleId = artefaktBudleId;
     }
 
     public String getServerUrl() {
         return serverUrl;
     }
 
     public void setServerUrl(String serverUrl) {
         this.serverUrl = serverUrl;
     }
 
     String stageKurzName;
 
     String artefaktBudleId;
 
     String serverUrl;
 
 }
