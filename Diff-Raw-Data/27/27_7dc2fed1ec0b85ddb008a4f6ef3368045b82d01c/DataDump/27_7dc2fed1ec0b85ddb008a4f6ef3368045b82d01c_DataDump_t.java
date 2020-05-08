 package com.rathesh.codejam2012.server;
 
 import java.util.List;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 public class DataDump {
   
   
   DataList price;
   DataList smaSlow;
   DataList smaFast;
 
   DataList lwmaSlow;
   DataList lwmaFast;
 
   DataList emaSlow;
   DataList emaFast;
 
   DataList tmaSlow;
   DataList tmaFast;
   
   boolean finished;
 
   public DataDump() {
     price = new DataList(0);
     smaSlow = new DataList(0);
     smaFast = new DataList(0);
     lwmaSlow = new DataList(0);
     lwmaFast = new DataList(0);
     emaSlow = new DataList(0);
     emaFast = new DataList(0);
     tmaSlow = new DataList(0);
     tmaFast = new DataList(0);
     finished = false;
   }
   public void setTime(int time){
     price.setTime(time);
     smaSlow.setTime(time);
     smaFast.setTime(time);
     lwmaSlow.setTime(time);
     lwmaFast.setTime(time);
     emaSlow.setTime(time);
     emaFast.setTime(time);
     tmaSlow.setTime(time);
     tmaFast.setTime(time);
     
   }
   public void setFinished(boolean finished) {
     this.finished = true;
   }
 
   public void setPrices(List<Double> prices) {
     price.set(prices);
   }
 
   public void setSmaSlow(List<Double> smaSlows) {
 
     smaSlow.set(smaSlows);
 
   }
 
   public void setSmaFast(List<Double> smaFasts) {
     smaFast.set(smaFasts);
   }
 
   public void setLwmaSlow(List<Double> lwmaSlows) {
 
     lwmaSlow.set(lwmaSlows);
   }
 
   public void setLwmaFast(List<Double> lwmaFasts) {
     lwmaFast.set(lwmaFasts);
   }
 
   public void setEmaSlow(List<Double> emaSlows) {
     emaSlow.set(emaSlows);
   }
 
   public void setEmaFast(List<Double> emaFasts) {
     emaFast.set(emaFasts);
   }
 
   public void setTmaSlow(List<Double> tmaSlows) {
     tmaSlow.set(tmaSlows);
   }
 
   public void setTmaFast(List<Double> tmaFasts) {
     tmaFast.set(tmaFasts);
   }
 
   @Override
   public String toString() {
     GsonBuilder builder = new GsonBuilder();
     builder.registerTypeAdapter(DataList.class, new GraphAdapter());
     Gson gson = builder.create();
     String priceJson = gson.toJson(price);
     String smaSlowJson = gson.toJson(smaSlow);
     String smaFastJson = gson.toJson(smaFast);
     String lwmaSlowJson = gson.toJson(lwmaSlow);
     String lwmaFastJson = gson.toJson(lwmaFast);
     String emaSlowJson = gson.toJson(emaSlow);
     String emaFastJson = gson.toJson(emaFast);
     String tmaSlowJson = gson.toJson(tmaSlow);
     String tmaFastJson = gson.toJson(tmaFast);
 
     String json = "{";
     json = json + "\"price\":" + priceJson.substring(1, priceJson.length() - 1) + ",";
     json = json + "\"smaSlow\":" + smaSlowJson.substring(1, smaSlowJson.length() - 1) + ",";
     json = json + "\"smaFast\":" + smaFastJson.substring(1, smaFastJson.length() - 1) + ",";
     json = json + "\"lwmaSlow\":" + lwmaSlowJson.substring(1, lwmaSlowJson.length() - 1) + ",";
     json = json + "\"lwmaFast\":" + lwmaFastJson.substring(1, lwmaFastJson.length() - 1) + ",";
     json = json + "\"emaSlow\":" + emaSlowJson.substring(1, emaSlowJson.length() - 1) + ",";
     json = json + "\"emaFast\":" + emaFastJson.substring(1, emaFastJson.length() - 1) + ",";
     json = json + "\"tmaSlow\":" + tmaSlowJson.substring(1, tmaSlowJson.length() - 1) + ",";
     json = json + "\"tmaFast\":" + tmaFastJson.substring(1, tmaFastJson.length() - 1) + ",";
     json = json + "\"finished\":" + this.finished + "}";
 
     return json;
 
   }
 }
