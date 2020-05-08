 package com.enonic.autotests;
 
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import com.enonic.autotests.exceptions.ContentRepositoryException;
 import com.enonic.autotests.exceptions.ContentTypeException;
 import com.enonic.autotests.model.ContentRepository;
 import com.enonic.autotests.model.ContentType;
 import com.enonic.autotests.model.FilesContent;
 import com.enonic.autotests.pages.v4.adminconsole.content.CreateContentRepositoryWizardPage;
 import com.enonic.autotests.pages.v4.adminconsole.content.RepositoriesListFrame;
 import com.enonic.autotests.pages.v4.adminconsole.contenttype.ContentTypesFrame;
 import com.enonic.autotests.providers.ContentRepositoryProvider;
 import com.enonic.autotests.providers.ContentTypeTestsProvider;
 import com.enonic.autotests.services.v4.PageNavigatorV4;
 import com.enonic.autotests.testdata.contenttype.ContentConvertor;
 import com.enonic.autotests.testdata.contenttype.ContentRepositoryXml;
 import com.enonic.autotests.testdata.contenttype.ContentTypeXml;
 
 public class ContentTypeTests extends BaseTest{
 
 	@Test(description = "positive tests",dataProvider = "createContentTypePositive", dataProviderClass = ContentTypeTestsProvider.class)
 	public void testCreateContentTypePositive( ContentTypeXml ctypeXML){
 		logger.info(ctypeXML.getCaseInfo());
 		ContentType ctype = ContentConvertor.convertXmlDataToContentType(ctypeXML);		
 		adminConsoleServiceV4.createContentType(getTestSession(), ctype );
 		ContentTypesFrame frame = new ContentTypesFrame(getTestSession());
 		boolean isCreated = frame.verifyIsCreated(ctype.getName());
 		Assert.assertTrue(isCreated,"new Content Type was not found on the Content Types page! ");
 	}
	
 	@Test(dependsOnMethods="testCreateContentTypePositive",description = "negative tests",expectedExceptions=ContentTypeException.class,dataProvider = "createContentTypeNegative", dataProviderClass = ContentTypeTestsProvider.class)
 	public void createContentTypeNegative(ContentTypeXml ctypeXML){
 		logger.info(ctypeXML.getCaseInfo());
 		ContentType ctype = ContentConvertor.convertXmlDataToContentType(ctypeXML);		
 		adminConsoleServiceV4.createContentType(getTestSession(), ctype );
 		
 	}
 	
 	@Test(description="create new Content Repository with new Content Type",dependsOnMethods="testCreateContentTypePositive",dataProvider = "createContentRepositoryPositive", dataProviderClass = ContentRepositoryProvider.class)
 	public void createContentRepository(ContentRepositoryXml contentRepoXML){
 		logger.info(contentRepoXML.getCaseInfo());
 		ContentRepository cRepository = ContentConvertor.convertXmlDataToContentRepository(contentRepoXML);		
 		adminConsoleServiceV4.createContentRepository(getTestSession(), cRepository);
 		RepositoriesListFrame repositoryFrame = new RepositoriesListFrame(getTestSession());
 		repositoryFrame.verifyIsPresentedInTable(cRepository.getName());
 		
 	}
 	@Test(description="negative test",expectedExceptions = ContentRepositoryException.class,dependsOnMethods="testCreateContentTypePositive",dataProvider = "createContentRepositoryNegative", dataProviderClass = ContentRepositoryProvider.class)
 	public void createContentRepositoryNegative(ContentRepositoryXml contentRepoXML){
 		logger.info(contentRepoXML.getCaseInfo());
 		ContentRepository cRepository = ContentConvertor.convertXmlDataToContentRepository(contentRepoXML);		
 		adminConsoleServiceV4.createContentRepository(getTestSession(), cRepository);		
 	}       
 	
	@Test(dataProvider = "createContentRepositoryPositive", dataProviderClass = ContentRepositoryProvider.class)
 	public void testOpenRepositoryProperties(ContentRepositoryXml contentRepoXML){
 		
 		ContentRepository cRepository = ContentConvertor.convertXmlDataToContentRepository(contentRepoXML);
 		CreateContentRepositoryWizardPage page = PageNavigatorV4.openRepositoryProperties(getTestSession(), cRepository.getName());
 		boolean result = page.verifyData(cRepository);
 		Assert.assertTrue(result,"expected and actual ContentRepository's properties are not equals!!!");
 	}    
 	
 	
 	@Test(dataProvider = "deleteRepository", dataProviderClass = ContentRepositoryProvider.class)
 	public void testCreateAndDeleteContentRepository(ContentRepositoryXml contentRepoXML){		
 		ContentRepository cRepository = ContentConvertor.convertXmlDataToContentRepository(contentRepoXML);
 		RepositoriesListFrame repositoriesFrame = adminConsoleServiceV4.createContentRepository(getTestSession(), cRepository);
 		boolean isPresent = repositoriesFrame.verifyIsPresentedInTable(cRepository.getName());
 		Assert.assertTrue(isPresent,"new repository was not created!");
 		repositoriesFrame = adminConsoleServiceV4.deleteContentType(getTestSession(), cRepository.getName());
 		isPresent = repositoriesFrame.verifyIsPresentedInTable(cRepository.getName());
 		Assert.assertFalse(isPresent,"repository was not deleted!");
 	}   
 	
 	//@Test(dataProvider = "", dataProviderClass = ContentRepositoryProvider.class)
 	//public void testVerifyAllInputs(ContentRepositoryXml contentRepoXML){		
 	//	ContentRepositoryFrame frame = PageNavigatorV4.openContentRepositoryDashboard(getTestSession(), repName);
 	//	AddNewContentWizardPage wizardPage = frame.openAddContentWizardPage(repName);
 	//}   
 ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Test(dataProvider = "deleteRepository", dataProviderClass = ContentRepositoryProvider.class)
 	public void testAddContentToRepository(ContentRepositoryXml contentRepoXML){
		ContentRepository cRepository = null;// ContentConvertor.convertXmlDataToContentRepository(contentRepoXML);
 		//cRepository.getTopCategory().getContentType().getContentHandler();
 		FilesContent fc = null;
 		adminConsoleServiceV4.addContentToRepository(getTestSession(),cRepository,fc);
 		System.out.println("");
 	}    
 
 
 }
