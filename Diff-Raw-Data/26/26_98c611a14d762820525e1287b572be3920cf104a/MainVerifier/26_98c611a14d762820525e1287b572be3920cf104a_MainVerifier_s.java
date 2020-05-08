 package algorithms.verifiers;
 
 import main.Logger;
 import algorithms.params.Parameters;
 import algorithms.params.Parameters.Type;
 import arithmetic.objects.ByteTree;
 import arithmetic.objects.ElementsExtractor;
 import arithmetic.objects.LargeInteger;
 import arithmetic.objects.arrays.ArrayGenerators;
 import arithmetic.objects.arrays.ArrayOfElements;
 import arithmetic.objects.basicelements.BigIntLeaf;
 import arithmetic.objects.basicelements.Node;
 import arithmetic.objects.basicelements.StringLeaf;
 import arithmetic.objects.groups.IGroupElement;
 import arithmetic.objects.groups.ProductGroupElement;
 import arithmetic.objects.ring.IntegerRingElement;
 import arithmetic.objects.ring.Ring;
 import cryptographic.primitives.HashFuncPRG;
 import cryptographic.primitives.HashFuncPRGRandomOracle;
 import cryptographic.primitives.HashFunction;
 import cryptographic.primitives.SHA2HashFunction;
 
 /**
  * This class describes the required behavior expected from the main verifier.
  * 
  * @author Sofi
  */
 public class MainVerifier {
 
 	private static final String PUBLIC_KEY = "PublicKey";
 	private static final String GENERATORS = "generators";
 	private static final String EMPTY_STRING = "";
 	private static final String SECRET_KEY = "SecretKey";
 	private static final String PROOFS = "proofs";
 	private static final String BT_EXT = ".bt";
 	private static final String FULL_PUBLIC_KEY_BT = "FullPublicKey" + BT_EXT;
 	private static final String CIPHERTEXTS_FILE_NAME = "Ciphertexts";
 	private static final String SHUFFCT_FILE_PATH = "ShuffledCiphertexts"
 			+ BT_EXT;
 	private static final String PLAIN_TEXTS_FILE_PATH = "Plaintexts" + BT_EXT;
 
 	private Parameters params;
 	private HashFunction H;
 	private Logger logger;
 
 	/**
 	 * Constructor for tests
 	 * 
 	 * @param params
 	 * @param H
 	 */
 	MainVerifier(Parameters params, HashFunction H) {
 		this.params = params;
 		this.H = H;
 	}
 
 	/**
 	 * Empty constructor for the cmd line
 	 */
 	public MainVerifier(Logger logger) {
 		this.logger = logger;
 	}
 
 	/**
 	 * @return true if verification was successful and false otherwise.
 	 * @throws Exception
 	 */
 	public boolean verify(String protInfo, String directory, Type type,
 			String auxid, int w, boolean posc, boolean ccpos, boolean dec) {
 
 		logger.sendLog("Start verifing prove of " + type + " with:",
 				Logger.Severity.NORMAL);
 		logger.sendLog("	Proof of shuffling commitment: " + getOnOrOff(posc),
 				Logger.Severity.NORMAL);
 		logger.sendLog("	Commitment consistent proof of shuffling: "
 				+ getOnOrOff(ccpos), Logger.Severity.NORMAL);
 		logger.sendLog("	Proof of correct dycreption: " + getOnOrOff(dec),
 				Logger.Severity.NORMAL);
 
 		// *****Section 1 and 2 in the algorithm*****
 		// create the Parameters object using the command line parameters and
 		// fill parameters from xml and directory
 		logger.sendLog("Getting parameters from xml and directory",
 				Logger.Severity.NORMAL);
 		params = new Parameters(protInfo, directory, type, auxid, w, posc,
 				ccpos, dec, logger);
 		if (!fillParamsFromXmlAndDir(params) || !checkFilledParams()) {
 			return false;
 		}
 
 		// *******Section 3 in the Algorithm*********
 		// derive the objects: IGroup Gq, Ring Zq, Hashfunction H, and PRG.
 		if (!deriveSetsAndObjects()) {
 			return false;
 		}
 
 		// *******Section 4 in the Algorithm*********
 		// create the random oracles used in the provers using prg and
 		// hash function
 		createPrefixToRo();
 
 		// *******Section 5 in the Algorithm*********
 		if (!ReadKeys()) {
 			return false;
 		}
 		logger.sendLog("Verification of keys succeeded.",
 				Logger.Severity.NORMAL);
 
 		// *******Section 6 in the Algorithm*********
 		if (!ReadLists()) {
 			return false;
 
 		}
 
 		// create random array to send to the verifiers
 		createRandomArray();
 
 		// *******Section 7 in the Algorithm*********
 		// call the subroutines of the verifier
 		// 7a - Verify Shuffling
 		if ((Type.MIXING.equals(params.getType()) || Type.SHUFFLING
 				.equals(params.getType()))
 				&& (params.isPosc() || params.isCcpos())) {
 
 			if (!runVerShuffling()) {
 				return false;
 			}
 		}
 
 		// 7b - Verify Decryption
 		// if isDec==true means we need to check the decryption.
 		if (params.isDec()) {
 			if (Type.MIXING.equals(params.getType())) {
 				if (!runVerDec(Type.MIXING)) {
 					return false;
 				}
 			}
 			if (Type.DECRYPTION.equals(params.getType())) {
 				if (!runVerDec(Type.DECRYPTION)) {
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 
 	private String getOnOrOff(boolean b) {
 		return (b ? "ON" : "OFF");
 	}
 
 	private void createRandomArray() {
 		StringLeaf stringLeaf = new StringLeaf(GENERATORS);
 		byte[] independentSeed = params.getROseed().getRandomOracleOutput(
 				ArrayGenerators.concatArrays(params.getPrefixToRO(),
 						stringLeaf.toByteArray()));
 		ArrayOfElements<IGroupElement> h = params.getGq()
 				.createRandomArray(params.getN(), params.getPrg(),
 						independentSeed, params.getNr());
 		params.setRandArray(h);
 	}
 
 	// expect type to be MIXING or DECRYPTION
 	private boolean runVerDec(Type type) {
 		ArrayOfElements<ProductGroupElement> l;
 		if (Type.MIXING.equals(type)) {
 			l = params.getArrOfCiphertexts().getAt(params.getThreshold());
 		} else {
 			l = params.getArrOfCiphertexts().getAt(0);
 		}
 
 		return VerDec.verify(params.getROseed(), params.getROchallenge(),
 				params.getDirectory(), params.getPrefixToRO(),
 				params.getThreshold(), params.getN(), params.getNe(),
 				params.getNr(), params.getNv(), params.getPrg(),
 				params.getGq(), params.getFullPublicKey(), l,
 				params.getPlaintexts(), params.getZq(),
 				params.getMixPublicKey(), params.getMixSecretKey(),
 				params.getW(), params.getRandArray(), logger);
 	}
 
 	private boolean runVerShuffling() {
 		return VerShuffling.verify(params.getROseed(), params.getROchallenge(),
 				params.getDirectory(), params.getPrefixToRO(),
 				params.getThreshold(), params.getN(), params.getNe(),
 				params.getNr(), params.getNe(), params.getPrg(),
 				params.getGq(),
 				params.getFullPublicKey(),
 				// params.getCiphertexts(), params.getShuffledCiphertexts(),
 				params.getArrOfCiphertexts(), params.isPosc(),
 				params.isCcpos(), params.getZq(), params.getW(),
 				params.getRandArray(), logger);
 	}
 
 	private boolean checkFilledParams() {
 		if (!params.getProtVersion().equals(params.getVersion())) {
 			return false;
 		}
 
 		if (!(params.getType().equals(params.getTypeExpected()))) {
 			return false;
 		}
 
 		if (!params.getAuxsid().equals(params.getAuxidExp())) {
 			return false;
 		}
 
 		if ((params.getWidthExp() == 0)
 				&& (params.getW() != params.getwDefault())) {
 			return false;
 		}
 
 		if ((params.getWidthExp() != 0)
 				&& (params.getW() != params.getWidthExp())) {
 			return false;
 		}
 
 		return true;
 	}
 
 	private boolean fillParamsFromXmlAndDir(Parameters params) {
 		// fill the parameters from the xml, also checks that the file is valid
 		if (!params.fillFromXML()) {
 			return false;
 		}
 
 		// *****Section 2 in the algorithm*****
 		// fill parameters from proof directory
 		if (!params.fillFromDirectory()) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * @return true if successfully derived Gq, Zq, Mw, Cw, Rw, the hash
 	 *         function H and the PRG and false otherwise.
 	 */
 	public boolean deriveSetsAndObjects() {
 		// unmarshall Gq
 		params.setGq(ElementsExtractor.unmarshal(params.getsGq()));
 		if (params.getGq() == null) {
 			logger.sendLog(
 					"Name of Java class is not recognized by the system. Failed to derive Gq.",
 					Logger.Severity.ERROR);
 			return false;
 		}
 
 		// create the Ring
		params.setZq(new Ring(params.getGq().getFieldOrder()));
 
 		// set the Hashfunction and the pseudo random generator
 		H = new SHA2HashFunction(params.getSh()); // TODO Daniel - try and catch
 		params.setPrg(new HashFuncPRG(new SHA2HashFunction(params.getsPRG())));
 
 		return true;
 	}
 
 	public void createPrefixToRo() {
 		ByteTree btAuxid = new StringLeaf(params.getSessionID() + "."
 				+ params.getAuxsid());
 		ByteTree version_proof = new StringLeaf(params.getVersion());
 		ByteTree sGq = new StringLeaf(params.getsGq());
 		ByteTree sPRG = new StringLeaf(params.getsPRG());
 		ByteTree sH = new StringLeaf(params.getSh());
 		ByteTree Ne = new BigIntLeaf(new LargeInteger(Integer.toString(params
 				.getNe())), 4);
 		ByteTree Nr = new BigIntLeaf(new LargeInteger(Integer.toString(params
 				.getNr())), 4);
 		ByteTree Nv = new BigIntLeaf(new LargeInteger(Integer.toString(params
 				.getNv())), 4);
 
 		ByteTree[] input = new ByteTree[8];
 
 		input[0] = version_proof;
 		input[1] = btAuxid;
 		input[2] = Nr;
 		input[3] = Nv;
 		input[4] = Ne;
 		input[5] = sPRG;
 		input[6] = sGq;
 		input[7] = sH;
 
 		byte[] Seed = new Node(input).toByteArray();
 		params.setPrefixToRO(H.digest(Seed));
 
 		// set random oracles:
 		params.setROseed(new HashFuncPRGRandomOracle(H, params.getPrg()
 				.seedlen()));
 		params.setROchallenge(new HashFuncPRGRandomOracle(H, params.getNv()));
 
 	}
 
 	public boolean ReadKeys() {
 		// read Public Key
 		ProductGroupElement pk;
 
 		byte[] bpk = ElementsExtractor.btFromFile(params.getDirectory(),
 				FULL_PUBLIC_KEY_BT);
 		if (bpk == null) {
 			logger.sendLog("Problem while reading FullPublicKey.bt",
 					Logger.Severity.ERROR);
 			return false;
 		}
 
 		pk = ElementsExtractor.createSimplePGE(bpk, params.getGq(), 2);
 
 		// extract y and g from the public key
 		IGroupElement y = pk.getElements().getAt(1);
 		IGroupElement g = pk.getElements().getAt(0);
 
 		params.setFullPublicKey(pk);
 
 		IGroupElement yi;
 		IntegerRingElement xi;
 		int i;
 
 		// get the Identity element and multiply all of the yi's
 		IGroupElement res = params.getGq().one();
 
 		for (i = 0; i < params.getThreshold(); i++) {
 			// assume that the file exists
 			byte[] byi = ElementsExtractor.btFromFile(params.getDirectory(),
 					PROOFS, PUBLIC_KEY + (i < 10 ? "0" : EMPTY_STRING)
 							+ (i + 1) + BT_EXT);
 			if (byi == null) {
 				logger.sendLog("Problem while reading " + PUBLIC_KEY
 						+ (i < 10 ? "0" : EMPTY_STRING) + (i + 1) + BT_EXT,
 						Logger.Severity.ERROR);
 				return false;
 			}
 
 			yi = ElementsExtractor.createGroupElement(byi, params.getGq());
 
 			params.getMixPublicKey().add(yi);
 			res = res.mult(yi);
 		}
 
 		if (!res.equals(y)) {
 			logger.sendLog(
 					"Public keys of mix servers are not consistent with the full public key.",
 					Logger.Severity.ERROR);
 			return false;
 		}
 
 		// check the secret keys - xi: the file can be null
 		for (i = 0; i < params.getThreshold(); i++) {
 			byte[] xFile = ElementsExtractor.btFromFile(params.getDirectory(),
 					PROOFS, SECRET_KEY + (i < 10 ? "0" : EMPTY_STRING)
 							+ (i + 1) + BT_EXT);
 
 			// xi = null if the file doesn't exist
 			xi = (xFile == null) ? null : new IntegerRingElement(
 					ElementsExtractor.leafToInt(xFile), params.getZq());
 			params.getMixSecretKey().add(xi);
 		}
 
 		// check the 3.b part of keys verifier
 		for (i = 0; i < params.getThreshold(); i++) {
 			xi = params.getMixSecretKey().getAt(i);
 			yi = params.getMixPublicKey().getAt(i);
 
 			if ((xi != null) && !(yi.equals(g.power(xi.getElement())))) {
 				logger.sendLog("Verification of keys failed.",
 						Logger.Severity.ERROR);
 				return false;
 			}
 		}
 		return true;
 	}
 
 	// Part 6 of the algorithm
 	public boolean ReadLists() {
 		byte[] file;
 
 		if (!readArrayOfCiphertexts())
 			return false;
 
 		// Section 6c of the algorithm
 		if (params.getType().equals(Type.MIXING)
 				|| params.getType().equals(Type.DECRYPTION)) {
 			file = ElementsExtractor.btFromFile(params.getDirectory(),
 					PLAIN_TEXTS_FILE_PATH);
 			if (file == null) {
 				logger.sendLog("Plaintexts file not found.",
 						Logger.Severity.ERROR);
 				return false;
 			}
 
 			ArrayOfElements<ProductGroupElement> plaintexts = ArrayGenerators
 					.createArrayOfPlaintexts(file, params.getGq(),
 							params.getW());
 			if (plaintexts.getSize() != params.getN()) {
 				logger.sendLog("Plaintexts array is in the wrong size.",
 						Logger.Severity.ERROR);
 				return false;
 			}
 			params.setPlaintexts(plaintexts);
 		}
 		return true;
 	}
 
 	private boolean readArrayOfCiphertexts() {
 
 		ArrayOfElements<ArrayOfElements<ProductGroupElement>> Arr = new ArrayOfElements<ArrayOfElements<ProductGroupElement>>();
 
 		// section 6a of the Algorithm
 		byte[] file = ElementsExtractor.btFromFile(params.getDirectory(),
 				CIPHERTEXTS_FILE_NAME + BT_EXT);
 		if (file == null) {
 			logger.sendLog("Ciphertexts file not found.", Logger.Severity.ERROR);
 			return false;
 		}
 
 		ArrayOfElements<ProductGroupElement> ciphertexts = ArrayGenerators
 				.createArrayOfCiphertexts(file, params.getGq(), params.getW());
 
 		// Puts the ciphertexts at Arr[0]
 		Arr.add(ciphertexts);
 		// params.setCiphertexts(ciphertexts);
 		params.setN(ciphertexts.getSize());
 
 		ArrayOfElements<ProductGroupElement> Li;
 
 		for (int i = 1; i < params.getThreshold(); i++) {
 
 			byte[] bLi = ElementsExtractor.btFromFile(params.getDirectory(),
 					PROOFS, CIPHERTEXTS_FILE_NAME + getNumStringForFileName(i)
 							+ BT_EXT);
 			Li = null;
 			if (bLi == null) {
 				if (!(params.getType().equals(Type.DECRYPTION))) {
 					logger.sendLog("Ciphertexts file not found.",
 							Logger.Severity.ERROR);
 					return false;
 				}
 			}
 
 			if (!params.getType().equals(Type.DECRYPTION)) {
 				Li = ArrayGenerators.createArrayOfCiphertexts(bLi,
 						params.getGq(), params.getW());
 				if (Li.getSize() != params.getN()) {
 					return false;
 				}
 			}
 
 			// put the ciphertexts at Arr[i]
 			Arr.add(Li);
 
 		}
 
 		// section 6b of the Algorithm - read shuffled ciphertexts
 		// if the type == mixing, read the file Ciphertexts_threshold from
 		// Directory/proofs
 		// if the type==shuffling, read the file ShuffledCiphertexts.bt from
 		// Directory
 
 		if (params.getType().equals(Type.MIXING)) {
 			file = ElementsExtractor.btFromFile(params.getDirectory(), PROOFS,
 					CIPHERTEXTS_FILE_NAME
 							+ (params.getThreshold() < 10 ? "0" : EMPTY_STRING)
 							+ params.getThreshold() + BT_EXT);
 			if (file == null) {
 				logger.sendLog("Shuffled ciphertexts file not found.",
 						Logger.Severity.ERROR);
 				return false;
 			}
 		}
 
 		if (params.getType().equals(Type.SHUFFLING)) {
 			file = ElementsExtractor.btFromFile(params.getDirectory(),
 					SHUFFCT_FILE_PATH);
 			if (file == null) {
 				logger.sendLog("Shuffled ciphertexts file not found.",
 						Logger.Severity.ERROR);
 				return false;
 			}
 		}
 
 		ArrayOfElements<ProductGroupElement> ShuffledCiphertexts = ArrayGenerators
 				.createArrayOfCiphertexts(file, params.getGq(), params.getW());
 		if (ShuffledCiphertexts.getSize() != params.getN()) {
 			logger.sendLog("Shuffled ciphertexts array is in the wrong size.",
 					Logger.Severity.ERROR);
 			return false;
 		}
 
 		// Add L_lambda at Arr[lambda]
 		Arr.add(ShuffledCiphertexts);
 		params.setArrOfCiphertexts(Arr);
 
 		// TODO pintouts
 		/*System.out.println("b(L0)"
 				+ bytArrayToHex(params.getArrOfCiphertexts().getAt(0)
 						.toByteArray()));
 		System.out.println("b(L2)"
 				+ bytArrayToHex(params.getArrOfCiphertexts().getAt(2)
 						.toByteArray()));*/
 
 		return true;
 
 	}
 
 	/**
 	 * Getter for the params field.
 	 * 
 	 * @return the parameters
 	 */
 	public Parameters getParams() {
 		return params;
 	}
 
 	// TODO printout method - delete?
 	String bytArrayToHex(byte[] a) {
 		StringBuilder sb = new StringBuilder();
 		for (byte b : a)
 			sb.append(String.format("%02x", b & 0xff));
 		return sb.toString();
 	}
 
 	/**
 	 * @param i
 	 *            the number to change
 	 * @return the number as string, if i < 10, add "0" to the begining of the
 	 *         string
 	 */
 	private static String getNumStringForFileName(int i) {
 		return (i < 10 ? "0" : EMPTY_STRING) + i;
 	}
 
 }
