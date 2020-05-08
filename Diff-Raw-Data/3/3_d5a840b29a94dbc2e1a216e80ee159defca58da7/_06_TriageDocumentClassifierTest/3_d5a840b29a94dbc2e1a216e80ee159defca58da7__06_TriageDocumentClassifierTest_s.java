 package edu.isi.bmkeg.skm.triage.bin;
 
 import java.io.File;
 import java.sql.SQLException;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.context.ApplicationContext;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import edu.isi.bmkeg.digitalLibrary.bin.AddPmidEncodedPdfsToCorpus;
 import edu.isi.bmkeg.digitalLibrary.bin.EditArticleCorpus;
 import edu.isi.bmkeg.digitalLibrary.dao.vpdmf.VpdmfCitationsDao;
 import edu.isi.bmkeg.skm.triage.cleartk.bin.PreprocessTriageScores;
 import edu.isi.bmkeg.skm.triage.cleartk.bin.TriageDocumentsClassifier;
 import edu.isi.bmkeg.utils.springContext.AppContext;
 import edu.isi.bmkeg.utils.springContext.BmkegProperties;
 import edu.isi.bmkeg.vpdmf.controller.VPDMfKnowledgeBaseBuilder;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations={ "/edu/isi/bmkeg/skm/triage/appCtx-VPDMfTest.xml"})
 public class _06_TriageDocumentClassifierTest {
 ApplicationContext ctx;
 	
 	String login, password, dbUrl;
 	String triageCorpusName, targetCorpusName;
 	File archiveFile, pmidFile_allChecked, triageCodes, pdfDir, pdfDir2, outDir;
 	VPDMfKnowledgeBaseBuilder builder;
 	
 	VpdmfCitationsDao dao;
 	
 	String queryString;
 	
 	@Before
 	public void setUp() throws Exception {
 		
 		ctx = AppContext.getApplicationContext();
 		BmkegProperties prop = (BmkegProperties) ctx.getBean("bmkegProperties");
 
 		login = prop.getDbUser();
 		password = prop.getDbPassword();
 		dbUrl = prop.getDbUrl();
 		String wd = prop.getWorkingDirectory();
 		
 		int l = dbUrl.lastIndexOf("/");
 		if (l != -1)
 			dbUrl = dbUrl.substring(l + 1, dbUrl.length());
 	
 		archiveFile = ctx.getResource(
 				"classpath:edu/isi/bmkeg/skm/triage/triage-mysql.zip").getFile();
 		
 		outDir = new File("target");
 		
 		File pdf1 = ctx.getResource(
 				"classpath:edu/isi/bmkeg/skm/triage/small/pdfs/19763139_A.pdf").getFile();
 		pdfDir = pdf1.getParentFile();
 		triageCodes = new File(pdfDir.getParent() + "/triageCodes.txt");
 
 		File pdf2 = ctx.getResource(
 				"classpath:edu/isi/bmkeg/skm/triage/small3/pdfs/21884797.pdf").getFile();
 		pdfDir2 = pdf2.getParentFile();
 
 		builder = new VPDMfKnowledgeBaseBuilder(archiveFile, 
 				login, password, dbUrl); 
 
 		try {
 			
 			builder.destroyDatabase(dbUrl);
 	
 		} catch (SQLException sqlE) {		
 			
 			// Gully: Make sure that this runs, avoid silly issues.
 			if( !sqlE.getMessage().contains("database doesn't exist") ) {
 				sqlE.printStackTrace();
 			}
 			
 		} 
 		
 		builder.buildDatabaseFromArchive();
 				
 		String[] args = new String[] { 
 				"-name", "GO", 
 				"-desc", "Test triage corpus", 
 				"-regex", "G", 
 				"-owner", "Gully Burns",
 				"-db", dbUrl, 
 				"-l", login, 
 				"-p", password 
 				};
 
 		EditArticleCorpus.main(args);
 
 		args = new String[] { 
 				"-name", "AP", 
 				"-desc", "Test target corpus", 
 				"-regex", "A", 
 				"-owner", "Gully Burns",
 				"-db", dbUrl, 
 				"-l", login, 
 				"-p", password 
 				};
 
 		EditArticleCorpus.main(args);
 
 		triageCorpusName = "triageCorpus1";
 		args = new String[] { 
 				"-name", triageCorpusName, 
 				"-desc", "Test target corpus", 
 				"-owner", "Gully Burns",
 				"-db", dbUrl, 
 				"-l", login, 
 				"-p", password 
 				};
 
 		EditTriageCorpus.main(args);
 
 		
 		args = new String[] { 
 				"-pdfs", pdfDir.getPath(), 
 				"-triageCorpus", triageCorpusName, 
 				"-db", dbUrl, 
 				"-l", login, 
 				"-p", password
 				};
 
 		BuildTriageCorpusFromPdfDir.main(args);
 		
 		args = new String[] { 
 				"-pdfs", pdfDir2.getPath(), 
 				"-triageCorpus", triageCorpusName, 
 				"-db", dbUrl, 
 				"-l", login, 
 				"-p", password
 				};
 
 		BuildTriageCorpusFromPdfDir.main(args);		
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		
 		builder.destroyDatabase(dbUrl);
 		
 	}
 		
 	@Test
 	public final void testTriageDocumentsClassifier() throws Exception {
 
 		targetCorpusName = "AP";
 		String[] args = new String[] {
 				"-train",
 				"-triageCorpus", triageCorpusName, 
 				"-targetCorpus", targetCorpusName, 
 				"-homeDir", outDir.getAbsolutePath(), 
 				"-l", login, 
 				"-p", password, 
 				"-db", dbUrl
 				};
 
 		TriageDocumentsClassifier.main(args);
 		
 		Assert.assertTrue(new File(outDir, dbUrl + "/" + targetCorpusName).exists());
 		
 		args[0] = "-predict";
 
 		TriageDocumentsClassifier.main(args);
 				
 		Assert.assertTrue(true);
 	}
 
 	@Test
 	public final void testTriageDocumentsClassifierAllTriageCorpora() throws Exception {

 		String origUserHomeProp = System.getProperty("user.home");
 		
 		try {
 			
 			File homeDir = new File(outDir, "userHome");
 			
 			System.setProperty("user.home", homeDir.getAbsolutePath());
 			
 			String[] args = new String[] {
 					"-train",
 					"-targetCorpus", targetCorpusName, 
 					"-l", login, 
 					"-p", password, 
 					"-db", dbUrl
 					};
 
 			TriageDocumentsClassifier.main(args);
 			
 			Assert.assertTrue(new File(homeDir, "bmkeg/" + dbUrl + "/" + targetCorpusName).exists());			
 			
 		} finally {
 		
 			if (origUserHomeProp != null) {
 				System.setProperty("user.home", origUserHomeProp);				
 			}
 				
 			
 		}
 		
 	}
 
 }
 
