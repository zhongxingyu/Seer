 package models;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import play.Logger;
 import play.Play;
 import play.exceptions.UnexpectedException;
 import play.libs.IO;
 import util.Check;
 import util.Utils;
 
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
 import com.thoughtworks.xstream.annotations.XStreamImplicit;
 import com.thoughtworks.xstream.annotations.XStreamOmitField;
 
 import exception.CommandException;
 
 /**
  * Execute the specified command on the system cluster 
  * 
  * @author Paolo Di Tommaso
  *
  */
 @XStreamAlias("qsub")
 public class QsubCommand extends AbstractShellCommand {
 	
 	@XStreamImplicit
 	List<AbstractShellCommand> _commands;
 
 	@XStreamAsAttribute
 	String queue;
 	
 	@XStreamAsAttribute
 	String jobname;
 	
 	/** the qsub script filename */
 	String jobfile;
 	
 	@XStreamOmitField private File fJobFile; 
 	@XStreamOmitField private AbstractShellCommand command;
 
 	private String jobid;
 	
 	public Boolean disabled;
 	
 	/** 
 	 * template string can be used to 'post-process' the command to be executed in the qsub script
 	 */
 	public String wrapper;
 	
 	/** The default constructor */
 	public QsubCommand() {}
 	
 	public QsubCommand( QsubCommand that ) {
 		super(that);
 		this._commands = Utils.copy(that._commands);
 		this.queue = that.queue;
 		this.jobname = that.jobname;
 		this.jobfile = that.jobfile;
 	}
 	
 	String getQueue() {
 		return queue;
 	}
 	
 	String getJobName() {
 		return jobname;
 	}
 	
 	File getJobFile() {
 		return fJobFile;
 	}
 	
 	String getJobId() {
 		return jobid;
 	}
 	
 	public boolean getDisabled() { 
 		if( disabled != null ) { 
 			return disabled;
 		}
 		
 		String result = AppProps.instance().getString("qsub.disabled");
 		disabled = result != null ? "true".equals(result) : Play.mode.isDev();
 		return disabled;
 	}
 	
 	public String getWrapper() { 
 		if( wrapper != null ) { 
 			return wrapper;
 		}
 		
 		return wrapper = AppProps.instance().getString("qsub.wrapper","").trim();
 	}
 	
 	@Override
 	public void init(CommandCtx ctx) {
 		
 		command =  _commands != null && _commands.size()>0 ? _commands.get(0) : null;
 		Check.notNull(command, "Missing nested command");
 
 		
 		/*
 		 * initialize the target command 
 		 */
 		command.init(ctx);
 		
 		if( disabled ) {
 			return;
 		}
 		
 		/*
 		 * initialize this command 
 		 */
 		if( Utils.isEmpty(logfile) ) logfile = "_qsub.out.log";
 		if( Utils.isEmpty(errfile) ) errfile = "_qsub.err.log";
 		if( Utils.isEmpty(envfile) ) envfile = "_qsub.env.txt";
 		if( Utils.isEmpty(cmdfile) ) cmdfile = "_qsub.cmd.txt";
 		if( Utils.isEmpty(jobfile) ) jobfile = "_qsub.pbs";
 		
 		super.init(ctx); 
 	}
 	
 	@Override
 	protected void onInitEnv(Map<String, String> map) {
 		
 		super.onInitEnv(map);
 
 		/*
 		 * add the SGE_ROOT env variable if exists 
 		 */
 		addEnvironmentVariableIfPropertyExists(map, "SGE_ROOT");
 		addEnvironmentVariableIfPropertyExists(map, "SGE_EXECD_PORT");
 		addEnvironmentVariableIfPropertyExists(map, "SGE_CLUSTER_NAME");
 		addEnvironmentVariableIfPropertyExists(map, "SGE_QMASTER_PORT");
 		addEnvironmentVariableIfPropertyExists(map, "SGE_CELL");
 	
 	}
 	
 	private void addEnvironmentVariableIfPropertyExists(Map<String,String> map, String key) {
 		if( map.containsKey(key)) return;
 		
 		String val = AppProps.instance().getString("settings." + key);
 		if( Utils.isNotEmpty(val)) {
 			map.put(key,val);
 		}
 	}
 	
 	@Override
 	protected String onInitCommandLine(String cmdLine) {
 		
 		/*
 		 * 0. validation 
 		 */
 		if( Utils.isEmpty(queue) ) {
 			queue = AppProps.instance().getString("settings.SGE_QUEUE");
 			Check.notEmpty(queue, "Qsub queue parameter cannot be empty");
 		}
 		
 		if( Utils.isEmpty(jobname)) {
 			jobname = "t-"+ ctx.get("_rid") ;
 		}
  		
 		/*
 		 * 1. create the PBD script file to be submited in the cluster queue
 		 */
 		fJobFile = new File(ctxfolder,jobfile);
 		StringBuilder script = new StringBuilder();
 		script.append("#!/bin/sh\n");
 
 		/*
 		 * 2. fetch the env vars from the target command
 		 *    and add them to t  
 		 */
 		Map<String,String> targetEnv = new HashMap<String, String>();
 		command.onInitEnv(targetEnv);
 		
 		if( targetEnv != null && targetEnv.size()>0 ) {
 			for( Map.Entry<String,String> entry : targetEnv.entrySet() ) {
 				script.append("#$ -v ");
				script.append(entry.getKey()).append("=").append(entry.getValue());
 				script.append("\n"); // <-- don't forget the blank
 			}
 		}
 
 		/* write the command and save */
 		String cmd = command.getCmdLine();
 		if( Utils.isNotEmpty(wrapper)) { 
 			cmd = String.format(wrapper, cmd);
 		}
 		script.append(cmd);
 
 		/* save the qsub script */
 		Utils.write(script, fJobFile);
 		
 
 		/*
 		 * 3. return the qsub command 
 		 */
 		File targetOutFile = command.getLogFile();
 		File targetErrFile = command.getErrFile();
 		StringBuilder result = new StringBuilder();
 		result .append("qsub ");
 		result .append("-cwd ");
 		result .append("-sync y ");
 		result .append("-r no ");
 		result .append("-terse ");
 		result .append("-q ") .append(queue) .append(" ");
 
 		if( targetOutFile != null ) {
 			result .append("-o ") .append(targetOutFile.getName()) .append(" ");
 		}
 
 		if( targetErrFile != null ) {
 			result .append("-e ") .append(targetErrFile.getName()) .append(" ");
 		}
 		
 		if( Utils.isNotEmpty(jobname) ) {
 			result.append("-N ") .append(jobname) .append(" ");
 		}
 
 		result.append( fJobFile.getName() );
 		return  result.toString();
 	}
 	
 	@Override
 	public boolean run() throws CommandException {
 		return (disabled)
 			 	? command.run()
 				: super.run();
 	}
 	
 	@Override
 	protected boolean done(boolean success) {
 		
 		/*
 		 * complete the target command
 		 */
 		success = command.done(success);
 		
 		/* 
 		 * the result of this command is the same as the target command 
 		 */
 		result = command.result;
 
 		if( disabled ) { 
 			return success;
 		}
 	
 		/*
 		 * complete this job parsing the qsub output
 		 */
 		parseResultFile();
 		if( hasErrors() ) {
 			success = false;
 		}
 		
 		return success;
 	}
 
 	private boolean hasErrors() {
 		
 		File file = getErrFile(); 
 		if( file == null || !file.exists() || file.length() == 0) {
 			/* no error file so .. no errors ! */
 			return false;
 		}
 		
 		String error;
 		try {
 			error = IO.readContentAsString(file);
 		} 
 		catch (UnexpectedException e) {
 			Logger.error(e, "Enable to read qsub error file: '%s'", file);
 			error = "(qsub reports problems but the error file cannot be read)";
 		}
 		
 		if( error == null || (error=error.trim()).length() == 0 ) { 
 			/* empty error file so .. NO ERROR */
 			return false;
 		}
 		
 		result.clearErrors();
 		result.addError(error);
 		return true;
 	}
 
 	private boolean parseResultFile() {
 		BufferedReader reader;
 		try {
 			reader = new BufferedReader(new FileReader(getLogFile()));
 			jobid = reader.readLine().trim();  // <-- by definition the first line contains the submitted job-id 
 			reader.close();
 		} 
 		catch (IOException e) {
 			Logger.error(e, "Error on parsing qsub result file: ", getLogFile());
 		}
 		
 		if( Utils.isEmpty(jobid)) { 
 			result.addError("Unable to submit your job to grid for computation");
 			return false;
 		}
 		
 		return true;
 	}
 	
 }
