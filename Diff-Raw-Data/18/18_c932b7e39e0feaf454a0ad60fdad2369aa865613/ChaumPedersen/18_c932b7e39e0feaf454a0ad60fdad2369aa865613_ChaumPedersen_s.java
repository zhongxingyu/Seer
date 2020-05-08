 package com.rau.evoting.ElGamal;
 
 import java.math.BigInteger;
 
public class ChaumPedersen {
 
 	private BigInteger p;
 	private BigInteger g;
 	private BigInteger y;
 
 	private BigInteger a;
 	private BigInteger b;
 	private String message;
 
 	private BigInteger a1;
 	private BigInteger a2;
 	private BigInteger s;
 
 	public ChaumPedersen(BigInteger p, BigInteger g, BigInteger y,
 			BigInteger a, BigInteger b, String message, BigInteger a1,
 			BigInteger a2, BigInteger s) {
 		this.a = a;
 		this.b = b;
 		this.message = message;
 	}
 
 	public BigInteger getP() {
 		return p;
 	}
 
 	public void setP(BigInteger p) {
 		this.p = p;
 	}
 
 	public BigInteger getG() {
 		return g;
 	}
 
 	public void setG(BigInteger g) {
 		this.g = g;
 	}
 
 	public BigInteger getY() {
 		return y;
 	}
 
 	public void setY(BigInteger y) {
 		this.y = y;
 	}
 
 	public BigInteger getA() {
 		return a;
 	}
 
 	public void setA(BigInteger a) {
 		this.a = a;
 	}
 
 	public BigInteger getB() {
 		return b;
 	}
 
 	public void setB(BigInteger b) {
 		this.b = b;
 	}
 
 	public String getMessage() {
 		return message;
 	}
 
 	public void setMessage(String message) {
 		this.message = message;
 	}
 
 	public BigInteger getA1() {
 		return a1;
 	}
 
 	public void setA1(BigInteger a1) {
 		this.a1 = a1;
 	}
 
 	public BigInteger getA2() {
 		return a2;
 	}
 
 	public void setA2(BigInteger a2) {
 		this.a2 = a2;
 	}
 
 	public BigInteger getS() {
 		return s;
 	}
 
 	public void setS(BigInteger s) {
 		this.s = s;
 	}
 
 }
