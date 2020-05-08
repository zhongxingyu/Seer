 package net.chat.integration.vo;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import com.sun.xml.txw2.annotation.XmlCDATA;
 
 @XmlRootElement(name = "xml")
 public class WeChatReqBean {
 	private String toUserName;
 	private String fromUserName;
 	private Long createTime;
 	private String msgType;
 	private String content;
 
 	private String event;
 	private String eventKey;
 	private String ticket;
 
 	private Double location_X;
 	private Double location_Y;
 	private Integer scale;
 	private String label;
 
 	private Long msgId;
 
 	public String getToUserName() {
 		return toUserName;
 	}
 
 	@XmlCDATA
 	@XmlElement(name = "ToUserName")
 	public void setToUserName(String toUserName) {
 		this.toUserName = toUserName;
 	}
 
 	public String getFromUserName() {
 		return fromUserName;
 	}
 
 	@XmlCDATA
 	@XmlElement(name = "FromUserName")
 	public void setFromUserName(String fromUserName) {
 		this.fromUserName = fromUserName;
 	}
 
 	public Long getCreateTime() {
 		return createTime;
 	}
 
 	@XmlElement(name = "CreateTime")
 	public void setCreateTime(Long createTime) {
 		this.createTime = createTime;
 	}
 
 	public String getMsgType() {
 		return msgType;
 	}
 
 	@XmlCDATA
 	@XmlElement(name = "MsgType")
 	public void setMsgType(String msgType) {
 		this.msgType = msgType;
 	}
 
 	public String getContent() {
 		return content;
 	}
 
 	@XmlCDATA
 	@XmlElement(name = "Content")
 	public void setContent(String content) {
 		this.content = content;
 	}
 
 	public Double getLocation_X() {
 		return location_X;
 	}
 
 	@XmlElement(name = "Location_X")
 	public void setLocation_X(Double location_X) {
 		this.location_X = location_X;
 	}
 
 	public Double getLocation_Y() {
 		return location_Y;
 	}
 
 	@XmlElement(name = "Location_Y")
 	public void setLocation_Y(Double location_Y) {
 		this.location_Y = location_Y;
 	}
 
 	public Integer getScale() {
 		return scale;
 	}
 
 	@XmlElement(name = "Scale")
 	public void setScale(Integer scale) {
 		this.scale = scale;
 	}
 
 	public String getLabel() {
 		return label;
 	}
 
 	@XmlCDATA
 	@XmlElement(name = "Label")
 	public void setLabel(String label) {
 		this.label = label;
 	}
 
 	public Long getMsgId() {
 		return msgId;
 	}
 
 	@XmlElement(name = "MsgId")
 	public void setMsgId(Long msgId) {
 		this.msgId = msgId;
 	}
 
 	public String getEvent() {
 		return event;
 	}
 
 	@XmlElement(name = "Event")
 	public void setEvent(String event) {
 		this.event = event;
 	}
 
 	public String getEventKey() {
 		return eventKey;
 	}
 
 	@XmlElement(name = "EventKey")
 	public void setEventKey(String eventKey) {
 		this.eventKey = eventKey;
 	}
 
 	public String getTicket() {
 		return ticket;
 	}
 
 	@XmlElement(name = "Ticket")
 	public void setTicket(String ticket) {
 		this.ticket = ticket;
 	}
 
 }
