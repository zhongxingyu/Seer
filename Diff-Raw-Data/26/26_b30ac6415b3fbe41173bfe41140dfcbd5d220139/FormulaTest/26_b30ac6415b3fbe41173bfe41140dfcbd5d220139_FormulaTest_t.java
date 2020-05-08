 package eu.mapperproject.xmml.util;
 
 import static org.junit.Assert.*;
 
import java.text.ParseException;
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
 		x = 2d;
 		y = 3d;
		
		try {
			xy = Formula.parseFormula("x*y");
			log = Formula.parseFormula("log(x)");
			plus = Formula.parseFormula("y*y+x*y-x+x*log(x)-sqrt(x)-sqrt(y)");
			pow = Formula.parseFormula("x^2");
			sizeof = Formula.parseFormula("x*sizeof(double)");
			parensA = Formula.parseFormula("x*sizeof(double) + (x+y)/-x");
			parensB = Formula.parseFormula("((x+x)*3 + (x+y)/x)");
		} catch (ParseException e) {
			e.printStackTrace();
		}
 		
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
		assertEquals(new Double(Math.pow(x, 2d)), new Double(pow.evaluate(xyGive)));
 		assertEquals(new Double(y*y+x*y-x+x*Math.log(x)-Math.sqrt(x)-Math.sqrt(y)), new Double(plus.evaluate(xyGive)));
 		assertEquals(new Double(x*64+(x+y)/-x), new Double(parensA.evaluate(xyGive)));
 		assertEquals(new Double(((x+x)*3 + (x+y)/x)), new Double(parensB.evaluate(xyGive)));
 	}
 }
