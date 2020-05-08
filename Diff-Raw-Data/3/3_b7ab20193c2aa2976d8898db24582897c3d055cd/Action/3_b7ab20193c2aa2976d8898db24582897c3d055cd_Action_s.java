 package com.cl.cx.platform.dto;
 
 import lombok.Data;
 
 @Data
 public class Action {
     private String href;
     private String method;
 
     public Action(String href,String method){
         this.href = href;
         this.method = method;
     }
 }
