 package grisu.backend.model.job.gt5;
 
 import grisu.backend.info.InformationManagerManager;
 import grisu.jcommons.constants.Constants;
 import grisu.jcommons.interfaces.InformationManager;
 import grisu.jcommons.utils.JsdlHelpers;
 import grisu.settings.ServerPropertiesManager;
 
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.StringUtils;
 import org.globus.rsl.Binding;
 import org.globus.rsl.Bindings;
 import org.globus.rsl.NameOpValue;
 import org.globus.rsl.RslNode;
 import org.w3c.dom.Document;
 
 public class RSLFactory {
 
 	private static RSLFactory singleton = null;
 	
 	public static  RSLFactory getRSLFactory(){
 		if (singleton == null){
 			singleton = new RSLFactory();
 		}
 		return singleton;
 	}
 	
 	private int commitTimeout = 5;
 	
 	private final InformationManager informationManager = InformationManagerManager
 	.getInformationManager(ServerPropertiesManager
 			.getInformationManagerConf());
 	
 	private void addWhenNotBlank(RslNode rsl, String attribute, String value){
 		if (StringUtils.isNotBlank(value)){
 			rsl.add(new NameOpValue(attribute, NameOpValue.EQ,value));
 		}
 	}
 	
 	public RslNode create(final Document jsdl, final String fqan) throws RSLCreationException{
 		RslNode result = new RslNode();
 		
 		if (fqan == null){
 			throw new RSLCreationException("fqan cannot be null");
 		} 
 		else if (!Pattern.matches("(/\\S+)+",fqan)){
 			throw new RSLCreationException("fqan " + fqan + " format is invalid");
 		}
 		
 		// add executable 
 		
 		String executable = JsdlHelpers.getPosixApplicationExecutable(jsdl);
 		if (executable == null) {
 			throw new RSLCreationException("executable is not set");
 		}
 		result.add( new NameOpValue("executable",NameOpValue.EQ, executable));
 
 		// add arguments 
 		
 		String[] arguments = JsdlHelpers.getPosixApplicationArguments(jsdl);
 		if ((arguments != null) && (arguments.length > 0)) {
 			result.add(new NameOpValue("arguments", NameOpValue.EQ,
 					arguments));
 		}
 		
 		// add modules
 		String[] modules = getModulesFromMDS(jsdl);
 		for (String module: modules){
 			result.add(new NameOpValue("module", NameOpValue.EQ,
 					module));
 		}
 		
 		// job name
 		String jobname = JsdlHelpers.getJobname(jsdl);
 		jobname = (jobname == null)?"":jobname.substring(Math.max(0, jobname.length() - 6));
 		addWhenNotBlank(result, "jobname", jobname);
 				
 		addWhenNotBlank(result, "stdout", JsdlHelpers.getPosixStandardOutput(jsdl));
 		addWhenNotBlank(result, "stderr", JsdlHelpers.getPosixStandardError(jsdl));
 		addWhenNotBlank(result, "stdin", JsdlHelpers.getPosixStandardInput(jsdl));
 		
 		addWhenNotBlank(result, "directory", JsdlHelpers.getWorkingDirectory(jsdl));
 		
 		addWhenNotBlank(result,"email_address",JsdlHelpers.getEmail(jsdl));
 		if (JsdlHelpers.getSendEmailOnJobFinish(jsdl)){
 			result.add(new NameOpValue("email_on_termination", NameOpValue.EQ, "yes"));
 		}
 		if (JsdlHelpers.getSendEmailOnJobStart(jsdl)){
 			result.add(new NameOpValue("email_on_execution", NameOpValue.EQ, "yes"));
 			result.add(new NameOpValue("email_on_abort", NameOpValue.EQ, "yes"));
 		}
 		
 		// Add "queue" node
 		// TODO change that once I know how to specify queues in jsdl
 		final String[] queues = JsdlHelpers.getCandidateHosts(jsdl);
 		if ((queues != null) && (queues.length > 0)) {
 			String queue = queues[0];
 
 			if (queue.indexOf(":") != -1) {
 				queue = queue.substring(0, queue.indexOf(":"));
 				result.add(new NameOpValue("queue", NameOpValue.EQ, queue));
 			} else {
 				throw new RSLCreationException("queue " + queue + ": invalid format");
 			}
 
 		} else {
 			throw new RSLCreationException("queue not set");
 		}
 		
 		// Add "jobtype" if mpi
 		final int pcount = JsdlHelpers.getProcessorCount(jsdl);
 		String jobtype = JsdlHelpers.getArcsJobType(jsdl);
 		final int hcount = JsdlHelpers.getResourceCount(jsdl);
 		
 		if (StringUtils.isBlank(jobtype)){
			jobtype = "single";
 			
 		}
 		result.add(new NameOpValue("count", NameOpValue.EQ, "" + pcount));
 		result.add(new NameOpValue("jobtype", NameOpValue.EQ, jobtype));
 		if (hcount >= 0){
 			result.add(new NameOpValue("hostCount", NameOpValue.EQ, "" + hcount));
 		}
 		
 		// total memory
 		Long memory = JsdlHelpers.getTotalMemoryRequirement(jsdl);
 		if ((memory != null) && (memory >= 0)) {
 			result.add(new NameOpValue("max_memory", NameOpValue.EQ, "" + (memory / (1024 * 1024))));
 		}
 		
 
 		// Add "maxWallTime" node
 		final int walltime = JsdlHelpers.getWalltime(jsdl);
 		if (walltime > 0) {
 			result.add(new NameOpValue("max_wall_time", NameOpValue.EQ, "" + (walltime / 60)));
 		}
 
 		// environment variables
 
 		Map<String,String> env = JsdlHelpers.getPosixApplicationEnvironment(jsdl);
 		if ((env != null) && (env.size() > 0)){
 			Bindings b = new Bindings("environment");
 			for (String var: env.keySet()){
 				b.add(new Binding(var, env.get(var)));
 			}
 			result.add(b);
 		}
 			
 		result.add(new NameOpValue("save_state", NameOpValue.EQ, "yes"));
 		result.add(new NameOpValue("two_phase", NameOpValue.EQ, this.commitTimeout + ""));
 		result.add(new NameOpValue("vo",NameOpValue.EQ,fqan));
 		
 		return result;
 	}
 	
 	private String[] getModulesFromMDS(final Document jsdl) {
 		String[] modules_string = JsdlHelpers.getModules(jsdl);
 		if (modules_string != null) {
 			return modules_string;
 		}
 		// mds based
 		String application = JsdlHelpers.getApplicationName(jsdl);
 		String version = JsdlHelpers.getApplicationVersion(jsdl);
 		String[] subLocs = JsdlHelpers.getCandidateHosts(jsdl);
 		String subLoc = null;
 
 		if ((subLocs != null)
 				&& (subLocs.length > 0)
 				&& (StringUtils.isNotBlank(subLocs[0]) && (StringUtils
 						.isNotBlank(application)))
 				&& (!Constants.GENERIC_APPLICATION_NAME.equals(application))) {
 
 			subLoc = subLocs[0];
 
 		} else {
 			return new String[] {};
 		}
 
 		if (StringUtils.isBlank(version)) {
 			version = Constants.NO_VERSION_INDICATOR_STRING;
 		}
 
 		final Map<String, String> appDetails = this.informationManager
 				.getApplicationDetails(application, version, subLoc);
 		String m = appDetails.get(Constants.MDS_MODULES_KEY);
 		if ( StringUtils.isBlank(m) ) {
 			return new String[]{};
 		}
 		modules_string = appDetails.get(Constants.MDS_MODULES_KEY).split(",");
 		if (modules_string != null) {
 			return modules_string;
 		} else {
 			return new String[] {};
 		}
 
 	}
 	
 	public void setCommitTimeout(int commitTimeout){
 		this.commitTimeout = commitTimeout;
 	}
 }
