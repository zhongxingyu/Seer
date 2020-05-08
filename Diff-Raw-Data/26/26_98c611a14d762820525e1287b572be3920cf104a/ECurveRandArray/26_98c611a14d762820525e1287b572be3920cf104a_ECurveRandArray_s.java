 package arithmetic.objects.groups;
 
 
 import algorithms.provers.Prover;
 import arithmetic.objects.LargeInteger;
 import arithmetic.objects.arrays.ArrayOfElements;
 import arithmetic.objects.field.IField;
 import arithmetic.objects.field.IntegerFieldElement;
 import arithmetic.objects.field.PrimeOrderField;
 import cryptographic.primitives.PseudoRandomGenerator;
 
 /**
  * This object returns a new array of N random elliptic curve points.
  * 
  * @author Sofia
  * 
  */
 public class ECurveRandArray {
 
 	ArrayOfElements<IGroupElement> Rand;
 	// Powers:
 	LargeInteger Q;
 	LargeInteger s;
 	// Least QNR
 	LargeInteger k;
 	// Prime order of field
 	LargeInteger q;
 
 	// eCurve parameters:
 	LargeInteger a;
 	LargeInteger b;
 
 	IField<IntegerFieldElement> field;
 
 	// Constructor for tests
 	public ECurveRandArray(LargeInteger q, LargeInteger a, LargeInteger b) {
 		this.q = q;
 		this.a = a;
 		this.b = b;
 		findPowers();
 		findLeastQNR();
 	}
 
 	/**
 	 * 
 	 * @param q
 	 *            - prime field order
 	 * @param N
 	 *            - size of the array
 	 * @param prg
 	 *            - PseudoRandomGenerator to generate random numbers
 	 * @param seed
 	 *            - for the PRG
 	 * @param nr
 	 * @param a
 	 *            ,b - parameters of the elliptic curve
 	 * @param G
 	 *            - the group
 	 */
 	public ECurveRandArray(int N, PseudoRandomGenerator prg, byte[] seed,
 			int nr, LargeInteger a, LargeInteger b, ECurveGroup G) {
 
 		this.a = a;
 		this.b = b;
 		this.q = G.getFieldOrder();
 		field = new PrimeOrderField(q);
 		setEnv();
 		
 		/*
 		 * Create the random array - first we generate numbers using prg, and
 		 * then we check if these numbers fit to the elliptic curve. 
 		*/
 		
 		int counter = 0;
 
 		ECurveGroupElement element;
 		Point point;
 		
 		ArrayOfElements<IGroupElement> RandArray = new ArrayOfElements<IGroupElement>();
 		int nq = this.q.bitLength();
 
		//TODO check
 		int length = 8 * ((int) Math.ceil((double) ((nq + nr) / 8.0)));
		//int length = 7 + nr + nq;
 		prg.setSeed(seed);
 
 		// We run until q (this is the maximum, but we break when we have N
 		// elements
 		for (LargeInteger i = LargeInteger.ZERO; !i.equals(q
 				.subtract(LargeInteger.ONE));) {
 
 			byte[] arr = prg.getNextPRGOutput(length);
 			LargeInteger t = Prover.byteArrayToPosLargeInteger(arr);
 			LargeInteger ttag = t.mod((new LargeInteger("2")).power(nq + nr));
 
 			// xi is the x coordinate we want to check
 			LargeInteger xValue = ttag.mod(q);
 
 			// check f(xi) is a quadratic residue (mod q) and we need to find
 			// the roots.
 			LargeInteger zValue = f(xValue);
 			if (Legendre(zValue) == 1) {
 				// find the smallest root
				// TODO check if our ressol gives the same result
 				LargeInteger yValue = shanksTonelli(zValue);
			//try {
//				LargeInteger yValue = new LargeInteger(IntegerFunctions.ressol(
//						zValue, q));
//				LargeInteger yValueRoot = q.subtract(yValue);
//				if (yValueRoot.compareTo(yValue) < 0)
//					yValue = yValueRoot;
				
 				// Create coordinates
 				IntegerFieldElement xi = new IntegerFieldElement(xValue, field);
 				IntegerFieldElement yi = new IntegerFieldElement(yValue, field);
 				point = new Point(xi, yi);
 
 				element = new ECurveGroupElement(point, G);
 				RandArray.add(element);
 				
 				counter++;
 				// We count how many elements we added to the array. We need N elements
 				if (counter == N)
 					break;
 
 				i = i.add(LargeInteger.ONE);
//			} catch (Exception e) {
//				continue;
 			}
 			Rand = RandArray;
 		}
 	}
 
 	private void setEnv() {
 		/*
 		 * Prepare the ground for running ressol algorithm: We compute all of
 		 * the things we can compute only once, like the powers Q and s, the the
 		 * least quadratic non residue (mod q). We do this here because the
 		 * complexity of each function can be finally O(q).
 		 */
 		// Define Q and s to be p-1=Q2^s when Q is odd
 		findPowers();
 
 		// Define b as a the least quadratic non residual
 		findLeastQNR();
 
 	}
 
 	/**
 	 * 
 	 * @param xi
 	 *            - input for the elliptic curve
 	 * @return zValue = f(xi) = y^2 = x^3 + ax +b
 	 */
 	public LargeInteger f(LargeInteger xi) {
		//LargeInteger result = (LargeInteger.power(xi, new LargeInteger("3")))
				//.mod(q);
 		LargeInteger result = (xi.power(3)).mod(q);
 		result = (result.add(a.multiply(xi))).mod(q);
 		result = result.add(b.mod(q));
 		return result.mod(q);
 	}
 
 	/**
 	 * 
 	 * @param num
 	 *            - an integer which is a quadratic residue (mod p).
 	 * @param p
 	 *            - prime order of field
 	 * @return the smallest square root that satisfies y^2 = a (mod p)
 	 */
 	public LargeInteger shanksTonelli(LargeInteger num) {
 
 		LargeInteger privateCase = simpleShanksTonelli(num);
 
 		// If the private case isn't relevant we compute the roots.
 		if (privateCase.compareTo(LargeInteger.ZERO) == 0) {
 
 			LargeInteger[] retVal = new LargeInteger[2];
 
 			// Verify that the Lagendre symbol of a and p is not -1 or 0:
 			if (Legendre(num) <= 0)
 				return null;
 
 			// Find a's opposite in the modular field
 			LargeInteger atag = num.modInverse(q);
 
 			// compute c
 			LargeInteger c = k.modPow(Q, q);
 
 			// compute R
 			LargeInteger r = num.modPow(
 					Q.add(LargeInteger.ONE).divide(new LargeInteger("2")), q);
 
 			LargeInteger i = LargeInteger.ONE;
 			LargeInteger d;
 
 			for (i = LargeInteger.ONE; !i.equals(s);) {
 				// compute the power as s-i-1
 				LargeInteger pow = new LargeInteger("2").power(s.intValue()
 						- i.intValue() - 1);
 				d = (r.power(2).multiply(atag).modPow(pow, q));
 
 				if (d.equals(q.subtract(LargeInteger.ONE)))
 					r = r.multiply(c).mod(q);
 				c = c.modPow(new LargeInteger("2"), q);
 
 				i = i.add(LargeInteger.ONE);
 			}
 
 			retVal[0] = r;
 			retVal[1] = q.subtract(r); // -r mod p
 
 			// return the smallest root
 			return retVal[0].min(retVal[1]);
 		} else {
 			return privateCase;
 		}
 	}
 
 	/**
 	 * This function returns the roots of some private cases of mod(p) -- some
 	 * of them have fixed function that compute the roots.
 	 * 
 	 * @param num
 	 *            - the number to find it's (mod p) square root
 	 * @return the root if the case is p= 4m (mod 3) or 0 otherwise.
 	 */
 	public LargeInteger simpleShanksTonelli(LargeInteger num) {
 		if (q.mod(new LargeInteger("4")).compareTo(new LargeInteger("3")) == 0) {
 			LargeInteger root1;
 			LargeInteger root2;
 
 			LargeInteger exponent = (q.add(LargeInteger.ONE))
 					.divide(new LargeInteger("4"));
 			root1 = num.modPow(exponent, q);
 			root2 = q.subtract(root1);
 			return root1.min(root2);
 		}
 		return LargeInteger.ZERO;
 	}
 
 	/**
 	 * 
 	 * @return the first quadratic non residue of q
 	 */
 	public void findLeastQNR() {
 		LargeInteger retVal = LargeInteger.ONE;
 		boolean flag = false;
 		while (!flag) {
 			retVal = retVal.add(LargeInteger.ONE);
 			if (Legendre(retVal) == -1) {
 				this.k = retVal;
 				flag = true;
 				return;
 			} else
 				retVal = retVal.add(LargeInteger.ONE);
 
 			if (retVal.compareTo(q.subtract(LargeInteger.ONE)) >= 0)
 				this.k = retVal;
 		}
 
 		// The function shouldn't get here at all. Half of the values should be
 		// quadratic non residues of p
 		this.k = retVal;
 	}
 
 	/**
 	 * @param a
 	 *            - integer
 	 * @param p
 	 *            - prime
 	 * @return the Legendre symbol of a / b using Euler criterion.
 	 */
 	public int Legendre(LargeInteger a) {
 		// This should happen in our case because we only check numbers smaller
 		// than q, and q is prime.
 		if (a.remainder(q).equals(LargeInteger.ZERO)) {
 			return 0;
 		}
 		LargeInteger exponent = q.subtract(LargeInteger.ONE);
 		exponent = exponent.divide(new LargeInteger("2"));
 		LargeInteger result = a.modPow(exponent, q); // 1 <= result <= p - 1
 
 		if (result.equals(LargeInteger.ONE)) {
 			return 1;
 		} else if (result.equals(q.subtract(LargeInteger.ONE))) {
 			return -1;
 		} else {
 			throw new ArithmeticException(
 					"Error computing the Legendre symbol.");
 		}
 	}
 
 	/**
 	 * Find positive integers Q and S such that p-1=Q*2^s, where p is a prime
 	 * number it means that p-1 is even. Time complexity of this function is
 	 * log(n).
 	 * 
 	 * @param p
 	 * @return an array of 2 integers where {s,Q}
 	 */
 	public void findPowers() {
 
 		LargeInteger s = LargeInteger.ZERO;
 		LargeInteger Q = q.subtract(LargeInteger.ONE);// p-1 is even
 		// Q needs to be odd
 		while (Q.mod(LargeInteger.valueOf(2)).equals(LargeInteger.ZERO)) {
 			Q = Q.divide(new LargeInteger("2"));
 			s = s.add(LargeInteger.ONE);
 		}
 		this.s = s;
 		this.Q = Q;
 	}
 
 	public ArrayOfElements<IGroupElement> getRand() {
 		return Rand;
 	}
 
 	public LargeInteger getQ() {
 		return Q;
 	}
 
 	public void setQ(LargeInteger q) {
 		Q = q;
 	}
 
 	public LargeInteger getS() {
 		return s;
 	}
 
 	public void setS(LargeInteger s) {
 		this.s = s;
 	}
 
 	public LargeInteger getB() {
 		return k;
 	}
 
 	public void setB(LargeInteger b) {
 		this.k = b;
 	}
 
 	public void setRand(ArrayOfElements<IGroupElement> rand) {
 		Rand = rand;
 	}
 }
