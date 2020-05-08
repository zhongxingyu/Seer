 package org.soulspace.template.parser.ast.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.soulspace.template.exception.GenerateException;
 import org.soulspace.template.parser.ast.AstNodeType;
 import org.soulspace.template.parser.ast.AstNode;
 import org.soulspace.template.parser.ast.MethodNode;
 import org.soulspace.template.parser.ast.Signature;
 import org.soulspace.template.value.MethodValue;
 import org.soulspace.template.value.SymbolTable;
 import org.soulspace.template.value.Value;
 import org.soulspace.template.value.ValueType;
 import org.soulspace.template.value.impl.MethodValueImpl;
 import org.soulspace.template.value.impl.StringValueImpl;
 import org.soulspace.template.value.impl.SymbolTableImpl;
 
 public class MethodCallNodeImpl extends AbstractAstNode {
 
 	public MethodCallNodeImpl() {
 		super();
 		setType(AstNodeType.METHOD_CALL);
 	}
 
 	public String getSignatureString(List<Value> valueList) {
 		StringBuilder sb = new StringBuilder();
 		sb.append(getData());
 		sb.append("(");
 		String sep = "";
 		for(Value v : valueList) {
 			sb.append(sep);
 			sb.append(v.getType());
 			sep = ", ";
 		}
 		sb.append(")");
 		return sb.toString();
 	}
 
 	public String getMethodName() {
 		return getData();
 	}
 
 	public Value generateValue() {
		if (getMethodName().equals("super")) {
			return generateSuperCall();
		} else {
			return generateMethodCall();
 		}
 	}
 
 	public List<Value> evaluateArgList(ArgListNodeImpl argList) {
 		List<Value> valueList = new ArrayList<Value>();
 		for(int i = 0; i < argList.getChildCount(); i++) {
 			AstNode argNode = argList.getChild(i);
 			valueList.add(argNode.generateValue());
 		}
 		return valueList;
 	}
 	
 	public Value generateMethodCall() {
 		MethodNode methodNode;
 		ArgListNodeImpl argList = (ArgListNodeImpl) getChild(0);
 		List<Value> valueList = evaluateArgList(argList);
 		
 		Value value = lookupSymbol(getMethodName());
 		
 		if(value != null && value.getType().equals(ValueType.METHOD)) {
 			MethodValue methodValue = (MethodValue) value;
 			// lookup method by the data of the value
 			methodNode = getMethodNode(methodValue.getData(), valueList);
 		} else {
 			methodNode = getMethodNode(getMethodName(), valueList);
 		}
 		
 		if(methodNode != null) {
 			// create symbol table for arguments
 			SymbolTable symbolTable = createSymbolTable(methodNode, valueList);
 
 			// generate from the method
 			return ((MethodNodeImpl) methodNode).generateSymbol(methodNode,
 					symbolTable);		
 		} else {
 			throw new GenerateException(
 					"No method node found for signature "
 							+ getSignatureString(valueList) + "! Template " + getTemplate() + ", line " + getLine());
 		}
 	}
 	
 	public Value generateSuperCall() {
 		AstNode node = getParent();
 		boolean found = false;
 		while (!found) {
 			if (node != null) {
 				if (node instanceof MethodNodeImpl) {
 					MethodNodeImpl mNode = (MethodNodeImpl) node;
 					if (mNode.getSuperMethod() == null) {
 						throw new GenerateException(
 								"super() call, but there is no super method! Template " + getTemplate() + ", line " + getLine());
 					}
 					return mNode.callSuperMethod();
 				}
 				node = node.getParent();
 			} else {
 				throw new GenerateException(
 						"super() call outside a method body! Template " + getTemplate() + ", line " + getLine());
 			}
 		}
 		return new StringValueImpl("");
 	}
 	
 	/**
 	 * 
 	 * @param node
 	 * @return
 	 * @throws GenerateException
 	 */
 	SymbolTable createSymbolTable(AstNode node, List<Value> valueList) throws GenerateException {
 		SymbolTable symbolTable = new SymbolTableImpl();
 
 		if (!(node instanceof MethodNodeImpl)) {
 			throw new GenerateException("Wrong node type " + node.getType() + "! Template " + getTemplate() + ", line " + getLine());
 		}
 		if (node.getChild(0) == null) {
 			throw new GenerateException("Missing parameter list! Template " + getTemplate() + ", line " + getLine());
 		}
 		AstNode paramList = node.getChild(0);
 		if (paramList.getChildCount() != valueList.size()) {
 			throw new GenerateException("Wrong argument count in method "
 					+ getData() + "! Template " + getTemplate() + ", line " + getLine());
 		}
 
 		for (int i = 0; i < paramList.getChildCount(); i++) {
 			AstNode paramNode = paramList.getChild(i);
 			String paramName = paramNode.getChild(0).getData();
 			symbolTable.addSymbol(paramName, valueList.get(i));
 		}
 
 		return symbolTable;
 	}
 
 }
