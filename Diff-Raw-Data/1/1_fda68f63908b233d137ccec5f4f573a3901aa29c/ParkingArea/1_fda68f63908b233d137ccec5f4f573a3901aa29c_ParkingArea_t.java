 package cn.beihangsoft.parkingsystem.model;
 
 public class ParkingArea {
 	private int freeSlots;
 	private int totalSlots;
 
 	public ParkingArea(int totalSlots) {
 		this.totalSlots = totalSlots;
		this.freeSlots = totalSlots;
 	}
 
 	public int getSlotsNum() {
 		return freeSlots;
 	}
 
 	public void setSlotsNum(int slotsNum) {
 		this.freeSlots = slotsNum;
 	}
 
 	public int getTotalSlots() {
 		return totalSlots;
 	}
 
 	public void setTotalSlots(int totalSlots) {
 		this.totalSlots = totalSlots;
 	}
 }
