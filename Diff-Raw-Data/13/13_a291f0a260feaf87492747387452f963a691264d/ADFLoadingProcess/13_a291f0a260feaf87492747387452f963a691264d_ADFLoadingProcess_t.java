 package uk.ac.ebi.fgpt.conan.process.ae2;
 
 import net.sourceforge.fluxion.spi.ServiceProvider;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import uk.ac.ebi.arrayexpress2.exception.ComponentNames;
 import uk.ac.ebi.fgpt.conan.ae.AccessionParameter;
 import uk.ac.ebi.fgpt.conan.model.ConanParameter;
 import uk.ac.ebi.fgpt.conan.properties.ConanProperties;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Map;
 
 /**
  * Process for ADF loading
  *
  * @author Natalja Kurbatova
  * @date 21-Oct-2010
  */
 @ServiceProvider
 public class ADFLoadingProcess extends AbstractAE2LSFProcess {
     private final Collection<ConanParameter> parameters;
     private final AccessionParameter accessionParameter;
 
     private Logger log = LoggerFactory.getLogger(getClass());
 
     public ADFLoadingProcess() {
         parameters = new ArrayList<ConanParameter>();
         accessionParameter = new AccessionParameter();
         parameters.add(accessionParameter);
     }
 
     protected Logger getLog() {
         return log;
     }
 
     public String getName() {
         return "adf loading";
     }
 
     public Collection<ConanParameter> getParameters() {
         return parameters;
     }
 
     protected int getMemoryRequirement(Map<ConanParameter, String> parameterStringMap) {
         return 8000;
     }
 
     protected String getComponentName() {
         return ComponentNames.ADFLOADER;
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
             if (!accession.isExperiment()) {
                 String environmentPath = ConanProperties.getProperty("environment.path");
                 return environmentPath + "software/bin/adfloader.sh load " + accession.getFile().getAbsolutePath();
             }
             else {
                 throw new IllegalArgumentException("ADF Loader loads arrays, not experiments");
             }
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
             return new File(outputDir, "adfloader.lsfoutput.txt").getAbsolutePath();
         }
     }
 }
