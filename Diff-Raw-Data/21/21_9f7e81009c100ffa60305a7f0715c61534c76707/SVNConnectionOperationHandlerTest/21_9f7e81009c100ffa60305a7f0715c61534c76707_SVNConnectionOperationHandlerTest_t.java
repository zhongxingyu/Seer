 package org.codefaces.core.svn.internal.operations;
 import static org.junit.Assert.*;
 
 import org.codefaces.core.connectors.SCMConnector;
 import org.codefaces.core.connectors.SCMResponseException;
 import org.codefaces.core.models.Repo;
 import org.codefaces.core.operations.SCMOperationParameter;
 import org.codefaces.core.operations.SCMOperationParameters;
 import org.codefaces.core.svn.internal.operations.SVNConnectionOperationHandler;
 import org.junit.Before;
 import org.junit.Test;
 
 
 public class SVNConnectionOperationHandlerTest {
 
	private static final String TEST_NORMAL_URL_REQUIRE_USERNAME = "http://subclipse.tigris.org/svn/subclipse/trunk";
	private static final String TEST_NORMAL_URL_WITH_TRAILING_SLASH = "http://subclipse.tigris.org/svn/subclipse/trunk/";
	private static final String TEST_NORMAL_USERNAME = "guest";
 	private static final String TEST_NORMAL_PASSWORD = "''";
 	
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
 		para.addParameter(SCMOperationParameter.URL, TEST_NORMAL_URL_REQUIRE_USERNAME);
 		para.addParameter(SCMOperationParameter.USER, TEST_NORMAL_USERNAME);
 		para.addParameter(SCMOperationParameter.PASSWORD, TEST_NORMAL_PASSWORD);
 		
 		Repo svnRepo = connectionHandler.execute(connector, para);
 		assertEquals(TEST_NORMAL_URL_REQUIRE_USERNAME, svnRepo.getUrl());
 		assertEquals(TEST_NORMAL_USERNAME, svnRepo.getCredential().getUser());
 		assertEquals(TEST_NORMAL_PASSWORD, svnRepo.getCredential().getPassword());
 	}
 	
 	@Test
 	public void trailingSlashIsRemovedWhenTheInputUrlContainsTrailingSlash(){
 		SCMOperationParameters para = SCMOperationParameters.newInstance();
 		para.addParameter(SCMOperationParameter.URL, TEST_NORMAL_URL_WITH_TRAILING_SLASH);
 		para.addParameter(SCMOperationParameter.USER, TEST_NORMAL_USERNAME);
 		para.addParameter(SCMOperationParameter.PASSWORD, TEST_NORMAL_PASSWORD);
 		
 		Repo svnRepo = connectionHandler.execute(connector, para);
 		assertEquals(TEST_NORMAL_URL_REQUIRE_USERNAME, svnRepo.getUrl());		
 	}
 	
 	@Test(expected = SCMResponseException.class)
 	public void throwExceptionWhenNoSuchRepository(){
 		SCMOperationParameters para = SCMOperationParameters.newInstance();
 		para.addParameter(SCMOperationParameter.URL, TEST_NO_SUCH_URL);
 		connectionHandler.execute(connector, para);
 	}
 	
 	
 	@Test(expected = SCMResponseException.class)
 	public void throwExceptionWhenNoPermission(){
 		SCMOperationParameters para = SCMOperationParameters.newInstance();
 		para.addParameter(SCMOperationParameter.URL, TEST_NO_PERMISSION);
 		connectionHandler.execute(connector, para);
 	}
 	
 }
