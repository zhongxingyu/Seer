 package org.vamdc.portal.registry;
 
 import java.util.Date;
 
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
 import org.jboss.seam.annotations.Scope;
 
 @Name("progressbarbean")
 @Scope(ScopeType.CONVERSATION)
 public class ProgressBarBean {
 	@In("#{nodespecies}") NodeSpecies nodeSpecies;
 	
     private boolean started = false;
     private Long startTime;
     
     /**
      * Bean containing results
      */
     private SpeciesResult speciesResult = new SpeciesResult();
     
     /**
      * Duration in seconds before timeout
      */
     private Long maxValue = 30L;
     
     public ProgressBarBean() {
     }
     
     public Long getMaxValue(){
     	return this.maxValue;
     }
     
    public String startProcess() {
         setStartTime(new Date().getTime());
         nodeSpecies.querySpecies(nodeSpecies.getIvoaId(), speciesResult);
        return null;
     }
 
     public Long getCurrentValue(){
    	if(started == false){
     		this.startProcess();
     		started = true;    		
     	}
     	
     	if(speciesResult.isReady()){
     		return this.maxValue+1;
     	}
     	
        if (started){
             return (new Date().getTime() - startTime)/1000;
        } if (startTime == null) {
             return Long.valueOf(-1);
         }
        else
            return Long.valueOf(this.maxValue+1);
     }
     
     public String getMessage(){
     	return speciesResult.getMessage();
     }
     
     public String getFormattedResult(){
     	return speciesResult.getFormattedResult();
     }
 
     public Long getStartTime() {
         return startTime;
     }
 
     public void setStartTime(Long startTime) {
         this.startTime = startTime;
     }
 }
