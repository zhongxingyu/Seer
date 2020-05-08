 package com.nbcedu.function.teachersignup.action;
 
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import com.nbcedu.function.teachersignup.biz.TSActivityBiz;
 import com.nbcedu.function.teachersignup.biz.TSSignBiz;
 import com.nbcedu.function.teachersignup.biz.TSSubjectBiz;
 import com.nbcedu.function.teachersignup.core.action.BaseAction;
 import com.nbcedu.function.teachersignup.core.util.exl.ExlAnnotationUtil;
 import com.nbcedu.function.teachersignup.model.TSActivity;
 import com.nbcedu.function.teachersignup.model.TSReward;
 import com.nbcedu.function.teachersignup.model.TSSign;
 import com.nbcedu.function.teachersignup.model.TSSubject;
 import com.nbcedu.function.teachersignup.vo.TSActivitiSignVo;
 import com.nbcedu.function.teachersignup.vo.TSSignVo;
 
 @SuppressWarnings("serial")
 public class TSSignAction extends BaseAction{
 
 	private TSSignBiz signBiz;
 	private TSActivityBiz actBiz;
 	private TSSubjectBiz subBiz;
 	
 	private String[] subjectIds;
 	private String actId;
 	private String userName;
 	private String subId;
 	private String rewId;
 	private String signId;
 	private TSActivitiSignVo asvo;
	private TSActivity act ;
 	
 	private Set<TSReward> rewdSet = new HashSet<TSReward>();
 	private Set<TSSubject> subjSet = new HashSet<TSSubject>();
 	private List<TSSignVo> signList = new ArrayList<TSSignVo>();
 	
	
 	/**
 	 * 增加报名
 	 * @return
 	 * @author xuechong
 	 */
 	public String addSign(){
 		this.signBiz.addNewSign(this.getCurUser(),new String[]{this.subId});
 		return "reloadCom";
 	}
 
 	/**
 	 * 取消报名
 	 * @return
 	 * @author xuechong
 	 */
 	public String cancel(){
 		this.signBiz.removeById(this.id);
 		return "reloadCom";
 	}
 	
 	/**
 	 * 导出报名情况exl
 	 * 
 	 * @author xuechong
 	 */
 	public void adminExl(){
 		TSActivity act = this.actBiz.findById(this.actId);
 		List<TSSignVo> datas = new ArrayList<TSSignVo>();
 		String head = "报名情况列表s";
 		if(act!=null){
 			datas = TSSignVo.Factory.build(this.signBiz.findByActId(actId), 
 					new ArrayList<TSSubject>(act.getSubjects()), 
 					new ArrayList<TSReward>(act.getRewards()));
 			head = act.getName() + "报名情况";
 		}
 		ExlAnnotationUtil.export(head, datas);
 	}
 	
 	/**
 	 * 普通老师报名项目列表
 	 * @return
 	 * @author xuechong
 	 */
 	public String subList(){
 		TSActivity act = this.actBiz.findById(this.actId);
 		List<TSSign> signedList = 
 			this.signBiz.findAllByUidActId(getCurUserUid(), actId);
 		this.asvo = TSActivitiSignVo.Factory.build(act, signedList);
 		return "comSignList";
 	}
 	
 	/**
 	 * 管理员获奖情况列表
 	 * @return
 	 * @author xuechong
 	 */
 	public String adminList(){
		this.act = this.actBiz.findById(actId);
 		this.rewdSet = act.getRewards();
 		this.subjSet = act.getSubjects();
 		this.pm = this.signBiz.pageByAct(actId, subId, rewId, userName);
 		return "adminList";
 	}
 	
 	public String masterList(){
 		this.adminList();
 		return "masterList";
 	}
 	
 	/**
 	 * 管理员追加获奖情况
 	 * @return
 	 * @author xuechong
 	 */
 	public String addRew(){
 		this.signBiz.addRew(signId, rewId);
 		this.rewId="";
 		return "reloadAdminList";
 	}
 	/**
 	 * 普通教师已报名项目列表
 	 * @return
 	 * @author xuechong
 	 */
 	public String comFinSubList(){
 		TSActivity act = this.actBiz.findById(this.actId);
 		if(act!=null){
 			List<TSSign> signedList = 
 				this.signBiz.findAllByUidActId(getCurUserUid(), actId);
 			this.asvo = TSActivitiSignVo.Factory.build(act, signedList);
 			this.signList = TSSignVo.Factory.build(signedList, 
 					new ArrayList<TSSubject>(act.getSubjects()), 
 					new ArrayList<TSReward>(act.getRewards()));
 		}
 		
 		return "comFinSubList";
 	}
 	
 	//////////////////////////
 	///getters&setters////////
 	//////////////////////////
 	public void setSignBiz(TSSignBiz signBiz) {
 		this.signBiz = signBiz;
 	}
 	public String[] getSubjectIds() {
 		return subjectIds;
 	}
 	public void setSubjectIds(String[] subjectIds) {
 		this.subjectIds = subjectIds;
 	}
 	public String getActId() {
 		return actId;
 	}
 	public void setActId(String actId) {
 		this.actId = actId;
 	}
 	public String getUserName() {
 		return userName;
 	}
 	public void setUserName(String userName) {
 		this.userName = userName;
 	}
 	public String getSubId() {
 		return subId;
 	}
 	public void setSubId(String subId) {
 		this.subId = subId;
 	}
 	public String getRewId() {
 		return rewId;
 	}
 	public void setRewId(String rewId) {
 		this.rewId = rewId;
 	}
 	public void setActBiz(TSActivityBiz actBiz) {
 		this.actBiz = actBiz;
 	}
 	public void setSubBiz(TSSubjectBiz subBiz) {
 		this.subBiz = subBiz;
 	}
 	public TSActivitiSignVo getAsvo() {
 		return asvo;
 	}
 	public void setAsvo(TSActivitiSignVo asvo) {
 		this.asvo = asvo;
 	}
 	public Set<TSReward> getRewdSet() {
 		return rewdSet;
 	}
 	public void setRewdSet(Set<TSReward> rewdSet) {
 		this.rewdSet = rewdSet;
 	}
 	public Set<TSSubject> getSubjSet() {
 		return subjSet;
 	}
 	public void setSubjSet(Set<TSSubject> subjSet) {
 		this.subjSet = subjSet;
 	}
 	public String getSignId() {
 		return signId;
 	}
 	public void setSignId(String signId) {
 		this.signId = signId;
 	}
 	public List<TSSignVo> getSignList() {
 		return signList;
 	}
 	public void setSignList(List<TSSignVo> signList) {
 		this.signList = signList;
 	}
	public TSActivity getAct() {
		return act;
	}
	public void setAct(TSActivity act) {
		this.act = act;
	}
 	
 }
