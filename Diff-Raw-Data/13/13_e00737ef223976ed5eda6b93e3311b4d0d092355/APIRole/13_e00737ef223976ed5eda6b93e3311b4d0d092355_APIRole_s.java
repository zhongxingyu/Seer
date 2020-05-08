 package com.testdroid.api.model;
 
 import com.testdroid.api.APIEntity;
 import java.util.Date;
 import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonValue;
 
 /**
  *
  * @author kajdus
  */
 @XmlRootElement
 public class APIRole extends APIEntity {
     private String name;
     private Integer useLimit;
     private Date expireTime;
 
     public APIRole() {}
     public APIRole(Long id, String name, Integer useLimit, Date expireTime) {
         super(id);
         this.name = name;
         this.useLimit = useLimit;
         this.expireTime = expireTime;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public Integer getUseLimit() {
         return useLimit;
     }
 
     public void setUseLimit(Integer useLimit) {
         this.useLimit = useLimit;
     }
 
     public Date getExpireTime() {
         return expireTime;
     }
 
     public void setExpireTime(Date expireTime) {
         this.expireTime = expireTime;
     }
 
     public boolean isLimited() {
         return getUseLimit() != null;
     }
     
     public boolean isLimitUsed() {
         return getUseLimit() != null && getUseLimit() <= 0;
     }
 
 }
