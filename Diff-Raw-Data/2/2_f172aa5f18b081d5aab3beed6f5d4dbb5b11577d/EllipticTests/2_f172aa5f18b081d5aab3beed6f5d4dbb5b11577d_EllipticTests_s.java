 package algorithms.verifiers.Main;
 
 import main.Logger;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import algorithms.params.Parameters.Type;
 import algorithms.verifiers.MainVerifier;
 import algorithms.verifiers.MainVerifierTests;
 
 public class EllipticTests {
 	
 	private final Logger logger = new MockedLogger();
 	
 	
 	//****************SHUFFLE*******************
 	
 	@Test
 	public void VerifyShuffleECurveSmallTest() throws Exception {
 
 		MainVerifier mainVer = new MainVerifier(logger);
 		Assert.assertTrue(mainVer.verify(getClass().getClassLoader()
 				.getResource("exportEcurveSmall/protInfo.xml").getFile(), getClass()
 				.getClassLoader().getResource("exportEcurveSmall/default").getFile(),
 				Type.MIXING, "default", 1, true, true, false));
 	}
 	
 	@Test
 	public void VerifyShuffleECurveTest() throws Exception {
 
 		MainVerifier mainVer = new MainVerifier(logger);
 		Assert.assertTrue(mainVer.verify(getClass().getClassLoader()
 				.getResource("export/protInfo.xml").getFile(), getClass()
 				.getClassLoader().getResource("export/default").getFile(),
 				Type.MIXING, "default", 1, true, true, false));
 	}
 	
 	@Test
 	public void VerifyShuffleExportDecTest1() throws Exception {
 
 		MainVerifier mainVer = new MainVerifier(logger);
 		Assert.assertTrue(mainVer.verify(
 				getClass().getClassLoader()
 						.getResource("exportsDecOutputs/w1Elliptic/protInfo.xml").getFile(),
 				getClass().getClassLoader()
 						.getResource("exportsDecOutputs/w1Elliptic/default").getFile(),
 				Type.MIXING, "default", 1, true, true, false));
 	}
 	
 	@Test
 	public void VerifyShuffleExportDecTest3() throws Exception {
 
 		MainVerifier mainVer = new MainVerifier(logger);
 		Assert.assertTrue(mainVer.verify(
 				getClass().getClassLoader()
 						.getResource("exportsDecOutputs/w3Elliptic/protInfo.xml").getFile(),
 				getClass().getClassLoader()
 						.getResource("exportsDecOutputs/w3Elliptic/default").getFile(),
 				Type.MIXING, "default", 3, true, true, false));
 	}
 	
 	@Test
 	public void VerifyShuffleEcurveTestTypeDec() throws Exception {
 
 		MainVerifier mainVer = new MainVerifier(
 				new MainVerifierTests.MockedLogger());
 		Assert.assertTrue(mainVer.verify(getClass().getClassLoader()
 				.getResource("exportDec/protInfo.xml").getFile(), getClass()
 				.getClassLoader().getResource("exportDec/default").getFile(),
 				Type.DECRYPTION, "default", 1, true, true, false));
 	}
 	
 	
 	//******************DECRYPTION*******************
 
 	@Test
 	public void VerifyDecEcurveSmallTest() throws Exception {
 
 		MainVerifier mainVer = new MainVerifier(
 				new MainVerifierTests.MockedLogger());
 		Assert.assertTrue(mainVer.verify(getClass().getClassLoader()
 				.getResource("exportEcurveSmall/protInfo.xml").getFile(), getClass()
 				.getClassLoader().getResource("exportEcurveSmall/default").getFile(),
 				Type.MIXING, "default", 1, false, false, true));
 	}
 
 	
 	
 	@Test
 	public void VerifyDecEcurveTest() throws Exception {
 
 		MainVerifier mainVer = new MainVerifier(
 				new MainVerifierTests.MockedLogger());
 		Assert.assertTrue(mainVer.verify(getClass().getClassLoader()
 				.getResource("exportDec/protInfo.xml").getFile(), getClass()
 				.getClassLoader().getResource("exportDec/default").getFile(),
 				Type.DECRYPTION, "default", 1, false, false, true));
 	}
 
 	
 	@Test
 	public void VerifyDecExportDecTest1() throws Exception {
 
 		MainVerifier mainVer = new MainVerifier(logger);
 		Assert.assertTrue(mainVer.verify(
 				getClass().getClassLoader()
 						.getResource("exportsDecOutputs/w1Elliptic/protInfo.xml").getFile(),
 				getClass().getClassLoader()
 						.getResource("exportsDecOutputs/w1Elliptic/default").getFile(),
 				Type.MIXING, "default", 1, false, false, true));
 	}
 	
 	
 	@Test
 	public void VerifyDecExportDecTest3() throws Exception {
 
 		MainVerifier mainVer = new MainVerifier(logger);
 		Assert.assertTrue(mainVer.verify(
 				getClass().getClassLoader()
 						.getResource("exportsDecOutputs/w3Elliptic/protInfo.xml").getFile(),
 				getClass().getClassLoader()
 						.getResource("exportsDecOutputs/w3Elliptic/default").getFile(),
 				Type.MIXING, "default", 3, false, false, true));
 	}
 	
 	@Test
 	public void VerifyDecExportDecTest4() throws Exception {
 
 		MainVerifier mainVer = new MainVerifier(logger);
 		Assert.assertTrue(mainVer.verify(
 				getClass().getClassLoader()
 						.getResource("exportsDecOutputs/w4EllipticDec/protInfo.xml").getFile(),
 				getClass().getClassLoader()
 						.getResource("exportsDecOutputs/w4EllipticDec/default").getFile(),
				Type.MIXING, "default", 3, false, false, true));
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
