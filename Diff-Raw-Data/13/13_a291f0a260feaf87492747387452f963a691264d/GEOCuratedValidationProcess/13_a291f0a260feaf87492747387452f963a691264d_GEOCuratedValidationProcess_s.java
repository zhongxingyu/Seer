 package uk.ac.ebi.fgpt.conan.process.ae2;
 
 import net.sourceforge.fluxion.spi.ServiceProvider;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import uk.ac.ebi.fgpt.conan.ae.GEOCuratedAccessionParameter;
 import uk.ac.ebi.fgpt.conan.lsf.AbstractLSFProcess;
 import uk.ac.ebi.fgpt.conan.lsf.LSFProcess;
 import uk.ac.ebi.fgpt.conan.model.ConanParameter;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Map;
 
 /**
  * Javadocs go here!
  *
  * @author Tony Burdett
  * @date 28/03/11
  */
 @ServiceProvider
 public class GEOCuratedValidationProcess extends AbstractAE2LSFProcess {
     private final Collection<ConanParameter> parameters;
     private final GEOCuratedAccessionParameter accessionParameter;
 
     private Logger log = LoggerFactory.getLogger(getClass());
 
     public GEOCuratedValidationProcess() {
         parameters = new ArrayList<ConanParameter>();
         accessionParameter = new GEOCuratedAccessionParameter();
         parameters.add(accessionParameter);
        setQueueName("production");
     }
 
     protected Logger getLog() {
         return log;
     }
 
 
     public String getName() {
         return "geo validation";
     }
 
     public Collection<ConanParameter> getParameters() {
         return parameters;
     }
 
     protected String getComponentName() {
         return LSFProcess.UNSPECIFIED_COMPONENT_NAME;
     }
 
     protected String getCommand(Map<ConanParameter, String> parameters) throws IllegalArgumentException {
         getLog().debug("Executing " + getName() + " with the following parameters: " + parameters.toString());
 
         // deal with parameters
         GEOCuratedAccessionParameter accession = new GEOCuratedAccessionParameter();
         accession.setAccession(parameters.get(accessionParameter));
         if (accession.getAccession() == null) {
             throw new IllegalArgumentException("Accession cannot be null");
         }
         else {
             // main command to execute perl script
             String mainCommand = "cd " + accession.getFile().getParentFile().getAbsolutePath() + "; " +
                     "perl /ebi/microarray/ma-subs/AE/subs/PERL_SCRIPTS/validate_magetab.pl ";
             // path to relevant file
             String filePath = accession.getFile().getAbsolutePath();
             // return command string
             if (accession.isExperiment()) {
                 return mainCommand + "-i " + filePath;
             }
             else {
                 return mainCommand + "-a " + filePath;
             }
         }
     }
 
     protected String getLSFOutputFilePath(Map<ConanParameter, String> parameters)
             throws IllegalArgumentException {
         getLog().debug("Executing " + getName() + " with the following parameters: " + parameters.toString());
 
         // deal with parameters
         GEOCuratedAccessionParameter accession = new GEOCuratedAccessionParameter();
         accession.setAccession(parameters.get(accessionParameter));
         if (accession.getAccession() == null) {
             throw new IllegalArgumentException("Accession cannot be null");
         }
         else {
             // get the mageFile parent directory
             final File parentDir = accession.getFile().getAbsoluteFile().getParentFile();
 
             // files to write output to
             final File outputDir = new File(parentDir, ".conan");
 
             // lsf output file
             return new File(outputDir, "magetabvalidator.lsfoutput.txt").getAbsolutePath();
         }
     }
 }
