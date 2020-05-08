 package edu.berkeley.grippus.util;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 public class Pair<A, B> implements Serializable {
 	private static final long serialVersionUID = 1L;
 	private A car;
 	private B cdr;
 
 	public Pair(A car, B cdr) {
 		this.car = car;
 		this.cdr = cdr;
 	}
 
 	public A car() {
 		return car;
 	}
 
 	public B cdr() {
 		return cdr;
 	}
 
 	@Override
 	public String toString() {
 		return "<" + car + "," + cdr + ">";
 	}
 
 	public static <A, B> List<A> cars(Iterable<Pair<A, B>> pairs) {
 		List<A> result = new ArrayList<A>();
 		for (Pair<A, B> pair : pairs)
 			result.add(pair.car);
 		return result;
 	}
 
 	public static <A, B> List<B> cdrs(Iterable<Pair<A, B>> pairs) {
 		List<B> result = new ArrayList<B>();
 		for (Pair<A, B> pair : pairs)
 			result.add(pair.cdr);
 		return result;
 	}
 
 	public void setCar(A car) {
 		this.car = car;
 	}
 
 	public void setCdr(B cdr) {
 		this.cdr = cdr;
 	}
 
 	@Override
 	public int hashCode() {
 		return car.hashCode() ^ cdr.hashCode();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (!(obj instanceof Pair<?, ?>))
 			return false;
 		Pair<?, ?> other = (Pair<?, ?>) obj;
 		return ((other.car == null && car == null) || (other.car.equals(car)))
			&& ((other.cdr == null && cdr == null) || (other.cdr.equals(cdr)));
 	}
 }
