 package eu.neq.mais.domain.gnuhealth;
 
 import java.util.Date;
 
 public class DateGnu {
 
 	private Integer month;
 	private Integer day;
 	private Integer year;
 	
 	
 	public Integer getMonth() {
 		return month;
 	}
 	public void setMonth(Integer month) {
 		this.month = month;
 	}
 	public Integer getDay() {
 		return day;
 	}
 	public void setDay(Integer day) {
 		this.day = day;
 	}
 	public Integer getYear() {
 		return year;
 	}
 	public void setYear(Integer year) {
 		this.year = year;
 	}	
 	
     public boolean latest(DateGnu dg){
     	if(this.year > dg.getYear()){
     		return true;
     	}
     	if(this.year == dg.getYear()){
     		if(this.getMonth()>dg.getMonth()){
     			return true;
     		}
     		if(this.getMonth() == dg.getMonth()){
     			if(this.getDay() > dg.getDay()){
     				return true;
     			}else{
     				return false;
     			}
     		}else{
     			return false;
     		}
     	}else{
     		return false;
     	}
     }
     
     public long getTimeInMillis() {
     	Date d = new Date();
     	d.setDate(getDay());
     	d.setYear(getYear()-1900);
     	d.setMonth(getMonth());
 
     	return d.getTime();	
     }
 
     	
 }
