 package com.marakana.concurrency;
 
 import java.math.BigInteger;
 import java.util.Random;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 public class Fibonacci {
 
 	private static final Random random = new Random();
 
 	public static BigInteger fib(int index) {
 		switch (index) {
 		case 0:
 		case 1:
 			return BigInteger.ONE;
 		default:
 			return fib(index - 1).add(fib(index - 2));
 		}
 	}
 
	public static void main(String[] args) {
 		ExecutorService e = Executors.newFixedThreadPool(10);
 
 		final Logger l = new Logger();
 		
 		for (int i = 0; i < 10; i++) {
 			e.execute(new Runnable() {
 				public void run() {
 					while (true) {
						int input = random.nextInt(30);
 						BigInteger output = fib(input);
 						l.log(String.format("fib(%d) = %s", input, output));
 					}
 				}
 			});
 		}
 		
 		l.run();
 		e.shutdown();
 
 	}
 
 }
