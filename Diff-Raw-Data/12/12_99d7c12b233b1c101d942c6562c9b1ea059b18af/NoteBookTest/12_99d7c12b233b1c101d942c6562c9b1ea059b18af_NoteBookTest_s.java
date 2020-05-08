 // Copyright (c) 2011 Martin Ueding <dev@martin-ueding.de>
 
 package tests.jscribble.notebook;
 
 import java.awt.Dimension;
 import java.io.File;
 
 import jscribble.NoteBookProgram;
 import jscribble.notebook.NoteBook;
 import jscribble.notebook.NoteSheet;
 import junit.framework.TestCase;
 
 public class NoteBookTest extends TestCase {
 
 	private NoteBook createTempNoteBook() {
 		return new NoteBook(new Dimension(100, 100), new File(System.getProperty("java.io.tmpdir")), "JUnit-Test");
 	}
 
 	public NoteBookTest() {
 		super();
 	}
 
 	public void testEmptySheetCount() {
 		NoteBook nb = createTempNoteBook();
 		assertEquals(0, nb.getSheetCount());
 	}
 
 	public void testUntouchedFirstPage() {
 		NoteBook nb = createTempNoteBook();
 		assertFalse(nb.getCurrentSheet().touched());
 	}
 
 	public void testName() {
 		NoteBook nb = createTempNoteBook();
 		assertEquals("JUnit-Test", nb.getName());
 	}
 
 	public void testCurrentSheet() {
 		NoteBook nb = createTempNoteBook();
 		assertNotNull(nb.getCurrentSheet());
 	}
 
 	public void testSheetCountAfterFirstLine() {
 		NoteBook nb = createTempNoteBook();
 		nb.drawLine(1, 1, 2, 2);
 		assertEquals(1, nb.getSheetCount());
 	}
 
 	public void testWritingToConfigFile() {
 		NoteBook nb = createTempNoteBook();
 		nb.saveToConfig(new File(System.getProperty("java.io.tmpdir")));
 		File outfile = nb.getConfigFile();
 		assertNotNull(outfile);
 		assertTrue(outfile.exists());
 		nb.deleteSure();
 		assertFalse(outfile.exists());
 	}
 
 	public void testSaving() {
 		NoteBook nb = createTempNoteBook();
 		NoteSheet current = nb.getCurrentSheet();
 		nb.drawLine(0, 0, 0, 0);
 		nb.saveToFiles();
 		assertNotSame(0, current.getFilename().length());
 	}
 
 	public void testPageNumber() {
 		NoteBook nb = createTempNoteBook();
 		assertEquals(1, nb.getCurrentSheet().getPagenumber());
 		nb.drawLine(0, 0, 0, 0);
 		assertEquals(1, nb.getCurrentSheet().getPagenumber());
 		nb.goForward();
 		assertEquals(2, nb.getCurrentSheet().getPagenumber());
 	}
 
 	public void testLoadFromConfig() {
 		NoteBook nb = createTempNoteBook();
 		nb.saveToConfig(new File(System.getProperty("java.io.tmpdir")));
 		nb.drawLine(0, 0, 0, 0);
 		nb.saveToFiles();
 
 		File outfile = nb.getConfigFile();
 
 		NoteBook reloaded = new NoteBook(outfile);
 
 		assertEquals(nb.getName(), reloaded.getName());
 
 		nb.gotoFirst();
 		reloaded.gotoFirst();
 
 		assertEquals(nb.getCurrentSheet().getFilename().getAbsolutePath(), reloaded.getCurrentSheet().getFilename().getAbsolutePath());
 		assertEquals(nb.getSheetCount(), reloaded.getSheetCount());
 	}
 
 	public void testDeletionOfPictureFile() {
 		NoteBook nb = new NoteBook(new Dimension(10, 10), new File(System.getProperty("java.io.tmpdir")), "JUnit-testDeletionOfPictureFile");
 		nb.saveToConfig(new File(System.getProperty("java.io.tmpdir")));
 		File outfile = nb.getConfigFile();
 
 		File[] filenames = new File[20];
 
 		for (int i = 0; i < filenames.length; i++) {
 			nb.drawLine(0, 0, 0, 0);
 			filenames[i] = nb.getCurrentSheet().getFilename();
			nb.goForward();
 		}
 
 		nb.saveToFiles();
 
 		int oldSheetCount = nb.getSheetCount();
 
 		// delete a file from the notebook
 		for (File file : filenames) {
 			assertTrue(String.format("File %s should exist.", file.getAbsolutePath()), file.exists());
 		}
 		filenames[3].delete();
 		assertFalse(filenames[3].exists());
 
 		NoteBook reloaded = new NoteBook(outfile);
 
 		assertEquals(oldSheetCount - 1, reloaded.getSheetCount());
 
 		NoteBookProgram.log(getClass().getName(), "Trying to delete test files.");
		System.out.println("Test");
 		for (File file : filenames) {
 			file.delete();
 		}
 		nb.getConfigFile().delete();
 	}
 }
