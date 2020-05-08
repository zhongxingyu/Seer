 package cs4120.der34dlc287lg342.xi.ast;
 
 import java.util.ArrayList;
 
 import cs4120.der34dlc287lg342.xi.ir.Binop;
 import cs4120.der34dlc287lg342.xi.ir.Call;
 import cs4120.der34dlc287lg342.xi.ir.Const;
 import cs4120.der34dlc287lg342.xi.ir.Expr;
 import cs4120.der34dlc287lg342.xi.ir.Jump;
 import cs4120.der34dlc287lg342.xi.ir.Mem;
 import cs4120.der34dlc287lg342.xi.ir.Move;
 import cs4120.der34dlc287lg342.xi.ir.Name;
 import cs4120.der34dlc287lg342.xi.ir.Return_DEPRECATED;
 import cs4120.der34dlc287lg342.xi.ir.Seq;
 import cs4120.der34dlc287lg342.xi.ir.Temp;
 import cs4120.der34dlc287lg342.xi.ir.context.IRContextStack;
 import cs4120.der34dlc287lg342.xi.ir.context.InvalidIRContextException;
 import cs4120.der34dlc287lg342.xi.ir.context.Label;
 import cs4120.der34dlc287lg342.xi.ir.context.TempRegister;
 import cs4120.der34dlc287lg342.xi.ir.translate.IRTranslation;
 import cs4120.der34dlc287lg342.xi.ir.translate.IRTranslationStmt;
 import cs4120.der34dlc287lg342.xi.typechecker.ContextList;
 import cs4120.der34dlc287lg342.xi.typechecker.XiFunctionType;
 import cs4120.der34dlc287lg342.xi.typechecker.XiPrimitiveType;
 import cs4120.der34dlc287lg342.xi.typechecker.XiType;
 import cs4120.der34dlc287lg342.xi.typechecker.InvalidXiTypeException;
 
 import edu.cornell.cs.cs4120.util.VisualizableTreeNode;
 import edu.cornell.cs.cs4120.xi.AbstractSyntaxNode;
 import edu.cornell.cs.cs4120.xi.CompilationException;
 import edu.cornell.cs.cs4120.xi.Position;
 
 public class ReturnNode extends AbstractSyntaxTree {
 
 	public Position position;
 	protected ArrayList<VisualizableTreeNode> children = new ArrayList<VisualizableTreeNode>();
 	
 	public void add(AbstractSyntaxNode expr){
 		children.add(expr);
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
 		return "RETURN";
 	}
 	
 	@Override
 	public XiType typecheck(ContextList stack) throws CompilationException {
 		XiFunctionType function;
 		try {
 			function = stack.find_return();
 		}catch (InvalidXiTypeException ex) {
 			throw new CompilationException(ex.getMessage(),position);
 		}
 		
 		if (function != null) {
 			if (function.ret.size() == children.size())
 			{
 				for (int index = 0; index < function.ret.size(); index++) {
 					XiType t = ((AbstractSyntaxTree)children.get(index)).typecheck(stack);
 					if (!t.equals(function.ret.get(index)))
 						throw new CompilationException("Invalid return type("+(index+1)+"): expected ["+function.ret.get(index)+"] but got ["+t+"] instead", 
 								((AbstractSyntaxTree)children.get(index)).position());
 				}
 				
 				type = XiPrimitiveType.VOID;
 				return type;
 			} else {
 				throw new CompilationException("Invalid number of return types: expected ["+function.ret.size()+"] but got ["+children.size()+"] instead", position);
 			}
 		}
 		throw new CompilationException("Invalid return type", position);
 	}
 	
 	@Override
 	public AbstractSyntaxTree foldConstants(){
 		int i = 0;
 		for (VisualizableTreeNode c : children()){
 			AbstractSyntaxTree t = ((AbstractSyntaxTree)c).foldConstants();
 			resolve_const(i++,t,null);
 		}
 		return null;
 	}
 	
 	@Override
 	public IRTranslation to_ir(IRContextStack stack) throws InvalidIRContextException{
 		/*
 		 * only do primitive types right now
 		 * Move(RV, expr)
 		 * Return
 		 */
 		
 		if (children.isEmpty())
 			return new IRTranslationStmt(new Jump(stack.return_to()));
 		
 		Seq seq = new Seq();
 		
 		if (children.size() == 1){
 			AbstractSyntaxTree e = (AbstractSyntaxTree) children.get(0);
 			IRTranslation tr = e.to_ir(stack);
 			seq = new Seq(new Move(new Temp(TempRegister.RV), tr.expr()));
 		} else if (children.size() > 1){
			Expr return_register = stack.add_register("_tuple");
 			// allocate heap here
 			//seq.add(new Move(return_register, new Call(new Name(Label.alloc), new Const(8*children.size()))));
 			for (int i = 0; i < children.size();i++){
 				AbstractSyntaxTree e = (AbstractSyntaxTree) children.get(i);
 				IRTranslation tr = e.to_ir(stack);
 				
 				// put it on the heap
 				seq.add(new Move(new Mem(new Binop(Binop.PLUS, return_register, new Const(8*i))), tr.expr()));
 			}
 		}
 		
 		seq.add(new Jump(stack.return_to()));
 		return new IRTranslationStmt(seq);
 	}
 }
