 package com.rathesh.codejam2012.server;
 
 import java.util.List;
 
 import com.google.common.collect.Lists;
 import com.google.gson.Gson;
 
 public class DataDump {
   List<double[]> price;
   List<double[]> smaSlow;
   List<double[]> smaFast;
 
   List<double[]> lwmaSlow;
   List<double[]> lwmaFast;
 
   List<double[]> emaSlow;
   List<double[]> emaFast;
 
   List<double[]> tmaSlow;
   List<double[]> tmaFast;
   
   public DataDump() {
     price = Lists.newArrayList();
     smaSlow = Lists.newArrayList();
     smaFast = Lists.newArrayList();
     lwmaSlow = Lists.newArrayList();
     lwmaFast = Lists.newArrayList();
     emaSlow = Lists.newArrayList();
     emaFast = Lists.newArrayList();
     tmaSlow = Lists.newArrayList();
     tmaFast = Lists.newArrayList();
   }
   
   public List<double[]> getPrice() {
     return price;
   }
   
   public void setPrices(List<Double> prices) {
    price = Lists.newArrayList();
     for (int i = 0; i < prices.size(); i++) {
       double[] point = new double[2];
       point[0] = i + 1;
       point[1] = prices.get(i);
       price.add(point);
     }
   }
 
   public List<double[]> getSmaSlow() {
     return smaSlow;
   }
 
   public void setSmaSlow(List<Double> smaSlows) {
    smaSlow = Lists.newArrayList();
     for (int i = 0; i < smaSlows.size(); i++) {
       double[] point = new double[2];
       point[0] = i + 1;
       point[1] = smaSlows.get(i);
       smaSlow.add(point);
     }
   }
 
   public List<double[]> getSmaFast() {
     return smaFast;
   }
 
   public void setSmaFast(List<Double> smaFasts) {
    smaFast = Lists.newArrayList();
     for (int i = 0; i < smaFasts.size(); i++) {
       double[] point = new double[2];
       point[0] = i + 1;
       point[1] = smaFasts.get(i);
       smaFast.add(point);
     }
   }
 
   public List<double[]> getLwmaSlow() {
     return lwmaSlow;
   }
 
   public void setLwmaSlow(List<Double> lwmaSlows) {
    lwmaSlow = Lists.newArrayList();
     for (int i = 0; i < lwmaSlows.size(); i++) {
       double[] point = new double[2];
       point[0] = i + 1;
       point[1] = lwmaSlows.get(i);
       lwmaSlow.add(point);
     }
   }
 
   public List<double[]> getLwmaFast() {
     return lwmaFast;
   }
 
   public void setLwmaFast(List<Double> lwmaFasts) {
    lwmaFast = Lists.newArrayList();
     for (int i = 0; i < lwmaFasts.size(); i++) {
       double[] point = new double[2];
       point[0] = i + 1;
       point[1] = lwmaFasts.get(i);
       lwmaFast.add(point);
     }
   }
 
   public List<double[]> getEmaSlow() {
     return emaSlow;
   }
 
   public void setEmaSlow(List<Double> emaSlows) {
    emaSlow = Lists.newArrayList();
     for (int i = 0; i < emaSlows.size(); i++) {
       double[] point = new double[2];
       point[0] = i + 1;
       point[1] = emaSlows.get(i);
       emaSlow.add(point);
     }
   }
 
   public List<double[]> getEmaFast() {
     return emaFast;
   }
 
   public void setEmaFast(List<Double> emaFasts) {
    emaFast = Lists.newArrayList();
     for (int i = 0; i < emaFasts.size(); i++) {
       double[] point = new double[2];
       point[0] = i + 1;
       point[1] = emaFasts.get(i);
       emaFast.add(point);
     }
   }
 
   public List<double[]> getTmaSlow() {
     return tmaSlow;
   }
 
   public void setTmaSlow(List<Double> tmaSlows) {
    tmaSlow = Lists.newArrayList();
     for (int i = 0; i < tmaSlows.size(); i++) {
       double[] point = new double[2];
       point[0] = i + 1;
       point[1] = tmaSlows.get(i);
       tmaSlow.add(point);
     }
   }
 
   public List<double[]> getTmaFast() {
     return tmaFast;
   }
 
   public void setTmaFast(List<Double> tmaFasts) {
    tmaFast = Lists.newArrayList();
     for (int i = 0; i < tmaFasts.size(); i++) {
       double[] point = new double[2];
       point[0] = i + 1;
       point[1] = tmaFasts.get(i);
       tmaFast.add(point);
     }
   }
 
   @Override
   public String toString() {
     Gson gson = new Gson();
    return gson.toJson(this);
   }
 }
