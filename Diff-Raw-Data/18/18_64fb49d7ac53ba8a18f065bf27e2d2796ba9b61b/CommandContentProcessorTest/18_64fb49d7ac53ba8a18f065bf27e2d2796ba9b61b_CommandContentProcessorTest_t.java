 package fulbot.model.mail.incoming;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class CommandContentProcessorTest {
 
 	private CommandContentProcessor processor;
 
 	@Before
 	public void setUp() throws Exception {
 		Collection<String> addCommands = Arrays.asList("add", "going", "count me in");
 		Collection<String> removeCommands = Arrays.asList("remove", "not going", "count me out");
 		processor = new CommandContentProcessor(addCommands, removeCommands);
 	}
 
 	@Test
 	public void testShouldAddSenderWhenAddCommandIsMatched() {
 		String sender = "sender";
 		List<String> attendance = new ArrayList<>();
 		
 		processor.process("add", sender, attendance);
 		
 		assertTrue(attendance.contains(sender));
 	}
 
 	@Test
 	public void testShouldRemoveSenderWhenRemoveCommandIsMatched() {
 		String sender = "sender";
 		List<String> attendance = new ArrayList<>(Arrays.asList(sender));
 		
 		processor.process("remove", sender, attendance);
 		
 		assertFalse(attendance.contains(sender));
 	}
 
 	@Test
 	public void testShouldDoNothingWhenNoCommandIsMatched() {
 		String sender = "sender";
 		List<String> attendance = new ArrayList<>();
 		
 		processor.process("non-matching-command", sender, attendance);
 		
 		assertTrue(attendance.isEmpty());
 	}
 
 	@Test
 	public void testShouldNotAddDuplicateWhenAddCommandIsMatchedAndSenderIsAlreadyInAttendance() {
 		String sender = "sender";
 		List<String> attendance = new ArrayList<>(Arrays.asList(sender));
 		
 		processor.process("add", sender, attendance);
 		
 		assertTrue(attendance.contains(sender));
 		assertEquals(1, attendance.size());
 	}
 
 	@Test
 	public void testShouldDoNothingWhenRemoveCommandIsMatchedAndSenderNotInAttendance() {
 		String sender = "sender";
 		String existingAttendee = "existingAttendee";
 		List<String> attendance = new ArrayList<>(Arrays.asList(existingAttendee));
 		
 		processor.process("remove", sender, attendance);
 		
 		assertTrue(attendance.contains(existingAttendee));
 		assertEquals(1, attendance.size());
 	}
 
 	@Test
 	public void testShouldMatchCommandsIgnoringCaseAndTrailingSpaces() {
 		String sender = "sender";
 		List<String> attendance = new ArrayList<>();
 		
 		processor.process(" AdD  \t ", sender, attendance);
 		
 		assertTrue(attendance.contains(sender));
 	}
 
 	@Test
 	public void testShouldMatchCommandsIgnoringPunctiation() {
 		String sender = "sender";
 		List<String> attendance = new ArrayList<>();
 		
 		processor.process("count me in. !!", sender, attendance);
 		
 		assertTrue(attendance.contains(sender));
 	}
 
 	@Test
 	public void testShouldOnlyUseFirstLineToMatchACommand() {
 		String sender = "sender";
 		List<String> attendance = new ArrayList<>();
 		
 		processor.process("add\nsome other text", sender, attendance);
 		
 		assertTrue(attendance.contains(sender));
 	}
 
 	@Test
 	public void testShouldMatchAnyAddCommand() {
 		String sender = "sender";
 		List<String> attendance = new ArrayList<>();
 		
 		processor.process("count me in", sender, attendance);
 		
 		assertTrue(attendance.contains(sender));
 	}
 
 	@Test
 	public void testShouldMatchAnyRemoveCommand() {
 		String sender = "sender";
 		List<String> attendance = new ArrayList<>(Arrays.asList(sender));
 		
 		processor.process("count me out", sender, attendance);
 		
 		assertFalse(attendance.contains(sender));
 	}
 	
 	@Test
	public void testShouldDoNothingWhenConentIsJustANewLine() {
 		String sender = "sender";
 		List<String> attendance = new ArrayList<>();
 		
		processor.process("\n", sender, attendance);
 		
 		assertTrue(attendance.isEmpty());
 	}
 }
