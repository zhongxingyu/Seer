 package edu.tum.lua.junit.stdlib;
 
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
		assertEquals("Translating a false String", null, p.apply(l).get(0));
 
 		l = new LinkedList<Object>();
 		l.add("FF");
 		l.add(new Integer(16));
 		assertEquals("Translating a String, Base", 255, (int) Math.ceil((double) p.apply(l).get(0)));
 
 		l = new LinkedList<Object>();
 		l.add("10");
 		l.add(new Integer(16));
 		assertEquals("Translating a String, Base", 10, (int) Math.ceil((double) p.apply(l).get(0)));
 
 		l = new LinkedList<Object>();
 		l.add("Z");
 		l.add(new Integer(36));
 		assertEquals("Translating a String, Base", 35, (int) Math.ceil((double) p.apply(l).get(0)));
 
 		l = new LinkedList<Object>();
 		l.add(new Integer(10));
 		l.add(new Integer(16));
 		assertEquals("Translating a Number, Base", 10, (int) Math.ceil((double) p.apply(l).get(0)));
 
 		l = new LinkedList<Object>();
 		l.add(new Integer(10));
 		l.add(new Integer(5));
 		assertEquals("Translating a Number, Base < Number", null, (double) p.apply(l).get(0));
 	}
 
 }
