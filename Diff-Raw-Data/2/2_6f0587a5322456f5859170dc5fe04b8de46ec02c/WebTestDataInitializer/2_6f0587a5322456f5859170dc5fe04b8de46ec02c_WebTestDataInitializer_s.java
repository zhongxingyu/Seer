 /*
  * 
  */
 package uk.ac.ebi.fg.myequivalents.webservices.server.test;
 
 import java.io.StringReader;
 
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 import javax.xml.bind.JAXBException;
 
 import uk.ac.ebi.fg.myequivalents.managers.ServiceManager;
 import uk.ac.ebi.fg.myequivalents.managers.impl.base.BaseEntityMappingManager;
 import uk.ac.ebi.fg.myequivalents.model.Repository;
 import uk.ac.ebi.fg.myequivalents.model.ServiceCollection;
 
 /**
  * This defines some test data if the system property uk.ac.ebi.fg.myequivalents.test_flag is true. It is attached to 
  * the servlet engine via web.xml.
  *
  * <dl><dt>date</dt><dd>Sep 11, 2012</dd></dl>
  * @author Marco Brandizi
  *
  */
 public class WebTestDataInitializer implements ServletContextListener
 {
 
 	@Override
 	public void contextDestroyed ( ServletContextEvent e )
 	{
 		if ( !"true".equals ( System.getProperty ( "uk.ac.ebi.fg.myequivalents.test_flag", null ) ) ) return;
 
 		BaseEntityMappingManager emapMgr = new BaseEntityMappingManager ();
 		emapMgr.deleteEntities ( "test.testweb.service6:acc3" );
 		emapMgr.deleteMappings ( "test.testweb.service7:acc1" );
 	}
 
 	@Override
 	public void contextInitialized ( ServletContextEvent e )
 	{
 		if ( !"true".equals ( System.getProperty ( "uk.ac.ebi.fg.myequivalents.test_flag", null ) ) ) return;
 	
 		System.out.println ( "\n\n _________________________________ Creating Test Data ________________________________ \n\n\n" );
 		
 		try 
 		{
 			ServiceManager serviceMgr = new ServiceManager ();
 			
 			ServiceCollection sc1 = new ServiceCollection ( 
 				"test.testweb.serviceColl1", null, "Test Service Collection 1", "The Description of the SC 1" 
 			);
 			serviceMgr.storeServiceCollections ( sc1 );
 			
 			Repository repo1 = new Repository ( "test.testweb.repo1", "Test Repo 1", "The Description of Repo1" );
 			serviceMgr.storeRepositories ( repo1 );
 	
 			String testServiceXml =
 				"<service-items>\n" +
 				"  <services>\n" +
 		    "    <service uri-pattern='http://somewhere.in.the.net/testweb/service6/someType1/${accession}'\n" + 
 				"           uri-prefix='http://somewhere.in.the.net/testweb/service6/'\n" + 
 		    "           entity-type='testweb.someType1' title='A Test Service 6' name='test.testweb.service6'>\n" +
 		    "      <description>The Description of a Test Service 6</description>\n" + 
 		    "    </service>\n" + 
 		    "    <service entity-type='testweb.someType7' title='A Test Service 7' name='test.testweb.service7'" +
 		    "           repository-name = 'test.testweb.repo1'" +
 		    "           service-collection-name = 'test.testweb.serviceColl1'>\n" +
 		    "      <description>The Description of a Test Service 7</description>\n" +
 		    "    </service>\n" +
 		    "    <service uri-prefix='http://somewhere-else.in.the.net/testweb/service8/'\n" +
 		    "             entity-type='testweb.someType2' title='A Test Service 8' name='test.testweb.service8'" +
 		    "             repository-name = 'test.testweb.addedRepo1'>\n" + 
 		    "      <description>The Description of a Test Service 8</description>\n" + 
 		    "    </service>\n" +
 		    "  </services>\n" +
 		    "  <repositories>" +
 		    "  		<repository name = 'test.testweb.addedRepo1'>\n" +
 		    "       <description>A test Added Repo 1</description>\n" +
 		    "     </repository>\n" +
 		    "  </repositories>\n" +
 		    "  <service-collections>" +
 		    "  		<service-collection name = 'test.testweb.added-sc-1' title = 'Added Test SC 1'>\n" +
 		    "       <description>A test Added SC 1</description>\n" +
 		    "     </service-collection>\n" +
 		    "  </service-collections>\n" +
 		    "</service-items>";		
 	
 			serviceMgr.storeServicesFromXML ( new StringReader ( testServiceXml ) );
 			
 			BaseEntityMappingManager emapMgr = new BaseEntityMappingManager ();
 			emapMgr.storeMappings (
 				"test.testweb.service6:acc1", "test.testweb.service8:acc2", 
 				"test.testweb.service6:acc3", "test.testweb.service6:acc4" 
 			);
 			emapMgr.storeMappingBundle ( 
 				"test.testweb.service7:acc1", "test.testweb.service6:acc4", "test.testweb.service6:acc1"
 			);
 			
 		}
 		catch ( JAXBException ex ){
 			throw new RuntimeException ( "Internal error while loading test data: '" + ex.getMessage () + "'", ex );
 		}
 	}
 }
