 package com.chinarewards.qqgbvpn.domain;
 
 import java.util.Date;
 
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 
 import org.hibernate.annotations.GenericGenerator;
 
 import com.chinarewards.qqgbvpn.domain.status.RedeemStatus;
 
 /**
  * 
  * @author iori
  * 
  */
 @Entity
 public class HuaxiaRedeem {
 
 	@Id
 	@GeneratedValue(generator = "system-uuid")
 	@GenericGenerator(name = "system-uuid", strategy = "uuid")
 	String id;
 
 	/**
 	 * serial number (from huaxia bank)
 	 */
 	String serialNum;
 	
 	/**
	 * bank card number (last 4 digit from huaxia bank)
 	 */
 	String cardNum;
 	
 	/**
 	 * order date (from huaxia bank)
 	 */
 	String orderDate;
 	
 	/**
 	 * init date (status:U)
 	 */
 	Date createDate;
 	
 	/**
 	 * confirm date (status:A)
 	 */
 	Date confirmDate;
 	
 	/**
 	 * ack date (status:R)
 	 */
 	Date ackDate;
 	
 	/**
 	 * redeem Status
 	 */
 	@Enumerated(EnumType.STRING)
 	RedeemStatus status;
 
 	/**
 	 * POS ID, Should be copied from <code>PosAssignment</code>. The value
 	 * should be <code>Pos.getPosId()</code>.
 	 */
 	String posId;
 
 	/**
 	 * POS Model, Should be copied from <code>PosAssignment</code>. The value
 	 * should be <code>Pos.getModel()</code>.
 	 */
 	String posModel;
 
 	/**
 	 * POS SIM Phone Number, Should be copied from <code>PosAssignment</code>.
 	 * The value should be <code>Pos.getSimPhoneNo()</code>.
 	 */
 	String posSimPhoneNo;
 
 	/**
 	 * Should be copied from <code>PosAssignment</code>. The value should be
 	 * <code>Agent.getId()</code>.
 	 */
 	String agentId;
 
 	/**
 	 * Should be copied from <code>PosAssignment</code>. The value should be
 	 * <code>Agent.getName()</code>.
 	 */
 	String agentName;
 
 	public Date getConfirmDate() {
 		return confirmDate;
 	}
 
 	public void setConfirmDate(Date confirmDate) {
 		this.confirmDate = confirmDate;
 	}
 
 	public Date getAckDate() {
 		return ackDate;
 	}
 
 	public void setAckDate(Date ackDate) {
 		this.ackDate = ackDate;
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	public String getSerialNum() {
 		return serialNum;
 	}
 
 	public void setSerialNum(String serialNum) {
 		this.serialNum = serialNum;
 	}
 
 	public String getOrderDate() {
 		return orderDate;
 	}
 
 	public void setOrderDate(String orderDate) {
 		this.orderDate = orderDate;
 	}
 
 	public Date getCreateDate() {
 		return createDate;
 	}
 
 	public void setCreateDate(Date createDate) {
 		this.createDate = createDate;
 	}
 
 	public RedeemStatus getStatus() {
 		return status;
 	}
 
 	public void setStatus(RedeemStatus status) {
 		this.status = status;
 	}
 
 	public String getPosId() {
 		return posId;
 	}
 
 	public void setPosId(String posId) {
 		this.posId = posId;
 	}
 
 	public String getPosModel() {
 		return posModel;
 	}
 
 	public void setPosModel(String posModel) {
 		this.posModel = posModel;
 	}
 
 	public String getPosSimPhoneNo() {
 		return posSimPhoneNo;
 	}
 
 	public void setPosSimPhoneNo(String posSimPhoneNo) {
 		this.posSimPhoneNo = posSimPhoneNo;
 	}
 
 	public String getAgentId() {
 		return agentId;
 	}
 
 	public void setAgentId(String agentId) {
 		this.agentId = agentId;
 	}
 
 	public String getAgentName() {
 		return agentName;
 	}
 
 	public void setAgentName(String agentName) {
 		this.agentName = agentName;
 	}
 	
 	public String getCardNum() {
 		return cardNum;
 	}
 
 	public void setCardNum(String cardNum) {
 		this.cardNum = cardNum;
 	}
 	
 }
