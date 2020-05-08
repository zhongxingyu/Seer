 package org.daisy.maven.xproc;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.maven.plugin.MojoExecutionException;
 
 import static org.daisy.maven.xproc.utils.asURI;
 
 /**
  * Run an XProc pipeline.
  *
  * @goal xproc
  */
 public class XProcMojo extends AbstractMojo {
 	
 	/**
 	 * Path to the pipeline.
 	 *
 	 * @parameter
 	 * @required
 	 */
 	private File pipeline;
 	
 	/**
 	 * Input paths.
 	 *
 	 * @parameter
 	 */
 	private Map<String,String> inputs;
 	
 	/**
 	 * Output paths.
 	 *
 	 * @parameter
 	 */
 	private Map<String,String> outputs;
 	
 	/**
 	 * Options.
 	 *
 	 * @parameter
 	 */
 	private Map<String,String> options;
 	
 	public void execute() throws MojoExecutionException {
 		String pipelineAsURI = asURI(pipeline);
 		Map<String,List<String>> inputsAsURIs = null;
 		Map<String,String> outputsAsURIs = null;
 		if (inputs != null) {
 			inputsAsURIs = new HashMap<String,List<String>>();
 			for (String port : inputs.keySet()) {
 				String[] sequence = inputs.get(port).trim().split("\\s+");
 				for (int i = 0; i < sequence.length; i++)
 					sequence[i] = asURI(new File(sequence[i]));
 				inputsAsURIs.put(port, Arrays.asList(sequence)); }}
 		if (outputs != null) {
 			outputsAsURIs = new HashMap<String,String>();
 			for (String port : outputs.keySet())
 				outputsAsURIs.put(port, asURI(new File(outputs.get(port)))); }
 		try {
			getLog().info("Running XProc ...");
 			engine.run(pipelineAsURI,
 			           inputsAsURIs,
 			           outputsAsURIs,
 			           options,
 			           null); }
		catch (XProcExecutionException e) {
			getLog().error(e.getMessage());
			Throwable cause = e.getCause();
			if (cause != null)
				getLog().debug(cause); }
 		catch (Exception e) {
			throw new MojoExecutionException("Unexpected error", e); }
 	}
 }
