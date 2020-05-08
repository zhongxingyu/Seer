   package com.rau.evoting.utils;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.rau.evoting.models.Answer;
 
 public class StringHelper {
 
	private static String delimiter = "\\.";
 	
 	public static String convertIntArrayToString(int[] array) {	
 		StringBuilder stringBuilder = new StringBuilder();
 		if (array.length > 0) {
 			stringBuilder.append(array[0]);
 			for (int i = 1; i < array.length; ++i) {
 				stringBuilder.append(delimiter);
 				stringBuilder.append(array[i]);
 			}
 		}
 		return stringBuilder.toString();
 	}
 
 	public static String converInttListToString(List<Integer> list) {
 		StringBuilder stringBuilder = new StringBuilder();
 		if (!list.isEmpty()) {
 			stringBuilder.append(list.get(0));
 			for (int i = 1; i < list.size(); ++i) {
 				stringBuilder.append(delimiter);
 				stringBuilder.append(list.get(i));
 			}
 		}
 		return stringBuilder.toString();
 	}
 	
 	public static String convertAnswersIdsToString(List<Answer> answers) {
 		StringBuilder stringBuilder = new StringBuilder();
 		if (!answers.isEmpty()) {
 			stringBuilder.append(answers.get(0).getId());
 			for (int i = 1; i < answers.size(); ++i) {
 				stringBuilder.append(delimiter);
 				stringBuilder.append(answers.get(i).getId());
 			}
 		}
 		return stringBuilder.toString();	
 	}
 
 	public static List<Integer> converStringToInttList(String s) {
 
 		List<Integer> list = new ArrayList<Integer>();

 		String[] res = s.split(delimiter);
 		for (int i = 0; i < res.length; ++i) {
 			Integer x;
 			try {
 				x = Integer.parseInt(res[i]);				
 			} catch (NumberFormatException ex) {
 				System.out.println(ex.getMessage());
 				continue;
 			}
 			list.add(x);
 		}
 
 		return list;
 	}
 		
 	public static String getSHA256hash(String s) {
 		String hash = org.apache.commons.codec.digest.DigestUtils.shaHex(s);
 		return hash;
 	}
 
 }
