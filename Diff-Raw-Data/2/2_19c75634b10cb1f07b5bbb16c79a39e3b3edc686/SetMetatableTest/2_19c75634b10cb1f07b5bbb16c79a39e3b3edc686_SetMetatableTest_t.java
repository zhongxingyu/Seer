 package edu.tum.lua.junit.stdlib;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.tum.lua.LuaBadArgumentException;
 import edu.tum.lua.stdlib.SetMetatable;
 import edu.tum.lua.types.LuaTable;
 
 public class SetMetatableTest {
 
	private SetMetatable setMetatable;
 
 	@Before
 	public void setUp() {
 		setMetatable = new SetMetatable();
 	}
 
 	@Test
 	public void test() {
 		SetMetatable setMetatable = new SetMetatable();
 
 		LuaTable table = new LuaTable();
 		LuaTable metaTable = new LuaTable();
 
 		assertEquals((Object) null, table.getMetatable());
 
 		assertEquals(table, setMetatable.apply(table, metaTable).get(0));
 		assertEquals(metaTable, table.getMetatable());
 
 		assertEquals(table, setMetatable.apply(table, null).get(0));
 		assertEquals((Object) null, table.getMetatable());
 	}
 
 	@Test(expected = LuaBadArgumentException.class)
 	public void testNoArgument() {
 		setMetatable.apply();
 	}
 
 	@Test(expected = LuaBadArgumentException.class)
 	public void testFirstBadArgument() {
 		setMetatable.apply(1.0);
 	}
 
 	@Test(expected = LuaBadArgumentException.class)
 	public void testSecondBadArgument() {
 		setMetatable.apply(new LuaTable(), 1.0);
 	}
 }
