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
 package org.GreenTeaScript;
 import java.util.ArrayList;
 //endif VAJA
 
 //GreenTea Generator should be written in each language.
 
 public class BashSourceGenerator extends SourceGenerator {
 	/*field*/boolean inFunc = false;
 	/*field*/boolean inMainFunc = false;
 
 	BashSourceGenerator/*constructor*/(String TargetCode, String OutputFile, int GeneratorFlag) {
 		super(TargetCode, OutputFile, GeneratorFlag);
 		this.TrueLiteral  = "0";
 		this.FalseLiteral = "1";
 		this.NullLiteral = this.Quote("__NULL__");
 		this.MemberAccessOperator = "__MEMBER__";
 		this.LineComment = "##";
 	}
 
 	@Override public void InitContext(GtContext Context) {
 		super.InitContext(Context);
 		this.WriteLineHeader("#!/bin/bash");
 		this.WriteLineCode(this.LineFeed + "source $GREENTEA_HOME/include/bash/GreenTeaPlus.sh" + this.LineFeed);
 	}
 
 	@Override public String GenerateFuncTemplate(int ParamSize, GtFunc Func) {
 		/*local*/int BeginIdx = 1;
 		/*local*/String Template = "";
 		/*local*/boolean IsNative = false;
 		if(Func == null) {
 			Template = "$1";
 			BeginIdx = 2;
 		}
 		else if(Func.Is(NativeFunc)) {
 			Template = "$1" + this.MemberAccessOperator + Func.FuncName;
 			BeginIdx = 2;
 		}
 		else if(Func.Is(NativeMacroFunc)) {
 			Template = Func.GetNativeMacro();
 			IsNative = true;
 		}
 		else {
 			Template = Func.GetNativeFuncName();
 		}
 		/*local*/int i = BeginIdx;
 		if(IsNative == false) {
 			while(i < ParamSize) {
 				Template += " $" + i;
 				i = i + 1;
 			}
 		}
 		return Template;
 	}
 
 	private String VisitBlockWithIndent(GtNode Node, boolean allowDummyBlock) {
 		return this.VisitBlockWithOption(Node, true, allowDummyBlock, false);
 	}
 
 	private String VisitBlockWithSkipJump(GtNode Node, boolean allowDummyBlock) {
 		return this.VisitBlockWithOption(Node, true, allowDummyBlock, true);
 	}
 
 	private String VisitBlockWithoutIndent(GtNode Node, boolean allowDummyBlock) {
 		return this.VisitBlockWithOption(Node, false, allowDummyBlock, false);
 	}
 
 	private String VisitBlockWithOption(GtNode Node, boolean inBlock, boolean allowDummyBlock, boolean skipJump) {
 		/*local*/String Code = "";
 		if(inBlock) {
 			this.Indent();
 		}
 		/*local*/GtNode CurrentNode = Node;
 		if(this.IsEmptyBlock(Node) && allowDummyBlock) {
 			Code += this.GetIndentString() + "echo dummy block!! &> /dev/zero" + this.LineFeed;
 		}
 		while(!this.IsEmptyBlock(CurrentNode)) {
 			/*local*/String poppedCode = this.VisitNode(CurrentNode);
 			if(skipJump && (CurrentNode instanceof BreakNode || CurrentNode instanceof ContinueNode)) {
 				poppedCode = "echo skip jump code $> /dev/zero";
 			}
 			if(!LibGreenTea.EqualsString(poppedCode, "")) {
 				Code += this.GetIndentString() + poppedCode + this.LineFeed;
 			}
 			CurrentNode = CurrentNode.NextNode;
 		}
 		if(inBlock) {
 			this.UnIndent();
 			Code += this.GetIndentString();
 		}
 		else {
 			if(Code.length() > 0) {
 				Code = Code.substring(0, Code.length() - 1);
 			}
 		}
 		return Code;
 	}
 
 	private String Quote(String Value) {
 		return "\"" + Value  + "\"";
 	}
 
 	public GtNode CreateDoWhileNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Cond, GtNode Block) {
 		/*
 		 * do { Block } while(Cond)
 		 * => while(True) { Block; if(!Cond) { break; } }
 		 */
 		/*local*/GtNode Break = this.CreateBreakNode(Type, ParsedTree, null);
		/*local*/GtPolyFunc PolyFunc = ParsedTree.NameSpace.GetGreenMethod(Cond.Type, "!", true);
 		/*local*/GtTypeEnv Gamma = new GtTypeEnv(ParsedTree.NameSpace);
 		/*local*/GtFunc Func = null;
 		if(PolyFunc != null) {
 			Func = PolyFunc.ResolveUnaryFunc(Gamma, ParsedTree, Cond);
 		}
 		Cond = this.CreateUnaryNode(Type, ParsedTree, Func, Cond);
 		/*local*/GtNode IfBlock = this.CreateIfNode(Type, ParsedTree, Cond, Break, null);
 		GtStatic.LinkNode(Block.MoveTailNode(), IfBlock);
 		/*local*/GtNode TrueNode = this.CreateConstNode(ParsedTree.NameSpace.Context.BooleanType, ParsedTree, true);
 		return this.CreateWhileNode(Type, ParsedTree, TrueNode, Block);
 	}
 
 	private String ResolveCondition(GtNode Node) {
 		/*local*/String Cond = this.VisitNode(Node);
 		if(LibGreenTea.EqualsString(Cond, "0")) {
 			Cond = "((1 == 1))";
 		}
 		else if(LibGreenTea.EqualsString(Cond, "1")) {
 			Cond = "((1 != 1))";
 		}
 		return Cond;
 	}
 
 	@Override public void VisitWhileNode(WhileNode Node) {
 		/*local*/String Program = "while " + this.ResolveCondition(Node.CondExpr) + " ;do" + this.LineFeed;
 		Program += this.VisitBlockWithIndent(Node.LoopBody, true) + "done";
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitForNode(ForNode Node) {
 		/*local*/String Cond = this.ResolveCondition(Node.CondExpr);
 		/*local*/String Iter = this.VisitNode(Node.IterExpr);
 		/*local*/String Program = "for((; " + Cond  + "; " + Iter + " )) ;do" + this.LineFeed;
 		Program += this.VisitBlockWithIndent(Node.LoopBody, true) + "done";
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitForEachNode(ForEachNode Node) {
 		/*local*/String Variable = this.VisitNode(Node.Variable);
 		/*local*/String Iter = this.VisitNode(Node.IterExpr);
 		/*local*/String Program = "for " + Variable + " in " + "${" + Iter + "[@]} ;do" + this.LineFeed;
 		Program += this.VisitBlockWithIndent(Node.LoopBody, true) + "done";
 		this.PushSourceCode(Program);
 	}
 
 	private String[] MakeParamCode(ArrayList<GtNode> ParamList) {
 		/*local*/int Size = LibGreenTea.ListSize(ParamList);
 		/*local*/String[] ParamCode = new String[Size - 1];
 		/*local*/int i = 1;
 		while(i < Size) {
 			ParamCode[i - 1] = this.ResolveValueType(ParamList.get(i), false);
 			i = i + 1;
 		}
 		return ParamCode;
 	}
 
 	private String CreateAssertFunc(ApplyNode Node) {
 		/*local*/GtNode ParamNode = Node.NodeList.get(1);
 		return "assert " + this.Quote(this.ResolveCondition(ParamNode));
 	}
 
 	@Override public void VisitApplyNode(ApplyNode Node) {
 		/*local*/String[] ParamCode = this.MakeParamCode(Node.NodeList);
 		if(Node.Func == null) {
 			this.PushSourceCode(this.JoinCode(ParamCode[0] + " ", 0, ParamCode, "", " "));
 		}
 //		else if(Node.Func.Is(NativeFunc)) {
 //			this.PushSourceCode(this.JoinCode(ParamCode[0] + "." + Node.Func.FuncName + " ", 0, ParamCode, ""));
 //		}
 		else if(Node.Func.Is(NativeMacroFunc)) {
 			/*local*/String NativeMacro = Node.Func.GetNativeMacro();
 			if(LibGreenTea.EqualsString(NativeMacro, "assert $1")) {
 				this.PushSourceCode(this.CreateAssertFunc(Node));
 				return;
 			}
 			this.PushSourceCode(this.ApplyMacro2(NativeMacro, ParamCode));
 		}
 		else {
 			this.PushSourceCode(this.JoinCode(Node.Func.GetNativeFuncName() + " ", 0, ParamCode, "", " "));
 		}
 	}
 
 	@Override public void VisitUnaryNode(UnaryNode Node) {
 		/*local*/String FuncName = Node.Token.ParsedText;
 		/*local*/GtFunc Func = Node.Func;
 		/*local*/String Expr = this.ResolveValueType(Node.Expr, false);	//TODO: support ++ --
 		/*local*/String Macro = null;
 		if(Func != null) {
 			FuncName = Func.GetNativeFuncName();
 			if(IsFlag(Func.FuncFlag, NativeMacroFunc)) {
 				Macro = Func.GetNativeMacro();
 			}
 		}
 		if(Macro == null) {
 			Macro = "((" + FuncName + " $1))";
 		}
 		this.PushSourceCode(Macro.replace("$1", Expr));
 	}
 
 	@Override public void VisitBinaryNode(BinaryNode Node) {
 		/*local*/String FuncName = Node.Token.ParsedText;
 		/*local*/GtFunc Func = Node.Func;
 		/*local*/String Left = this.ResolveValueType(Node.LeftNode, false);
 		/*local*/String Right = this.ResolveValueType(Node.RightNode, false);
 		/*local*/String Macro = null;
 		if(Func != null) {
 			FuncName = Func.GetNativeFuncName();
 			if(IsFlag(Func.FuncFlag, NativeMacroFunc)) {
 				Macro = Func.GetNativeMacro();
 			}
 		}
 		if(Macro == null) {
 			Macro = "(($1 " + FuncName + " $2))";
 		}
 		this.PushSourceCode(Macro.replace("$1", Left).replace("$2", Right));
 	}
 
 	private String GetMemberIndex(GtType ClassType, String MemberName) {
 		return "$" + ClassType.ShortClassName + this.MemberAccessOperator + MemberName;
 	}
 
 	private boolean IsNativeType(GtType Type) {
 		if(Type != null && Type.IsNative()) {
 			return true;
 		}
 		return false;
 	}
 
 	@Override public void VisitGetterNode(GetterNode Node) {
 		this.PushSourceCode(this.VisitNode(Node.Expr) + "[" + this.GetMemberIndex(Node.Expr.Type, Node.Func.FuncName) + "]");
 	}
 
 	@Override public void VisitIndexerNode(IndexerNode Node) {
 		this.PushSourceCode(this.VisitNode(Node.Expr) + "[" + this.ResolveValueType(Node.GetAt(0), false) + "]");
 	}
 
 	@Override public void VisitAndNode(AndNode Node) {
 		this.PushSourceCode("(" + this.ResolveCondition(Node.LeftNode) + " && " + this.ResolveCondition(Node.RightNode) + ")");
 	}
 
 	@Override public void VisitOrNode(OrNode Node) {
 		this.PushSourceCode("(" + this.ResolveCondition(Node.LeftNode) + " || " + this.ResolveCondition(Node.RightNode) + ")");
 	}
 
 	@Override public void VisitAssignNode(AssignNode Node) {
 		this.PushSourceCode(this.VisitNode(Node.LeftNode) + "=" + this.ResolveValueType(Node.RightNode, true));
 	}
 
 	@Override public void VisitVarNode(VarNode Node) {
 		/*local*/String VarName = Node.NativeName;
 		/*local*/String Declare = "declare ";
 		/*local*/String Option = "";
 		if(this.inFunc) {
 			Declare = "local ";
 		}
 		if(this.IsNativeType(Node.DeclType)) {
 			Option = "-a ";
 		}
 		
 		/*local*/String Code = Declare + Option + VarName + this.LineFeed;
 		Code += this.GetIndentString() + VarName;
 		if(Node.InitNode != null) {
 			Code += "=" + this.ResolveValueType(Node.InitNode, true);
 		} 
 		Code +=  this.LineFeed;
 		this.PushSourceCode(Code + this.VisitBlockWithoutIndent(Node.BlockNode, false));
 	}
 
 	@Override public void VisitTrinaryNode(TrinaryNode Node) {
 		/*local*/String CondExpr = this.ResolveCondition(Node.CondExpr);
 		/*local*/String Then = this.ResolveValueType(Node.ThenExpr, false);
 		/*local*/String Else = this.ResolveValueType(Node.ElseExpr, false);
 		this.PushSourceCode("((" + CondExpr + " ? " + Then + " : " + Else + "))");
 	}
 
 	@Override public void VisitIfNode(IfNode Node) {
 		/*local*/String CondExpr = this.ResolveCondition(Node.CondExpr);
 		/*local*/String ThenBlock = this.VisitBlockWithIndent(Node.ThenNode, true);
 		/*local*/String Code = "if " + CondExpr + " ;then" + this.LineFeed + ThenBlock;
 		if(!this.IsEmptyBlock(Node.ElseNode)) {
 			Code += "else" + this.LineFeed + this.VisitBlockWithIndent(Node.ElseNode, false);
 		}
 		Code += "fi";
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitSwitchNode(SwitchNode Node) {
 		/*local*/String Code = "case " + this.ResolveValueType(Node.MatchNode, false) + " in";
 		/*local*/int i = 0;
 		while(i < Node.CaseList.size()) {
 			/*local*/GtNode Case  = Node.CaseList.get(i);
 			/*local*/GtNode Block = Node.CaseList.get(i+1);
 			Code += this.LineFeed + this.GetIndentString() + this.VisitNode(Case) + ")" + this.LineFeed;
 			Code += this.VisitBlockWithSkipJump(Block, true) + ";;";
 			i = i + 2;
 		}
 		if(Node.DefaultBlock != null) {
 			Code += this.LineFeed + this.GetIndentString() + "*)" + this.LineFeed;
 			Code += this.VisitBlockWithSkipJump(Node.DefaultBlock, false) + ";;";
 		}
 		Code += this.LineFeed + this.GetIndentString() + "esac";
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitReturnNode(ReturnNode Node) {
 		if(!this.inFunc) {
 			return;
 		}
 		
 		if(Node.Expr != null) {
 			/*local*/String Ret = this.ResolveValueType(Node.Expr, false);
 			if(Node.Type.equals(Node.Type.Context.BooleanType) || 
 					(Node.Type.equals(Node.Type.Context.IntType) && this.inMainFunc)) {
 				this.PushSourceCode("return " + Ret);
 				return;
 			}
 			this.PushSourceCode("echo " + Ret + this.LineFeed + this.GetIndentString() + "return 0");
 			return;
 		}
 		this.PushSourceCode("return 0");
 	}
 
 	@Override public void VisitTryNode(TryNode Node) {
 		/*local*/GtNode TrueNode = new ConstNode(Node.Type.Context.BooleanType, null, true);
 		/*local*/String Code = "trap ";
 		/*local*/String Try = this.VisitNode(new IfNode(null, null, TrueNode, Node.TryBlock, null));
 		/*local*/String Catch = this.VisitNode(new IfNode(null, null, TrueNode, Node.CatchBlock, null));
 		Code += this.Quote(Catch) + " ERR" + this.LineFeed;
 		Code += this.GetIndentString() + Try + this.LineFeed + this.GetIndentString() + "trap ERR";
 		if(Node.FinallyBlock != null) {
 			/*local*/String Finally = this.VisitNode(new IfNode(null, null, TrueNode, Node.FinallyBlock, null));
 			Code += this.LineFeed + this.GetIndentString() + Finally;
 		}
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitThrowNode(ThrowNode Node) {
 		this.PushSourceCode("kill &> /dev/zero");
 	}
 
 	@Override public void VisitErrorNode(ErrorNode Node) {
 		this.PushSourceCode("echo " + this.Quote(Node.Token.ParsedText) + " >&2");
 	}
 
 	@Override public void VisitCommandNode(CommandNode Node) {
 		/*local*/String Code = "";
 		/*local*/GtType Type = Node.Type;
 		/*local*/CommandNode CurrentNode = Node;
 		while(CurrentNode != null) {
 			Code += this.AppendCommand(CurrentNode);
 			CurrentNode = (/*cast*/CommandNode) CurrentNode.PipedNextNode;
 		}
 		
 		if(Type.equals(Type.Context.StringType)) {
 			Code = "execCommadString " + this.Quote(Code);
 		}
 		else if(Type.equals(Type.Context.BooleanType)) {
 			Code = "execCommadBool " + this.Quote(Code);
 		}
 		this.PushSourceCode(Code);
 	}
 
 	private String AppendCommand(CommandNode CurrentNode) {
 		/*local*/String Code = "";
 		/*local*/int size = CurrentNode.Params.size();
 		/*local*/int i = 0;
 		while(i < size) {
 			Code += this.ResolveValueType(CurrentNode.Params.get(i), false) + " ";
 			i = i + 1;
 		}
 		return Code;
 	}
 
 	private GtNode ResolveParamName(GtFunc Func, ArrayList<String> ParamNameList, GtNode Body) {
 		return this.ConvertParamName(Func, ParamNameList, Body, 0);
 	}
 
 	private GtNode ConvertParamName(GtFunc Func, ArrayList<String> ParamNameList, GtNode Body, int index) {
 		if(ParamNameList == null || index == ParamNameList.size()) {
 			return Body;
 		}
 		
 		/*local*/GtType ParamType = Func.GetFuncParamType(index);
 		/*local*/GtNode oldVarNode = new LocalNode(ParamType, null, "" + (index + 1));
 		/*local*/GtNode Let = new VarNode(null, null, ParamType, ParamNameList.get(index), oldVarNode, null);
 		index += 1;
 		Let.NextNode = this.ConvertParamName(Func, ParamNameList, Body, index);
 		return Let;
 	}
 
 	private boolean CheckConstFolding(GtNode TargetNode) {
 		if(TargetNode instanceof ConstNode) {
 			return true;
 		}
 		else if(TargetNode instanceof UnaryNode) {
 			/*local*/UnaryNode Unary = (/*cast*/UnaryNode) TargetNode;
 			return this.CheckConstFolding(Unary.Expr);
 		}
 		else if(TargetNode instanceof BinaryNode) {
 			/*local*/BinaryNode Binary = (/*cast*/BinaryNode) TargetNode;
 			if(this.CheckConstFolding(Binary.LeftNode) && this.CheckConstFolding(Binary.RightNode)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private String ResolveValueType(GtNode TargetNode, boolean isAssign) {
 		/*local*/String ResolvedValue;
 		/*local*/String Value = this.VisitNode(TargetNode);
 		/*local*/GtType Type = TargetNode.Type;
 		
 		// resolve constant folding
 		if(this.CheckConstFolding(TargetNode)) {
 			return Value;
 		}
 		
 		// resolve boolean function
 		if(Type != null && Type.equals(Type.Context.BooleanType)) {
 			if(TargetNode instanceof ApplyNode || TargetNode instanceof UnaryNode || 
 					TargetNode instanceof CommandNode || TargetNode instanceof BinaryNode) {
 				return "$(valueOfBool " + this.Quote(Value) + ")";
 			}
 		}
 		
 		if(TargetNode instanceof ConstNode || TargetNode instanceof NullNode) {
 			return Value;
 		}
 		else if(TargetNode instanceof IndexerNode || TargetNode instanceof GetterNode) {
 			ResolvedValue = "${" + Value + "}";
 		}
 		else if(TargetNode instanceof ApplyNode || TargetNode instanceof CommandNode || TargetNode instanceof NewNode) {
 			ResolvedValue = "$(" + Value + ")";
 		}
 		else if(TargetNode instanceof LocalNode && !this.IsNativeType(Type)) {
 			/*local*/LocalNode Local = (/*cast*/LocalNode) TargetNode;
 			/*local*/String Name = Local.NativeName;
 			ResolvedValue = "${" + Value + "[@]}";
 			if(Name.length() == 1 && LibGreenTea.IsDigit(Name, 0)) {
 				ResolvedValue = "$" + Value;
 			}
 		}
 		else {
 			ResolvedValue = "$" + Value;
 		}
 		
 		// resolve assigned object
 		if(isAssign) {
 			if(!this.IsNativeType(Type)) {
 				ResolvedValue = "(" + ResolvedValue + ")";
 				return ResolvedValue;
 			}
 		}
 		
 		// resolve string and object value
 		if(Type != null) {
 			if(Type.equals(Type.Context.StringType) || !this.IsNativeType(Type)) {
 				ResolvedValue = this.Quote(ResolvedValue);
 			}
 		}
 		return ResolvedValue;
 	}
 
 	@Override public void GenerateFunc(GtFunc Func, ArrayList<String> ParamNameList, GtNode Body) {
 		this.FlushErrorReport();
 		/*local*/String Function = "";
 		/*local*/String FuncName = Func.GetNativeFuncName();
 		this.inFunc = true;
 		if(LibGreenTea.EqualsString(FuncName, "main")) {
 			this.inMainFunc = true;
 		}
 		Function += FuncName + "() {" + this.LineFeed;
 		/*local*/String Block = this.VisitBlockWithIndent(this.ResolveParamName(Func, ParamNameList, Body), true);
 		Function += Block + "}" + this.LineFeed;
 		this.WriteLineCode(Function);
 		this.inFunc = false;
 		this.inMainFunc = false;
 	}
 
 	@Override protected String GetNewOperator(GtType Type) {
 		return this.Quote("$(__NEW__" + Type.ShortClassName + ")");
 	}
 
 	@Override public void GenerateClassField(GtType Type, GtClassField ClassField) {	//TODO: support super
 		/*local*/String Program = "__NEW__" + Type.ShortClassName + "() {" + this.LineFeed;
 		this.WriteLineCode("#### define class " + Type.ShortClassName + " ####");
 		this.Indent();
 		Program += this.GetIndentString() + "local -a " + this.GetRecvName() + this.LineFeed;
 
 		/*local*/int i = 0;
 		while(i < ClassField.FieldList.size()) {
 			/*local*/GtFieldInfo FieldInfo = ClassField.FieldList.get(i);
 			/*local*/String InitValue = this.StringifyConstValue(FieldInfo.InitValue);
 			if(!FieldInfo.Type.IsNative()) {
 				InitValue = "NULL";
 			}
 			this.WriteLineCode(Type.ShortClassName + this.MemberAccessOperator + FieldInfo.NativeName + "=" + i);
 			
 			Program += this.GetIndentString() + this.GetRecvName();
 			Program += "[" + this.GetMemberIndex(Type, FieldInfo.NativeName) + "]=" + InitValue + this.LineFeed;
 			i = i + 1;
 		}
 		Program += this.GetIndentString() + "echo ";
 		Program += this.Quote("${" + this.GetRecvName() + "[@]}") + this.LineFeed;
 		this.UnIndent();
 		Program += "}";
 		
 		this.WriteLineCode("\n" + Program);
 	}
 
 	@Override public Object Eval(GtNode Node) {
 		/*local*/String Code = this.VisitBlockWithoutIndent(Node, false);
 		if(!LibGreenTea.EqualsString(Code, "")) {
 			this.WriteLineCode(Code);
 		}
 		return Code;
 	}
 
 	@Override public void InvokeMainFunc(String MainFuncName) {
 		this.WriteLineCode(MainFuncName);
 	}
 }
