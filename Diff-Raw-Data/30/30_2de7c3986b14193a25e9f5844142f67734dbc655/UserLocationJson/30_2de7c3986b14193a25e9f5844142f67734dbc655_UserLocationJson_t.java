 package edu.depaul.se491.snotg;
 
 import java.math.BigDecimal;
 import java.util.Date;
 
 
public class UserLocationJson {
 	String userName;
 	BigDecimal latit;
 	BigDecimal longit;
 	Date lastUpdated;
 
 	public String getUserName() {
 		return userName;
 	}
 	public void setUserName(String userName) {
 		this.userName = userName;
 	}
 	public BigDecimal getLatit() {
 		return latit;
 	}
 	public void setLatit(BigDecimal latit) {
 		this.latit = latit;
 	}
 	public BigDecimal getLongit() {
 		return longit;
 	}
 	public void setLongit(BigDecimal longit) {
 		this.longit = longit;
 	}
 	public Date getLastUpdated() {
 		return lastUpdated;
 	}
 	public void setLastUpdated(Date lastUpdated) {
 		this.lastUpdated = lastUpdated;
 	}
 
 	
 }
