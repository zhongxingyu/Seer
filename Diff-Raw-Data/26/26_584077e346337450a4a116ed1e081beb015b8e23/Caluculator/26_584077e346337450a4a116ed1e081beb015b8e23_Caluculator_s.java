 package ch09.ex04;
 
 public class Caluculator {
 	public static void main(String[] args) {
 		int i = 3;
		System.out.println(3 << 2L - 1);
		System.out.println((3L << 2) - 1);
		System.out.println(10 < 12 == 6 > 17);
		System.out.println(10 << 12 == 6 >> 17);
		System.out.println(13.5e-1 % Float.POSITIVE_INFINITY);
		System.out.println(Float.POSITIVE_INFINITY + Double.NEGATIVE_INFINITY);
		System.out.println(Double.POSITIVE_INFINITY - Float.NEGATIVE_INFINITY);
		System.out.println(0.0 / -0.0 == -0.0 / 0.0);
		System.out.println(Integer.MAX_VALUE + Integer.MIN_VALUE);
		System.out.println(Long.MAX_VALUE + 5);
		System.out.println((short) 5 * (byte) 10);
		System.out.println((i < 15 ? 1.72e3f : 0));
		System.out.println(i++ + i++ + --i);
 	}
 }
