 package com.cl.cx.platform.dto;
 
import com.fasterxml.jackson.annotation.JsonTypeInfo;
 import lombok.Data;
 
 @Data
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property= "@type")
 public class Action {
     private String href;
     private String method;
 
     public Action(String href,String method){
         this.href = href;
         this.method = method;
     }
 }
