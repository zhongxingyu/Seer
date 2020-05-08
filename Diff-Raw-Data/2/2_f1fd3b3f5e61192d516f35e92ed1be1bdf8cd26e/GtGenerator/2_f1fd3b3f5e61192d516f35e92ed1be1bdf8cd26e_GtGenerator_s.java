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
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 
 import org.GreenTeaScript.DShell.DShellProcess;
 //endif VAJA
 
 public class GtGenerator extends GreenTeaUtils {
 	/*field*/public final String      TargetCode;
 	/*field*/public GtParserContext    Context;
 	/*field*/public ArrayList<Object> GeneratedCodeStack;
 	/*field*/public String OutputFile;
 	/*field*/public int GeneratorFlag;
 
 	GtGenerator/*constructor*/(String TargetCode, String OutputFile, int GeneratorFlag) {
 		this.TargetCode = TargetCode;
 		this.OutputFile = OutputFile;
 		this.GeneratorFlag = GeneratorFlag;
 		this.Context = null;
 		this.GeneratedCodeStack = null;
 	}
 
 	public void InitContext(GtParserContext Context) {
 		this.Context = Context;
 		this.GeneratedCodeStack = new ArrayList<Object>();
 		Context.RootNameSpace.LoadRequiredLib("common");
 	}
 
 	public final GtNode CreateUnsupportedNode(GtType Type, GtSyntaxTree ParsedTree) {
 		/*local*/GtToken Token = ParsedTree.KeyToken;
 		this.Context.ReportError(ErrorLevel, Token, this.TargetCode + " has no language support for " + Token.ParsedText);
 		return new GtErrorNode(GtStaticTable.VoidType, ParsedTree.KeyToken);
 	}
 
 	public GtNode CreateEmptyNode(GtType Type) {
 		return new GtEmptyNode(Type, GtTokenContext.NullToken);
 	}
 
 	public GtNode CreateNullNode(GtType Type, GtSyntaxTree ParsedTree) {
 		return new GtNullNode(Type, ParsedTree.KeyToken);
 	}
 
 	public GtNode CreateBooleanNode(GtType Type, GtSyntaxTree ParsedTree, boolean Value) {
 		return new GtBooleanNode(Type, ParsedTree.KeyToken, Value);
 	}
 
 	public GtNode CreateIntNode(GtType Type, GtSyntaxTree ParsedTree, long Value) {
 		return new GtIntNode(Type, ParsedTree.KeyToken, Value);
 	}
 
 	public GtNode CreateFloatNode(GtType Type, GtSyntaxTree ParsedTree, double Value) {
 		return new GtFloatNode(Type, ParsedTree.KeyToken, Value);
 	}
 
 	public GtNode CreateStringNode(GtType Type, GtSyntaxTree ParsedTree, String Value) {
 		return new GtStringNode(Type, ParsedTree.KeyToken, Value);
 	}
 
 	public GtNode CreateRegexNode(GtType Type, GtSyntaxTree ParsedTree, String Value) {
 		return new GtRegexNode(Type, ParsedTree.KeyToken, Value);
 	}
 
 	public GtNode CreateConstPoolNode(GtType Type, GtSyntaxTree ParsedTree, Object Value) {
 		return new GtConstPoolNode(Type, ParsedTree.KeyToken, Value);
 	}
 
 	public GtNode CreateArrayLiteralNode(GtType Type, GtSyntaxTree ParsedTree) {
 		return new GtArrayLiteralNode(Type, ParsedTree.KeyToken);
 	}
 
 	public GtNode CreateMapLiteralNode(GtType Type, GtSyntaxTree ParsedTree) {
 		return new GtMapLiteralNode(Type, ParsedTree.KeyToken);
 	}
 
 	public GtNode CreateParamNode(GtType Type, GtSyntaxTree ParsedTree, String Name, GtNode InitNode) {
 		return new GtParamNode(Type, ParsedTree.KeyToken, Name, InitNode);
 	}
 
 	public GtNode CreateFunctionLiteralNode(GtType Type, GtSyntaxTree ParsedTree, GtNode BodyNode) {
 		return new GtFunctionLiteralNode(Type, ParsedTree.KeyToken, BodyNode);
 	}
 
 	public GtNode CreateGetLocalNode(GtType Type, GtSyntaxTree ParsedTree, String NativeName) {
 		return new GtGetLocalNode(Type, ParsedTree.KeyToken, NativeName);
 	}
 
 	public GtNode CreateSetLocalNode(GtType Type, GtSyntaxTree ParsedTree, String NativeName, GtNode ValueNode) {
 		return new GtSetLocalNode(Type, ParsedTree.KeyToken, NativeName, ValueNode);
 	}
 
 	public GtNode CreateGetCapturedNode(GtType Type, GtSyntaxTree ParsedTree, String NativeName) {
 		return new GtGetCapturedNode(Type, ParsedTree.KeyToken, NativeName);
 	}
 
 	public GtNode CreateSetCapturedNode(GtType Type, GtSyntaxTree ParsedTree, String NativeName, GtNode ValueNode) {
 		return new GtSetCapturedNode(Type, ParsedTree.KeyToken, NativeName, ValueNode);
 	}
 
 	public GtNode CreateGetterNode(GtType Type, GtSyntaxTree ParsedTree, GtNode RecvNode, String NativeName) {
 		return new GtGetterNode(Type, ParsedTree.KeyToken, RecvNode, NativeName);
 	}
 
 	public GtNode CreateSetterNode(GtType Type, GtSyntaxTree ParsedTree, GtNode RecvNode, String NativeName, GtNode ValueNode) {
 		return new GtSetterNode(Type, ParsedTree.KeyToken, RecvNode, NativeName, ValueNode);
 	}
 
 	public GtNode CreateApplySymbolNode(GtType Type, GtSyntaxTree ParsedTree, String FuncName, GtFunc Func) {
 		GtApplySymbolNode Node = new GtApplySymbolNode(Type, ParsedTree.KeyToken, FuncName);
 		Node.ResolvedFunc = Func;
 		return Node;
 	}
 
 	public GtNode CreateApplyFunctionObjectNode(GtType Type, GtSyntaxTree ParsedTree, GtNode FuncNode) {
 		return new GtApplyFunctionObjectNode(Type, ParsedTree.KeyToken, FuncNode);
 	}
 
 	public GtNode CreateApplyOverridedMethodNode(GtType Type, GtSyntaxTree ParsedTree, GtNameSpace NameSpace, GtFunc Func) {
 		return new GtApplyOverridedMethodNode(Type, ParsedTree.KeyToken, NameSpace, Func);
 	}
 
 	public GtNode CreateGetIndexNode(GtType Type, GtSyntaxTree ParsedTree, GtNode RecvNode, GtFunc Func, GtNode IndexNode) {
 		GtGetIndexNode Node = new GtGetIndexNode(Type, ParsedTree.KeyToken, RecvNode, IndexNode);
 		Node.ResolvedFunc = Func;
 		return Node;
 	}
 
 	public GtNode CreateSetIndexNode(GtType Type, GtSyntaxTree ParsedTree, GtNode RecvNode, GtFunc Func, GtNode IndexNode, GtNode ValueNode) {
 		GtSetIndexNode Node = new GtSetIndexNode(Type, ParsedTree.KeyToken, RecvNode, IndexNode, ValueNode);
 		Node.ResolvedFunc = Func;
 		return Node;
 	}
 
 	public GtNode CreateSliceNode(GtType Type, GtSyntaxTree ParsedTree, GtNode RecvNode, GtNode Index1, GtNode Index2) {
 		return new GtSliceNode(Type, ParsedTree.KeyToken, RecvNode, Index1, Index2);
 	}
 
 	public GtNode CreateAndNode(GtType Type, GtSyntaxTree ParsedTree, GtNode LeftNode, GtNode RightNode) {
 		return new GtAndNode(Type, ParsedTree.KeyToken, LeftNode, RightNode);
 	}
 
 	public GtNode CreateOrNode(GtType Type, GtSyntaxTree ParsedTree, GtNode LeftNode, GtNode RightNode) {
 		return new GtOrNode(Type, ParsedTree.KeyToken, LeftNode, RightNode);
 	}
 
 	public GtNode CreateUnaryNode(GtType Type, GtSyntaxTree ParsedTree, String OperatorName, GtNode ValueNode) {
 		return new GtUnaryNode(Type, ParsedTree.KeyToken, OperatorName, ValueNode);
 	}
 
 	public GtNode CreatePrefixInclNode(GtType Type, GtSyntaxTree ParsedTree, GtNode RecvNode) {
 		return new GtPrefixInclNode(Type, ParsedTree.KeyToken, RecvNode);
 	}
 
 	public GtNode CreatePrefixDeclNode(GtType Type, GtSyntaxTree ParsedTree, GtNode RecvNode) {
 		return new GtPrefixDeclNode(Type, ParsedTree.KeyToken, RecvNode);
 	}
 
 	public GtNode CreateSuffixInclNode(GtType Type, GtSyntaxTree ParsedTree, GtNode RecvNode) {
 		return new GtSuffixInclNode(Type, ParsedTree.KeyToken, RecvNode);
 	}
 
 	public GtNode CreateSuffixDeclNode(GtType Type, GtSyntaxTree ParsedTree, GtNode RecvNode) {
 		return new GtSuffixDeclNode(Type, ParsedTree.KeyToken, RecvNode);
 	}
 
 	public GtNode CreateBinaryNode(GtType Type, GtSyntaxTree ParsedTree, String OperatorName, GtNode LeftNode, GtNode RightNode) {
 		return new GtBinaryNode(Type, ParsedTree.KeyToken, OperatorName, LeftNode, RightNode);
 	}
 
 	public GtNode CreateTrinaryNode(GtType Type, GtSyntaxTree ParsedTree, GtNode CondNode, GtNode ThenNode, GtNode ElseNode) {
 		return new GtTrinaryNode(Type, ParsedTree.KeyToken, CondNode, ThenNode, ElseNode);
 	}
 
 	public GtNode CreateConstructorNode(GtType Type, GtSyntaxTree ParsedTree, GtFunc Func) {
 		return new GtConstructorNode(Type, ParsedTree.KeyToken, Func);
 	}
 
 	public GtNode CreateAllocateNode(GtType Type, GtSyntaxTree ParsedTree) {
 		return new GtAllocateNode(Type, ParsedTree.KeyToken);
 	}
 
 	public GtNode CreateNewArrayNode(GtType Type, GtSyntaxTree ParsedTree) {
 		return new GtNewArrayNode(Type, ParsedTree.KeyToken);
 	}
 
 	public GtNode CreateInstanceOfNode(GtType Type, GtSyntaxTree ParsedTree, GtNode ExprNode, GtType TypeInfo) {
 		return new GtInstanceOfNode(Type, ParsedTree.KeyToken, ExprNode, TypeInfo);
 	}
 
 	public GtNode CreateCastNode(GtType Type, GtSyntaxTree ParsedTree, GtNode ExprNode, GtType TypeInfo) {
 		return new GtCastNode(Type, ParsedTree.KeyToken, TypeInfo, ExprNode);
 	}
 
 	public GtNode CreateVarDeclNode(GtType Type, GtSyntaxTree ParsedTree, GtType DeclType, String VariableName, GtNode InitNode, GtNode Block) {
 		return new GtVarDeclNode(Type, ParsedTree.KeyToken, DeclType, VariableName, InitNode, Block);
 	}
 
 	public GtNode CreateUsingNode(GtType Type, GtSyntaxTree ParsedTree, GtType DeclType, String VariableName, GtNode InitNode, GtNode Block) {
 		return new GtUsingNode(Type, ParsedTree.KeyToken, DeclType, VariableName, InitNode, Block);
 	}
 
 	public GtNode CreateIfNode(GtType Type, GtSyntaxTree ParsedTree, GtNode CondNode, GtNode ThenNode, GtNode ElseNode) {
 		return new GtIfNode(Type, ParsedTree.KeyToken, CondNode, ThenNode, ElseNode);
 	}
 
 	public GtNode CreateWhileNode(GtType Type, GtSyntaxTree ParsedTree, GtNode CondNode, GtNode BodyNode) {
 		return new GtWhileNode(Type, ParsedTree.KeyToken, CondNode, BodyNode);
 	}
 
 	public GtNode CreateDoWhileNode(GtType Type, GtSyntaxTree ParsedTree, GtNode CondNode, GtNode BodyNode) {
 		return new GtDoWhileNode(Type, ParsedTree.KeyToken, CondNode, BodyNode);
 	}
 
 	public GtNode CreateForNode(GtType Type, GtSyntaxTree ParsedTree, GtNode CondNode, GtNode IterNode, GtNode BodyNode) {
 		return new GtForNode(Type, ParsedTree.KeyToken, CondNode, IterNode, BodyNode);
 	}
 
 	public GtNode CreateForEachNode(GtType Type, GtSyntaxTree ParsedTree, GtNode VariableNode, GtNode IterNode, GtNode BodyNode) {
 		return new GtForEachNode(Type, ParsedTree.KeyToken, VariableNode, IterNode, BodyNode);
 	}
 
 	public GtNode CreateContinueNode(GtType Type, GtSyntaxTree ParsedTree, String LabelName) {
 		return new GtContinueNode(Type, ParsedTree.KeyToken, LabelName);
 	}
 
 	public GtNode CreateBreakNode(GtType Type, GtSyntaxTree ParsedTree, String LabelName) {
 		return new GtBreakNode(Type, ParsedTree.KeyToken, LabelName);
 	}
 
 	public GtNode CreateStatementNode(GtType Type, GtSyntaxTree ParsedTree, GtNode ValueNode) {
 		return new GtStatementNode(Type, ParsedTree.KeyToken, ValueNode);
 	}
 
 	public GtNode CreateReturnNode(GtType Type, GtSyntaxTree ParsedTree, GtNode ValueNode) {
 		return new GtReturnNode(Type, ParsedTree.KeyToken, ValueNode);
 	}
 
 	public GtNode CreateYieldNode(GtType Type, GtSyntaxTree ParsedTree, GtNode ValueNode) {
 		return new GtYieldNode(Type, ParsedTree.KeyToken, ValueNode);
 	}
 
 	public GtNode CreateThrowNode(GtType Type, GtSyntaxTree ParsedTree, GtNode ValueNode) {
 		return new GtThrowNode(Type, ParsedTree.KeyToken, ValueNode);
 	}
 
 	public GtNode CreateTryNode(GtType Type, GtSyntaxTree ParsedTree, GtNode TryBlock, GtNode FinallyBlock) {
 		return new GtTryNode(Type, ParsedTree.KeyToken, TryBlock, FinallyBlock);
 	}
 
 	public GtNode CreateCatchNode(GtType Type, GtSyntaxTree ParsedTree, GtType ExceptionType, String Name, GtNode BodyNode) {
 		return new GtCatchNode(Type, ParsedTree.KeyToken, ExceptionType, Name, BodyNode);
 	}
 
 	public GtNode CreateSwitchNode(GtType Type, GtSyntaxTree ParsedTree, GtNode MatchNode, GtNode DefaultBlock) {
 		return new GtSwitchNode(Type, ParsedTree.KeyToken, MatchNode, DefaultBlock);
 	}
 
 	public GtNode CreateCaseNode(GtType Type, GtSyntaxTree ParsedTree, GtNode CaseNode, GtNode BodyNode) {
 		return new GtCaseNode(Type, ParsedTree.KeyToken, CaseNode, BodyNode);
 	}
 
 	public GtNode CreateCommandNode(GtType Type, GtSyntaxTree ParsedTree, GtNode PipedNextNode) {
 		return new GtCommandNode(Type, ParsedTree.KeyToken, PipedNextNode);
 	}
 
 	public GtNode CreateErrorNode(GtType Type, GtSyntaxTree ParsedTree) {
 		return new GtErrorNode(Type, ParsedTree.KeyToken);
 	}
 
 	// useful Create* API
 	public final GtNode CreateCoercionNode(GtType Type, GtNameSpace NameSpace, GtFunc Func, GtNode Node) {
 		/*local*/GtNode ApplyNode = this.CreateApplySymbolNode(Type, null, "Coercion", Func);
 		ApplyNode.Append(Node);
 		return ApplyNode;
 	}
 	public final GtNode CreateConstNode(GtType Type, GtSyntaxTree ParsedTree, Object Value) {
 		if(Value instanceof Boolean) {
 			return CreateBooleanNode(Type, ParsedTree, (Boolean) Value);
 		}
 		if(Value instanceof Long) {
 			return CreateIntNode(Type, ParsedTree, (Long) Value);
 		}
 		if(Value instanceof Double) {
 			return CreateFloatNode(Type, ParsedTree, (Double) Value);
 		}
 		if(Value instanceof String) {
 			return CreateStringNode(Type, ParsedTree, (String) Value);
 		}
 //		if(Value instanceof Rexex) {
 //			return CreateRegexNode(Type, ParsedTree, (String) Value);
 //		}
 		return CreateConstPoolNode(Type, ParsedTree, Value);
 	}
 
 	public GtNode CreateApplyMethodNode(GtType Type, GtSyntaxTree ParsedTree, String FuncName, GtFunc Func) {
 		if(Func != null && Func.Is(VirtualFunc)) {
 			return CreateApplyOverridedMethodNode(Type, ParsedTree, ParsedTree.NameSpace.Minimum(), Func);
 		}
 		return CreateApplySymbolNode(Type, ParsedTree, FuncName, Func);
 	}
 
 	public GtNode CreateUpdateNode(GtType Type, GtSyntaxTree ParsedTree, GtFunc ResolovedFunc, GtNode LeftNode, GtNode RightNode) {
 		GtNode Node = null;
 		if(LeftNode instanceof GtGetIndexNode) {
 			/*local*/GtGetIndexNode IndexNode = (/*cast*/GtGetIndexNode) LeftNode;
			return CreateSetIndexNode(LeftNode.Type, ParsedTree, IndexNode.RecvNode, IndexNode.IndexNode, RightNode);
 		}
 		else if(LeftNode instanceof GtGetLocalNode) {
 			/*local*/GtGetLocalNode LocalNode = (/*cast*/GtGetLocalNode) LeftNode;
 			Node = CreateSetLocalNode(LeftNode.Type, ParsedTree, LocalNode.NativeName, RightNode);
 		}
 		else if(LeftNode instanceof GtGetterNode) {
 			/*local*/GtGetterNode GetterNode = (/*cast*/GtGetterNode) LeftNode;
 			Node = CreateSetterNode(LeftNode.Type, ParsedTree, GetterNode.RecvNode, GetterNode.NativeName, RightNode);
 		}
 		else {
 			LibGreenTea.Assert(false); // unreachable
 		}
 		if(Node instanceof GtSymbolNode) {
 			((/*cast*/GtSymbolNode) Node).ResolvedFunc = ResolovedFunc;
 		}
 		return Node;
 	}
 
 	/* language constructor */
 
 	public void OpenClassField(GtSyntaxTree ParsedTree, GtType DefinedType, GtClassField ClassField) {
 		/*extension*/
 	}
 
 	public void CloseClassField(GtType DefinedType, ArrayList<GtFunc> MemberList) {
 		/*extension*/
 	}
 
 	public GtFunc CreateFunc(int FuncFlag, String FuncName, int BaseIndex, ArrayList<GtType> TypeList) {
 		return new GtFunc(FuncFlag, FuncName, BaseIndex, TypeList);
 	}
 
 	public void GenerateFunc(GtFunc Func, ArrayList<String> ParamNameList, GtNode Body) {
 		/*extenstion*/
 	}
 
 	public void SyncCodeGeneration() {
 		/*extension*/
 	}
 
 	public final void StopVisitor(GtNode Node) {
 		Node.NextNode = null;
 	}
 
 	public final boolean IsEmptyBlock(GtNode Node) {
 		return Node == null || (Node instanceof GtEmptyNode) && Node.NextNode == null;
 	}
 
 	public final GtForNode FindParentForNode(GtNode Node) {
 		/*local*/GtNode Parent = Node.ParentNode;
 		while(Parent != null) {
 			if(Parent instanceof GtForNode) {
 				return (/*cast*/GtForNode)Parent;
 			}
 			if(Parent.ParentNode == null) {
 				Parent = Parent.MoveHeadNode();
 			}
 			Parent = Parent.ParentNode;
 		}
 		return null;
 	}
 
 	//------------------------------------------------------------------------
 
 	public void VisitEmptyNode(GtEmptyNode Node) {
 		LibGreenTea.DebugP("empty node: " + Node.Token.ParsedText);
 		/*extension*/
 	}
 
 	public void VisitNullNode(GtNullNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitBooleanNode(GtBooleanNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitIntNode(GtIntNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitFloatNode(GtFloatNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitStringNode(GtStringNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitRegexNode(GtRegexNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitConstPoolNode(GtConstPoolNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitArrayLiteralNode(GtArrayLiteralNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitMapLiteralNode(GtMapLiteralNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitParamNode(GtParamNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitFunctionLiteralNode(GtFunctionLiteralNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitGetLocalNode(GtGetLocalNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitSetLocalNode(GtSetLocalNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitGetCapturedNode(GtGetCapturedNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitSetCapturedNode(GtSetCapturedNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitGetterNode(GtGetterNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitSetterNode(GtSetterNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitApplySymbolNode(GtApplySymbolNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitApplyFunctionObjectNode(GtApplyFunctionObjectNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitApplyOverridedMethodNode(GtApplyOverridedMethodNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitGetIndexNode(GtGetIndexNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitSetIndexNode(GtSetIndexNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitSliceNode(GtSliceNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitAndNode(GtAndNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitOrNode(GtOrNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitUnaryNode(GtUnaryNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitPrefixInclNode(GtPrefixInclNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitPrefixDeclNode(GtPrefixDeclNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitSuffixInclNode(GtSuffixInclNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitSuffixDeclNode(GtSuffixDeclNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitBinaryNode(GtBinaryNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitTrinaryNode(GtTrinaryNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitConstructorNode(GtConstructorNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitAllocateNode(GtAllocateNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitNewArrayNode(GtNewArrayNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitInstanceOfNode(GtInstanceOfNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitCastNode(GtCastNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitVarDeclNode(GtVarDeclNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitUsingNode(GtUsingNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitIfNode(GtIfNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitWhileNode(GtWhileNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitDoWhileNode(GtDoWhileNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitForNode(GtForNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitForEachNode(GtForEachNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitContinueNode(GtContinueNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitBreakNode(GtBreakNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitStatementNode(GtStatementNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitReturnNode(GtReturnNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitYieldNode(GtYieldNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitThrowNode(GtThrowNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitTryNode(GtTryNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitCatchNode(GtCatchNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitSwitchNode(GtSwitchNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitCaseNode(GtCaseNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitCommandNode(GtCommandNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public void VisitErrorNode(GtErrorNode Node) {
 		if(GreenTeaConsts.DebugVisitor) { throw new RuntimeException("not implemented"); }
 	}
 
 	public final void VisitBlock(GtNode Node) {
 		/*local*/GtNode CurrentNode = Node;
 		while(CurrentNode != null) {
 			CurrentNode.Accept(this);
 			CurrentNode = CurrentNode.NextNode;
 		}
 	}
 
 	// This must be extended in each language
 
 	public boolean IsStrictMode() {
 		return true; /* override this in dynamic languages */
 	}
 
 	public String GetSourceCode() {
 		return null;
 		/*extension*/
 	}
 
 	public void FlushBuffer() {
 		/*extension*/
 	}
 
 	public String BlockComment(String Comment) {
 		return "/*" + Comment + "*/";
 	}
 
 	protected void PushCode(Object Code) {
 		this.GeneratedCodeStack.add(Code);
 	}
 
 	protected final Object PopCode() {
 		/*local*/int Size = this.GeneratedCodeStack.size();
 		if(Size > 0) {
 			/*local*/Object content = this.GeneratedCodeStack.get(Size - 1);
 			this.GeneratedCodeStack.remove(Size - 1);
 			return content;
 		}
 		return "";
 	}
 
 	public String GetRecvName() {
 		return "this";  // default
 	}
 
 	public void InvokeMainFunc(String MainFuncName) {
 		/*extension*/
 	}
 
 	private Object[] MakeArguments(Object RecvObject, ArrayList<GtNode> ParamList, boolean EnforceConst) {
 		/*local*/int StartIdx = 0;
 		/*local*/int Size = LibGreenTea.ListSize(ParamList);
 		/*local*/Object[] Values = new Object[RecvObject == null ? Size : Size + 1];
 		if(RecvObject != null) {
 			Values[0] = RecvObject;
 			StartIdx = 1;
 		}
 		/*local*/int i = 0;
 		while(i < Size) {
 			/*local*/GtNode Node = ParamList.get(i);
 			if(Node.IsNullNode()) {
 				Values[StartIdx + i] = null;
 			}
 			else {
 				/*local*/Object Value = Node.ToConstValue(this.Context, EnforceConst);
 				if(Value == null) {
 					return null;
 				}
 				Values[StartIdx + i] = Value;
 			}
 			i += 1;
 		}
 		return Values;
 	}
 
 	// EnforceConst : 
 	
 	public Object EvalAllocateNode(GtAllocateNode Node, boolean EnforceConst) {
 //ifdef JAVA  this is for JavaByteCodeGenerator and JavaSourceGenerator
 		if(EnforceConst && Node.Type.TypeBody instanceof Class<?>) {
 			Class<?> NativeClass = (/*cast*/Class<?>)Node.Type.TypeBody;
 			try {
 				Constructor<?> NativeConstructor = NativeClass.getConstructor(GtType.class);
 				return NativeConstructor.newInstance(Node.Type);
 			} catch (Exception e) {
 				LibGreenTea.VerboseException(e);
 			}
 		}
 //endif VAJA
 		return Node.ToNullValue(this.Context, EnforceConst);  // if unsupported
 	}
 
 	public Object EvalConstructorNode(GtConstructorNode Node, boolean EnforceConst) {
 //ifdef JAVA  this is for JavaByteCodeGenerator and JavaSourceGenerator
 		if(EnforceConst && Node.Type.TypeBody instanceof Class<?>) {
 			try {
 				Constructor<?> NativeConstructor = (Constructor<?>)Node.Func.FuncBody;
 				Object[] Arguments = new Object[Node.ParamList.size()];
 				for(int i = 0; i < Arguments.length; i++) {
 					GtNode ArgNode = Node.ParamList.get(i);
 					Arguments[i] = ArgNode.ToConstValue(this.Context, EnforceConst);
 					if(Arguments[i] == null && !ArgNode.IsNullNode()) {
 						return null;
 					}
 					//System.err.println("@@@@ " + i + ", " + Arguments[i] + ", " + Arguments[i].getClass());
 				}
 				return NativeConstructor.newInstance(Arguments);
 			} catch (Exception e) {
 				LibGreenTea.VerboseException(e);
 			}
 		}
 		//endif VAJA
 		return Node.ToNullValue(this.Context, EnforceConst);  // if unsupported
 	}
 
 	public Object EvalApplyNode(GtApplyNode Node, boolean EnforceConst) {
 //ifdef JAVA  this is for JavaByteCodeGenerator and JavaSourceGenerator
 		//System.err.println("@@@@ " + (Node.Func.NativeRef.getClass()));
 		if(Node.Func != null && (EnforceConst || Node.Func.Is(ConstFunc)) && Node.Func.FuncBody instanceof Method) {
 			Object RecvObject = null;
 			int StartIndex = 1;
 			if(Node.Func.Is(NativeMethodFunc)  && Node.NodeList.size() > 1) {
 				RecvObject = Node.NodeList.get(1).ToConstValue(this.Context, EnforceConst);
 				if(RecvObject == null) {
 					return null;
 				}
 				StartIndex = 2;
 			}
 			Object[] Arguments = new Object[Node.NodeList.size() - StartIndex];
 			for(int i = 0; i < Arguments.length; i++) {
 				GtNode ArgNode = Node.NodeList.get(StartIndex+i);
 				Arguments[i] = ArgNode.ToConstValue(this.Context, EnforceConst);
 				if(Arguments[i] == null && !ArgNode.IsNullNode()) {
 					return null;
 				}
 				//System.err.println("@@@@ " + i + ", " + Arguments[i] + ", " + Arguments[i].getClass());
 			}
 			return LibNative.ApplyMethod(Node.Func, RecvObject, Arguments);
 		}
 //endif VAJA
 		return Node.ToNullValue(this.Context, EnforceConst);  // if unsupported
 	}
 	public Object EvalArrayNode(GtArrayLiteralNode Node, boolean EnforceConst) {
 		/*local*/Object ArrayObject = null;
 //ifdef JAVA  this is for JavaByteCodeGenerator and JavaSourceGenerator
 		Object Values[] = new Object[LibGreenTea.ListSize(Node.NodeList)];
 		for(int i = 0; i < LibGreenTea.ListSize(Node.NodeList); i++) {
 			Object Value = Node.NodeList.get(i).ToConstValue(this.Context, EnforceConst);
 			if(Value == null) {
 				return Value;
 			}
 			Values[i] = Value;
 		}
 		ArrayObject = LibGreenTea.NewNewArray(Node.Type, Values);
 //endif VAJA
 		return ArrayObject;  // if unsupported
 	}
 	public Object EvalNewArrayNode(GtNewArrayNode Node, boolean EnforceConst) {
 		/*local*/Object ArrayObject = null;
 //ifdef JAVA  this is for JavaByteCodeGenerator and JavaSourceGenerator
 		Object Values[] = new Object[LibGreenTea.ListSize(Node.NodeList)];
 		for(int i = 0; i < LibGreenTea.ListSize(Node.NodeList); i++) {
 			Object Value = Node.NodeList.get(i).ToConstValue(this.Context, EnforceConst);
 			if(Value == null) {
 				return Value;
 			}
 			Values[i] = Value;
 		}
 		ArrayObject = LibGreenTea.NewArray(Node.Type, Values);
 //endif VAJA
 		return ArrayObject;  // if unsupported
 	}
 
 	public Object EvalGetterNode(GtGetterNode Node, boolean EnforceConst) {
 //ifdef JAVA  this is for JavaByteCodeGenerator and JavaSourceGenerator
 		//System.err.println("** Node.Func = " + Node.Func);
 		if(Node.ResolvedFunc != null) {
 			Object Value = Node.RecvNode.ToConstValue(this.Context, EnforceConst);
 			if(Value == null) {
 				return Value;
 			}
 			//System.err.println("** Node.Func = " + Node.Func.FuncBody.getClass());
 			if(Node.ResolvedFunc.FuncBody instanceof Field) {
 				Value = LibNative.GetNativeFieldValue(Value, (/*cast*/Field)Node.ResolvedFunc.FuncBody);
 				return Value;
 			}
 //			if(Node.Func.FuncBody instanceof Method) {
 //				return LibNative.ApplyMethod1(Node.Func, null, Value);
 //			}
 		}
 //endif VAJA
 		return Node.ToNullValue(this.Context, EnforceConst); // if unsupported
 	}
 
 	public Object EvalSetterNode(GtSetterNode Node, boolean EnforceConst) {
 //ifdef JAVA  this is for JavaByteCodeGenerator and JavaSourceGenerator
 		if(Node.ResolvedFunc != null && EnforceConst) {
 			Object LeftValue = Node.RecvNode.ToConstValue(this.Context, EnforceConst);
 			if(LeftValue == null) {
 				return LeftValue;
 			}
 			Object RightValue = Node.ValueNode.ToConstValue(this.Context, EnforceConst);
 			if(RightValue == null && !Node.ValueNode.IsNullNode()) {
 				return RightValue;
 			}
 			if(Node.ResolvedFunc.FuncBody instanceof Field) {
 				return LibGreenTea.NativeFieldSetter(LeftValue, (/*cast*/Field)Node.ResolvedFunc.FuncBody, RightValue);
 			}
 //			if(Node.Func.FuncBody instanceof Method) {
 //				return LibNative.ApplyMethod2(Node.Func, null, LeftValue, RightValue);
 //			}
 		}
 //endif VAJA
 		return Node.ToNullValue(this.Context, EnforceConst); // if unsupported
 	}
 
 	public Object EvalCommandNode(GtCommandNode Node, boolean EnforceConst) {
 //ifdef JAVA  this is for JavaByteCodeGenerator and JavaSourceGenerator
 		if(!EnforceConst) {
 			return null;
 		}
 		/*local*/ArrayList<String[]> ArgsBuffer = new ArrayList<String[]>();
 		/*local*/GtType Type = Node.Type;
 		/*local*/GtCommandNode CurrentNode = Node;
 		while(CurrentNode != null) {
 			/*local*/int ParamSize = LibGreenTea.ListSize(CurrentNode.ArgumentList);
 			/*local*/String[] Buffer = new String[ParamSize];
 			for(int i =0; i < ParamSize; i++) {
 				/*local*/Object Value = CurrentNode.ArgumentList.get(i).ToConstValue(this.Context, EnforceConst);
 				if(!(Value instanceof String)) {
 					return null;
 				}
 				Buffer[i] = (/*cast*/String)Value;
 			}
 			ArgsBuffer.add(Buffer);
 			CurrentNode = (/*cast*/GtCommandNode) CurrentNode.PipedNextNode;
 		}
 		
 		/*local*/int NodeSize = LibGreenTea.ListSize(ArgsBuffer);
 		/*local*/String[][] Args = new String[NodeSize][];
 		for(int i = 0; i < NodeSize; i++) {
 			/*local*/String[] Buffer = ArgsBuffer.get(i);
 			/*local*/int CommandSize = Buffer.length;
 			Args[i] = new String[CommandSize];
 			for(int j = 0; j < CommandSize; j++) {
 				Args[i][j] = Buffer[j];
 			}
 		}
 		if(Type.IsStringType()) {
 			return DShellProcess.ExecCommandString(Args);
 		}
 		else if(Type.IsBooleanType()) {
 			return DShellProcess.ExecCommandBool(Args);
 		}
 		else if(LibGreenTea.EqualsString(Type.toString(), "Task")) {
 			return DShellProcess.ExecCommandTask(Args);
 		}
 		else {
 			DShellProcess.ExecCommandVoid(Args);
 		}
 //endif VAJA
 		return null;
 	}
 
 	public Object EvalApplySymbolNode(GtApplySymbolNode ApplyNode, boolean EnforceConst) {
 		if((EnforceConst || ApplyNode.ResolvedFunc.Is(ConstFunc)) /*&& ApplyNode.Func.FuncBody instanceof Method */) {
 			/*local*/Object[] Arguments = this.MakeArguments(null, ApplyNode.ParamList, EnforceConst);
 			if(Arguments != null) {
 				return LibGreenTea.InvokeFunc(ApplyNode.ResolvedFunc, Arguments);
 			}
 		}
 		return null;
 	}
 
 	public Object EvalApplyOverridedMethodNode(GtApplyOverridedMethodNode ApplyNode, boolean EnforceConst) {
 		if((EnforceConst || ApplyNode.Func.Is(ConstFunc))) {
 			/*local*/Object[] Arguments = this.MakeArguments(null, ApplyNode.ParamList, EnforceConst);
 			if(Arguments != null) {
 				return LibGreenTea.InvokeOverridedMethod(0, ApplyNode.NameSpace, ApplyNode.Func, Arguments);
 			}
 		}
 		return null;
 	}
 
 	public Object EvalApplyFuncionObjectNode(GtApplyFunctionObjectNode ApplyNode, boolean EnforceConst) {
 		/*local*/GtFunc Func = (/*cast*/GtFunc)ApplyNode.FuncNode.ToConstValue(this.Context, EnforceConst);
 		if(Func != null) {
 			/*local*/Object[] Arguments = this.MakeArguments(null, ApplyNode.ParamList, EnforceConst);
 			if(Arguments != null) {
 				return LibGreenTea.InvokeFunc(Func, Arguments);
 			}
 		}
 		return null;
 	}
 
 	public Object EvalApplyDynamicFuncNode(GtApplyDynamicFuncNode ApplyNode, boolean EnforceConst) {
 		/*local*/Object[] Arguments = this.MakeArguments(null, ApplyNode.ParamList, EnforceConst);
 		if(Arguments != null) {
 			return LibGreenTea.InvokeDynamicFunc(0, ApplyNode.Type, ApplyNode.NameSpace, ApplyNode.FuncName, Arguments);
 		}
 		return null;
 	}
 
 	public Object EvalApplyDynamicMethodNode(GtApplyDynamicMethodNode ApplyNode, boolean EnforceConst) {
 		/*local*/Object[] Arguments = this.MakeArguments(null, ApplyNode.ParamList, EnforceConst);
 		if(Arguments != null) {
 			return LibGreenTea.InvokeDynamicMethod(0, ApplyNode.Type, ApplyNode.NameSpace, ApplyNode.FuncName, Arguments);
 		}
 		return null;
 	}
 
 	@Deprecated
 	public Object EvalDyGetterNode(GtDyGetterNode GetterNode, boolean EnforceConst) {
 		/*local*/Object RecvObject = GetterNode.RecvNode.ToConstValue(this.Context, EnforceConst);
 		if(RecvObject != null) {
 			/*local*/Object Value = LibGreenTea.DynamicGetter(RecvObject, GetterNode.FieldName);
 			return LibGreenTea.DynamicCast(GetterNode.Type, Value);
 		}
 		return null;
 	}
 
 	@Deprecated
 	public Object EvalDySetterNode(GtDySetterNode SetterNode, boolean EnforceConst) {
 		/*local*/Object RecvObject = SetterNode.RecvNode.ToConstValue(this.Context, EnforceConst);
 		if(RecvObject != null) {
 			/*local*/Object Value = SetterNode.ValueNode.ToConstValue(this.Context, EnforceConst);
 			if(Value != null || SetterNode.ValueNode.IsNullNode()) {
 				return LibGreenTea.DynamicSetter(RecvObject, SetterNode.FieldName, Value);
 			}
 		}
 		return null;
 	}
 }
