 package com.nbcedu.function.schoolmaster2.action;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.util.CollectionUtils;
 
 import com.nbcedu.function.schoolmaster2.biz.SM2MasterSubBiz;
 import com.nbcedu.function.schoolmaster2.core.action.BaseAction;
 import com.nbcedu.function.schoolmaster2.utils.Utils;
 import com.nbcedu.function.schoolmaster2.vo.SubWeekSearch;
 import com.nbcedu.function.schoolmaster2.vo.SubjectWeekVo;
 
 /**
  * 本周工作汇总
  * @author xuechong
  */
 @SuppressWarnings("serial")
 public class WeekAction extends BaseAction{
 	
 	private SubWeekSearch search = new SubWeekSearch();
 	
 	private SM2MasterSubBiz subBiz;
 	
 	public String search(){
 		
 		if(search.getPublisher().size()<=0){
 			search.setPublisher(Utils.getAllManagerUids());
 		}
 		
 		Date weekStart = weekStart();
 		if(search.getUpdateDate() == null){
 			search.setStart(weekStart);
 		}
 		
 		if(search.getStatus().size()<=0){
 			search.setStatus(new String[] { "new","updated" });
 		}
 		
 		Map<String, WeekDisplayVo> result =null;
 		
		if(search.getPublisher().size()==1){//单个人
 			
			List<SubjectWeekVo> list = this.subBiz.findWeekSingle(search);
 			
 			if(!CollectionUtils.isEmpty(list)){
 				result = new HashMap<String, WeekAction.WeekDisplayVo>(list.size());
 				
 				for (SubjectWeekVo sub : list) {
 					
 					if(!result.containsKey(sub.getCreatorUid())){
 						
 						result.put(sub.getCreatorUid(), 
 								new WeekDisplayVo(sub.getCreatorUid(), 
 										sub.getCreatorName()+"的本周工作"));
 						
 					}
 					
 					result.get(sub.getCreatorUid()).getSubs().add(sub);
 					
 				}
 				
 			}
 			
		}else{
 			
			List<SubjectWeekVo> list = this.subBiz.findWeek(search);
 			
 			if(!CollectionUtils.isEmpty(list)){
 				
 				result = new HashMap<String, WeekAction.WeekDisplayVo>(list.size());
 				
 				for (SubjectWeekVo sub : list) {
 					
 					if(!result.containsKey(sub.getTypeId())){
 						result.put(sub.getTypeId(), 
 								new WeekDisplayVo(sub.getTypeId(), sub.getTypeName()));
 					}
 					
 					result.get(sub.getTypeId()).getSubs().add(sub);
					
 				}
 				
 			}
 			
 		}
 		
 		this.getRequestMap().put("view",
 				result == null ? Collections.EMPTY_MAP : result);
 		this.getRequestMap().put("weekStart", weekStart);
 		return "weekList";
 	}
 	
 	public static class WeekDisplayVo{
 		
 		private String id;
 		private String name;
 		private List<SubjectWeekVo> subs = new LinkedList<SubjectWeekVo>() ;
 		
 		public WeekDisplayVo(){}
 		public WeekDisplayVo(String id, String name) {
 			super();
 			this.id = id;
 			this.name = name;
 		}
 		
 		public String getId() {
 			return id;
 		}
 		public void setId(String id) {
 			this.id = id;
 		}
 		public String getName() {
 			return name;
 		}
 		public void setName(String name) {
 			this.name = name;
 		}
 		public List<SubjectWeekVo> getSubs() {
 			return subs;
 		}
 		public void setSubs(List<SubjectWeekVo> subs) {
 			this.subs = subs;
 		}
 		
 	}
 	
 	private static Date weekStart(){
 		
 		int curDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
 		Integer day = curDay >= 2 ? curDay - 2 : 6;// 计算需要减去几天才到周一
 		long diff = day.longValue() * 1000L * 60L * 60L * 24L;
 		Date firstDayOfWeek = new Date(new Date().getTime() - diff);
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 		Date result = null;
 		try {
 			result = sdf.parse(sdf.format(firstDayOfWeek));
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 		
 		return result;
 		
 	}
 
 	
 	
 	//////////////////////////////////
 	//////getters&setters//////
 	////////////////////////
 	public SubWeekSearch getSearch() {
 		return search;
 	}
 	public void setSearch(SubWeekSearch search) {
 		this.search = search;
 	}
 	public void setSubBiz(SM2MasterSubBiz subBiz) {
 		this.subBiz = subBiz;
 	}
 	
 	
 }
