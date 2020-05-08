 package com.tp.dto;
 
 import org.apache.commons.lang3.builder.ToStringBuilder;
 import org.dozer.Mapping;
 
 /**
  * User: ken.cui
  * Date: 13-6-5
  * Time: 下午5:21
  */
 public class LogCoopDTO {
 
     private String imei;
     private String imsi;
     private String v;
     private String op;
     private String fm;
     private String net;
     private String l;
     private String app;
     private String r;
     private String dt;
     private String ct;
     private String price;
     private String charge;
     private String deduct;
     private String model;
     private String chargeTime;
 
     public String getImei() {
         return imei;
     }
 
     public void setImei(String imei) {
         this.imei = imei;
     }
 
     public String getImsi() {
         return imsi;
     }
 
     public void setImsi(String imsi) {
         this.imsi = imsi;
     }
 
 
     @Mapping("clientVersion")
 
     public String getV() {
         return v;
     }
 
     public void setV(String v) {
         this.v = v;
     }
 
     @Mapping("operators")
     public String getOp() {
         return op;
     }
 
     public void setOp(String op) {
         this.op = op;
     }
 
     @Mapping("fromMarket")
     public String getFm() {
         return fm;
     }
 
     public void setFm(String fm) {
         this.fm = fm;
     }
 
     @Mapping("resolution")
     public String getR() {
         return r;
     }
 
     public void setR(String r) {
         this.r = r;
     }
 
     @Mapping("netEnv")
     public String getNet() {
         return net;
     }
 
     public void setNet(String net) {
         this.net = net;
     }
 
     @Mapping("language")
     public String getL() {
         return l;
     }
 
     public void setL(String l) {
         this.l = l;
     }
 
     @Mapping("appName")
     public String getApp() {
         return app;
     }
 
     public void setApp(String app) {
         this.app = app;
     }
 
     @Mapping("doType")
     public String getDt() {
         return dt;
     }
 
     public void setDt(String dt) {
         this.dt = dt;
     }
 
     @Mapping("clientType")
     public String getCt() {
         return ct;
     }
 
     public void setCt(String ct) {
         this.ct = ct;
     }
 
     public String getPrice() {
         return price;
     }
 
     public void setPrice(String price) {
         this.price = price;
     }
 
     public String getCharge() {
         return charge;
     }
 
     public void setCharge(String charge) {
         this.charge = charge;
     }
 
     public String getDeduct() {
         return deduct;
     }
 
     public void setDeduct(String deduct) {
         this.deduct = deduct;
     }
 
     public String getModel() {
         return model;
     }
 
     public void setModel(String model) {
         this.model = model;
     }
 
     public String getChargeTime() {
         return chargeTime;
     }
 
     public void setChargeTime(String chargeTime) {
         this.chargeTime = chargeTime;
     }
 
     @Override
     public String toString() {
         return ToStringBuilder.reflectionToString(this);
     }
 }
