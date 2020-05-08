 package de.gymbuetz.gsgbapp;
 
 import java.util.List;
 
 public class RepPlan{
 	public final List<RepPlanDay> Days;
 	
 	public RepPlan(List<RepPlanDay> DayList) {
 		this.Days = DayList;
 	}
 	
 	public void addDay(RepPlanDay rpd) {
 		this.Days.add(rpd);
 	}
 }
