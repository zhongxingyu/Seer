 package com.thiagovinicius.web.urlmkr.server;
 
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.HashMap;
 import java.util.Map;
 
 public class MorseCoder implements IdCoder {
 
 	private static enum SYMBOL {
 		DI, DAH;
 	}
 
 	private static final Map<Character, List<SYMBOL>> TO_MORSE;
 	private static final Map<List<SYMBOL>, Character> FROM_MORSE;
 
 	private static final IdCoder INTERMEDIATE_CODER = new Base32Coder();
 
 	static {
 		final SYMBOL DI = SYMBOL.DI;
 		final SYMBOL DAH = SYMBOL.DAH;
 		TO_MORSE = new HashMap<Character, List<SYMBOL>>();
 		TO_MORSE.put('a', Arrays.asList(new SYMBOL [] {DI, DAH}));
 		TO_MORSE.put('b', Arrays.asList(new SYMBOL [] {DAH, DI, DI, DI}));
 		TO_MORSE.put('c', Arrays.asList(new SYMBOL [] {DAH, DI, DAH, DI}));
 		TO_MORSE.put('d', Arrays.asList(new SYMBOL [] {DAH, DI, DI}));
 		TO_MORSE.put('e', Arrays.asList(new SYMBOL [] {DI}));
 		TO_MORSE.put('f', Arrays.asList(new SYMBOL [] {DI, DI, DAH, DI}));
 		TO_MORSE.put('g', Arrays.asList(new SYMBOL [] {DAH, DAH, DI}));
 		TO_MORSE.put('h', Arrays.asList(new SYMBOL [] {DI, DI, DI, DI}));
 		TO_MORSE.put('i', Arrays.asList(new SYMBOL [] {DI, DI}));
 		TO_MORSE.put('j', Arrays.asList(new SYMBOL [] {DI, DAH, DAH, DAH}));
 		TO_MORSE.put('k', Arrays.asList(new SYMBOL [] {DAH, DI, DAH}));
 		TO_MORSE.put('l', Arrays.asList(new SYMBOL [] {DI, DAH, DI, DI}));
 		TO_MORSE.put('m', Arrays.asList(new SYMBOL [] {DAH, DAH}));
 		TO_MORSE.put('n', Arrays.asList(new SYMBOL [] {DAH, DI}));
 		TO_MORSE.put('o', Arrays.asList(new SYMBOL [] {DAH, DAH, DAH}));
 		TO_MORSE.put('p', Arrays.asList(new SYMBOL [] {DI, DAH, DAH, DI}));
 		TO_MORSE.put('q', Arrays.asList(new SYMBOL [] {DAH, DAH, DI, DAH}));
 		TO_MORSE.put('r', Arrays.asList(new SYMBOL [] {DI, DAH, DI}));
 		TO_MORSE.put('s', Arrays.asList(new SYMBOL [] {DI, DI, DI}));
 		TO_MORSE.put('t', Arrays.asList(new SYMBOL [] {DAH}));
 		TO_MORSE.put('u', Arrays.asList(new SYMBOL [] {DI, DI, DAH}));
 		TO_MORSE.put('v', Arrays.asList(new SYMBOL [] {DI, DI, DI, DAH}));
 		TO_MORSE.put('w', Arrays.asList(new SYMBOL [] {DI, DAH, DAH}));
 		TO_MORSE.put('x', Arrays.asList(new SYMBOL [] {DAH, DI, DI, DAH}));
 		TO_MORSE.put('y', Arrays.asList(new SYMBOL [] {DAH, DI, DAH, DAH}));
 		TO_MORSE.put('z', Arrays.asList(new SYMBOL [] {DAH, DAH, DI, DI}));
 		TO_MORSE.put('1', Arrays.asList(new SYMBOL [] {DI, DAH, DAH, DAH, DAH}));
 		TO_MORSE.put('2', Arrays.asList(new SYMBOL [] {DI, DI, DAH, DAH, DAH}));
 		TO_MORSE.put('3', Arrays.asList(new SYMBOL [] {DI, DI, DI, DAH, DAH}));
 		TO_MORSE.put('4', Arrays.asList(new SYMBOL [] {DI, DI, DI, DI, DAH}));
 		TO_MORSE.put('5', Arrays.asList(new SYMBOL [] {DI, DI, DI, DI, DI}));
 		TO_MORSE.put('6', Arrays.asList(new SYMBOL [] {DAH, DI, DI, DI, DI}));
 		TO_MORSE.put('7', Arrays.asList(new SYMBOL [] {DAH, DAH, DI, DI, DI}));
 		TO_MORSE.put('8', Arrays.asList(new SYMBOL [] {DAH, DAH, DAH, DI, DI}));
 		TO_MORSE.put('9', Arrays.asList(new SYMBOL [] {DAH, DAH, DAH, DAH, DI}));
 		TO_MORSE.put('0', Arrays.asList(new SYMBOL [] {DAH, DAH, DAH, DAH, DAH}));
 
 		FROM_MORSE = new HashMap<List<SYMBOL>, Character>(TO_MORSE.size());
 		for (Character c : TO_MORSE.keySet()) {
 			FROM_MORSE.put(TO_MORSE.get(c), c);
 		}
 	}
 
 	@Override
 	public String encodeId(long id) {
 		String intermediate = INTERMEDIATE_CODER.encodeId(id);
 		StringBuilder morse = new StringBuilder();
 		for (int i = 0; i < intermediate.length(); ++i) {
 			char origChar = intermediate.charAt(i);
 			List<SYMBOL> codedChar = TO_MORSE.get(origChar);
 			for (SYMBOL s : codedChar) {
 				if (s == SYMBOL.DI) {
 					morse.append("-");
 				} else if (s == SYMBOL.DAH) {
 					morse.append("---");
 				}
 				morse.append("."); // low signal separing Dits from Dashs
 			}
 			morse.append(".."); // pause between letters
 		}
		morse.setLength(morse.length()-3); // minus the pause signal
		morse.append("~"); // for facebug, otherwise the generated URL is not clickable
		return morse.toString();
 	}
 
 	@Override
 	public long decodeId(String encoded) {
		final int interestLength = encoded.endsWith("~") ?
				encoded.length() -1 : encoded.length();
 		List<SYMBOL> decodedChar = new LinkedList<MorseCoder.SYMBOL>();
 		StringBuilder encodedSignal = new StringBuilder();
 		StringBuilder intermediateResult = new StringBuilder();
		for (int i = 0; i < interestLength; ++i) {
 			encodedSignal.append(encoded.charAt(i));
 			if (encodedSignal.toString().equals("-.")) {
 				decodedChar.add(SYMBOL.DI);
 				encodedSignal.setLength(0);
 			} else if (encodedSignal.toString().equals("---.")) {
 				decodedChar.add(SYMBOL.DAH);
 				encodedSignal.setLength(0);
 			} else if (encodedSignal.toString().equals("..")) {
 				intermediateResult.append(FROM_MORSE.get(decodedChar));
 				encodedSignal.setLength(0);
 				decodedChar.clear();
 			}
 		}
 		if (encodedSignal.toString().equals("-")) {
 			decodedChar.add(SYMBOL.DI);
 			encodedSignal.setLength(0);
 		} else if (encodedSignal.toString().equals("---")) {
 			decodedChar.add(SYMBOL.DAH);
 			encodedSignal.setLength(0);
 		}
 		intermediateResult.append(FROM_MORSE.get(decodedChar));
 		encodedSignal.setLength(0);
 		decodedChar.clear();
 
 		return INTERMEDIATE_CODER.decodeId(intermediateResult.toString());
 	}
 
 }
