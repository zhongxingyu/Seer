  package com.qut.spc.calculations;
 
 import com.qut.spc.api.ElectricityCalculationApi;
 
 /**
  * @author Zuojun Chen
  *
  */
 
 public class ElectricityCalculator implements ElectricityCalculationApi{
 	
 	//from google docs
 	private static final double CORRECTION_FACTOR = 1.25;
     //private static final double PANEL_AGE_EFFICIENCY_LOSS = 0.007;
 	
 	@Override
 	public double getElectricityProduction(double dailySun,
 			double inverterEfficiency, double ageEfficiencyLoss,
 			double solarPowerOutput,double dailyHours, double timespan) throws IllegalArgumentException {
 		
 		//formula: (  estimated watt need/timespan*1.25)*(invertefficiency) 
 		//assume that capacity(solarPowerOutput) is peak electricity generation per hour
 		
 		restrictInput(dailySun,inverterEfficiency, ageEfficiencyLoss,solarPowerOutput,dailyHours,timespan);
 		int timespanYear = (int)timespan/365; //get how many timespan user want to calculate
 		
 		double electricity = 0;
 		for (int i = 0; i < (timespanYear + 1); i ++) {
			double actualSunPower = (dailySun/CORRECTION_FACTOR); //a sunlight correction factor of southern hemisphere
			double effloss=1-(ageEfficiencyLoss*i);
 			if (solarPowerOutput >= actualSunPower){
 				if (i != timespanYear) {
					electricity +=   actualSunPower*dailyHours*365*inverterEfficiency*effloss;
 				}
 				else  {
					electricity +=actualSunPower*dailyHours*(timespan - 365*i)*inverterEfficiency*effloss;
 				}
 			} else {
 				if (i != timespanYear) {
					electricity +=   solarPowerOutput*dailyHours*365*inverterEfficiency*effloss;
 				}
 				else  {
					electricity += solarPowerOutput*dailyHours*(timespan - 365*i)*inverterEfficiency*effloss;
 				}
 			}
 		}
 		return electricity;
 	}
 
 	private void restrictInput(double dailySun, double inverterEfficiency,
 			double ageEfficiencyLoss, double solarPowerOutput, double dailyHours,
 			double timespan) throws IllegalArgumentException{
 		
 		if (dailySun < 0) {
 			throw new IllegalArgumentException("Invaild dailySun input, " +
 					"the daily sun parameter shoulbe be more than zero. ");
 		}  else if (inverterEfficiency > 1 || inverterEfficiency < 0) {
 			throw new IllegalArgumentException("Invaild inverterEfficiency input, " +
 					"the inverter efficiency parameter shoulbe be between 0 and 1. ");
 		} else if (ageEfficiencyLoss > 1 || ageEfficiencyLoss < 0) {
 			throw new IllegalArgumentException("Invaild ageEfficiencyLoss input," +
 					"the solar ageEfficiencyLoss parameter shoulbe be between 0 and 1. ");
 		} else if (solarPowerOutput < 0) {
 			throw new IllegalArgumentException("Invaild solarPowerOutPut input, " +
 					"the solar power output parameter shoulbe be more than zero. ");
 		} else if (dailyHours < 0) {
 			throw new IllegalArgumentException("Invaild daily hours input, " +
 					"the dailyHours parameter should be more than zero.");
 		} else if (timespan < 0) {
 			throw new IllegalArgumentException("Invaild time  span input, " +
 					"the time span parameter should be more than zero.");
 		}
 		
 	}
 	
 	
 
 }
