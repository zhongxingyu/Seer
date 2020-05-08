 package de.enwida.web.utils;
 
 import de.enwida.transport.DataResolution;
 
 public class NavigationDefaults {
     
     private int tsoId;
    private DataResolution resolution;
     private int product;
     private CalendarRange timeRange;
     
    public NavigationDefaults(int tsoId, DataResolution resolution, int product, CalendarRange timeRange) {
         this.tsoId = tsoId;
        this.resolution = resolution;
         this.product = product;
         this.timeRange = timeRange;
     }
 
     public int getTsoId() {
         return tsoId;
     }
 
     public void setTsoId(int tsoId) {
         this.tsoId = tsoId;
     }
 
    public DataResolution getResolution() {
        return resolution;
     }
 
    public void setResolution(DataResolution resoulution) {
        this.resolution = resoulution;
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
