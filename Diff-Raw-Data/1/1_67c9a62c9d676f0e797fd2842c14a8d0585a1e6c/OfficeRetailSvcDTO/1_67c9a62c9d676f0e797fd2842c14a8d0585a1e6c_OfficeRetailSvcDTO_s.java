 package com.westchase.persistence.dto.cmu.report;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.westchase.persistence.model.CmuOfficeRetailSvc;
 import com.westchase.persistence.model.CmuQuarter;
 import com.westchase.persistence.model.Property;
 
 public class OfficeRetailSvcDTO implements CmuDTO, Serializable {
 
 	private Integer id;
 	private Property property;
 	private CmuQuarter cmuQuarter;
 	private String completedBy;
 	private boolean forSale;
 	private String forSaleContact;
 	private String forSalePhone;
 	private Double sqFtForLease;
 	private Double occupancy;
 	private Double occupied;
 	private Double largestSpace;
 	private Double largestSpace6mths;
 	private Double largestSpace12mths;
 	private String propertyMgr;
 	private String propertyMgrPhone;
 	private String propertyMgrFax;
 	private String propertyMgrEmail;
 	private String mgmtCompany;
 	private String mgmtCompanyAddr;
 	private String leasingCompany;
 	private String leasingCompanyAddr;
 	private String leasingAgent;
 	private String leasingAgentPhone;
 	private String leasingAgentFax;
 	private String leasingAgentEmail;
 	private String comments;
 	private Date updated;
 	private Date verified;
 	private boolean staticInfoCorrect;
 	
 	public OfficeRetailSvcDTO(CmuOfficeRetailSvc ors) {
 		super();
 		if (ors != null) {
 			setId(ors.getId());
 			setProperty(ors.getProperty());
 			setCmuQuarter(ors.getCmuQuarter());
 			setCompletedBy(ors.getCompletedBy());
 			setForSale(ors.isForSale());
 			setForSaleContact(ors.getForSaleContact());
 			setForSalePhone(ors.getForSalePhone());
 			setSqFtForLease(ors.getSqFtForLease());
 			setOccupancy(ors.getOccupancy());
 			setOccupied(ors.getOccupied());
 			setStaticInfoCorrect(ors.isStaticInfoCorrect());
 			setVerified(ors.getVerified());
 			setUpdated(ors.getUpdated());
 			setComments(ors.getComments());
 		}
 	}
 	
 	public void setManagementCompany() {
 		setPropertyMgr(propertyMgr);
 		setPropertyMgrPhone(propertyMgrPhone);
 		setPropertyMgrFax(propertyMgrFax);
 		setPropertyMgrEmail(propertyMgrEmail);
 		setMgmtCompany(mgmtCompany);
 		setMgmtCompanyAddr(mgmtCompanyAddr);
 	}
 	
 	public void setLeasingCompany() {
 		setLeasingCompany(leasingCompany);
 		setLeasingCompanyAddr(leasingCompanyAddr);
 		setLeasingAgent(leasingAgent);
 		setLeasingAgentPhone(leasingAgentPhone);
 		setLeasingAgentFax(leasingAgentFax);
 		setLeasingAgentEmail(leasingAgentEmail);
 	}
 
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	public Property getProperty() {
 		return property;
 	}
 
 	public void setProperty(Property property) {
 		this.property = property;
 	}
 
 	public CmuQuarter getCmuQuarter() {
 		return cmuQuarter;
 	}
 
 	public void setCmuQuarter(CmuQuarter cmuQuarter) {
 		this.cmuQuarter = cmuQuarter;
 	}
 
 	public String getCompletedBy() {
 		return completedBy;
 	}
 
 	public void setCompletedBy(String completedBy) {
 		this.completedBy = completedBy;
 	}
 
 	public boolean isForSale() {
 		return forSale;
 	}
 
 	public void setForSale(boolean forSale) {
 		this.forSale = forSale;
 	}
 
 	public String getForSaleContact() {
 		return forSaleContact;
 	}
 
 	public void setForSaleContact(String forSaleContact) {
 		this.forSaleContact = forSaleContact;
 	}
 
 	public String getForSalePhone() {
 		return forSalePhone;
 	}
 
 	public void setForSalePhone(String forSalePhone) {
 		this.forSalePhone = forSalePhone;
 	}
 
 	public Double getSqFtForLease() {
 		return sqFtForLease;
 	}
 
 	public void setSqFtForLease(Double sqFtForLease) {
 		this.sqFtForLease = sqFtForLease;
 	}
 
 	public Double getOccupancy() {
 		return occupancy;
 	}
 
 	public void setOccupancy(Double occupancy) {
 		this.occupancy = occupancy;
 	}
 
 	public Double getOccupied() {
 		return occupied;
 	}
 
 	public void setOccupied(Double occupied) {
 		this.occupied = occupied;
 	}
 
 	public Double getLargestSpace() {
 		return largestSpace;
 	}
 
 	public void setLargestSpace(Double largestSpace) {
 		this.largestSpace = largestSpace;
 	}
 
 	public Double getLargestSpace6mths() {
 		return largestSpace6mths;
 	}
 
 	public void setLargestSpace6mths(Double largestSpace6mths) {
 		this.largestSpace6mths = largestSpace6mths;
 	}
 
 	public Double getLargestSpace12mths() {
 		return largestSpace12mths;
 	}
 
 	public void setLargestSpace12mths(Double largestSpace12mths) {
 		this.largestSpace12mths = largestSpace12mths;
 	}
 
 	public String getPropertyMgr() {
 		return propertyMgr;
 	}
 
 	public void setPropertyMgr(String propertyMgr) {
 		this.propertyMgr = propertyMgr;
 	}
 
 	public String getPropertyMgrPhone() {
 		return propertyMgrPhone;
 	}
 
 	public void setPropertyMgrPhone(String propertyMgrPhone) {
 		this.propertyMgrPhone = propertyMgrPhone;
 	}
 
 	public String getPropertyMgrFax() {
 		return propertyMgrFax;
 	}
 
 	public void setPropertyMgrFax(String propertyMgrFax) {
 		this.propertyMgrFax = propertyMgrFax;
 	}
 
 	public String getPropertyMgrEmail() {
 		return propertyMgrEmail;
 	}
 
 	public void setPropertyMgrEmail(String propertyMgrEmail) {
 		this.propertyMgrEmail = propertyMgrEmail;
 	}
 
 	public String getMgmtCompany() {
 		return mgmtCompany;
 	}
 
 	public void setMgmtCompany(String mgmtCompany) {
 		this.mgmtCompany = mgmtCompany;
 	}
 
 	public String getMgmtCompanyAddr() {
 		return mgmtCompanyAddr;
 	}
 
 	public void setMgmtCompanyAddr(String mgmtCompanyAddr) {
 		this.mgmtCompanyAddr = mgmtCompanyAddr;
 	}
 
 	public String getLeasingCompany() {
 		return leasingCompany;
 	}
 
 	public void setLeasingCompany(String leasingCompany) {
 		this.leasingCompany = leasingCompany;
 	}
 
 	public String getLeasingCompanyAddr() {
 		return leasingCompanyAddr;
 	}
 
 	public void setLeasingCompanyAddr(String leasingCompanyAddr) {
 		this.leasingCompanyAddr = leasingCompanyAddr;
 	}
 
 	public String getLeasingAgent() {
 		return leasingAgent;
 	}
 
 	public void setLeasingAgent(String leasingAgent) {
 		this.leasingAgent = leasingAgent;
 	}
 
 	public String getLeasingAgentPhone() {
 		return leasingAgentPhone;
 	}
 
 	public void setLeasingAgentPhone(String leasingAgentPhone) {
 		this.leasingAgentPhone = leasingAgentPhone;
 	}
 
 	public String getLeasingAgentFax() {
 		return leasingAgentFax;
 	}
 
 	public void setLeasingAgentFax(String leasingAgentFax) {
 		this.leasingAgentFax = leasingAgentFax;
 	}
 
 	public String getLeasingAgentEmail() {
 		return leasingAgentEmail;
 	}
 
 	public void setLeasingAgentEmail(String leasingAgentEmail) {
 		this.leasingAgentEmail = leasingAgentEmail;
 	}
 
 	public String getComments() {
 		return comments;
 	}
 
 	public void setComments(String comments) {
 		this.comments = comments;
 	}
 
 	public Date getUpdated() {
 		return updated;
 	}
 
 	public void setUpdated(Date updated) {
 		this.updated = updated;
 	}
 
 	public Date getVerified() {
 		return verified;
 	}
 
 	public void setVerified(Date verified) {
 		this.verified = verified;
 	}
 
 	public boolean isStaticInfoCorrect() {
 		return staticInfoCorrect;
 	}
 
 	public void setStaticInfoCorrect(boolean staticInfoCorrect) {
 		this.staticInfoCorrect = staticInfoCorrect;
 	}
 
 	public void setPropertyMgr(String firstName, String lastName) {
 		setPropertyMgr(String.format("%s %s", firstName, lastName));
 	}
 
 	public void setPropertyMgrPhone(String phone, String ext) {
 		if (StringUtils.isNotBlank(ext)) {
 			setPropertyMgr(String.format("%s x%s", phone, ext));
 		} else {
 			setPropertyMgrPhone(phone);
 		}
 	}
 
 	public void setMgmtCompanyAddr(String stNumber, String stAddress, String roomNum, String city, String state,
 			String zip) {
 		if (StringUtils.isNotBlank(roomNum)) {
 			setMgmtCompanyAddr(String.format("%s %s %s %s %s %s", stNumber, stAddress, roomNum, city, state, zip));
 		} else {
 			setMgmtCompanyAddr(String.format("%s %s %s %s %s", stNumber, stAddress, city, state, zip));
 		}
 	}
 
 	public void setLeasingCompanyAddr(String stNumber, String stAddress, String roomNum, String city, String state,
 			String zip) {
 		if (StringUtils.isNotBlank(roomNum)) {
 			setLeasingCompanyAddr(String.format("%s %s %s %s %s %s", stNumber, stAddress, roomNum, city, state, zip));
 		} else {
 			setMgmtCompanyAddr(String.format("%s %s %s %s %s", stNumber, stAddress, city, state, zip));
 		}
 	}
 
 	public void setLeasingAgent(String firstName, String lastName) {
 		setLeasingAgent(String.format("%s %s", firstName, lastName));
 	}
 
 	public void setLeasingAgentPhone(String phone, String ext) {
 		if (StringUtils.isNotBlank(ext)) {
 			setLeasingAgentPhone(String.format("%s x%s", phone, ext));
 		} else {
 			setLeasingAgentPhone(phone);
 		}
 	}
 
 }
