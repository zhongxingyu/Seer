 package algorithms.provers;
 
 import arithmetic.objects.ByteTree;
 import arithmetic.objects.LargeInteger;
 import arithmetic.objects.arrays.ArrayGenerators;
 import arithmetic.objects.arrays.ArrayOfElements;
 import arithmetic.objects.basicelements.BigIntLeaf;
 import arithmetic.objects.basicelements.Node;
 import arithmetic.objects.basicelements.StringLeaf;
 import arithmetic.objects.groups.IGroup;
 import arithmetic.objects.groups.IGroupElement;
 import arithmetic.objects.groups.ProductGroupElement;
 import arithmetic.objects.ring.IntegerRingElement;
 import arithmetic.objects.ring.ProductRingElement;
 import cryptographic.primitives.PseudoRandomGenerator;
 import cryptographic.primitives.RandomOracle;
 
 /**
  * This class provides the functionality of proving the correctness of
  * re-encryption and permutation of the input ciphertexts.
  * 
  * @author Tagel
  */
 public class ProveShuffling extends Prover {
 
 	
 	/**
 	 * This is the main function of this class which executes the shuffling
 	 * algorithm.
 	 * 
 	 * @param ROSeed
 	 *            RandomOracle for computing the seed
 	 * @param ROChallenge
 	 *            RandomOracle for computing the challenge
 	 * @param ro
 	 *            prefix to random oracle
 	 * @param N
 	 *            size of the arrays
 	 * @param Ne
 	 *            number of bits in each component
 	 * @param Nr
 	 *            Acceptable "statistical error" when deriving independent
 	 *            generators
 	 * @param Nv
 	 *            Number of bits in the challenge
 	 * @param prg
 	 *            Pseudo-random generator used to derive random vectors for
 	 *            batching
 	 * @param Gq
 	 *            The group
 	 * @param pk
 	 *            The public key
 	 * @param wInput
 	 *            Array of input ciphertexts
 	 * @param wOutput
 	 *            Array of output ciphertexts
 	 * @param width
 	 *            width of plaintexts and ciphertexts
 	 * @param permutationCommitment
 	 *            commitment to a permutation
 	 * @param PoSCommitment
 	 *            Proof commitment of the proof of a shuffle
 	 * @param PoSReply
 	 *            Proof reply of the proof of a shuffle
 	 * @return true if we accept the proof and false otherwise
 	 */
 
 	public static boolean prove(RandomOracle ROSeed, RandomOracle ROChallenge,
 			byte[] ro, int N, int Ne, int Nr, int Nv,
 			PseudoRandomGenerator prg, IGroup Gq, ProductGroupElement pk,
 			ArrayOfElements<ProductGroupElement> wInput,
 			ArrayOfElements<ProductGroupElement> wOutput, int width,
 			ArrayOfElements<IGroupElement> permutationCommitment,
 			Node PoSCommitment, Node PoSReply) {
 
 		try {
 
 			/**
 			 * 1(a) - interpret permutationCommitment (miu) as an array of
 			 * Pedersen commitments in Gq
 			 */
 			ArrayOfElements<IGroupElement> u = permutationCommitment;
 
 			/**
 			 * 1(b) - interpret Tpos as Node(B,A',B',C',D',F')
 			 */
 			
 			// creating B,A',B',C',D',F'
 			@SuppressWarnings("unchecked")
 			ArrayOfElements<IGroupElement> B = (ArrayOfElements<IGroupElement>) (PoSCommitment
 					.getAt(0));
 			@SuppressWarnings("unchecked")
 			ArrayOfElements<IGroupElement> Btag = (ArrayOfElements<IGroupElement>) (PoSCommitment
 					.getAt(2));
 
 			IGroupElement Atag = (IGroupElement) PoSCommitment.getAt(1);
 			IGroupElement Ctag = (IGroupElement) PoSCommitment.getAt(3);
 			IGroupElement Dtag = (IGroupElement) PoSCommitment.getAt(4);
 			ProductGroupElement Ftag = (ProductGroupElement) PoSCommitment
 					.getAt(5);
 
 			/**
 			 * 1(c) - interpret Opos as Node(Ka,Kb,Kc,Kd,Ke,Kf)
 			 */
 			IntegerRingElement Ka = (IntegerRingElement) PoSReply.getAt(0);
 			IntegerRingElement Kc = (IntegerRingElement) PoSReply.getAt(2);
 			IntegerRingElement Kd = (IntegerRingElement) PoSReply.getAt(3);
 			ProductRingElement Kf = (ProductRingElement) PoSReply.getAt(5);
 
 			@SuppressWarnings("unchecked")
 			ArrayOfElements<IntegerRingElement> Kb = (ArrayOfElements<IntegerRingElement>) (PoSReply
 					.getAt(1));
 
 			@SuppressWarnings("unchecked")
 			ArrayOfElements<IntegerRingElement> Ke = (ArrayOfElements<IntegerRingElement>) (PoSReply
 					.getAt(4));
 
 			/**
 			 * 2 - computing the seed
 			 */
 			StringLeaf stringLeaf = new StringLeaf(GENERATORS);
 			byte[] independentSeed = ROSeed
 					.getRandomOracleOutput(ArrayGenerators.concatArrays(ro,
 							stringLeaf.toByteArray()));
 			ArrayOfElements<IGroupElement> h = Gq.createRandomArray(N, prg,
 					independentSeed, Nr);
 
 			IGroupElement g = Gq.getGenerator();
 
 			ByteTree[] input = new ByteTree[6];
 			input[0] = g;
 			input[1] = h;
 			input[2] = u;
 			input[3] = pk;
 			input[4] = wInput;
 			input[5] = wOutput;
 			Node nodeForSeed = new Node(input);
 			byte[] seed = ComputeSeed(ROSeed, nodeForSeed, ro);
 
 			/**
 			 * 3 - Computation of A and F
 			 */
 			IGroupElement A = computeA(N, Ne, seed, prg, u, Gq);
 			ProductGroupElement F = computeF(N, Ne, seed, prg, wInput);
 
 			/**
 			 * 4 - Computation of the challenge
 			 */
 			ByteTree leaf = new BigIntLeaf(new LargeInteger(seed));
 
 			ByteTree[] inputChallenge = new ByteTree[2];
 			inputChallenge[0] = leaf;
 			inputChallenge[1] = PoSCommitment;
 			Node nodeForChallenge = new Node(inputChallenge);
 
 			byte[] challenge = ROChallenge
 					.getRandomOracleOutput(ArrayGenerators.concatArrays(ro,
 							nodeForChallenge.toByteArray()));
 
 			/* Computation of v: */
 			LargeInteger v = new LargeInteger(challenge);
 			LargeInteger twoNv = (new LargeInteger("2")).power(Nv);
 			v = v.mod(twoNv);
 
 			/**
 			 * 5 - Compute C,D and verify equalities
 			 */
 			LargeInteger E = computeE(N, Ne, seed, prg);
 			IGroupElement C = computeC(u, h, N);
 			IGroupElement D = computeD(E, B, h, N);
 			// TODO: B-1 = ho - WTF??
 
 			/*
 			 * Equation 1: A^v * Atag = (g^ka) * PI(h[i]^ke[i])
 			 */
 			if (!verifyAvAtag(A, Atag, v, Ke, g, N, h, Ka)) {
 				return false;
 			}
 
 			/*
 			 * Equation 2: (B[i]^v) * Btag[i] = (g^Kb[i]) * (B[i-1]^Ke[i]),
 			 * where B[-1] = h[0]
 			 */
 			if (!verifyBvBtag(B, Btag, Kb, Ke, g, v, h, N)) {
 				return false;
 			}
 
 			/*
 			 * Equation 3: F^v*Ftag = Enc(1,-Kf) * PI(wOutput[i]^Ke[i])
 			 */
 			ProductGroupElement leftF = F.power(v).mult(Ftag);
 
 			ProductGroupElement W = wOutput.getAt(0).power(
 					Ke.getAt(0).getElement());
 			for (int i = 1; i < N; i++) {
 				W = W.mult(wOutput.getAt(i).power(Ke.getAt(i).getElement()));
 			}
 
 			// create ProductGroupElement of 1s
 			ArrayOfElements<IGroupElement> arrOfOnes = new ArrayOfElements<IGroupElement>();
 			for (int i = 0; i < width; i++) {
 				arrOfOnes.add(Gq.one());
 			}
 
 			ProductGroupElement ones = new ProductGroupElement(arrOfOnes);
 			ProductGroupElement rigthF = encrypt(ones, Kf, pk, Gq);
 			if (!leftF.equals(rigthF)) {
 				return false;
 			}
 
 			/*
 			 * Equation 4: (C^v)*Ctag = g^Kc
 			 */
 			if (!verifyCvCtag(C, Ctag, v, Kc, g)) {
 				return false;
 			}
 
 			/*
 			 * Equation 5: (D^v)*Dtag = g^Kd
 			 */
 			if (!verifyDvDtag(D, Dtag, v, Kd, g)) {
 				return false;
 			}
 
 			/* All equalities exist. */
 			return true;
 
 		} catch (Exception e) {
 			System.err.println(e.getMessage());
 			return false;
 		}
 	}
 }
