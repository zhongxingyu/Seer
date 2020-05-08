 /*
  * Created on Apr 18, 2005
  */
 package org.soulspace.template.parser.ast.impl;
 
 import java.util.List;
 
 import org.soulspace.template.exception.GenerateException;
 import org.soulspace.template.parser.ast.AstNodeType;
 import org.soulspace.template.parser.ast.IAstNode;
 import org.soulspace.template.value.IListValue;
 import org.soulspace.template.value.IValue;
 import org.soulspace.template.value.impl.StringValue;
 import org.soulspace.template.value.impl.SymbolTable;
 import org.soulspace.template.value.impl.ValueType;
 
 public class ForeachNode extends AbstractAstNode {
 
 	/**
    * 
    */
 	public ForeachNode() {
 		this(null);
 	}
 
 	/**
 	 * @param parent
 	 */
 	public ForeachNode(IAstNode parent) {
 		super(parent);
 		setType(AstNodeType.FOREACH);
 	}
 
 	public IValue generateSymbol() {
 		StringBuilder sb = new StringBuilder(128);
 		setSymbolTable(new SymbolTable());
 
 		List<IValue> list;
 
 		if (getChildNodes().size() < 3 || getChildNodes().size() > 4) {
 			throw new GenerateException("Syntax error in foreach! Template "
 					+ getTemplate() + ", line " + getLine());
 		}
 
 		IdentifierNode id = (IdentifierNode) getChild(0);
 		String elName = id.getData();
 
 		if (lookupSymbolInBlock(id.getData()) != null) {
			throw new GenerateException("Symbol already exists: "
					+ id.getData() + "! Template " + getTemplate() + ", line "
 					+ getLine());
 		}
 
		// TODO implement filter
 		IAstNode loopNode = null;
 		IAstNode filterNode = null;
 		IAstNode stmtNode = null;
 		if (getChildNodes().size() == 4) {
 			filterNode = getChild(1);
 			loopNode = getChild(2);
 			stmtNode = getChild(3);
 		} else {
 			loopNode = getChild(1);
 			stmtNode = getChild(2);
 		}
 
 		// lookup loop variable
 		IValue symbol = getSymbol(loopNode);
 		if (symbol == null) {
 			// Missing Variable
 			throw new GenerateException("Variable " + loopNode.getData()
					+ " is not initialized! Template " + getTemplate()
 					+ ", line " + getLine());
 		}
 
 		// Check type of the variable
 		if (!(symbol.getType() == ValueType.LIST)) {
 			// Variable not of type LIST
 			throw new GenerateException(
					"Expecting variable of type LIST! Template "
 							+ getTemplate() + ", line " + getLine());
 		}
 		IListValue lSymbol = (IListValue) symbol;
 		// Get List to iterate over
 		list = lSymbol.getData();
 
 		for (int i = 0; i < list.size(); i++) {
 			// Set iteration variables
 			lSymbol.setIndex(i);
 			lSymbol.setEntry(list.get(i));
 			getSymbolTable().addSymbol(elName, list.get(i));
 
 			// execute block
 			if (filterNode != null) {
 				// filter present
 				IValue exSymbol = null;
 				exSymbol = filterNode.generateSymbol();
 				if (exSymbol != null && exSymbol.isTrue()) {
 					sb.append(stmtNode.generateSymbol().evaluate());
 				}
 			} else {
 				sb.append(stmtNode.generateSymbol().evaluate());
 			}
 		}
 
 		// Clear 'ENTRY' reference
 		lSymbol.setEntry(null);
 
		// System.out.append(sb.toString());
 		return new StringValue(sb.toString());
 	}
 }
