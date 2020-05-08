 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.smartitengineering.demo.roa.domains;
 
 import com.smartitengineering.domain.AbstractPersistentDTO;
 import java.util.Date;
 import org.apache.commons.lang.StringUtils;
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 /**
  *
  * @author imyousuf
  */
 public class Author extends AbstractPersistentDTO<Author> {
 
   public static final String UNIQUE_NICK_NAME = "uniqueNickname";
 
   private String nationalId;
   private String name;
   private String uniqueNickname;
   private Date lastModifiedDate;
 
   @JsonIgnore
   public Date getLastModifiedDate() {
     return lastModifiedDate;
   }
 
   @JsonIgnore
   public void setLastModifiedDate(Date lastModifiedDate) {
     this.lastModifiedDate = lastModifiedDate;
   }
 
   public String getName() {
     return name;
   }
 
   public void setName(String name) {
     this.name = name;
   }
 
   public String getNationalId() {
     return nationalId;
   }
 
   public void setNationalId(String nationalId) {
     this.nationalId = nationalId;
   }
 
   public String getUniqueNickname() {
     return uniqueNickname;
   }
 
   public void setUniqueNickname(String uniqueNickname) {
     this.uniqueNickname = uniqueNickname;
   }
 
   @Override
   public boolean isValid() {
     return StringUtils.isNotBlank(uniqueNickname) && StringUtils.isNotBlank(name);
   }
 }
