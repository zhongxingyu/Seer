 package com.rau.evoting.ElGamal;
 
 import java.math.BigInteger;
 
 import com.rau.evoting.utils.Pair;
 import com.rau.evoting.utils.RandomHelper;
 
 public class ChaumPedersenProof {
 
 	private BigInteger y1;
 	private BigInteger y2;
 	private BigInteger a1;
 	private BigInteger a2;
 	private BigInteger k;
 	private BigInteger c;
 	private BigInteger s;
 	private BigInteger r;
 
 	private BigInteger g;
 	private BigInteger p;
 	private BigInteger y;
 
 	private BigInteger res1;
 	private BigInteger res2;
 
 
 	public Pair<BigInteger, BigInteger> generate(BigInteger pubKey, BigInteger r) {
 		g = GlobalParameters.getParams().getG();
 		p = GlobalParameters.getParams().getP();
 		y = pubKey;
 		this.r = r;
 
 		y1 = g.modPow(r, p);
 		y2 = y.modPow(r, p);
 
 		ProverIteration1();
 		VerifierIteration1();
 		ProverIteration2();
 		VerifierIteration2();
 
 		return new Pair<BigInteger, BigInteger>(res1, res2);
 	}
 
 	public Pair<BigInteger, BigInteger> ProverIteration1() {
 		k = RandomHelper.randomNonZeroBigInteger(p
 				.subtract(new BigInteger("1")));
 		a1 = g.modPow(k, p);
 		a2 = y.modPow(k, p);
 		return new Pair<BigInteger, BigInteger>(a1, a2);
 
 	}
 
 	public BigInteger ProverIteration2() {
 		BigInteger temp = c.multiply(r).mod(p);
 		s = k.subtract(temp).mod(p);
 		return s;
 	}
 
 	public BigInteger VerifierIteration1() {
 		String temp = y1.toString().concat(y2.toString());
 		long t2 = temp.hashCode();
 		c = BigInteger.valueOf(t2);
 		// RandomHelper.randomNonZeroBigInteger(pubKey.getParameters().getP()
 		// .subtract(new BigInteger("1")));
 		return c;
 	}
 
 	public boolean VerifierIteration2() {
 		res1 = g.modPow(s, p).multiply(y1.modPow(c, p).mod(p));
 		res2 = y.modPow(s, p).multiply(y2.modPow(c, p).mod(p));
 
 		if (a1.equals(res1) && a2.equals(res2))
 			return true;
 
 		return false;
 	}
 
 	public BigInteger getA1() {
 		return a1;
 	}
 
 	public BigInteger getA2() {
 		return a2;
 	}
 
 }
