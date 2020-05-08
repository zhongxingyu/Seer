 package models;
 
 import javax.persistence.*;
 
 import play.data.validation.Max;
 import play.data.validation.Required;
 import play.db.jpa.Model;
 
 import java.sql.Timestamp;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: reyoung
  * Date: 3/17/13
  * Time: 2:27 PM
  * To change this template use File | Settings | File Templates.
  */
 @Entity
 public class AirlinePlan extends Model {
     @Column(name = "Number",nullable = false)
     @Required
     @Max(value = 255)
     public String Number;
 
     @Column(name = "LeaveTime",nullable = false)
     @Required
     public Date LeaveTime;
 
     @Required
     @Column(name = "FlyTime",nullable = false)
     public int FlyTime;
 
     @Column(name = "Repeat",nullable = false)
     @Required
     @Max(value = 255)
     public String Repeat;
 
 
     @OneToOne
     @JoinColumn(name = "Company",nullable = false)
     public AirCompany Company;
 
     @Required
     @OneToOne()
     @JoinColumn(name = "LeavePlace", nullable = false)
     public Airport LeavePlace;
 
     @Required
     @OneToOne()
     @JoinColumn(name = "ArrivePlace",nullable = false)
     public Airport ArrivePlace;
 
     @OneToMany()
     public List<Airport> StopoverPlaces;
     public String switchNumber(String cha){
     	if(cha.equals("1")){
     		cha="一";
     	}else if(cha.equals("2")){
     		cha="二";
     	}else if(cha.equals("2")){
     		cha="二";
     	}else if(cha.equals("3")){
     		cha="三";
     	}else if(cha.equals("4")){
     		cha="四";
     	}else if(cha.equals("5")){
     		cha="五";
     	}else if(cha.equals("6")){
     		cha="六";
     	}else if(cha.equals("7")){
     		cha="日";
     	}
 		return cha;
     }
 
     public String getReadableLeaveTime() {
         String strLeaveTime=null;
     	SimpleDateFormat df = new SimpleDateFormat("HH:mm");
         if(this.Repeat.subSequence(0, 1).equals("N")){
         	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
         	strLeaveTime=sdf.format(this.LeaveTime)+"起飞";
         }else if(this.Repeat.subSequence(0, 1).equals("W")){
        	if(this.Repeat.subSequence(0, 1).equals("W1234567")){
         		strLeaveTime="每天";
         	}else{
         		strLeaveTime="每周";
             	for(int i=1;i<Repeat.length();i++){
             		String cha=Repeat.substring(i,i+1);
             		strLeaveTime+=switchNumber(cha)+",";
             		}
                 strLeaveTime=strLeaveTime.subSequence(0, strLeaveTime.length()-1)+df.format(this.LeaveTime)+"起飞";
         	}
         }else if(this.Repeat.subSequence(0, 1).equals("M")){
     		strLeaveTime="每月"+Repeat.subSequence(1, Repeat.length())+"号"+df.format(this.LeaveTime)+"起飞";
         }
         return strLeaveTime;
 
         
       
     }
     public String getReadableFlyTime(){
     	
     	int day=FlyTime/1440;
     	int hour=(FlyTime/60)%24;
     	int minute=FlyTime%60;
     	String strFlyTime="飞行时间为："+day+"天"+hour+"小时"+minute+"分钟";
         return strFlyTime;
     }
     public String getReadableStopovers(){
         if (StopoverPlaces.size()==0){
             return "无" ;
         } else {
             StringBuilder sb = new StringBuilder();
             sb.append("[");
             for (Airport ap : StopoverPlaces){
                 sb.append(ap.toString());
                 sb.append(",");
             }
             sb.setCharAt(sb.length()-1,']');
             return sb.toString();
         }
     }
     public String getEditLeaveTime()  {
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
         return sdf.format(this.LeaveTime);
     }
 }
