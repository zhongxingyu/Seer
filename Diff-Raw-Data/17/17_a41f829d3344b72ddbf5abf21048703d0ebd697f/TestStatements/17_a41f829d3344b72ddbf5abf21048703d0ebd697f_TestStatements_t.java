 package org.cwi.waebric.interpreter;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import org.cwi.waebric.TestUtilities;
 import org.cwi.waebric.parser.ast.AbstractSyntaxTree;
 import org.cwi.waebric.parser.ast.basic.IdCon;
 import org.cwi.waebric.parser.ast.basic.StrCon;
 import org.cwi.waebric.parser.ast.expression.Expression;
 import org.cwi.waebric.parser.ast.expression.KeyValuePair;
 import org.cwi.waebric.parser.ast.markup.Designator;
 import org.cwi.waebric.parser.ast.markup.Markup;
 import org.cwi.waebric.parser.ast.module.function.FunctionDef;
 import org.cwi.waebric.parser.ast.statement.Statement;
 import org.cwi.waebric.parser.ast.statement.embedding.Embed;
 import org.cwi.waebric.parser.ast.statement.embedding.Embedding;
 import org.cwi.waebric.parser.ast.statement.embedding.PostText;
 import org.cwi.waebric.parser.ast.statement.embedding.PreText;
 import org.cwi.waebric.parser.ast.statement.embedding.TextTail;
 import org.cwi.waebric.parser.ast.statement.predicate.Predicate;
 import org.cwi.waebric.parser.ast.statement.predicate.Type;
 
 import org.jdom.CDATA;
 import org.jdom.Comment;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.Text;
 import org.junit.Before;
 import org.junit.Test;
 
 public class TestStatements {
 	
 	private JDOMVisitor visitor;
 	private Document document;
 
 	@Before
 	public void setUp() {
 		this.document = new Document();
 		this.visitor = new JDOMVisitor(document);
 	}
 	
 	/**
 	 * Initiate an if-statement with a true predicate. While interpreting the statement
 	 * check if the sub statement (echo "success";) is executed, by comparing the current
 	 * text to "success".<br><br>
 	 * 
 	 * <code>if(var) echo "success";</code>
 	 */
 	@Test
 	public void testIf() {
 		Expression text = new Expression.TextExpression("success");
 		
 		// Create a true predicate
 		Expression.VarExpression var = new Expression.VarExpression(new IdCon("var"));
 		visitor.getEnvironment().storeVariable("var", text);
 		Predicate truePredicate = new Predicate.RegularPredicate(var);
 
 		// Create sub-statement
 		Statement.Echo successEcho = new Statement.Echo();
 		successEcho.setExpression(text);
 		
 		// Create if statement
 		Statement.If ifStatement = new Statement.If();
 		ifStatement.setPredicate(truePredicate);
 		ifStatement.setTrueStatement(successEcho);
 		
 		Element placeholder = new Element("placeholder");
 		visitor.setCurrent(placeholder);
 		ifStatement.accept(visitor);
 		
 		assertEquals("success", placeholder.getText());
 	}
 	
 	/**
 	 * Create two if-else statements, one with a true predicate and one with a
 	 * false predicate. While interpreting the statement with a true predicate,
 	 * the sub-statement (echo "success";) will be executed, while a false 
 	 * predicate will trigger (echo "fail;")<br><br>
 	 * 
 	 * Consecutively execute the statements with a true and false predicate,
 	 * then compare the current text to "successfail" to verify they statements
 	 * maintained the correct flow.<br><br>
 	 * 
 	 * <code>if(predicate) echo "success"; else echo "fail";</code>
 	 */
 	@Test
 	public void testIfElse() {
 		// Create a true and false predicate
 		Expression.VarExpression var = new Expression.VarExpression(new IdCon("var"));
 		visitor.getEnvironment().storeVariable("var", new Expression.ListExpression());
 		Predicate truePredicate = new Predicate.RegularPredicate(var);
 		Predicate falsePredicate = new Predicate.Not(truePredicate);
 
 		// Create sub-statement
 		Statement.Echo successEcho = new Statement.Echo(new Expression.TextExpression("success"));
 		Statement.Echo failEcho = new Statement.Echo(new Expression.TextExpression("fail"));
 		
 		// Create instance of if-else statement with different flows
 		Statement.IfElse ifFlow = new Statement.IfElse(truePredicate, successEcho, failEcho);
 		Statement.IfElse elseFlow = new Statement.IfElse(falsePredicate, successEcho, failEcho);
 		
 		Element placeholder = new Element("placeholder");
 		visitor.setCurrent(placeholder);
 		ifFlow.accept(visitor);
 		elseFlow.accept(visitor);
 		
 		assertEquals("successfail", placeholder.getText());
 	}
 	
 	/**
 	 * Construct a blow statement, with three sub-statements (echo "one"; echo "two";
 	 * echo "three";). While interpreting the block, each sub-statement should be visited, 
 	 * this can be asserted by comparing the current text to "onetwothree".<br><br>
 	 * 
 	 * <code>{ echo "one"; echo "two; echo "three"; }</code>
 	 */
 	@Test
 	public void testBlock() {
 		Statement.Block block = new Statement.Block();
 		block.addStatement(new Statement.Echo(new Expression.TextExpression("one")));
 		block.addStatement(new Statement.Echo(new Expression.TextExpression("two")));
 		block.addStatement(new Statement.Echo(new Expression.TextExpression("three")));
 		
 		Element placeholder = new Element("placeholder");
 		visitor.setCurrent(placeholder);
 		block.accept(visitor);
 		
 		assertEquals("onetwothree", placeholder.getText());
 	}
 	
 	/**
 	 * Construct a comment statement with the textual value "success". After interpreting
 	 * the statement a comment object is expected in the content. Compare the text of 
 	 * comment to the expected "success".<br><br>
 	 * 
 	 * <code>comment "success";</code>
 	 */
 	@Test
 	public void testComment() {
 		Statement.Comment comment = new Statement.Comment();
 		comment.setComment(new StrCon("success"));
 		
 		Element placeholder = new Element("placeholder");
 		visitor.setCurrent(placeholder);
 		comment.accept(visitor);
 		
 		assertEquals(Comment.class, placeholder.getContent(0).getClass());
 		assertEquals("success", ((Comment) placeholder.getContent(0)).getText());
 	}
 	
 	/**
 	 * Construct a CDATA statement with the textual value "success". After interpreting
 	 * the statement a CDATA object is expected in the content. Compare the text of 
 	 * CDATA to the expected "success".<br><br>
 	 * 
 	 * <code>cdata "success";</code>
 	 */
 	@Test
 	public void testCData() {
 		Statement.CData comment = new Statement.CData();
 		comment.setExpression(new Expression.TextExpression("success"));
 		
 		Element placeholder = new Element("placeholder");
 		visitor.setCurrent(placeholder);
 		comment.accept(visitor);
 		
 		assertEquals(CDATA.class, placeholder.getContent(0).getClass());
 		assertEquals("success", ((CDATA) placeholder.getContent(0)).getText());
 	}
 	
 	/**
 	 * Construct an echo statement with the textual expression "success". During
 	 * interpretation this statement will store the expression in its current element.
 	 * After interpretation check if the current element indeed has "success" as
 	 * text value.<br><br>
 	 * 
 	 * <code>echo "success";</code>
 	 */
 	@Test
 	public void testEchoExpression() {
 		Statement.Echo echo = new Statement.Echo();
 		echo.setExpression(new Expression.TextExpression("success"));
 		
 		Element placeholder = new Element("placeholder");
 		visitor.setCurrent(placeholder);
 		echo.accept(visitor);
 		
 		assertEquals("success", placeholder.getText());
 	}
 	
     /** 
      * Construct an echo statement with the embedding "left<"success">right". During
      * interpretation the text and expression of embedding will be stored into the
      * current element. After interpretation check if the current element indeed
      * has "leftsuccessright" as text value.<br><br>
      * 
      * <code>echo "left<"success">right";</code>
 	 */
 	@Test
 	public void testEchoEmbedding() {
 		Statement.EchoEmbedding echo = new Statement.EchoEmbedding();
 		Embedding embedding = new Embedding();
 		embedding.setPre(new PreText("left"));
 		embedding.setEmbed(new Embed.ExpressionEmbed(new Expression.TextExpression("success")));
 		embedding.setTail(new TextTail.PostTail(new PostText("right")));
 		echo.setEmbedding(embedding);
 		
 		Element placeholder = new Element("placeholder");
 		visitor.setCurrent(placeholder);
 		echo.accept(visitor);
 		
 		assertEquals("leftsuccessright", placeholder.getText());
 	}
 	
 	/**
 	 * Create an each statement, which will iterate over the list ["test","has","succeeded"].
 	 * For each element the statement (echo e) will be executed, in which the variable e
 	 * represents an element in the list. The interpretation will be verified by comparing
 	 * the current text to "testhassucceeded".<br><br>
 	 * 
 	 * <code>each(myvar:["test","has","succeeded"]) echo myvar;</code>
 	 */
 	@Test
 	public void testEach() {
 		Statement.Each each = new Statement.Each();
 		
 		// List expression on which will be iterated
 		Expression.ListExpression list = new Expression.ListExpression();
 		list.addExpression(new Expression.TextExpression("test"));
 		list.addExpression(new Expression.TextExpression("has"));
 		list.addExpression(new Expression.TextExpression("succeeded"));
 		each.setExpression(list);
 		
 		// Variable which is constructed per element
 		IdCon var = new IdCon("myvar");
 		each.setVar(var);
 		
 		// Statement which is executed for each element
 		Statement.Echo echo = new Statement.Echo();
 		echo.setExpression(new Expression.VarExpression(var));
 		each.setStatement(echo);
 		
 		Element placeholder = new Element("placeholder");
 		visitor.setCurrent(placeholder);
 		visitor.visit(each); // Execute visit
 		
 		assertEquals("testhassucceeded", placeholder.getText());
 	}
 	
 	/**
 	 * Create a yield stack and check if the correct statement is executed.<br><br>
 	 * 
 	 * Simulated program:<br>
 	 * <code>
 	 * 	def main() call1() echo "success"; end
 	 * 	def call1() call2() yield; end
 	 * 	def call2() yield; end
 	 * </code>
 	 */
 	@Test
 	public void testYield() {
 		Statement yield = new Statement.Yield();
 		
 		visitor.addYield(new Statement.Echo(new Expression.TextExpression("success")));
 		visitor.addYield(new Statement.Yield());
 		
 		Element placeholder = new Element("placeholder");
 		visitor.setCurrent(placeholder);
 		yield.accept(visitor); // Execute visit
 		
 		assertEquals("success", placeholder.getText());
 	}
 	
 	/**
 	 * Check if let correctly initiates a variable definition 
 	 * and destroys it afterwards.
 	 */
 	@Test
 	public void testVarLet() {
 		AbstractSyntaxTree ast = TestUtilities.quickParse("src/test/waebric/stm/letvar.wae");
 		FunctionDef main = ast.getRoot().get(0).getFunctionDefinitions().get(0);
 		
 		Element placeholder = new Element("placeholder");
 		visitor.setCurrent(placeholder);
 		main.accept(visitor);
 		
 		assertEquals("undefsuccessundef", placeholder.getText());
 	}
 	
 	/**
 	 * Check if let correctly initiates a function definition 
 	 * and destroys it afterwards.
 	 */
 	@Test
 	public void testFuncLet() {
 		AbstractSyntaxTree ast = TestUtilities.quickParse("src/test/waebric/stm/letfunc.wae");
 		FunctionDef main = ast.getRoot().get(0).getFunctionDefinitions().get(0);
 		
 		Element placeholder = new Element("placeholder");
 		visitor.setCurrent(placeholder);
 		main.accept(visitor);
 	
 		// <func/>success<func/>
 		Element func1 = (Element) placeholder.getContent().get(0);
 		assertEquals("func", func1.getName());
 		Text success = (Text) placeholder.getContent().get(1);
 		assertEquals("success", success.getText());
 		Element func2 = (Element) placeholder.getContent().get(2);
 		assertEquals("func", func2.getName());
 	}
 	
 	/**
 	 * Check if let correctly initiates both a function and variable
 	 * definition and destroys it afterwards.
 	 */
 	@Test
 	public void testVarFuncLet() {
 		AbstractSyntaxTree ast = TestUtilities.quickParse("src/test/waebric/stm/letfuncvar.wae");
 		FunctionDef main = ast.getRoot().get(0).getFunctionDefinitions().get(0);
 		
 		Element placeholder = new Element("placeholder");
 		visitor.setCurrent(placeholder);
 		main.accept(visitor);
 		
 		// <func value="UNDEFINED" />success<func value="UNDEFINED" />
 		Element func1 = (Element) placeholder.getContent().get(0);
 		assertEquals("func", func1.getName());
 		assertEquals("UNDEFINED", func1.getAttributeValue("value"));
 		Text success = (Text) placeholder.getContent().get(1);
 		assertEquals("success", success.getText());
 		Element func2 = (Element) placeholder.getContent().get(2);
 		assertEquals("func", func2.getName());
 		assertEquals("UNDEFINED", func2.getAttributeValue("value"));
 	}
 	
 	@Test
 	public void testNestedLet() {
 		AbstractSyntaxTree ast = TestUtilities.quickParse("src/test/waebric/stm/letnested.wae");
 		FunctionDef main = ast.getRoot().get(0).getFunctionDefinitions().get(0);
 		
 		Element placeholder = new Element("placeholder");
 		visitor.setCurrent(placeholder);
 		main.accept(visitor);
		
		// <func value="UNDEFINED" />success<func value="success" /><func value="UNDEFINED" />
		Element func1 = (Element) placeholder.getContent().get(0);
		assertEquals("func", func1.getName());
		assertEquals("UNDEFINED", func1.getAttributeValue("value"));
		Text success = (Text) placeholder.getContent().get(1);
		assertEquals("success", success.getText());
		Element func2 = (Element) placeholder.getContent().get(2);
		assertEquals("func", func2.getName());
		assertEquals("success", func2.getAttributeValue("value"));
		Element func3 = (Element) placeholder.getContent().get(3);
		assertEquals("func", func3.getName());
		assertEquals("UNDEFINED", func3.getAttributeValue("value"));
 	}
 	
 	@Test
 	public void testRecursiveLet() {
 		AbstractSyntaxTree ast = TestUtilities.quickParse("src/test/waebric/stm/letrecurssion.wae");
 		FunctionDef main = ast.getRoot().get(0).getFunctionDefinitions().get(0);
 		
 		Element placeholder = new Element("placeholder");
 		visitor.setCurrent(placeholder);
 		main.accept(visitor);
		
		// success
		assertEquals("success", placeholder.getText());
 	}
 	
 	/**
 	 * Execute mark-up statement, which consists of a single tag called "test". 
 	 * Verify that the root element contains a child element with the name test
 	 * after interpretation.
 	 */
 	@Test
 	public void testRegularMarkupStatement() {
 		Statement.RegularMarkupStatement stm = new Statement.RegularMarkupStatement();
 		stm.setMarkup(new Markup.Tag(new Designator(new IdCon("test"))));
 		
 		Element placeholder = new Element("placeholder");
 		visitor.setCurrent(placeholder);
 		stm.accept(visitor); // Execute visit
 		
 		assertNotNull(placeholder.getChild("test"));
 	}
 	
 	/**
 	 * Execute mark-ups statement consisting of two tags "test1" and "test2".
 	 * After interpretation verify that the structure is stored as: 
 	 * <code>< test1 >< test2 / >< /test1 ></code>
 	 */
 	@Test
 	public void testMarkupMarkup() {
 		Statement.MarkupMarkup stm = new Statement.MarkupMarkup();
 		stm.addMarkup(new Markup.Tag(new Designator(new IdCon("test1"))));
 		stm.setMarkup(new Markup.Tag(new Designator(new IdCon("test2"))));
 		
 		Element placeholder = new Element("placeholder");
 		visitor.setCurrent(placeholder);
 		stm.accept(visitor); // Execute visit
 		
 		Element test1 = placeholder.getChild("test1");
 		assertNotNull(test1);
 		assertNotNull(test1.getChild("test2")); 
 	}
 	
 	/**
 	 * Execute mark-expr statement consisting of tag "test" and expression "success".
 	 * After interpretation verify that the structure is stored as: 
 	 * <code>< test > success < /test ></code>
 	 */
 	@Test
 	public void testMarkupExpression() {
 		Statement.MarkupExp stm = new Statement.MarkupExp();
 		stm.addMarkup(new Markup.Tag(new Designator(new IdCon("test"))));
 		stm.setExpression(new Expression.TextExpression("success"));
 		
 		Element placeholder = new Element("placeholder");
 		visitor.setCurrent(placeholder);
 		stm.accept(visitor); // Execute visit
 		
 		Element test = placeholder.getChild("test");
 		assertNotNull(test);
 		assertEquals("success", test.getText());
 	}
 	
 	/**
 	 * Execute mark-embedding statement consisting of tag "test" and embedding 
 	 * "left<"success">right". After interpretation verify that the structure:
 	 * <code>< test > leftsuccessright < /test ></code>
 	 */
 	@Test
 	public void testMarkupEmbedding() {
 		Statement.MarkupEmbedding stm = new Statement.MarkupEmbedding();
 		stm.addMarkup(new Markup.Tag(new Designator(new IdCon("test"))));
 		Embedding embedding = new Embedding();
 		embedding.setPre(new PreText("left"));
 		embedding.setEmbed(new Embed.ExpressionEmbed(new Expression.TextExpression("success")));
 		embedding.setTail(new TextTail.PostTail(new PostText("right")));
 		stm.setEmbedding(embedding);
 		
 		Element placeholder = new Element("placeholder");
 		visitor.setCurrent(placeholder);
 		stm.accept(visitor); // Execute visit
 		
 		Element test = placeholder.getChild("test");
 		assertNotNull(test);
 		assertEquals("leftsuccessright", test.getText());
 	}
 	
 	/**
 	 * Execute mark-embedding statement consisting of tag "test" and statement 
 	 * (echo "success";). After interpretation verify that the structure:
 	 * <code>< test > success < /test ></code>
 	 */
 	@Test
 	public void testMarkupStatement() {
 		Statement.MarkupStat stm = new Statement.MarkupStat();
 		stm.addMarkup(new Markup.Tag(new Designator(new IdCon("test"))));
 		stm.setStatement(new Statement.Echo(new Expression.TextExpression("success")));
 		
 		Element placeholder = new Element("placeholder");
 		visitor.setCurrent(placeholder);
 		stm.accept(visitor); // Execute visit
 		
 		Element test = placeholder.getChild("test");
 		assertNotNull(test);
 		assertEquals("success", test.getText());
 	}
 	
 	/**
 	 * Verify that all predicates evaluate in the correct boolean value.
 	 */
 	@Test
 	public void testEvaluatePredicate() {
 		// Is-string predicate
 		Predicate.Is validString = new Predicate.Is();
 		validString.setExpression(new Expression.TextExpression("test"));
 		validString.setType(new Type.StringType());
 		assertTrue(visitor.evaluatePredicate(validString));
 		
 		Predicate.Is invalidString = new Predicate.Is();
 		invalidString.setExpression(new Expression.NatExpression(1337));
 		invalidString.setType(new Type.StringType());
 		assertFalse(visitor.evaluatePredicate(invalidString));
 		
 		// Is-list predicate
 		Predicate.Is validList = new Predicate.Is();
 		validList.setExpression(new Expression.ListExpression());
 		validList.setType(new Type.ListType());
 		assertTrue(visitor.evaluatePredicate(validList));
 		
 		Predicate.Is invalidList = new Predicate.Is();
 		invalidList.setExpression(new Expression.NatExpression(1337));
 		invalidList.setType(new Type.ListType());
 		assertFalse(visitor.evaluatePredicate(invalidList));
 	
 		// Is-record predicate
 		Predicate.Is validRecord = new Predicate.Is();
 		validRecord.setExpression(new Expression.RecordExpression());
 		validRecord.setType(new Type.RecordType());
 		assertTrue(visitor.evaluatePredicate(validRecord));
 		
 		Predicate.Is invalidRecord = new Predicate.Is();
 		invalidRecord.setExpression(new Expression.NatExpression(1337));
 		invalidRecord.setType(new Type.RecordType());
 		assertFalse(visitor.evaluatePredicate(invalidRecord));
 		
 		// Field predicate, check if record element can be found
 		Expression.RecordExpression recordExpr = new Expression.RecordExpression();
 		KeyValuePair e = new KeyValuePair();
 		e.setIdentifier(new IdCon("valid"));
 		e.setExpression(new Expression.TextExpression("succes"));
 		recordExpr.addKeyValuePair(e);
 		
 		Predicate.RegularPredicate validField = new Predicate.RegularPredicate();
 		validField.setExpression(new Expression.Field(recordExpr, new IdCon("valid")));
 		assertTrue(visitor.evaluatePredicate(validField));
 		
 		Predicate.RegularPredicate invalidField = new Predicate.RegularPredicate();
 		invalidField.setExpression(new Expression.Field(recordExpr, new IdCon("invalid")));
 		assertFalse(visitor.evaluatePredicate(invalidField));
 		
 		// Variable predicate, check if variable is defined
 		visitor.getEnvironment().storeVariable("valid", new Expression.TextExpression("success"));
 		Predicate.RegularPredicate validVar = new Predicate.RegularPredicate();
 		validVar.setExpression(new Expression.VarExpression(new IdCon("valid")));
 		assertTrue(visitor.evaluatePredicate(validVar));
 		
 		Predicate.RegularPredicate invalidVar = new Predicate.RegularPredicate();
 		invalidVar.setExpression(new Expression.VarExpression(new IdCon("invalid")));
 		assertFalse(visitor.evaluatePredicate(invalidVar));
 
 		// And predicate
 		assertTrue(visitor.evaluatePredicate(new Predicate.And(validString, validString))); // true && true = true
 		assertFalse(visitor.evaluatePredicate(new Predicate.And(validString, invalidString))); // true && false = false
 		assertFalse(visitor.evaluatePredicate(new Predicate.And(invalidString, validString))); // false && true = false
 		assertFalse(visitor.evaluatePredicate(new Predicate.And(invalidString, invalidString))); // false && false = false
 		
 		// Or predicate
 		assertTrue(visitor.evaluatePredicate(new Predicate.Or(validString, validString))); // true || true = true
 		assertTrue(visitor.evaluatePredicate(new Predicate.Or(validString, invalidString))); // true || false = true
 		assertTrue(visitor.evaluatePredicate(new Predicate.Or(invalidString, validString))); // false || true = true
 		assertFalse(visitor.evaluatePredicate(new Predicate.Or(invalidString, invalidString))); // false || false = false
 		
 		// Not predicate
 		assertTrue(visitor.evaluatePredicate(new Predicate.Not(invalidString))); // ! false = true
 		assertFalse(visitor.evaluatePredicate(new Predicate.Not(validString))); // ! true = false
 	}
 	
 }
