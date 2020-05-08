 package com.tesserate.jackpot;
 
 public class StructJackpot {
 	
 	public static int delay = 5000;
 	public static int countdown = 0;
 	public static int nextBall = 5;
 	public static int cueBall = 0;
 	public static int prizes = 1;
 	public static int players = 0;
 	public static int remaining = 0;
 	
 	public static void reset() {
 		delay = 5000;
 		countdown = 0;
 		nextBall = 5;
 		cueBall = 0;
		prizes = 1;
 		players = 0;
 		remaining = 0;
 	}
 	
 	public static int getPrizes() {
 		return prizes;
 	}
 	
 	public static int getPlayers() {
 		return players;
 	}
 }
