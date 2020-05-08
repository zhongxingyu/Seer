 package csuf.cs544.hw1.controller;
 
 public class Formatter {
 	
 	public static String EMPTY_STRING_ERROR = "Input cannot be empty",
						 PARSE_ERROR = "Please enter space-separated numers only";						 
 	
 	public Formatter() {}
 	
 	/**
 	 * Transmit space-separated string into array of numbers
 	 * @param input user inputed string
 	 * @return array of numbers
 	 * @throws Exception if string cannot be transformed into an array of numbers
 	 */
 	public int[] format(String input) throws Exception {
 
 		if (input.trim().length() == 0) {
 			throw new Exception(EMPTY_STRING_ERROR);
 		}
 		
 		String[] maybeNums = input.split(" ");
 		int[] nums = new int[maybeNums.length];
 	
 		try {
 			for(int i = 0; i < maybeNums.length; i++) {
 				nums[i] = Integer.parseInt(maybeNums[i]);
 			}
 		}
 		catch(Exception e) {
 			throw new Exception(PARSE_ERROR, e);
 		}
 		return nums;
 	}
 
 }
