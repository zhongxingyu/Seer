 package com.gmail.chamoners.chamunda;
 
 public enum Zeit {
	Dawn(22900), Day(23450), Dusk(12000), Night(13100);
 
 	private final long time;
 
 	private Zeit(final long _time) {
 		time = _time;
 	}
 
 	public long getTime() {
 		return time;
 	}
 }
