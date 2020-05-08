 package com.digt.model;
 
 import java.util.Date;
 import java.util.List;
 
 import org.codehaus.jackson.annotate.JsonIgnoreProperties;
 
 @JsonIgnoreProperties({"certificate", "data"})
 public interface CertificateTrustedList {
 
 	public byte[] getData();
 	public void setData(byte[] data);
 
	public String getId();
	public void setId(String id);

 	public String getUserId();
 	public void setUserId(String userId);
 
 	public Date getValidFrom();
 	public void setValidFrom(Date validFrom);
 
 	public Date getValidTo();
 	public void setValidTo(Date validTo);
 
 	public String getSignerDn();
 	public void setSignerDn(String signerDn);
 
 	public List<PersonCertificate> getCertificates();
 	public void setCertificates(List<PersonCertificate> certificates);
 	
 	public Date getCreated();
 	public void setCreated(Date created);
 }
