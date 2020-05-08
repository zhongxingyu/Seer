 package com.hexcore.cas.rulesystems;
 
 import java.util.ArrayList;
 import com.hexcore.cas.rulesystems.TableEntry;
 import com.hexcore.cas.rulesystems.ConstantRecord;
 import com.hexcore.cas.rulesystems.SymbolTable;
 import com.hexcore.cas.rulesystems.PrimaryPair;
 import org.objectweb.asm.Label;
 
 import java.io.*;
 
 public class Parser {
 	static final int _EOF = 0;
 	static final int _number = 1;
 	static final int _double = 2;
 	static final int _postInc = 3;
 	static final int _postDec = 4;
 	static final int _identifier = 5;
 	// terminals
 	static final int EOF_SYM = 0;
 	static final int number_Sym = 1;
 	static final int double_Sym = 2;
 	static final int postInc_Sym = 3;
 	static final int postDec_Sym = 4;
 	static final int identifier_Sym = 5;
 	static final int ruleset_Sym = 6;
 	static final int lbrace_Sym = 7;
 	static final int rbrace_Sym = 8;
 	static final int typecount_Sym = 9;
 	static final int semicolon_Sym = 10;
 	static final int property_Sym = 11;
 	static final int type_Sym = 12;
 	static final int equal_Sym = 13;
 	static final int lparen_Sym = 14;
 	static final int rparen_Sym = 15;
 	static final int lbrack_Sym = 16;
 	static final int rbrack_Sym = 17;
 	static final int point_Sym = 18;
 	static final int if_Sym = 19;
 	static final int else_Sym = 20;
 	static final int loop_Sym = 21;
 	static final int from_Sym = 22;
 	static final int to_Sym = 23;
 	static final int var_Sym = 24;
 	static final int comma_Sym = 25;
 	static final int plus_Sym = 26;
 	static final int minus_Sym = 27;
 	static final int equalequal_Sym = 28;
 	static final int bangequal_Sym = 29;
 	static final int greater_Sym = 30;
 	static final int less_Sym = 31;
 	static final int greaterequal_Sym = 32;
 	static final int lessequal_Sym = 33;
 	static final int barbar_Sym = 34;
 	static final int star_Sym = 35;
 	static final int slash_Sym = 36;
 	static final int percent_Sym = 37;
 	static final int andand_Sym = 38;
 	static final int NOT_SYM = 39;
 	// pragmas
 
 	static final int maxT = 39;
 
 	static final boolean T = true;
 	static final boolean x = false;
 	static final int minErrDist = 2;
 
 	public static Token token;    // last recognized token   /* pdt */
 	public static Token la;       // lookahead token
 	static int errDist = minErrDist;
 
 	static enum RelOpE{LT, GT, LE, GE, EQ, NE, UN};
 static enum AddOpE{ADD, SUB, OR, UN};
 static enum MulOpE{MUL, DIV, MOD, AND, UN};
 static enum PostOpE{INC, DEC, UN};
 
 static SymbolTable table;
 static int typeCountExpected = 0;
 static int typeCountActual = 0;
 static boolean postOpQueued = false;
 static PostOpE postOpType;
 static boolean valid = true;
 static int typeCount = 0;
 
 static public int getErrorCount()
 {
 	return Errors.count;
 }
 
 
 static public ArrayList<String> getResult()
 {
 	ArrayList<String> results = Parser.getErrorList();
 	if(Parser.getErrorCount() == 0)
 		results.add("Compiled Successfully");
 	
 	return results;
 }
 
 
 static ArrayList<String> getErrorList()
 {
 	ArrayList<String> results = new ArrayList<String>();
 		
 	ErrorRec current = Errors.first;
 	
 	while(current != null)
 	{
 		results.add("Error at line " + current.line + ": " + current.str);
 		current = current.next;
 	}
 	
 	return results;
 }
 
 static void SemanticError(String msg)
 {
 	valid = false;
 	SemError(msg);
 	System.err.println("SEM ERROR");
 }
 
 static byte[] getCode()
 {
 	return CodeGen.getCode();
 }
 
 static public void reset()
 {
 	errDist = minErrDist;
 	Errors.count = 0;
 	Errors.first = null;
 	Errors.last = null;
 	Errors.eof = false;
 	valid = true;
 }
 
 
 
 	//Changed by Karl Zoller
 	static void SynErr (int n) {
 		System.err.println("SYN ERROR");
 		valid = false;
 		if (errDist >= minErrDist) Errors.SynErr(la.line, la.col, n);
 		errDist = 0;
 	}
 
 	public static void SemErr (String msg) {
 		if (errDist >= minErrDist) Errors.Error(token.line, token.col, msg); /* pdt */
 		errDist = 0;
 	}
 
 	public static void SemError (String msg) {
 		if (errDist >= minErrDist) Errors.Error(token.line, token.col, msg); /* pdt */
 		errDist = 0;
 	}
 
 	public static void Warning (String msg) { /* pdt */
 		if (errDist >= minErrDist) Errors.Warn(token.line, token.col, msg);
 		errDist = 0;
 	}
 
 	public static boolean Successful() { /* pdt */
 		return Errors.count == 0;
 	}
 
 	public static String LexString() { /* pdt */
 		return token.val;
 	}
 
 	public static String LookAheadString() { /* pdt */
 		return la.val;
 	}
 
 	static void Get () {
 		for (;;) {
 			token = la; /* pdt */
 			la = Scanner.Scan();
 			if (la.kind <= maxT) { ++errDist; break; }
 
 			la = token; /* pdt */
 		}
 	}
 
 	static void Expect (int n) {
 		if (la.kind==n) Get(); else { SynErr(n); }
 	}
 
 	static boolean StartOf (int s) {
 		return set[s][la.kind];
 	}
 
 	static void ExpectWeak (int n, int follow) {
 		if (la.kind == n) Get();
 		else {
 			SynErr(n);
 			while (!StartOf(follow)) Get();
 		}
 	}
 
 	static boolean WeakSeparator (int n, int syFol, int repFol) {
 		boolean[] s = new boolean[maxT+1];
 		if (la.kind == n) { Get(); return true; }
 		else if (StartOf(repFol)) return false;
 		else {
 			for (int i=0; i <= maxT; i++) {
 				s[i] = set[syFol][i] || set[repFol][i] || set[0][i];
 			}
 			SynErr(n);
 			while (!s[la.kind]) Get();
 			return StartOf(syFol);
 		}
 	}
 
 	static void CAL() {
 		table = new SymbolTable();
 		typeCount = 0;
 		
 		RuleSet();
 		if(typeCount != typeCountExpected)
 		{
 			SemanticError("Mismatch between declared and actual type count");
 		}
 		
 		if(valid)
 		{
 			CodeGen.endExecute();
 			CodeGen.endClass();
 		}
 		
 	}
 
 	static void RuleSet() {
 		String name = "";
 		table.pushScope();
 		table.prepare();
 		
 		Expect(ruleset_Sym);
 		name  = Ident();
 		if(valid)
 		{
 			CodeGen.initClass(name);
 			CodeGen.initExecute();
 		}
 		
 		Expect(lbrace_Sym);
 		TypeCount();
 		Property();
 		while (la.kind == property_Sym) {
 			Property();
 		}
 		TypeSpec();
 		while (la.kind == type_Sym) {
 			TypeSpec();
 		}
 		Expect(rbrace_Sym);
 	}
 
 	static String Ident() {
 		String name;
 		name = "";
 		
 		if (la.kind == identifier_Sym) {
 			Get();
 			name = token.val;
 			
 		} else if (la.kind == type_Sym) {
 			Get();
 			name = "type";
 		} else SynErr(40);
 		return name;
 	}
 
 	static void TypeCount() {
 		int value = 0;
 		Expect(typecount_Sym);
 		value  = IntConst();
 		if(value < 1)
 		{
 			SemanticError("At least one type of cell must be declared");
 		}
 		typeCountExpected = value;
 		
 		if(valid)
 			CodeGen.initFramework(typeCountExpected);
 		
 		Expect(semicolon_Sym);
 	}
 
 	static void Property() {
 		String name = "";
 		Expect(property_Sym);
 		name  = Ident();
 		if(table.find(name).type != TableEntry.noType) SemanticError("Identifier \"" + name + "\" already declared.");
 		TableEntry entry = new TableEntry();
 		entry.name = name;
 		entry.type = TableEntry.doubleType;
 		entry.kind = TableEntry.Property;
 		if(valid)
 			entry.offset = CodeGen.declareProperty(name);
 		table.insert(entry);
 		
 		Expect(semicolon_Sym);
 	}
 
 	static void TypeSpec() {
 		table.pushScope();  												
 		
 		Expect(type_Sym);
 		Expect(identifier_Sym);
 		if(typeCount >= typeCountExpected)
 		{
 			SemanticError("Too many types");
 		}
 		typeCount++;
 		if(valid)
 			CodeGen.initType();
 		
 		Expect(lbrace_Sym);
 		while (StartOf(1)) {
 			Statement();
 		}
 		Expect(rbrace_Sym);
 		table.popScope();
 		
 		if(valid)
 			CodeGen.endType();
 		
 	}
 
 	static int IntConst() {
 		int value;
 		Expect(number_Sym);
 		try
 		{
 			value = Integer.parseInt(token.val);
 		}
 		catch(NumberFormatException e)
 		{
 			value =  0;
 		}
 		
 		return value;
 	}
 
 	static void Statement() {
 		if (la.kind == lbrace_Sym) {
 			Block();
 		} else if (la.kind == identifier_Sym || la.kind == type_Sym) {
 			AssignCall();
 		} else if (la.kind == if_Sym) {
 			IfStatement();
 		} else if (la.kind == loop_Sym) {
 			Loop();
 		} else if (la.kind == var_Sym) {
 			VarDeclaration();
 		} else SynErr(41);
 	}
 
 	static void Block() {
 		Expect(lbrace_Sym);
 		table.pushScope();
 		while (StartOf(1)) {
 			Statement();
 		}
 		Expect(rbrace_Sym);
 		table.popScope();
 	}
 
 	static void AssignCall() {
 		TableEntry entry = null; int type = TableEntry.noType; int typeA = TableEntry.noType;
 		entry = Designator(false);
 		PostOpE T = PostOpE.UN;
 		if (la.kind == equal_Sym) {
 			Get();
 			type  = Expression();
 			if(!(TableEntry.isArith(entry.type) && TableEntry.isArith(type)) && !(TableEntry.isBool(entry.type) && TableEntry.isBool(type)))
 			{
 				SemanticError("Incompatable Types");
 			}
 			else if(!(TableEntry.isArray(entry.type) && TableEntry.isArray(type)) && !(!TableEntry.isArray(entry.type) && !TableEntry.isArray(type)))
 			{
 				SemanticError("Cannot mix scalar and array types in assignment");
 			}
 			
 			if(entry.immutable)
 			{
 				SemError("Cannot assign to immutable symbol");
 			}
 			
 			if(valid)
 			{
 				if(entry.kind == TableEntry.Variable)
 				{
 					if(TableEntry.isBool(type))
 						CodeGen.toDouble();
 					CodeGen.storeVariable(entry.offset);
 				}
 				else if(entry.kind == TableEntry.Property)
 				{
 					CodeGen.storeProperty(entry.offset);
 				}
 			}
 			
 		} else if (la.kind == postInc_Sym || la.kind == postDec_Sym) {
 			T  = PostOp();
 			if(!TableEntry.isArith(entry.type))
 				SemanticError("Cannot perform a post operation on a boolan type.");
 				
 			if(entry.immutable)
 			{
 				SemError("Cannot alter immutable symbol");
 			}
 				
 			if(entry.kind != TableEntry.Variable)
 			{
 				SemanticError("Can only perform post operation on a variable.");
 			}
 			else
 			{
 				if(valid)
 				{
 					if(T == PostOpE.INC)
 						CodeGen.performPostOp(entry.offset, 1);
 					else
 						CodeGen.performPostOp(entry.offset, -1);
 				}
 			}
 			
 		} else if (la.kind == lparen_Sym) {
 			Get();
 			typeA  = Arguments();
 			if(!TableEntry.isFunction(entry.kind))
 			{
 			SemanticError("Arguments can only be given to a function.");
 			}
 			
 			if(entry.kind == TableEntry.aFunction)
 			{
 			if(!TableEntry.isArray(typeA))
 			{
 			SemanticError("Invalid argument. Argument must be an array");
 			}
 			}
 			else
 			{
 			if(TableEntry.isArray(typeA))
 			{
 			SemanticError("Invalid argument. Argument must be a scalar type");
 			}
 			}
 			
 			Expect(rparen_Sym);
 		} else SynErr(42);
 		Expect(semicolon_Sym);
 	}
 
 	static void IfStatement() {
 		int type = TableEntry.noType;
 		Expect(if_Sym);
 		Expect(lparen_Sym);
 		type  = Expression();
 		Expect(rparen_Sym);
 		if (!TableEntry.isBool(type))
 		{
 			SemanticError("Expected a boolean result for the if statement's expression");
 		}
 		
 		Label[] pointers = null;	
 		if(valid)
 		{	
 			pointers = CodeGen.initIf();
 		}
 		
 		Statement();
 		if(valid)
 		{
 			CodeGen.jump(pointers[1]);
 			CodeGen.visitLabel(pointers[0]);
 		}
 		
 		if (la.kind == else_Sym) {
 			Get();
 			Statement();
 		}
 		if(valid)
 			CodeGen.visitLabel(pointers[1]);
 		
 	}
 
 	static void Loop() {
 		int type1 = 0, type2 = 0;
 		String name = "";
 		Label[] loopLabels = null;
 		TableEntry entry = new TableEntry();
 		
 		Expect(loop_Sym);
 		name  = Ident();
 		if(table.find(name).type != TableEntry.noType)
 		{
 			SemError("Redeclared identifier");
 		}
 		
 		if(valid)
 		{  														
 			entry.kind = TableEntry.Variable;
 			entry.name = name;
 			entry.type = TableEntry.intType;
 			entry.immutable = true;
 			table.insert(entry);
 			entry.offset = CodeGen.declareLoopVariables();
 		}
 		
 		Expect(from_Sym);
 		type1  = Expression();
 		Expect(to_Sym);
 		type2  = Expression();
 		if(!TableEntry.isArith(type1) || !TableEntry.isArith(type2))
 		{
 			SemError("Loop bounds must be arithmetic expressions");
 		}
 		
 		if(TableEntry.isArray(type1) || TableEntry.isArray(type2))
 		{
 			SemError("Loop bounds must be scalar");
 		}
 		
 		if(valid)
 		{
 			loopLabels = CodeGen.initLoop(entry.offset);
 		}
 		
		Statement();
 		if(valid)
 			CodeGen.endLoop(loopLabels, entry.offset);
 		
 	}
 
 	static void VarDeclaration() {
 		Expect(var_Sym);
 		OneVar();
 		while (la.kind == comma_Sym) {
 			Get();
 			OneVar();
 		}
 		Expect(semicolon_Sym);
 	}
 
 	static TableEntry Designator(boolean attr) {
 		TableEntry entry;
 		int type = TableEntry.noType;
 		int typeE = TableEntry.noType;
 		String name = "";
 		TableEntry entryA = null;
 		
 		name  = Ident();
 		entry = table.find(name);
 		if(entry.type == TableEntry.noType)
 			SemanticError("Undeclared identifier \"" + name + "\"");
 		else
 			type = entry.type;
 			
 		if(entry.kind == TableEntry.Property && attr == false)
 			SemanticError("Properties must be called with \"self\"");
 			
 			
 		if(entry.kind == TableEntry.Cell)
 			if(valid)
 				CodeGen.derefRef(entry.offset);
 			
 		
 		if (la.kind == lbrack_Sym) {
 			Get();
 			typeE  = Expression();
 			if((type % 2) == 0) SemanticError("Cannot index scalar type \"" + name +  "\"");
 			if(!TableEntry.isArith(typeE))
 			{
 			SemanticError("Index must be arithmetic");
 			}
 			
 			if(valid)
 			CodeGen.derefArrayRef();
 			
 			Expect(rbrack_Sym);
 			if(!TableEntry.isArray(type))
 			{
 				SemanticError("Can only index arrays");
 			}
 			TableEntry entryS = new TableEntry();
 			entryS.name = entry.name;
 			entryS.kind = entry.kind;
 			entryS.type = entry.type - 1;
 			entry = entryS;
 			
 		}
 		if (la.kind == point_Sym) {
 			Get();
 			entryA  = Attribute();
 			if(entry.kind != TableEntry.Cell) SemanticError("Only cells have attributes.");
 			if(entryA.kind != TableEntry.Property) SemanticError("Only declared properties can be used as cell attributes.");
 			TableEntry entryAA = new TableEntry();
 			entryAA.name = entryA.name;
 			entryAA.kind = entryA.kind;
 			entryAA.offset = entryA.offset;
 			if(TableEntry.isArray(entry.type))
 			{
 				entryAA.type = entryA.type + 1;
 			}
 			else
 			{
 				entryAA.type = entryA.type;
 			}
 			
 			entry = entryAA;
 			
 		}
 		return entry;
 	}
 
 	static int Expression() {
 		int type;
 		int type1, type2;
 		type = TableEntry.noType;
 		
 		type1  = AddExp();
 		RelOpE op = RelOpE.UN;
 		type = type1;
 		
 		if (StartOf(2)) {
 			op  = RelOp();
 			type2  = AddExp();
 			if(!(TableEntry.isArith(type1) && TableEntry.isArith(type2)) && !(TableEntry.isBool(type1) && TableEntry.isBool(type2)))
 			{
 				SemanticError("Type mismatch");
 				type = TableEntry.noType;
 			}
 				
 			type = TableEntry.boolType;
 			
 			if(valid)
 				CodeGen.performRelationalOp(op);										
 			
 		}
 		return type;
 	}
 
 	static PostOpE PostOp() {
 		PostOpE op;
 		op = PostOpE.UN;	
 		if (la.kind == postInc_Sym) {
 			Get();
 			op = PostOpE.INC;	
 		} else if (la.kind == postDec_Sym) {
 			Get();
 			op = PostOpE.DEC;	
 		} else SynErr(43);
 		return op;
 	}
 
 	static int Arguments() {
 		int type;
 		type = TableEntry.noType;
 		if (StartOf(3)) {
 			type  = Expression();
 		}
 		return type;
 	}
 
 	static TableEntry Attribute() {
 		TableEntry entry;
 		entry = Designator(true);
 		return entry;
 	}
 
 	static void OneVar() {
 		int type = TableEntry.noType; String name = "";
 		name  = Ident();
 		TableEntry entry = new TableEntry();
 		entry.name = "__new__";
 		
 		if(table.find(name).type != TableEntry.noType)
 			SemanticError("Identifier \"" + name + "\" already declared.");
 		else
 		{
 			entry.kind = TableEntry.Variable;
 			entry.name = name;
 			entry.offset = CodeGen.declareLocalVariable(name);
 		}
 		
 		if (la.kind == equal_Sym) {
 			Get();
 			type  = Expression();
 			if(!entry.name.equals("__new__"))
 			{
 				entry.kind = TableEntry.Variable;
 				entry.type = type;
 			}
 			
 			if(valid)
 				CodeGen.storeVariable(entry.offset);
 			
 		}
 		if(!entry.name.equals("__new__"))
 			table.insert(entry);
 		
 	}
 
 	static int AddExp() {
 		int type;
 		int type1, type2;
 		type = TableEntry.noType;
 		boolean negative = false;
 		
 		if (la.kind == plus_Sym || la.kind == minus_Sym) {
 			if (la.kind == plus_Sym) {
 				Get();
 			} else {
 				Get();
 				negative = true;
 			}
 		}
 		type1  = Term();
 		AddOpE op = AddOpE.UN;
 		type = type1;
 		
 		if(negative)
 		{
 			if(!TableEntry.isArith(type1))
 				SemanticError("Cannot negate a boolean type");
 			else
 				if(valid)
 					CodeGen.negate();
 		}
 		
 		while (la.kind == plus_Sym || la.kind == minus_Sym || la.kind == barbar_Sym) {
 			op  = AddOp();
 			type2  = Term();
 			switch(op)
 			{
 				case OR:
 					if(!TableEntry.isBool(type1) || !TableEntry.isBool(type2))
 						SemanticError("Boolean Types Required");
 					type = TableEntry.boolType;
 					break;
 				default:
 					if(!TableEntry.isArith(type1) || !TableEntry.isArith(type2))
 						SemanticError("Numeric Types Required");
 					if((type1 == TableEntry.intType) && (type2 == TableEntry.intType))
 						type = TableEntry.intType;
 					else
 						type = TableEntry.doubleType;
 			}
 			
 			if(valid)
 				CodeGen.performAddOp(op);
 			
 		}
 		return type;
 	}
 
 	static RelOpE RelOp() {
 		RelOpE op;
 		op = RelOpE.UN;	
 		switch (la.kind) {
 		case equalequal_Sym: {
 			Get();
 			op = RelOpE.EQ;	
 			break;
 		}
 		case bangequal_Sym: {
 			Get();
 			op = RelOpE.NE;	
 			break;
 		}
 		case greater_Sym: {
 			Get();
 			op = RelOpE.GT;	
 			break;
 		}
 		case less_Sym: {
 			Get();
 			op = RelOpE.LT;	
 			break;
 		}
 		case greaterequal_Sym: {
 			Get();
 			op = RelOpE.GE;	
 			break;
 		}
 		case lessequal_Sym: {
 			Get();
 			op = RelOpE.LE;	
 			break;
 		}
 		default: SynErr(44); break;
 		}
 		return op;
 	}
 
 	static int Term() {
 		int type;
 		int type1, type2;
 		type = TableEntry.noType;
 		
 		type1  = Factor();
 		MulOpE op = MulOpE.UN;
 		type = type1;
 		
 		while (StartOf(4)) {
 			op  = MulOp();
 			type2  = Factor();
 			switch(op)
 			{
 				case AND:
 					if(!TableEntry.isBool(type1) || !TableEntry.isBool(type2))
 						SemanticError("Boolean Types Required");
 					type = TableEntry.boolType;
 					break;
 				default:
 					if(!TableEntry.isArith(type1) || !TableEntry.isArith(type2))
 						SemanticError("Numeric Types Required");
 					if((type1 == TableEntry.intType) && (type2 == TableEntry.intType))
 						type = TableEntry.intType;
 					else
 						type = TableEntry.doubleType;  														
 			}
 			
 			if(valid)
 				CodeGen.performMulOp(op);
 			
 		}
 		return type;
 	}
 
 	static AddOpE AddOp() {
 		AddOpE op;
 		op = AddOpE.UN;	
 		if (la.kind == plus_Sym) {
 			Get();
 			op = AddOpE.ADD;	
 		} else if (la.kind == minus_Sym) {
 			Get();
 			op = AddOpE.SUB;	
 		} else if (la.kind == barbar_Sym) {
 			Get();
 			op = AddOpE.OR;	
 		} else SynErr(45);
 		return op;
 	}
 
 	static int Factor() {
 		int type;
 		PostOpE T = PostOpE.UN;
 		PrimaryPair p = new PrimaryPair();
 		
 		p  = Primary();
 		type = p.type;
 		if (la.kind == postInc_Sym || la.kind == postDec_Sym) {
 			T  = PostOp();
 			if(!TableEntry.isArith(type)) SemanticError("Cannot perform a post operation on a boolan type.");
 			
 			if(p.kind != TableEntry.Variable)
 			{
 				SemanticError("Can only perform post operations on a variable.");
 			}
 			else
 			{
 			if(valid)
 			{
 			if(T == PostOpE.INC)
 			{
 				CodeGen.performPostOp(p.offset, 1);
 			}
 			else
 			{
 				CodeGen.performPostOp(p.offset, -1);
 			}
 			}
 			}
 			
 		}
 		return type;
 	}
 
 	static MulOpE MulOp() {
 		MulOpE op;
 		op = MulOpE.UN;	
 		if (la.kind == star_Sym) {
 			Get();
 			op = MulOpE.MUL;	
 		} else if (la.kind == slash_Sym) {
 			Get();
 			op = MulOpE.DIV;	
 		} else if (la.kind == percent_Sym) {
 			Get();
 			op = MulOpE.MOD;	
 		} else if (la.kind == andand_Sym) {
 			Get();
 			op = MulOpE.AND;	
 		} else SynErr(46);
 		return op;
 	}
 
 	static PrimaryPair Primary() {
 		PrimaryPair p;
 		p = new PrimaryPair();
 		p.type = TableEntry.noType;
 		
 		int typeA = TableEntry.noType;
 		int typeE = TableEntry.noType;
 		
 		ConstantRecord con;
 		TableEntry entry = null;
 		
 		if (la.kind == identifier_Sym || la.kind == type_Sym) {
 			entry = Designator(false);
 			p.type = entry.type;
 			p.kind = TableEntry.Variable;
 			p.offset = entry.offset;
 			
 			
 			if(valid)
 			{
 			if(entry.kind == TableEntry.Variable)
 					CodeGen.derefVariable(entry.offset);		  											
 				else if(entry.kind == TableEntry.Property)
 				{
 					if(TableEntry.isArray(entry.type))
 						CodeGen.generatePropertyArray(entry.offset);
 					else
 						CodeGen.derefProperty(entry.offset);
 				}
 			}
 			
 			if (la.kind == lparen_Sym) {
 				Get();
 				typeA  = Arguments();
 				if(!TableEntry.isFunction(entry.kind))
 				{
 				SemanticError("Arguments can only be given to a function.");
 				}
 				
 				if(entry.kind == TableEntry.aFunction)
 				{
 				if(!TableEntry.isArray(typeA))
 				{
 					SemanticError("Invalid argument. Argument must be an array");
 				}
 				else
 				{
 					if(valid)
 						CodeGen.invokeStandardArrayMethod(entry.name, entry.argType, entry.type);
 				}
 				}
 				else
 				{
 				if(TableEntry.isArray(typeA))
 				{
 					SemanticError("Invalid argument. Argument must be a scalar type");
 				}
 				else
 				{
 					if(valid)
 						CodeGen.invokeStandardScalarMethod(entry.name, entry.argType, entry.type);
 				}
 				}
 				p.kind = TableEntry.Constant;
 				
 				
 				Expect(rparen_Sym);
 			}
 		} else if (la.kind == number_Sym || la.kind == double_Sym) {
 			con  = Constant();
 			p.type = con.type;
 			p.kind = TableEntry.Constant;
 			
 			if(valid)
 				CodeGen.loadConstant((double)con.value);
 			
 		} else if (la.kind == lparen_Sym) {
 			Get();
 			typeE  = Expression();
 			p.type = typeE;
 			p.kind = TableEntry.Constant;
 			
 			Expect(rparen_Sym);
 		} else SynErr(47);
 		return p;
 	}
 
 	static ConstantRecord Constant() {
 		ConstantRecord con;
 		con = new ConstantRecord();
 		
 		if (la.kind == double_Sym) {
 			con.value  = DoubleConst();
 			con.type = TableEntry.doubleType;	
 		} else if (la.kind == number_Sym) {
 			con.value  = IntConst();
 			con.type = TableEntry.intType;		
 		} else SynErr(48);
 		return con;
 	}
 
 	static double DoubleConst() {
 		double value;
 		Expect(double_Sym);
 		try
 		{
 			value = Double.parseDouble(token.val);
 		} catch(NumberFormatException e)
 		{
 			value = 0;
 		}
 		
 		
 		return value;
 	}
 
 
 
 	public static void Parse() {
 		la = new Token();
 		la.val = "";
 		Get();
 		CAL();
 		Expect(EOF_SYM);
 
 	}
 
 	private static boolean[][] set = {
 		{T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x},
 		{x,x,x,x, x,T,x,T, x,x,x,x, T,x,x,x, x,x,x,T, x,T,x,x, T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x},
 		{x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, T,T,T,T, T,T,x,x, x,x,x,x, x},
 		{x,T,T,x, x,T,x,x, x,x,x,x, T,x,T,x, x,x,x,x, x,x,x,x, x,x,T,T, x,x,x,x, x,x,x,x, x,x,x,x, x},
 		{x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,T, T,T,T,x, x}
 
 	};
 
 } // end Parser
 
 /* pdt - considerable extension from here on */
 
 class ErrorRec {
 	public int line, col, num;
 	public String str;
 	public ErrorRec next;
 
 	public ErrorRec(int l, int c, String s) {
 		line = l; col = c; str = s; next = null;
 	}
 
 } // end ErrorRec
 
 class Errors {
 
 	public static int count = 0;                                     // number of errors detected
 	public static String errMsgFormat = "file {0} : ({1}, {2}) {3}"; // 0=file 1=line, 2=column, 3=text
 	static String fileName = "";
 	static String listName = "";
 	static boolean mergeErrors = false;
 	static PrintWriter mergedList;
 
 	static ErrorRec first = null, last;
 	static boolean eof = false;
 
 	static String getLine() {
 		char ch, CR = '\r', LF = '\n';
 		int l = 0;
 		StringBuffer s = new StringBuffer();
 		ch = (char) Buffer.Read();
 		while (ch != Buffer.EOF && ch != CR && ch != LF) {
 			s.append(ch); l++; ch = (char) Buffer.Read();
 		}
 		eof = (l == 0 && ch == Buffer.EOF);
 		if (ch == CR) {  // check for MS-DOS
 			ch = (char) Buffer.Read();
 			if (ch != LF && ch != Buffer.EOF) Buffer.pos--;
 		}
 		return s.toString();
 	}
 
 	static private String Int(int n, int len) {
 		String s = String.valueOf(n);
 		int i = s.length(); if (len < i) len = i;
 		int j = 0, d = len - s.length();
 		char[] a = new char[len];
 		for (i = 0; i < d; i++) a[i] = ' ';
 		for (j = 0; i < len; i++) {a[i] = s.charAt(j); j++;}
 		return new String(a, 0, len);
 	}
 
 	static void display(String s, ErrorRec e) {
 		mergedList.print("**** ");
 		for (int c = 1; c < e.col; c++)
 			if (s.charAt(c-1) == '\t') mergedList.print("\t"); else mergedList.print(" ");
 		mergedList.println("^ " + e.str);
 	}
 
 	public static void Init (String fn, String dir, boolean merge) {
 		fileName = fn;
 		listName = dir + "listing.txt";
 		mergeErrors = merge;
 		if (mergeErrors)
 			try {
 				mergedList = new PrintWriter(new BufferedWriter(new FileWriter(listName, false)));
 			} catch (IOException e) {
 				Errors.Exception("-- could not open " + listName);
 			}
 	}
 
 	public static void Summarize () {
 		if (mergeErrors) {
 			ErrorRec cur = first;
 			Buffer.setPos(0);
 			int lnr = 1;
 			String s = getLine();
 			while (!eof) {
 				mergedList.println(Int(lnr, 4) + " " + s);
 				while (cur != null && cur.line == lnr) {
 					display(s, cur); cur = cur.next;
 				}
 				lnr++; s = getLine();
 			}
 			if (cur != null) {
 				mergedList.println(Int(lnr, 4));
 				while (cur != null) {
 					display(s, cur); cur = cur.next;
 				}
 			}
 			mergedList.println();
 			mergedList.println(count + " errors detected");
 			mergedList.close();
 		}
 		switch (count) {
 			case 0 : System.out.println("Parsed correctly"); break;
 			case 1 : System.out.println("1 error detected"); break;
 			default: System.out.println(count + " errors detected"); break;
 		}
 		if (count > 0 && mergeErrors) System.out.println("see " + listName);
 	}
 
 	public static void storeError (int line, int col, String s) {
 		if (mergeErrors) {
 			ErrorRec latest = new ErrorRec(line, col, s);
 			if (first == null) first = latest; else last.next = latest;
 			last = latest;
 		} else printMsg(fileName, line, col, s);
 	}
 
 	public static void SynErr (int line, int col, int n) {
 		String s;
 		switch (n) {
 			case 0: s = "EOF expected"; break;
 			case 1: s = "number expected"; break;
 			case 2: s = "double expected"; break;
 			case 3: s = "postInc expected"; break;
 			case 4: s = "postDec expected"; break;
 			case 5: s = "identifier expected"; break;
 			case 6: s = "\"ruleset\" expected"; break;
 			case 7: s = "\"{\" expected"; break;
 			case 8: s = "\"}\" expected"; break;
 			case 9: s = "\"typecount\" expected"; break;
 			case 10: s = "\";\" expected"; break;
 			case 11: s = "\"property\" expected"; break;
 			case 12: s = "\"type\" expected"; break;
 			case 13: s = "\"=\" expected"; break;
 			case 14: s = "\"(\" expected"; break;
 			case 15: s = "\")\" expected"; break;
 			case 16: s = "\"[\" expected"; break;
 			case 17: s = "\"]\" expected"; break;
 			case 18: s = "\".\" expected"; break;
 			case 19: s = "\"if\" expected"; break;
 			case 20: s = "\"else\" expected"; break;
 			case 21: s = "\"loop\" expected"; break;
 			case 22: s = "\"from\" expected"; break;
 			case 23: s = "\"to\" expected"; break;
 			case 24: s = "\"var\" expected"; break;
 			case 25: s = "\",\" expected"; break;
 			case 26: s = "\"+\" expected"; break;
 			case 27: s = "\"-\" expected"; break;
 			case 28: s = "\"==\" expected"; break;
 			case 29: s = "\"!=\" expected"; break;
 			case 30: s = "\">\" expected"; break;
 			case 31: s = "\"<\" expected"; break;
 			case 32: s = "\">=\" expected"; break;
 			case 33: s = "\"<=\" expected"; break;
 			case 34: s = "\"||\" expected"; break;
 			case 35: s = "\"*\" expected"; break;
 			case 36: s = "\"/\" expected"; break;
 			case 37: s = "\"%\" expected"; break;
 			case 38: s = "\"&&\" expected"; break;
 			case 39: s = "??? expected"; break;
 			case 40: s = "invalid Ident"; break;
 			case 41: s = "invalid Statement"; break;
 			case 42: s = "invalid AssignCall"; break;
 			case 43: s = "invalid PostOp"; break;
 			case 44: s = "invalid RelOp"; break;
 			case 45: s = "invalid AddOp"; break;
 			case 46: s = "invalid MulOp"; break;
 			case 47: s = "invalid Primary"; break;
 			case 48: s = "invalid Constant"; break;
 			default: s = "error " + n; break;
 		}
 		storeError(line, col, s);
 		count++;
 	}
 
 	public static void SemErr (int line, int col, int n) {
 		storeError(line, col, ("error " + n));
 		count++;
 	}
 
 	public static void Error (int line, int col, String s) {
 		storeError(line, col, s);
 		count++;
 	}
 
 	public static void Warn (int line, int col, String s) {
 		storeError(line, col, s);
 	}
 
 	public static void Exception (String s) {
 		System.out.println(s);
 		System.exit(1);
 	}
 
 	private static void printMsg(String fileName, int line, int column, String msg) {
 		StringBuffer b = new StringBuffer(errMsgFormat);
 		int pos = b.indexOf("{0}");
 		if (pos >= 0) { b.replace(pos, pos+3, fileName); }
 		pos = b.indexOf("{1}");
 		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, line); }
 		pos = b.indexOf("{2}");
 		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, column); }
 		pos = b.indexOf("{3}");
 		if (pos >= 0) b.replace(pos, pos+3, msg);
 		System.out.println(b.toString());
 	}
 
 } // end Errors
