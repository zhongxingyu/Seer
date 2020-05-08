 // Copyright (c) 2011 Martin Ueding <dev@martin-ueding.de>
 
 package tests.jscribble.notebook;
 
 import java.awt.Dimension;
 import java.io.File;
 import java.io.IOException;
 
 import jscribble.notebook.NoteSheet;
 import junit.framework.TestCase;
 
 
 public class NoteSheetTest extends TestCase {
 
 	/**
 	 * Generates a temporary NoteSheet for testing.
 	 */
 	private NoteSheet getTempNoteSheet() {
 		return new NoteSheet(new Dimension(1024, 600), 0, null);
 	}
 
 	public NoteSheetTest() {
 		super();
 	}
 
 	/**
 	 * Tests whether drawing a line causes a change in color in the image.
 	 */
 	public void testDrawing() {
 		NoteSheet n = getTempNoteSheet();
 		assertNotNull(n);
 		assertNotNull(n.getImg());
 		int previousColor = n.getImg().getRGB(100, 100);
 		n.drawLine(100, 100, 100, 200);
 		int rgbarray[] = n.getImg().getRGB(100, 100, 1, 1, null, 0, 1);
 		assertTrue(rgbarray.length > 0);
 		assertFalse(rgbarray[0] == previousColor);
 	}
 
 	/**
 	 * Tests whether a new NoteSheet is untouched and does not need any saving.
 	 */
 	public void testTouched() {
 		NoteSheet n = getTempNoteSheet();
 		assertFalse(n.touched());
 		assertFalse(n.unsaved());
 		n.drawLine(0, 0, 0, 0);
 		assertTrue(n.touched());
 		assertTrue(n.unsaved());
 	}
 
 	/**
 	 * Creates a single NoteSheet with an existing temporary file and tests whether it is untouched and does not need any saving.
 	 */
 	public void testTouchedWithEmptyTempfile() {
 		try {
			File tempfile = File.createTempFile("", ".png");
 			tempfile.createNewFile();
 			NoteSheet n = new NoteSheet(new Dimension(100, 100), 0, tempfile);
 
 			assertFalse(n.unsaved());
 			assertFalse(n.touched());
 			n.drawLine(0, 0, 0, 0);
 			assertTrue(n.touched());
 			assertTrue(n.unsaved());
 
 		}
 		catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
