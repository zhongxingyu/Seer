 package com.rathesh.codejam2012.server;
 
 import java.util.List;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.rathesh.codejam2012.server.strategies.GraphAdapter;
 
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
 
   public DataDump() {
     price = new DataList();
     smaSlow = new DataList();
     smaFast = new DataList();
     lwmaSlow = new DataList();
     lwmaFast = new DataList();
     emaSlow = new DataList();
     emaFast = new DataList();
     tmaSlow = new DataList();
     tmaFast = new DataList();
   }
 
   public void setPrices(List<Double> prices) {
     price.addAll(prices);
   }
 
   public void setSmaSlow(List<Double> smaSlows) {
    smaSlow = Lists.newArrayList();

     smaSlow.addAll(smaSlows);
 
    smaFast = Lists.newArrayList();
   }
 
   public void setSmaFast(List<Double> smaFasts) {
     smaFast.addAll(smaFasts);
   }
 
   public void setLwmaSlow(List<Double> lwmaSlows) {
    lwmaSlow = Lists.newArrayList();
 
     lwmaSlow.addAll(lwmaSlows);
   }
 
   public void setLwmaFast(List<Double> lwmaFasts) {
     lwmaFast.addAll(lwmaFasts);
   }
 
   public void setEmaSlow(List<Double> emaSlows) {
     emaSlow.addAll(emaSlows);
   }
 
   public void setEmaFast(List<Double> emaFasts) {
     emaFast.addAll(emaFasts);
   }
 
   public void setTmaSlow(List<Double> tmaSlows) {
     tmaSlow.addAll(tmaSlows);
   }
 
   public void setTmaFast(List<Double> tmaFasts) {
     tmaFast.addAll(tmaFasts);
   }
 
   @Override
   public String toString() {
     GsonBuilder builder = new GsonBuilder();
     builder.registerTypeAdapter(DataList.class, new GraphAdapter());
     Gson gson = builder.create();
     String json = "{";
     json = json +"\"price\":"+gson.toJson(price)+",";
     json = json +"\"smaSlow\":"+gson.toJson(smaSlow)+",";
     json = json +"\"smaFast\":"+gson.toJson(smaFast)+",";
     json = json +"\"lwmaSlow\":"+gson.toJson(lwmaSlow)+",";
     json = json +"\"lwmaFast\":"+gson.toJson(lwmaFast)+",";
     json = json +"\"emaSlow\":"+gson.toJson(emaSlow)+",";
     json = json +"\"emaFast\":"+gson.toJson(emaFast)+",";
     json = json +"\"tmaSlow\":"+gson.toJson(tmaSlow)+",";
     json = json +"\"tmaFast\":"+gson.toJson(tmaFast)+"}";
     
    
 
   }
 }
