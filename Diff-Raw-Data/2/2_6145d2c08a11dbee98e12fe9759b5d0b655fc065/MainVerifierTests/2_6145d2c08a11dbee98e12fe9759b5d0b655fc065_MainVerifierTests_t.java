 package algorithms.verifiers;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 
 import main.Logger;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import algorithms.params.Parameters;
 import algorithms.params.Parameters.Type;
 import cryptographic.primitives.CryptoUtils;
 import cryptographic.primitives.HashFunction;
 import cryptographic.primitives.SHA2HashFunction;
 
 /**
  * Tests for the MainVerifier class.
  * 
  * @author Sofi & Tagel & Itay & Daniel
  * 
  */
 public class MainVerifierTests {
 
 	private final Logger logger = new MockedLogger();
 
 	@Test
 	public void deriveSetsAndObjectsTest() {
 		Parameters params = new Parameters(getClass().getClassLoader()
 				.getResource("protInfo.xml").getFile(), getClass()
 				.getClassLoader().getResource("export/default").getFile(),
 				null, "auxsid", 1, false, false, false, logger);
 		params.fillFromXML();
 		params.fillFromDirectory();
 		HashFunction H = new SHA2HashFunction(params.getSh());
 		MainVerifier mainVer = new MainVerifier(params, H);
 		Assert.assertTrue(mainVer.deriveSetsAndObjects());
 	}
 
 	@Test
 	public void createPrefixToRoTest() throws UnsupportedEncodingException {
 		Parameters params = new Parameters(getClass().getClassLoader()
 				.getResource("protInfo.xml").getFile(), getClass()
 				.getClassLoader().getResource("export/default").getFile(),
 				null, "auxsid", 1, false, false, false, logger);
 		params.fillFromXML();
 		params.fillFromDirectory();
 		HashFunction H = new SHA2HashFunction(params.getSh());
 		MainVerifier mainVer = new MainVerifier(params, H);
 		mainVer.deriveSetsAndObjects();
 		Assert.assertNull(params.getPrefixToRO());
 		mainVer.createPrefixToRo();
 		byte[] prefix = params.getPrefixToRO();
 		Assert.assertEquals(CryptoUtils.bytesToHexString(prefix),
 				"992c7e81390dbd441e9e86004521efbb03d90b1a63d66413ed3d0323ec334334");
 	}
 
 	@Test
 	public void readKeysTest() throws IOException {
 		Parameters params = new Parameters(getClass().getClassLoader()
 				.getResource("protInfo.xml").getFile(), getClass()
 				.getClassLoader().getResource("export/default").getFile(),
 				null, "auxsid", 1, false, false, false, logger);
 		params.fillFromXML();
 		params.fillFromDirectory();
 		HashFunction H = new SHA2HashFunction(params.getSh());
 		MainVerifier mainVer = new MainVerifier(params, H);
 		mainVer.deriveSetsAndObjects();
 		Assert.assertTrue(mainVer.ReadKeys());
 		Assert.assertNotNull(params.getMixPublicKey());
 		Assert.assertEquals(2, params.getMixPublicKey().getSize());
 		Assert.assertNull(params.getShuffledCiphertexts());
 		// TODO - test case secret key does exits
 		Assert.assertNull(params.getMixSecretKey().getAt(0));
 		Assert.assertTrue(params.getProtVersion().equals(params.getVersion()));
 		Assert.assertEquals(params.getwDefault(), params.getW());
 	}
 
 	@Test
 	public void readListsTest() throws IOException {
 		Parameters params = new Parameters(getClass().getClassLoader()
 				.getResource("protInfo.xml").getFile(), getClass()
 				.getClassLoader().getResource("export/default").getFile(),
 				Type.MIXING, "auxsid", 1, false, false, false, logger);
 		params.fillFromXML();
 		params.fillFromDirectory();
 		HashFunction H = new SHA2HashFunction(params.getSh());
 		MainVerifier mainVer = new MainVerifier(params, H);
 		mainVer.deriveSetsAndObjects();
 		Assert.assertTrue(mainVer.ReadLists());
 		Assert.assertEquals(mainVer.getParams().getN(), 100);
 
 	}
 
 	@Test
 	public void VerifyShuffleTest() throws Exception {
 
 		MainVerifier mainVer = new MainVerifier(logger);
 		Assert.assertTrue(mainVer.verify(getClass().getClassLoader()
 				.getResource("protInfo.xml").getFile(), getClass()
 				.getClassLoader().getResource("export/default").getFile(),
 				Type.MIXING, "default", 1, true, true, false));
 	}
 	
 	@Test
 	public void VerifyDecTest() throws Exception {
 		
		MainVerifier mainVer = new MainVerifier(new MainVerifierTests.MockedLogger());
 		Assert.assertTrue(mainVer.verify(getClass().getClassLoader()
 				.getResource("protInfo.xml").getFile(), getClass()
 				.getClassLoader().getResource("export/default").getFile(),
 				Type.MIXING, "default", 1, false, false, true));
 	}
 
 	public static class MockedLogger extends Logger {
 		public MockedLogger() {
 			super(false);
 		}
 
 		@Override
 		public void sendLog(String message, Severity severity) {
 		}
 	}
 }
