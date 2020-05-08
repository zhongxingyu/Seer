 package testCallgraphanalyzer;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Set;
 
 import org.junit.Test;
 
 import callgraphanalyzer.CallGraphAnalyzer;
 import callgraphanalyzer.Comparator;
 import callgraphanalyzer.Comparator.CompareResult;
 import callgraphanalyzer.Comparator.ModifiedMethod;
 import db.CallGraphDb;
 
 
 public class TestComparator {
 
 	private static CallGraphDb db = new CallGraphDb();
 	private String dbName ="testproject";
 	private String dbBranch = "master";
 	
 	@Test
 	public void testComparatorConstructor() {
 		db.connect(dbName);
 		db.setBranchName(dbBranch);
 		
 		String newCommit = "719e209640b77f685e6ea38ca78e1addebdcecd7";
 		String oldCommit = "2969fc3a59fbac659524685a5198673c561aac0c";
 		Comparator compare = new Comparator(db, newCommit, oldCommit);
 		
 		assertEquals(compare.newCommit.getCommit_id(), newCommit);
 		assertEquals(compare.oldCommit.getCommit_id(), oldCommit);
 		
 		Comparator compare1 = new Comparator(db, oldCommit, newCommit);
 		
 		assertEquals(compare1.newCommit.getCommit_id(), newCommit);
 		assertEquals(compare1.oldCommit.getCommit_id(), oldCommit);
 	}
 
 	@Test
 	public void testComparatorBinaryFile2ConsecutiveCommits() {
 		db.connect(dbName);
 		db.setBranchName(dbBranch);
 		
 		String newCommit = "719e209640b77f685e6ea38ca78e1addebdcecd7";
 		String oldCommit = "93851ad94b877dbe13ea85a3ed420bc0810ac835";
 		Comparator compare = new Comparator(db, newCommit, oldCommit);
 		
 		// CommitsInBetween also included the newer Commit, so eventhough no commits in between, the size is 1 
 		assertEquals(compare.commitsInBetween.size(), 1);
 		assertTrue(compare.commitsInBetween.containsKey(newCommit));
 		
 		// Test diff Binary File
 		Set<String> files = compare.commitsInBetween.get(newCommit);
 		assertTrue(files.size() == 4);
 		assertTrue(files.contains("src/binary/eula.dll"));
 		assertTrue(files.contains("src/binary/attach.png"));
 		assertTrue(files.contains("src/binary/cancel.png"));
 		assertTrue(files.contains("src/binary/activation.jar"));
 		
 		// Compare file
 		compare.CompareCommits();
 		CompareResult result = compare.getCompareResult();
 		assertEquals(result.modifiedBinaryFiles.size(), 4);
 		assertTrue( result.modifiedBinaryFiles.containsKey("src/binary/eula.dll") &&
 					result.modifiedBinaryFiles.containsKey("src/binary/attach.png") &&
 					result.modifiedBinaryFiles.containsKey("src/binary/cancel.png") &&
 					result.modifiedBinaryFiles.containsKey("src/binary/activation.jar"));
 	}
 	
 	@Test
 	public void testComparatorBinaryFileNoneConsecutiveCommits() {
 		db.connect(dbName);
 		db.setBranchName(dbBranch);
 		
 		String newCommit = "f788f2183525e2e0fe2ab7137c90a37e45ab214e";
 		String oldCommit = "76f22a97e1a29ef6a18f9a3553e01b26298f597b";
 		Comparator compare = new Comparator(db, newCommit, oldCommit);
 		
 		// 12 commits
 		assertEquals(compare.commitsInBetween.size(), 12);
 		assertTrue(compare.commitsInBetween.containsKey(newCommit));
 		
 		// First commit after initial commit
 		Set<String> files = compare.commitsInBetween.get("ea276fbd7e46f84e02574823169cc06982542f0f");
 		assertTrue(files.size() == 6);
 		assertTrue(files.contains(".classpath"));
 		assertTrue(files.contains(".gitignore"));
 		assertTrue(files.contains(".project"));
 		assertTrue(files.contains("src/test/A.java"));
 		assertTrue(files.contains("src/test/B.java"));
 		assertTrue(files.contains("src/test/C.java"));
 		
 		// Compare file
 		compare.CompareCommits();
 		CompareResult result = compare.getCompareResult();
 		assertEquals(result.modifiedBinaryFiles.size(), 0);
 		assertEquals(result.deletedFiles.size(), 0);
 		assertEquals(result.modifiedFileMethodMap.size(), 0);
 		assertEquals(result.addedFiles.size(), 19);
 		
 		assertTrue( result.addedFiles.contains("src/binary/eula.dll") &&
 					result.addedFiles.contains("src/binary/attach.png") &&
 					result.addedFiles.contains("src/binary/cancel.png") &&
 					result.addedFiles.contains("src/binary/activation.jar"));
 	}
 	
 	@Test
 	public void testComparatorCompareCommitsWithFirstCommit() {
 		db.connect(dbName);
 		db.setBranchName(dbBranch);
 		
 		String newCommit = "f788f2183525e2e0fe2ab7137c90a37e45ab214e";
 		String oldCommit = "76f22a97e1a29ef6a18f9a3553e01b26298f597b";
 		Comparator compare = new Comparator(db, newCommit, oldCommit);
 		compare.CompareCommits();
 		
 		// 19 files added
 		CompareResult result = compare.getCompareResult();
 		assertEquals(result.addedFiles.size(), 19);
 		assertEquals(result.deletedFiles.size(),0);
 		assertEquals(result.modifiedFileMethodMap.size(),0);
 		assertEquals(result.modifiedBinaryFiles.size(),0);
 	}
 	
 	@Test
 	public void testComparatorCompareCommitsConsecutive() {
 		db.connect(dbName);
 		db.setBranchName(dbBranch);
 		
 		String newCommit = "753bd7d12df07cd285956b67c33bacb7fa4fbf2a";
 		String oldCommit = "f4672afd22f79dde79a4248b66616591db40befc";
 		Comparator compare = new Comparator(db, newCommit, oldCommit);
 		compare.CompareCommits();
 		
 		// 2 files added, 2 modified
 		CompareResult result = compare.getCompareResult();
 		assertEquals(result.addedFiles.size(), 2);
 		assertEquals(result.deletedFiles.size(),0);
 		assertEquals(result.modifiedFileMethodMap.size(),2);
 		assertEquals(result.modifiedBinaryFiles.size(),0);
 		
 		// Function changed
 		ModifiedMethod methods = result.modifiedFileMethodMap.get("src/pak/B.java");
		assertEquals(methods.oldMethods.size(), 3);
		assertEquals(methods.newMethods.size(), 6);
 		
 		methods = result.modifiedFileMethodMap.get("src/test/Child.java");
 		assertEquals(methods.oldMethods.size(),0);
 		assertEquals(methods.newMethods.size(),1);
 	}
 }
