 package org.soulspace.template.parser.ast.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.soulspace.template.method.IMethod;
 import org.soulspace.template.method.impl.MethodRegistryImpl;
 import org.soulspace.template.parser.GenerateException;
 import org.soulspace.template.parser.ast.IAstNode;
 import org.soulspace.template.parser.ast.IExpressionNode;
 import org.soulspace.template.symbols.ISymbol;
 
 public class TypeMethodCallNode extends ExpressionNode implements IExpressionNode {
 
 	private static MethodRegistryImpl methodRegistry = new MethodRegistryImpl();
 	
 	public TypeMethodCallNode() {
 		super();
 		setType(AstNodeType.TYPE_METHOD_CALL);
 	}
 
 	public ISymbol generateSymbol() {
 		IAstNode typeNode = getChild(0);
     ISymbol symbol = getSymbol(typeNode);    
     if (symbol == null) {
       throw new GenerateException("Variable " + typeNode.getData()
           + " is not initialized!");
     }
 
     IMethod method = methodRegistry.lookup(getData());
    if(method == null) {
      throw new GenerateException("Method " + getData()
          + " not found!");    	
    }
     List<ISymbol> args = new ArrayList<ISymbol>();
     args.add(symbol);
 
     if(getChildCount() == 2) {
       IAstNode argListNode = getChild(1);
       for(int i = 0; i < argListNode.getChildCount(); i++) {
       	ISymbol s = argListNode.getChild(i).generateSymbol();
       	args.add(s);
       }
     }
     
     ISymbol result = method.evaluate(args);
     return result;
 	}
 }
