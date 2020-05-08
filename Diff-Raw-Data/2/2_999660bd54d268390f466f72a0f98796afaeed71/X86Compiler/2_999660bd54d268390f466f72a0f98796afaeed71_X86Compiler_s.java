 package latte.grammar;
 
 import org.antlr.runtime.tree.CommonTree;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Stack;
 
 public class X86Compiler {
 
 	private HashMap<String, String> storage_func = new HashMap<String, String>();
 	private HashMap<String, Integer> storage_func_max_locals = new HashMap<String, Integer>();
 	private Stack<HashMap<String, Integer>> storage_vars = new Stack<HashMap<String,Integer>>();
 	private Stack<HashMap<String, String>> storage_var_types = new Stack<HashMap<String,String>>();
 	private int labelCounter;
 	private String currentReturnType;
 	private String className;
 	private CommonTree troot;
 	FileWriter fwriter;
 	BufferedWriter output;
 	File fout;
 	
 	public X86Compiler(
 			String name,
 			CommonTree tree) throws IOException {
 		this.className = name;
 		this.troot = tree;
 		
 		fout = new File(name+".s");
 		if(!fout.exists()){
 			fout.createNewFile();
 		}
 		this.fwriter = new FileWriter(className+".s");
 		this.output = new BufferedWriter(fwriter);
 	}
 
 	private void X86write(String out, int indentionLevel) throws IOException {
 		String indention = "";
 		for (int i = 0; i < indentionLevel; i++) {
 			indention = indention.concat("    ");
 		}
 		output.write(indention + out + "\r\n");
 	}
 
 	private void X86write(String out) throws IOException {
 		X86write(out, 0);
 	}
 
 	private void X86write(String func, String params) throws IOException {
 		X86write(func+"\t"+params, 2);
 	}
 
 	private void X86writeEnd() throws IOException {
 		output.close();
 	}
 
 	public void X86generate() throws IOException {
 		X86LoadFunctions();
 
 		X86write(".file	\"" + this.className + ".lat \"", 2);
 		X86write(".text", 2);
 		
 		X86traverse(troot);
 		
 		X86writeEnd();
 	}
 
 	private void X86LoadFunctions() {
 		if (troot.token == null) {
 			@SuppressWarnings("unchecked")
 			List<CommonTree> children = troot.getChildren();
 
 			if (children != null) {
 				for (Iterator<CommonTree> i = children.iterator(); i.hasNext();) {
 					CommonTree child = i.next();
 					X86AddFunction(child);
 				}
 			}			
 		} else {
 			X86AddFunction(troot);
 		}
 	}
 
 
 	private void X86AddFunction(CommonTree topdef) {
 		@SuppressWarnings("unchecked")
 		List<CommonTree> func = topdef.getChildren();
 		
 		String ident = func.get(1).token.getText();
 		String out = X86EncodeType(func.get(0).token.getType());
 		String args = "";
 		if (func.get(2).getType() == latteParser.ARGS) {
 			@SuppressWarnings("unchecked")
 			List<CommonTree> argsList = func.get(2).getChildren();
 			for (Iterator<CommonTree> iterator = argsList.iterator(); iterator.hasNext();) {
 				CommonTree commonTree = (CommonTree) iterator.next();
 				int type = commonTree.getChild(0).getType();
 				args = args.concat(X86EncodeType(type));
 			}
 		}
 		
 		int max_locals = X86CountLocals(topdef);
 
 		String jvmname = ident + "(" + args + ")" + out;
 		storage_func.put(ident, jvmname);
 		storage_func_max_locals.put(ident, max_locals);
 	}
 
 	private int X86CountLocals(CommonTree tree) {
  		int token_type = -1;
 		if (tree.token != null) {
 			token_type = tree.token.getType();
 		}
 		@SuppressWarnings("unchecked")
 		List<CommonTree> children = tree.getChildren();
 
 		switch (token_type) {
 
 		case latteParser.TOP_DEF: {
 			CommonTree args = children.get(2);
 		    // Traversing function body.
 			if (args.getType() == latteParser.ARGS) {
 				return X86CountLocals(children.get(3));
 			} else {
 				return X86CountLocals(children.get(2));
 			}
 		}
 		case latteParser.BLOCK: {
 			int max_count = 0;
 			storage_vars.push(new HashMap<String, Integer>());
 			if (children != null) {
 				for (Iterator<CommonTree> i = children.iterator(); i.hasNext();) {
 					CommonTree child = i.next();
 					int count = X86CountLocals(child);
 					if (count > max_count) {
 						max_count = count;
 					}
 				}
 			}
 			HashMap<String, Integer> peek = storage_vars.pop();
 			int bytes = 0;
 			for (Iterator<Integer> iterator = peek.values().iterator(); iterator.hasNext();) {
 				Integer bytes0 = iterator.next();
 				bytes += bytes0;
 			}
 			return max_count + bytes;
 		}
 		case latteParser.DECL: {
 			int varType = children.get(0).token.getType(); // TODO: for byte count
 			for(int i = 1; i < children.size(); i++) {
 				CommonTree child = children.get(i);
 				@SuppressWarnings("unchecked")
 				List<CommonTree> declaration = child.getChildren();
 				String ident = declaration.get(0).token.getText();
 				storage_vars.peek().put(ident, 4);
 			}
 			break;
 		}
 		default: {
 			if (children != null) {
 				for (Iterator<CommonTree> i = children.iterator(); i.hasNext();) {
 					CommonTree child = i.next();
 					return X86CountLocals(child);
 				}
 			}
 			break;
 		}
 		
 		}
 
 		return 0;
 	}
 
 	private String X86EncodeType(int type) {
 		String out = "";
 		switch (type) {
 		case latteParser.TYPE_INT:
 			out = "I";
 			break;
 		case latteParser.TYPE_STRING:
 			out = "Ljava/lang/String;";
 			break;
 		case latteParser.TYPE_BOOLEAN:
 			out = "I";
 			break;
 		case latteParser.TYPE_VOID:
 			out = "V";
 			break;
 		default:
 		}
 		return out;
 	}
 
 	private String X86NextLabel() {
 		labelCounter++;
 		return "Label"+labelCounter;
 	}
 
 	private String X86traverse(CommonTree tree) throws IOException {
  		int token_type = -1;
 		if (tree.token != null) {
 			token_type = tree.token.getType();
 		}
 		@SuppressWarnings("unchecked")
 		List<CommonTree> children = tree.getChildren();
 
 		switch (token_type) {
 
 		case latteParser.TOP_DEF: {
 			String name = children.get(1).getText();
 			CommonTree args = children.get(2);
 			int max_bytes_for_locals = storage_func_max_locals.get(name);
 
 		    X86write("");
 		    X86write("");
 		    X86write(name+":");
 
 			X86write("push","%ebp");
 			X86write("mov", "%esp, %ebp");
 
 			if (name.compareTo("main") == 0) {
 				X86write("and", "$0xfffffff0,%esp");
 			}
 			
 			if (max_bytes_for_locals != 0) {
 				X86write("sub", "$"+max_bytes_for_locals+",%esp");
 			}
 
 		    // Traversing function body.
 		    currentReturnType = X86TypeForVar(children.get(0).getType());
 			if (args.getType() == latteParser.ARGS) {
 				storage_vars.push(new HashMap<String, Integer>());
 				storage_var_types.push(new HashMap<String, String>());
 				@SuppressWarnings("unchecked")
 				List<CommonTree> argsList = args.getChildren();
 				int freeId = X86FreeVarId(storage_vars);
 				for (int i = 0; i < argsList.size(); i++) {
 					int type = argsList.get(i).getChild(0).getType();
 					String ident = argsList.get(i).getChild(1).getText();
 					int freeIdShift = freeId + i;
 					storage_vars.peek().put(ident, freeIdShift);
 					storage_var_types.peek().put(ident, X86TypeForVar(type));
 				}
 				X86traverse(children.get(3));
 				storage_vars.pop();
 				storage_var_types.pop();
 			} else {
 				X86traverse(children.get(2));
 			}
 
 			X86write("mov", "%ebp, %esp");
 			if (name.compareTo("main") == 0) {
 				X86write("leave", 2);
 			} else {
 				X86write("pop", "%ebp");
 			}
 			X86write("ret", 2);
 
 			break;
 		}
 		case latteParser.BLOCK: {
 			storage_vars.push(new HashMap<String, Integer>());
 			storage_var_types.push(new HashMap<String, String>());
 			if (children != null) {
 				for (Iterator<CommonTree> i = children.iterator(); i.hasNext();) {
 					CommonTree child = i.next();
 					X86traverse(child);
 				}
 			}
 			storage_vars.pop();
 			storage_var_types.pop();
 			break;
 		}
 		case latteParser.DECL: {
 			int varType = children.get(0).token.getType();
 			for(int i = 1; i < children.size(); i++) {
 				CommonTree child = children.get(i);
 				@SuppressWarnings("unchecked")
 				List<CommonTree> declaration = child.getChildren();
 				String ident = declaration.get(0).token.getText();
 
 				int freeIdShift = X86FreeVarId(storage_vars);
 
 				switch (varType) {
 				case latteParser.TYPE_INT:
 				case latteParser.TYPE_BOOLEAN: {
 					if (declaration.size() == 2) {
 					    String src = X86traverse(declaration.get(1));
 					    X86write("mov", src+", -"+freeIdShift+"(%ebp)");
 					} else {
 					    X86write("mov", "$0, -"+freeIdShift+"(%ebp)");
 					}
 					break;
 				}
 				case latteParser.TYPE_STRING: {
 					if (declaration.size() == 2) {
 					    X86traverse(declaration.get(1));
 					} else {
 //					    X86write("ldc \"\"", 1);	
 					}
 //					X86write("astore " + freeIdShift, 1);
 					break;
 				}
 				default:
 					break;
 				}
 
 				storage_vars.peek().put(ident, freeIdShift);
 				storage_var_types.peek().put(ident, X86TypeForVar(varType));
 			}
 			break;
 		}
 		case latteParser.EAPP: {
 			String functionName = children.get(0).getText();
 			if (functionName.compareTo("printInt") == 0) {
 //				X86write("getstatic java/lang/System/out Ljava/io/PrintStream;", 1);
 				X86traverse(children.get(1));
 //				X86write("invokevirtual java/io/PrintStream/println(I)V", 1);
 			} else if (functionName.compareTo("printString") == 0) {
 //				X86write("getstatic java/lang/System/out Ljava/io/PrintStream;", 1);
 				X86traverse(children.get(1));
 //				X86write("invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V", 1);
 			} else if (functionName.compareTo("readInt") == 0) {
 				int freeId = X86FreeVarId(storage_vars);
 				int freeId2 = freeId + 1;
 //				X86write("new java/io/InputStreamReader", 1);
 //				X86write("dup", 1);
 //				X86write("getstatic	java/lang/System/in Ljava/io/InputStream;", 1);
 //				X86write("invokespecial java/io/InputStreamReader/<init>(Ljava/io/InputStream;)V", 1);
 //				X86write("astore " + freeId, 1);
 //				X86write("new java/io/BufferedReader", 1);
 //				X86write("dup", 1);
 //				X86write("aload " + freeId, 1);
 //				X86write("invokespecial java/io/BufferedReader/<init>(Ljava/io/Reader;)V", 1);
 //				X86write("astore " + freeId2, 1);
 //				X86write("aload " + freeId2, 1);
 //				X86write("invokevirtual java/io/BufferedReader/readLine()Ljava/lang/String;", 1);
 //				X86write("invokestatic java/lang/Integer/parseInt(Ljava/lang/String;)I", 1);
 			} else if (functionName.compareTo("readString") == 0) {
 				int freeId = X86FreeVarId(storage_vars);
 				int freeId2 = freeId + 1;
 //				X86write("new java/io/InputStreamReader", 1);
 //				X86write("dup", 1);
 //				X86write("getstatic	java/lang/System/in Ljava/io/InputStream;", 1);
 //				X86write("invokespecial java/io/InputStreamReader/<init>(Ljava/io/InputStream;)V", 1);
 //				X86write("astore " + freeId, 1);
 //				X86write("new java/io/BufferedReader", 1);
 //				X86write("dup", 1);
 //				X86write("aload " + freeId, 1);
 //				X86write("invokespecial java/io/BufferedReader/<init>(Ljava/io/Reader;)V", 1);
 //				X86write("astore " + freeId2, 1);
 //				X86write("aload " + freeId2, 1);
 //				X86write("invokevirtual java/io/BufferedReader/readLine()Ljava/lang/String;", 1);
 			} else {
 				for (int i = 1; i < children.size(); i++) {
 					X86traverse(children.get(i));
 				}
 //				X86write("invokestatic "+className+"."+storage_func.get(functionName), 1);
 			}
 			break;
 		}
 		case latteParser.COND: {
 			if (children.size() == 3) {
 				String elseLabel = X86NextLabel();
 				String endifLabel = X86NextLabel();
 				X86traverse(children.get(0));
 				//X86write("ifeq " + elseLabel, 1);
 				X86traverse(children.get(1));
 				//X86write("goto " + endifLabel, 1);
 				//X86write(elseLabel+":");
 				X86traverse(children.get(2));
 				//X86write(endifLabel+":");
 			} else {
 				String endifLabel = X86NextLabel();
 				X86traverse(children.get(0));
 				//X86write("ifeq " + endifLabel, 1);
 				X86traverse(children.get(1));
 				//X86write(endifLabel+":");
 			}
 			break;
 		}
 		case latteParser.SWHILE: {
 			String whilebodyLabel = X86NextLabel();
 			String endwhileLabel = X86NextLabel();
 			//X86write(whilebodyLabel+":");
 			X86traverse(children.get(0));
 			//X86write("ifeq " + endwhileLabel, 1);
 			X86traverse(children.get(1));
 			//X86write("goto " + whilebodyLabel, 1);
 			//X86write(endwhileLabel+":");
 			break;
 		}
 		case latteParser.ASS: {
 			X86traverse(children.get(1));
 			String idName = children.get(0).getText();
 			int idNo = X86VarToId(idName);
 			String type = X86GetVarType(idName);
 			//X86write(type + "store " + idNo, 1);
 			break;
 		}
 		case latteParser.DECR: {
 			String idName = children.get(0).getText();
 			int idNo = X86VarToId(idName);
 		    //X86write("iinc " + idNo + " -1", 1);
 			break;
 		}
 		case latteParser.INCR: {
 			String idName = children.get(0).getText();
 			int idNo = X86VarToId(idName);
 		    //X86write("iinc " + idNo + " 1", 1);
 			break;
 		}
 		case latteParser.RET: {
 			String src = X86traverse(children.get(0));
 		    X86write("mov", src+", %eax");
 			break;
 		}
 		case latteParser.RETV: {
 			break;
 		}
 		case latteParser.OP_PLUS: {
 			String type = X86CheckPlusOpType(children.get(0));
 			if (type.compareTo("a") == 0) {
 				//X86write("new java/lang/StringBuilder", 1);
 				//X86write("dup", 1);
 				//X86write("invokespecial java/lang/StringBuilder/<init>()V", 1);
 				X86traverse(children.get(0));
 				//X86write("invokevirtual java/lang/StringBuilder/append(Ljava/lang/String;)Ljava/lang/StringBuilder;", 1);
 				X86traverse(children.get(1));
 				//X86write("invokevirtual	java/lang/StringBuilder/append(Ljava/lang/String;)Ljava/lang/StringBuilder;", 1);
 				//X86write("invokevirtual	java/lang/StringBuilder/toString()Ljava/lang/String;", 1);
 			} else {
 				X86traverse(children.get(0));
 				X86traverse(children.get(1));
 			    //X86write("iadd", 1);
 			}
 		    break;
 		}
 		case latteParser.OP_MINUS: {
 			X86traverse(children.get(0));
 			X86traverse(children.get(1));
 		    //X86write("isub", 1);
 		    break;
 		}
 		case latteParser.OP_TIMES: {
 			X86traverse(children.get(0));
 			X86traverse(children.get(1));
 		    //X86write("imul", 1);
 		    break;
 		}
 		case latteParser.OP_DIV: {
 			X86traverse(children.get(0));
 			X86traverse(children.get(1));
 		    //X86write("idiv", 1);
 		    break;
 		}
 		case latteParser.OP_MOD: {
 			X86traverse(children.get(0));
 			X86traverse(children.get(1));
 		    //X86write("irem", 1);
 			break;
 		}
 		case latteParser.OP_LTH: {
 			X86traverse(children.get(0));
 			X86traverse(children.get(1));
 			String elseLabel = X86NextLabel();
 			String endifLabel = X86NextLabel();
 			//X86write("if_icmplt " + elseLabel, 1);
 			//X86write("iconst_0", 1);
 			//X86write("goto " + endifLabel, 1);
 			//X86write(elseLabel+":");
 			//X86write("iconst_1", 1);
 			//X86write(endifLabel+":");
 			break;
 		}
 		case latteParser.OP_LE: {
 			X86traverse(children.get(0));
 			X86traverse(children.get(1));
 			String elseLabel = X86NextLabel();
 			String endifLabel = X86NextLabel();
 			//X86write("if_icmple " + elseLabel, 1);
 			//X86write("iconst_0", 1);
 			//X86write("goto " + endifLabel, 1);
 			//X86write(elseLabel+":");
 			//X86write("iconst_1", 1);
 			//X86write(endifLabel+":");
 			break;
 		}
 		case latteParser.OP_GTH: {
 			X86traverse(children.get(0));
 			X86traverse(children.get(1));
 			String elseLabel = X86NextLabel();
 			String endifLabel = X86NextLabel();
 			//X86write("if_icmpgt " + elseLabel, 1);
 			//X86write("iconst_0", 1);
 			//X86write("goto " + endifLabel, 1);
 			//X86write(elseLabel+":");
 			//X86write("iconst_1", 1);
 			//X86write(endifLabel+":");
 			break;
 		}
 		case latteParser.OP_GE: {
 			X86traverse(children.get(0));
 			X86traverse(children.get(1));
 			String elseLabel = X86NextLabel();
 			String endifLabel = X86NextLabel();
 			//X86write("if_icmpge " + elseLabel, 1);
 			//X86write("iconst_0", 1);
 			//X86write("goto " + endifLabel, 1);
 			//X86write(elseLabel+":");
 			//X86write("iconst_1", 1);
 			//X86write(endifLabel+":");
 			break;
 		}
 		case latteParser.OP_EQU: {
 			X86traverse(children.get(0));
 			X86traverse(children.get(1));
 			String elseLabel = X86NextLabel();
 			String endifLabel = X86NextLabel();
 			//X86write("if_icmpne " + elseLabel, 1);
 			//X86write("iconst_1", 1);
 			//X86write("goto " + endifLabel, 1);
 			//X86write(elseLabel+":");
 			//X86write("iconst_0", 1);
 			//X86write(endifLabel+":");
 			break;
 		}
 		case latteParser.OP_NE: {
 			X86traverse(children.get(0));
 			X86traverse(children.get(1));
 			String elseLabel = X86NextLabel();
 			String endifLabel = X86NextLabel();
 			//X86write("if_icmpeq " + elseLabel, 1);
 			//X86write("iconst_1", 1);
 			//X86write("goto " + endifLabel, 1);
 			//X86write(elseLabel+":");
 			//X86write("iconst_0", 1);
 			//X86write(endifLabel+":");
 			break;
 		}
 		case latteParser.OP_AND: {
 			X86traverse(children.get(0));
 			String elseLabel = X86NextLabel();
 			String endifLabel = X86NextLabel();
 			//X86write("ifeq " + elseLabel, 1);
 			X86traverse(children.get(1));
 			//X86write("ifeq " + elseLabel, 1);
 			//X86write("iconst_1", 1);
 			//X86write("goto " + endifLabel, 1);
 			//X86write(elseLabel+":");
 			//X86write("iconst_0", 1);
 			//X86write(endifLabel+":");
 			break;
 		}
 		case latteParser.OP_OR: {
 			X86traverse(children.get(0));
 			String elseLabel = X86NextLabel();
 			String endifLabel = X86NextLabel();
 			//X86write("ifeq " + elseLabel, 1);
 			//X86write("iconst_1", 1);
 			//X86write("goto " + endifLabel, 1);
 			//X86write(elseLabel+":");
 			X86traverse(children.get(1));
 			//X86write(endifLabel+":");
 			break;
 		}
 		case latteParser.NEGATION: {
 			X86traverse(children.get(0));
 		    //X86write("ineg", 1);
 		    break;
 		}
 		case latteParser.NOT: {
 			String elseLabel = X86NextLabel();
 			String endifLabel = X86NextLabel();
 			X86traverse(children.get(0));
 			//X86write("ifne " + elseLabel, 1);
 			//X86write("iconst_1", 1);
 			//X86write("goto " + endifLabel, 1);
 			//X86write(elseLabel+":");
 			//X86write("iconst_0", 1);
 			//X86write(endifLabel+":");
 			break;
 		}
 		case latteParser.VAR_IDENT: {
 			String idName = children.get(0).getText();
 			int idNo = X86VarToId(idName);
 			String type = X86GetVarType(idName);
 			//X86write(type + "load " + idNo, 1);
 			break;
 		}
 		case latteParser.INTEGER: {
 			return "$"+tree.getText();
 		}
 		case latteParser.FALSE: {
 			//X86write("ldc 0", 1);
 			break;
 		}
 		case latteParser.TRUE: {
 			//X86write("ldc 1", 1);
 			break;
 		}
 		case latteParser.STRING: {
 			//X86write("ldc " + tree.getText(), 1);
 			break;
 		}
 		default: {
 			if (children != null) {
 				for (Iterator<CommonTree> i = children.iterator(); i.hasNext();) {
 					CommonTree child = i.next();
 					X86traverse(child);
 				}
 			}
 			break;
 		}
 		
 		}
 
 		return null;
 	}
 
 	private String X86CheckPlusOpType(CommonTree node) {
 		String type = "i";
 		if (node.getType() == latteParser.VAR_IDENT) {
 			String idName = node.getChild(0).getText();
 			type = X86GetVarType(idName);
 		} else if (node.getType() == latteParser.STRING) {
 			type = "a";
 		} else if (node.getType() == latteParser.OP_PLUS) {
 			type = X86CheckPlusOpType((CommonTree)node.getChild(0));
 		}
 		return type;
 	}
 
 	private String X86GetVarType(String idName) {
 		for(int i = storage_var_types.size()-1; i >= 0; i--) {
 			HashMap<String,String> locVar = storage_var_types.get(i);
 			if (locVar.containsKey(idName)) {
 				return locVar.get(idName);
 			}
 		}
 		return null;
 	}
 
 	private String X86TypeForVar(int varType) {
 		switch (varType) {
 		case latteParser.TYPE_STRING:
 			return "a";
 		case latteParser.TYPE_VOID:
 			return "";
 		default:
 			return "i";
 		}
 	}
 
 	private int X86VarToId(String idName) {
 		for(int i = storage_vars.size()-1; i >= 0; i--) {
 			HashMap<String,Integer> locVar = storage_vars.get(i);
 			if (locVar.containsKey(idName)) {
 				return locVar.get(idName);
 			}
 		}
 		return -1;
 	}
 
 	private int X86FreeVarId(Stack<HashMap<String, Integer>> vars) {
 		int freeId = 0;
 		for (Iterator<HashMap<String, Integer>> iterator = vars.iterator(); iterator.hasNext();) {
 			HashMap<String, Integer> hashMap = (HashMap<String, Integer>) iterator.next();
 			freeId += hashMap.size();
 		}
		return freeId;
 	}
 		
 }
