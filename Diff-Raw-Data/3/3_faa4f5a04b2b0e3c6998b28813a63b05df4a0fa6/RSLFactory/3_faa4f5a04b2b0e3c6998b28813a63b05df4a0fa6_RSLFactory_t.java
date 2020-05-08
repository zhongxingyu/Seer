 package grisu.backend.model.job.gt5;
 
 import grisu.jcommons.constants.Constants;
 import grisu.jcommons.interfaces.InformationManager;
 import grisu.jcommons.utils.JsdlHelpers;
 import grisu.model.FileManager;
 import grisu.model.info.dto.Module;
 import grisu.model.info.dto.Package;
 import grisu.settings.ServerPropertiesManager;
 
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.StringUtils;
 import org.globus.rsl.Binding;
 import org.globus.rsl.Bindings;
 import org.globus.rsl.NameOpValue;
 import org.globus.rsl.RslNode;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 
 public class RSLFactory {
 
 	private static RSLFactory singleton = null;
 
 	public static final Logger myLogger = LoggerFactory.getLogger(RSLFactory.class);
 
 	public static RSLFactory getRSLFactory() {
 		if (singleton == null) {
 			singleton = new RSLFactory();
 		}
 		return singleton;
 	}
 
 	private InformationManager infoManager = null;
 
 	private int commitTimeout = 5;
 
 	// private final InformationManager informationManager =
 	// InformationManagerManager
 	// .getInformationManager(ServerPropertiesManager
 	// .getInformationManagerConf());
 
 	private void addWhenNotBlank(RslNode rsl, String attribute, String value) {
 		if (StringUtils.isNotBlank(value)) {
 			rsl.add(new NameOpValue(attribute, NameOpValue.EQ, value));
 		}
 	}
 
 	public RslNode create(final Document jsdl, final String fqan)
 			throws RSLCreationException {
 		final RslNode result = new RslNode();
 
 		if (fqan == null) {
 			throw new RSLCreationException("fqan cannot be null");
 		} else if (!Pattern.matches("(/\\S+)+", fqan)) {
 			throw new RSLCreationException("fqan " + fqan
 					+ " format is invalid");
 		}
 
 		// add executable
 
 		final String executable = JsdlHelpers
 				.getPosixApplicationExecutable(jsdl);
 		if (executable == null) {
 			throw new RSLCreationException("executable is not set");
 		}
 		result.add(new NameOpValue("executable", NameOpValue.EQ, executable));
 
 		// add arguments
 
 		final String[] arguments = JsdlHelpers
 				.getPosixApplicationArguments(jsdl);
 		if ((arguments != null) && (arguments.length > 0)) {
 			result.add(new NameOpValue("arguments", NameOpValue.EQ, arguments));
 		}
 
 		// add modules
 		final String[] modules = getModulesFromMDS(jsdl);
 		for (final String module : modules) {
 			result.add(new NameOpValue("module", NameOpValue.EQ, module));
 		}
 
 		// job name
 		String jobname = JsdlHelpers.getJobname(jsdl);
 		if (ServerPropertiesManager.getShortenJobname()) {
 			jobname = (jobname == null) ? "" : jobname.substring(Math.max(0,
 					jobname.length() - 6));
 		}
 
 		addWhenNotBlank(result, "jobname", jobname);
 
 		String workingDirectory = JsdlHelpers.getWorkingDirectory(jsdl);
 
 		if (StringUtils.isBlank(workingDirectory)) {
 			throw new RSLCreationException("No working directory specified.");
 		}
 
 		workingDirectory = FileManager.ensureTrailingSlash(workingDirectory);
 
 		addWhenNotBlank(result, "directory", workingDirectory);
 
 		addWhenNotBlank(result, "stdout",
 				workingDirectory + JsdlHelpers.getPosixStandardOutput(jsdl));
 		addWhenNotBlank(result, "stderr",
 				workingDirectory + JsdlHelpers.getPosixStandardError(jsdl));
 
 		String stdin = JsdlHelpers.getPosixStandardInput(jsdl);
 		if (StringUtils.isNotBlank(stdin)) {
 			addWhenNotBlank(result, "stdin", workingDirectory + stdin);
 		}
 
 
 		addWhenNotBlank(result, "email_address", JsdlHelpers.getEmail(jsdl));
 		if (JsdlHelpers.getSendEmailOnJobFinish(jsdl)) {
 			result.add(new NameOpValue("email_on_termination", NameOpValue.EQ,
 					"yes"));
 		}
 		if (JsdlHelpers.getSendEmailOnJobStart(jsdl)) {
 			result.add(new NameOpValue("email_on_execution", NameOpValue.EQ,
 					"yes"));
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
 				throw new RSLCreationException("queue " + queue
 						+ ": invalid format");
 			}
 
 		} else {
 			throw new RSLCreationException("queue not set");
 		}
 
 		// Add "jobtype" if mpi
 		final int pcount = JsdlHelpers.getProcessorCount(jsdl);
 		String jobtype = JsdlHelpers.getArcsJobType(jsdl);
 		final int hcount = JsdlHelpers.getResourceCount(jsdl);
 
 		if (StringUtils.isBlank(jobtype)) {
 			jobtype = "single";
 
 		}
 		result.add(new NameOpValue("count", NameOpValue.EQ, "" + pcount));
 		result.add(new NameOpValue("jobtype", NameOpValue.EQ, jobtype));
 		if (hcount > 0) {
 			result.add(new NameOpValue("hostCount", NameOpValue.EQ, "" + hcount));
 		}
 
 		// total memory
 		Long memory = JsdlHelpers.getTotalMemoryRequirement(jsdl);
 		if ((memory != null) && (memory >= 0)) {
 			// for mpi, we need the specified memory per cpu
 			memory = memory * pcount;
 			result.add(new NameOpValue("max_memory", NameOpValue.EQ, ""
 					+ (memory / (1024 * 1024))));
 		}
 
 		// Add "maxWallTime" node
 		final int walltime = JsdlHelpers.getWalltime(jsdl);
 		if (walltime > 0) {
 			result.add(new NameOpValue("max_wall_time", NameOpValue.EQ, ""
 					+ (walltime / 60)));
 		}
 
 		// environment variables
 
 		final Map<String, String> env = JsdlHelpers
 				.getPosixApplicationEnvironment(jsdl);
 		if ((env != null) && (env.size() > 0)) {
 			final Bindings b = new Bindings("environment");
 			for (final String var : env.keySet()) {
 				b.add(new Binding(var, env.get(var)));
 			}
 			result.add(b);
 		}
 
 		result.add(new NameOpValue("save_state", NameOpValue.EQ, "yes"));
 		result.add(new NameOpValue("two_phase", NameOpValue.EQ,
 				this.commitTimeout + ""));
 		result.add(new NameOpValue("vo", NameOpValue.EQ, fqan));
 
 		return result;
 	}
 
 	private String[] getModulesFromMDS(final Document jsdl) {
 
 
 		String[] modules_string = JsdlHelpers.getModules(jsdl);
 		if (modules_string != null) {
 			return modules_string;
 		}
 
 		if ( this.infoManager == null ) {
 			myLogger.debug("No infomanager set, not looking up modules...");
 			return new String[]{};
 		}
 		// mds based
 		final String application = JsdlHelpers.getApplicationName(jsdl);
 		String version = JsdlHelpers.getApplicationVersion(jsdl);
 		final String[] subLocs = JsdlHelpers.getCandidateHosts(jsdl);
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
 
 		final Package pkg = this.infoManager.getPackage(application, version, subLoc);
		if ( pkg == null ) {
			return new String[]{};
		}
 
 		final Module m = pkg.getModule();
 		if (m == null) {
 			return new String[] {};
 		}
 		modules_string = new String[] { m.getModule() };
 		if (modules_string != null) {
 			return modules_string;
 		} else {
 			return new String[] {};
 		}
 
 	}
 
 	public void setCommitTimeout(int commitTimeout) {
 		this.commitTimeout = commitTimeout;
 	}
 
 	public void setInformationManager(InformationManager im) {
 		this.infoManager = im;
 	}
 }
