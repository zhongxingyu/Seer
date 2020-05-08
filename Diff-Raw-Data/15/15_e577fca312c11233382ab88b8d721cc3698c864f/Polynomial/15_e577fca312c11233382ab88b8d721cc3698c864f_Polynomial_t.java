 package com.scs.security;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 public class Polynomial {
 
 	private int degree;
 	private BigInteger[] coefficients; /*
 									 * [a(1), a(2), ...., a(m)] where
 									 * polynomial is a(0)* x^0 + a(1)* x^1 + ...
 									 * + a(m) * x^m
 									 */
 	private BigInteger zerothCoefficient;
 
 	/* generate a polynomial with randomly generated co-efficients with y(0) = hpwd */
 	public static Polynomial getRandomPolynomial(int degree, BigInteger hpwd) {
 		Polynomial p = new Polynomial(degree);
 		p.setRandomCoefficients();
 		p.setZerothCoefficient(hpwd);
 		return p;
 	}
 
 	private void setRandomCoefficients() {
 		for (int i = 1; i <= degree; i++) {
 			this.coefficients[i] = new BigInteger(Generator.BIT_LENGTH, new Random());
 					// new BigInteger(new Long(new Random().nextLong() % LIMITER).toString()).abs();
 		}
 	}
 	
 	private void setZerothCoefficient(BigInteger zerothCoefficient) {
 		this.zerothCoefficient = zerothCoefficient;
 	}
 	
 	public BigInteger getZerothCoefficient() {
 		return zerothCoefficient;
 	}
 
 	public Polynomial(int degree) {
 		this.degree = degree;
 		this.coefficients = new BigInteger[degree + 1];
 	}
 
 	/* evaluate the polynomial for a given x */
 	public BigInteger evaluate(BigInteger x) {
 		BigInteger result = new BigInteger("0");
 		for (int i = 1; i <= degree; i++) {
 			result = result.add(coefficients[i].multiply(
 					x.modPow(new BigInteger(i+""), Constants.Q)).mod(Constants.Q));
 		}
 		result = result.add(zerothCoefficient).mod(Constants.Q);
 		return result;
 	}
 	
 	/* returns zeroth coefficient of polynomial from a set of (degree + 1) points on it */
 	public static BigInteger generateZerothCoefficientFromPoints(List<Point> points) {
 		BigInteger sum = new BigInteger("0");
 		BigInteger lambda_i = null;
 		
 		for(int i = 0; i < points.size(); i++) {
 			lambda_i = getLambda(points, i);
 			sum = sum.add(points.get(i).y.multiply(lambda_i).mod(Constants.Q)).mod(Constants.Q);
 		}
		
 		return sum;
 	}
 	
 	/* generates lambda values for lagrange interpolation */
 	private static BigInteger getLambda(List<Point> points, int i) {
 		BigInteger numer = new BigInteger("1");
 		BigInteger denom = new BigInteger("1");
 		
 		for(int j = 0; j < points.size(); j++) {
 			if (j != i) {
 				numer = numer.multiply(points.get(j).x).mod(Constants.Q);
 				denom = denom.multiply(points.get(j).x.subtract(points.get(i).x)).mod(Constants.Q);
 			}
 		}
 		
 		return numer.multiply(denom.modInverse(Constants.Q)).mod(Constants.Q);
 	}
 
 	public void display() {
 		System.out.print(zerothCoefficient);
 		for (int i = 1; i <= degree; i++) {
 				System.out.print("  +  " + coefficients[i] + " * x^" + i);
 		}
 		System.out.println();
 	}
 	
 	public static Point getPoint(BigInteger x, BigInteger y) {
 		Point p = new Point(x, y);
 		return p;
 	}
 
 	public static void main(String[] args) {
 		int degree = 50;
 		
 		BigInteger hpwd = new BigInteger(256, new Random()).mod(Constants.Q);
 		Polynomial p = Polynomial.getRandomPolynomial(degree, hpwd);
 		p.display();
 		
 		List<Point> points = new ArrayList<Point>(degree);
 		for (int i = 1; i <= degree+1; i++) {
 			BigInteger x = new BigInteger(Generator.BIT_LENGTH, new Random()).mod(Constants.Q);
 			BigInteger y = p.evaluate(x);
 			points.add(new Point(x, y));
 			System.out.println("( " + x + " , " + y + " )");
 		}
 		
 		BigInteger z = generateZerothCoefficientFromPoints(points);
 		System.out.println(z);
 		System.out.println(hpwd.equals(z) ? "\nGood!" : "\nBad!");
 	}
 }
 
 class Point {
 	BigInteger x;
 	BigInteger y;
 	
 	public Point(BigInteger x, BigInteger y) {
 		this.x = x;
 		this.y = y;
 	}
 }
