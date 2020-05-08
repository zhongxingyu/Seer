 package uk.ac.ebi.fgpt.conan.process.biosd;
 
 import net.sourceforge.fluxion.spi.ServiceProvider;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import uk.ac.ebi.fgpt.conan.lsf.AbstractLSFProcess;
 import uk.ac.ebi.fgpt.conan.lsf.LSFProcess;
 import uk.ac.ebi.fgpt.conan.model.ConanParameter;
 import uk.ac.ebi.fgpt.conan.process.biosd.model.SampleTabAccessionParameter;
 import uk.ac.ebi.fgpt.conan.properties.ConanProperties;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Map;
 
 @ServiceProvider
 public class SampleTabLoadLSFProcess extends AbstractBioSDLSFProcess {
 
 	private Logger log = LoggerFactory.getLogger(getClass());
 
 	public String getName() {
 		return "load";
 	}
 
 	protected Logger getLog() {
 		return log;
 	}
 
 	public Collection<ConanParameter> getParameters() {
 		return parameters;
 	}
 
 	protected String getComponentName() {
 		return LSFProcess.UNSPECIFIED_COMPONENT_NAME;
 	}
 
 	protected String getCommand(Map<ConanParameter, String> parameters)
 			throws IllegalArgumentException {
 		getLog().info(
 				"Executing " + getName() + " with the following parameters: "
 						+ parameters.toString());
 
 		// deal with parameters
 		SampleTabAccessionParameter accession = new SampleTabAccessionParameter();
 		accession.setAccession(parameters.get(accessionParameter));
 		if (accession.getAccession() == null) {
 			throw new IllegalArgumentException("Accession cannot be null");
 		}
 
 		String scriptpath = ConanProperties.getProperty("biosamples.script.path");
 		File script = new File(scriptpath, "AgeTab-Loader.sh");
 
 		File outdir;
 		try {
 			outdir = getOutputDirectory(accession);
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new IllegalArgumentException(
 					"Unable to create directories for " + accession);
 		}
 
 		File agedir = new File(outdir, "age");
         File ageout = new File(outdir, "load");
 
 		// main command to execute script
 		String mainCommand = script.getAbsolutePath() 
 				+ " -s -m -i -e"
 				+ " -o "+ageout.getAbsolutePath()
 				+ " -u "+ConanProperties.getProperty("biosamples.biosd.username")
 				+ " -p "+ConanProperties.getProperty("biosamples.biosd.password")
				+ " -h \""+ConanProperties.getProperty("biosamples.biosd.url")+"\""
 				+ " "+agedir.getAbsolutePath(); 
 		getLog().info("Command is: <" + mainCommand + ">");
 		return mainCommand;
 	}
 }
