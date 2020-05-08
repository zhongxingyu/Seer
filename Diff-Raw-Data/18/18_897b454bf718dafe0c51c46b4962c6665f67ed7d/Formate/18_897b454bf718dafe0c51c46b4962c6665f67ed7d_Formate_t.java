 package jxt.util;
 
 /**
  * Created with IntelliJ IDEA.
  * User: karl
  * Date: 5/8/13
  * Time: 8:59 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Formate {
    public String forMate(String string) {
        String date ="";
        if (string != null) {
            if (string.length() != 0) {
                String year = string.substring(0, 4);    //year
                String month = string.substring(4, 6);   //month
                String day = string.substring(6, 8);     //day
                date = year + "-" + month + "-" + day;
            }
             return date;
        } else {
             return "";
         }
     }
 }
