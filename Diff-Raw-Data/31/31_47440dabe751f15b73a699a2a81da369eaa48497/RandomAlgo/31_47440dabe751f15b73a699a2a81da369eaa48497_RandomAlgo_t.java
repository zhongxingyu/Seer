 package secret.algo.random;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 
 public class RandomAlgo {
 
 	public char[] getRandomBinary(int scopeMax) {
 		ArrayList<Float> randomFloatList = new ArrayList<Float>();
 		randomFloatList = (ArrayList<Float>) getRandomFloatList(scopeMax - 1);
 
 		float seed = randomFloatList.get(0);
 
 		Collections.sort(randomFloatList);
 
 		int randomNumber = randomFloatList.indexOf(seed);
 		String randomBinaryString = Integer.toBinaryString(randomNumber);
 		int requiredBinaryLength = Integer.toBinaryString((scopeMax - 1)).length();
		int currentRandomStringLength = randomBinaryString.length();
		
		while (randomBinaryString.length() < requiredBinaryLength) {
			for (int i = 0; i < (requiredBinaryLength - currentRandomStringLength); i++) {
 				randomBinaryString = "0" + randomBinaryString;
 			}
 		}
		
 		char[] randomBinaryNumberArray = randomBinaryString.toCharArray();
 
 		return randomBinaryNumberArray;
 	}
 	
 	public int getRandomDecimal(int scopeMax) {
 		ArrayList<Float> randomFloatList = new ArrayList<Float>();
 		randomFloatList = (ArrayList<Float>) getRandomFloatList(scopeMax - 1);
 
 		float seed = randomFloatList.get(0);
 
 		Collections.sort(randomFloatList);
 		
 		return randomFloatList.indexOf(seed);
 	}
 
 	private List<Float> getRandomFloatList(int listScope) {
 		Random random = new Random();
 		List<Float> randomFloatList = new ArrayList<Float>();
 
 		for (int i = 0; i < listScope;) {
 			randomFloatList.add(random.nextFloat());
 			i++;
 		}
 		return randomFloatList;
 	}
 }
