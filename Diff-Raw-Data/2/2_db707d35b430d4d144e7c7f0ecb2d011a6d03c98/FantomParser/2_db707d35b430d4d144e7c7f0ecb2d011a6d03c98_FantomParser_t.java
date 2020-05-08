 /*
  * Thibaut Colar Feb 5, 2010
  */
 package net.colar.netbeans.fan.parboiled;
 
 import net.colar.netbeans.fan.FanParserTask;
 import org.parboiled.BaseParser;
 import org.parboiled.Rule;
 import net.colar.netbeans.fan.parboiled.FantomLexerTokens.TokenName;
 import org.parboiled.annotations.SuppressSubnodes;
 
 /**
  * Parboiled parser for the Fantom Language
  *
  * Current for Fantom grammar 1.0.51
  *
  * Grammar spec:
  * http://fantom.org/doc/docLang/Grammar.html
  *
  * Test Suite: net.colar.netbeans.fan.Test.FantomParserTest
  *
  * Note: ast.newNode() calls create ast nodes, using the last node matched (LAST_NODE())
  *		 ast.newScopeNode(), does the same for items that should introduce a scope.
  *
  * @author thibautc
  */
 @SuppressWarnings(
 {
 	"InfiniteRecursion"
 })
 public class FantomParser extends BaseParser<AstNode>
 {
 	//Note: Fields in PB parse can NOT be private
 	final FantomParserAstActions ast = new FantomParserAstActions();
 
 	boolean inFieldInit = false; // to help with differentiation of field accesor & itBlock
 	boolean inEnum = false; // so we know whether to allow enumDefs
 	// TODO: See if I can do away with this one and simplify  the map /simpelMap stuff
 	boolean noSimpleMap = false; // to disallow ambigous simpleMaps in certain situations (within another map, ternaryExpr)
 	/**Parse task that kicked in this parser*/
 	final FanParserTask parserTask;
 	public boolean cancel;
 
 	public FantomParser(FanParserTask parserTask)
 	{
 		this.parserTask=parserTask;
 	}
 
 	// ------------ Comp Unit --------------------------------------------------
 	public Rule compilationUnit()
 	{
 		return Sequence(
 			OPT_LF(),
 			Sequence(
 			// Missing from grammar: Optional unix env line
 			Optional(unixLine()),
 			ZeroOrMore(FirstOf(using(), incUsing())),
 			ZeroOrMore(typeDef()),
 			OPT_LF(),
 			ZeroOrMore(doc()) // allow for extra docs at end of file (if last type commented out)
 			// Create comp. unit AST node (root node)
 			), ast.newRootNode(AstKind.AST_COMP_UNIT, parserTask),
 			OPT_LF(),
 			Eoi());
 	}
 
 	public Rule unixLine()
 	{
 		return Sequence("#!", ZeroOrMore(Sequence(TestNot("\n"), Any())), "\n").label(TokenName.UNIXLINE.name());
 	}
 
 	public Rule using()
 	{
 		return Sequence(OPT_LF(), Sequence(
 			KW_USING,
 			Optional(ffi()),
 			Sequence(id(),
 			ZeroOrMore(Sequence(DOT, id())),
 			Optional(Sequence(SP_COLCOL, id()))), ast.newNode(AstKind.AST_ID),
 			Optional(usingAs()),
 			eos()), ast.newNode(AstKind.AST_USING), OPT_LF());
 	}
 
 	// Incomplete using - to allow for completion
 	public Rule incUsing()
 	{
 		return Sequence(OPT_LF(), Sequence(
 			KW_USING,
 			Optional(ffi()),
 			Sequence(Optional(id()), // Not Optional, but we want a valid ast for completion if missing
 			ZeroOrMore(Sequence(DOT, Optional(id()))),// not enforced to allow completion
 			Optional(Sequence(SP_COLCOL, Optional(id())))), ast.newNode(AstKind.AST_ID),// not enforced to allow completion
 			Optional(Sequence(Sequence(KW_AS, Optional(id())), ast.newNode(AstKind.AST_USING_AS))),
 			eos()), ast.newNode(AstKind.AST_INC_USING), OPT_LF());
 	}
 
 	public Rule ffi()
 	{
 		return enforcedSequence(SQ_BRACKET_L, id(), ast.newNode(net.colar.netbeans.fan.parboiled.AstKind.AST_USING_FFI), SQ_BRACKET_R);
 	}
 
 	public Rule usingAs()
 	{
 		return Sequence(KW_AS, id(), ast.newNode(AstKind.AST_USING_AS));
 	}
 
 	public Rule staticBlock()
 	{
 		return Sequence(KW_STATIC, OPT_LF(), enforcedSequence(BRACKET_L, ZeroOrMore(stmt()), OPT_LF(), BRACKET_R), ast.newScopeNode(AstKind.AST_BLOCK), OPT_LF());
 	}
 
 	// ------------- Type def --------------------------------------------------
 	public Rule typeDef()
 	{
 		// grouped together classdef, enumDef, mixinDef & facetDef as they are grammatically very similar (optimized)
 		return Sequence(
 			setInEnum(false),
 			OPT_LF(),
 			Sequence(
 			Optional(doc()),
 			ZeroOrMore(facet()),
 			Optional(protection()),
 			enforcedSequence(
 			FirstOf(
 			// Some fantom code has protection after modifiers, so allowing that
 			Sequence(Sequence(ZeroOrMore(Sequence(FirstOf(KW_ABSTRACT, KW_FINAL, KW_CONST), ast.newNode(AstKind.AST_MODIFIER))), Optional(protection()), KW_CLASS), ast.newNode(AstKind.AST_CLASS)), // standard class
 			enforcedSequence(ENUM, KW_CLASS, setInEnum(true), ast.newNode(AstKind.AST_ENUM)), // enum class
 			enforcedSequence(FACET, KW_CLASS, ast.newNode(AstKind.AST_FACET)), // facet class
 			Sequence(Sequence(Optional(Sequence(KW_CONST, ast.newNode(AstKind.AST_MODIFIER))), KW_MIXIN), ast.newNode(AstKind.AST_MIXIN)) // mixin
 			),
 			id(), ast.newNode(AstKind.AST_ID),
 			Optional(inheritance()),
 			OPT_LF(),
 			Sequence(
 			BRACKET_L,
 			OPT_LF(),
 			Optional(Sequence(Test(inEnum),Optional(enumValDefs()))), // only valid for enums, but simplifying
 			// Static block missing from Fan grammar
 			ZeroOrMore(FirstOf(staticBlock(), slotDef())),
 			BRACKET_R), ast.newNode(AstKind.AST_BLOCK))),
 			ast.newScopeNode(AstKind.AST_TYPE_DEF), OPT_LF());
 	}
 
 	public Rule protection()
 	{
 		return Sequence(FirstOf(KW_PUBLIC, KW_PROTECTED, KW_INTERNAL, KW_PRIVATE), ast.newNode(AstKind.AST_MODIFIER));
 	}
 
 	public Rule inheritance()
 	{
 		return enforcedSequence(SP_COL, typeList(), ast.newNode(AstKind.AST_INHERITANCE));
 	}
 
 	// ------------- Facets ----------------------------------------------------
 	public Rule facet()
 	{
 		return enforcedSequence(AT, simpleType(), Optional(facetVals()), OPT_LF());
 	}
 
 	public Rule facetVals()
 	{
 		return enforcedSequence(
 			BRACKET_L,
 			facetVal(),
 			ZeroOrMore(Sequence(eos(), facetVal())),
 			BRACKET_R);
 	}
 
 	public Rule facetVal()
 	{
 		return enforcedSequence(id(), AS_EQUAL, expr());
 	}
 
 	//------------------- Slot Def ---------------------------------------------
 	public Rule enumValDefs()
 	{
 		return Sequence(Sequence(enumValDef(), ZeroOrMore(Sequence(SP_COMMA, enumValDef()))), ast.newNode(AstKind.AST_ENUM_DEFS), eos());
 	}
 
 	public Rule enumValDef()
 	{
 		// Fantom grammar is missing "doc"
 		return Sequence(OPT_LF(), Optional(doc()), id(), ast.newNode(AstKind.AST_ENUM_NAME), Optional(enforcedSequence(PAR_L, Optional(args()), PAR_R)));
 	}
 
 	public Rule slotDef()
 	{
 		// Rewrote this to "unify" slots common parts (for better performance)
 		return Sequence(
 			OPT_LF(),
 			Sequence(
 			Optional(doc()),// common to all slots
 			ZeroOrMore(facet()),// common to all slots
 			Optional(protection()),// common to all slots
 			FirstOf(
 				ctorDef(), // look for 'new'
 				methodDef(), // look for params : '('
 				fieldDef())), // others
 			ast.newNode(AstKind.AST_SLOT_DEF),
 			OPT_LF());
 	}
 
 	public Rule fieldDef()
 	{
 		return Sequence(Sequence(
 			ZeroOrMore(Sequence(FirstOf(KW_ABSTRACT, KW_CONST, KW_FINAL, KW_STATIC,
 			KW_NATIVE, KW_OVERRIDE, KW_READONLY, KW_VIRTUAL), ast.newNode(AstKind.AST_MODIFIER))),
 			// Some fantom code has protection after modifiers, so allowing that
 			Optional(protection()),
 			/*typeAndOrId(), // Type required for fields(no infered) (Grammar does not say so)*/
 			type(),  ast.newNode(AstKind.AST_TYPE),
 			id(), ast.newNode(AstKind.AST_ID),
 			setFieldInit(true),
 			Optional(enforcedSequence(AS_INIT, OPT_LF(), expr())),
 			Optional(fieldAccessor()),
 			setFieldInit(false)), ast.newNode(AstKind.AST_FIELD_DEF),
 			eos());
 	}
 
 	public Rule methodDef()
 	{
 		return Sequence(enforcedSequence(
 				Sequence(
 				// Fan grammar misses 'final'
 				ZeroOrMore(Sequence(FirstOf(KW_ABSTRACT, KW_NATIVE, KW_ONCE, KW_STATIC,
 				KW_OVERRIDE, KW_VIRTUAL, KW_FINAL), ast.newNode(AstKind.AST_MODIFIER))),
 				// Some fantom code has protection after modifiers, so allowing that
 				Optional(protection()),
 				type(), ast.newNode(AstKind.AST_TYPE),
 				id(), ast.newNode(AstKind.AST_ID),
 				PAR_L),
 			Optional(params()),
 			PAR_R,
 			methodBody()), ast.newNode(AstKind.AST_METHOD_DEF)); // nees own scope because of params
 	}
 
 	public Rule ctorDef()
 	{
 		return Sequence(enforcedSequence(KW_NEW,
 			id(), ast.newNode(AstKind.AST_ID),
 			PAR_L,
 			Optional(params()),
 			PAR_R,
 			Optional( // ctorChain
 			// Fantom  Grammar page is missing the ':'
 			enforcedSequence(Sequence(OPT_LF(), SP_COL),
 			FirstOf(
 			enforcedSequence(KW_THIS, DOT, id(), enforcedSequence(PAR_L, Optional(args()), PAR_R)),
 			enforcedSequence(KW_SUPER, Optional(enforcedSequence(DOT, id())), enforcedSequence(PAR_L, Optional(args()), PAR_R))))),
 			methodBody()), ast.newNode(AstKind.AST_CTOR_DEF));
 	}
 
 	public Rule methodBody()
 	{
 		return Sequence(FirstOf(
 			enforcedSequence(Sequence(OPT_LF(), BRACKET_L), ZeroOrMore(stmt()), OPT_LF(), BRACKET_R),
 			eos()), ast.newScopeNode(AstKind.AST_BLOCK), OPT_LF()); // method with no body
 	}
 
 	public Rule params()
 	{
 		return Sequence(param(), ZeroOrMore(enforcedSequence(SP_COMMA, params())));
 	}
 
 	public Rule param()
 	{
 		return Sequence(OPT_LF(),
 			Sequence(type(), ast.newNode(AstKind.AST_TYPE), id(), ast.newNode(AstKind.AST_ID), Optional(enforcedSequence(AS_INIT, expr()))),
 			ast.newNode(AstKind.AST_PARAM),
 			OPT_LF());
 	}
 
 	public Rule fieldAccessor()
 	{
 		return Sequence(OPT_LF(),
 			enforcedSequence(
 			BRACKET_L,
 			Optional(Sequence(OPT_LF(), enforcedSequence(GET, FirstOf(block(), eos())))),
 			Optional(Sequence(OPT_LF(), Optional(protection()), enforcedSequence(SET, FirstOf(block(), eos())))),
 			BRACKET_R), ast.newNode(AstKind.AST_FIELD_ACCESSOR)); // do not consume trailing LF, since fieldDef looks for EOS
 	}
 
 	public Rule args()
 	{
 		return Sequence(expr(), ast.newNode(AstKind.AST_ARG),
 			ZeroOrMore(
 				Sequence(OPT_LF(),
 				enforcedSequence(SP_COMMA, OPT_LF(), expr(), ast.newNode(AstKind.AST_ARG)))),
 			OPT_LF());
 	}
 
 	// ------------ Statements -------------------------------------------------
 	public Rule block()
 	{
 		return Sequence(OPT_LF(), FirstOf(
 			enforcedSequence(BRACKET_L, ZeroOrMore(stmt()), OPT_LF(), BRACKET_R),
 			stmt() // single statement
 			), ast.newScopeNode(AstKind.AST_BLOCK), OPT_LF());
 	}
 
 	public Rule stmt()
 	{
 		return Sequence(TestNot(BRACKET_R), OPT_LF(),
 			FirstOf(
 			Sequence(KW_BREAK, eos()),
 			Sequence(KW_CONTINUE, eos()),
 			Sequence(KW_RETURN, Sequence(Optional(expr()), eos())),
 			Sequence(KW_THROW, expr(), eos()),
 			if_(),
 			for_(),
 			switch_(),
 			while_(),
 			try_(),
 			// check local var definition as it's faster to parse ':='
 			localDef(),
 			// otherwise expression (Optional Comma for itAdd expression)
 			// using FirstOf, because "," in this case can be considered an end of statement
 			Sequence(expr(), FirstOf(Sequence(SP_COMMA, Optional(eos())), eos()))),
 			OPT_LF());
 	}
 
 	public Rule for_()
 	{
 		return Sequence(enforcedSequence(KW_FOR, PAR_L,
 			// LocalDef consumes the SEMI as part of loking for EOS, so rewrote to deal with this
 			FirstOf(SP_SEMI, FirstOf(localDef(), Sequence(expr(), SP_SEMI))),
 			FirstOf(SP_SEMI, Sequence(expr(), SP_SEMI)),
 			Optional(expr()),
 			PAR_R,
 			block()), ast.newScopeNode(AstKind.AST_FOR_LOOP)); // introducing a scopr for the loop var
 	}
 
 	public Rule if_()
 	{
 		// using condExpr rather than expr
 		return enforcedSequence(KW_IF, PAR_L, condOrExpr(), PAR_R, block(),
 			Optional(enforcedSequence(KW_ELSE, block())));
 	}
 
 	public Rule while_()
 	{
 		// using condExpr rather than expr
 		return enforcedSequence(KW_WHILE, PAR_L, condOrExpr(), PAR_R, block());
 	}
 
 	public Rule localDef()
 	{
 		// slight change from the grammar to match either:
 		// 'Int j', 'j:=27', 'Int j:=27'
 		return Sequence(
 			FirstOf(
 			// fan parser says if it's start with "id :=" or "Type, id", then it gotta be a localDef (enforce)
 			enforcedSequence(typeAndId(), AS_INIT, OPT_LF(), expr()),
 			// same if it starts with "id :="
 			enforcedSequence(Sequence(id(), ast.newNode(AstKind.AST_ID), AS_INIT), OPT_LF(), expr()),
 			// var def with no value
 			typeAndId()),
 			ast.newNode(AstKind.AST_LOCAL_DEF),
 			eos());
 	}
 
 	public Rule typeAndId()
 	{
 		return Sequence(Sequence(type(), ast.newNode(AstKind.AST_TYPE), id(), ast.newNode(AstKind.AST_ID)), ast.newNode(AstKind.AST_TYPE_AND_ID));
 	}
 
 	public Rule try_()
 	{
 		return enforcedSequence(KW_TRY, block(), ZeroOrMore(catch_()),
 			Optional(Sequence(KW_FINALLY, block())));
 	}
 
 	public Rule catch_()
 	{
 		return Sequence(enforcedSequence(KW_CATCH,
 				Optional(enforcedSequence(PAR_L, type(),
 				ast.newNode(AstKind.AST_TYPE), id(),
 				ast.newNode(AstKind.AST_ID), PAR_R)), block()),
 			ast.newNode(AstKind.AST_CATCH_BLOCK));
 	}
 
 	public Rule switch_()
 	{
 		// Note: unlike java, fan as a scope for each 'case'
 		return Sequence(enforcedSequence(KW_SWITCH, PAR_L, expr(), PAR_R,
 			OPT_LF(), BRACKET_L, OPT_LF(),
 			ZeroOrMore(enforcedSequence(Sequence(
 			OneOrMore(Sequence(KW_CASE, setNoSimpleMap(true), expr(), setNoSimpleMap(false), SP_COL, OPT_LF())),
 			ZeroOrMore(Sequence(stmt(), OPT_LF()))), ast.newScopeNode(AstKind.AST_SWITCH_CASE))),
 			Optional(enforcedSequence(Sequence(KW_DEFAULT, SP_COL, ZeroOrMore(Sequence(stmt(),OPT_LF()))),
 			ast.newScopeNode(AstKind.AST_SWITCH_CASE))),
 			OPT_LF(), BRACKET_R), ast.newScopeNode(AstKind.AST_SWITCH));
 	}
 
 	// ----------- Expressions -------------------------------------------------
 	public Rule expr()
 	{
 		return Sequence(assignExpr(), ast.newNode(AstKind.AST_EXPR));
 	}
 
 	public Rule assignExpr()
 	{
 		// check '=' first as is most common
 		// moved localDef to statement since it can't be on the right hand side
 		return Sequence(ifExpr(), Optional(enforcedSequence(FirstOf(AS_EQUAL, AS_OPS), OPT_LF(), assignExpr(), ast.newNode(AstKind.AST_EXPR_ASSIGN))));
 	}
 
 	public Rule ifExpr()
 	{
 		// rewritten (together with ternaryTail, elvisTail) such as we check condOrExpr only once
 		// this makes a gigantic difference in parser speed form original grammar
 		return Sequence(condOrExpr(),
 			Optional(FirstOf(elvisTail(), ternaryTail())));
 	}
 
 	public Rule ternaryTail()
 	{
 		return enforcedSequence(Sequence(OPT_LF(), SP_QMARK), OPT_LF(), setNoSimpleMap(true), ifExprBody(), setNoSimpleMap(false), OPT_LF(), SP_COL, OPT_LF(), ifExprBody());
 	}
 
 	public Rule elvisTail()
 	{
 		return enforcedSequence(Sequence(OPT_LF(), OP_ELVIS), OPT_LF(), ifExprBody());
 	}
 
 	public Rule ifExprBody()
 	{
 		return FirstOf(enforcedSequence(KW_THROW, expr()), condOrExpr());
 	}
 
 	public Rule condOrExpr()
 	{
 		return Sequence(condAndExpr(), ZeroOrMore(enforcedSequence(OP_OR, OPT_LF(), condAndExpr())));
 	}
 
 	public Rule condAndExpr()
 	{
 		return Sequence(equalityExpr(), ZeroOrMore(enforcedSequence(OP_AND, OPT_LF(), equalityExpr())));
 	}
 
 	public Rule equalityExpr()
 	{
 		return Sequence(relationalExpr(), ZeroOrMore(enforcedSequence(CP_EQUALITY, OPT_LF(), relationalExpr())));
 	}
 
 	public Rule relationalExpr()
 	{
 		// Changed (with typeCheckTail, compareTail) to check for rangeExpr only once (way faster)
 		return Sequence(rangeExpr(), Optional(FirstOf(typeCheckTail(), compareTail())));
 	}
 
 	public Rule typeCheckTail()
 	{
 		// changed to required, otherwise consumes all rangeExpr and compare never gets evaled
 		return Sequence(enforcedSequence(
 				FirstOf(KW_IS, KW_ISNOT, KW_AS),
 				type(),
 				ast.newNode(AstKind.AST_TYPE)),
 			ast.newNode(AstKind.AST_EXPR_TYPE_CHECK));
 	}
 
 	public Rule compareTail()
 	{
 		// changed to not be ZeroOrMore as there can be only one comparaison check in an expression (no 3< x <5)
 		return /*ZeroOrMore(*/enforcedSequence(CP_COMPARATORS, OPT_LF(), rangeExpr())/*)*/;
 	}
 
 	public Rule rangeExpr()
 	{
 		// changed to not be ZeroOrMore(opt instead) as there can be only one range in an expression (no [1..3..5])
 		return Sequence(Sequence(addExpr(), ast.newNode(AstKind.AST_EXPR),
 			Optional(enforcedSequence(FirstOf(OP_RANGE_EXCL, OP_RANGE), OPT_LF(), addExpr(), ast.newNode(AstKind.AST_EXPR)))),
 				ast.newNode(AstKind.AST_EXPR_RANGE));
 	}
 
 	public Rule addExpr()
 	{
 		return Sequence(multExpr(),
 			// checking it's not '+=' or '-=', so we can let assignExpr happen
 			ZeroOrMore(enforcedSequence(Sequence(FirstOf(OP_PLUS, OP_MINUS), TestNot(AS_EQUAL)), OPT_LF(), multExpr(), ast.newNode(AstKind.AST_EXPR_ADD))));
 	}
 
 	public Rule multExpr()
 	{
 		return Sequence(parExpr(),
 			// checking it's not '*=', '/=' or '%=', so we can let assignExpr happen
 			ZeroOrMore(enforcedSequence(Sequence(FirstOf(OP_MULT, OP_DIV, OP_MODULO), TestNot(AS_EQUAL)), OPT_LF(), parExpr(), ast.newNode(AstKind.AST_EXPR_MULT))));
 	}
 
 	public Rule parExpr()
 	{
 		return FirstOf(castExpr(), groupedExpr(), unaryExpr());
 	}
 
 	public Rule castExpr()
 	{
 		return Sequence(Sequence(PAR_L, type(), ast.newNode(AstKind.AST_TYPE), PAR_R, parExpr(), ast.newNode(AstKind.AST_EXPR)), ast.newNode(AstKind.AST_EXR_CAST));
 	}
 
 	public Rule groupedExpr()
 	{
 		return Sequence(PAR_L, OPT_LF(), expr(), OPT_LF(), PAR_R, ZeroOrMore(termChain()));
 	}
 
 	public Rule unaryExpr()
 	{
 		// grouped with postfixEpr to avoid looking for termExpr twice (very slow parsing !)
 		return FirstOf(prefixExpr(), Sequence(termExpr(), Optional(FirstOf(OP_2MINUS, OP_2PLUS))));
 	}
 
 	public Rule prefixExpr()
 	{
 		return Sequence(
 			FirstOf(OP_CURRY, OP_BANG, OP_2PLUS, OP_2MINUS, OP_PLUS, OP_MINUS),
 			parExpr());
 	}
 
 	public Rule termExpr()
 	{
 		return Sequence(termBase(), ZeroOrMore(termChain()));
 	}
 
 	public Rule termBase()
 	{
 		// check for ID alone last (and not as part of idExpr) otherwise it would never check literal & typebase !
 		return FirstOf(idExprReq(), litteral(), typeBase(), Sequence(id(), ast.newNode(AstKind.AST_ID)));
 	}
 
 	public Rule typeBase()
 	{
 		return FirstOf(
 				enforcedSequence(Sequence(OP_POUND, id()), ast.newNode(AstKind.AST_TYPE_LITTERAL)), // slot litteral (without type)
 				closure(),
 				Sequence(dsl(), ast.newNode(AstKind.AST_DSL)), // DSL
 				// Optimized by grouping all the items that start with "type" (since looking for type if resource intensive)
 				Sequence(type(), ast.newNode(AstKind.AST_ID),
 				FirstOf(
 					Sequence(Sequence(OP_POUND, Optional(id())), ast.newNode(AstKind.AST_TYPE_LITTERAL)), // type/slot litteral
 					Sequence(DOT, KW_SUPER, ast.newNode(AstKind.AST_CALL)), // named super
 					Sequence(Sequence(DOT, ast.newNode(AstKind.LBL_OP), idExpr()), ast.newNode(AstKind.AST_CALL_EXPR)), // static call
 					Sequence(PAR_L, expr(), PAR_R), // simple ?? (ctor call)
 					ctorBlock() // ctor block
 				)));
 	}
 
 	public Rule ctorBlock()
 	{
 		return Sequence(
 			OPT_LF(),
 			enforcedSequence(Sequence(BRACKET_L,
 			// Note, don't allow field accesors to be parsed as ctorBlock
 			TestNot(Sequence(inFieldInit, OPT_LF(), FirstOf(protection(), KW_STATIC, KW_READONLY, GET, SET, GET, SET)))),
 			ZeroOrMore(stmt()), BRACKET_R), ast.newScopeNode(AstKind.AST_CTOR_BLOCK));
 	}
 
 	public Rule dsl()
 	{
 		//TODO: unclosed DSL ?
 		return Sequence(simpleType(), ast.newNode(net.colar.netbeans.fan.parboiled.AstKind.AST_TYPE),
 			enforcedSequence(DSL_OPEN, OPT_LF(), ZeroOrMore(Sequence(TestNot(DSL_CLOSE), Any())), OPT_LF(), DSL_CLOSE)).label(TokenName.DSL.name());
 	}
 
 	public Rule closure()
 	{
 		return Sequence(Sequence(OPT_LF(), funcType(), OPT_LF(),
 			Sequence(enforcedSequence(BRACKET_L, ZeroOrMore(stmt()), BRACKET_R), ast.newScopeNode(AstKind.AST_BLOCK))
 			), ast.newNode(AstKind.AST_CLOSURE));
 	}
 
 	public Rule itBlock()
 	{
 		return Sequence(
 			OPT_LF(),
 			enforcedSequence(Sequence(BRACKET_L,
 			// Note, don't allow field accesors to be parsed as itBlock
 			TestNot(Sequence(inFieldInit, OPT_LF(), FirstOf(protection(), KW_STATIC, KW_READONLY, GET, SET, GET, SET)/*, echo("Skipping itBlock")*/))),
 			ZeroOrMore(stmt()), BRACKET_R), ast.newScopeNode(AstKind.AST_IT_BLOCK));
 	}
 
 	public Rule termChain()
 	{
 		return Sequence(OPT_LF(),
 				FirstOf(safeDotCall(), safeDynCall(), dotCall(), dynCall(), indexExpr(),
 			callOp(), itBlock(), incCall()));
 	}
 
 	public Rule dotCall()
 	{
 		// Test not "..", as this would be a range
 		return Sequence(enforcedSequence(Sequence(DOT, TestNot(DOT)), ast.newNode(AstKind.LBL_OP), idExpr()), ast.newNode(AstKind.AST_CALL_EXPR));
 	}
 
 	public Rule dynCall()
 	{
 		return Sequence(enforcedSequence(OP_ARROW, ast.newNode(AstKind.LBL_OP), idExpr()), ast.newNode(AstKind.AST_CALL_EXPR));
 	}
 
 	public Rule safeDotCall()
 	{
 		return Sequence(enforcedSequence(OP_SAFE_CALL, ast.newNode(AstKind.LBL_OP), idExpr()), ast.newNode(AstKind.AST_CALL_EXPR));
 	}
 
 	public Rule safeDynCall()
 	{
 		return Sequence(enforcedSequence(OP_SAFE_DYN_CALL, ast.newNode(AstKind.LBL_OP), idExpr()), ast.newNode(AstKind.AST_CALL_EXPR));
 	}
 
 	// incomplete dot call, make valid to allow for completion
 	//TODO: this is not shown as an error
 	public Rule incCall()
 	{
 		return Sequence(TestNot(Sequence(DOT, DOT)), // DOT DOT would be a range.
 			FirstOf(DOT, OP_SAFE_DYN_CALL), ast.newNode(AstKind.AST_INC_CALL));
 	}
 
 	public Rule idExpr()
 	{
 		// this can be either a local def(toto.value) or a call(toto.getValue or toto.getValue(<params>)) + opt. closure
 		return FirstOf(idExprReq(),
 				Sequence(
 					Sequence(id(), ast.newNode(AstKind.AST_ID))
 					,ast.newNode(AstKind.AST_CALL)));
 	}
 
 	public Rule idExprReq()
 	{
 		// Same but without matching ID by itself (this would prevent termbase from checking literals).
 		return FirstOf(field(), call());
 	}
 
 	// require '*' otherwise it's just and ID (this would prevent termbase from checking literals)
 	public Rule field()
 	{
		return Sequence(OP_CURRY, id(), ast.newNode(AstKind.AST_ID));
 	}
 
 	// require params or/and closure, otherwise it's just and ID (this would prevent termbase from checking literals)
 	public Rule call()
 	{
 		return Sequence(Sequence(id(), ast.newNode(AstKind.AST_ID),
 			FirstOf(
 			Sequence(noSpace(), enforcedSequence(PAR_L, OPT_LF(), Optional(args()), PAR_R), Optional(closure())), //params & opt. closure
 			closure())), ast.newNode(AstKind.AST_CALL)); // closure only
 	}
 
 	public Rule indexExpr()
 	{
 		return Sequence(noSpace(), SQ_BRACKET_L, expr(), ast.newNode(AstKind.AST_EXPR_INDEX), SQ_BRACKET_R);
 	}
 
 	public Rule callOp()
 	{
 		return enforcedSequence(noSpace(), PAR_L, Optional(args()), PAR_R, Optional(closure()));
 	}
 
 	public Rule litteral()
 	{
 		return FirstOf(litteralBase(), list(), map());
 	}
 
 	public Rule litteralBase()
 	{
 		return Sequence(FirstOf(KW_NULL, KW_THIS, KW_SUPER, KW_IT, KW_TRUE, KW_FALSE,
 			strs(), uri(), number(), char_()), ast.newNode(AstKind.AST_EXPR_LIT_BASE));
 	}
 
 	public Rule list()
 	{
 		return Sequence(Sequence(
 			Optional(Sequence(type(), ast.newNode(AstKind.AST_TYPE))), OPT_LF(),
 			SQ_BRACKET_L, OPT_LF(), listItems(), OPT_LF(), SQ_BRACKET_R), ast.newNode(AstKind.AST_LIST));
 	}
 
 	public Rule listItems()
 	{
 		return FirstOf(
 			SP_COMMA,
 			// allow extra comma
 			Sequence(expr(), ZeroOrMore(Sequence(SP_COMMA, OPT_LF(), expr())), Optional(SP_COMMA)));
 	}
 
 	public Rule map()
 	{
 		return Sequence(Sequence(
 			Optional(Sequence(FirstOf(mapType(), simpleMapType()), ast.newNode(AstKind.AST_TYPE))),
 			// Not enforced to allow resolving list of typed maps like [Str:Int][]
 			Sequence(SQ_BRACKET_L, OPT_LF(), mapItems(), OPT_LF(), SQ_BRACKET_R)),ast.newNode(AstKind.AST_MAP));
 	}
 
 	public Rule mapItems()
 	{
 		return FirstOf(SP_COL,//empty map
 			// allow extra comma
 			Sequence(mapPair(), ZeroOrMore(Sequence(SP_COMMA, OPT_LF(), mapPair())), Optional(SP_COMMA)));
 	}
 
 	public Rule mapPair()
 	{
 		// allowing all expressions is probably more than really needed
 		return Sequence(Sequence(expr(), enforcedSequence(SP_COL, expr())), ast.newNode(AstKind.AST_MAP_PAIR));
 	}
 
 	public Rule mapItem()
 	{
 		return expr();
 	}
 
 	// ------------ Litteral items ---------------------------------------------
 	@SuppressSubnodes
 	public Rule strs()
 	{
 		return FirstOf(
 			enforcedSequence("\"\"\"", // triple quoted string, // (not using 3QUOTE terminal, since it could consume empty space inside the string)
 			ZeroOrMore(FirstOf(
 			unicodeChar(),
 			escapedChar(),
 			Sequence(TestNot(QUOTES3), Any()))), QUOTES3),
 			enforcedSequence("\"", // simple string, (not using QUOTE terminal, since it could consume empty space inside the string)
 			ZeroOrMore(FirstOf(
 			unicodeChar(),
 			escapedChar(),
 			Sequence(TestNot(QUOTE), Any()))), QUOTE)).label(TokenName.STRS.name());
 	}
 
 	@SuppressSubnodes
 	public Rule uri()
 	{
 		return enforcedSequence("`",// (not using TICK terminal, since it could consume empty space inside the string)
 			ZeroOrMore(FirstOf(
 			unicodeChar(),
 			// missing from Fantom litteral page, special URI escape Sequences
 			Sequence('\\', FirstOf(':','/','#','[',']','@','&','=',';','?')),
 			escapedChar(),
 			Sequence(TestNot(TICK), Any()))),
 			TICK).label(TokenName.URI.name());
 	}
 
 	@SuppressSubnodes
 	public Rule char_()
 	{
 		return enforcedSequence('\'',// (not using SINGLE_Q terminal, since it could consume empty space inside the char)
 				FirstOf(
 				unicodeChar(),
 				escapedChar(), // standard esapes
 				Any()), //all else
 				SINGLE_Q).label(TokenName.CHAR_.name());
 	}
 
 	@SuppressSubnodes
 	public Rule escapedChar()
 	{
 		return enforcedSequence('\\', FirstOf('b', 'f', 'n', 'r', 't', '"', '\'', '`', '$', '\\'));
 	}
 
 	@SuppressSubnodes
 	public Rule unicodeChar()
 	{
 		return enforcedSequence("\\u", hexDigit(), hexDigit(), hexDigit(), hexDigit());
 	}
 
 	@SuppressSubnodes
 	public Rule hexDigit()
 	{
 		return FirstOf(digit(),
 			CharRange('a', 'f'),
 			CharRange('A', 'F'));
 	}
 
 	@SuppressSubnodes
 	public Rule digit()
 	{
 		return CharRange('0', '9');
 	}
 
 	@SuppressSubnodes
 	public Rule number()
 	{
 		return Sequence(
 			Optional(OP_MINUS),
 			FirstOf(
 			// hex number
 			enforcedSequence(FirstOf("0x", "0X"), OneOrMore(FirstOf("_", hexDigit()))),
 			// decimal
 			// fractional
 			enforcedSequence(fraction(), Optional(exponent())),
 			enforcedSequence(digit(),
 			ZeroOrMore(Sequence(ZeroOrMore("_"), digit())),
 			Optional(fraction()),
 			Optional(exponent()))),
 			Optional(nbType()), OPT_SP).label(TokenName.NUMBER.name());
 	}
 
 	@SuppressSubnodes
 	public Rule fraction()
 	{
 		// not enfored to allow: "3.times ||" constructs as well as ranges 3..5
 		return Sequence(DOT, digit(), ZeroOrMore(Sequence(ZeroOrMore("_"), digit())));
 	}
 
 	@SuppressSubnodes
 	public Rule exponent()
 	{
 		return enforcedSequence(CharSet("eE"),
 			Optional(FirstOf(OP_PLUS, OP_MINUS)),
 			digit(),
 			ZeroOrMore(Sequence(ZeroOrMore("_"), digit())));
 	}
 
 	@SuppressSubnodes
 	public Rule nbType()
 	{
 		return FirstOf(
 			"day", "hr", "min", "sec", "ms", "ns", //durations
 			"f", "F", "D", "d" // float / decimal
 			);
 	}
 
 	/** Rewrote more like Fantom Parser (simpler & faster) - look for type base then listOfs, mapOfs notations
 	 * Added check so that the "?" (nullable type indicator) cannot be separated from it's type (noSpace)
 	 * @return
 	 */
 	public Rule type()
 	{
 		return Sequence(
 			FirstOf(
 				mapType(),
 				funcType(),
 				Sequence(id(), Optional(Sequence(noSpace(), SP_COLCOL, noSpace(), id())))
 				),
 			// Look for Optional map/list/nullable items
 			// Note: ? folowed by a dot is nor part of the type (null check call)
 			Optional(Sequence(noSpace(), SP_QMARK, TestNot(DOT))), //nullable
 			ZeroOrMore(Sequence(noSpace(),SQ_BRACKET_L, SQ_BRACKET_R)),//list(s)
 			// Do not allow simple maps within left side of expressions ... this causes issues with ":"
 			Optional(Sequence(TestNot(noSimpleMap), SP_COL, type())),//simple map Int:Str
 			Optional(Sequence(noSpace(), SP_QMARK, TestNot(DOT))) // nullable list/map
 			);
 	}
 
 	public Rule simpleMapType()
 	{
 		// It has to be other nonSimpleMapTypes, otherwise it's left recursive (loops forever)
 		return Sequence(nonSimpleMapTypes(), SP_COL, nonSimpleMapTypes(),
 			Optional(
 			// Not enforcing [] because of problems with maps like this Str:int["":5]
 			Sequence(Optional(Sequence(noSpace(),SP_QMARK, TestNot(DOT))), SQ_BRACKET_L, SQ_BRACKET_R)), // list of '?[]'
 			Optional(Sequence(noSpace(),SP_QMARK, TestNot(DOT)))); // nullable
 	}
 
 	// all types except simple map
 	public Rule nonSimpleMapTypes()
 	{
 		return Sequence(
 			FirstOf(funcType(), mapType(), simpleType()),
 			Optional(
 			// Not enforcing [] because of problems with maps like this Str:int["":5]
 			Sequence(Optional(Sequence(noSpace(),SP_QMARK, TestNot(DOT))), SQ_BRACKET_L, SQ_BRACKET_R)), // list of '?[]'
 			Optional(Sequence(noSpace(),SP_QMARK, TestNot(DOT)))); // nullable
 	}
 
 	public Rule simpleType()
 	{
 		return Sequence(
 			id(),
 			Optional(enforcedSequence(SP_COLCOL, id())));
 	}
 
 	public Rule mapType()
 	{
 		// We use nonSimpleMapTypes here as well, because [Str:Int:Str] would be confusing
 		// not enforced to allow map rule to work ([Int:Str][5,""])
 		return Sequence(SQ_BRACKET_L, nonSimpleMapTypes(), SP_COL, nonSimpleMapTypes(), SQ_BRACKET_R);
 	}
 
 	public Rule funcType()
 	{
 		return Sequence(
 			TestNot(OP_OR), // '||' that's a logical OR not a function type
 			// '|' could be the closing pipe so we can't enforce
 			Sequence(SP_PIPE,
 			FirstOf(
 			// First we check for one with no formals |->| or |->Str|
 			enforcedSequence(OP_ARROW, Optional(Sequence(type(), ast.newNode(AstKind.AST_TYPE)))),
 			// Then we check for one with formals |Int i| or maybe full: |Int i -> Str|
 			Sequence(formals(), Optional(enforcedSequence(OP_ARROW, Optional(Sequence(type(), ast.newNode(AstKind.AST_TYPE))))))),
 			SP_PIPE));
 	}
 
 	public Rule formals()
 	{
 		// Allowing funcType within formals | |Int-Str a| -> Str|
 		return Sequence(
 			formal(),
 			ZeroOrMore(enforcedSequence(SP_COMMA, formal())));
 	}
 
 	public Rule typeList()
 	{
 		return Sequence(type(), ast.newNode(AstKind.AST_TYPE), ZeroOrMore(enforcedSequence(SP_COMMA, type(), ast.newNode(AstKind.AST_TYPE))));
 	}
 
 	public Rule formal()
 	{
 		// Note it can be "type id", "type" or "id"
 		// but parser can't know if it's "type" or "id" so always recognize as type
 		// so would never actually hit id()
 		return Sequence(FirstOf(
 			typeAndId(),
 			Sequence(type(), ast.newNode(AstKind.AST_ID)))
 			/*, id()*/
 			, ast.newNode(AstKind.AST_FORMAL));
 	}
 	// ------------ Misc -------------------------------------------------------
 
 	@SuppressSubnodes
 	public Rule id()
 	{
 		return Sequence(TestNot(keyword()),
 			Sequence(FirstOf(CharRange('A', 'Z'), CharRange('a', 'z'), "_"),
 			ZeroOrMore(FirstOf(CharRange('A', 'Z'), CharRange('a', 'z'), '_', CharRange('0', '9')))),
 			OPT_SP).label(TokenName.ID.name());
 	}
 
 	@SuppressSubnodes
 	public Rule doc()
 	{
 		// In theory there are no empty lines betwen doc and type ... but that does happen so alowing it
 		return OneOrMore(Sequence(OPT_SP, "**", ZeroOrMore(Sequence(TestNot("\n"), Any())), OPT_LF())).label(TokenName.DOC.name());
 	}
 
 	@SuppressSubnodes
 	public Rule spacing()
 	{
 		return OneOrMore(FirstOf(
 			// whitespace (Do NOT eat \n since it can be meaningful)
 			whiteSpace(), comment())).label(TokenName.SPACING.name());
 	}
 
 	@SuppressSubnodes
 	public Rule whiteSpace()
 	{
 		return OneOrMore(CharSet(" \t\u000c")).label(TokenName.WHITESPACE.name());
 	}
 
 	@SuppressSubnodes
 	public Rule comment()
 	{
 		return FirstOf(
 			// multiline comment
 			Sequence("/*", ZeroOrMore(Sequence(TestNot("*/"), Any())), "*/"),
 			// if incomplete multiline comment, then end at end of line
 			Sequence("/*", ZeroOrMore(Sequence(TestNot(CharSet("\r\n")), Any()))),
 			// single line comment
 			Sequence("//", ZeroOrMore(Sequence(TestNot(CharSet("\r\n")), Any())))).label(TokenName.COMMENT.name());
 	}
 
 	@SuppressSubnodes
 	public Rule LF()
 	{
 		return Sequence(OneOrMore(Sequence(OPT_SP, CharSet("\n\r"))), OPT_SP).label(TokenName.LF.name());
 	}
 
 	@SuppressSubnodes
 	public Rule OPT_LF()
 	{
 		return Optional(LF());
 	}
 
 	@SuppressSubnodes
 	public Rule keyword(String string)
 	{
 		// Makes sure not to match things that START with a keyword like "thisisaTest"
 		return Sequence(string,
 			TestNot(FirstOf(digit(), CharRange('A', 'Z'), CharRange('a', 'z'), "_")),
 			Optional(spacing())).label(string);
 	}
 
 	@SuppressSubnodes
 	public Rule terminal(String string)
 	{
 		return Sequence(string, Optional(spacing())).label(string);
 	}
 
 	@SuppressSubnodes
 	public Rule terminal(String string, Rule mustNotFollow)
 	{
 		return Sequence(string, TestNot(mustNotFollow), Optional(spacing())).label(string);
 	}
 
 	@SuppressSubnodes
 	public Rule keyword()
 	{
 		return Sequence(
 			// don't bother unless it starts with 'a'-'z'
 			Test(CharRange('a', 'z')),
 			FirstOf(KW_ABSTRACT, KW_AS, KW_ASSERT, KW_BREAK, KW_CATCH, KW_CASE, KW_CLASS,
 			KW_CONST, KW_CONTINUE, KW_DEFAULT, KW_DO, KW_ELSE, KW_FALSE, KW_FINAL,
 			KW_FINALLY, KW_FOR, KW_FOREACH, KW_IF, KW_INTERNAL, KW_IS, KW_ISNOT, KW_IT,
 			KW_MIXIN, KW_NATIVE, KW_NEW, KW_NULL, KW_ONCE, KW_OVERRIDE, KW_PRIVATE,
 			KW_PROTECTED, KW_PUBLIC, KW_READONLY, KW_RETURN, KW_STATIC, KW_SUPER, KW_SWITCH,
 			KW_THIS, KW_THROW, KW_TRUE, KW_TRY, KW_USING, KW_VIRTUAL, KW_VOID, KW_VOLATILE,
 			KW_WHILE)).label(TokenName.KEYWORD.name());
 	}
 	// -------------- Terminal items -------------------------------------------
 	// -- Keywords --
 	public final Rule KW_ABSTRACT = keyword("abstract");
 	public final Rule KW_AS = keyword("as");
 	public final Rule KW_ASSERT = keyword("assert"); // not a grammar kw
 	public final Rule KW_BREAK = keyword("break");
 	public final Rule KW_CATCH = keyword("catch");
 	public final Rule KW_CASE = keyword("case");
 	public final Rule KW_CLASS = keyword("class");
 	public final Rule KW_CONST = keyword("const");
 	public final Rule KW_CONTINUE = keyword("continue");
 	public final Rule KW_DEFAULT = keyword("default");
 	public final Rule KW_DO = keyword("do"); // unused, reserved
 	public final Rule KW_ELSE = keyword("else");
 	public final Rule KW_FALSE = keyword("false");
 	public final Rule KW_FINAL = keyword("final");
 	public final Rule KW_FINALLY = keyword("finally");
 	public final Rule KW_FOR = keyword("for");
 	public final Rule KW_FOREACH = keyword("foreach"); // unused, reserved
 	public final Rule KW_IF = keyword("if");
 	public final Rule KW_INTERNAL = keyword("internal");
 	public final Rule KW_IS = keyword("is");
 	public final Rule KW_IT = keyword("it");
 	public final Rule KW_ISNOT = keyword("isnot");
 	public final Rule KW_MIXIN = keyword("mixin");
 	public final Rule KW_NATIVE = keyword("native");
 	public final Rule KW_NEW = keyword("new");
 	public final Rule KW_NULL = keyword("null");
 	public final Rule KW_ONCE = keyword("once");
 	public final Rule KW_OVERRIDE = keyword("override");
 	public final Rule KW_PRIVATE = keyword("private");
 	public final Rule KW_PUBLIC = keyword("public");
 	public final Rule KW_PROTECTED = keyword("protected");
 	public final Rule KW_READONLY = keyword("readonly");
 	public final Rule KW_RETURN = keyword("return");
 	public final Rule KW_STATIC = keyword("static");
 	public final Rule KW_SUPER = keyword("super");
 	public final Rule KW_SWITCH = keyword("switch");
 	public final Rule KW_THIS = keyword("this");
 	public final Rule KW_THROW = keyword("throw");
 	public final Rule KW_TRUE = keyword("true");
 	public final Rule KW_TRY = keyword("try");
 	public final Rule KW_USING = keyword("using");
 	public final Rule KW_VIRTUAL = keyword("virtual");
 	public final Rule KW_VOID = keyword("void"); // unused, reserved
 	public final Rule KW_VOLATILE = keyword("volatile"); // unused, reserved
 	public final Rule KW_WHILE = keyword("while");
 	// Non keyword meningful items
 	public final Rule ENUM = keyword("enum");
 	public final Rule FACET = keyword("facet");
 	public final Rule GET = keyword("get");
 	public final Rule SET = keyword("set");
 	// operators
 	public final Rule OP_SAFE_CALL = terminal("?.");
 	public final Rule OP_SAFE_DYN_CALL = terminal("?->");
 	public final Rule OP_ARROW = terminal("->");
 	public final Rule OP_ELVIS = terminal("?:");
 	public final Rule OP_OR = terminal("||");
 	public final Rule OP_AND = terminal("&&");
 	public final Rule OP_RANGE = terminal("..");
 	public final Rule OP_RANGE_EXCL = terminal("..<");
 	public final Rule OP_CURRY = terminal("&");
 	public final Rule OP_BANG = terminal("!");
 	public final Rule OP_2PLUS = terminal("++");
 	public final Rule OP_2MINUS = terminal("--");
 	public final Rule OP_PLUS = terminal("+");
 	public final Rule OP_MINUS = terminal("-");
 	public final Rule OP_MULT = terminal("*");
 	public final Rule OP_DIV = terminal("/");
 	public final Rule OP_MODULO = terminal("%");
 	public final Rule OP_POUND = terminal("#");
 	// comparators
 	public final Rule CP_EQUALITY = FirstOf(terminal("==="), terminal("!=="),
 		terminal("=="), terminal("!="));
 	public final Rule CP_COMPARATORS = FirstOf(terminal("<=>"), terminal("<="),
 		terminal(">="), terminal("<"), terminal(">"));
 	// separators
 	public final Rule SP_PIPE = terminal("|");
 	public final Rule SP_QMARK = terminal("?");
 	public final Rule SP_COLCOL = terminal("::");
 	public final Rule SP_COL = terminal(":");
 	public final Rule SP_COMMA = terminal(",");
 	public final Rule SP_SEMI = terminal(";");
 	// assignment
 	public final Rule AS_INIT = terminal(":=");
 	public final Rule AS_EQUAL = terminal("=");
 	public final Rule AS_OPS = FirstOf(terminal("*="), terminal("/="),
 		terminal("%="), terminal("+="), terminal("-="));
 	// others
 	public final Rule QUOTES3 = terminal("\"\"\"");
 	public final Rule QUOTE = terminal("\"");
 	public final Rule TICK = terminal("`");
 	public final Rule SINGLE_Q = terminal("'");
 	public final Rule DOT = terminal(".").label(TokenName.DOT.name());
 	public final Rule AT = terminal("@").label(TokenName.AT.name());
 	public final Rule DSL_OPEN = terminal("<|").label(TokenName.DSL_OPEN.name());
 	public final Rule DSL_CLOSE = terminal("|>").label(TokenName.DSL_CLOSE.name());
 	public final Rule SQ_BRACKET_L = terminal("[").label(TokenName.SQ_BRACKET_L.name());
 	public final Rule SQ_BRACKET_R = terminal("]").label(TokenName.SQ_BRACKET_R.name());
 	public final Rule BRACKET_L = terminal("{").label(TokenName.BRACKET_L.name());
 	public final Rule BRACKET_R = terminal("}").label(TokenName.BRACKET_R.name());
 	public final Rule PAR_L = terminal("(").label(TokenName.PAR_L.name());
 	public final Rule PAR_R = terminal(")").label(TokenName.PAR_R.name());
 	// shortcut for Optional spacing
 	public final Rule OPT_SP = Optional(spacing());
 
 	// ----------- Custom action rules -----------------------------------------
 	/**
 	 * Custom action to find an end of statement
 	 * @return
 	 */
 	@SuppressSubnodes
 	public Rule eos()
 	{
 		return Sequence(OPT_SP, FirstOf(
 			SP_SEMI, // ;
 			LF(), // \n
 			Test(BRACKET_R), // '}' is end of statement too, but do NOT consume it !
 			Test(Eoi()))); // Eoi is end of statement too, but do NOT consume it !
 	}
 
 	/**Pass if not following some whitespace*/
 	public Rule noSpace()
 	{
 		return TestNot(ToRule(afterSpace()));
 	}
 
 	public boolean afterSpace()
 	{
 		//char c = getContext().getCurrentLocation().lookAhead(getContext().getInputBuffer(), -1);
 		int index = getContext().getCurrentLocation().getIndex();
 		char c = getContext().getInputBuffer().charAt(index-1);
 		return Character.isWhitespace(c);
 	}
 
 	public boolean setFieldInit(boolean val)
 	{
 		inFieldInit = val;
 		return true;
 	}
 	public boolean setNoSimpleMap(boolean val)
 	{
 		noSimpleMap = val;
 		return true;
 	}
 	public boolean setInEnum(boolean val)
 	{
 		inEnum = val;
 		return true;
 	}
 	/**
 	 * In Parboiled 0.9.5.0, enforcedSequence was removed, just use Sequence now.
 	 * Maybe do a replaceAll later.
 	 * @param rules
 	 * @return
 	 */
 	public Rule enforcedSequence(Object... rules)
 	{
 		return Sequence(rules);
 	}
 
 	// Debugging utils
 	/** System.out - eval to true*/
 	public boolean echo(String str)
 	{
 		System.out.println(str);
 		return true;
 	}
 	/** System.out - eval to false*/
 	public boolean echof(String str)
 	{
 		System.out.println(str);
 		return false;
 	}
 
 	/** System.out current node path - eval to true*/
 	public boolean printPath()
 	{
 		System.out.println(getContext().getPath());
 		return true;
 	}
 
 	public void cancel()
 	{
 		System.out.println("cancel called!");
 		//cancel=true;
 	}
 
 		// ============ Simulate a lexer ===========================================
 	// This should just create tokens for the items we want to highlight(color) in the IDE
 	// It should be able to deal with "Anything" and not ever fail if possible.
 	public Rule lexer()
 	{
 		// If Any changes made here, keep in sync with lexerTokens list in FantomParserTokens.java
 		return Sequence(
 				ZeroOrMore(lexerItem()).label("lexerItems"),
 				Optional(Eoi())); // until end of file
 	}
 
 	public Rule lexerItem()
 	{
 		return FirstOf(
 				comment(), unixLine(), doc(),
 				strs(), uri(), char_(), dsl(),
 				lexerInit(), lexerComps(), lexerAssign(), lexerOps(), lexerSeps(), // operators/separators
 				BRACKET_L, BRACKET_R, SQ_BRACKET_L, SQ_BRACKET_R, PAR_L, PAR_R, DOT, AT, DSL_CLOSE, DSL_OPEN, // other known items
 				keyword(), lexerId(), number(),
 				whiteSpace(),
 				// "Any" includes "everything else" - items withough highlighting.
 				// "Any" is also is a catchall for other unexpected items (should not happen)
 				Any().label(TokenName.UNEXPECTED.name()));
 	}
 
 	public Rule lexerId()
 	{
 		// same as ID but don't allow space/commennts
 		return Sequence(TestNot(keyword()),
 				Sequence(FirstOf(CharRange('A', 'Z'), CharRange('a', 'z'), "_"),
 				ZeroOrMore(FirstOf(CharRange('A', 'Z'), CharRange('a', 'z'), '_', CharRange('0', '9'))))).label(TokenName.LEXERID.name());
 	}
 
 	public Rule lexerOps()
 	{
 		return FirstOf(OP_2MINUS, OP_2PLUS, OP_AND, OP_ARROW, AS_INIT, OP_BANG, OP_CURRY, OP_DIV, OP_ELVIS,
 				OP_MINUS, OP_MODULO, OP_MULT, OP_OR, OP_PLUS, OP_POUND, OP_RANGE, OP_RANGE_EXCL, OP_SAFE_CALL,
 				OP_SAFE_DYN_CALL).label(TokenName.LEXEROPS.name());
 	}
 
 	public Rule lexerSeps()
 	{
 		return FirstOf(SP_COL, SP_COLCOL, SP_COMMA, SP_PIPE, SP_QMARK, SP_SEMI).label(TokenName.LEXERSEPS.name());
 	}
 
 	public Rule lexerComps()
 	{
 		return FirstOf(CP_COMPARATORS, CP_EQUALITY).label(TokenName.LEXERCOMPS.name());
 	}
 
 	public Rule lexerAssign()
 	{
 		return FirstOf(AS_EQUAL, AS_OPS).label(TokenName.LEXERASSIGN.name());
 	}
 
 	public Rule lexerInit()
 	{
 		return AS_INIT.label(TokenName.LEXERINIT.name());
 	}
 
 	/**
 	 * Helps with debugging parser, not for normal use
 	 * @return
 	 */
 	/*@Override
 	public Context<AstNode> getContext()
 	{
 		Context<AstNode> ctx = super.getContext();
 		int now=ctx.getCurrentLocation().getIndex();
 		int before=ctx.getCurrentLocation().getIndex()-10;
 		if(before<0)
 			before=0;
 		String t = ctx.getInputBuffer().extract(before, now);
 		System.out.println("get ctx: @"+now+" : "+t);
 		return ctx;
 	}*/
 
 	// for unit tsting
 	public Rule testExpr()
 	{
 		return Sequence(expr(), ast.newRootNode(AstKind.DUMMY_ROOT_NODE, parserTask));
 	}
 }
