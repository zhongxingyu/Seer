 package ast;
 
 public class VisitorAdapter implements Visitor{
 	
     
 	@Override
 	public Object visit(AccessorNode n) {
 		for (String id : n.ids) {
 			System.out.print(id);
 		}
 		// TODO Auto-generated method stub
 		return null;
 	}
     
     
 	@Override
 	public Object visit(AssignNode n) {
 		n.value.accept(this);
 		System.out.print("=");
 		n.var.accept(this);
 		System.out.println(";");
 
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(BegColNode n) {
 		n.value1.accept(this);
 		System.out.print("[:");
 		n.value2.accept(this);
 		System.out.print("]");
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(BinaryTermNode n) {
 		
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(BlockNode n) {
 		
 		if (n.declarations!=null){
 			n.declarations.accept(this);
 		}
 		if (n.statements!=null){
 			System.out.print("{");
 		n.statements.accept(this);
 		System.out.print("}");
 		}
 		
 
 
 
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(BoolNode n) {
 		n.accept(this);
 		System.out.print(n.value);
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(CharNode n) {
 		n.accept(this);
 		System.out.print(n.value);
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(ConcatNode n) {
 		
 		n.value.accept(this);
 		System.out.print("::");
 		
 		n.term.accept(this);
 		System.out.println(";");
 
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(DatatypeNode n) {
 		System.out.print(n.id);
 		System.out.print(":");
 		n.params.accept(this);
 		System.out.println(";");
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(DeclListNode n) {
 		for(DeclNode d : n.declarations){
 			d.accept(this);
 		}
 		
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	@Override
 	public Object visit(ListNode n) {
 		System.out.print("[");
 		n.values.accept(this);
 		System.out.print("]");
 		
 		return null;
 
 		
 	}
     
 	@Override
 	public Object visit(DeclNode n) {
 		
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(DivideNode n) {
 		n.term.accept(this);
 		System.out.print(" / ");
 		n.factor.accept(this);
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(DualExprNode n) {
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(EndColNode n) {
 		n.value1.accept(this);
 		System.out.print("[");
 		n.value2.accept(this);
 		System.out.print(":]");
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(EqNode n) {
 		n.value.accept(this);
 		System.out.print(" == ");
 		n.term.accept(this);
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(ExponentNode n) {
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(ExprListNode n) {
 		for(ExprNode a : n.expressions){System.out.println("ssssize!"+n.expressions.size());
 			a.accept(this);
 		}
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(ExprNode n) {
 		
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(ExprTermNode n) {
 
 		n.term.accept(this);
 		n.value.accept(this);
 
 		//n.term.accept(this);
 		//n.value.accept(this);
 		n.term.accept(this);
 		n.value.accept(this);
 	
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(FactorNode n) {
 		
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(FinalNode n) {
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(FloatNode n) {
 		System.out.print(n.value);
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(FuncCallNode n) {
 		System.out.print(n.id);
 		System.out.print("(");
 		n.params.accept(this);
 		System.out.print(")");
 
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(FuncCallStmtNode n) {
 		n.function.accept(this);
 		System.out.print(";");
 
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(FuncDeclNode n) {
 		System.out.print("fdef ");
 		System.out.print(n.id);
 		System.out.print("(");
 		n.params.accept(this);
 		System.out.print(")");
 		System.out.print(" : ");
 		n.type.accept(this);
 		n.block.accept(this);
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(GreatThEqNode n) {
 		n.value.accept(this);
 		System.out.print(" >= ");
 		n.term.accept(this);
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(GreatThNode n) {
 		n.value.accept(this);
 		System.out.print(" > ");
 		n.term.accept(this);
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(IfElseNode n) {
 		System.out.print("if ");
 		System.out.print("(");
 		n.cond.accept(this);
 		System.out.print(")");
 		System.out.println("{");
 		n.body.accept(this);
 		System.out.print("}");
 		System.out.print(" else ");
 		System.out.println("{");
 		n.elsebody.accept(this);
 		System.out.println("}");
 
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(InNode n) {
 		n.value.accept(this);
 		System.out.print(" in ");
 		n.term.accept(this);
 
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(IntNode n) {
 		System.out.print(n.value);
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(LengthNode n) {
 		System.out.print("len ");
 		System.out.print("(");
 		n.value.accept(this);
 		System.out.print(")");
 
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(LessThEqNode n) {
 		n.value.accept(this);
 		System.out.print(" <= ");
 		n.term.accept(this);
 		
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(LessThNode n) {
 		n.value.accept(this);
 		System.out.print(" < ");
 		n.term.accept(this);
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(MinExponentNode n) {
 		System.out.print(" - ");
 		n.exponent.accept(this);
 
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(MinusNode n) {
 		n.value.accept(this);
 		System.out.print(" - ");
 		n.term.accept(this);
 
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(MultiplyNode n) {
 		n.term.accept(this);
 		System.out.print(" * ");
 		n.factor.accept(this);
 		return null;
 	}
     
 	@Override
 	public Object visit(NoColNode n) {
 		n.value1.accept(this);
 		System.out.print("(");
 		n.value2.accept(this);
 		System.out.print(")");
 
 
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(Node n) {
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(NotEqNode n) {
 		n.value.accept(this);
 		System.out.print(" != ");
 		n.term.accept(this);
 
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(NotExprNode n) {
 		System.out.print(" ! ");
 		n.value.accept(this);
 
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(OrNode n) {
 		n.value.accept(this);
 		System.out.print(" || ");
 		n.term.accept(this);
 
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(ParamListNode n) {
 		for(VarTypeNode a : n.params)
 		{
 			a.accept(this);
 		}
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(PlusNode n) {
 		n.value.accept(this);
 		System.out.print(" + ");
 		n.term.accept(this);
 		return null;
 	}
     
 	@Override
 	public Object visit(PowerNode n) {
 		n.exponent.accept(this);
 		System.out.print("^");
 		n.factor.accept(this);
 		return null;
 	}
     
 	@Override
 	public Object visit(PowerTermNode n) {
 		n.term.accept(this);
 		System.out.print("^");
 		n.factor.accept(this);
 		return null;
 	}
     
 	@Override
 	public Object visit(ProgramNode n) {
 		if (n==null) { System.out.println("Null programnode!");}
 		n.declarations.accept(this);
 		n.block.accept(this);
 		return null;
 	}
     
 	@Override
 	public Object visit(ReturnStmtNode n) {
 		System.out.print("return ");
 		n.value.accept(this);
 		System.out.println(" ; ");
 
 
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(RptUntilNode n) {
 		System.out.print("repeat ");
 		System.out.print("{");
 		n.body.accept(this);
 		System.out.print("}");
 		System.out.print(" until ");
 		System.out.print("(");
 		n.cond.accept(this);
 		System.out.print(")");
 		System.out.print(";");
 
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(SequenceNode n) {
 		
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(StatementListNode n) {
 		for(StmtNode a : n.statements)
 		{
 			a.accept(this);
 		}
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(StmtNode n) {
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(StringLitNode n) {
 		System.out.print(n.value);
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(TermNode n) {
 		
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(ThreeExprNode n) {
 		n.value1.accept(this);
 		System.out.print("(");
 		n.value2.accept(this);
 		System.out.print(" : ");
 		n.value3.accept(this);
 		System.out.print(")");
 
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(TupleNode n) {
 		System.out.print("[");
 		n.value.accept(this);
 		System.out.print("]");
 
 
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(TypeNode n) {
 		System.out.print(n.type);
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(VarDeclNode n) {
 		n.id.accept(this);
 		System.out.print(" = ");
 		n.declarations.accept(this);
 		
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(WhileStmtNode n) {
 		System.out.print(" while ");
 		System.out.print("(");
 		n.body.accept(this);
 		System.out.print(")");
 		System.out.print(" do ");
 		System.out.println(" { ");
 		n.cond.accept(this);
 		System.out.print("}");
 
 		// TODO Auto-generated method stub
 		return null;
 	}
     
 	@Override
 	public Object visit(AndNode n) {
 		n.term.accept(this);
 		System.out.print(" && ");
 		n.factor.accept(this);
 
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	@Override
 	public Object visit(VarTypeNode n) {
 		System.out.print(n.id);
 		System.out.print(" : ");
 		n.type.accept(this);
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	@Override
 	public Object visit(BrkExprNode n) {
 		System.out.print("(");
 		n.value.accept(this);
 		System.out.print(")");
 
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
     
 }
