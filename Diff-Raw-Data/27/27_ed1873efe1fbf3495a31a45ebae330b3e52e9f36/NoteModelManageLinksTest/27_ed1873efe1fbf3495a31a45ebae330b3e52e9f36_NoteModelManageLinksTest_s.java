 package net.todd.biblestudy.rcp.models;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.util.List;
 
 import net.todd.biblestudy.db.Note;
 import net.todd.biblestudy.db.NoteStyle;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class NoteModelManageLinksTest
 {
 	private static final String ORIGINAL_NOTE_CONTENT = "blah blah blah";
 	private NoteModel noteModel;
 
 	@Before
 	public void setup()
 	{
 		noteModel = new NoteModelHarness()
 		{
 			@Override
 			protected Note getSampleNote()
 			{
 				Note note = new Note();
 				note.setName("Test");
 				note.setNoteId(new Integer(1));
 				note.setText(ORIGINAL_NOTE_CONTENT);
 				return note;
 			}
 		};
 		
 		noteModel.populateNoteInfo(null);
 	}
 	
 	@Test
 	public void testAddLinkToNote()
 	{
 		List<NoteStyle> noteStyles = noteModel.getNoteStylesForRange(0, 100);
 		assertEquals(0, noteStyles.size());
 		
 		noteModel.addLink("blah", 5, 9);
 		
 		// before link
 		noteStyles = noteModel.getNoteStylesForRange(0, 4);
 		assertEquals(0, noteStyles.size());
 		
 		// after link
 		noteStyles = noteModel.getNoteStylesForRange(10, 100);
 		assertEquals(0, noteStyles.size());
 		
 		// exactly on link
 		noteStyles = noteModel.getNoteStylesForRange(5, 9);
 		assertEquals(1, noteStyles.size());
 		assertEquals(new Integer(5), noteStyles.get(0).getStart());
 		assertEquals(new Integer(4), noteStyles.get(0).getLength());
 		assertTrue(noteStyles.get(0).isUnderlined());
 		
 		// starting before link but ending in middle of link
 		noteStyles = noteModel.getNoteStylesForRange(6, 8);
 		assertEquals(1, noteStyles.size());
 		assertEquals(new Integer(6), noteStyles.get(0).getStart());
 		assertEquals(new Integer(2), noteStyles.get(0).getLength());
 		assertTrue(noteStyles.get(0).isUnderlined());
 		
 		// inside link in both start and end
 		noteStyles = noteModel.getNoteStylesForRange(3, 7);
 		assertEquals(1, noteStyles.size());
 		assertEquals(new Integer(5), noteStyles.get(0).getStart());
 		assertEquals(new Integer(2), noteStyles.get(0).getLength());
 		assertTrue(noteStyles.get(0).isUnderlined());
 		
 		// starting in middle of linke and ending after link
 		noteStyles = noteModel.getNoteStylesForRange(7, 11);
 		assertEquals(1, noteStyles.size());
 		assertEquals(new Integer(7), noteStyles.get(0).getStart());
 		assertEquals(new Integer(2), noteStyles.get(0).getLength());
 		assertTrue(noteStyles.get(0).isUnderlined());
 	}
 	
 	@Test
 	public void testLinksStaySameWhenUpdateContentWithSameContent() throws Exception
 	{
 		noteModel.addLink("blah", 5, 9);
 		
 		List<NoteStyle> noteStyles = noteModel.getNoteStylesForRange(0, 100);
 		assertEquals(1, noteStyles.size());
 		
 		noteModel.updateContent(ORIGINAL_NOTE_CONTENT);
 		
 		noteStyles = noteModel.getNoteStylesForRange(0, 4);
 		assertEquals(0, noteStyles.size());
 		
 		noteStyles = noteModel.getNoteStylesForRange(10, 100);
 		assertEquals(0, noteStyles.size());
 		
 		noteStyles = noteModel.getNoteStylesForRange(5, 9);
 		assertEquals(1, noteStyles.size());
 		assertEquals(new Integer(5), noteStyles.get(0).getStart());
 		assertEquals(new Integer(4), noteStyles.get(0).getLength());
 		assertTrue(noteStyles.get(0).isUnderlined());
 		
 		noteStyles = noteModel.getNoteStylesForRange(6, 8);
 		assertEquals(1, noteStyles.size());
 		assertEquals(new Integer(6), noteStyles.get(0).getStart());
 		assertEquals(new Integer(2), noteStyles.get(0).getLength());
 		assertTrue(noteStyles.get(0).isUnderlined());
 		
 		noteStyles = noteModel.getNoteStylesForRange(3, 7);
 		assertEquals(1, noteStyles.size());
 		assertEquals(new Integer(5), noteStyles.get(0).getStart());
 		assertEquals(new Integer(2), noteStyles.get(0).getLength());
 		assertTrue(noteStyles.get(0).isUnderlined());
 		
 		noteStyles = noteModel.getNoteStylesForRange(7, 11);
 		assertEquals(1, noteStyles.size());
 		assertEquals(new Integer(7), noteStyles.get(0).getStart());
 		assertEquals(new Integer(2), noteStyles.get(0).getLength());
 		assertTrue(noteStyles.get(0).isUnderlined());
 	}
 	
 	@Test
 	public void testLinksAreRemovedWhenUpdateContentWithCompletelyDifferentContent() throws Exception
 	{
 		List<NoteStyle> noteStyles = noteModel.getNoteStylesForRange(0, 100);
 		assertEquals(0, noteStyles.size());
 		
 		noteModel.addLink("blah", 5, 9);
 		
 		noteModel.updateContent("woot woot woot");
 		
 		noteStyles = noteModel.getNoteStylesForRange(0, 4);
 		assertEquals(0, noteStyles.size());
 		
 		noteStyles = noteModel.getNoteStylesForRange(10, 100);
 		assertEquals(0, noteStyles.size());
 		
 		noteStyles = noteModel.getNoteStylesForRange(5, 9);
 		assertEquals(0, noteStyles.size());
 		
 		noteStyles = noteModel.getNoteStylesForRange(6, 8);
 		assertEquals(0, noteStyles.size());
 		
 		noteStyles = noteModel.getNoteStylesForRange(3, 7);
 		assertEquals(0, noteStyles.size());
 		
 		noteStyles = noteModel.getNoteStylesForRange(7, 11);
 		assertEquals(0, noteStyles.size());
 	}
 	
 	@Test
 	public void testLinksShiftRightWhenContentAddedBeforeContent() throws Exception
 	{
 		List<NoteStyle> noteStyles = noteModel.getNoteStylesForRange(0, 100);
 		assertEquals(0, noteStyles.size());
 		
 		noteModel.addLink("blah", 5, 9);
 		
 		noteModel.updateContent("woot " + ORIGINAL_NOTE_CONTENT);
 		
 		// new link should be at 10, 14
 		
 		// before link
 		noteStyles = noteModel.getNoteStylesForRange(0, 9);
 		assertEquals(0, noteStyles.size());
 
 		// after link
 		noteStyles = noteModel.getNoteStylesForRange(15, 100);
 		assertEquals(0, noteStyles.size());
 		
 		// exactly on link
 		noteStyles = noteModel.getNoteStylesForRange(10, 14);
 		assertEquals(1, noteStyles.size());
 		assertEquals(new Integer(10), noteStyles.get(0).getStart());
 		assertEquals(new Integer(4), noteStyles.get(0).getLength());
 		assertTrue(noteStyles.get(0).isUnderlined());
 		
 		// inside link in both start and end
 		noteStyles = noteModel.getNoteStylesForRange(11, 13);
 		assertEquals(1, noteStyles.size());
 		assertEquals(new Integer(11), noteStyles.get(0).getStart());
 		assertEquals(new Integer(2), noteStyles.get(0).getLength());
 		assertTrue(noteStyles.get(0).isUnderlined());
 		
 		// starting before link but ending in middle of link
 		noteStyles = noteModel.getNoteStylesForRange(8, 12);
 		assertEquals(1, noteStyles.size());
 		assertEquals(new Integer(10), noteStyles.get(0).getStart());
 		assertEquals(new Integer(2), noteStyles.get(0).getLength());
 		assertTrue(noteStyles.get(0).isUnderlined());
 		
 		// starting in middle of link and ending after link
 		noteStyles = noteModel.getNoteStylesForRange(12, 16);
 		assertEquals(1, noteStyles.size());
 		assertEquals(new Integer(12), noteStyles.get(0).getStart());
 		assertEquals(new Integer(2), noteStyles.get(0).getLength());
 		assertTrue(noteStyles.get(0).isUnderlined());
 	}
 	
 	@Test
 	public void testAddOneSpaceBeforeLinkAndLinkShouldMoveOneSpace() throws Exception
 	{
 		noteModel.updateContent("blah");
 		
 		List<NoteStyle> noteStyles = noteModel.getNoteStylesForRange(0, 100);
 		assertEquals(0, noteStyles.size());
 		
 		noteModel.addLink("blah", 0, 4);
 		
 		noteStyles = noteModel.getNoteStylesForRange(0, 100);
 		assertEquals(1, noteStyles.size());
 		
 		NoteStyle noteStyle = noteStyles.get(0);
 		assertEquals(0, noteStyle.getStart().intValue());
 		assertEquals(4, noteStyle.getLength().intValue());
 		
 		noteModel.updateContent(" blah");
 		
 		noteStyles = noteModel.getNoteStylesForRange(0, 100);
 		assertEquals(1, noteStyles.size());
 		
 		noteStyle = noteStyles.get(0);
 		assertEquals(1, noteStyle.getStart().intValue());
 		assertEquals(4, noteStyle.getLength().intValue());
 	}
 }
