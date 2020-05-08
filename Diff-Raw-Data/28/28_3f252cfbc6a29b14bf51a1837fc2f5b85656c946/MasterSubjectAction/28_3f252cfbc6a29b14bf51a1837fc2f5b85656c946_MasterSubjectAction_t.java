 package com.nbcedu.function.schoolmaster2.action;
 
 import static com.nbcedu.function.schoolmaster2.tags.EmotionDisplayTag.replaceEmos;
 import static org.apache.taglibs.standard.tag.common.core.Util.escapeXml;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.commons.lang.xwork.StringUtils;
 import org.apache.struts2.ServletActionContext;
 import org.springframework.util.CollectionUtils;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Lists;
 import com.google.common.reflect.TypeToken;
 import com.nbcedu.function.schoolmaster2.biz.SM2CommentBiz;
 import com.nbcedu.function.schoolmaster2.biz.SM2DisscusBiz;
 import com.nbcedu.function.schoolmaster2.biz.SM2MasterSubBiz;
 import com.nbcedu.function.schoolmaster2.biz.Sm2ProgressBiz;
 import com.nbcedu.function.schoolmaster2.biz.Sm2ReadsBiz;
 import com.nbcedu.function.schoolmaster2.core.action.BaseAction;
 import com.nbcedu.function.schoolmaster2.core.pager.PagerModel;
 import com.nbcedu.function.schoolmaster2.core.util.struts2.Struts2Utils;
 import com.nbcedu.function.schoolmaster2.data.model.TSm2Comment;
 import com.nbcedu.function.schoolmaster2.data.model.TSm2Disscus;
 import com.nbcedu.function.schoolmaster2.data.model.TSm2Progress;
 import com.nbcedu.function.schoolmaster2.data.model.TSm2Step;
 import com.nbcedu.function.schoolmaster2.data.model.TSm2Subject;
 import com.nbcedu.function.schoolmaster2.data.vo.ProgressVo;
 import com.nbcedu.function.schoolmaster2.utils.UCService;
 import com.nbcedu.function.schoolmaster2.utils.Utils;
 import com.nbcedu.function.schoolmaster2.vo.DepartmentVo;
 import com.nbcedu.function.schoolmaster2.vo.MasterSubSearchVO;
 import com.nbcedu.function.schoolmaster2.vo.StepVo;
 
 /**
  * 校长
  * @author xuechong
  */
 @SuppressWarnings("serial")
 public class MasterSubjectAction extends BaseAction{
 	
 	private String moduleId;
 	private TSm2Subject subject;
 	private TSm2Step step;
 	private List<ProgressVo> proList = new ArrayList<ProgressVo>();
 	private String name;
 	private String subjectId;
 	private MasterSubSearchVO search = new MasterSubSearchVO();
 	
 	private SM2MasterSubBiz subBiz;
 	private Sm2ProgressBiz progBiz;
 	private Sm2ReadsBiz readsBiz;
 	private SM2DisscusBiz disscusBiz;
 	private SM2CommentBiz comBiz;
 	
 	
 	public String list(){
 		List<DepartmentVo> departments = UCService.findDepartment();
 		this.getRequest().setAttribute("departments",departments);
 		if(StringUtils.isNotBlank(moduleId)){
 			this.search.setModuleId(moduleId);
 			if(!Utils.getDefaultMasterUids().contains(this.getUserId())){
 				this.search.setReceiverUid( this.getUserId());
 			}
 			this.pm = this.subBiz.findBySearchVo(search); 
 			return moduleId;
 		}else{
 			return "404";
 		}
 	}
 	
 	/**
 	 * 查看详细
 	 * @return
 	 * @author xuechong
 	 */
 	public String detail(){
 		this.subject = this.subBiz.findById(this.id);
 		List<StepVo> steps = this.subBiz.findAllSteps(this.id);
 		this.getRequestMap().put("steps", steps);
 		return "detail";
 	}
 	
 	/**
 	 * 查看步骤
 	 * @return
 	 * @author xuechong
 	 */
 	public String showStep(){
 		this.proList = this.progBiz.findVoByStepId(this.id);
 		this.readsBiz.addByStep(this.id, getUserId());
 		if(!CollectionUtils.isEmpty(proList)){
 			Collection<String> progIds = Collections2.transform(
 					proList, new Function<TSm2Progress, String>() {
 						@Override
 						public String apply(TSm2Progress input) {
 							return input.getId();
 						}
 					});
 			
 			/*disCuz map*/{
 				List<TSm2Disscus> disList = this.disscusBiz.findByProgIds(progIds,5);
 				if(!CollectionUtils.isEmpty(disList)){
 					
 					HashMap<String, List<TSm2Disscus>> disMap = 
 						new HashMap<String, List<TSm2Disscus>>(disList.size());
 					
 					for (TSm2Disscus dis : disList) {
 						if(!disMap.containsKey(dis.getProgressId())){
 							disMap.put(dis.getProgressId(),
 									new ArrayList<TSm2Disscus>());
 						}
 						disMap.get(dis.getProgressId()).add(dis);
 					}
 					
 					this.getRequestMap().put("disMap", disMap);
 				}
 			}/*disCuz map*/
 			
 			/*comment map*/{
 				List<TSm2Comment> comList = this.comBiz.findByProgIds(progIds,5);
 				if(!CollectionUtils.isEmpty(comList)){
 					
 					HashMap<String, List<TSm2Comment>> comMap = 
 						new HashMap<String, List<TSm2Comment>>(comList.size());
 					
 					for (TSm2Comment comment : comList) {
 						if(!comMap.containsKey(comment.getProgressId())){
 							comMap.put(comment.getProgressId(),
 									new ArrayList<TSm2Comment>());
 						}
 						comMap.get(comment.getProgressId()).add(comment);
 					}
 					this.getRequestMap().put("comMap", comMap);
 				}
 			}/*comment map*/
 			
 			
 		}
 		
 		
 		return "stepDetail";
 	}
 	
 	
 	
 	/**
 	 * 转发人员树
 	 * @author xuechong
 	 */
 	public void transTree(){
 		String json = UCService.getPersonJson(Lists.newArrayList(getUserId()),false);
 		Struts2Utils.renderJson(json);
 	}
 	
 	public String daiban(){
 		List<TSm2Subject> list = this.subBiz.findByMsterModule(
 				this.getUserId(),
 				new LinkedList<String>(){{
 					add("jinjizhongyao");
 					add("qingshibaopi");
 					add("zongjiehuibao");
 					add("linshishixiang");
 				}}
 				, 5);
 		
 		this.getRequestMap().put("subList", list);
 		return "daibanshiyi";
 	}
 	
 	public String daiBanPage(){
 		PagerModel page = this.subBiz.findByMaster(this.moduleId, getUserId(),1);
 		this.getRequestMap().put("subList", page.getDatas());
 		return "daibanshiyi";
 	}
 	
 	public String changeProgress(){
 		Integer percent = this.subject.getProgress();
 		if(percent!=null ){
 			this.subject = this.subBiz.findById(this.id);
 			if(percent>subject.getProgress()){
 				this.subject.setProgress(percent);
 				this.subBiz.update(subject);
 			}
 		}
 		return "refreshDetail";
 	}
 	
 	/**
 	 * 显示所有评论
 	 * @author xuechong
 	 */
 	public void allDiss(){
 		List<TSm2Disscus> disList = this.disscusBiz.findByProgIds(Arrays.asList(this.id));
 		if(CollectionUtils.isEmpty(disList)){
 			return;
 		}
 		List<MsgContent> results = Lists.transform(disList, new Function<TSm2Disscus, MsgContent>() {
 			@Override
 			public MsgContent apply(TSm2Disscus in) {
 				MsgContent msg = new MsgContent();
 				msg.setTime(Utils.Dates.fullSdf.format(in.getCreateTime()));
 				msg.setUser(in.getUserName());
 				msg.setContent(replaceEmos(in.getContent(), ctxPath()));
 				return msg;
 			}
 		});
 		renderMsgJson(results);
 	}
 	
 	/**
 	 * 显示所有批示
 	 * @author xuechong
 	 */
 	public void allComm(){
 		List<TSm2Comment> comList = this.comBiz.findByProgIds(Arrays.asList(this.id));
 		if(CollectionUtils.isEmpty(comList)){
 			return;
 		}
 		List<MsgContent> results = Lists.transform(comList,new Function<TSm2Comment, MsgContent>() {
 			@Override
 			public MsgContent apply(TSm2Comment in) {
 				MsgContent msg = new MsgContent();
 				msg.setUser(in.getUserName());
 				msg.setTime(Utils.Dates.fullSdf.format(in.getCreatetime()));
 				msg.setContent(escapeXml(in.getContent()));
 				return msg;
 			}
 		});
 		renderMsgJson(results);
 	}
 	
 	private void renderMsgJson(List<MsgContent> contents){
 		Struts2Utils.renderJson(
 				Utils.gson.toJson(contents, new TypeToken<List<MsgContent>>(){}.getType()));
 	}
 	
 	private String ctxPath(){
 		return ServletActionContext.getServletContext().getContextPath();
 	}
 	
 	public static class MsgContent{
 		private String user;
 		private String time;
 		private String content;
 		public String getUser() {
 			return user;
 		}
 		public void setUser(String user) {
 			this.user = user;
 		}
 		public String getTime() {
 			return time;
 		}
 		public void setTime(String time) {
 			this.time = time;
 		}
 		public String getContent() {
 			return content;
 		}
 		public void setContent(String content) {
 			this.content = content;
 		}
 	}
 	///////////////////////
 	////getters&setters////
 	///////////////////////
 	public String getModuleId() {
 		return moduleId;
 	}
 	public void setModuleId(String moduleId) {
 		this.moduleId = moduleId;
 	}
 	public void setSubBiz(SM2MasterSubBiz subBiz) {
 		this.subBiz = subBiz;
 	}
 	public TSm2Subject getSubject() {
 		return subject;
 	}
 	public void setSubject(TSm2Subject subject) {
 		this.subject = subject;
 	}
 	public TSm2Step getStep() {
 		return step;
 	}
 	public void setStep(TSm2Step step) {
 		this.step = step;
 	}
 	public List<ProgressVo> getProList() {
 		return proList;
 	}
 	public void setProList(List<ProgressVo> proList) {
 		this.proList = proList;
 	}
 	public void setProgBiz(Sm2ProgressBiz progBiz) {
 		this.progBiz = progBiz;
 	}
 	public void setReadsBiz(Sm2ReadsBiz readsBiz) {
 		this.readsBiz = readsBiz;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public void setDisscusBiz(SM2DisscusBiz disscusBiz) {
 		this.disscusBiz = disscusBiz;
 	}
 	public void setComBiz(SM2CommentBiz comBiz) {
 		this.comBiz = comBiz;
 	}
 	public MasterSubSearchVO getSearch() {
 		return search;
 	}
 	public void setSearch(MasterSubSearchVO search) {
 		this.search = search;
 	}
 
 	public String getSubjectId() {
 		return subjectId;
 	}
 
 	public void setSubjectId(String subjectId) {
 		this.subjectId = subjectId;
 	}
 	
 }
