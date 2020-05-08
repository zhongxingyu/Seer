 package darep.repos;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import junit.framework.Assert;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import darep.DarepController;
 import darep.Helper;
 
 public class DatasetTest {
 	
 	private DarepController darep = new DarepController();
 	private Database database;
 	private File testdir = new File("testdir");
 	private File testFileInRepo;
 	private File testFileNotInRepo;
 	private Metadata metadata;
 	private String repoName = "testrepo";
 	
 	private final String testFileName = "TESTFILE.TXT";
 	private final String testFileNotInRepoName = "TESTFILE2.TXT";
 
 	@Before
 	public void setUp() throws Exception {
 		createTestFiles();
 		createMetaData();
 		darep.processCommand(getArgs("add " + testFileInRepo.getPath()));
 		
 		database = new Database(repoName);
 	}
 	
 	private void createMetaData() {
 		metadata = new Metadata(testFileNotInRepoName, testFileNotInRepoName,
 					"teh_d3scr!pto0r", 0, 0, repoName);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		Helper.deleteDir(testdir);
 		Helper.deleteDir(new File(repoName));
 	}
 
 	@Test
 	public void testReadDataset() throws RepositoryException {
 		Dataset.readDataset("TESTFILE.TXT", database);
 	}
 	
 	@Test
 	public void testCreateDataset() {
 		Dataset ds = Dataset.createNewDataset(testFileNotInRepo, metadata, database);
 		Assert.assertEquals(1, ds.getMetadata().getNumberOfFiles());
		Assert.assertEquals(testFileNotInRepoName.length(), ds.getMetadata().getSize());
 	}
 	
 	private String[] getArgs(String str) {
 		return (str + " -r " + repoName).split(" "); 
 	}
 	
 	private void createTestFiles() {
 		testdir.mkdirs();
 		testFileInRepo = new File(testdir, testFileName);
 		testFileNotInRepo = new File(testdir, testFileNotInRepoName);
 		try {
 			testFileInRepo.createNewFile();
 			FileWriter writer = new FileWriter(testFileInRepo);
 			try {
 				writer.write("FU METADATA");
 			} finally {
 				writer.close();
 			}
 			
 			testFileNotInRepo.createNewFile();
 			writer = new FileWriter(testFileInRepo);
 			try {
 				for (int i = 0; i < 100; i++) {
 					writer.write("lal" + i + "\n");
 				}
 			} finally {
 				writer.close();
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
