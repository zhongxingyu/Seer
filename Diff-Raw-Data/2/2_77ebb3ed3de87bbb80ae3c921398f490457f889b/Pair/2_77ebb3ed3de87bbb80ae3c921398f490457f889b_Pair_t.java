 package net.slreynolds.ds.util;
 
 public class Pair<S,T> {
 	
 	private final S one;
 	private final T two;
 	
 	public Pair(S s, T t) {
 		one = s;
 		two = t;
 	}
 	
 	public S first() {
 		return one;
 	}
 
 	public T second() {
 		return two;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((one == null) ? 0 : one.hashCode());
 		result = prime * result + ((two == null) ? 0 : two.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (!(obj instanceof Pair)) {
 			return false;
 		}
		Pair<?,?> other = (Pair<?,?>) obj;
 		if (one == null) {
 			if (other.one != null) {
 				return false;
 			}
 		} else if (!one.equals(other.one)) {
 			return false;
 		}
 		if (two == null) {
 			if (other.two != null) {
 				return false;
 			}
 		} else if (!two.equals(other.two)) {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "Pair ["+ one +  two + "]";
 	}
 	
 	
 }
