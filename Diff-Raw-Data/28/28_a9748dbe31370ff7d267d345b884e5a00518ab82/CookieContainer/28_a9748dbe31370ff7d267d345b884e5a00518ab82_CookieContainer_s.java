 package info.joaolopes;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import java.util.TimeZone;
 
 
 /**
  *
  * @author João Lopes <mail at joaolopes.info>
  */
 public class CookieContainer {
     private class CookieInfo {
         public String domain;
         public String path;
         public Date expires;
         public Boolean secure;
         public String name;
         public String value;
         
         public CookieInfo(){
             this.domain = "";
             this.secure = false;
             this.path = "/";
             Calendar cal = Calendar.getInstance();
             cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + 10);
 
             this.expires = cal.getTime();
         }
     }
     
     
     
     
     private List<CookieInfo> cookiesInfo;
     
     public CookieContainer(){
         cookiesInfo = new ArrayList<>();
     }
     
     public void addCookie(String cookieString){
         addCookie(cookieString, "");
     }
     
     public void addCookie(String cookieString, String domain){
         CookieInfo ci = new CookieInfo();
         ci.domain = domain;
         String[] cookieData = cookieString.split("; ");
         
         for (String cookieD : cookieData) {
             String[] info = cookieD.split("=", 2);
             if(info[0].equalsIgnoreCase("path")){
                 ci.path = info[1];
             }else if( info[0].equalsIgnoreCase("httponly")){
                 ci.secure = true;
             }else if( info[0].equalsIgnoreCase("domain")){
                 ci.domain = info[1];
             }else if (info[0].equalsIgnoreCase("expires")){
                 SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss z", Locale.ENGLISH);
                 try {
                     ci.expires = sdf.parse(info[1]);
                 } catch (ParseException ex) {}
             }else{
                 ci.name = info[0];
                 ci.value = info[1];
             }
         }
         
         if(!hasCookie(ci.name, ci.domain)){
             cookiesInfo.add(ci);
         }else{
             cookiesInfo.set(getCookieNumber(ci.name, ci.domain), ci);
         }
         
         
     }
     
     private int getCookieNumber(String key, String domain){
         int i = 0;
         for (CookieInfo cookieInfo : cookiesInfo) {
             if(cookieInfo.name.equals(key)){
                 return i;
             }
             i++;
         }
         
         return -1;
     }
     
     @Override
     public String toString(){
         String cookies = "";
         for (CookieInfo cookieInfo : cookiesInfo) {
            cookies = cookies.concat(cookiesString(cookieInfo.domain));
         }
         return cookies;
     }
     private String cookiesString(String domain){
         String cookies = "";
         SimpleDateFormat df = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss z", Locale.ENGLISH);
         df.setTimeZone(TimeZone.getTimeZone("GMT"));
         for (CookieInfo cookieInfo : cookiesInfo) {
             if(cookieInfo.domain.equals(domain)){
                 cookies += cookieInfo.name + "=" + cookieInfo.value;
                 cookies += "; expires=" + df.format(cookieInfo.expires);
                 cookies += "; path=" + cookieInfo.path;
                 if(domain.length() > 0){
                     cookies += "; domain=" + cookieInfo.domain;
                 }
                 if(cookieInfo.secure){
                     cookies += "; httponly";
                 }
                 cookies += "\n";
             }
         }
         
         return cookies;
     }
     public boolean hasCookie(String name) { return hasCookie(name, ""); }
     public boolean hasCookie(String name, String domain){
         for (CookieInfo cookieInfo : cookiesInfo) {
             if(cookieInfo.domain.equals(domain)){
                 if(cookieInfo.name.equals(name)) return true;
             }
         }
         return false;
     }
     
     public String getHeaderCookies(String domain){
         if(isEmpty()) return "";
         String cookies = "";
         for (CookieInfo cookieInfo : cookiesInfo) {
             if(new Date().before(cookieInfo.expires)){
                 cookies += cookieInfo.name + "=" + cookieInfo.value + "; ";
             }
         }
         cookies = cookies.substring(0, cookies.length() - 2);
         
         return cookies;
     }
     
     public void clear(){
         this.cookiesInfo.clear();
     }
     
     public boolean isEmpty(){
         return cookiesInfo.isEmpty();
     }
     
     public List<String> keys(){
         return keys("");
     }
     public List<String> keys(String domain){
         List<String> toReturn = new ArrayList<>();
         for (CookieInfo cookieInfo : cookiesInfo) {
             if(cookieInfo.domain.equals(domain)){
                 toReturn.add(cookieInfo.name);
             }
         }
         return toReturn;
     }
     
     public String read(String key){
         return read(key, null);
     }
     
     public String read(String key, String domain){
         for (CookieInfo cookieInfo : cookiesInfo) {
             if(domain != null){
                 if(cookieInfo.domain.equals(domain)){
                     if(cookieInfo.name.equals(key)){
                         return cookieInfo.value;
                     }
                 }
             }else{
                 String val = this.read(key, cookieInfo.domain);
                 if(val != null){
                     return val;
                 }
             }
         }
         return null;
     }
     
     
     public void remove(String key){
         remove(key, null);
     }
     
     public void remove(String key, String domain){
         SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss z", Locale.ENGLISH);
         for (CookieInfo cookieInfo : cookiesInfo) {
             if(domain != null){
                 if(cookieInfo.domain.equals(domain)){
                     if(cookieInfo.name.equals(key)){
                         try {
                             cookieInfo.expires = sdf.parse("Thu, 01 Jan 1970 00:00:00 GMT");
                         } catch (ParseException ex) {}
                     }
                 }
             }else{
                 this.remove(key, cookieInfo.domain);
             }
         }
     }
     
    
     
 }
