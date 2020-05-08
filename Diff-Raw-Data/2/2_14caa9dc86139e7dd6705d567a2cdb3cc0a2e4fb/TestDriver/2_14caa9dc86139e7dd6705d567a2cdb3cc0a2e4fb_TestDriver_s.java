 package samplecode;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 public class TestDriver {
 
 	private final String fileName = "aoj0101.dat";
	private final String targetClass = aoj0101.Main.class.getName();
 	
 	@Test
 	public void test() {
 		System.out.print(test.Test.execResult(targetClass, fileName));
 		assertEquals(test.Test.readFile(fileName), test.Test.exec(targetClass, fileName));
 	}
 	
 	
 }
