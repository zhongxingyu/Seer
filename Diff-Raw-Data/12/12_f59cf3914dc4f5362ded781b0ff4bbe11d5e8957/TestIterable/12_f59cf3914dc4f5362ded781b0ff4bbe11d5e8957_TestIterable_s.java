 package fr.cantor.functional.test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.TestCase;
 import fr.cantor.functional.Iterable;
 import fr.cantor.functional.IterableArray;
 import fr.cantor.functional.IterationException;
 import fr.cantor.functional.Iterator;
 import fr.cantor.functional.functions.Identity;
 import fr.cantor.functional.functions.Injecter;
 import fr.cantor.functional.functions.method.Method0;
 import fr.cantor.functional.functions.predicates.FalsePredicate1;
 import fr.cantor.functional.functions.predicates.Predicate1;
 import fr.cantor.functional.functions.predicates.TruePredicate1;
 import fr.cantor.functional.nuple.Pair;
 
 public class TestIterable extends TestCase
 {
 	public void testCombineSameSize() throws IterationException
 	{
 		final Iterable<String> strings1 = array("plop1", "onk1");
 		final Iterable<String> strings2 = array("plop2", "onk2");
 		final Iterator<Pair<String, String>> combined = strings1.combine(strings2).iterator();
 		
 		assertTrue(combined.hasNext());
 		Pair<String, String> pair;
 		pair = combined.next();
 		assertEquals("plop1", pair.first);
 		assertEquals("plop2", pair.second);
 		
 		assertTrue(combined.hasNext());
 		pair = combined.next();
 		assertEquals("onk1", pair.first);
 		assertEquals("onk2", pair.second);
 		
 		assertFalse(combined.hasNext());
 	}
 	public void testCombineSmaller() throws IterationException
 	{
 		final Iterable<String> strings1 = array("plop1", "onk1");
 		final Iterable<String> strings2 = array("plop2");
 		final Iterator<Pair<String, String>> combined = strings1.combine(strings2).iterator();
 		
 		assertTrue(combined.hasNext());
 		Pair<String, String> pair;
 		pair = combined.next();
 		assertEquals("plop1", pair.first);
 		assertEquals("plop2", pair.second);
 		
 		assertTrue(combined.hasNext());
 		pair = combined.next();
 		assertEquals("onk1", pair.first);
 		assertNull(pair.second);
 		
 		assertFalse(combined.hasNext());
 	}
 	public void testCombineLarger() throws IterationException
 	{
 		final Iterable<String> strings1 = array("plop1");
 		final Iterable<String> strings2 = array("plop2", "onk2");
 		final Iterator<Pair<String, String>> combined = strings1.combine(strings2).iterator();
 		
 		assertTrue(combined.hasNext());
 		Pair<String, String> pair;
 		pair = combined.next();
 		assertEquals("plop1", pair.first);
 		assertEquals("plop2", pair.second);
 		
 		assertTrue(combined.hasNext());
 		pair = combined.next();
 		assertNull(pair.first);
 		assertEquals("onk2", pair.second);
 		
 		assertFalse(combined.hasNext());
 	}
 	
 	public void testEquals() throws IterationException
 	{
 		assertTrue(array("plop", "onk").equals(array("plop", "onk")));
 	}
 	
 	public void testDump() throws IterationException
 	{
 		List<String> astr = array("plop", "onk").dump(new ArrayList<String>());
 		assertEquals(2, astr.size());
 		assertEquals("plop", astr.get(0));
 		assertEquals("onk", astr.get(1));
 	}
 	
 	public void testJoin() throws IterationException
 	{
 		assertEquals("plop,onk,gloubi", array("plop", "onk", "gloubi").join(","));
 	}
 	
 	public void testInject() throws IterationException
 	{
 		Iterable<Integer> numbers = array(1, 2, 3, 4, 5, 6);
 		assertEquals(Integer.valueOf(21), numbers.inject(0, new Injecter<Integer, Integer>()
 		{
 			public Integer call(Integer sum, Integer n) throws Exception
 			{
 				return sum + n;
 			}
 		}));
 		assertEquals(Integer.valueOf(720), numbers.inject(new Injecter<Integer, Integer>()
 		{
 			public Integer call(Integer a, Integer b) throws Exception
 			{
 				return a * b;
 			}
 		}));
 	}
 	
 	public void testAllAny() throws IterationException
 	{
 		Iterable<Integer> numbers = array(1, 2, 3, 4, 5, 6);
 		assertFalse(numbers.all(new Predicate1<Integer>()
 		{
 			public Boolean call(Integer n) throws Exception
 			{
 				return n <= 5;
 			}
 		}));
 		assertTrue(numbers.all(new Predicate1<Integer>()
 		{
 			public Boolean call(Integer n) throws Exception
 			{
 				return n <= 6;
 			}
 		}));
 		assertFalse(numbers.any(new Predicate1<Integer>()
 		{
 			public Boolean call(Integer n) throws Exception
 			{
 				return n > 6;
 			}
 		}));
 		assertTrue(numbers.any(new Predicate1<Integer>() 
 		{
 			public Boolean call(Integer n) throws Exception
 			{
 				return n > 5;
 			}
 		}));
 	}
 	
 	public void testFirst() throws IterationException
 	{
 		assertEquals(null, array().first());
 		assertEquals("plop", array("plop", "onk").first());
 		assertEquals("plop", array("plop", "onk").select(new TruePredicate1<String>()).first());
 		assertEquals(null, array("plop", "onk").select(new FalsePredicate1<String>()).first());
 		assertEquals("Onk", array("Plop", "Onk", "plop", "onk").select(new Predicate1<String>()
 		{
 			public Boolean call(String str) throws Exception
 			{
 				return str.substring(0, 1).toUpperCase().equals("O");
 			}
 		}).first());
 	}
 	
 	public void testMap() throws IterationException, SecurityException, NoSuchMethodException
 	{
 		assertEquals(array("plop", "onk"), array("plop", "onk").map(new Identity<String>()));
 		assertEquals(array("PLOP", "ONK"), array("plop", "onk").map(new Method0<String, String>("toUpperCase")));
		assertEquals(array("PLOP", "ONK"), array("plop", "onk").map(String.class.getMethod("toUpperCase")));
 	}
 	
 	public void testSelect() throws IterationException
 	{
 		assertEquals(array("plop", "onk"), array("plop", "onk").select(new TruePredicate1<String>()));
 		assertFalse(array("plop", "onk").select(new FalsePredicate1<String>()).iterator().hasNext());
 		assertEquals(array("Plop", "plop"), array("Plop", "Onk", "plop", "onk").select(new Predicate1<String>()
 		{
 			public Boolean call(String str) throws Exception
 			{
 				return str.substring(0, 1).toUpperCase().equals("P");
 			}
 		}));
 	}
 	
 	private <T> Iterable<T> array(T... values)
 	{
 		return new IterableArray<T>(values);
 	}
 }
