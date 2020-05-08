 package edu.isi.bmkeg.vpdmf.bin;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.context.ApplicationContext;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import edu.isi.bmkeg.uml.model.UMLmodel;
 import edu.isi.bmkeg.utils.springContext.AppContext;
 import edu.isi.bmkeg.utils.springContext.BmkegProperties;
 import edu.isi.bmkeg.vpdmf.controller.VPDMfKnowledgeBaseBuilder;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations={ "/edu/isi/bmkeg/vpdmf/appCtx-VPDMfTest.xml"})
 public class BuildVpdmfModelMavenProjectTest {
 	
 	ApplicationContext ctx;
 
 	String login, password, dbUrl;
 	File spec;
	File ftdSpecFile, peopleSpecFile, digLibSpecFile, triageSpecFile;

 	@Before
 	public void setUp() throws Exception {
 		
 		ctx = AppContext.getApplicationContext();
 		BmkegProperties prop = (BmkegProperties) ctx.getBean("bmkegProperties");
 
 		login = prop.getDbUser();
 		password = prop.getDbPassword();
 		dbUrl = prop.getDbUrl();
 		
 		int l = dbUrl.lastIndexOf("/");
 		if (l != -1)
 			dbUrl = dbUrl.substring(l + 1, dbUrl.length());
 	
//		spec = ctx.getResource(
//				"classpath:edu/isi/bmkeg/vpdmf/ooevv/ooevv_vpdmf.xml").getFile();
		ftdSpecFile = ctx.getResource(
				"classpath:edu/isi/bmkeg/vpdmf/ftd/ftd_vpdmfSpec.xml").getFile();

		digLibSpecFile = ctx.getResource(
				"classpath:edu/isi/bmkeg/vpdmf/digitalLibrary/digitalLibrary_vpdmfSpec.xml").getFile();

		peopleSpecFile = ctx.getResource(
				"classpath:edu/isi/bmkeg/vpdmf/people/people_vpdmfSpec.xml").getFile();

		triageSpecFile = ctx.getResource(
				"classpath:edu/isi/bmkeg/vpdmf/triage/triage_vpdmfSpec.xml").getFile();
 		
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		
 	}
 	
	@Test
 	public final void testRunExecWithFullPaths() throws Exception {
 				
 		String[] args = new String[] { 
				triageSpecFile.getParent(),
				digLibSpecFile.getParent(),
				ftdSpecFile.getParent(),
				peopleSpecFile.getParent(),
				"target",
				"1.1.3-SNAPSHOT"
 				};
 		
 		BuildVpdmfModelMavenProject.main(args);
 				
 	}
 		
 }
 
