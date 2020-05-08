 package org.suite.node;
 
 public class Int extends Node {
 
 	private int number;
 
 	private static final int poolLo = -256;
 	private static final int poolHi = 256;
 	private static final Int pool[] = new Int[poolHi - poolLo];
 
 	private Int(int number) {
 		this.number = number;
 	}
 
 	@Override
 	public int hashCode() {
 		return number;
 	}
 
 	@Override
 	public boolean equals(Object object) {
 		if (object instanceof Int) {
 			Int i = (Int) object;
 			return number == i.number;
 		} else
 			return false;
 	}
 
 	public static Int create(int i) {
 		Int ret;
		if (poolLo <= i && i < poolHi) {
 			int index = i - poolLo;
 			ret = pool[index];
 			if (ret == null)
 				ret = pool[index] = new Int(i);
 		} else
 			ret = new Int(i);
 		return ret;
 	}
 
 	public int getNumber() {
 		return number;
 	}
 
 }
