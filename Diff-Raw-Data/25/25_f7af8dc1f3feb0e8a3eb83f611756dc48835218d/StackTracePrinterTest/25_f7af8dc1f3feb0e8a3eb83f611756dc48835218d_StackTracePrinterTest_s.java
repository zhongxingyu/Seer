 package org.kalibro.core.util;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 
 import org.apache.commons.io.IOUtils;
 import org.junit.Test;
 import org.kalibro.KalibroTestCase;
 
 public class StackTracePrinterTest extends KalibroTestCase {
 
 	private static final Exception EXCEPTION = new Exception("StackTracePrinterTest message");
 
 	private StackTracePrinter printer;
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldPrintStackTraceWithoutMaximum() throws IOException {
 		printer = new StackTracePrinter();
		assertEquals(expected(), printer.printStackTrace(EXCEPTION));
		assertEquals(expected(), printer.printStackTrace(EXCEPTION));
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
	public void shouldRestrictBytesOfStackTraceWithMaximum() throws IOException {
 		printer = new StackTracePrinter(42);
		assertEquals(expected().substring(0, 42), printer.printStackTrace(EXCEPTION));
 	}
 
	private String expected() throws IOException {
		return IOUtils.toString(StackTracePrinterTest.class.getResourceAsStream("stackTrace.txt"));
 	}
 }
