 package phase2;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 public class HomeStarRunner2 {
 
 	public static void main(String[]args) throws IOException
 	{		
 		LL1parser parse = new LL1parser();
 		
		parse.inputFile("testGrammar.txt");
 		parse.createFirstSets();
 		parse.createFollowSets();
 		System.out.println("First sets: " + parse.getFirstSets());
 		System.out.println("Follow sets: " + parse.getFollowSets());
 		//parse.finishParseTable();
 		/*
 	    HashMap<String, HashSet<String>> map = parse.getFollowSets();
 		Set<String> keys = map.keySet();
 		parse.createFollowSets();
 	    HashMap<Token, HashSet<Token>> map2 = parse.getFirstSets();
 		Set<Token> keys2 = map2.keySet();
 
 		System.out.println("First Sets");
 
 	    
 	    for(Token key : keys2)
 		{
 			System.out.println(key.getValue() + " : " + setToString2(map2.get(key)));
 		}	
 		
         
         
         
 		System.out.println("\nFollow Sets");
 
         for(String key : keys)
 		{
 			System.out.println(key + " : " + setToString(map.get(key)));
 		}		
 		
 		//FinalDriver fd = new FinalDriver("phase2/grammar.txt", "phase2/script.txt", "phase2/token_spec.txt", "phase2/output");
 		//fd.start();*/
 	}
 		
 		
 		
 		
 		
 	
 	public static String setToString(Set<String> set)
 	{
 		String ret="";
 		
 		for(String s : set)
 		{
 			String s2=s;
 			ret+=s2;
 			ret+=" ";
 		}	
 		return ret;
 	}	
 	
 	public static String setToString2(Set<Token> set)
 	{
 		String ret="";
 		
 		for(Token s : set)
 		{
 			String s2=s.getValue();
 			ret+=s2;
 			ret+=" ";
 		}	
 		return ret;
 	}	
 }
