 /*
  * Thibaut Colar Feb 5, 2010
  */
 package net.colar.netbeans.fan.parboiled;
 
 import org.parboiled.BaseParser;
 import org.parboiled.Rule;
 import org.parboiled.support.Cached;
 import org.parboiled.support.Leaf;
 
 /**
  * Parboiled parser for the Fantom Language
  * Started with Fantom Grammar 1.0.50
  * Grammar spec:
  * http://fantom.org/doc/docLang/Grammar.html
  *
  * Test Suite: net.colar.netbeans.fan.test.FantomParserTest
  *
  * @author thibautc
  */
 @SuppressWarnings(
 {
 	"InfiniteRecursion"
 })
 public class FantomParser extends BaseParser<Object>
 {
 
 	public boolean inFieldInit = false; // to help with differentiation of field accesor & itBlock
 	public boolean noSimpleMap = false; // to disallow ambigous simppeMaps in certain situations (within another map, ternaryExpr)
 
 	// ------------ Comp Unit --------------------------------------------------
 	public Rule compilationUnit()
 	{
 		return sequence(
 			OPT_LF(),
 			// Missing from grammar: Optional unix env line
 			optional(sequence("#!", zeroOrMore(sequence(testNot("\n"), any())), "\n")),
 			zeroOrMore(firstOf(using(), incUsing())),
 			zeroOrMore(typeDef()),
 			OPT_LF(),
 			zeroOrMore(doc()), // allow for extra docs at end of file (if last type commented out)
 			OPT_LF(),
 			eoi());
 	}
 
 	public Rule using()
 	{
 		return sequence(OPT_LF(), enforcedSequence(
 			KW_USING,
 			optional(ffi()),
 			id(),
 			zeroOrMore(enforcedSequence(DOT, id())),
 			optional(enforcedSequence(SP_COLCOL, id())),// not enforced to allow completion
 			optional(usingAs()),
 			eos()), OPT_LF());
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
 			optional(eos()), OPT_LF());
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
 		// grouped together classdef, enumDef, mixinDef & facetDef as they are grammatically very similar (optimized)
 		return sequence(
 			OPT_LF(),
 			optional(doc()),
 			zeroOrMore(facet()),
 			optional(protection()),
 			enforcedSequence(
 			firstOf(
 			sequence(zeroOrMore(firstOf(KW_ABSTRACT, KW_FINAL, KW_CONST)), KW_CLASS), // standard class
 			enforcedSequence(ENUM, KW_CLASS), // enum class
 			enforcedSequence(FACET, KW_CLASS), // facet class
 			KW_MIXIN // mixin
 			),
 			id(),
 			optional(inheritance()),
 			OPT_LF(),
 			BRACKET_L,
 			OPT_LF(),
 			//TODO: conflicts with slotDef : optional(enumValDefs()), // only valid for enums, but simplifying
 			zeroOrMore(slotDef()),
 			BRACKET_R, OPT_LF()));
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
 		return enforcedSequence(AT, simpleType(), optional(facetVals()), OPT_LF());
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
 		return sequence(id(), optional(enforcedSequence(PAR_L, optional(args()), PAR_R)), OPT_LF());
 	}
 
 	public Rule slotDef()
 	{
 		return sequence(
 			OPT_LF(),
 			optional(doc()),// common to all slots
 			zeroOrMore(facet()),// common to all slots
 			optional(protection()),// common to all slots
 			firstOf(
 				ctorDef(), // look for 'new'
 				methodDef(), // look for params : '('
 				fieldDef()), // others
 			OPT_LF());
 	}
 
 	public Rule fieldDef()
 	{
 		return sequence(
 			zeroOrMore(firstOf(KW_ABSTRACT, KW_CONST, KW_FINAL, KW_STATIC,
 			KW_NATIVE, KW_OVERRIDE, KW_READONLY, KW_VIRTUAL)),
 			/*typeAndOrId(),*/ type(), id(), // Type required for fields(no infered) (Grammar does not say so)
 			//TODO: when there is an OP_ASSIGN AND a fieldAccesor, parser takes forever !!
 			// probably confused with an itBlock
 			setFieldInit(true),
 			optional(enforcedSequence(OP_ASSIGN, OPT_LF(), expr())),
 			optional(fieldAccessor()),
 			setFieldInit(false),
 			eos());
 	}
 
 	public Rule methodDef()
 	{
 		return enforcedSequence(
 				sequence(
 				zeroOrMore(firstOf(KW_ABSTRACT, KW_NATIVE, KW_ONCE, KW_STATIC,
 				KW_OVERRIDE, KW_VIRTUAL)),
 				type(),
 				id(),
 				PAR_L),
 			optional(params()),
 			PAR_R,
 			methodBody());
 	}
 
 	public Rule ctorDef()
 	{
 		return enforcedSequence(KW_NEW,
 			id(),
 			PAR_L, 
 			optional(params()),
 			PAR_R,
 			optional( // ctorChain
 			// Fantom  Grammar page is missing SP_COL
 			enforcedSequence(sequence(OPT_LF(), SP_COL),
 			firstOf(
 			enforcedSequence(KW_THIS, DOT, id(), enforcedSequence(PAR_L, optional(args()), PAR_R)),
 			enforcedSequence(KW_SUPER, optional(enforcedSequence(DOT, id())), enforcedSequence(PAR_L, optional(args()), PAR_R))))),
 			methodBody());
 	}
 
 	public Rule methodBody()
 	{
 		return sequence(firstOf(
 			enforcedSequence(sequence(OPT_LF(), BRACKET_L), zeroOrMore(stmt()), OPT_LF(), BRACKET_R),
 			eos()), OPT_LF()); // method with no body
 	}
 
 	public Rule params()
 	{
 		return sequence(param(), zeroOrMore(enforcedSequence(SP_COMMA, params())));
 	}
 
 	public Rule param()
 	{
 		return sequence(OPT_LF(), type(), id(), optional(enforcedSequence(OP_ASSIGN, expr())), OPT_LF());
 	}
 
 	public Rule fieldAccessor()
 	{
 		return sequence(OPT_LF(),
 			enforcedSequence(
 			BRACKET_L,
 			optional(sequence(OPT_LF(), enforcedSequence(GET, firstOf(block(), eos())))),
 			optional(sequence(OPT_LF(), optional(protection()), enforcedSequence(SET, firstOf(block(), eos())))),
 			BRACKET_R), OPT_LF());
 	}
 
 	public Rule args()
 	{
 		return sequence(expr(), zeroOrMore(sequence(OPT_LF(), enforcedSequence(SP_COMMA, OPT_LF(), expr()))), OPT_LF());
 	}
 
 	// ------------ Statements -------------------------------------------------
 	public Rule block()
 	{
 		return sequence(OPT_LF(), firstOf(
 			enforcedSequence(BRACKET_L, zeroOrMore(stmt()), OPT_LF(), BRACKET_R), // block
 			stmt() // single statement
 			), OPT_LF());
 	}
 
 	public Rule stmt()
 	{
 		return sequence(testNot(BRACKET_R), OPT_LF(),
 			firstOf(
 			sequence(KW_BREAK, eos()),
 			sequence(KW_CONTINUE, eos()),
 			sequence(KW_RETURN, sequence(optional(expr()), eos())).label("MY_RT_STMT"),
 			sequence(KW_THROW, expr(), eos()),
 			if_(),
 			for_(),
 			switch_(),
 			while_(),
 			try_(),
 			// local var definition (:=)
 			localDef(),
 			// otherwise expression (optional Comma for itAdd expression)
 			// do firstOf, because "," in this case can be considered an end of statement
 			sequence(expr(), firstOf(sequence(SP_COMMA, optional(eos())), eos()))),
 			OPT_LF());
 	}
 
 	public Rule for_()
 	{
 		return enforcedSequence(KW_FOR, PAR_L,
 			optional(firstOf(localDef(), sequence(condOrExpr(), SP_SEMI)/*was expr*/)),
 			optional(expr()), SP_SEMI, optional(expr()), PAR_R,
 			block());
 	}
 
 	public Rule if_()
 	{
 		return enforcedSequence(KW_IF, PAR_L, condOrExpr()/*was expr*/, PAR_R, block(),
 			optional(enforcedSequence(KW_ELSE, block())));
 	}
 
 	public Rule while_()
 	{
 		return enforcedSequence(KW_WHILE, PAR_L, condOrExpr()/*was expr*/, PAR_R, block());
 	}
 
 	public Rule localDef()
 	{
 		// this is changed to matched either:
 		// 'Int j', 'j:=27', 'Int j:=27'
 		return sequence(
 			firstOf(
 			// fan parser says if it's start with "id :=" or "Type, id", then it gotta be a localDef (enforce)
 			enforcedSequence(sequence(type(), id(), OP_ASSIGN), OPT_LF(), expr()),
 			// same if it starts with "id :="
 			enforcedSequence(sequence(id(), OP_ASSIGN), OPT_LF(), expr()),
 			// var def with no value
 			sequence(type(), id())),
 			eos());
 	}
 
 	public Rule try_()
 	{
 		return enforcedSequence(KW_TRY, block(), zeroOrMore(catch_()),
 			optional(sequence(KW_FINALLY, block())));
 	}
 
 	public Rule catch_()
 	{
 		return enforcedSequence(KW_CATCH, optional(enforcedSequence(PAR_L, type(), id(), PAR_R)), block());
 	}
 
 	public Rule switch_()
 	{
 		return enforcedSequence(KW_SWITCH, PAR_L, expr(), PAR_R,
 			OPT_LF(), BRACKET_L, OPT_LF(),
 			zeroOrMore(enforcedSequence(KW_CASE, expr(), SP_COL, zeroOrMore(firstOf(stmt(), LF())))),
 			optional(enforcedSequence(KW_DEFAULT, SP_COL, zeroOrMore(firstOf(stmt(), LF())))),
 			OPT_LF(), BRACKET_R);
 	}
 
 	// ----------- Expressions -------------------------------------------------
 	public Rule expr()
 	{
 		return assignExpr();
 	}
 
 	public Rule assignExpr()
 	{
 		// check '=' first as is most common
 		// moved localDef to statement since it can't be on the right hand side
 		return sequence(ifExpr(), optional(enforcedSequence(firstOf(AS_EQUAL, AS_ASSIGN_OPS), OPT_LF(), assignExpr())));
 	}
 
 	public Rule ifExpr()
 	{
 		// rewritten (together with ternaryTail, elvisTail) such as we check condOrExpr only once
 		return sequence(condOrExpr(),
 			optional(firstOf(elvisTail(), ternaryTail())));
 	}
 
 	public Rule ternaryTail()
 	{
 		return enforcedSequence(SP_QMARK, OPT_LF(), setNoSimpleMap(true), ifExprBody(), setNoSimpleMap(false), SP_COL, OPT_LF(), ifExprBody());
 	}
 
 	public Rule elvisTail()
 	{
 		return enforcedSequence(OP_ELVIS, OPT_LF(), ifExprBody());
 	}
 
 	public Rule ifExprBody()
 	{
 		return firstOf(enforcedSequence(KW_THROW, expr()), condOrExpr());
 	}
 
 	public Rule condOrExpr()
 	{
 		return sequence(condAndExpr(), zeroOrMore(enforcedSequence(OP_OR, OPT_LF(), condAndExpr())));
 	}
 
 	public Rule condAndExpr()
 	{
 		return sequence(equalityExpr(), zeroOrMore(enforcedSequence(OP_AND, OPT_LF(), equalityExpr())));
 	}
 
 	public Rule equalityExpr()
 	{
 		return sequence(relationalExpr(), zeroOrMore(enforcedSequence(CP_EQUALITY, OPT_LF(), relationalExpr())));
 	}
 
 	public Rule relationalExpr()
 	{
 		// chg to add ramgeExp as last option
 		// Changed (with typeCheckTail, compareTail) to check for rangeExpr only one
 		return sequence(rangeExpr(), optional(firstOf(typeCheckTail(), compareTail())));
 	}
 
 	public Rule typeCheckTail()
 	{
 		// changed to required, otherwise eats all rangeExpr and compare never gets evaled
 		return enforcedSequence(firstOf(KW_IS, KW_ISNOT, KW_AS), type());
 	}
 
 	public Rule compareTail()
 	{
 		// TODO: check if that is correct
 		// changed to not be zeroOrMore as there can be only one comparaison check in an expression (no 3< x <5)
 		return /*zeroOrMore(*/enforcedSequence(CP_COMPARATORS, OPT_LF(), rangeExpr())/*)*/;
 	}
 
 	public Rule rangeExpr()
 	{
 		// TODO: check if that is correct
 		// changed to not be zeroOrMore(opt instead) as there can be only one range in an expression (no [1..3..5])
 		return sequence(addExpr(),
 			optional(enforcedSequence(firstOf(OP_RANGE_EXCL, OP_RANGE), OPT_LF(), addExpr())));
 	}
 
 	public Rule addExpr()
 	{
 		return sequence(multExpr(),
 			// checking it's not '+=' or '-=', so we can let assignExpr happen
 			zeroOrMore(enforcedSequence(sequence(firstOf(OP_PLUS, OP_MINUS), testNot(AS_EQUAL)), OPT_LF(), multExpr())));
 	}
 
 	public Rule multExpr()
 	{
 		return sequence(parExpr(),
 			// checking it's not '*=', '/=' or '%=', so we can let assignExpr happen
 			zeroOrMore(enforcedSequence(sequence(firstOf(OP_MULT, OP_DIV, OP_MODULO), testNot(AS_EQUAL)), OPT_LF(), parExpr())));
 	}
 
 	public Rule parExpr()
 	{
 		return firstOf(castExpr(), groupedExpr(), unaryExpr());
 	}
 
 	public Rule castExpr()
 	{
 		return sequence(PAR_L, type(), PAR_R, parExpr());
 	}
 
 	public Rule groupedExpr()
 	{
 		return sequence(PAR_L, OPT_LF(), expr(), OPT_LF(), PAR_R, zeroOrMore(termChain()));
 	}
 
 	public Rule unaryExpr()
 	{
 		// grouped with postfixEpr to avoid looking for termExpr twice !
 		return firstOf(prefixExpr(), sequence(termExpr(), optional(firstOf(OP_2MINUS, OP_2PLUS))));
 	}
 
 	public Rule prefixExpr()
 	{
 		return sequence(
 			firstOf(OP_CURRY, OP_BANG, OP_2PLUS, OP_2MINUS, OP_PLUS, OP_MINUS),
 			parExpr());
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
 		return firstOf(
 				enforcedSequence(OP_POUND, id()), // slot litteral (without type)
 				closure(),
 				dsl(), // DSL
 				// Optimized by grouping all the items that start with "type" (since looking for type if slow)
 				sequence(type(), firstOf(
 					sequence(OP_POUND, optional(id())), // type/slot litteral
 					sequence(DOT, KW_SUPER), // named super
 					sequence(DOT, /*OPT_LF(),*/ idExpr()), // static call
 					sequence(PAR_L, expr(), PAR_R), // "simple"
 					itBlock() // ctor block
 				)));
 	}
 
 	public Rule dsl()
 	{
 		//TODO: unclosed DSL ?
 		return sequence(simpleType(),
 			enforcedSequence(DSL_OPEN, OPT_LF(), zeroOrMore(sequence(testNot(DSL_CLOSE), any())), OPT_LF(), DSL_CLOSE));
 	}
 
 	public Rule closure()
 	{
		return sequence(funcType(), OPT_LF(), enforcedSequence(BRACKET_L, zeroOrMore(stmt()), BRACKET_R));
 	}
 
 	public Rule itBlock()
 	{
 		// Do not enforce because it prevents fieldAccesor to work.
 		return sequence(
 			OPT_LF(),
 			enforcedSequence(sequence(BRACKET_L,
 			// Note, don't allow field accesors to be parsed as itBlock
 			peekTestNot(sequence(inFieldInit, OPT_LF(), firstOf(protection(), KW_STATIC, KW_READONLY, GET, SET, GET, SET)/*, echo("Skipping itBlock")*/))),
 			zeroOrMore(stmt()), BRACKET_R));
 	}
 
 	public Rule termChain()
 	{
 		return sequence(OPT_LF(),
 				firstOf(safeDotCall(), safeDynCall(), dotCall(), dynCall(), indexExpr(),
 			callOp(), itBlock()/*, incDotCall()*/));
 	}
 
 	public Rule dotCall()
 	{
 		// test not "..", as this would be a range
 		return enforcedSequence(sequence(DOT, testNot(DOT)), idExpr());
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
 		return sequence(OP_MULT, id());
 	}
 
 	// require params or/and closure, otherwise it's just and ID (this would prevent termbase from checking literals)
 	public Rule call()
 	{
 		return sequence(id(),
 			firstOf(
 			sequence(enforcedSequence(PAR_L, OPT_LF(), optional(args()), PAR_R), optional(closure())), //params & opt. closure
 			closure())); // closure only
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
 		return sequence(SQ_BRACKET_L, expr(), SQ_BRACKET_R);
 	}
 
 	public Rule callOp()
 	{
 		return enforcedSequence(PAR_L, optional(args()), PAR_R, optional(closure()));
 	}
 
 	public Rule litteral()
 	{
 		return firstOf(litteralBase(), list(), map());
 	}
 
 	public Rule litteralBase()
 	{
 		return firstOf(KW_NULL, KW_THIS, KW_SUPER, KW_IT, KW_TRUE, KW_FALSE,
 			strs(), uri(), number(), char_());
 	}
 
 	public Rule list()
 	{
 		return sequence(
 			optional(type()), OPT_LF(),
 			SQ_BRACKET_L, OPT_LF(), listItems(), OPT_LF(), SQ_BRACKET_R);
 	}
 
 	public Rule listItems()
 	{
 		return firstOf(
 			SP_COMMA,
 			// allow extra comma
 			sequence(expr(), zeroOrMore(sequence(SP_COMMA, OPT_LF(), expr())), optional(SP_COMMA)));
 	}
 
 	public Rule map()
 	{
 		return sequence(
 			optional(firstOf(mapType(), simpleMapType())),
 			// Not enforced to allow resolving list of maps [Str:Int][]
 			sequence(SQ_BRACKET_L, OPT_LF(), mapItems(), OPT_LF(), SQ_BRACKET_R));
 	}
 
 	public Rule mapItems()
 	{
 		return firstOf(SP_COL,//empty map
 			// allow extra comma
 			sequence(mapPair(), zeroOrMore(sequence(SP_COMMA, OPT_LF(), mapPair())), optional(SP_COMMA)));
 	}
 
 	public Rule mapPair()
 	{
 		// allowing all expressions is probably more than really needed
 		return sequence(expr(), enforcedSequence(SP_COL, expr()));
 	}
 
 	public Rule mapItem()
 	{
 		return expr();//sequence(firstOf(litteralBase(), id()), zeroOrMore(termChain()));
 	}
 
 	// ------------ Litteral items ---------------------------------------------
 	public Rule strs()
 	{
 		return firstOf(
 			enforcedSequence("\"\"\"", // triple quoted string, // (not using 3QUOTE terminal, since it could eat empty space inside the string)
 			zeroOrMore(firstOf(
 			unicodeChar(),
 			escapedChar(),
 			sequence(testNot(QUOTES3), any()))), QUOTES3),
 			enforcedSequence("\"", // simple string, (not using QUOTE terminal, since it could eat empty space inside the string)
 			zeroOrMore(firstOf(
 			unicodeChar(),
 			escapedChar(),
 			sequence(testNot(QUOTE), any()))), QUOTE));
 	}
 
 	public Rule uri()
 	{
 		return enforcedSequence("`",// (not using TICK terminal, since it could eat empty space inside the string)
 			zeroOrMore(firstOf(
 			unicodeChar(),
 			escapedChar(),
 			sequence(testNot(TICK), any()))),
 			TICK);
 	}
 
 	public Rule char_()
 	{
 		return firstOf(
 			"' '", 
 			enforcedSequence('\'',// (not using SINGLE_Q terminal, since it could eat empty space inside the char)
 				firstOf(
 				unicodeChar(),
 				escapedChar(),
 				any()), //all else
 				SINGLE_Q));
 	}
 
 	public Rule escapedChar()
 	{
 		return enforcedSequence('\\', firstOf('b', 'f', 'n', 'r', 't', '"', '\'', '`', '$', '\\'));
 	}
 
 	public Rule unicodeChar()
 	{
 		return enforcedSequence("\\u", hexDigit(), hexDigit(), hexDigit(), hexDigit());
 	}
 
 	public Rule hexDigit()
 	{
 		return firstOf(digit(),
 			charRange('a', 'f'),
 			charRange('A', 'F'));
 	}
 
 	public Rule digit()
 	{
 		return charRange('0', '9');
 	}
 
 	public Rule number()
 	{
 		return sequence(
 			optional(OP_MINUS),
 			firstOf(
 			// hex number
 			enforcedSequence(firstOf("0x", "0X"), oneOrMore(firstOf("_", hexDigit()))),
 			// decimal
 			// fractional
 			enforcedSequence(fraction(), optional(exponent())),
 			enforcedSequence(digit(),
 			zeroOrMore(sequence(zeroOrMore("_"), digit())),
 			optional(fraction()),
 			optional(exponent()))),
 			optional(nbType()),
 			OPT_SP);
 	}
 
 	public Rule fraction()
 	{
 		// not enfored to allow: "3.times ||" constructs as well as ranges 3..5
 		return sequence(DOT, digit(), zeroOrMore(sequence(zeroOrMore("_"), digit())));
 	}
 
 	public Rule exponent()
 	{
 		return enforcedSequence(charSet("eE"),
 			optional(firstOf(OP_PLUS, OP_MINUS)),
 			digit(),
 			zeroOrMore(sequence(zeroOrMore("_"), digit())));
 	}
 
 	public Rule nbType()
 	{
 		return firstOf(
 			"day", "hr", "min", "sec", "ms", "ns", //durations
 			"f", "F", "D", "d" // float / decimal
 			);
 	}
 
 	// Rewrote more like Fantom Parser (simpler & optimized)
 	public Rule type()
 	{
 		return sequence(
 			firstOf(
 				mapType(),
 				funcType(),
 				sequence(id(), optional(sequence(SP_COLCOL, id())))
 				),
 			// Look for optional map/list/nullable items
 			optional(SP_QMARK), //nullable
 			zeroOrMore(sequence(SQ_BRACKET_L, SQ_BRACKET_R)),//list(s)
 			// Do not allow simple maps within left side of expressions ... this causes issues with ":"
 			optional(sequence(peekTestNot(noSimpleMap), SP_COL, type())),//simple map Int:Str
 			optional(SP_QMARK) // nullable list/map
 			);
 	}
 
 	public Rule simpleMapType()
 	{
 		// It has to be other nonSimpleMapTypes, otherwise it's left recursive (loops forever)
 		return sequence(nonSimpleMapTypes(), SP_COL, nonSimpleMapTypes(),
 			optional(
 			// Not enforcing [] because of problems with maps like this Str:int["":5]
 			sequence(optional(SP_QMARK), SQ_BRACKET_L, SQ_BRACKET_R)), // list of '?[]'
 			optional(SP_QMARK)); // nullable
 	}
 
 	// all types except simple map
 	public Rule nonSimpleMapTypes()
 	{
 		return sequence(
 			firstOf(funcType(), mapType(), simpleType()),
 			optional(
 			// Not enforcing [] because of problems with maps like this Str:int["":5]
 			sequence(optional(SP_QMARK), SQ_BRACKET_L, SQ_BRACKET_R)), // list of '?[]'
 			optional(SP_QMARK)); // nullable
 	}
 
 	// all types except function
 	public Rule nonFunctionType()
 	{
 		return sequence(
 			// Don't allow simpleMap starting with '|' as this conflict with closing Function.
 			firstOf(sequence(testNot(SP_PIPE), simpleMapType()), mapType(), simpleType()),
 			optional(
 			sequence(optional(SP_QMARK), enforcedSequence(
 			SQ_BRACKET_L, SQ_BRACKET_R))), // list of '?[]'
 			optional(SP_QMARK)); // nullable
 	}
 
 	public Rule simpleType()
 	{
 		return sequence(
 			id(),
 			optional(enforcedSequence(SP_COLCOL, id())));
 	}
 
 	public Rule mapType()
 	{
 		// We use nonSimpleMapTypes here as well, because [Str:Int:Str] would be confusing
 		// not enforced to allow map rule to work ([Int:Str][5,""])
 		return sequence(SQ_BRACKET_L, nonSimpleMapTypes(), SP_COL, nonSimpleMapTypes(), SQ_BRACKET_R);
 	}
 
 	public Rule funcType()
 	{
 		return sequence(
 			testNot(sequence(SP_PIPE, SP_PIPE)), // that's a logical OR not a function type
 			enforcedSequence(SP_PIPE,
 			firstOf(
 			// Fantom grammar is not correct for this.
 			// type() stating with '|' would cause issues because it would get confused with the closing "|"
 			// so not allowing types starting with PIPE (testNot), such as an inner function Type (or simpleMap of).
 			// We check in an order that's faster
 			// First we check for one with no formals |->| or |->Str|
 			enforcedSequence(OP_ARROW, optional(nonFunctionType())),
 			// Then we check for one with formals only |Int i| or maybe full: |Int i -> Str|
 			sequence(formals(), optional(enforcedSequence(OP_ARROW, optional(nonFunctionType()))))),
 			SP_PIPE));
 	}
 
 	public Rule formals()
 	{
 		// Formal type of function type would cause issues because it will get confused whether it's
 		// the closing "|" or opening of an inner one. -> not allowing nested functions def (using nonFunctionTypes())
 		return sequence(
 			testNot(SP_PIPE), typeAndOrId(),
 			zeroOrMore(enforcedSequence(SP_COMMA, testNot(SP_PIPE), typeAndOrId()))); // typeAnrOrId - funtional types
 	}
 
 	public Rule typeList()
 	{
 		return sequence(type(), zeroOrMore(enforcedSequence(SP_COMMA, type())));
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
 		return sequence(testNot(keyword()),
 			firstOf(charRange('A', 'Z'), charRange('a', 'z'), "_"),
 			zeroOrMore(firstOf(charRange('A', 'Z'), charRange('a', 'z'), '_', charRange('0', '9'))),
 			OPT_SP);
 	}
 
 	public Rule doc()
 	{
 		// In theory there are no empty lines betwen doc and type ... but thta does happen so alowing it
 		return oneOrMore(sequence(OPT_SP, "**", zeroOrMore(sequence(testNot("\n"), any())), OPT_LF()));
 	}
 
 	@Leaf
 	public Rule spacing()
 	{
 		return oneOrMore(firstOf(
 			// whitespace (Do NOT eat \n since it can be meaningful)
 			oneOrMore(charSet(" \t\u000c")),
 			// multiline comment
 			sequence("/*", zeroOrMore(sequence(testNot("*/"), any())), "*/"), // normal comment
 			// if incomplete multiline comment, then end at end of line
 			sequence("/*", zeroOrMore(sequence(testNot(charSet("\r\n")), any()))/*, charSet("\r\n")*/),
 			// single line comment
 			sequence("//", zeroOrMore(sequence(testNot(charSet("\r\n")), any()))/*, charSet("\r\n")*/)));
 	}
 
 	public Rule LF()
 	{
 		return sequence(oneOrMore(sequence(OPT_SP, charSet("\n\r"))), OPT_SP);
 	}
 
 	public Rule OPT_LF()
 	{
 		return optional(LF());
 	}
 
 	@Leaf
 	public Rule keyword(String string)
 	{
 		// Makes sure not to match things that START with a keyword like "thisisatest"
 		return sequence(string,
 			testNot(firstOf(digit(), charRange('A', 'Z'), charRange('a', 'z'), "_")),
 			optional(spacing())).label(string);
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
 
 	public Rule keyword()
 	{
 		return sequence(
 			// don't bother unless it starts with 'a'-'z'
 			test(charRange('a', 'z')),
 			firstOf(KW_ABSTRACT, KW_AS, KW_ASSERT, KW_BREAK, KW_CATCH, KW_CASE, KW_CLASS,
 			KW_CONST, KW_CONTINUE, KW_DEFAULT, KW_DO, KW_ELSE, KW_FALSE, KW_FINAL,
 			KW_FINALLY, KW_FOR, KW_FOREACH, KW_IF, KW_INTERNAL, KW_IS, KW_ISNOT, KW_IT,
 			KW_MIXIN, KW_NATIVE, KW_NEW, KW_NULL, KW_ONCE, KW_OVERRIDE, KW_PRIVATE,
 			KW_PROTECTED, KW_PUBLIC, KW_READONLY, KW_RETURN, KW_STATIC, KW_SUPER, KW_SWITCH,
 			KW_THIS, KW_THROW, KW_TRUE, KW_TRY, KW_USING, KW_VIRTUAL, KW_VOID, KW_VOLATILE,
 			KW_WHILE));
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
 	public final Rule KW_IT = keyword("ii");
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
 	public final Rule OP_MODULO = terminal("%");
 	public final Rule OP_POUND = terminal("#");
 	// comparators
 	public final Rule CP_EQUALITY = firstOf(terminal("==="), terminal("!=="),
 		terminal("=="), terminal("!="));
 	public final Rule CP_COMPARATORS = firstOf(terminal("<=>"), terminal("<="),
 		terminal(">="), terminal("<"), terminal(">"));
 	// separators
 	public final Rule SP_PIPE = terminal("|");
 	public final Rule SP_QMARK = terminal("?");
 	public final Rule SP_COLCOL = terminal("::");
 	public final Rule SP_COL = terminal(":");
 	public final Rule SP_COMMA = terminal(",");
 	public final Rule SP_SEMI = terminal(";");
 	// assignment
 	public final Rule AS_EQUAL = terminal("=");
 	public final Rule AS_ASSIGN_OPS = firstOf(terminal("*="), terminal("/="),
 		terminal("%="), terminal("+="), terminal("-="));
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
 
 	/**
 	 * Override because sandard firstOf() complains about empty matches
 	 * and we allow empty matches in peekTest
 	 * @param rules
 	 * @return
 	 */
 	@Override
 	@Cached
 	public Rule firstOf(Object[] rules)
 	{
 		return new PeekFirstOfMatcher(toRules(rules));
 	}
 
 	/**
 	 * Custom test matchers which allow result without matching any data (boolean check)
 	 * @param rule
 	 * @return
 	 */
 	@Cached
 	public Rule peekTestNot(Object rule)
 	{
 		return new PeekTestMatcher(toRule(rule), true);
 	}
 	@Cached
 	public Rule peekTest(Object rule)
 	{
 		return new PeekTestMatcher(toRule(rule), false);
 	}
 
 	// ----------- Custom action rules -----------------------------------------
 	/**
 	 * Custom action to find an end of statement
 	 * @return
 	 */
 	public Rule eos()
 	{
 		return sequence(OPT_SP, firstOf(
 			SP_SEMI, // ;
 			LF(), // \n
 			peekTest(BRACKET_R), // '}' is end of statement oo, but no NOT consume it !
 			peekTest(eoi()))); // EOI is end of statement oo, but no NOT consume it !
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
 }
