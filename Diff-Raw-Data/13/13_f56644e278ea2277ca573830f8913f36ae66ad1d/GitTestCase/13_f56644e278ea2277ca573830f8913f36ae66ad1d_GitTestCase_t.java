 package org.fedoraproject.eclipse.packager.tests.git.utils;
 
 import org.eclipse.core.resources.IProject;
 import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
 import org.fedoraproject.eclipse.packager.LookasideCache;
 
 import junit.framework.TestCase;
 
 public class GitTestCase extends TestCase {
 	
 	private GitTestProject project;
 	private IProject iProject;
 	private FedoraProjectRoot fedoraprojectRoot;
 	
 	@Override
 	protected void setUp() throws Exception {
 		project = new GitTestProject("eclipse-rpm-editor");
 		iProject = project.getProject();
 		// create a fedoraprojectRoot for this project
		fedoraprojectRoot = new FedoraProjectRoot(iProject, "org.fedoraproject.eclipse.packager.DownloadHandler");		
 	}
 
 	/**
 	 * @return the fedoraprojectRoot
 	 */
 	public FedoraProjectRoot getFedoraprojectRoot() {
 		return fedoraprojectRoot;
 	}
 
 	/**
 	 * @param fedoraprojectRoot the fedoraprojectRoot to set
 	 */
 	public void setFedoraprojectRoot(FedoraProjectRoot fedoraprojectRoot) {
 		this.fedoraprojectRoot = fedoraprojectRoot;
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		project.dispose();
 	}
 
 	/**
 	 * @return the project
 	 */
 	public GitTestProject getProject() {
 		return project;
 	}
 
 	/**
 	 * @param project the project to set
 	 */
 	public void setProject(GitTestProject project) {
 		this.project = project;
 	}
 
 	/**
 	 * @return the iProject
 	 */
 	public IProject getiProject() {
 		return iProject;
 	}
 
 	/**
 	 * @param iProject the iProject to set
 	 */
 	public void setiProject(IProject iProject) {
 		this.iProject = iProject;
 	}
 }
