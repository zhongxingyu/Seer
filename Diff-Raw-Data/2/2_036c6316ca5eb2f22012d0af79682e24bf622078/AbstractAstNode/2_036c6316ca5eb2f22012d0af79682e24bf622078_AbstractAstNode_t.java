 /*
  * Created on Mar 10, 2005
  */
 package org.soulspace.template.parser.ast.impl;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.soulspace.template.environment.Environment;
 import org.soulspace.template.exception.GenerateException;
 import org.soulspace.template.exception.SyntaxException;
 import org.soulspace.template.parser.ast.AstNode;
 import org.soulspace.template.parser.ast.AstNodeType;
 import org.soulspace.template.parser.ast.MethodNode;
 import org.soulspace.template.value.ListValue;
 import org.soulspace.template.value.MapValue;
 import org.soulspace.template.value.NumericValue;
 import org.soulspace.template.value.StringValue;
 import org.soulspace.template.value.SymbolTable;
 import org.soulspace.template.value.Value;
 import org.soulspace.template.value.ValueType;
 import org.soulspace.template.value.impl.NumericValueImpl;
 import org.soulspace.template.value.impl.StringValueImpl;
 
 /**
  * @author soulman
  * 
  */
 public abstract class AbstractAstNode implements AstNode {
 
 	private AstNode parent;
 	private AstNodeType type;
 	protected Environment environment = null;
 	private String template = "";
 	private int line = 0;
 	private SymbolTable symbolTable;
 	private List<AstNode> childNodes = new ArrayList<AstNode>();
 	private String data;
 	private Map<String, List<MethodNode>> methodRegistry;
 
 	/**
 	 * Constuctor
 	 */
 	public AbstractAstNode() {
 		super();
 	}
 
 	/**
 	 * Constructor
 	 * 
 	 * @param type
 	 * @param parent
 	 */
 	public AbstractAstNode(AstNode parent) {
 		super();
 		this.parent = parent;
 	}
 
 	/**
 	 * 
 	 * @param type
 	 */
 	public AbstractAstNode(AstNodeType type) {
 		super();
 		this.type = type;
 	}
 
 	/**
 	 * 
 	 * @return Returns the type.
 	 */
 	public AstNodeType getType() {
 		return type;
 	}
 
 	/**
 	 * 
 	 * @param type
 	 *            The type to set.
 	 */
 	public void setType(AstNodeType type) {
 		this.type = type;
 	}
 
 	/**
 	 * @return the template
 	 */
 	public String getTemplate() {
 		return template;
 	}
 
 	/**
 	 * @param template
 	 *            the template to set
 	 */
 	public void setTemplate(String template) {
 		this.template = template;
 	}
 
 	/**
 	 * @return the line
 	 */
 	public int getLine() {
 		return line;
 	}
 
 	/**
 	 * @param line
 	 *            the line to set
 	 */
 	public void setLine(int line) {
 		this.line = line;
 	}
 
 	/**
 	 * 
 	 * @return Returns the subNodes.
 	 */
 	public Collection<AstNode> getChildNodes() {
 		return childNodes;
 	}
 
 	public int getChildCount() {
 		return childNodes.size();
 	}
 
 	public void addChildNode(AstNode astNode) {
 		if (astNode != null) {
 			childNodes.add(astNode);
 			astNode.setParent(this);
 		}
 	}
 
 	public AstNode getParent() {
 		return parent;
 	}
 
 	public void setParent(AstNode parent) {
 		this.parent = parent;
 	}
 
 	public String getData() {
 		return data;
 	}
 
 	public Environment getEnvironment() {
 		return environment;
 	}
 
 	public void setEnvironment(Environment environment) {
 		this.environment = environment;
 	}
 
 	public Value lookupSymbol(String name) {
 		return environment.lookupValue(name);
 	}
 
 	public Value lookupSymbolInBlock(String name) {
 		Value symbol = null;
 		if (symbolTable != null) {
 			symbol = symbolTable.getSymbol(name);
 		}
 		return symbol;
 	}
 
 	protected Value derefSymbol(Value symbol, AstNode ref) {
 		Value aSymbol = null;
 		if (symbol == null) {
 			aSymbol = ref.generateValue(getEnvironment());
 		} else if (symbol instanceof MapValue) {
 			aSymbol = ((MapValue) symbol).getData().getSymbol(ref.getData());
 		} else if (symbol instanceof ListValue) {
 			if (isNumeric(ref.getData())) {
 				// Get entry by index
 				List<Value> list = ((ListValue) symbol).getData();
 				int i = Integer.parseInt(roundResult(ref.getData()));
 				if (list.size() > i) {
 					aSymbol = list.get(i);
 				}
 			}
 		}
 
 		return aSymbol;
 	}
 
 	protected Value derefSymbol(Value symbol, String ref) {
 		Value aSymbol = null;
 		if (symbol == null) {
 			aSymbol = lookupSymbol(ref);
 		} else if (symbol instanceof MapValue) {
 			aSymbol = ((MapValue) symbol).getData().getSymbol(ref);
 		} else if (symbol instanceof ListValue) {
 			if (isNumeric(ref)) {
 				// Get entry by index
 				List<Value> list = ((ListValue) symbol).getData();
 				int i = Integer.parseInt(roundResult(ref));
 				if (list.size() > i) {
 					aSymbol = list.get(i);
 				}
 			}
 		}
 
 		return aSymbol;
 	}
 
 	/**
 	 * @param data
 	 *            The data to set.
 	 */
 	public void setData(String data) {
 		this.data = data;
 	}
 
 	public String toString() {
 		StringBuilder sb = new StringBuilder(32);
 		sb.append("AstNode[");
 		sb.append("Type=" + getType().getName() + ", ");
 		if (getData() != null) {
 			sb.append("Data=" + getData() + ", ");
 		}
 		sb.append("Template=" + getTemplate() + ", ");
 		sb.append("Line=" + getLine());
 		sb.append("]");
 		return sb.toString();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.soulspace.templates.ast.IAstNode#getChild(int)
 	 */
 	public AstNode getChild(int index) {
 		if (childNodes.size() > index) {
 			return (AstNode) childNodes.get(index);
 		} else {
 			return null;
 		}
 	}
 
 	protected boolean isTrue(String value) {
 		if (!value.equals("") && !value.equals("0")
 				&& !value.equalsIgnoreCase("false")) {
 			return true;
 		}
 		return false;
 	}
 
 	public boolean isNumeric(String result) {
 		try {
 			Double.parseDouble(result);
 		} catch (NumberFormatException ex) {
 			return false;
 		}
 		return true;
 	}
 
 	public String roundResult(String result) {
 		if (!result.equals("") && isNumeric(result)) {
 			double doubleResult = Double.parseDouble(result);
 			long longResult = (new Double(doubleResult)).longValue();
 			if ((doubleResult - longResult) == 0) {
 				return String.valueOf(longResult);
 			}
 		}
 		return result;
 	}
 
 	public NumericValueImpl asNumeric(Value symbol) {
 		if (symbol == null) {
 			return new NumericValueImpl(0);
 		}
 		if (symbol instanceof NumericValue) {
 			return (NumericValueImpl) symbol;
 		} else if (symbol instanceof StringValue) {
 			return new NumericValueImpl(((StringValue) symbol).getData());
 		} else if (symbol instanceof ListValue) {
 			return new NumericValueImpl(((ListValue) symbol).getData().size());
 		} else if (symbol instanceof MapValue) {
 			return new NumericValueImpl(((MapValue) symbol).getData()
 					.getSymbolCount());
 		} else {
 			throw new GenerateException("Unknown type conversion to type numeric: "
 					+ symbol.getClass().getSimpleName() + "! Template "
 					+ getTemplate() + ", line " + getLine());
 		}
 	}
 
 	public StringValueImpl asString(Value symbol) {
 		if (symbol == null) {
 			return new StringValueImpl("");
 		}
 		if (symbol instanceof StringValue) {
 			return (StringValueImpl) symbol;
 		} else if (symbol instanceof NumericValue) {
 			return new StringValueImpl(((NumericValue) symbol).getData());
 		} else if (symbol instanceof ListValue) {
 			return new StringValueImpl(String.valueOf(((ListValue) symbol)
 					.getData().size()));
 		} else if (symbol instanceof MapValue) {
 			return new StringValueImpl(String.valueOf(((MapValue) symbol)
 					.getData().getSymbolCount()));
 		} else {
 			throw new GenerateException("Unknown type conversion to type string: "
 					+ symbol.getClass().getSimpleName() + "! Template "
 					+ getTemplate() + ", line " + getLine());
 		}
 	}
 
 	/**
 	 * 
 	 * @param symbol
 	 * @return
 	 */
 	String evaluate(Value symbol) {
 		String result = "";
 		if (symbol == null) {
 			// throw new GenerateException("Symbol not found: " + getData());
 			System.out.println("Symbol not found: " + getData());
 			return "";
 		}
 
 		if (symbol.getType().equals(ValueType.STRING)) {
 			result = ((StringValue) symbol).getData();
 		} else if (symbol.getType().equals(ValueType.NUMERIC)) {
 			result = ((NumericValue) symbol).getData();
 		} else if (symbol.getType().equals(ValueType.LIST)) {
 			// return number of elements in scalar context
 			// TODO is this necessary, now we have ":_SIZE"?
 			System.out.println("reducing list to scalar");
 			result = String.valueOf(((ListValue) symbol).getData().size());
 		} else if (symbol.getType().equals(ValueType.MAP)) {
 			// return number of elements in scalar context
 			// TODO is this necessary, now we have ":_SIZE"?
 			System.out.println("reducing map to scalar");
 			result = String.valueOf(((MapValue) symbol).getData()
 					.getSymbolCount());
 		} else if(symbol.getType().equals(ValueType.METHOD)) {
 			System.out.println("Don't know how to evaluate method symbol: " + getData());
 		}
 		return result;
 	}
 
 	public final void initMethodRegistry() {
 		this.methodRegistry = new HashMap<String, List<MethodNode>>();
 	}
 	
 	public Map<String, List<MethodNode>> getMethodRegistry() {
 		if (methodRegistry == null && parent != null) {
 			return parent.getMethodRegistry();
 		} else {
 //			if(methodRegistry != null) {
 //				System.out.println("Method registry found in node of type " + getType());
 //			}
 			return methodRegistry;
 		}
 	}
 
 	public MethodNode getMethodNode(AstNode callNode, String signature, List<Value> valueList) {
 		// TODO implement a lookup with signature compatibility and best match
 		// strategy
 		List<MethodNode> methodNodeList = getMethodRegistry().get(signature);
 		if(methodNodeList == null) {
 			throw new GenerateException(
 					"No method node found for signature "
 							+ signature + "! Called from template " + callNode.getTemplate() + ", line " + callNode.getLine());	
 		}
 		MethodNode matchedNode = null;
 		
 		for(MethodNode node : methodNodeList) {
 			AstNode paramList = node.getChild(0);
 			if(paramList.getChildCount() == valueList.size()) {
 				boolean matches = true;
 				for (int i = 0; i < paramList.getChildCount(); i++) {
 					AstNode paramNode = paramList.getChild(i);
 					String paramType = paramNode.getData();
 					if(valueList.get(i) == null ||
 							!valueList.get(i).getType().equals(ValueType.valueOf(paramType))) {
 						matches = false;
 					}
 				}
 				if(matches) {
 					matchedNode = node;
 				}
 			}
 		}
 		return matchedNode;
 	}
 
 	public void addMethodNode(MethodNode node) {
 		if (node != null) {
 			List<MethodNode> methodNodeList = getMethodRegistry().get(node.getMethodName());
 			if(methodNodeList == null) {
 				methodNodeList = new ArrayList<MethodNode>();
 				methodNodeList.add(node);
 				getMethodRegistry().put(node.getMethodName(), methodNodeList);
 				return;
 			} else {
 				MethodNode superNode = null;
 				for(MethodNode tNode : methodNodeList) {
 					if(checkTypes(node, tNode)) {
 						superNode = tNode;
 					}
 				}
 				if(superNode != null) {
 					methodNodeList.remove(superNode);
 					node.setSuperMethod(superNode);
 				}
 				methodNodeList.add(node);
 				getMethodRegistry().put(node.getMethodName(), methodNodeList);
 			}
 		} else {
 			throw new SyntaxException(
					"Error while trying to add a method node. The node is null!");
 		}
 	}
 
 	boolean checkTypes(MethodNode node1, MethodNode node2) {
 		AstNode argList1 = node1.getChild(0); 
 		AstNode argList2 = node2.getChild(0); 
 		if(argList1.getChildCount() != argList2.getChildCount()) {
 			return false;
 		}
 		for(int i = 0; i < argList1.getChildCount(); i++) {
 			AstNode argNode1 = argList1.getChild(i);
 			AstNode argNode2 = argList2.getChild(i);
 			if(!argNode1.getData().equals(argNode2.getData())) {
 				return false;
 			}
 		}
 		return true;
 	}
 }
