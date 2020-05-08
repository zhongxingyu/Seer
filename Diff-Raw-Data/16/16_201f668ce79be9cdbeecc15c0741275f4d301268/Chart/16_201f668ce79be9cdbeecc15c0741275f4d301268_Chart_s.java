 package com.ryliu.j2ee.lab02.shop;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpSession;
 
 public class Chart {
 
    private final List<Good> goods = new ArrayList<Good>();
 
     private long idCount = 0;
 
     public List<Good> getGoods() {
         return goods;
     }
 
     public void addGood(Good good) {
         good.setId(idCount++);
         goods.add(good);
     }
 
     public void removeGood(Good good) {
         goods.remove(good);
     }
 
     public void clear() {
         goods.clear();
     }
 
     public double getAmount() {
         double res = 0;
         for (Good good : goods) {
             res += good.getPrice();
         }
         return res;
     }
 
     public static Chart getChartFromSession(HttpSession session) {
         Chart chart;
         if (session.getAttribute("chart") == null) {
             chart = new Chart();
             session.setAttribute("chart", chart);
         } else {
             chart = (Chart) session.getAttribute("chart");
         }
         return chart;
     }
 }
