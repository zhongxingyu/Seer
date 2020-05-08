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
 import org.apache.log4j.Logger;
 
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
 
 	static final Logger myLogger = Logger.getLogger(JobsFileSystemPlugin.class
 			.getName());
 
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
 
 		int index = BASE.length();
 		String importantUrlPart = path.substring(index);
 		String[] tokens = StringUtils.split(importantUrlPart, '/');
 
 		if (tokens.length == 0) {
 			// means root of job virtual filesystem
 
 			GridFile result = null;
 			result = new GridFile(path, -1L);
 			result.setIsVirtual(true);
 			result.setPath(path);
 
 			if (recursiveLevels == 0) {
 				return result;
 			}
 
 			GridFile active = new GridFile(BASE + "/" + ACTIVE_IDENTIFIER, -1L);
 			active.setVirtual(true);
 			active.setPath(path + "/" + ACTIVE_IDENTIFIER);
 
 			List<Job> jobs = user.getActiveJobs(null, false);
 			for (Job job : jobs) {
 				active.addSite(job
 						.getJobProperty(Constants.SUBMISSION_SITE_KEY));
 			}
 
 			result.addChild(active);
 
 			GridFile archived = new GridFile(BASE + "/" + ARCHIVED_IDENTIFIER,
 					-1L);
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
 
 			String jobname = tokens[1];
 
 			if (!GridFile.getChildrenNames(parent).contains(jobname)) {
 				throw new InvalidPathException("Job not available: "
 						+ tokens[0] + "/" + jobname);
 			}
 
 			GridFile jobDir = parent.getChild(jobname);
 
 			StringBuffer url = new StringBuffer(jobDir.getUrl());
 			for (int i = 2; i < tokens.length; i++) {
 				url.append("/" + tokens[i]);
 			}
 
 			try {
 				GridFile result = user.ls(url.toString(), 1);
 				result.setPath(path);
 				for (GridFile f : result.getChildren()) {
 					f.setPath(result.getPath() + "/" + f.getName());
 				}
 				return result;
 			} catch (RemoteFileSystemException e) {
 				throw new InvalidPathException(e);
 			}
 
 		}
 
 
 	}
 
 	private GridFile getAllActiveJobsListing() {
 
 		GridFile active = new GridFile(BASE + "/" + ACTIVE_IDENTIFIER, -1L);
 		active.setVirtual(true);
 		active.setPath(BASE + "/" + ACTIVE_IDENTIFIER);
 
 		List<Job> jobs = user.getActiveJobs(null, false);
 		for (Job job : jobs) {
 			active.addSite(job
 					.getJobProperty(Constants.SUBMISSION_SITE_KEY));
 
 			String url = job.getJobProperty(Constants.JOBDIRECTORY_KEY);
 
 			if (StringUtils.isBlank(url)) {
 				continue;
 			}
 			GridFile jobDir = new GridFile(url, -1L);
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
 
 		List<Job> jobs = user.getArchivedJobs(null);
 		for (Job job : jobs) {
 
 			String url = job.getJobProperty(Constants.JOBDIRECTORY_KEY);
 			GridFile jobDir = new GridFile(url, -1L);
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
