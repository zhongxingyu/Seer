 package com.chuckanutbay.webapp.common.shared;
 
 import static com.google.common.collect.Lists.newArrayList;
 
 import java.io.Serializable;
 import java.util.List;
 
 public class WeekReportData implements Serializable {
 	private static final long serialVersionUID = 1L;
 	public List<DayReportData> dayReportData;
 	public double hoursNormalPay = 0;
 	public double hoursOvertime = 0;
 	public double prePayPeriodHours = 0;
 	
 	public WeekReportData() {}
 
 	public List<DayReportData> getDayReportData() {
 		return dayReportData;
 	}
 
 	public void setDayReportData(List<DayReportData> dayReportData) {
 		this.dayReportData = dayReportData;
 	}
 
 	public double getHoursNormalPay() {
 		return hoursNormalPay;
 	}
 
 	public void setHoursNormalPay(double hoursNormalPay) {
 		this.hoursNormalPay = hoursNormalPay;
 	}
 
 	public double getHoursOvertime() {
 		return hoursOvertime;
 	}
 
 	public void setHoursOvertime(double hoursOvertime) {
 		this.hoursOvertime = hoursOvertime;
 	}
 	
 	public void addHours(double hours) {
 		hoursNormalPay += hours;
 		if ((hoursNormalPay + prePayPeriodHours) > 40) {
			hoursOvertime = (hoursNormalPay + prePayPeriodHours) - 40;
 			hoursNormalPay = (40 - prePayPeriodHours);
 		}
 	}
 	
 	public void addPrePayPeriodHours(double hours) {
 		prePayPeriodHours += hours;
 	}
 	
 	public void addInterval(DayReportData day) {
 		if (dayReportData == null) {
 			dayReportData = newArrayList();
 		}
 		dayReportData.add(day);
 	}
 
 	public double getPrePayPeriodHours() {
 		return prePayPeriodHours;
 	}
 
 	public void setPrePayPeriodHours(double prePayPeriodHours) {
 		this.prePayPeriodHours = prePayPeriodHours;
 	}
 	
 	
 }
