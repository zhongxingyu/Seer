 package main;
 
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetEncoder;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import algorithms.params.Parameters.Type;
 
 /**
  * Tests for the CommandLineParser class.
  * 
  * @author Daniel
  * 
  */
 public class CommandLineParserTests {
 	private CommandLineParser parser = new CommandLineParser();
 	private CharsetEncoder asciiEncoder = Charset.forName("US-ASCII")
 			.newEncoder();
 
 	@Test
 	// not legal - should print the command usage
 	public void caseOneArgs_notEnoughtTest() {
 		String[] argv = new String[1];
 		argv[0] = "-mix";
 		parser.parseCommand(argv);
 	}
 
 	@Test
 	// not legal - should print the command usage
 	public void caseTwoArgs_notEnoughtTest() {
 		String[] argv = new String[2];
 		argv[0] = "verifier";
 		argv[1] = "-test";
 		parser.parseCommand(argv);
 	}
 
 	@Test
 	// should print the versions that we support
	public void caseOneArgs_compatTest() {
 		String[] argv = new String[1];
 		argv[0] = "-compat";
 		parser.parseCommand(argv);
 	}
 
 	@Test
 	// not legal - should print the command usage
 	public void caseTwoArgs_notCompatTest() {
 		String[] argv = new String[2];
 		argv[0] = "verifier";
 		argv[1] = "-com";
 		parser.parseCommand(argv);
 	}
 
 	@Test
 	public void defaultMixingTest() {
 		String[] argv = new String[3];
 		argv[0] = "-mix";
 		argv[1] = "protInfo.xml";
 		argv[2] = "directory";
 		parser.parseCommand(argv);
 
 		Assert.assertEquals("default", parser.getAuxsid());
 		Assert.assertTrue(parser.getPosc());
 		Assert.assertTrue(parser.getCcpos());
 		Assert.assertTrue(parser.getDec());
 		Assert.assertEquals(0, parser.getWidth());
 
 		Assert.assertTrue(Type.MIXING.equals(parser.getType()));
 		Assert.assertTrue(parser.shouldVerify());
 	}
 
 	@Test
 	public void wrongArgMixingTest() {
 		String[] argv = new String[4];
 		argv[0] = "-mix";
 		argv[1] = "protInfo.xml";
 		argv[2] = "directory";
 		argv[3] = "list";
 		parser.parseCommand(argv);
 
 		Assert.assertEquals("default", parser.getAuxsid());
 		Assert.assertTrue(parser.getPosc());
 		Assert.assertTrue(parser.getCcpos());
 		Assert.assertTrue(parser.getDec());
 		Assert.assertEquals(0, parser.getWidth());
 
 		Assert.assertTrue(Type.MIXING.equals(parser.getType()));
 		Assert.assertFalse(parser.shouldVerify());
 	}
 
 	@Test
 	public void defaultShufflingTest() {
 		String[] argv = new String[3];
 		argv[0] = "-shuffle";
 		argv[1] = "protInfo.xml";
 		argv[2] = "directory";
 		parser.parseCommand(argv);
 
 		Assert.assertEquals("default", parser.getAuxsid());
 		Assert.assertTrue(parser.getPosc());
 		Assert.assertTrue(parser.getCcpos());
 		Assert.assertFalse(parser.getDec());
 		Assert.assertEquals(0, parser.getWidth());
 
 		Assert.assertTrue(Type.SHUFFLING.equals(parser.getType()));
 		Assert.assertTrue(parser.shouldVerify());
 	}
 
 	@Test
 	public void defaultDecryptionTest() {
 		String[] argv = new String[3];
 		argv[0] = "-decrypt";
 		argv[1] = "protInfo.xml";
 		argv[2] = "directory";
 		parser.parseCommand(argv);
 
 		Assert.assertEquals("default", parser.getAuxsid());
 		Assert.assertFalse(parser.getPosc());
 		Assert.assertFalse(parser.getCcpos());
 		Assert.assertTrue(parser.getDec());
 		Assert.assertEquals(0, parser.getWidth());
 
 		Assert.assertTrue(Type.DECRYPTION.equals(parser.getType()));
 		Assert.assertTrue(parser.shouldVerify());
 	}
 
 	@Test
 	public void nodecFlag_DecryptionTest() {
 		String[] argv = new String[4];
 		argv[0] = "-decrypt";
 		argv[1] = "protInfo.xml";
 		argv[2] = "directory";
 		argv[3] = "-nodec";
 		parser.parseCommand(argv);
 
 		Assert.assertEquals("default", parser.getAuxsid());
 		Assert.assertFalse(parser.getPosc());
 		Assert.assertFalse(parser.getCcpos());
 		Assert.assertTrue(parser.getDec());
 		Assert.assertEquals(0, parser.getWidth());
 
 		Assert.assertTrue(Type.DECRYPTION.equals(parser.getType()));
 		Assert.assertFalse(parser.shouldVerify());
 	}
 
 	@Test
 	public void noposFlag_DecryptionTest() {
 		String[] argv = new String[4];
 		argv[0] = "-decrypt";
 		argv[1] = "protInfo.xml";
 		argv[2] = "directory";
 		argv[3] = "-nopos";
 		parser.parseCommand(argv);
 
 		Assert.assertEquals("default", parser.getAuxsid());
 		Assert.assertFalse(parser.getPosc());
 		Assert.assertFalse(parser.getCcpos());
 		Assert.assertTrue(parser.getDec());
 		Assert.assertEquals(0, parser.getWidth());
 
 		Assert.assertTrue(Type.DECRYPTION.equals(parser.getType()));
 		Assert.assertFalse(parser.shouldVerify());
 	}
 
 	@Test
 	public void auxsidLegal_DecryptionTest() {
 		String[] argv = new String[5];
 		argv[0] = "-decrypt";
 		argv[1] = "protInfo.xml";
 		argv[2] = "directory";
 		argv[3] = "-auxsid";
 		argv[4] = "legal";
 		parser.parseCommand(argv);
 
 		Assert.assertEquals("legal", parser.getAuxsid());
 		Assert.assertFalse(parser.getPosc());
 		Assert.assertFalse(parser.getCcpos());
 		Assert.assertTrue(parser.getDec());
 		Assert.assertEquals(0, parser.getWidth());
 
 		Assert.assertTrue(Type.DECRYPTION.equals(parser.getType()));
 		Assert.assertTrue(parser.shouldVerify());
 	}
 
 	@Test
 	public void auxsidNotLegal_DecryptionTest() {
 		Assert.assertFalse(asciiEncoder.canEncode("Ral"));
 		String[] argv = new String[5];
 		argv[0] = "-decrypt";
 		argv[1] = "protInfo.xml";
 		argv[2] = "directory";
 		argv[3] = "-auxsid";
 		argv[4] = "Ral";
 		parser.parseCommand(argv);
 
 		Assert.assertEquals("default", parser.getAuxsid());
 		Assert.assertFalse(parser.getPosc());
 		Assert.assertFalse(parser.getCcpos());
 		Assert.assertTrue(parser.getDec());
 		Assert.assertEquals(0, parser.getWidth());
 
 		Assert.assertTrue(Type.DECRYPTION.equals(parser.getType()));
 		Assert.assertFalse(parser.shouldVerify());
 	}
 
 	@Test
 	public void auxsidMissingValue_DecryptionTest() {
 		String[] argv = new String[4];
 		argv[0] = "-decrypt";
 		argv[1] = "protInfo.xml";
 		argv[2] = "directory";
 		argv[3] = "-auxsid";
 		parser.parseCommand(argv);
 
 		Assert.assertEquals("default", parser.getAuxsid());
 		Assert.assertFalse(parser.getPosc());
 		Assert.assertFalse(parser.getCcpos());
 		Assert.assertTrue(parser.getDec());
 		Assert.assertEquals(0, parser.getWidth());
 
 		Assert.assertTrue(Type.DECRYPTION.equals(parser.getType()));
 		Assert.assertFalse(parser.shouldVerify());
 	}
 
 	@Test
 	public void widthMissingValue_DecryptionTest() {
 		String[] argv = new String[4];
 		argv[0] = "-decrypt";
 		argv[1] = "protInfo.xml";
 		argv[2] = "directory";
 		argv[3] = "-width";
 		parser.parseCommand(argv);
 
 		Assert.assertEquals("default", parser.getAuxsid());
 		Assert.assertFalse(parser.getPosc());
 		Assert.assertFalse(parser.getCcpos());
 		Assert.assertTrue(parser.getDec());
 		Assert.assertEquals(0, parser.getWidth());
 
 		Assert.assertTrue(Type.DECRYPTION.equals(parser.getType()));
 		Assert.assertFalse(parser.shouldVerify());
 	}
 
 	@Test
 	public void verboseTrue_MixingTest() {
 		String[] argv = new String[4];
 		argv[0] = "-mix";
 		argv[1] = "protInfo.xml";
 		argv[2] = "directory";
 		argv[3] = "-v";
 		parser.parseCommand(argv);
 
 		Assert.assertEquals("default", parser.getAuxsid());
 		Assert.assertTrue(parser.getPosc());
 		Assert.assertTrue(parser.getCcpos());
 		Assert.assertTrue(parser.getDec());
 		Assert.assertEquals(0, parser.getWidth());
 		Assert.assertTrue(parser.getVerbose());
 
 		Assert.assertTrue(Type.MIXING.equals(parser.getType()));
 		Assert.assertTrue(parser.shouldVerify());
 	}
 
 	@Test
 	public void allFlagsFalse_MixingTest() {
 		String[] argv = new String[5];
 		argv[0] = "-mix";
 		argv[1] = "protInfo.xml";
 		argv[2] = "directory";
 		argv[3] = "-nopos";
 		argv[4] = "-nodec";
 		parser.parseCommand(argv);
 
 		Assert.assertEquals("default", parser.getAuxsid());
 		Assert.assertFalse(parser.getPosc());
 		Assert.assertFalse(parser.getCcpos());
 		Assert.assertTrue(parser.getDec());
 		Assert.assertEquals(0, parser.getWidth());
 		Assert.assertFalse(parser.getVerbose());
 
 		Assert.assertTrue(Type.MIXING.equals(parser.getType()));
 		Assert.assertFalse(parser.shouldVerify());
 	}
 }
