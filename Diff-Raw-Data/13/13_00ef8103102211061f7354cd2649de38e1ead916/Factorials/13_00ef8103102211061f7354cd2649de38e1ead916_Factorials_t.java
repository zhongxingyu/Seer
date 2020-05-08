 package com.github.renszarv.algobase;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.List;
 
 public class Factorials {
 	private List<BigInteger> factorials = new ArrayList<BigInteger>();
 
 	private Factorials() {
 		factorials.add(BigInteger.ONE);
 	}
 
 	private synchronized void ensure(int size) {
 		if (factorials.size() < size) {
 			BigInteger last = factorials.get(factorials.size() - 1);
 			for (int i = factorials.size(); i < size; i++) {
 				last = last.multiply(BigInteger.valueOf(i + 1));
 				factorials.add(last);
 			}
 		}
 	}
 
 	public BigInteger nth(int index) {
 		ensure(index);
		return (index <= 0) ? BigInteger.ONE : factorials.get(index - 1);
 	}
 
 	private static Factorials instance;
 
 	public synchronized static Factorials getInstance() {
 		if (instance == null) {
 			instance = new Factorials();
 		}
 		return instance;
 	}
 }
