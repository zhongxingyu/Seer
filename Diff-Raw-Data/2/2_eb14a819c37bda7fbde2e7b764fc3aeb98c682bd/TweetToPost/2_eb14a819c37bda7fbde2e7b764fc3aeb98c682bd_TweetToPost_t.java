 package pt.isel.pdm.yamba.model;
 
import java.util.Date;
 
 public class TweetToPost {
 
     private Date _date;
     private String _text;
     
     public TweetToPost(Date date, String text){
         _date = date;
         _text = text;
     }
     public Date getDate(){
         return _date;
     }
     public String getText(){
         return _text;
     }
 }
