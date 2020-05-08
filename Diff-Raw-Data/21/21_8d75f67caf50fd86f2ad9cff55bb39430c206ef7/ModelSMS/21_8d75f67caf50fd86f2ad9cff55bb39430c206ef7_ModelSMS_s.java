 package com.huhuo.carservicecore.cust.ms;
 
 import com.huhuo.carservicecore.sys.user.ModelUser;
 import com.huhuo.integration.base.BaseModel;
 
 
 public class ModelSMS extends BaseModel {
 
 	private static final long serialVersionUID = -3764026478305825381L;
 	
 	/** 短信内容 **/
 	private String content;
 	/** 发送者id（与sys_user关联） **/
 	private Long senderId;
 	/** 接收者id（与sys_user关联） **/
 	private Long recieverId;
 	/** 接收者手机号码 **/
 	private Long phoneNum;
 	
 	/** 外联对象 **/
 	private ModelUser sender;
	
 	private ModelUser reciever;
 	
 	public String getContent() {
 		return content;
 	}
 	public void setContent(String content) {
 		this.content = content;
 	}
 	public Long getSenderId() {
 		return senderId;
 	}
 	public void setSenderId(Long senderId) {
 		this.senderId = senderId;
 	}
 	public Long getRecieverId() {
 		return recieverId;
 	}
 	public void setRecieverId(Long recieverId) {
 		this.recieverId = recieverId;
 	}
 	public Long getPhoneNum() {
 		return phoneNum;
 	}
 	public void setPhoneNum(Long phoneNum) {
 		this.phoneNum = phoneNum;
 	}
 	public ModelUser getSender() {
 		return sender;
 	}
 	public void setSender(ModelUser sender) {
 		this.sender = sender;
 	}
 	public ModelUser getReciever() {
 		return reciever;
 	}
 	public void setReciever(ModelUser reciever) {
 		this.reciever = reciever;
 	}
 	
 }
