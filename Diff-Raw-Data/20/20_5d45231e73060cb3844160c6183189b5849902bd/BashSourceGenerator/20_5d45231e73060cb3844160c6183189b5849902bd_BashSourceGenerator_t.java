 import java.util.ArrayList;
 
 //GreenTea Generator should be written in each language.
 
 public class BashSourceGenerator extends SourceGenerator {
 	/*local*/private boolean inFun = false;
 
 	BashSourceGenerator() {
 		super("BashSource");
 	}
 
 	public void VisitEach(TypedNode Node) {
 		/*local*/String Code = "\n";
 		this.Indent();
 		/*local*/TypedNode CurrentNode = Node;
 		while(CurrentNode != null) {
 			CurrentNode.Evaluate(this);
 			Code += this.GetIndentString() + this.PopSourceCode() + "\n";
 			CurrentNode = CurrentNode.NextNode;
 		}
 		this.UnIndent();
 		Code += this.GetIndentString();
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitEmptyNode(TypedNode Node) {
 	}
 
 	@Override public void VisitIndexerNode(IndexerNode Node) {
 		Node.Indexer.Evaluate(this);
 		Node.Expr.Evaluate(this);
 		this.PushSourceCode("${" + this.PopSourceCode() + "[" + this.PopSourceCode() + "]}");
 	}
 
 	@Override public void VisitMessageNode(MessageNode Node) {
 		// do nothing
 	}
 
 	@Override public void VisitWhileNode(WhileNode Node) {
 		Node.CondExpr.Evaluate(this);
 		/*local*/String Program = "while(( " + this.PopSourceCode() + " ))\ndo";
 		this.VisitEach(Node.LoopBody);
 		Program += this.PopSourceCode() + "done\n";
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitDoWhileNode(DoWhileNode Node) {
 	}
 
 	@Override public void VisitForNode(ForNode Node) {
 		Node.IterExpr.Evaluate(this);
 		Node.CondExpr.Evaluate(this);
 		/*local*/String Cond = this.PopSourceCode();
 		/*local*/String Iter = this.PopSourceCode();
 
 		/*local*/String Program = "for((; " + Cond  + "; " + Iter + " ))\ndo";
 		this.VisitEach(Node.LoopBody);
 		Program += this.PopSourceCode() + "done\n";
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitForEachNode(ForEachNode Node) {
 	}
 
 	@Override public void VisitConstNode(ConstNode Node) {
 		/*local*/String value = Node.ConstValue.toString();
 		
 		if(value.equals("true")) {
 			value = "0";
 		}
 		else if(value.equals("false")) {
 			value = "1";
 		}
 		this.PushSourceCode(value);
 	}
 
 	@Override public void VisitNewNode(NewNode Node) {
 //		String Type = Node.Type.ShortClassName;
 //		this.PushSourceCode("new " + Type);
 	}
 
 	@Override public void VisitNullNode(NullNode Node) {
 		this.PushSourceCode("0");
 	}
 
 	@Override public void VisitLocalNode(LocalNode Node) {
 		this.PushSourceCode("$" + Node.LocalName);
 	}
 
 	@Override public void VisitGetterNode(GetterNode Node) {
 //		Node.Expr.Evaluate(this);
 //		this.PushSourceCode(this.PopSourceCode() + "." + Node.Method.MethodName);
 	}
 
 	private String[] EvaluateParam(ArrayList<TypedNode> Params) {
 		/*local*/int Size = Params.size();
 		/*local*/String[] Programs = new String[Size];
 		for(int i = 0; i < Size; i++) {
 			/*local*/TypedNode Node = Params.get(i);
 			Node.Evaluate(this);
 			Programs[Size - i - 1] = this.PopSourceCode();
 		}
 		return Programs;
 	}
 
 	@Override public void VisitApplyNode(ApplyNode Node) {
 		/*local*/String Program = Node.Method.MethodName + " ";
 		/*local*/String[] Params = EvaluateParam(Node.Params);
 		for(int i = 0; i < Params.length; i++) {
 			String P = Params[i];
 			if(i != 0) {
 				Program += " ";
 			}
 			Program += P;
 		}
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitSuffixNode(SuffixNode Node) {
 		/*local*/String MethodName = Node.Token.ParsedText;
 		if(MethodName.equals("++")) {
 		}
 		else if(MethodName.equals("--")) {
 		}
 		else {
 			throw new RuntimeException("NotSupportOperator: " + MethodName);
 		}
 		Node.Expr.Evaluate(this);
 		this.PushSourceCode("((" + this.PopSourceCode() + MethodName + "))");
 	}
 
 	@Override public void VisitUnaryNode(UnaryNode Node) {
 		/*local*/String MethodName = Node.Token.ParsedText;
 
 		if(MethodName.equals("+")) {
 		}
 		else if(MethodName.equals("-")) {
 		}
 		else if(MethodName.equals("~")) {
 		}
 		else if(MethodName.equals("!")) {
 		}
 		else if(MethodName.equals("++")) {
 		}
 		else if(MethodName.equals("--")) {
 		}
 		else {
 			throw new RuntimeException("NotSupportOperator: " + MethodName);
 		}
 		Node.Expr.Evaluate(this);
 		this.PushSourceCode("((" + MethodName + this.PopSourceCode() + "))");
 	}
 
	@Override public void VisitBinaryNode(BinaryNode Node) {	//TODO: support string 
 		/*local*/String MethodName = Node.Token.ParsedText;
 
 		if(MethodName.equals("+")) {
 		}
 		else if(MethodName.equals("-")) {
 		}
 		else if(MethodName.equals("*")) {
 		}
 		else if(MethodName.equals("/")) {
 		}
 		else if(MethodName.equals("%")) {
 		}
 		else if(MethodName.equals("<<")) {
 		}
 		else if(MethodName.equals(">>")) {
 		}
 		else if(MethodName.equals("&")) {
 		}
 		else if(MethodName.equals("|")) {
 		}
 		else if(MethodName.equals("^")) {
 		}
 		else if(MethodName.equals("<=")) {
 		}
 		else if(MethodName.equals("<")) {
 		}
 		else if(MethodName.equals(">=")) {
 		}
 		else if(MethodName.equals(">")) {
 		}
 		else if(MethodName.equals("!=")) {
 		}
 		else if(MethodName.equals("==")) {
 		}
 		else {
 			throw new RuntimeException("NotSupportOperator: " + MethodName);
 		}
 		Node.RightNode.Evaluate(this);
 		Node.LeftNode.Evaluate(this);
 		this.PushSourceCode("((" + this.PopSourceCode() + " " + MethodName + " " + this.PopSourceCode() + "))");
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
 
	@Override public void VisitAssignNode(AssignNode Node) {	//TODO: support array
 		Node.RightNode.Evaluate(this);
 		Node.LeftNode.Evaluate(this);
		
		/*local*/String left = this.PopSourceCode();
		/*local*/String right = this.PopSourceCode();
		
		if(left.startsWith("$")) {
			left = left.substring(1, left.length());
		}
		
		if(!right.startsWith("$")) {
			right = "$" + right;
		}
		
		this.PushSourceCode(left + "=" + right);
 	}
 
 	@Override public void VisitLetNode(LetNode Node) {
 		Node.VarNode.Evaluate(this);
 		/*local*/String Code = this.PopSourceCode();
 		this.VisitEach(Node.BlockNode);
 		
 		/*local*/String head = "";
 		if(inFun) {
 			head = "local ";
 		}
 		this.PushSourceCode(head + Code + this.PopSourceCode());
 	}
 
 	@Override public void VisitIfNode(IfNode Node) {
 		Node.CondExpr.Evaluate(this);
 		this.VisitEach(Node.ThenNode);
 		this.VisitEach(Node.ElseNode);
 
 		/*local*/String ElseBlock = this.PopSourceCode();
 		/*local*/String ThenBlock = this.PopSourceCode();
 		/*local*/String CondExpr = this.PopSourceCode();
 		/*local*/String Code = "if " + CondExpr + "\nthen" + ThenBlock;
 		if(Node.ElseNode != null) {
 			Code += "\nelse" + ElseBlock;
 		}
 		Code += "fi";
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitSwitchNode(SwitchNode Node) {
 
 	}
 
 	@Override public void VisitReturnNode(ReturnNode Node) {	// only support int value
 		/*local*/String Code = "return";
 		if(Node.Expr != null) {
 			Node.Expr.Evaluate(this);
 			Code += " " + this.PopSourceCode();
 		}
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitLabelNode(LabelNode Node) {
 	}
 
 	@Override public void VisitJumpNode(JumpNode Node) {
 	}
 
 	@Override public void VisitBreakNode(BreakNode Node) {
 		/*local*/String Code = "break";	// not support label
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitContinueNode(ContinueNode Node) {
 		/*local*/String Code = "continue";	// not support label
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitTryNode(TryNode Node) {
 //		String Code = "try";
 //		//this.VisitEach(Node.CatchBlock);
 //		this.VisitEach(Node.TryBlock);
 //		Code += this.PopSourceCode();
 //		if(Node.FinallyBlock != null) {
 //			this.VisitEach(Node.FinallyBlock);
 //			Code += " finally " + this.PopSourceCode();
 //		}
 //		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitThrowNode(ThrowNode Node) {
 //		Node.Expr.Evaluate(this);
 //		String Code = "throw " + this.PopSourceCode();
 //		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitFunctionNode(FunctionNode Node) {
 	}
 
 	@Override public void VisitErrorNode(ErrorNode Node) {
 //		String Code = "throw Error(\"" + Node.Token.ParsedText + "\")";
 //		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitCommandNode(CommandNode Node) {	// currently only support statement
 		/*local*/String Code = "";
 		/*local*/int count = 0;
 		/*local*/CommandNode CurrentNode = Node;
 		while(CurrentNode != null) {
 			if(count > 0) {
 				Code += " | ";
 			}
 			Code += CreateCommand(CurrentNode);
 			count++;
 			CurrentNode = (CommandNode) CurrentNode.PipedNextNode;
 		}
 		this.PushSourceCode(Code);
 	}
 
 	private String CreateCommand(CommandNode CurrentNode) {
 		/*local*/String Code = "";
 		/*local*/int size = CurrentNode.Params.size();
 		for(int i = 0; i < size; i++) {
 			CurrentNode.Params.get(i).Evaluate(this);
 			Code += this.PopSourceCode() + " ";
 		}
 		return Code;
 	}
 
 	private TypedNode ResolveParamName(ArrayList<String> ParamNameList, TypedNode Body) {
 		return ConvertParamName(ParamNameList, Body, 0);
 	}
 
 	private TypedNode ConvertParamName(ArrayList<String> ParamNameList, TypedNode Body, int index) {
 		if(index  == ParamNameList.size()) {
 			return Body;
 		}
 		
 		/*local*/TypedNode varNode = new LocalNode(null, null, ParamNameList.get(index));
 		/*local*/TypedNode oldVarNode = new LocalNode(null, null, "" +(index + 1));
 		/*local*/TypedNode assignNode = new AssignNode(null, null, varNode, oldVarNode);
 		assignNode.NextNode = ConvertParamName(ParamNameList, Body, ++index);
 		return new LetNode(null, null, null, varNode, assignNode);
 	}
 
 	@Override public void DefineFunction(GtMethod Method, ArrayList<String> ParamNameList, TypedNode Body) {
 		/*local*/String Function = "function ";
 		inFun = true;
 		Function += Method.MethodName + "() {";
 		this.VisitEach(ResolveParamName(ParamNameList, Body));
 		Function += PopSourceCode() + "\n}";
 		this.WriteTranslatedCode(Function);
 		inFun = false;
 	}
 
 	@Override public Object Eval(TypedNode Node) {
 		this.VisitEach(Node);
 		/*local*/String Code = this.PopSourceCode();
 		this.WriteTranslatedCode(Code);
 		return Code;
 	}
 
 	@Override public void AddClass(GtType Type) {
 	}
 
 	@Override public void SetLanguageContext(GtContext Context) {
 		new JavaLayerDef().MakeDefinition(Context.DefaultNameSpace);
 	}
 }
