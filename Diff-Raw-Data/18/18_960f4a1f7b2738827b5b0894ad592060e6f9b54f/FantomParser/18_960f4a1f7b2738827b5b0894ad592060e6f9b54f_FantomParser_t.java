 /*
  * Thibaut Colar Feb 5, 2010
  */
 package net.colar.netbeans.fan.parboiled;
 
 import org.parboiled.BaseParser;
 import org.parboiled.MatcherContext;
 import org.parboiled.Node;
 import org.parboiled.Rule;
 import org.parboiled.matchers.SequenceMatcher;
 import org.parboiled.matchers.TestMatcher;
 import org.parboiled.support.InputBuffer;
 import org.parboiled.support.Leaf;
 import org.parboiled.support.ParseTreeUtils;
 
 /**
  * Parboiled parser for the Fantom Language
  * Started with Fantom Grammar 1.0.50
  * Grammar spec:
  * http://fantom.org/doc/docLang/Grammar.html
  * @author thibautc
  */
 @SuppressWarnings(
 {
 	"InfiniteRecursion"
 })
 public class FantomParser extends BaseParser<Object>
 {
 	// ------------ Comp Unit --------------------------------------------------
 
 	//TODO: look for all specila code in fan.g (comments) and see what needs to be brought over
 	//TODO: funcType: // op_safe_dyn_call elxer confusion between |Str?->| (formal) and Str?->  (dyn call)
 	//TODO: typeCheckExpr: we compare to typeRoot instead of type, because type would eat the trailing ?, breaking ternary expressions
 	//TODO: itBlocks can have a COMMA after statements, which means to call it.add(expr).
 
 
 	public Rule compilationUnit()
 	{
 		return sequence(
 			OPT_SP,
 			zeroOrMore(firstOf(using(),incUsing())),
 			zeroOrMore(typeDef()),
 			OPT_SP,
 			zeroOrMore(doc()), // allow for extra docs at end of file (if last type commented out)
 			OPT_SP,
 			eoi());
 	}
 
 	public Rule using()
 	{
 		return enforcedSequence(
 			KW_USING,
 			optional(ffi()),
 			id(), 
 			zeroOrMore(enforcedSequence(DOT, id())),
 			optional(enforcedSequence(SP_COLCOL, id())),// not enforced to allow completion
 			optional(usingAs()),
 			eos());
 	}
 	
 	// Inconplete using - to allow completion
 	public Rule incUsing()
 	{
 		return enforcedSequence(
 			KW_USING,
 			optional(ffi()),
 			optional(id()), // Not optional, but we want a valid ast for completion if missing
 			zeroOrMore(sequence(DOT, id())),// not enforced to allow completion
 			optional(sequence(SP_COLCOL, id())),// not enforced to allow completion
 			optional(usingAs()),
 			optional(eos())); 		
 	}
 
 	public Rule ffi()
 	{
 		return enforcedSequence(SQ_BRACKET_L, id(), SQ_BRACKET_R);
 	}
 
 	public Rule usingAs()
 	{
 		return enforcedSequence(KW_AS, id());
 	}
 
 	// ------------- Type def --------------------------------------------------
 	public Rule typeDef()
 	{
 		// regrouped together classdef, enumDef, mixinDef & facetDef as they are grammatically very similar
 		return sequence(
 			OPT_SP,
 			optional(doc()),
 			zeroOrMore(facet()),
 			optional(protection()),
 			firstOf(
 				sequence(zeroOrMore(firstOf(KW_ABSTRACT, KW_FINAL, KW_CONST)),	KW_CLASS), // standard class
 				enforcedSequence(ENUM, KW_CLASS), // enum class
 				enforcedSequence(FACET, KW_CLASS), // facet class
 				KW_MIXIN // mixin
 			),
 			id(),
 			optional(inheritance()),
 			BRACKET_L,
 			optional(enumValDefs()), // only valid for enums, but simplifying
 			zeroOrMore(slotDef()),
 			BRACKET_R
 			);
 	}
 
 	public Rule protection()
 	{
 		return firstOf(KW_PUBLIC, KW_PROTECTED, KW_INTERNAL, KW_PRIVATE);
 	}
 
 	public Rule inheritance()
 	{
 		return enforcedSequence(SP_COL, typeList());
 	}
 	
 	// ------------- Facets ----------------------------------------------------
 	public Rule facet()
 	{
 		return enforcedSequence(AT, simpleType(), optional(facetVals()));
 	}
 
 	public Rule facetVals()
 	{
 		return enforcedSequence(
 			BRACKET_L,
 			facetVal(),
 			zeroOrMore(sequence(eos(), facetVal())),
 			BRACKET_R);
 	}
 
 	public Rule facetVal()
 	{
 		return enforcedSequence(id(), AS_EQUAL, expr());
 	}
 
 	//------------------- Slot Def ---------------------------------------------
 	public Rule enumValDefs()
 	{
 		return sequence(enumValDef(), zeroOrMore(sequence(SP_COMMA, enumValDef())), eos());
 	}
 
 	public Rule enumValDef()
 	{
 		return sequence(id(), optional(enforcedSequence(PAR_L, args() ,PAR_R)));
 	}
 
 	public Rule slotDef()
 	{
 		return firstOf(ctorDef(), methodDef(), fieldDef());
 	}
 
 	public Rule fieldDef()
 	{
 			return sequence(
 					zeroOrMore(facet()),
 					optional(protection()),
 					zeroOrMore(firstOf(KW_ABSTRACT, KW_CONST, KW_FINAL, KW_STATIC,
 						KW_NATIVE, KW_OVERRIDE, KW_READONLY, KW_VIRTUAL)),
 					typeAndOrId(),
 					optional(sequence(OP_ASSIGN, expr())),
 					optional(fieldAccessor()),
 					eos()
 					);
 	}
 
 	public Rule methodDef()
 	{
 			return sequence(
 					zeroOrMore(facet()),
 					optional(protection()),
 					zeroOrMore(firstOf(KW_ABSTRACT, KW_NATIVE, KW_ONCE, KW_STATIC,
 						KW_OVERRIDE, KW_VIRTUAL)),
 					type(),
 					id(),
 					PAR_L,
 					optional(params()),
 					PAR_R,
 					methodBody());
 	}
 
 	public Rule ctorDef()
 	{
 			return sequence(
 					zeroOrMore(facet()),
 					optional(protection()),
 					type(),
 					KW_NEW,
 					id(),
 					optional(
 						firstOf(	// ctor chain
 						enforcedSequence(KW_THIS, DOT, id(), PAR_L, args(), PAR_R),
 						enforcedSequence(KW_SUPER, optional(enforcedSequence(DOT, id())), PAR_L, args(), PAR_R)
 						)
 					),
 					PAR_L,
 					optional(params()),
 					PAR_R,
 					methodBody());
 	}
 
 	public Rule methodBody()
 	{
 		return	firstOf(
 				 enforcedSequence(BRACKET_L, zeroOrMore(stmt()), BRACKET_R),
 				 eos()); // method with no body
 	}
 
 	public Rule params()
 	{
 		return sequence(param(), zeroOrMore(enforcedSequence(SP_COMMA, params())));
 	}
 
 	public Rule param()
 	{
 		return sequence(type(), id(), optional(enforcedSequence(OP_ASSIGN, expr())));
 	}
 
 	public Rule fieldAccessor()
 	{
 		return enforcedSequence(
 				BRACKET_L,
 				optional(sequence("get", firstOf(eos(), block()))),
 				optional(sequence(optional(protection()),"set", firstOf(eos(), block()))),
 				BRACKET_R
 				);
 	}
 
 	public Rule args()
 	{
 		return sequence(expr(), zeroOrMore(enforcedSequence(SP_COMMA, expr())));
 	}
 
 	// ------------ Statements -------------------------------------------------
 	public Rule block()
 	{
 		return firstOf(
 				itBlock(), // it block is normal block
 				stmt() // singlr statement
 			);
 	}
 
 	public Rule stmt()
 	{
 		return sequence(OPT_SP,
 				firstOf(sequence(KW_BREAK, eos()), sequence(KW_CONTINUE, eos()), for_(),
 						if_(), sequence(KW_RETURN, optional(expr()), eos()), switch_(),
 						sequence(KW_THROW, expr(), eos()), while_(), try_(),
 						localDef(), itAdd(), sequence(expr(), eos())),
 						OPT_SP);
 	}
 
 	public Rule for_()
 	{
 		return enforcedSequence(KW_FOR, PAR_L,
 				optional(firstOf(localDef(), expr())),
 				SP_SEMI, optional(expr()), SP_SEMI, optional(expr()), PAR_R,
 				block());
 	}
 
 	public Rule if_()
 	{
 		return enforcedSequence(KW_IF, PAR_L, expr(), PAR_R, block(),
 			optional(enforcedSequence(KW_ELSE, block())));
 	}
 
 	public Rule while_()
 	{
 		return enforcedSequence(KW_WHILE, PAR_L, expr(), PAR_R, block());
 	}
 
 	public Rule localDef()
 	{
 		return sequence(typeAndOrId(),
 						optional(enforcedSequence(OP_ASSIGN, expr())),
 						eos());
 	}
 
 	public Rule itAdd()
 	{
 		return sequence(expr(), zeroOrMore(sequence(SP_COMMA, expr())), eos());
 	}
 
 	public Rule try_()
 	{
 		return enforcedSequence(KW_TRY, block(), zeroOrMore(catch_()),
 			optional(sequence(KW_FINALLY, block())));
 	}
 
 	public Rule catch_()
 	{
 		return enforcedSequence(KW_CATCH, PAR_L, type(), id(), PAR_R, block());
 	}
 
 	public Rule switch_()
 	{
 		return enforcedSequence(KW_SWITCH, PAR_L, expr(), PAR_R,
 			BRACKET_L,
 			zeroOrMore(enforcedSequence(KW_CASE, expr(), SP_COL, zeroOrMore(stmt()))),
 			optional(enforcedSequence(KW_DEFAULT, SP_COL, zeroOrMore(stmt()))),
 			BRACKET_R);
 	}
 
 	// ----------- Expressions -------------------------------------------------
 	public Rule expr()
 	{
 		return assignExpr();
 	}
 
 	public Rule assignExpr()
 	{
 		return sequence(ifExpr(), optional(enforcedSequence(firstOf(AS_EQUAL, AS_ASSIGN_OPS), assignExpr())));
 	}
 
 	public Rule ifExpr()
 	{
 		return firstOf(elvisExpr(), ternaryExpr());
 	}
 
 	public Rule ternaryExpr()
 	{
 		return sequence(condOrExpr(),
 			optional(enforcedSequence(SP_QMARK, ifExprBody(), SP_COL, ifExprBody())));
 	}
 
 	public Rule elvisExpr()
 	{
 		return sequence(condOrExpr(), OP_ELVIS, ifExprBody());
 	}
 
 	public Rule ifExprBody()
 	{
 		return firstOf(condOrExpr(), enforcedSequence(KW_THROW, expr()));
 	}
 
 	public Rule condOrExpr()
 	{
 		return sequence(condAndExpr(), zeroOrMore(enforcedSequence(OP_OR, condAndExpr())));
 	}
 
 	public Rule condAndExpr()
 	{
 		return sequence(equalityExpr(), zeroOrMore(enforcedSequence(OP_AND, equalityExpr())));
 	}
 
 	public Rule equalityExpr()
 	{
 		return sequence(relationalExpr(), zeroOrMore(enforcedSequence(CP_EQUALITY, relationalExpr())));
 	}
 
 	public Rule relationalExpr()
 	{
 		return firstOf(typeCheckExpr(), compareExpr());
 	}
 
 	public Rule typeCheckExpr()
 	{
 		return sequence(rangeExpr(),
 			optional(enforcedSequence(firstOf(KW_IS, KW_ISNOT, KW_AS),type())));
 	}
 
 	public Rule compareExpr()
 	{
 		return sequence(rangeExpr(), zeroOrMore(enforcedSequence(CP_COMPARATORS, rangeExpr())));
 	}
 
 	public Rule rangeExpr()
 	{
 		return sequence(addExpr(), 
 			zeroOrMore(enforcedSequence(firstOf(OP_RANGE_EXCL, OP_RANGE), addExpr())));
 	}
 
 	public Rule addExpr()
 	{
 		return sequence(multExpr(),
 			zeroOrMore(enforcedSequence(firstOf(OP_PLUS, OP_MINUS), multExpr())));
 	}
 
 	public Rule multExpr()
 	{
 		return sequence(parExpr(),
 			zeroOrMore(enforcedSequence(firstOf(OP_MULT, OP_DIV), parExpr())));
 	}
 
 	public Rule parExpr()
 	{
 		return firstOf(castExpr(), groupedExpr(), unaryExpr());
 	}
 
 	public Rule castExpr()
 	{
 		return sequence(noNl(), PAR_L, type(), PAR_R, noNl(), parExpr());
 	}
 
 	public Rule groupedExpr()
 	{
 		return sequence(PAR_L, expr(), PAR_R, zeroOrMore(termChain()));
 	}
 
 	public Rule unaryExpr()
 	{
 		return firstOf(prefixExpr(), postfixExpr(), termExpr());
 	}
 
 	public Rule prefixExpr()
 	{
 		return sequence(
 				firstOf(OP_CURRY, OP_BANG, OP_2PLUS, OP_2MINUS, OP_PLUS, OP_MINUS),
 				parExpr());
 	}
 
 	public Rule postfixExpr()
 	{
 		return sequence(termExpr(), firstOf(OP_2MINUS, OP_2PLUS));
 	}
 
 	public Rule termExpr()
 	{
 		return sequence(termBase(), zeroOrMore(termChain()));
 	}
 
 	public Rule termBase()
 	{
 		// check for ID alone last (and not as part of idExpr) otherwise it would never check literal & typebase !
 		return firstOf(idExprReq(), litteral(), typeBase(), id());
 	}
 
 	public Rule typeBase()
 	{
 		return firstOf(typeLitteral(), slotLitteral(), namedSuper(), staticCall(),
 						dsl(), closure(), simple(), ctorBlock());
 	}
 
 	public Rule typeLitteral()
 	{
 		return sequence(type(), noNl(), OP_POUND);
 	}
 
 	public Rule slotLitteral()
 	{
 		return sequence(optional(type()), noNl(), OP_POUND, noNl(), id());
 	}
 
 	public Rule namedSuper()
 	{
 		return sequence(type(), DOT, KW_SUPER);
 	}
 
 	public Rule staticCall()
 	{
 		return sequence(type(), DOT, idExpr());
 	}
 
 	public Rule dsl()
 	{
 		//TODO: unclosed DSL ?
 		return sequence(simpleType(),
 			sequence(DSL_OPEN, zeroOrMore(sequence(testNot(DSL_CLOSE), any())), DSL_OPEN));
 	}
 
 	public Rule closure()
 	{
 		return sequence(funcType(), BRACKET_L, zeroOrMore(stmt()), BRACKET_R);
 	}
 
 	public Rule simple()
 	{
 		return sequence(type(), PAR_L, expr(), PAR_R);
 	}
 
 	public Rule ctorBlock()
 	{
 		return sequence(type(), noNl(), itBlock());
 	}
 
 	public Rule itBlock()
 	{
 		return enforcedSequence(BRACKET_L, zeroOrMore(stmt()), BRACKET_R);
 	}
 
 	public Rule termChain()
 	{
 		return firstOf(safeDotCall(), safeDynCall(), dotCall(), dynCall(), indexExpr(),
 						callOp(), itBlock(), incDotCall());
 	}
 
 	public Rule dotCall()
 	{
 		return enforcedSequence(DOT, idExpr());
 	}
 
 	public Rule dynCall()
 	{
 		return enforcedSequence(OP_ARROW, idExpr());
 	}
 
 	public Rule safeDotCall()
 	{
 		return enforcedSequence(OP_SAFE_CALL, idExpr());
 	}
 
 	public Rule safeDynCall()
 	{
 		return enforcedSequence(OP_SAFE_DYN_CALL, idExpr());
 	}
 
 	// incomplete dot call, make valid to allow for completion
 	public Rule incDotCall()
 	{
 		return DOT; 
 	}
 
 	public Rule idExpr()
 	{
 		// this can be either a local def(toto.value) or a call(toto.getValue or toto.getValue(<params>)) + opt. closure
 		return firstOf(idExprReq(), id());
 	}
 
 	public Rule idExprReq()
 	{
 		// Same but without matching ID by itself (this would prevent termbase from checking literals).
 		return firstOf(field(), call());
 	}
 
 	// require '*' otherwise it's just and ID (this would prevent termbase from checking literals)
 	public Rule field()
 	{
 		return sequence(OP_MULT,id());
 	}
 
 	// require params or/and closure, otherwise it's just and ID (this would prevent termbase from checking literals)
 	public Rule call()
 	{
 		return sequence(id(),
 						enforcedSequence(PAR_L, args(), PAR_R), // params
 						optional(closure()));
 	}
 
 	/*
 	 *  TODO: does not always  work, will be parsed as a "list" Type because the grammar is confusing
 	 *	it's context sensitive - need to review.
 	 *  Can't cleanly differentiate Int[3] (List of Int with val '3') and var[3] : int.get[3]
      *  So for now I'm gonna get a List in the ast and deal with it there
      *  indexExpr 	:	({notAfterEol()}? sq_bracketL expr sq_bracketR)
 	 * 	-> ^(AST_INDEX_EXPR sq_bracketL expr sq_bracketR);
 	 */
 	public Rule indexExpr()
 	{
 		return sequence(noNl(), SQ_BRACKET_L, expr(), SQ_BRACKET_R);
 	}
 
 	public Rule callOp()
 	{
 		return enforcedSequence(PAR_L, args(), PAR_R, optional(closure()));
 	}
 
 	public Rule litteral()
 	{
 		return firstOf(KW_NULL, KW_THIS, KW_SUPER, KW_IT, KW_TRUE, KW_FALSE,
 						strs(), uri(), number(), char_(), namedSuper(),
 						list(), map());
 	}
 
 	public Rule list()
 	{
 		return sequence(
 			optional(sequence(type(), noNl())),
 			SQ_BRACKET_L, listItems(), SQ_BRACKET_R
 			);
 	}
 
 	public Rule listItems()
 	{
 		return firstOf(
 			SP_COMMA,
 			sequence(expr(), zeroOrMore(enforcedSequence(SP_COMMA, expr())))
 			);
 	}
 
 	public Rule map()
 	{
 		return sequence(optional(mapType()), noNl(), SQ_BRACKET_L, mapItems(), SQ_BRACKET_R);
 	}
 
 	public Rule mapItems()
 	{
 		return firstOf(SP_COL,
 			sequence(mapPair(), zeroOrMore(sequence(SP_COMMA, mapPair())))
 			);
 	}
 
 	public Rule mapPair()
 	{
 		return sequence(expr(), SP_COL, expr());
 	}
 
 	// ------------ Litteral items ---------------------------------------------
 	// Todo: review if allows things like : "aa\"bb"
 	public Rule strs()
 	{
 		return firstOf(
 			sequence(QUOTE, zeroOrMore(sequence(testNot(QUOTE), any())), QUOTE),
 			sequence(QUOTES3, zeroOrMore(sequence(testNot(QUOTES3), any())), QUOTES3)
 			);
 	}
 
 	public Rule uri()
 	{
 		return sequence(TICK, zeroOrMore(sequence(testNot(TICK), any())), TICK);
 	}
 
 	public Rule char_()
 	{
 		return firstOf(
 			enforcedSequence(SINGLE_Q, any(), SINGLE_Q),
 			enforcedSequence("\\u", hexDigit(), hexDigit(), hexDigit(), hexDigit())
 			);
 	}
 
 	public Rule hexDigit()
 	{
 		return firstOf(charRange('a', 'f'),
 						charRange('A','F'),
 						digit()
 						);
 	}
 
 	public Rule digit()
 	{
 		return charRange('0','9');
 	}
 
 	public Rule number()
 	{
 		return sequence(
 			optional(OP_MINUS),
 			firstOf(
 				// hex number
 				sequence(firstOf("0x","0X"),
 						oneOrMore(firstOf("_",hexDigit()))),
 				// decimal
 				sequence(digit(),
 						zeroOrMore(sequence(zeroOrMore("_"), digit())),
 						optional(fraction()),
 						optional(exponent())),
 				// fractional
 				sequence(fraction(), optional(exponent()), optional(nbType()))
 			));
 	}
 
 	public Rule fraction()
 	{
 		return sequence(DOT, digit(), zeroOrMore(sequence(zeroOrMore("_"), digit())));
 	}
 
 	public Rule exponent()
 	{
 		return enforcedSequence(charSet("eE"),
 				optional(firstOf(OP_PLUS, OP_MINUS)),
 				digit(),
 				zeroOrMore(sequence(zeroOrMore("_"), digit()))
 			);
 	}
 
 	public Rule nbType()
 	{
 		return firstOf(
 				"day", "hr", "min", "sec", "ms", "ns", //durations
 				"f", "F", "D", "d" // float / decimal
 			);
 	}
 
 	// ------------ Type -------------------------------------------------------
 	// Types use noNl() a lot to avoid gramar confusion
 	public Rule type()
 	{
 		return firstOf(simpleMapType(), otherTypes());
 	}
 
 	public Rule simpleMapType()
 	{
 		// It has to be other types, otherwise it's left recursive (loops forever)
 		return sequence(otherTypes(), noNl(), SP_COL, noNl(), otherTypes(),
 		optional(
 			sequence(noNl(), optional(SP_QMARK), noNl(), enforcedSequence(
 			SQ_BRACKET_L, SQ_BRACKET_R))), // list of '?[]'
 			optional(sequence(noNl(), SP_QMARK))); // nullable
 	}
 
 	public Rule otherTypes()
 	{
 		return sequence(
 			firstOf(funcType(), mapType(), simpleType()),
 			optional(
 			sequence(noNl(),optional(SP_QMARK), noNl(), enforcedSequence(
 			SQ_BRACKET_L, SQ_BRACKET_R))), // list of '?[]'
 			optional(sequence(noNl(), SP_QMARK))); // nullable
 	}
 
 	public Rule simpleType()
 	{
 		return sequence(
 			id(),
 			optional(sequence(noNl(), enforcedSequence(SP_COLCOL, noNl(), id()))));
 	}
 
 	public Rule mapType()
 	{
 		// We use otherTypes here as well, because [Str:Int:Str] would be confusing too
 		return enforcedSequence(SQ_BRACKET_L, noNl(), otherTypes(), noNl(), SP_COL, noNl(), otherTypes(), noNl(), SQ_BRACKET_R);
 	}
 
 	public Rule funcType()
 	{
 		return enforcedSequence(SP_PIPE, noNl(), optional(formals()), noNl(), OP_ARROW, noNl(), optional(type()), noNl(), SP_PIPE);
 	}
 
 	public Rule formals()
 	{
 		return sequence(typeAndOrId(), zeroOrMore(enforcedSequence(noNl(), SP_COMMA, typeAndOrId())));
 	}
 
 	public Rule typeList()
 	{
 		return sequence(type(), zeroOrMore(sequence(noNl(), SP_COMMA, type())));
 	}
 
 	public Rule typeAndOrId()
 	{
 		// Note it can be "type id", "type" or "id"
 		// but parser can't know if it's "type" or "id" so always recognize as type
 		// so would never actually hit id()
 		return firstOf(sequence(type(), id()), type()/*, id()*/);
 	}
 	// ------------ Misc -------------------------------------------------------	
 	public Rule id()
 	{
 		return sequence(
 			firstOf(charRange('A', 'Z'), charRange('a', 'z'), "_"),
 			zeroOrMore(firstOf(charRange('A', 'Z'), charRange('a', 'z'), "_", charRange('0', '9'))),
 			OPT_SP);
 	}
 
 	public Rule doc()
 	{
 		return oneOrMore(sequence("**", zeroOrMore(sequence(testNot("\n"), any())), "\n"));
 	}
 
 	@Leaf
 	public Rule spacing()
 	{
 		return oneOrMore(firstOf(
 			// whitespace
 			oneOrMore(charSet(" \t\r\n\u000c")),
 			// multiline comment
 			sequence("/*", zeroOrMore(sequence(testNot("*/"), any())), "*/"), // normal comment
 			// if incomplete multiline comment, then end at end of line
 			sequence("/*", zeroOrMore(sequence(testNot("\n"), any())), "\n"),
 			// single line comment
 			sequence("//", zeroOrMore(sequence(testNot(charSet("\r\n")), any())), charSet("\r\n"))));
 	}
 
 	@Leaf
 	public Rule terminal(String string)
 	{
 		return sequence(string, optional(spacing())).label(string);
 	}
 
 	@Leaf
 	public Rule terminal(String string, Rule mustNotFollow)
 	{
 		return sequence(string, testNot(mustNotFollow), optional(spacing())).label(string);
 	}
 	// -------------- Terminal items -------------------------------------------
 	// -- Keywords --
 	public final Rule KW_USING = terminal("using");
 	public final Rule KW_ABSTRACT = terminal("abstract");
 	public final Rule KW_AS = terminal("as");
 	public final Rule KW_ASSERT = terminal("assert"); // not a grammar kw
 	public final Rule KW_BREAK = terminal("break");
 	public final Rule KW_CATCH = terminal("catch");
 	public final Rule KW_CASE = terminal("case");
 	public final Rule KW_CLASS = terminal("class");
 	public final Rule KW_CONST = terminal("const");
 	public final Rule KW_CONTINUE = terminal("continue");
 	public final Rule KW_DEFAULT = terminal("default");
 	public final Rule KW_DO = terminal("do"); // unused, reserved
 	public final Rule KW_ELSE = terminal("else");
 	public final Rule KW_FALSE = terminal("false");
 	public final Rule KW_FINAL = terminal("final");
 	public final Rule KW_FINALLY = terminal("finally");
 	public final Rule KW_FOR = terminal("for");
 	public final Rule KW_FOREACH = terminal("foreach"); // unused, reserved
 	public final Rule KW_IF = terminal("if");
 	public final Rule KW_INTERNAL = terminal("internal");
 	public final Rule KW_IS = terminal("is");
 	public final Rule KW_IT = terminal("ii");
 	public final Rule KW_ISNOT = terminal("isnot");
 	public final Rule KW_MIXIN = terminal("mixin");
 	public final Rule KW_NATIVE = terminal("native");
 	public final Rule KW_NEW = terminal("new");
 	public final Rule KW_NULL = terminal("null");
 	public final Rule KW_ONCE = terminal("once");
 	public final Rule KW_OVERRIDE = terminal("override");
 	public final Rule KW_PRIVATE = terminal("private");
 	public final Rule KW_PUBLIC = terminal("public");
 	public final Rule KW_PROTECTED = terminal("protected");
 	public final Rule KW_READONLY = terminal("readonly");
 	public final Rule KW_RETURN = terminal("return");
 	public final Rule KW_STATIC = terminal("static");
 	public final Rule KW_SUPER = terminal("super");
 	public final Rule KW_SWITCH = terminal("switch");
 	public final Rule KW_THIS = terminal("this");
 	public final Rule KW_THROW = terminal("throw");
 	public final Rule KW_TRUE = terminal("true");
 	public final Rule KW_TRY = terminal("tty");
 	public final Rule KW_VIRTUAL = terminal("virtual");
 	public final Rule KW_VOID = terminal("void"); // unused, reserved
 	public final Rule KW_VOLATILE = terminal("volatile"); // unused, reserved
 	public final Rule KW_WHILE = terminal("while");
 	// Non keyword meningful items
 	public final Rule ENUM = terminal("enum");
 	public final Rule FACET = terminal("facet");
 	// operators
 	public final Rule OP_SAFE_CALL = terminal("?.");
 	public final Rule OP_SAFE_DYN_CALL = terminal("?->");
 	public final Rule OP_ARROW = terminal("->");
 	public final Rule OP_ASSIGN = terminal(":=");
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
 	public final Rule OP_POUND = terminal("#");
 	// comparators
 	public final Rule CP_EQUALITY = firstOf(terminal("==="), terminal("!=="),
 							terminal("=="), terminal("!="));
 	public final Rule CP_COMPARATORS = firstOf(terminal("<="), terminal(">="),
 							terminal("<=>"), terminal("<"), terminal(">"));
 	// separators
 	public final Rule SP_PIPE = terminal("|");
 	public final Rule SP_QMARK = terminal("?");
 	public final Rule SP_COLCOL = terminal("::");
 	public final Rule SP_COL = terminal(":");
 	public final Rule SP_COMMA = terminal(",");
 	public final Rule SP_SEMI = terminal(";");
 	// assignment
 	public final Rule AS_EQUAL = terminal("=");
 	public final Rule AS_ASSIGN_OPS = firstOf(terminal("*="),terminal("/="),
 							terminal("%="),terminal("+="),terminal("-="));
 	// others
 	public final Rule QUOTES3 = terminal("\"\"\"");
 	public final Rule QUOTE = terminal("\"");
 	public final Rule TICK = terminal("`");
 	public final Rule SINGLE_Q = terminal("'");
 	public final Rule DOT = terminal(".");
 	public final Rule AT = terminal("@");
 	public final Rule DSL_OPEN = terminal("<|");
 	public final Rule DSL_CLOSE = terminal("|>");
 	public final Rule SQ_BRACKET_L = terminal("[");
 	public final Rule SQ_BRACKET_R = terminal("]");
 	public final Rule BRACKET_L = terminal("{");
 	public final Rule BRACKET_R = terminal("}");
 	public final Rule PAR_L = terminal("(");
 	public final Rule PAR_R = terminal(")");
 	// shortcut for optional spacing
 	public final Rule OPT_SP = optional(spacing()); 
 
 	// ----------- Custom action rules -----------------------------------------
 
 	/**
 	 * Look for end of statement
 	 * @return
 	 */
 	public boolean eos()
 	{
 		// First check if we just passed a '\n' (ignored spacing)
 		if(isAfterNL())
 			return true;
 		// Otherwise look for upcoming statement ending chars: ';' '\n' or '}'
		SequenceMatcher seq = (SequenceMatcher)sequence(OPT_SP,"");
 		if(seq.match((MatcherContext)getContext())) return true;
		// '}' is eos too, but we should not consume it, so using a test
 		SequenceMatcher test = (SequenceMatcher)sequence(OPT_SP, test("}"));
 		if(test.match((MatcherContext)getContext())) return true;
 		return false;
 	}
 
 	/**
 	 * Checks if we just passed a '\n' (ignored spacing)
 	 * Usually we ignore newline (meaningless to grammar), but sometimes they
 	 * do are mening ful ex:
 	 * Str[Int]  != Str\n[Int]
 	 * @return
 	 */
 	public boolean isAfterNL()
 	{
 		if(getContext()==null)
 			return false;
 		InputBuffer buffer = getContext().getInputBuffer();
 		Node nen = ParboiledUtils.getLastNonEmptyNode(getContext(), buffer);
 		if(nen!=null)
 		{
 			String text = ParseTreeUtils.getNodeText(nen, buffer);
 			if(nen.getLabel().equals("spacing") && text.indexOf("\n")!=-1)
 				return true; // we just passed a line break
 		}
 		return false;
 	}
 
 	// shortcut for ! isAfterNL();
 	public boolean noNl()
 	{
 		return ! isAfterNL();
 	}
 }
