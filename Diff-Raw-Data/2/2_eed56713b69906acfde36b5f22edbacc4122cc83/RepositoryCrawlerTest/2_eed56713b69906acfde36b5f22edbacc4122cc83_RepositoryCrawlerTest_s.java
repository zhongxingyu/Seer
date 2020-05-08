 package edu.illinois.gitsvn.infra;
 
 import java.io.File;
 
 import org.eclipse.jgit.api.Git;
 import org.gitective.tests.GitTestCase;
 import org.junit.Before;
 import org.junit.Test;
 
 public class RepositoryCrawlerTest extends GitTestCase{
 	
 	private RepositoryCrawler crawler;
 
 	@Before
 	public void setUp() throws Exception {
 		super.setUp();
 		crawler = new RepositoryCrawler();
 	}
 	
 	@Test
 	public void testProducesCorrectOutput() throws Exception {
 		add("test.java", "Some java program", "first");
 		add("test2.java", "Some other java program", "second");
 		mv("test.java", "test_rename.java");
 		add("readme","A non-java file", "forth");
 		
 		crawler.crawlRepo(Git.open(testRepo));
 		
		File file = new File("mumu.txt");
 		assertTrue(file.exists());
 	}
 }
