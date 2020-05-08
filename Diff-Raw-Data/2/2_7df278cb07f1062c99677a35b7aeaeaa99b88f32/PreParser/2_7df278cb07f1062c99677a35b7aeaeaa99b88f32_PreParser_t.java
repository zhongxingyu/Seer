 package com.awesomecat.jslogger;
 
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 public class PreParser {
 	
 	/**
 	 * Allowed flags by our regular expression evaluator
 	 */
 	public static final String allowedFlags = "sim";
 
 	/**
 	 * Is the potential flag valid?
 	 * @param potentialFlag The length 1 string to check
 	 * @return True if valid, false otherwise
 	 */
 	public static boolean isValidFlag(String potentialFlag){
 		return potentialFlag.length() == 1 && allowedFlags.indexOf(potentialFlag) >= 0;
 	}
 
 	/**
 	 * Converts the string flags to their pattern constants
 	 * @param flags
 	 * @return
 	 */
 	public static int convertFlagsToConstants(ArrayList<String> flags){
 		int ret = 0;
 		for(String s : flags){
 			if(s.equals("i")){
 				ret |= Pattern.CASE_INSENSITIVE;
 			} else if(s.equals("m")){
 				ret |= Pattern.MULTILINE;
 			} else {
 				ret |= Pattern.DOTALL;
 			}
 		}
 		return ret;
 	}
 
 	/**
 	 * Determines if a given regular expression is strict enough to meet out requirements.
 	 * @param re Format as: /^ABC, 123$/gim
 	 * @return
 	 */
     public static boolean validRegularExpression(String re) {
         /*
          * Steps before building:
          * 1) We need to determine which flags we support (i.e. /hi/gim, the "gim" part)
          * 
          * During evaluation:
          * 1) If null, return false
          * 2) Strip off valid flags from the end. If invalid flag is found, return false
          * 3) Strip off the / at the start and / at the end.
          * 4) If first character is not start anchor (^), return false
          * 5) If last character is not end anchor ($), return false
          * 6) Strip anchors.  At this point, we have our entire regular expression to validate.
          * 7) We need to not allow huge wildcards... i.e. we should ban the use of *, +, and {#,} (this last one leaves open arbitrarily long messages)
          * 		NOTE: We don't ban {,#} because they are willingly implementing a max limit
          * 		NOTE2: Be careful to not ban \* or \+... those are valid escaped characters
          * 
          * 8) At this point, I think everything should be valid and not overly inefficient
          * 9) Make sure it is a valid expression based on the regular expression validator(CHRIS)
          * 
          * 
          * NOTE: What about if they escape that last /?   (i.e. /hi\/  wouldn't be valid)
          */ 
     	if (re == null || re.isEmpty()) {
     		  return false;
     	}
     	int flag_index=0;
    	ArrayList<String> flags = new ArrayList<String>();
     	for(int i= (re.length()-1);i>=0; i-- ){
     		String cur_char = re.substring(i,i+1);
     		System.out.println("cur_char: "+cur_char); //DEBUG
     		if(cur_char.equals("/")){
     			flag_index = i;
     			System.out.println("break");
     			break;
     		}
     		if(!isValidFlag(cur_char)){
     			System.out.println("!isValidFlag(cur_char)");
     			return false;
     		}
     		else{
     			flags.add(cur_char);
     		}
     	}
     	if(flag_index == 0){
     		System.out.println("flag_index == 0");
     		return false;
     	}
     	String flagless = re.substring(0, flag_index+1);
     	System.out.println("flagless: "+flagless);
     	System.out.println(flagless.substring(0,1));
     	if(!flagless.substring(0,1).equals("/") 
     		|| !flagless.substring(flagless.length()-1,flagless.length()).equals("/")
     		|| flagless.substring(flagless.length()-2,flagless.length()-1).equals("\\")){
     		System.out.println("flagless");
     		return false;
     	}
     	String no_slash = flagless.substring(1,flagless.length()-1); //is this right?
     	if(!no_slash.substring(0,1).equals("^") 
     		|| !no_slash.substring(no_slash.length()-1,no_slash.length()).equals("$")
     		|| no_slash.substring(no_slash.length()-2,no_slash.length()-1).equals("\\")){
     		System.out.println("no_slash");
     		return false;
     	}
     	String no_anchor = no_slash.substring(1,no_slash.length()-1); //is this right?
     	for(int i= 0;i<no_anchor.length(); i++ ){
     		String cur_char = no_anchor.substring(i,i+1);
     		String before_char = no_anchor.substring(i,i+1);
     		if(i!=0){
     			before_char = no_anchor.substring(i-1,i);
     		}
     		
     		if((cur_char.equals("+") || cur_char.equals("*")) && !before_char.equals("\\")){
     			System.out.println("no_anchor");
     			return false;
     		}
     		if(cur_char.equals("{") && !before_char.equals("\\")){
     			//2 bad cases: {,} and {#,}
     			if(no_anchor.substring(i,i+3).equals("{,}")){
     				System.out.println("no_anchor.substring(i,i+3) == \"{,}\"");
     				return false;
     			}
     			Pattern p = Pattern.compile("\\{\\d+,\\}");
     			if(matchesPattern(p,no_anchor.substring(i))){ //might be slow
     				System.out.println("matchesPattern(p,no_anchor.substring(i))");
     				return false;
     			}
     		}
     	}
         try {
         	int flag_value = convertFlagsToConstants(flags);
             Pattern.compile(no_slash, flag_value); //doesn't handle flags
         } catch (PatternSyntaxException exception) {
             return false;
         }
     	return true;
     }
     //ADDED(Aaron)
     private static boolean matchesPattern(Pattern p,String sentence) {
         Matcher m = p.matcher(sentence);
 
         if (m.find()) {
           return true;
         }
 
         return false;
       }
 
     /**
      * Determine if inputted size is valid.
      * @param input
      * @return
      */
     public static boolean validWindowSize(String input){
     	try {
 	    	int i = Integer.parseInt(input);
 	    	return i > 0;
     	} catch (Exception e){
     		return false;
     	}
     }
 }
