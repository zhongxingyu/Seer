 /*
  * Copyright 2010 kk-electronic a/s. 
  * 
  * This file is part of KKPortal.
  *
  * KKPortal is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * KKPortal is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with KKPortal.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package com.kk_electronic.kkportal.core.security;
 
 import com.google.inject.Inject;
 
 /**
  * Implements the SHA256 algorithm as specified in 
  * <a href="http://csrc.nist.gov/publications/fips/fips180-3/fips180-3_final.pdf">Secure Hash Standard</a>
  * 
  * Some special consideration has to be done to ensure that javascript handles the interger overflows needed for the
  * algorithm correctly.
  * @author Jes Andersen
  */
 public class SHA256 implements Hasher {
 
 	/**
 	 * Internal state of the hash function
 	 */
 	private double H[];
 
 	/**
 	 * These words represent the first thirty-two bits of the fractional parts of
 	 * the cube roots of the first sixty-four prime numbers
 	 */
 	private double K[] = new double[] { 0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5,
 			0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5, 0xd807aa98,
 			0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe,
 			0x9bdc06a7, 0xc19bf174, 0xe49b69c1, 0xefbe4786, 0x0fc19dc6,
 			0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
 			0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3,
 			0xd5a79147, 0x06ca6351, 0x14292967, 0x27b70a85, 0x2e1b2138,
 			0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e,
 			0x92722c85, 0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3,
 			0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070, 0x19a4c116,
 			0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a,
 			0x5b9cca4f, 0x682e6ff3, 0x748f82ee, 0x78a5636f, 0x84c87814,
 			0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2 };
 
 	@Inject
 	public SHA256() {
 		reset();
 	}
 
 	@Override
 	public String hash(String input) {
 		// Reset internal variables
 		reset();
 		
 		//Make the length of the final message modulo 512
 		String padded = addPadding(input);
 		
 		for (int i = 0; i < padded.length(); i += 512 / 8) {
 			//Run the algorithm for each block
 			processBlock(makeWords(padded.substring(i, i + 512 / 8)));
 		}
 		// Finally return the hexdigest of the internal state
 		return toHex();
 	}
 
 	private String toHex() {
 		StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < 8; i++) {
 			double d = H[i];
 			//If the value is negative we emulate integer underflow
 			if(d < 0){
 				d += 4294967296.0;
 			}
 			// digits defines the value and ordering since charat(value) return the right character for any single digit value
 			String digits = "0123456789abcdef";
 			/*
 			 * We are storing a 32bit int in the double datatype which is 8 hexdigits.
 			 * Using big endian (Most significant value first) we get the value with bit mangling.
 			 * By shifting the value 28 bit to the right (shr) and taking the now first 4 bits (mod)
 			 * we get the most significant hex digit.
 			 * This process is done for all the digits.
 			 */ 
 			sb.append(digits.charAt(mod(shr(d, 28), 16)));
 			sb.append(digits.charAt(mod(shr(d, 24), 16)));
 			sb.append(digits.charAt(mod(shr(d, 20), 16)));
 			sb.append(digits.charAt(mod(shr(d, 16), 16)));
 			sb.append(digits.charAt(mod(shr(d, 12), 16)));
 			sb.append(digits.charAt(mod(shr(d, 8), 16)));
 			sb.append(digits.charAt(mod(shr(d, 4), 16)));
 			sb.append(digits.charAt(mod(d,16)));
 		}
 		return sb.toString();
 	}
 
 	private double[] makeWords(String s) {
 		double[] words = new double[80];
 		int i;
 		for (i = 0; i < 16; i++) {
 			words[i] = (s.charAt(i * 4 + 0) << 24)
 					| (s.charAt(i * 4 + 1) << 16) | (s.charAt(i * 4 + 2) << 8)
 					| s.charAt(i * 4 + 3);
 		}
 		for (i = 16; i < 80; i++) {
 			words[i] = add(add(add(gamma1256(words[i - 2]), words[i - 7]),
 					gamma0256(words[i - 15])), words[i - 16]);
 		}
 		return words;
 	}
 
 	private void processBlock(double[] words) {
 		/*
 		 * a 0 b 1 c 2 d 3 e 4 f 5 g 6 h 7
 		 */
 		int t;
 		double[] Hcopy = new double[8];
 		for (t = 0; t < 8; t++) {
 			Hcopy[t] = H[t];
 		}
 		for (t = 0; t < 64; t++) {
 			double T1 = add(add(add(add(Hcopy[7], sigma1(Hcopy[4])), Ch(
 					Hcopy[4], Hcopy[5], Hcopy[6])), K[t]), words[t]);
 			double T2 = add(sigma0(Hcopy[0]), Maj(Hcopy[0], Hcopy[1], Hcopy[2]));
 			Hcopy[7] = Hcopy[6];
 			Hcopy[6] = Hcopy[5];
 			Hcopy[5] = Hcopy[4];
 			Hcopy[4] = add(Hcopy[3], T1);
 			Hcopy[3] = Hcopy[2];
 			Hcopy[2] = Hcopy[1];
 			Hcopy[1] = Hcopy[0];
 			Hcopy[0] = add(T1, T2);
 		}
 		for (t = 0; t < 8; t++) {
 			H[t] = add(H[t], Hcopy[t]);
 		}
 	}
 
 	/**
 	 * We implement this directly to make it prettier.
 	 * Javascript can do binary operations on doubles directly.
 	 */
 	private native double Ch(double x, double y, double z) /*-{
 		return (x & y) ^ (~x & z);
 	}-*/;
 
 	/**
 	 * We implement this directly to make it prettier.
 	 * Javascript can do binary operations on doubles directly.
 	 */
 	private native double Maj(double x, double y, double z) /*-{
 		return (x & y) ^ (x & z) ^ (y & z);
 	}-*/;
 
 	/**
 	 * We implement this directly to make it prettier.
 	 * Javascript can do binary operations on doubles directly.
 	 */
 	private double sigma0(double x) {
 		return xor(xor(rotr(x, 2), rotr(x, 13)), rotr(x, 22));
 	}
 
 	/**
 	 * We implement this directly to make it prettier.
 	 * Javascript can do binary operations on doubles directly.
 	 */
 	private double sigma1(double x) {
 		return xor(xor(rotr(x, 6), rotr(x, 11)), rotr(x, 25));
 	}
 
 	/**
 	 * We implement this directly to make it prettier.
 	 * Javascript can do binary operations on doubles directly.
 	 */
 	private double gamma0256(double x) {
 		return xor(xor(rotr(x, 7), rotr(x, 18)), shr(x, 3));
 	}
 
 	/**
 	 * We implement this directly to make it prettier.
 	 * Javascript can do binary operations on doubles directly.
 	 */
 	private double gamma1256(double x) {
 		return xor(xor(rotr(x, 17), rotr(x, 19)), shr(x, 10));
 	}
 
 	/**
 	 * We implement this directly to make it prettier.
 	 * Javascript can do binary operations on doubles directly.
 	 */
 	private native double shr(double x, double n) /*-{
 		return (x >>> n);
 	}-*/;
 
 	/**
 	 * We implement this directly to make it prettier.
 	 * Javascript can do binary operations on doubles directly.
 	 */
 	private native double rotr(double x, double n) /*-{
 		return ((x >>> n) | (x << (32 - n)));
 	}-*/;
 
 	/**
 	 * As per specifications [ M | 1 | 0000...000 | L ] where M is the input 1
 	 * is a high bit 0000...000 the smallest number of zeroes such that the
 	 * entire message is a multiple of 512 bits L is the number of bits in M
 	 * 
 	 * @param input
 	 * @return
 	 */
 
 	private String addPadding(String input) {
 		StringBuilder sb = new StringBuilder(input);
 		char a = 0x80; // The one bit followed by the first 7 zeroes
 		sb.append(a);
 		long l = input.length() * 8;
 		int i = (int) (l % 512);
		int p = ((1024 - 64 - 8 - i) % 512) / 8;
 		while (p > 0) {
 			sb.append((char) 0);
 			p--;
 		}
 		sb.append((char) ((l >> 56) & 0xff));
 		sb.append((char) ((l >> 48) & 0xff));
 		sb.append((char) ((l >> 40) & 0xff));
 		sb.append((char) ((l >> 32) & 0xff));
 		sb.append((char) ((l >> 24) & 0xff));
 		sb.append((char) ((l >> 16) & 0xff));
 		sb.append((char) ((l >> 8) & 0xff));
 		sb.append((char) (l & 0xff));
 		return sb.toString();
 	}
 
 	/**
 	 * The add function need to make it modulo integer size to emulate overflows
 	 */
 	public native double add(double a, double b) /*-{
 		return ((a+b) % 4294967296);
 	}-*/;
 
 	/**
 	 * We implement this directly to make it prettier.
 	 * Javascript can do binary operations on doubles directly.
 	 */
 	public native int mod(double a, int b) /*-{
 		return (a % b);
 	}-*/;
 
 	/**
 	 * We implement this directly to make it prettier.
 	 * Javascript can do binary operations on doubles directly.
 	 */
 	public native double xor(double a, double b) /*-{
 		return a ^ b;
 	}-*/;
 
 	/**
 	 * Look at the specification for the these numbers.
 	 */
 	public void reset() {
 		H = new double[] { 0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a,
 				0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19 };
 	}
 }
