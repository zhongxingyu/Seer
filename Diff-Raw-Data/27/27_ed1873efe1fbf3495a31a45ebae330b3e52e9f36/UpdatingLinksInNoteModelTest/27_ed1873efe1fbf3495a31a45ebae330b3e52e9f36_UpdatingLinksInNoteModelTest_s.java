 package net.todd.biblestudy.rcp.models;
 
 import static org.junit.Assert.assertEquals;
 import net.todd.biblestudy.db.Note;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class UpdatingLinksInNoteModelTest
 {
 	private NoteModel noteModel;
 	
 	@Before
 	public void setup()
 	{
 		noteModel = new NoteModelHarness()
 		{
 			@Override
 			protected Note getSampleNote()
 			{
				return new Note();
 			}
 		};
 		
 		noteModel.populateNoteInfo(null);
 	}
 	
 	@Test
 	public void testLocationAndLengthOfNewText() throws Exception
 	{
 		noteModel.getNote().setText("abcdefg");
 		
 		String newContent = "abczdefg";
 		
 		int location = noteModel.findLocationOfNewText(newContent);
 		int differenceLength = noteModel.findLengthOfDifferingText(newContent);
 		
 		assertEquals(newContent.indexOf("z"), location);
 		assertEquals("z".length(), differenceLength);
 		
 		
 		noteModel.getNote().setText("abcdefg");
 		
 		newContent = "abc z defg";
 		
 		location = noteModel.findLocationOfNewText(newContent);
 		differenceLength = noteModel.findLengthOfDifferingText(newContent);
 		
 		assertEquals(newContent.indexOf(" z "), location);
 		assertEquals(" z ".length(), differenceLength);
 		
 		
 		noteModel.getNote().setText("a b c d e f g");
 		
 		newContent = "a b c z d e f g";
 		
 		location = noteModel.findLocationOfNewText(newContent);
 		differenceLength = noteModel.findLengthOfDifferingText(newContent);
 		
 		assertEquals(newContent.indexOf("z "), location);
 		assertEquals("z ".length(), differenceLength);
 	}
 	
 	@Test
 	public void testLocationAndLengthOfNewTextWhenJustAddingOneWhiteSpace() throws Exception
 	{
 		noteModel.getNote().setText("test");
 		
 		String newContent = " test";
 		
 		int location = noteModel.findLocationOfNewText(newContent);
 		int differenceLength = noteModel.findLengthOfDifferingText(newContent);
 		
 		assertEquals(0, location);
 		assertEquals(1, differenceLength);
 	}
 	
 	@Test
 	public void testLocationAndLengthOfNewTextWhenAddingTwoWhiteSpaces() throws Exception
 	{
 		noteModel.getNote().setText(" test");
 		
 		String newContent = "  test";
 		
 		int location = noteModel.findLocationOfNewText(newContent);
 		int differenceLength = noteModel.findLengthOfDifferingText(newContent);
 		
 		assertEquals(1, location);
 		assertEquals(1, differenceLength);
 	}
 	
 	@Test
 	public void testLocationAndLengthOfNewTextWhenAddingSameLetterToFront() throws Exception
 	{
 		// TODO: fix this...
 		noteModel.getNote().setText("abcd");
 		
 		String newContent = "aabcd";
 		
 		int location = noteModel.findLengthOfDifferingText(newContent);
 		int differenceLength = noteModel.findLengthOfDifferingText(newContent);
 		
 		assertEquals(1, differenceLength);
 		assertEquals(0, location);
 	}
 }
