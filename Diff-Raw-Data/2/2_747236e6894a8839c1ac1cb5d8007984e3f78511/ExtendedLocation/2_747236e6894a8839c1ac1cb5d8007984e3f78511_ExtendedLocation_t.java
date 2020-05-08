 package no.api.meteo.entity.extras.locationgroup;
 
 import no.api.meteo.entity.core.Location;
 
 public class ExtendedLocation extends Location {
 
     private String name;
 
     public ExtendedLocation(String name, Double longitude, Double latitude, Double altitude) {
         super(longitude, latitude, altitude);
         this.name = name;
     }
 
     public String getName() {
         return name;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) {
             return true;
         }
         if (o == null || getClass() != o.getClass()) {
             return false;
         }
         if (!super.equals(o)) {
             return false;
         }
 
         ExtendedLocation that = (ExtendedLocation) o;
 
         if (name != null ? !name.equals(that.name) : that.name != null) {
             return false;
         }
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = super.hashCode();
        result = HASH_CODE * result + (name != null ? name.hashCode() : 0);
         return result;
     }
 }
