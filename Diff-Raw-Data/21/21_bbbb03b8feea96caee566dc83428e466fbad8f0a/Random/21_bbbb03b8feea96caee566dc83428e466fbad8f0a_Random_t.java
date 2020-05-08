 /**
  * Copyright 2011. Adam Lock <locka99@gmail.com>
  *
  * Available as open source under the terms of LGPLv3
  */
 package com.adamlock.cards;
 
 public class Random {
 	private final java.util.Random random;
 
	public Random(java.util.Random random) {
		this.random = random;
 	}

	public Random() {
		random = new java.util.Random();
		random.setSeed(System.currentTimeMillis());
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
