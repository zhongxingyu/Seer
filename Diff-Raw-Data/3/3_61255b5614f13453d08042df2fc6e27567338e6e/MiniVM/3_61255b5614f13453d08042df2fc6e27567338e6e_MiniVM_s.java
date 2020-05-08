 package MiniRE.VirtualMachine;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.Writer;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.TreeMap;
 
 import Generator.Lexer.Lexer;
 import Generator.Lexer.Token;
 import MiniRE.AST;
 
 public class MiniVM {
 	Map<String, Variable> symbol_table;
 	
 	public boolean debug;
 	
 	public MiniVM() {
 		symbol_table = new TreeMap<>();
 		debug = false;
 	}
 	
 	public void match(String rule_id, AST ast) throws Exception {
 		if(ast == null || !rule_id.equals(ast.rule_id)) {
			if(debug)
				System.out.println("MiniVM failed: " + rule_id + " was " + ast.rule_id);
 			throw new Exception();
 		} else {
 			if(debug)
 				System.out.println("MiniVM matched: " + rule_id + " as " + ast);
 		}
 	}
 	
 	public void run(AST ast) throws Exception {
 		symbol_table.clear();
 		MiniRE_program(ast);
 	}
 	
 	public void MiniRE_program(AST ast) throws Exception {
 		match("MiniRE-program", ast);
 		
 		match("BEGIN", ast.get(0));
 		statement_list(ast.get(1));
 		match("END", ast.get(2));
 	}
 	
 	public void statement_list(AST ast) throws Exception {
 		match("statement-list", ast);
 		
 		statement(ast.get(0));
 		statement_list_tail(ast.get(1));
 	}
 	
 	public void statement_list_tail(AST ast) throws Exception {
 		match("statement-list-tail", ast);
 		
 		switch (ast.get(0).rule_id) {
 		case "statement" :
 			statement(ast.get(0));
 			statement_list_tail(ast.get(1));
 			break;
 		case "epsilon" :
 			match("epsilon", ast.get(0));
 			break;
 		}
 	}
 	
 	public void statement(AST ast) throws Exception {
 		match("statement", ast);
 		
 		switch (ast.get(0).rule_id) {
 		case "REPLACE" :
 			replace(ast);
 			break;
 		case "RECREP" :
 			recursivereplace(ast);
 			break;
 		case "ID" :
 			assign_expr(ast);
 			break;
 		case "PRINT" :
 			print(ast);
 			break;
 		}
 	}
 	
 	/*
 	 * Input AST is <statement>, use the children of statement
 	 */
 	public void replace(AST ast) throws Exception{
 		match("REPLACE", ast.get(0));
 		Lexer lexer = regex(ast.get(1));
 		match("WITH", ast.get(2));
 		String ascii_str = ascii_str(ast.get(3));
 		match("IN", ast.get(4));
 		String[] fnames = file_names(ast.get(5));
 		
 		String content = parseFile(fnames[0]);
 		lexer.tokenize(content);
 		
 		while(lexer.hasNext("REGEX")) {
 			Token token = lexer.next("REGEX");
 			lexer.replace(token, ascii_str);
 		}
 		
 		String output = lexer.code();
 		Writer out = new BufferedWriter(new FileWriter(fnames[1]));
 		out.write(output);
 		out.close();
 	}
 
 	/*
 	 * Input AST is <statement>, use the children of statement
 	 */
 	public void recursivereplace(AST ast) throws Exception {
 		match("RECREP", ast.get(0));
 		Lexer lexer = regex(ast.get(1));
 		match("WITH", ast.get(2));
 		String ascii_str = ascii_str(ast.get(3));
 		match("IN", ast.get(4));
 		String[] fnames = file_names(ast.get(5));
 		match("SEMICOLON", ast.get(6));
 		
 		String content = parseFile(fnames[0]);
 		lexer.tokenize(content);
 		
 		int count = 1; //counts how many regex has been replaced in one run.
 		
 		while(count != 0) {
 			count = 0;
 			
 			while(lexer.hasNext("REGEX")){
 				
 				Token token = lexer.next("REGEX");
 				
 				if(token.equals(ascii_str)){
 					continue;
 				}
 				else {
 					lexer.replace(token,  ascii_str);
 					count++;
 				}
 			}
 			
 			lexer.reset();
 		}
 		
 		String output = lexer.code();
 		Writer out = new BufferedWriter(new FileWriter(fnames[1]));
 		out.write(output);
 		out.close();
 	}
 	
 	public String parseFile(String fname) throws Exception {
 		Scanner in;
 		String code = "";
 		
 		in = new Scanner(new File(fname));
 		code = in.useDelimiter("\\Z").next();
 		in.close();
 		
 		return code;
 	}
 	
 	/*
 	 * Input AST is <statement>, use the children of statement
 	 * 
 	 * <statement> --
 	 * ID = <statement-righthand>;
 	 */
 	public void assign_expr(AST ast) throws Exception {
 		String id = id(ast.get(0));
 		match("EQ", ast.get(1));
 		Variable var = statement_righthand(ast.get(2));
 		match("SEMICOLON", ast.get(3));
 		
 		var.name = id;
 		
 		symbol_table.put(id, var);
 	}
 	
 	public String id(AST ast) throws Exception {
 		match("ID", ast);
 		
 		return ast.value;
 	}
 	
 	/*
 	 * Input AST is <statement>, use the children of statement
 	 */
 	public void print(AST ast) throws Exception {
 		match("PRINT", ast.get(0));
 		match("OPENPARENS", ast.get(1));
 		List<Variable> exp_list = exp_list(ast.get(2));
 		match("CLOSEPARENS", ast.get(3));
 		match("SEMICOLON", ast.get(4));
 		
 		for(Variable var : exp_list) {
 			System.out.println(var);
 		}
 	}
 	
 	public Variable statement_righthand(AST ast) throws Exception {
 		match("statement-righthand", ast);
 		
 		Variable var = null;
 		switch (ast.get(0).rule_id) {
 		case "exp" :
 			var = exp(ast.get(0));
 			break;
 		case "HASH" :
 			var = hash(ast);
 			break;
 		case "MAXFREQ" :
 			var = maxfreq(ast);
 			break;
 		}
 		
 		return var;
 	}
 	
 	/*
 	 * Input AST is <statement-righthand>, use the children
 	 */
 	public Variable hash(AST ast) throws Exception {
 		match("HASH", ast.get(0));
 		Variable var = exp(ast.get(1));
 		
 		// Implement #<exp>
 		StringMatchList sml = var.getStringMatchList();
 		var = new Variable(sml.size());
 		
 		return var;
 	}
 	
 	/*
 	 * Input AST is <statement-righthand>, use the children of statement
 	 */
 	public Variable maxfreq(AST ast) throws Exception {
 		match("MAXFREQ", ast.get(0));
 		match("OPENPARENS", ast.get(1));
 		String id = id(ast.get(2));
 		match("CLOSEPARENS", ast.get(3));
 		
 		// Implement maxfreq
 		
 		Variable var = symbol_table.get(id);
 		StringMatchList sml = var.getStringMatchList();
 		
 		var = new Variable(sml.maxFreq());
 		
 		return var;
 	}
 	
 	public String[] file_names(AST ast) throws Exception{
 		match("file-names", ast);
 		
 		String[] files = new String[2];
 		files[0] = source_file(ast.get(0));
 		match("GRTNOT", ast.get(1));
 		files[1] = destination_file(ast.get(2));
 		
 		return files;
 	}
 	
 	public String ascii_str(AST ast) throws Exception {
 		match("ASCII-STR", ast);
 		
 		String ascii = ast.value;
 		ascii = ascii.substring(1, ascii.length()-1);
 		return ascii;
 	}
 	
 	public String source_file(AST ast) throws Exception{
 		match("source-file", ast);
 		
 		return ascii_str(ast.get(0));
 	}
 	
 	public String destination_file(AST ast) throws Exception{
 		match("destination-file", ast);
 		
 		return ascii_str(ast.get(0));
 
 	}
 	
 	public List<Variable> exp_list(AST ast) throws Exception{
 		match("exp-list", ast);
 		
 		List<Variable> exp_list = new LinkedList<Variable>();
 		
 		exp_list.add(exp(ast.get(0)));
 		exp_list = exp_list_tail(ast.get(1), exp_list);
 		
 		return exp_list;
 	}
 	
 	public List<Variable> exp_list_tail(AST ast, List<Variable> exp_list) throws Exception{
 		match("exp-list-tail", ast);
 		
 		switch (ast.get(0).rule_id) {
 		case "COMMA" :
 			match("COMMA", ast.get(0));
 			exp_list.add(exp(ast.get(1)));
 			exp_list = exp_list_tail(ast.get(2), exp_list);
 			break;
 		default :
 			// Do nothing
 		}
 		
 		return exp_list;
 	}
 	
 	public Variable exp(AST ast) throws Exception{
 		match("exp", ast);
 		
 		Variable var = null;
 		switch(ast.get(0).rule_id) {
 		case "ID" :
 			String id = id(ast.get(0));
 			var = symbol_table.get(id);
 			break;
 		case "exp" :
 			var = exp(ast.get(0));
 			break;
 		case "term" :
 			var = term(ast.get(0));
 			var = exp_tail(ast.get(1), var);
 			break;
 		}
 		
 		return var;
 	}
 	
 	public Variable exp_tail(AST ast, Variable lhs) throws Exception{
 		match("exp-tail", ast);
 		
 		Variable var = null;
 		switch(ast.get(0).rule_id) {
 		case "bin-op" :
 			Variable rhs = term(ast.get(1));
 			var = bin_op(ast.get(0), lhs, rhs);
 			var = exp_tail(ast.get(2), var);
 			break;
 		case "epsilon" :
 			match("epsilon", ast.get(0));
 			var = lhs;
 			break;
 		}
 		
 		return var;
 	}
 	
 	public Variable term(AST ast) throws Exception {
 		match("term", ast);
 		
 		match("FIND", ast.get(0));
 		Lexer lexer = regex(ast.get(1));
 		match("IN", ast.get(2));
 		String fname = file_name(ast.get(3));
 		
 		Variable var = new Variable(new StringMatchList());
 		
 		// Implement term
 		Scanner in;
 		in = new Scanner(new File(fname));
 		String content = in.useDelimiter("\\Z").next();
 		in.close();
 		
 		lexer.tokenize(content);
 		while(lexer.hasNext("REGEX")) {
 			var.getStringMatchList().add(new StringMatch(fname, lexer.next("REGEX")));
 		}
 		
 		return var;
 	}
 	
 	public String file_name(AST ast) throws Exception {
 		match("file-name", ast);
 		
 		return ascii_str(ast.get(0));
 	}
 	
 	public Variable bin_op(AST ast, Variable lhs, Variable rhs) throws Exception {
 		match("bin-op", ast);
 		
 		Variable var = null;
 		switch(ast.get(0).rule_id){
 		case "DIFF" :
 			match("DIFF", ast.get(0));
 			var = new Variable(lhs.getStringMatchList().diff(rhs.getStringMatchList()));
 			break;
 		case "UNION" :
 			match("UNION", ast.get(0));
 			var = new Variable(lhs.getStringMatchList().union(rhs.getStringMatchList()));
 			break;
 		case "INTERS" :
 			match("INTERS", ast.get(0));
 			var = new Variable(lhs.getStringMatchList().inters(rhs.getStringMatchList()));
 			break;
 		}
 		
 		return var;
 	}
 	
 	public Lexer regex(AST ast) throws Exception {
 		match("REGEX", ast);
 		
 		String regex = ast.value.substring(1, ast.value.length()-1);
 		Lexer lexer = new Lexer("$REGEX (" + regex + ")");
 		
 		return lexer;
 	}
 }
