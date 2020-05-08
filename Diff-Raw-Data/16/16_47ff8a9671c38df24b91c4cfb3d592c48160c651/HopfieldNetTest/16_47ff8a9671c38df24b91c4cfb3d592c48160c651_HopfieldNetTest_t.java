 package hopfield.test;
 
 import hopfield.Config;
 import hopfield.HopfieldNet;
 import utils.PatternUtils;
 
 public class HopfieldNetTest {
 	
 	/**
 	 * Presenta el patron a la red sin cambios.
 	 */
 	public static final int TEST_PATTERN 			= 0;
 	/**
 	 * Aplica addNoise1() al patron introducido.
 	 */
 	public static final int TEST_PATTERN_NOISE_1 	= 1;
 	/**
 	 * Invierte el patron introducido.
 	 */
 	public static final int TEST_PATTERN_NOISE_2 	= 2;
 	/**
 	 * Invierte y aplica ruido al patron introducido.
 	 */
 	public static final int TEST_PATTERN_NOISE_3 	= 3;
 	/**
 	 * Borra la primera parte del patron.
 	 */
 	public static final int TEST_PATTERN_NOISE_4 	= 4;
 	
 	private HopfieldNet net;
 	private String[] patternNames;
 	private String recognizeName;
 	
 	public HopfieldNetTest(HopfieldNet net) {
 		this.net = net;
 	}
 	
 	public void setPatternNames(String[] patternNames) {
 		if (patternNames.length % 2 == 0) {
			System.out.println("Warning: using even number of patterns");
 		}
 		for (int i = 0; i < patternNames.length; i++) {
 			if (!patternNames[i].endsWith(".png")) {
 				patternNames[i] += ".png";
 			}
 		}
 		this.patternNames = patternNames;
 	}
 	
 	public void setRecognizeName(String recognizeName) {
 		if (!recognizeName.endsWith(".png")) {
 			recognizeName += ".png";
 		}
 		this.recognizeName = recognizeName;
 	}
 	
 	public void execTest(int testNumber) {
 		int[] original = Config.getImageAsState(recognizeName);
 		int[] recognized;
 		switch (testNumber) {
 			case TEST_PATTERN:
 				recognized = reconocerPatron(original.clone());
 				break;
 			case TEST_PATTERN_NOISE_1:
 				recognized = reconocerPatronConRuido1(original.clone());
 				break;
 			case TEST_PATTERN_NOISE_2:
 				recognized = reconocerPatronConRuido2(original.clone());
 				break;
 			case TEST_PATTERN_NOISE_3:
 				recognized = reconocerPatronConRuido3(original.clone());
 				break;
 			case TEST_PATTERN_NOISE_4:
 				recognized = reconocerPatronConRuido4(original.clone());
 				break;
 			default:
 				throw new IllegalArgumentException("Unknown test: " + testNumber);
 		}
 		Config.saveStateToImage(original, "Original");
 		Config.saveStateToImage(recognized,"Resultado");
 		System.out.println("Error: " + PatternUtils.error(recognized, original));
 	}
 
 	private int[] reconocerPatron(int[] pattern) {
 		return testNet(getPatterns(patternNames), pattern);
 	}
 
 	private int[] reconocerPatronConRuido1(int[] pattern) {
 		PatternUtils.addNoise2(pattern, 0.1f);
 		return testNet(getPatterns(patternNames), pattern);
 	}
 
 	private int[] reconocerPatronConRuido2(int[] pattern) {
 		PatternUtils.invert(pattern);
 		return testNet(getPatterns(patternNames), pattern);
 	}
 
 	private int[] reconocerPatronConRuido3(int[] pattern) {
 		PatternUtils.invert(pattern);
 		PatternUtils.addNoise2(pattern, 0.1f);
 		return testNet(getPatterns(patternNames), pattern);
 	}
 
 	private int[] reconocerPatronConRuido4(int[] pattern) {
 		PatternUtils.addNoiseH(pattern);
 		return testNet(getPatterns(patternNames), pattern);
 	}
 
 	private int[][] getPatterns(String[] patternNames) {
 		int totalPatterns = patternNames.length;
 		int[][] patterns = new int[totalPatterns][];
 		for (int i = 0; i < totalPatterns; i++) {
 			patterns[i] = Config.getImageAsState(patternNames[i]);
 		}
 		return patterns;
 	}
 
 	private int[] testNet(int[][] patterns, int[] recognize) {
 		Config.saveStateToImage(recognize, "Imagen presentada");
 		net.storePatterns(patterns);
 		net.initialize(recognize);
 		return net.iterateUntilConvergence();
 	}
 
 }
