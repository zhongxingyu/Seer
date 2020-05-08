 package se.fnord.jamon;
 
 import static se.fnord.jamon.CharacterMatchers.digit;
 import static se.fnord.jamon.CharacterMatchers.identifierPart;
 import static se.fnord.jamon.CharacterMatchers.identifierStart;
 import static se.fnord.jamon.CharacterMatchers.match;
 import static se.fnord.jamon.CharacterMatchers.not;
 import static se.fnord.jamon.Parsers.alternative;
 import static se.fnord.jamon.Parsers.between;
 import static se.fnord.jamon.Parsers.delimitedSequence;
 import static se.fnord.jamon.Parsers.endOfInput;
 import static se.fnord.jamon.Parsers.exact;
 import static se.fnord.jamon.Parsers.join;
 import static se.fnord.jamon.Parsers.lalternative;
 import static se.fnord.jamon.Parsers.lbetween;
 import static se.fnord.jamon.Parsers.lift;
 import static se.fnord.jamon.Parsers.loptional;
 import static se.fnord.jamon.Parsers.lrepeat;
 import static se.fnord.jamon.Parsers.lsequence;
 import static se.fnord.jamon.Parsers.matches;
 import static se.fnord.jamon.Parsers.peek;
 import static se.fnord.jamon.Parsers.preserve;
 import static se.fnord.jamon.Parsers.reference;
 import static se.fnord.jamon.Parsers.repeat;
 import static se.fnord.jamon.Parsers.sequence;
 import static se.fnord.jamon.Parsers.skip;
 import static se.fnord.jamon.Parsers.strip;
 
 import org.junit.Test;
 
 public class TestMiniExpParser {
 	public static Parser fieldRef(final Consumer exprRef, final Parser identifier) {
 		final Parser index = between("[", exprRef, "]").attach("index");
 		return delimitedSequence(lsequence(identifier.attach("field"), lrepeat(index)), ".").attach("reference");
 	}
 
 	public static Parser expression() {
 		final Parser identifier = identifier();
 		final Parser mulOp = strip(lalternative(exact("*").attach("*"), exact("/").attach("/")));
 		final Parser addOp = strip(lalternative(exact("+").attach("+"), exact("-").attach("-")));
 		final Parser relOp = strip(lalternative(
 		    	exact("==").attach("=="),
 		    	exact("!=").attach("!="),
 		    	exact("<").attach("<"),
 		        exact("<=").attach("<="),
 		        exact(">").attach(">"),
 		        exact(">=").attach(">=")
 		        // exact("in").attach(RelOp.In)
 		        // ssequence(exact("not"), exact("in")).attach(RelOp.NotIn)
 		));
 		final Parser andOp = strip(exact("and").attach("and"));
 		final Parser orOp = strip(exact("or").attach("or"));
 
 		final ParserReference exprRef = reference();
 		final ParserReference atomRef = reference();
 
 		// FIXME ...
 
 		final Consumer fieldref = fieldRef(exprRef, identifier);
 
 		final Consumer atom = lalternative(
 			literal(),
 			fieldref,
 			between("(", strip(exprRef), ")").attach("paren"),
 		    sequence(strip(skip("-")), atomRef).attach("negate")
 		);
 		atomRef.setTarget(atom);
 
 		final Parser call = lalternative(
 				sequence(atom, lbetween("(", lift(delimitedSequence(0, strip(exprRef), ",")), ")")).attach("call"),
 				atom);
 		final Consumer math = oper(oper(call, mulOp), addOp);
 		final Consumer comparison = oper(math, relOp);
 		final Consumer logical = oper(oper(comparison, andOp), orOp);
 
 		final Consumer condexpr = sequence(logical, loptional(lsequence(sskip("if"), logical, sskip("else"), exprRef))).attach("if");
 		exprRef.setTarget(condexpr);
 		return strip(condexpr);
 	}
 	
 	private static Parser sskip(final String token) {
 		return strip(skip(token));
 	}
 
 	private static Consumer oper(final Consumer term, final Parser op) {
 		return lalternative(delimitedSequence(2, term, op).attach("oper"), term);
 	}
 
 	public static Consumer literal() {
 		return lalternative(
 			wordBoundary("null").attach("null"),
 		    wordBoundary("true").attach("true"),
 		    wordBoundary("false").attach("false"),
 		    integer(),
 		    string()
 		);
 	}
 
 	public static Parser identifier() {
 		return join(sequence(matches(1, 1, identifierStart()), matches(0, identifierPart())));
 	}
 
 	public static Parser integer() {
 		return matches(digit()).attach("long");
 	}
 
 	public static Consumer string() {
 		final Parser dquote = between("\"", repeat(alternative(exact("\\\""), matches(not(match('"'))))), "\"");
 		final Parser squote = between("\'", repeat(alternative(exact("\\\'"), matches(not(match('\''))))), "\'");
 		return join(alternative(dquote, squote)).attach("string");
 	}
 
 	static Parser wordBoundary(final String token) {
 		return lsequence(exact(token), peek(alternative(matches(not(identifierPart())), endOfInput())));
 	}
 
 
 	static Consumer ap() {
 		final Parser entity = identifier().attach("entity");
 		final Parser field = identifier().attach("field");
 
 		final ParserReference ref = reference();
 		final Consumer fieldRef = sequence(entity, lrepeat(1, -1, lsequence(sskip("."), field)), loptional(preserve(skip(sequence(sskip("."), field))))).attach("field reference");
 
 		final Consumer ap = Parsers.subparse(fieldRef, sequence(ref, sskip("."), field));
 		ref.setTarget(ap);
 
 		return ap;
 	}
 	
 	@Test
 	public void testAp() throws Exception {
 		Consumer ap = ap();
 		System.err.println(Parsers.parse(ap, "fnord.fnord.banan").dump());
 	}
 	/*
 	@Test
 	public void testPerformance() throws ParseException, FatalParseException {
 		final Parser p = expression();
 		for (int i = 0; i < 1000; i++)
 			Parsers.parse(p, "default(x[\"foo\"].y[\"bar\"].z(a.b[c]) + (d + e) / 42 * 17, a.b.c.d.e.f.g.h[\"ijklmnopqrstuv\"]) / 32");
 	}
 	public static void main(String[] args) throws ParseException, FatalParseException {
 		new TestMiniExpParser().testPerformance();
 	}
 	
 	 */
 }
