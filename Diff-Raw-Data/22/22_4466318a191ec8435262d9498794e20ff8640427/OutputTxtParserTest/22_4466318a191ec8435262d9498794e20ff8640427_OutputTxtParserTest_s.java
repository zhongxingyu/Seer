 package org.araqne.logdb.query.parser;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 
 import org.araqne.logdb.QueryParseException;
 import org.araqne.logdb.QueryStopReason;
 import org.araqne.logdb.query.command.OutputTxt;
 import org.junit.Test;
 
 /**
  * @since 1.6.7
  * @author darkluster
  * 
  */
 public class OutputTxtParserTest {
 
 	@Test
 	public void testNormalCase() {
 		new File("logexport.txt").delete();
 		OutputTxt txt = null;
 		try {
 			OutputTxtParser p = new OutputTxtParser();
 			txt = (OutputTxt) p.parse(null, "outputtxt logexport.txt sip, dip ");
 
 			File f = txt.getTxtFile();
 			assertEquals("logexport.txt", f.getName());
 			assertEquals("sip", txt.getFields().get(0));
 			assertEquals("dip", txt.getFields().get(1));
 			assertEquals(" ", txt.getDelimiter());
 
			assertEquals("outputtxt logexport.txt sip, dip", txt.toString());
 		} finally {
 			if (txt != null)
 				txt.onClose(QueryStopReason.End);
 			new File("logexport.txt").delete();
 		}
 	}
 
 	@Test
 	public void testDelimiter() {
 		new File("logexport.txt").delete();
 		OutputTxt txt = null;
 		try {
 			OutputTxtParser p = new OutputTxtParser();
 			txt = (OutputTxt) p.parse(null, "outputtxt delimiter=\"|\" logexport.txt sip, dip ");
 
 			assertEquals("|", txt.getDelimiter());
			assertEquals("outputtxt delimiter=| logexport.txt sip, dip", txt.toString());
 		} finally {
 			if (txt != null)
 				txt.onClose(QueryStopReason.End);
 			new File("logexport.txt").delete();
 		}
 	}
 
 	@Test
 	public void testInvalidDelimiterPosition() {
 		new File("logexport.txt").delete();
 		OutputTxt txt = null;
 		try {
 			OutputTxtParser p = new OutputTxtParser();
 			txt = (OutputTxt) p.parse(null, "outputtxt logexport.txt delimiter=\"|\" sip, dip ");
 
 			assertEquals("|", txt.getDelimiter());
 		} catch (QueryParseException e) {
 			assertEquals("invalid-option", e.getType());
 			assertEquals(-1, (int) e.getOffset());
 		} finally {
 			new File("logexport.csv").delete();
 		}
 	}
 
 	@Test
 	public void testMissingField1() {
 		new File("logexport.txt").delete();
 		try {
 			OutputTxtParser p = new OutputTxtParser();
 			p.parse(null, "outputtxt logexport.txt ");
 			fail();
 		} catch (QueryParseException e) {
 			assertEquals("missing-field", e.getType());
 			assertEquals(14, (int) e.getOffset());
 		} finally {
 			new File("logexport.txt").delete();
 		}
 	}
 
 	@Test
 	public void testMissingField2() {
 		new File("logexport.txt").delete();
 		try {
 			OutputTxtParser p = new OutputTxtParser();
 			p.parse(null, "outputtxt logexport.txt sip,");
 			fail();
 		} catch (QueryParseException e) {
 			assertEquals("missing-field", e.getType());
 			assertEquals(28, (int) e.getOffset());
 		} finally {
 			new File("logexport.txt").delete();
 		}
 	}
 
 	@Test
 	public void testMissingField3() {
 		new File("logexport.txt").delete();
 		try {
 			OutputTxtParser p = new OutputTxtParser();
 			p.parse(null, "outputtxt");
 			fail();
 		} catch (QueryParseException e) {
 			assertEquals("missing-field", e.getType());
 			assertEquals(0, (int) e.getOffset());
 		} finally {
 			new File("logexport.txt").delete();
 		}
 	}
 }
