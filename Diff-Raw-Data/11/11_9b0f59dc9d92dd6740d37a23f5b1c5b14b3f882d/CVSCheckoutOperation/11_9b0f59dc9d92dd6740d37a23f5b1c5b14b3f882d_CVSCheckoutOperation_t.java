 package org.fedoraproject.eclipse.packager.cvs;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.team.internal.ccvs.core.CVSTag;
 import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
 import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
 import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
 import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
 import org.eclipse.team.internal.ccvs.core.resources.RemoteFolder;
 import org.eclipse.team.internal.ccvs.core.resources.RemoteModule;
 import org.eclipse.team.internal.ccvs.ui.operations.CheckoutSingleProjectOperation;
 
 /**
  * Operation for checking out a module from CVS
  *
  */
 @SuppressWarnings("restriction")
 public class CVSCheckoutOperation {
 
 	
 	private String moduleName;
 	private IProject project;
 	private String scmURL;
 	private IRunnableWithProgress runnable;
 	private boolean hasRun = false;
 	
 	/**
 	 * Set the module name, which should get checked out.
 	 * 
 	 * @param moduleName
 	 */
 	public void setModuleName(String moduleName) {
 		this.moduleName = moduleName;
 	}
 	
 	/**
 	 * Set the base URL to checkout from.
 	 * 
 	 * @param scmURL
 	 */
 	public void setScmURL(String scmURL) {
 		this.scmURL = scmURL;
 	}
 	
 	/**
 	 * Prepare the checkout operation.
 	 * 
 	 * @throws Exception
 	 */
 	public void prepareRunable() throws Exception {
 		// make sure module name is properly set
 		if (moduleName == null) {
 			throw new IllegalStateException();
 		}
 		ICVSRepositoryLocation repo = CVSRepositoryLocation.fromString(getScmURL());
 		ICVSRemoteFolder remoteFolder = repo.getRemoteFolder("rpms", null);
 		RemoteModule remoteModule = new RemoteModule(moduleName, (RemoteFolder) remoteFolder, repo, moduleName,new LocalOption[]{}, new CVSTag(), true);
 		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 		project = root.getProject(moduleName);
 		runnable = new CheckoutSingleProjectOperation(null, remoteModule, project, null, false);
 		hasRun = true;
 	}
 	
 	/**
 	 * @return the scmURL
 	 */
 	public String getScmURL() {
 		// return the default if not set explicitly
 		if (this.scmURL == null) {
 			return CVSUtils.getDefaultCVSBaseUrl();
 		}
 		return scmURL;
 	}
 	
 	/**
 	 * 
 	 * @return the runnable.
 	 * 
 	 * @throws IllegalAccessException
 	 */
 	public IRunnableWithProgress getRunnable() throws IllegalAccessException {
 		if (!hasRun) {
 			throw new IllegalAccessException();
 		}
 		return this.runnable;
 	}
 	
 	/**
 	 * 
 	 * @return the underlying project of this op.
 	 * 
 	 * @throws IllegalAccessException
 	 */
 	public IProject getProject() throws IllegalAccessException {
 		if (!hasRun) {
 			throw new IllegalAccessException();
 		}
 		return this.project;
 	}
 	
 }
