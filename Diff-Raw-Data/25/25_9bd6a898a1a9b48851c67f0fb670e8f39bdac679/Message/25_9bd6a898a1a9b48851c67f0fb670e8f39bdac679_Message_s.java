 package com.gemantic.killer.common.model;
 
 import java.io.Serializable;
import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.List;

 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.commons.lang.builder.ToStringStyle;
 
 public class Message implements Serializable, Cloneable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -3343613574728149833L;
 
 	public Message() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	public Message(String subject, String predict, String object, String color, String expression, String where, String content, String version,Long time) {
 		this.subject = subject;
 		this.predict = predict;
 		this.object = object;
 		this.color = color;
 		this.expression = expression;
 		this.where = where;
 		this.content = content;
 		this.time =time;
 		this.version = version;
 		
 	}
 	
 	public Message(String subject, String predict, String object, String color, String expression, String where, String content, String version) {
 		this.subject = subject;
 		this.predict = predict;
 		this.object = object;
 		this.color = color;
 		this.expression = expression;
 		this.where = where;
 		this.content = content;
 		this.time =System.currentTimeMillis();
 		this.version = version;
 		
 	}
 
 	public Message(String subject, String predict, String content, List<String> accepts) {
 
 		this.subject = subject;
 		this.predict = predict;
 		this.content = content;
 		this.accepts = accepts;
 	}
 
 	public Message(String subject, String predict, String object) {
 
 		this.subject = subject;
 		this.predict = predict;
 		this.object = object;
 
 	}
 	/**
 	 * id
 	 */
 	private Long id;
 
 	/**
 	 * 主题,一般是动作的发起人
 	 */
 	private String subject="";
 	/**
 	 * 谓词 一般是指动作
 	 */
 	private String predict="";
 	/**
 	 * 客体,一般是动作的承受者
 	 */
 	private String object="";
 	/**
 	 * 颜色
 	 */
 	private String color="";
 	/**
 	 * 表情
 	 */
 	private String expression="";
 	/**
	 * 时间
 	 */
 	private Long time=System.currentTimeMillis();
 	/**
 	 * 内容,一般是动作的附带内容
 	 */
 	private String content="";
 
 	/**
 	 * 版本
 	 */
 	private String version="";
 
 	/**
 	 * 地点,一般指游戏房间的id
 	 */
 	private String where="";
 	
 	/**
 	 * 计划执行时间
 	 */
 	private Long scheduledTime;
 
 	/**
 	 * 消息的接收者,用于发送消息,和消息的生成无关
 	 */
 	private List<String> accepts = new ArrayList();
 
 	public String getPredict() {
 		return predict;
 	}
 
 	public void setPredict(String predict) {
 		this.predict = predict;
 	}
 
 	public String getSubject() {
 		return subject;
 	}
 
 	public void setSubject(String subject) {
 		this.subject = subject;
 	}
 
 	public String getObject() {
 		return object;
 	}
 
 	public void setObject(String object) {
 		this.object = object;
 	}
 	
 	
 
 	
 
 	public String getWhere() {
 		return where;
 	}
 
 	public void setWhere(String where) {
 		this.where = where;
 	}
 
 	public String getColor() {
 		return color;
 	}
 
 	public void setColor(String color) {
 		this.color = color;
 	}
 
 	public String getExpression() {
 		return expression;
 	}
 
 	public void setExpression(String expression) {
 		this.expression = expression;
 	}
 
 	public Long getTime() {
 		return time;
 	}
 
 	public void setTime(Long time) {
 		this.time = time;
 	}
 
 	public String getContent() {
 		return content;
 	}
 
 	public void setContent(String content) {
 		this.content = content;
 	}
 
 	public String getVersion() {
 		return version;
 	}
 
 	public void setVersion(String version) {
 		this.version = version;
 	}
 	
 	
 	
 
 	
 
 	public List<String> getAccepts() {
 		return accepts;
 	}
 
 	public void setAccepts(List<String> accepts) {
 		this.accepts = accepts;
 	}
 	
 	
 	
 
 	
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 	
 	
 	
 
 	public Long getScheduledTime() {
 		return scheduledTime;
 	}
 
 	public void setScheduledTime(Long scheduledTime) {
 		this.scheduledTime = scheduledTime;
 	}
 
 	public String toString() {
 		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
 	}
 
 }
