 package com.aciertoteam.mail.entity;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 
 import com.aciertoteam.common.entity.AbstractEntity;
 
 /**
  * @author Bogdan Nechyporenko
  */
 @Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"CONF_KEY", "validThru"})})
 public class MailConfiguration extends AbstractEntity {
 
     private static final long serialVersionUID = 3056671598908776995L;
 
     @Column(name = "CONF_KEY", nullable = false)
     private String key;
 
     @Column(name = "CONF_VALUE")
     private String value;
 
     @Column(name = "DEFAULT_VALUE", nullable = false)
     private String defaultValue;
 
     public MailConfiguration() {
     }
 
     public MailConfiguration(String key, String value) {
         this.key = key;
         this.value = value;
         this.defaultValue = value;
     }
 
     public MailConfiguration(String key, String value, String defaultValue) {
         this.key = key;
         this.value = value;
         this.defaultValue = defaultValue;
     }
 
     public String getKey() {
         return key;
     }
 
     public void setKey(String key) {
         this.key = key;
     }
 
     public String getValue() {
         return value;
     }
 
     public void setValue(String value) {
         this.value = value;
     }
 
     public String getDefaultValue() {
         return defaultValue;
     }
 
     public void setDefaultValue(String defaultValue) {
         this.defaultValue = defaultValue;
     }
 }
