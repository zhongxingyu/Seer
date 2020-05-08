 package edu.csh.coursebrowser;
 
 public class Time {
 	public final String day, start, end, code, number, room, building, buildingCode;
 	
 	public Time(String day, String start, String end, String code, String number, String room, String building, String buildCode) {
 		this.day = day;
 		this.start = start;
 		this.end = end;
 		this.code = code;
 		this.number = number;
 		this.room = room;
 		this.building = building;
 		this.buildingCode = buildCode;
 	}
 	
 	public String toString() {
		return building+ " " + buildingCode+ "-" + room +": " + start + " - " + end;
 	}
 }
