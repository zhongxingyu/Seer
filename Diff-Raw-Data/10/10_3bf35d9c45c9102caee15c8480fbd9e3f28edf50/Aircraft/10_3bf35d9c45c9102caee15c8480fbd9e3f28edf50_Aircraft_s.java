 package net.taviscaron.airliners.model;
 
 import java.io.Serializable;
 
 /**
  * Aircraft base entity class
  * @author Andrei Senchuk
  */
 public class Aircraft implements Serializable {
     protected String id;
     protected String aircraft;
     protected String airline;
     protected String reg;
     protected String cn;
     protected String code;
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getAircraft() {
         return aircraft;
     }
 
     public void setAircraft(String aircraft) {
         this.aircraft = aircraft;
     }
 
     public String getAirline() {
         return airline;
     }
 
     public void setAirline(String airline) {
         this.airline = airline;
     }
 
     public String getReg() {
         return reg;
     }
 
     public void setReg(String reg) {
         this.reg = reg;
     }
 
     public String getCn() {
         return cn;
     }
 
     public void setCn(String cn) {
         this.cn = cn;
     }
 
     public String getCode() {
         return code;
     }
 
     public void setCode(String code) {
         this.code = code;
     }
 
     public String fullReg() {
         StringBuilder sb = new StringBuilder();
 
         if(reg != null) {
             sb.append(reg);
         }
 
         if(code != null) {
             if(sb.length() > 0) {
                 sb.append(" / ");
             }
             sb.append(code);
         }
 
         if(cn != null) {
             sb.append(String.format("(cn %s)", cn));
         }
 
         return sb.toString();
     }
 }
