 package com.inb372.solar;
 import static java.lang.Math.*;
 
 public class Calculator {
 
 	private userInput userData;
 	private int noHoursInDay = 6;
 	
 	public Calculator(userInput userData)
 	{
 		this.userData = userData;
 	}
 	
 	private double calcPanelEff()
 	{
 		double wKwConversion = 1000;
		return ((userData.getWattage()/userData.getSize())/wKwConversion);
 	}
 
 	private double[] calcSunPerHour()
 	{
 		double avgDaySun = 6.7;
 		double[] hrSun = new double[noHoursInDay];
 		for (int i = 0;i<noHoursInDay;i++)
 		{
 			hrSun[i] = avgDaySun/noHoursInDay;
 		}
 		
 		return hrSun;
 	}
 	
 	private double[] calcAngleSunPanel(double[] angSunGround)
 	{
 		double[] angSunPanel = new double[noHoursInDay];
 		double angPanelGround = userData.panelAngle();
 		for (int i = 0;i<noHoursInDay;i++)
 		{
 			angSunPanel[i] = angSunGround[i] - angPanelGround;
 		}
 		
 		return angSunPanel;
 	}
 	
 	private double[] calcHourlyInsolation(double[] angSunPanel)
 	{
 		double[] hourlyInsolation = new double[noHoursInDay];
 		for (int i = 0; i < noHoursInDay;i++)
 		{
 			hourlyInsolation[i] = sin(angSunPanel[i]);
 		}
 		
 		return hourlyInsolation;
 	}
 	
 	private double calcDailySunHit(double[] hourlyInsolation, double[] hourlySun)
 	{
 		double dailySun = 0.0;
 		for (int i =0;i<noHoursInDay;i++)
 		{
 			dailySun += hourlyInsolation[i]*hourlySun[i];
 		}
 		return dailySun;
 	}
 	
 	public double calcDailyPower()
 	{
 		return 7.0;
 	}
 	
 	public double calcDailyExcess()
 	{
 		return calcDailyPower()-userData.getdailyConsume();
 	}
 }
 
