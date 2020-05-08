 package uk.ac.ebi.fgpt.conan.process.ae2;
 
 import net.sourceforge.fluxion.spi.ServiceProvider;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import uk.ac.ebi.arrayexpress2.exception.ComponentNames;
 import uk.ac.ebi.fgpt.conan.ae.AccessionParameter;
 import uk.ac.ebi.fgpt.conan.lsf.AbstractLSFProcess;
 import uk.ac.ebi.fgpt.conan.model.ConanParameter;
 import uk.ac.ebi.fgpt.conan.properties.ConanProperties;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Map;
 
 /**
  * Process that should be run after each unload to perform required cleanup of load directories and FTP directories,
  * ready for "clean" reload.
  *
  * @author Tony Burdett
  * @date 19/04/11
  */
 @ServiceProvider
 public class UnloadCleanupProcess extends AbstractAE2LSFProcess {
     private final Collection<ConanParameter> parameters;
     private final AccessionParameter accessionParameter;
 
     private Logger log = LoggerFactory.getLogger(getClass());
 
     public UnloadCleanupProcess() {
         parameters = new ArrayList<ConanParameter>();
         accessionParameter = new AccessionParameter();
         parameters.add(accessionParameter);
     }
 
     protected Logger getLog() {
         return log;
     }
 
     public String getName() {
         return "unload cleanup";
     }
 
     public Collection<ConanParameter> getParameters() {
         return parameters;
     }
 
     protected int getMemoryRequirement(Map<ConanParameter, String> parameterStringMap) {
         return 128;
     }
 
     protected String getComponentName() {
         return ComponentNames.EXPLOADER;
     }
 
     protected String getCommand(Map<ConanParameter, String> parameters) {
         getLog().debug("Executing " + getName() + " with the following parameters: " + parameters.toString());
 
         // deal with parameters
         AccessionParameter accession = new AccessionParameter();
         accession.setAccession(parameters.get(accessionParameter));
         if (accession.getAccession() == null) {
             throw new IllegalArgumentException("Accession cannot be null");
         }
         else {
             //execution
             return generateCleanupCommand(accession);
         }
     }
 
     protected String getLSFOutputFilePath(Map<ConanParameter, String> parameters) {
         // deal with parameters
         AccessionParameter accession = new AccessionParameter();
         for (ConanParameter conanParameter : parameters.keySet()) {
             if (conanParameter == accessionParameter) {
                 accession.setAccession(parameters.get(conanParameter));
             }
         }
 
         if (accession.getAccession() == null) {
             throw new IllegalArgumentException("Accession cannot be null");
         }
         else {
             // get the mageFile parent directory
             final File parentDir = accession.getFile().getAbsoluteFile().getParentFile();
 
             // files to write output to
             final File outputDir = new File(parentDir, ".conan");
 
             // lsf output file
             return new File(outputDir, "unloadcleanup.lsfoutput.txt").getAbsolutePath();
         }
     }
 
     private String generateCleanupCommand(AccessionParameter accession) {
         // execute unloadCleanup script given environment and accession
         String environmentPath = ConanProperties.getProperty("environment.path");
         String ftpPath = ConanProperties.getProperty("full.ftp.location");
        return environmentPath + "software/bin/unloadCleanup.sh -a " + accession.getAccession() + " -f " + ftpPath;
     }
 }
