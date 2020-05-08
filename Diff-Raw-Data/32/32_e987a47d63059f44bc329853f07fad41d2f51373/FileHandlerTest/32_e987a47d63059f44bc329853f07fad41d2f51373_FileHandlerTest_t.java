 package test.integration.filing;
 
 import java.io.IOException;
 
 import jcifs.smb.NtlmPasswordAuthentication;
 import jcifs.smb.SmbException;
 import jcifs.smb.SmbFile;
 import junit.framework.Assert;
 
 import org.junit.After;
 import org.junit.Before;
import org.junit.Ignore;
 import org.junit.Test;
 
 import filing.FileHandler;
 
@Ignore
 public class FileHandlerTest
 {
 	private String user = "rubie:5haft03/27O2";
 	
 	NtlmPasswordAuthentication auth;
 	
 	private String sourceFolder = "smb://ls-01/share/fileHandlerIntegrationTest/source/";
 	private String destinationFolder = "smb://ls-01/share/fileHandlerIntegrationTest/destination/";
 	
 	@Before
 	public void setUp() throws Exception
 	{
 		auth = new NtlmPasswordAuthentication(user);
 		
 		createFolder(this.sourceFolder);
 		createFolder(this.destinationFolder);
 		
 		populateSourceFolder();
 	}
 
 	private void createFolder(String folderPath) throws IOException
 	{
 		SmbFile folder = new SmbFile(folderPath, auth);
 		
 		deleteFolderAndContents(folder);
 		
 		folder.mkdirs();
 	}
 
 	private void deleteFolderAndContents(SmbFile folder)
 	{
 		try
 		{
 			if(folder.exists())
 			{
 				SmbFile[] contents = folder.listFiles();
 				
 				for (int i = 0; i < contents.length; i++) 
 					if (contents[i].isFile())
 						contents[i].delete();
 					else
 						deleteFolderAndContents(contents[i]);
 				
 				folder.delete();
 			}
 		} catch (SmbException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	private void populateSourceFolder()
 	{
 		try 
 		{
 			SmbFile emptyFolder = new SmbFile(combinePathWithSourceFolder("emptyFolder"), auth);
 			emptyFolder.mkdir();
 			SmbFile notEmptyFolder = new SmbFile(combinePathWithSourceFolder("notEmptyFolder"), auth);
 			notEmptyFolder.mkdir();
 
 			SmbFile file = new SmbFile(combinePathWithSourceFolder("aa.mp4"), auth);
 			file.createNewFile();
 			
 			file = new SmbFile(combinePathWithSourceFolder("bb.mp4"), auth);
 			file.createNewFile();
 	
 			file = new SmbFile(combinePathWithSourceFolder("aa.avi"), auth);
 			file.createNewFile();
 			
 			file = new SmbFile(combinePathWithSourceFolder("bb.avi"), auth);
 			file.createNewFile();
 	
 			file = new SmbFile(combinePathWithSourceFolder("aa.mkv"), auth);
 			file.createNewFile();
 			
 			file = new SmbFile(combinePathWithSourceFolder("bb.mkv"), auth);
 			file.createNewFile();
 
 			file = new SmbFile(combinePathWithSourceFolder("notEmptyFolder/aa.mp4"), auth);
 			file.createNewFile();
 			
 			file = new SmbFile(combinePathWithSourceFolder("notEmptyFolder/bb.mp4"), auth);
 			file.createNewFile();
 	
 			file = new SmbFile(combinePathWithSourceFolder("notEmptyFolder/aa.avi"), auth);
 			file.createNewFile();
 			
 			file = new SmbFile(combinePathWithSourceFolder("notEmptyFolder/bb.avi"), auth);
 			file.createNewFile();
 
 			file = new SmbFile(combinePathWithSourceFolder("notEmptyFolder/aa.mkv"), auth);
 			file.createNewFile();
 			
 			file = new SmbFile(combinePathWithSourceFolder("notEmptyFolder/bb.mkv"), auth);
 			file.createNewFile();
 
 			file = new SmbFile(this.destinationFolder+"bb.mkv", auth);
 			file.createNewFile();
 
 		} catch (IOException e)	{}
 		finally {}
 	}	
 	
 	private String combinePathWithSourceFolder(String path)
 	{
 		return this.sourceFolder + path;
 	}
 
 	@After
 	public void tearDown() throws Exception
 	{
 		deleteFolderAndContents(new SmbFile("smb://ls-01/share/fileHandlerIntegrationTest/", auth));
 	}
 
 	@Test
 	public void getFileNames_MultipleFilesAndFolder_ListsAllFilesIncludingSubFolders()
 	{
 		FileHandler fileHandler = new FileHandler(auth);
 		int result = fileHandler.getFileNames(sourceFolder).size();
 		
 		Assert.assertTrue(result == 8);
 	}
 	
 	@Test
 	public void moveFile_aaMp4_ReturnsTrue()
 	{
 		FileHandler fileHandler = new FileHandler(auth);
 		Assert.assertTrue(fileHandler.moveFile(combinePathWithSourceFolder("aa.mp4"), this.destinationFolder+"aa.mp4"));
 	}	
 
 	@Test
 	public void moveFile_aaMp4_MovesFile()
 	{
 		FileHandler fileHandler = new FileHandler(auth);
 		fileHandler.moveFile(combinePathWithSourceFolder("aa.mp4"), this.destinationFolder+"aa.mp4");
 		Assert.assertTrue(fileHandler.getFileNames(this.destinationFolder).size() == 1);
 	}	
 
 	@Test
 	public void moveFile_aaMp4_DeletesSource()
 	{
 		FileHandler fileHandler = new FileHandler(auth);
 		fileHandler.moveFile(combinePathWithSourceFolder("aa.mp4"), this.destinationFolder+"aa.mp4");
 		Assert.assertTrue(fileHandler.getFileNames(this.sourceFolder).size() == 7);
 	}	
 
 	@Test
 	public void moveFile_bbmp4_DeletesSource()
 	{
 		FileHandler fileHandler = new FileHandler(auth);
 		fileHandler.moveFile(combinePathWithSourceFolder("bb.mp4"), this.destinationFolder+"bb.mp4");
 		Assert.assertTrue(fileHandler.getFileNames(this.sourceFolder).size() == 7);
 	}	
 }
