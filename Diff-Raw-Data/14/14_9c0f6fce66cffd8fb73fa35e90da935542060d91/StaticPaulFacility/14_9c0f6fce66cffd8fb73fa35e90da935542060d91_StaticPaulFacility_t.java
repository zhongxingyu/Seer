 package au.edu.uq.cmm.paul;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import au.edu.uq.cmm.aclslib.config.StaticFacilityConfig;
 
 public class StaticPaulFacility extends StaticFacilityConfig implements
         GrabberFacilityConfig {
     
    private boolean caseInsensitive;
     private boolean useFileLocks;
     private int fileSettlingTime;
     private List<StaticDatafileTemplateConfig> datafileTemplates =
             new ArrayList<StaticDatafileTemplateConfig>();
 
     @Override
     public List<? extends DatafileTemplateConfig> getDatafileTemplates() {
         return datafileTemplates;
     }
 
     @Override
     public boolean isCaseInsensitive() {
        return caseInsensitive;
     }
 
     @Override
     public boolean isUseFileLocks() {
         return useFileLocks;
     }
 
     @Override
     public int getFileSettlingTime() {
         return fileSettlingTime;
     }
 
    public void setCaseInsensitive(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
     }
 
     public void setUseFileLocks(boolean useFileLocks) {
         this.useFileLocks = useFileLocks;
     }
 
     public void setFileSettlingTime(int fileSettlingTime) {
         this.fileSettlingTime = fileSettlingTime;
     }
 
     public void setDatafileTemplates(
             List<StaticDatafileTemplateConfig> datafileTemplates) {
         this.datafileTemplates = datafileTemplates;
     }
 }
