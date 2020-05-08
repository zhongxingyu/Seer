 package com.example.lifestylemotivator.models;
 
 
 
 public class LMActivity implements Cloneable {
 public String value;
 
 	
 	public boolean isTempratureReqd = false;
 	public float meanTemp = 0;
 	public float stdDevTemp = 1;
 	
 	public boolean isWindSpeedReqd = false;
 	public float    meanWindSpeed = 0;
 	public float    stdDevWindSpeed = 1;
 	
 	public boolean isHumidityReqd = false;
 	public float meanHumidity = 0;
 	public float stdDevHumidity = 1;
 	
 	public boolean isFacilityReqd = false;
 	public String facilityType;
 	
 	public boolean isPhysicalLevelReqd = false;
 	public boolean isActive = false;
 	
 	public boolean moodEnhancing = false;
 	
 	public boolean isBuddyReqd = false;
 	public String buddy;
 	
 
 	
 	
 	/**
 	 * Check whether the activity has the resources it needs.
 	 */
 	public boolean isDoable(LMCurrentCtxt ctxt) {
 		if(isBuddyReqd && !ctxt.isBuddyValid)
 			return false;
 		if(isFacilityReqd && !ctxt.isFacilityValid)
 			return false;
 		if(isHumidityReqd && !ctxt.isHumidityValid)
 			return false;
 		if(isPhysicalLevelReqd && (!ctxt.isPhysicalLevelValid | !ctxt.isActive))
 			return false;
 		if(isTempratureReqd && !ctxt.isTempratureValid)
 			return false;
 		if(isWindSpeedReqd && ! ctxt.isWindSpeedValid)
 			return false;
 		
 		return true;
 	}
 	/**
 	 *  Compute the score of the activity. Higher score means more spread from the mean
 	 *  and so more undesirable. 
 	 * @param activity
 	 * @param ctxt
 	 * @return Standard score of the activity
 	 */
 	public int getStdScore(LMCurrentCtxt ctxt) {
 		float score = 0;
 		
 		if(ctxt.isHumidityValid) {
 			score += Math.abs((ctxt.humidity - meanHumidity)/ stdDevHumidity);
 		}
 		if(ctxt.isTempratureValid) {
 			score += Math.abs((ctxt.temprature - meanTemp) / stdDevTemp);
 		}
 		if(ctxt.isWindSpeedValid) {
 			score += Math.abs((ctxt.windSpeed - meanWindSpeed) / stdDevWindSpeed);
 		}
 		
 		return (int)score;
 	}
 	/**
 	 * Cusotmize the mesage using the current context.
 	 * @param msg
 	 * @param ctxt
 	 * @return
 	 */
 	public void cutomizeMsg(LMCurrentCtxt ctxt) {
 		String rslt = this.value;
 	
 		if(ctxt.isFacilityValid) {
 			rslt = rslt.replaceAll("\\$facilityName", ctxt.facilityName);
 		}
 		if(ctxt.isBuddyValid) {
 			rslt = rslt.replaceAll("\\$buddyName", ctxt.buddyName);
 		}
 		
 		this.value = rslt;
 	}
 	public Object clone() throws CloneNotSupportedException {
 		return super.clone();
 	}
 	
	public String getValue() {
		return value;
	}
 	public String toString() {
 		StringBuilder result = new StringBuilder();
 		 String NEW_LINE = System.getProperty("line.separator");
 		 result.append(" value: " + this.value + NEW_LINE);
 		 
 		 result.append(" isTempratureRequired:" + this.isTempratureReqd + NEW_LINE);
 		 result.append(" meanTemp: " + this.meanTemp + NEW_LINE);
 		 result.append(" stdDevTemp: " + this.stdDevTemp + NEW_LINE	);
 		 
 		 result.append(" isWindSpeedReqd:" + this.isWindSpeedReqd + NEW_LINE);
 		 result.append(" meanWindspeed: " + this.meanWindSpeed + NEW_LINE);
 		 result.append(" stdDevWindSpeed: " + this.stdDevWindSpeed + NEW_LINE	);
 		 
 		 result.append(" isHumidityReqd:" + this.isHumidityReqd + NEW_LINE);
 		 result.append(" meanHumidity: " + this.meanHumidity + NEW_LINE);
 		 result.append(" stdDevHumidity: " + this.stdDevHumidity + NEW_LINE	);
 		 
 		 result.append(" isFacilityReqd:" + this.isFacilityReqd + NEW_LINE);
 		 
 		 result.append(" isPhysicalLevelReqd:" + this.isPhysicalLevelReqd + NEW_LINE);
 		 result.append(" isActive:" + this.isActive + NEW_LINE);
 		 
 		 result.append(" moodEnhancing:" + this.moodEnhancing + NEW_LINE);
 		 
 		 result.append(" isBuddyReqd:" + this.isBuddyReqd + NEW_LINE);
 		 return result.toString();
 	}
 }
