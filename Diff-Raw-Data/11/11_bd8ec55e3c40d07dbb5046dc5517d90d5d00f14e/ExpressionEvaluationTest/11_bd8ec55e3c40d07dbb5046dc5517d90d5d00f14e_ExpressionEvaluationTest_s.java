 /**
  * ExpressionEvaluationTest.java
  * 
  * Class to test some of the features of JEval and the the integration
  * into our simulation.
  * 
  * @autor Emmanuel Pederson
  */
 package edu.wheaton.simulator.test.expression;
 
 import net.sourceforge.jeval.EvaluationException;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.wheaton.simulator.datastructure.ElementAlreadyContainedException;
 import edu.wheaton.simulator.entity.Agent;
 import edu.wheaton.simulator.entity.Entity;
 import edu.wheaton.simulator.entity.Prototype;
 import edu.wheaton.simulator.expression.AbstractExpressionFunction;
 import edu.wheaton.simulator.expression.Expression;
 import edu.wheaton.simulator.simulation.Grid;
 
 public class ExpressionEvaluationTest {
 
 	/*
 	 * When triggers and agents are working properly, then set them up here for
 	 * further testing.
 	 */
 	@Before
 	public void setUp() {
 		//TODO ExpressionEvaluationTest.setUp() is empty
 	}
 
 	@After
 	public void tearDown() {
 		//TODO ExpressionEvaluationTest.tearDown() is empty
 	}
 
 	@Test
 	public void testSimpleBooleanEvaluation() {
 		try {
 			Assert.assertTrue(Expression.evaluateBool("1<2"));
 		} catch (EvaluationException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void testSimpleBooleanEvaluation2() {
 		try {
 			Assert.assertFalse(Expression.evaluateBool("1 == 0"));
 		} catch (EvaluationException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Test
 	public void testSimpleBooleanEqualEvaluation() {
 		try {
			Assert.assertTrue(Expression.evaluateBool("1=1"));
 		} catch (EvaluationException e) {
 			e.printStackTrace();
 		}
 	}
 	@Test
 	public void testComplexBooleanEvaluation() {
 		try {
 			Assert.assertFalse(Expression
 					.evaluateBool("(1+2) > (2*2) && (3/2) < 3"));
 		} catch (EvaluationException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Test
 	public void testDoubleEvaluation() {
 		Double result = 1.0;
 		try {
 			result = Expression.evaluateDouble("3 * (3 + 1)");
 		} catch (EvaluationException e) {
 			e.printStackTrace();
 		}
 		Assert.assertTrue(11 < result && result < 13);
 	}
 
 	@Test
 	public void testStringEvaluation() {
 		try {
 			Assert.assertEquals("'tenants'",
 					Expression.evaluateString("'ten' + 'ants'"));
 		} catch (EvaluationException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Test
 	public void testAddVariables() {
 		Expression testExpression = new Expression(
 				"#{three} < #{ten}");
 		testExpression.importVariable("three", "3");
 		testExpression.importVariable("ten", "10");
 		try {
 			Assert.assertTrue(testExpression.evaluateBool());
 		} catch (EvaluationException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void testNonStaticTestEquals() {
		Expression testExpression = new Expression("1=1");
 		try {
 			Assert.assertTrue(testExpression.evaluateBool());
 		} catch (EvaluationException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void testNonStaticTestEqualWithVar() {
		Expression testExpression = new Expression("{one}=1");
 		testExpression.importVariable("one", "1");
 		try {
 			Assert.assertTrue(testExpression.evaluateBool());
 		} catch (EvaluationException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Test
 	public void testEntityFieldExpression() {
 		Entity entity = new Entity();
 		try {
 			entity.addField("name", "'mmmhmmmhm'");
 		} catch (ElementAlreadyContainedException e1) {
 			e1.printStackTrace();
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 
 		Expression testExpression = new Expression("#{entity.name}");
 		testExpression.importEntity("entity", entity);
 
 		try {
 			Assert.assertEquals("'mmmhmmmhm'", testExpression.evaluateString());
 		} catch (EvaluationException e) {
 			System.err.println("##testEntityFieldReference() failed##");
 		}
 	}
 
 	@Test
 	public void testComplicatedEntityFieldExpression() throws Exception {
 		Entity xThis = new Entity();
 		Entity xOther = new Entity();
 
 		xThis.addField("x", "5");
 		xThis.addField("y", "0");
 
 		xOther.addField("x", "0");
 		xOther.addField("y", "0");
 
 		Expression distanceExpression = new Expression(
 				"sqrt(pow(#{this.x}-#{other.x},2) + pow(#{this.y}-#{other.y},2))");
 		distanceExpression.importEntity("this", xThis);
 		distanceExpression.importEntity("other", xOther);
 
 		Assert.assertEquals(new Double(5.0),
 				distanceExpression.evaluateDouble());
 	}
 	
 	@Test
 	public void testRPSLogic() throws Exception {
 		Grid testGrid = new Grid(100, 100);
 //		ExpressionEvaluator xMoveRight = new Expression("move('this', #{this.x} + 1, #{this.y})");
 //		ExpressionEvaluator yMoveUp = new Expression("move('this', #{this.x}, #{this.y} + 1)");		
 //		ExpressionEvaluator xMoveLeft = new Expression("move('this', #{this.x} - 1, #{this.y})");		
 //		ExpressionEvaluator yMoveDown = new Expression("move('this', #{this.x}, #{this.y} - 1)");
		Expression dir1 = new Expression("#{this.direction}=1");
 		Prototype testPrototype = new Prototype(testGrid, "name");
 		testPrototype.addField("type", "'test'");
 		testPrototype.addField("direction", "1");
 //		Trigger testTrigger = new Trigger("moveRight", 1, dir1, xMoveRight);
 //		testPrototype.addTrigger(testTrigger);
 		Agent testAgent1 = testPrototype.clonePrototype();
 //		Agent testAgent2 = testPrototype.clonePrototype();
 		testGrid.addAgent(testAgent1, 0, 0);
 //		testGrid.addAgent(testAgent2, 1, 0);
 //		ExpressionEvaluator condition = dir1.clone();
 		dir1.importEntity("this", testAgent1);
 //		condition.importFunction(new IsSlotOpen());
 //		boolean testBool = condition.evaluateBool();
 		Assert.assertTrue(dir1.evaluateBool());
 	}
 	
 	@Test
 	public void testCustomFunctionExpression() throws Exception {
 		Entity xThis = new Entity();
 		Entity xOther = new Entity();
 
 		xThis.addField("x", "5");
 		xThis.addField("y", "0");
 
 		xOther.addField("x", "0");
 		xOther.addField("y", "0");
 
 		final Expression testExpression = new Expression(
 				"distance('this','other')");
 		testExpression.importEntity("this", xThis);
 		testExpression.importEntity("other", xOther);
 
 		testExpression.importFunction(new AbstractExpressionFunction() {
 
 			@Override
 			public String getName() {
 				return "distance";
 			}
 
 			@Override
 			public int getResultType() {
 				return AbstractExpressionFunction.RESULT_TYPE_NUMERIC;
 			}
 
 			@Override
 			public String execute(String[] args) throws EvaluationException {
 				for (int i = 0; i < args.length; ++i)
 					args[i] = args[i].replaceAll("'", "");
 
 				Entity arg0 = resolveEntity(args[0]);
 				Entity arg1 = resolveEntity(args[1]);
 
 				Expression genericDistanceExpression = new Expression(
 						"sqrt(pow(#{arg0.x}-#{arg1.x},2) + pow(#{arg0.y}-#{arg1.y},2))");
 
 				genericDistanceExpression.importEntity("arg0", arg0);
 				genericDistanceExpression.importEntity("arg1", arg1);
 				return genericDistanceExpression.evaluateString();
 			}
 		});
 
 		Expression expr = testExpression.clone();
 		
 		Assert.assertEquals(new Double(5.0), expr.evaluateDouble());
 	}
 }
