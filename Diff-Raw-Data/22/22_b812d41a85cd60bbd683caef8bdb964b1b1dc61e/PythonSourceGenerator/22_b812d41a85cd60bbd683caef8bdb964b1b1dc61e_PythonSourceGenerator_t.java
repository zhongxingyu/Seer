 // ***************************************************************************
 // Copyright (c) 2013, JST/CREST DEOS project authors. All rights reserved.
 // Redistribution and use in source and binary forms, with or without
 // modification, are permitted provided that the following conditions are met:
 //
 // *  Redistributions of source code must retain the above copyright notice,
 //    this list of conditions and the following disclaimer.
 // *  Redistributions in binary form must reproduce the above copyright
 //    notice, this list of conditions and the following disclaimer in the
 //    documentation and/or other materials provided with the distribution.
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 // TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 // PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 // CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 // EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 // PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 // OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 // WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 // OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 // ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 // **************************************************************************
 
 //ifdef JAVA
 import java.util.ArrayList;
 //endif VAJA
 
 //GreenTea Generator should be written in each language.
 
 public class PythonSourceGenerator extends SourceGenerator {
 
 	PythonSourceGenerator/*constructor*/(String TargetCode, String OutputFile, int GeneratorFlag) {
 		super(TargetCode, OutputFile, GeneratorFlag);
 		this.Tab = "    ";
 	}
 
 	public String VisitBlockWithIndent(GtNode Node, boolean inBlock) {
 		/*local*/String Code = "";
 		if(inBlock) {
 			this.Indent();
 		}
 		/*local*/GtNode CurrentNode = Node;
 		while(CurrentNode != null) {
 			/*local*/String poppedCode = this.VisitNode(CurrentNode);
 			if(!poppedCode.equals("")) {
 				Code += this.GetIndentString() + poppedCode + this.LineFeed;
 			}
 			CurrentNode = CurrentNode.NextNode;
 		}
 		if(inBlock) {
 			this.UnIndent();
 			Code += this.GetIndentString();
 		}		else {
 			if(Code.length() > 0) {
 				Code = Code.substring(0, Code.length() - 1);
 			}
 		}
 		return Code;
 	}
 
 	@Override public void VisitEmptyNode(GtNode Node) {
 	}
 
 	@Override public void VisitIndexerNode(IndexerNode Node) {
 		this.PushSourceCode(this.VisitNode(Node.Expr) + "[" + this.VisitNode(Node.IndexAt) + "]");
 	}
 
 	@Override public void VisitMessageNode(MessageNode Node) {
 	}
 
 	@Override public void VisitWhileNode(WhileNode Node) {
 		/*local*/String Program = "while " + this.VisitNode(Node.CondExpr) + ":" + this.LineFeed;
 		Program += this.VisitBlockWithIndent(Node.LoopBody, true);
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitDoWhileNode(DoWhileNode Node) {
 		/*local*/String LoopBody = this.VisitBlockWithIndent(Node.LoopBody, true);
 		/*local*/String Program = "if True:" + this.LineFeed + LoopBody;
 		Program += "while " + this.VisitNode(Node.CondExpr) + ":" + this.LineFeed;
 		this.PushSourceCode(Program + LoopBody);
 	}
 
 	@Override public void VisitForNode(ForNode Node) {
 		Node.LoopBody.MoveTailNode().NextNode = Node.IterExpr;
 		/*local*/GtNode NewLoopBody = Node.LoopBody;
 		/*local*/WhileNode NewNode = new WhileNode(Node.Type, Node.Token, Node.CondExpr, NewLoopBody);
 		this.VisitWhileNode(NewNode);
 	}
 
 	@Override public void VisitForEachNode(ForEachNode Node) {
 		/*local*/String Iter = this.VisitNode(Node.IterExpr);
 		/*local*/String Variable = this.VisitNode(Node.Variable);
 		/*local*/String Program = "for " + Variable + " in " + Iter + ":" + this.LineFeed;
 		Program += this.VisitBlockWithIndent(Node.LoopBody, true);
 		this.PushSourceCode(Program);
 	}
 
 	@Override protected String StringfyConstValue(Object ConstValue) {
 		if(ConstValue == null) {
 			return "None";
 		}
 		if(ConstValue instanceof Boolean) {
 			if(ConstValue.equals(true)) {
 				return "True";
 			}
 			return "False";
 		}
 		return super.StringfyConstValue(ConstValue);
 	}
 
 	@Override public void VisitConstNode(ConstNode Node) {
 		/*local*/String StringfiedValue = this.StringfyConstValue(Node.ConstValue);
 		this.PushSourceCode(StringfiedValue);
 	}
 
 	@Override public void VisitNullNode(NullNode Node) {
 		this.PushSourceCode("None");
 	}
 
 	@Override public void VisitLocalNode(LocalNode Node) {
 		this.PushSourceCode(Node.NativeName);
 	}
 
 	@Override public void VisitGetterNode(GetterNode Node) {
 		this.PushSourceCode(this.VisitNode(Node.Expr) + "." + Node.Token.ParsedText);
 	}
 
 	private String[] MakeParamCode1(GtNode Node) {
 		/*local*/String[] ParamCode = new String[1];
 		ParamCode[0] = this.VisitNode(Node);
 		return ParamCode;
 	}
 	
 	private String[] MakeParamCode2(GtNode Node, GtNode Node2) {
 		/*local*/String[] ParamCode = new String[2];
 		ParamCode[0] = this.VisitNode(Node);
 		ParamCode[1] = this.VisitNode(Node2);
 		return ParamCode;
 	}
 
 	private String[] MakeParamCode(ArrayList<GtNode> ParamList) {
 		/*local*/int Size = GtStatic.ListSize(ParamList);
 		/*local*/String[] ParamCode = new String[Size - 1];
 		/*local*/int i = 1;
 		while(i < Size) {
 			/*local*/GtNode Node = ParamList.get(i);
 			ParamCode[Size - i - 1] = this.VisitNode(Node);
 			i = i + 1;
 		}
 		return ParamCode;
 	}
 
 	private String JoinCode(String BeginCode, int BeginIdx, String[] ParamCode, String EndCode) {
 		/*local*/String JoinedCode = BeginCode;
 		/*local*/int i = BeginIdx;
 		while(i < ParamCode.length) {
 			/*local*/String P = ParamCode[i];
 			if(i != BeginIdx) {
 				JoinedCode += ", ";
 			}
 			JoinedCode += P;
 			i = i + 1;
 		}
 		return JoinedCode + EndCode;
 	}
 
 	@Override public void VisitApplyNode(ApplyNode Node) {
 		/*local*/String Program = this.GenerateApplyFunc(Node);
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitSuffixNode(SuffixNode Node) {	//FIXME
 		/*local*/String FuncName = Node.Token.ParsedText;
 		if(FuncName.equals("++")) {
 			FuncName = " += 1";
 		}
 		else if(FuncName.equals("--")) {
 			FuncName = " -= 1";
 		}
 		else {
 			LibGreenTea.DebugP(FuncName + " is not supported suffix operator!!");
 		}
 		this.PushSourceCode(this.VisitNode(Node.Expr) + FuncName);
 	}
 
 	@Override public void VisitNewNode(NewNode Node) {
 		/*local*/int ParamSize = GtStatic.ListSize(Node.Params);
 		/*local*/String Type = Node.Type.ShortClassName;
 		/*local*/String Template = this.GenerateFuncTemplate(ParamSize, Node.Func);
 		Template = Template.replace("$1", Type + "()");
 		this.PushSourceCode(this.ApplyMacro(Template, Node.Params));
 	}
 
 	@Override public void VisitUnaryNode(UnaryNode Node) {
 		/*local*/String FuncName = Node.Token.ParsedText;
 		/*local*/String Expr = this.VisitNode(Node.Expr);
 		this.PushSourceCode("(" + SourceGenerator.GenerateApplyFunc1(Node.Func, FuncName, false, Expr) + ")");
 	}
 
 	@Override public void VisitBinaryNode(BinaryNode Node) {
 		/*local*/String FuncName = Node.Token.ParsedText;
 		/*local*/String Left = this.VisitNode(Node.LeftNode);
 		/*local*/String Right = this.VisitNode(Node.RightNode);
 		this.PushSourceCode("(" + SourceGenerator.GenerateApplyFunc2(Node.Func, FuncName, Left, Right) + ")");
 	}
 
 	@Override public void VisitAndNode(AndNode Node) {
 		this.PushSourceCode("(" + this.VisitNode(Node.LeftNode) + " and " + this.VisitNode(Node.RightNode) + ")");
 	}
 
 	@Override public void VisitOrNode(OrNode Node) {
 		this.PushSourceCode("(" + this.VisitNode(Node.LeftNode) + " or " + this.VisitNode(Node.RightNode) + ")");
 	}
 
 	@Override public void VisitAssignNode(AssignNode Node) {
 		this.PushSourceCode(this.VisitNode(Node.LeftNode) + " = " + this.VisitNode(Node.RightNode));
 	}
 
 	@Override public void VisitLetNode(LetNode Node) {
 		/*local*/String Code = Node.VariableName;
 		/*local*/String InitValue = "None";
 		if(Node.InitNode != null) {
 			InitValue = this.VisitNode(Node.InitNode);
 		}
 		Code += " = " + InitValue + this.LineFeed;
 		this.PushSourceCode(Code + this.VisitBlockWithIndent(Node.BlockNode, false));
 	}
 
 	@Override public void VisitIfNode(IfNode Node) {
 		/*local*/String CondExpr = this.VisitNode(Node.CondExpr);
 		/*local*/String ThenBlock = this.VisitBlockWithIndent(Node.ThenNode, true);
 		/*local*/String ElseBlock = this.VisitBlockWithIndent(Node.ElseNode, true);
 		/*local*/String Code = "if " + CondExpr + ":" + this.LineFeed + ThenBlock;
 		if(Node.ElseNode != null) {
 			Code += "else:" + this.LineFeed + ElseBlock;
 		}
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitSwitchNode(SwitchNode Node) {
 	}
 
 	@Override public void VisitReturnNode(ReturnNode Node) {
 		/*local*/String retValue = "";
 		if(Node.Expr != null) {
 			retValue = this.VisitNode(Node.Expr);
 		}
 		this.PushSourceCode("return " + retValue);
 	}
 
 	@Override public void VisitBreakNode(BreakNode Node) {
 		/*local*/String Code = "break";
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitContinueNode(ContinueNode Node) {
 		/*local*/String Code = "continue";
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitTryNode(TryNode Node) {
 		/*local*/String Code = "try:" + this.LineFeed;
 		Code += this.VisitBlockWithIndent(Node.TryBlock, true);
 		/*local*/LetNode Val = (/*cast*/LetNode) Node.CatchExpr;
 		Code += "except " + Val.Type.toString() + ", " + Val.VariableName + ":" + this.LineFeed;
 		Code += this.VisitBlockWithIndent(Node.CatchBlock, true);
 		if(Node.FinallyBlock != null) {
 			/*local*/String Finally = this.VisitBlockWithIndent(Node.FinallyBlock, true);
 			Code += "finally:" + this.LineFeed + Finally;
 		}
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitThrowNode(ThrowNode Node) {
 		/*local*/String expr = "";
 		if(Node.Expr != null) {
 			expr = this.VisitNode(Node.Expr);
 		}
 		this.PushSourceCode("raise " + expr);
 	}
 
 	@Override public void VisitFunctionNode(FunctionNode Node) {
 	}
 
 	@Override public void VisitErrorNode(ErrorNode Node) {
 		/*local*/String Code = "raise SoftwareFault(\"" + Node.Token.ParsedText + "\")";
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitCommandNode(CommandNode Node) {
 		/*local*/String Code = "";
 		/*local*/int count = 0;
 		/*local*/CommandNode CurrentNode = Node;
 		while(CurrentNode != null) {
 			if(count > 0) {
 				Code += " | ";
 			}
 			Code += this.AppendCommand(CurrentNode);
 			count += 1;
 			CurrentNode = (/*cast*/CommandNode) CurrentNode.PipedNextNode;
 		}
 		
 		if(Node.Type.equals(Node.Type.Context.StringType)) {
 			Code = "subprocess.check_output(\"" + Code + "\", shell=True)";
 		}
 		else if(Node.Type.equals(Node.Type.Context.BooleanType)) {
 			Code = "(subprocess.call(\"" + Code + "\", shell=True) == 0)";
 		}
 		else {
 			Code = "subprocess.call(\"" + Code + "\", shell=True)";
 		}
 		this.PushSourceCode(Code);
 	}
 
 	private String AppendCommand(CommandNode CurrentNode) {
 		/*local*/String Code = "";
 		/*local*/int size = CurrentNode.Params.size();
 		/*local*/int i = 0;
 		while(i < size) {
 			Code += this.VisitNode(CurrentNode.Params.get(i)) + " ";
 			i = i + 1;
 		}
 		return Code;
 	}
 
 	@Override public void GenerateFunc(GtFunc Func, ArrayList<String> ParamNameList, GtNode Body) {
 		/*local*/String Function = "def ";
 		Function += Func.GetNativeFuncName() + "(";
 		/*local*/int i = 0;
 		/*local*/int size = ParamNameList.size();
 		while(i < size) {
 			if(i > 0) {
 				Function += ", ";
 			}
 			Function += ParamNameList.get(i);
 			i = i + 1;
 		}
 		/*local*/String Block = this.VisitBlockWithIndent(Body, true);
 		Function += "):" + this.LineFeed + Block + this.LineFeed;
 		this.WriteLineCode(Function);
 	}
 
 	@Override public void GenerateClassField(GtType Type, GtClassField ClassField) {
 		/*local*/String Program = this.GetIndentString() + "class " + Type.ShortClassName;
//		if(Type.SuperType != null) {
//			Program += "(" + Type.SuperType.ShortClassName + ")";
//		}
 		Program += ":" + this.LineFeed;
 		this.Indent();
 		
 		Program += this.GetIndentString() + "def __init__(" + this.GetRecvName() + ")" + ":" + this.LineFeed;
 		this.Indent();
 		/*local*/int i = 0;
 		while (i < ClassField.FieldList.size()) {
 			/*local*/GtFieldInfo FieldInfo = ClassField.FieldList.get(i);
 			/*local*/String InitValue = this.StringfyConstValue(FieldInfo.InitValue);
 			if(!FieldInfo.Type.IsNative()) {
 				InitValue = "None";
 			}
 			Program += this.GetIndentString() + this.GetRecvName() + "." + FieldInfo.NativeName + " = " + InitValue + this.LineFeed;
 			i = i + 1;
 		}
 		this.UnIndent();
 		
 		this.UnIndent();
 		this.WriteLineCode(Program);
 	}
 
 	@Override public Object Eval(GtNode Node) {
 		/*local*/String Code = this.VisitBlockWithIndent(Node, false);
 		if(!Code.equals("")) {
 			this.WriteLineCode(Code);
 		}
 		return Code;
 	}
 
 	@Override public String GetRecvName() {
 		return "self";
 	}
 
 	@Override public void FinishCompilationUnit() {
 		//this.WriteLineCode("if __name__ == '__main__':");
 		//this.WriteLineCode(this.Tab + "main()");
 	}
 	
 	@Override public void InvokeMainFunc(String MainFuncName) {
 		this.WriteLineCode("if __name__ == '__main__':");
 		this.WriteLineCode(this.Tab + MainFuncName + "()");
 	}
 
 }
