package main.java.itsp2;

 import java.math.BigInteger;
 import java.security.KeyFactory;
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.security.SecureRandom;
 import java.util.Date;
 
 import javax.crypto.spec.DHParameterSpec;
 import javax.crypto.spec.DHPublicKeySpec;
 
 public class DiffieHellman {
 
 	static BigInteger alice_p;
 	static BigInteger alice_a;
 	static BigInteger alice_A;
 	static BigInteger alice_g;
 	static BigInteger alice_K;
 	static BigInteger bob_b;
 	static BigInteger bob_B;
 	static BigInteger bob_K;
 
 	static Date start;
 	static Date end;
 
 	public static void main(String[] args) {
 		p("DiffieHellman");
 		wikitest();
 
 	}
 
 	public static void wikitest() {
 		start = new Date();
 		p("Wikpedia Test");
 		p("p = 13, g = 2");
 		p("a = 5, b = 7");
 
 		alice_p = BigInteger.valueOf(13);
 		alice_g = BigInteger.valueOf(2);
 
 		alice_a = BigInteger.valueOf(5);
 		bob_b = BigInteger.valueOf(7);
 		p("Alice berechnet A = 2^5 mod 13 = 6 und sendet dieses Ergebnis an Bob.");
 		alice_A = calculate(alice_g, alice_a, alice_p);
 		p("Test: " + alice_A);
 
 		p("Bob berechnet B = 2^7 mod 13 = 11 und sendet dieses Ergebnis an Alice.");
 		bob_B = calculate(alice_g, bob_b, alice_p);
 		p("Test: " + bob_B);
 
 		p("Alice berechnet K = 11^5 mod 13 = 7.");
 		alice_K = calculate(bob_B, alice_a, alice_p);
 		p("Test: " + alice_K);
 
 		p("Bob berechnet K = 6^7 mod 13 = 7.");
 		bob_K = calculate(alice_A, bob_b, alice_p);
 		p("Test: " + bob_K);
 
 		p("Beide haben den selben Schlüssel: Alice(" + alice_K + ") und Bob("
 				+ bob_K + ")");
 
 		end = new Date();
 
 		long time = end.getTime() - start.getTime();
 		p("Zeit: " + time + "ms");
 	}
 
 	public static BigInteger getRandomNumber(BigInteger p) {
 		SecureRandom rnd = new SecureRandom();
 		BigInteger r;
 		do {
 			r = new BigInteger(p.bitLength(), rnd);
 		} while (r.compareTo(p.subtract(BigInteger.valueOf(2))) >= 0);
 		return r;
 	}
 
 	public static BigInteger getPrime() {
 		return BigInteger.valueOf(-1);
 	}
 
 	public static BigInteger calculate(BigInteger base, BigInteger expo,
 			BigInteger mod) {
 		BigInteger tmp = power(base, expo);
 		return tmp.mod(mod);
 	}
 
 	public static void p(String s) {
 		System.out.println(s);
 	}
 
 	public static BigInteger power(BigInteger a, BigInteger exponent) {
 		if (exponent.equals(BigInteger.ONE))
 			return a;
 		else if (exponent.equals(BigInteger.ZERO))
 			return BigInteger.ONE;
 		else
 			return a.multiply(power(a, exponent.subtract(BigInteger.ONE)));
 	}
 }
