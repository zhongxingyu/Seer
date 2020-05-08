 package de.enwida.web.utils;
 
 import de.enwida.transport.DataResolution;
 
 public class NavigationDefaults {
     
     private int tsoId;
    private DataResolution resoulution;
     private int product;
     private CalendarRange timeRange;
     
    public NavigationDefaults(int tsoId, DataResolution resoulution, int product, CalendarRange timeRange) {
         this.tsoId = tsoId;
        this.resoulution = resoulution;
         this.product = product;
         this.timeRange = timeRange;
     }
 
     public int getTsoId() {
         return tsoId;
     }
 
     public void setTsoId(int tsoId) {
         this.tsoId = tsoId;
     }
 
    public DataResolution getResoulution() {
        return resoulution;
     }
 
    public void setResoulution(DataResolution resoulution) {
        this.resoulution = resoulution;
     }
 
     public int getProduct() {
         return product;
     }
 
     public void setProduct(int product) {
         this.product = product;
     }
 
     public CalendarRange getTimeRange() {
         return timeRange;
     }
 
     public void setTimeRange(CalendarRange timeRange) {
         this.timeRange = timeRange;
     }
     
 
 }
