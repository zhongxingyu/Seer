 package CLInterface;
 import static org.junit.Assert.*;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 
 /**
  * Test cases for PipelineConverter
  * 
  * @author Chris Tandiono
  */
 public class PipelineConverterTest {
 
 	Options options;
 	CommandLineParser parser;
 	
 	/**
 	 * @throws Exception if setUp was unsuccessful
 	 */
 	@Before
 	public final void setUp() throws Exception {
 		options = PipelineConverter.makeOptions();
 		parser = new PosixParser();
 	}
 	
 	/**
 	 * @throws Exception if tearDown was unsuccessful
 	 */
 	@After
 	public final void tearDown() throws Exception {
 		ConverterConfig.reset();
 	}
 	
 	/**
 	 * Tests Galaxy input (invalid command line: no input app dir)
 	 * @throws ParseException if the test has been improperly configured
 	 */
 	@Test
 	public final void testConfigureInputInvalidGalaxyIn() throws ParseException {
 		Format inFormat = Format.GALAXY;
 		Format outFormat = Format.TAVERNA;
 		
 		String[] args = {"-i", "foo."+inFormat.getExtension(), "--output-format", outFormat.getExtension()};
 		CommandLine cmd;
 		cmd = parser.parse(options, args);
 		try {
 			PipelineConverter.configureInput(cmd);
 			fail("Failed to catch invalid input");
 		} catch (InvalidInputException e) {
 			/* do nothing: this is the expected result */
 		}
 	}
 	
 	/**
 	 * Simple test with simple arguments
 	 * @throws ParseException if the test has been improperly configured
 	 */
 	@Test
 	public final void testConfigureInputSimple() throws ParseException {
 		Format inFormat = Format.LONI;
 		String inFile = "foo." + inFormat.getExtension();
 		Format outFormat = Format.TAVERNA;
 		String outFile = "bar." + outFormat.getExtension();
 		
 		String[] args = {"-i", inFile, "-o", outFile, "-v"};
 		CommandLine cmd = parser.parse(options, args);
 		PipelineConverter.configureInput(cmd);
 		
 		assertTrue(ConverterConfig.INPUT_FORMAT == inFormat);
 		assertTrue(ConverterConfig.INPUT_PATH.equals(inFile));
 		assertTrue(ConverterConfig.GALAXY_INPUT_DIR == null);
 	}
 	
 	/**
 	 * Tests -c option
 	 * @throws ParseException if the test has been improperly configured
 	 */
 	@Test
 	public final void testConfigureInputStdout() throws ParseException {
 		Format inFormat = Format.LONI;
 		String inFile = "foo." + inFormat.getExtension();
 		Format outFormat = Format.TAVERNA;
 		
 		String[] args = {"-i", inFile, "--output-format", outFormat.getExtension(), "-c"};
 		CommandLine cmd = parser.parse(options, args);
 		PipelineConverter.configureInput(cmd);
 		
 		assertTrue(ConverterConfig.INPUT_FORMAT == inFormat);
 		assertTrue(ConverterConfig.INPUT_PATH.equals(inFile));
 		assertTrue(ConverterConfig.GALAXY_INPUT_DIR == null);
 	}
 	
 	/**
 	 * Tests Galaxy input (valid command line)
 	 * @throws ParseException if the test has been improperly configured
 	 */
 	@Test
 	public final void testConfigureInputValidGalaxyIn() throws ParseException {
 		Format inFormat = Format.GALAXY;
 		String inFile = "foo." + inFormat.getExtension();
 		Format outFormat = Format.TAVERNA;
 		
 		String[] args = {"-i", inFile, "--galaxy-app-dir", ".", "--output-format", outFormat.getExtension()};
 		CommandLine cmd = parser.parse(options, args);
 		PipelineConverter.configureInput(cmd);
 		
 		assertTrue(ConverterConfig.INPUT_FORMAT == inFormat);
 		assertTrue(ConverterConfig.INPUT_PATH.equals(inFile));
 		assertTrue(ConverterConfig.GALAXY_INPUT_DIR.equals("."));
 	}
 
 	/**
 	 * Tests Galaxy output (valid command line)
 	 * @throws ParseException if the test has been improperly configured
 	 */
 	@Test
 	public final void testConfigureInputValidGalaxyOut() throws ParseException {
 		Format inFormat = Format.TAVERNA;
 		String inFile = "foo." + inFormat.getExtension();
 		Format outFormat = Format.GALAXY;
 		
 		String[] args = {"-i", inFile, "--galaxy-output-app-dir", ".", "--output-format", outFormat.getExtension()};
 		CommandLine cmd = parser.parse(options, args);
 		PipelineConverter.configureInput(cmd);
 		
 		assertTrue(ConverterConfig.INPUT_FORMAT == inFormat);
 		assertTrue(ConverterConfig.INPUT_PATH.equals(inFile));
 		assertTrue(ConverterConfig.GALAXY_INPUT_DIR == null);
 	}
 	
 	/**
 	 * Tests Galaxy output (valid command line) again
 	 * @throws ParseException if the test has been improperly configured
 	 */
 	@Test
 	public final void testConfigureInputValidGalaxyOut2() throws ParseException {
 		Format inFormat = Format.TAVERNA;
 		String inFile = "foo." + inFormat.getExtension();
 		Format outFormat = Format.GALAXY;
 		String outFile = "asdf." + outFormat.getExtension();
 		
 		String[] args = {"-i", inFile, "--galaxy-output-app-dir", ".", "-o", outFile};
 		CommandLine cmd = parser.parse(options, args);
 		PipelineConverter.configureInput(cmd);
 		
 		assertTrue(ConverterConfig.INPUT_FORMAT == inFormat);
 		assertTrue(ConverterConfig.INPUT_PATH.equals(inFile));
 		assertTrue(ConverterConfig.GALAXY_INPUT_DIR == null);
 	}
 	
 	/**
 	 * Tests Galaxy output (invalid command line: missing output app dir)
 	 * @throws ParseException if the test has been improperly configured
 	 */
 	@Test
 	public final void testConfigureOutputInvalidGalaxyOut() throws ParseException {
 		Format inFormat = Format.TAVERNA;
 		String inFile = "foo." + inFormat.getExtension();
 		Format outFormat = Format.GALAXY;
 		
 		String[] args = {"-i", inFile, "--output-format", outFormat.getExtension()};
 		CommandLine cmd = parser.parse(options, args);
 		try {
 			PipelineConverter.configureOutput(cmd);
 			fail("Failed to catch invalid input");
 		} catch (InvalidInputException e) {
 			/* do nothing */
 		}
 	}
 	
 	/**
 	 * Tests Galaxy output (invalid command line: missing output app dir) again
 	 * @throws ParseException if the test has been improperly configured
 	 */
 	@Test
 	public final void testConfigureOutputInvalidGalaxyOut2() throws ParseException {
 		Format inFormat = Format.TAVERNA;
 		String inFile = "foo." + inFormat.getExtension();
 		Format outFormat = Format.GALAXY;
 		String outFile = "asdf." + outFormat.getExtension();
 		
 		String[] args = {"-i", inFile, "-o", outFile};
 		CommandLine cmd = parser.parse(options, args);
 		try {
 			PipelineConverter.configureOutput(cmd);
 			fail("Failed to catch invalid input");
 		} catch (InvalidInputException e) {
 			/* do nothing */
 		}
 	}
 	
 	/**
 	 * Tests output (invalid command line: missing output extension)
 	 * @throws ParseException if the test has been improperly configured
 	 */
 	@Test
 	public final void testConfigureOutputInvalidOutExtension() throws ParseException {
 		Format inFormat = Format.TAVERNA;
 		String inFile = "foo." + inFormat.getExtension();
 		String outFile = "asdf.bar";
 		
 		String[] args = {"-i", inFile, "-o", outFile};
 		CommandLine cmd = parser.parse(options, args);
 		try {
 			PipelineConverter.configureOutput(cmd);
 			fail("Failed to catch invalid input");
 		} catch (InvalidInputException e) {
 			/* do nothing */
 		}
 	}
 	
 	/**
 	 * Test that specified output format must match output filename
 	 * @throws ParseException if the test has been improperly configured
 	 */
 	@Test
 	public final void testConfigureOutputMatchOutExtension() throws ParseException {
 		Format inFormat = Format.LONI;
 		String inFile = "foo." + inFormat.getExtension();
 		Format outFormat = Format.TAVERNA;
 		String outFile = "bar." + outFormat.getExtension();
 		
 		String[] args = {"-i", inFile, "-o", outFile, "-v", "--output-format", outFormat.getExtension()};
 		CommandLine cmd = parser.parse(options, args);
 		PipelineConverter.configureInput(cmd);
 		PipelineConverter.configureOutput(cmd);
 		
 		assertTrue(ConverterConfig.OUTPUT_FORMAT == outFormat);
 		assertTrue(ConverterConfig.OUTPUT_PATH.equals(outFile));
 		assertTrue(ConverterConfig.GALAXY_OUTPUT_DIR == null);
 		assertTrue(ConverterConfig.OUTPUT == null);
 	}
 	
 	/**
 	 * Tests output (don't allow forcing ridiculous file names)
 	 * @throws ParseException if the test has been improperly configured
 	 */
 	@Test
 	public final void testConfigureOutputMismatchOutExtension() throws ParseException {
 		Format inFormat = Format.TAVERNA;
 		String inFile = "foo." + inFormat.getExtension();
 		Format outFormat = Format.LONI;
 		String outFile = "asdf." + outFormat.getExtension();
 		
 		String[] args = {"-i", inFile, "-o", outFile, "-f", "--output-format", Format.GALAXY.getExtension()};
 		CommandLine cmd = parser.parse(options, args);
 		PipelineConverter.configureSpecial(cmd);
 		try {
 			PipelineConverter.configureOutput(cmd);
 			fail("Failed to catch invalid input");
 		} catch (InvalidInputException e) {
 			/* do nothing */
 		}
 	}
 	
 	/**
 	 * Tests output (invalid command line: missing output extension)
 	 * @throws ParseException if the test has been improperly configured
 	 */
 	@Test
 	public final void testConfigureOutputMissingOutExtension() throws ParseException {
 		String[] args = {"-i", "foo.t2flow", "-o", "asdf.bar/foo"};
 		CommandLine cmd = parser.parse(options, args);
 		try {
 			PipelineConverter.configureOutput(cmd);
 			fail("Failed to catch invalid input");
 		} catch (InvalidInputException e) {
 			/* do nothing */
 		}
 	}
 	
 	/**
 	 * Simple test with simple arguments
 	 * @throws ParseException if the test has been improperly configured
 	 */
 	@Test
 	public final void testConfigureOutputSimple() throws ParseException {
 		Format outFormat = Format.TAVERNA;
 		String outFile = "bar." + outFormat.getExtension();
 		
 		String[] args = {"-i", "foo.pipe", "-o", outFile, "-v"};
 		CommandLine cmd = parser.parse(options, args);
 		PipelineConverter.configureInput(cmd);
 		PipelineConverter.configureOutput(cmd);
 		
 		assertTrue(ConverterConfig.OUTPUT_FORMAT == outFormat);
 		assertTrue(ConverterConfig.OUTPUT_PATH.equals(outFile));
 		assertTrue(ConverterConfig.GALAXY_OUTPUT_DIR == null);
 		assertTrue(ConverterConfig.OUTPUT == null);
 	}
 	
 	/**
 	 * Tests -c option
 	 * @throws ParseException if the test has been improperly configured
 	 */
 	@Test
 	public final void testConfigureOutputStdout() throws ParseException {
 		Format outFormat = Format.TAVERNA;
 		
 		String[] args = {"-i", "foo.pipe", "--output-format", outFormat.getExtension(), "-c"};
 		CommandLine cmd = parser.parse(options, args);
 		PipelineConverter.configureInput(cmd);
 		PipelineConverter.configureOutput(cmd);
 		
 		assertTrue(ConverterConfig.OUTPUT_FORMAT == outFormat);
 		assertTrue(ConverterConfig.OUTPUT_PATH == null);
 		assertTrue(ConverterConfig.GALAXY_OUTPUT_DIR == null);
 		assertTrue(ConverterConfig.OUTPUT != null);
 	}
 	
 	/**
 	 * Tests Galaxy input (valid command line)
 	 * @throws ParseException if the test has been improperly configured
 	 */
 	@Test
 	public final void testConfigureOutputValidGalaxyIn() throws ParseException {
 		Format inFormat = Format.GALAXY;
 		String name = "foo.";
 		String inFile = name + inFormat.getExtension();
 		Format outFormat = Format.TAVERNA;
 		String outFile = name + outFormat.getExtension();
 		
 		String[] args = {"-i", inFile, "--galaxy-app-dir", ".", "--output-format", outFormat.getExtension()};
 		CommandLine cmd = parser.parse(options, args);
 		PipelineConverter.configureInput(cmd);
 		PipelineConverter.configureOutput(cmd);
 		
 		assertTrue(ConverterConfig.OUTPUT_FORMAT == outFormat);
 		assertTrue(ConverterConfig.OUTPUT_PATH.equals(outFile));
 		assertTrue(ConverterConfig.GALAXY_OUTPUT_DIR == null);
 		assertTrue(ConverterConfig.OUTPUT == null);
 	}
 	
 	/**
 	 * Tests Galaxy output (valid command line)
 	 * @throws ParseException if the test has been improperly configured
 	 */
 	@Test
 	public final void testConfigureOutputValidGalaxyOut() throws ParseException {
 		String name = "foo.";
 		Format outFormat = Format.GALAXY;
 		
 		String outputDir = ".";
 		String[] args = {"-i", name + Format.TAVERNA.getExtension(), "--galaxy-output-app-dir", outputDir, "--output-format", outFormat.getExtension()};
 		CommandLine cmd = parser.parse(options, args);
 		PipelineConverter.configureInput(cmd);
 		PipelineConverter.configureOutput(cmd);
 		
 		assertTrue(ConverterConfig.OUTPUT_FORMAT == outFormat);
 		assertTrue(ConverterConfig.OUTPUT_PATH.equals(name + outFormat.getExtension()));
 		assertTrue(ConverterConfig.GALAXY_OUTPUT_DIR.equals(outputDir));
 		assertTrue(ConverterConfig.OUTPUT == null);
 	}
 	
 	/**
 	 * Tests Galaxy output (valid command line) again
 	 * @throws ParseException if the test has been improperly configured
 	 */
 	@Test
 	public final void testConfigureOutputValidGalaxyOut2() throws ParseException {
 		Format outFormat = Format.GALAXY;
 		String outFile = "asdf." + outFormat.getExtension();
 		
 		String inputDir = ".";
 		String[] args = {"-i", "foo.t2flow", "--galaxy-output-app-dir", inputDir, "-o", outFile};
 		CommandLine cmd = parser.parse(options, args);
 		PipelineConverter.configureInput(cmd);
 		PipelineConverter.configureOutput(cmd);
 		
 		assertTrue(ConverterConfig.OUTPUT_FORMAT == outFormat);
 		assertTrue(ConverterConfig.OUTPUT_PATH.equals(outFile));
 		assertTrue(ConverterConfig.GALAXY_OUTPUT_DIR.equals(inputDir));
 		assertTrue(ConverterConfig.OUTPUT == null);
 	}
 	
 	/**
	 * Tests output (invalid command line: no output configuration)
	 * @throws ParseException if the test has been improperly configured
	 */
	@Test
	public final void testConfigureOutputMissingOutput() throws ParseException {
		String[] args = {"-i", "foo.t2flow"};
		CommandLine cmd = parser.parse(options, args);
		try {
			PipelineConverter.configureOutput(cmd);
			fail("Failed to catch invalid input");
		} catch (InvalidInputException e) {
			/* do nothing */
		}
	}
	
	/**
 	 * Tests conversion of file extension to Format enum. Kind of a silly test...
 	 */
 	@Test
 	public final void testExtToFormat() {
 		for (int i = 0; i < Format.values().length; i++) {
 			assertTrue(PipelineConverter.extToFormat(Format.values()[i].getExtension()) == Format.values()[i]);
 		}
 		
 		try {
 			PipelineConverter.extToFormat(".ga");
 			fail("Failed to catch invalid input");
 		} catch (InvalidInputException e) { }
 		try {
 			PipelineConverter.extToFormat("a");
 			fail("Failed to catch invalid input");
 		} catch (InvalidInputException e) { }
 		try {
 			PipelineConverter.extToFormat("ga ");
 			fail("Failed to catch invalid input");
 		} catch (InvalidInputException e) { }
 	}
 	
 	/**
 	 * Simple test with no arguments
 	 * @throws ParseException if the test has been improperly configured
 	 */
 	@Test
 	public final void testMakeOptionsNoArgs() {
 		String[] args = {"-i", "foo.pipe", "-o", "bar.t2flow", "-v"};
 		try {
 			@SuppressWarnings("unused")
 			CommandLine cmd = parser.parse(options, args);
 		} catch (ParseException e) {
 			/* do nothing */
 		}
 	}
 	
 }
