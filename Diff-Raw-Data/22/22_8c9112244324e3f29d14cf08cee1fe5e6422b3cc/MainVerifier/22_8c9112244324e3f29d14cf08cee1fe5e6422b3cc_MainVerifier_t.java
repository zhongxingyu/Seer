 package algorithms.verifiers;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 
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
 import arithmetic.objects.ring.IRing;
 import arithmetic.objects.ring.IntegerRingElement;
 import arithmetic.objects.ring.ProductRingElement;
 import arithmetic.objects.ring.Ring;
 import cryptographic.primitives.HashFuncPRG;
 import cryptographic.primitives.HashFuncPRGRandomOracle;
 import cryptographic.primitives.HashFunction;
 import cryptographic.primitives.RandomOracle;
 import cryptographic.primitives.SHA2HashFunction;
 
 /**
  * This class describes the required behavior expected from the main verifier.
  * 
  * @author Tagel & Sofi
  */
 public class MainVerifier {
 
 	private Parameters params;
 	public Parameters getParams() {
 		return params;
 	}
 
 	private HashFunction H;
 	private static final String ciphertextsFilePath = "Ciphertexts.bt";
	private static final String partialCipherFilePath = "Ciphertexts";
 	private static final String shuffCTFilePath = "ShuffledCiphertexts.bt";
 	private static final String plaintextsFilePath = "Plaintexts.bt";
 	
 	
 	/**
 	 * Constructor for tests
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
 	public MainVerifier() {
 		
 	}
 	
 	/**
 	 * @return true if verification was successful and false otherwise.
 	 * @throws Exception 
 	 */
 	public boolean verify(String protInfo, String directory, Type type,
 			String auxid, int w, boolean posc, boolean ccpos, boolean dec)
 			throws Exception {
 
 		// *****Section 1 in the algorithm*****
 		// First create the Parameters object using the
 		// command line parameters
 		params = new Parameters(protInfo, directory, type, auxid, w, posc,
 				ccpos, dec);
 
 		// create the Xml protInfo file and check it
 		// fill the relevant parameters:
 		// versionprot, sid, k, thresh, ne, nr, nv, sH, sPRG, sGq , and wdefault
 		// fillFromXML checks if the file is valid, rejects if not
 		if (!params.fillFromXML()) {
 			return false;
 		}
 
 		// *****Section 2 in the algorithm*****
 		// fill version_proof(Version) type, auxid, w from proof directory
 		// if this fails, return false.
 		if (!params.fillFromDirectory()) {
 			return false;
 		}
 
 		// params.getVersion() = version_proof in the document
 		if (!params.getProtVersion().equals(params.getVersion()))
 			return false;
 
 		if (!(params.getType().equals(params.getTypeExpected())))
 			return false;
 
 		if (!params.getAuxsid().equals(params.getAuxidExp()))
 			return false;
 
 		// The document says that widthExp should be NULL, but here we will only
 		// assign 0
 		// Document: Code:
 		// w_expected w_expected
 		// w w
 		// w_deafult wDeafult
 		if ((params.getWidthExp() == 0)
 				&& (params.getW() != params.getwDefault()))
 			return false;
 
 		if ((params.getWidthExp() != 0)
 				&& (params.getW() != params.getWidthExp()))
 			return false;
 
 		// *******Section 3 in the Algorithm*********
 		// Derives the relevant objects: IGroup Gq, Ring Zq, Hashfunction H, and
 		// PRG.
 		if (!deriveSetsAndObjects())
 			return false;
 
 		// *******Section 4 in the Algorithm*********
 		// Also creates the random oracles used in the provers using prg and
 		// hashfunction
 		createPrefixToRo();
 
 		// *******Section 5 in the Algorithm*********
 		if (!ReadKeys())// Attempt to read was unsuccessful
 			return false;
 
 		// *******Section 6 in the Algorithm*********
 		if (!ReadLists())
 			return false;
 
 		// *******Section 7 in the Algorithm*********
 		// Here we call the subroutines of the verifier
 		// 7a -- Verify Shuffling
 		if ((params.getType().equals(Type.MIXING) || params.getType().equals(
 				Type.SHUFFLING))
 				&& (params.isPosc() || params.isCcpos()))
 
 			if (!VerShuffling.verify(params.getROseed(),
 					params.getROchallenge(), params.getDirectory(),
 					params.getPrefixToRO(), params.getThreshold(),
 					params.getN(), params.getNe(), params.getNr(),
 					params.getNe(), params.getPrg(), params.getGq(),
 					params.getFullPublicKey(), params.getCiphertexts(),
 					params.getShuffledCiphertexts(), params.isPosc(),
 					params.isCcpos(), params.getZq(), params.getW()))
 				return false;
 
 		// 7b - Verify Decryption
 		if (params.isDec()) {// isDec==true means we need to check the
 								// decryption.
 			if (params.getType().equals(Type.MIXING))
 				if (!VerDec.verify(params.getROseed(), params.getROchallenge(),
 						params.getDirectory(), params.getPrefixToRO(),
 						params.getThreshold(), params.getN(), params.getNe(),
 						params.getNr(), params.getNv(), params.getPrg(),
 						params.getGq(), params.getFullPublicKey(),
 						params.getShuffledCiphertexts(),
 						params.getPlaintexts(), params.getZq(),
 						params.getMixPublicKey(), params.getMixSecretKey(), params.getW()))
 					return false;
 
 			if (params.getType().equals(Type.DECRYPTION))
 				if (!VerDec.verify(params.getROseed(), params.getROchallenge(),
 						params.getDirectory(), params.getPrefixToRO(),
 						params.getThreshold(), params.getN(), params.getNe(),
 						params.getNr(), params.getNv(), params.getPrg(),
 						params.getGq(), params.getFullPublicKey(),
 						params.getCiphertexts(), params.getPlaintexts(),
 						params.getZq(), params.getMixPublicKey(),
 						params.getMixSecretKey(), params.getW()))
 					return false;
 		}
 
 		return true;
 	}
 
 	/**
 	 * @return true if successfully derived Gq, Zq, Mw, Cw, Rw, the hash
 	 *         function H and the PRG and false otherwise.
 	 */
 	public boolean deriveSetsAndObjects() {
 		// Unmarshall Gq
 		try {
 			params.setGq(ElementsExtractor.unmarshal(params.getsGq()));
 		} catch (UnsupportedEncodingException e) {
 			System.out.println("Error unmarshalling Gq");
 			return false;
 		}
 
 		// create the Ring
 		IRing<IntegerRingElement> temp = new Ring(params.getGq()
 				.getFieldOrder());
 		params.setZq(temp);
 
 		// Set the Hashfunction and the pseudo random generator
 		H = new SHA2HashFunction(params.getSh());
 		params.setPrg(new HashFuncPRG(new SHA2HashFunction(params.getsPRG())));
 
 		return true;
 	}
 
 	public void createPrefixToRo() throws UnsupportedEncodingException {
 		String s = params.getSessionID() + "." + params.getAuxsid();
 		ByteTree btAuxid = new StringLeaf(s);
 		ByteTree version_proof = new StringLeaf(params.getVersion());
 		ByteTree sGq = new StringLeaf(params.getsGq());
 		ByteTree sPRG = new StringLeaf(params.getsPRG());
 		ByteTree sH = new StringLeaf(params.getSh());
 
 		ByteTree Ne = new BigIntLeaf(new LargeInteger(Integer.toString(params
 				.getNe())));
 		ByteTree Nr = new BigIntLeaf(new LargeInteger(Integer.toString(params
 				.getNr())));
 		ByteTree Nv = new BigIntLeaf(new LargeInteger(Integer.toString(params
 				.getNv())));
 		ByteTree btW = new BigIntLeaf(new LargeInteger(Integer.toString(params
 				.getW())));
 
 		ByteTree[] input = new ByteTree[9];
 		input[0] = version_proof;
 		input[1] = btAuxid;
 		input[2] = btW;
 		input[3] = Ne;
 		input[4] = Nr;
 		input[5] = Nv;
 		input[6] = sGq;
 		input[7] = sPRG;
 		input[8] = sH;
 
 		Node node = new Node(input);
 		byte[] Seed = node.toByteArray();
 
 		params.setPrefixToRO(H.digest((Seed)));
 
 		// Set random oracles:
 		RandomOracle ROseed = new HashFuncPRGRandomOracle(H, params.getPrg()
 				.seedlen());
 		RandomOracle ROchallenge = new HashFuncPRGRandomOracle(H,
 				params.getNv());
 
 		params.setROseed(ROseed);
 		params.setROchallenge(ROchallenge);
 
 	}
 
 	public boolean ReadKeys() {
 		// Read Public Key
 		ProductGroupElement pk;
 		try {
 			pk = ElementsExtractor.createSimplePGE(ElementsExtractor
 					.btFromFile(params.getDirectory(), "FullPublicKey.bt"),
 					params.getGq());
 		} catch (UnsupportedEncodingException e) {
 			return false;
 		} catch (IOException e) {
 			return false;
 		}
 
 		// Extract y and g from the public key
 		IGroupElement y = pk.getElements().getAt(1);
 		IGroupElement g = pk.getElements().getAt(0);
 
 		params.setFullPublicKey(pk);
 
 		// Initialize the elements
 		IGroupElement yi;
 		IntegerRingElement xi;
 		int i;
 
 		// Here we get the Identity element and multiply all of the yi's
 		IGroupElement res = params.getGq().one();
 
 		for (i = 0; i < params.getThreshold(); i++) {
 			// Here we assume that the file exists
 			try {
 				yi = ElementsExtractor.createGroupElement(ElementsExtractor
 						.btFromFile(params.getDirectory(), "proofs",
 								"PublicKey" + (i < 10 ? "0" : "") + (i + 1)
 										+ ".bt"), params.getGq());
 			} catch (IOException e) {
 				return false;
 			}
 			// Check if yi exists
 			if (yi == null)
 				return false;
 
 			params.getMixPublicKey().add(yi);
 			res = res.mult(yi);
 		}
 
 		if (!res.equal(y))
 			return false;
 
 		// Here we check the secret keys - xi:
 		// The file can be null
 		for (i = 0; i < params.getThreshold(); i++) {
 
 			byte[] xFile;
 			try {
 				xFile = ElementsExtractor.btFromFile(params.getDirectory(),
 						"proofs", "SecretKey" + (i < 10 ? "0" : "") + (i + 1)
 								+ ".bt");
 			} catch (IOException e) {
 				return false;
 			}
 
 			if (xFile == null)
 				xi = null;
 			else
 				xi = new IntegerRingElement(ElementsExtractor.leafToInt(xFile),
 						params.getZq());
 
 			// xi = null if the file doesn't exist
 			params.getMixSecretKey().add(xi);
 		}
 
 		// Check the 3.b part of keys verifier
 		for (i = 0; i < params.getThreshold(); i++) {
 			xi = params.getMixSecretKey().getAt(i);
 			yi = params.getMixPublicKey().getAt(i);
 
 			if ((xi != null) && !(yi.equal(g.power(xi.getElement()))))
 				return false;
 		}
 
 		return true;
 
 	}
 
 	// Part 6 of the algorithm
 	public boolean ReadLists() throws IOException {
 		// section 6a of the Algorithm
 		byte[] file = ElementsExtractor.btFromFile(params.getDirectory(),
 				ciphertextsFilePath);
 		if (file == null)
 			return false;
 
 		ArrayOfElements<ProductGroupElement> ciphertexts = ArrayGenerators
 				.createArrayOfCiphertexts(file, params.getGq(), params.getW());
 
 		params.setCiphertexts(ciphertexts);
 		params.setN(ciphertexts.getSize());
 
		// section 6b of the Algorithm -- read shuffled ciphertexts
		// if the type==mixing, read the file Ciphertexts_threshold from
		// Directory/proofs 
		// if the type==shuffling, read the file ShuffledCiphertexts.bt from Directory
 		
 		if (params.getType().equals(Type.MIXING)) {
			file = ElementsExtractor.btFromFile(params.getDirectory(),"proofs",
					partialCipherFilePath+(params.getThreshold() < 10 ? "0" : "")+".bt");
 			if (file == null)
 				return false;
 		} 
 		
 		if (params.getType().equals(Type.SHUFFLING)) {
 			file = ElementsExtractor.btFromFile(params.getDirectory(),
					shuffCTFilePath);
 			if (file == null)
 				return false;
 		}
 
 		ArrayOfElements<ProductGroupElement> ShuffledCiphertexts = ArrayGenerators
 				.createArrayOfCiphertexts(file, params.getGq(), params.getW());
 		if (ShuffledCiphertexts.getSize() != params.getN())
 			return false;
 
 		params.setShuffledCiphertexts(ShuffledCiphertexts);
 
 		// Section 6c of the algorithm
 		if (params.getType().equals(Type.MIXING)
 				|| params.getType().equals(Type.DECRYPTION)) {
 			file = ElementsExtractor.btFromFile(params.getDirectory(),
 					plaintextsFilePath);
 			if (file == null)
 				return false;
 
 			ArrayOfElements<ProductRingElement> plaintexts = ArrayGenerators
 					.createArrayOfPlaintexts(file, params.getZq());
 			params.setPlaintexts(plaintexts);
 		}
 
 		return true;
 	}
 
 }
