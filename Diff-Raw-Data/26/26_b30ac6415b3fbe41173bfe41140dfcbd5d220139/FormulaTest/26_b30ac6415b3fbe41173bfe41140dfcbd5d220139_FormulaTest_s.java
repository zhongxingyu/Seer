 package eu.mapperproject.xmml.util;
 
 import static org.junit.Assert.*;
 
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class FormulaTest {
 	double x, y;
 	private Formula xy, log, plus, pow, sizeof, parensA, parensB;
 	private Set<String> xySet;
 	private Map<String,Integer> xyGive;
 	@Before
 	public void setUp() {
		try {
 		x = 2d;
 		y = 3d;
		xy = Formula.parse("x*y");
		log = Formula.parse("log(x)");
		plus = Formula.parse("y*y+x*y-x+x*log(x)-sqrt(x)-sqrt(y)");
		pow = Formula.parse("x^2");
		sizeof = Formula.parse("x*sizeof(double)");
		parensA = Formula.parse("x*sizeof(double) + (x+y)/-x");
		parensB = Formula.parse("((x+x)*3 + (x+y)/x)");
		} catch (Exception e) {};
 		
 		xySet = new TreeSet<String>();
 		xySet.add("x"); xySet.add("y");
 		xyGive = new TreeMap<String, Integer>();
 		xyGive.put("x", 2); xyGive.put("y", 3);
 	}
 	
 	@Test
 	public void variables() {
 		assertEquals(xySet, xy.getVariableNames());
 		assertFalse(log.getVariableNames().equals(xySet));
 	}
 	
 	@Test
 	public void values() {
 		assertEquals(new Double(x*y), new Double(xy.evaluate(xyGive)));
 		assertEquals(new Double(x*64), new Double(sizeof.evaluate(xyGive)));
 		assertEquals(new Double(y*y+x*y-x+x*Math.log(x)-Math.sqrt(x)-Math.sqrt(y)), new Double(plus.evaluate(xyGive)));
 		assertEquals(new Double(x*64+(x+y)/-x), new Double(parensA.evaluate(xyGive)));
 		assertEquals(new Double(((x+x)*3 + (x+y)/x)), new Double(parensB.evaluate(xyGive)));
 	}
 }
