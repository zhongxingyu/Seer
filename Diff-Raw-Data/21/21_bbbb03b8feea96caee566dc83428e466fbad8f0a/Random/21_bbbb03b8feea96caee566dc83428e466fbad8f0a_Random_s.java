 /**
  * Copyright 2011. Adam Lock <locka99@gmail.com>
  *
  * Available as open source under the terms of LGPLv3
  */
 package com.adamlock.cards;
 
import java.security.SecureRandom;

 public class Random {
 	private final java.util.Random random;
 
	public Random() {
		this(false);
 	}
	
	public Random(boolean useSecureRandom) {
//		if (useSecureRandom) {
//			random = new SecureRandom();
//		} else 
		{
			random = new java.util.Random();
			random.setSeed(System.currentTimeMillis());
		}
 		random.nextInt();
 	}
 
 	public int nextInt(int modulo) {
 		return random.nextInt(modulo);
 	}
 
 	public long nextLong() {
 		return random.nextLong();
 	}
 
 	public void next(byte[] bytes) {
 		random.nextBytes(bytes);
 	}
 
 	public java.util.Random getRandom() {
 		return random;
 	}
 }
