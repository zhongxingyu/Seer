 package com.korwe.kordapt.api.bean;
 
 import com.google.common.base.Joiner;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author <a href="mailto:tjad.clark@korwe.com>Tjad Clark</a>
  * */
 public class ServiceFunction {
     private String name;
     private String description;
     private List<ServiceFunctionParameter> parameters;
     private Type returnType;
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public List<ServiceFunctionParameter> getParameters() {
         return parameters;
     }
 
     public void setParameters(List<ServiceFunctionParameter> parameters) {
         this.parameters = parameters;
     }
 
     public Type getReturnType() {
         return returnType;
     }
 
     public void setReturnType(Type returnType) {
         this.returnType = returnType;
     }
 
     public Boolean getMultiParam(){
         return this.parameters != null && this.parameters.size() > 1;
     }
 }
