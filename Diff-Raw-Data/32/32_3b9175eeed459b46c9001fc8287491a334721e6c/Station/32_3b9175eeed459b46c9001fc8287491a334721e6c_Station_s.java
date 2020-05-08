 package tedx.mpk;
 
 public class Station {
 
    private long latitude;
    private long longitude;
     private String name;
 
     public Station() {
 
     }
 
    public long getLatitude() {
         return latitude;
     }
 
    public void setLatitude(long latitude) {
         this.latitude = latitude;
     }
 
    public long getLongitude() {
         return longitude;
     }
 
    public void setLongitude(long longitude) {
         this.longitude = longitude;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 }
