 package net.toobop.dot.parser;
 
 import org.parboiled.BaseParser;
 import org.parboiled.Rule;
 import org.parboiled.annotations.BuildParseTree;
 
 
 @BuildParseTree
 public class DotParser extends BaseParser<Object>
 {
 
 	AstActions actions = new AstActions();
 
 	final Rule Apo = String("\"");
 
 	final Rule Comma = Sequence(String(","), WhiteSpace());
 
 	final Rule DirectedLine = String("->");
 
 	final Rule Equal = String("=");
 
 	final Rule Gt = String(">");
 
 	final Rule Lt = String("<");
 
 	final Rule Semicolon = String(";");
 
 	final Rule UndirectedLine = String("--");
 
 	Rule Attribute()
 	{
 		return Sequence(Id(), WhiteSpace(), Equal(), Value(), ACTION(actions.attribute()));
 	}
 
 	Rule AttributeList()
 	{
 		return ZeroOrMore(Brackets(Sequence(ZeroOrMore(Sequence(Attribute(), Comma)), Optional(Attribute()))));
 	}
 
 	Rule Body()
 	{
 		return this.ZeroOrMore(FirstOf(Edge(), Node()));
 	}
 
 	Rule Braces(Rule body)
 	{
 		return Sequence(String("{"), WhiteSpace(), body, String("}"), WhiteSpace());
 	}
 
 	Rule Brackets(Rule body)
 	{
 		return Sequence(String("["), WhiteSpace(), body, String("]"), WhiteSpace());
 	}
 
 	Rule Edge()
 	{
 		return Sequence(EdgePoint(),//
 			EdgeLine(),//
 			EdgePoint(),//
 			AttributeList(),//
 			OptionalSemicolon(),//
 			ACTION(actions.edge()));
 	}
 
 	Rule EdgeLine()
 	{
 		return Sequence(FirstOf(DirectedLine, UndirectedLine), WhiteSpace());
 	}
 
 	Rule EdgePoint()
 	{
 		return Sequence(OneOrMore(IdLetter()), push(matchOrDefault("")), //
 			Optional(Sequence(String(":"), push(matchOrDefault("")), Port(), push(matchOrDefault("")))),//
 			WhiteSpace(), ACTION(actions.edgePoint())); //
 	}
 
 	Rule Equal()
 	{
 		return Sequence(Equal, WhiteSpace());
 	}
 
 	public Rule Graph()
 	{
 		return Sequence(Optional(String("strict")),//
 			push(matchOrDefault("")),//
 			FirstOf(String("graph"), String("digraph")),//
 			push(matchOrDefault("")),//
 			WhiteSpace(),//
 			ACTION(actions.graph()),//
 			Braces(Body()),//
 			WhiteSpace(),//
 			EOI);
 	}
 
 	Rule HtmlValue()
 	{
		return Sequence(Lt, Lt, ZeroOrMore(TestNot(">>"), ANY), push(matchOrDefault("")), Gt, Gt, WhiteSpace());
 	}
 
 	Rule HtmlValueLetter()
 	{
 		return ZeroOrMore(TestNot(String(">>")));
 	}
 
 	Rule Id()
 	{
 
 		return Sequence(OneOrMore(IdLetter()), push(matchOrDefault("")));
 	}
 
 	Rule IdLetter()
 	{
 		return new IdLetterMatcher();
 	}
 
 	Rule Node()
 	{
 		return Sequence(Id(), WhiteSpace(), AttributeList(), OptionalSemicolon(), ACTION(actions.node()));
 	}
 
 	Rule OptionalSemicolon()
 	{
 		return ZeroOrMore(Sequence(Semicolon, WhiteSpace()));
 	}
 
 	Rule Port()
 	{
 
 		return OneOrMore(PortLetter());
 	}
 
 	Rule PortLetter()
 	{
 		return new IdLetterMatcher();
 	}
 
 	Rule StringValue()
 	{
 		// TODO missing escapes ""
 		return Sequence(Apo, OneOrMore(ValueLetter()), push(matchOrDefault("")), Apo, WhiteSpace());
 	}
 
 	Rule Value()
 	{
 		return FirstOf(StringValue(), HtmlValue());
 	}
 
 	Rule ValueLetter()
 	{
 		return new ValueLetterMatcher();
 	}
 
 	Rule WhiteSpace()
 	{
 		return ZeroOrMore(AnyOf("\r\n \t\f"));
 	}
 
 }
