 package com.github.barcodescanner.barcode;
 
 import android.annotation.SuppressLint;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 public class BCGenerator {
 	//String used to represent bits in a barcode
 	private String[] barcodeNumbers;
 
 	public BCGenerator() {
 		barcodeNumbers = new String[10];
 		barcodeNumbers[0] = "3211";
 		barcodeNumbers[1] = "2221";
 		barcodeNumbers[2] = "2122";
 		barcodeNumbers[3] = "1411";
 		barcodeNumbers[4] = "1132";
 		barcodeNumbers[5] = "1231";
 		barcodeNumbers[6] = "1114";
 		barcodeNumbers[7] = "1312";
 		barcodeNumbers[8] = "1213";
 		barcodeNumbers[9] = "3112";
 	}
 
 	/**
 	 * Generates a new array representing the length of each part in the barcode
 	 * 
 	 * @param lines segments of 0s and 1s containing a barcode found in BCLocator,
 	 * 
 	 * @return the new array
 	 */
 	public List<Integer> generate(List<List<Integer>> lines) {
 		List<List<Integer>> lineHolder = new ArrayList<List<Integer>>();
 		List<Integer> unNormalized = new ArrayList<Integer>();
 		List<Integer> newSumList = new ArrayList<Integer>();
 
 		int height = lines.size();
 		for (int j = 0; j < lines.size(); j++) {
 			List<Integer> line = lines.get(j);
 			unNormalized.clear();
 			int count = 1;
 			for (int i = 0; i < line.size() - 1; i++) {
 				if (line.get(i) == line.get(i + 1)) {
 					count++;
 				} else {
 					unNormalized.add(count);
 					count = 1;
 				}
 			}
 			unNormalized.add(count);
 			lineHolder.add(unNormalized);
 		}
 
 		int width = lineHolder.get(0).size();
 		List<Integer> sumList = new ArrayList<Integer>();
 
 		for (int i = 0; i < width; i++) {
 			sumList.clear();
 			for (int j = 0; j < height; j++) {
 				sumList.add(lineHolder.get(j).get(i));
 			}
 			newSumList.add(mostOccuringNumber(sumList));
 		}
 
 		return newSumList;
 	}
 	/**
 	 * Given an ArrayList of Integers, returns the Integer that is occurs most
 	 * often in the given ArrayList.
 	 * 
 	 * @param numberList
 	 * @return most represented number
 	 */
 	@SuppressLint("UseSparseArrays")
 	private Integer mostOccuringNumber(List<Integer> numberList) {
 		HashMap<Integer, Integer> hashMap = new HashMap<Integer, Integer>();
 		
 		for (int i = 0; i < numberList.size(); i++) {
 			if (hashMap.containsKey(numberList.get(i))) { 
 				// key has already been mapped, so increase value by 1
 				hashMap.put(numberList.get(i),
 						hashMap.get(numberList.get(i)) + 1);
 			} else { 
 				// key hasn't been mapped, so set it's value to 1
 				hashMap.put(numberList.get(i), 1);
 			}
 		}
 		
 		int highestValue = 0, highestKey = 0;
 		for (int i = 0; i < numberList.size(); i++) {
 			if (hashMap.get(numberList.get(i)) > highestValue) {
 				highestValue = hashMap.get(numberList.get(i));
 				highestKey = numberList.get(i);
 			}
 		}
 		
 		return highestKey;
 	}
 
 	/**
 	 * Normalizes the array given in the generate method
 	 * 
 	 * @param unNormalized, the list of integers (the barcode data) to be normalized
 	 * @return a string, containing the normalized data this string is used as key in the database
 	 */
 	public String normalize(List<Integer> unNormalized) {
 		StringBuffer tempStringBuffer = new StringBuffer();
 		StringBuffer finalStringBuffer = new StringBuffer();
 		String tempString = "";
 		int leastDistance = 999999;
 		int index = -1;
 
 		if (unNormalized == null) {
 			return "No barcode data is set";
 		} else {
 			int lengths = unNormalized.get(0) + unNormalized.get(1)
 					+ unNormalized.get(unNormalized.size() - 1)
 					+ unNormalized.get(unNormalized.size() - 2);
 			float division = lengths / 4;
 
 			for (int i = 0; i < unNormalized.size(); i++) {
 				int value = Math.round(unNormalized.get(i) / division);
 				tempStringBuffer.append(value);
 			}
 
			// Delete the middle "guard" bars.
 			tempStringBuffer.delete(28, 33);
 
 			for (int i = 3; i < tempStringBuffer.length() - 2; i += 4) {
 				leastDistance = 9999;
 				index = -1;
 
 				if (tempStringBuffer.length() >= i + 4) {
 					tempString = tempStringBuffer.substring(i, i + 4);
 				}
 
 				for (int j = 0; j < barcodeNumbers.length; j++) {
 					int distance = compare(barcodeNumbers[j], tempString);
 
 					if (distance < leastDistance) {
 						leastDistance = distance;
 						index = j;
 					}
 				}
 				finalStringBuffer.append(index);
 			}
 		}
 
 		return finalStringBuffer.toString();
 	}
 
 	/**
 	 * Compares two strings and decides if this is the same product
 	 * 
 	 * @param one
 	 * @param two
 	 * @param threshhold
 	 * @return integer representing the distance between the strings, -1 for undefined
 	 */
 	public Integer compare(String one, String two) {
 		int distance = 0;
 		if (one.length() == two.length()) {
 			int current;
 			for (int i = 0; i < one.length(); i++) {
 				current = Integer.parseInt(Integer.toString(one.charAt(i)))
 						- Integer.parseInt(Integer.toString(two.charAt(i)));
 				distance += Math.abs(current);
 			}
 		} else {
 			distance = -1;
 		}
 		return distance;
 	}
 }
