 package Generator.Character;
 
 import java.util.ArrayList;
 
 import Generator.Tokenizer;
 
 public class CharacterClassTest 
 {
 	public static void main(String[] args) 
 	{
 		String regex1 = "$NAME [abc]";
 		String regex2 = "[abc]";
 				
 		String regex3 = "$NAME [kswl-q]";
 		String regex4 = "[oolav-zldg]";
 	
 		String regex5 = "$EXCLUDE [^abc] IN $NAME";
 		String regex6 = "$EXCLUDE [^abc] IN [a-z]";
 		
 		String regex7 = "[^skn] IN $NAME"; 
 		String regex8 = "[^owq] IN [a-z]";
 		
 		String regex9 = "[a-dc-wA-Jj-u]";
 		String regex10 = "[a-dXXc-wYYA-Jj-u]";
 		
 		ArrayList<String> all = new ArrayList<String>();
 		all.add(regex1);
 		all.add(regex2);
 		all.add(regex3);
 		all.add(regex4);
 		all.add(regex5);
 		all.add(regex6);
 		all.add(regex7);
 		all.add(regex8);
 		all.add(regex9);
 		all.add(regex10);
 		
 		for(String s: all)
 		{
 			Tokenizer t = new Tokenizer(s);
 			CharacterClassFactory ccf = new CharacterClassFactory(t);
 			CharacterClass cc = new CharacterClass("$NAME");
 			cc.acceptBoundary('a', 'z');
 			ccf.map.put("$NAME", cc);
 			ccf.build(s).print();
 		}
 
 		System.out.println("\n\n=====================================================================================================");
 	}
 }
