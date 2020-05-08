 import java.util.ArrayList;
 import java.util.regex.Pattern;
 /**
  * <code>StringHolder</code> takes in strings from the input loop and
  * returns the proper file. Use the process method.
  * @author Keller Scholl keller.scholl@gmail.com
  * @since 29/3/12
  */
 public class StringHolder
 {	//Standard declaration of the ArrayLists that will contain the individual strings,
 	//divided by type, in the order that they were entered.
 	public ArrayList<String> orders;
 	public ArrayList<String> methods;
 	public ArrayList<String> imports;
 	//Default Constructor
 	public StringHolder(){
 		orders = new ArrayList<String>();
 		methods = new ArrayList<String>();
 		imports = new ArrayList<String>();
 	}
 	/**
	 * Takes in a string that should be added.
 	 * @param entry The string that is to be added to the file
 	 */
	void add(String entry){
 		entry.matches(" *import .*;");
 		if (entry.matches(" *import .*;"))
 			imports.add(entry);
 		//something, a word, a space, a word, open paren, 
 		//any number of inputs, close paren, white space/newlines,
 		//curly brace, stuff, curlybrace at end.
 		else if (entry.matches("}.*"))
 			methods.add(entry.substring(1));
 		else
 			orders.add(entry);		
 	}
	/**
	 * Returns the updated String that  should be compiled
	 * @return the new string that should be compiled into a file.
	 */
	String toFile(){
 		String base = "";
 		base = combine(imports, base);
 		base = base.concat(" public class " + RunCode.CLASS + "{\n");
 		base = combine(methods, base);
 		base = base.concat("public static void main(String[] args){\n");
 		base = combine(orders, base);
 		//check if method is void
 		base = base.concat("System.out.println( "+orders.get(orders.size()-1).substring(0,orders.get(orders.size()-1).length()-1)+".toString());\n");
 		base = base.concat("}\n}");
 		return base;
 	}
 	static private String combine(ArrayList<String> adder, String base){
 		Object[] hold = adder.toArray();
 		for (Object anImport : hold)
 			base = base.concat(anImport.toString() + "\n");
 		return base;
 	}
 	//Editin
 	public static void main(String[] args){
 		//Test code
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
