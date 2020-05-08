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
 
 public class CSourceGenerator extends GtSourceGenerator {
 	CSourceGenerator/*constructor*/(String TargetCode, String OutputFile, int GeneratorFlag) {
 		super(TargetCode, OutputFile, GeneratorFlag);
 		this.TrueLiteral  = "1";
 		this.FalseLiteral = "0";
 		this.Tab = "\t";
 		this.NullLiteral = "NULL";
 	}
 
 	@Override public void InitContext(GtParserContext Context) {
 		super.InitContext(Context);
 		this.HeaderBuilder.AppendLine("#include \"GreenTeaPlus.h\"");
 	}
 
 	@Override public String GetRecvName() {
 		return "self";
 	}
 
 	private String GetLocalType(GtType Type, boolean IsPointer) {
 		if(Type.IsDynamicType() || Type.IsNativeType()) {
 			if(Type.IsBooleanType()) {
 				return "int";
 			}
 			return Type.ShortName;
 		}
 		/*local*/String TypeName = "struct " + Type.ShortName;
 		if(IsPointer) {
 			TypeName += "*";
 		}
 		return TypeName;
 
 	}
 	public String NativeTypeName(GtType Type) {
 		return this.GetLocalType(Type, false);
 	}
 
 	public String LocalTypeName(GtType Type) {
 		return this.GetLocalType(Type, true);
 	}
 
 	public String GtTypeName(GtType Type) {
 		return Type.ShortName;
 	}
 
 	@Override public void VisitNullNode(GtNullNode Node) {
		this.VisitingBuilder.Append(this.NullLiteral);
 	}
 
 	@Override public void VisitBooleanNode(GtBooleanNode Node) {
		if(Node.Value) {
			this.VisitingBuilder.Append(this.TrueLiteral);
		}
		else {
			this.VisitingBuilder.Append(this.FalseLiteral);
		}
 	}
 
 	@Override public void VisitIntNode(GtIntNode Node) {
 		this.VisitingBuilder.Append(Long.toString(Node.Value));
 	}
 
 	@Override public void VisitFloatNode(GtFloatNode Node) {
 		this.VisitingBuilder.Append(Double.toString(Node.Value));
 	}
 
 	@Override public void VisitStringNode(GtStringNode Node) {
 		this.VisitingBuilder.Append(LibGreenTea.QuoteString(Node.Value));
 	}
 
 	//FIXME Need to Implement
 //	@Override public void VisitRegexNode(GtRegexNode Node) {
 //		this.VisitingBuilder.Append("");
 //	}
 //
 //	@Override public void VisitConstPoolNode(GtConstPoolNode Node) {
 //		this.VisitingBuilder.Append("");
 //	}
 //
 //	@Override public void VisitArrayLiteralNode(GtArrayLiteralNode Node) {
 //		this.VisitingBuilder.Append("");
 //	}
 //
 //	@Override public void VisitMapLiteralNode(GtMapLiteralNode Node) {
 //		this.VisitingBuilder.Append("");
 //	}
 
 	@Override public void VisitParamNode(GtParamNode Node) {
 		this.VisitingBuilder.Append("");
 	}
 
 //	@Override public void VisitFunctionLiteralNode(GtFunctionLiteralNode Node) {
 //		this.VisitingBuilder.Append("");
 //	}
 
 	@Override public void VisitGetLocalNode(GtGetLocalNode Node) {
 		this.VisitingBuilder.Append(Node.NativeName);
 	}
 
 	@Override public void VisitSetLocalNode(GtSetLocalNode Node) {
 		this.VisitingBuilder.Append(Node.NativeName + " = ");
 		Node.ValueNode.Accept(this);
 	}
 
 	@Override public void VisitGetCapturedNode(GtGetCapturedNode Node) {
 		this.VisitingBuilder.Append("__env->" + Node.NativeName);
 	}
 
 	@Override public void VisitSetCapturedNode(GtSetCapturedNode Node) {
 		this.VisitingBuilder.Append("__env->" + Node.NativeName + " = ");
 		Node.ValueNode.Accept(this);
 	}
 
 	@Override public void VisitGetterNode(GtGetterNode Node) {
 		/*local*/String FieldName = Node.NativeName;
 		/*local*/GtType RecvType = Node.ResolvedFunc.GetRecvType();
 		this.VisitingBuilder.Append("GT_GetField(" + this.LocalTypeName(RecvType) + ", ");
 		Node.RecvNode.Accept(this);
 		this.VisitingBuilder.Append(", " + FieldName + ")");
 	}
 
 	@Override public void VisitSetterNode(GtSetterNode Node) {
 		/*local*/String FieldName = Node.NativeName;
 		/*local*/GtType RecvType = Node.ResolvedFunc.GetRecvType();
 		this.VisitingBuilder.Append("GT_SetField(" + this.LocalTypeName(RecvType) + ", ");
 		Node.RecvNode.Accept(this);
 		this.VisitingBuilder.Append(", " + FieldName + ", ");
 		Node.ValueNode.Accept(this);
 		this.VisitingBuilder.Append(")");
 	}
 
 	@Override public void VisitApplySymbolNode(GtApplySymbolNode Node) {
 		this.VisitingBuilder.Append(Node.NativeName);
 		this.VisitingBuilder.Append("(");
 		for(/*local*/int i = 0; i < LibGreenTea.ListSize(Node.ParamList); i++){
 			if(i > 0){
 				this.VisitingBuilder.Append(", ");
 			}
 			Node.ParamList.get(i).Accept(this);
 		}
 		this.VisitingBuilder.Append(")");
 	}
 
 //	@Override public void VisitApplyFunctionObjectNode(GtApplyFunctionObjectNode Node) {
 //		this.VisitingBuilder.Append("");
 //	}
 //
 //	@Override public void VisitApplyOverridedMethodNode(GtApplyOverridedMethodNode Node) {
 //		this.VisitingBuilder.Append("");
 //	}
 
 	@Override public void VisitGetIndexNode(GtGetIndexNode Node) {
 		Node.RecvNode.Accept(this);
 		this.VisitingBuilder.Append("[");
 		Node.IndexNode.Accept(this);
 		this.VisitingBuilder.Append("]");
 	}
 
 	@Override public void VisitSetIndexNode(GtSetIndexNode Node) {
 		Node.RecvNode.Accept(this);
 		this.VisitingBuilder.Append("[");
 		Node.IndexNode.Accept(this);
 		this.VisitingBuilder.Append("] = ");
 		Node.ValueNode.Accept(this);
 	}
 
 //	@Override public void VisitSliceNode(GtSliceNode Node) {
 //		this.VisitingBuilder.Append("");
 //	}
 
 	@Override public void VisitAndNode(GtAndNode Node) {
 		this.VisitingBuilder.Append("(");
 		Node.LeftNode.Accept(this);
 		this.VisitingBuilder.Append(" && ");
 		Node.RightNode.Accept(this);
 		this.VisitingBuilder.Append(")");
 	}
 
 	@Override public void VisitOrNode(GtOrNode Node) {
 		this.VisitingBuilder.Append("(");
 		Node.LeftNode.Accept(this);
 		this.VisitingBuilder.Append(" || ");
 		Node.RightNode.Accept(this);
 		this.VisitingBuilder.Append(")");
 	}
 
 	@Override public void VisitUnaryNode(GtUnaryNode Node) {
 		this.VisitingBuilder.Append("(");
 		this.VisitingBuilder.Append(Node.NativeName);
 		Node.RecvNode.Accept(this);
 		this.VisitingBuilder.Append(")");
 	}
 
 	@Override public void VisitPrefixInclNode(GtPrefixInclNode Node) {
 		this.VisitingBuilder.Append("(++");
 		Node.RecvNode.Accept(this);
 		this.VisitingBuilder.Append(")");
 	}
 
 	@Override public void VisitPrefixDeclNode(GtPrefixDeclNode Node) {
 		this.VisitingBuilder.Append("(--");
 		Node.RecvNode.Accept(this);
 		this.VisitingBuilder.Append(")");
 	}
 
 	@Override public void VisitSuffixInclNode(GtSuffixInclNode Node) {
 		this.VisitingBuilder.Append("(");
 		Node.RecvNode.Accept(this);
 		this.VisitingBuilder.Append("++)");
 	}
 
 	@Override public void VisitSuffixDeclNode(GtSuffixDeclNode Node) {
 		this.VisitingBuilder.Append("(");
 		Node.RecvNode.Accept(this);
 		this.VisitingBuilder.Append("--)");
 	}
 
 	@Override public void VisitBinaryNode(GtBinaryNode Node) {
 		this.VisitingBuilder.Append("(");
 		Node.LeftNode.Accept(this);
 		this.VisitingBuilder.Append(" " + Node.NativeName + " ");
 		Node.RightNode.Accept(this);
 		this.VisitingBuilder.Append(")");
 	}
 
 	@Override public void VisitTrinaryNode(GtTrinaryNode Node) {
 		this.VisitingBuilder.Append("(");
 		Node.CondNode.Accept(this);
 		this.VisitingBuilder.Append(") ? (");
 		Node.ThenNode.Accept(this);
 		this.VisitingBuilder.Append(") : (");
 		Node.ElseNode.Accept(this);
 		this.VisitingBuilder.Append(")");
 	}
 
 //	@Override public void VisitConstructorNode(GtConstructorNode Node) {
 //		this.VisitingBuilder.Append("");
 //	}
 //
 //	@Override public void VisitAllocateNode(GtAllocateNode Node) {
 //		this.VisitingBuilder.Append("");
 //	}
 //
 //	@Override public void VisitNewArrayNode(GtNewArrayNode Node) {
 //		this.VisitingBuilder.Append("");
 //	}
 //
 //	@Override public void VisitInstanceOfNode(GtInstanceOfNode Node) {
 //		this.VisitingBuilder.Append("");
 //	}
 //
 //	@Override public void VisitCastNode(GtCastNode Node) {
 //		this.VisitingBuilder.Append("");
 //	}
 
 	@Override public void VisitVarDeclNode(GtVarDeclNode Node) {
 		/*local*/String Type = this.LocalTypeName(Node.DeclType);
 		/*local*/String VarName = Node.NativeName;
 		/*local*/String Code = Type + " " + VarName;
 		this.VisitingBuilder.Append(Code);
 		if(Node.InitNode != null) {
 			this.VisitingBuilder.Append(" = ");
 			Node.InitNode.Accept(this);
 		}
 		this.VisitingBuilder.AppendLine(this.SemiColon);
 		this.VisitingBuilder.AppendIndent();
 		this.VisitIndentBlock("{", Node.BlockNode, "}");
 	}
 
 	@Override public void VisitUsingNode(GtUsingNode Node) {
 		throw new RuntimeException("FIXME");
 	}
 
 	@Override public void VisitIfNode(GtIfNode Node) {
 		this.VisitingBuilder.Append("if(");
 		Node.CondNode.Accept(this);
 		this.VisitingBuilder.Append(")");
 		this.VisitIndentBlock("{", Node.ThenNode, "}");
 		if(Node.ElseNode != null) {
 			this.VisitingBuilder.Append("else");
 			this.VisitIndentBlock("{", Node.ElseNode, "}");
 		}
 	}
 
 	@Override public void VisitWhileNode(GtWhileNode Node) {
 		this.VisitingBuilder.Append("while(");
 		Node.CondNode.Accept(this);
 		this.VisitingBuilder.Append(")");
 		this.VisitIndentBlock("{", Node.BodyNode, "}");
 	}
 
 	@Override public void VisitDoWhileNode(GtDoWhileNode Node) {
 		this.VisitingBuilder.Append("do ");
 		this.VisitIndentBlock("{", Node.BodyNode, "}");
 		this.VisitingBuilder.Append("while(");
 		Node.CondNode.Accept(this);
 		this.VisitingBuilder.Append(")");
 	}
 
 	@Override public void VisitForNode(GtForNode Node) {
 		this.VisitingBuilder.Append("for(;");
 		Node.CondNode.Accept(this);
 		this.VisitingBuilder.Append(";");
 		Node.IterNode.Accept(this);
 		this.VisitingBuilder.Append(") ");
 		this.VisitIndentBlock("{", Node.BodyNode, "}");
 	}
 
 	@Override public void VisitForEachNode(GtForEachNode Node) {
 		this.VisitingBuilder.Append("");
 	}
 
 	@Override public void VisitContinueNode(GtContinueNode Node) {
 		this.VisitingBuilder.Append("continue");
 		if(Node.Label != null) {
 			this.VisitingBuilder.Append(" " + Node.Label);
 		}
 	}
 
 	@Override public void VisitBreakNode(GtBreakNode Node) {
 		this.VisitingBuilder.Append("break");
 		if(Node.Label != null) {
 			this.VisitingBuilder.Append(" " + Node.Label);
 		}
 	}
 
 	@Override public void VisitStatementNode(GtStatementNode Node) {
 		Node.ValueNode.Accept(this);
 	}
 
 	@Override public void VisitReturnNode(GtReturnNode Node) {
 		this.VisitingBuilder.Append("return ");
 		if(Node.ValueNode != null) {
 			Node.ValueNode.Accept(this);
 		}
 		this.StopVisitor(Node);
 	}
 
 	@Override public void VisitYieldNode(GtYieldNode Node) {
 		this.VisitingBuilder.Append("yield ");
 		Node.ValueNode.Accept(this);
 		this.StopVisitor(Node);
 	}
 
 	@Override public void VisitThrowNode(GtThrowNode Node) {
 		this.VisitingBuilder.Append("throw ");
 		Node.ValueNode.Accept(this);
 		this.StopVisitor(Node);
 	}
 
 	@Override public void VisitTryNode(GtTryNode Node) {
 		this.VisitingBuilder.Append("try ");
 		this.VisitIndentBlock("{", Node.TryNode, "}");
 		for (int i = 0; i < LibGreenTea.ListSize(Node.CatchList); i++) {
 			Node.CatchList.get(i).Accept(this);
 		}
 		if(Node.FinallyNode != null) {
 			this.VisitingBuilder.Append("finally ");
 			this.VisitIndentBlock("{", Node.FinallyNode, "}");
 		}
 	}
 
 	@Override public void VisitCatchNode(GtCatchNode Node) {
 		this.VisitingBuilder.Append(" catch (" + Node.ExceptionType);
 		this.VisitingBuilder.Append(" " + Node.ExceptionName + ") ");
 		this.VisitIndentBlock("{", Node.BodyNode, "}");
 	}
 
 	@Override public void VisitSwitchNode(GtSwitchNode Node) {
 		this.VisitingBuilder.Append("switch (");
 		Node.MatchNode.Accept(this);
 		this.VisitingBuilder.AppendLine(") {");
 		for (/*local*/int i = 0; i < LibGreenTea.ListSize(Node.CaseList); i++) {
 			Node.CaseList.get(i).Accept(this);
 		}
 		this.VisitingBuilder.AppendLine("}");
 	}
 
 	@Override public void VisitCaseNode(GtCaseNode Node) {
 		this.VisitingBuilder.Append("case ");
 		Node.CaseNode.Accept(this);
 		this.VisitingBuilder.Append(" : ");
 		this.VisitIndentBlock("{", Node.BodyNode, "}");
 	}
 
 	@Override public void VisitCommandNode(GtCommandNode Node) {
 		this.VisitingBuilder.Append("String __Command = ");
 		for(/*local*/int i = 0; i < LibGreenTea.ListSize(Node.ArgumentList); i += 1) {
 			/*local*/GtNode Param = Node.ArgumentList.get(i);
 			if(i != 0) {
 				this.VisitingBuilder.Append(" + ");
 			}
 			this.VisitingBuilder.Append("(");
 			Param.Accept(this);
 			this.VisitingBuilder.Append(")");
 		}
 		this.VisitingBuilder.AppendLine(this.SemiColon);
 		this.VisitingBuilder.AppendIndent();
 		this.VisitingBuilder.Append("system(__Command)");
 	}
 
 	@Override public void VisitErrorNode(GtErrorNode Node) {
 		/*local*/String Code = "throw Error(\"" + Node.Token.ParsedText + "\")";
 		this.VisitingBuilder.Append(Code);
 		this.StopVisitor(Node);
 	}
 
 	@Override public void GenerateFunc(GtFunc Func, ArrayList<String> ParamNameList, GtNode Body) {
 		this.FlushErrorReport();
 		/*local*/GtSourceBuilder Builder = this.NewSourceBuilder();
 		/*local*/GtSourceBuilder PrevBuilder = this.VisitingBuilder;
 		this.VisitingBuilder = Builder;
 
 		/*local*/String Code = "";
 		if(!Func.Is(ExportFunc)) {
 			Code = "static ";
 		}
 		//Body = this.Opt.Fold(Body);
 		/*local*/String RetTy = this.LocalTypeName(Func.GetReturnType());
 		Code += RetTy + " " + Func.GetNativeFuncName() + "(";
 		/*local*/int i = 0;
 		while(i < ParamNameList.size()) {
 			/*local*/String ParamTy = this.LocalTypeName(Func.GetFuncParamType(i));
 			if(i > 0) {
 				Code += ", ";
 			}
 			Code += ParamTy + " " + ParamNameList.get(i);
 			i = i + 1;
 		}
 		Code += ")";
 		this.VisitingBuilder.Append(Code);
 		this.VisitIndentBlock("{", Body, "}");
 		this.VisitingBuilder = PrevBuilder;
 	}
 
 	@Override public void OpenClassField(GtSyntaxTree ParsedTree, GtType Type, GtClassField ClassField) {
 		/*local*/String TypeName = Type.ShortName;
 		/*local*/String LocalType = this.LocalTypeName(Type);
 		this.VisitingBuilder.Indent();
 		this.VisitingBuilder.AppendLine("struct " + TypeName + " {");;
 		if(Type.SuperType != null) {
 			this.VisitingBuilder.AppendIndent();
 			this.VisitingBuilder.AppendLine("// " + this.LocalTypeName(Type.SuperType) + " __base;");
 		}
 		for(/*local*/int i = 0; i < LibGreenTea.ListSize(ClassField.FieldList); i = i + 1) {
 			/*local*/GtFieldInfo FieldInfo = ClassField.FieldList.get(i);
 			/*local*/GtType VarType = FieldInfo.Type;
 			/*local*/String VarName = FieldInfo.NativeName;
 			this.VisitingBuilder.AppendIndent();
 			this.VisitingBuilder.AppendLine(this.LocalTypeName(VarType) + " " + VarName + this.SemiColon);
 		}
 		this.VisitingBuilder.UnIndent();
 
 		this.VisitingBuilder.AppendIndent();
 		this.VisitingBuilder.AppendLine("}" + this.SemiColon);
 
 		this.VisitingBuilder.AppendIndent();
 		this.VisitingBuilder.AppendLine(LocalType + " NEW_" + TypeName + "() {");
 
 		this.VisitingBuilder.Indent();
 		this.VisitingBuilder.AppendIndent();
 		this.VisitingBuilder.AppendLine(LocalType + " " + this.GetRecvName() + " = " + "GT_New("+LocalType+")" + this.SemiColon);
 		for(/*local*/int i = 0; i < LibGreenTea.ListSize(ClassField.FieldList); i = i + 1) {
 			/*local*/GtFieldInfo FieldInfo = ClassField.FieldList.get(i);
 			/*local*/String VarName = FieldInfo.NativeName;
 			/*local*/String InitValue = this.StringifyConstValue(FieldInfo.InitValue);
 			if(!FieldInfo.Type.IsNativeType()) {
 				InitValue = this.NullLiteral;
 			}
 			this.VisitingBuilder.AppendIndent();
 			this.VisitingBuilder.AppendLine(this.GetRecvName() + "->" + VarName + " = " + InitValue + this.SemiColon);
 		}
 		this.VisitingBuilder.AppendIndent();
 		this.VisitingBuilder.AppendLine("return " + this.GetRecvName() + this.SemiColon);
 		this.VisitingBuilder.UnIndent();
 		this.VisitingBuilder.AppendIndent();
 		this.VisitingBuilder.AppendLine("}" + this.SemiColon);
 	}
 }
