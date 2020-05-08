 package prettyprinter;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 
 import org.antlr.runtime.RecognitionException;
 
 import utils.Logger;
 
 import analyze.WhileTree;
 
 public class PrettyPrinter {
 
 	private WhileTree whileTree;
 	private Formater formater;
 	private HashMap<String, Integer> map;
 	
 	/** Default indentation */
 	private int defIndent = 4;
 	
 	/**
 	 * Constructor
 	 * @param map, it contains the application parameters :
 	 * defIndent Default indentation (!)
 	 * whileIndent Indentation for WHILE struct
 	 * forIndent Indentation for FOR struct
 	 * ifIndent Indentation for IF struct
 	 */
 	public PrettyPrinter(HashMap<String, Integer> map) {
 		this.map = map;
 	}
 	
 	/**
 	 * Create the formater with the parameters contained in the map
 	 * @pre We need to create the tree before the Formater
 	 * @return true if it works
 	 */
	public boolean createFormater() {	
		int forIndent, whileIndent, ifIndent;
 		
		this.defIndent = this.initIndent("defIndent");
		forIndent = this.initIndent("forIndent");
		whileIndent = this.initIndent("whileIndent");
		ifIndent = this.initIndent("ifIndent");
 		
 		try {
 			this.formater = new Formater(this.defIndent, whileIndent, forIndent, ifIndent, whileTree);
 			return true;
 		} catch (Exception e) {
 			Logger.log("prettyPrinter.log", "Pretty Printer : Error when creating Formater");
 			return false;
 		}
 	}
 	
 	/**
 	 * Write the indented code in a file
 	 * @param filename, it writes the code here
 	 */
 	public boolean write(String filename) {
 		try {
 			BufferedWriter buff = new BufferedWriter(new FileWriter(filename));
 			buff.write(formater.getIndentedCode());
 			buff.close();
 			return true;
 		} catch (IOException e) {
 			Logger.log("prettyPrinter.log", "PrettyPrinter : Error when write in " + filename);
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	/**
 	 * Init the indentation parameters
 	 * @param indentName, parameters name in the map
 	 * @param indent, value of the indentation parameter
 	 */
	private int initIndent(String indentName) {
 		if(this.map.containsKey(indentName) && this.map.get(indentName) >= 0)
			return this.map.get(indentName);
 		else
			Logger.log("prettyPrinter.log", "Pretty Printer : Error " + indentName + " < 0 \n Default value : " + this.defIndent);
		return this.defIndent;
 	}
 	
 	/**
 	 * Create the WhileTree
 	 * @param filename the file read by the WhileTree.
 	 * @return true, if it works
 	 */
 	public boolean createWhileTree(String filename) {
 		try {
 			this.whileTree = new WhileTree(filename);
 			return true;
 		} catch (IOException e) {
 			Logger.log("prettyPrinter.log", "PrettyPrinter : Error when creating WhileTree \n File " + filename + " doesn't exist");
 			return false;
 		} catch (RecognitionException e) {
 			Logger.log("prettyPrinter.log", "PrettyPrinter : Error when creating WhileTree \n");
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	
 }
