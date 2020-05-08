 /*
  * Thibaut Colar Mar 24, 2010
  */
 package net.colar.netbeans.fan.test;
 
 import java.util.List;
 import net.colar.netbeans.fan.FanParserTask;
 import net.colar.netbeans.fan.indexer.model.FanSlot;
 import net.colar.netbeans.fan.parboiled.AstKind;
 import net.colar.netbeans.fan.parboiled.AstNode;
 import net.colar.netbeans.fan.parboiled.FanLexAstUtils;
 import net.colar.netbeans.fan.parboiled.FantomParser;
 import net.colar.netbeans.fan.parboiled.pred.NodeKindPredicate;
 import net.colar.netbeans.fan.scope.FanAstScopeVarBase;
 import net.colar.netbeans.fan.scope.FanAstScopeVarBase.VarKind;
 import net.colar.netbeans.fan.scope.FanLocalScopeVar;
 import net.colar.netbeans.fan.types.FanResolvedListType;
 import net.colar.netbeans.fan.types.FanResolvedNullType;
 import net.colar.netbeans.fan.types.FanResolvedType;
 import net.jot.testing.JOTTester;
 import org.parboiled.Parboiled;
 import org.parboiled.RecoveringParseRunner;
 import org.parboiled.common.StringUtils;
 import org.parboiled.support.ParsingResult;
 
 /**
  * Test type resolution
  * @author thibautc
  */
 public class FantomTypesTest extends FantomCSLTest
 {
 	// List some fq imports we will use in the tests, so we can add them to the node's scope
 	// Note: no need to add the 'sys' types (implied)
 
 	static String[] testUsings =
 	{
 		"web::Weblet", "fwt::Button", "fwt::Widget"
 	};
 
 	/*static FanAstScopeVarBase[] scopeVars =
 	{
 		new FanAstScopeVar(null, VarKind.METHOD, FanResolvedType.mak) {} {}
 	};*/
 
 	@Override
 	public void cslTest() throws Throwable
 	{
 		// Dummy task & rootNode to pass along
 		FanParserTask task = new FanParserTask(null);
 		DummyAstRootNode node = new DummyAstRootNode(task);
 		addUsingsToNode(node, testUsings);
 		addObjectSlotsToNode(node, task);
 
 		// Testing type signature stuff
 		FanResolvedType t = FanResolvedType.makeFromTypeSig(node, "sys::Str");
 		JOTTester.checkIf("Type sig 1", t.getQualifiedType().equals("sys::Str") && !t.isNullable(), t.toString());
 		JOTTester.checkIf("Type sig 1a", t.asNullableContext(true).isNullable() && t.asStaticContext(true).isStaticContext(), t.toString());
 		t = FanResolvedType.makeFromTypeSig(node, "sys::Str?");
 		JOTTester.checkIf("Type sig 2", t.getQualifiedType().equals("sys::Str") && t.isNullable(), t.toString());
 		t = FanResolvedType.makeFromTypeSig(node, "sys::Str[]");
 		JOTTester.checkIf("Type sig 3", (t instanceof FanResolvedListType) && t.toTypeSig(true).equals("sys::Str[]"), t.toString());
 		t = FanResolvedType.makeFromTypeSig(node, "Int");
 		check(t, "sys::Int", false, true);
 		t = FanResolvedType.makeFromTypeSig(node, "Int?");
 		check(t, "sys::Int?", true, true);
 
 		// checking expressions
 		checkExpr("5", "sys::Int", false, false);
 		AstNode resultNode = checkExpr("null", "null::null?", true, false);
 		JOTTester.checkIf("checking null type", resultNode.getType() instanceof FanResolvedNullType);
 
 		checkExpr("0..<20", "sys::Range", false, false);
 		checkExpr("(0..<20).toList", "sys::Int[]", false, false);
 		checkExpr("\"abc\".split[0]", "sys::Str", false, false);
 		checkExpr("\"abc\".split[0..2]", "sys::Str[]", false, false);
 		checkExpr("Obj", "sys::Obj", false, true);
 		checkExpr("Obj()", "sys::Obj", false, false);
 		checkExpr("Str.defVal", "sys::Str", false, false);
 		checkExpr("\"abc\".isEmpty", "sys::Bool", false, false);
 		checkExpr("[3,4]", "sys::Int[]", false, false);
 		checkExpr("[3, 2f ,4]", "sys::Num[]", false, false);
 
		checkExpr("[3,null,4]", "sys::Int?[]", false, false);
 		t = FanResolvedType.makeFromTypeSig(node, "Obj?[]");
 		check(t, "sys::Obj?[]", false, false);
 		t = FanResolvedType.makeFromTypeSig(node, "Obj[]?");
 		check(t, "sys::Obj[]?", true, false);
 		t = FanResolvedType.makeFromTypeSig(node, "Str[][]?");
 		check(t, "sys::Str[][]?", true, false);
 		JOTTester.checkIf("List of List type",
 				(t instanceof FanResolvedListType)
 				&& ((FanResolvedListType) t).getItemType().toTypeSig(true).equals("sys::Str[]"), t.toString());
 
 		resultNode = checkExpr("|str| { echo(str) }", "|sys::Obj?->sys::Void|", false, false);
 		AstNode block = FanLexAstUtils.getFirstChildRecursive(resultNode, new NodeKindPredicate(AstKind.AST_BLOCK));
 		resultNode = checkExpr("|Int i| { echo(i) }", "|sys::Int->sys::Void|", false, false);
 		block = FanLexAstUtils.getFirstChildRecursive(resultNode, new NodeKindPredicate(AstKind.AST_BLOCK));
 		checkExpr("Button {text=\"aa\"; it.text=\"aa\"}", null, false, false);
 		checkExpr("Button.make {text=\"aa\"; it.text=\"aa\"}", null, false, false);
 		checkExpr("Button.make() {text=\"aa\"; it.text=\"aa\"}", null, false, false);
 		checkExpr("Str[,].with { fill(\"x\", 3) }", null, false, false);
 		checkExpr("Str[,] { fill(\"x\", 3) }", null, false, false);
 		checkExpr("[\"a\", \"b\"].each { echo(toStr); echo(it.toStr) }", null, false, false);
 		checkExpr("[5:3].each |Str val| { echo(val)}", null, false, false);
 		checkExpr("Actor(ActorPool()) |msg| {echo(msg)}", null, false, false);
 		checkExpr("Actor.make(ActorPool()) |msg| {echo(msg)}", null, false, false);
 
 		// Testing isCompatible()
 		JOTTester.checkIf("Compatibility of Enum vs Obj", mkt("sys::Enum", node).isTypeCompatible(mkt("sys::Obj", node)));
 		JOTTester.checkIf("Compatibility of Obj vs Obj", mkt("sys::Obj", node).isTypeCompatible(mkt("sys::Obj", node)));
 		JOTTester.checkIf("Un-Compatibility of mixin vs Obj", !mkt("web::Weblet", node).isTypeCompatible(mkt("sys::Obj", node)));
 		JOTTester.checkIf("Compatibility of Button vs Obj", mkt("fwt::Button", node).isTypeCompatible(mkt("sys::Obj", node)));
 		JOTTester.checkIf("Compatibility of Button vs widget", mkt("fwt::Button", node).isTypeCompatible(mkt("fwt::Widget", node)));
 
 		// Testing getParent()
 		t = mkt("sys::Actor", node);
 		FanResolvedType p = t.getParentType();
 		JOTTester.checkIf("Actor parent is Obj", p.getDbType().getQualifiedName().equals("sys::Obj"), p.toString());
 		t = mkt("sys::Weekday", node);
 		p = t.getParentType();
 		JOTTester.checkIf("Weekday parent is Enum", p.getDbType().getQualifiedName().equals("sys::Enum"), p.toString());
 		t = mkt("sys::Enum", node);
 		p = t.getParentType();
 		JOTTester.checkIf("Enum parent is Obj", p.getDbType().getQualifiedName().equals("sys::Obj"), p.toString());
 		t = mkt("web::Weblet", node);
 		p = t.getParentType();
 		JOTTester.checkIf("Mixin parent is null", p == null, p == null ? "null" : p.toString());
 	}
 
 	private FanResolvedType mkt(String qType, AstNode node)
 	{
 		return FanResolvedType.makeFromDbType(node, qType);
 	}
 
 	/**
 	 * Chceck a type against expected values
 	 * @param t
 	 * @param typeSig
 	 * @param isNullable
 	 * @param isStatic
 	 * @throws Exception
 	 */
 	private void check(FanResolvedType t, String typeSig, boolean isNullable, boolean isStatic) throws Exception
 	{
 		check(t.getAsTypedType(), t, typeSig, isNullable, isStatic);
 	}
 
 	/**
 	 * if typeSig is null, not checked
 	 * @param infoStr
 	 * @param t
 	 * @param typeSig
 	 * @param isNullable
 	 * @param isStatic
 	 * @throws Exception
 	 */
 	private void check(String infoStr, FanResolvedType t, String typeSig, boolean isNullable, boolean isStatic) throws Exception
 	{
 		boolean good = t != null
 				&& t.isResolved()
 				&& (typeSig==null || t.toTypeSig(true).equals(typeSig))
 				&& t.isNullable() == isNullable
 				&& t.isStaticContext() == isStatic;
 		String expected = "got: " + t + ", expected: " + typeSig + ", ST:" + isStatic + ", NL:" + isNullable;
 		JOTTester.checkIf("Checking " + infoStr, good, expected);
 	}
 
 	/**
 	 * Parse the expression into a type and check it against expected values
 	 * @param expr
 	 * @param typeSig
 	 * @param isNullable
 	 * @param isStatic
 	 * @throws Exception
 	 */
 	private AstNode checkExpr(String expr, String typeSig, boolean isNullable, boolean isStatic) throws Exception
 	{
 		FanParserTask task = new FanParserTask(null);
 		FantomParser parser = Parboiled.createParser(FantomParser.class, task);
 		ParsingResult<AstNode> result = RecoveringParseRunner.run(parser.testExpr(), expr);
 		JOTTester.checkIf("Checking for parsing errors: "+expr, ! result.hasErrors(), StringUtils.join(result.parseErrors,"\n"));
 		AstNode node = result.parseTreeRoot.getValue();
 		task.prune(node, task.getRootLabel(node));
 		addUsingsToNode(node, testUsings);
 		addObjectSlotsToNode(node, task);
 		task.parseVars(node, null);
 		//System.out.println("Node for "+expr+" : ");
 		//FanLexAstUtils.dumpTree(node, 0);
 		JOTTester.checkIf("Checking for Semantic errors: "+expr, task.getDiagnostics().isEmpty(), StringUtils.join(task.getDiagnostics(),"\n"));
 		FanResolvedType t = node.getType();
 		check(expr, t, typeSig, isNullable, isStatic);
 		return node;
 	}
 
 	public static void main(String[] args)
 	{
 		try
 		{
 			JOTTester.singleTest(new FantomTypesTest(), false);
 		} catch (Throwable t)
 		{
 			t.printStackTrace();
 		}
 	}
 
 	public static void addUsingsToNode(AstNode node, String[] usings)
 	{
 		for (String using : usings)
 		{
 			String shortName = using.substring(using.indexOf("::") + 2);
 			node.addScopeVar(shortName, VarKind.IMPORT, FanResolvedType.makeFromDbType(node, using), false);
 		}
 	}
 
 	public static void addObjectSlotsToNode(AstNode node, FanParserTask task)
 	{
 		FanResolvedType baseType=FanResolvedType.makeFromDbType(node, "sys::Obj");
 		List<FanSlot> slots = FanSlot.getAllSlotsForType("sys::Obj", false, task);
 		for (FanSlot slot : slots)
 		{
 			FanAstScopeVarBase newVar = new FanLocalScopeVar(node, baseType, slot, slot.getName());
 			node.addScopeVar(newVar, true);
 		}
 	}
 }
