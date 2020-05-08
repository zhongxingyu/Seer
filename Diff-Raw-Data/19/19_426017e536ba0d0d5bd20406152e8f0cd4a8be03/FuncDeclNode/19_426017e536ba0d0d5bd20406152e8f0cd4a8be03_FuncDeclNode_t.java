 package cs4120.der34dlc287lg342.xi.ast;
 
 import java.util.ArrayList;
 
 import cs4120.der34dlc287lg342.xi.typechecker.*;
 
 import edu.cornell.cs.cs4120.util.VisualizableTreeNode;
 import edu.cornell.cs.cs4120.xi.AbstractSyntaxNode;
 import edu.cornell.cs.cs4120.xi.CompilationException;
 import edu.cornell.cs.cs4120.xi.Position;
 
 public class FuncDeclNode extends AbstractSyntaxTree {
 
 	public Position position;
 	/**The id of this function declaration represented as an 
 	 * IDNode*/
 	public AbstractSyntaxNode id;
 	/**The arguments of this function declaration represented as a list 
 	 * of variable declarations*/
 	public ArrayList<VisualizableTreeNode> args;
 	/**?? All the children of this function declaration node.*/
 	protected ArrayList<VisualizableTreeNode> children;
 	/**The block associated with this function declaration represented
 	 * as a BlockNode*/
 	public BlockNode block;
 	/**The return types of this function declaration represented as a
 	 * list of  XiPrimitiveTypes. Note this is not a Node in the AST. */
 	public ArrayList<XiPrimitiveType> types;
 	
 	/**Sets all fields including the type field which is part of the super class AbstractSyntaxTree*/
 	public FuncDeclNode(IdNode id, ArrayList<VisualizableTreeNode> args, ArrayList<XiPrimitiveType> types, BlockNode block, Position position){
 		this.id = id;
 		this.args = args;
 		this.position = position;
 		this.block = block;
 		this.types = types;
 		children = new ArrayList<VisualizableTreeNode>();
 		children.add(id);
 		children.addAll(args);
 		children.add(block);
 		
 		ArrayList<XiPrimitiveType> argumentList = new ArrayList<XiPrimitiveType>();
 		for (VisualizableTreeNode arg : this.args){
 			DeclNode decl = (DeclNode) arg;
 			argumentList.add(new XiPrimitiveType(decl.type_name, decl.brackets));
 		}
 		
 		this.type = new XiFunctionType(argumentList, this.types);;
 	}
 	
 	@Override
 	public Position position() {
 		return position;
 	}
 
 	@Override
 	public Iterable<VisualizableTreeNode> children() {
 		return children;
 	}
 	
 	@Override
 	public String label() {
 		return "FUNCDECL";
 	}
 
 	public XiType typecheck(ContextList stack) throws CompilationException{
 		// push a new context frame onto the stack
 		XiTypeContext frame = new XiTypeContext((XiFunctionType) type);
 		/*
 		 * before type checking this function we need to add the arguments to the
 		 * current context
 		 */
 		for (VisualizableTreeNode arg : this.args){ 
 			DeclNode decl = (DeclNode) arg;
 			IdNode id = (IdNode)decl.id;
 			try {
 				frame.add(id.id, new XiPrimitiveType(decl.type_name, decl.brackets));
 			} catch (InvalidXiTypeException e) {
 				throw new CompilationException(e.getMessage(), position());
 			}
 		}
 		stack.add(frame);
 		
 		// ensure that we're already on the stack
 		XiType our_type = ((AbstractSyntaxTree)id).typecheck(stack);
 		if (!our_type.equals(type))
 			throw new CompilationException("Invalid function declaration: signature doesn't match actual type", position());
 		
		/*
		 * If the block type checks we return a 
		 * XiFuncDecType for this node, otherwise a CompilationException
		 * is thrown.
		 */
 		XiType t = block.typecheck(stack);
 		if (t.equals(XiPrimitiveType.UNIT)){
 			// make sure that type has no return types
 			if (types.size() > 0)
 				throw new CompilationException("Function expects return types of " + types + " but got no returns", position());
 		}
 		
 		try {
 			stack.pop();
 		} catch (InvalidXiTypeException e) {
 			throw new CompilationException(e.getMessage(), position());
 		}
 		return type;
 	}
 }
