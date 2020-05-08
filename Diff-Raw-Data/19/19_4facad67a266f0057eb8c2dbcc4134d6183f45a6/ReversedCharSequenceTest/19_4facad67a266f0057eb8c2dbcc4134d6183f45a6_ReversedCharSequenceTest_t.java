 package dfh.grammar;
 
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 
 /**
  * Just to make sure {@link ReversedCharSequence} class works as intended.
  * <p>
  * <b>Creation date:</b> Apr 10, 2011
  * 
  * @author David Houghton
  * 
  */
 public class ReversedCharSequenceTest {
 
 	@Test
 	public void toStringTest() {
 		String s = "abc";
 		ReversedCharSequence rcs = new ReversedCharSequence(s, s.length());
 		assertTrue("reversed string", rcs.toString().equals("cba"));
 	}
 
 	@Test
 	public void lengthTest() {
 		String s = "abc";
 		ReversedCharSequence rcs = new ReversedCharSequence(s, s.length());
 		assertTrue("figured out length", rcs.length() == 3);
 	}
 
 	@Test
 	public void charAtTest0() {
 		String s = "abc";
 		ReversedCharSequence rcs = new ReversedCharSequence(s, s.length());
 		assertTrue("got char 0", rcs.charAt(0) == 'c');
 	}
 
 	@Test
 	public void charAtTest1() {
 		String s = "abc";
 		ReversedCharSequence rcs = new ReversedCharSequence(s, s.length());
 		assertTrue("got char 1", rcs.charAt(1) == 'b');
 	}
 
 	@Test
 	public void substringTest1() {
 		String s = "abcdefghijklmnopqrstuvwxyz";
 		ReversedCharSequence rcs = new ReversedCharSequence(s, 5);
 		assertTrue("reversed string", rcs.toString().equals("edcba"));
 	}
 
 	@Test
 	public void substringTest2() {
 		String s = "abcdefghijklmnopqrstuvwxyz";
 		ReversedCharSequence rcs = new ReversedCharSequence(s, 5, 1);
 		assertTrue("reversed string", rcs.toString().equals("edcb"));
 	}
 
 	@Test
 	public void oneArgConstructorTest() {
 		String s = "abc";
 		ReversedCharSequence rcs = new ReversedCharSequence(s);
 		assertTrue("one arg constructor worked", "cba".equals(rcs.toString()));
 	}
 
 	@Test
 	public void subsequenceTest() {
 		String s = "abc";
 		ReversedCharSequence rcs = new ReversedCharSequence(s);
 		assertTrue("returned correct subsequence", rcs.subSequence(0, 1)
 				.toString().equals("c"));
 	}
 
 	@Test
 	public void doubleReversalTest() {
 		String s = "abc";
 		ReversedCharSequence rcs = new ReversedCharSequence(s);
 		rcs = new ReversedCharSequence(rcs);
 		assertTrue("double reversal worked", rcs.toString().equals("abc"));
 	}
 
 	@Test
 	public void translationTest() {
 		String s = "abcdefghijklmnopqrstuvwxyz";
 		ReversedCharSequence rcs = new ReversedCharSequence(s);
 		for (int i = 0; i < s.length(); i++)
 			assertTrue(s.charAt(i) == rcs.charAt(rcs.translate(i)));
 	}

	@Test
	public void subsequenceRegression() {
		String s = "1 01 001";
		String ss1 = s.subSequence(3, 8).toString();
		s = new ReversedCharSequence(s).toString();
		ReversedCharSequence rcs = new ReversedCharSequence(s);
		String ss2 = rcs.subSequence(3, 8).toString();
		assertTrue(ss1.equals(ss2));
	}
 }
