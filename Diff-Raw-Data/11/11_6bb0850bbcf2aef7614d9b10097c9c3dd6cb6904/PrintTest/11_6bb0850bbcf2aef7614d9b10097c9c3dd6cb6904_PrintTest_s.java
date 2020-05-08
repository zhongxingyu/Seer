 package edu.tum.lua.junit.stdlib;
 
import static org.junit.Assert.assertNull;
 
 import java.util.LinkedList;
 
 import org.junit.Test;
 
import edu.tum.lua.stdlib.VoidFunction;
 import edu.tum.lua.stdlib.Print;
 import edu.tum.lua.types.LuaTable;
 
 public class PrintTest {
 
 	@Test
 	public void test() {
 		Print p = new Print();
 		LinkedList<Object> l = new LinkedList<Object>();
 
 		l.add(1.0);
 		l.add("abc");
 		l.add(null);
 		l.add(true);
 		l.add(new LuaTable());
 		l.add(new VoidFunction());
 		LinkedList<Object> l1 = new LinkedList<Object>();
 
		assertNull(p.apply(l));
		assertNull(p.apply(l1));
 	}
 }
