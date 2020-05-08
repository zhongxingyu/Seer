 package edu.tum.lua.junit.stdlib;
 
 import static org.junit.Assert.assertEquals;
 
import java.util.Collections;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.tum.lua.stdlib.Next;
 import edu.tum.lua.stdlib.Pairs;
 import edu.tum.lua.types.LuaFunction;
 import edu.tum.lua.types.LuaTable;
 
 public class PairsTest {
 
 	Pairs pairs;
 
 	@Before
 	public void setUp() throws Exception {
 		pairs = new Pairs();
 	}
 
 	@Test
 	public void testApplyListOfObject() {
 
 		LuaTable table = new LuaTable();
 
 		// First return-value is the next() function
 		assertEquals((new Next()).getClass(), pairs.apply(table).get(0).getClass());
 
 		// Second return-value is the same table
 		// TODO Did it get changed??
 		assertEquals(table, pairs.apply(table).get(1));
 
 		// Third return-value is nil
 		assertEquals(null, pairs.apply(table).get(2));
 
 		// Empty table returns nil
 		// > f, t, v = pairs({})
 		// > print(f(t,v)) >> nil
 		List<Object> returnList = pairs.apply(table);
 		LuaFunction returnFunction = (LuaFunction) returnList.get(0);
 		LuaTable returnTable = (LuaTable) returnList.get(1);
 		Object returnObject = returnList.get(2);
		assertEquals(Collections.singletonList(null), returnFunction.apply(returnTable, returnObject));
 
 	}
 }
