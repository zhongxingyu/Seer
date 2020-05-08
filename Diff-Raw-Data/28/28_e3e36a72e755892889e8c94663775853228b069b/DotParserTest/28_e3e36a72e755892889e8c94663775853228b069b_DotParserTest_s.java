 package net.toobop.dot.parser;
 
 import net.toobop.dot.model.Graph;
 import net.toobop.dot.model.Node;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.parboiled.Parboiled;
 import org.parboiled.parserunners.ReportingParseRunner;
 import org.parboiled.support.ParseTreeUtils;
 import org.parboiled.support.ParsingResult;
 
 
 public class DotParserTest extends AbstractParserTest
 {
 
 	@Test
 	public void testDigraph()
 	{
 		String input = "digraph {}";
 		DotParser parser = Parboiled.createParser(DotParser.class);
 		ParsingResult<?> result = new ReportingParseRunner(parser.Graph()).run(input);
 		GraphContext graphContext = (GraphContext) result.valueStack.peek();
 		Assert.assertTrue(graphContext.getGraph().isDirected());
 	}
 
 	@Test
 	public void testEdge()
 	{
 		String input = "graph { hallo -> xxx }";
 		DotParser parser = Parboiled.createParser(DotParser.class);
 		ParsingResult<?> result = new ReportingParseRunner(parser.Graph()).run(input);
 		assertParse(result);
 		GraphContext graphContext = (GraphContext) result.valueStack.peek();
 		Graph graph = graphContext.create();
 		Assert.assertEquals(1, graph.getEdgesFrom("hallo").size());
 	}
 
 	@Test
 	public void testEdgeWithAttributes()
 	{
 		String input = "graph { hallo -> xxx [x=\"hhh\"] }";
 		DotParser parser = Parboiled.createParser(DotParser.class);
 		ParsingResult<?> result = new ReportingParseRunner(parser.Graph()).run(input);
 		assertParse(result);
 		GraphContext graphContext = (GraphContext) result.valueStack.peek();
 		Graph graph = graphContext.create();
 		Assert.assertEquals(1, graph.getEdgesFrom("hallo").size());
 		Assert.assertEquals("hhh", graph.getEdgesFrom("hallo").iterator().next().getValue("x"));
 	}
 
 	@Test
 	public void testEdgeWithAttributesAndLotsofWhiteSpace()
 	{
 		String input = "graph\t \n{ \nhallo -> xxx [\nx = \"hhh\" \n] }";
 		DotParser parser = Parboiled.createParser(DotParser.class);
 		ParsingResult<?> result = new ReportingParseRunner(parser.Graph()).run(input);
 		assertParse(result);
 		GraphContext graphContext = (GraphContext) result.valueStack.peek();
 		Graph graph = graphContext.create();
 		Assert.assertEquals(1, graph.getEdgesFrom("hallo").size());
 		Assert.assertEquals("hhh", graph.getEdgesFrom("hallo").iterator().next().getValue("x"));
 	}
 
 	@Test
 	public void testEdgeWithPort()
 	{
 		String input = "graph{ hallo -> xxx:y }";
 		DotParser parser = Parboiled.createParser(DotParser.class);
 		ParsingResult<?> result = new ReportingParseRunner(parser.Graph()).run(input);
 		assertParse(result);
 		GraphContext graphContext = (GraphContext) result.valueStack.peek();
 		Graph graph = graphContext.create();
 		Assert.assertEquals(1, graph.getEdgesFrom("hallo").size());
 	}
 
 	@Test
 	public void testEdgeWithSemicolons()
 	{
 		String input = "graph{ hallo -> xxx ;;; }";
 		DotParser parser = Parboiled.createParser(DotParser.class);
 		ParsingResult<?> result = new ReportingParseRunner(parser.Graph()).run(input);
 		assertParse(result);
 		GraphContext graphContext = (GraphContext) result.valueStack.peek();
 		Graph graph = graphContext.create();
 		Assert.assertEquals(1, graph.getEdgesFrom("hallo").size());
 	}
 
 	@Test
 	public void testHtmlNode()
 	{
		String input = "graph{hallo [label=<<aaaa>>]}";
 		DotParser parser = Parboiled.createParser(DotParser.class);
 		ParsingResult<?> result = new ReportingParseRunner(parser.Graph()).run(input);
 		assertParse(result);
 		GraphContext graphContext = (GraphContext) result.valueStack.peek();
 		Node node = graphContext.create().getNode("hallo");
 		Assert.assertNotNull(node);
		Assert.assertEquals("aaaa", node.getValue("label"));
 	}
 
 	@Test
 	public void testId()
 	{
 		String input = "hallo  ";
 		DotParser parser = Parboiled.createParser(DotParser.class);
 		ParsingResult<?> result = new ReportingParseRunner(parser.Id()).run(input);
 		String parseTreePrintOut = ParseTreeUtils.printNodeTree(result);
 		assertParse(result);
 	}
 
 	@Test
 	public void testIdNoBlank()
 	{
 		String input = " ";
 		DotParser parser = Parboiled.createParser(DotParser.class);
 		ParsingResult<?> result = new ReportingParseRunner(parser.Id()).run(input);
 		Assert.assertFalse(result.matched);
 	}
 
 	@Test
 	public void testNode()
 	{
 		String input = "graph{hallo}";
 		DotParser parser = Parboiled.createParser(DotParser.class);
 		ParsingResult<?> result = new ReportingParseRunner(parser.Graph()).run(input);
 		assertParse(result);
 		GraphContext graphContext = (GraphContext) result.valueStack.peek();
 		Assert.assertNotNull(graphContext.getGraph().getNode("hallo"));
 	}
 
 	@Test
 	public void testNodeWithAttribute()
 	{
 		String input = "graph{hallo [x=\"hhh\"]}";
 		DotParser parser = Parboiled.createParser(DotParser.class);
 		ParsingResult<?> result = new ReportingParseRunner(parser.Graph()).run(input);
 		assertParse(result);
 		GraphContext graphContext = (GraphContext) result.valueStack.peek();
 		Node node = graphContext.getGraph().getNode("hallo");
 		Assert.assertNotNull(node);
 		Assert.assertEquals("hhh", node.getValue("x"));
 	}
 
 	@Test
 	public void testNodeWithAttributeAndWhiteSpace()
 	{
 		String input = "graph{hallo [x=\"h\nhh\"]}";
 		DotParser parser = Parboiled.createParser(DotParser.class);
 		ParsingResult<?> result = new ReportingParseRunner(parser.Graph()).run(input);
 		assertParse(result);
 		GraphContext graphContext = (GraphContext) result.valueStack.peek();
 		Node node = graphContext.getGraph().getNode("hallo");
 		Assert.assertNotNull(node);
 		Assert.assertEquals("h\nhh", node.getValue("x"));
 	}
 
 	@Test
 	public void testNodeWithEmptyAttribute()
 	{
 		String input = "graph{hallo []}";
 		DotParser parser = Parboiled.createParser(DotParser.class);
 		ParsingResult<?> result = new ReportingParseRunner(parser.Graph()).run(input);
 		assertParse(result);
 		GraphContext graphContext = (GraphContext) result.valueStack.peek();
 		Node node = graphContext.getGraph().getNode("hallo");
 		Assert.assertNotNull(node);
 	}
 
 	@Test
 	public void testNodeWithManyAttributes()
 	{
 		String input = "graph{hallo [x=\"hhh\",y=\"rrr\"]}";
 		DotParser parser = Parboiled.createParser(DotParser.class);
 		ParsingResult<?> result = new ReportingParseRunner(parser.Graph()).run(input);
 		assertParse(result);
 		GraphContext graphContext = (GraphContext) result.valueStack.peek();
 		Node node = graphContext.getGraph().getNode("hallo");
 		Assert.assertNotNull(node);
 		Assert.assertEquals("hhh", node.getValue("x"));
 		Assert.assertEquals("rrr", node.getValue("y"));
 	}
 
 	@Test
 	public void testNodeWithSemicolon()
 	{
 		String input = "graph{hallo;}";
 		DotParser parser = Parboiled.createParser(DotParser.class);
 		ParsingResult<?> result = new ReportingParseRunner(parser.Graph()).run(input);
 		assertParse(result);
 		GraphContext graphContext = (GraphContext) result.valueStack.peek();
 		Assert.assertNotNull(graphContext.getGraph().getNode("hallo"));
 	}
 
 	@Test
 	public void testParenthesis()
 	{
 		String input = "{}";
 		DotParser parser = Parboiled.createParser(DotParser.class);
 		ParsingResult<?> result = new ReportingParseRunner(parser.Braces(parser.Body())).run(input);
 		assertParse(result);
 	}
 }
