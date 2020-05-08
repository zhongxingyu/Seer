 package ghm.follow.test;
 
 import static org.junit.Assert.assertEquals;
 import ghm.follow.io.JTextComponentDestination;
 import ghm.follow.io.OutputDestination;
 
 import java.util.Date;
 import java.util.Random;
 
 import javax.swing.JTextPane;
 import javax.swing.text.Document;
 
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 public class FileFollowerT extends BaseTestCase {
 
 	@Override
 	@Before
 	public void setUp() throws Exception {
 		super.setUp();
 		testination = new Testination();
 		follower.addOutputDestination(testination);
 	}
 
 	@Test
 	public void textShouldMatchInputWhenAddingAfterStart() throws Exception {
 		follower.start();
 		String control = "control";
 		writeToFollowedFileAndWait(control);
 		assertEquals(control, testination.strBuf.toString());
 		String control2 = "control2";
 		writeToFollowedFileAndWait(control2);
 		assertEquals(control + control2, testination.strBuf.toString());
 	}
 
 	@Test
 	public void testShouldMatchInputWhenLatencyIsShort() throws Exception {
 		follower.setLatency(100);
 		follower.start();
 		String control = "control";
 		writeToFollowedFileAndWait(control);
 		assertEquals(control, testination.strBuf.toString());
 	}
 
	@Test
 	public void textShouldMatchInputWhenBufferSizeIsSmall() throws Exception {
 		int bufferSize = 10;
 		follower.setBufferSize(bufferSize);
 		String control = "32098jaspfj234-08uewrfiojsad;lfkjqw4poiru2340ruwefkjasd;lkjq2po43iu123-4r098uasdfl;asdclkjasdfasdf9834roaerf";
 		String subcontrol = control.substring(control.length() - bufferSize);
 		// initial read of the 'file' will only contain as many characters as
 		// the buffer size
 		writeToFollowedFileAndWait(control);
 		follower.start();
 		assertEquals(subcontrol, testination.strBuf.toString());
 		// subsequent reads of the 'file' will contain all previous characters
 		// and any newly added ones
 		writeToFollowedFileAndWait(control);
 		assertEquals(subcontrol + control, testination.strBuf.toString());
 	}
 
 	@Test
 	public void textInMultipleDestinationsShouldMatch() throws Exception {
 		Testination testination2 = new Testination();
 		follower.addOutputDestination(testination2);
 		follower.start();
 		String control = "control";
 		writeToFollowedFileAndWait(control);
 		assertEquals(control, testination.strBuf.toString());
 		assertEquals(control, testination2.strBuf.toString());
 	}
 
 	@Ignore
 	public void testJTextComponentDestinationFor2Minutes() throws Exception {
 		final long TWO_MINUTES = 2 * 60 * 1000;
 
 		JTextPane textPane = new JTextPane();
 		JTextComponentDestination dest = new JTextComponentDestination(
 				textPane, true);
 		Document doc = dest.getJTextComponent().getDocument();
 		follower.addOutputDestination(dest);
 		follower.start();
 		Date start = new Date();
 		Random rand = new Random();
 		StringBuffer buffer = new StringBuffer();
 
 		long end = start.getTime() + TWO_MINUTES;
 		while (end > new Date().getTime()) {
 			StringBuffer sb = new StringBuffer();
 			double length = rand.nextDouble() * 100;
 			while (length > 0) {
 				sb.append((char) (length-- % 26));
 				// if (rand.nextDouble() >= .8) {
 				// sb = new StringBuffer();
 				// dest.clear();
 				// // clearFollowedFile();
 				// break;
 				// }
 			}
 			if (sb.length() > 0 && rand.nextBoolean()) {
 				sb.append("\n");
 			}
 			String text = sb.toString();
 			writeToFollowedFileAndWait(text);
 			buffer.append(text);
 			assertEquals(buffer.length(), doc.getLength());
 			assertEquals(buffer.toString(), doc.getText(0, doc.getLength()));
 		}
 	}
 
 	private Testination testination;
 
 	class Testination implements OutputDestination {
 		public void print(String s) {
 			strBuf.append(s);
 		}
 
 		public void clear() {
 			strBuf.delete(0, strBuf.length());
 		}
 
 		StringBuffer strBuf = new StringBuffer();
 	}
 
 }
