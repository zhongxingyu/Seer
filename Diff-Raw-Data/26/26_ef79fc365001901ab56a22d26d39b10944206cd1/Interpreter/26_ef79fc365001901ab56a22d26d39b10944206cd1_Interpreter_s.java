 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Scanner;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.lang.Math;
 
 //TODO should derive functions and vars from same base class to handle functions as parameters?
 //TODO should add base class for basic vars and classes?
 
 public class Interpreter implements Runnable {
 
 	//public enum double_ops {LT, LE, GT, GE, EQ, NE};
 	private enum block_type {FUNCTION, CONDITIONAL};
 	private enum return_state{FUNC_RETURN, END_OF_BLOCK, STOP};
 
 	//private final int NUM_COMMANDS = 14;
 	private int numStepsToRun = 0;
 
 	public SymbolTable symbolTable;
 	private Controller controller;
 	private String CppSrcFile;
 	private boolean StopRun = false;
 	private boolean checkOnly = false;
 
 	private Lexer.Token token;
 
 	private var_type ret_value; /* function return value */
 
 	//TODO: need to look at removing the hard limits for variables or adding checks/error messages
 
 	private final int NUM_FUNC = 100;
 	private func_type func_table[] = new func_type[NUM_FUNC];
 	private int func_index = 0; // index into function table/
 
 	static private String interpretation = "";
 	Lexer lexer = null;
 
 	// -----------------------------------------------------------------------
 	// Constructors
 
 	@SuppressWarnings("unused")
 	private Interpreter(){
 
 		}
 
 	public Interpreter(Controller c, String s, int numSteps, SymbolTable stab)
 	{
 		controller = c;
 		CppSrcFile = s;
 		numStepsToRun = numSteps;
 		this.symbolTable = stab;
 	}
 
 	public Interpreter(Controller c, String s, int numSteps){
 		controller = c;
 		CppSrcFile = s;
 		numStepsToRun = numSteps;
 		symbolTable = new SymbolTable();
 		
 	}
 
 	// -----------------------------------------------------------------------
 	// Interprete
 
 	@Override
 	public void run() {
 		// TODO Auto-generated method stub
 
 		try {
 			runAll();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		controller.consoleOut("run ended");
 		controller.setInterpretingDone();
 	}
 
 	public String runAll() throws IOException {
 
 		interpretation = "";
 
 		lexer = new Lexer();
 		lexer.loadSourceFile(CppSrcFile);
 
 		boolean good = prescan1();
 		if(good == false){
 			return interpretation;
 			}
 
 		lexer.index = 0;
 		func_index = 0;
 		func_table = new func_type[NUM_FUNC];
 
 		int index = -1;
 		try {
 			prescan();
 		} catch (SyntaxError e) {
 			controller.consoleOut("Syntax Error: "+e.toString()+" at line: " + e.getLine()+'\n');
 		} /* find the location of all functions and global variables in the program */
 
 		if(!isUserFunc("main")){
 			controller.consoleOut("Syntax Error: main() not found");
 			return interpretation;
 		}
 		ArrayList<var_type> args = new ArrayList<var_type>();
 		try {
 			index = find_func("main", args);
 		} catch (SyntaxError e1) {
 			controller.consoleOut("Syntax Error: main() not found"+'\n');
 			return interpretation;
 		} //set up call to main		
 		lexer.index = func_table[index].location;
 		symbolTable.pushFuncScope("main");
 
 		//TODO: need special call main
 		try {
 			interp_block(block_type.FUNCTION, true);
 		} catch (StopException e) {
 		} catch (SyntaxError e) {
 			controller.consoleOut("Syntax Error: "+e.toString()+" at line: " + e.getLine()+'\n');
 		}
 		symbolTable.popScope();
 
 		return interpretation;
 	}
 
 	public void test() throws IOException, SyntaxError {
 		lexer = new Lexer();
 		lexer.loadSourceFile("testCode.cpp");
 
 		token = lexer.get_token();
 		while(token.key!=keyword.FINISHED){
 			lexer.get_token();
 			System.out.println(token.value);
 		}
 
 	}
 
 	//TODO I used charAt(0) alot without additional checks... This is probably bad
 
 	public void sntx_err(String s) throws SyntaxError {
 	throw new SyntaxError(s,lexer.getLineNum(),lexer.getColumnNum());
 	}
 
 	//TODO:
 	public void warning(){
 		@SuppressWarnings("unused")
 		int a =1/0;
 	}
 
 	/* Interpret a single statement or block of code. When
 	 interp_block() returns from its initial call, the final
 	 brace (or a return) in main() has been encountered.
 	 */
 	//TODO need to allow variables to be declared in a block and not accessible outside it!!!
 	//TODO same with if/while/etc
 	public return_state interp_block(block_type b_type, boolean needsReturn) throws StopException, SyntaxError {
 		int block = 0;
 
 		/* If interpreting single statement, return on
 		first semicolon.*/
 		do {
 
 			synchronized(this){
 				while(numStepsToRun==0 && !StopRun){
 					try{
 						wait();				
 					}
 					catch(InterruptedException e) {}
 				}
 				if(numStepsToRun>0){
 					numStepsToRun--;
 				}
 				if(StopRun) throw new StopException(0);
 			}
 
 			token = lexer.get_token();
 			
 			//TODO FIX SO CAN OPEN A NEW BLOCK
 			/* see what kind of token is up */
 			if(token.type==token_type.BLOCK) { /* if block delimiter */
 				if(token.value.charAt(0) == '{' && block==0){ /* is a block */
 				block = 1;/* interpreting block, not statement */
 				addSteps(1); // add step (curly brace isn't counted as a step
 				}
 				else if(token.value.equals("{") && block!=0)
 					sntx_err("TODO THIS IS VALID SYNTAX, not allowed in citrin");
 				else{
 				addSteps(1); // add step (curly brace isn't counted as a step
 				if(block==0)
 					sntx_err("Expecting an expression before }");
 				return return_state.END_OF_BLOCK; /* is a }, so return */
 				}
 			}
 			else if(token.type == token_type.IDENTIFIER || token.type == token_type.NUMBER || token.type==token_type.CHAR 
 					|| token.type==token_type.OPERATOR || token.value.equals("(") ) {
 			/* Not a keyword, so process expression. */
 			lexer.putback(); /* restore token to input stream for
 								further processing by eval_exp() */
 			//evaluate expression
 			try {
 				evalOrCheckExpression(false);
 			} catch (StopException e) {
 				return return_state.STOP;
 			} 
 			token = lexer.get_token();
 
 			if(token.value.charAt(0)!=';') sntx_err("Expecting semi colon");
 			}
 			else if(token.value.equals(";")){
 				// empty statement do nothing
 			}
 			else /* is keyword */
 				switch(token.key) {
 
 				case SHORT:
 				case FLOAT:
 				case BOOL:
 				case DOUBLE:
 				case CHAR:
 				case INT: // declare local variables
 					lexer.putback();
 					try {
 					decl_var();
 				} catch (StopException e) {
 					return return_state.STOP;
 				}
 					break;
 				case RETURN: // return from function call 
 					if(b_type == block_type.FUNCTION)
 						func_ret();
 					else{
 						// if in a conditional block (if,while,..) put the return token back
 						// return to previous interp_block statement
 						// TODO: this seems like a hack... needs to be improved
 						lexer.putback();
 						synchronized(this){
 							if(numStepsToRun>=0)
 							numStepsToRun++;
 						}
 					}
 					return return_state.FUNC_RETURN;
 				case IF: // process an if statement 
 					exec_if();
 					break;
 				/*case ELSE: // process an else statement 
 					find_eob(); // find end of else block and continue execution 
 					break;*/
 				case WHILE: // process a while loop 
 					exec_while();
 					break;
 				case DO: // process a do-while loop 
 					exec_do();
 					break;
 				default:
 					sntx_err("CITRIN doesnt recognize this statement: "+token.value);
 				}
 
 		} while (token.key != keyword.FINISHED && block!=0);
 
 	return return_state.END_OF_BLOCK;
 
 }
 
 
 void printVarVal(var_type v){
 	String message = "";
 	if(v.v_type == keyword.INT || v.v_type == keyword.SHORT || v.v_type == keyword.BOOL )
 		message += ( v.var_name + " = " + v.value.intValue() + "\n");
 	else if(v.v_type == keyword.CHAR)
 		message += ( v.var_name + " = " + (char)(v.value.intValue()) + "\n");			
 	else if(v.v_type == keyword.FLOAT || v.v_type  == keyword.DOUBLE)
 		message += ( v.var_name + " = " + v.value.doubleValue() + "\n");
 
 	controller.consoleOut(message);
 
 }
 
 
 public void run_err() {
 	interpretation += "Run Error";
 	// System.out.println("Run Error");
 	// TODO Auto-generated method stub
 
 }
 
 
 //TODO need to allow variables to be declared in a block and not accessible outside it!!!
 private boolean check_block(){
 	int block = 0;
 	boolean syntaxGood = true;
 
 	/* If interpreting single statement, return on
 	first semicolon.*/
 	do {
 
 		try {
 			token = lexer.get_token();
 		} catch (SyntaxError e) {
 			controller.consoleOut(e.toString()+" at line: "+e.getLine()+'\n');
 			syntaxGood = false;
 		}
 		//TODO FIX SO CAN OPEN A NEW BLOCK
 		/* see what kind of token is up */
 		if(token.type==token_type.BLOCK) { /* if block delimiter */
 			if(token.value.charAt(0) == '{' && block==0){ /* is a block */
 				block = 1;/* interpreting block, not statement */
 			}
 			else if(token.value.equals("{") && block!=0){
 				controller.consoleOut("TODO: THIS IS VALID SYNTAX NOT allowed in CIRIN at line: "+lexer.getLineNum());
 				syntaxGood = false;
 			}
 			else{
 				if(block==0){
 					controller.consoleOut("Expecting an expression before } at line: "+lexer.getLineNum() + '\n');
 					syntaxGood = false;
 					return syntaxGood;
 					}
 				else{
 					return syntaxGood;
 				}
 			}
 		}
 		else if(token.type == token_type.IDENTIFIER || token.type == token_type.NUMBER || token.type==token_type.CHAR 
 				|| token.type==token_type.OPERATOR || token.value.equals("(") ) {
 			/* Not a keyword, so process expression. */
 			lexer.putback(); /* restore token to input stream for
 							further processing by eval_exp() */
 			//evaluate
 			try {
 				evalOrCheckExpression(false);
 				token = lexer.get_token();
 				if(token.value.charAt(0)!=';'){ 
 					controller.consoleOut("Expecting semicolon before line: "+lexer.getLineNum()+ 
 							" column: " + lexer.getColumnNum()+'\n');
 					lexer.putback();
 					syntaxGood = false;
 				}
 			} catch (StopException e) {} 
 			catch (SyntaxError e) {
 				controller.consoleOut(e.toString()+" at line: " +e.getLine()+'\n');
 				syntaxGood = false;
 				findEndOfStatement();
 			} 
 		}
 		else if(token.value.equals(";")){
 			// empty statement do nothing
 		}
 		else if(token.key!=null) /* is keyword */
 		switch(token.key) {
 
 		case SHORT:
 		case FLOAT:
 		case BOOL:
 		case DOUBLE:
 		case CHAR:
 		case INT: // declare local variables
 			lexer.putback();
 			try {
 				decl_var();
 			} catch (StopException e) {}
 			catch(SyntaxError e){
 				controller.consoleOut(e.toString()+" at line: "+e.getLine()+'\n');
 				syntaxGood = false;
 			}
 			break;
 		case RETURN: // return from function call 
 			syntaxGood = syntaxGood && check_func_ret();
 			break;
 		case IF: // process an if statement 
 			syntaxGood = syntaxGood && check_if();
 			break;
 		/*case ELSE: // process an else statement 
 			find_eob(); // find end of else block and continue execution 
 			break;*/
 		case WHILE: // process a while loop 
 			syntaxGood = syntaxGood && check_while();
 			break;
 		case DO: // process a do-while loop 
 			syntaxGood = syntaxGood && check_do();
 			break;
 		default:
 			if(token.key != keyword.FINISHED){
 				controller.consoleOut("CITRIN DOESNT RECOGNIZE THIS STATEMENT: " + token.value+" at line: " +lexer.getLineNum()+'\n');
 				syntaxGood = false;
 			}
 		}
 		else{
 			controller.consoleOut("CITRIN DOESNT RECOGNIZE THIS STATEMENT: " + token.value+" at line: " +lexer.getLineNum()+'\n');
 			syntaxGood = false;
 		}
 
 	} while (token.key != keyword.FINISHED && block!=0);
 
 	return syntaxGood;
 
 }
 
 
 //TODO add int a(5) declaration option
 private void decl_var() throws StopException, SyntaxError{
 	token = lexer.get_token(); /* get type */
 	keyword type = token.key;
 	var_type value;
 
 	do { /* process comma-separated list */
 		var_type i = new var_type();
 		i.v_type = type;
 		i.value = 0; /* init to 0 should remove this*/
 		token = lexer.get_token(); /* get var name */
 		SymbolLocation loc = new SymbolLocation(lexer.getLineNum(),lexer.getColumnNum());
 		i.var_name = new String(token.value);
 
 		//check for initialize
 		token = lexer.get_token();
 		if(token.value.equals("=")){ //initialize
 			value = evalOrCheckExpression(true);
 			if(!checkOnly){
 				i.assignVal(value);
 			}
 		}
 		else{
 			lexer.putback();
 		}
 
 		//push var onto stack
 		SymbolDiagnosis d = symbolTable.pushSymbol(new Symbol(loc, i));
 		if(d == SymbolDiagnosis.Conflict){
 			SymbolLocation conflictLocation = symbolTable.findVar(i.var_name).location;
 			sntx_err("Variable name \""+i.var_name+"\" conflicts with previous declaration at line " + 
 			conflictLocation.lnum + " of \"" + i.var_name + "\"");
 		}
 		if(!checkOnly)
 			printVarVal(i);
 
 		token = lexer.get_token();
 	} while( token.value.charAt(0) == ',');
 	if(token.value.charAt(0) != ';'){
 		sntx_err("Semicolon expected");
 		lexer.putback();
 	}
 }
 
 
 
 	void exec_while() throws StopException, SyntaxError{
 		var_type cond;
 		int cond_index;
 		return_state r;
 
 		lexer.putback(); // go back to top of loop
 		cond_index = lexer.index; //save top of loop location
 		token = lexer.get_token(); // read in "while" token again
 
 		cond = evalOrCheckExpression(false); //evaluate the conditional statement
 
 		if(cond.value.doubleValue()!=0){	// if any bit is not 0
 			r =interp_block(block_type.CONDITIONAL, false);  // execute loop
 			if(r==return_state.FUNC_RETURN)
 				return;
 		}
 		else{
			find_eob(); //find the end of the loop
 			return;
 		}
 
 		lexer.index = cond_index; // loop back to top
 	}
 
 	boolean check_while() {
 		boolean syntaxGood = true;
 		try {
 			evalOrCheckExpression(false); //evaluate the conditional statement
 		} catch (SyntaxError e) {
 			controller.consoleOut(e.toString()+" at line: "+e.getLine()+'\n');
 			syntaxGood = false;
 			findEndOfStatement();
 		} catch (StopException e) {	} 
 
 		syntaxGood = syntaxGood && check_block();
 
 		return syntaxGood;
 	}
 
 	void exec_if() throws StopException, SyntaxError{
 		var_type cond;
 		return_state r;
 		cond = evalOrCheckExpression(false); //evaluate the conditional statement
 
 		if(cond.value.doubleValue()!=0){	// if any bit is not 0
 			r = interp_block(block_type.CONDITIONAL, false);	// execute block
 			if(r == return_state.FUNC_RETURN)
 				return;
 
 			token = lexer.get_token();
 			if(token.key==keyword.ELSE){
 				token = lexer.get_token();
 				if(!token.value.equals("{")){
 					findEndOfStatement();
 				}
 				else{
 					find_eob();					
 				}
 			}
 			else{
 				lexer.putback();
 			}
 		}
 		else{ //skip around block, check for else
			find_eob(); //find the end of the loop
 			token = lexer.get_token();
 
 			if(token.key != keyword.ELSE){
 				lexer.putback();
 			}
 			else{
 				r = interp_block(block_type.CONDITIONAL, false);
 				if(r == return_state.FUNC_RETURN)
 					return;
 			}
 		}
 
 	}
 
 	boolean check_if() {
 		boolean syntaxGood = true;
 		try {
 			evalOrCheckExpression(false); //evaluate the conditional statement
 		} catch (SyntaxError e) {
 			controller.consoleOut(e.toString()+" at line: "+e.getLine()+'\n');
 			syntaxGood = false;
 			findEndOfStatement();
 		} catch (StopException e) {	} 
 
 			syntaxGood = syntaxGood && check_block();
 
 			try {
 				token = lexer.get_token();
 				if(token.key == keyword.ELSE){
 					syntaxGood = syntaxGood && check_block();
 				}
 				else{
 					lexer.putback();
 				}
 			} catch (SyntaxError e) {
 				controller.consoleOut(e.toString()+"at line: "+e.getLine()+'\n');
 				syntaxGood = false;
 		}
 
 		return syntaxGood;
 	}
 
 	private void findEndOfStatement() {
 	do{
 		try {
 			token = lexer.get_token();
 		} catch (SyntaxError e) {
 			controller.consoleOut(e.toString()+" at line: "+e.getLine()+'\n');
 		}
 	}while(!token.value.equals(";") && !token.value.equals("}") && !token.value.equals("{") && token.type!=token_type.FINISHED);
 	if(token.value.equals("}")||token.value.equals("{"))
 		lexer.putback();
 }
 
 void exec_do() throws StopException, SyntaxError{
 		var_type cond;
 		return_state r;
 		int do_index;
 
 		lexer.putback(); // go back to top of loop
 		do_index = lexer.index; //save top of loop location
 		token = lexer.get_token(); // read in "do" token again
 		r = interp_block(block_type.CONDITIONAL,false); //interpret loop
 		if(r == return_state.FUNC_RETURN)
 			return;
 
 		token = lexer.get_token();
 		if(token.key!=keyword.WHILE) sntx_err("while expected to end do block");
 
 		cond = evalOrCheckExpression(false); //evaluate the conditional statement
 
 		if(cond.value.doubleValue()!=0)  // if any bit is not 0
 			lexer.index = do_index; //loop back
 }
 
 boolean check_do() {
 	boolean syntaxGood = true;
 
 	syntaxGood = syntaxGood && check_block();
 
 	try {
 		evalOrCheckExpression(false); //evaluate the conditional statement
 	} catch (SyntaxError e) {
 		controller.consoleOut(e.toString()+" at line: "+e.getLine()+'\n');
 		syntaxGood = false;
 		findEndOfStatement();
 	} catch (StopException e) {	} 
 
 	try {
 		token = lexer.get_token();
 		if(token.key!=keyword.WHILE) 
 			sntx_err("while expected to end do block");
 		evalOrCheckExpression(false);
 	} catch (SyntaxError e) {
 		controller.consoleOut(e.toString()+" at line: "+e.getLine()+'\n');
 		syntaxGood = false;
 	} catch (StopException e) {}
 
 
 		return syntaxGood;
 }
 
 
 	void find_eob() throws SyntaxError{
 		int brace_count = 1;
 
 		//TODO check this, next line isn't needed
 		token = lexer.get_token();
 
 		do{
 			token = lexer.get_token();
 			if(token.value.charAt(0) == '{')
 				brace_count++;
 			else if(token.value.charAt(0) == '}')
 				brace_count--;
 
 		} while(brace_count>0);
 
 	}
 
 	// Return index of internal library function or -1 if not found.
 	public int internal_func(String s)
 	{
 		//TODO
 		//for(int i=0; intern_func[i].f_name[0]; i++) {
 		//if(!strcmp(intern_func[i].f_name, s)) return i;
 		//}
 		return -1;
 	}
 
 	private boolean prescan1()
 	{
 		checkOnly = true;
 		boolean syntaxGood = true;
 		int oldIndex = lexer.index;
 		int tempIndex;
 		String temp;
 		keyword datatype;
 		int tempNumStepsToRun = numStepsToRun;
 		numStepsToRun = -1;
 		func_index = 0;
 
 		do {
 			tempIndex = lexer.index;
 			try {
 			token = lexer.get_token();
 		} catch (SyntaxError e) {
 			controller.consoleOut(e.toString()+" at line: "+e.getLine()+'\n');
 			syntaxGood = false;
 		}
 			//this token is var type or function type?
 			if(token.key==keyword.INT || token.key==keyword.CHAR || 
 					token.key==keyword.SHORT || token.key==keyword.BOOL ||
 					token.key==keyword.FLOAT || token.key==keyword.DOUBLE || token.key==keyword.VOID) {
 				datatype = token.key; //save data type
 				try {
 				token = lexer.get_token();
 			} catch (SyntaxError e) {
 				controller.consoleOut(e.toString()+" at line: "+e.getLine()+'\n');
 				syntaxGood = false;
 			}
 				if(token.type == token_type.IDENTIFIER) {
 					temp = new String(token.value);
 					try {
 					token = lexer.get_token();
 				} catch (SyntaxError e) {
 					controller.consoleOut(e.toString()+" at line: "+e.getLine()+'\n');
 					syntaxGood = false;
 				}
 					if(token.value.charAt(0) != '(') { //must be a global var
 						lexer.index = tempIndex; //return to start of declaration
 						try {
 						decl_var();
 					} catch (SyntaxError e) {
 						controller.consoleOut(e.toString()+" at line: "+e.getLine()+'\n');
 						syntaxGood = false;
 					} catch (StopException e) { }
 					}
 					else{ //must be a function
 						try {
 						func_table[func_index] = new func_type();
 						func_table[func_index].ret_type = datatype;
 						func_table[func_index].func_name = new String(temp);
 						func_table[func_index].params = get_params();
 						func_table[func_index].location = lexer.index;
 						func_index++; 
 						// now at opening curly brace of function
 						} catch (SyntaxError e) {
 							controller.consoleOut(e.toString()+" at line: "+e.getLine()+'\n');
 							syntaxGood = false;
 							findEndOfStatement();
 						}
 						try{
 							token = lexer.get_token();
 							if(!token.value.equals("{"))
 								sntx_err("There must be a { before the openening statement to a function "+token.value);
 							lexer.putback();
 						} catch (SyntaxError e) {
 							controller.consoleOut(e.toString()+" at line: "+e.getLine()+'\n');
 							syntaxGood = false;
 						}
 						symbolTable.pushFuncScope(func_table[func_index-1].func_name);
 							ArrayList<var_type> params = func_table[func_index-1].params; //get set of params
 							for(int i=0;i<params.size();i++){
 								var_type v = new var_type();
 								v.v_type = params.get(0).v_type;
 								v.var_name = params.get(0).var_name;
 								SymbolLocation loc = new SymbolLocation(-1,-1);//TODO parameter, should do something
 								symbolTable.pushSymbol(new Symbol(loc, v));
 							}  
 
 							syntaxGood = syntaxGood && check_block();					
 							symbolTable.popScope();
 
 					}
 				}
 			}
 			else if(token.key!=keyword.FINISHED) controller.consoleOut("Unkown data type or command: "
 			+token.value+" at line: "+lexer.getLineNum()+'\n');
 		} while(token.key!=keyword.FINISHED);
 		lexer.index = oldIndex;
 		checkOnly = false;
 		numStepsToRun = tempNumStepsToRun;
 		return syntaxGood;
 	}
 
 
 	/* Find the location of all functions in the program
 	and store global variables. */
 	// TODO: I should do something about functions as parameters, which may have functions as parameters, recursive..
 	// TODO: add ability to have default parameters...
 	// TODO: add ability to have definition and implementation for functions
 	private void prescan() throws SyntaxError
 	{
 		int oldIndex = lexer.index;
 		int tempIndex;
 		String temp;
 		keyword datatype;
 		int brace = 0; //When 0, this var tells us that current source position is outside of a function
 		func_index = 0;
 
 		do {
 			while(brace>0 && token.key!=keyword.FINISHED) { //skip code inside functions
 				//index = 205;
 				token = lexer.get_token();
 				if(token.value.equals("{"))  brace++;
 				if(token.value.equals("}"))  brace--;
 			}
 
 			tempIndex = lexer.index;
 			token = lexer.get_token();
 			//this token is var type or function type?
 			if(token.key==keyword.INT || token.key==keyword.CHAR || 
 					token.key==keyword.SHORT || token.key==keyword.BOOL ||
 					token.key==keyword.FLOAT || token.key==keyword.DOUBLE || token.key==keyword.VOID) {
 				datatype = token.key; //save data type
 				token = lexer.get_token();
 				if(token.type == token_type.IDENTIFIER) {
 					temp = new String(token.value);
 					token = lexer.get_token();
 					if(token.value.charAt(0) != '(') { //must be a global var
 						lexer.index = tempIndex; //return to start of declaration
 						try {
 							decl_var();
 						} catch (StopException e) { }
 					}
 					else{ //must be a function
 						func_table[func_index] = new func_type();
 						func_table[func_index].ret_type = datatype;
 						func_table[func_index].func_name = new String(temp);
 						func_table[func_index].params = get_params();
 						func_table[func_index].location = lexer.index;
 						func_index++;
 						// now at opening curly brace of function
 					}
 				}
 			}
 			else if(token.value.equals("{"))	brace++;
 			else if(token.key!=keyword.FINISHED) sntx_err("Unkown data type or command: "+token.value);
 		} while(token.key!=keyword.FINISHED);
 		lexer.index = oldIndex;
 	}
 
 	/* calls the function whose name is in token, 
 	 index should be after the function name (at the open parenthesis, 
 	 before the arguments to the function)*/
 	var_type call(String func_name) throws StopException, SyntaxError{
 		int loc, temp;
 		ret_value = null;
 		ArrayList<var_type> args, params;
 		args = get_args();
 		int func_index;
 
 		//find function
 		func_index = find_func(func_name,args);
 
 		// set start of function
 		loc = func_table[func_index].location;
 		if(loc < 0){
 			sntx_err("Function: "+func_name+"has not been defined");
 		}
 		else {
 			temp = lexer.index; //save return location
 			symbolTable.pushFuncScope(func_name);
 			lexer.index = loc; //reset prog to start of function
 			params = func_table[func_index].params; //get set of params
 			putParamsOnStack(args,params);
 			if(func_table[func_index].ret_type==keyword.VOID)
 				interp_block(block_type.FUNCTION,false); //run the function
 			else
 				interp_block(block_type.FUNCTION,true); //run the function
 
 			lexer.index = temp; //reset the program index
 			symbolTable.popScope();
 		}
 
 		//TODO VOID FUNCTIONS!
 		/*
 		if(func_table[func_index].ret_type = keyword.VOID){
 			return null;
 		}*/
 		var_type v = new var_type();
 		v.v_type = func_table[func_index].ret_type;
 		v.assignVal(ret_value);
 		return v;
 	}
 
 
 
 	var_type checkCall(String func_name) throws StopException, SyntaxError{
 		ret_value = null;
 		ArrayList<var_type> args;
 		args = get_args();
 		int func_index;
 
 		//find function
 		func_index = find_func(func_name,args);
 
 		//TODO VOID FUNCTIONS!
 		/*
 		if(func_table[func_index].ret_type = keyword.VOID){
 			return null;
 		}*/
 		var_type v = new var_type();
 		v.v_type = func_table[func_index].ret_type;
 		return v;
 	}
 
 	void putParamsOnStack(ArrayList<var_type> args, ArrayList<var_type> params) throws SyntaxError{
 
 		if(args.size()!=params.size()){
 			sntx_err("BAD MATCH, this is probably a bug in CITRIN");
 		}
 		for(int i=0;i<params.size();i++){
 			var_type v = new var_type();
 			v.v_type = params.get(0).v_type;
 			v.var_name = params.get(0).var_name;
 			v.assignVal(args.get(0));
 			printVarVal(v);
 			SymbolLocation loc = new SymbolLocation(-1,-1);//TODO parameter, should do something
 			symbolTable.pushSymbol(new Symbol(loc, v));
 		}  
 	}
 
 
 
 	// get arguments from function call
 	ArrayList<var_type> get_args() throws StopException, SyntaxError{
 		var_type value; 
 		ArrayList<var_type> args = new ArrayList<var_type>();
 
 		token = lexer.get_token();
 		if(token.value.charAt(0) != '(') sntx_err("Expected (");
 
 		//check if the function has args
 		token = lexer.get_token();
 		if(!token.value.equals(")")){
 			lexer.putback();
 			//process comma separated list of values
 			do{
 			value = evalOrCheckExpression(true); 
 			args.add(value); // save value temporarily
 			token = lexer.get_token();
 			} while(token.value.charAt(0) == ',');
 			if(!token.value.equals(")"))
 				sntx_err("Expected )");
 		}
 		return args;
 	}
 
 	ArrayList<var_type> get_params() throws SyntaxError{
 		var_type p;
 		ArrayList<var_type> params = new ArrayList<var_type>();
 
 		do { //process comma separated list of params
 			token = lexer.get_token();
 			if(token.value.charAt(0)!=')') {
 				p = new var_type();
 				if(token.key != keyword.INT && token.key != keyword.CHAR && 
 						token.key != keyword.FLOAT && token.key != keyword.DOUBLE && 
 						token.key != keyword.BOOL && token.key != keyword.SHORT)
 					sntx_err("Type Expected");
 
 				p.v_type = token.key;
 
 				token = lexer.get_token();
 				if(token.type != token_type.IDENTIFIER){
 				 sntx_err("Identifier Expected");
 				}
 				p.var_name = token.value;
 				params.add(p);
 
 				token = lexer.get_token();
 			}
 			else break;
 		} while(token.value.charAt(0) == ',');
 		if(!token.value.equals(")")) sntx_err("Expected )");
 
 		return params;
 	}
 
 
 	//return from a function. sets ret_value to the returned value
 	void func_ret() throws StopException, SyntaxError{
 		var_type value = null;
 		//get return value (if any)
 		value = evalOrCheckExpression(false);
 
 		token = lexer.get_token();
 		if(!token.value.equals(";")){
 			sntx_err("Expected ;");
 		}
 
 		//TODO function should change return value to the correct type 
 		//TODO need to do something about void functions
 
 		ret_value = new var_type(value);	
 	}
 
 	boolean check_func_ret(){
 		boolean syntaxGood = true;
 		var_type value = null;
 		//get return value (if any)
 		try {
 			value = evalOrCheckExpression(false);
 			token = lexer.get_token();
 			if(!token.value.equals(";")){
 					sntx_err("Expected ;");
 		}
 		} catch (SyntaxError e) {
 			controller.consoleOut(e.toString()+" at line: "+e.getLine()+'\n');
 			syntaxGood = false;
 			findEndOfStatement();
 		} catch (StopException e) {}
 			//TODO function should change return value to the correct type 
 			//TODO need to do something about void functions		
 			ret_value = new var_type(value); 
 			return syntaxGood;
 	}
 
 	boolean isUserFunc(String name){
 		int i;
 		for(i=0; i < func_index; i++)
 			if(name.equals(func_table[i].func_name))
 				return true;
 
 		return false;		
 
 	}
 
 
 	ArrayList<Integer> find_func_indexes(String name,  ArrayList<var_type> args){
 		ArrayList<Integer> indexes = new ArrayList<Integer>();
 		for(int i=0; i < func_index; i++)
 			if(name.equals(func_table[i].func_name) && args.size()==func_table[i].params.size() )
 				indexes.add(i);
 
 		return indexes;
 	}
 
 	// returns index of function in func_table
 	// matches to the best match as described here:
 	//http://www.learncpp.com/cpp-tutorial/76-function-overloading/
 	int find_func(String name, ArrayList<var_type> args) throws SyntaxError{
 		ArrayList<Integer> indexes = find_func_indexes(name, args);
 
 		// remove possible matches that we can't convert args to
 		for(int j=0;j<indexes.size();++j){
 			for(int k=0; k<args.size(); ++k){
 				var_type arg = args.get(k);
 				var_type param = func_table[indexes.get(j)].params.get(k);
 				if(!arg.canConvertTo(param.v_type)){
 					indexes.remove(j);
 					j--;
 				}
 			}  
 		}
 
 		if(indexes.size()==0){ // no matches\
 			String err = "No matching function for call to "+name+"(";
 			for(int i=0;i<args.size();i++){
 				if(i>0)
 					err+=", ";
 				err+=args.get(i).getName();
 			}
 			err+=")";
 			sntx_err(err);
 			return -1;
 		}
 		if(indexes.size()==1) // single match
 			return indexes.get(0);
 
 		// Try to disambiguate overloaded function
 		// find best match
 		int bestMatch = -1;
 		int bestNumPerfectMatches = -1;
 		int bestNumPromotedMatches = -1;
 		boolean isBetterThan2nd = false;
 
 		// to do this, first determine best match
 		// (most perfect matches, ties determined by most promoted matches)
 		for(int j=0;j<indexes.size();++j){ //for each function
 			int numPerfectMatches = 0;
 			int numPromotedMatches = 0;
 
 			for(int k=0; k<args.size(); ++k){ //for each arg
 				var_type arg = args.get(k);
 				var_type param = func_table[indexes.get(j)].params.get(k);
 				if(arg.v_type==param.v_type){
 					numPerfectMatches++;
 				}
 				else if(arg.getPromotedType() == param.v_type){
 					numPromotedMatches++;
 				}
 			}
 
 			// check if best
 			if(numPerfectMatches > bestNumPerfectMatches || 
 					(numPerfectMatches == bestNumPerfectMatches && numPromotedMatches>bestNumPromotedMatches)){
 				bestNumPerfectMatches = numPerfectMatches;
 				bestNumPromotedMatches = numPromotedMatches;
 				bestMatch = j;
 				isBetterThan2nd=true;
 
 			}
 			//check if old best isn't better than it
 			else if(numPerfectMatches == bestNumPerfectMatches && 
 					numPromotedMatches == bestNumPromotedMatches){
 				isBetterThan2nd=false;				
 			}
 
 
 		}
 
 		if(!isBetterThan2nd){ //ambiguous: no clear best
 			sntx_err("This function call is abmiguous");
 			return -1;
 		}
 
 
 		// now make sure best function is at least as good as all others for each arg
 		for(int j=0;j<indexes.size();++j){ //for each function
 			if(j==bestMatch) continue;
 
 			for(int k=0; k<args.size(); ++k){ //for each arg
 				var_type arg = args.get(k);
 				var_type param = func_table[indexes.get(j)].params.get(k);
 				var_type paramFromBest = func_table[indexes.get(bestMatch)].params.get(k);
 
 				if(paramFromBest.v_type == arg.v_type) //best is at least as good
 					continue;
 				if(param.v_type == arg.v_type || 
 						(arg.getPromotedType() == param.v_type && arg.getPromotedType() != paramFromBest.v_type) )
 				{
 					sntx_err("This function call is abmiguous");
 					return -1;
 				}
 			}
 		}
 
 
 		return indexes.get(bestMatch);
 	}
 
 	private var_type evalOrCheckExpression(boolean commasAreDelimiters) throws SyntaxError, StopException {
 		ExpressionEvaluator exp = new ExpressionEvaluator(this, symbolTable);
 		if(checkOnly)
 			return exp.check_expr(commasAreDelimiters);
 		else
 			return exp.eval_exp(commasAreDelimiters);
 	}
 
 	public synchronized void stop(){
 		StopRun = true;
 		notifyAll();
 	}
 
 	public synchronized void addSteps(int s){
 		if(numStepsToRun>=0)
 			numStepsToRun+=s;
 		notifyAll();
 	}
 
 	public synchronized void runToBreak(){
 		numStepsToRun = -1;
 		notifyAll();
 	}
 
 	public class commands {
 		public commands(String c, keyword t){
 			command = c;
 			tok = t;
 		}
 	public String command;
 	public keyword tok;
 	}
 
 	public class func_type {
 		public String func_name;
 		public keyword ret_type;
 		public int location;
 		public ArrayList<var_type> params;//TODO 
 		public boolean overloaded;
 	}
 
 }
 
