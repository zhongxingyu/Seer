 package org.kalibro.core.util;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
import java.util.regex.Pattern;
 
 import org.apache.commons.io.IOUtils;
 import org.junit.Test;
 import org.kalibro.KalibroTestCase;
 
 public class StackTracePrinterTest extends KalibroTestCase {
 
 	private static final Exception EXCEPTION = new Exception("StackTracePrinterTest message");
 
 	private StackTracePrinter printer;
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldPrintStackTraceWithoutMaximum() throws IOException {
 		printer = new StackTracePrinter();
		assertTrue(expected().matcher(printer.printStackTrace(EXCEPTION)).matches());
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
	public void shouldResetPrintStackTrace() {
		printer = new StackTracePrinter();
		String first = printer.printStackTrace(EXCEPTION);
		String second = printer.printStackTrace(EXCEPTION);
		assertEquals(first, second);
	}

	@Test(timeout = UNIT_TIMEOUT)
	public void shouldRestrictBytesOfStackTraceWithMaximum() {
 		printer = new StackTracePrinter(42);
		assertEquals("java.lang.Exception: StackTracePrinterTest", printer.printStackTrace(EXCEPTION));
 	}
 
	private Pattern expected() throws IOException {
		String regexp = IOUtils.toString(StackTracePrinterTest.class.getResourceAsStream("stackTrace.txt"));
		return Pattern.compile(regexp, Pattern.MULTILINE);
 	}
 }
