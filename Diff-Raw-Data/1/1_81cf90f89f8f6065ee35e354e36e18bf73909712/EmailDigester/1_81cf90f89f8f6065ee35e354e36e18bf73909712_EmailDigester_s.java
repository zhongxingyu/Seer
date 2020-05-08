 package module.workingCapital.domain;
 
import module.mission.domain.EmailDigesterUtil;
 import pt.ist.fenixWebFramework.services.Service;
 
 public class EmailDigester extends EmailDigester_Base {
     
     public EmailDigester() {
         super();
     }
 
     @Override
     @Service
     public void executeTask() {
 	EmailDigesterUtil.executeTask();
     }
 
     @Override
     public String getLocalizedName() {
 	return getClass().getName();
     }
 
 }
