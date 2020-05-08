 // CompilationEngine.java
 
 package JackCompiler;
 
 import java.io.*;
 import java.util.*;
 import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.List;
 import JackCompiler.JackTokenizer.Token;
 import JackCompiler.SymbolTable;
 import JackCompiler.SymbolTable.Kind;
 import JackCompiler.VMWriter.Segment;
 import JackCompiler.VMWriter.Command;
 import JackCompiler.SymbolTable.Entry;
 
 public class CompilationEngine {
   public OutputStreamWriter outStream;
   public VMWriter writer;
   public JackTokenizer tokenizer;
   public JackTokenizer.Token token_type;
   public char[] op = {'+','-','*','/','&','|','<','>','='};
   public SymbolTable table;
   public String className;
   public int labelCount = 0;
 
   public enum Cat {
 	  VAR, ARG, STATIC, FIELD, CLASS, SUB, TYPE;
   }
 
   // Creates a new compilation engine
   public CompilationEngine(JackTokenizer token, OutputStreamWriter stream) throws IOException {	
 	table = new SymbolTable();
 	writer = new VMWriter(stream);
     outStream = stream;
 	tokenizer = token;
   }
 
   // Compiles a complete class
   public void CompileClass() throws IOException {
 	// Starting to compile a class
 
 	if(tokenizer.keyWord().equals("class")) {
 		// Skip over class keyword
 		String keyword = tokenizer.keyWord();
 		tokenizer.advance();
 		
 		// Save the class name in a variable
 		className = tokenizer.identifier();
 		tokenizer.advance();
 		
 		// Skip over opening bracket
 		tokenizer.advance();
 		
 		// Compile any class level variable declarations
 		while(tokenizer.keyWord().equals("static") || tokenizer.keyWord().equals("field")) {
 			CompileClassVarDec();
 		}
 		
 		// Compile all other subroutines
 		while(tokenizer.hasMoreTokens() && (tokenizer.keyWord().equals("constructor") || tokenizer.keyWord().equals("function") || tokenizer.keyWord().equals("method"))) {
 			CompileSubroutine();
 		}
 		
 	}
 	
 	// Finished with class, skip closing bracket
 
 	// DEBUG
 	printTable(table.classSymbols);
   }
   
   // Compiles a static declaration or a field declaration
   public void CompileClassVarDec() throws IOException {
 	Boolean cont = true;
	Cat cat = Cat.STATIC;
 	
 	// Save category of declaration
 	if (tokenizer.keyWord().equals("field")){
		cat = Cat.FIELD;
 	}
 	tokenizer.advance();
 	
 	// Save the type of the variables
 	String type;
 	if (tokenizer.tokenType() == Token.KEYWORD){
 		type = tokenizer.keyWord();
 	} else {
 		type = tokenizer.identifier();
 	}
 	tokenizer.advance();
 	
 	// Save the variable in the class scope table
	table.Define(tokenizer.identifier(), type, Kind.VAR, true);
 	tokenizer.advance();
 	
 	// Check if there are more
 	if (tokenizer.symbol() == ';'){
 		cont = false;
 	}
 	
 	// If there are more variable declarations continue looping
 	while (cont){
 	
 		// Add variable name to class scope table (skips over commas)
 		if (tokenizer.tokenType() == Token.IDENTIFIER){
			table.Define(tokenizer.identifier(), type, Kind.VAR, true);
 		}
 		tokenizer.advance();
 
 		if (tokenizer.tokenType() == Token.SYMBOL){
 			if (tokenizer.symbol() == ';'){
 				cont = false;
 			}
 		}
 	}
 	
 	// Handle semi-colon at end
 	tokenizer.advance();
   }
   
   // Compiles a complete method, function, or constructor
   public void CompileSubroutine() throws IOException {
 	// Start a new subroutine in the symbol table
 	table.startSubroutine();
 	
 	// Save subroutine type
 	String thing = tokenizer.keyWord();
 	tokenizer.advance();
 	
 	// Check return type
 	if(tokenizer.tokenType() == Token.IDENTIFIER) {
 		tokenizer.advance();
 	}else{
 		tokenizer.advance();
 	}
 	
 	// If this is a method, save subroutine name in local scope
 	// NOT NECESSARY???
	if (thing.equals("method")){
 		table.Define(tokenizer.identifier(), thing, Kind.ARG, false);
 	}
 	
 	// Create a string to hold the function name
 	String functionName = className + "." + tokenizer.identifier();
 	tokenizer.advance();
 	
   	// Skip opening parenthesis
 	tokenizer.advance();
 	
 	//Parameter list
 	compileParameterList();
 	
 	//Subroutine body:
 	
 	// Skip over opening bracket
 	tokenizer.advance();
 	
 	// Compile all the variable declarations and keep count
 	int count = 0;
 	while (tokenizer.keyWord().equals("var")){
 		count = count + compileVarDec();
 	}
 	
 	// Write function declaration with name and num local variables
 	writer.writeFunction(functionName, count);
 	
 	// Compile statements
 	compileStatements();
 	
 	// Skip over closing bracket for subroutine body
 	tokenizer.advance();
 	
 	// DEBUG
 	printTable(table.symbols);
   }
   
   // Compiles a parameter list
   public int compileParameterList() throws IOException {
 	
 	Boolean cont = true;
 	token_type = tokenizer.tokenType();
 	
 	int count = 0;
 	
 	// If parameter list is empty, handle and return
 	if (token_type == Token.SYMBOL){
 		if (tokenizer.symbol() == ')'){
 			// Skip the closing parenthesis
 			tokenizer.advance();
 			return count;
 		}
 	}
 	
 	// Otherwise loop through and print out the parameter list
 	while (cont){
 		// Get the type
 		String type;
 		if (tokenizer.tokenType() == Token.KEYWORD){
 			type = tokenizer.keyWord();
 		} else {
 			type = tokenizer.identifier();
 		}
 		tokenizer.advance();
 		
     	// Save the variable name in local scope and increment count
 		table.Define(tokenizer.identifier(), type, Kind.ARG, false);
 		count++;
 		tokenizer.advance();
 		
 		// Check for closing parenthesis
 		token_type = tokenizer.tokenType();
 		if (token_type == Token.SYMBOL){
 			if (tokenizer.symbol() == ')'){
 				cont = false;
 			} else {
 				// Skip the comma
 				tokenizer.advance();
 				
 				// Get the type
 				token_type = tokenizer.tokenType();
 			}
 		}
 	}
 	
 	// Handle closing parenthesis
 	tokenizer.advance();
 	
 	return count;
   }
   
   // Compiles a var declaration
   public int compileVarDec() throws IOException {
 	
   	Boolean cont = true;
 	int count = 0;
 	
 	// Pass up the var keyword
 	tokenizer.advance();
 	
 	// Get type of variables
 	String type;
 	if(tokenizer.tokenType() == Token.IDENTIFIER) {
 		type = tokenizer.identifier();
 	}else{
 		type = tokenizer.keyWord();
 	}
 	tokenizer.advance();
 	
 	// Add variable name to local scope table and increment count
 	table.Define(tokenizer.identifier(), type, Kind.VAR, false);
 	count++;
 	tokenizer.advance();
 	
 	// Check if there are more
 	if (tokenizer.symbol() == ';'){
 		cont = false;
 	}
 	
 	// If there are more variable declarations continue looping
 	while (cont){
 		// Pass up the comma
 		tokenizer.advance();
 		
 		// Put the variable into the local scope table and increment count
 		table.Define(tokenizer.identifier(), type, Kind.VAR, false);
 		count++;
 		tokenizer.advance();
 		
 		if (tokenizer.symbol() == ';'){
 			cont = false;
 		}
 	}
 	
 	// Handle semi-colon at end
 	tokenizer.advance();
 	
 	return count;
 	
   }
   
   // Compiles a sentence of statements
   public void compileStatements() throws IOException {
 	
 	outStream.write("// Compiling statements\n");
 	
 	String kwd = tokenizer.keyWord();
 	
 	Boolean cont = true;
 	while (cont){
 		if (kwd.equals("let")){
 					outStream.write("// Let statement\n");
 					compileLet();
 					outStream.write("// End let statement\n");
 				} else if (kwd.equals("if")){
 					outStream.write("// If statement\n");
 					compileIf();
 					outStream.write("// End if statement\n");
 				} else if (kwd.equals("while")){
 					outStream.write("// While statement\n");
 					compileWhile();
 					outStream.write("// End while statement\n");
 				} else if (kwd.equals("do")){
 					outStream.write("// Do statement\n");
 					compileDo();
 					outStream.write("// End do statement\n");
 				} else if (kwd.equals("return")){
 					outStream.write("// Return statement\n");
 					compileReturn();
 					outStream.write("// End return statement\n");
 				} else {
 					System.err.println("Error parsing statements.");
 				}
 				
 				// Stopping condition if have reached a symbol, the closing }
 				if (tokenizer.tokenType() == Token.SYMBOL) {
 					cont = false;
 				} else {
 					kwd = tokenizer.keyWord();
 				}
 			}
   }
   
   // Compiles a do statement
   public void compileDo() throws IOException {
 	
 	// Pass up the do keyword
 	tokenizer.advance();
 	
 	compileSubroutine();
 	
 	// Pass up the ending semi-colon
 	tokenizer.advance();
   }
   
   public void compileSubroutine() throws IOException {
 	String name = "";
 	
 	// Save the subroutine name or class name
 	if (tokenizer.tokenType() == Token.IDENTIFIER){
 		name = tokenizer.identifier();
 	}
 	
 	tokenizer.advance();
 	
 	// If that is all there is to the subroutine call, get the arguments
 	if (tokenizer.symbol() == '('){
 		// Pass up opening parenthesis
 		tokenizer.advance();
 
 		// Compile expression list
 		int count = CompileExpressionList();
 		
 		// Write the function call
		writer.writeCall(name, count);
 		
 	} else if (tokenizer.symbol() == '.'){
 		// Pass up the . symbol
 		tokenizer.advance();
 		
 		// Save the subroutine name
 		name = name + "." + tokenizer.identifier();
 		
 		tokenizer.advance();
 		
 		// Pass up the opening parenthesis
 		tokenizer.advance();
 		
 		// Compile the expression list
 		int count = CompileExpressionList();
 		
 		// Write the function call and then pop off the void return value
 		writer.writeCall(name, count);
 		writer.writePop(Segment.TEMP, 0);
 	}
   }
   
   // Compiles a let statement
   public void compileLet() throws IOException {
 	Segment dest = Segment.NONE;
 	int destIndex = 0;
 	
 	// Pass up the let keyword
 	tokenizer.advance();
 	
 	// Handle the destination variable name
 	
 	// Check to see if variable is in current scope
 	if (table.symbols.containsKey(tokenizer.identifier())){
 	
 		// If in current scope, determine whether it is an argument or local variable
 		if (table.KindOf(tokenizer.identifier(), false) == Kind.ARG) {
 			dest = Segment.ARG;
 		} else if (table.KindOf(tokenizer.identifier(), false) == Kind.VAR) {
 			dest = Segment.LOCAL;
 		}
 		
 		// Save the index
 		destIndex = table.IndexOf(tokenizer.identifier(), false);
 		
 	} else if (table.classSymbols.containsKey(tokenizer.identifier())){
 	
 		// If in class scope, determine whether it is a static or field variable
 		if (table.KindOf(tokenizer.identifier(), true) == Kind.STATIC) {
 			dest = Segment.STATIC;
 		} else if (table.KindOf(tokenizer.identifier(), true) == Kind.FIELD) {
 			dest = Segment.THIS;
 		}
 		
 		// Save the index
 		destIndex = table.IndexOf(tokenizer.identifier(), true);
 	}
 	tokenizer.advance();
 	
 	// Handle a bracketed expression if it exists
 	if (tokenizer.symbol() == '['){
 		// Print out opening bracket
 		OutputXML(tokenizer.tokenType());
 		tokenizer.advance();
 		// Compile expression
 		CompileExpression();
 		// Print out closing bracket
 		OutputXML(tokenizer.tokenType());
 		tokenizer.advance();
 	}
 	
 	// Pass up the equals sign
 	tokenizer.advance();
 	
 	// Compile expression
 	CompileExpression();
 	
 	// Pass up the semi-colon
 	tokenizer.advance();
 	
 	// Write a pop statement to pop the result into the destination variable
 	writer.writePop(dest, destIndex);
   }
   
   // Compiles a while statement
   public void compileWhile() throws IOException {
 	
 	// Create a label a beginning of while
 	String labelName1 = "whileStart" + labelCount;
 	String labelName2 = "whileEnd" + labelCount;
 	labelCount++;
 	writer.writeLabel(labelName1);
 	
 	// Pass up the first keyword
 	tokenizer.advance();
 	
 	// Pass up opening parenthesis
 	tokenizer.advance();
 	
 	// Compile the expression
 	CompileExpression();
 	
 	// Check the opening condition by negating the expression
 	writer.writeArithmetic(Command.NOT);
 	writer.writeIf(labelName2);
 	
 	// Pass up the closing parenthesis
 	tokenizer.advance();
 	
 	// Pass up the opening bracket
 	tokenizer.advance();
 	
 	// Compile statements
 	compileStatements();
 	
 	// Pass up closing bracket
 	tokenizer.advance();
 	
 	// Write the goto statement so loop will continue
 	writer.writeGoto(labelName1);
 	
 	// Write out the label for the end of the while loop
 	writer.writeLabel(labelName2);
   }
   
   // Compiles a return statement
   public void compileReturn() throws IOException {
 	
 	// Pass up the return keyword
 	tokenizer.advance();
 	
 	if (tokenizer.tokenType() == Token.SYMBOL){
 		if (tokenizer.symbol() == ';'){
 			// If it is returning void push zero, then return
 			writer.writePush(Segment.CONST, 0);
 			writer.writeReturn();
 			
 			// Pass up the semi-colon
 			tokenizer.advance();
 		}
 	} else {
 		CompileExpression();
 		
 		// Write a regular return statement
 		writer.writeReturn();
 		
 		// Pass up the semi-colon
 		tokenizer.advance();
 	}
   }
   
   // Compiles an if statement
   public void compileIf() throws IOException {
 	
 	// Define label variables for if
 	String labelName1 = "ifEnd" + labelCount;
 	String labelName2 = "elseEnd" + labelCount;
 	labelCount++;
 	
 	// Pass up the if keyword
 	tokenizer.advance();
 	
 	// Pass up opening parenthesis
 	tokenizer.advance();
 	
 	// Compile the expression
 	CompileExpression();
 	
 	// Negate the expression and check the condition
 	writer.writeArithmetic(Command.NOT);
 	
 	// If condition doesn't hold goto else or end of if
 	writer.writeIf(labelName1);
 	
 	// Pass up the closing parenthesis
 	tokenizer.advance();
 	
 	// Pass up the opening bracket
 	tokenizer.advance();
 	
 	// Compile statements
 	compileStatements();
 	
 	// Write statement to jump to end of if/else
 	writer.writeGoto(labelName2);
 
 	// Pass up closing bracket
 	tokenizer.advance();
 
 	// First label for end of if, start of else
 	writer.writeLabel(labelName1);
 		
 	// Handle else statement
 	if (tokenizer.tokenType() == Token.KEYWORD && tokenizer.keyWord().equals("else")){
 		// Pass up the else keyword
 		tokenizer.advance();
 		
 		// Pass up the opening bracket
 		tokenizer.advance();
 		
 		// Compile statements
 		compileStatements();
 		
 		// Pass up closing bracket
 		tokenizer.advance();
 	}
 	
 	// Write the second label at end of if/else
 	writer.writeLabel(labelName2);
 	
   }
   
   // Compiles an expression
   public void CompileExpression() throws IOException {
   
 	// Compile the first term
 	CompileTerm();
 	
 	// Save the symbol
 	char sym = tokenizer.symbol();
 	
 	while (sym == '+' || sym == '-' || sym == '*' || sym == '/' || sym == '&' || sym == '|' || sym == '<' || sym == '>' || sym == '='){
 		// Pass up the operation
 		tokenizer.advance();
 		
 		// Compile the term
 		CompileTerm();
 		
 		// Write appropriate vm operation given the symbol used
 		if (sym == '+') {
 			writer.writeArithmetic(Command.ADD);
 		} else if (sym == '-') {
 			writer.writeArithmetic(Command.SUB);
 		} else if (sym == '*') {
 			writer.writeCall("Math.multiply", 2);
 		} else if (sym == '/') {
 			writer.writeCall("Math.divide", 2);
 		} else if (sym == '&') {
 			writer.writeArithmetic(Command.AND);
 		} else if (sym == '|') {
 			writer.writeArithmetic(Command.OR);
 		} else if (sym == '<') {
 			writer.writeArithmetic(Command.LT);
 		} else if (sym == '>') {
 			writer.writeArithmetic(Command.GT);
 		} else if (sym == '=') {
 			writer.writeArithmetic(Command.EQ);
 		}
 		
 		// Check next symbol
 		sym = tokenizer.symbol();
 	}
   }
   
   // Compiles a term
   public void CompileTerm() throws IOException {
 	
 	// Check for expression in parentheses or unary operation
 	if(tokenizer.tokenType() == Token.SYMBOL && tokenizer.symbol() == '(') {
 		// Pass up opening parenthesis
 		tokenizer.advance();
 		
 		// Compile an expression
 		CompileExpression();
 		
 		// Pass up closing parenthesis
 		tokenizer.advance();
 		
 	} else if(tokenizer.tokenType() == Token.SYMBOL && (tokenizer.symbol() == '-' || tokenizer.symbol() == '~'	)) {
 		// Save the symbol
 		char sym = tokenizer.symbol();
 		
 		// Pass up the symbol
 		tokenizer.advance();
 		
 		// Compile a term
 		CompileTerm();
 		
 		// Write appropriate vm operation given the symbol used
 		if (sym == '-') {
 			writer.writeArithmetic(Command.NEG);
 		} else if (sym == '~') {
 			writer.writeArithmetic(Command.NOT);
 		}
 		
 	}else {
 		String name = "";
 		
 		// Check for constants or identifiers
 		if(tokenizer.tokenType() == Token.IDENTIFIER) {
 			// Save the name of the identifier
 			name = tokenizer.identifier();
 			tokenizer.advance();
 		}else if (tokenizer.tokenType() == Token.STRING_CONST) {
 			OutputXML(tokenizer.tokenType());
 			tokenizer.advance();
 		} else if (tokenizer.tokenType() == Token.INT_CONST) {
 			// Push the integer constant
 			writer.writePush(Segment.CONST, tokenizer.intVal());
 			tokenizer.advance();
 		} else if (tokenizer.tokenType() == Token.KEYWORD) {
 			// Get the keyword
 			String keyword = tokenizer.keyWord();
 			
 			// Handle keyword constants
 			if (keyword.equals("true")) {
 				writer.writePush(Segment.CONST, 1);
 				writer.writeArithmetic(Command.NEG);
 				
 			}
 			
 			if (keyword.equals("false") || keyword.equals("null")) {
 				writer.writePush(Segment.CONST, 0);
 			}
 			
 			tokenizer.advance();
 		} else {
 			// Shouldn't get here
 			OutputXML(tokenizer.tokenType());
 			tokenizer.advance();
 		}
 		
 		// If there is more to the term (another expression or subroutine call) handle this
 		if(tokenizer.tokenType() == Token.SYMBOL && tokenizer.symbol() == '[') {
 			// Handle expression
 			outStream.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
 			tokenizer.advance();
 			CompileExpression();
 			outStream.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
 			tokenizer.advance();
 		} else if(tokenizer.tokenType() == Token.SYMBOL && tokenizer.symbol() == '('){
 			// Handle subroutine call
 			outStream.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
 			tokenizer.advance();
 			CompileExpressionList();
 			outStream.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
 			tokenizer.advance();
 		} else if(tokenizer.tokenType() == Token.SYMBOL && tokenizer.symbol() == '.') {
 			// Handle subroutine call
 			
 			// Pass up . symbol
 			tokenizer.advance();
 			
 			// Add subroutine name to overall name
 			name = name + "." + tokenizer.identifier();
 			tokenizer.advance();
 			
 			// Pass up opening parenthesis
 			tokenizer.advance();
 			
 			// Compile the argument list for the subroutine
 			int count = CompileExpressionList();
 			
 			// Call the subroutine
 			writer.writeCall(name, count);
 			
 		} else {
 			
 			// Handle case for variable name
 			Segment dest = Segment.NONE;
 			int destIndex = 0;
 			
 			// Check to see if variable is in current scope or class scope
 			if (table.symbols.containsKey(name)){
 			
 				// If in current scope, determine whether it is an argument or local variable
 				if (table.KindOf(name, false) == Kind.ARG) {
 					dest = Segment.ARG;
 				} else if (table.KindOf(name, false) == Kind.VAR) {
 					dest = Segment.LOCAL;
 				}
 				
 				// Write a push statement for the variable
 				destIndex = table.IndexOf(name, false);
 				writer.writePush(dest, destIndex);
 				
 			} else if (table.classSymbols.containsKey(name)){
 			
 				// If in class scope, determine whether it is a static or field variable
 				if (table.KindOf(name, true) == Kind.STATIC) {
 					dest = Segment.STATIC;
 				} else if (table.KindOf(name, true) == Kind.FIELD) {
 					dest = Segment.THIS;
 				}
 				
 				// Write a push statement for the variable
 				destIndex = table.IndexOf(name, true);
 				writer.writePush(dest, destIndex);
 				
 			}
 		}
 	}
   }
   
   // Compiles a comma-separated list of expressions
   public int CompileExpressionList() throws IOException {
 	int count = 0;
 	Boolean cont = true;
 	token_type = tokenizer.tokenType();
 	
 	// If parameter list is empty, handle and return
 	if (token_type == Token.SYMBOL){
 		if (tokenizer.symbol() == ')'){
 			// Pass over ending parenthesis
 			tokenizer.advance();
 			return count;
 		}
 	}
 	
 	// Otherwise loop through and print out the parameter list
 	while (cont){
     	// Compile the expression and add it to the count of expressions
 		CompileExpression();
 		count++;
 		
 		token_type = tokenizer.tokenType();
 		if (token_type == Token.SYMBOL){
 			if (tokenizer.symbol() == ')'){
 				cont = false;
 			} else if (tokenizer.symbol() == ','){
 				// Skip over comma
 				tokenizer.advance();
 			}
 		}
 	}
 		
 	// Skip over closing parenthesis
 	tokenizer.advance();
 	
 	return count;
   }
   
   public void OutputXML(Token token_type) throws IOException {
 		if(token_type == Token.KEYWORD) {
 			try{
 				outStream.write("<keyword> " + tokenizer.keyWord() + " </keyword>\n");
 			}catch(IOException x){
 				//TODO: print error?
 			}
 		}
 		if(token_type == Token.SYMBOL) {
 			String x = Character.toString(tokenizer.symbol());
 			if(x.equals("<")) {
 				x = "&lt;";
 			}
 			if(x.equals(">")) {
 				x = "&gt;";
 			}
 			if(x.equals("&")) {
 				x = "&amp;";
 			}
 			try{
 				outStream.write("<symbol> " + x + " </symbol>\n");
 			}catch(IOException z){
 				System.err.println("error outputing XML");
 			}
 		}
 		if(token_type == Token.IDENTIFIER) {
 			try{
 				outStream.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
 			}catch(IOException h){
 				System.err.println("error outputing XML");
 			}
 		}
 		if(token_type == Token.INT_CONST) {	
 			try{
 				outStream.write("<integerConstant> " + tokenizer.intVal() + " </integerConstant>\n");
 			}catch(IOException k) {
 				System.err.println("error outputing XML");
 			}
 		}
 		if(token_type == Token.STRING_CONST) {
 			try{
 				outStream.write("<stringConstant> " + tokenizer.stringVal() + " </stringConstant>\n");
 			}catch(IOException m){
 				System.err.println("error outputing XML");
 			}
 		}
   }
   
   public void OutputXML(Token token_type, Cat c, Boolean used) throws IOException {
 		if(token_type == Token.KEYWORD) {
 			try{
 				outStream.write("<keyword> " + tokenizer.keyWord() + " </keyword>\n");
 			}catch(IOException x){
 				//TODO: print error?
 			}
 		}
 		if(token_type == Token.SYMBOL) {
 			String x = Character.toString(tokenizer.symbol());
 			if(x.equals("<")) {
 				x = "&lt;";
 			}
 			if(x.equals(">")) {
 				x = "&gt;";
 			}
 			if(x.equals("&")) {
 				x = "&amp;";
 			}
 			try{
 				outStream.write("<symbol> " + x + " </symbol>\n");
 			}catch(IOException z){
 				System.err.println("error outputing XML");
 			}
 		}
 		if(token_type == Token.IDENTIFIER) {
 			try{
 				outStream.write("<identifier> " + tokenizer.identifier() + " CAT=" + c + " USED=" + used + " </identifier>\n");
 			}catch(IOException h){
 				System.err.println("error outputing XML");
 			}
 		}
 		if(token_type == Token.INT_CONST) {	
 			try{
 				outStream.write("<integerConstant> " + tokenizer.intVal() + " </integerConstant>\n");
 			}catch(IOException k) {
 				System.err.println("error outputing XML");
 			}
 		}
 		if(token_type == Token.STRING_CONST) {
 			try{
 				outStream.write("<stringConstant> " + tokenizer.stringVal() + " </stringConstant>\n");
 			}catch(IOException m){
 				System.err.println("error outputing XML");
 			}
 		}
   }
 
   public void printTable(HashMap<String, Entry> map) {
 	System.out.println("Printing table hopefully printing...");
 	Iterator iterator = map.keySet().iterator();  
 	
 	while (iterator.hasNext()) {  
 	   String key = iterator.next().toString();  
 	   Entry value = map.get(key);  
 
 	   System.out.println(key + " " + value.type + " " + value.kind + " " + value.index);
 	}
 	
 	System.out.println();
 }
 }
