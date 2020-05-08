 package models;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import play.Logger;
 import play.Play;
 import play.libs.IO;
 import util.Check;
 import util.Utils;
 
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
 import com.thoughtworks.xstream.annotations.XStreamImplicit;
 import com.thoughtworks.xstream.annotations.XStreamOmitField;
 
 import exception.CommandException;
 
 /**
  * Execute the command on the LSF Platform Computing trough the "bsub" system command
  * 
  * See  http://www.platform.com/workload-management/high-performance-computing/lp
  * 
  * @author Paolo Di Tommaso
  *
  */
 @XStreamAlias("bsub")
 public class BsubCommand extends AbstractShellCommand {
 	
 	static final Pattern JOB_PATTERN = Pattern.compile("^Job <(\\d+)> .*$");
 	
 	@XStreamImplicit
 	List<AbstractShellCommand> _commands;
 
 	@XStreamAsAttribute
 	String queue;
 	
 	@XStreamAsAttribute
 	String jobname;
 	
 	
 	@XStreamOmitField private File fPbsFile; 
 	@XStreamOmitField private AbstractShellCommand command;
 
 	private String jobid;
 	
 	public Boolean disabled;
 	
 	/** The default constructor */
 	public BsubCommand() {}
 	
 	public BsubCommand( BsubCommand that ) {
 		super(that);
 		this._commands = Utils.copy(that._commands);
 		this.queue = that.queue;
 		this.jobname = that.jobname;
 	}
 	
 	String getQueue() {
 		return queue;
 	}
 	
 	String getJobName() {
 		return jobname;
 	}
 	
 	File getPbsFile() {
 		return fPbsFile;
 	}
 	
 	String getJobId() {
 		return jobid;
 	}
 	
 	public boolean getDisabled() { 
 		if( disabled != null ) { 
 			return disabled;
 		}
 		
 		String result = AppProps.instance().getProperty("bsub.disabled");
 		disabled = result != null ? "true".equals(result) : Play.mode.equals( Play.Mode.DEV );
 		return disabled;
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
 		if( Utils.isEmpty(logfile) ) logfile = "_bsub.out.log";
 		if( Utils.isEmpty(errfile) ) errfile = "_bsub.err.log";
 		if( Utils.isEmpty(envfile) ) envfile = "_bsub.env.txt";
 		if( Utils.isEmpty(cmdfile) ) cmdfile = "_bsub.cmd.txt";
 		
 		super.init(ctx); 
 	}
 	
 	@Override
 	protected String onInitCommandLine(String cmdLine) {
 		
 		/*
 		 * 0. validation 
 		 */
 		if( Utils.isEmpty(queue) ) {
 			queue = AppProps.instance().getString("bsub.queue");
 		}
 		
 		if( Utils.isEmpty(jobname)) {
 			jobname = "tcoffee-" + ctx.get("_rid");
 		}
  		
 
 		/*
 		 * 3. return the bsub command 
 		 */
 		File targetOutFile = command.getLogFile();
 		File targetErrFile = command.getErrFile();
 		StringBuilder result = new StringBuilder("bsub ");
 
 		result .append("-K ");		// sync mode i.e. wait for termination before exit
 		result .append("-rn ");		// define as not restartable
 	
 
 		// add the queue name
 		if( Utils.isNotEmpty(queue)) { 
 			result .append("-q ") .append(queue) .append(" ");
 		}
 		
 		// add the job name
 		if( Utils.isNotEmpty(jobname) ) {
 			result.append("-J ") .append(jobname) .append(" ");
 		}
 
 		// add the std output filename
 		if( targetOutFile != null ) {
 			result .append("-o ") .append(targetOutFile.getName()) .append(" ");
 		}
 
 		// add the std error filename
 		if( targetErrFile != null ) {
 			result .append("-e ") .append(targetErrFile.getName()) .append(" ");
 		}
 		
 		// append the original command which is being submitted 
 		if( Utils.isNotEmpty(command.cmdfile)) { 
 			result.append( "< " ) .append( command.cmdfile ) ;
 		}
 		else { 
 			result.append( command.getCmdLine() );
 		}
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
 		 * complete this job parsing the bsub output
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
 		catch (IOException e) {
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
 			String line = reader.readLine().trim();
 			reader.close();
 			Matcher matcher = JOB_PATTERN.matcher(line);
 			if(matcher.matches()) { 
 				jobid = matcher.group(1); 
 			}
 			
 		} 
 		catch (Exception e) {
 			Logger.error(e, "Error on parsing bsub result file: ", getLogFile());
 		}

		if( Utils.isEmpty(jobid)) { 
			result.addError("Unable to submit your job to grid for computation");
			return false;
		}
		
		return true;	
 	}
 	
 }
