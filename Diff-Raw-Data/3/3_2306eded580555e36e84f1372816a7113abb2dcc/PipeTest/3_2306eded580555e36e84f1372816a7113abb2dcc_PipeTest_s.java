 package pleocmd.pipe;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 
 import org.junit.Test;
 
 import pleocmd.Log;
 import pleocmd.Testcases;
 import pleocmd.exc.PipeException;
 import pleocmd.pipe.in.Input;
 import pleocmd.pipe.in.StaticInput;
 import pleocmd.pipe.out.InternalCommandOutput;
 
 public class PipeTest extends Testcases {
 
 	@Test
 	public final void testPipeAllData() throws PipeException,
 			InterruptedException, IOException {
 		PipeFeedback fb;
 
 		Log.consoleOut("Test empty pipe");
 		fb = testSimplePipe("", -1, -1, 0, 0, 0, 0, 0, 0, 0, 0);
 
 		Log.consoleOut("Test simple pipe");
 		fb = testSimplePipe("SC|SLEEP|100\nSC|ECHO|Echo working\n", 100, -1, 2,
 				0, 2, 0, 0, 0, 0, 0);
 
 		Log.consoleOut("Test bad input");
 		fb = testSimplePipe("SC|HELP", -1, -1, 0, 0, 0, 1, 0, 0, 0, 0);
 
 		Log.consoleOut("Test unknown command");
 		fb = testSimplePipe("UNKNOWN|0|6.5|Hello\n", -1, -1, 1, 0, 0, 1, 0, 0,
 				0, 0);
 
 		Log.consoleOut("Test executing rest of queue after close");
 		fb = testSimplePipe("SC|SLEEP|500\nSC|SLEEP|1\nSC|SLEEP|1\n"
 				+ "SC|SLEEP|1\nSC|SLEEP|1\nSC|SLEEP|1\nSC|ECHO|End\n", 500, -1,
 				7, 0, 7, 0, 0, 0, 0, 0);
 
 		Log.consoleOut("Test interrupt");
 		fb = testSimplePipe("[P-10]SC|SLEEP|10000\nSC|ECHO|HighPrio\n"
 				+ "SC|SLEEP|1\nSC|SLEEP|1\nSC|SLEEP|1\nSC|SLEEP|1\n"
 				+ "SC|SLEEP|1\nSC|SLEEP|1\nSC|SLEEP|1\nSC|SLEEP|1\n", -1, 9000,
 				10, 0, -1, 0, 0, 1, -1, 0);
 		assertTrue(fb.getDataOutputCount() == 9
 				|| fb.getDataOutputCount() == 10);
 		assertTrue(fb.getDropCount() <= 1);
 
 		Log.consoleOut("Test low priority drop");
 		fb = testSimplePipe("SC|SLEEP|400\n[P-10]SC|SLEEP|30000\n", 400, 25000,
 				2, 0, 1, 0, 0, 0, 1, 0);
 
 		Log.consoleOut("Test high priority queue clearing");
 		fb = testSimplePipe("SC|SLEEP|10000\nSC|FAIL\nSC|FAIL\nSC|FAIL\n"
 				+ "SC|FAIL\nSC|FAIL\n[P05]SC|ECHO|HighPrio\n", -1, -1, 7, 0, 2,
 				0, 0, 1, 5, 0);
 
 		Log.consoleOut("Test timed execution (need to wait)");
 		fb = testSimplePipe(
 				"SC|SLEEP|400\n[T600msP10]SC|ECHO|Timed HighPrio\n", 600, 950,
 				2, 0, 2, 0, 0, 0, 0, 0);
 
 		Log.consoleOut("Test timed execution (short delay)");
 		fb = testSimplePipe("SC|SLEEP|100\n[T50ms]SC|ECHO|Short Delay\n", 100,
 				-1, 2, 0, 2, 0, 0, 0, 0, 0);
 
 		Log.consoleOut("Test timed execution (long delay)");
 		fb = testSimplePipe("SC|SLEEP|500\n[T0ms]SC|ECHO|Long Delay\n", 500,
 				-1, 2, 0, 2, 0, 0, 0, 0, 1);
 
 		Log.consoleOut("Test timed execution combined "
 				+ "with low priority (executed)");
 		fb = testSimplePipe("[T500ms]SC|ECHO|Printed\n"
 				+ "[T900msP-99]SC|ECHO|PrintedToo\n", 900, -1, 2, 0, 2, 0, 0,
 				0, 0, 0);
 
 		Log.consoleOut("Test timed execution combined "
 				+ "with low priority (dropped)");
 		fb = testSimplePipe("[T500ms]SC|SLEEP|500\n"
 				+ "[T900msP-99]SC|FAIL|Dropped\n", 1000, -1, 2, 0, 1, 0, 0, 0,
 				1, 0);
 
 		Log.consoleOut("Test timed execution combined "
 				+ "with high priority (executed)");
 		fb = testSimplePipe("[T500ms]SC|ECHO|Printed\n"
 				+ "[T900msP33]SC|ECHO|HighPrio\n", 900, -1, 2, 0, 2, 0, 0, 0,
 				0, 0);
 
 		Log.consoleOut("Test timed execution combined "
 				+ "with high priority (interrupted)");
 		fb = testSimplePipe("[T500ms]SC|SLEEP|500\n"
 				+ "[T900msP33]SC|ECHO|HighPrio\n", 900, -1, 2, 0, 2, 0, 0, 1,
 				0, 0);
 
 		Log.consoleOut("Test continuing after temporary error");
 		fb = testSimplePipe("SC|FAIL\nSC|SLEEP|500\n", 500, -1, 2, 0, 2, 1, 0,
 				0, 0, 0);
 
 		Log.consoleOut("Test complex situation");
 		fb = testSimplePipe("[T100ms]SC|ECHO|1\n" + "SC|FAIL|UnknownCommand\n"
 				+ "[T300msP10]SC|ECHO|2\n" + "[P10T300ms]SC|ECHO|3\n"
 				+ "[P05T400ms]SC|ECHO|4\n" + "[P05T400ms]SC|SLEEP|600\n"
 				+ "[T400ms]SC|FAIL|Drop\n" + "SC|FAIL|Drop\n"
 				+ "InvalidInputßßß\n" + "SC|FAIL|Drop\n"
 				+ "[T550ms]SC|FAIL|Drop\n" + "[T600msP05]SC|ECHO|I'm late\n"
 				+ "[P05]SC|ECHO|5\n" + "[T1100msP05]SC|ECHO|6\n"
 				+ "[T1200ms]SC|SLEEP|300\n" + "[P99T1350ms]SC|ECHO|7\n", 1350,
 				-1, 15, 0, 11, 2, 0, 1, 4, 1);
 
 		Log.consoleOut("Test continuing after permanent error");
 		// TODO two input, the first fails => the second must work
 		// TODO converter fails => output unconverted
 		// TODO the single output fails => pipe stopped
 		// TODO two output, the first fails => the second must work
 	}
 
 	// CS_IGNORE_NEXT this many parameters are ok here - only a test case
 	private PipeFeedback testSimplePipe(final String staticData,
 			final long minTime, final long maxTime, final int dataIn,
 			final int dataCvt, final int dataOut, final int tempErr,
 			final int permErr, final int intrCnt, final int dropCnt,
 			final int behindCnt) throws PipeException, InterruptedException,
 			IOException {
 		// create pipe
 		final Pipe pipe = new Pipe();
 		if (!staticData.isEmpty()) {
 			final Input in = new StaticInput();
 			in.getConfig().get(0).setFromString(staticData);
 			pipe.addInput(in);
 			pipe.addOutput(new InternalCommandOutput());
 		}
 
 		// execute pipe
 		pipe.configure();
 		pipe.pipeAllData();
 
 		// print log
 		Log.consoleOut(pipe.getFeedback().toString());
 		Log.consoleOut("Finished Pipe '%s' containing '%s'", pipe, staticData
 				.replaceAll("\n(.)", "; $1").replace("\n", ""));
 		Log.consoleOut("");
 
 		// check result
 		final PipeFeedback fb = pipe.getFeedback();
 		if (minTime != -1)
 			assertTrue("Took not long enough", fb.getElapsed() >= minTime);
 		if (maxTime != -1)
 			assertTrue("Took too long", fb.getElapsed() <= maxTime);
 		if (dataIn != -1) assertEquals(dataIn, fb.getDataInputCount());
 		if (dataCvt != -1) assertEquals(dataCvt, fb.getDataConvertedCount());
 		if (dataOut != -1) assertEquals(dataOut, fb.getDataOutputCount());
 		if (tempErr != -1)
 			assertEquals(tempErr, fb.getTemporaryErrors().size());
 		if (permErr != -1)
 			assertEquals(permErr, fb.getPermanentErrors().size());
 		if (intrCnt != -1) assertEquals(intrCnt, fb.getInterruptionCount());
 		if (dropCnt != -1) assertEquals(dropCnt, fb.getDropCount());
		if (behindCnt != -1) assertEquals(behindCnt, fb.getBehindCount());
 		return fb;
 	}
 
 	@Test
 	public final void testReadWriteFiles() {
 		// TODO fail("Not yet implemented");
 	}
 
 }
