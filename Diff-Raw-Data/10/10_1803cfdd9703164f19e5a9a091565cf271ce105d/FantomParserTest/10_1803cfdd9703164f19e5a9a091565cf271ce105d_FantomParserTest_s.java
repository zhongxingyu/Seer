 /*
  * Thibaut Colar Feb 5, 2010
  */
 package net.colar.netbeans.fan.test;
 
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.Date;
 import net.colar.netbeans.fan.FanParserTask;
 import net.colar.netbeans.fan.parboiled.AstNode;
 import net.colar.netbeans.fan.parboiled.FantomParser;
 import net.jot.testing.JOTTestable;
 import net.jot.testing.JOTTester;
 import org.parboiled.Parboiled;
 import org.parboiled.RecoveringParseRunner;
 import org.parboiled.Rule;
 import org.parboiled.common.StringUtils;
 import org.parboiled.support.ParseTreeUtils;
 import org.parboiled.support.ParsingResult;
 
 /**
  * Test new parboiled based parser
  * @author thibautc
  */
 public class FantomParserTest implements JOTTestable
 {
 
 	public static final String FAN_HOME = "/home/thibautc/fantom-1.0.51";
 
 	public void jotTest() throws Throwable
 	{
 		FantomParser parser = Parboiled.createParser(FantomParser.class, (FanParserTask)null);
 		ParsingResult<AstNode> result = null;
 
 		boolean singleTest = false;// Do just the 1 first test
 		boolean grammarTest = true; // Do all the grammar tests
 		boolean refFilesTest = false; // parse the reference test files
 		boolean fantomFilesTest = true; // parse fantom distro files
 		boolean fantomFilesLexerTest = true; // parse fantom distro files (lexer rules)
 
 		if (singleTest)
 		{
 			try
 			{
 				//result = parse(parser, parser.slotDef(), "override CType? base { get { load; return *base } internal set}");
 				//testNodeName("singleTest1", result, "slotDef", "override CType? base { get { load; return *base } internal set}");
 				//result = parse(parser, parser.ternaryExpr(), "col==0 ? key: Buf.fromBase64(map[key]).readAllStr");
 				//testNodeName("ternaryExpr2", result, "ternaryExpr", "col==0 ? key: Buf.fromBase64(map[key]).readAllStr");
 				testAst("/home/thibautc/dummy/fan/Main.fan");
 			} catch (Exception e)
 			{
 				e.printStackTrace();
 			}
 			//System.out.println(ParseTreeUtils.printNodeTree(result));
 			return;
 		}
 		// --- Full test Suite -------------
 		if (grammarTest)
 		{
 			//spacing
 			result = parse(parser, parser.spacing(), "  \r \n\r\t	\n \u000c ");
 			testNodeName("Spacing", result, "spacing");
 			result = parse(parser, parser.spacing(), "   \n//fdsfdsfs\n	\t");
 			testNodeName("Spacing2", result, "spacing");
 			result = parse(parser, parser.spacing(), "   \n/*fdsfdsfs*/	\t");
 			testNodeName("Spacing3", result, "spacing");
 			result = parse(parser, parser.spacing(), "/* blah\nblu"); // unterminated ml comment
 			testNodeName("Spacing4", result, "spacing", "/* blah"); // should match until \n
 			// litterals
 			result = parse(parser, parser.char_(), "'a'");
 			testNodeName("Char", result, "char_", "'a'");
 			result = parse(parser, parser.char_(), "'\\n'");
 			testNodeName("Char2", result, "char_", "'\\n'");
 			result = parse(parser, parser.char_(), "'\\u5F9a'");
 			testNodeName("Char3", result, "char_", "'\\u5F9a'");
 			result = parse(parser, parser.uri(), "`http://www.google.com/`");
 			testNodeName("Uri", result, "uri", "`http://www.google.com/`");
 			result = parse(parser, parser.strs(), "\"aa\"");
 			testNodeName("String", result, "strs", "\"aa\"");
 			result = parse(parser, parser.strs(), "\"aa\\r\\nbb\"");
 			testNodeName("String 2", result, "strs", "\"aa\\r\\nbb\"");
 			result = parse(parser, parser.strs(), "\"a \\n $toto \\$thingy \\t \\u5F39\"");
 			testNodeName("String 3", result, "strs", "\"a \\n $toto \\$thingy \\t \\u5F39\"");
 			result = parse(parser, parser.strs(), "\"gggfdgdfgfdgdfgkkjdlkewd erfreggreg gtrgtrtrgtrg trgtrgtrrtg rgrgtrgrtgrtg\"");
 			testNodeName("String 4", result, "strs", "\"gggfdgdfgfdgdfgkkjdlkewd erfreggreg gtrgtrtrgtrg trgtrgtrrtg rgrgtrgrtgrtg\"");
 			result = parse(parser, parser.expr(), "\"/*toto*/ text\"");
 			testNodeName("String 5", result, "expr", "\"/*toto*/ text\"");
 			result = parse(parser, parser.strs(), "\"aa\\r\\nbb\"");
 			testNodeName("3Quotes Str 1", result, "strs", "\"aa\\r\\nbb\"");
 			result = parse(parser, parser.strs(), "\"\"\"\"hello\" \"\"\"");
 			testNodeName("3Quotes Str 2", result, "strs", "\"\"\"\"hello\" \"\"\"");
 			result = parse(parser, parser.strs(), "\"\"\"\"hello\"\\n\"\" \"\"\"");
 			testNodeName("3Quotes Str 3", result, "strs", "\"\"\"\"hello\"\\n\"\" \"\"\"");
 			result = parse(parser, parser.dsl(), "Str<| dfdsfsd \\ \\n |  > dsfsd |>");
 			testNodeName("DSL", result, "dsl", "Str<| dfdsfsd \\ \\n |  > dsfsd |>");
 			// numbers
 			result = parse(parser, parser.digit(), "7");
 			testNodeName("Digit", result, "digit");
 			result = parse(parser, parser.hexDigit(), "7");
 			testNodeName("HexDigit", result, "hexDigit");
 			result = parse(parser, parser.hexDigit(), "D");
 			testNodeName("HexDigit2", result, "hexDigit");
 			result = parse(parser, parser.hexDigit(), "d");
 			testNodeName("HexDigit3", result, "hexDigit");
 			result = parse(parser, parser.number(), "0X_5__A6_F");
 			testNodeName("Number 1", result, "number");
 			result = parse(parser, parser.number(), "0xCAFE_BABE");
 			testNodeName("Number 2", result, "number");
 			result = parse(parser, parser.number(), "1_000_000");
 			testNodeName("Number 3", result, "number");
 			result = parse(parser, parser.number(), "-89_039");
 			testNodeName("Number 4", result, "number");
 			result = parse(parser, parser.number(), "-.25");
 			testNodeName("Number f 1", result, "number");
 			result = parse(parser, parser.number(), "3.0f");
 			testNodeName("Number f 2", result, "number");
 			result = parse(parser, parser.number(), "3f");
 			testNodeName("Number f 3", result, "number");
 			result = parse(parser, parser.number(), "123_456.0f");
 			testNodeName("Number f 4", result, "number");
 			result = parse(parser, parser.number(), "3e6f");
 			testNodeName("Number f 5", result, "number");
 			result = parse(parser, parser.number(), "0.2e+6f");
 			testNodeName("Number f 6", result, "number");
 			result = parse(parser, parser.number(), "1_2.3_7e-5_6f");
 			testNodeName("Number f 7", result, "number");
 			result = parse(parser, parser.number(), "5f");
 			testNodeName("Number f 8", result, "number");
 			result = parse(parser, parser.number(), "4d");
 			testNodeName("Number d 1", result, "number");
 			result = parse(parser, parser.number(), "4.00");
 			testNodeName("Number d 2", result, "number");
 			result = parse(parser, parser.number(), "123_456d");
 			testNodeName("Number d 3", result, "number");
 			result = parse(parser, parser.number(), "0.2e+6D");
 			testNodeName("Number d 4", result, "number");
 			result = parse(parser, parser.number(), "1_2.3_7e-5_6");
 			testNodeName("Number d 5", result, "number");
 			// base items
 			result = parse(parser, parser.doc(), "**\n");
 			testNodeName("Doc", result, "doc");
 			result = parse(parser, parser.doc(), "**\n** some doc\n");
 			testNodeName("Multi Doc", result, "doc");
 			result = parse(parser, parser.id(), "ab_FG09__");
 			testNodeName("ID", result, "id");
 			// types
 			result = parse(parser, parser.typeAndOrId(), "ab_FG09__");
 			testNodeName("TypeAndOrId", result, "typeAndOrId");
 			result = parse(parser, parser.typeAndOrId(), "ab_FG09__ ab_FG09__");
 			testNodeName("TypeAndOrId 2", result, "typeAndOrId", "ab_FG09__ ab_FG09__");
 			result = parse(parser, parser.simpleType(), "myThingy");
 			testNodeName("SimpleType", result, "simpleType");
 			result = parse(parser, parser.simpleType(), "Email::email");
 			testNodeName("SimpleType2", result, "simpleType");
 			result = parse(parser, parser.type(), "[Sys::Str:Int]");
 			testNodeName("MapType2", result, "type");
 			result = parse(parser, parser.type(), "Str:Int");
 			testNodeName("MapType3", result, "type");
 			result = parse(parser, parser.type(), "[Str:Int]:Str");
 			testNodeName("MapType4", result, "type");
 			result = parse(parser, parser.type(), "[Str?[]?:Int?]");
 			testNodeName("MapType5", result, "type");
 			result = parse(parser, parser.type(), "[|Str->Int|:Int?]");
 			testNodeName("MapType6", result, "type");
 			result = parse(parser, parser.type(), "[Sys::Str:Sys::Int]");
 			testNodeName("MapType7", result, "type");
 			result = parse(parser, parser.formals(), "myThingy, Str myOtherThingy, [Str:Int] one5More");
 			testNodeName("Formals", result, "formals");
 			result = parse(parser, parser.funcType(), "|->|");
 			testNodeName("FuncType1", result, "funcType");
 			result = parse(parser, parser.funcType(), "|Sys::Str->|");
 			testNodeName("FuncType2", result, "funcType");
 			result = parse(parser, parser.funcType(), "|->Int|");
 			testNodeName("FuncType3", result, "funcType");
 			result = parse(parser, parser.funcType(), "|Str?[]? s, Int[]? i->[Str:Int?]?|");
 			testNodeName("FuncType4", result, "funcType");
 			result = parse(parser, parser.type(), "toto");
 			testNodeName("Type", result, "type");
 			result = parse(parser, parser.type(), "toto?");
 			testNodeName("Type2", result, "type");
 			result = parse(parser, parser.type(), "toto[]?");
 			testNodeName("Type3", result, "type");
 			result = parse(parser, parser.type(), "Str:email");
 			testNodeName("Type4", result, "type");
 			result = parse(parser, parser.type(), "[Str?[]?:Int]");
 			testNodeName("Type5", result, "type");
 			result = parse(parser, parser.type(), "|Str s, Int->Int|");
 			testNodeName("Type6", result, "type");
 			result = parse(parser, parser.type(), "Sys::Str?[]?");
 			testNodeName("Type7", result, "type");
 
 			// expressions
 			result = parse(parser, parser.mapPair(), "doThis:doThat");
 			testNodeName("Map pair", result, "mapPair", "doThis:doThat");
 			result = parse(parser, parser.mapItems(), "doThis:doThat, a:b, c:d");
 			testNodeName("Map items", result, "mapItems", "doThis:doThat, a:b, c:d");
 			result = parse(parser, parser.map(), "[:]");
 			testNodeName("Map 1", result, "map", "[:]");
 			result = parse(parser, parser.map(), "[5:9]");
 			testNodeName("Map 2", result, "map", "[5:9]");
 			result = parse(parser, parser.map(), "[5:9,7:12]");
 			testNodeName("Map 3", result, "map", "[5:9,7:12]");
 			//result = parse(parser, parser.map(), "Int:Str[5:9,7:12]");
 			//testNodeName("Map 4", result, "map", "Int:Str[5:9,7:12]");
 			result = parse(parser, parser.map(), "[Int:Str][5:9,7:12]");
 			testNodeName("Map 5", result, "map", "[Int:Str][5:9,7:12]");
 			result = parse(parser, parser.map(), "[4:\"four\", 5f:\"five\"]");
 			testNodeName("Map 6", result, "map", "[4:\"four\", 5f:\"five\"]");
 			result = parse(parser, parser.closure(), "|->|{}");
 			testNodeName("Closure 1", result, "closure", "|->|{}");
 			result = parse(parser, parser.closure(), "|->|{i=5}");
 			testNodeName("Closure 2", result, "closure", "|->|{i=5}");
 			result = parse(parser, parser.closure(), "|val| {r += \"$val \"}");
 			testNodeName("Closure 3", result, "closure", "|val| {r += \"$val \"}");
 			result = parse(parser, parser.call(), "toto()");
 			testNodeName("Call1", result, "call", "toto()");
 			result = parse(parser, parser.call(), "toto(a, b)");
 			testNodeName("Call2", result, "call", "toto(a, b)");
 			result = parse(parser, parser.call(), "toto|->|{i=5}");
 			testNodeName("Call3", result, "call", "toto|->|{i=5}");
 			result = parse(parser, parser.call(), "toto(a, b)|->|{i=5}");
 			testNodeName("Call4", result, "call", "toto(a, b)|->|{i=5}");
 			result = parse(parser, parser.idExpr(), "toto");
 			testNodeName("IdExpr1", result, "idExpr", "toto");//id
 			result = parse(parser, parser.idExpr(), "*toto");
 			testNodeName("IdExpr2", result, "idExpr", "*toto");//field
 			result = parse(parser, parser.idExpr(), "blah()");
 			testNodeName("IdExpr3", result, "idExpr", "blah()");//call
 			result = parse(parser, parser.expr(), "toto.blah()");
 			testNodeName("StaticCall1", result, "expr", "toto.blah()");
 			result = parse(parser, parser.expr(), "toto.toto");
 			testNodeName("StaticCall2", result, "expr", "toto.toto");
 			result = parse(parser, parser.localDef(), "a:=23");
 			testNodeName("localDef1", result, "localDef", "a:=23");
 			result = parse(parser, parser.localDef(), "Int a:=23");
 			testNodeName("localDef2", result, "localDef", "Int a:=23");
 			result = parse(parser, parser.localDef(), "Int var");
 			testNodeName("localDef5", result, "localDef", "Int var");
 			result = parse(parser, parser.litteral(), "[4:\"four\", 5f:\"five\"]");
 			testNodeName("litteral1", result, "litteral", "[4:\"four\", 5f:\"five\"]");
 			result = parse(parser, parser.termExpr(), "[4:\"four\", 5f:\"five\"]");
 			testNodeName("termExpr1", result, "termExpr", "[4:\"four\", 5f:\"five\"]");
 			result = parse(parser, parser.parExpr(), "[4:\"four\", 5f:\"five\"]");
 			testNodeName("parExpr1", result, "parExpr", "[4:\"four\", 5f:\"five\"]");
 			result = parse(parser, parser.equalityExpr(), "[4:\"four\", 5f:\"five\"]");
 			testNodeName("equalityExpr1", result, "equalityExpr", "[4:\"four\", 5f:\"five\"]");
 			result = parse(parser, parser.condOrExpr(), "[4:\"four\", 5f:\"five\"]");
 			testNodeName("condOrExpr1", result, "condOrExpr", "[4:\"four\", 5f:\"five\"]");
 			result = parse(parser, parser.expr(), "toto.doit");
 			testNodeName("expr1", result, "expr", "toto.doit");
 			result = parse(parser, parser.expr(), "i=5");
 			testNodeName("expr2", result, "expr", "i=5");
 			result = parse(parser, parser.expr(), "[4:\"four\", 5f:\"five\"]");
 			testNodeName("expr3", result, "expr", "[4:\"four\", 5f:\"five\"]");
 			result = parse(parser, parser.expr(), "r += 4");
 			testNodeName("expr4", result, "expr", "r += 4");
 			result = parse(parser, parser.expr(), "5>3");
 			testNodeName("expr5", result, "expr", "5>3");
 			result = parse(parser, parser.expr(), "Buf.fromBase64(map[key]).readAllStr");
 			testNodeName("expr6", result, "expr", "Buf.fromBase64(map[key]).readAllStr");//
 			result = parse(parser, parser.expr(), "m = m is JavaMethod ? ((JavaMethod)m).next : null");
 			testNodeName("expr7", result, "expr", "m = m is JavaMethod ? ((JavaMethod)m).next : null");
 			result = parse(parser, parser.condOrExpr(), "str[n] == 55 || str[n] == ','");
 			testNodeName("condOr", result, "condOrExpr", "str[n] == 55 || str[n] == ','");
 			result = parse(parser, parser.if_(), "if(5>3)doit()");
 			testNodeName("if1", result, "if_", "if(5>3)doit()");
 			result = parse(parser, parser.if_(), "if(5>3)doit();else doThat();");
 			testNodeName("if2", result, "if_", "if(5>3)doit();else doThat();");
 			result = parse(parser, parser.if_(), "if(5>3+2){doThis();doThat()}");
 			testNodeName("if3", result, "if_", "if(5>3+2){doThis();doThat()}");
 			result = parse(parser, parser.if_(), "if(5>3+2){doThis();doThat()}else{doThat}");
 			testNodeName("if4", result, "if_", "if(5>3+2){doThis();doThat()}else{doThat}");//
 			result = parse(parser, parser.if_(), "if (str[n] == ' ' || str[n] == ',') break");
 			testNodeName("if5", result, "if_", "if (str[n] == ' ' || str[n] == ',') break");
 			result = parse(parser, parser.expr(), "2>3?a:b");
 			testNodeName("ternaryExpr1", result, "expr", "2>3?a:b");
 			result = parse(parser, parser.expr(), "col==0 ? key : Buf.fromBase64(map[key]).readAllStr");
 			testNodeName("ternaryExpr2", result, "expr", "col==0 ? key : Buf.fromBase64(map[key]).readAllStr");
 			// TODO: many more expr
 
 			// facets
 			result = parse(parser, parser.facet(), "@Str");
 			testNodeName("facet1", result, "facet", "@Str");
 			result = parse(parser, parser.facet(), "@Sys::Str");
 			testNodeName("facet2", result, "facet", "@Sys::Str");
 			result = parse(parser, parser.facet(), "@Str{id=toto.doit}");
 			testNodeName("facet3", result, "facet", "@Str{id=toto.doit}");
 			result = parse(parser, parser.facet(), "@Str{id=toto.doit;name='c'\nstatus=25}");
 			testNodeName("facet4", result, "facet", "@Str{id=toto.doit;name='c'\nstatus=25}");
 
 			// stmt
 			result = parse(parser, parser.stmt(), "toto.doit");
 			testNodeName("stmt1", result, "stmt", "toto.doit");
 			result = parse(parser, parser.stmt(), "i=5");
 			testNodeName("Stmt2", result, "stmt", "i=5");
 			result = parse(parser, parser.stmt(), "Int i:=5");
 			testNodeName("Stmt3", result, "stmt", "Int i:=5");
 			result = parse(parser, parser.stmt(), "Int i:=5+3");
 			testNodeName("Stmt4", result, "stmt", "Int i:=5+3");
 			result = parse(parser, parser.stmt(), "show([4:\"four\", 5f:\"five\"],\"same as above with type inference\")");
 			testNodeName("Stmt5", result, "stmt", "show([4:\"four\", 5f:\"five\"],\"same as above with type inference\")");
 			result = parse(parser, parser.stmt(), "Label { text=\"Name\"  },"); // addIt stmt
 			testNodeName("Stmt6", result, "stmt", "Label { text=\"Name\"  },");
 			result = parse(parser, parser.stmt(), "obj := | |->Int| a, |->Int| b->Int| { return (Int)a.call + (Int)b.call }");
 			testNodeName("Stmt7", result, "stmt", "obj := | |->Int| a, |->Int| b->Int| { return (Int)a.call + (Int)b.call }");
 			result = parse(parser, parser.stmt(), "MethodDef.makeInstanceInit(iInit.loc, t, iInit)");
 			testNodeName("Stmt8", result, "stmt", "MethodDef.makeInstanceInit(iInit.loc, t, iInit)");
 			result = parse(parser, parser.stmt(), "ii := MethodDef.makeInstanceInit(iInit.loc, t, iInit)");
 			testNodeName("Stmt9", result, "stmt", "ii := MethodDef.makeInstanceInit(iInit.loc, t, iInit)");
 			result = parse(parser, parser.block(), "i=5");//
 			testNodeName("Block", result, "block", "i=5");
 			result = parse(parser, parser.block(), "{i=5}");
 			testNodeName("Block2", result, "block", "{i=5}");
 			result = parse(parser, parser.block(), "{i=5;k=7\n\nj=6}");
 			testNodeName("Block3", result, "block", "{i=5;k=7\n\nj=6}");
 
 			// Slot Def
 			result = parse(parser, parser.fieldAccessor(), "{get{i=23}}");
 			testNodeName("FieldAccesor1", result, "fieldAccessor", "{get{i=23}}");
 			result = parse(parser, parser.fieldAccessor(), "{get{i=23}\nprivate set{}\n}");
 			testNodeName("FieldAccesor2", result, "fieldAccessor", "{get{i=23}\nprivate set{}\n}");
 			result = parse(parser, parser.slotDef(), "Int a");
 			testNodeName("FieldDef1", result, "slotDef", "Int a");
 			result = parse(parser, parser.slotDef(), "private const Int a");
 			testNodeName("FieldDef2", result, "slotDef", "private const Int a");
 			result = parse(parser, parser.slotDef(), "static override readonly Int a:=23");
 			testNodeName("FieldDef3", result, "slotDef", "static override readonly Int a:=23");
 			result = parse(parser, parser.slotDef(), "Int a{get{i=23}\nprivate set{}\n}");
 			testNodeName("FieldDef4", result, "slotDef", "Int a{get{i=23}\nprivate set{}\n}");
 			result = parse(parser, parser.slotDef(), "Int a:=23{get{i=23}\nprivate set{}\n}");
 			testNodeName("FieldDef5", result, "slotDef", "Int a:=23{get{i=23}\nprivate set{}\n}");
 			result = parse(parser, parser.slotDef(), "override CType? base { get { load; return *base } internal set}");
 			testNodeName("FieldDef6", result, "slotDef", "override CType? base { get { load; return *base } internal set}");
 			result = parse(parser, parser.slotDef(), "**comment\nUri reqUri := ``\n  {\n    set { if (!it.isAbs) throw ArgErr(\"Request URI not absolute: `$it`\"); *reqUri = it }\n  }");
 			testNodeName("FieldDef7", result, "slotDef", "**comment\nUri reqUri := ``\n  {\n    set { if (!it.isAbs) throw ArgErr(\"Request URI not absolute: `$it`\"); *reqUri = it }\n  }");
 			result = parse(parser, parser.slotDef(), "private static Void doit(Str a, Int b){}");
 			testNodeName("MethodDef1", result, "slotDef", "private static Void doit(Str a, Int b){}");
 			result = parse(parser, parser.slotDef(), "Void doit(Str s){i:=5}");
 			testNodeName("MethodDef2", result, "slotDef", "Void doit(Str s){i:=5}");
 			result = parse(parser, parser.slotDef(), "Void doit(Str s){Int i:=5\n\n\tj:=7}");
 			testNodeName("MethodDef3", result, "slotDef", "Void doit(Str s){Int i:=5\n\n\tj:=7}");
 			result = parse(parser, parser.slotDef(), "new doIt(Str s){Int i:=5\n\n\tj:=7}");
 			testNodeName("Ctor1", result, "slotDef", "new doIt(Str s){Int i:=5\n\n\tj:=7}");
 			result = parse(parser, parser.slotDef(), "new doIt(Str s):this.make(null, last){Int i:=5\n\n\tj:=7}");
 			testNodeName("Ctor2", result, "slotDef", "new doIt(Str s):this.make(null, last){Int i:=5\n\n\tj:=7}");
 			result = parse(parser, parser.slotDef(), "new doIt(Str s):super(){Int i:=5\n\n\tj:=7}");
 			testNodeName("Ctor3", result, "slotDef", "new doIt(Str s):super(){Int i:=5\n\n\tj:=7}");
 			result = parse(parser, parser.staticBlock(), "static{Int i:=5\n\n\tj:=7}");
 			testNodeName("StaticBlock", result, "staticBlock", "static{Int i:=5\n\n\tj:=7}");
 
 			// Type Def
 			result = parse(parser, parser.typeDef(), "internal final class Dummy\n{Int var}");
 			testNodeName("TypeDef - class", result, "typeDef", "internal final class Dummy\n{Int var}");
 			result = parse(parser, parser.typeDef(), "internal final class Dummy:Toto, Tata, Tutu\n{Int var}");
 			testNodeName("TypeDef - class2", result, "typeDef", "internal final class Dummy:Toto, Tata, Tutu\n{Int var}");
 			result = parse(parser, parser.typeDef(), "internal mixin Dummy\n{Int var}");
 			testNodeName("TypeDef - mixin", result, "typeDef", "internal mixin Dummy\n{Int var}");
 			//TODO: enum class, inheritance, facet class
 			//TODO: deal with enum val defs
 		}
 
 		if (refFilesTest)
 		{
 			// Test real files   TODO: relative paths
 			testFile("/home/thibautc/NetBeansProjects/Fan/test/parser_test/test.fan", false);
 			testFile("/home/thibautc/NetBeansProjects/Fan/test/parser_test/hello.fan", false);
 			testFile("/home/thibautc/NetBeansProjects/Fan/test/parser_test/files.fan", false);
 			testFile("/home/thibautc/NetBeansProjects/Fan/test/parser_test/maps.fan", false);
 			//testFile("/home/thibautc/NetBeansProjects/Fan/test/parser_test/sending.fan");
 		}
 
 		if (fantomFilesTest)
 		{
 			// Test all Fantom distro examples
 			testAllFanFilesUnder(FAN_HOME + "/examples/", false);
 			// Test all Fantom distro sources
 			testAllFanFilesUnder(FAN_HOME + "/src/", false);
 		}
 
 		if (fantomFilesLexerTest)
 		{
 			// Test all Fantom distro examples
 			testAllFanFilesUnder(FAN_HOME + "/examples/", true);
 			// Test all Fantom distro sources
 			testAllFanFilesUnder(FAN_HOME + "/src/", true);
 		}
 	}
 
 	public ParsingResult<AstNode> parse(FantomParser parser, Rule rule, String input)
 	{
 		long start = new Date().getTime();
 		ParsingResult<AstNode> result = RecoveringParseRunner.run(rule, input);
 		long time = new Date().getTime() - start;
 		//System.err.println("Parsing in " + (new Date().getTime() - start) + "ms");
 		if (time > 100)
 		{
 			System.err.println("Long parsing : " + (new Date().getTime() - start) + "ms, for:\n" + input);
 			//System.err.println(ParseTreeUtils.printNodeTree(result));
 		}
 		return result;
 	}
 
 	private void testAllFanFilesUnder(String folderPath, boolean lexerOnly) throws Exception
 	{
 		File folder = new File(folderPath);
 		File[] files = folder.listFiles();
 		for (File f : files)
 		{
 			if (f.isDirectory())
 			{
 				testAllFanFilesUnder(f.getAbsolutePath(), lexerOnly);
 			} else
 			{
 				if (f.getName().equals("gamma.fan")) // Known invalid file
 				{
 					continue;
 				}
 				if (f.getName().endsWith(".fan") || f.getName().endsWith(".fwt"))
 				{
 					testFile(f.getAbsolutePath(), lexerOnly);
 				}
 			}
 		}
 	}
 
 	public void testFile(String filePath, boolean lexerOnly) throws Exception
 	{
 		try
 		{
 			long start = new Date().getTime();
 			FantomParser parser = Parboiled.createParser(FantomParser.class, (Object)null);
 
 			DataInputStream dis = new DataInputStream(new FileInputStream(filePath));
 			byte[] buffer = new byte[dis.available()];
 			dis.readFully(buffer);
 			dis.close();//TODO: finally
 			String testInput = new String(buffer);
 
 			ParsingResult<AstNode> result = RecoveringParseRunner.run((lexerOnly ? parser.lexer() : parser.compilationUnit()), testInput);
 
 			long length = new Date().getTime() - start;
 			if (result.hasErrors())
 			{
 				System.err.println(StringUtils.join(result.parseErrors, "---\n"));
 				//System.err.println("Parse Tree:\n" + ParseTreeUtils.printNodeTree(result) + '\n');
 			}
 			/*if (length > 5000)
 			{
 			System.out.println("Slow parsing for " + filePath + " in " + length + "ms");
 			System.err.println("Parse Tree:\n" + ParseTreeUtils.printNodeTree(result) + '\n');
 			} else*/
 			{
 				System.out.println("Parsed " + (lexerOnly ? "(lex only)" : "") + filePath + " in " + length + "ms");
 			}
 
 			JOTTester.checkIf("Parsing " + (lexerOnly ? "(lex only)" : "") + filePath, !result.hasErrors());
 			JOTTester.checkIf("Parsing time " + (lexerOnly ? "(lex only)" : "") + filePath, length < 2000, "Took: " + length);
 		} catch (Exception e)
 		{
 			JOTTester.checkIf("Exception while parsing " + (lexerOnly ? "(lex only)" : "") + filePath, false);
 			e.printStackTrace();
 			//throw (e);
 		}
 	}
 
 	public void testNodeName(String label, ParsingResult<AstNode> result, String nodeName) throws Exception
 	{
 		testNodeName(label, result, nodeName, null);
 	}
 
 	public void testNodeName(String label, ParsingResult<AstNode> result, String nodeName, String value) throws Exception
 	{
 		if (result.hasErrors())
 		{
 			System.err.println(label + " -> " + StringUtils.join(result.parseErrors, "---\n"));
 			System.err.println(label + " -> " + ParseTreeUtils.printNodeTree(result));
 		}
 		JOTTester.checkIf(label + " - parsing success", !result.hasErrors());
 		JOTTester.checkIf(label + " - root not null", result.parseTreeRoot != null);
		JOTTester.checkIf(label + " - check name(" + nodeName + ")", result.parseTreeRoot.getLabel().equals(nodeName), result.parseTreeRoot.getLabel());
 		if (value != null)
 		{
 			String txt = ParseTreeUtils.getNodeText(result.parseTreeRoot, result.inputBuffer);
 			JOTTester.checkIf(label + " - check value", txt.equals(value), "'" + value + "' VS '" + txt + "'");
 		}
 	}
 
 	public void testAst(String filePath) throws Exception
 	{
 		FantomParser parser = Parboiled.createParser(FantomParser.class, (FanParserTask)null);
 
 		DataInputStream dis = new DataInputStream(new FileInputStream(filePath));
 		byte[] buffer = new byte[dis.available()];
 		dis.readFully(buffer);
 		dis.close();//TODO: finally
 		String testInput = new String(buffer);
 
 		ParsingResult<AstNode> result = RecoveringParseRunner.run(parser.compilationUnit(), testInput);
 
 		if (result.hasErrors())
 		{
 			System.err.println(StringUtils.join(result.parseErrors, "---\n"));
 			System.err.println("Parse Tree:\n" + ParseTreeUtils.printNodeTree(result) + '\n');
 		}
 
 		AstNode nd = result.parseTreeRoot.getValue();
 		String inc = "";
 		AstNode.printNodeTree(nd, inc, result.inputBuffer);
 		//System.out.println("Abstract Syntax Tree:\n" +
 		//            GraphUtils.printTree(result.parseTreeRoot.getValue(), new ToStringFormatter<AstNode>()) + '\n');
 		//System.err.println("Parse Tree:\n" + ParseTreeUtils.printNodeTree(result) + '\n');
 	}
 
 	public static void main(String[] args)
 	{
 		try
 		{
 			JOTTester.singleTest(new FantomParserTest(), false);
 		} catch (Throwable t)
 		{
 			t.printStackTrace();
 		}
 	}
 }
