 package com.aciertoteam.geo.entity;
 
 import com.aciertoteam.common.entity.AbstractEntity;
 import com.aciertoteam.common.utils.ContractEqualsBuilder;
 import org.apache.commons.lang3.builder.HashCodeBuilder;
 import org.codehaus.jackson.annotate.JsonAutoDetect;
 import org.codehaus.jackson.annotate.JsonIgnoreProperties;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 
 /**
  * @author Bogdan Nechyporenko
  */
 @Entity
 @JsonAutoDetect
 @JsonIgnoreProperties(ignoreUnknown = true)
 public class Language extends AbstractEntity {
 
     @Column(unique = true, nullable = false)
     private String code;
 
     @Column(unique = true, nullable = false)
     private String englishName;
 
    @Column(unique = true, nullable = false)
     private String nativeLanguageName;
 
     Language() {
         //hibernate
     }
 
     public Language(String code, String englishName, String nativeLanguageName) {
         this.code = code;
         this.englishName = englishName;
         this.nativeLanguageName = nativeLanguageName;
     }
 
     public String getCode() {
         return code;
     }
 
     public void setCode(String code) {
         this.code = code;
     }
 
     public String getEnglishName() {
         return englishName;
     }
 
     public void setEnglishName(String englishName) {
         this.englishName = englishName;
     }
 
     public String getNativeLanguageName() {
         return nativeLanguageName;
     }
 
     public void setNativeLanguageName(String nativeLanguageName) {
         this.nativeLanguageName = nativeLanguageName;
     }
 
     @Override
     public final boolean equals(Object obj) {
         return ContractEqualsBuilder.isEquals(this, obj, "code", "englishName", "nativeLanguageName");
     }
 
     @Override
     public final int hashCode() {
         return new HashCodeBuilder().append(code).append(englishName).append(nativeLanguageName).toHashCode();
     }
 
     @Override
     public String toString() {
         return "Language{code='" + code + "\', englishName='" + englishName + "\', nativeLanguageName='" + nativeLanguageName + "\'}";
     }
 }
