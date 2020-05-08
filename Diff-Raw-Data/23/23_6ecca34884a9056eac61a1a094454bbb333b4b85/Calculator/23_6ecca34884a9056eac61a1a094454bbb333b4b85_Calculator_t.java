 import java.util.ArrayList;
 import java.util.Scanner;
 
 public class Calculator {
 	
 
 	public static enum Operator{
 		PLUS ("+",1),TIMES ("*",2),DIVIDE ("/",3);
 		
 		private String key;
 		private int num;
 		Operator(String s, int i) {
 			key=s;
 			num=i;
 		}
 		String getStr() { return key; }
 		public String toString() { return key; }
 		int getNum() { return num; }
 	};
 	
 	public static String fix(String s)
 	{
 	String out = "0+"+s;
 		
 	return out;
 	}
	public static String fixDoubleNeg(String s)
	{
		for(int i=1;i<s.length();i++)
			if(s.substring(i-1,i+1).equals("--"))
				s = s.substring(0,i-1)+"+"+s.substring(i+1);
		return s;
	}
 	public static String fixMinus(String s)
 	{
 		for(int i=1;i<s.length();i++){
			if(s.substring(i,i+1).equals("-") && !(hasOps(s.substring(i-1,i))))
 				s = s.substring(0,i)+"+"+s.substring(i);
 		}
 		return s;
 	}
 	
 	public static double runOp(double a, double b, Operator o)
 	{
 		switch(o.getNum()){
 		case 1:
 			return a+b;
 		case 2:
 			return a*b;
 		case 3:
 			return a/b;
 		}
 		
 		return -1;
 	}
 	
 	
 	public static String result(String s)
 	{
		s = fix(fixMinus(fixDoubleNeg(s)));
		System.out.println("postfix: "+s);
 		ArrayList<Double> numbers = getNumbers(s);
 		ArrayList<Operator> ops = getOps(s);
 		
 		while(ops.size()>0){
 			double a = numbers.remove(0);
 			double b = numbers.remove(0);
 			numbers.add(0, runOp(a,b,ops.remove(0)));
 		}
 		
 		
 		return ""+numbers.get(0);
 		
 	}
 	public static boolean hasOps(String s){
 		for(Operator o:Operator.values())
 			if(s.indexOf(o.getStr())!=-1)
 				return true;
 		return false;
 	}
 	public static int nextOpIndex(String s){
 		int out = -1;
 		for(Operator o:Operator.values())
 			if(s.indexOf(o.getStr())!=-1)
 				if(out==-1 || s.indexOf(o.getStr())<out)
 					out = s.indexOf(o.getStr());
 		return out;
 	}
 	public static ArrayList<Double> getNumbers(String s)
 	{
 		ArrayList<Double> out = new ArrayList<Double>();
 		while(s.length()>0){
 			System.out.println(s+" "+out);
 			int nextOp = hasOps(s)?nextOpIndex(s):s.length();
 			out.add(Double.parseDouble(s.substring(0,nextOp)));
 			s = hasOps(s)?s.substring(nextOp+1):"";
 		}
 		System.out.println(s+" "+out);
 		return out;
 	}
 	public static Operator whichOp(String s){
 		for(Operator o:Operator.values())
 			if(o.getStr().equals(s))
 				return o;
 		return null;
 	}
 	public static ArrayList<Operator> getOps(String s)
 	{
 		System.out.println("scannin ops");
 		ArrayList<Operator> out = new ArrayList<Operator>();
 		while(hasOps(s)){
 			System.out.println(s+" "+out);
 			int index = nextOpIndex(s);
 			Operator o = whichOp(s.substring(index,index+1));
 			out.add(o);
 			s = s.substring(index+1);
 		}
 		return out;
 	}
 	
 	
 }
