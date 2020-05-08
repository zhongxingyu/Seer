 package com.horsefire.syncaws.fingerprint;
 
 import java.io.File;
 
 import junit.framework.TestCase;
 
 import org.junit.Test;
 
 import com.horsefire.syncaws.fingerprint.Fingerprint.Diff;
 import com.horsefire.util.FileDelegate;
 
 public class InkStampTest extends TestCase {
 
 	private static final FileInfo FILE_A = new FileInfo("mysite.html",
			"59C9A5690432939EC043B23E6D06B3FB", 22837);
 	private static final String FILE_BASE = "src/test/resources/sampleFiles";
 
 	private InkStamp m_stamp;
 
 	@Override
 	protected void setUp() throws Exception {
 		m_stamp = new InkStamp(new Md5Calculator(new NumberConverter()),
 				new DirectoryWalker<FileInfo>(), new FileDelegate());
 	}
 
 	@Test
 	public void testCreateNew() throws Exception {
 		Fingerprint fingerprint = m_stamp.createNew(new File(FILE_BASE
 				+ "/typeA"));
 		FileInfo file = fingerprint.getFiles().iterator().next();
 		assertEquals(FILE_A, file);
 	}
 
 	@Test
 	public void testIdenticalDirs() {
 		Fingerprint fingerprintA = m_stamp.createNew(new File(FILE_BASE
 				+ "/typeA"));
 		Fingerprint fingerprintA2 = m_stamp.createNew(new File(FILE_BASE
 				+ "/typeA2"));
 		assertTrue(fingerprintA.equals(fingerprintA2));
 		assertTrue(fingerprintA2.equals(fingerprintA));
 		Diff diff = fingerprintA.diff(fingerprintA2);
 		assertTrue(diff.addedFiles().isEmpty());
 		assertTrue(diff.removedFiles().isEmpty());
 	}
 
 	@Test
 	public void testDifferentDirs() {
 		Fingerprint fingerprintA = m_stamp.createNew(new File(FILE_BASE
 				+ "/typeA"));
 		Fingerprint fingerprintB = m_stamp.createNew(new File(FILE_BASE
 				+ "/typeB"));
 		assertFalse(fingerprintA.equals(fingerprintB));
 		assertFalse(fingerprintB.equals(fingerprintA));
 		Diff diff = fingerprintA.diff(fingerprintB);
 		assertEquals(1, diff.addedFiles().size());
 		assertEquals("notes.txt", diff.addedFiles().get(0).getPath());
 		assertEquals(1, diff.removedFiles().size());
 		assertEquals("notes.txt", diff.removedFiles().get(0).getPath());
 
 		Fingerprint fingerprintC = m_stamp.createNew(new File(FILE_BASE
 				+ "/typeC"));
 		assertFalse(fingerprintA.equals(fingerprintC));
 		assertFalse(fingerprintC.equals(fingerprintA));
 		diff = fingerprintA.diff(fingerprintC);
 		assertEquals(2, diff.addedFiles().size());
 		assertEquals("notes.txt", diff.addedFiles().get(0).getPath());
 		assertEquals("pics/fry.jpg", diff.addedFiles().get(1).getPath());
 		assertEquals(2, diff.removedFiles().size());
 		assertEquals("notes.txt", diff.removedFiles().get(0).getPath());
 		assertEquals("pics/zoidberg.jpg", diff.removedFiles().get(1).getPath());
 
 		assertFalse(fingerprintB.equals(fingerprintC));
 		assertFalse(fingerprintC.equals(fingerprintB));
 		diff = fingerprintB.diff(fingerprintC);
		assertEquals(2, diff.addedFiles().size());
		assertEquals("notes.txt", diff.addedFiles().get(0).getPath());
		assertEquals("pics/fry.jpg", diff.addedFiles().get(1).getPath());
		assertEquals(2, diff.removedFiles().size());
		assertEquals("notes.txt", diff.removedFiles().get(0).getPath());
		assertEquals("pics/zoidberg.jpg", diff.removedFiles().get(1).getPath());
 	}
 }
