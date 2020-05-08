 package edu.illinois.gitsvn.infra;
 
 import java.io.IOException;
 import java.nio.file.FileVisitResult;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.SimpleFileVisitor;
 import java.nio.file.attribute.BasicFileAttributes;
 
 import org.gitective.core.CommitFinder;
import org.gitective.core.filter.commit.AndCommitFilter;
 import org.gitective.core.filter.commit.CommitCountFilter;
 import org.gitective.tests.GitTestCase;
 import org.junit.Test;
 
 import edu.illinois.gitsvn.infra.filters.blacklister.AllDeletionDiffFilter;
 
 public class AllDeletionDiffFilterTest extends GitTestCase {
 
 	@Test
 	public void testIgnoreCommitsThatOnlyDelete() throws Exception {
 		add("file/a.txt", "mumu", "c1");
 		add("file/a1.txt", "mumu", "c2");
 		add("file/a2.txt", "mumu", "c3");
 
 		delete("file");
 
 		AllDeletionDiffFilter filter = new AllDeletionDiffFilter();
 		CommitCountFilter count = new CommitCountFilter();
 
		AndCommitFilter all = new AndCommitFilter();
 		all.add(filter, count);
 
 		CommitFinder finder = new CommitFinder(testRepo);
 
 		finder.setFilter(count);
 		finder.find();
 		assertEquals(4, count.getCount());
 
 		count.reset();
 
		finder.setFilter(all);
 		finder.find();
 		assertEquals(3, count.getCount());
 	}
 
 	private void printRepo() throws IOException {
 		Files.walkFileTree(testRepo.getParentFile().toPath(), new SimpleFileVisitor<Path>() {
 			@Override
 			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
 				System.out.println("\t" + file.getFileName() + ":" + file.getParent().getFileName());
 				return super.visitFile(file, attrs);
 			}
 
 			@Override
 			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
 				System.out.println(dir.getFileName());
 				return super.preVisitDirectory(dir, attrs);
 			}
 		});
 	}
 }
