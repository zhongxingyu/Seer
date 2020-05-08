 package org.apache.commons.cli;
 
 import java.io.ByteArrayOutputStream;
 import java.io.PrintWriter;
 
 import junit.framework.TestCase;
 
 public class HelpFormatterTest extends TestCase {
 
     private Options _options;
 
     protected void setUp() {
         _options = new Options();
         Option aOption = new Option("a", "Aa", false, "option A");
         Option bOption = new Option("b", "Bb", false, "option B");
         OptionGroup group1 = new OptionGroup();
         group1.addOption(aOption);
         group1.addOption(bOption);
         _options.addOptionGroup(group1);
     }
 
     /**
      * the setUp above used to print [-a | -b] [-a] [-b]
      */
     public void testOptionGroupDuplication() {
         String help = getFormattedHelp();
        String expectedHelp = new String("usage: syntax [-a | -b]\r\n-a,--Aa option A\r\n-b,--Bb option B\r\n");
        assertEquals("expected usage to be '" + expectedHelp + "' instead of '" + help + "'", expectedHelp, help);
     }
 
     /**
     * Options following an option group used to be non blank separated: [-b | -a][-o] instead of [-b | -a] [-o]
      */
     public void testOptionGroupSubsequentOptions() {
         _options.addOption(new Option("o", "Option O"));
         String help = getFormattedHelp();
         assertTrue(help.indexOf("][") < 0);
         assertTrue(help.indexOf("[-a | -b] [-o]") >= 0);
     }
 
     //
     // private methods
     //

     private String getFormattedHelp() {
         HelpFormatter formatter = new HelpFormatter();
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         PrintWriter pw = new PrintWriter(baos);
         formatter.printHelp(pw, 60, "syntax", null, _options, 0, 1, null, true);
         pw.flush();
         String usage = baos.toString();
         return usage;
     }

 }
