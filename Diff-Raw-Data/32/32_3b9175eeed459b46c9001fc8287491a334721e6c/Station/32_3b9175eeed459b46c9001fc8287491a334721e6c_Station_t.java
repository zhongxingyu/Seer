 package tedx.mpk;
 
 public class Station {
 
    private int latitude;
    private int longitude;
     private String name;
 
     public Station() {
 
     }
 
    public int getLatitude() {
         return latitude;
     }
 
    public void setLatitude(int latitude) {
         this.latitude = latitude;
     }
 
    public int getLongitude() {
         return longitude;
     }
 
    public void setLongitude(int longitude) {
         this.longitude = longitude;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 }
