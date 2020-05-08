 package edu.tum.lua.junit.stdlib;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.tum.lua.stdlib.IPairs;
 import edu.tum.lua.types.LuaFunction;
 import edu.tum.lua.types.LuaTable;
 
 public class IPairsTest {
 
 	IPairs ipairs;
 
 	@Before
 	public void setUp() throws Exception {
 		ipairs = new IPairs();
 	}
 
 	@Test
 	public void testApplyListOfObject() {
 
 		LuaTable table = new LuaTable();
 
 		// First return-value is the private inext() function
 
 		// Second return-value is the same table
 		assertEquals(table, ipairs.apply(table).get(1));
 		// Third return-value is 0.0
 		assertEquals(0.0, ipairs.apply(table).get(2));
 
 		// Empty table
 		// > f, t, v = pairs({})
 		// > print(f(t,v)) >> nil
 		List<Object> returnList = ipairs.apply(table);
		@SuppressWarnings("unused")
 		LuaFunction returnFunction = (LuaFunction) returnList.get(0);
		@SuppressWarnings("unused")
 		LuaTable returnTable = (LuaTable) returnList.get(1);
		@SuppressWarnings("unused")
 		Object returnObject = returnList.get(2);
 
 		// TODO write some more testcases
 		// for example
 
 		// {[1] = "one"}
 		// {["1"] = "one"}
 		// {[0] = "zero", [1] = "one"}
 		// {[0] = "zero", [1] = "one", [2] = "two"}
 		// {[0] = "zero", [1] = "one", [2] = "two", [4] = "four"}
 
 	}
 
 }
