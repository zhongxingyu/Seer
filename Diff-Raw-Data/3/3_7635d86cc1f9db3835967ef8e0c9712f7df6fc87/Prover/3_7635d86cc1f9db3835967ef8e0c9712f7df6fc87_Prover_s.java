 package algorithms.provers;
 
 import arithmetic.objects.ByteTree;
 import arithmetic.objects.LargeInteger;
 import arithmetic.objects.arrays.ArrayGenerators;
 import arithmetic.objects.arrays.ArrayOfElements;
 import arithmetic.objects.basicelements.Node;
 import arithmetic.objects.groups.IGroup;
 import arithmetic.objects.groups.IGroupElement;
 import arithmetic.objects.groups.ProductGroupElement;
 import arithmetic.objects.ring.IntegerRingElement;
 import arithmetic.objects.ring.ProductRingElement;
 import cryptographic.primitives.PseudoRandomGenerator;
 import cryptographic.primitives.RandomOracle;
 
 /**
  * This class is an abstract class which concludes the mutual code for all the
  * provers
  * 
  * @author tagel
  */
 public abstract class Prover {
 
 	/**
 	 * This function decrypts a given ciphertext back to its decryption factor
 	 * 
 	 * @param x
 	 *            Additive inverse of the exponent by which the standard
 	 *            generator in the public key is raised
 	 * @param A
 	 *            ciphertext to be decrypted
 	 * @return the decryption factor
 	 */
 	public static ProductGroupElement PDecrypt(IntegerRingElement x,
 			ProductGroupElement A) {
		// TODO return A.getLeft().power(x.neg().getElement());
		return null;
 	}
 
 	/**
 	 * This function decrypts a given array of ciphertexts back to their
 	 * decryption factors
 	 * 
 	 * @param x
 	 *            array of the additive inverse of the exponent by which the
 	 *            standard generator in the public key is raised
 	 * @param A
 	 *            array of the ciphertexts to be decrypted
 	 * @return array of decryption factors
 	 */
 	public static ArrayOfElements<ProductGroupElement> PDecrypt(
 			IntegerRingElement x, ArrayOfElements<ProductGroupElement> A) {
 
 		ArrayOfElements<ProductGroupElement> res = new ArrayOfElements<ProductGroupElement>();
 		for (int i = 0; i < A.getSize(); i++) {
 			res.add(PDecrypt(x, A.getAt(i)));
 		}
 		return res;
 	}
 
 	/**
 	 * This function does a trivial decryption
 	 * 
 	 * @param A
 	 *            ciphertext to be decrypted
 	 * @param f
 	 *            decrypion factor
 	 * @return m the plaintext
 	 */
 	public static ProductGroupElement TDecrypt(ProductGroupElement A,
 			ProductGroupElement f) {
 		return A.getRight().mult(f);
 	}
 
 	/**
 	 * This function does a trivial decryption
 	 * 
 	 * @param A
 	 *            array of ciphertexts to be decrypted
 	 * @param f
 	 *            array of decrypion factors
 	 * @return array of plaintexts
 	 */
 	public static ArrayOfElements<ProductGroupElement> TDecrypt(
 			ArrayOfElements<ProductGroupElement> L,
 			ArrayOfElements<ProductGroupElement> f) {
 
 		ArrayOfElements<ProductGroupElement> res = new ArrayOfElements<ProductGroupElement>();
 		for (int i = 0; i < L.getSize(); i++) {
 			res.add(TDecrypt(L.getAt(i), f.getAt(i)));
 		}
 		return res;
 	}
 
 	/**
 	 * This function encrypts the message m using the public key pk, and returns
 	 * the CipherText.
 	 * 
 	 * @param m
 	 *            plaintexts
 	 * @param pk
 	 *            Public key (g, y) used to encrypt the message
 	 * @param s
 	 *            random element in Zq
 	 * @param Gq
 	 *            the group
 	 * @param negative
 	 * @return ciphertext, encryption of the plaintext
 	 */
 	public static ProductGroupElement encrypt(ProductGroupElement m,
 			ProductRingElement s, ProductGroupElement pk, IGroup Gq,
 			boolean negative) {
 
 		IGroupElement g = pk.getElements().getAt(0);
 		IGroupElement y = pk.getElements().getAt(1);
 		ArrayOfElements<IntegerRingElement> powers = s.getElements();
 		ArrayOfElements<IGroupElement> ms = m.getElements();
 		ArrayOfElements<IGroupElement> left = new ArrayOfElements<IGroupElement>();
 		ArrayOfElements<IGroupElement> right = new ArrayOfElements<IGroupElement>();
 
 		for (int i = 0; i < powers.getSize(); i++) {
 			if (!negative) {
 				left.add(g.power(powers.getAt(i).getElement()));
 				right.add((y.power(powers.getAt(i).getElement())).mult(ms
 						.getAt(i)));
 			} else {
 				left.add((g.power(powers.getAt(i).getElement())).inverse());
 				right.add(((y.power(powers.getAt(i).getElement())).mult(ms
 						.getAt(i))).inverse());
 			}
 		}
 
 		ProductGroupElement encryptedMsg = new ProductGroupElement(
 				new ProductGroupElement(left), new ProductGroupElement(right));
 		return encryptedMsg;
 	}
 
 	/**
 	 * This function verifies the equation: F^v * Ftag == ENCpk(1,-Kf) *
 	 * PI((wi)^Ke)
 	 * 
 	 * @param N
 	 *            size of the arrays
 	 * @param Gq
 	 *            The group
 	 * @param pk
 	 *            The public key
 	 * @param wOutput
 	 *            Array of output ciphertexts
 	 * @param width
 	 *            width of plaintexts and ciphertexts
 	 * @param Ftag
 	 *            ciphertext
 	 * @param Kf
 	 *            ciphertext
 	 * @param Ke
 	 *            array of N elements in Zq
 	 * @param F
 	 *            the multiplication of WInput^ei N times
 	 * @param v
 	 *            challenge computed by the Random Oracle
 	 * @return true if the equation is correct and false otherwise.
 	 */
 	protected static boolean verifyFFtag(int N, IGroup Gq,
 			ProductGroupElement pk,
 			ArrayOfElements<ProductGroupElement> wOutput, int width,
 			ProductGroupElement Ftag, ProductRingElement Kf,
 			ArrayOfElements<IntegerRingElement> Ke, ProductGroupElement F,
 			LargeInteger v) {
 
 		ProductGroupElement leftF = (F.power(v)).mult(Ftag);
 
 		// TODO remove
 		System.out.println("F'Fv = " + leftF);
 
 		ProductGroupElement W = wOutput.getAt(0)
 				.power(Ke.getAt(0).getElement());
 
 		for (int i = 1; i < N; i++) {
 			W = W.mult(wOutput.getAt(i).power(Ke.getAt(i).getElement()));
 		}
 
 		// create ProductGroupElement of 1s
 		ArrayOfElements<IGroupElement> arrOfOnes = new ArrayOfElements<IGroupElement>();
 		for (int i = 0; i < width; i++) {
 			arrOfOnes.add(Gq.one());
 		}
 
 		ProductGroupElement ones = new ProductGroupElement(arrOfOnes);
 
 		ProductGroupElement rigthF = encrypt(ones, Kf, pk, Gq, true);
 
 		// TODO remove
 		System.out.println("enc(1, -kf) = " + rigthF);
 
 		if (!leftF.equals(rigthF.mult(W))) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * This function computes a seed.
 	 * 
 	 * @param ROSeed
 	 *            RandomOracle for computing the seed
 	 * @param nodeForSeed
 	 *            a node includes the params needed for the seed
 	 * @param ro
 	 *            prefix to random oracle
 	 * @return a seed represented as a byte[]
 	 */
 	protected static byte[] ComputeSeed(RandomOracle ROSeed, Node nodeForSeed,
 			byte[] ro) {
 		return ROSeed.getRandomOracleOutput(ArrayGenerators.concatArrays(ro,
 				nodeForSeed.toByteArray()));
 	}
 
 	/**
 	 * This function makes sure 0 < v < 2^Nv
 	 * 
 	 * @param Nv
 	 *            number of bits in challenge
 	 * @param challenge
 	 *            the challenge itself
 	 * @return a number 0 < v < 2^Nv
 	 */
 	protected static LargeInteger computeV(int Nv, byte[] challenge) {
 		/* Computation of v: */
 		LargeInteger v = byteArrayToPosLargeInteger(challenge);
 		LargeInteger twoNv = new LargeInteger("2").power(Nv);
 		v = v.mod(twoNv);
 		return v;
 	}
 
 	/**
 	 * This function computes the challenge used in the proof
 	 * 
 	 * @param ROChallenge
 	 *            RandomOracle for computing the challenge
 	 * @param ro
 	 *            prefix to random oracle
 	 * @param node
 	 *            Node used for the RO's input
 	 * @param leaf
 	 *            leaf used for the RO's input
 	 * @return the challenge v as a byte array
 	 */
 	protected static byte[] computeChallenge(RandomOracle ROChallenge,
 			byte[] ro, Node node, ByteTree leaf) {
 		ByteTree[] inputChallenge = new ByteTree[2];
 		inputChallenge[0] = leaf;
 		inputChallenge[1] = node;
 		Node nodeForChallenge = new Node(inputChallenge);
 
 		byte[] challenge = ROChallenge.getRandomOracleOutput(ArrayGenerators
 				.concatArrays(ro, nodeForChallenge.toByteArray()));
 		return challenge;
 	}
 
 	/**
 	 * This function computes A, needed in the proof.
 	 * 
 	 * @param N
 	 *            size of the arrays
 	 * @param Ne
 	 *            number of bits in each component
 	 * @param seed
 	 *            byte[] needed to initialize the prg
 	 * @param prg
 	 *            Pseudo-random generator used to derive random vectors for
 	 *            batching
 	 * @param u
 	 *            Array of Pedersen Commitments
 	 * @return A - a multiplication of Ui^Ei N times
 	 */
 	protected static IGroupElement computeA(int N, int Ne, byte[] seed,
 			PseudoRandomGenerator prg, ArrayOfElements<IGroupElement> u,
 			IGroup Gq) {
 		// TODO check this
 		int length = 8 * ((int) Math.ceil((double) (Ne / 8.0)));
 		// int length = Ne + 7;
 		prg.setSeed(seed);
 
 		byte[] byteArrToBigInt;
 		LargeInteger t;
 		LargeInteger e;
 		IGroupElement A = Gq.one();
 
 		for (int i = 0; i < N; i++) {
 			byteArrToBigInt = prg.getNextPRGOutput(length);
 			t = byteArrayToPosLargeInteger(byteArrToBigInt);
 			e = t.mod(new LargeInteger("2").power(Ne));
 			A = A.mult(u.getAt(i).power(e));
 		}
 		return A;
 	}
 
 	/**
 	 * This function computes E, needed in the proof.
 	 * 
 	 * @param N
 	 *            size of the arrays
 	 * @param Ne
 	 *            number of bits in each component
 	 * @param seed
 	 *            byte[] needed to initialize the prg
 	 * @param prg
 	 *            Pseudo-random generator used to derive random vectors for
 	 *            batching
 	 * @return E, the multiplication of Ei N times
 	 */
 	protected static LargeInteger computeE(int N, int Ne, byte[] seed,
 			PseudoRandomGenerator prg) {
 		// TODO check this
 		// int length = Ne + 7;
 		int length = 8 * ((int) Math.ceil((double) (Ne / 8.0)));
 		prg.setSeed(seed);
 		byte[] byteArrToBigInt;
 		LargeInteger t;
 		LargeInteger E = LargeInteger.ONE;
 
 		for (int i = 0; i < N; i++) {
 			byteArrToBigInt = prg.getNextPRGOutput(length);
 			t = byteArrayToPosLargeInteger(byteArrToBigInt);
 			LargeInteger pow = new LargeInteger("2").power(Ne);
 			LargeInteger a = t.mod(pow);
 			E = E.multiply(a);
 		}
 
 		// TODO
 		System.out.println("E :" + E);
 
 		return E;
 	}
 
 	/**
 	 * This function computes F, needed in the proof.
 	 * 
 	 * @param N
 	 *            size of the arrays
 	 * @param Ne
 	 *            number of bits in each component
 	 * @param seed
 	 *            the byte[] needed to initialize the prg
 	 * @param prg
 	 *            Pseudo-random generator used to derive random vectors for
 	 *            batching
 	 * @param wInput
 	 *            Array of input ciphertexts
 	 * @return F, the multiplication of WInput^ei N times
 	 */
 	protected static ProductGroupElement computeF(int N, int Ne, byte[] seed,
 			PseudoRandomGenerator prg,
 			ArrayOfElements<ProductGroupElement> wInput) {
 
 		// TODO check this
 		int length = 8 * ((int) Math.ceil((double) (Ne / 8.0)));
 		// int length = Ne + 7;
 
 		prg.setSeed(seed);
 		byte[] ByteArrToBigInt = prg.getNextPRGOutput(length);
 		LargeInteger t = new LargeInteger(ByteArrToBigInt);
 		LargeInteger e = t.mod(new LargeInteger("2").power(Ne));
 		ProductGroupElement F = wInput.getAt(0).power(e);
 
 		for (int i = 1; i < N; i++) {
 			ByteArrToBigInt = prg.getNextPRGOutput(length);
 			t = byteArrayToPosLargeInteger(ByteArrToBigInt);
 			e = t.mod(new LargeInteger("2").power(Ne));
 			F = F.mult(wInput.getAt(i).power(e));
 		}
 		return F;
 	}
 
 	/**
 	 * This function computes C, needed in the proof.
 	 * 
 	 * @param u
 	 *            Array of Pedersen Commitments in Gq
 	 * @param h
 	 *            Array of random elements used to compute the seed
 	 * @param N
 	 *            Size of the arrays
 	 * @return C, the multiplication of Ui elements divided by multiplication of
 	 *         hi elements
 	 */
 	protected static IGroupElement computeC(ArrayOfElements<IGroupElement> u,
 			ArrayOfElements<IGroupElement> h, int N) {
 
 		IGroupElement CNumerator = u.getAt(0);
 		IGroupElement CDenominator = h.getAt(0);
 		for (int i = 1; i < N; i++) {
 			CNumerator = CNumerator.mult(u.getAt(i));
 			CDenominator = CDenominator.mult(h.getAt(i));
 		}
 		IGroupElement C = CNumerator.divide(CDenominator);
 		return C;
 	}
 
 	/**
 	 * This function computes D, needed in the proof.
 	 * 
 	 * @param B
 	 *            array of N elements on Gq
 	 * @param h
 	 *            Array of random elements used to compute the seed
 	 * @param N
 	 *            size of the arrays
 	 * @param prg
 	 * @param seed
 	 * @param Ne
 	 * @return D, B[n-1] divided by h0^(the multiplication of e's elements)
 	 */
 	protected static IGroupElement computeD(ArrayOfElements<IGroupElement> B,
 			ArrayOfElements<IGroupElement> h, int N, int Ne, byte[] seed,
 			PseudoRandomGenerator prg) {
 
 		int length = 8 * ((int) Math.ceil((double) (Ne / 8.0)));
 		prg.setSeed(seed);
 		byte[] byteArrToBigInt;
 		LargeInteger t;
 		// LargeInteger E = LargeInteger.ONE;
 		LargeInteger e;
 		IGroupElement tempH = h.getAt(0);
 
 		for (int i = 0; i < N; i++) {
 			byteArrToBigInt = prg.getNextPRGOutput(length);
 			t = byteArrayToPosLargeInteger(byteArrToBigInt);
 			LargeInteger pow = (new LargeInteger("2")).power(Ne);
 			e = t.mod(pow);
 			tempH = tempH.power(e);
 		}
 
 		//TODO printouts
 		System.out.println("h0^pi(e) : " + tempH);
 
 		IGroupElement D = B.getAt(N - 1).divide(tempH);
 		return D;
 	}
 
 	/**
 	 * This function verifies the first equation: A^v * Atag == (g^ka) *
 	 * PI(h[i]^ke[i])
 	 * 
 	 * @param A
 	 *            the multiplication of Ui^Ei N times
 	 * @param Atag
 	 *            element in Gq
 	 * @param v
 	 *            challenge computed by the Random Oracle
 	 * @param Ke
 	 *            array of N elements in Zq
 	 * @param g
 	 *            generator of Gq
 	 * @param N
 	 *            size of the arrays
 	 * @param h
 	 *            Array of random elements used to compute the seed
 	 * @param Ka
 	 *            element in Zq
 	 * @return true if the equation is correct and false otherwise.
 	 */
 	protected static boolean verifyAvAtag(IGroupElement A, IGroupElement Atag,
 			LargeInteger v, ArrayOfElements<IntegerRingElement> Ke,
 			IGroupElement g, int N, ArrayOfElements<IGroupElement> h,
 			IntegerRingElement Ka) {
 
 		IGroupElement left = (A.power(v)).mult(Atag);
 		IGroupElement hPi = h.getAt(0).power(Ke.getAt(0).getElement());
 		for (int i = 1; i < N; i++) {
 			hPi = hPi.mult(h.getAt(i).power(Ke.getAt(i).getElement()));
 		}
 		IGroupElement right = (g.power(Ka.getElement())).mult(hPi);
 		// TODO
 		System.out.println("A^v : " + A.power(v));
 		System.out.println("A^v A' : " + left);
 
 		if (!left.equals(right)) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * This function verifies the equation: B^v * Btag == g^kbi * B[i-1]^Kei
 	 * 
 	 * @param B
 	 *            Array of N elements in Gq
 	 * @param Btag
 	 *            Array of N elements in Gq
 	 * @param Kb
 	 *            array of N elements in Zq
 	 * @param Ke
 	 *            array of N elements in Zq
 	 * @param g
 	 *            generator of Gq
 	 * @param v
 	 *            challenge computed by the Random Oracle
 	 * @param h
 	 *            Array of random elements used to compute the seed
 	 * @param N
 	 *            size of the arrays
 	 * @return true if the equation is correct and false otherwise.
 	 */
 	protected static boolean verifyBvBtag(ArrayOfElements<IGroupElement> B,
 			ArrayOfElements<IGroupElement> Btag,
 			ArrayOfElements<IntegerRingElement> Kb,
 			ArrayOfElements<IntegerRingElement> Ke, IGroupElement g,
 			LargeInteger v, ArrayOfElements<IGroupElement> h, int N) {
 
 		IGroupElement left = ((B.getAt(0)).power(v)).mult(Btag.getAt(0));
 		IGroupElement right = g.power(Kb.getAt(0).getElement()).mult(
 				h.getAt(0).power(Ke.getAt(0).getElement()));
 		if (!left.equals(right)) {
 			return false;
 		}
 
 		for (int i = 1; i < N; i++) {
 			left = ((B.getAt(i)).power(v)).mult(Btag.getAt(i));
 			right = (g.power(Kb.getAt(i).getElement())).mult(B.getAt(i - 1)
 					.power(Ke.getAt(i).getElement()));
 			if (!left.equals(right)) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * This function verifies the equation: C^v * Ctag == g^kc
 	 * 
 	 * @param C
 	 *            the multiplication of u elements
 	 * @param Ctag
 	 *            element in Gq
 	 * @param v
 	 *            challenge computed by the Random Oracle
 	 * @param Kc
 	 *            element in Zq
 	 * @param g
 	 *            generator of Gq
 	 * @return true if the equation is correct and false otherwise.
 	 */
 	protected static boolean verifyCvCtag(IGroupElement C, IGroupElement Ctag,
 			LargeInteger v, IntegerRingElement Kc, IGroupElement g) {
 
 		IGroupElement left = (C.power(v)).mult(Ctag);
 		IGroupElement right = g.power(Kc.getElement());
 
 		// TODO printout
 		System.out.println("C^v : " + C.power(v));
 		System.out.println("C^v C' : " + left);
 		if (!left.equals(right)) {
 			return false;
 		}
 
 		return true;
 	}
 
 	/**
 	 * This function verifies the equation: D^v * Dtag == g^kd
 	 * 
 	 * @param D
 	 *            the multiplication of h elements
 	 * @param Dtag
 	 *            element in Gq
 	 * @param v
 	 *            challenge computed by the Random Oracle
 	 * @param Kd
 	 *            element in Zq
 	 * @param g
 	 *            generator of Gq
 	 * @return true if the equation is correct and false otherwise.
 	 */
 	protected static boolean verifyDvDtag(IGroupElement D, IGroupElement Dtag,
 			LargeInteger v, IntegerRingElement Kd, IGroupElement g) {
 
 		IGroupElement left = (D.power(v)).mult(Dtag);
 		IGroupElement right = g.power(Kd.getElement());
 
 		// TODO print
 		System.out.println("D^v: " + D.power(v));
 		System.out.println("D^v D' : " + left);
 		if (!left.equals(right)) {
 			return false;
 		}
 		return true;
 	}
 
 	public static LargeInteger byteArrayToPosLargeInteger(byte[] bytes) {
 		byte[] byteArrToBigIntPos = new byte[bytes.length + 1];
 		byteArrToBigIntPos[0] = 0x00;
 		System.arraycopy(bytes, 0, byteArrToBigIntPos, 1, bytes.length);
 
 		return new LargeInteger(byteArrToBigIntPos);
 	}
 
 }
