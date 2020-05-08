 package com.nbcedu.function.teachersignup.vo;
 
 import static org.springframework.util.CollectionUtils.isEmpty;
 
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.Transformer;
 
 
 import com.nbcedu.function.teachersignup.model.TSActivity;
 import com.nbcedu.function.teachersignup.util.Utils;
 
 public class TSActivityVO {
 	private String id;
 	private String openDate;
 	private String endDate;
 	private String name;
 	private String countDownTime;
 	
 	
 	public static class Factory{
 		
 		public static List<TSActivityVO> build(List<TSActivity> actList){
 			if(isEmpty(actList)){
 				return new LinkedList<TSActivityVO>();
 			}
 			CollectionUtils.transform(actList, new Transformer() {
 				@Override
 				public Object transform(Object obj) {
 					TSActivity origin = (TSActivity)obj;
 					TSActivityVO result = new TSActivityVO();
 					result.setOpenDate(Utils.Dates.fullDateStr(origin.getOpenDate()));
 					result.setEndDate(Utils.Dates.fullDateStr(origin.getEndDate()));
 					result.setId(origin.getId());
 					result.setName(origin.getName());
 					result.setCountDownTime(countDownTimeStr(origin.getOpenDate(),origin.getEndDate()));
 					return result;
 				}
 
 				private final long day = 1000L*60L*60L*24L;
 				private final long hour = 1000L*60L*60L;
 				private final long min = 1000L*60L;
 				private final long sec = 1000L;
 				
 				private String countDownTimeStr(Date openDate, Date endDate) {
 					
 					StringBuilder result = new StringBuilder();
 					Date now = new Date();
 					Date deadLine = openDate.after(now)?openDate:endDate;
 					long diff = Math.abs(now.getTime() - deadLine.getTime());
 					long dayDiff = diff/day;
 					result.append(
 						dayDiff>9L?dayDiff:"0" + dayDiff
 					).append("日");
 					
 					long hourDiff = (diff%day)/hour;
 					result.append(
 						hourDiff>9L?hourDiff:"0"+hourDiff
 					).append("时");
 					
 					long minDiff = (diff%hour)/min;
 					result.append(
 						minDiff>9L?minDiff:"0"+minDiff
 					).append("分");
 					
 					long secDiff = (diff%min)/sec;
 					result.append(
 						secDiff>9L?secDiff:"0"+secDiff
 					).append("秒");
 					
 					return result.toString();
 				}
 				
 			});
 			List result = actList;
 			return result;
 		}
 		
 		
 	}
 	/////////////////////////////
 	//////getters&setters///////
 	///////////////////////////
 	public String getId() {
 		return id;
 	}
 	public void setId(String id) {
 		this.id = id;
 	}
 	public String getOpenDate() {
 		return openDate;
 	}
 	public void setOpenDate(String openDate) {
 		this.openDate = openDate;
 	}
 	public String getEndDate() {
 		return endDate;
 	}
 	public void setEndDate(String endDate) {
 		this.endDate = endDate;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public String getCountDownTime() {
 		return countDownTime;
 	}
 	public void setCountDownTime(String countDownTime) {
 		this.countDownTime = countDownTime;
 	}
 	
 }
