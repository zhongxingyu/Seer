 import java.util.ArrayList;
 
 //GreenTea Generator should be written in each language.
 
 public class PerlSourceGenerator extends GreenTeaGenerator {
 	IndentGenerator Indent;
	PerlSourceGenerator() {
 		super("Perl");
 		this.Indent = new IndentGenerator();
 	}
 
 	public void VisitEach(TypedNode Node) {
 		String Code = "{\n";
 		this.Indent.AddIndent(1);
 		/*local*/TypedNode CurrentNode = Node;
 		while(CurrentNode != null) {
 			CurrentNode.Evaluate(this);
 			Code += this.Indent.Get() + this.PopCode() + ";\n";
 			CurrentNode = CurrentNode.NextNode;
 		}
 		this.Indent.AddIndent(-1);
 		Code += this.Indent.Get() + "}";
 		this.PushCode(Code);
 	}
 
 	public void VisitEmptyNode(TypedNode Node) {	
 	}
 
 	public void VisitSuffixNode(SuffixNode Node) {
 		GtMethod Method = Node.Method;
 		if(Method.MethodName.equals("++")) {
 		} else if(Method.MethodName.equals("--")) {
 		} else {
 			throw new RuntimeException("NotSupportOperator");
 		}
 		Node.Expr.Evaluate(this);
 		this.PushCode(this.PopCode() + Method.MethodName);
 	}
 
 	public void VisitUnaryNode(UnaryNode Node) {
 		GtMethod Method = Node.Method;
 		if(Method.MethodName.equals("+")) {
 		} else if(Method.MethodName.equals("-")) {
 		} else if(Method.MethodName.equals("~")) {
 		} else if(Method.MethodName.equals("!")) {
 		} else if(Method.MethodName.equals("++")) {
 		} else if(Method.MethodName.equals("--")) {
 		} else {
 			throw new RuntimeException("NotSupportOperator");
 		}
 		Node.Expr.Evaluate(this);
 		this.PushCode(Method.MethodName + this.PopCode());
 	}
 
 	public void VisitIndexerNode(IndexerNode Node) {
 		Node.Indexer.Evaluate(this);
 		Node.Expr.Evaluate(this);
 		this.PushCode(this.PopCode() + "[" + this.PopCode() + "]");
 		
 	}
 
 	public void VisitMessageNode(MessageNode Node) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void VisitWhileNode(WhileNode Node) {
 		Node.CondExpr.Evaluate(this);
 		String Program = "while(" + this.PopCode() + ")";
 		this.VisitEach(Node.LoopBody);
 		Program += this.PopCode();
 		Indent.AddIndent(-1);
 		this.PushCode(Program);
 	}
 
 	public void VisitDoWhileNode(DoWhileNode Node) {		
 		String Program = "do";
 		this.VisitEach(Node.LoopBody);
 		Node.CondExpr.Evaluate(this);
 		Program += " while(" + this.PopCode() + ")";
 		this.PushCode(Program);
 	}
 
 	public void VisitForNode(ForNode Node) {
 		Node.IterExpr.Evaluate(this);
 		Node.CondExpr.Evaluate(this);
 		Node.InitNode.Evaluate(this);
 		String Init = this.PopCode();
 		String Cond = this.PopCode();
 		String Iter = this.PopCode();
 		
 		String Program = "for(" + Init + "; " + Cond  + "; " + Iter + ")";
 		Node.LoopBody.Evaluate(this);
 		Program += this.PopCode();
 		this.PushCode(Program);		
 	}
 
 	public void VisitForEachNode(ForEachNode Node) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void VisitConstNode(ConstNode Node) {
 		this.PushCode(Node.ConstValue.toString());
 	}
 
 	public void VisitNewNode(NewNode Node) {
 		String Type = Node.Type.ShortClassName;
 		this.PushCode("new " + Type);
 
 	}
 
 	public void VisitNullNode(NullNode Node) {
 		this.PushCode("NULL");
 	}
 
 	public void VisitLocalNode(LocalNode Node) {
 		this.PushCode("$" + Node.LocalName);
 	}
 
 	public void VisitGetterNode(GetterNode Node) {
 		Node.Expr.Evaluate(this);
 		this.PushCode(this.PopCode() + "->" + Node.Method.MethodName);
 	}
 
 	private String[] EvaluateParam(ArrayList<TypedNode> Params) {
 		int Size = Params.size();
 		String[] Programs = new String[Size];
 		for(int i = 0; i < Size; i++) {
 			TypedNode Node = Params.get(i);
 			Node.Evaluate(this);
 			Programs[Size - i - 1] = this.PopCode();
 		}
 		return Programs;
 	}
 
 	public void VisitApplyNode(ApplyNode Node) {
 		/*local*/String Program = "&" + Node.Method.MethodName + "(";
 		/*local*/String[] Params = EvaluateParam(Node.Params);
 		for(int i = 0; i < Params.length; i++) {
 			String P = Params[i];
 			if(i != 0) {
 				Program += ",";
 			}
 			Program += P;
 		}
 		Program += ")";
 		this.PushCode(Program);
 	}
 
 	public void VisitBinaryNode(BinaryNode Node) {
 		String MethodName = Node.Method.MethodName;
 		if(MethodName.equals("+")) {
 		} else if(MethodName.equals("-")) {
 		} else if(MethodName.equals("*")) {
 		} else if(MethodName.equals("/")) {
 		} else if(MethodName.equals("%")) {
 		} else if(MethodName.equals("<<")) {
 		} else if(MethodName.equals(">>")) {
 		} else if(MethodName.equals("&")) {
 		} else if(MethodName.equals("|")) {
 		} else if(MethodName.equals("^")) {
 		} else if(MethodName.equals("<=")) {
 		} else if(MethodName.equals("<")) {
 		} else if(MethodName.equals(">=")) {
 		} else if(MethodName.equals(">")) {
 		} else if(MethodName.equals("!=")) {
 			if(Node.Method.GetRecvType().ShortClassName.equals("String")) {
 				MethodName = "ne";
 			}
 		} else if(MethodName.equals("==")) {
 			if(Node.Method.GetRecvType().ShortClassName.equals("String")) {
 				MethodName = "eq";
 			}
 		} else {
 			throw new RuntimeException("NotSupportOperator");
 		}
 		Node.RightNode.Evaluate(this);
 		Node.LeftNode.Evaluate(this);
 		this.PushCode(this.PopCode() + " " + MethodName + " " + this.PopCode());
 	}
 
 	public void VisitAndNode(AndNode Node) {
 		Node.RightNode.Evaluate(this);
 		Node.LeftNode.Evaluate(this);
 		this.PushCode(this.PopCode() + " && " + this.PopCode());
 	}
 
 	public void VisitOrNode(OrNode Node) {
 		Node.RightNode.Evaluate(this);
 		Node.LeftNode.Evaluate(this);
 		this.PushCode(this.PopCode() + " || " + this.PopCode());
 	}
 
 	public void VisitAssignNode(AssignNode Node) {
 		Node.RightNode.Evaluate(this);
 		Node.LeftNode.Evaluate(this);
 		this.PushCode(this.PopCode() + " = " + this.PopCode());
 	}
 
 	public void VisitLetNode(LetNode Node) {
 		String Type = Node.DeclType.ShortClassName;
 		Node.VarNode.Evaluate(this);
 		String Code = Type + " " + this.PopCode();
 		Node.BlockNode.Evaluate(this);
 		this.PushCode(Code + this.PopCode());
 	}
 
 	public void VisitIfNode(IfNode Node) {
 		Node.CondExpr.Evaluate(this);
 		this.VisitEach(Node.ThenNode);
 		this.VisitEach(Node.ElseNode);
 
 		String ElseBlock = this.PopCode();
 		String ThenBlock = this.PopCode();
 		String CondExpr = this.PopCode();
 		String Code = "if(" + CondExpr + ") " + ThenBlock;
 		if(Node.ElseNode != null) {
 			Code += " else " + ElseBlock;
 		}
 		this.PushCode(Code);
 		
 	}
 
 	public void VisitSwitchNode(SwitchNode Node) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void VisitLoopNode(LoopNode Node) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void VisitReturnNode(ReturnNode Node) {
 		String Code = "return";
 		if(Node.Expr != null) {
 			Node.Expr.Evaluate(this);
 			Code += " " + this.PopCode();
 		}
 		this.PushCode(Code);
 	}
 
 	public void VisitLabelNode(LabelNode Node) {
 		String Label = Node.Label;
 		this.PushCode(Label + ":");
 	}
 
 	public void VisitJumpNode(JumpNode Node) {
 		String Label = Node.Label;
 		this.PushCode("goto " + Label);
 	}
 
 	public void VisitBreakNode(BreakNode Node) {
 		String Code = "break";
 		String Label = Node.Label;
 		if(Label != null) {
 			Code += " " + Label;
 		}
 		this.PushCode(Code);
 	}
 
 	public void VisitContinueNode(ContinueNode Node) {
 		String Code = "continue";
 		String Label = Node.Label;
 		if(Label != null) {
 			Code += " " + Label;
 		}
 		this.PushCode(Code);
 	}
 
 	public void VisitTryNode(TryNode Node) {
 		String Code = "try";
 		//this.VisitEach(Node.CatchBlock);
 		this.VisitEach(Node.TryBlock);
 		
 		Code += this.PopCode();
 		if(Node.FinallyBlock != null) {
 			this.VisitEach(Node.FinallyBlock);
 			Code += " finally " + this.PopCode();
 		}
 		this.PushCode(Code);
 	}
 
 	public void VisitThrowNode(ThrowNode Node) {
 		Node.Expr.Evaluate(this);
 		String Code = "throw " + this.PopCode();
 		this.PushCode(Code);
 	}
 
 	public void VisitFunctionNode(FunctionNode Node) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void VisitErrorNode(ErrorNode Node) {
 		String Code = "throw Error(\"" + Node.Token.ParsedText + "\")";
 		this.PushCode(Code);
 		
 	}
 
 	public void DefineFunction(GtMethod Method, ArrayList<String> ParamNameList, TypedNode Body) {
 		String Program = "";
 		String RetTy = Method.GetReturnType().ShortClassName;
 		String ThisTy = Method.GetRecvType().ShortClassName;
 		String FuncName = ThisTy + "_" + Method.MethodName;
 		String Signature = "# ";
 		String Arguments = "";
 		Signature += RetTy + " " + FuncName + "(";
 		Signature += ThisTy + " this";
 		Indent.AddIndent(1);
 
 		Arguments += Indent.Get() + "my $this = $_[0]\n";
 		for(int i = 0; i < ParamNameList.size(); i++) {
 			String ParamTy = Method.GetParamType(i).ShortClassName;
 			Signature += " ," + ParamTy + " " + ParamNameList.get(i);
 			Arguments += Indent.Get() + "my $" + ParamNameList.get(i) + " = $_[" + (i + 1) + "]\n";
 		}
 		Indent.AddIndent(-1);
 		Program += Signature + "\n" + Indent.Get() + "sub " + FuncName + "{\n";
 		Indent.AddIndent(1);
 		Program += Arguments;
 		Indent.AddIndent(1);
 		Program += Eval(Body);
 		Indent.AddIndent(-1);
 		Indent.AddIndent(-1);
 		Program += Program + "\n" + Indent.Get() + "}";
 		DebugP(Program);
 	}
 
 	public Object Eval(TypedNode Node) {
 		this.VisitEach(Node);
 		return this.PopCode();
 	}
 
 	public void AddClass(GtType Type) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void LoadContext(GtContext Context) {
 		// TODO Auto-generated method stub
 		
 	}
 }
 
