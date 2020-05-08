 package grisu.backend.model.fs;
 
 import grisu.backend.model.User;
 import grisu.backend.model.job.Job;
 import grisu.control.ServiceInterface;
 import grisu.control.exceptions.RemoteFileSystemException;
 import grisu.jcommons.constants.Constants;
 import grisu.model.FileManager;
 import grisu.model.dto.GridFile;
 
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * A plugin to list archived and running jobs in a tree-like structure.
  * 
  * The base url for this plugin is grid://jobs . The next token is either
  * "active" or "archived" and then comes the name of the job followed by this
  * job directories content.
  * 
  * @author Markus Binsteiner
  * 
  */
 public class JobsFileSystemPlugin implements VirtualFileSystemPlugin {
 
 	static final Logger myLogger = LoggerFactory
 			.getLogger(JobsFileSystemPlugin.class.getName());
 
 	public static final String IDENTIFIER = "jobs";
 	public static final String ACTIVE_IDENTIFIER = "active";
 	public static final String ARCHIVED_IDENTIFIER = "archived";
 	private final static String BASE = (ServiceInterface.VIRTUAL_GRID_PROTOCOL_NAME
 			+ "://" + IDENTIFIER);
 
 	private final User user;
 
 	public JobsFileSystemPlugin(User user) {
 		this.user = user;
 	}
 
 	public GridFile createGridFile(final String path, int recursiveLevels)
 			throws InvalidPathException {
 
 		if (recursiveLevels > 1) {
 			throw new RuntimeException(
 					"Recursion levels other than 1 not supported yet");
 		}
 
 		final int index = BASE.length();
 		final String importantUrlPart = path.substring(index);
 		final String[] tokens = StringUtils.split(importantUrlPart, '/');
 
 		if (tokens.length == 0) {
 			// means root of job virtual filesystem
 
 			GridFile result = null;
 			result = new GridFile(path, -1L);
 			result.setIsVirtual(true);
 			result.setPath(path);
 
 			if (recursiveLevels == 0) {
 				return result;
 			}
 
 			final GridFile active = new GridFile(
 					BASE + "/" + ACTIVE_IDENTIFIER, -1L);
 			active.setVirtual(true);
 			active.setPath(path + "/" + ACTIVE_IDENTIFIER);
 
 			final List<Job> jobs = user.getJobManager().getActiveJobs(
 					null, false);
 			for (final Job job : jobs) {
 				active.addSite(job
 						.getJobProperty(Constants.SUBMISSION_SITE_KEY));
 			}
 
 			result.addChild(active);
 
 			final GridFile archived = new GridFile(BASE + "/"
 					+ ARCHIVED_IDENTIFIER, -1L);
 			archived.setVirtual(true);
 			archived.setPath(path + "/" + ARCHIVED_IDENTIFIER);
 
 			result.addChild(archived);
 
 			return result;
 
 		} else if (tokens.length == 1) {
 			// means either archived or active
 
 			if (ACTIVE_IDENTIFIER.equals(tokens[0])) {
 				return getAllActiveJobsListing();
 			} else if (ARCHIVED_IDENTIFIER.equals(tokens[0])) {
 				return getAllArchivedJobsListing();
 			} else {
 				throw new InvalidPathException("Job state not recognized: "
 						+ tokens[0] + ". Needs to be either "
 						+ ARCHIVED_IDENTIFIER + " or " + ACTIVE_IDENTIFIER);
 			}
 		} else {
 
 			GridFile parent = null;
 			if (ACTIVE_IDENTIFIER.equals(tokens[0])) {
 				parent = getAllActiveJobsListing();
 			} else if (ARCHIVED_IDENTIFIER.equals(tokens[0])) {
 				parent = getAllArchivedJobsListing();
 			} else {
 				throw new InvalidPathException("Job state not recognized: "
 						+ tokens[0] + ". Needs to be either "
 						+ ARCHIVED_IDENTIFIER + " or " + ACTIVE_IDENTIFIER);
 			}
 
 			final String jobname = tokens[1];
 
 			if (!GridFile.getChildrenNames(parent).contains(jobname)) {
 				throw new InvalidPathException("Job not available: "
 						+ tokens[0] + "/" + jobname);
 			}
 
 			final GridFile jobDir = parent.getChild(jobname);
 
 			final StringBuffer url = new StringBuffer(jobDir.getUrl());
 			for (int i = 2; i < tokens.length; i++) {
 				url.append("/" + tokens[i]);
 			}
 
 			try {
 				final GridFile result = user.ls(url.toString(), 1);
 				result.setPath(path);
 				for (final GridFile f : result.getChildren()) {
 					f.setPath(result.getPath() + "/" + f.getName());
 				}
 				return result;
 			} catch (final RemoteFileSystemException e) {
 				throw new InvalidPathException(e);
 			}
 
 		}
 
 	}
 
 	private GridFile getAllActiveJobsListing() {
 
 		final GridFile active = new GridFile(BASE + "/" + ACTIVE_IDENTIFIER,
 				-1L);
 		active.setVirtual(true);
 		active.setPath(BASE + "/" + ACTIVE_IDENTIFIER);
 
 		final List<Job> jobs = user.getJobManager().getActiveJobs(null,
 				false);
 		for (final Job job : jobs) {
 			active.addSite(job.getJobProperty(Constants.SUBMISSION_SITE_KEY));
 
 			final String url = job.getJobProperty(Constants.JOBDIRECTORY_KEY);
 
 			if (StringUtils.isBlank(url)) {
 				continue;
 			}
 			final GridFile jobDir = new GridFile(url, -1L);
 			jobDir.setPath(BASE + "/" + ACTIVE_IDENTIFIER + "/"
 					+ FileManager.getFilename(url));
 			jobDir.addFqan(job.getFqan());
 			jobDir.addSite(job.getJobProperty(Constants.SUBMISSION_SITE_KEY));
 			jobDir.setIsVirtual(false);
 
 			active.addChild(jobDir);
 		}
 
 		return active;
 	}
 
 	private GridFile getAllArchivedJobsListing() {
 
 		GridFile result = null;
 		result = new GridFile(BASE + "/" + ARCHIVED_IDENTIFIER, -1L);
 		result.setIsVirtual(true);
 		result.setPath(BASE + "/" + ARCHIVED_IDENTIFIER);
 
 		final List<Job> jobs = user.getJobManager()
 				.getArchivedJobs(null);
 		for (final Job job : jobs) {
 
 			final String url = job.getJobProperty(Constants.JOBDIRECTORY_KEY);
 			final GridFile jobDir = new GridFile(url, -1L);
 			jobDir.setPath(BASE + "/" + ARCHIVED_IDENTIFIER + "/"
 					+ FileManager.getFilename(url));
 
 			// jobDir.addFqan(job.getFqan());
 			// jobDir.addSite(job.getJobProperty(Constants.SUBMISSION_SITE_KEY));
 			jobDir.setIsVirtual(false);
 
 			result.addChild(jobDir);
 		}
 
 		return result;
 	}
 
 }
