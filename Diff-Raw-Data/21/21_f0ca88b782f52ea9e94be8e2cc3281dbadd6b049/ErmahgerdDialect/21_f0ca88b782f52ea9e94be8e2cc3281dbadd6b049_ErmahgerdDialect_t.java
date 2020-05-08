 package com.shootthesquare.ermahgerd;
 
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class ErmahgerdDialect implements Dialect {
 
 	public String translate(String input) {
 
		if (input.length()<1) return "";
 		String[] splitInput = input.split(" ");
		if (splitInput.length<1) return "";
 		
		List<String> myList = new ArrayList<String>();

 		for(String word: splitInput)
 		{
 			if (word.length() == 1)
 			{
 				myList.add(word.toUpperCase());
 				continue;
 			}
 			
 			word = word.toLowerCase();
 			word = specialCases(word);
 			word = dropLastVowel(word);
 			word = condenseVowels(word);
 			word = processSpecialRules(word);
 			word = condenseConsonants(word);
 			word = parseToErmahgerd(word);
 			
 			myList.add(word);
 		}
				
 		StringBuilder combined = new StringBuilder();
 		for(String word: myList)
 		{
 			combined.append(word);
 			combined.append(" ");
 		}
 		combined.deleteCharAt(combined.length() - 1);
 		
 		return combined.toString();
 	}
 	
 	public String specialCases(String word)
 	{
 		if (word.compareTo("goosebumps") == 0) return "GERSBERMS";
 		if (word.compareTo("awesome") == 0) return "ERSUM";
 		if (word.compareTo("banana") == 0) return "BERNERNER";
 		if (word.compareTo("bayou") == 0) return "BERU";
 		if (word.compareTo("favorite") == 0) return "FRAVRIT";
 		if (word.compareTo("favourite") == 0) return "FRAVRIT";
 		if (word.compareTo("long") == 0) return "LERNG";
 		if (word.compareTo("the") == 0) return "DA";
 		if (word.compareTo("they") == 0) return "DEY";
 		if (word.compareTo("we're") == 0) return "WER";
 		if (word.compareTo("you") == 0) return "U";
 		if (word.compareTo("you're") == 0) return "YER";
 		
 		if (word.charAt(0) == 'y')
 		{
 			String newYWord = "Y";
 			newYWord += word.substring(1);
 			return newYWord;
 		}
 		
 		return word;
 	}
 	
 	public String dropLastVowel(String word)
 	{
 		if (word.length() < 3)
 			return word;
 		
 		if (word.charAt(word.length() - 1) == 'y')
 			return word;
 		
 		if (isVowel(word.charAt(word.length() - 1)))
 			word = word.substring(0, word.length() - 1);
 			
 		return word;
 	}
 	
 	public String condenseVowels(String word)
 	{
 		StringBuilder condensedWord = new StringBuilder();
 		boolean onVowel = false;
 		
 		
 		for(char myChar: word.toCharArray())
 		{
 			if(!isVowel(myChar))
 			{
 				onVowel = false;
 				condensedWord.append(myChar);
 			}
 			else
 			{
 				if (onVowel)
 					condensedWord.deleteCharAt(condensedWord.length() - 1);
 		
 				onVowel = true;	
 				condensedWord.append(myChar);
 			}
 			
 		}
 		
 		return condensedWord.toString();
 	}
 	
 	public String condenseConsonants(String word)
 	{
 		StringBuilder condensedWord = new StringBuilder();
 		//Assuming that the string should always be > 1 character
 		condensedWord.append(word.charAt(0));
 		
 		for(int i = 1; i < word.length(); i++)
 		{
 			char myChar = word.charAt(i);
 			
 			if (isVowel(myChar))
 			{
 				condensedWord.append(myChar);
 			}
 			else
 			{
 				if(myChar != condensedWord.charAt(condensedWord.length() - 1))
 					condensedWord.append(myChar);
 			}
 		}
 		
 		return condensedWord.toString();
 	}
 	
 	public String processSpecialRules(String word)
 	{
 		StringBuilder newWord = new StringBuilder();
 	
 		if(word.length() < 4)
 			return word;
 		
 		for(int i = 0; i < word.length() - 2; i++)
 		{
 			boolean found = false;
 			
 			if(isVowel(word.charAt(i)))
 				if(!isVowel(word.charAt(i+1)))
 					if(word.charAt(i+2)=='e')
 						if(!isVowel(word.charAt(i+3)))
 						{
 							//newWord.append(word.charAt(i) + word.charAt(i + 1) + word.charAt(i + 3));
 							newWord.append(word.charAt(i));
 							newWord.append(word.charAt(i + 1));
 							newWord.append(word.charAt(i + 3));
 							i = i + 3;
 							found = true;
 						}
 			
 			if(!found)
 				newWord.append(word.charAt(i));
 			
 			if(i == word.length() - 3)
 				{
 					newWord.append(word.charAt(i + 1));
 					newWord.append(word.charAt(i + 2));
 				}
 		}
 		
 		return newWord.toString();
 	}
 	
 	//1- vPvD = ERPED
 	//2- LOW = LO
 	//3- vNG = IN
 	//4- Mv = MAH
 	//5- vH = ER
 	//6- OW = ER
 	//7- v = ER
 	public String parseToErmahgerd(String word)
 	{
 		StringBuilder newWord = new StringBuilder();
 		
 		int i = 0;
 		while(i < word.length())
 		{
 			int charsRemaining = word.length() - i;
 			
 			String wordSubString = word.substring(i, Math.min(i + 4, i + charsRemaining));
 			
 			ParseResults result = rule1(wordSubString);
 			if(!result.Parsed) result = rule2(wordSubString);
 			if(!result.Parsed) result = rule3(wordSubString);
 			if(!result.Parsed) result = rule4(wordSubString);
 			if(!result.Parsed) result = rule5(wordSubString);
 			if(!result.Parsed) result = rule6(wordSubString);
 			if(!result.Parsed) result = rule7(wordSubString);
 			
 			if(result.Parsed)
 			{
 				i += result.CharsProcessed;
 				newWord.append(result.Results);
 			}
 			else
 			{
 				newWord.append(word.substring(i, i + 1).toUpperCase());
 				i++;
 			}
 
 		}
 		
 		return newWord.toString();
 	}
 	
 	private ParseResults rule1(String word)
 	{
 		ParseResults result = new ParseResults();
 		result.CharsProcessed = 4;
 		
 		if(word.length() < result.CharsProcessed)
 			return result;
 		
 		if(isVowel(word.charAt(0)) && word.charAt(1) == 'p' && isVowel(word.charAt(2)) && word.charAt(3) == 'd')
 		{
 			result.Parsed = true;
 			result.Results = "ERPED";
 		}
 		
 		return result;
 	}
 	
 	private ParseResults rule2(String word)
 	{
 		ParseResults result = new ParseResults();
 		result.CharsProcessed = 3;
 		
 		if(word.length() < result.CharsProcessed)
 			return result;
 		
 		if(word.substring(0, 3).compareTo("low") == 0)
 		{
 			result.Parsed = true;
 			result.Results = "LO";
 		}
 		
 		return result;
 	}
 	
 	private ParseResults rule3(String word)
 	{
 		ParseResults result = new ParseResults();
 		result.CharsProcessed = 3;
 		
 		if(word.length() < result.CharsProcessed)
 			return result;
 		
 		if(isVowel(word.charAt(0)) && word.substring(1, 3).compareTo("ng") == 0)
 		{
 			result.Parsed = true;
 			result.Results = "IN";
 		}
 		
 		return result;
 	}
 	
 	private ParseResults rule4(String word)
 	{
 		ParseResults result = new ParseResults();
 		result.CharsProcessed = 2;
 		
 		if(word.length() < result.CharsProcessed)
 			return result;
 		
 		if(word.charAt(0) == 'm' && isVowel(word.charAt(1)))
 		{
 			result.Parsed = true;
 			result.Results = "MAH";
 		}
 		
 		return result;
 	}
 	
 	private ParseResults rule5(String word)
 	{
 		ParseResults result = new ParseResults();
 		result.CharsProcessed = 2;
 		
 		if(word.length() < result.CharsProcessed)
 			return result;
 		
 		if(isVowel(word.charAt(0)) && word.charAt(1) == 'h')
 		{
 			result.Parsed = true;
 			result.Results = "ER";
 		}
 		
 		return result;
 	}
 	
 	private ParseResults rule6(String word)
 	{
 		ParseResults result = new ParseResults();
 		result.CharsProcessed = 2;
 		
 		if(word.length() < result.CharsProcessed)
 			return result;
 		
 		if(word.substring(0, 2).compareTo("ow") == 0)
 		{
 			result.Parsed = true;
 			result.Results = "ER";
 		}
 		
 		return result;
 	}
 	
 	private ParseResults rule7(String word)
 	{
 		ParseResults result = new ParseResults();
 		result.CharsProcessed = 1;
 		
 		if(word.length() < result.CharsProcessed)
 			return result;
 		
 		if(isVowel(word.charAt(0)))
 		{
 			result.Parsed = true;
 			result.Results = "ER";
 		}
 		
 		return result;
 	}
 
 	public boolean isVowel(char myChar)
 	{
 		return (myChar == 'a' || myChar =='e' || 
 		   myChar =='i' || myChar =='o' || 
 		   myChar =='u' || myChar =='y');
 	}
 
 }
