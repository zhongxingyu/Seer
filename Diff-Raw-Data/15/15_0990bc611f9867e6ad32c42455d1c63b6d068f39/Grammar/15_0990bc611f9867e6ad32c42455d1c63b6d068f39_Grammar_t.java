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
	public static void main(String[] args) throws Exception{
 		
		Grammar a=new Grammar("S = ()","S = (S)","S = SS","S = A","A = 0 | 1","A = ()"," = E");
		System.out.println(a.check("()"));
		System.out.println(a.check("((1)((0)))"));
 		
		Grammar b=new Grammar("S = x | y | z","S = S + S","S = S - S","S = S * S","S = S / S","S = (S)"," = E");
 		
 		System.out.println(b.check("(x + y) * x - z * y / (x + x)"));
 		System.out.println(b.check("(xx - zz + x / z)"));
 		System.out.println(b.check("x + y * x - z * y / x + x"));
 		
		Grammar c=new Grammar("C = 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9","D = 0 | C","T = DT | E","S = CT");
		System.out.println(c.check("12456"));
 	}
 	
 }
