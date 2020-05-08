 //ifdef JAVA
 import java.util.ArrayList;
 //endif VAJA
 
 //GreenTea Generator should be written in each language.
 
 public class BashSourceGenerator extends SourceGenerator {
 	/*field*/boolean inFunc = false;
 	/*field*/final String retVar = "__ret";
 
 	BashSourceGenerator/*constructor*/() {
 		super("BashSource");
 		this.WriteTranslatedCode("#!/usr/bin/bash\n");
 	}
 
 	public void VisitBlockWithIndent(GtNode Node) {
 		/*local*/String Code = "\n";
 		this.Indent();
 		/*local*/GtNode CurrentNode = Node;
 		while(CurrentNode != null) {
 			CurrentNode.Evaluate(this);
 			Code += this.GetIndentString() + this.PopSourceCode() + "\n";
 			CurrentNode = CurrentNode.NextNode;
 		}
 		this.UnIndent();
 		Code += this.GetIndentString();
 		this.PushSourceCode(Code);
 	}
 	
 	public void VisitBlockWithoutIndent(GtNode Node) {
 		/*local*/String Code = "";
 		/*local*/GtNode CurrentNode = Node;
 		while(CurrentNode != null) {
 			CurrentNode.Evaluate(this);
 			Code += this.GetIndentString() + this.PopSourceCode() + "\n";
 			CurrentNode = CurrentNode.NextNode;
 		}
 		this.PushSourceCode(Code.substring(0, Code.length() - 1));
 	}
 
 	@Override public void VisitEmptyNode(GtNode Node) {
 	}
 
 	@Override public void VisitIndexerNode(IndexerNode Node) {
 		Node.Indexer.Evaluate(this);
 		Node.Expr.Evaluate(this);
 		this.PushSourceCode(this.PopSourceCode() + "[" + this.PopSourceCode() + "]");
 	}
 
 	@Override public void VisitMessageNode(MessageNode Node) {
 		// not support
 	}
 
 	@Override public void VisitWhileNode(WhileNode Node) {
 		Node.CondExpr.Evaluate(this);
 		/*local*/String Program = "while " + this.PopSourceCode() + " ;do";
 		this.VisitBlockWithIndent(Node.LoopBody);
 		Program += this.PopSourceCode() + "done";
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitDoWhileNode(DoWhileNode Node) {
 		// not support
 	}
 
 	@Override public void VisitForNode(ForNode Node) {
 		Node.IterExpr.Evaluate(this);
 		Node.CondExpr.Evaluate(this);
 		/*local*/String Cond = this.PopSourceCode();
 		/*local*/String Iter = this.PopSourceCode();
 		
 		/*local*/String Program = "for((; " + Cond  + "; " + Iter + " )) ;do";
 		this.VisitBlockWithIndent(Node.LoopBody);
 		Program += this.PopSourceCode() + "done";
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitForEachNode(ForEachNode Node) {
 	}
 
 	@Override public void VisitConstNode(ConstNode Node) {
 		/*local*/String value = Node.ConstValue.toString();
 
 		if(Node.Type.equals(Node.Type.Context.BooleanType)) {
 			if(value.equals("true")) {
 				value = "0";
 			}
 			else if(value.equals("false")) {
 				value = "1";
 			}
 		}
 		this.PushSourceCode(value);
 	}
 
 	@Override public void VisitNewNode(NewNode Node) {
 //		/*local*/String Type = Node.Type.ShortClassName;
 //		this.PushSourceCode("new " + Type);
 	}
 
 	@Override public void VisitNullNode(NullNode Node) {
 		this.PushSourceCode("NULL");
 	}
 
 	@Override public void VisitLocalNode(LocalNode Node) {
 		this.PushSourceCode(Node.LocalName);
 	}
 
 	@Override public void VisitGetterNode(GetterNode Node) {
 		// not support
 	}
 
 	private String[] EvaluateParam(ArrayList<GtNode> Params) {
 		/*local*/int Size = Params.size();
 		/*local*/String[] Programs = new String[Size];
 		/*local*/int i = 0;
 		while(i < Size) {
 			/*local*/GtNode Node = Params.get(i);
 			Node.Evaluate(this);
 			Programs[Size - i - 1] = this.ResolveValueType(Node, this.PopSourceCode());
 			i = i + 1;
 		}
 		return Programs;
 	}
 
 	@Override public void VisitApplyNode(ApplyNode Node) {
 		/*local*/String Program = Node.Method.MethodName + " ";
 		/*local*/String[] Params = this.EvaluateParam(Node.Params);
 		/*local*/int i = 0;
 		while(i < Params.length) {
 			/*local*/String P = Params[i];
 			if(i != 0) {
 				Program += " ";
 			}
 			Program += P;
 			i = i + 1;
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
 			//throw new RuntimeException("NotSupportOperator: " + MethodName);
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
 			//throw new RuntimeException("NotSupportOperator: " + MethodName);
 		}
 		Node.Expr.Evaluate(this);
 		this.PushSourceCode("((" + MethodName + this.PopSourceCode() + "))");
 	}
 
 	@Override public void VisitBinaryNode(BinaryNode Node) {
 		/*local*/String MethodName = Node.Token.ParsedText;
 		if(Node.Type.equals(Node.Type.Context.StringType)) {
 			Node.RightNode.Evaluate(this);
 			Node.LeftNode.Evaluate(this);
 
 			if(MethodName.equals("+")) {
 				this.PushSourceCode(this.PopSourceCode() + this.PopSourceCode());
 				return;
 			}
 			else if(MethodName.equals("!=")) {
 			}
 			else if(MethodName.equals("==")) {
 			}
 			else {
 				//throw new RuntimeException("NotSupportOperator: " + MethodName);
 			}
 
 			Node.RightNode.Evaluate(this);
 			Node.LeftNode.Evaluate(this);
 			/*local*/String left = this.ResolveValueType(Node.LeftNode, this.PopSourceCode());
 			/*local*/String right = this.ResolveValueType(Node.RightNode, this.PopSourceCode());
 			this.PushSourceCode("((" + this.PopSourceCode() + " " + left  + " " + right + "))");
 			return;
 		}
 
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
 			//throw new RuntimeException("NotSupportOperator: " + MethodName);
 		}
 
 		Node.RightNode.Evaluate(this);
 		Node.LeftNode.Evaluate(this);
 		/*local*/String left = this.ResolveValueType(Node.LeftNode, this.PopSourceCode());
 		/*local*/String right = this.ResolveValueType(Node.RightNode, this.PopSourceCode());
 		
 //		if(Node.Type.equals(Node.Type.Context.Float)) {	// support float value
 //			this.PushSourceCode("(echo \"scale=10; " + left + " " + MethodName + " " + right + "\" | bc)");
 //			return;
 //		}
 		
 		this.PushSourceCode("((" + left + " " + MethodName + " " + right + "))");
 	}
 
 	@Override public void VisitAndNode(AndNode Node) {
 		Node.RightNode.Evaluate(this);
 		Node.LeftNode.Evaluate(this);
 		this.PushSourceCode("(" + this.PopSourceCode() + " && " + this.PopSourceCode() + ")");
 	}
 
 	@Override public void VisitOrNode(OrNode Node) {
 		Node.RightNode.Evaluate(this);
 		Node.LeftNode.Evaluate(this);
 		this.PushSourceCode("(" + this.PopSourceCode() + " || " + this.PopSourceCode() + ")");
 	}
 
 	@Override public void VisitAssignNode(AssignNode Node) {
 		Node.RightNode.Evaluate(this);
 		Node.LeftNode.Evaluate(this);
 		/*local*/String left = this.PopSourceCode();
 		/*local*/String code = this.PopSourceCode();
 		/*local*/String right = this.ResolveValueType(Node.RightNode, code);
 
 		this.PushSourceCode(left + "=" + right);
 	}
 
 	@Override public void VisitLetNode(LetNode Node) {
 		Node.VarNode.Evaluate(this);
 		/*local*/String Code = this.PopSourceCode();
 		this.VisitBlockWithoutIndent(Node.BlockNode);
 
 		/*local*/String head = "";
 		if(this.inFunc) {
 			head = "local ";
 		}
 		this.PushSourceCode(head + Code + "\n" + this.PopSourceCode());
 	}
 
 	@Override public void VisitIfNode(IfNode Node) {
 		Node.CondExpr.Evaluate(this);
 		this.VisitBlockWithIndent(Node.ThenNode);
 		this.VisitBlockWithIndent(Node.ElseNode);
 		
 		/*local*/String ElseBlock = this.PopSourceCode();
 		/*local*/String ThenBlock = this.PopSourceCode();
 		/*local*/String CondExpr = this.PopSourceCode();
 		/*local*/String Code = "if " + CondExpr + " ;then" + ThenBlock;
 		if(Node.ElseNode != null) {
 			Code += "else" + ElseBlock;
 		}
 		Code += "fi";
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitSwitchNode(SwitchNode Node) {
 	}
 
 	@Override public void VisitReturnNode(ReturnNode Node) {
 		if(this.inFunc && Node.Expr != null) {
 			Node.Expr.Evaluate(this);
 			/*local*/String expr = this.PopSourceCode();
 			/*local*/String ret = this.ResolveValueType(Node.Expr, expr);
 
 			if(Node.Expr instanceof CommandNode) {
 				this.PushSourceCode(expr + "\n" + this.GetIndentString() + "return " + ret);
 				return;
 			}
 
 			if(Node.Type.equals(Node.Type.Context.BooleanType) ||
 					Node.Type.equals(Node.Type.Context.IntType)) {
 				this.PushSourceCode("return " + ret);
 				return;
 			}
 
 			ret = this.retVar + "=" + ret;
 			this.PushSourceCode(ret);
 		}
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
 //		/*local*/String Code = "try";
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
 //		/*local*/String Code = "throw Error(\"" + Node.Token.ParsedText + "\")";
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
 			Code += this.CreateCommand(CurrentNode);
 			count++;
 			CurrentNode = (/*cast*/CommandNode) CurrentNode.PipedNextNode;
 		}
 		this.PushSourceCode(Code);
 	}
 
 	private String CreateCommand(CommandNode CurrentNode) {
 		/*local*/String Code = "";
 		/*local*/int size = CurrentNode.Params.size();
 		/*local*/int i = 0;
 		while(i < size) {
 			CurrentNode.Params.get(i).Evaluate(this);
 			Code += this.PopSourceCode() + " ";
 			i = i + 1;
 		}
 		return Code;
 	}
 
 	private GtNode ResolveParamName(ArrayList<String> ParamNameList, GtNode Body) {
 		return this.ConvertParamName(ParamNameList, Body, 0);
 	}
 
 	private GtNode ConvertParamName(ArrayList<String> ParamNameList, GtNode Body, int index) {
 		if(index  == ParamNameList.size()) {
 			return Body;
 		}
 
 		/*local*/GtNode VarNode = new LocalNode(null, null, ParamNameList.get(index));
 		/*local*/GtNode oldVarNode = new LocalNode(null, null, "" +(index + 1));
 		/*local*/GtNode assignNode = new AssignNode(null, null, VarNode, oldVarNode);
 		assignNode.NextNode = this.ConvertParamName(ParamNameList, Body, ++index);
 		return new LetNode(null, null, null, VarNode, assignNode);
 	}
 
 	private String ResolveValueType(GtNode TargetNode, String value) {
 		/*local*/String resolvedValue;
 		
 		if(TargetNode instanceof ConstNode || TargetNode instanceof NullNode) {
 			resolvedValue = value;
 		}
 		else if(TargetNode instanceof IndexerNode) {
 			resolvedValue = "${" + value + "}";
 		}
 		else if(TargetNode instanceof CommandNode) {	// TODO: support statement and expression
 			resolvedValue = "$?";
 		}
 //		else if(TargetNode instanceof ApplyNode) {
 //			//TODO
 //		}
 		else {
 			resolvedValue = "$" + value;
 		}
 		return resolvedValue;
 	}
 
 	@Override public void DefineFunction(GtMethod Method, ArrayList<String> ParamNameList, GtNode Body) {
 		/*local*/String Function = "function ";
 		this.inFunc = true;
 		Function += Method.MethodName + "() {";
 		this.VisitBlockWithIndent(this.ResolveParamName(ParamNameList, Body));
		Function += PopSourceCode() + "}";
 		this.WriteTranslatedCode(Function);
 		this.inFunc = false;
 	}
 
 	@Override public Object Eval(GtNode Node) {
 		this.VisitBlockWithoutIndent(Node);
 		/*local*/String Code = this.PopSourceCode();
 		this.WriteTranslatedCode(Code);
 		return Code;
 	}
 
 	@Override public void AddClass(GtType Type) {
 	}
 
 	@Override public void SetLanguageContext(GtContext Context) {
 	}
 }
