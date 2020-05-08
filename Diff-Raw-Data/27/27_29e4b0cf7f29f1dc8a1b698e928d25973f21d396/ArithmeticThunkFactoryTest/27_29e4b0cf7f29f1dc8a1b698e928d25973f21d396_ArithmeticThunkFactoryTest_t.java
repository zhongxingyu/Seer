 package org.fuelsyourcyb.exathunk;
 
 import org.junit.Test;
 import static org.junit.Assert.assertEquals;
 
 public class ArithmeticThunkFactoryTest {
 
     public void parseHelper(String expression, Integer intResult) {
 	ArithmeticThunkFactory factory = new ArithmeticThunkFactory((StateFactory<State>)Unit.getInstance());
 	ArithmeticParser parser = new ArithmeticParser(factory);
 
 	NTree<String, Thunk<Integer>> result = new NTree<String, Thunk<Integer>>(
 	    new PresentThunk<Integer>(Unit.getInstance(), intResult));
 
 	NTree<String, Integer> parseTree = parser.parse(expression);
 	System.out.println(parseTree.toString());
 
 	NTree<String, Thunk<Integer>> thunkTree = ThunkUtils.makeThunkTree(parseTree, factory);
 	System.out.println(thunkTree.toString());
 
	thunkTree.bindInto(new ThunkUtils.Evaluator<String, Integer>(factory));
 	System.out.println(thunkTree.toString());
 
 	assert(thunkTree.equals(result));
     }
 
     @Test
     public void testParse1() {
 	parseHelper("* 3 2", 6);
     }
 
     @Test
     public void testParse2() {
 	parseHelper("+ - 1 2 * 3 / 16 4", 11);
     }
 
     /*private class DelayingThunk<Result> implements Thunk<Integer, Result> {
 	private final Integer numStepsNeeded;
 	private Integer numStepsTaken;
 	private final Result delayedResult;
 
 	public DelayingThunk(Integer numStepsNeeded, Result delayedResult) {
 	    this.numStepsNeeded = numStepsNeeded;
 	    this.delayedResult = delayedResult;
 	    this.numStepsTaken = 0;
 	}
 
 	public boolean isFinished() {
 	    return numStepsTaken >= numStepsNeeded;
 	}
 
 	public void step() {
 	    if (!isFinished()) {
 		++numStepsTaken;
 	    }
 	}
 
 	public Integer getState() {
 	    return numStepsTaken;
 	}
 
 	public Result getResult() {
 	    if (isFinished()) {
 		return delayedResult;
 	    } else {
 		return null;
 	    }
 	}
 
 	public String toString() {
 	    return "DelayedThunk<"+numStepsTaken+"/"+numStepsNeeded+" "+delayedResult+">";
 	}
 
 	@SuppressWarnings("unchecked")
 	public boolean equals(Object o) {
 	    if (o == null || !(o instanceof Thunk)) return false;
 	    return ThunkUtils.statefulEquals(this, (Thunk)o);
 	}
     }
 
     private class ZeroFactory implements TypeFactory<Integer> {
 	public Integer makeInstance() { return new Integer(0); }
     }
 
     private class DelayingArithmeticThunkFactory extends ArithmeticThunkFactory<Integer> {
 	private final Integer numSteps;
 
 	public DelayingArithmeticThunkFactory(Integer numSteps) {
 	    super(new ZeroFactory());
 	    this.numSteps = numSteps;
 	}
 
 	public NTree<String, Thunk<Integer, Integer>> makeIntegerThunk(Integer n) {
 	    return new NTree<String, Thunk<Integer, Integer>>(
           	new DelayingThunk<Integer>(numSteps, n));
 	}
     }
 
     @Test
     public void testDelaying() {
 	DelayingArithmeticThunkFactory factory = new DelayingArithmeticThunkFactory(3);
 	String expression = "* 3 2";
 	NTree<String, Thunk<Integer, Integer>> result =
 	    new NTree<String, Thunk<Integer, Integer>>(new PresentThunk<Integer, Integer>(0, 6));
 	NTree<String, Thunk<Integer, Integer>> tree = factory.parse(expression);
 	System.out.println(tree.toString());
 	
 	for (int i = 0; i < 4; ++i) {
 	    assert(!tree.equals(result));
 	    tree.bindInto(factory.getEvaluator());
 	    System.out.println(tree.toString());
 	}
 
 	System.out.println(result.toString());
 	assert(tree.equals(result));
 	
 	}*/
 }
