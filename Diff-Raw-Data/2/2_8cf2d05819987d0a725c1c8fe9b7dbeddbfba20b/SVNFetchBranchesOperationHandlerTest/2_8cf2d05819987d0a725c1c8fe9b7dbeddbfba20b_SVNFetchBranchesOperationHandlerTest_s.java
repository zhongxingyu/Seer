 package org.codefaces.core.svn.operations;
 
 import static org.junit.Assert.*;
 
 import java.net.MalformedURLException;
 import java.util.Collection;
 
 import org.codefaces.core.connectors.SCMConnector;
 import org.codefaces.core.models.Repo;
 import org.codefaces.core.models.RepoBranch;
 import org.codefaces.core.models.RepoCredential;
 import org.codefaces.core.operations.SCMOperationHandler;
 import org.codefaces.core.operations.SCMOperationParameters;
 import org.codefaces.core.svn.operations.SVNConstants;
 import org.codefaces.core.svn.operations.SVNFetchBranchesOperationHandler;
 import org.junit.Before;
 import org.junit.Test;
 
public class SvnFetchBranchesQueryTest {
 	
 	private static String TEST_URL_WITHOUT_BRANCHES = "http://code.djangoproject.com/svn/django/trunk";
 	private static String TEST_URL_WITH_BRANCHES = "http://code.djangoproject.com/svn/django";
 	
 	private static String TEST_BRAHCH_NAME = "0.96-bugfixes";
 	
 	private SVNFetchBranchesOperationHandler handler;
 	private SCMConnector connector;
 
 	@Before
 	public void setUp(){
 		connector = new MockSCMConnector(TestSvnJavaHlClientAdaptor.getClient());
 		handler = new SVNFetchBranchesOperationHandler();
 	}
 	
 	private Repo createMockRepo(String url, String username, String password){
 		RepoCredential credential = new RepoCredential(null, username, password);
 		return new Repo(null, url, url, credential);
 	}
 	
 	@Test
 	public void directoriesWithoutBranchesFolderShouldOnlyContainsDefaultBranch(){
 		SCMOperationParameters para = SCMOperationParameters.newInstance();
 		Repo repo = createMockRepo(TEST_URL_WITHOUT_BRANCHES, null, null);
 		para.addParameter(SCMOperationHandler.PARA_REPO, repo);
 		
 		Collection<RepoBranch> branches = handler.execute(connector, para);
 		
 		assertEquals(1, branches.size());
 		
 		RepoBranch defaultBranch = ((RepoBranch) branches.toArray()[0]);
 		assertEquals(SVNConstants.DEFAULT_BRANCH, defaultBranch.getName());
 	}
 		
 	
 	@Test
 	public void directoriesInsideBranchesFolderShouldBeReturnedWhenThereIsAFolderCalledBranches()
 			throws MalformedURLException{
 		SCMOperationParameters para = SCMOperationParameters.newInstance();
 		Repo repo = createMockRepo(TEST_URL_WITH_BRANCHES, null, null);
 		para.addParameter(SCMOperationHandler.PARA_REPO, repo);
 		Collection<RepoBranch> allBranches = handler.execute(connector, para);
 		
 		//TEST_BRANCH_NAME should be one of the branches
 		//also for defaultBranch
 		boolean branchFound = false;
 		boolean defaultBranchFound = false;
 		for(RepoBranch branch: allBranches){
 			if(branch.getName().equals(TEST_BRAHCH_NAME)){
 				branchFound = true;
 				assertTrue(branch.isMaster() == false);
 			}
 			else if(branch.getName().equals(SVNConstants.DEFAULT_BRANCH)){
 				defaultBranchFound = true;
 				assertTrue(branch.isMaster() == true);
 			}
 		}
 		assertTrue(branchFound);
 		assertTrue(defaultBranchFound);
 	}
 }
