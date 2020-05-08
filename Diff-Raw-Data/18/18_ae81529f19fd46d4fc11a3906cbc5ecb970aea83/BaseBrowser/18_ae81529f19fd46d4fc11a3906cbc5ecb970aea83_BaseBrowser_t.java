 package gc.carbon;
 
 import com.jellymold.utils.ThreadBeanHolder;
 import gc.carbon.domain.path.PathItem;
 import gc.carbon.domain.profile.StartEndDate;
 
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.Date;
 
 import org.joda.time.DateTime;
 
 public abstract class BaseBrowser implements Serializable {
 
     protected PathItem pathItem;
    private final StartEndDate epoch = new StartEndDate();
 
     protected StartEndDate startDate;
     protected StartEndDate endDate;
 
     public BaseBrowser() {
         super();
         setPathItem((PathItem) ThreadBeanHolder.get("pathItem"));
     }
 
     public void setPathItem(PathItem pathItem) {
         this.pathItem = pathItem;
     }
 
     public PathItem getPathItem() {
         return pathItem;
     }
 
     public void setStartDate(String date) {
         startDate = new StartEndDate(date, false);
     }
 
     public void setEndDate(String date) {
         if (date != null)
             endDate = new StartEndDate(date, false);
     }
 
     public StartEndDate getStartDate() {
         return (startDate != null ? startDate : epoch);
     }
 
     public StartEndDate getEndDate() {
         return endDate;
     }
 
     public void setEndDate(Date endDate) {
         if (endDate != null)
             this.endDate = new StartEndDate(endDate);
     }
 
     public void setStartDate(Date startDate) {
         this.startDate = new StartEndDate(startDate);
     }
 
     public boolean isQuery() {
         return (startDate != null) || (endDate != null);
     }
 }
