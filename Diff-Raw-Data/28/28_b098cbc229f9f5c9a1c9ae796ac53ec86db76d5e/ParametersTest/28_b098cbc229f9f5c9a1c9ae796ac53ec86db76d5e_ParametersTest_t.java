 package fit;
 
 //Copyright (c) 2008 Cunningham & Cunningham, Inc.
 //Released under the terms of the GNU General Public License version 2 or later.
 //Contributed by James Shore with inspiration from Martin Busik
 
 import junit.framework.TestCase;
 
 public class ParametersTest extends TestCase {
 
 	public void testInputAndOutput() throws Exception {
 		Parameters p = new Parameters(new String[] {"in", "out"});
 		assertEquals("in", p.input());
 		assertEquals("out", p.output());
 	}
 
 	public void testLegacyArguments_NoteThatThisMethodSucksAndWillGoAwayAsSoonAsPossible() throws Exception {
 		Parameters p = new Parameters(new String[] {"--encoding=foo", "in", "out"});
 		String[] args = p.legacyArguments();
 		assertEquals(2, args.length);
 		assertEquals("in", args[0]);
 		assertEquals("out", args[1]);
 	}
 
 	public void testDefaultEncoding() throws Exception {
 		Parameters p = new Parameters(new String[] {"in", "out"});
 		assertEquals(System.getProperty("file.encoding"), p.encoding());
 		assertEquals("encoding unspecified", false, p.encodingSpecified());
 		assertEquals("encoding specified", true, new Parameters(new String[] {"in", "out", "--encoding=foo"}).encodingSpecified());
 	}	
 
 	public void testOptionsWorkInAnyPosition() throws Exception {
 		assertParseWorked("--encoding=foo", "in", "out");
 		assertParseWorked("in", "--encoding=foo", "out");
 		assertParseWorked("in", "out", "--encoding=foo");
 	}
 
 	private void assertParseWorked(String parm1, String parm2, String parm3) throws CommandLineException {
 		Parameters p = new Parameters(new String[] {parm1, parm2, parm3});
 		assertEquals("in", p.input());
 		assertEquals("out", p.output());
 		assertEquals("foo", p.encoding());
 	}
	
	public void testDoubleDash() throws Exception {
		Parameters p = new Parameters(new String[] {"--encoding=foo", "--", "in", "--encoding=foo"});
		assertEquals("in", p.input());
		assertEquals("--encoding=foo", p.output());
		assertEquals("foo", p.encoding());
	}
 
 	public void testOptionParser() throws Exception {
 		assertEquals("foo", newParameters("--encoding=foo").encoding());
 		assertEquals("foo bar", newParameters("--encoding=foo bar").encoding());
 		assertEquals("foo=bar", newParameters("--encoding=foo=bar").encoding());
 		assertEquals("foo-bar", newParameters("--encoding=foo-bar").encoding());
 	}
 
 	public void testArgumentErrorHandling() throws Exception {
 		assertParseException("fit: missing input-file and output-file", new String[] {});
 		assertParseException("fit: missing output-file", new String[] {"in"});
 		assertParseException("fit: too many file parameters", new String[] {"in", "out", "quack"});
 		assertParseException("fit: --encoding: duplicated parameter", new String[] {"in", "out", "--encoding=a", "--encoding=b"});
 		assertParseException("fit: --notparm: unknown option", new String[] {"in", "out", "--encoding=a", "--notparm=b"});
 		assertParseException("fit: --notparm: unknown option", new String[] {"in", "out", "--notparm=a", "--notparm=b"});
 	}
 
 	public void testOptionErrorHandling() throws Exception {
 		assertParseException("fit: too many file parameters", "=foo");
 		assertParseException("fit: -encoding: unknown option", "-encoding=foo");
 		assertParseException("fit: --notparm: unknown option", "--notparm=blah");
 		assertParseException("fit: --notparm: unknown option", "--notparm");
 		assertParseException("fit: -: unknown option", "-");
 		assertParseException("fit: --encoding: missing value", "--encoding");
 		assertParseException("fit: --encoding: missing value", "--encoding=");
 	}
 	
 	public void testVersion() throws Exception {
 		try {
 			newParameters("--version");
 			fail("expected exception");
 		}
 		catch (CommandLineException e) {
 			assertEquals(Parameters.VERSION_TEXT, e.getMessage());
 		}
 	}
 	
 	public void testHelp() throws Exception {
 		try {
 			newParameters("--help");
 			fail("expected exception");
 		}
 		catch (CommandLineException e) {
 			assertEquals(Parameters.HELP_TEXT, e.getMessage());
 		}
 	}
 	
 	private Parameters newParameters(String encoding) throws CommandLineException {
 		return new Parameters(new String[] {"in", "out", encoding});
 	}
 
 	private void assertParseException(String expectedErrorMessage, String encodingParm) throws CommandLineException {
 		assertParseException(expectedErrorMessage, new String[] {"in", "out", encodingParm});
 	}
 
 	private void assertParseException(String expectedErrorMessage, String[] args) throws CommandLineException {
 		try {
 			Parameters p = new Parameters(args);
 			fail("expected exception");
 		}
 		catch (CommandLineParseException e) {
 			assertEquals(expectedErrorMessage, e.getMessage());
 		}
 	}
 }
