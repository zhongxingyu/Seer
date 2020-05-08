 
 import java.util.ArrayList;
 // GreenTea Generator should be written in each language.
 
 class TypedNode extends GtStatic {
 	/*field*/public TypedNode	ParentNode;
 	/*field*/public TypedNode	PrevNode;
 	/*field*/public TypedNode	NextNode;
 
 	/*field*/public GtType	Type;
 	/*field*/public GtToken	Token;
 
 	TypedNode/*constructor*/(GtType Type, GtToken Token) {
 		this.Type = Type;
 		this.Token = Token;
 		this.ParentNode = null;
 		this.PrevNode = null;
 		this.NextNode = null;
 	}
 
 	public final TypedNode MoveHeadNode() {
 		/*local*/TypedNode Node = this;
 		while(Node.PrevNode != null) {
 			Node = Node.PrevNode;
 		}
 		return Node;
 	}
 
 	public final TypedNode MoveTailNode() {
 		/*local*/TypedNode Node = this;
 		while(Node.NextNode != null) {
 			Node = Node.NextNode;
 		}
 		return this;
 	}
 
 	public void Append(TypedNode Node) {
 		/*extension*/
 	}
 
 	public void Evaluate(GreenTeaGenerator Visitor) {
 		Visitor.VisitEmptyNode(this);  /* must override */
 	}
 
 	public final boolean IsError() {
 		return (this instanceof ErrorNode);
 	}
 
 	@Override public String toString() {
 		return "(TypedNode)";
 	}
 
 	public static String Stringify(TypedNode Block) {
 		/*local*/String Text = Block.toString();
 		while(Block != null) {
 			Text += Block.toString() + " ";
 			Block = Block.NextNode;
 		}
 		return Text;
 	}
 
 	public int CountForrowingNode() {
 		/*local*/int n = 0;
 		/*local*/TypedNode node = this;
 		while(node != null){
 			n++;
 			node = node.NextNode;
 		}
 		return n;
 	}
 }
 
 class UnaryNode extends TypedNode {
 	/*field*/public GtMethod    Method;
 	/*field*/public TypedNode	Expr;
 	UnaryNode/*constructor*/(GtType Type, GtToken Token, GtMethod Method, TypedNode Expr) {
 		super(Type, Token);
 		this.Method = Method;
 		this.Expr = Expr;
 	}
 	@Override public void Evaluate(GreenTeaGenerator Visitor) {
 		Visitor.VisitUnaryNode(this);
 	}
 }
 
 class SuffixNode extends TypedNode {
 	/*field*/public GtMethod    Method;
 	/*field*/public TypedNode	Expr;
 	SuffixNode/*constructor*/(GtType Type, GtToken Token, GtMethod Method, TypedNode Expr) {
 		super(Type, Token);
 		this.Method = Method;
 		this.Expr = Expr;
 	}
 	@Override public void Evaluate(GreenTeaGenerator Visitor) {
 		Visitor.VisitSuffixNode(this);
 	}
 }
 
 class BinaryNode extends TypedNode {
 	/*field*/public GtMethod    Method;
 	/*field*/public TypedNode   LeftNode;
 	/*field*/public TypedNode	RightNode;
 	BinaryNode/*constructor*/(GtType Type, GtToken Token, GtMethod Method, TypedNode Left, TypedNode Right) {
 		super(Type, Token);
 		this.Method = Method;
 		this.LeftNode  = Left;
 		this.RightNode = Right;
 	}
 	@Override public void Evaluate(GreenTeaGenerator Visitor) {
 		Visitor.VisitBinaryNode(this);
 	}
 }
 
 class AndNode extends TypedNode {
 	/*field*/public TypedNode   LeftNode;
 	/*field*/public TypedNode	RightNode;
 	AndNode/*constructor*/(GtType Type, GtToken Token, TypedNode Left, TypedNode Right) {
 		super(Type, Token);
 		this.LeftNode  = Left;
 		this.RightNode = Right;
 	}
 	@Override public void Evaluate(GreenTeaGenerator Visitor) {
 		Visitor.VisitAndNode(this);
 	}
 	@Override public String toString() {
 		return "(And:" + this.Type + " " + TypedNode.Stringify(this.LeftNode) + ", " + TypedNode.Stringify(this.RightNode) + ")";
 	}
 }
 
 class OrNode extends TypedNode {
 	/*field*/public TypedNode   LeftNode;
 	/*field*/public TypedNode	RightNode;
 	OrNode/*constructor*/(GtType Type, GtToken Token, TypedNode Left, TypedNode Right) {
 		super(Type, Token);
 		this.LeftNode  = Left;
 		this.RightNode = Right;
 	}
 	@Override public void Evaluate(GreenTeaGenerator Visitor) {
 		Visitor.VisitOrNode(this);
 	}
 	@Override public String toString() {
 		return "(Or:" + this.Type + " " + TypedNode.Stringify(this.LeftNode) + ", " + TypedNode.Stringify(this.RightNode) + ")";
 	}
 }
 
 class GetterNode extends TypedNode {
 	/*field*/public TypedNode Expr;
 	/*field*/public GtMethod  Method;
 	GetterNode/*constructor*/(GtType Type, GtToken Token, GtMethod Method, TypedNode Expr) {
 		super(Type, Token);
 		this.Method = Method;
 		this.Expr = Expr;
 	}
 	@Override public void Evaluate(GreenTeaGenerator Visitor) {
 		Visitor.VisitGetterNode(this);
 	}
 	@Override public String toString() {
 		return "(Getter:" + this.Type + " " + TypedNode.Stringify(this.Expr) + ", " + Method.MethodName + ")";
 	}
 }
 
 class IndexerNode extends TypedNode {
 	/*field*/public GtMethod  Method;
 	/*field*/public TypedNode Expr;
 	/*field*/public TypedNode Indexer;
 	IndexerNode/*constructor*/(GtType Type, GtToken Token, GtMethod Method, TypedNode Expr, TypedNode Indexer) {
 		super(Type, Token);
 		this.Method = Method;
 		this.Expr = Expr;
 		this.Indexer = Indexer;
 	}
 	@Override public void Evaluate(GreenTeaGenerator Visitor) {
 		Visitor.VisitIndexerNode(this);
 	}
 	@Override public String toString() {
 		return "(Index:" + this.Type + " " + TypedNode.Stringify(this.Expr) + ", " + TypedNode.Stringify(this.Indexer) + ")";
 	}
 }
 
 class AssignNode extends TypedNode {
 	/*field*/public TypedNode   LeftNode;
 	/*field*/public TypedNode	RightNode;
 	AssignNode/*constructor*/(GtType Type, GtToken Token, TypedNode Left, TypedNode Right) {
 		super(Type, Token);
 		this.LeftNode  = Left;
 		this.RightNode = Right;
 	}
 	@Override public void Evaluate(GreenTeaGenerator Visitor) {
 		Visitor.VisitAssignNode(this);
 	}
 	@Override public String toString() {
 		return "(Assign:" + this.Type + " " + TypedNode.Stringify(this.LeftNode) + " = " + TypedNode.Stringify(this.RightNode) + ")";
 	}
 }
 
 class ConstNode extends TypedNode {
 	/*field*/public Object	ConstValue;
 	ConstNode/*constructor*/(GtType Type, GtToken Token, Object ConstValue) {
 		super(Type, Token);
 		this.ConstValue = ConstValue;
 	}
 	@Override public void Evaluate(GreenTeaGenerator Visitor) {
 		Visitor.VisitConstNode(this);
 	}
 	@Override public String toString() {
 		return "(Const:" + this.Type + " "+ ConstValue.toString() + ")";
 	}
 }
 
 class LocalNode extends TypedNode {
 	/*field*/public String LocalName;
 	LocalNode/*constructor*/(GtType Type, GtToken Token, String LocalName) {
 		super(Type, Token);
 		this.LocalName = LocalName;
 	}
 	@Override public void Evaluate(GreenTeaGenerator Visitor) {
 		Visitor.VisitLocalNode(this);
 	}
 	@Override public String toString() {
 		return "(Local:" + this.Type + " " + LocalName + ")";
 	}
 }
 
 class NullNode extends TypedNode {
 	NullNode/*constructor*/(GtType Type, GtToken Token) {
 		super(Type, Token);
 	}
 	@Override public void Evaluate(GreenTeaGenerator Visitor) {
 		Visitor.VisitNullNode(this);
 	}
 	@Override public String toString() {
 		return "(Null:" + this.Type + " " + ")";
 	}
 }
 
 class LetNode extends TypedNode {
 	/*field*/public GtType	    DeclType;
 	/*field*/public TypedNode	VarNode;
 	/*field*/public TypedNode	BlockNode;
 	/* let VarNode in Block end */
 	LetNode/*constructor*/(GtType Type, GtToken Token, GtType DeclType, TypedNode VarNode, TypedNode Block) {
 		super(Type, Token);
 		this.DeclType = DeclType;
 		this.VarNode  = VarNode;
 		this.BlockNode = Block;
 	}
 	@Override public void Evaluate(GreenTeaGenerator Visitor) {
 		Visitor.VisitLetNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String Block = TypedNode.Stringify(this.BlockNode);
 		return "(Let:" + this.Type + " " + TypedNode.Stringify(this.VarNode) + " in {" + Block + "})";
 	}
 }
 
 class ApplyNode extends TypedNode {
 	/*field*/public GtMethod	Method;
 	/*field*/public ArrayList<TypedNode>  Params; /* [this, arg1, arg2, ...] */
 	ApplyNode/*constructor*/(GtType Type, GtToken KeyToken, GtMethod Method) {
 		super(Type, KeyToken);
 		this.Method = Method;
 		this.Params = new ArrayList<TypedNode>();
 	}
 	@Override public void Append(TypedNode Expr) {
 		this.Params.add(Expr);
 	}
 
 	@Override public void Evaluate(GreenTeaGenerator Visitor) {
 		Visitor.VisitApplyNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String Param = "";
 		for(/*local*/int i = 0; i < Params.size(); i++) {
 			TypedNode Node = Params.get(i);
 			if(i != 0) {
 				Param += ", ";
 			}
 			Param += TypedNode.Stringify(Node);
 		}
 		return "(Apply:" + this.Type + " " + Param + ")";
 	}
 }
 
 
 class MessageNode extends TypedNode {
 	/*field*/public GtMethod	Method;
 	/*field*/public ArrayList<TypedNode>  Params; /* [this, arg1, arg2, ...] */
 	MessageNode/*constructor*/(GtType Type, GtToken KeyToken, GtMethod Method) {
 		super(Type, KeyToken);
 		this.Method = Method;
 		this.Params = new ArrayList<TypedNode>();
 	}
 	@Override public void Append(TypedNode Expr) {
 		this.Params.add(Expr);
 	}
 
 	@Override public void Evaluate(GreenTeaGenerator Visitor) {
 		Visitor.VisitMessageNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String Param = "";
 		for(/*local*/int i = 0; i < Params.size(); i++) {
 			TypedNode Node = Params.get(i);
 			if(i != 0) {
 				Param += ", ";
 			}
 			Param += TypedNode.Stringify(Node);
 		}
 		return "(Message:" + this.Type + " " + Param + ")";
 	}
 }
 
 class NewNode extends TypedNode {
 	/*field*/public ArrayList<TypedNode>	Params; /* [this, arg1, arg2, ...] */
 	NewNode/*constructor*/(GtType Type, GtToken Token) {
 		super(Type, Token);
 		this.Params = new ArrayList<TypedNode>();
 	}
 	@Override public void Append(TypedNode Expr) {
 		this.Params.add(Expr);
 	}
 	@Override public void Evaluate(GreenTeaGenerator Visitor) {
 		Visitor.VisitNewNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String Param = "";
 		for(/*local*/int i = 0; i < Params.size(); i++) {
 			TypedNode Node = Params.get(i);
 			if(i != 0) {
 				Param += ", ";
 			}
 			Param += TypedNode.Stringify(Node);
 		}
 		return "(New:" + this.Type + " " + Param + ")";
 	}
 }
 
 class IfNode extends TypedNode {
 	/*field*/public TypedNode	CondExpr;
 	/*field*/public TypedNode	ThenNode;
 	/*field*/public TypedNode	ElseNode;
 	/* If CondExpr then ThenBlock else ElseBlock */
 	IfNode/*constructor*/(GtType Type, GtToken Token, TypedNode CondExpr, TypedNode ThenBlock, TypedNode ElseNode) {
 		super(Type, Token);
 		this.CondExpr = CondExpr;
 		this.ThenNode = ThenBlock;
 		this.ElseNode = ElseNode;
 	}
 	@Override public void Evaluate(GreenTeaGenerator Visitor) {
 		Visitor.VisitIfNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String Cond = TypedNode.Stringify(this.CondExpr);
 		/*local*/String Then = TypedNode.Stringify(this.ThenNode);
 		/*local*/String Else = TypedNode.Stringify(this.ElseNode);
 		return "(If:" + this.Type + " Cond:" + Cond + " Then:"+ Then + " Else:" + Else + ")";
 	}
 }
 
 class WhileNode extends TypedNode {
 	/*field*/public TypedNode	CondExpr;
 	/*field*/public TypedNode	LoopBody;
 	WhileNode/*constructor*/(GtType Type, GtToken Token, TypedNode CondExpr, TypedNode LoopBody) {
 		super(Type, Token);
 		this.CondExpr = CondExpr;
 		this.LoopBody = LoopBody;
 	}
 	@Override public void Evaluate(GreenTeaGenerator Visitor) {
 		Visitor.VisitWhileNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String Cond = TypedNode.Stringify(this.CondExpr);
 		/*local*/String Body = TypedNode.Stringify(this.LoopBody);
 		return "(While:" + this.Type + " Cond:" + Cond + " Body:"+ Body + ")";
 	}
 }
 
 class DoWhileNode extends TypedNode {
 	/*field*/public TypedNode	CondExpr;
 	/*field*/public TypedNode	LoopBody;
 	DoWhileNode/*constructor*/(GtType Type, GtToken Token, TypedNode CondExpr, TypedNode LoopBody) {
 		super(Type, Token);
 		this.CondExpr = CondExpr;
 		this.LoopBody = LoopBody;
 	}
 	@Override public void Evaluate(GreenTeaGenerator Visitor) {
 		Visitor.VisitDoWhileNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String Cond = TypedNode.Stringify(this.CondExpr);
 		/*local*/String Body = TypedNode.Stringify(this.LoopBody);
 		return "(DoWhile:" + this.Type + " Cond:" + Cond + " Body:"+ Body + ")";
 	}
 }
 
 class ForNode extends TypedNode {
 	/*field*/public TypedNode   InitNode;
 	/*field*/public TypedNode	CondExpr;
 	/*field*/public TypedNode	IterExpr;
 	/*field*/public TypedNode	LoopBody;
 	ForNode/*constructor*/(GtType Type, GtToken Token, TypedNode CondExpr, TypedNode IterExpr, TypedNode LoopBody) {
 		super(Type, Token);
 		this.CondExpr = CondExpr;
 		this.LoopBody = LoopBody;
 		this.IterExpr = IterExpr;
 	}
 	@Override public void Evaluate(GreenTeaGenerator Visitor) {
 		Visitor.VisitForNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String Cond = TypedNode.Stringify(this.CondExpr);
 		/*local*/String Body = TypedNode.Stringify(this.LoopBody);
 		/*local*/String Iter = TypedNode.Stringify(this.IterExpr);
 		return "(For:" + this.Type + " Cond:" + Cond + " Body:"+ Body + " Iter:" + Iter + ")";
 	}
 }
 
 class ForEachNode extends TypedNode {
 	/*field*/public TypedNode	CondExpr;
 	/*field*/public TypedNode	Variable;
 	/*field*/public TypedNode	IterExpr;
 	/*field*/public TypedNode	LoopBody;
 	ForEachNode/*constructor*/(GtType Type, GtToken Token, TypedNode Variable, TypedNode IterExpr, TypedNode LoopBody) {
 		super(Type, Token);
 		this.Variable = Variable;
 		this.IterExpr = IterExpr;
 		this.LoopBody = LoopBody;
 	}
 	@Override public void Evaluate(GreenTeaGenerator Visitor) {
 		Visitor.VisitForEachNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String Cond = TypedNode.Stringify(this.CondExpr);
 		/*local*/String Var = TypedNode.Stringify(this.Variable);
 		/*local*/String Body = TypedNode.Stringify(this.LoopBody);
 		/*local*/String Iter = TypedNode.Stringify(this.IterExpr);
 		return "(Foreach:" + this.Type + " Var:" + Var + " Cond:" + Cond + " Body:"+ Body + " Iter:" + Iter + ")";
 	}
 }
 
 class LoopNode extends TypedNode {
 	/*field*/public TypedNode	CondExpr;
 	/*field*/public TypedNode	LoopBody;
 	/*field*/public TypedNode	IterExpr;
 	LoopNode/*constructor*/(GtType Type, GtToken Token, TypedNode CondExpr, TypedNode LoopBody, TypedNode IterExpr) {
 		super(Type, Token);
 		this.CondExpr = CondExpr;
 		this.LoopBody = LoopBody;
 		this.IterExpr = IterExpr;
 	}
 	@Override public void Evaluate(GreenTeaGenerator Visitor) {
 		Visitor.VisitLoopNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String Cond = TypedNode.Stringify(this.CondExpr);
 		/*local*/String Body = TypedNode.Stringify(this.LoopBody);
 		return "(Loop:" + this.Type + " Cond:" + Cond + " Body:"+ Body + ")";
 	}
 }
 
 class LabelNode extends TypedNode {
 	/*field*/public String Label;
 	LabelNode/*constructor*/(GtType Type, GtToken Token, String Label) {
 		super(Type, Token);
 		this.Label = Label;
 	}
 	@Override public void Evaluate(GreenTeaGenerator Visitor) {
 		Visitor.VisitLabelNode(this);
 	}
 	@Override public String toString() {
 		return "(Label:" + this.Type + " " + Label + ")";
 	}
 }
 
 class JumpNode extends TypedNode {
 	/*field*/public String Label;
 	JumpNode/*constructor*/(GtType Type, GtToken Token, String Label) {
 		super(Type, Token);
 		this.Label = Label;
 	}
 	@Override public void Evaluate(GreenTeaGenerator Visitor) {
 		Visitor.VisitJumpNode(this);
 	}
 	@Override public String toString() {
 		return "(Jump:" + this.Type + " " + Label + ")";
 	}
 }
 
 class ContinueNode extends TypedNode {
 	/*field*/public String Label;
 	ContinueNode/*constructor*/(GtType Type, GtToken Token, String Label) {
 		super(Type, Token);
 		this.Label = Label;
 	}
 	@Override public void Evaluate(GreenTeaGenerator Visitor) {
 		Visitor.VisitContinueNode(this);
 	}
 	@Override public String toString() {
 		return "(Continue:" + this.Type + ")";
 	}
 }
 
 class BreakNode extends TypedNode {
 	/*field*/public String Label;
 	BreakNode/*constructor*/(GtType Type, GtToken Token, String Label) {
 		super(Type, Token);
 		this.Label = Label;
 	}
 	@Override public void Evaluate(GreenTeaGenerator Visitor) {
 		Visitor.VisitBreakNode(this);
 	}
 	@Override public String toString() {
 		return "(Break:" + this.Type + ")";
 	}
 }
 
 class ReturnNode extends TypedNode {
 	/*field*/public TypedNode Expr;
 	ReturnNode/*constructor*/(GtType Type, GtToken Token, TypedNode Expr) {
 		super(Type, Token);
 		this.Expr = Expr;
 	}
 	@Override public void Evaluate(GreenTeaGenerator Visitor) {
 		Visitor.VisitReturnNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String Text = "";
 		if(Text != null) {
 			Text = TypedNode.Stringify(this.Expr);
 		}
 		return "(Return:" + this.Type + " " + Text + ")";
 	}
 }
 
 class ThrowNode extends TypedNode {
 	/*field*/public TypedNode Expr;
 	ThrowNode/*constructor*/(GtType Type, GtToken Token, TypedNode Expr) {
 		super(Type, Token);
 		this.Expr = Expr;
 	}
 	@Override public void Evaluate(GreenTeaGenerator Visitor) {
 		Visitor.VisitThrowNode(this);
 	}
 	@Override public String toString() {
 		return "(Throw:" + this.Type + " " + TypedNode.Stringify(this.Expr) + ")";
 	}
 }
 
 class TryNode extends TypedNode {
 	public TypedNode	TryBlock;
 	public TypedNode	CatchBlock;
 	public TypedNode	FinallyBlock;
 	TryNode/*constructor*/(GtType Type, GtToken Token, TypedNode TryBlock, TypedNode FinallyBlock) {
 		super(Type, Token);
 		this.TryBlock = TryBlock;
 		this.FinallyBlock = FinallyBlock;
 		this.CatchBlock = null;
 	}
 //	public void addCatchBlock(TypedNode TargetException, TypedNode CatchBlock) { //FIXME
 //		this.TargetException.add(TargetException);
 //		this.CatchBlock.add(CatchBlock);
 //	}
 	@Override public void Evaluate(GreenTeaGenerator Visitor) {
 		Visitor.VisitTryNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String TryBlock = TypedNode.Stringify(this.TryBlock);
 		return "(Try:" + this.Type + " " + TryBlock + ")";
 	}
 }
 
 class SwitchNode extends TypedNode {
 	SwitchNode/*constructor*/(GtType Type, GtToken Token) {
 		super(Type, Token);
 	}
 //	public TypedNode	CondExpr;
 //	public ArrayList<TypedNode>	Labels;
 //	public ArrayList<TypedNode>	Blocks;
 	@Override public void Evaluate(GreenTeaGenerator Visitor) {
 		Visitor.VisitSwitchNode(this);
 	}
 	@Override public String toString() {
 		//FIXME
 		return "(Switch:" + this.Type + ")";
 	}
 }
 
 class FunctionNode extends TypedNode {
 	FunctionNode/*constructor*/(GtType Type, GtToken Token) {
 		super(Type, Token); // TODO
 	}
 	@Override public void Evaluate(GreenTeaGenerator Visitor) {
 		Visitor.VisitFunctionNode(this);
 	}
 	@Override public String toString() {
 		return "(Function:" + this.Type + ")";
 	}
 }
 
 class ErrorNode extends TypedNode {
 	ErrorNode/*constructor*/(GtType Type, GtToken Token) {
 		super(Type, Token);
 	}
 	@Override public void Evaluate(GreenTeaGenerator Visitor) {
 		Visitor.VisitErrorNode(this);
 	}
 	@Override public String toString() {
 		return "(Error:" + this.Type + " " + Token.toString() + ")";
 	}
 }
 
 class IndentGenerator {
 	/*field*/private int    IndentLevel					= 0;
 	/*field*/private String CurrentLevelIndentString	= "";
 	/*field*/private String IndentString				= "\t";
 
 	public IndentGenerator/*constructor*/() {
 	}
 
 	public IndentGenerator/*constructor*/(int Tabstop) {
 		this.IndentString = IndentGenerator.Repeat(" ", Tabstop);
 	}
 
 	private static String Repeat(String Unit, int Times) {
 		/*local*/StringBuilder Builder = new StringBuilder();
 		for(int i = 0; i < Times; ++i) {
 			Builder.append(Unit);
 		}
 		return Builder.toString();
 	}
 
 	public void SetIndent(int Level) {
 		if(Level < 0)
 			Level = 0;
 		if(this.IndentLevel != Level) {
 			this.IndentLevel = Level;
 			this.CurrentLevelIndentString = IndentGenerator.Repeat(this.IndentString, Level);
 		}
 	}
 
 	public void AddIndent(int LevelDelta) {
 		this.SetIndent(this.IndentLevel + LevelDelta);
 	}
 
 	public String Get() {
 		return this.CurrentLevelIndentString;
 	}
 
 	public String GetAndAddIndent(int LevelDelta) {
 		/*local*/String IndentString = this.CurrentLevelIndentString;
 		this.AddIndent(LevelDelta);
 		return IndentString;
 	}
 
 	public String AddIndentAndGet(int LevelDelta) {
 		this.AddIndent(LevelDelta);
 		return this.CurrentLevelIndentString;
 	}
 }
 
 public class GreenTeaGenerator extends GtStatic {
 	/*field*/public String LangName;
 	/*field*/ArrayList<String> GeneratedCodeStack;
 	/*field*/GtContext Context;
 
 	GreenTeaGenerator/*constructor*/(String LangName) {
 		this.LangName = LangName;
 		this.GeneratedCodeStack = new ArrayList<String>();
 	}
 
 	public final TypedNode UnsupportedNode(GtType Type, SyntaxTree ParsedTree) {
 		GtToken Token = ParsedTree.KeyToken;
 		ParsedTree.NameSpace.ReportError(ErrorLevel, Token, this.LangName + " has no language support for " + Token.ParsedText);
 		return new ErrorNode(ParsedTree.NameSpace.Context.VoidType, ParsedTree.KeyToken);
 	}
 
 	public TypedNode CreateConstNode(GtType Type, SyntaxTree ParsedTree, Object Value) {
 		return new ConstNode(Type, ParsedTree.KeyToken, Value);
 	}
 
 	public TypedNode CreateNullNode(GtType Type, SyntaxTree ParsedTree) {
 		return new NullNode(Type, ParsedTree.KeyToken);
 	}
 
 	public TypedNode CreateLocalNode(GtType Type, SyntaxTree ParsedTree, String LocalName) {
 		return new LocalNode(Type, ParsedTree.KeyToken, LocalName);
 	}
 
 	public TypedNode CreateGetterNode(GtType Type, SyntaxTree ParsedTree, GtMethod Method, TypedNode Expr) {
 		return new GetterNode(Type, ParsedTree.KeyToken, Method, Expr);
 	}
 
 	public TypedNode CreateIndexerNode(GtType Type, SyntaxTree ParsedTree, GtMethod Method, TypedNode Expr, TypedNode Index) {
 		return new IndexerNode(Type, ParsedTree.KeyToken, Method, Expr, Index);
 	}
 
 	public TypedNode CreateApplyNode(GtType Type, SyntaxTree ParsedTree, GtMethod Method) {
 		return new ApplyNode(Type, ParsedTree.KeyToken, Method);
 	}
 
 	public TypedNode CreateMessageNode(GtType Type, SyntaxTree ParsedTree, GtMethod Method) {
 		return new MessageNode(Type, ParsedTree.KeyToken, Method);
 	}
 
 	public TypedNode CreateNewNode(GtType Type, SyntaxTree ParsedTree) {
 		return new NewNode(Type, ParsedTree.KeyToken);
 	}
 
 	public TypedNode CreateUnaryNode(GtType Type, SyntaxTree ParsedTree, GtMethod Method, TypedNode Expr) {
 		return new UnaryNode(Type, ParsedTree.KeyToken, Method, Expr);
 	}
 
 	public TypedNode CreateSuffixNode(GtType Type, SyntaxTree ParsedTree, GtMethod Method, TypedNode Expr) {
 		return new SuffixNode(Type, ParsedTree.KeyToken, Method, Expr);
 	}
 
 	public TypedNode CreateBinaryNode(GtType Type, SyntaxTree ParsedTree, GtMethod Method, TypedNode Left, TypedNode Right) {
 		return new BinaryNode(Type, ParsedTree.KeyToken, Method, Left, Right);
 	}
 
 	public TypedNode CreateAndNode(GtType Type, SyntaxTree ParsedTree, TypedNode Left, TypedNode Right) {
 		return new AndNode(Type, ParsedTree.KeyToken, Left, Right);
 	}
 
 	public TypedNode CreateOrNode(GtType Type, SyntaxTree ParsedTree, TypedNode Left, TypedNode Right) {
 		return new OrNode(Type, ParsedTree.KeyToken, Left, Right);
 	}
 
 	public TypedNode CreateAssignNode(GtType Type, SyntaxTree ParsedTree, TypedNode Left, TypedNode Right) {
 		return new AssignNode(Type, ParsedTree.KeyToken, Left, Right);
 	}
 
 	public TypedNode CreateLetNode(GtType Type, SyntaxTree ParsedTree, GtType DeclType, TypedNode VarNode, TypedNode Block) {
 		return new LetNode(Type, ParsedTree.KeyToken, DeclType, VarNode, Block);
 	}
 
 	public TypedNode CreateIfNode(GtType Type, SyntaxTree ParsedTree, TypedNode Cond, TypedNode Then, TypedNode Else) {
 		return new IfNode(Type, ParsedTree.KeyToken, Cond, Then, Else);
 	}
 
 	public TypedNode CreateSwitchNode(GtType Type, SyntaxTree ParsedTree, TypedNode Match) {
 		return null;
 	}
 
 	public TypedNode CreateWhileNode(GtType Type, SyntaxTree ParsedTree, TypedNode Cond, TypedNode Block) {
 		return new WhileNode(Type, ParsedTree.KeyToken, Cond, Block);
 	}
 
 	public TypedNode CreateDoWhileNode(GtType Type, SyntaxTree ParsedTree, TypedNode Cond, TypedNode Block) {
 		return new DoWhileNode(Type, ParsedTree.KeyToken, Cond, Block);
 	}
 
 	public TypedNode CreateForNode(GtType Type, SyntaxTree ParsedTree, TypedNode Cond, TypedNode IterNode, TypedNode Block) {
 		return new ForNode(Type, ParsedTree.KeyToken, Cond, Block, IterNode);
 	}
 
 	public TypedNode CreateForEachNode(GtType Type, SyntaxTree ParsedTree, TypedNode VarNode, TypedNode IterNode, TypedNode Block) {
 		return new ForEachNode(Type, ParsedTree.KeyToken, VarNode, IterNode, Block);
 	}
 
 	public TypedNode CreateLoopNode(GtType Type, SyntaxTree ParsedTree, TypedNode Cond, TypedNode Block, TypedNode IterNode) {
 		return new LoopNode(Type, ParsedTree.KeyToken, Cond, Block, IterNode);
 	}
 
 	public TypedNode CreateReturnNode(GtType Type, SyntaxTree ParsedTree, TypedNode Node) {
 		return new ReturnNode(Type, ParsedTree.KeyToken, Node);
 	}
 
 	public TypedNode CreateLabelNode(GtType Type, SyntaxTree ParsedTree, TypedNode Node) {
 		return null;
 	}
 
 	public TypedNode CreateJumpNode(GtType Type, SyntaxTree ParsedTree, TypedNode Node, String Label) {
 		return new JumpNode(Type, ParsedTree.KeyToken, Label);
 	}
 
 	public TypedNode CreateBreakNode(GtType Type, SyntaxTree ParsedTree, TypedNode Node, String Label) {
 		return new BreakNode(Type, ParsedTree.KeyToken, Label);
 	}
 
 	public TypedNode CreateContinueNode(GtType Type, SyntaxTree ParsedTree, TypedNode Node, String Label) {
 		return new ContinueNode(Type, ParsedTree.KeyToken, Label);
 	}
 
 	public TypedNode CreateTryNode(GtType Type, SyntaxTree ParsedTree, TypedNode TryNode, TypedNode FinallyNode) {
 		return new TryNode(Type, ParsedTree.KeyToken, TryNode, FinallyNode);
 	}
 
 	public TypedNode CreateThrowNode(GtType Type, SyntaxTree ParsedTree, TypedNode Node) {
 		return new ThrowNode(Type, ParsedTree.KeyToken, Node);
 	}
 
 	public TypedNode CreateFunctionNode(GtType Type, SyntaxTree ParsedTree, TypedNode Block) {
 		return null;
 	}
 
 	public TypedNode CreateDefineNode(GtType Type, SyntaxTree ParsedTree, Object Module) {
 		return null;
 	}
 
 	public TypedNode CreateEmptyNode(GtType Type, SyntaxTree ParsedTree) {
 		return new TypedNode(ParsedTree.NameSpace.Context.VoidType, ParsedTree.KeyToken);
 	}
 
 	public TypedNode CreateErrorNode(GtType Type, SyntaxTree ParsedTree) {
 		return new ErrorNode(ParsedTree.NameSpace.Context.VoidType, ParsedTree.KeyToken);
 	}
 
 
 	public void VisitEmptyNode(TypedNode EmptyNode) {
 		GtStatic.DebugP("empty node: " + EmptyNode.Token.ParsedText);
 	}
 
 	public void VisitSuffixNode(SuffixNode SuffixNode) {
 		/*extension*/
 	}
 
 	public void VisitUnaryNode(UnaryNode UnaryNode) {
 		/*extension*/
 	}
 
 	public void VisitIndexerNode(IndexerNode IndexerNode) {
 		/*extension*/
 	}
 
 	public void VisitMessageNode(MessageNode MessageNode) {
 		/*extension*/
 	}
 
 	public void VisitWhileNode(WhileNode WhileNode) {
 		/*extension*/
 	}
 
 	public void VisitDoWhileNode(DoWhileNode DoWhileNode) {
 		/*extension*/
 	}
 
 	public void VisitForNode(ForNode ForNode) {
 		/*extension*/
 	}
 
 	public void VisitForEachNode(ForEachNode ForEachNode) {
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
 
 	public void VisitLetNode(LetNode Node) {
 		/*extension*/
 	}
 
 	public void VisitIfNode(IfNode Node) {
 		/*extension*/
 	}
 
 	public void VisitSwitchNode(SwitchNode Node) {
 		/*extension*/
 	}
 
 	public void VisitLoopNode(LoopNode Node) {
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
 
 	public final void VisitBlock(TypedNode Node) {
 		/*local*/TypedNode CurrentNode = Node;
 		while(CurrentNode != null) {
 			CurrentNode.Evaluate(this);
 			CurrentNode = CurrentNode.NextNode;
 		}
 	}
 
 	// This must be extended in each language
 	public void DefineFunction(GtMethod Method, ArrayList<String> ParamNameList, TypedNode Body) {
 		
 		/*extenstion*/
 	}
 
 	public Object Eval(TypedNode Node) {
 		this.VisitBlock(Node);
 		return null;
 	}
 
 	public void AddClass(GtType Type) {
 		/*extension*/
 	}
 
 	public void LoadContext(GtContext Context) {
 		/*extension*/
 	}
 
 	protected void PushCode(String Code){
 		this.GeneratedCodeStack.add(Code);
 	}
 
 	protected String PopCode(){
 		/*local*/int Size = this.GeneratedCodeStack.size();
 		if(Size > 0){
 			return this.GeneratedCodeStack.remove(Size - 1);
 		}
 		return "";
 	}
 
 	protected String[] PopManyCode(int n) {
 		/*local*/String[] array = new String[n];
 		for(/*local*/int i = 0; i < n; ++i) {
 			array[i] = this.PopCode();
 		}
 		return array;
 	}
 
 	protected String[] PopManyCodeReverse(int n) {
 		/*local*/String[] array = new String[n];
 		for(/*local*/int i = 0; i < n; ++i) {
 			array[n - i - 1] = this.PopCode();
 		}
 		return array;
 	}
 
 	protected String PopManyCodeWithModifier(int n, boolean reverse, String prefix, String suffix, String delim) {
 		if(prefix == null) {
 			prefix = "";
 		}
 		if(suffix == null) {
 			suffix = "";
 		}
 		if(delim == null) {
 			delim = "";
 		}
 		/*local*/String[] array = reverse ? this.PopManyCodeReverse(n) : this.PopManyCode(n);
 		/*local*/String Code = "";
 		for(/*local*/int i = 0; i < n; ++i) {
 			if(i > 0) {
 				Code += delim;
 			}
 			Code = Code + prefix + array[i] + suffix;
 		}
 		return Code;
 	}
 }
 
