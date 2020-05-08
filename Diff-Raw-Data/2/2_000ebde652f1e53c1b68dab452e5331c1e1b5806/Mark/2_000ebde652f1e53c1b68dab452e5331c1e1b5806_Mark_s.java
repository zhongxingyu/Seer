 package com.ngdb;
 
 import static org.apache.commons.lang.StringUtils.remove;
 
 public class Mark {
 
     private double starsAsDouble;
     private String nativeValue;
     public static final Mark ZERO = new Mark("0");
 
     public Mark(String nativeValue) {
         this.nativeValue = nativeValue;
         if(nativeValue.contains("%")) {
             starsAsDouble = convertPercents();
         } else if (nativeValue.contains("/")) {
             starsAsDouble = convertQuotients();
         }
     }
 
     private double convertQuotients() {
         String sup = nativeValue.split("/")[0];
         sup = sup.replace(',','.');
         Double supAsDouble = Double.valueOf(sup);
 
         double valueFrom0To5;
         int inf = Integer.valueOf(nativeValue.split("/")[1]);
         if(inf == 5) {
             valueFrom0To5 = supAsDouble;
         } else {
             valueFrom0To5 = (supAsDouble/inf)*5;
         }
         return round(valueFrom0To5);
     }
 
     private double convertPercents() {
         double value = Double.parseDouble(nativeValue.replaceAll("%", ""));
         value -= 50;
         if(value < 0) {
             value = 0;
         }
         double valueFrom0To5 = value / 10;
         return round(valueFrom0To5);
     }
 
     private Double round(Double value) {
         Double v = new Double(value.intValue());
         double diff = value - v;
         if (diff < 0.5) {
             value = v;
         } else if (diff >= 0.5) {
             value = v + 0.5;
         }
         return value;
     }
 
     public double getStarsAsDouble() {
         return starsAsDouble;
     }
 
     public String toString() {
         if(starsAsDouble == 0) {
             return "00";
         }
         int i = (int) (starsAsDouble * 10);
         if(i < 10) {
             return "0"+i;
         }
         return i +"";
     }
 
     public int getAsPercent() {
         if (nativeValue == null) {
             return 0;
         }
         if (nativeValue.contains("%")) {
             return Integer.valueOf(remove(nativeValue, '%'));
         } else if (nativeValue.contains("/")) {
             Double numerator = Double.valueOf(nativeValue.split("/")[0]);
             Double denominator = Double.valueOf(nativeValue.split("/")[1]);
             return (int) ((numerator / denominator) * 100);
         }
         return 0;
     }
 
     public String getStars() {
        String mark = nativeValue;
         if (mark.length() == 1) {
             mark = "0" + mark;
         }
         int numStars = Integer.valueOf("" + mark.charAt(0));
         boolean halfStar = "5".equals("" + mark.charAt(1));
         String stars = "";
         int numGreyStars = 5 - numStars;
         for (int i = 0; i < numStars; i++) {
             stars += "<img width='20px' src='/img/stars/star.png'>";
         }
         if (halfStar) {
             stars += "<img width='20px' src='/img/stars/half_star.png'>";
             numGreyStars--;
         }
         for (int i = 0; i < numGreyStars; i++) {
             stars += "<img width='20px' src='/img/stars/grey_star.png'>";
         }
         return stars;
     }
 
 }
