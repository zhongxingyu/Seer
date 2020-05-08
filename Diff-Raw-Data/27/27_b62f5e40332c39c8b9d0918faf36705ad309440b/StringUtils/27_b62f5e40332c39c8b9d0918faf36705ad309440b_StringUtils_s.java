 package edu.grinnell.csc207.turneral1.utils;
 
 public class StringUtils {
 
 
     /**
      * preconditions:
      * Takes an input string that may or may not have
      * the separating character. Also takes the separating
      * character. 
      * 
      * postconditions:
      * Returns an array of strings in which each string 
      * is what was before after and in between the given
      * separators.
      *
      */
     public static String[] splitAt(String inputString, char inputChar){
 	int slots = 0; //number of string slots
 	int len = inputString.length();
 	int count; //used for for-loop control to stay within string bounds
 	boolean frontOrEnd = false; //empty string at the end of beginning
 	char currentChar;
 
 	//set len slots to the number of strings needed to return
 	for (count = 0; count<len; count++){
 	    currentChar = inputString.charAt(count);
 	    if (currentChar == inputChar){
 		if(count == 0 || count == (len -1)){
 		    if (frontOrEnd == false)
 			slots++;
 		    frontOrEnd = true;
 		}//if
 		slots++;
 	    }//if
 	} // for
 
 	if (frontOrEnd == false)
 	    slots++;
 
 	String[] toReturn = new String[slots]; 
 
 	//special null case
 	if (slots == 0){
 	    toReturn[0] = inputString;
 	    return toReturn;
 	}
 
 	int stringCount = 0;
 	int goTo = 0;
 
 	//fills to return
 	while (stringCount < slots){
 	    goTo = inputString.indexOf(inputChar);
 	    //stay in bounds
 	    if (goTo != -1){
 		String newString = inputString.substring(0,goTo);
 		toReturn[stringCount] = newString;
 		//remove the unneeded part from input string
 		inputString = inputString.substring(goTo+1);
 	    }
 	    else{
 		toReturn[stringCount] = inputString;
 		break;
 	    }
 	    stringCount++;
 	}
 	//stay in bounds
 	if (goTo != -1){
 	    String newString = inputString.substring(0,goTo);	
 	    toReturn[stringCount] = newString;
 	}
 	return toReturn;
     }
 
 
     public static String[] splitCSV(String inputString){
 	char dividerChar = ',';
 	String dividerString = Character.toString(dividerChar);
 	int slots = 0; //number of string slots
 	int len = inputString.length();
 	int count; //used for for-loop control to stay within string bounds
 	boolean frontOrEnd = false; //empty string at the end of beginning
 	char currentChar = '0';
 	char lastChar = '0';
 
 	//set len slots to the number of strings needed to return
 	for (count = 0; count<len; count++){
 	    if (count != 0)
 		lastChar = currentChar;
 	    currentChar = inputString.charAt(count);
 	    if ((currentChar == dividerChar) && 
 		    ((count == 0) || (lastChar != '"'))){
 		if(count == 0 || count == (len -1)){
 		    if (frontOrEnd == false)
 			slots++;
 		    frontOrEnd = true;
 		}//if
 		slots++;
 	    }//if
 	}
 
 	if (frontOrEnd == false)
 	    slots++;
 
 	String[] toReturn = new String[slots]; 
 
 	String current = "";
 	String holder = "";
 	int stringIndex = 0;
 	String quote = "closed";
 	while (stringIndex<slots){
 	    if (inputString.length() > 0)
 		current = inputString.substring(0,1);
 	    if (inputString.length()<1){
 		toReturn[stringIndex] = holder;
 		break;
 	    }
 	    else if ((current.equals(dividerString)) && quote.equals("closed")){
 		inputString = inputString.substring(1); //get rid of comma
 		toReturn[stringIndex] = holder;
 		holder = "";
 		stringIndex++; //to load the next string next time
 	    }
 	    else if ((current.equals(dividerString)) && (quote.equals("open"))){
 		holder += current;
 		inputString = inputString.substring(1);
 	    }
 	    else if (inputString.length() > 0 && current.equals("\"") &&
 		    (inputString.charAt(inputString.indexOf(current) + 1) == '\"')){
 		inputString = inputString.substring(2);
 		holder += "\"";
 	    }
 	    else if (current.equals("\"")){
 		//openQuote = !(openQuote);
 		if (quote == "open")
 		    quote = "closed";
 		else if (quote.equals("closed"))
 		    quote = "open";
 		inputString = inputString.substring(1);
 	    }
 	    else {
 		holder+=current;
 		inputString = inputString.substring(1);
 	    }
 	}
 	return toReturn;
     }
 
     /* 
      * deLeet takes a string of "leet" text and produces the human-readable
      * version.
      * Pre-conditions: 
      *  None.
      * Post-conditions:
      *  The input string is returned, with the following replacements:
      *    * '1' is replaced with 'l'
      *    * '|3' is replaced with 'b'
      *    * '3' is replaced with 'e'
      *    * '+' is replaced with 't'
      *    * '0' is replaced with 'o'
      *    * '@' is replaced with 'a'
      *    * '|\\|' is replaced with 'n'
 	 */
 	public static String deLeet(String leet){
 		String normal = leet.replace("1","l");
 		normal = normal.replace("|3","b");
 		normal = normal.replace("3","e");
 		normal = normal.replace("+","t");
 		normal = normal.replace("0","o");
 		normal = normal.replace("@","a");
 		return normal.replace("|\\|","n");
 	} // deLeet
 
     /*
      * nameGame takes a string as input, and returns Ms. Ellis's verse using
      * the name.
      */
 	public static String nameGame(String input){
 		// find the first vowel occurence's index.
 	    	int i;
 		for (i = 0; i < input.length() && (input.charAt(i) != 'a' && 
 			input.charAt(i) != 'e' && input.charAt(i) != 'i' && 
 			input.charAt(i) != 'o' && input.charAt(i) != 'u'); i++);
 		String subName = input.substring(i);
 		return (String) (input + '!' + '\n' + input + ", " + input 
 			+ " bo " + 'B' + subName + " Bonana fanna fo F" 
 			+ subName + '\n' + "Fee fy mo M" + subName + 
 			", " + input + '!' + '\n');
 	} // nameGame
 
 	/*
 	 * fewestCoins recursively searches to find the fewest number of 
 	 * coins that produces value, and returns the coins' values in
 	 * an array of integers. 
 	 */
     public static int[] fewestCoins(int value){
 	int values[] = {2,7,11,54};
 	int coinArr[] = {0,0,0,0,Integer.MAX_VALUE};
 	
 	coinArr = fewestCoinsSearch(value,coinArr, values, 4);
 	
 	int coinTotal = coinArr[0]+coinArr[1]+coinArr[2]+coinArr[3];
 	int valueArr[] = new int[coinTotal];
 	
 	// builds the array of coins used (by denomination)
 	for(int i = 0, coins = 0; coins<coinTotal; i++)
 	    while(coinArr[i] > 0){
 		valueArr[coins] = values[i];
 	    	coinArr[i]--;
 	    	coins++;
 	    } // while
 	
 	return valueArr;
     } // fewestCoins
     
     /* 
      * fewestCoinsSearch is a helper procedure for recursive tree search.
      * Pre-conditions: 
      *   value is a positive integer.
      *   counts[0:length-1], values contain only non-zero positive integers.
      *   length denotes the number of coin types.
      * Post-conditions:
      *   When value == 0, returns counts. 
      *   counts[length] contains how many coins are used.
      */  
    public static int[] fewestCoinsSearch(int value, int a, int b, int c, 
	                                   int d, int[] values, int length){
	// had to convert at last minute - couldn't make coin array immutable.
	int bestCounts[] = {a, b, c, d};
	int counts[]
	
 	for(int i = length-1; i >= 0; i--){
 		if(value-values[i] == 0){
 		    // the coin is currently being considered
 		    int currentCoins = 1;
 		    for (int j = 0; j < length; j++)
 			currentCoins += counts[j];
 
 		    if(currentCoins < counts[length]){
 			counts[length] = currentCoins;
 			counts[i]++;
 			return counts;
 		    } // if
 		} // if
 		
		else if(value-values[i] > 0){
 		    int newCounts[] = counts;
 		    newCounts[i]++;
 		    newCounts = fewestCoinsSearch(value-values[i], newCounts, 
 			    values, length);
 		    if (newCounts[length] < bestCounts[length])
 			bestCounts = newCounts;
 		} // if
 	} // for
	
	System.out.println("BestCounts");
	for(int i = 0; i <= length; i++)
	    System.out.print(bestCounts[i] + " ");
	System.out.println();
	
 	return bestCounts;
     } // fewestCoinsSearch
 } // StringUtils
