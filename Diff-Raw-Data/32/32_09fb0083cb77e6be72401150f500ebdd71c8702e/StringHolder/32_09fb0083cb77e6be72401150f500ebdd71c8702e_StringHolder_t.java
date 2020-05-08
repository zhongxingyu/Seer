 import java.util.ArrayList;
 import java.util.regex.Pattern;
 
 public class StringHolder
 {
 	public ArrayList<String> orders;
 	public ArrayList<String> methods;
 	public ArrayList<String> imports;
 	
 	public StringHolder(){
 		orders = new ArrayList<String>();
 		methods = new ArrayList<String>();
 		imports = new ArrayList<String>();
 	}
 	void add(String entry){
 		entry.matches(" *import .*;");
 		if (entry.matches(" *import .*;"))
 			imports.add(entry);
 		//something, a word, a space, a word, open paren, 
 		//any number of inputs, close paren, white space/newlines,
 		//curly brace, stuff, curlybrace at end.
		else if (entry.matches("}.*"))//Pattern.compile(".*?[a-zA-Z0-9]* [a-zA-Z0-9]*\\(.*\\)[ \n\t]+\\{.*\\}", Pattern.DOTALL).matcher(entry).matches())
			methods.add(entry.substring(1));
 		else
 			orders.add(entry);		
 	}
 	String toFile(){
		String base = "";
 		base = combine(imports, base);
 		base = base.concat(" public class " + RunCode.CLASS + "{");
 		base = combine(methods, base);
		base = base.concat("public static void main(String[] args){");
 		base = combine(orders, base);
 		//check if method is void
		base = base.concat("System.out.println( "+orders.get(orders.size()-1).substring(0,orders.get(orders.size()-1).length()-1)+".toString());");
		base = base.concat("}}");
 		return base;
 	}
 	static private String combine(ArrayList<String> adder, String base){
 		Object[] hold = adder.toArray();
 		for (Object anImport : hold)
 			base = base.concat(anImport.toString());
 		return base;
 	}
 	public static void main(String[] args){
 		if (!(("(".matches("\\("))&&
 				("void add".matches("[a-zA-Z0-9]+ [a-zA-Z0-9]+"))&&
 				("(String entry)".matches("\\(.*\\)"))&&
 				("void add(String entry)".matches("[a-zA-Z0-9]+ [a-zA-Z0-9]+\\(.*\\)"))&&
 				("{  n\t]*\\.*\\')) methods.add(entry); else orders.add(entry);}".matches("\\{.+\\}"))&&
 				(Pattern.compile(".*?[a-zA-Z0-9]* [a-zA-Z0-9]*\\(.*\\)[ \n\t]+\\{.*\\}", Pattern.DOTALL).matcher(
 						"void add(String entry) { entry.matches(' *import .*;'); if (entry.matches(' *import .*;')) imports.add(entry); else if (entry.matches('.*?[a-zA-Z0-9]* [a-zA-Z0-9]*\\(.*\\)[ \n\t]*\\{.*\\}')) methods.add(entry); else orders.add(entry);}").matches())))
 			System.out.println("Failure!");
 	}
 }
