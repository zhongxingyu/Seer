 package com.orangeleap.tangerine.domain;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.Locale;
 import java.util.TimeZone;
 
 import org.springframework.core.style.ToStringCreator;
 
 public class Site implements Serializable {
 
     private static final long serialVersionUID = 1L;
     
     public Site() {
         super();
     }
 
     public Site(String name) {
         super();
         this.name = name;
     }
 
     public Site(String name, String merchantNumber, String merchantBin) {
         this(name);
         this.merchantNumber = merchantNumber;
         this.merchantBin = merchantBin;
     }
 
     public Site(String name, String merchantNumber, String merchantBin, Site parentSite) {
         this(name, merchantNumber, merchantBin);
         this.parentSite = parentSite;
     }
 
     private String name;
     private String merchantNumber;
     private String merchantBin;
     private String localeString;
     private String timeZoneString;
     private boolean active = true;
     private Site parentSite;
     private Date createDate;
     private Date updateDate;
     private Integer achSiteNumber;
     private Integer achMerchantId;
     private Integer achRuleNumber;
     private String achCompanyName;
     private Integer achTestMode;
     private String  smtpServerName;
     private String  smtpAccountName;
     private String  smtpPassword;
     private String  smtpFromAddress;
    private Integer majorDonorAccountManagerId;
     
     public Locale getLocale() {
     	return localeString == null?Locale.getDefault():new Locale(localeString);
     }
 
     public TimeZone getTimeZone() {
     	return timeZoneString == null?TimeZone.getDefault():TimeZone.getTimeZone(timeZoneString);
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getMerchantNumber() {
         return merchantNumber;
     }
 
     public void setMerchantNumber(String merchantNumber) {
         this.merchantNumber = merchantNumber;
     }
 
     public String getMerchantBin() {
         return merchantBin;
     }
 
     public void setMerchantBin(String merchantBin) {
         this.merchantBin = merchantBin;
     }
 
     public Site getParentSite() {
         return parentSite;
     }
 
     public void setParentSite(Site parentSite) {
         this.parentSite = parentSite;
     }
 
     public Date getCreateDate() {
         return createDate;
     }
 
     public void setCreateDate(Date createDate) {
         this.createDate = createDate;
     }
 
     public Date getUpdateDate() {
         return updateDate;
     }
 
     public void setUpdateDate(Date updateDate) {
         this.updateDate = updateDate;
     }
 
 	public void setLocaleString(String localeString) {
 		this.localeString = localeString;
 	}
 
 	public String getLocaleString() {
 		return localeString;
 	}
 
 	public void setTimeZoneString(String timeZoneString) {
 		this.timeZoneString = timeZoneString;
 	}
 
 	public String getTimeZoneString() {
 		return timeZoneString;
 	}
 	
 	public void setActive(boolean active) {
 		this.active = active;
 	}
 
 	public boolean isActive() {
 		return active;
 	}
 
 	
     @Override
     public String toString() {
         return new ToStringCreator(this).append("name", name).append("merchantNumber", merchantNumber).append("merchantBin", merchantBin)
         		.append("localeString", localeString).append("timeZoneString", timeZoneString)
         		.append("parentSite", parentSite).append("createDate", createDate).append("updateDate", updateDate).toString();
     }
 
 	public Integer getAchSiteNumber() {
 		return achSiteNumber;
 	}
 
 	public void setAchSiteNumber(Integer achSiteNumber) {
 		this.achSiteNumber = achSiteNumber;
 	}
 
 	public Integer getAchMerchantId() {
 		return achMerchantId;
 	}
 
 	public void setAchMerchantId(Integer achMerchantId) {
 		this.achMerchantId = achMerchantId;
 	}
 
 	public Integer getAchRuleNumber() {
 		return achRuleNumber;
 	}
 
 	public void setAchRuleNumber(Integer achRuleNumber) {
 		this.achRuleNumber = achRuleNumber;
 	}
 
 	public String getAchCompanyName() {
 		return achCompanyName;
 	}
 
 	public void setAchCompanyName(String achCompanyName) {
 		this.achCompanyName = achCompanyName;
 	}
 
 	public boolean isAchTestMode() {
 		return achTestMode == 1;
 	}
 
 	public void setAchTestMode(Integer achTestMode) {
 		this.achTestMode = achTestMode;
 	}
 
 	public String getSmtpServerName() {
 		return smtpServerName;
 	}
 
 	public void setSmtpServerName(String smtpServerName) {
 		this.smtpServerName = smtpServerName;
 	}
 
 	public String getSmtpAccountName() {
 		return smtpAccountName;
 	}
 
 	public void setSmtpAccountName(String smtpAccountName) {
 		this.smtpAccountName = smtpAccountName;
 	}
 
 	public String getSmtpPassword() {
 		return smtpPassword;
 	}
 
 	public void setSmtpPassword(String smtpPassword) {
 		this.smtpPassword = smtpPassword;
 	}
 
 	public String getSmtpFromAddress() {
 		return smtpFromAddress;
 	}
 
 	public void setSmtpFromAddress(String smtpFromAddress) {
 		this.smtpFromAddress = smtpFromAddress;
 	}
 
 	public Integer getAchTestMode() {
 		return achTestMode;
 	}
 
	public Integer getMajorDonorAccountManagerId() {
		return majorDonorAccountManagerId;
	}

	public void setMajorDonorAccountManagerId(Integer majorDonorAccountManagerId) {
		this.majorDonorAccountManagerId = majorDonorAccountManagerId;
	}

 
 	
 }
