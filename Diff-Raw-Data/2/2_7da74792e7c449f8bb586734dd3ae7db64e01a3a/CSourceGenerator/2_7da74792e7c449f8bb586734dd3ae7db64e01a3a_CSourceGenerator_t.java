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
 
 //ifdef  JAVA
 import java.util.ArrayList;
 //endif VAJA
 
 //GreenTea Generator should be written in each language.
 
 public class CSourceGenerator extends SourceGenerator {
 	/*field*/ConstantFolder Opt;
 	CSourceGenerator/*constructor*/(String TargetCode, String OutputFile, int GeneratorFlag) {
 		super(TargetCode, OutputFile, GeneratorFlag);
 		this.Opt = new ConstantFolder(TargetCode, OutputFile, GeneratorFlag);
 	}
 	@Override public void InitContext(GtClassContext Context) {
 		super.InitContext(Context);
 		this.Opt.InitContext(Context);
 	}
 
 	private String GetLocalType(GtType Type, boolean IsPointer) {
 		if(Type.IsDynamic() || Type.IsNative()) {
 			return Type.ShortClassName;
 		}
 		/*local*/String TypeName = "struct " + Type.ShortClassName;
 		if(IsPointer) {
 			TypeName += "*";
 		}
 		return TypeName;
 
 	}
 	public String NativeTypeName(GtType Type) {
 		return GetLocalType(Type, false);
 	}
 
 	public String LocalTypeName(GtType Type) {
 		return GetLocalType(Type, true);
 	}
 
 	public String GreenTeaTypeName(GtType Type) {
 		return Type.ShortClassName;
 	}
 
 	@Override protected String StringfyConstValue(Object ConstValue) {
 		if(ConstValue == null) {
 			return "NULL";
 		}
 		if(ConstValue instanceof Boolean) {
 			if(ConstValue.equals(true)) {
 				return "1";
 			}
 			return "0";
 		}
 		return super.StringfyConstValue(ConstValue);
 	}
 
 	public void VisitBlockEachStatementWithIndent(GtNode Node, boolean NeedBlock) {
 		/*local*/String Code = "";
 		if(NeedBlock) {
 			Code += "{" + this.LineFeed;
 			this.Indent();
 		}
 		/*local*/GtNode CurrentNode = Node;
 		while(CurrentNode != null) {
 			Code += this.GetIndentString() + this.VisitNode(CurrentNode) + ";" + this.LineFeed;
 			CurrentNode = CurrentNode.NextNode;
 		}
 		if(NeedBlock) {
 			this.UnIndent();
 			Code += this.GetIndentString() + "}";
 		}
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitEmptyNode(GtNode Node) {
 		this.PushSourceCode("");
 	}
 
 	@Override public void VisitSuffixNode(SuffixNode Node) {
 		/*local*/String FuncName = Node.Token.ParsedText;
 		this.PushSourceCode(this.VisitNode(Node.Expr) + FuncName);
 	}
 
 	@Override public void VisitUnaryNode(UnaryNode Node) {
 		/*local*/String FuncName = Node.Token.ParsedText;
 		/*local*/String Expr = this.VisitNode(Node.Expr);
 		this.PushSourceCode("(" + SourceGenerator.GenerateApplyFunc1(Node.Func, FuncName, false, Expr) + ")");
 	}
 
 	@Override public void VisitIndexerNode(IndexerNode Node) {
 		this.PushSourceCode(this.VisitNode(Node.Expr) + "[" + this.VisitNode(Node.IndexAt) + "]");
 	}
 
 	@Override public void VisitMessageNode(MessageNode Node) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override public void VisitWhileNode(WhileNode Node) {
 		/*local*/String Program = "while(" + this.VisitNode(Node.CondExpr) + ")";
 		this.VisitBlockEachStatementWithIndent(Node.LoopBody, true);
 		Program += this.PopSourceCode();
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitDoWhileNode(DoWhileNode Node) {
 		/*local*/String Program = "do";
 		this.VisitBlockEachStatementWithIndent(Node.LoopBody, true);
 		Program += " while(" + this.VisitNode(Node.CondExpr) + ")";
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitForNode(ForNode Node) {
 		/*local*/String Cond = this.VisitNode(Node.CondExpr);
 		/*local*/String Iter = this.VisitNode(Node.IterExpr);
 		/*local*/String Program = "for(; " + Cond  + "; " + Iter + ")";
 		Program += this.VisitNode(Node.LoopBody);
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
 		/*local*/int ParamSize = GtStatic.ListSize(Node.Params);
 		/*local*/String Type = this.GreenTeaTypeName(Node.Type);
 		/*local*/String Template = this.GenerateFuncTemplate(ParamSize, Node.Func);
 		Template = Template.replace("$1", "NEW_" + Type + "()");
 		this.PushSourceCode(this.ApplyMacro(Template, Node.Params));
 	}
 
 	@Override public void VisitNullNode(NullNode Node) {
 		this.PushSourceCode("NULL");
 	}
 
 	@Override public void VisitLocalNode(LocalNode Node) {
 		this.PushSourceCode(Node.NativeName);
 	}
 
 	@Override public void VisitGetterNode(GetterNode Node) {
 		/*local*/String Program = this.VisitNode(Node.Expr);
 		/*local*/String FieldName = Node.Func.FuncName;
 		/*local*/GtType RecvType = Node.Func.GetRecvType();
 		if(Node.Expr.Type == RecvType) {
 			Program = Program + "->" + FieldName;
 		}
 		else {
 			Program = "GT_GetField(" + this.LocalTypeName(RecvType) + ", " + Program + ", " + FieldName + ")";
 		}
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitApplyNode(ApplyNode Node) {
 		/*local*/String Program = this.GenerateApplyFunc(Node);
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitBinaryNode(BinaryNode Node) {
 		/*local*/String FuncName = Node.Token.ParsedText;
 		/*local*/String Left = this.VisitNode(Node.LeftNode);
 		/*local*/String Right = this.VisitNode(Node.RightNode);
 		this.PushSourceCode("(" + SourceGenerator.GenerateApplyFunc2(Node.Func, FuncName, Left, Right) + ")");
 	}
 
 	@Override public void VisitAndNode(AndNode Node) {
 		this.PushSourceCode(this.VisitNode(Node.LeftNode) + " && " + this.VisitNode(Node.RightNode));
 	}
 
 	@Override public void VisitOrNode(OrNode Node) {
 		this.PushSourceCode(this.VisitNode(Node.LeftNode) + " || " + this.VisitNode(Node.RightNode));
 	}
 
 	@Override public void VisitAssignNode(AssignNode Node) {
 		this.PushSourceCode(this.VisitNode(Node.LeftNode) + " = " + this.VisitNode(Node.RightNode));
 	}
 
 	@Override public void VisitLetNode(LetNode Node) {
 		/*local*/String Type = this.LocalTypeName(Node.DeclType);
 		/*local*/String VarName = Node.VariableName;
 		/*local*/String Code = Type + " " + VarName;
 		/*local*/boolean CreateNewScope = true;
 		if(Node.InitNode != null) {
 			Code += " = " + this.VisitNode(Node.InitNode);
 		}
 		Code +=  ";" + this.LineFeed;
 		if(CreateNewScope) {
 			Code += this.GetIndentString();
 		}
 		this.VisitBlockEachStatementWithIndent(Node.BlockNode, CreateNewScope);
 		this.PushSourceCode(Code + this.PopSourceCode());
 	}
 
 	@Override public void VisitIfNode(IfNode Node) {
 		/*local*/String CondExpr = this.VisitNode(Node.CondExpr);
 		this.VisitBlockEachStatementWithIndent(Node.ThenNode, true);
 		this.VisitBlockEachStatementWithIndent(Node.ElseNode, true);
 		/*local*/String ElseBlock = this.PopSourceCode();
 		/*local*/String ThenBlock = this.PopSourceCode();
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
 			Code += " " + this.VisitNode(Node.Expr);
 		}
 		this.PushSourceCode(Code);
 		this.StopVisitor(Node);
 	}
 
 	@Override public void VisitLabelNode(LabelNode Node) {
 		/*local*/String Label = Node.Label;
 		this.PushSourceCode(Label + ":");
 	}
 
 	@Override public void VisitJumpNode(JumpNode Node) {
 		/*local*/String Label = Node.Label;
 		this.PushSourceCode("goto " + Label);
 		this.StopVisitor(Node);
 	}
 
 	@Override public void VisitBreakNode(BreakNode Node) {
 		/*local*/String Code = "break";
 		/*local*/String Label = Node.Label;
 		if(Label != null) {
 			Code += " " + Label;
 		}
 		this.PushSourceCode(Code);
 		this.StopVisitor(Node);
 	}
 
 	@Override public void VisitContinueNode(ContinueNode Node) {
 		/*local*/String Code = "continue";
 		/*local*/String Label = Node.Label;
 		if(Label != null) {
 			Code += " " + Label;
 		}
 		this.PushSourceCode(Code);
 		this.StopVisitor(Node);
 	}
 
 	@Override public void VisitTryNode(TryNode Node) {
 		/*local*/String Code = "try ";
 		this.VisitBlockEachStatementWithIndent(Node.TryBlock, true);
 		Code += this.PopSourceCode();
 		/*local*/LetNode Val = (/*cast*/LetNode) Node.CatchExpr;
 		Code += " catch (" + Val.Type.toString() + " " + Val.VariableName + ") ";
 		this.VisitBlockEachStatementWithIndent(Node.CatchBlock, true);
 		Code += this.PopSourceCode();
 		if(Node.FinallyBlock != null) {
 			this.VisitBlockEachStatementWithIndent(Node.FinallyBlock, true);
 			Code += " finally " + this.PopSourceCode();
 		}
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitThrowNode(ThrowNode Node) {
 		/*local*/String Code = "throw " + this.VisitNode(Node.Expr);
 		this.PushSourceCode(Code);
 		this.StopVisitor(Node);
 	}
 
 	@Override public void VisitFunctionNode(FunctionNode Node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override public void VisitErrorNode(ErrorNode Node) {
 		/*local*/String Code = "throw Error(\"" + Node.Token.ParsedText + "\")";
 		this.PushSourceCode(Code);
 		this.StopVisitor(Node);
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
 		Code = Command + ";" + this.LineFeed + this.GetIndentString() + Code + "__Command)";
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void GenerateFunc(GtFunc Func, ArrayList<String> ParamNameList, GtNode Body) {
 		/*local*/String Code = "";
 		if(!Func.Is(ExportFunc)) {
 			Code = "static ";
 		}
 		Body = this.Opt.Fold(Body);
 		/*local*/String RetTy = this.LocalTypeName(Func.GetReturnType());
 		Code += RetTy + " " + Func.GetNativeFuncName() + "(";
 		/*local*/int i = 0;
 		while(i < ParamNameList.size()) {
 			String ParamTy = this.LocalTypeName(Func.GetFuncParamType(i));
 			if(i > 0) {
 				Code += ", ";
 			}
 			Code += ParamTy + " " + ParamNameList.get(i);
 			i = i + 1;
 		}
 		Code += ")";
 		this.VisitBlockEachStatementWithIndent(Body, true);
 		Code += this.PopSourceCode();
 		this.WriteLineCode(Code);
 	}
 
 	@Override public Object Eval(GtNode Node) {
 		Node = this.Opt.Fold(Node);
 		this.VisitBlockEachStatementWithIndent(Node, false);
 		/*local*/String Code = this.PopSourceCode();
 		if(Code.equals(";" + this.LineFeed)) {
 			return "";
 		}
 		this.WriteLineCode(Code);
 		return Code;
 	}
 
 	@Override public void GenerateClassField(GtType Type, GtClassField ClassField) {
 		/*local*/String TypeName = Type.ShortClassName;
 		/*local*/String LocalType = this.LocalTypeName(Type);
 		/*local*/String Program = this.GetIndentString() + "struct " + TypeName + " {" + this.LineFeed;
 		this.Indent();
 		if(Type.SuperType != null) {
			Program += this.GetIndentString() + "// " + this.LocalTypeName(Type.SuperType) + " __base;" + this.LineFeed;
 		}
 		/*local*/int i = 0;
 		while (i < ClassField.FieldList.size()) {
 			/*local*/GtFieldInfo FieldInfo = ClassField.FieldList.get(i);
 			/*local*/GtType VarType = FieldInfo.Type;
 			/*local*/String VarName = FieldInfo.NativeName;
 			Program += this.GetIndentString() + this.LocalTypeName(VarType) + " " + VarName + ";" + this.LineFeed;
 			i = i + 1;
 		}
 		this.UnIndent();
 		Program += this.GetIndentString() + "};" + this.LineFeed;
 		Program += this.GetIndentString() + LocalType + " NEW_" + TypeName + "() {" + this.LineFeed;
 		this.Indent();
 		i = 0;
 		Program +=  this.GetIndentString() + LocalType + " " + this.GetRecvName() + " = " + "GT_New("+LocalType+");" + this.LineFeed;
 		while (i < ClassField.FieldList.size()) {
 			/*local*/GtFieldInfo FieldInfo = ClassField.FieldList.get(i);
 			/*local*/String VarName = FieldInfo.NativeName;
 			/*local*/String InitValue = this.StringfyConstValue(FieldInfo.InitValue);
 			if(!FieldInfo.Type.IsNative()) {
 				InitValue = "NULL";
 			}
 			Program += this.GetIndentString() + this.GetRecvName() + "->" + VarName + " = " + InitValue + ";" + this.LineFeed;
 			i = i + 1;
 		}
 		Program += this.GetIndentString() + "return " + this.GetRecvName() + ";" + this.LineFeed;
 		this.UnIndent();
 		Program += this.GetIndentString() + "};";
 		
 		this.WriteLineCode(Program);
 	}
 
 	@Override public void StartCompilationUnit() {
 		this.WriteLineCode("#include \"GreenTea.h\"");
 	}
 
 	@Override public String GetRecvName() {
 		return "self";
 	}
 
 }
