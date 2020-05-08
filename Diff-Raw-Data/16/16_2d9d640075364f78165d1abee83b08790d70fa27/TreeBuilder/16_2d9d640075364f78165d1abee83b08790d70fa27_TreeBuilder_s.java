 package latte.grammar;
 
 import latte.grammar.latteParser.program_return;
 
 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.TokenStream;
 import org.antlr.runtime.tree.CommonTree;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Stack;
 
 public class TreeBuilder {
 
 	private HashMap<String, CommonTree> storage_func = new HashMap<String, CommonTree>();
 	private Stack<HashMap<String, Integer>> storage_vars = new Stack<HashMap<String,Integer>>();
 	private Stack<HashMap<String, Integer>> storage_subs = new Stack<HashMap<String,Integer>>();
 	
 	public CommonTree buildTree(String program_data) throws RecognitionException {
 		CharStream charStream = new ANTLRStringStream(program_data);
 		latteLexer lexer = new latteLexer(charStream);
 		TokenStream tokenStream = new CommonTokenStream(lexer);
 		latteParser parser = new latteParser(tokenStream);
 
 		program_return program = parser.program();
 		return program.tree;
 	}
 
 	public void checkType(CommonTree root) throws LatteException {
 		loadFunctions(root);
 		checkTypes(root);
 		checkReturns();
 	}
 
 	private void checkReturns() throws LatteException {
 		for (Iterator<CommonTree> iterator = storage_func.values().iterator(); iterator.hasNext();) {
 			CommonTree fun = iterator.next();
 			if (fun != null) {
 				@SuppressWarnings("unchecked")
 				List<CommonTree> topdef = fun.getChildren();
 	
 				int expectedReturn = topdef.get(0).token.getType();
 				if (expectedReturn == latteParser.TYPE_VOID) {
 					continue;
 				}
 				CommonTree blockLookup;
 				if (topdef.get(2).token.getType() == latteParser.BLOCK) {
 					blockLookup = topdef.get(2);
 				} else {
 					blockLookup = topdef.get(3);
 				}
 	
 				storage_vars.push(new HashMap<String, Integer>());
 				CommonTree args = topdef.get(2);
 				if (args.getType() == latteParser.ARGS) {
 					@SuppressWarnings("unchecked")
 					List<CommonTree> argsToLoad = args.getChildren();
 					for(int i = 0; i < argsToLoad.size(); i++) {
 						CommonTree arg = argsToLoad.get(i);
 						String ident = arg.getChild(1).getText();
 						int type = arg.getChild(0).getType();
 						storage_vars.peek().put(ident, type);
 					}
 				}
 				returnLookup(expectedReturn, blockLookup);
 				storage_vars.pop();
 			}
 		}
 	}
 
 	private boolean returnLookup(int expectedReturn, CommonTree blockLookup) throws LatteException {
 		@SuppressWarnings("unchecked")
 		List<CommonTree> children = blockLookup.getChildren();
 		
 		// Only last stmt can be return
 		if (children != null) {
 			for (int i = 0; i < children.size() - 1; i++) {
 				isNotReturn(children.get(i), true);
 			}
 			return isReturn(children.get(children.size()-1), expectedReturn);
 		} else if (expectedReturn != latteParser.TYPE_VOID) {
 			throw new LatteException("No return statement found.", blockLookup);
 		}
 		
 		return false;
 	}
 
 	private boolean isReturn(CommonTree commonTree, int expectedReturn) throws LatteException {
 		int type = commonTree.token.getType();
 		
 		switch (type) {
 		case latteParser.RET:
 			int givenType = checkTypes((CommonTree)commonTree.getChild(0)); 
 			if (expectedReturn != givenType) {
 				throw new LatteException("Return type expected: "+typeToString(expectedReturn)+
 						", but given: "+typeToString(givenType)+".", commonTree);
 			}
 			return true;
 			
 		case latteParser.RETV:
 			if (expectedReturn != latteParser.TYPE_VOID) {
 				throw new LatteException("Excpected void return.", commonTree);
 			}
 			return true;
 
 		case latteParser.COND: {
 			boolean result = false;
 			if (commonTree.getChildren().size() == 3) {
 				CommonTree expr = (CommonTree)commonTree.getChild(0);
 				if (expr.token.getType() == latteParser.TRUE) {
 					result = isReturn((CommonTree)commonTree.getChild(1), expectedReturn);
 				} else if (expr.token.getType() == latteParser.FALSE) {
 					result = isReturn((CommonTree)commonTree.getChild(2), expectedReturn);
 				} else {
 					boolean lret = isReturn((CommonTree)commonTree.getChild(1), expectedReturn);
 					boolean rret = isReturn((CommonTree)commonTree.getChild(2), expectedReturn);
 					result = (lret && rret);
 				}
 			} else {
 				CommonTree expr = (CommonTree)commonTree.getChild(0);
 				if (expr.token.getType() == latteParser.FALSE) {
 					result = false;
 				} else {
 					result = isReturn((CommonTree)commonTree.getChild(1), expectedReturn);
 				}
 			}
 			if (!result){
 				throw new LatteException("No return statement found.", commonTree);
 			}
 			
 			return result;
 		}
 
 		case latteParser.BLOCK: {
 			storage_vars.push(new HashMap<String, Integer>());
 			boolean result = returnLookup(expectedReturn, commonTree);
 			storage_vars.pop();
 			
 			return result;
 		}
 		
 		case latteParser.DECL: {
 			@SuppressWarnings("unchecked")
 			List<CommonTree> decl = commonTree.getChildren();
 			int varType = decl.get(0).token.getType();
 			for(int i = 1; i < decl.size(); i++) {
 				CommonTree child = decl.get(i);
 				@SuppressWarnings("unchecked")
 				List<CommonTree> declaration = child.getChildren();
 				String ident = declaration.get(0).token.getText();
 				storage_vars.peek().put(ident, varType);
 			}
 			break;
 		}
 
 		default:
 			if (expectedReturn != latteParser.TYPE_VOID) {
 				throw new LatteException("No return statement at the end of function.", commonTree);
 			}
 			break;
 		}
 		
 		return false;
 	}
 
 	private String typeToString(int expectedReturn) {
 		switch (expectedReturn) {
 		case latteParser.TYPE_BOOLEAN:
 			return "boolean";
 		case latteParser.TYPE_INT:
 			return "integer";
 		case latteParser.TYPE_STRING:
 			return "string";
 		case latteParser.TYPE_VOID:
 			return "void";
 
 		default:
 			break;
 		}
 		return "";
 	}
 
 	private boolean isNotReturn(CommonTree commonTree, boolean topLevel) throws LatteException {
 		int type = commonTree.token.getType();
 		
 		switch (type) {
 		case latteParser.RET:
 		case latteParser.RETV:
 			if (topLevel) {
 				throw new LatteException("Unreachable code after return.", commonTree);	
 			} else {
 				return false;
 			}
 
 		case latteParser.COND:
 			boolean result = true;
 
 			if (commonTree.getChildren().size() == 3) {
 				CommonTree expr = (CommonTree)commonTree.getChild(1);
 				if (expr.token.getType() == latteParser.TRUE) {
 					result = isNotReturn((CommonTree)commonTree.getChild(1), true);
 				} else if (expr.token.getType() == latteParser.FALSE) {
 					result = isNotReturn((CommonTree)commonTree.getChild(2), true);
 				} else {
 					boolean lret = isNotReturn((CommonTree)commonTree.getChild(1), false);
 					boolean rret = isNotReturn((CommonTree)commonTree.getChild(2), false);
 					result = (lret || rret);
 				}
 			} else {
 				CommonTree expr = (CommonTree)commonTree.getChild(0);
 				if (expr.token.getType() == latteParser.TRUE) {
 					result = isNotReturn((CommonTree)commonTree.getChild(1), true);
 				}
 			}
 			
 			if (result) {
 				return true;
 			} else {
 				if (topLevel) {
 					throw new LatteException("Unreachable code after return.", commonTree);	
 				} else {
 					return false;
 				}
 			}
 
 		case latteParser.BLOCK:
 			storage_vars.push(new HashMap<String, Integer>());
 			@SuppressWarnings("unchecked")
 			List<CommonTree> children = commonTree.getChildren();
 			if (children != null) {
 				for (Iterator<CommonTree> i = children.iterator(); i.hasNext();) {
 					CommonTree child = i.next();
 					isNotReturn(child, true);
 				}
 			}
 			storage_vars.pop();
 			
 			break;
 
 		case latteParser.DECL: {
 			@SuppressWarnings("unchecked")
 			List<CommonTree> decl = commonTree.getChildren();
 			int varType = decl.get(0).token.getType();
 			for(int i = 1; i < decl.size(); i++) {
 				CommonTree child = decl.get(i);
 				@SuppressWarnings("unchecked")
 				List<CommonTree> declaration = child.getChildren();
 				String ident = declaration.get(0).token.getText();
 				storage_vars.peek().put(ident, varType);
 			}
 			break;
 		}
 			
 		default:
 			break;
 		}
 		
 		return true;
 	}
 
 	private void loadFunctions(CommonTree root) throws LatteException {
 		// lang defined functions
 		storage_func.put("printString", null);
 		storage_func.put("printInt", null);
 		storage_func.put("readString", null);
 		storage_func.put("readInt", null);
 		
 		if (root.token == null) {
 			@SuppressWarnings("unchecked")
 			List<CommonTree> children = root.getChildren();
 
 			if (children != null) {
 				for (Iterator<CommonTree> i = children.iterator(); i.hasNext();) {
 					CommonTree child = i.next();
 					addFunction(child);
 				}
 			}			
 		} else {
 			addFunction(root);
 		}
 	}
 
 	private void addFunction(CommonTree topdef) throws LatteException {
 		@SuppressWarnings("unchecked")
 		List<CommonTree> func = topdef.getChildren();
 		
 		String ident = func.get(1).token.getText();
 		if (lookupFun(ident)) {
 			throw new LatteException("Function name duplicated", (CommonTree)func.get(1));
 		}
 		storage_func.put(ident, topdef);
 	}
 	
 	private int lookupVar(String ident) {
 		for(int i = storage_vars.size()-1; i >= 0; i--) {
 			HashMap<String,Integer> locVar = storage_vars.get(i);
 			if (locVar.containsKey(ident)) {
 				return locVar.get(ident);
 			}
 		}
 		return -1;
 	}
 	
 	private int lookupSubs(String ident) {
 		for(int i = storage_subs.size()-1; i >= 0; i--) {
 			HashMap<String,Integer> locVar = storage_subs.get(i);
 			if (locVar.containsKey(ident)) {
 				return locVar.get(ident);
 			}
 		}
 		return -1;
 	}
 
 	private boolean lookupFun(String funName) {
 		return storage_func.containsKey(funName);
 	}
 
 	public int checkTypes(CommonTree root) throws LatteException {
 		int token_type = -1;
 		if (root.token != null) {
 			token_type = root.token.getType();
 		}
 		@SuppressWarnings("unchecked")
 		List<CommonTree> children = root.getChildren();
 
 		switch (token_type) {
 		
 		// int int || str str
 		case latteParser.OP_PLUS: {
 			int type_left = checkTypes(children.get(0));
 			int type_right = checkTypes(children.get(1));
 			
 			if (type_left != type_right || 
 					(type_left != latteParser.TYPE_INT &&
 					type_left != latteParser.TYPE_STRING)) {
 				throw new LatteException("Arithmetic operation types mismatch", children.get(0));
 			}
 			
 			return type_left;
 		}
 			
 		// int int
 		case latteParser.OP_MINUS:
 		case latteParser.OP_TIMES:
 		case latteParser.OP_DIV:
 		case latteParser.OP_MOD: {
 			int type_left = checkTypes(children.get(0));
 			int type_right = checkTypes(children.get(1));
 
 			if (type_left != type_right ||
 					type_left != latteParser.TYPE_INT) {
 				throw new LatteException("Arithmetic operation types mismatch", children.get(0));
 			}
 
 			return latteParser.TYPE_INT;
 		}
 		
 		// int int -> bool
 		case latteParser.OP_LTH:
 		case latteParser.OP_LE:
 		case latteParser.OP_GTH:
 		case latteParser.OP_GE: {
 			int type_left = checkTypes(children.get(0));
 			int type_right = checkTypes(children.get(1));
 
 			if (type_left != type_right ||
 					type_left != latteParser.TYPE_INT) {
 				throw new LatteException("Comparator types mismatch", children.get(0));
 			}
 
 			return latteParser.TYPE_BOOLEAN;
 		}
 		
 		// int int || bool bool -> bool
 		case latteParser.OP_EQU:
 		case latteParser.OP_NE: {
 			int type_left = checkTypes(children.get(0));
 			int type_right = checkTypes(children.get(1));
 
 			if (type_left != type_right ||
 					(type_left != latteParser.TYPE_INT &&
 					type_left != latteParser.TYPE_BOOLEAN)) {
 				throw new LatteException("Comparator types mismatch", children.get(0));
 			}
 
 			return latteParser.TYPE_BOOLEAN;
 		}
 		
 		// bool bool
 		case latteParser.OP_AND:
 		case latteParser.OP_OR: {
 			int type_left = checkTypes(children.get(0));
 			int type_right = checkTypes(children.get(1));
 
 			if (type_left != type_right ||
 					type_left != latteParser.TYPE_BOOLEAN) {
 				throw new LatteException("Boolean operation types mismatch", children.get(0));
 			}
 
 			return type_left;
 		}
 		
 		case latteParser.NEGATION: {
 			int type_left = checkTypes(children.get(0));
 
 			if (type_left != latteParser.TYPE_INT) {
 				throw new LatteException("Wrong negation type", children.get(0));
 			}
 			
 			return latteParser.TYPE_INT;
 		}
 		
 		case latteParser.NOT: {
 			int type_left = checkTypes(children.get(0));
 
 			if (type_left != latteParser.TYPE_BOOLEAN) {
 				throw new LatteException("Boolean operation type mismatch", children.get(0));
 			}
 			
 			return latteParser.TYPE_BOOLEAN;
 		}
 
 		case latteParser.INTEGER:
 			return latteParser.TYPE_INT;
 		case latteParser.FALSE:
 			return latteParser.TYPE_BOOLEAN;
 		case latteParser.TRUE:
 			return latteParser.TYPE_BOOLEAN;
 		case latteParser.STRING:
 			return latteParser.TYPE_STRING;
 		case latteParser.VAR_IDENT: {
 			int result = lookupVar(children.get(0).token.getText());
 			if (result != -1) {
 				return lookupVar(children.get(0).token.getText());
 			} else {
 				throw new LatteException("Unknown variable.", children.get(0));
 			}
 		}
 			
 		case latteParser.EAPP: {
 			String funName = children.get(0).token.getText();
 			if (!lookupFun(funName)) {
 				throw new LatteException("Unknown function identifier.", children.get(0));
 			}
 
 			if (funName.compareTo("printString") == 0) { 
 				return checkPrintString(children); 
 			}
 			if (funName.compareTo("printInt") == 0) { 
 				return checkPrintInt(children); 
 			}
 			if (funName.compareTo("readString") == 0) { 
 				return checkReadString(children); 
 			}
 			if (funName.compareTo("readInt") == 0) { 
 				return checkReadInt(children); 
 			}
 
 			CommonTree func = storage_func.get(funName);
 			CommonTree args = (CommonTree)func.getChildren().get(2);
 
 			// args type cheking
 			if (args.token.getType() == latteParser.ARGS) {
 				@SuppressWarnings("unchecked")
 				List<CommonTree> argsList = args.getChildren();
 				if (children.size()-1 != argsList.size()) {
 					throw new LatteException("No of passed arguments mismatch. Expected: "+argsList.size()+
 							", given: "+(children.size()-1)+".", children.get(0));
 				}
 				for (int i = 0; i < argsList.size(); i++) {
 					int givenType = checkTypes(children.get(i+1));
 					int expectedType = argsList.get(i).getChild(0).getType();
 					if (givenType != expectedType) {
 						throw new LatteException("Argument type expected: "+typeToString(expectedType)+
 								", given: "+typeToString(givenType)+".", (CommonTree)argsList.get(i).getChild(0));
 					}
 				}
 			} else if (children.size()-1 != 0) {
 				throw new LatteException("Passing arguments to zero-arg function.", children.get(0));
 			}
 			
 			return checkTypes((CommonTree)func.getChildren().get(0));
 		}
 		
 		case latteParser.ASS: {
 			int type = lookupVar(children.get(0).getChild(0).getText());
 			int subs = children.get(0).getType();
 			if (type == -1) {
 				type = lookupVar(children.get(0).getChild(0).getChild(0).getText());
 			}
 			if (type == latteParser.ARRTYPE && subs == latteParser.SUBSCRIB) {
 				int currType = checkTypes(children.get(1));
 				type = lookupSubs(children.get(0).getChild(0).getChild(0).getText());
 				if (type != currType) {
 					throw new LatteException("Type mismatch in assignment. Expected: "+typeToString(type)+
 							", given: "+typeToString(currType)+".", children.get(0));
 				}
 			} else if (type != -1) {
 				int currType = checkTypes(children.get(1));
 				if (type != currType) {
 					throw new LatteException("Type mismatch in assignment. Expected: "+typeToString(type)+
 							", given: "+typeToString(currType)+".", children.get(0));
 				}
 			} else {
 				throw new LatteException("Unknown variable", children.get(0));
 			}
 			
 			break;
 		}
 
 		case latteParser.DECR:
 		case latteParser.INCR: {
 			int type = lookupVar(children.get(0).token.getText());
 			if (type == -1) {
 				throw new LatteException("Unknown variable", children.get(0));
 			} else if (type != latteParser.TYPE_INT) {
 				throw new LatteException("Expected variable of integer type but given: "+
 						typeToString(type)+".", children.get(0));
 			}
 			
 			break;
 		}
 		
 		case latteParser.BLOCK: {
 			if (children != null) {
 				// new block vars
 				storage_vars.push(new HashMap<String, Integer>());
 				storage_subs.push(new HashMap<String, Integer>());
 	
 				// iterating with new variables block
 				for (Iterator<CommonTree> i = children.iterator(); i.hasNext();) {
 					CommonTree child = i.next();
 					checkTypes(child);
 				}
 				
 				// old block vars
 				storage_vars.pop();
 				storage_subs.pop();
 			}
 			break;
 		}
 	
 		case latteParser.TOP_DEF: {
 			// new block vars
 			storage_vars.push(new HashMap<String, Integer>());
 			storage_subs.push(new HashMap<String, Integer>());
 
 			// checking args integrity
 			CommonTree args = children.get(2);
 			if (args.getType() == latteParser.ARGS) {
 				@SuppressWarnings("unchecked")
 				List<CommonTree> argsToLoad = args.getChildren();
 				for(int i = 0; i < argsToLoad.size(); i++) {
 					CommonTree arg = argsToLoad.get(i);
 					String ident = arg.getChild(1).getText();
 					int type = arg.getChild(0).getType();
 					int arr_type = 0;
 					if (type == latteParser.ARRTYPE) {
 						arr_type = arg.getChild(0).getChild(0).getType(); 
 					}
 					if (lookupVar(arg.getChild(1).getText()) != -1) {
 						throw new LatteException("Duplicated argument name in function", (CommonTree)arg.getChild(1));
 					}
 					storage_vars.peek().put(ident, type);
 					storage_subs.peek().put(ident, arr_type);
 				}
 				checkTypes(children.get(3));
 			} else {
 				checkTypes(children.get(2));
 			}
 			
 			// old block vars
 			storage_vars.pop();
 			storage_subs.pop();
 			break;
 		}
 
 		case latteParser.SFOR: {
 			break;
 		}
 		
 		case latteParser.ARRTYPE: {
 			return latteParser.ARRTYPE;
 		}
 		
 		case latteParser.NEWARR: {
 			return latteParser.ARRTYPE;
 		}
 		
 		case latteParser.SUBSCRIB: {
 			int type = lookupSubs(children.get(0).getChild(0).getText());
 			return type;
 		}
 		
 		case latteParser.ATTRIBUTE: {
 			int type = checkTypes(children.get(0));
 			if (type == latteParser.ARRTYPE) {
 				String attrName = children.get(1).token.getText();
 				if (attrName.compareTo("length") == 0) {
 					return latteParser.TYPE_INT;
 				} else {
 					throw new LatteException("Wrong attribute.", children.get(0));
 				}
 			} else {
 				throw new LatteException("Object has no attributes.", children.get(0));
 			}
 		}
 		
 		case latteParser.DECL: {
 			int type = children.get(0).token.getType();
 			int arr_type = 0;
 			if (type == latteParser.ARRTYPE) {
 				arr_type = children.get(0).getChild(0).getType(); 
 			}
 			for(int i = 1; i < children.size(); i++) {
 				CommonTree child = children.get(i);
 				@SuppressWarnings("unchecked")
 				List<CommonTree> declaration = child.getChildren();
 				String ident = declaration.get(0).token.getText();
 
 				if (storage_vars.peek().containsKey(ident)) {
 					throw new LatteException("Variable already declared", declaration.get(0));
 				}
 				
 				if (child.token.getType() == latteParser.INIT) {
 					int currType = checkTypes(declaration.get(1));
 					if (currType == latteParser.ARRTYPE) {
 						int curr_arr_type = getArrType(declaration.get(1));
 						if (arr_type != curr_arr_type) {
 							throw new LatteException("Type mismatch in declaration", child);
 						}
 					} else if (type != currType) {
 						throw new LatteException("Type mismatch in declaration", child);
 					}
 				}
 
 				storage_vars.peek().put(ident, type);
 				storage_subs.peek().put(ident, arr_type);
 			}
 			break;
 		}
 
 //		case latteParser.COND: {
 //			CommonTree expr = children.get(0);
 //			if (expr.token.getType() == latteParser.TRUE) {
 //				root.replaceChildren(0, 0, 0);// = children.get(1);
 //			}
 //			break;
 //		}
 
 		default: {
 			if (children != null) {
 				for (Iterator<CommonTree> i = children.iterator(); i.hasNext();) {
 					CommonTree child = i.next();
 					checkTypes(child);
 				}
 			}
 			break;
 		}
 		}
 
 		return token_type;
 	}
 
 	private int getArrType(CommonTree root) {		
 		int token_type = -1;
 		if (root.token != null) {
 			token_type = root.token.getType();
 		}
 		@SuppressWarnings("unchecked")
 		List<CommonTree> children = root.getChildren();
 	
 		switch (token_type) {
 		case latteParser.ARRTYPE: {
 			int arr_type = children.get(0).getChild(0).getType(); // TODO: check!
 			return arr_type;
 		}
 		
 		case latteParser.NEWARR: {
 			int arr_type = children.get(0).token.getType();
 			return arr_type;
 		}
 
 		case latteParser.EAPP: {
 			String funName = children.get(0).token.getText();
 			CommonTree func = storage_func.get(funName);
 			int arr_type = func.getChild(0).getChild(0).getType();
 			return arr_type;
 		}
 		
 		}
 		return 0;
 	}
 
 	private int checkReadInt(List<CommonTree> children) throws LatteException {
 		if (children.size()-1 != 0) {
 			throw new LatteException("printStr expects zero argument.", children.get(0));
 		}
 		return latteParser.TYPE_INT;
 	}
 
 	private int checkReadString(List<CommonTree> children) throws LatteException {
 		if (children.size()-1 != 0) {
 			throw new LatteException("printStr expects zero argument.", children.get(0));
 		}
 		return latteParser.TYPE_STRING;
 	}
 
 	private int checkPrintString(List<CommonTree> children) throws LatteException {
 		if (children.size()-1 != 1) {
 			throw new LatteException("printStr expects one argument.", children.get(0));
 		}
 		int givenType = checkTypes(children.get(1));
 		int expectedType = latteParser.TYPE_STRING;
 		if (givenType != expectedType) {
 			throw new LatteException("Expected `different type argument.", children.get(0));
 		}
 		return latteParser.TYPE_VOID;
 	}
 
 	private int checkPrintInt(List<CommonTree> children) throws LatteException {
 		if (children.size()-1 != 1) {
 			throw new LatteException("printInt expects one argument.", children.get(0));
 		}
 		int givenType = checkTypes(children.get(1));
 		int expectedType = latteParser.TYPE_INT;
 		if (givenType != expectedType) {
 			throw new LatteException("Expected `different type argument.", children.get(0));
 		}
 		return latteParser.TYPE_VOID;
 	}
 }
