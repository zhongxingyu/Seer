 package grammar;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Grammar {
 	private List<String> rules;
 	
 	public Grammar(String...strings){
 		rules=new ArrayList<>();
 		for(String rule :strings)
 			rules.add(rule);
 	}
 	public List<String> getRules(){
 		return rules;
 	}
	public static void main(String[] args){
 		
		Grammar a=new Grammar("S = ()","S = (S)","S = SS");
 		
		System.out.println(a.check("(()(()))"));
		
		Grammar b=new Grammar("S = x | y | z","S = S + S","S = S - S","S = S * S","S = S / S","S = (S)");
 		
 		System.out.println(b.check("(x + y) * x - z * y / (x + x)"));
 		System.out.println(b.check("(xx - zz + x / z)"));
 		System.out.println(b.check("x + y * x - z * y / x + x"));
 		
 	}
 	
 }
