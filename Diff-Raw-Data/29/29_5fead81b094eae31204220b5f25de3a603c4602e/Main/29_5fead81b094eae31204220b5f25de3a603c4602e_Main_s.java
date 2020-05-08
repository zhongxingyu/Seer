 package adfgvx;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Scanner;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 /**
  * This class takes care of the basic setup of the application, like scanning
  * the command line parameters and initiating the analysis.
  * 
  * @author Ben Ruijl
  * 
  */
 public class Main {
     /** Logger. */
     private static final Logger LOG = Logger.getLogger(Main.class);
 
     private static final String cipherTextTest2 = "DAVFGDFVDDVFGDVADVVFFGDVAVVDFVD"
 	    + "DDXDAFADADVAGADVFAGADDFGXAGXAAADAVGFGFAGGFVVFDGGGFAGXGVVFDADAGAVDVVFGXGFFVVDD"
 	    + "GDGFFXFAVFVVFGDGFXFDGGFXDGVAVAVFFDDDXDFDFGAFGFFVGAGADAVAFDAGXAVAAFGFGXADVAVAAAV"
 	    + "DDFAXVFAVDFFAGVGVVVDFGFFVAVDFVFVVAGGDVFVFDVDFVFVFFFDVGVVGVADFAGDFVAGDAAGFAXADADXD"
 	    + "FAXXDGGFDGAFDXXAVFXFAGGXGGFXFFFDAFVFFDFVDDFGXVFFDDVVDAVDFVFVFVVVVAFFVDAAVDAADXDAGFA"
 	    + "GGAGGDXDXAAFAGGAAVGDDFDGDAAAGFGFXAAFGFXDFGFGAADADDXFGFFDAFGGDVFGDDXGAVXGFFVFXAADFVXVFD"
 	    + "VDFDVVVXDVGAGFVFGVFXVDDVDDGVAGGVDVAVFDVVVVVADVFGGDGVVAGXFVV";
 
     private static final String cipherTextTest = "VXDFFFDADFDAAXDVAFFXAAAXFFGFXDDVFGAVXDXADXDXFFAAGDAVGDAADVGGGFAXA"
 	    + "AXDDXDDAFADGAVDDGDXAXDAADADADAFXDXADGVFVDVXDVDDXGVGAXXXDAFDFAGXFD"
 	    + "DFDAAFGXXGGAXXAAFADVXDGDGXGVAXGFXXDXDDDXGAXXGFFXDAXVVDDDFDAFXAFDG"
 	    + "DAFFXGFGDAFAVFDAAAVFAAFAVDXAFDXDGFGAAXXGVDXADXVDXAGVAGFDVVDXFADDF"
 	    + "GFDXXXXFAAXXADFDFVAXXXFGAVDDXADFAGVXFFVVDDVXXAAVDGAAAFXVDXDAFDDFX"
 	    + "XFDFXXDXVAVGGGAADFAVFDAGAFAFVVXVFFDAVGGFDDFDADDDGFFDFGAAAFDDXFADD"
 	    + "AVFGXGVAAAGDAFXDFFXFVVDVAADAGXAAXVDFAXVGDDXDAADXFDVADFVFAFDGDGGXX"
 	    + "DGVXVAFXXGFDDFGDADADDAADAXADXAAAFDXDXAFDX";
 
     /**
      * Creates the main application.
      * 
      * @param cipherText
      *            Cipher text / source file
      * @param pat
      *            Filename of pattern frequencies
      * @param tet
      *            Filename of tetgram frequencies
      */
     public Main(final String cipherText, final String pat, final String tet) {
 	try {
 	    final Pattern pattern = new Pattern(pat);
 	    final Tetragram tetragram = new Tetragram(tet);
 	    final Analysis analysis = new Analysis(pattern, tetragram);
 
 	    final String largeText = readCipher(cipherText);
 
 	    for (int i = 0; i < 1000; i++) {
 		analysis.doAnalysis(largeText, 2); // do analysis
 	    }
 
 	    LOG.info("Correct ones: " + analysis.getCorrectAnalysis());
 
 	    // analysis.doHillclimbTestRun(largeText); // just solve a mono sub
 	    // analysis.decrypt(cipherTextTest, 16);
 	} catch (final IOException e) {
 	    e.printStackTrace();
 	}
 
     }
 
     /**
      * Strips a text from all non-uppercase characters.
      * 
      * @param filename
      *            Filename
      * @return Stripped text
      */
     public final String readCipher(final String filename) {
 	String file;
 	try {
 	    file = new Scanner(new File(filename)).useDelimiter("\\Z").next();
 	    file = file.replaceAll("[^A-Z]", "");
 	    return file;
 	} catch (final FileNotFoundException e) {
 	    e.printStackTrace();
 	}
 
 	return null;
     }
 
     /**
      * Start of the application.
      * 
      * @param args
      *            Command line arguments
      */
     public static void main(final String[] args) {
 	BasicConfigurator.configure();
 	LOG.setLevel(Level.INFO);
 	LOG.info("Started");
 
 	final Options options = new Options();
 	options.addOption("v", false, "Be verbose");
 	options.addOption("c", true, "Cipher text");
 	options.addOption("p", true, "Patterns");
 	options.addOption("t", true, "tetragrams");
 	options.addOption("help", false, "Display help message");
 
 	final CommandLineParser parser = new GnuParser();
 	try {
 	    final CommandLine cmd = parser.parse(options, args);
 
 	    if (cmd.hasOption("help")) {
 		final HelpFormatter formatter = new HelpFormatter();
 		formatter.printHelp("adfgvSolver", options);
 
 		return;
 	    }
 
 	    if (cmd.hasOption("c") && cmd.hasOption("p") && cmd.hasOption("t")) {
 		new Main(cmd.getOptionValue("c"), cmd.getOptionValue("p"),
 			cmd.getOptionValue("t"));
 	    } else {
 		LOG.error("Please check input.");
 
 		final HelpFormatter formatter = new HelpFormatter();
 		formatter.printHelp("adfgvSolver", options);
 
 		return;
 	    }
 
 	} catch (final ParseException e) {
 	    LOG.info("Could not parse arguments. Pass -help for help."
 		    + e.getMessage());
 	}
 
     }
 }
