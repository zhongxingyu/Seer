 package net.vidageek.invariant;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.junit.runner.RunWith;
 
 @RunWith(InvariantRunner.class)
 final public class InvariantUseDefinition {
 
 	private static final Set<String> files = new HashSet<String>();
 
 	@Invariant
 	public void invariantDefinitionShouldWork(final FileData data) {
 	}
 
 	@Invariant(affects = ".*\\.cfx")
 	public void invariantFindsAllMatchingFiles(final FileData data) {
 		assertTrue("", data.getName().endsWith(".cfx"));
 		files.add(data.getName());
 	}
 
 	@Invariant(affects = ".*a\\.cfx")
 	public void checkPreviousInvariant(final FileData data) {
 		assertEquals(3, files.size());
 		assertTrue(files.contains("a.cfx"));
 		assertTrue(files.contains("b.cfx"));
 		assertTrue(files.contains("c.cfx"));
 	}
 
 	@Invariant
 	public void doesNotVisitHiddenFiles(final FileData data) {
 		assertFalse(data.getName().contains(".gitignore"));
 	}
 
 	@Invariant(folder = "src/main/java")
 	public void startsAtFolder(final FileData data) {
 		assertFalse(data.getName().endsWith("cfx"));
 		assertTrue(data.getName().endsWith("java"));
 	}
 
 	@Invariant
 	public void canAccessFile(final FileData data) {
 		assertNotNull(data.getFile());
 		assertTrue(data.getFile().getAbsolutePath().endsWith(data.getName()));
 	}
 
 	@Invariant(affects = ".*\\.cfx")
 	public void getsContentFromFiles(final FileData data) {
 		if (data.getName().equals("c.cfx")) {
 			assertEquals("", data.getContent());
 		} else {
 			assertEquals("content", data.getContent());
 		}
 	}
 }
