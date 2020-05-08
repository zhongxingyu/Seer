 package edu.mayo.cts2.framework.plugin.service.exist.profile;
 
 import org.junit.Before;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.xmldb.api.base.XMLDBException;

 import edu.mayo.cts2.framework.plugin.service.exist.dao.ExistManager;
 import edu.mayo.cts2.framework.plugin.service.exist.profile.update.ExistChangeSetService;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(value="classpath:/exist-test-context.xml")
 public class BaseServiceDbCleaningBase {
 
 	@Autowired 
 	public ExistChangeSetService changeSetService;
 	
 	@Autowired
     public ExistManager existManager;
 	
 	@Before
 	public void cleanExist() throws XMLDBException {
		  existManager.getCollectionManagementService().removeCollection("/db");
 	}
 }
