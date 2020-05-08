 package org.codefaces.core.svn.operations;
 
 import static org.junit.Assert.*;
 
 import org.codefaces.core.connectors.SCMConnector;
 import org.codefaces.core.models.Repo;
 import org.codefaces.core.models.RepoBranch;
 import org.codefaces.core.models.RepoCredential;
 import org.codefaces.core.models.RepoFile;
 import org.codefaces.core.models.RepoFileInfo;
 import org.codefaces.core.operations.SCMOperationHandler;
 import org.codefaces.core.operations.SCMOperationParameters;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 
 import org.junit.Before;
 import org.junit.Test;
 
public class SVNFetchFileInfoOperationHandlerTest {
 	
 	private static String TEST_URL_WITHOUT_BRANCHES = 
 		"http://code.djangoproject.com/svn/django/trunk";
 	private static String TEST_URL_WITH_BRANCHES = 
 		"http://code.djangoproject.com/svn/django";
 	private static final String TEST_USERNAME =  null;
 	private static final String TEST_PASSWORD = null;
 	
 	private static final String TEST_FILENAME_IN_TRUNK = "README";
 	private static final String TEST_FILEPATH_IN_TRUNK = "/README";
 	private static final String TEST_PREFIX_FOR_TESTFILE_IN_TRUNK = "Django";
 	
 	private static final String TEST_BRANCH = "0.96-bugfixes";
 	private static final String TEST_FILENAME_IN_BRANCH = "MANIFEST.in";
 	private static final String TEST_FILEPATH_IN_BRANCH = "/MANIFEST.in";
 	private static final String TEST_PREFIX_FOR_TESTFILE_IN_BRANCH = "include AUTHORS";
 	
 	private SVNFetchFileInfoOperationHandler handler;
 	private SCMConnector connector;
 
 	@Before
 	public void setUp(){
 		connector = new MockSCMConnector(TestSvnJavaHlClientAdaptor.getClient());
 		handler = new SVNFetchFileInfoOperationHandler();
 	}
 	
 	
 	private RepoFile createMockFile(String repoUrl, String username,
 			String password, boolean inMasterBranch, String branchName,
 			String fileName, final String fileFullPath) {
 		RepoCredential credential = new RepoCredential(null, username, password);
 		Repo repo = new Repo(null, repoUrl, repoUrl, credential);
 		RepoBranch branch = new RepoBranch(repo, "branchId", branchName,
 				inMasterBranch);
 		return new RepoFile(branch.getRoot(), branch.getRoot(), "fileId", fileName){
 			@Override
 			public IPath getFullPath(){
 				return new Path(fileFullPath);
 			}
 		};
 	}
 	
 	@Test
 	public void gettingFileInDefaultBranch(){
 		RepoFile file = createMockFile(TEST_URL_WITHOUT_BRANCHES,
 				TEST_USERNAME, TEST_PASSWORD, true,
 				SVNConstants.DEFAULT_BRANCH, TEST_FILENAME_IN_TRUNK,
 				TEST_FILEPATH_IN_TRUNK);
 		SCMOperationParameters para = SCMOperationParameters.newInstance();
 		para.addParameter(SCMOperationHandler.PARA_REPO_FILE, file);
 		RepoFileInfo info = handler.execute(connector, para);
 		assertTrue(info.getContent().startsWith(TEST_PREFIX_FOR_TESTFILE_IN_TRUNK));
 	}
 
 	@Test
 	public void gettingFileInsideABranch(){
 		RepoFile file = createMockFile(TEST_URL_WITH_BRANCHES,
 				TEST_USERNAME, TEST_PASSWORD, false,
 				TEST_BRANCH, TEST_FILENAME_IN_BRANCH,
 				TEST_FILEPATH_IN_BRANCH);
 		SCMOperationParameters para = SCMOperationParameters.newInstance();
 		para.addParameter(SCMOperationHandler.PARA_REPO_FILE, file);
 		RepoFileInfo info = handler.execute(connector, para);
 		assertTrue(info.getContent().startsWith(TEST_PREFIX_FOR_TESTFILE_IN_BRANCH));
 	}
 	
 }
