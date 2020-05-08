 package uk.ac.ucl.comp2010.bestgroup;
 import java.util.List;
 import java.util.ListIterator;
 
 import com.sun.org.apache.xpath.internal.operations.Bool;
 
 import uk.ac.ucl.comp2010.bestgroup.AST.*;
 
 public abstract class Visitor {
 	
 	public void visit(Object... args) {
 		for(Object n : args) {
 			visit(n);
 		}
 	}
 
 	public Object visit(AccessorNode node) {
 		visitList(node.path);
 		return null;
 	}
 
 	public Object visit(AssignmentNode node) {
 		visit(node.var, node.value);
 		return null;
 	}
 
 	public Object visit(BlockNode node) {
 		visitList(node.declarations);
 		visitList(node.statements);
 		return null;
 	}
 
 	public Object visit(BooleanOperationNode node) {
 		visit(node.left, node.right);
 		return null;
 	}
 
 	public Object visit(BoolNode node) {
 		return null;
 	}
 
 	public Object visit(CharNode node) {
 		return null;
 	}
 
 	public Object visit(ComparisonNode node) {
 		visit(node.left, node.right);
 		return null;
 	}
 
 	public Object visit(ConcatNode node) {
 		visit(node.left, node.right);
 		return null;
 	}
 
 	public Object visit(DatatypeDeclNode node) {
 		visit(node.fields);
 		return null;
 	}
 
 	public Object visit(EqualsNode node) {
 		visit(node.left, node.right);
 		return null;
 	}
 
 	public Object visit(FloatNode node) {
 		return null;
 	}
 
 	public Object visit(FuncCallExprNode node) {
 		visit(node.args);
 		return null;
 	}
 
 	public Object visit(FuncCallStmtNode node) {
 		visit(node.call);
 		return null;
 	}
 
 	public Object visit(FuncDeclNode node) {
 		visit(node.args, node.body);
 		return null;
 	}
 
 	public Object visit(IfNode node) {
 		visit(node.condition, node.true_block);
 		if(node.false_block != null)
 			visit(node.false_block);		
 		return null;
 	}
 
 	public Object visit(IndexNode node) {
 		visit(node.var, node.index);
 		return null;
 	}
 
 	public Object visit(IndexRangeNode node) {
 		visit(node.var);
 		if(node.from != null)
 			visit(node.from);
 		if(node.to != null)
 			visit(node.to);
 		return null;
 	}
 
 	public Object visit(InNode node) {
 		visit(node.left, node.right);
 		return null;
 	}
 
 	public Object visit(IntNode node) {
 		return null;
 	}
 
 	public Object visit(LengthNode node) {
 		visit(node.sequence);
 		return null;
 	}
 
 	public Object visit(NegativeNode node) {
 		visit(node.expr);
 		return null;
 	}
 
 	public Object visit(NotNode node) {
 		visit(node.expr);
 		return null;
 	}
 
 	public Object visit(NumericOperationNode node) {
 		visit(node.left, node.right);
 		return null;
 	}
 
 	public Object visit(ProgramNode node) {
 		visitList(node.declarations);
 		visit(node.main);
 		return null;
 	}
 
 	public Object visit(RepeatNode node) {
 		visit(node.loop, node.condition);
 		return null;
 	}
 
 	public Object visit(ReturnNode node) {
 		if(node.expr != null) {
 			visit(node.expr);
 		}
 		return null;
 	}
 
 	public Object visit(SequenceNode node) {
 		visitList(node.elements);
 		return null;
 	}
 
 	public Object visit(StringNode node) {
 		return null;
 	}
 	
 	public Object visit(VarDeclNode node) {
 		visit(node.var);
 		if(node.value != null){
 			visit(node.value);
 		}
 		return null;
 	}
 
 	public Object visit(VarTypeNode node) {
 		visit(node.id + ":" + node.type);
 		return null;
 	}
 
 	public Object visit(WhileNode node) {
 		visit(node.condition, node.loop);
 		return null;
 	}
 	
 	
 	
 	// --------------
 
 	
 	
 	public Object visitList(List l) {
 		ListIterator li = l.listIterator();
 		while(li.hasNext()) {
 			Object n = li.next();
 			visit(n);
 		}
 		return null;
 	}
 	
 
 	public Object visit(Object node) {
		//System.out.println(node.toString());
 		if(node instanceof AccessorNode) {
 			return visit((AccessorNode)node);
 		} else if(node instanceof AssignmentNode) {
 			return visit((AssignmentNode)node);
 		} else if(node instanceof BlockNode) {
 			return visit((BlockNode)node);
 		} else if(node instanceof BooleanOperationNode) {
 			return visit((BooleanOperationNode)node);
 		} else if(node instanceof BoolNode) {
 			return visit((BoolNode)node);
 		} else if(node instanceof CharNode) {
 			return visit((CharNode)node);
 		} else if(node instanceof ComparisonNode) {
 			return visit((ComparisonNode)node);
 		} else if(node instanceof ConcatNode) {
 			return visit((ConcatNode)node);
 		} else if(node instanceof DatatypeDeclNode) {
			//System.out.println("Yo, I am here" + node.toString());
 			return visit((DatatypeDeclNode)node);
 		} else if(node instanceof EqualsNode) {
 			return 	visit((EqualsNode)node);
 		} else if(node instanceof FloatNode) {
 			return visit((FloatNode)node);
 		} else if(node instanceof FuncCallExprNode) {
 			return visit((FuncCallExprNode)node);
 		} else if(node instanceof FuncCallStmtNode) {
 			return visit((FuncCallStmtNode)node);
 		} else if(node instanceof FuncDeclNode) {
 			return visit((FuncDeclNode)node);
 		} else if(node instanceof IfNode) {
 			return visit((IfNode)node);
 		} else if(node instanceof IndexNode) {
 			return 	visit((IndexNode)node);
 		} else if(node instanceof IndexRangeNode) {
 			return visit((IndexRangeNode)node);
 		} else if(node instanceof InNode) {
 			return visit((InNode)node);
 		} else if(node instanceof IntNode) {
 			return visit((IntNode)node);
 		} else if(node instanceof LengthNode) {
 			return visit((LengthNode)node);
 		} else if(node instanceof NegativeNode) {
 			return visit((NegativeNode)node);
 		} else if(node instanceof NotNode) {
 			return 	visit((NotNode)node);
 		} else if(node instanceof NumericOperationNode) {
 			return 	visit((NumericOperationNode)node);
 		} else if(node instanceof ProgramNode) {
 			return 	visit((ProgramNode)node);
 		} else if(node instanceof RepeatNode) {
 			return visit((RepeatNode)node);
 		} else if(node instanceof ReturnNode) {
 			return visit((ReturnNode)node);
 		} else if(node instanceof StringNode) {
 			return visit((StringNode)node);
 		} else if(node instanceof SequenceNode) {
 			return visit((SequenceNode)node);
 		} else if(node instanceof VarDeclNode) {
 			return visit((VarDeclNode)node);
 		} else if(node instanceof VarTypeNode) {
 			return visit((VarTypeNode)node);
 		} else if(node instanceof WhileNode) {
 			return visit((WhileNode)node);
 		} else if(node instanceof Node){
 			System.out.print("<<" + node.getClass().getSimpleName() + ":" + node + ">>");
 			return null;
 		}
 		return null;
 	}
 
 }
