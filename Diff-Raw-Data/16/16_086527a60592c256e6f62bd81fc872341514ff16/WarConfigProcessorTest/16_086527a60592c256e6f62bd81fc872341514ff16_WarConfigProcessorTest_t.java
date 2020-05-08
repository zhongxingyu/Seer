 package com.photon.phresco.util;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.xml.bind.JAXBException;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.Test;
 
import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.plugins.model.Assembly.FileSets.FileSet;
 import com.photon.phresco.plugins.util.WarConfigProcessor;
 
 public class WarConfigProcessorTest {
 	
 	private File warFile = new File("src/test/resources/war-config.xml");
 	private File warWrongFile = new File("src/test/resources/war-conf.xml");
 	private FileSet fileSet;
 	
 	@Test
 	public void testSave() throws JAXBException, IOException {
 		WarConfigProcessor warConfigProcessor = new WarConfigProcessor(warFile);
 		warConfigProcessor.save();		
 	}
 	
 	@Test
 	public void testGetFileSet() throws JAXBException, IOException {
 		WarConfigProcessor warConfigProcessor = new WarConfigProcessor(warFile);
 		fileSet = warConfigProcessor.getFileSet("excludeFile");
 		assertNotNull(fileSet);
 		warConfigProcessor.createFileSet(fileSet);
 	}
 	
 	@Test
 	public void testWithWrongFile() throws JAXBException, IOException {
 		WarConfigProcessor warConfigProcessor = new WarConfigProcessor(warWrongFile);
 		warWrongFile.delete();
 	}
 	
 	@Test
    public void delete() throws IOException, PhrescoException {
		File directory = new File(Utility.getProjectHome() + "wp1-wordpress3.4.2");
 		FileUtils.deleteDirectory(directory);
 		
     }
 }
