 package br.com.caelum.zhit.infra;
 
import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 
 import org.junit.Test;
 
 public class GitTreeInflaterTest {
 
 	@Test
 	public void should_read_tree_object_file() {
 		GitTreeInflater inflater = new GitTreeInflater();
 		File treeObject = new File("src/test/resources/sample-tree-object", "534682ea8852c433a71aa0f5db2dcf5ea6280b52");
 		String tree = inflater.inflate(treeObject);
 		assertTrue(tree.contains("100644 blob 2beae51a0e14b3167fd7e81119972caef95779f4	 .gitignore"));
 		assertTrue(tree.contains("040000 tree 1ad54ebb3da514b6faef8d49ed9dda62509ceed9	site"));
 	}
 	
 }
