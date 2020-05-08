 package com.globalresolvewws;
 
 
 /**
 * The riskmodel currently scores risk on a 1-10 scale with a default of 5.
  * 
  * @author Caleb
  * 
  */
 
 public class RiskModel {
 	
 	private int currentRisk;
 	private int adders;
 	private int subtractors;
 	
 	
 	
 public int getCurrentRisk() {
 	return currentRisk;
 }
 
 public void setCurrentRisk(int currentRisk) {
 	this.currentRisk = currentRisk;
 }
 
 public int getAdders() {
 	return adders;
 }
 
 public void setAdders(int adders) {
 	this.adders = adders;
 }
 
 public int getSubtractors() {
 	return subtractors;
 }
 
 public void setSubtractors(int subtractors) {
 	this.subtractors = subtractors;
 }
 
 public int getRisk()
 {
 	return currentRisk + adders - subtractors;
 }
 
 public void calculateRisk(int alarmHigh, int alarmLow, int alarmHumid, int dayAdder)
 {
 	WeatherService WS = new WeatherService();
 	Weather dayToConsider = WS.Weather().get(dayAdder);
 	if(dayToConsider.getMaxTemp() > alarmHigh)
 	{
 		adders++;
 	}
 	if(dayToConsider.getMinTemp() < alarmLow)
 	{
 		adders++;
 	}
 	if(dayToConsider.getHumidity() > alarmHumid)
 	{
 		adders++;
 	}
 }
 
 public void setBaseRisk()
 {
 	currentRisk = 5;
 	adders = 0;
 	subtractors = 0;
 }
 
 public alarmEnum checkForAlarm()
 {
 	alarmEnum alarmStatus;
 	if(currentRisk < 5)
 		alarmStatus = alarmEnum.LOW;
 	else if(currentRisk < 7)
 		alarmStatus = alarmEnum.MEDIUM;
 	else
 		alarmStatus = alarmEnum.HIGH;
 	return alarmStatus;
 }
 
 }
