 package org.uncertweb.aquacrop;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.Reader;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.uncertweb.aquacrop.data.Output;
 import org.uncertweb.aquacrop.data.Project;
 import org.uncertweb.io.StreamGobbler;
 
 /**
  * Okay...
  * 
  * The AquaCrop plug-in program has no UI, but creates an application window when it runs
  * (containing nothing, disappears upon program termination).
  * 
  * Therefore running the plug-in program with Wine results in the following:
  *  - Application tried to create a window, but no driver could be loaded.
  *  - Make sure that your X server is running and that $DISPLAY is set correctly.
  * 
  * As we want to run the program behind a web service interface, on a server somewhere, which
  * may or may not have X server running or a display, this poses an issue!
  * 
  * The solution is Xvfb.
  * 
  * The xvfb-run script creates...
  *  
  * 
  * 
  * @author Richard Jones
  *
  */
 public class AquaCropRunner {
 	
 	private final Logger logger = LoggerFactory.getLogger(AquaCropRunner.class);
 
 	private String basePath;
 	private String prefixCommand;
 	private String basePathOverride;
 
 	public AquaCropRunner(String basePath) {
 		this.basePath = basePath;
 	}
 
 	/**
 	 * 
 	 * 
 	 * 
 	 * @param basePath
 	 * @param prefixCommand
 	 */
 	public AquaCropRunner(String basePath, String prefixCommand, String basePathOverride) {
 		this(basePath);
 		this.prefixCommand = prefixCommand;
 		this.basePathOverride = basePathOverride;
 	}
 
 	public Output run(Project project) throws AquaCropException {
 		// generate an id
 		final String runId = String.valueOf(System.currentTimeMillis());
 
 		// serialize project and run
 		try {
 			// this will create all data files
 			logger.debug("Serializing project.");
 			AquaCropSerializer.serializeProject(project, basePath, basePathOverride, runId);
 
 			// get runtime
			logger.debug("Getting runtime");
 			Runtime runtime = Runtime.getRuntime();
 			
 			// for monitoring and reading
 			File outputFile = new File(basePath, "ACsaV31plus/OUTP/" + runId + "PRO.OUT");
 
 			// run program
 			try {			
 				// start aquacrop process
 				logger.debug("Starting process.");
 				Process process = runtime.exec((prefixCommand != null ? prefixCommand + " " : "") + basePath + "/ACsaV31plus/ACsaV31plus.exe");
 				
 				// consume streams
 				StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream());            
 				StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream());
 	            errorGobbler.start();
 	            outputGobbler.start();
 	            
 	            // wait for process
 	            // we could be waiting forever if there's been an error...
	            logger.debug("Waiting for process to end...");
	            process.waitFor();
 			}
 			catch (IOException e) {
 				throw new AquaCropException("Couldn't run AquaCrop: " + e.getMessage());
 			}
 			catch (InterruptedException e) {
 				throw new AquaCropException("Couldn't run AquaCrop: " + e.getMessage());
 			}
 
 			// parse output
			logger.debug("Process finished, parsing output.");
 			FileReader reader = new FileReader(outputFile);
 			Output output = AquaCropRunner.deserializeOutput(reader);
 			reader.close();
 
 			// remove output file
 			logger.debug("Removing output file.");
 			outputFile.delete();
 			
 			if (output == null) {
 				// must be a problem running AquaCrop, but unfortunately it only gives error messages in dialogs!
 				String message = "Couldn't parse AquaCrop empty output, parameters may be invalid.";
 				logger.error(message);
 				throw new AquaCropException(message);
 			}
 			else {
 				logger.debug("Parsed output successfully.");
 				return output;
 			}				
 		}
 		catch (IOException e) {
 			// will be from creating the project file, or reading the output file
 			String message = "Error reading input/output files: " + e.getMessage();
 			logger.error(message);
 			throw new AquaCropException(message);
 		}
 		finally {
 			// clean up input files			
 			File dataDir = new File(basePath, "AquaCrop/DATA/");
 			for (File dataFile : dataDir.listFiles(new FilenameFilter() {
 				public boolean accept(File arg0, String arg1) {
 					return (arg1.startsWith(runId));
 				}				
 			})) {
 				dataFile.delete();
 			}
 			new File(basePath, "ACsaV31plus/LIST/" + runId + ".PRO").delete();
 		}
 	}
 
 	private static Output deserializeOutput(Reader reader) throws FileNotFoundException, IOException {
 		BufferedReader bufReader = new BufferedReader(reader);
 		bufReader.readLine(); // skip first line containing aquacrop version, creation date
 		bufReader.readLine(); // skip following empty line
 		bufReader.readLine(); // skip column headings
 		bufReader.readLine(); // skip column headings units
 
 		String line;
 		while ((line = bufReader.readLine()) != null && line.length() > 0) { // could be blank line at bottom
 			// split on whitespace
 			String[] tokens = line.trim().split("\\s+");
 
 			// totals is all we want
 			if (tokens[0].startsWith("Tot")) {
 				// parse results
 				double rain = Double.parseDouble(tokens[4]);
 				double eto = Double.parseDouble(tokens[5]);
 				double gdd = Double.parseDouble(tokens[6]);
 				double co2 = Double.parseDouble(tokens[7]);
 				double irri = Double.parseDouble(tokens[8]);
 				double infilt = Double.parseDouble(tokens[9]);
 				double e = Double.parseDouble(tokens[10]);
 				double eEx = Double.parseDouble(tokens[11]);
 				double tr = Double.parseDouble(tokens[12]);
 				double trTrx = Double.parseDouble(tokens[13]);
 				double drain = Double.parseDouble(tokens[14]);
 				double bioMass = Double.parseDouble(tokens[15]);
 				double brW = Double.parseDouble(tokens[16]);
 				double brWsf = Double.parseDouble(tokens[17]);
 				double wPetB = Double.parseDouble(tokens[18]);
 				double hi = Double.parseDouble(tokens[19]);
 				double yield = Double.parseDouble(tokens[20]);
 				double wPetY = Double.parseDouble(tokens[21]);
 
 				// and return
 				return new Output(rain, eto, gdd, co2, irri, infilt, e, eEx, tr, trTrx, drain, bioMass, brW, brWsf, wPetB, hi, yield, wPetY);
 			}
 		}
 		
 		return null;
 	}
 
 }
