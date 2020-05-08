 package org.sireum.test.jvm.samples;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Target;
 
 import com.google.common.annotations.Beta;
 
 @Target(ElementType.FIELD)
 @interface xyz {
 }
 
 @Deprecated
 public class HelloWorld2 {
 	public static final int field = 9;
 	public int field2;
 	public Point p = new Point(1, 2);
 
 	@Beta
 	public static void main(String[] args) throws java.io.IOException,
 			java.io.FileNotFoundException {
 		System.out.println("hello");
 		int i = 1;
 		int j = 2;
 		int k = i + j;
 
 		int l = -i;
 		long l2 = i;
 
 		HelloWorld2 hw = new HelloWorld2();
 		hw.field2 = field + (new HelloWorld2()).field2
 				+ (i>j ? 2:3) + (new HelloWorld2()).field2;
 		hw.p.x = 3;
 
 		int[] arr = new int[10];
 		l++;
 
 		int adf = i + j * 3 - 4;
 
 		if (i < j) {
 			System.out.println("less than");
 		}
 		if (i == 0) {
 			if (hw != null) {
 				hw.sum(i, j);
 			}
 		}
 
 		for (int laf = 0; laf < 10; laf++) {
 			hw.sum(laf, j);
 		}
 
 		if (hw instanceof HelloWorld2) {
 			hw.sum(i, j);
 		}
 
 		Object o = "adfa";
 		String lajfa = (String)o;
 		
 		HelloWorld2[] hw2 = new HelloWorld2[10];
		i = hw2.length;
 		try {
 			hw2[9].field2 = 2;
 		} catch (ArithmeticException ae) {
 
 		}
 
 		try {
 			i = 2;
 		} catch (ArithmeticException ae) {
 
 		}
 
 		switch (i) {
 		case 1:
 			i = 2;
 		case 200:
 			i = 3;
 		case 3000:
 			i = 4;
 		}
 
 		switch (i) {
 		case 1:
 			return;
 		case 2:
 			return;
 		case 3:
 			return;
 		default:
 			return;
 		}
 
 		// int [][] asakf = new int[10][10];
 	}
 
 	public int sum(int i, int j) {
 		return i + j;
 	}
 
 	class Point {
 		int x;
 		int y;
 
 		Point(int x, int y) {
 			this.x = x;
 			this.y = y;
 		}
 
 		void print() {
 			System.out.println(HelloWorld2.this.field);
 		}
 	}
 
 }
