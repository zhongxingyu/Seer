 package latte.grammar;
 
 import org.antlr.runtime.tree.CommonTree;
 import org.antlr.runtime.tree.Tree;
 
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
 	private String className;
 	private String compiledStart = "";
 	private String compiledEnd = "";
 	private CommonTree troot;
 	FileWriter fwriter;
 	BufferedWriter output;
 	int globalVarShift = 0;
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
 		compiledEnd = compiledEnd.concat(indention + out + "\r\n");
 	}
 
 	private void X86preWrite(String out, int indentionLevel) {
 		String indention = "";
 		for (int i = 0; i < indentionLevel; i++) {
 			indention = indention.concat("    ");
 		}
 		compiledStart = compiledStart.concat(indention + out + "\r\n");
 	}
 
 	private void X86write(String out) throws IOException {
 		X86write(out, 0);
 	}
 
 	private void X86write(String func, String params) throws IOException {
 		X86write(func+"\t"+params, 2);
 	}
 
 	private void X86writeEnd() throws IOException {
 		output.write(compiledStart);
 		output.write(compiledEnd);
 		output.close();
 	}
 
 	public void X86generate() throws IOException {
 		X86LoadFunctions();
 
 		X86preWrite(".file	\"" + this.className + ".lat \"", 0);
 		X86preWrite(".text", 0);
 		X86preWrite("", 0);
 		X86preWrite("int_format:", 0);
 		X86preWrite(".string \"%d\\n\"", 2);
 		X86preWrite("int_read:", 0);
 		X86preWrite(".string \"%d\"", 2);
 		X86preWrite("str_format:", 0);
 		X86preWrite(".string \"%s\\n\"", 2);
 
 		X86write("", 0);
 
 		X86traverse(troot);
 		X86writeAux();
 
 		X86writeEnd();
 	}
 
 	private void X86writeAux() throws IOException {
 		X86write("",0);
 		X86write(".globl concat", 2);
 		X86write("concat:", 0);
 		X86write("push","%ebp");
 		X86write("mov", "%esp, %ebp");
 		X86write("pushl", "%edi");
 		X86write("subl", "$52, %esp");
 		X86write("movl", "8(%ebp), %eax");
 		X86write("movl", "$-1, -28(%ebp)");
 		X86write("movl", "%eax, %edx");
 		X86write("movl", "$0, %eax");
 		X86write("movl", "-28(%ebp), %ecx");
 		X86write("movl", "%edx, %edi");
 		X86write("repnz", "scasb");
 		X86write("movl", "%ecx, %eax");
 		X86write("notl", "%eax");
 		X86write("subl", "$1, %eax");
 		X86write("movl", "%eax, -20(%ebp)");
 		X86write("movl", "12(%ebp), %eax");
 		X86write("movl", "$-1, -28(%ebp)");
 		X86write("movl", "%eax, %edx");
 		X86write("movl", "$0, %eax");
 		X86write("movl", "-28(%ebp), %ecx");
 		X86write("movl", "%edx, %edi");
 		X86write("repnz", "scasb");
 		X86write("movl", "%ecx, %eax");
 		X86write("notl", "%eax");
 		X86write("subl", "$1, %eax");
 		X86write("movl", "%eax, -16(%ebp)");
 		X86write("movl", "-16(%ebp), %eax");
 		X86write("movl", "-20(%ebp), %edx");
 		X86write("addl", "%edx, %eax");
 		X86write("addl", "$1, %eax");
 		X86write("movl", "%eax, (%esp)");
 		X86write("call", "malloc");
 		X86write("movl", "%eax, -12(%ebp)");
 		X86write("movl", "-12(%ebp), %eax");
 		X86write("movl", "8(%ebp), %edx");
 		X86write("movl", "-20(%ebp), %ecx");
 		X86write("movl", "%ecx, 8(%esp)");
 		X86write("movl", "%edx, 4(%esp)");
 		X86write("movl", "%eax, (%esp)");
 		X86write("call", "memcpy");
 		X86write("movl", "-16(%ebp), %eax");
 		X86write("leal", "1(%eax), %ecx");
 		X86write("movl", "-20(%ebp), %eax");
 		X86write("movl", "-12(%ebp), %edx");
 		X86write("addl", "%edx, %eax");
 		X86write("movl", "12(%ebp), %edx");
 		X86write("movl", "%ecx, 8(%esp)");
 		X86write("movl", "%edx, 4(%esp)");
 		X86write("movl", "%eax, (%esp)");
 		X86write("call", "memcpy");
 		X86write("movl", "-12(%ebp), %eax");
 		X86write("addl", "$52, %esp");
 		X86write("popl", "%edi");
 		X86write("popl", "%ebp");
 		X86write("ret", 2);
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
 			//int varType = children.get(0).token.getType(); // TODO: for byte count
 			for(int i = 1; i < children.size(); i++) {
 				CommonTree child = children.get(i);
 				@SuppressWarnings("unchecked")
 				List<CommonTree> declaration = child.getChildren();
 				String ident = declaration.get(0).token.getText();
 				storage_vars.peek().put(ident, 4);
 			}
 			break;
 		}
 		case latteParser.SFOR: {
 			int count = X86CountLocals(children.get(2));
 			return count + 8; // var + counter
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
 		return "_Label_"+labelCounter;
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
 			if (name.compareTo("main") == 0) {
 				X86write(".global main", 2);
 				X86write(name+":");
 			} else {
 				X86write(".global "+name, 2);
 				X86write("__"+name+":");
 			}
 
 			X86write("push","%ebp");
 			X86write("mov", "%esp, %ebp");
 
 			if (name.compareTo("main") == 0) {
 				X86write("and", "$0xfffffff0,%esp");
 			}
 			
 			if (max_bytes_for_locals != 0) {
 				X86write("sub", "$"+max_bytes_for_locals+",%esp");
 			}
 
 		    X86TypeForVar(children.get(0).getType());
 			storage_vars.push(new HashMap<String, Integer>());
 			storage_var_types.push(new HashMap<String, String>());
 			if (args.getType() == latteParser.ARGS) {
 				@SuppressWarnings("unchecked")
 				List<CommonTree> argsList = args.getChildren();
 				int freeId = 4;
 				for (int i = 0; i < argsList.size(); i++) {
 					int type = argsList.get(i).getChild(0).getType();
 					String ident = argsList.get(i).getChild(1).getText();
 					freeId = freeId + 4;
 					storage_vars.peek().put(ident, freeId);
 					storage_var_types.peek().put(ident, X86TypeForVar(type));
 				}
 				X86traverse(children.get(3));
 			} else {
 				X86traverse(children.get(2));
 			}
 			storage_vars.pop();
 			storage_var_types.pop();
 
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
 						if (src.contains("%ebp") || src.contains("(")) {
 							X86write("movl", src+", %eax");
 							src = "%eax";
 						}
 					    X86write("movl", src+", "+freeIdShift+"(%ebp)");
 					} else {
 					    X86write("movl", "$0, "+freeIdShift+"(%ebp)");
 					}
 					break;
 				}
 				case latteParser.TYPE_STRING: {
 					if (declaration.size() == 2) {
 					    String src = X86traverse(declaration.get(1));
 					    X86write("movl", src+", "+freeIdShift+"(%ebp)");
 					} else {
 						X86write("movl", "$1, (%esp)");
 						X86write("call", "malloc");
 						X86write("movl", "%eax, "+freeIdShift+"(%ebp)");
 						X86write("movb", "$0, (%eax)");
 					}
 					break;
 				}
 				case latteParser.ARRTYPE: {
 					if (declaration.size() == 2) {
 						String len = "";
 						if (declaration.get(1).getType() == latteParser.NEWARR) {
 							len = X86traverse((CommonTree)declaration.get(1).getChild(1));
 							X86write("mov", len+", %eax");
 						    X86write("imull", "$4, %eax");
 						    X86write("addl", "$4, %eax");
 							X86write("movl", "%eax, (%esp)");
 							X86write("call", "malloc");
 							X86write("movl", "%eax, "+freeIdShift+"(%ebp)");
 							X86write("movl", freeIdShift+"(%ebp), %eax");
 							len = X86traverse((CommonTree)declaration.get(1).getChild(1));
 						    X86write("movl", len+", %edx");
 							//X86write("addl", "$0, %eax");
 							X86write("movl", "%edx, (%eax)");
 						} else {
 							len = X86traverse(declaration.get(1));
 						    X86write("movl", len+", "+freeIdShift+"(%ebp)");
 						}
 					} else {
 						X86write("movl", "$4, (%esp)");
 						X86write("call", "malloc");
 						X86write("movl", "%eax, "+freeIdShift+"(%ebp)");
 						X86write("movl", "$0, (%eax)");
 					}
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
 				String src = X86traverse(children.get(1));
 				X86write("pusha", 2);
 				X86write("pushl", src);
 			    X86write("pushl", "$int_format");
 			    X86write("call", "printf");
 			    X86write("add", "$8, %esp");
 			    X86write("popa", 2);
 			} else if (functionName.compareTo("printString") == 0) {
 				String src = X86traverse(children.get(1));
 				X86write("pusha", 2);
 				X86write("pushl", src);
 			    X86write("pushl", "$str_format");
 			    X86write("call", "printf");
 			    X86write("add", "$8, %esp");
 			    X86write("popa", 2);
 			} else if (functionName.compareTo("error") == 0) {
 			} else if (functionName.compareTo("readInt") == 0) {
 				X86write("pushl", "$0");
 				X86write("leal", "(%esp), %eax");
 				X86write("pushl", "%eax");
 			    X86write("pushl", "$int_read");
 			    X86write("call", "scanf");
 			    X86write("addl", "$8, %esp");
 			    X86write("popl", "%eax");
 			} else if (functionName.compareTo("readString") == 0) {
 				X86write("pushl", "$0"); // pointer to string
 				X86write("leal", "(%esp), %edx"); // make pointer to pointer
 				X86write("pushl", "$8"); // no of bytes to read
 				X86write("leal", "(%esp), %ecx"); // pointer to no of bytes
 				X86write("movl", "stdin, %eax"); // descriptor
 				X86write("pushl", "%eax");
 				X86write("pushl", "%ecx");
 				X86write("pushl", "%edx");
 			    X86write("call", "getline");
 			    X86write("add", "$16, %esp");
 
 			    X86write("pop", "%eax");
 				X86write("pushl", "%eax"); // eax holds string
 
 				X86write("movl", "%eax, %edx");
 				X86write("movl", "$0, %eax");
 				X86write("movl", "$-1, %ecx");
 				X86write("movl", "%edx, %edi");
 				X86write("repnz", "scasb");
 				X86write("movl", "%ecx, %eax");
 				X86write("notl", "%eax");
 				X86write("subl", "$1, %eax"); // eax holds string length
 
 				X86write("movl", "%eax, %edx"); // edx holds string length
 			    X86write("pop", "%eax");
 				X86write("pushl", "%eax"); // eax holds string
 
 				X86write("addl", "%edx, %eax");
 				X86write("movzbl", "(%eax), %eax");
 				X86write("cmpb", "$10, %al");
 				String endifLabel = X86NextLabel();
 				//X86write("jne", endifLabel); // FIXME
 			    X86write("pop", "%eax");
 				X86write("pushl", "%eax"); // eax holds string
 				X86write("subl", "$1, %edx");
 				X86write("addl", "%edx, %eax");
 				X86write("movb", "$0, (%eax)");
 				X86write(endifLabel+" :");
 			    X86write("pop", "%eax");
 			} else {
 				for (int i = children.size() - 1; i > 0; --i) {
 					String src = X86traverse(children.get(i));
 					X86write("push", src);
 				}
 				if (functionName.compareTo("main") == 0) {
 					X86write("call", functionName);
 				} else {
 					X86write("call", "__"+functionName);
 				}
 				int argsCount = children.size() - 1;
 				if (argsCount != 0) {
 					X86write("add", "$"+argsCount*4+", %esp");
 				}
 			}
 			return "%eax";
 		}
 		case latteParser.COND: {
 			if (children.size() == 3) {
 				String elseLabel = X86NextLabel();
 				String endifLabel = X86NextLabel();
 				String src = X86traverse(children.get(0));
 				X86write("mov", src+", %eax");
 				X86write("cmp", "$1, %eax");
 				X86write("jne", elseLabel);
 				X86traverse(children.get(1));
 				X86write("jmp", endifLabel);
 				X86write(elseLabel+" :");
 				X86traverse(children.get(2));
 				X86write(endifLabel+" :");
 			} else {
 				String endifLabel = X86NextLabel();
 				String src = X86traverse(children.get(0));
 				X86write("mov", src+", %eax");
 				X86write("cmp", "$1, %eax");
 				X86write("jne", endifLabel);
 				X86traverse(children.get(1));
 				X86write(endifLabel+" :");
 			}
 			break;
 		}
 		case latteParser.SWHILE: {
 			String whilebodyLabel = X86NextLabel();
 			String endwhileLabel = X86NextLabel();
 			X86write(whilebodyLabel+" :");
 			String src = X86traverse(children.get(0));
 			X86write("mov", src+", %eax");
 			X86write("cmp", "$1, %eax");
 			X86write("jne", endwhileLabel);
 			X86traverse(children.get(1));
 			X86write("jmp", whilebodyLabel);
 			X86write(endwhileLabel+" :");
 			break;
 		}
 		case latteParser.ASS: {
 			if (children.get(0).getType() == latteParser.SUBSCRIB) {
 				String src2 = X86traverse(children.get(1));
 				X86write("movl", src2+", %eax");
 				X86write("pushl", "%eax");
 
 				String ind = X86traverse((CommonTree)children.get(0).getChild(1));
 				X86write("movl", ind+", %edx");
 				String src = X86traverse((CommonTree)children.get(0).getChild(0));
 				X86write("movl", src+", %eax");
 				X86write("addl", "$1, %edx");
 				X86write("imul", "$4, %edx");
 				X86write("addl", "%edx, %eax");
 				X86write("popl", "%edx");
 				X86write("movl", "%edx, (%eax)");
 				break;
 			} else {
 				String src = X86traverse(children.get(1));
 				String idName = children.get(0).getChild(0).getText();
 				int idNo = X86VarToId(idName);
 				if (src.contains("%ebp") || src.contains("(")) {
 					X86write("movl", src+", %eax");
 					src = "%eax";
 				}
 				X86write("movl", src+", "+idNo+"(%ebp)");
 				break;
 			}
 		}
 		case latteParser.DECR: {
 			String idName = children.get(0).getText();
 			int idNo = X86VarToId(idName);
 			X86write("subl", "$1, "+idNo+"(%ebp)");
 			break;
 		}
 		case latteParser.INCR: {
 			String idName = children.get(0).getText();
 			int idNo = X86VarToId(idName);
 			X86write("addl", "$1, "+idNo+"(%ebp)");
 			break;
 		}
 		case latteParser.RET: {
 			String src = X86traverse(children.get(0));
 		    X86write("mov", src+", %eax");
 			X86write("mov", "%ebp, %esp");
 			X86write("pop", "%ebp");
 			X86write("ret", 2);
 			break;
 		}
 		case latteParser.RETV: {
 			X86write("mov", "%ebp, %esp");
 			X86write("pop", "%ebp");
 			X86write("ret", 2);
 			break;
 		}
 		case latteParser.SFOR: {
 			storage_vars.push(new HashMap<String, Integer>());
 			storage_var_types.push(new HashMap<String, String>());
 
 			X86traverse(children.get(0));
 			String tabIdent = children.get(1).getText();
 			int counterid = X86FreeVarId(storage_vars);
 			int varid = counterid + 4;
 			globalVarShift += 1;
 			
 		    X86write("movl", "$0, "+counterid+"(%ebp)");
 
 			String whilebodyLabel = X86NextLabel();
 			String endwhileLabel = X86NextLabel();
 			X86write(whilebodyLabel+" :");
 
 			int idNo = X86VarToId(tabIdent);
 			X86write("movl", idNo+"(%ebp), %ecx");
 			X86write("movl", "(%ecx), %eax"); // array length in eax
 
 			String elseLabel = X86NextLabel();
 			String endifLabel = X86NextLabel();
 			X86write("cmp", counterid+"(%ebp), %eax");
 			X86write("jg", elseLabel);
 		    X86write("movl", "$0, %eax");
 			X86write("jmp", endifLabel);
 			X86write(elseLabel+" :");
 		    X86write("movl", "$1, %eax");
 			X86write(endifLabel+" :"); // bool condition in eax
 			
 			X86write("cmp", "$1, %eax");
 			X86write("jne", endwhileLabel);
 
 			X86write("movl", idNo+"(%ebp), %ecx");
 			X86write("movl", counterid+"(%ebp), %edx");
 			X86write("addl", "$1, %edx");
 			X86write("imul", "$4, %edx");
 			X86write("addl", "%edx, %ecx");
 			X86write("movl", "(%ecx), %eax");
 		    X86write("movl", "%eax, "+varid+"(%ebp)");
 			
 			X86traverse(children.get(2)); // loop body
 
 			X86write("addl", "$1, "+counterid+"(%ebp)"); // counter increment
 
 			X86write("jmp", whilebodyLabel);
 			X86write(endwhileLabel+" :");
 
 			storage_vars.pop();
 			storage_var_types.pop();
 			globalVarShift -= 1;
 			break;
 		}
 		case latteParser.ARRTYPE: {
 			break;
 		}
 		case latteParser.NEWARR: {
 			break;
 		}
 		case latteParser.SUBSCRIB: {
 			String ind = X86traverse(children.get(1));
 			X86write("movl", ind+", %edx");
 			String src = X86traverse(children.get(0));
 			X86write("movl", src+", %eax");
 			X86write("addl", "$1, %edx");
 			X86write("imul", "$4, %edx");
 			X86write("addl", "%edx, %eax");
 			return "(%eax)";
 		}
 		case latteParser.ATTRIBUTE: {
 			String src = X86traverse(children.get(0));
 			X86write("movl", src+", %eax");
 			X86write("movl", "(%eax), %eax");
 			return "%eax";
 		}
 		case latteParser.OP_PLUS: {
 			String type = X86CheckPlusOpType(children.get(0));
 			if (type.compareTo("a") == 0) {
 				String src = X86traverse(children.get(1));
 			    X86write("push", src);
 				src = X86traverse(children.get(0));
 			    X86write("push", src);
 			    X86write("call concat", 2);
 			    X86write("add", "$8, %esp");
 			    return "%eax";
 			} else {
 				String src1 = X86traverse(children.get(1));
 			    X86write("pushl", src1);
 				String reg = X86traverse(children.get(0));
 				if (!reg.startsWith("%")) {
 				    X86write("mov", reg+", %eax");
 				    reg = "%eax";
 				}
 			    X86write("popl", "%edx");
 			    X86write("addl", "%edx, "+reg);
 			    return reg;
 			}
 		}
 		case latteParser.OP_MINUS: {
 			String src1 = X86traverse(children.get(1));
 		    X86write("pushl", src1);
 			String reg = X86traverse(children.get(0));
 			if (!reg.startsWith("%")) {
 			    X86write("mov", reg+", %eax");
 			    reg = "%eax";
 			}
 		    X86write("popl", "%edx");
 		    X86write("subl", "%edx, "+reg);
 		    return reg;
 		}
 		case latteParser.OP_TIMES: {
 			String src1 = X86traverse(children.get(1));
 		    X86write("pushl", src1);
 			String reg = X86traverse(children.get(0));
 			if (!reg.startsWith("%eax")) {
 			    X86write("mov", reg+", %eax");
 			}
 		    X86write("popl", "%edx");
 		    X86write("imul", "%edx");
 		    return "%eax";
 		}
 		case latteParser.OP_DIV: {
 			String src1 = X86traverse(children.get(1));
 		    X86write("pushl", src1);
 			String reg = X86traverse(children.get(0));
 			if (!reg.startsWith("%eax")) {
 			    X86write("mov", reg+", %eax");
 			}
 		    X86write("popl", "%ecx");
 		    X86write("mov", "$0, %edx");
 		    X86write("idiv", "%ecx");
 		    return "%eax";
 		}
 		case latteParser.OP_MOD: {
 			String src1 = X86traverse(children.get(1));
 		    X86write("pushl", src1);
 			String reg = X86traverse(children.get(0));
 			if (!reg.startsWith("%eax")) {
 			    X86write("mov", reg+", %eax");
 			}
 		    X86write("popl", "%ecx");
 		    X86write("mov", "$0, %edx");
 		    X86write("idiv", "%ecx");
 		    X86write("mov", "%edx, %eax");
 		    return "%eax";
 		}
 		case latteParser.OP_LTH: {
 			return X86ConditionCompile("jl", children);
 		}
 		case latteParser.OP_LE: {
 			return X86ConditionCompile("jle", children);
 		}
 		case latteParser.OP_GTH: {
 			return X86ConditionCompile("jg", children);
 		}
 		case latteParser.OP_GE: {
 			return X86ConditionCompile("jge", children);
 		}
 		case latteParser.OP_EQU: {
 			return X86ConditionCompile("je", children);
 		}
 		case latteParser.OP_NE: {
 			return X86ConditionCompile("jne", children);
 		}
 		case latteParser.OP_AND: {
 			String elseLabel = X86NextLabel();
 			String endifLabel = X86NextLabel();
 			String src = X86traverse(children.get(0));
 			if (!src.startsWith("%eax")) {
 			    X86write("mov", src+", %eax");
 			}
 			X86write("cmp", "$0, %eax");
 			X86write("je", elseLabel);
 			src = X86traverse(children.get(1));
 			if (!src.startsWith("%eax")) {
 			    X86write("mov", src+", %eax");
 			}
 			X86write("cmp", "$0, %eax");
 			X86write("je", elseLabel);
 		    X86write("mov", "$1, %eax");
 			X86write("jmp", endifLabel);
 			X86write(elseLabel+" :");
 		    X86write("mov", "$0, %eax");
 			X86write(endifLabel+" :");
 			return "%eax";
 		}
 		case latteParser.OP_OR: {
 			String elseLabel = X86NextLabel();
 			String endifLabel = X86NextLabel();
 			String src = X86traverse(children.get(0));
 			if (!src.startsWith("%eax")) {
 			    X86write("mov", src+", %eax");
 			}
 			X86write("cmp", "$1, %eax");
 			X86write("je", elseLabel);
 			src = X86traverse(children.get(1));
 			if (!src.startsWith("%eax")) {
 			    X86write("mov", src+", %eax");
 			}
 			X86write("cmp", "$1, %eax");
 			X86write("je", elseLabel);
 		    X86write("mov", "$0, %eax");
 			X86write("jmp", endifLabel);
 			X86write(elseLabel+" :");
 		    X86write("mov", "$1, %eax");
 			X86write(endifLabel+" :");
 			return "%eax";
 		}
 		case latteParser.NEGATION: {
 			String src = X86traverse(children.get(0));
 			if (!src.startsWith("%")) {
 				X86write("mov", src+", %edx");
 				src = "%edx";
 			}
 			X86write("neg", src);
 			return src;
 		}
 		case latteParser.NOT: {
 			String elseLabel = X86NextLabel();
 			String endifLabel = X86NextLabel();
 			String src = X86traverse(children.get(0));
 			if (!src.startsWith("%eax")) {
 			    X86write("mov", src+", %eax");
 			}
 			X86write("cmp", "$1, %eax");
 			X86write("je", elseLabel);
 		    X86write("mov", "$1, %eax");
 			X86write("jmp", endifLabel);
 			X86write(elseLabel+" :");
 		    X86write("mov", "$0, %eax");
 			X86write(endifLabel+" :");
 			return "%eax";
 		}
 		case latteParser.VAR_IDENT: {
 			String idName = children.get(0).getText();
 			int idNo = X86VarToId(idName);
 			return idNo+"(%ebp)";
 		}
 		case latteParser.INTEGER: {
 			return "$"+tree.getText();
 		}
 		case latteParser.FALSE: {
 			return "$0";
 		}
 		case latteParser.TRUE: {
 			return "$1";
 		}
 		case latteParser.STRING: {
 			String str_label = X86NextLabel();
 			X86preWrite(str_label+":", 0);
 			X86preWrite(".string "+tree.getText(), 2);
 			return "$"+str_label;
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
 
 	private String X86ConditionCompile(String condition, List<CommonTree> children) throws IOException {
 		String elseLabel = X86NextLabel();
 		String endifLabel = X86NextLabel();
 		String src = X86traverse(children.get(1));
 	    X86write("pushl", src);
 	    src = X86traverse(children.get(0));
 		if (!src.startsWith("%eax")) {
 		    X86write("movl", src+", %eax");
 		}
 	    X86write("popl", "%ecx");
 		X86write("cmp", "%ecx, %eax");
 		X86write(condition, elseLabel);
 	    X86write("movl", "$0, %eax");
 		X86write("jmp", endifLabel);
 		X86write(elseLabel+" :");
 	    X86write("movl", "$1, %eax");
 		X86write(endifLabel+" :");
 		return "%eax";
 	}
 
 	private String X86CheckPlusOpType(CommonTree node) {
 		String type = "i";
 		if (node.getType() == latteParser.VAR_IDENT) {
 			String idName = node.getChild(0).getText();
 			type = X86GetVarType(idName);
 		} else if (node.getType() == latteParser.EAPP) {
 			String functionName = node.getChild(0).getText();
 			type = storage_func.get(functionName);
 		} else if (node.getType() == latteParser.STRING) {
 			type = "a";
 		} else if (node.getType() == latteParser.OP_PLUS) {
 			type = X86CheckPlusOpType((CommonTree)node.getChild(0));
 		}
 		
 		if (type.contains("String;")) {
 			type = "a";
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
 			return "l";
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
 		for(int i = vars.size()-1; i >= 1; i--) {
 			HashMap<String, Integer> locVar = vars.get(i);
 			freeId += locVar.size();
 		}
 		freeId += globalVarShift;
 		return -1 * (freeId * 4 + 4);
 	}
 		
 }
