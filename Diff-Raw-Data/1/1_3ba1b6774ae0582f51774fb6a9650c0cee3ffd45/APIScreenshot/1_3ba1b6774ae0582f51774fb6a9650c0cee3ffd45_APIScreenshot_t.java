 package com.testdroid.api.model;
 
 import com.testdroid.api.APIEntity;
 import com.testdroid.api.APIException;
 import java.io.InputStream;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 /**
  *
  * @author Łukasz Kajda <lukasz.kajda@bitbar.com>
  */
 @XmlRootElement
 public class APIScreenshot extends APIEntity {
     
     @XmlType(namespace = "APIScreenshot")
     public static enum Type { LANDSCAPE, PORTRAIT }
     
     private String originalName;
     private Boolean fail;
     private Type type;
     private Long takeTimestamp;
 
     public APIScreenshot() {}
 
     public APIScreenshot(Long id, String originalName, Boolean fail, Type type, Long takeTimestamp) {
         super(id);
         this.originalName = originalName;
         this.fail = fail;
         this.type = type;
         this.takeTimestamp = takeTimestamp;
     }
 
     public String getOriginalName() {
         return originalName;
     }
 
     public void setOriginalName(String originalName) {
         this.originalName = originalName;
     }
 
     public Boolean isFail() {
         return fail;
     }
 
     public void setFail(Boolean fail) {
         this.fail = fail;
     }
 
     public Type getType() {
         return type;
     }
 
     public void setType(Type type) {
         this.type = type;
     }
 
     public Long getTakeTimestamp() {
         return takeTimestamp;
     }
 
     public void setTakeTimestamp(Long takeTimestamp) {
         this.takeTimestamp = takeTimestamp;
     }
     
    @JsonIgnore
     public InputStream getContent() throws APIException {
         return getFile(selfURI);
     }
 
     @Override
     @JsonIgnore
     protected <T extends APIEntity> void clone(T from) {
         APIScreenshot apiScreenshot = (APIScreenshot) from;
         cloneBase(from);
         this.fail = apiScreenshot.fail;
         this.originalName = apiScreenshot.originalName;
         this.type = apiScreenshot.type;
         this.takeTimestamp = apiScreenshot.takeTimestamp;
     }
     
 }
