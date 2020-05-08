 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 /**
  * Tests {@link Interval}.
  */
 public class IntervalTests {
 
 	@Test
 	public void testConstructor() {
 		Interval interval = Interval.of(2, 3);
 		assertInterval(2, 3, interval);
 		assertFalse(interval.isBottom());
 		assertFalse(interval.isTop());
 
 		Interval singletonInterval = Interval.of(4);
 		assertInterval(4, 4, singletonInterval);
 		assertFalse(singletonInterval.isBottom());
 		assertFalse(singletonInterval.isTop());
 
 		assertTrue(Interval.TOP.isTop());
 		assertFalse(Interval.TOP.isBottom());
 
 		assertFalse(Interval.BOTTOM.isTop());
 		assertTrue(Interval.BOTTOM.isBottom());
 
 		Interval someBottom = Interval.of(4, 2);
 		assertTrue(someBottom.isBottom());
 		assertTrue(someBottom.equals(Interval.BOTTOM));
 		assertTrue(someBottom.hashCode() == Interval.BOTTOM.hashCode());
 	}
 
 	@Test
 	public void testPlus() {
 		Interval interval;
 		Interval otherInterval;
 
 		interval = Interval.of(2, 4);
 		otherInterval = Interval.of(6, 8);
 		assertInterval(8, 12, Interval.plus(interval, otherInterval));
 		assertInterval(8, 12, Interval.plus(otherInterval, interval));
 
 		assertTrue(Interval.plus(interval, Interval.BOTTOM).isBottom());
 		assertTrue(Interval.plus(Interval.BOTTOM, interval).isBottom());
 
 		interval = Interval.of(Integer.MAX_VALUE);
 		otherInterval = Interval.of(1, 2);
 		assertInterval(Integer.MIN_VALUE, Integer.MIN_VALUE + 1,
 				Interval.plus(interval, otherInterval));
 		assertInterval(Integer.MIN_VALUE, Integer.MIN_VALUE + 1,
 				Interval.plus(otherInterval, interval));
 
 		interval = Interval.of(Integer.MAX_VALUE);
 		otherInterval = Interval.of(-1, 1);
 		assertTrue(Interval.plus(interval, otherInterval).isTop());
 		assertTrue(Interval.plus(otherInterval, interval).isTop());
 
 		interval = Interval.of(Integer.MAX_VALUE - 1, Integer.MAX_VALUE);
 		otherInterval = Interval.of(1);
 		assertTrue(Interval.plus(interval, otherInterval).isTop());
 		assertTrue(Interval.plus(otherInterval, interval).isTop());
 
 		interval = Interval.of(Integer.MIN_VALUE);
 		otherInterval = Interval.of(-2, -1);
 		assertInterval(Integer.MAX_VALUE - 1, Integer.MAX_VALUE,
 				Interval.plus(interval, otherInterval));
 		assertInterval(Integer.MAX_VALUE - 1, Integer.MAX_VALUE,
 				Interval.plus(otherInterval, interval));
 
 		interval = Interval.of(Integer.MIN_VALUE);
 		otherInterval = Interval.of(-1, 1);
 		assertTrue(Interval.plus(interval, otherInterval).isTop());
 		assertTrue(Interval.plus(otherInterval, interval).isTop());
 
 		interval = Interval.of(Integer.MIN_VALUE, Integer.MIN_VALUE + 1);
 		otherInterval = Interval.of(-1);
 		assertTrue(Interval.plus(interval, otherInterval).isTop());
 		assertTrue(Interval.plus(otherInterval, interval).isTop());
 
 		interval = Interval.of(Integer.MIN_VALUE);
 		otherInterval = Interval.of(Integer.MAX_VALUE);
 		assertInterval(-1, Interval.plus(interval, otherInterval));
 
 		interval = Interval.TOP;
 		otherInterval = Interval.TOP;
 		assertEquals(Interval.TOP, Interval.plus(interval, otherInterval));
 	}
 
 	@Test
 	public void testSub() {
 		Interval interval;
 		Interval otherInterval;
 
 		interval = Interval.of(2, 4);
 		otherInterval = Interval.of(6, 8);
 		assertInterval(-6, -2, Interval.sub(interval, otherInterval));
 		assertInterval(2, 6, Interval.sub(otherInterval, interval));
 
 		assertTrue(Interval.sub(interval, Interval.BOTTOM).isBottom());
 		assertTrue(Interval.sub(Interval.BOTTOM, interval).isBottom());
 
 		interval = Interval.of(Integer.MIN_VALUE);
 		otherInterval = Interval.of(1, 2);
 		assertInterval(Integer.MAX_VALUE - 1, Integer.MAX_VALUE,
 				Interval.sub(interval, otherInterval));
 		assertInterval(Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2,
 				Interval.sub(otherInterval, interval));
 
 		interval = Interval.of(Integer.MIN_VALUE);
 		otherInterval = Interval.of(-1, 1);
 		assertTrue(Interval.sub(interval, otherInterval).isTop());
 		assertTrue(Interval.sub(otherInterval, interval).isTop());
 
 		interval = Interval.of(Integer.MIN_VALUE, Integer.MIN_VALUE + 1);
 		otherInterval = Interval.of(1);
 		assertTrue(Interval.sub(interval, otherInterval).isTop());
 		assertInterval(Integer.MIN_VALUE, Integer.MIN_VALUE + 1,
 				Interval.sub(otherInterval, interval));
 
 		interval = Interval.of(Integer.MAX_VALUE);
 		otherInterval = Interval.of(-2, -1);
 		assertInterval(Integer.MIN_VALUE, Integer.MIN_VALUE + 1,
 				Interval.sub(interval, otherInterval));
 		assertTrue(Interval.sub(otherInterval, interval).isTop());
 
 		interval = Interval.of(Integer.MAX_VALUE);
 		otherInterval = Interval.of(-1, 1);
 		assertTrue(Interval.sub(interval, otherInterval).isTop());
 		assertInterval(Integer.MIN_VALUE, Integer.MIN_VALUE + 2,
 				Interval.sub(otherInterval, interval));
 
 		interval = Interval.of(Integer.MAX_VALUE + 1, Integer.MAX_VALUE);
 		otherInterval = Interval.of(-1);
 		assertTrue(Interval.sub(interval, otherInterval).isTop());
 		assertTrue(Interval.sub(otherInterval, interval).isTop());
 
 		interval = Interval.of(Integer.MAX_VALUE);
 		otherInterval = Interval.of(Integer.MIN_VALUE);
 		assertInterval(-1, Interval.sub(interval, otherInterval));
 	}
 
 	@Test
 	public void testNeg() {
 		Interval interval;
 
 		interval = Interval.of(2, 4);
 		assertInterval(-4, -2, Interval.neg(interval));
 
 		interval = Interval.of(-2, 4);
 		assertInterval(-4, 2, Interval.neg(interval));
 
 		interval = Interval.of(-4, -2);
 		assertInterval(2, 4, Interval.neg(interval));
 
 		interval = Interval.of(Integer.MAX_VALUE);
 		assertInterval(Integer.MIN_VALUE + 1, Interval.neg(interval));
 
 		interval = Interval.of(Integer.MIN_VALUE + 1);
 		assertInterval(Integer.MAX_VALUE, Interval.neg(interval));
 
 		interval = Interval.of(Integer.MIN_VALUE);
 		assertInterval(Integer.MIN_VALUE, Interval.neg(interval));
 
 		interval = Interval.of(Integer.MIN_VALUE, Integer.MIN_VALUE + 1);
 		assertTrue(Interval.neg(interval).isTop());
 	}
 
 	@Test
 	public void testMul() {
 		Interval leftInterval;
 		Interval rightInterval;
 
 		leftInterval = Interval.of(2, 4);
 		rightInterval = Interval.of(3, 5);
 		assertInterval(6, 20, Interval.mul(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(-2, 4);
 		rightInterval = Interval.of(3, 5);
 		assertInterval(-10, 20, Interval.mul(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(-4, 2);
 		rightInterval = Interval.of(3, 5);
 		assertInterval(-20, 10, Interval.mul(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(-2, 4);
 		rightInterval = Interval.of(-3, 5);
 		assertInterval(-12, 20, Interval.mul(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(-2, 4);
 		rightInterval = Interval.of(-5, -3);
 		assertInterval(-20, 10, Interval.mul(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(-4, -2);
 		rightInterval = Interval.of(-5, -3);
 		assertInterval(6, 20, Interval.mul(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(Integer.MAX_VALUE);
 		rightInterval = Interval.of(2);
 		assertInterval(-2, Interval.mul(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(Integer.MAX_VALUE);
 		rightInterval = Interval.of(-1);
 		assertInterval(Integer.MIN_VALUE + 1,
 				Interval.mul(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(Integer.MAX_VALUE);
 		rightInterval = Interval.of(-1, 1);
 		assertInterval(Integer.MIN_VALUE + 1, Integer.MAX_VALUE,
 				Interval.mul(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(Integer.MAX_VALUE);
 		rightInterval = Interval.of(1, 2);
		assertTrue(Interval.mul(leftInterval, rightInterval).isTop());
 
 		leftInterval = Interval.of(Integer.MIN_VALUE);
 		rightInterval = Interval.of(-1);
 		assertInterval(Integer.MIN_VALUE,
 				Interval.mul(leftInterval, rightInterval));
 	}
 
 	@Test
 	public void testShl() {
 		Interval leftInterval;
 		Interval rightInterval;
 
 		leftInterval = Interval.of(2, 4);
 		rightInterval = Interval.of(0, 2);
 		assertInterval(2, 16, Interval.shl(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(-2, 4);
 		rightInterval = Interval.of(1, 2);
 		assertInterval(-8, 16, Interval.shl(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(3, 3);
 		rightInterval = Interval.of(-2, -2);
 		assertInterval(3 << -2, 3 << -2,
 				Interval.shl(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(3, 6);
 		rightInterval = Interval.of(-2, -2);
 		assertIsTop(Interval.shl(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(3, 4);
 		rightInterval = Interval.of(-2, -2);
 		assertIsTop(Interval.shl(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(-3, 3);
 		rightInterval = Interval.of(-2, -2);
 		assertIsTop(Interval.shl(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(3, 3);
 		rightInterval = Interval.of(-1, Integer.MAX_VALUE);
 		assertIsTop(Interval.shl(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(-3, 4);
 		rightInterval = Interval.of(-2, 2);
 		assertIsTop(Interval.shl(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(3, 3);
 		rightInterval = Interval.of(31, 31);
 		assertInterval(3 << 31, 3 << 31,
 				Interval.shl(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(1024, 1024);
 		rightInterval = Interval.of(15, 48);
 		assertIsTop(Interval.shl(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(1024, 1024);
 		rightInterval = Interval.of(15, 49);
 		assertIsTop(Interval.shl(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(1024, 1024);
 		rightInterval = Interval.of(31, 34);
 		assertInterval(0, 4096, Interval.shl(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(1024, 1024);
 		rightInterval = Interval.of(30, 48);
 		assertIsTop(Interval.shl(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(1024, 1024);
 		rightInterval = Interval.of(15, 30);
 		assertIsTop(Interval.shl(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(3, 3);
 		rightInterval = Interval.of(29, 31);
 		assertIsTop(Interval.shl(leftInterval, rightInterval));
 	}
 
 	@Test
 	public void testShr() {
 		Interval leftInterval;
 		Interval rightInterval;
 
 		leftInterval = Interval.of(8, 8);
 		rightInterval = Interval.of(2, 2);
 		assertInterval(2, 2, Interval.shr(leftInterval, rightInterval));
 
 		// TODO: Add more tests
 	}
 
 	@Test
 	public void testUshr() {
 		Interval leftInterval;
 		Interval rightInterval;
 
 		leftInterval = Interval.of(8, 8);
 		rightInterval = Interval.of(2, 2);
 		assertInterval(2, 2, Interval.ushr(leftInterval, rightInterval));
 
 		// TODO: Add more tests
 	}
 
 	@Test
 	public void testAnd() {
 		Interval leftInterval;
 		Interval rightInterval;
 
 		leftInterval = Interval.of(5, 5);
 		rightInterval = Interval.of(3, 3);
 		assertInterval(1, 1, Interval.and(leftInterval, rightInterval));
 
 		// TODO: Add more tests
 	}
 
 	@Test
 	public void testOr() {
 		Interval leftInterval;
 		Interval rightInterval;
 
 		for (int i = 0; i < 8; i++) {
 			leftInterval = Interval.of(0, i);
 			rightInterval = Interval.of(0, 0);
 			assertInterval(0, i, Interval.or(leftInterval, rightInterval));
 		}
 	}
 
 	@Test
 	public void testXor() {
 		Interval leftInterval;
 		Interval rightInterval;
 
 		leftInterval = Interval.of(5, 5);
 		rightInterval = Interval.of(3, 3);
 		assertInterval(6, 6, Interval.xor(leftInterval, rightInterval));
 
 		// TODO: Add more tests
 	}
 
 	@Test
 	public void testLt() {
 		Interval leftInterval;
 		Interval rightInterval;
 
 		leftInterval = Interval.of(2, 4);
 		rightInterval = Interval.of(6, 8);
 		assertInterval(2, 4, Interval.lt(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(2, 4);
 		rightInterval = Interval.of(4, 6);
 		assertInterval(2, 4, Interval.lt(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(2, 4);
 		rightInterval = Interval.of(3, 5);
 		assertInterval(2, 4, Interval.lt(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(2, 4);
 		rightInterval = Interval.of(1, 3);
 		assertInterval(2, 2, Interval.lt(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(2, 4);
 		rightInterval = Interval.of(0, 2);
 		assertTrue(Interval.lt(leftInterval, rightInterval).isBottom());
 
 		leftInterval = Interval.of(2, 4);
 		rightInterval = Interval.of(Integer.MIN_VALUE);
 		assertTrue(Interval.lt(leftInterval, rightInterval).isBottom());
 	}
 
 	@Test
 	public void testLe() {
 		Interval leftInterval;
 		Interval rightInterval;
 
 		leftInterval = Interval.of(2, 4);
 		rightInterval = Interval.of(6, 8);
 		assertInterval(2, 4, Interval.le(leftInterval, rightInterval));
 
 		assertTrue(Interval.le(leftInterval, Interval.BOTTOM).isBottom());
 		assertTrue(Interval.le(Interval.BOTTOM, leftInterval).isBottom());
 
 		leftInterval = Interval.of(2, 4);
 		rightInterval = Interval.of(4, 6);
 		assertInterval(2, 4, Interval.le(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(2, 4);
 		rightInterval = Interval.of(3, 5);
 		assertInterval(2, 4, Interval.le(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(2, 4);
 		rightInterval = Interval.of(1, 3);
 		assertInterval(2, 3, Interval.le(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(2, 4);
 		rightInterval = Interval.of(0, 2);
 		assertInterval(2, Interval.le(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(Integer.MIN_VALUE);
 		rightInterval = Interval.of(Integer.MIN_VALUE);
 		assertInterval(Integer.MIN_VALUE,
 				Interval.le(leftInterval, rightInterval));
 	}
 
 	@Test
 	public void testEq() {
 		Interval leftInterval;
 		Interval rightInterval;
 
 		leftInterval = Interval.of(2, 4);
 		rightInterval = Interval.of(2, 4);
 		assertInterval(2, 4, Interval.eq(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(2, 4);
 		rightInterval = Interval.of(4, 6);
 		assertInterval(4, Interval.eq(leftInterval, rightInterval));
 		assertInterval(4, Interval.eq(rightInterval, leftInterval));
 
 		leftInterval = Interval.of(2, 4);
 		rightInterval = Interval.of(5, 7);
 		assertTrue(Interval.eq(leftInterval, rightInterval).isBottom());
 		assertTrue(Interval.eq(rightInterval, leftInterval).isBottom());
 
 		assertTrue(Interval.eq(leftInterval, Interval.BOTTOM).isBottom());
 		assertTrue(Interval.eq(Interval.BOTTOM, leftInterval).isBottom());
 	}
 
 	@Test
 	public void testNe() {
 		Interval leftInterval;
 		Interval rightInterval;
 
 		leftInterval = Interval.of(2, 4);
 		rightInterval = Interval.of(2, 4);
 		assertInterval(2, 4, Interval.ne(leftInterval, rightInterval));
 
 		assertTrue(Interval.ne(leftInterval, Interval.BOTTOM).isBottom());
 		assertTrue(Interval.ne(Interval.BOTTOM, leftInterval).isBottom());
 
 		leftInterval = Interval.of(2, 2);
 		rightInterval = Interval.of(2, 2);
 		assertTrue(Interval.ne(leftInterval, rightInterval).isBottom());
 
 		leftInterval = Interval.of(2, 4);
 		rightInterval = Interval.of(1, 1);
 		assertInterval(2, 4, Interval.ne(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(2, 4);
 		rightInterval = Interval.of(2, 2);
 		assertInterval(3, 4, Interval.ne(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(2, 4);
 		rightInterval = Interval.of(3, 3);
 		assertInterval(2, 4, Interval.ne(leftInterval, rightInterval));
 		rightInterval = Interval.of(4, 4);
 		leftInterval = Interval.of(2, 4);
 
 		assertInterval(2, 3, Interval.ne(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(2, 4);
 		rightInterval = Interval.of(5, 5);
 		assertInterval(2, 4, Interval.ne(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(4, 4);
 		rightInterval = Interval.of(2, 4);
 		assertTrue(Interval.ne(leftInterval, rightInterval).isBottom());
 
 		leftInterval = Interval.of(5, 5);
 		rightInterval = Interval.of(2, 4);
 		assertInterval(5, 5, Interval.ne(leftInterval, rightInterval));
 	}
 
 	@Test
 	public void testGe() {
 		Interval leftInterval;
 		Interval rightInterval;
 
 		leftInterval = Interval.of(6, 8);
 		rightInterval = Interval.of(2, 4);
 		assertInterval(6, 8, Interval.ge(leftInterval, rightInterval));
 
 		assertTrue(Interval.ge(leftInterval, Interval.BOTTOM).isBottom());
 		assertTrue(Interval.ge(Interval.BOTTOM, leftInterval).isBottom());
 
 		leftInterval = Interval.of(4, 6);
 		rightInterval = Interval.of(2, 4);
 		assertInterval(4, 6, Interval.ge(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(3, 5);
 		rightInterval = Interval.of(2, 4);
 		assertInterval(3, 5, Interval.ge(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(1, 3);
 		rightInterval = Interval.of(2, 4);
 		assertInterval(2, 3, Interval.ge(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(0, 2);
 		rightInterval = Interval.of(2, 4);
 		assertInterval(2, Interval.ge(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(Integer.MIN_VALUE);
 		rightInterval = Interval.of(Integer.MIN_VALUE);
 		assertInterval(Integer.MIN_VALUE,
 				Interval.ge(leftInterval, rightInterval));
 	}
 
 	@Test
 	public void testGt() {
 		Interval leftInterval;
 		Interval rightInterval;
 
 		leftInterval = Interval.of(6, 8);
 		rightInterval = Interval.of(2, 4);
 		assertInterval(6, 8, Interval.gt(leftInterval, rightInterval));
 
 		assertTrue(Interval.gt(leftInterval, Interval.BOTTOM).isBottom());
 		assertTrue(Interval.gt(Interval.BOTTOM, leftInterval).isBottom());
 
 		leftInterval = Interval.of(4, 6);
 		rightInterval = Interval.of(2, 4);
 		assertInterval(4, 6, Interval.gt(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(3, 5);
 		rightInterval = Interval.of(2, 4);
 		assertInterval(3, 5, Interval.gt(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(1, 3);
 		rightInterval = Interval.of(2, 4);
 		assertInterval(3, Interval.gt(leftInterval, rightInterval));
 
 		leftInterval = Interval.of(0, 2);
 		rightInterval = Interval.of(2, 4);
 		assertTrue(Interval.gt(leftInterval, rightInterval).isBottom());
 
 		leftInterval = Interval.of(Integer.MAX_VALUE);
 		rightInterval = Interval.of(Integer.MAX_VALUE);
 		assertTrue(Interval.gt(leftInterval, rightInterval).isBottom());
 	}
 
 	protected void assertInterval(int expectedLower, int expectedUpper,
 			Interval interval) {
 		assertEquals(expectedLower, interval.getLower());
 		assertEquals(expectedUpper, interval.getUpper());
 	}
 
 	protected void assertInterval(int expectedValue, Interval interval) {
 		assertEquals(expectedValue, interval.getLower());
 		assertEquals(expectedValue, interval.getUpper());
 	}
 
 	protected void assertIsTop(Interval interval) {
 		assertEquals(Interval.TOP, interval);
 	}
 
 }
