 package eu.stratosphere.meteor.client;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.cli.CommandLine;
 
 import eu.stratosphere.meteor.QueryParser;
 import eu.stratosphere.meteor.client.CLClient;
 import eu.stratosphere.meteor.client.web.ErrorServlet;
 import eu.stratosphere.nephele.configuration.GlobalConfiguration;
 import eu.stratosphere.sopremo.client.DefaultClient;
 import eu.stratosphere.sopremo.client.StateListener;
 import eu.stratosphere.sopremo.execution.ExecutionRequest.ExecutionMode;
 import eu.stratosphere.sopremo.execution.ExecutionResponse.ExecutionState;
 import eu.stratosphere.sopremo.execution.SopremoConstants;
 import eu.stratosphere.sopremo.execution.SopremoID;
 import eu.stratosphere.sopremo.operator.SopremoPlan;
 import eu.stratosphere.sopremo.query.QueryParserException;
 
 public class ClientFrontend {
 	
 	/**
 	 * CLClient as base client to handle options and commands
 	 */
 	private CLClient baseClient;
 	private CommandLine cmd;
 	
 	/**
 	 * Sopremo plan and default client to handle process
 	 */
 	private SopremoPlan sopremoPlan;
 	private DefaultClient sopremoClient;
 	
 	/**
 	 * Save the meteor script
 	 */
 	private String meteorScript;
 	
 	/**
 	 * the outputs of the script
 	 */
 	private List<String> outputs;
 	
 	private String visOutput = null;
 	
 	private SopremoID currentSopremoID = null;
 	
 	/**
 	 * job status flags
 	 */	
 	private String currentJobStatus = "{ \"jobstatus\": \"currently no jobs running\" }";
 	
 	private String jobStates = currentJobStatus;
 	
 	private long startTimestamp = 0L;
 	
 	private long currentTimestamp = 0L;
 	
 	/**
 	 * Constructs a new client by given options and commands.
 	 * See CLClient for more details.
 	 * @param args
 	 */
 	public ClientFrontend( String[] args ){
 		// create a base client and handle commands
 		baseClient = new CLClient();
 		cmd = baseClient.parseOptions( args );
 		configureClient( cmd );
 	}
 	
 	/**
 	 * Configure the default client. Method is equal to configureClient in CLClient.
 	 * @param cmd
 	 */
 	private void configureClient(CommandLine cmd) {
 		String configDir = cmd.getOptionValue("configDir");
 		GlobalConfiguration.loadConfiguration(configDir);
 		this.sopremoClient = new DefaultClient(GlobalConfiguration.getConfiguration());
 
 		int updateTime = 1000;
 		if (cmd.hasOption("updateTime"))
 			updateTime = Integer.parseInt(cmd.getOptionValue("updateTime"));
 		this.sopremoClient.setUpdateTime(updateTime);
 
 		String address = cmd.getOptionValue("server"), port = cmd.getOptionValue("port");
 		if (address != null || port != null) {
 			this.sopremoClient.setServerAddress(new InetSocketAddress(
 				address == null ? "localhost" : address,
 				port == null ? SopremoConstants.DEFAULT_SOPREMO_SERVER_IPC_PORT : Integer.parseInt(port)));
 		}
 
 		this.sopremoClient.setExecutionMode(ExecutionMode.RUN_WITH_STATISTICS);
 	}
 	
 	/**
 	 * Starts the process. Creates a sopremo plan by given meteor script and submit this plan by default client.
 	 * @param meteorScript
 	 */
 	public void execute( String meteorScript ) throws QueryParserException, IOException {
 		this.startTimestamp = System.currentTimeMillis();		
 		this.meteorScript = meteorScript;
 		this.jobStates = "";
 		extractHDFSWrites();
 		
 		// TODO: we should have another AJAX servlet, that is periodically queried -> will trigger complete reload
 		
		ByteArrayInputStream inputStream = new ByteArrayInputStream( meteorScript.getBytes("UTF-8") );
		try {
 			sopremoPlan = new QueryParser().tryParse( inputStream );
 			currentSopremoID = sopremoClient.submit(sopremoPlan, new SopremoStateListener(), false);
 			System.out.println("submitted with sopremoID " + currentSopremoID);
 			//sopremoClient.close();
 		} catch ( IOException ioe ){ 
 			configureClient( cmd ); //resets
 			throw ioe;
		} finally {
			inputStream.close();
 		}
 	}
 	
 	/**
 	 * Returns the paths to JSON files
 	 * @return
 	 */
 	public List<String> getOutputPaths(){
 		return Collections.unmodifiableList(outputs);
 	}
 	
 	/**
 	 * Returns the path to visualization file
 	 * @return
 	 */
 	public String getVisualizationDataURL () {
 		return visOutput;
 	}
 	
 	/**
 	 * Returns the whole JSON representation of job states
 	 * @return
 	 */
 	public String getJobStates(){
 		return jobStates;
 	}
 	
 	/**
 	 * Returns metadata of sopremoclient
 	 * @param key
 	 * @return
 	 */
 	public String getMetaData(String key) {
 		System.out.println("returning metadata for " + currentSopremoID);
 		if (currentSopremoID != null) {
 			try {
 				return sopremoClient.getMetaData(currentSopremoID, key).toString();
			} catch (Exception  e) {
 				System.out.println("Error retrieving metadata for " + currentSopremoID);
 				e.printStackTrace(System.out);
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Adds states to JSON string.
 	 */
 	private void addStates(){
 		jobStates += "{";
 		jobStates += "\"jobID\": " + currentSopremoID + ",";
 		jobStates += "\"timestamp\": " + (currentTimestamp - startTimestamp) + ",";
 		jobStates += "\"jobstatus\": \"" + currentJobStatus + "\"";
 		jobStates += "}";
 	}
 	
 	/**
 	 * Adds error status.
 	 */
 	private void addError( String detail ){
 		jobStates += ",{";
 		jobStates += "\"jobID\": " + currentSopremoID + ",";
 		jobStates += "\"timestamp\": " + (currentTimestamp - startTimestamp) + ",";
 		jobStates += "\"jobstatus\": \"error occured, not finished job. See /error subpage for more informations.\"";
 		jobStates += "}";
 		ErrorServlet.setError( detail );
 	}
 	
 	/**
 	 * Returns the path to written json file
 	 * @return path to json file
 	 */
 	private void extractHDFSWrites() {
 		// try to find matches in the entire script using regex
 		// sample line that would match: write $correlation to 'hdfs://localhost/user/result.json';
 		Pattern pattern = Pattern.compile ("write\\s+\\$\\w+\\s+to\\s+'\\s*(hdfs://[^']+)'\\s*;");
 		Matcher matcher = pattern.matcher(meteorScript);
 		outputs = new LinkedList<String> ();
 		while (matcher.find()) {
 			outputs.add(matcher.group(1));
 		}
 		
 		// TODO: hack to decide whether the data should be visualized... for now we just search vis_ in the url
 		for (String out : outputs) {
 			if (out.contains("vis_")) {
 				visOutput = out;
 			}
 		}
 	}
 	
 	/**
 	 * Private class for state listener.
 	 * @author André Greiner-Petter
 	 */
 	private class SopremoStateListener extends StateListener {
 		/*
 		 * (non-Javadoc)
 		 * @see eu.stratosphere.sopremo.client.StateListener#stateChanged(eu.stratosphere.sopremo.execution.
 		 * ExecutionResponse.ExecutionState, java.lang.String)
 		 */
 		@SuppressWarnings("incomplete-switch")
 		@Override
 		public void stateChanged(ExecutionState executionState, String detail) {
 			System.out.println();
 			currentTimestamp = System.currentTimeMillis();
 			switch (executionState) {
 			case ENQUEUED:
 				jobStates = "";
 				currentJobStatus = "Submitted script from web frontend.";
 				addStates();
 				System.out.print( currentJobStatus );
 				break;
 			case RUNNING:
 				currentJobStatus = "Executing script from web frontend.";
 				addStates();
 				System.out.print( currentJobStatus );
 				break;
 			case FINISHED:
 				currentJobStatus = detail;
 				addStates();
 				System.out.print(detail);
 				sopremoClient.close();
 				break;
 			case ERROR:
 				addError(detail);
 				System.out.print(detail);
 				break;
 			}
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * @see eu.stratosphere.sopremo.client.StateListener#stateNotChanged(eu.stratosphere.sopremo.execution.
 		 * ExecutionResponse.ExecutionState, java.lang.String)
 		 */
 		@Override
 		protected void stateNotChanged(ExecutionState state, String detail) {
 			System.out.print(".");
 		}
 	}
 }
