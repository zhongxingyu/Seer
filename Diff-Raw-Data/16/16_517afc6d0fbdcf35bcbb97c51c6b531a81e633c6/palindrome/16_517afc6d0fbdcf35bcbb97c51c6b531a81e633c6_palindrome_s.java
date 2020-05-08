 	/**
 	 * palindrome
 	 *
 	 * 		returns if a string is a palindrome
 	 *
 	 * Kyle Vigil
 	 */
 class palindrome {
 
 	/**
 	 * main
 	 *
 	 * 		prints out if the string is a boolean
 	 *
 	 *@param a string
 	 */
 	public static void main(String[] args) {
 		if(iterativePalindrome(args[0]) == true){
 			System.out.println("The word " + args[0] + " is a palendrome");
 		} else {
 			System.out.println("The word " + args[0] + " is NOT a palendrome");
 		}
 	}
 
 	/**
 	 * iterativePalindrome
 	 *
 	 * 		returns if the string is a palindrome
 	 *
 	 * @param a string
 	 * @return true or false if it is a palindrome
 	 */
 	public static boolean iterativePalindrome(String pal){
 		for(int i=1; i<pal.length(); i++){
 			if(pal.charAt(i-1) != pal.charAt(pal.length() - i)){
 				return false;
 			}
 		}
 		return true;
 	}
 
 
 	/**
 	 * recursivePalindrome
 	 *
 	 * 		returns if the string is a palindrome
 	 *
 	 * @param a string
 	 * @return true or false if it is a palindrome
 	 */
	public static boolean recursivePalindrome(String pal){
 		if(pal.length() < 2){
			return true;
		} 
		return recursivePalindrome(pal.substring(1,pal.length()-2));
 	}
}
