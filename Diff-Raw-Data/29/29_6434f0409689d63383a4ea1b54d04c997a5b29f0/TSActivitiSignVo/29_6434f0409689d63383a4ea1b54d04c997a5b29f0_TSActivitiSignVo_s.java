 package com.nbcedu.function.teachersignup.vo;
 
 import static org.springframework.util.CollectionUtils.isEmpty;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.util.CollectionUtils;
 
 import com.nbcedu.function.teachersignup.model.TSActivity;
 import com.nbcedu.function.teachersignup.model.TSSign;
 import com.nbcedu.function.teachersignup.model.TSSubject;
 
 /**
  * 报名页面VO
  * @author xuechong
  */
 public class TSActivitiSignVo {
 	
 	private String actId;
 	private String actName;
 	private String actComment;
 	private String attaName;
 	private List<SignVO> signList = new ArrayList<SignVO>();
 
 	
 	public static class Factory{
 		public static TSActivitiSignVo build(TSActivity act,
 							List<TSSign> signList){
 			
 			TSActivitiSignVo result = new TSActivitiSignVo();
 			result.setActId(act.getId());
 			result.setActName(act.getName());
 			result.setAttaName(act.getFileName());
 			result.setActComment(act.getComment());
 			result.setSignList(SignVO.build(act.getSubjects(), signList));
 			return result; 
 		}
 	}
 	
 	public static class SignVO{
 		
 		private String signId;
 		private String subId;
 		private String subName;
 		private boolean signed;
 		@SuppressWarnings("serial")
 		public static List<SignVO> build(final Collection<TSSubject> subList,
 				final List<TSSign> signList){
 			
 			List<SignVO> result = new ArrayList<SignVO>();
 			if(!isEmpty(subList)){
 				
 				/***key:subjectId | value:signId***/
 				Map<String, String> signedIds = new HashMap<String, String>(subList.size()){{
 					if(!CollectionUtils.isEmpty(signList)){
 						for (TSSign sig : signList) {
 							put(sig.getSubjectId(), sig.getId());
 						}
 					}
 				}}; 
 				
 				for (TSSubject sub : subList) {
 					SignVO sign = new SignVO();
 					sign.setSubId(sub.getId());
 					sign.setSubName(sub.getName());
 					sign.setSigned(signedIds.containsKey(sub.getId()));
 					sign.setSignId(signedIds.get(sub.getId()));
 					result.add(sign);
 				}
 				
 			}
 			return result;
 		}
 		///////////////////////
 		////getters&setters////
 		///////////////////////
 		public String getSignId() {
 			return signId;
 		}
 		public void setSignId(String signId) {
 			this.signId = signId;
 		}
 		public String getSubId() {
 			return subId;
 		}
 		public void setSubId(String subId) {
 			this.subId = subId;
 		}
 		public String getSubName() {
 			return subName;
 		}
 		public void setSubName(String subName) {
 			this.subName = subName;
 		}
 		public boolean getSigned() {
 			return signed;
 		}
 		public void setSigned(boolean signed) {
 			this.signed = signed;
 		}
 		
 	}
 	
 	///////////////////////
 	////getters&setters////
 	///////////////////////
 	public String getActId() {
 		return actId;
 	}
 	public void setActId(String actId) {
 		this.actId = actId;
 	}
 	public String getActName() {
 		return actName;
 	}
 	public void setActName(String actName) {
 		this.actName = actName;
 	}
 	public String getActComment() {
 		return actComment;
 	}
 	public void setActComment(String actComment) {
 		this.actComment = actComment;
 	}
 	public String getAttaName() {
 		return attaName;
 	}
 	public void setAttaName(String attaName) {
 		this.attaName = attaName;
 	}
 	public List<SignVO> getSignList() {
 		return signList;
 	}
 	public void setSignList(List<SignVO> signList) {
 		this.signList = signList;
 	}
 	
 	
 }
