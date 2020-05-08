 import hopfield.AsynchHopfieldNet;
 import hopfield.AsynchHopfieldNetWithParityBit;
 import hopfield.HopfieldNet;
 import hopfield.PseudoInverseHopfield;
 import hopfield.SynchHopfieldNet;
 import hopfield.test.HopfieldNetTest;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 public class Main {
 	
 	private static final int INDEX_NET_TYPE 	= 0;
 	private static final int INDEX_TEST_TYPE 	= 1;
 	private static final int INDEX_TO_RECOGNIZE = 2;
 	private static final int INDEX_PATTERNS 	= 3;
 	
 	public static void main(String[] args) {
 		if (args == null || args.length == 0) {
 			System.out.println("No arguments given - Using defaults -");
 			args = getDefaultArgs();
 		}
 		if (args.length == 1) {
 			if ("help".equals(args[0].toLowerCase())) {
 				printHelp();
 			} else {
 				System.out.println("Type in help for program usage.");
 			}
 			return;
 		}
 		runTest(args);
 		System.out.println("*** Program terminated ***");
 	}
 	
 	private static void printHelp() {
 		System.out.println("Help menu for Hopfield Test program:");
 		System.out.println("Argu 1: Net Type to use: [synch | asynch | paritybit | pseudoinverse ]");
 		System.out.println("Argu 2: Test type [0 | 1 | 2 | 3 | 4]");
 		System.out.println("Argu 3: Image to recognize [ fileName ] - Without extension");
 		System.out.println("Argu 4: Images to memorize [ fileNameList ] - Without extension");
 	}
 
 	private static String[] getDefaultArgs() {
 		String[] patternNames = {"line1", "line2", "line3", "line4"};
 		String[] args = new String[3 + patternNames.length];
 		args[INDEX_NET_TYPE] = "paritybit";
 		args[INDEX_TEST_TYPE] = "3";
 		args[INDEX_TO_RECOGNIZE] = "line1";
 		System.arraycopy(patternNames, 0, args, INDEX_PATTERNS, patternNames.length);
 		return args;
 	}
 	
 	private static void runTest(String[] args) {
 		HopfieldNetTest test = new HopfieldNetTest(getHopfieldNet(args[INDEX_NET_TYPE]));
 		int testNumber;
 		try {
 			testNumber = Integer.parseInt(args[INDEX_TEST_TYPE]);
 		} catch (NumberFormatException e) {
 			System.out.println("Second argument must indicate test number to run: " 
 				+ args[INDEX_TEST_TYPE] + " is invalid.");
 			return;
 		}
 		test.setRecognizeName(args[INDEX_TO_RECOGNIZE]);
		test.setPatternNames(Arrays.copyOfRange(args, INDEX_PATTERNS, args.length - INDEX_PATTERNS));
 		test.execTest(testNumber);
 	}
 
 	private static HopfieldNet getHopfieldNet(String type) {
 		Map<String, HopfieldNet> netTypes = new HashMap<String, HopfieldNet>();
 		netTypes.put("synch", new SynchHopfieldNet());
 		netTypes.put("asynch", new AsynchHopfieldNet());
 		netTypes.put("paritybit", new AsynchHopfieldNetWithParityBit());
 		netTypes.put("pseudoinverse", new PseudoInverseHopfield());
 		if (!netTypes.containsKey(type)) {
 			throw new IllegalArgumentException("Unknown Net type: " + type);
 		}
 		return netTypes.get(type.toLowerCase());
 	}
 }
