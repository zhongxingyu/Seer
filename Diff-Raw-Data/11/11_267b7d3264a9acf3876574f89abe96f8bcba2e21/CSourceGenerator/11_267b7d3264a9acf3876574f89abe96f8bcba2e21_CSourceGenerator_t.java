 //ifdef  JAVA
 import java.util.ArrayList;
 //endif VAJA
 
 //GreenTea Generator should be written in each language.
 
 public class CSourceGenerator extends SourceGenerator {
 
 	CSourceGenerator/*constructor*/() {
 		super("C");
 	}
 
 	public void VisitBlockEachStatementWithIndent(GtNode Node, boolean NeedBlock) {
 		/*local*/String Code = "";
 		if(NeedBlock) {
 			Code += "{\n";
 			this.Indent();
 		}
 		/*local*/GtNode CurrentNode = Node;
 		while(CurrentNode != null) {
 			CurrentNode.Evaluate(this);
 			Code += this.GetIndentString() + this.PopSourceCode() + ";\n";
 			CurrentNode = CurrentNode.NextNode;
 		}
 		if(NeedBlock) {
 			this.UnIndent();
 			Code += this.GetIndentString() + "}";
 		}
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitEmptyNode(GtNode Node) {
 		//this.PushSourceCode("/*empty*/");
 	}
 
 	@Override public void VisitSuffixNode(SuffixNode Node) {
 		/*local*/String MethodName = Node.Token.ParsedText;
 		//if(MethodName.equals("++")) {
 		//}
 		//else if(MethodName.equals("--")) {
 		//}
 		Node.Expr.Evaluate(this);
 		this.PushSourceCode(this.PopSourceCode() + MethodName);
 	}
 
 	@Override public void VisitUnaryNode(UnaryNode Node) {
 		/*local*/String MethodName = Node.Token.ParsedText;
 		//if(MethodName.equals("+")) {
 		//}
 		//else if(MethodName.equals("-")) {
 		//}
 		//else if(MethodName.equals("~")) {
 		//}
 		//else if(MethodName.equals("!")) {
 		//}
 		//else if(MethodName.equals("++")) {
 		//}
 		//else if(MethodName.equals("--")) {
 		//}
 		//else {
 		//	throw new RuntimeException("NotSupportOperator");
 		//}
 		Node.Expr.Evaluate(this);
 		this.PushSourceCode(MethodName + this.PopSourceCode());
 	}
 
 	@Override public void VisitIndexerNode(IndexerNode Node) {
 		Node.Indexer.Evaluate(this);
 		Node.Expr.Evaluate(this);
 		this.PushSourceCode(this.PopSourceCode() + "[" + this.PopSourceCode() + "]");
 	}
 
 	@Override public void VisitMessageNode(MessageNode Node) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override public void VisitWhileNode(WhileNode Node) {
 		Node.CondExpr.Evaluate(this);
 		/*local*/String Program = "while(" + this.PopSourceCode() + ")";
 		this.VisitBlockEachStatementWithIndent(Node.LoopBody, true);
 		Program += this.PopSourceCode();
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitDoWhileNode(DoWhileNode Node) {
 		/*local*/String Program = "do";
 		this.VisitBlockEachStatementWithIndent(Node.LoopBody, true);
 		Node.CondExpr.Evaluate(this);
 		Program += " while(" + this.PopSourceCode() + ")";
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitForNode(ForNode Node) {
 		Node.IterExpr.Evaluate(this);
 		Node.CondExpr.Evaluate(this);
 		/*local*/String Cond = this.PopSourceCode();
 		/*local*/String Iter = this.PopSourceCode();
 		/*local*/String Program = "for(; " + Cond  + "; " + Iter + ")";
 		Node.LoopBody.Evaluate(this);
 		Program += this.PopSourceCode();
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitForEachNode(ForEachNode Node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override public void VisitConstNode(ConstNode Node) {
 		/*local*/String Code = "NULL";
 		if(Node.ConstValue != null) {
 			Code = this.StringfyConstValue(Node.ConstValue);
 		}
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitNewNode(NewNode Node) {
 		/*local*/String Type = Node.Type.ShortClassName;
 		this.PushSourceCode("new " + Type);
 	}
 
 	@Override public void VisitNullNode(NullNode Node) {
 		this.PushSourceCode("NULL");
 	}
 
 	@Override public void VisitLocalNode(LocalNode Node) {
 		this.PushSourceCode(Node.LocalName);
 	}
 
 	@Override public void VisitGetterNode(GetterNode Node) {
 		Node.Expr.Evaluate(this);
 		this.PushSourceCode(this.PopSourceCode() + "->" + Node.Method.MethodName);
 	}
 
 	private String[] EvaluateParam(ArrayList<GtNode> Params) {
 		/*local*/int Size = GtStatic.ListSize(Params);
 		/*local*/String[] Programs = new String[Size];
 		/*local*/int i = 0;
 		while(i < Size) {
 			GtNode Node = Params.get(i);
 			Node.Evaluate(this);
 			Programs[Size - i - 1] = this.PopSourceCode();
 			i = i + 1;
 		}
 		return Programs;
 	}
 
 	@Override public void VisitApplyNode(ApplyNode Node) {
 		/*local*/String[] Params = this.EvaluateParam(Node.Params);
		/*local*/String Program = this.GenerateTemplate(Node);
 		/*local*/int i = 0;
 		while(i < Params.length) {
 			String P = Params[i];
 			Program = Program.replace("$" + i, P);
 			i = i + 1;
 		}
 		this.PushSourceCode(Program);
 	}
 
 	private String GenerateTemplate(ApplyNode Node) {
 		if(Node.Method.SourceMacro != null) {
 			return Node.Method.SourceMacro;
 		}
 		/*local*/String Template = Node.Method.LocalFuncName + "(";
 		/*local*/int i = 0;
 		/*local*/int ParamSize = Node.Params.size();
 		while(i < ParamSize) {
 			if(i != 0) {
 				Template += " ,";
 			}
 			Template += "$" + i;
 			i = i + 1;
 		}
 		Template += ")";
 		return Template;
 	}
 
 	@Override public void VisitBinaryNode(BinaryNode Node) {
 		/*local*/String MethodName = Node.Token.ParsedText;
 		//if(MethodName.equals("+")) {
 		//}
 		//else if(MethodName.equals("-")) {
 		//}
 		//else if(MethodName.equals("*")) {
 		//}
 		//else if(MethodName.equals("/")) {
 		//}
 		//else if(MethodName.equals("%")) {
 		//}
 		//else if(MethodName.equals("<<")) {
 		//}
 		//else if(MethodName.equals(">>")) {
 		//}
 		//else if(MethodName.equals("&")) {
 		//}
 		//else if(MethodName.equals("|")) {
 		//}
 		//else if(MethodName.equals("^")) {
 		//}
 		//else if(MethodName.equals("<=")) {
 		//}
 		//else if(MethodName.equals("<")) {
 		//}
 		//else if(MethodName.equals(">=")) {
 		//}
 		//else if(MethodName.equals(">")) {
 		//}
 		//else if(MethodName.equals("!=")) {
 		//}
 		//else if(MethodName.equals("==")) {
 		//}
 		//else {
 		//	throw new RuntimeException("NotSupportOperator");
 		//}
 		Node.RightNode.Evaluate(this);
 		Node.LeftNode.Evaluate(this);
 		this.PushSourceCode(this.PopSourceCode() + " " + MethodName + " " + this.PopSourceCode());
 	}
 
 	@Override public void VisitAndNode(AndNode Node) {
 		Node.RightNode.Evaluate(this);
 		Node.LeftNode.Evaluate(this);
 		this.PushSourceCode(this.PopSourceCode() + " && " + this.PopSourceCode());
 	}
 
 	@Override public void VisitOrNode(OrNode Node) {
 		Node.RightNode.Evaluate(this);
 		Node.LeftNode.Evaluate(this);
 		this.PushSourceCode(this.PopSourceCode() + " || " + this.PopSourceCode());
 	}
 
 	@Override public void VisitAssignNode(AssignNode Node) {
 		Node.RightNode.Evaluate(this);
 		Node.LeftNode.Evaluate(this);
 		this.PushSourceCode(this.PopSourceCode() + " = " + this.PopSourceCode());
 	}
 
 	@Override public void VisitLetNode(LetNode Node) {
 		/*local*/String Type = Node.DeclType.ShortClassName;
 		Node.VarNode.Evaluate(this);
 		/*local*/String Code = Type + " " + this.PopSourceCode() + ";\n";
 		this.VisitBlockEachStatementWithIndent(Node.BlockNode, true);
 		this.PushSourceCode(Code + this.GetIndentString() + this.PopSourceCode());
 	}
 
 	@Override public void VisitIfNode(IfNode Node) {
 		Node.CondExpr.Evaluate(this);
 		this.VisitBlockEachStatementWithIndent(Node.ThenNode, true);
 		this.VisitBlockEachStatementWithIndent(Node.ElseNode, true);
 		/*local*/String ElseBlock = this.PopSourceCode();
 		/*local*/String ThenBlock = this.PopSourceCode();
 		/*local*/String CondExpr = this.PopSourceCode();
 		/*local*/String Code = "if(" + CondExpr + ") " + ThenBlock;
 		if(Node.ElseNode != null) {
 			Code += " else " + ElseBlock;
 		}
 		this.PushSourceCode(Code);
 
 	}
 
 	@Override public void VisitSwitchNode(SwitchNode Node) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override public void VisitReturnNode(ReturnNode Node) {
 		/*local*/String Code = "return";
 		if(Node.Expr != null) {
 			Node.Expr.Evaluate(this);
 			Code += " " + this.PopSourceCode();
 		}
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitLabelNode(LabelNode Node) {
 		/*local*/String Label = Node.Label;
 		this.PushSourceCode(Label + ":");
 	}
 
 	@Override public void VisitJumpNode(JumpNode Node) {
 		/*local*/String Label = Node.Label;
 		this.PushSourceCode("goto " + Label);
 	}
 
 	@Override public void VisitBreakNode(BreakNode Node) {
 		/*local*/String Code = "break";
 		/*local*/String Label = Node.Label;
 		if(Label != null) {
 			Code += " " + Label;
 		}
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitContinueNode(ContinueNode Node) {
 		/*local*/String Code = "continue";
 		/*local*/String Label = Node.Label;
 		if(Label != null) {
 			Code += " " + Label;
 		}
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitTryNode(TryNode Node) {
 		/*local*/String Code = "try";
 		//this.VisitEach(Node.CatchBlock);
 		this.VisitBlockEachStatementWithIndent(Node.TryBlock, true);
 		Code += this.PopSourceCode();
 		if(Node.FinallyBlock != null) {
 			this.VisitBlockEachStatementWithIndent(Node.FinallyBlock, true);
 			Code += " finally " + this.PopSourceCode();
 		}
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitThrowNode(ThrowNode Node) {
 		Node.Expr.Evaluate(this);
 		/*local*/String Code = "throw " + this.PopSourceCode();
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitFunctionNode(FunctionNode Node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override public void VisitErrorNode(ErrorNode Node) {
 		/*local*/String Code = "throw Error(\"" + Node.Token.ParsedText + "\")";
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitCommandNode(CommandNode Node) {
 		/*local*/String Code = "system(";
 		/*local*/int i = 0;
 		/*local*/String Command = "String __Command = ";
 		while(i < GtStatic.ListSize(Node.Params)) {
 			GtNode Param = Node.Params.get(i);
 			if(i != 0) {
 				Command += " + ";
 			}
 			Param.Evaluate(this);
 			Command += "(" + this.PopSourceCode() + ")";
 			i = i + 1;
 		}
 		Code = Command + ";\n" + this.GetIndentString() + Code + "__Command)";
 		this.PushSourceCode(Code);
 	}
 
 	public String LocalTypeName(GtType Type) {
 		return Type.ShortClassName;
 	}
 
 	@Override public void DefineFunction(GtMethod Method, ArrayList<String> ParamNameList, GtNode Body) {
 		/*local*/String Code = "";
 		if(!Method.Is(ExportMethod)) {
 			Code = "static ";
 		}
 		/*local*/String RetTy = this.LocalTypeName(Method.GetReturnType());
 		Code += RetTy + " " + Method.LocalFuncName + "(";
 		/*local*/int i = 0;
 		while(i < ParamNameList.size()) {
 			String ParamTy = this.LocalTypeName(Method.GetParamType(i));
 			if(i > 0) {
 				Code += ", ";
 			}
 			Code += ParamTy + " " + ParamNameList.get(i);
 			i = i + 1;
 		}
 		Code += ")";
 		this.VisitBlockEachStatementWithIndent(Body, true);
 		Code += this.PopSourceCode();
 		this.WriteTranslatedCode(Code);
 	}
 
 	@Override public Object Eval(GtNode Node) {
 		this.VisitBlockEachStatementWithIndent(Node, false);
 		/*local*/String Code = this.PopSourceCode();
 		if(Code.equals(";\n")) {
 			return "";
 		}
 		this.WriteTranslatedCode(Code);
 		return Code;
 	}
 
 	@Override public void AddClass(GtType Type) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override public void SetLanguageContext(GtContext Context) {
 	}
 }
