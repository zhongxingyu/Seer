 package com.acmetelecom.test;
 
 import static org.junit.Assert.*;
 
 import java.io.ByteArrayOutputStream;
 import java.io.PrintStream;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import presentation.HtmlPrinter;
 
 public class HtmlPrinterTest {
 
 	HtmlPrinter printer;
 	ByteArrayOutputStream baos;
 
 	@Before
 	public void setUp() throws Exception {
 		baos = new ByteArrayOutputStream();
 		PrintStream ps = new PrintStream(baos);
		printer = (HtmlPrinter) HtmlPrinter.getInstance(ps);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testPrinterIsTypeHTMLPrinter() {
 		assertEquals(printer.getClass(), HtmlPrinter.class);
 	}
 
 	@Test
 	public void testH2() {
 
 		String text = "Peter";
 
 		String res = printer.h2(text);
 		String expected = "<h2>" + text + "</h2>";
 		assertEquals(expected, res);
 	}
 
 	@Test
 	public void testTr() {
 
 		String text = "Peter";
 
 		String res = printer.tr(text);
 		String expected = "<tr>" + text + "</tr>";
 		assertEquals(expected, res);
 	}
 
 	@Test
 	public void testTH() {
 
 		String text = "Peter";
 		String res = printer.th(text);
 		String expected = "<th width=\"160\">" + text + "</th>";
 		assertEquals(expected, res);
 	}
 
 	@Test
 	public void testTd() {
 
 		String text = "Peter";
 		String res = printer.td(text);
 		String expected = "<td>" + text + "</td>";
 		assertEquals(expected, res);
 	}
 
 	@Test
 	public void testBeginHTML() {
 
 		printer.beginHtml();
 		String res = baos.toString();
 		String expected = "<html><head></head><body><h1>Acme Telecom</h1>";
 		assertEquals(expected, res.replace("\n", ""));
 	}
 
 	@Test
 	public void testEndHTML() {
 
 		printer.endHtml();
 		String res = baos.toString();
 		String expected = "</body></html>";
 		assertEquals(expected, res.replace("\n", ""));
 	}
 
 
 }
