 package algorithms.params;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import cryptographic.primitives.CryptoUtils;
 import cryptographic.primitives.HashFunction;
 import cryptographic.primitives.SHA2HashFunction;
 
 import algorithms.verifiers.MainVerifier;
 
 public class MainVerifierTests {
 
 	@Test
 	public void deriveSetsAndObjectsTest() {
 		Parameters params = new Parameters(getClass().getClassLoader()
 				.getResource("protInfo.xml").getFile(), getClass()
 				.getClassLoader().getResource("export/default").getFile(),
 				"type", "auxsid", 1, false, false, false);
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
 				"type", "auxsid", 1, false, false, false);
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
 				"type", "auxsid", 1, false, false, false);
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
 	}
 
 	@Test
 	public void readListsTest() {
 		// TODO - need to test after create random cipher text array
 	}
 
 }
