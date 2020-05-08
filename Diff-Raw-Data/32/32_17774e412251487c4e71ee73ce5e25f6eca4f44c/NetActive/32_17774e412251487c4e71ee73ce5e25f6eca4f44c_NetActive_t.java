 package com.chess.genesis;
 
 import java.util.concurrent.atomic.AtomicInteger;
 
final class NetActive
 {
 	private static AtomicInteger active = new AtomicInteger(0);
 
 	private NetActive()
 	{
 	}
 
 	public static int get()
 	{
 		return active.get();
 	}
 
 	public static void inc()
 	{
 		active.incrementAndGet();
 	}
 
 	public static void dec()
 	{
 		if (active.decrementAndGet() < 1)
 			(new Thread(new NetDisconnect())).start();
 	}
 }
