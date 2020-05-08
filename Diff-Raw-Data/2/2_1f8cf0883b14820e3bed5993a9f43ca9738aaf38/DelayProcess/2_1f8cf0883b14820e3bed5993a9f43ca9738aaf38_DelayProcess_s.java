 package uk.ac.ebi.fgpt.conan.process.biosd;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Map;
 
 import uk.ac.ebi.fgpt.conan.model.ConanParameter;
 import uk.ac.ebi.fgpt.conan.model.ConanProcess;
 import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
 
 public class DelayProcess implements ConanProcess {
 
     protected final Collection<ConanParameter> parameters;
     
     public DelayProcess() {
 
         parameters = new ArrayList<ConanParameter>();
     }
     
     public boolean execute(Map<ConanParameter, String> parameters) throws ProcessExecutionException,
             IllegalArgumentException, InterruptedException {
         
         //sleep for 60 seconds
         Thread.sleep(60000);
         
         return true;
     }
 
     public String getName() {
        return "Delay";
     }
 
     public Collection<ConanParameter> getParameters() {
         return parameters;
     }
 
 }
