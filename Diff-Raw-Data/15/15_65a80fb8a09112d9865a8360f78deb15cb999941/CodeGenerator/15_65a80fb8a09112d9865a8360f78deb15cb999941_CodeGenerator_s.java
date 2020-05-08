 package codeGeneration;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.antlr.runtime.tree.Tree;
 
 import semantics_checks.SemanticsUtils;
 import symbol_table.SymbolTable;
 
 public class CodeGenerator
 {
 	private static ArrayList<String> instructions = new ArrayList<String>();
 	private static int identLevel = 0;
 	private static boolean includePrint = false;
 	private static boolean includeRead = false;
 
 	private static boolean includeReadIntTop = false;
 	private static boolean includeReadCharTop = false;
 	private static boolean includeReadStringTop = false;
 
 	
 	private static String printf  = "declare i32 @printf(i8*, ...)";
 	private static String scanf  = "declare i32 @scanf(i8*,...)";
 	
 //	@.str = private unnamed_addr constant [3 x i8] c"%i\00", align 1
 //	@.str1 = private unnamed_addr constant [3 x i8] c"%c\00", align 1
 //	@.str2 = private unnamed_addr constant [3 x i8] c"%s\00", align 1
 	
 	private static String readIntf = "@.readInt = private unnamed_addr " +
 			"constant [3 x i8] c\"%i" + '\\' + "00" + "\", align 1";
 	private static String readCharf = "@.readChar = private unnamed_addr " +
 			"constant [3 x i8] c\"%c" + '\\' + "00" + "\", align 1";
 	private static String readStringf = "@.readString = private unnamed_addr " +
 			"constant [3 x i8] c\"%s" + '\\' + "00" + "\", align 1";
 
 	
 	public static void generateCode(Tree tree, SymbolTable table)	
 	
 	{
 		Tree current = (tree.getText()==null)? tree.getChild(0) : tree ;;
 		current = Statement.checkAllStatements(current, table, new LabelGenerator());
 		moveHattaToEnd(tree);
 		System.out.println(tree.toStringTree());
 		while(current!=null)
 		{
 			Function.writeCodeForFunctions(current, table, new LabelGenerator());
 			current = Statement.checkAllStatements(current, table, new LabelGenerator());
 			current = SemanticsUtils.getNextChild(current);
 		}
 	}
 	
 	/**
 	 * Currently need to check with LIAM and JOSE how to delete the child in 
 	 * antlr tree. Since it seems to be giving troubles and once thats sorted
 	 * import should work like a charm :D
 	 * @param tree
 	 */
 	private static void moveHattaToEnd(Tree tree)
 	{
 		Tree curr = (tree.getText()==null)? tree.getChild(0) : tree ;;
 		while(!curr.getChild(0).getText().contentEquals("hatta"))
 		{
 			curr = SemanticsUtils.getNextChild(curr);
 		}
 		tree.addChild(curr);
 		tree.freshenParentAndChildIndexes();
 		tree.deleteChild(curr.getChildIndex());
 		tree.freshenParentAndChildIndexes();
 	}
 
 	public static void addGlobalInstruction(String ins)
 	{
 		instructions.add(0,ins);
 	}
 	public static void addInstruction(String ins)
 	{
 		instructions.add(getIdent() + ins);
 	}
 
 	private static String getIdent()
 	{
 		String ident = "";
 		for (int i=0; i<identLevel ; i++)
 		{
 			ident += '\t';
 		}
 		return ident;
 	}
 
 	public static void incrementIdentLevel() { identLevel++; }
 	public static void decrementIdentLevel() { identLevel--; }
 
 	public static int getNumberOfInstructions()
 	{
 		return instructions.size();
 	}
 
 	public static void addInstruction(String ins, int index)
 	{
 		instructions.add(index, getIdent() + ins);
 	}
 
 	public static void printInstructions()
 	{
 		for (String ins: instructions)
 		{
 			System.out.println(ins);
 		}
 	}
 
 	public static void emptyInstructions()
 	{
 		instructions.clear();
 	}
 
 	public static void saveToFile(String filepath)
 	{
 		try{
 			FileWriter fstream = new FileWriter(filepath);
 			BufferedWriter out = new BufferedWriter(fstream);
 			writeHeaders(out);
 			for (String line : instructions)
 			{
 				out.write(line + "\n");	
 			}
 			writeFooter(out);
 			out.close();
 		}catch (IOException e){
 			System.err.println("Error: " + e.getMessage());
 		}
 	}
 
 	private static void writeFooter(BufferedWriter out) throws IOException {
 		if (includePrint)
 		{
 			out.write(printf + "\n");
 		}
 		if (includeRead)
 		{
 			out.write(scanf+ "\n");
 		}
 	}
 
 	private static void writeHeaders(BufferedWriter out) throws IOException {
 		if (includeReadIntTop)
 		{
 			out.write(readIntf + "\n");
 		}
 		if (includeReadCharTop)
 		{
 			out.write(readCharf + "\n");				
 		}
 		if (includeReadStringTop)
 		{
 			out.write(readStringf + "\n");				
 		}
 	}
 	
 	public static void includePrint() { includePrint=true;}
 	public static void includeRead() { includeRead=true;}
 	
 	public static void includeReadInt() { includeReadIntTop = true;}
 	public static void includeReadChar() { includeReadCharTop = true;}
 	public static void includeReadString() { includeReadStringTop = true;}
 }
