 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 import java.util.Vector;
 
 /**
  *
  * @author dionysis
  */
 public class polyLineEncoder {
     private Vector<point> points=new Vector<point>();
     private String encoded_points="";    
     public polyLineEncoder() {
         points.clear();
         encoded_points=""; 
     }
     
     public void addPoint(double lat,double lon){
         points.add(new point(lat,lon));
     }
     
     public void encode(){
         point prevPoint=new point(0.0,0.0);
         for (int i=0;i<points.size();i++){
            encoded_points+=encodePoint(prevPoint,points.get(i)); 
            prevPoint=new point(points.get(i));
         }
 //        encoded_points += encodePoint(prevPoint,points.get(0));
     }
     public void clear(){
         points.clear();
         encoded_points=""; 
     }
     
     private String encodePoint(point prevPoint,point Point){
         long  late5 = Math.round(Point.lat * 1e5);
         long  plate5 = Math.round(prevPoint.lat * 1e5);
         long lnge5 = Math.round(Point.lon * 1e5);
         long plnge5 = Math.round(prevPoint.lon * 1e5);
         long dlng=lnge5 - plnge5;
         long dlat = late5 - plate5;
         
         return encodeSignedNumber(dlat) + encodeSignedNumber(dlng);
     }
     
     private String encodeSignedNumber(long num){
         long sgn_num = (num << 1);
          if (num < 0) {
              sgn_num = ~sgn_num;
          }
           return(encodeNumber(sgn_num));   
     }
     
     private String encodeNumber(long num){
         String encodeString = "";
          while (num >= 0x20) {
              char c=(char)((0x20 | (num & 0x1f)) + 63);
              encodeString+=c;
              num >>= 5;
          }
          char c2=(char)(num + 63);
          encodeString += (c2);
         return encodeString;
     }
     
 
     @Override
     public String toString() {
         return encoded_points;
     }
     
     
     
     private class point{
         double lat,lon;
 
         public point(double lat,double lon){
             this.lat=lat;
             this.lon=lon;
         }
           public point(point p){
               this.lat=p.lat;
               this.lon=p.lon;
           }
         
         
         public double getLat() {
             return lat;
         }
 
         public double getLon() {
             return lon;
         }
         
     }
     
 }
