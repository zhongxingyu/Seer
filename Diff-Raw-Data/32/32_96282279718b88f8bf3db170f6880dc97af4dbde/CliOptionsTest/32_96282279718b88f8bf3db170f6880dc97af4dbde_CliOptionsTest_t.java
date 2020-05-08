 /*
  * LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 42):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a beer in return.
  *
  */
 
 package de.weltraumschaf.jebnf.cli;
 
 import de.weltraumschaf.commons.CapturingOutputStream;
 import java.io.PrintStream;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.ParseException;
 import static org.junit.Assert.*;
 import org.junit.Test;
 
 /**
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class CliOptionsTest {
 
     private CliOptions setUpSut(final String[] args) throws ParseException {
         final CliOptions options = new CliOptions();
         options.parse(args);
         return options;
     }
 
     @Test public void defaults() {
         final CliOptions options = new CliOptions();
         assertFalse(options.isDebug());
         assertFalse(options.isHelp());
         assertFalse(options.isIde());
         assertFalse(options.hasOutputFile());
         assertFalse(options.hasSyntaxFile());
         assertFalse(options.hasOutputFormat());
         assertFalse(options.isShowVersion());
     }
 
     @Test public void parseNoArgOptions() throws ParseException {
         CliOptions options = setUpSut(new String[] {"-d"});
         assertTrue(options.isDebug());
         assertFalse(options.isHelp());
         assertFalse(options.isIde());
         assertFalse(options.isShowVersion());
 
         options = setUpSut(new String[] {"-h"});
         assertTrue(options.isHelp());
         assertFalse(options.isDebug());
         assertFalse(options.isIde());
         assertFalse(options.isShowVersion());
 
         options = setUpSut(new String[] {"-i"});
         assertTrue(options.isIde());
         assertFalse(options.isDebug());
         assertFalse(options.isHelp());
         assertFalse(options.isShowVersion());
 
         options = setUpSut(new String[] {"--ide"});
         assertTrue(options.isIde());
         assertFalse(options.isDebug());
         assertFalse(options.isHelp());
         assertFalse(options.isShowVersion());
 
         options = setUpSut(new String[] {"-v"});
         assertFalse(options.isIde());
         assertFalse(options.isDebug());
         assertFalse(options.isHelp());
         assertTrue(options.isShowVersion());
     }
 
     @Test public void parseArgOptions() throws ParseException {
         CliOptions options = setUpSut(new String[] {"-s", "foo"});
         assertTrue(options.hasSyntaxFile());
         assertEquals("foo", options.getSyntaxFile());
 
         options = setUpSut(new String[] {"-o", "bar"});
         assertTrue(options.hasOutputFile());
         assertEquals("bar", options.getOutputFile());
         assertFalse(options.hasSyntaxFile());
 
         options = setUpSut(new String[] {"-s", "foo.ebnf", "-f", "xml"});
         assertFalse(options.hasOutputFile());
         assertEquals("", options.getOutputFile());
     }
 
     @Test public void parseOutputFormatOptions() throws ParseException {
         CliOptions options = setUpSut(new String[] {"-f", "xml"});
         assertTrue(options.hasOutputFormat());
         assertEquals(OutputFormat.XML, options.getOutputFormat());
 
        options = setUpSut(new String[] {"-f", "tree"});
        assertTrue(options.hasOutputFormat());
        assertEquals(OutputFormat.TREE, options.getOutputFormat());

         options = setUpSut(new String[] {"-f", "jpg"});
         assertTrue(options.hasOutputFormat());
         assertEquals(OutputFormat.JPG, options.getOutputFormat());
 
         options = setUpSut(new String[] {"-f", "gif"});
         assertTrue(options.hasOutputFormat());
         assertEquals(OutputFormat.GIF, options.getOutputFormat());
 
         options = setUpSut(new String[] {"-f", "png"});
         assertTrue(options.hasOutputFormat());
         assertEquals(OutputFormat.PNG, options.getOutputFormat());
 
         options = setUpSut(new String[] {"-f", "foobar"});
         assertTrue(options.hasOutputFormat());
         assertEquals(OutputFormat.JPG, options.getOutputFormat());
     }
 
     @Test public void format() {
         final String expectedHelpMessage = String.format(
            "usage: jebnf [-d] [-f <format>] [-h] [-i] [-o <file>] [-s <file>] [-v]%n" +
             "%n" +
             "Write sone helpful text.%n" +
             " -d            Enables debug output.%n" +
            " -f <format>   Output format: tree, xml, jpg, gif, or png.%n" +
             " -h            This help.%n" +
             " -i,--ide      Starts the GUI IDE.%n" +
             " -o <file>     Output file name. If omitted output will be print to%n" +
             "               STDOUT.%n" +
             " -s <file>     EBNF syntax file to parse. Required option, unless you use%n" +
             "               the IDE.%n" +
             " -v            Show version information.%n" +
             "%n" +
             "Written 2012 by Sven Strittmatter <weltraumschaf@googlemail.com>%n" +
             "Write bugs to https://github.com/Weltraumschaf/jebnf/issues%n");
         final CapturingOutputStream stream = new CapturingOutputStream();
         final PrintStream out = new PrintStream(stream);
 
         final HelpFormatter formatter = new HelpFormatter();
         final CliOptions options = new CliOptions();
         options.format(formatter, out);
         assertEquals(expectedHelpMessage, stream.getCapturedOutput());
     }
 
 }
