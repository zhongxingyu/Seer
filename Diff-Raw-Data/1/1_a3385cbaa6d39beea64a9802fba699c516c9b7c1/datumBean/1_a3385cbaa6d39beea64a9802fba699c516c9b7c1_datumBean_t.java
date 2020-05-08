 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package beans;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 
 /**
  *
  * @author Robbie Vercammen
  */
 @ManagedBean(name="datum")
 @RequestScoped
 public class datumBean {
     private String strBegin, strEind;
     private Date dtBegin, dtEind;
     private Calendar calBegin, calEind;
 
     public datumBean() {
     }
 
     public Calendar getCalBegin() {
         return calBegin;
     }
 
     public void setCalBegin(Calendar calBegin) {
         this.calBegin = calBegin;
     }
 
     public Calendar getCalEind() {
         return calEind;
     }
 
     public void setCalEind(Calendar calEind) {
         this.calEind = calEind;
     }
 
     public String getStrBegin() {
         return strBegin;
     }
 
     public void setStrBegin(String strBegin) {
         this.strBegin = strBegin;
     }
 
     public String getStrEind() {
         return strEind;
     }
 
     public void setStrEind(String strEind) {
         this.strEind = strEind;
     }    
     
     public long getDuur() throws IllegalArgumentException, ParseException {
         try {
             createCalendars(strBegin, strEind);
             long lDagenBegin = calBegin.getTimeInMillis();
             long lDagenEinde = calEind.getTimeInMillis();
             long lDiff = lDagenEinde - lDagenBegin;
             long lDuur = lDiff / (24 * 60 * 60 * 1000);
             return lDuur;
         } catch (IllegalArgumentException ia) {
             throw ia;
         } catch (ParseException pe) {
             throw pe;
         }
     }
     
     public void createCalendars(String strBegin, String strEind) throws ParseException {
         DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");
         Date tempDatum = formatter.parse(strBegin);
         calBegin = Calendar.getInstance();
         calBegin.setTime(tempDatum);
         tempDatum = formatter.parse(strEind);
         calEind = Calendar.getInstance();
         calEind.setTime(tempDatum);
     }
 }
