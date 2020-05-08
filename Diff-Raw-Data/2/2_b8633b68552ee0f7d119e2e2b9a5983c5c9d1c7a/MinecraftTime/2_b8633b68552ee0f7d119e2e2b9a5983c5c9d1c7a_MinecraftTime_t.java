 package com.namarius.weathernews.utils;
 
 public class MinecraftTime {
 
 	private final long time;
 	private int faction;
 	private long main;
 	private long day;
 	private int hour;
 	private int minutes;
 	
 	public MinecraftTime(long time)
 	{
 		this.time=time;
 		update();
 	}
 	
 	private void update()
 	{
 		faction = (int) (time%1000);
 		main = time/1000;
 		day =main/24;
 		hour = (int) (main%24);
 		minutes = (faction*6)/100;
 	}
 	
 	@Override
 	public int hashCode() {
 		return (int) time;
 	}
 	
 	public String getDay()
 	{
 		return new Long(day).toString();
 	}
 	
 	public String nicePrint()
 	{
 		String day = this.day+" day";
 		String hour = this.hour+" hour";
 		String minute = this.minutes+" minute";
 		if(this.day==0)
 			day="";
 		else if(this.day!=1)
 			day+="s";
 		
 		if(this.hour==0)
 			hour="";
 		else if(this.hour!=1)
 			hour+="s";
 		
		if(this.hour!=0||(this.hour==0&&this.day!=0&&this.minutes!=0))
 			hour+=" and ";
 		
 		if(this.hour!=0 && this.day!=0)
 			day+= ", ";
 			
 		if(this.minutes!=1)
 			minute+="s";
 		
 		
 		return day+hour+minute; 
 	}
 	
 	@Override
 	public String toString() {
 		return day+":"+hour+":"+minutes;
 	}
 	
 }
