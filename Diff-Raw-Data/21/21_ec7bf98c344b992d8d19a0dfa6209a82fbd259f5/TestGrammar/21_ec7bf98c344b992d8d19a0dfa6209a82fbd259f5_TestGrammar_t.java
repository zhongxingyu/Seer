 import java.io.File;
 import java.util.Scanner;
 
 
 public class TestGrammar {
 	public static void main(String[] args) throws Exception {
		Grammar g = new Grammar(new Scanner(new File("grammars/minire.txt")), new Scanner(new File("specs/minire.txt")));
 		
 		System.out.println(g.toString());
 		g.calculateFirstSets();
 		g.calculateFollowSets();
 		for(Variable variable : g.getVariables()){
 			System.out.print("First("+variable+") = ");
 			System.out.println(variable.getFirst());
 		}
 		System.out.println();
 		for(Variable variable : g.getVariables()){
 			System.out.print("Follow("+variable+") = ");
 			System.out.println(variable.getFollow());
 		}
 		
 		ParseTable pt = new ParseTable(g);
 		System.out.println(pt.toString());
 		
		pt.walk((new Scanner(new File("scripts/script2.txt"))));
 	}
 }
