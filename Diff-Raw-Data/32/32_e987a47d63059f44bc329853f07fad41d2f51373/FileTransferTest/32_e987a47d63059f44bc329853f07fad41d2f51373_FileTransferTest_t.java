 package test.unit.filing;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import common.pojo.RetrievedEpisode;
 
 import test.unit.fakeObjects.FakeFileHandler;
 import test.unit.fakeObjects.FakeShowFilter;
 import filing.FileTransfer;
 
 public class FileTransferTest
 {
 
 	@Before
 	public void setUp() throws Exception
 	{
 	}
 
 	@After
 	public void tearDown() throws Exception
 	{
 	}
 
 	@Test()
 	public void process_NoFilesFound_0()
 	{
 		String sourceFolder = validPath;
 		String destinationRootFolder = validPath;
 		FakeShowFilter filter = new FakeShowFilter("aa");
 		FakeFileHandler fileHandler = new FakeFileHandler();
 				
 		FileTransfer transfer = new FileTransfer(sourceFolder, destinationRootFolder, filter, fileHandler);
 		List<RetrievedEpisode> processedFiles = transfer.process();
 		
 		Assert.assertTrue(processedFiles.size() == 0);
 	}
 	
 	@Test()
 	public void process_1FileFound_1()
 	{
 		String sourceFolder = validPath;
 		String destinationRootFolder = validPath;
 		FakeShowFilter filter = new FakeShowFilter("aa");
 		
 		List<String> fakeFileList = new ArrayList<String>();
		fakeFileList.add("smb://server/folder/aa");
 		FakeFileHandler fileHandler = new FakeFileHandler(fakeFileList);
 				
 		FileTransfer transfer = new FileTransfer(sourceFolder, destinationRootFolder, filter, fileHandler);
 		List<RetrievedEpisode> processedFiles = transfer.process();
 		
 		Assert.assertTrue(processedFiles.size() == 1);
 	}
 	
 	@Test()
 	public void process_2FileFound_1()
 	{
 		String sourceFolder = validPath;
 		String destinationRootFolder = validPath;
 		FakeShowFilter filter = new FakeShowFilter("aa");
 		
 		List<String> fakeFileList = new ArrayList<String>();
		fakeFileList.add("smb://server/folder/aa");
		fakeFileList.add("smb://server/folder/bb");
 		FakeFileHandler fileHandler = new FakeFileHandler(fakeFileList);
 				
 		FileTransfer transfer = new FileTransfer(sourceFolder, destinationRootFolder, filter, fileHandler);
 		List<RetrievedEpisode> processedFiles = transfer.process();
 		
 		Assert.assertTrue(processedFiles.size() == 1);
 	}
 	
 	private String validPath = "c:\\";
 }
 
