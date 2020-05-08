 package edu.tum.lua.junit;
 
 import static org.junit.Assert.*;
 
 import java.util.LinkedList;
 
 import org.junit.Test;
 
 import edu.tum.lua.LuaRuntimeException;
 import edu.tum.lua.stdlib.ToNumber;
 
 public class ToNumberTest {
 
 	@Test
 	public void test() {
 		ToNumber p = new ToNumber();
 		LinkedList<Object> l;
 
 		l = new LinkedList<Object>();
 
 		try {
 			p.apply(l);
 			fail("Accept empty input");
 		} catch (LuaRuntimeException e) {
 			assertTrue("Don't accept empty input", true);
 		} catch (Exception e) {
 			fail("Unknow exception");
 		}
 
 		l.add("1234");
		assertEquals("Translating a true int String", 1234, (int) Math.ceil((double) p.apply(l).get(0)));
 
 		l = new LinkedList<Object>();
 		l.add("42.21");
 		assertEquals("Translating a true double String", new Double(42.21), p.apply(l).get(0));
 
 		l = new LinkedList<Object>();
 		l.add("Hello123");
 		assertEquals("Translating a false String", null, (double) p.apply(l).get(0));
 
 		// TODO Test for base argument not completed yet
 	}
 
 }
