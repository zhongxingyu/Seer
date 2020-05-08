 package org.oecd.ant.git;
 
 import java.io.File;
 import java.text.MessageFormat;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 import org.eclipse.jgit.api.CloneCommand;
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.lib.Constants;
 import org.eclipse.jgit.lib.Ref;
 import org.oecd.ant.git.nested.CredentialsElement;
 
 public class CloneTask extends Task {
 
 	private File repo;
 	private String uri;
 	private boolean bare;
 	private String branch;
 	private CredentialsElement credentials;
 
 	private boolean verbose;
 
 	public File getRepo() {
 		return repo;
 	}
 
 	public void setRepo(File repo) {
 		this.repo = repo;
 	}
 
 	public String getUri() {
 		return uri;
 	}
 
 	public void setUri(String uri) {
 		this.uri = uri;
 	}
 
 	public boolean isBare() {
 		return bare;
 	}
 
 	public void setBare(boolean bare) {
 		this.bare = bare;
 	}
 
 	public String getBranch() {
 		return branch;
 	}
 
 	public void setBranch(String branch) {
 		this.branch = Constants.R_HEADS + branch;
 	}
 
 	public boolean isVerbose() {
 		return verbose;
 	}
 
 	public void setVerbose(boolean verbose) {
 		this.verbose = verbose;
 	}
 
 	public CredentialsElement getCredentials() {
 		return credentials;
 	}
 
 	public void addCredentials(CredentialsElement credentials) {
 		if (this.credentials != null)
 			throw new BuildException(":only_one");
 		this.credentials = credentials;
 	}
 
 	@Override
 	public final void execute() throws BuildException {
 		try {
 			log(MessageFormat.format("Cloning ''{0}'' to ''{1}''", getUri(), getRepo()));
 
 			CloneCommand cc = Git.cloneRepository();
 
 			cc.setDirectory(getRepo());
 			cc.setURI(getUri());
 
 			cc.setBare(isBare());
 
 			if (getBranch() != null) {
 				cc.setBranch(branch);
 			}
 
 			if (getCredentials() != null) {
 				cc.setCredentialsProvider(getCredentials().toCredentialsProvider());
 			}
 
 			cc.setProgressMonitor(new SimpleProgressMonitor(this));
 
 			Git git = cc.call();
 
 			// some testing on current HEAD, wrong branch parameter can leave the clone in a bad state
 			Ref headRef = git.getRepository().getRef(Constants.HEAD);
 			if (headRef != null) {
 				if (!headRef.isSymbolic())
 					throw new BuildException("detached HEAD");
 
 				String head = headRef.getTarget().getName();
				if (branch != null && !head.equals(branch))
 					throw new BuildException(branch + " doesn't seem to exists");
 
 				Ref testRef = git.getRepository().getAllRefs().get(head);
 				if (testRef == null)
 					throw new BuildException(head + " doesn't seem to exists");
 
 				log("HEAD is " + head);
 			} else {
 				throw new BuildException("Missing HEAD");
 			}
 		} catch (BuildException ex) {
 			throw ex;
 		} catch (Exception ex) {
 			BuildException be = new BuildException(ex);
 			throw be;
 		}
 	}
 }
