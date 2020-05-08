 package gov.usgs.gdp.bean;
 
 import static org.junit.Assert.*;
 import gov.usgs.gdp.helper.FileHelper;
 
 import java.io.File;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.log4j.Logger;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 
 public class FilesBeanTest {
 	private static org.apache.log4j.Logger log = Logger.getLogger(FilesBeanTest.class);
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		log.debug("Started testing class");
 	} 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 		log.debug("Ended testing class");
 	}
 	private String tempDir = "";
 
 	private String seperator = "";
 	
 	@Before
 	public void setUp() throws Exception {
 		this.tempDir = FileHelper.getSystemTemp() 
 		+ this.seperator 
 		+ "GDP-APP-TEMP" 
 		+ this.seperator
 		+ "testing-feel-free-to-delete"
 		+ this.seperator;
 	(new File(this.tempDir)).mkdir();
 		
 		// Copy example files 
 		ClassLoader cl = Thread.currentThread().getContextClassLoader(); 
 		URL sampleFileLocation = cl.getResource("Sample_Files/");
 		if (sampleFileLocation != null) {
 			File sampleFiles = null;
 			try {
 				sampleFiles = new File(sampleFileLocation.toURI());
 			} catch (URISyntaxException e) {
 				assertTrue("Exception encountered: " + e.getMessage(), false);
 			}
 			FileHelper.copyFileToFile(sampleFiles, this.tempDir + this.seperator);
 		} else {
 			fail("Sample files could not be loaded for test");
 		}
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		FileUtils.deleteDirectory((new File(this.tempDir)));
 	}
 	
 	@Test
 	public void testGetShapeFileSetList() {
 		String shpFile = this.tempDir 
 		+ this.seperator 
 		+ "Sample_Files" 
 		+ this.seperator
 		+ "Shapefiles" 
 		+ this.seperator
 		+ "hru20VSR.SHP";
 		
 		String prjFile = this.tempDir 
 		+ this.seperator 
 		+ "Sample_Files" 
 		+ this.seperator
 		+ "Shapefiles" 
 		+ this.seperator
 		+ "hru20VSR.PRJ";
 		
 		String dbfFile = this.tempDir 
 		+ this.seperator 
 		+ "Sample_Files" 
 		+ this.seperator
 		+ "Shapefiles" 
 		+ this.seperator
 		+ "hru20VSR.DBF";
 		
 		FilesBean filesBean = new FilesBean();
 		Collection<File> files = new ArrayList<File>();
 		files.add(new File(shpFile));
 		files.add(new File(prjFile));
 		files.add(new File(dbfFile));
 		filesBean.setFiles(files);
 		ShapeFileSetBean result = filesBean.getShapeFileSetBean();
 		assertNotNull(result);
 	}
 	
 	@Test
 	public void testGetFilesBeanSetList() {
 		Collection<File> files = FileHelper.getFileCollection(this.tempDir 
 				+ this.seperator 
 				+ "Sample_Files"
 				+ this.seperator , true);
 		assertNotNull(files);
 		List<FilesBean> filesList = FilesBean.getFilesBeanSetList(files);
 		assertNotNull(filesList);
 		assertFalse(filesList.isEmpty());
 	}
 	
 }
