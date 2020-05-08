 package test.pleocmd.pipe;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 
 import org.junit.Test;
 
 import pleocmd.Log;
 import pleocmd.cfg.Configuration;
 import pleocmd.exc.ConfigurationException;
 import pleocmd.exc.PipeException;
 import pleocmd.pipe.Pipe;
 import pleocmd.pipe.PipeFeedback;
 import pleocmd.pipe.in.Input;
 import pleocmd.pipe.in.StaticInput;
 import pleocmd.pipe.out.FileOutput;
 import pleocmd.pipe.out.InternalCommandOutput;
 import pleocmd.pipe.out.Output;
 import pleocmd.pipe.out.PrintType;
 import test.pleocmd.Testcases;
 
 public class PipeTest extends Testcases {
 
 	@Test
 	public final void testPipeAllData() throws Exception {
 		PipeFeedback fb;
 		final Pipe p = new Pipe(new Configuration());
 
 		Log.consoleOut("Test empty pipe");
 		p.reset();
 		fb = testSimplePipe(null, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0);
 
 		Log.consoleOut("Test simple pipe");
 		fb = testSimplePipe("SC|SLEEP|100\nSC|ECHO|Echo working\n", 100, -1, 2,
 				0, 2, 0, 0, 0, 0, 0);
 
 		Log.consoleOut("Test bad input");
 		fb = testSimplePipe("SC|HELP", -1, -1, 0, 0, 0, 0, 1, 0, 0, 0);
 
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
 				+ "SC|FAIL\nSC|FAIL\nSC|FAIL\nSC|FAIL\nSC|FAIL\nSC|FAIL\n"
 				+ "SC|FAIL\nSC|FAIL\nSC|FAIL\nSC|FAIL\nSC|FAIL\nSC|FAIL\n"
 				+ "SC|FAIL\nSC|FAIL\nSC|FAIL\nSC|FAIL\nSC|FAIL\nSC|FAIL\n"
 				+ "[P05]SC|ECHO|HighPrio\n", -1, -1, 23, 0, -1, 0, 0, 1, -1, 0);
 		assertTrue("Expected 1 output and 22 drops (slow computer) or "
 				+ "2 outputs and 21 drops (fast computer): ", fb
 				.getDataOutputCount() == 2
 				&& fb.getDropCount() == 21
 				|| fb.getDataOutputCount() == 1
 				&& fb.getDropCount() == 22);
 
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
 
 		Log.consoleOut("Test timing");
 		testSimplePipe("SC|ECHO|$ELAPSED=0?\n" + "[T150ms]SC|SLEEP|200\n"
 				+ "SC|ECHO|$ELAPSED=350?\n"
 				+ "[T450msP10]SC|ECHO|$ELAPSED=450?\n"
 				+ "[T550ms]SC|ECHO|$ELAPSED=550?\n"
 				+ "[T650ms]SC|ECHO|$ELAPSED=650?\n"
 				+ "[T700ms]SC|ECHO|$ELAPSED=700?\n"
 				+ "[T750ms]SC|ECHO|$ELAPSED=750?\n"
 				+ "[T775ms]SC|ECHO|$ELAPSED=775?\n"
 				+ "[T800ms]SC|ECHO|$ELAPSED=800?\n"
 				+ "[T825ms]SC|ECHO|$ELAPSED=825?\n"
 				+ "[T850ms]SC|ECHO|$ELAPSED=850?\n"
 				+ "[T860ms]SC|ECHO|$ELAPSED=860?\n"
 				+ "[T870ms]SC|ECHO|$ELAPSED=870?\n"
 				+ "[T880ms]SC|ECHO|$ELAPSED=880?\n"
 				+ "[T1000ms]SC|ECHO|$ELAPSED=1000?\n"
 				+ "SC|ECHO|$ELAPSED=1000+<10?\n"
 				+ "SC|ECHO|$ELAPSED=1000+<20?\n"
 				+ "SC|ECHO|$ELAPSED=1000+<30?\n", 450, -1, 19, 0, 19, 0, 0, 0,
 				0, 0);
 
 		Log.consoleOut("Test error handling (two inputs, first one fails)");
 		Input in1, in2;
 		Output out1, out2;
 		p.reset();
 		p.addInput(in1 = new StaticInput("FAILURE"));
 		p.addInput(in2 = new StaticInput("SC|ECHO|Second is working\n"));
 		p.addOutput(out1 = new InternalCommandOutput());
 		in1.connectToPipePart(out1);
 		in2.connectToPipePart(out1);
 		fb = testSimplePipe(p, -1, -1, 1, 0, 1, 0, 1, 0, 0, 0);
 
 		Log.consoleOut("Test error handling (converter fails)");
 		Log.consoleOut("TODO");
 		// TODO ENH converter fails => output unconverted
 
 		Log.consoleOut("Test error handling (sole output fails)");
 		final File tmpFile = File.createTempFile("PipeTest", null);
 		p.reset();
 		p.addInput(in1 = new StaticInput("[T1s]\n[T8sP10]\n"));
 		p.addOutput(out1 = new FileOutput(tmpFile, PrintType.Ascii));
 		in1.connectToPipePart(out1);
 		tmpFile.delete();
 		tmpFile.mkdir();
		fb = testSimplePipe(p, 1000, 7000, 2, 0, 0, 0, 1, 0, 0, 0);
 		tmpFile.delete();
 
 		Log.consoleOut("Test error handling (two outputs, first one fails)");
 		p.reset();
 		p.addInput(in1 = new StaticInput("SC|ECHO|Second is working\n"));
 		p.addOutput(out1 = new FileOutput(tmpFile, PrintType.Ascii));
 		p.addOutput(out2 = new InternalCommandOutput());
 		in1.connectToPipePart(out1);
 		in1.connectToPipePart(out2);
 		tmpFile.mkdir();
 		fb = testSimplePipe(p, -1, -1, 1, 0, 1, 0, 1, 0, 0, 0);
 		tmpFile.delete();
 	}
 
 	// CS_IGNORE_NEXT this many parameters are ok here - only a test case
 	private PipeFeedback testSimplePipe(final Object input, final long minTime,
 			final long maxTime, final int dataIn, final int dataCvt,
 			final int dataOut, final int tempErr, final int permErr,
 			final int intrCnt, final int dropCnt, final int behindCnt)
 			throws PipeException, InterruptedException, ConfigurationException {
 		// create pipe
 		final Pipe pipe;
 		if (input instanceof Pipe)
 			pipe = (Pipe) input;
 		else
 			pipe = new Pipe(new Configuration());
 		if (input instanceof String) {
 			final StaticInput in;
 			final InternalCommandOutput out;
 			pipe.reset();
 			pipe.addInput(in = new StaticInput((String) input));
 			pipe.addOutput(out = new InternalCommandOutput());
 			in.connectToPipePart(out);
 		}
 
 		// execute pipe
 		pipe.configure();
 		pipe.pipeAllData();
 
 		// print log
 		Log.consoleOut(pipe.getFeedback().toString());
 		if (input instanceof String)
 			Log.consoleOut("Finished Pipe containing '%s'", ((String) input)
 					.replaceAll("\n(.)", "; $1").replace("\n", ""));
 		else
 			Log.consoleOut("Finished Pipe");
 		Log.consoleOut("");
 
 		// check result
 		final PipeFeedback fb = pipe.getFeedback();
 		if (permErr != -1)
 			assertEquals("Permanent Error Count is wrong: ", permErr, fb
 					.getPermanentErrors().size());
 		if (tempErr != -1)
 			assertEquals("Temporary Error Count is wrong: ", tempErr, fb
 					.getTemporaryErrors().size());
 		if (intrCnt != -1)
 			assertEquals("Interrupt Count is wrong: ", intrCnt, fb
 					.getInterruptionCount());
 		if (dropCnt != -1)
 			assertEquals("Drop Count is wrong: ", dropCnt, fb.getDropCount());
 		if (behindCnt != -1)
 			assertEquals("Behind Count is wrong: ", behindCnt, fb
 					.getSignificantBehindCount());
 		if (dataIn != -1)
 			assertEquals("Data Input Count is wrong: ", dataIn, fb
 					.getDataInputCount());
 		if (dataCvt != -1)
 			assertEquals("Data Conversion Count is wrong: ", dataCvt, fb
 					.getDataConvertedCount());
 		if (dataOut != -1)
 			assertEquals("Data Output Count is wrong: ", dataOut, fb
 					.getDataOutputCount());
 		if (minTime != -1)
 			assertTrue("Took not long enough", fb.getElapsed() >= minTime);
 		if (maxTime != -1)
 			assertTrue("Took too long", fb.getElapsed() <= maxTime);
 		return fb;
 	}
 
 }
