 package stockwatch.securities;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class Option extends FutureContract {
     Date expirationDate;
     static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
     
     public Date getExpirationDate() {
         return expirationDate;
     }
 
     @Override
     public void setExpirationDate(String expirationDate) {
         try {
             this.expirationDate = dateFormat.parse(expirationDate);
         } catch (ParseException e) {
            this.expirationDate = new Date();
            this.expirationDate.setTime(0);
         }
     }
     
     @Override
     public String toString() {
         return String.format(OUTPUT_FORMAT, super.securityName) + " " 
             + String.format(OUTPUT_FORMAT, super.securityId) + " " 
             + String.format(OUTPUT_FORMAT, dateFormat.format(expirationDate)) + " "
             + String.format(OUTPUT_FORMAT, super.openPrice) + " " 
             + String.format(OUTPUT_FORMAT, super.lastTransactionPrice) + " "
             + String.format(OUTPUT_FORMAT, super.lastChanged) + " "
             + String.format(OUTPUT_FORMAT, super.lop) + " "
             + String.format(OUTPUT_FORMAT, super.lopChange) + " "
             + String.format(OUTPUT_FORMAT, super.volume) + " "
             + String.format(OUTPUT_FORMAT, super.percentageChange) + "%";
     }
 }
