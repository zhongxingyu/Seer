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
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 
 import org.GreenTeaScript.JVM.GtSubProc;
 //endif VAJA
 
 /* language */
 // GreenTea Generator should be written in each language.
 
 class GtNode extends GreenTeaUtils {
 	/*field*/public GtNode	ParentNode;
 	/*field*/public GtNode	PrevNode;
 	/*field*/public GtNode	NextNode;
 
 	/*field*/public GtType	Type;
 	/*field*/public GtToken	Token;
 
 	GtNode/*constructor*/(GtType Type, GtToken Token) {
 		this.Type = Type;
 		this.Token = Token;
 		this.ParentNode = null;
 		this.PrevNode = null;
 		this.NextNode = null;
 	}
 
 	public final GtNode GetParentNode() {
 		return this.ParentNode;
 	}
 
 	public final void SetParent(GtNode Node) {
 		if(Node != null) {
 			Node.ParentNode = this;
 		}
 	}
 
 	public final void SetParent2(GtNode Node, GtNode Node2) {
 		this.SetParent(Node);
 		this.SetParent(Node2);
 	}
 
 	public final GtNode GetNextNode() {
 		return this.NextNode;
 	}
 
 	public final void SetNextNode(GtNode Node) {
 		this.NextNode = Node;
 	}
 
 	public final GtNode GetPrevNode() {
 		return this.PrevNode;
 	}
 
 	public final void SetPrevNode(GtNode Node) {
 		this.PrevNode = Node;
 	}
 
 	public final GtNode MoveHeadNode() {
 		/*local*/GtNode Node = this;
 		while(Node.PrevNode != null) {
 			Node = Node.PrevNode;
 		}
 		return Node;
 	}
 
 	public final GtNode MoveTailNode() {
 		/*local*/GtNode Node = this;
 		while(Node.NextNode != null) {
 			Node = Node.NextNode;
 		}
 		return Node;
 	}
 
 	public void Append(GtNode Node) {
 		/*extension*/
 		this.SetParent(Node);
 	}
 
 	public final void AppendNodeList(ArrayList<GtNode> NodeList) {
 		/*local*/int i = 0;
 		while(i < LibGreenTea.ListSize(NodeList)) {
 			this.Append(NodeList.get(i));
 			i = i + 1;
 		}
 	}
 
 	public void Evaluate(GtGenerator Visitor) {
 		/* must override */
 	}
 
 	public final boolean IsError() {
 		return (this instanceof ErrorNode);
 	}
 
 	public Object ToConstValue(boolean EnforceConst)  {
 		if(EnforceConst) {
 			this.Type.Context.ReportError(ErrorLevel, this.Token, "value must be constant in this context");
 		}
 		return null;
 	}
 
 }
 
 final class EmptyNode extends GtNode {
 	EmptyNode/*constructor*/(GtType Type, GtToken Token) {
 		super(Type, Token);
 	}
 	public Object ToConstValue(boolean EnforceConst)  {
 		return null;
 	}
 }
 
 final class ConstNode extends GtNode {
 	/*field*/public Object	ConstValue;
 	ConstNode/*constructor*/(GtType Type, GtToken Token, Object ConstValue) {
 		super(Type, Token);
 		this.ConstValue = ConstValue;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitConstNode(this);
 	}
 	@Override public Object ToConstValue(boolean EnforceConst)  {
 		return this.ConstValue;
 	}
 }
 
 final class LocalNode extends GtNode {
 	/*field*/public String NativeName;
 	LocalNode/*constructor*/(GtType Type, GtToken Token, String NativeName) {
 		super(Type, Token);
 		this.NativeName = NativeName;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitLocalNode(this);
 	}
 }
 
 class NullNode extends GtNode {
 	NullNode/*constructor*/(GtType Type, GtToken Token) {
 		super(Type, Token);
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitNullNode(this);
 	}
 	public Object ToConstValue(boolean EnforceConst)  {
 		return null;
 	}
 }
 
 //E.g., (T) $Expr
 final class CastNode extends GtNode {
 	/*field*/public GtFunc  Func;
 	/*field*/public GtType	CastType;
 	/*field*/public GtNode	Expr;
 	CastNode/*constructor*/(GtType Type, GtToken Token, GtType CastType, GtNode Expr) {
 		super(Type, Token);
 		this.CastType = CastType;
 		this.Expr = Expr;
 		this.SetParent(Expr);
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitCastNode(this);
 	}
 	@Override public Object ToConstValue(boolean EnforceConst)  {
 		/*local*/Object Value = this.Expr.ToConstValue(EnforceConst) ;
 		if(Value != null) {
 			return LibGreenTea.EvalCast(this.CastType, Value);
 		}
 		return Value;
 	}
 }
 
 // E.g., "~" $Expr
 final class UnaryNode extends GtNode {
 	/*field*/public GtFunc  Func;
 	/*field*/public GtNode	Expr;
 	UnaryNode/*constructor*/(GtType Type, GtToken Token, GtFunc Func, GtNode Expr) {
 		super(Type, Token);
 		this.Func = Func;
 		this.Expr = Expr;
 		this.SetParent(Expr);
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitUnaryNode(this);
 	}
 	@Override public Object ToConstValue(boolean EnforceConst)  {
 		/*local*/Object Value = this.Expr.ToConstValue(EnforceConst) ;
 		if(Value != null) {
 			return LibGreenTea.EvalUnary(this.Type, this.Token.ParsedText, Value);
 		}
 		return Value;
 	}	
 }
 
 // E.g.,  $Expr "++"
 class SuffixNode extends GtNode {
 	/*field*/public GtFunc    Func;
 	/*field*/public GtNode	Expr;
 	SuffixNode/*constructor*/(GtType Type, GtToken Token, GtFunc Func, GtNode Expr) {
 		super(Type, Token);
 		this.Func = Func;
 		this.Expr = Expr;
 		this.SetParent(Expr);
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitSuffixNode(this);
 	}
 	@Override public Object ToConstValue(boolean EnforceConst)  {
 		/*local*/Object Value = this.Expr.ToConstValue(EnforceConst) ;
 		if(Value != null) {
 			return LibGreenTea.EvalSuffix(this.Type, Value, this.Token.ParsedText);
 		}
 		return Value;
 	}
 }
 
 //E.g., "exists" $Expr
 class ExistsNode extends GtNode {
 	/*field*/public GtFunc    Func;
 	/*field*/public GtNode	Expr;
 	ExistsNode/*constructor*/(GtType Type, GtToken Token, GtFunc Func, GtNode Expr) {
 		super(Type, Token);
 		this.Func = Func;
 		this.Expr = Expr;
 		this.SetParent(Expr);
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitExistsNode(this);
 	}
 }
 
 //E.g., $LeftNode = $RightNode
 class AssignNode extends GtNode {
 	/*field*/public GtNode   LeftNode;
 	/*field*/public GtNode	 RightNode;
 	AssignNode/*constructor*/(GtType Type, GtToken Token, GtNode Left, GtNode Right) {
 		super(Type, Token);
 		this.LeftNode  = Left;
 		this.RightNode = Right;
 		this.SetParent2(Left, Right);
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitAssignNode(this);
 	}
 }
 
 //E.g., $LeftNode += $RightNode
 class SelfAssignNode extends GtNode {
 	/*field*/public GtFunc Func;
 	/*field*/public GtNode LeftNode;
 	/*field*/public GtNode RightNode;
 	SelfAssignNode/*constructor*/(GtType Type, GtToken Token, GtFunc Func, GtNode Left, GtNode Right) {
 		super(Type, Token);
 		this.Func  = Func;
 		this.LeftNode  = Left;
 		this.RightNode = Right;
 		this.SetParent2(Left, Right);
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitSelfAssignNode(this);
 	}
 }
 
 //E.g., $ExprNode instanceof TypeInfo
 class InstanceOfNode extends GtNode {
 	/*field*/public GtNode   ExprNode;
 	/*field*/public GtType	 TypeInfo;
 	InstanceOfNode/*constructor*/(GtType Type, GtToken Token, GtNode ExprNode, GtType TypeInfo) {
 		super(Type, Token);
 		this.ExprNode = ExprNode;
 		this.TypeInfo = TypeInfo;
 		this.SetParent(ExprNode);
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitInstanceOfNode(this);
 	}
 	@Override public Object ToConstValue(boolean EnforceConst)  {
 		/*local*/Object Value = this.ExprNode.ToConstValue(EnforceConst) ;
 		if(Value != null) {
 			return LibGreenTea.EvalInstanceOf(Value, this.TypeInfo);
 		}
 		return Value;
 	}
 }
 
 // E.g., $LeftNode "+" $RightNode
 class BinaryNode extends GtNode {
 	/*field*/public GtFunc    Func;
 	/*field*/public GtNode   LeftNode;
 	/*field*/public GtNode	 RightNode;
 	BinaryNode/*constructor*/(GtType Type, GtToken Token, GtFunc Func, GtNode Left, GtNode Right) {
 		super(Type, Token);
 		this.Func = Func;
 		this.LeftNode  = Left;
 		this.RightNode = Right;
 		this.SetParent2(Left, Right);
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitBinaryNode(this);
 	}
 	@Override public Object ToConstValue(boolean EnforceConst)  {
 		/*local*/Object LeftValue = this.LeftNode.ToConstValue(EnforceConst) ;
 		if(LeftValue != null) {
 			/*local*/Object RightValue = this.RightNode.ToConstValue(EnforceConst) ;
 			if(RightValue != null) {
 				return LibGreenTea.EvalBinary(this.Type, LeftValue, this.Token.ParsedText, RightValue);
 			}
 		}
 		return null;
 	}
 
 }
 
 //E.g., $LeftNode && $RightNode
 class AndNode extends GtNode {
 	/*field*/public GtNode   LeftNode;
 	/*field*/public GtNode	RightNode;
 	AndNode/*constructor*/(GtType Type, GtToken Token, GtNode Left, GtNode Right) {
 		super(Type, Token);
 		this.LeftNode  = Left;
 		this.RightNode = Right;
 		this.SetParent2(Left, Right);
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitAndNode(this);
 	}
 	@Override public Object ToConstValue(boolean EnforceConst)  {
 		/*local*/Object LeftValue = this.LeftNode.ToConstValue(EnforceConst) ;
 		if(LeftValue instanceof Boolean && LibGreenTea.booleanValue(LeftValue)) {
 			return this.RightNode.ToConstValue(EnforceConst) ;
 		}
 		return null;
 	}
 }
 
 //E.g., $LeftNode || $RightNode
 class OrNode extends GtNode {
 	/*field*/public GtNode   LeftNode;
 	/*field*/public GtNode	RightNode;
 	OrNode/*constructor*/(GtType Type, GtToken Token, GtNode Left, GtNode Right) {
 		super(Type, Token);
 		this.LeftNode  = Left;
 		this.RightNode = Right;
 		this.SetParent2(Left, Right);
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitOrNode(this);
 	}
 	@Override public Object ToConstValue(boolean EnforceConst)  {
 		/*local*/Object LeftValue = this.LeftNode.ToConstValue(EnforceConst) ;
 		if(LeftValue instanceof Boolean) {
 			if(LibGreenTea.booleanValue(LeftValue)) {
 				return LeftValue;
 			}
 			else {
 				return this.RightNode.ToConstValue(EnforceConst) ;
 			}
 		}
 		return null;
 	}
 }
 
 //E.g., $CondExpr "?" $ThenExpr ":" $ElseExpr
 final class TrinaryNode extends GtNode {
 	/*field*/public GtFunc    Func;
 	/*field*/public GtNode	CondExpr;
 	/*field*/public GtNode	ThenExpr;
 	/*field*/public GtNode	ElseExpr;
 	TrinaryNode/*constructor*/(GtType Type, GtToken Token, GtNode CondExpr, GtNode ThenExpr, GtNode ElseExpr) {
 		super(Type, Token);
 		this.CondExpr = CondExpr;
 		this.ThenExpr = ThenExpr;
 		this.ElseExpr = ElseExpr;
 		this.SetParent(CondExpr);
 		this.SetParent2(ThenExpr, ElseExpr);
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitTrinaryNode(this);
 	}
 	@Override public Object ToConstValue(boolean EnforceConst)  {
 		/*local*/Object CondValue = this.CondExpr.ToConstValue(EnforceConst) ;
 		if(CondValue instanceof Boolean) {
 			if(LibGreenTea.booleanValue(CondValue)) {
 				return this.ThenExpr.ToConstValue(EnforceConst) ;
 			}
 			else {
 				return this.ElseExpr.ToConstValue(EnforceConst) ;
 			}
 		}
 		return null;
 	}
 }
 
 //E.g., $Expr . Token.ParsedText
 class GetterNode extends GtNode {
 	/*field*/public GtNode Expr;
 	/*field*/public GtFunc  Func;
 	GetterNode/*constructor*/(GtType Type, GtToken Token, GtFunc Func, GtNode Expr) {
 		super(Type, Token);
 		this.Func = Func;
 		this.Expr = Expr;
 		this.SetParent(Expr);
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitGetterNode(this);
 	}
 
 	@Override public Object ToConstValue(boolean EnforceConst)  {
 		/*local*/Object Value = this.Expr.ToConstValue(EnforceConst) ;
 		if(Value != null) {
 			return LibGreenTea.EvalGetter(this.Type, Value, this.Token.ParsedText);
 		}
 		return Value;
 	}
 }
 
 //E.g., $Expr "[" $Node, $Node "]"
 final class IndexerNode extends GtNode {
 	/*field*/public GtFunc Func;
 	/*field*/public GtNode Expr;
 	/*field*/public ArrayList<GtNode>  NodeList; /* [arg1, arg2, ...] */
 	IndexerNode/*constructor*/(GtType Type, GtToken Token, GtFunc Func, GtNode Expr) {
 		super(Type, Token);
 		this.Func = Func;
 		this.Expr = Expr;
 		this.NodeList = new ArrayList<GtNode>();
 		this.SetParent(Expr);
 	}
 
 	@Override public void Append(GtNode Expr) {
 		this.NodeList.add(Expr);
 		this.SetParent(Expr);
 	}
 
 	public GtNode GetAt(int Index) {
 		return this.NodeList.get(Index);
 	}
 
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitIndexerNode(this);
 	}
 }
 
 //E.g., $Expr "[" $Index ":" $Index2 "]"
 class SliceNode extends GtNode {
 	/*field*/public GtFunc  Func;
 	/*field*/public GtNode Expr;
 	/*field*/public GtNode Index1;
 	/*field*/public GtNode Index2;
 	SliceNode/*constructor*/(GtType Type, GtToken Token, GtFunc Func, GtNode Expr, GtNode Index1, GtNode Index2) {
 		super(Type, Token);
 		this.Func = Func;
 		this.Expr = Expr;
 		this.Index1 = Index1;
 		this.Index2 = Index2;
 		this.SetParent(Expr);
 		this.SetParent2(Index1, Index2);
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitSliceNode(this);
 	}
 }
 
 class VarNode extends GtNode {
 	/*field*/public GtType	DeclType;
 //	/*field*/public GtNode	VarNode;
 	/*field*/public String  NativeName;
 	/*field*/public GtNode	InitNode;
 	/*field*/public GtNode	BlockNode;
 	/* let VarNode in Block end */
 	VarNode/*constructor*/(GtType Type, GtToken Token, GtType DeclType, String VariableName, GtNode InitNode, GtNode Block) {
 		super(Type, Token);
 		this.NativeName = VariableName;
 		this.DeclType  = DeclType;
 		this.InitNode  = InitNode;
 		this.BlockNode = Block;
 		this.SetParent2(InitNode, this.BlockNode);
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitVarNode(this);
 	}
 }
 
 // E.g., $Param[0] "(" $Param[1], $Param[2], ... ")"
 class ApplyNode extends GtNode {
 	/*field*/public GtFunc	Func;
 	/*field*/public ArrayList<GtNode>  NodeList; /* [arg1, arg2, ...] */
 	ApplyNode/*constructor*/(GtType Type, GtToken KeyToken, GtFunc Func) {
 		super(Type, KeyToken);
 		this.Func = Func;
 		this.NodeList = new ArrayList<GtNode>();
 	}
 	@Override public void Append(GtNode Expr) {
 		this.NodeList.add(Expr);
 		this.SetParent(Expr);
 	}
 
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitApplyNode(this);
 	}
 
 	@Override public Object ToConstValue(boolean EnforceConst)  {
 		return this.Type.Context.Generator.EvalApplyNode(this, EnforceConst);
 	}
 }
 
 //E.g., "new" $Type "(" $Param[0], $Param[1], ... ")"
 class NewNode extends GtNode {
 	/*field*/public ArrayList<GtNode>	Params;
 	/*field*/GtFunc Func;
 	NewNode/*constructor*/(GtType Type, GtToken Token, GtFunc Func) {
 		super(Type, Token);
 		this.Params = new ArrayList<GtNode>();
 		this.Func = Func;
 		this.Params.add(new ConstNode(Func.GetFuncType(), Token, Func));
 	}
 	@Override public void Append(GtNode Expr) {
 		this.Params.add(Expr);
 		this.SetParent(Expr);
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitNewNode(this);
 	}
 }
 
 //E.g., "[" $Node, $Node "]"
 class ArrayNode extends GtNode {
 	/*field*/public ArrayList<GtNode>	NodeList;
 	/*field*/GtFunc Func;
 	ArrayNode/*constructor*/(GtType Type, GtToken Token) {
 		super(Type, Token);
 		this.NodeList = new ArrayList<GtNode>();
 	}
 	@Override public void Append(GtNode Expr) {
 		this.NodeList.add(Expr);
 		this.SetParent(Expr);
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitArrayNode(this);
 	}
 	@Override public Object ToConstValue(boolean EnforceConst)  {
 		if(EnforceConst) {
 			return this.Type.Context.Generator.EvalArrayNode(this, EnforceConst);
 		}
 		return null;
 	}
 }
 
 //E.g., "if" "(" $Cond ")" $ThenNode "else" $ElseNode
 class IfNode extends GtNode {
 	/*field*/public GtNode	CondExpr;
 	/*field*/public GtNode	ThenNode;
 	/*field*/public GtNode	ElseNode;
 	/* If CondExpr then ThenBlock else ElseBlock */
 	IfNode/*constructor*/(GtType Type, GtToken Token, GtNode CondExpr, GtNode ThenBlock, GtNode ElseNode) {
 		super(Type, Token);
 		this.CondExpr = CondExpr;
 		this.ThenNode = ThenBlock;
 		this.ElseNode = ElseNode;
 		this.SetParent(CondExpr);
 		this.SetParent2(ThenBlock, ElseNode);
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitIfNode(this);
 	}
 }
 
 //E.g., "while" "(" $CondExpr ")" $LoopBody
 class WhileNode extends GtNode {
 	/*field*/public GtNode	CondExpr;
 	/*field*/public GtNode	LoopBody;
 	WhileNode/*constructor*/(GtType Type, GtToken Token, GtNode CondExpr, GtNode LoopBody) {
 		super(Type, Token);
 		this.CondExpr = CondExpr;
 		this.LoopBody = LoopBody;
 		this.SetParent2(CondExpr, LoopBody);
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitWhileNode(this);
 	}
 }
 
 class DoWhileNode extends GtNode {
 	/*field*/public GtNode	CondExpr;
 	/*field*/public GtNode	LoopBody;
 	DoWhileNode/*constructor*/(GtType Type, GtToken Token, GtNode CondExpr, GtNode LoopBody) {
 		super(Type, Token);
 		this.CondExpr = CondExpr;
 		this.LoopBody = LoopBody;
 		this.SetParent2(CondExpr, LoopBody);
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitDoWhileNode(this);
 	}
 }
 
 //E.g., "for" "(" ";" $CondExpr ";" $IterExpr ")" $LoopNode
 class ForNode extends GtNode {
 	/*field*/public GtNode	CondExpr;
 	/*field*/public GtNode	IterExpr;
 	/*field*/public GtNode	LoopBody;
 	ForNode/*constructor*/(GtType Type, GtToken Token, GtNode CondExpr, GtNode IterExpr, GtNode LoopBody) {
 		super(Type, Token);
 		this.CondExpr = CondExpr;
 		this.LoopBody = LoopBody;
 		this.IterExpr = IterExpr;
 		this.SetParent2(CondExpr, LoopBody);
 		this.SetParent(IterExpr);
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitForNode(this);
 	}
 }
 
 //E.g., "for" "(" $Variable ":" $IterExpr ")" $LoopNode
 class ForEachNode extends GtNode {
 	/*field*/public GtNode	Variable;
 	/*field*/public GtNode	IterExpr;
 	/*field*/public GtNode	LoopBody;
 	ForEachNode/*constructor*/(GtType Type, GtToken Token, GtNode Variable, GtNode IterExpr, GtNode LoopBody) {
 		super(Type, Token);
 		this.Variable = Variable;
 		this.IterExpr = IterExpr;
 		this.LoopBody = LoopBody;
 		this.SetParent2(Variable, LoopBody);
 		this.SetParent(IterExpr);
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitForEachNode(this);
 	}
 }
 
 @Deprecated class LabelNode extends GtNode {
 	/*field*/public String Label;
 	LabelNode/*constructor*/(GtType Type, GtToken Token, String Label) {
 		super(Type, Token);
 		this.Label = Label;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitLabelNode(this);
 	}
 }
 
 @Deprecated class JumpNode extends GtNode {
 	/*field*/public String Label;
 	JumpNode/*constructor*/(GtType Type, GtToken Token, String Label) {
 		super(Type, Token);
 		this.Label = Label;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitJumpNode(this);
 	}
 }
 
 class ContinueNode extends GtNode {
 	/*field*/public String Label;
 	ContinueNode/*constructor*/(GtType Type, GtToken Token, String Label) {
 		super(Type, Token);
 		this.Label = Label;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitContinueNode(this);
 	}
 }
 
 class BreakNode extends GtNode {
 	/*field*/public String Label;
 	BreakNode/*constructor*/(GtType Type, GtToken Token, String Label) {
 		super(Type, Token);
 		this.Label = Label;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitBreakNode(this);
 	}
 }
 
 class ReturnNode extends GtNode {
 	/*field*/public GtNode Expr;
 	ReturnNode/*constructor*/(GtType Type, GtToken Token, GtNode Expr) {
 		super(Type, Token);
 		this.Expr = Expr;
 		this.SetParent(Expr);
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitReturnNode(this);
 	}
 }
 
 class ThrowNode extends GtNode {
 	/*field*/public GtNode Expr;
 	ThrowNode/*constructor*/(GtType Type, GtToken Token, GtNode Expr) {
 		super(Type, Token);
 		this.Expr = Expr;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitThrowNode(this);
 	}
 }
 
 class TryNode extends GtNode {
 	/*field*/public GtNode	TryBlock;
 	/*field*/public GtNode	CatchExpr;
 	/*field*/public GtNode	CatchBlock;
 	/*field*/public GtNode	FinallyBlock;
 	TryNode/*constructor*/(GtType Type, GtToken Token, GtNode TryBlock, GtNode CatchExpr, GtNode CatchBlock, GtNode FinallyBlock) {
 		super(Type, Token);
 		this.TryBlock = TryBlock;
 		this.CatchExpr = CatchExpr;
 		this.CatchBlock = CatchBlock;
 		this.FinallyBlock = FinallyBlock;
 		this.SetParent2(TryBlock, FinallyBlock);
 		this.SetParent2(CatchBlock, CatchExpr);
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitTryNode(this);
 	}
 }
 
 class SwitchNode extends GtNode {
 	/*field*/public GtNode	MatchNode;
 	/*field*/public GtNode	DefaultBlock;
 	/*field*/public ArrayList<GtNode> CaseList; // [expr, block, expr, block, ....]
 
 	SwitchNode/*constructor*/(GtType Type, GtToken Token, GtNode MatchNode, GtNode DefaultBlock) {
 		super(Type, Token);
 		this.MatchNode = MatchNode;
 		this.DefaultBlock = DefaultBlock;
 		this.CaseList = new ArrayList<GtNode>();
 		this.SetParent(DefaultBlock);
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitSwitchNode(this);
 	}
 	@Override public void Append(GtNode Expr) {
 		this.CaseList.add(Expr);
 		this.SetParent(Expr);		
 	}
 }
 
 class FunctionNode extends GtNode {
 	FunctionNode/*constructor*/(GtType Type, GtToken Token) {
 		super(Type, Token); // TODO
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitFunctionNode(this);
 	}
 }
 
 class ErrorNode extends GtNode {
 	ErrorNode/*constructor*/(GtType Type, GtToken Token) {
 		super(Type, Token);
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitErrorNode(this);
 	}
 	@Override public Object ToConstValue(boolean EnforceConst)  {
 		return null;
 	}
 }
 
 // E.g., "ls" "-a"..
 class CommandNode extends GtNode {
 	/*field*/public ArrayList<GtNode>  Params; /* ["ls", "-la", "/", ...] */
 	/*field*/public GtNode PipedNextNode;
 	CommandNode/*constructor*/(GtType Type, GtToken KeyToken, GtNode PipedNextNode) {
 		super(Type, KeyToken);
 		this.PipedNextNode = PipedNextNode;
 		this.Params = new ArrayList<GtNode>();
 	}
 	@Override public void Append(GtNode Expr) {
 		this.Params.add(Expr);
 		this.SetParent(Expr);
 	}
 
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitCommandNode(this);
 	}
 
 	@Override public Object ToConstValue(boolean EnforceConst) {	//FIXME: Exception
 		try {
 			return this.Type.Context.Generator.EvalCommandNode(this, EnforceConst);
 		} 
 		catch (Exception e) {
 			LibGreenTea.VerboseException(e);
 			return null;
 		}
 	}
 
 }
 
 class GtGenerator extends GreenTeaUtils {
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
 		Type.Context.ReportError(ErrorLevel, Token, this.TargetCode + " has no language support for " + Token.ParsedText);
 		return new ErrorNode(Type.Context.VoidType, ParsedTree.KeyToken);
 	}
 
 	public GtNode CreateConstNode(GtType Type, GtSyntaxTree ParsedTree, Object Value) {
 		return new ConstNode(Type, ParsedTree != null ? ParsedTree.KeyToken : GtTokenContext.NullToken, Value);
 	}
 
 	public GtNode CreateNullNode(GtType Type, GtSyntaxTree ParsedTree) {
 		return new NullNode(Type, ParsedTree.KeyToken);
 	}
 
 	public GtNode CreateArrayNode(GtType ArrayType, GtSyntaxTree ParsedTree) {
 		return new ArrayNode(ArrayType, ParsedTree.KeyToken);
 	}
 
 	public GtNode CreateLocalNode(GtType Type, GtSyntaxTree ParsedTree, String LocalName) {
 		return new LocalNode(Type, ParsedTree.KeyToken, LocalName);
 	}
 
 	public GtNode CreateGetterNode(GtType Type, GtSyntaxTree ParsedTree, GtFunc Func, GtNode Expr) {
 		return new GetterNode(Type, ParsedTree.KeyToken, Func, Expr);
 	}
 
 	public GtNode CreateIndexerNode(GtType Type, GtSyntaxTree ParsedTree, GtFunc Func, GtNode Expr) {
 		return new IndexerNode(Type, ParsedTree.KeyToken, Func, Expr);
 	}
 
 	public GtNode CreateApplyNode(GtType Type, GtSyntaxTree ParsedTree, GtFunc Func) {
 		return new ApplyNode(Type, ParsedTree == null ? GtTokenContext.NullToken : ParsedTree.KeyToken, Func);
 	}
 
 	public GtNode CreateNewNode(GtType Type, GtSyntaxTree ParsedTree, GtFunc Func, ArrayList<GtNode> NodeList) {
 		/*local*/NewNode Node = new NewNode(Type, ParsedTree.KeyToken, Func);
 		if(NodeList != null) {
 			Node.AppendNodeList(NodeList);
 		}
 		return Node;
 	}
 
 	public GtNode CreateUnaryNode(GtType Type, GtSyntaxTree ParsedTree, GtFunc Func, GtNode Expr) {
 		return new UnaryNode(Type, ParsedTree.KeyToken, Func, Expr);
 	}
 
 	public GtNode CreateSuffixNode(GtType Type, GtSyntaxTree ParsedTree, GtFunc Func, GtNode Expr) {
 		return new SuffixNode(Type, ParsedTree.KeyToken, Func, Expr);
 	}
 
 	public GtNode CreateBinaryNode(GtType Type, GtSyntaxTree ParsedTree, GtFunc Func, GtNode Left, GtNode Right) {
 		return new BinaryNode(Type, ParsedTree.KeyToken, Func, Left, Right);
 	}
 
 	public GtNode CreateAndNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Left, GtNode Right) {
 		return new AndNode(Type, ParsedTree.KeyToken, Left, Right);
 	}
 
 	public GtNode CreateOrNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Left, GtNode Right) {
 		return new OrNode(Type, ParsedTree.KeyToken, Left, Right);
 	}
 
 	public GtNode CreateInstanceOfNode(GtType Type, GtSyntaxTree ParsedTree, GtNode LeftNode, GtType GivenType) {
 		return new InstanceOfNode(Type, ParsedTree.KeyToken, LeftNode, GivenType);
 	}
 
 	public GtNode CreateAssignNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Left, GtNode Right) {
 		return new AssignNode(Type, ParsedTree.KeyToken, Left, Right);
 	}
 
 	public GtNode CreateSelfAssignNode(GtType Type, GtSyntaxTree ParsedTree, GtFunc Func, GtNode Left, GtNode Right) {
 		return new SelfAssignNode(Type, ParsedTree.KeyToken, Func, Left, Right);
 	}
 
 	public GtNode CreateVarNode(GtType Type, GtSyntaxTree ParsedTree, GtType DeclType, String VariableName, GtNode InitNode, GtNode Block) {
 		return new VarNode(Type, ParsedTree.KeyToken, DeclType, VariableName, InitNode, Block);
 	}
 
 	public GtNode CreateTrinaryNode(GtType Type, GtSyntaxTree ParsedTree, GtNode CondNode, GtNode ThenNode, GtNode ElseNode) {
 		return new TrinaryNode(Type, ParsedTree.KeyToken, CondNode, ThenNode, ElseNode);
 	}
 
 	public GtNode CreateIfNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Cond, GtNode Then, GtNode Else) {
 		return new IfNode(Type, ParsedTree.KeyToken, Cond, Then, Else);
 	}
 
 	public GtNode CreateSwitchNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Match, GtNode DefaultBlock) {
 		return new SwitchNode(Type, ParsedTree.KeyToken, Match, DefaultBlock);
 	}
 
 	public GtNode CreateWhileNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Cond, GtNode Block) {
 		return new WhileNode(Type, ParsedTree.KeyToken, Cond, Block);
 	}
 
 	public GtNode CreateDoWhileNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Cond, GtNode Block) {
 		return new DoWhileNode(Type, ParsedTree.KeyToken, Cond, Block);
 	}
 
 	public GtNode CreateForNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Cond, GtNode IterNode, GtNode Block) {
 		return new ForNode(Type, ParsedTree.KeyToken, Cond, IterNode, Block);
 	}
 
 	public GtNode CreateForEachNode(GtType Type, GtSyntaxTree ParsedTree, GtNode VarNode, GtNode IterNode, GtNode Block) {
 		return new ForEachNode(Type, ParsedTree.KeyToken, VarNode, IterNode, Block);
 	}
 
 	public GtNode CreateReturnNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Node) {
 		return new ReturnNode(Type, ParsedTree.KeyToken, Node);
 	}
 
 	public GtNode CreateLabelNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Node) {
 		return null;
 	}
 
 	public GtNode CreateJumpNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Node, String Label) {
 		return new JumpNode(Type, ParsedTree.KeyToken, Label);
 	}
 
 	public GtNode CreateBreakNode(GtType Type, GtSyntaxTree ParsedTree, String Label) {
 		return new BreakNode(Type, ParsedTree.KeyToken, Label);
 	}
 
 	public GtNode CreateContinueNode(GtType Type, GtSyntaxTree ParsedTree, String Label) {
 		return new ContinueNode(Type, ParsedTree.KeyToken, Label);
 	}
 
 	public GtNode CreateTryNode(GtType Type, GtSyntaxTree ParsedTree, GtNode TryBlock, GtNode CatchExpr, GtNode CatchNode, GtNode FinallyBlock) {
 		return new TryNode(Type, ParsedTree.KeyToken, TryBlock, CatchExpr, CatchNode, FinallyBlock);
 	}
 
 	public GtNode CreateThrowNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Node) {
 		return new ThrowNode(Type, ParsedTree.KeyToken, Node);
 	}
 
 	public GtNode CreateFunctionNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Block) {
 		return null;
 	}
 
 	public GtNode CreateEmptyNode(GtType Type) {
 		return new EmptyNode(Type, GtTokenContext.NullToken);
 	}
 
 	public GtNode CreateErrorNode(GtType Type, GtSyntaxTree ParsedTree) {
 		return new ErrorNode(ParsedTree.NameSpace.Context.VoidType, ParsedTree.KeyToken);
 	}
 
 	public GtNode CreateCommandNode(GtType Type, GtSyntaxTree ParsedTree, GtNode PipedNextNode) {
 		return new CommandNode(Type, ParsedTree.KeyToken, PipedNextNode);
 	}
 
 	/* language constructor */
 
 	public final Object ImportNativeObject(GtType ContextType, String PackageName) {
 		LibGreenTea.VerboseLog(VerboseNative, "importing " + PackageName);
 //ifdef JAVA
 		try {
 			/*local*/Class<?> NativeClass = Class.forName(PackageName);
 			return LibGreenTea.GetNativeType(this.Context, NativeClass);
 		} catch (ClassNotFoundException e) {
 			LibGreenTea.VerboseLog(VerboseException, e.toString());
 		}
 		Method NativeMethod = LibGreenTea.LoadNativeMethod(ContextType, PackageName, true/*static only*/);
 		if(NativeMethod != null) {
 			return LibGreenTea.ConvertNativeMethodToFunc(this.Context, NativeMethod);
 		}
 		//Object StaticFieldValue = LibGreenTea.LoadNativeStaticFieldValue(ClassType, Symbol);
 //endif VAJA
 		return null;
 	}
 
 	public GtType GetNativeType(Object Value) {
 		return LibGreenTea.GetNativeType(this.Context, Value);
 	}
 
 	public void OpenClassField(GtType Type, GtClassField ClassField) {
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
 		Node.SetNextNode(null);
 	}
 
 	public final boolean IsEmptyBlock(GtNode Node) {
 		return Node == null || (Node instanceof EmptyNode) && Node.GetNextNode() == null;
 	}
 
 	//------------------------------------------------------------------------
 
 	public void VisitEmptyNode(EmptyNode EmptyNode) {
 		LibGreenTea.DebugP("empty node: " + EmptyNode.Token.ParsedText);
 	}
 
 	public void VisitInstanceOfNode(InstanceOfNode Node) {
 		/*extention*/
 	}
 
 	public void VisitSelfAssignNode(SelfAssignNode Node) {
 		/*extention*/
 	}
 
 	public void VisitTrinaryNode(TrinaryNode Node) {
 		/*extension*/
 	}
 
 	public void VisitExistsNode(ExistsNode Node) {
 		/*extension*/
 	}
 
 	public void VisitCastNode(CastNode Node) {
 		/*extension*/
 	}
 
 	public void VisitSliceNode(SliceNode Node) {
 		/*extension*/
 	}
 
 	public void VisitSuffixNode(SuffixNode Node) {
 		/*extension*/
 	}
 
 	public void VisitUnaryNode(UnaryNode Node) {
 		/*extension*/
 	}
 
 	public void VisitIndexerNode(IndexerNode Node) {
 		/*extension*/
 	}
 
 	public void VisitArrayNode(ArrayNode Node) {
 		/*extension*/
 	}
 
 	public void VisitWhileNode(WhileNode Node) {
 		/*extension*/
 	}
 
 	public void VisitDoWhileNode(DoWhileNode Node) {
 		/*extension*/
 	}
 
 	public void VisitForNode(ForNode Node) {
 		/*extension*/
 	}
 
 	public void VisitForEachNode(ForEachNode Node) {
 		/*extension*/
 	}
 
 	public void VisitConstNode(ConstNode Node) {
 		/*extension*/
 	}
 
 	public void VisitNewNode(NewNode Node) {
 		/*extension*/
 	}
 
 	public void VisitNullNode(NullNode Node) {
 		/*extension*/
 	}
 
 	public void VisitLocalNode(LocalNode Node) {
 		/*extension*/
 	}
 
 	public void VisitGetterNode(GetterNode Node) {
 		/*extension*/
 	}
 
 	public void VisitApplyNode(ApplyNode Node) {
 		/*extension*/
 	}
 
 	public void VisitBinaryNode(BinaryNode Node) {
 		/*extension*/
 	}
 
 	public void VisitAndNode(AndNode Node) {
 		/*extension*/
 	}
 
 	public void VisitOrNode(OrNode Node) {
 		/*extension*/
 	}
 
 	public void VisitAssignNode(AssignNode Node) {
 		/*extension*/
 	}
 
 	public void VisitVarNode(VarNode Node) {
 		/*extension*/
 	}
 
 	public void VisitIfNode(IfNode Node) {
 		/*extension*/
 	}
 
 	public void VisitSwitchNode(SwitchNode Node) {
 		/*extension*/
 	}
 
 	public void VisitReturnNode(ReturnNode Node) {
 		/*extension*/
 	}
 
 	public void VisitLabelNode(LabelNode Node) {
 		/*extension*/
 	}
 
 	public void VisitJumpNode(JumpNode Node) {
 		/*extension*/
 	}
 
 	public void VisitBreakNode(BreakNode Node) {
 		/*extension*/
 	}
 
 	public void VisitContinueNode(ContinueNode Node) {
 		/*extension*/
 	}
 
 	public void VisitTryNode(TryNode Node) {
 		/*extension*/
 	}
 
 	public void VisitThrowNode(ThrowNode Node) {
 		/*extension*/
 	}
 
 	public void VisitFunctionNode(FunctionNode Node) {
 		/*extension*/
 	}
 
 	public void VisitErrorNode(ErrorNode Node) {
 		/*extension*/
 	}
 
 	public void VisitCommandNode(CommandNode Node) {
 		/*extension*/
 	}
 
 	public final void VisitBlock(GtNode Node) {
 		/*local*/GtNode CurrentNode = Node;
 		while(CurrentNode != null) {
 			CurrentNode.Evaluate(this);
 			CurrentNode = CurrentNode.GetNextNode();
 		}
 	}
 
 	// This must be extended in each language
 
 	public boolean IsStrictMode() {
 		return false; /* override this */
 	}
 
 	@Deprecated public Object Eval(GtNode Node) {
 		this.VisitBlock(Node);
 		return null;
 	}
 
 	// EnforceConst : 
 	public Object EvalApplyNode(ApplyNode Node, boolean EnforceConst) {
 //ifdef JAVA  this is for JavaByteCodeGenerator and JavaSourceGenerator
 		//System.err.println("@@@@ " + (Node.Func.NativeRef.getClass()));
 		if(Node.Func != null && (EnforceConst || Node.Func.Is(ConstFunc)) && Node.Func.NativeRef instanceof Method) {
 			Object RecvObject = null;
 			int StartIndex = 1;
 			if(!Node.Func.Is(NativeStaticFunc)) {
 				RecvObject = Node.NodeList.get(0).ToConstValue(EnforceConst);
 				if(RecvObject == null) {
 					return null;
 				}
 				StartIndex = 1;
 			}
 			Object[] Arguments = new Object[Node.NodeList.size() - StartIndex];
 			for(int i = 0; i < Arguments.length; i++) {
 				GtNode ArgNode = Node.NodeList.get(StartIndex+i);
 				Arguments[i] = ArgNode.ToConstValue(EnforceConst);
 				//System.err.println("@@@@ " + i + ", " + Arguments[i] + ", " + ArgNode.getClass());
 				if(Arguments[i] == null && !(ArgNode instanceof NullNode)) {
 					return null;
 				}
 			}
 			return LibGreenTea.ApplyFunc(Node.Func, RecvObject, Arguments);
 		}
 //endif VAJA
 		return null;  // if unsupported
 	}
 
 	public Object EvalArrayNode(ArrayNode Node, boolean EnforceConst) {
 		/*local*/ArrayList<Object> NewList = null;
 //ifdef JAVA  this is for JavaByteCodeGenerator and JavaSourceGenerator
 		NewList = new ArrayList<Object>(LibGreenTea.ListSize(Node.NodeList));
 		for(int i = 0; i < LibGreenTea.ListSize(Node.NodeList); i++) {
 			Object Value = Node.NodeList.get(i).ToConstValue(EnforceConst);
 			if(Value == null) {
 				return Value;
 			}
 			NewList.add(Value);
 		}
 //endif VAJA
 		return NewList;  // if unsupported
 	}
 	
 	public Object EvalCommandNode(CommandNode Node, boolean EnforceConst) throws Exception {
 //ifdef JAVA  this is for JavaByteCodeGenerator and JavaSourceGenerator
 		if(!EnforceConst) {
 			return null;
 		}
 		/*local*/ArrayList<String[]> ArgsBuffer = new ArrayList<String[]>();
 		/*local*/GtType Type = Node.Type;
 		/*local*/CommandNode CurrentNode = Node;
 		while(CurrentNode != null) {
 			/*local*/int paramSize = LibGreenTea.ListSize(Node.Params);
 			/*local*/String[] Buffer = new String[paramSize];
 			for(int i =0; i < paramSize; i++) {
				/*local*/Object Value = Node.Params.get(i).ToConstValue(EnforceConst);
 				if(!(Value instanceof String)) {
 					return null;
 				}
 				Buffer[i] = (/*cast*/String)Value;
 			}
 			ArgsBuffer.add(Buffer);
 			CurrentNode = (/*cast*/CommandNode) CurrentNode.PipedNextNode;
 		}
 		
 		/*local*/int nodeSize = LibGreenTea.ListSize(ArgsBuffer);
 		/*local*/String[][] Args = new String[nodeSize][];
 		for(int i = 0; i < nodeSize; i++) {
 			/*local*/String[] Buffer = ArgsBuffer.get(i);
 			/*local*/int commandSize = Buffer.length;
 			Args[i] = new String[commandSize];
 			for(int j = 0; j < commandSize; j++) {
 				Args[i][j] = Buffer[j];
 			}
 		}
 		
 		if(Type.equals(Type.Context.StringType)) {
 			return GtSubProc.ExecCommandString(Args);
 		}
 		else if(Type.equals(Type.Context.BooleanType)) {
 			return GtSubProc.ExecCommandBool(Args);
 		}
 		else {
 			GtSubProc.ExecCommandVoid(Args);
 		}
 //endif VAJA
 		return null;  // if unsupported
 	}
 
 	public void FlushBuffer() {
 		/*extension*/
 	}
 
 	public String BlockComment(String Comment) {
 		return "/*" + Comment + "*/";
 	}
 
 	public void StartCompilationUnit() {
 		/*extension*/
 	}
 
 	public void FinishCompilationUnit() {
 		/*extension*/
 	}
 
 	protected void PushCode(Object Code) {
 		this.GeneratedCodeStack.add(Code);
 	}
 
 	protected final Object PopCode() {
 		/*local*/int Size = this.GeneratedCodeStack.size();
 		if(Size > 0) {
 			return this.GeneratedCodeStack.remove(Size - 1);
 		}
 		return "";
 	}
 
 	public String GetRecvName() {
 		return "this";  // default 
 	}
 
 	public void InvokeMainFunc(String MainFuncName) {
 		/*extension*/
 	}
 
 	public void CloseClassField(GtType definedType, GtClassField classField) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
 
 class SourceGenerator extends GtGenerator {
 	/*field*/protected String    HeaderSource;
 	/*field*/protected String    BodySource;
 
 	/*field*/protected String    Tab;
 	/*field*/protected String    LineFeed;
 	/*field*/protected int       IndentLevel;
 	/*field*/protected String    CurrentLevelIndentString;
 
 	/*field*/protected boolean   HasLabelSupport;
 	/*field*/protected String    LogicalOrOperator;
 	/*field*/protected String    LogicalAndOperator;
 	/*field*/protected String    MemberAccessOperator;
 	/*field*/protected String    TrueLiteral;
 	/*field*/protected String    FalseLiteral;
 	/*field*/protected String    NullLiteral;
 	/*field*/protected String    LineComment;
 	/*field*/protected String    BreakKeyword;
 	/*field*/protected String    ContinueKeyword;
 	/*field*/protected String    ParameterBegin;
 	/*field*/protected String    ParameterEnd;
 	/*field*/protected String    ParameterDelimiter;
 	/*field*/protected String    SemiColon;
 	/*field*/protected String    BlockBegin;
 	/*field*/protected String    BlockEnd;
 
 	SourceGenerator/*constructor*/(String TargetCode, String OutputFile, int GeneratorFlag) {
 		super(TargetCode, OutputFile, GeneratorFlag);
 		this.LineFeed = "\n";
 		this.IndentLevel = 0;
 		this.Tab = "   ";
 		this.CurrentLevelIndentString = null;
 		this.HeaderSource = "";
 		this.BodySource = "";
 		this.HasLabelSupport = false;
 		this.LogicalOrOperator  = "||";
 		this.LogicalAndOperator = "&&";
 		this.MemberAccessOperator = ".";
 		this.TrueLiteral  = "true";
 		this.FalseLiteral = "false";
 		this.NullLiteral  = "null";
 		this.BreakKeyword = "break";
 		this.ContinueKeyword = "continue";
 		this.LineComment  = "//";
 		this.ParameterBegin = "(";
 		this.ParameterEnd = ")";
 		this.ParameterDelimiter = ",";
 		this.SemiColon = ";";
 		this.BlockBegin = "{";
 		this.BlockEnd = "}";
 	}
 
 	@Override public void InitContext(GtParserContext Context) {
 		super.InitContext(Context);
 		this.HeaderSource = "";
 		this.BodySource = "";
 	}
 
 	public final void WriteHeader(String Text) {
 		this.HeaderSource += Text;
 	}
 
 	public final void WriteLineHeader(String Text) {
 		this.HeaderSource += Text + this.LineFeed;
 	}
 
 	public final void WriteCode(String Text) {
 		this.BodySource += Text;
 	}
 
 	public final void WriteLineCode(String Text) {
 		this.BodySource += Text + this.LineFeed;
 	}
 
 	public final void WriteLineComment(String Text) {
 		this.BodySource += this.LineComment + " " + Text + this.LineFeed;
 	}
 
 	public final void FlushErrorReport() {
 		this.WriteLineCode("");
 		/*local*/String[] Reports = this.Context.GetReportedErrors();
 		/*local*/int i = 0;
 		while(i < Reports.length) {
 			this.WriteLineComment(Reports[i]);
 			i = i + 1;
 		}
 		this.WriteLineCode("");		
 	}
 
 	@Override public void FlushBuffer() {
 		LibGreenTea.WriteCode(this.OutputFile, this.HeaderSource + this.BodySource);			
 		this.HeaderSource = "";
 		this.BodySource = "";
 	}
 
 	/* GeneratorUtils */
 
 	public final void Indent() {
 		this.IndentLevel += 1;
 		this.CurrentLevelIndentString = null;
 	}
 
 	public final void UnIndent() {
 		this.IndentLevel -= 1;
 		this.CurrentLevelIndentString = null;
 		LibGreenTea.Assert(this.IndentLevel >= 0);
 	}
 
 	public final String GetIndentString() {
 		if(this.CurrentLevelIndentString == null) {
 			this.CurrentLevelIndentString = JoinStrings(this.Tab, this.IndentLevel);
 		}
 		return this.CurrentLevelIndentString;
 	}
 
 	public String VisitBlockWithIndent(GtNode Node, boolean NeedBlock) {
 		/*local*/String Code = "";
 		if(NeedBlock) {
 			Code += this.BlockBegin + this.LineFeed;
 			this.Indent();
 		}
 		/*local*/GtNode CurrentNode = Node;
 		while(CurrentNode != null) {
 			if(!this.IsEmptyBlock(CurrentNode)) {
 				/*local*/String Stmt = this.VisitNode(CurrentNode);
 				if(!LibGreenTea.EqualsString(Stmt, "")) {
 					Code += this.GetIndentString() + Stmt + this.SemiColon + this.LineFeed;
 				}
 			}
 			CurrentNode = CurrentNode.GetNextNode();
 		}
 		if(NeedBlock) {
 			this.UnIndent();
 			Code += this.GetIndentString() + this.BlockEnd;
 		}
 //		else if(Code.length() > 0) {
 //			Code = Code.substring(0, Code.length() - 1);
 //		}
 		return Code;
 	}
 
 	protected String StringifyConstValue(Object ConstValue) {
 		if(ConstValue == null) {
 			return this.NullLiteral;
 		}
 		if(ConstValue instanceof Boolean) {
 			if(ConstValue.equals(true)) {
 				return this.TrueLiteral;
 			}
 			else {
 				return this.FalseLiteral;
 			}
 		}
 		if(ConstValue instanceof String) {
 			return LibGreenTea.QuoteString((/*cast*/String)ConstValue);
 		}
 		if(ConstValue instanceof GreenTeaEnum) {
 			return "" + ((/*cast*/GreenTeaEnum) ConstValue).EnumValue;
 		}
 		return ConstValue.toString();
 	}
 
 	protected String GetNewOperator(GtType Type) {
 		return "new " + Type.ShortClassName + "()";
 	}
 
 	protected final void PushSourceCode(String Code) {
 		this.PushCode(Code);
 	}
 
 	protected final String PopSourceCode() {
 		return (/*cast*/String) this.PopCode();
 	}
 
 	public final String VisitNode(GtNode Node) {
 		Node.Evaluate(this);
 		return this.PopSourceCode();
 	}
 
 	public final String JoinCode(String BeginCode, int BeginIdx, String[] ParamCode, String EndCode, String Delim) {
 		/*local*/String JoinedCode = BeginCode;
 		/*local*/int i = BeginIdx;
 		while(i < ParamCode.length) {
 			/*local*/String P = ParamCode[i];
 			if(i != BeginIdx) {
 				JoinedCode += Delim;
 			}
 			JoinedCode += P;
 			i = i + 1;
 		}
 		return JoinedCode + EndCode;
 	}
 
 	public final static String GenerateApplyFunc1(GtFunc Func, String FuncName, boolean IsSuffixOp, String Arg1) {
 		/*local*/String Macro = null;
 		if(Func != null) {
 			FuncName = Func.GetNativeFuncName();
 			if(IsFlag(Func.FuncFlag, NativeMacroFunc)) {
 				Macro = Func.GetNativeMacro();
 			}
 		}
 		if(Macro == null) {
 			if(IsSuffixOp) {
 				Macro = "$1 " + FuncName;
 			}
 			else {
 				Macro = FuncName + " $1";
 			}
 		}
 		return Macro.replace("$1", Arg1);
 	}
 
 	public final static String GenerateApplyFunc2(GtFunc Func, String FuncName, String Arg1, String Arg2) {
 		/*local*/String Macro = null;
 		if(Func != null) {
 			FuncName = Func.GetNativeFuncName();
 			if(IsFlag(Func.FuncFlag, NativeMacroFunc)) {
 				Macro = Func.GetNativeMacro();
 			}
 		}
 		if(Macro == null) {
 			Macro = "$1 " + FuncName + " $2";
 		}
 		return Macro.replace("$1", Arg1).replace("$2", Arg2);
 	}
 
 	public String GenerateFuncTemplate(int ParamSize, GtFunc Func) {
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
 			Template += this.ParameterBegin;
 			while(i < ParamSize) {
 				if(i != BeginIdx) {
 					Template += this.ParameterDelimiter + " ";
 				}
 				Template += "$" + i;
 				i = i + 1;
 			}
 			Template += this.ParameterEnd;
 		}
 		return Template;
 	}
 
 	public final String ApplyMacro(String Template, ArrayList<GtNode> NodeList) {
 		/*local*/int ParamSize = LibGreenTea.ListSize(NodeList);
 		/*local*/int ParamIndex = ParamSize - 1;
 		while(ParamIndex >= 1) {
 			/*local*/String Param = this.VisitNode(NodeList.get(ParamIndex));
 			Template = Template.replace("$" + ParamIndex, Param);
 			ParamIndex = ParamIndex - 1;
 		}
 		return Template;
 	}
 	public final String ApplyMacro2(String Template, String[] ParamList) {
 		/*local*/int ParamSize = ParamList.length;
 		/*local*/int ParamIndex = ParamSize - 1;
 		while(ParamIndex >= 1) {
 			/*local*/String Param = ParamList[ParamIndex];
 			Template = Template.replace("$" + ParamIndex, Param);
 			ParamIndex = ParamIndex - 1;
 		}
 		return Template;
 	}
 
 	public final String GenerateApplyFunc(ApplyNode Node) {
 		/*local*/int ParamSize = LibGreenTea.ListSize(Node.NodeList);
 		/*local*/String Template = this.GenerateFuncTemplate(ParamSize, Node.Func);
 		return this.ApplyMacro(Template, Node.NodeList);
 	}
 
 	// Visitor API
 	@Override public void VisitEmptyNode(EmptyNode Node) {
 		this.PushSourceCode("");
 	}
 
 	@Override public void VisitInstanceOfNode(InstanceOfNode Node) {
 		this.PushSourceCode(this.VisitNode(Node.ExprNode) + " instanceof " + Node.TypeInfo);
 	}
 
 	@Override public final void VisitConstNode(ConstNode Node) {
 		this.PushSourceCode(this.StringifyConstValue(Node.ConstValue));
 	}
 
 	@Override public final void VisitNullNode(NullNode Node) {
 		this.PushSourceCode(this.NullLiteral);
 	}
 
 	@Override public void VisitLocalNode(LocalNode Node) {
 		this.PushSourceCode(Node.NativeName);
 	}
 
 	@Override public void VisitReturnNode(ReturnNode Node) {
 		/*local*/String Code = "return";
 		if(Node.Expr != null) {
 			Code += " " + this.VisitNode(Node.Expr);
 		}
 		this.PushSourceCode(Code);
 		this.StopVisitor(Node);
 	}
 
 	@Override public void VisitIndexerNode(IndexerNode Node) {
 		this.PushSourceCode(this.VisitNode(Node.Expr) + "[" + this.VisitNode(Node.GetAt(0)) + "]"); // FIXME: Multi
 	}
 
 	@Override public final void VisitNewNode(NewNode Node) {
 		/*local*/int ParamSize = LibGreenTea.ListSize(Node.Params);
 		/*local*/String NewOperator = this.GetNewOperator(Node.Type);
 		/*local*/String Template = this.GenerateFuncTemplate(ParamSize, Node.Func);
 		Template = Template.replace("$1", NewOperator);
 		this.PushSourceCode(this.ApplyMacro(Template, Node.Params));
 	}
 
 	@Override public void VisitApplyNode(ApplyNode Node) {
 		/*local*/String Program = this.GenerateApplyFunc(Node);
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitSuffixNode(SuffixNode Node) {
 		/*local*/String FuncName = Node.Token.ParsedText;
 		/*local*/String Expr = this.VisitNode(Node.Expr);
 		if(LibGreenTea.EqualsString(FuncName, "++")) {
 		}
 		else if(LibGreenTea.EqualsString(FuncName, "--")) {
 		}
 		else {
 			LibGreenTea.DebugP(FuncName + " is not supported suffix operator!!");
 		}
 		this.PushSourceCode("(" + SourceGenerator.GenerateApplyFunc1(Node.Func, FuncName, true, Expr) + ")");
 	}
 
 	@Override public void VisitSelfAssignNode(SelfAssignNode Node) {
 		/*local*/String FuncName = Node.Token.ParsedText;
 		/*local*/String Left = this.VisitNode(Node.LeftNode);
 		/*local*/String Right = this.VisitNode(Node.RightNode);
 		this.PushSourceCode("(" + Left + " = " + SourceGenerator.GenerateApplyFunc2(Node.Func, FuncName, Left, Right) + ")");
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
 
 	@Override public void VisitGetterNode(GetterNode Node) {
 		this.PushSourceCode(this.VisitNode(Node.Expr) + this.MemberAccessOperator + Node.Func.FuncName);
 	}
 	@Override public void VisitAssignNode(AssignNode Node) {
 		this.PushSourceCode(this.VisitNode(Node.LeftNode) + " = " + this.VisitNode(Node.RightNode));
 	}
 
 	@Override public void VisitAndNode(AndNode Node) {
 		/*local*/String Left = this.VisitNode(Node.LeftNode);
 		/*local*/String Right = this.VisitNode(Node.RightNode);
 		this.PushSourceCode("(" + Left + " " + this.LogicalAndOperator +" " + Right + ")");
 	}
 
 	@Override public void VisitOrNode(OrNode Node) {
 		/*local*/String Left = this.VisitNode(Node.LeftNode);
 		/*local*/String Right = this.VisitNode(Node.RightNode);
 		this.PushSourceCode("(" + Left + " " + this.LogicalOrOperator +" " + Right + ")");
 	}
 
 	@Override public void VisitTrinaryNode(TrinaryNode Node) {
 		/*local*/String CondExpr = this.VisitNode(Node.CondExpr);
 		/*local*/String ThenExpr = this.VisitNode(Node.ThenExpr);
 		/*local*/String ElseExpr = this.VisitNode(Node.ElseExpr);
 		this.PushSourceCode("((" + CondExpr + ")? " + ThenExpr + " : " + ElseExpr + ")");
 	}
 
 	@Override public void VisitBreakNode(BreakNode Node) {
 		/*local*/String Code = this.BreakKeyword;
 		if(this.HasLabelSupport) {
 			/*local*/String Label = Node.Label;
 			if(Label != null) {
 				Code += " " + Label;
 			}
 		}
 		this.PushSourceCode(Code);
 		this.StopVisitor(Node);
 	}
 
 	@Override public void VisitContinueNode(ContinueNode Node) {
 		/*local*/String Code = this.ContinueKeyword;
 		if(this.HasLabelSupport) {
 			/*local*/String Label = Node.Label;
 			if(Label != null) {
 				Code += " " + Label;
 			}
 		}
 		this.PushSourceCode(Code);
 		this.StopVisitor(Node);
 	}
 
 	@Override public void VisitSwitchNode(SwitchNode Node) {
 		/*local*/String Code = "switch (" + this.VisitNode(Node.MatchNode) + ") {" + this.LineFeed;
 		/*local*/int i = 0;
 		while(i < Node.CaseList.size()) {
 			/*local*/GtNode Case  = Node.CaseList.get(i);
 			/*local*/GtNode Block = Node.CaseList.get(i+1);
 			Code += this.GetIndentString() + "case " + this.VisitNode(Case) + ":";
 			if(this.IsEmptyBlock(Block)) {
 				this.Indent();
 				Code += this.LineFeed + this.GetIndentString() + "/* fall-through */" + this.LineFeed;
 				this.UnIndent();
 			}
 			else {
 				Code += this.VisitBlockWithIndent(Block, true) + this.LineFeed;
 			}
 			i = i + 2;
 		}
 		if(Node.DefaultBlock != null) {
 			Code += this.GetIndentString() + "default: ";
 			Code += this.VisitBlockWithIndent(Node.DefaultBlock, true) + this.LineFeed;
 		}
 		Code += this.GetIndentString() + "}";
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitLabelNode(LabelNode Node) {
 //		/*local*/String Label = Node.Label;
 //		this.PushSourceCode(Label + ":");
 	}
 
 	@Override public void VisitJumpNode(JumpNode Node) {
 //		/*local*/String Label = Node.Label;
 //		this.PushSourceCode("goto " + Label);
 //		this.StopVisitor(Node);
 	}
 }
