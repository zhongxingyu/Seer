 package org.eclipse.mylyn.reviews.r4e.core.versions;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.Assert;
 import junit.framework.TestCase;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IStorage;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.egit.core.op.AddToIndexOperation;
 import org.eclipse.egit.core.op.CommitOperation;
 import org.eclipse.egit.core.project.RepositoryMapping;
 import org.eclipse.egit.core.test.GitTestCase;
 import org.eclipse.egit.core.test.TestRepository;
 import org.eclipse.egit.core.test.TestUtils;
 import org.eclipse.jgit.lib.Repository;
 import org.eclipse.mylyn.reviews.r4e.core.TstGeneral;
 import org.eclipse.mylyn.versions.core.ChangeSet;
 import org.eclipse.mylyn.versions.core.ScmCore;
 import org.eclipse.mylyn.versions.core.ScmRepository;
 import org.eclipse.mylyn.versions.core.spi.ScmConnector;
 import org.eclipse.team.core.history.IFileRevision;
 import org.eclipse.team.core.history.provider.FileRevision;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author lmcalvs
  *
  * @version $Revision$
  */
 public class MVersionsAPITest extends GitTestCase {
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 	private IProject			fIProject		= null;
 	private TestRepository		testRepo		= null;
 	private Repository			repo			= null;
 	private ReviewsVersionsIF	versionsProx	= null;
 	private List<IResource>		resources		= new ArrayList<IResource>();
 	private IFile				ifileA			= null;
 	private IFile				ifileB			= null;
 	private IFile				ifileC			= null;
 	private IFile				ifileD			= null;
 	private ScmConnector		fConnector		= null;
 	private ScmRepository		fScmRepo			= null;
 	/**
 	 * Perform pre-test initialization
 	 *
 	 * @throws Exception
 	 *
 	 * @see TestCase#setUp()
 	 */
 	@Before
 	public void setUp() throws Exception {
 		super.setUp();
 
 		TstGeneral.activateTracer();
 		fIProject = project.getProject();
 		fIProject.open(null);
 		testRepo = new TestRepository(gitDir);
 		testRepo.connect(fIProject);
 		repo = RepositoryMapping.getMapping(fIProject).getRepository();
 
 		versionsProx = ReviewsVersionsIFFactory.instance.getVersionsIF(fIProject);
 
 		// Add a couple of files to the project - First commit
 		ifileA = testUtils.addFileToProject(fIProject, "sub/a.txt", "a text");
 		ifileB = testUtils.addFileToProject(fIProject, "sub/b.txt", "b text");
 		// Add a couple of files to the project - Second Commit
 		ifileC = testUtils.addFileToProject(fIProject, "sub2/c.txt", "c text");
 		ifileD = testUtils.addFileToProject(fIProject, "sub2/d.txt", "d text");
 
 		// First Commit: Add folder to index and commit
 		resources.add(project.getProject().getFolder("sub"));
 		new AddToIndexOperation(resources).execute(null);
 		CommitOperation commitOperation = new CommitOperation(null, null, null, TestUtils.AUTHOR, TestUtils.COMMITTER,
 				"first commit");
 		commitOperation.setCommitAll(true);
		commitOperation.setRepos(new Repository[] { repo });
 		commitOperation.execute(null);
 
 		// Second Commit:
 		resources.clear();
 		resources.add(project.getProject().getFolder("sub2"));
 		new AddToIndexOperation(resources).execute(null);
 		commitOperation = new CommitOperation(null, null, null, TestUtils.AUTHOR, TestUtils.COMMITTER, "Second commit");
 		commitOperation.setCommitAll(true);
		commitOperation.setRepos(new Repository[] { repo });
 		commitOperation.execute(null);
 
 		fIProject.refreshLocal(IProject.DEPTH_INFINITE, null);
 		fConnector = ScmCore.getConnector(fIProject);
 		fScmRepo = fConnector.getRepository(fIProject, null);
 	}
 
 	/**
 	 * Perform post-test clean up
 	 *
 	 * @throws Exception
 	 *
 	 * @see TestCase#tearDown()
 	 */
 	@After
 	public void tearDown() throws Exception {
 		testRepo.disconnect(fIProject);
 		fIProject.close(null);
 		testRepo.dispose();
 		repo = null;
 		super.tearDown();
 	}
 
 	/**
 	 * Make sure two commits are described
 	 */
 	@Test
 	public void fetchCommitListTest() {
 		IProgressMonitor monitor = null;
 		List<ChangeSet> commits = null;
 
 		try {
 			commits = fConnector.getChangeSets(fScmRepo, monitor);
 		} catch (CoreException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		Assert.assertNotNull("Returned list of commits is null", commits);
 		Assert.assertEquals(2, commits.size());
 
 	}
 
 	@Test
 	public void fetchCommitTest() {
 		IProgressMonitor monitor = null;
 		List<ChangeSet> commits = null;
 		ChangeSet change = null;
 		try {
 			commits = fConnector.getChangeSets(fScmRepo, monitor);
 			ChangeSet dComit = commits.get(0);
 			final String contentId = dComit.getId();
 
 			IFileRevision commitRev = new FileRevision() {
 
 				public IFileRevision withAllProperties(IProgressMonitor monitor) throws CoreException {
 					return null;
 				}
 
 
 				public boolean isPropertyMissing() {
 					return false;
 				}
 
 				public IStorage getStorage(IProgressMonitor monitor) throws CoreException {
 					return null;
 				}
 
 				public String getName() {
 					return contentId;
 				}
 
 				public String getContentIdentifier() {
 					return contentId;
 				}
 			};
 
 			change = fConnector.getChangeSet(fScmRepo, commitRev, null);
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 
 		Assert.assertNotNull("Returned change commit is null", change);
 		Assert.assertEquals(2, change.getChanges().size());
 
 	}
 }
