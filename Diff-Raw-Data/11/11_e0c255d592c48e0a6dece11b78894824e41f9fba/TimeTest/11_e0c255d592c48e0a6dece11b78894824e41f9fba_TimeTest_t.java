 package edu.tum.lua.junit.stdlib.os;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.Calendar;
 import java.util.Collections;
 
 import org.junit.Test;
 
 import edu.tum.lua.LuaRuntimeException;
 import edu.tum.lua.stdlib.os.Time;
 import edu.tum.lua.types.LuaTable;
 
 public class TimeTest {
 
 	@Test
 	public void emptyArgumentTest() throws Exception {
 		Time t = new Time();
 		Calendar c = Calendar.getInstance();
		assertTrue(c.getTime().getTime() - (double) t.apply(Collections.emptyList()).get(0) < 0.0001);
 	}
 
 	@Test
 	public void firstArgumentTest() {
 		Time t = new Time();
 		try {
 			t.apply("a");
 			fail("accept other type than table as first argument");
 		} catch (LuaRuntimeException e) {
 			assertTrue("don't accept other type than table as first argument", true);
 		}
 		LuaTable table = new LuaTable();
 		try {
 			t.apply(table);
 			fail("accept empty table");
 		} catch (LuaRuntimeException e) {
 			assertTrue("don't accept empty table", true);
 		}
 		table.set("year", 2013.0);
 		table.set("month", 2.0);
 		try {
 			t.apply(table);
 			fail("accept table without the field 'day'");
 		} catch (LuaRuntimeException e) {
 			assertTrue("don't accept table without the field 'day'", true);
 		}
 		table.set("month", null);
 		table.set("day", 12.0);
 		try {
 			t.apply(table);
 			fail("accept table without the field 'month'");
 		} catch (LuaRuntimeException e) {
 			assertTrue("don't accept table without the field 'month'", true);
 		}
 		table.set("year", null);
 		table.set("month", 2.0);
 		try {
 			t.apply(table);
 			fail("accept table withour the field 'year'");
 		} catch (LuaRuntimeException e) {
 			assertTrue("don't accept table without the field 'year'", true);
 		}
 	}
 
 	@Test
 	public void functionalTest() {
 		Time t = new Time();
 		LuaTable table = new LuaTable();
 		Calendar c = Calendar.getInstance();
 		table.set("year", 2013.0);
 		c.set(Calendar.YEAR, 2013);
 		table.set("month", 2.0);
 		c.set(Calendar.MONTH, 2);
 		table.set("day", 12.0);
 		c.set(Calendar.DAY_OF_MONTH, 12);
 		table.set("hour", 9.0);
 		c.set(Calendar.HOUR, 9);
 		table.set("min", 30.0);
 		c.set(Calendar.MINUTE, 30);
 		table.set("sec", 20.0);
 		c.set(Calendar.SECOND, 20);
 		assertEquals((double) c.getTime().getTime(), t.apply(table).get(0));
 	}
 
 }
