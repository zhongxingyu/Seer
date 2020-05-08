 package se.jayway.maven.lab;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.StringWriter;
 
 import org.junit.Test;
 
 public class LabCreatorTest {
 
 	@Test
 	public void correctContentsOfVersion() throws IOException {
 		String name = "VersionTest.txt";
 		VersionedContents versionedContents = new LabCreator().labify(new InputStreamReader(LabCreatorTest.class.getClassLoader().getResourceAsStream(name)), name);
		String contents1 = getContents(versionedContents, 1);
		assertEquals("VERSION1\n", contents1);
		String contents2 = getContents(versionedContents, 2);
		assertEquals("VERSION1\nVERSION2\n", contents2);
		String contents3 = getContents(versionedContents, 3);
		assertEquals("---\nVERSION1\nVERSION2\n---\n", contents3);
 	}
 
 	@Test
 	public void correctContentsOfVersionWithConstans() throws IOException {
 		String name = "VersionTestWithConstants.txt";
 		VersionedContents versionedContents = new LabCreator("noll", "ETT", "TV", "tre").labify(new InputStreamReader(LabCreatorTest.class.getClassLoader().getResourceAsStream(name)), name);
 		String contents1 = getContents(versionedContents, 1);
		assertEquals("VERSION1\n", contents1);
 		String contents2 = getContents(versionedContents, 2);
		assertEquals("VERSION1\nVERSION2\n", contents2);
 		String contents3 = getContents(versionedContents, 3);
		assertEquals("---\nVERSION1\nVERSION2\n---\n", contents3);
 	}
 
 	private String getContents(VersionedContents versionedContents, int step) {
 		StringWriter writer = new StringWriter();
 		versionedContents.writeVersion(writer, step);
 		return writer.getBuffer().toString();
 	}
 
 	@Test
 	public void nonJavaFilesShouldWork() throws IOException {
 		String name = "NonJavaFile.properties";
 		VersionedContents versionedContents = new LabCreator().labify(new InputStreamReader(LabCreatorTest.class.getClassLoader().getResourceAsStream(name)), name);
 		assertEquals(2, versionedContents.getMaxVersion());
 		assertEquals(0, versionedContents.getLeastVersion());
 	}
 	
 	@Test
 	public void normalLab() throws IOException {
 		String name = "Dummy.java.txt";
 		VersionedContents versionedContents = new LabCreator().labify(new InputStreamReader(LabCreatorTest.class.getClassLoader().getResourceAsStream(name)), name);
 		assertEquals(3, versionedContents.getMaxVersion());
 		assertEquals(1, versionedContents.getLeastVersion());
 	}
 	@Test
 	public void incorrectlyNestedTagsShouldGenerateException() throws IOException {
 		String name = "Evil.java.txt";
 		try {
 			new LabCreator().labify(new InputStreamReader(LabCreatorTest.class.getClassLoader().getResourceAsStream(name)), name);
 			fail("Expected exception!");
 		} catch (IllegalArgumentException e) {
 			// ok!
 		}
 	}
 	@Test
 	public void firstStepsMightBeEmpty() throws IOException {
 		String name = "StartingEmpty.java.txt";
 		VersionedContents versionedContents = new LabCreator().labify(new InputStreamReader(LabCreatorTest.class.getClassLoader().getResourceAsStream(name)), name);
 		assertEquals(3, versionedContents.getLeastVersion());
 		assertFalse(versionedContents.hasContents(2));
 		assertTrue(versionedContents.hasContents(3));
 		assertEquals(7, versionedContents.getMaxVersion());
 	}
 }
