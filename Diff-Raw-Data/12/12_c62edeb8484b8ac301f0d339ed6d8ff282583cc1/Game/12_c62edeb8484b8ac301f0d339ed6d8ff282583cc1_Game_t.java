 package edu.wm.werewolf.domain;
 
 import java.util.Date;
 
 public class Game {
 
 	private int dayNightFreq;
 	private Date createdDate;
 	private boolean isRunning;
 	private long timer;
 	private int millisecondsInAMin = 60000;
 	
 	public Game(int dayNightFreq) {
 		super();
 		this.dayNightFreq = dayNightFreq * millisecondsInAMin;
 		this.createdDate = new Date();
 		this.isRunning = true;
		this.timer = 0;
 	}
 	public int getDayNightFreq() {
 		return dayNightFreq;
 	}
 	public void setDayNightFreq(int dayNightFreq) {
 		this.dayNightFreq = dayNightFreq;
 	}
 	public Date getCreatedDate() {
 		return createdDate;
 	}
 	public void setCreatedDate(Date createdDate) {
 		this.createdDate = createdDate;
 	}
 	public boolean getIsRunning() {
 		return isRunning;
 	}
 	public void setIsRunning(boolean isRunning) {
 		this.isRunning = isRunning;
 	}
 	public long getTimer() {
 		return timer;
 	}
 	public void setTimer(long timer) {
 		this.timer = timer;
 	}
 	public boolean isNight() { 
 		Date currentDate = new Date();
 		long timeElapsed = currentDate.getTime() - this.createdDate.getTime();
 		long timeOfDay = (timeElapsed/this.dayNightFreq)%2;
 		System.out.println(this.createdDate + " curr " + currentDate + " time " + timeElapsed);
 		if (timeOfDay == 0) {
 			return false;
 		}
 		return true;
 	}
 }
