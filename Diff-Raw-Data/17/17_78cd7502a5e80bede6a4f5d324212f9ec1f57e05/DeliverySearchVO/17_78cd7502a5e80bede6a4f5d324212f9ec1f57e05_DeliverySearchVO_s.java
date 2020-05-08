 package com.chinarewards.qqgbvpn.mgmtui.model.delivery;
 
 import java.util.Date;
 
 import com.chinarewards.qqgbvpn.mgmtui.model.util.PaginationTools;
 
 public class DeliverySearchVO {
 
 	String dnNumber;
 
 	String status;
 
 	Date createDateFrom;
	Date createDateTO;
 
 	String agentId;
 	String agentName;
 
 	PaginationTools pagination;
 
 	@Override
 	public String toString() {
 		return "DeliverySearchVO [dnNumber=" + dnNumber + ", status=" + status
				+ ", createDateFrom=" + createDateFrom + ", createDateTO="
				+ createDateTO + ", agentId=" + agentId + ", agentName="
 				+ agentName + ", pagination=" + pagination + "]";
 	}
 
 	public String getDnNumber() {
 		return dnNumber;
 	}
 
 	public void setDnNumber(String dnNumber) {
 		this.dnNumber = dnNumber;
 	}
 
 	public String getStatus() {
 		return status;
 	}
 
 	public void setStatus(String status) {
 		this.status = status;
 	}
 
	public Date getCreateDateTO() {
		return createDateTO;
 	}
 
	public void setCreateDateTO(Date createDateTO) {
		this.createDateTO = createDateTO;
 	}
 
 	public Date getCreateDateFrom() {
 		return createDateFrom;
 	}
 
 	public void setCreateDateFrom(Date createDateFrom) {
 		this.createDateFrom = createDateFrom;
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
 
 	public PaginationTools getPagination() {
 		return pagination;
 	}
 
 	public void setPagination(PaginationTools pagination) {
 		this.pagination = pagination;
 	}
 
 }
