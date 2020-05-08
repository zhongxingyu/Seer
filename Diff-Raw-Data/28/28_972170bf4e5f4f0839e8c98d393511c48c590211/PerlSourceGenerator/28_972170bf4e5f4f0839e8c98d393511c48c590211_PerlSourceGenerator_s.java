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
 package org.GreenTeaScript;
 import java.util.ArrayList;
 //endif VAJA
 
 //GreenTea Generator should be written in each language.
 
 public class PerlSourceGenerator extends SourceGenerator {
 	PerlSourceGenerator/*constructor*/(String TargetCode, String OutputFile, int GeneratorFlag) {
 		super("perl", OutputFile, GeneratorFlag);
 		this.TrueLiteral  = "1";
 		this.FalseLiteral = "0";
 		this.NullLiteral = "undef";
 		this.LineComment = "##";
 		this.MemberAccessOperator = "->";
 		this.BreakKeyword = "last";
 		this.ContinueKeyword = "next";
 	}
 
 	public void VisitBlockEachStatementWithIndent(GtNode Node) {
 		/*local*/String Code = "{" + this.LineFeed;
 		this.Indent();
 		/*local*/GtNode CurrentNode = Node;
 		while(CurrentNode != null) {
 			CurrentNode.Evaluate(this);
 			Code += this.GetIndentString() + this.PopSourceCode() + ";" + this.LineFeed;
 			CurrentNode = CurrentNode.NextNode;
 		}
 		this.UnIndent();
 		Code += this.GetIndentString() + "}";
 		this.PushSourceCode(Code);
 	}
 
 	@Override protected final String GetNewOperator(GtType Type) {
 		return Type.ShortClassName + "->new";
 	}
 
 	@Override public void VisitWhileNode(WhileNode Node) {
 		Node.CondExpr.Evaluate(this);
 		/*local*/String Program = "while(" + this.PopSourceCode() + ")";
 		this.VisitBlockEachStatementWithIndent(Node.LoopBody);
 		Program += this.PopSourceCode();
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitDoWhileNode(DoWhileNode Node) {
		/*local*/String Program = "do";
 		this.VisitBlockEachStatementWithIndent(Node.LoopBody);
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
 
 	@Override public void VisitLocalNode(LocalNode Node) {
 		this.PushSourceCode("$" + Node.NativeName);
 	}
 
 	@Override public void VisitVarNode(VarNode Node) {
 		/*local*/String VarName = Node.NativeName;
 		/*local*/String Code = "my $" + VarName;
 		if(Node.InitNode != null) {
 			Code += " = " + this.VisitNode(Node.InitNode);
 		}
 		Code +=  ";" + this.LineFeed;
 		Code += this.GetIndentString();
 		this.VisitBlockEachStatementWithIndent(Node.BlockNode);
 		this.PushSourceCode(Code + this.PopSourceCode());
 
 	}
 
 	@Override public void VisitGetterNode(GetterNode Node) {
 		this.PushSourceCode(this.VisitNode(Node.Expr) + this.MemberAccessOperator + "{'" + Node.Func.FuncName + "'}");
 	}
 
 	@Override public void VisitIfNode(IfNode Node) {
 		/*local*/String CondExpr = this.VisitNode(Node.CondExpr);
 		this.VisitBlockEachStatementWithIndent(Node.ThenNode);
 		/*local*/String ThenBlock = this.PopSourceCode();
 		/*local*/String Code = "if(" + CondExpr + ") " + ThenBlock;
 		if(Node.ElseNode != null) {
 			this.VisitBlockEachStatementWithIndent(Node.ElseNode);
 			Code += " else " + this.PopSourceCode();
 		}
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitTryNode(TryNode Node) {
 		/*local*/String Code = "try";
 		//this.VisitEach(Node.CatchBlock);
 		this.VisitBlockEachStatementWithIndent(Node.TryBlock);
 
 		Code += this.PopSourceCode();
 		if(Node.FinallyBlock != null) {
 			this.VisitBlockEachStatementWithIndent(Node.FinallyBlock);
 			Code += " finally " + this.PopSourceCode();
 		}
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitThrowNode(ThrowNode Node) {
 		Node.Expr.Evaluate(this);
 		/*local*/String Code = "throw " + this.PopSourceCode();
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitErrorNode(ErrorNode Node) {
 		/*local*/String Code = "throw Error(\"" + Node.Token.ParsedText + "\")";
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitCommandNode(CommandNode Node) {
 		/*local*/String Code = "system(\"";
 		/*local*/int i = 0;
 		while(i < Node.Params.size()) {
 			/*local*/GtNode Param = Node.Params.get(i);
 			if(i != 0) {
 				Code += " ";
 			}
 			Param.Evaluate(this);
 			Code += this.PopSourceCode();
 			i = i + 1;
 		}
 		Code += "\")";
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void GenerateFunc(GtFunc Func, ArrayList<String> ParamNameList, GtNode Body) {
 		this.FlushErrorReport();
 		/*local*/String Program = "";
 		/*local*/String RetTy = Func.GetReturnType().ShortClassName;
 		/*local*/String FuncName = Func.GetNativeFuncName();
 		/*local*/String Signature = "# ";
 		/*local*/String Arguments = "";
 		Signature += RetTy + " " + FuncName + "(";
 		this.Indent();
 		/*local*/int i = 0;
 		while(i < ParamNameList.size()) {
 			/*local*/String ParamTy = Func.GetFuncParamType(i).ShortClassName;
 			Signature += " ," + ParamTy + " " + ParamNameList.get(i);
 			Arguments += this.GetIndentString() + "my $" + ParamNameList.get(i) + " = $_[" + i + "];" + this.LineFeed;
 			i = i + 1;
 		}
 		this.UnIndent();
 		Program += Signature + ");" + this.LineFeed + this.GetIndentString() + "sub " + FuncName + " {" + this.LineFeed;
 		this.Indent();
 		Program += Arguments + this.GetIndentString();
 		this.VisitBlockEachStatementWithIndent(Body);
 		Program += this.PopSourceCode();
 		this.UnIndent();
 		Program += this.LineFeed + this.GetIndentString() + "}";
 		this.WriteLineCode(Program);
 	}
 
 	@Override public void GenerateClassField(GtType Type, GtClassField ClassField) {
 		/*local*/String TypeName = Type.ShortClassName;
 		/*local*/String Program = this.GetIndentString() + "package " + TypeName + ";" + this.LineFeed;
 		if(Type.SuperType != null) {
 			Program += this.GetIndentString() + "# our @ISA = ('" + Type.SuperType.ShortClassName + "');" + this.LineFeed;
 		}
 		Program += this.GetIndentString() + "sub new {" + this.LineFeed;
 		this.Indent();
 		/*local*/int i = 0;
 		Program += this.GetIndentString() + "my $class = shift;" + this.LineFeed;
 		Program += this.GetIndentString() + "my $" + this.GetRecvName() + " = {};" + this.LineFeed;
 		while(i < ClassField.FieldList.size()) {
 			/*local*/GtFieldInfo FieldInfo = ClassField.FieldList.get(i);
 			/*local*/String InitValue = this.StringifyConstValue(FieldInfo.InitValue);
 			if(!FieldInfo.Type.IsNative()) {
 				InitValue = this.NullLiteral;
 			}
 			Program += this.GetIndentString() + "$" + this.GetRecvName() + "->{'" + FieldInfo.NativeName + "'} = " + InitValue + ";" + this.LineFeed;
 			i = i + 1;
 		}
 		Program += this.GetIndentString() + "return bless $" + this.GetRecvName() + ", $class" + this.LineFeed;
 		this.UnIndent();
 		Program += this.GetIndentString() + "}" + this.LineFeed;
 		Program += this.GetIndentString() + "package main;" + this.LineFeed;
 		this.WriteLineCode(Program);
 	}
 
 	@Override public Object Eval(GtNode SingleNode) {
 		SingleNode.Evaluate(this);
 		return this.PopSourceCode();
 	}
 
 	@Override public void StartCompilationUnit() {
 		this.WriteLineCode("use strict;");
 		this.WriteLineCode("use warnings;");
 	}
 
 	@Override public String GetRecvName() {
 		return "self";
 	}
 
 	@Override public void InvokeMainFunc(String MainFuncName) {
 		this.WriteLineCode(MainFuncName);
 	}
 }
