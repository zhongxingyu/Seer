 package com.nbcedu.function.schoolmaster2.action;
 
 import java.util.Date;
 
 import com.nbcedu.function.schoolmaster2.biz.SM2DataBiz;
 import com.nbcedu.function.schoolmaster2.constants.StatisticsEnum;
 import com.nbcedu.function.schoolmaster2.core.action.BaseAction;
 import com.nbcedu.function.schoolmaster2.core.util.DateUtil;
 import com.nbcedu.function.schoolmaster2.core.util.StringUtil;
 import com.nbcedu.function.schoolmaster2.data.interfaces.DataGenerator;
 import com.nbcedu.function.schoolmaster2.data.model.SM2Datas;
 import com.nbcedu.function.schoolmaster2.data.util.DataContext;
 import com.nbcedu.function.schoolmaster2.utils.Utils;
 
 @SuppressWarnings("serial")
 public class DataAction extends BaseAction{
 	
 	/**要查看 显示 的数据类型*/
 	private String dataType;
 	private String matcher;
 	private Date start;
 	private Date end;
 	private SM2Datas data = new SM2Datas();
 	private String xmlContent;
 	private SM2DataBiz sm2DataBiz;
 	private String name ;
 	
 	public String listStatistics(){
 		this.data.setStartDate(start);
 		this.data.setEndDate(end);
 		this.data.setMatcher(matcher);
 		this.pm=this.sm2DataBiz.findPageByModel(data);
 		return "listStatistics";
 	}
 	public String listMasterStatistics(){
 		this.data.setStartDate(start);
 		this.data.setEndDate(end);
 		this.data.setMatcher(matcher);
 		this.pm=this.sm2DataBiz.findPageByModel(data);
 		return "listMasterStatistics";
 	}
 	public String toChart(){
 		return "toChart";
 	}
 	public String toMasterChart(){
 		this.data = this.sm2DataBiz.findById(id);
 		return "toMasterChart";
 	}
 
 	public String chart(){
 		DataGenerator dataGen = 
 			DataContext.getContext().getbyMatcher(matcher);
 		this.dataType = dataGen.defaultChartType();
		if(Utils.isManager()){
 			this.xmlContent = dataGen.getDataByTime(start, end);
 		}else{
 			this.xmlContent = dataGen.getDataByTime(start, end,"F0F8FC");
 		}
 		return "chart";
 	}
 	
 	public String add(){
 		this.data.setLastUpdate(new Date());
 		this.data.setCreateDate(new Date());
 		this.data.setStatus(0);
 		this.data.setCreatorUid(this.getUserId());
 		this.data.setMatcher(matcher);
 		this.sm2DataBiz.add(data);
 		return "refreshTeacherList";
 	}
 	
 	public String listByType(){
 		this.pm = this.sm2DataBiz.findPageByMatcher(matcher);
 		return "listStatistics";
 	}
 	
 	public String remove(){
 		this.sm2DataBiz.removeById(this.id);
 		return "refreshTeacherList";
 	}
 	
 	public String modify(){
 		this.data.setLastUpdate(new Date());
 		this.sm2DataBiz.modify(this.data);
 		return "refreshTeacherList";
 	}
 	
 	/////////////////////////
 	/////getters&setters/////
 	/////////////////////////
 	public String getDataType() {
 		return dataType;
 	}
 	public void setDataType(String dataType) {
 		this.dataType = dataType;
 	}
 	public String getMatcher() {
 		return matcher;
 	}
 	public void setMatcher(String matcher) {
 		this.matcher = matcher;
 		if(!StringUtil.isEmpty(matcher)){
 			this.name = StatisticsEnum.getByType(matcher).name;
 		}
 	}
 	public Date getStart() {
 		return start;
 	}
 	public void setStart(Date start) {
 		if(start!=null)
 			try {
 				this.start = com.nbcedu.function.schoolmaster2.core.util.date.DateUtil.str2Date(DateUtil.format(start, DateUtil.YDM_DASH), "yyyy-MM-dd");
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		else
 			this.start = start;
 	}
 	public Date getEnd() {
 		return end;
 	}
 	public void setEnd(Date end) {
 		if(end!=null){
 			try {
 				this.end = com.nbcedu.function.schoolmaster2.core.util.date.DateUtil.str2Date(DateUtil.format(end, DateUtil.YDM_DASH), DateUtil.YDM_DASH);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}else
 		this.end = end;
 	}
 	public SM2Datas getData() {
 		return data;
 	}
 	public void setData(SM2Datas data) {
 		this.data = data;
 	}
 	public void setSm2DataBiz(SM2DataBiz sm2DataBiz) {
 		this.sm2DataBiz = sm2DataBiz;
 	}
 	public String getXmlContent() {
 		return xmlContent;
 	}
 	public void setXmlContent(String xmlContent) {
 		this.xmlContent = xmlContent;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 }
  
