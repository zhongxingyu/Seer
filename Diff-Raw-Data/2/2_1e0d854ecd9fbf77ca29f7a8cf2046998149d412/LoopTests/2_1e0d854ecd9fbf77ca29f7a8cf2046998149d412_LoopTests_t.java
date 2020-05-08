 import org.junit.Test;
 
 public class LoopTests extends AbstractTest {
 
 	public static void main(String[] args) {
 		testSafeLoop();
 	}
 
 	public static void testSafeLoop() {
 		int[] array = new int[8];
 		for (int i = 0; i < 8; i++) {
 			array[i] = i;
 		}
 	}
 
 	@Test
 	public void _testSafeLoop() {
 		assertSafe("testSafeLoop");
 	}
 
 	public static void testSafeLoopLength() {
 		int[] array = new int[8];
 		for (int i = 0; i < array.length; i++) {
 			array[i] = i;
 		}
 	}
 
 	@Test
 	public void _testSafeLoopLength() {
 		assertSafe("testSafeLoopLength");
 	}
 
 	public static void testUnsafeLoopLower() {
 		int[] array = new int[8];
 		for (int i = -1; i < 8; i++) {
 			array[i] = i;
 		}
 	}
 
 	@Test
 	public void _testUnsafeLoopLower() {
 		assertMaybeUnsafe("testUnsafeLoopLower");
 	}
 
 	public static void testUnsafeLoopUpperLeq() {
 		int[] array = new int[8];
 		for (int i = 0; i <= 8; i++) {
 			array[i] = i;
 		}
 	}
 
 	@Test
 	public void _testUnsafeLoopUpperLeq() {
 		assertMaybeUnsafe("testUnsafeLoopUpperLeq");
 	}
 
 	public static void testUnsafeLoopUpperLt() {
 		int[] array = new int[8];
 		for (int i = 0; i < 9; i++) {
 			array[i] = i;
 		}
 	}
 
 	@Test
 	public void _testUnsafeLoopUpperLt() {
 		assertMaybeUnsafe("testUnsafeLoopUpperLt");
 	}
 
 	public static void testUnsafeLoopLength() {
 		int[] array = new int[8];
 		for (int i = 0; i <= array.length; i++) {
 			array[i] = i;
 		}
 	}
 
 	@Test
 	public void _testUnsafeLoopLength() {
		assertMaybeUnsafe("testUnsafeLoopLength");
 	}
 
 }
