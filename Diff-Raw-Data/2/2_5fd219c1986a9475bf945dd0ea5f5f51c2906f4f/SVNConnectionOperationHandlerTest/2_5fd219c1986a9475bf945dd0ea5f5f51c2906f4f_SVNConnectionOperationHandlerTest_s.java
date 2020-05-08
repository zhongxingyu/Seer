 package org.codefaces.core.svn.operations;
 import static org.junit.Assert.*;
 
 import org.codefaces.core.connectors.SCMConnector;
 import org.codefaces.core.connectors.SCMResponseException;
 import org.codefaces.core.models.Repo;
 import org.codefaces.core.operations.SCMOperationHandler;
 import org.codefaces.core.operations.SCMOperationParameters;
 import org.junit.Before;
 import org.junit.Test;
 
 
public class SvnRepoQueryTest {
 
 	private static final String TEST_NORMAL_URL = "http://subclipse.tigris.org/svn/subclipse/trunk";
 	private static final String TEST_NORMAL_URL_WITH_TRAILING_SLASH = "http://subclipse.tigris.org/svn/subclipse/trunk/";
 	private static final String TEST_NORMAL_USERNAME = "guest";
 	private static final String TEST_NORMAL_PASSWORD = null;
 	
 	private static final String TEST_USERNAME_IN_URL = "http://guest@subclipse.tigris.org/svn/subclipse/trunk";
 	
 	private static final String TEST_NO_SUCH_URL = "http://svn.nosuchurl.org/svn";
 	private static final String TEST_NO_PERMISSION = "https://secure.jms1.net";
 	
 	private SVNConnectionOperationHandler connectionHandler;
 	private SCMConnector connector;
 
 	@Before
 	public void setUp(){
 		connector = new MockSCMConnector(TestSvnJavaHlClientAdaptor.getClient());
 		connectionHandler = new SVNConnectionOperationHandler();
 	}
 
 	@Test
 	public void credentialShouldBeSetWhenUsernameAndPasswordArePassedAsParameters(){
 		SCMOperationParameters para = SCMOperationParameters.newInstance();
 		para.addParameter(SCMOperationHandler.PARA_URL, TEST_NORMAL_URL);
 		para.addParameter(SCMOperationHandler.PARA_USERNAME, TEST_NORMAL_USERNAME);
 		para.addParameter(SCMOperationHandler.PARA_PASSWORD, TEST_NORMAL_PASSWORD);
 		
 		Repo svnRepo = connectionHandler.execute(connector, para);
 		assertEquals(TEST_NORMAL_URL, svnRepo.getUrl());
 		assertEquals(TEST_NORMAL_USERNAME, svnRepo.getCredential().getUser());
 		assertEquals(TEST_NORMAL_PASSWORD, svnRepo.getCredential().getPassword());
 	}
 	
 	@Test
 	public void trailingSlashIsRemovedWhenTheInputUrlContainsTrailingSlash(){
 		SCMOperationParameters para = SCMOperationParameters.newInstance();
 		para.addParameter(SCMOperationHandler.PARA_URL, TEST_NORMAL_URL_WITH_TRAILING_SLASH);
 		para.addParameter(SCMOperationHandler.PARA_USERNAME, TEST_NORMAL_USERNAME);
 		para.addParameter(SCMOperationHandler.PARA_PASSWORD, TEST_NORMAL_PASSWORD);
 		
 		Repo svnRepo = connectionHandler.execute(connector, para);
 		assertEquals(TEST_NORMAL_URL, svnRepo.getUrl());		
 	}
 	
 	@Test
 	public void credentialShouldNotBeSetWhenUsernameAndPasswordAreNotPassedAsParameters(){
 		SCMOperationParameters para = SCMOperationParameters.newInstance();
 		para.addParameter(SCMOperationHandler.PARA_URL, TEST_USERNAME_IN_URL);
 		
 		Repo svnRepo = connectionHandler.execute(connector, para);
 		assertNull(svnRepo.getCredential().getUser());
 		assertNull(svnRepo.getCredential().getPassword());
 	}
 	
 	
 	@Test(expected = SCMResponseException.class)
 	public void throwExceptionWhenNoSuchRepository(){
 		SCMOperationParameters para = SCMOperationParameters.newInstance();
 		para.addParameter(SCMOperationHandler.PARA_URL, TEST_NO_SUCH_URL);
 		connectionHandler.execute(connector, para);
 	}
 	
 	
 	@Test(expected = SCMResponseException.class)
 	public void throwExceptionWhenNoPermission(){
 		SCMOperationParameters para = SCMOperationParameters.newInstance();
 		para.addParameter(SCMOperationHandler.PARA_URL, TEST_NO_PERMISSION);
 		connectionHandler.execute(connector, para);
 	}
 	
 }
