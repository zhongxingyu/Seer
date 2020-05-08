 package parkhaus;
 
 /**
  *
  * @author Simon
  */
 public class Parkhaus {
 
     private String Parkhaus_Name;
     private int Hoehe_in_cm, Stellplaetze;
 
    public Parkhaus() {
     }
 
     public void setStellpleatze(int Stellpleatze) {
         this.Stellplaetze = Stellpleatze;
     }
 
     public int getStellpleatze() {
         return Stellplaetze;
     }
 
     public String getParkhaus_Name() {
         return Parkhaus_Name;
     }
 
     public int getHoehe_in_cm() {
         return Hoehe_in_cm;
     }
 
     public void setParkhaus_Name(String Parkhaus_Name) {
         this.Parkhaus_Name = Parkhaus_Name;
     }
 
     public void setHoehe_in_cm(int Hoehe_in_cm) {
         this.Hoehe_in_cm = Hoehe_in_cm;
     }
 
     public void setStellplaetze(int Stellplaetze) {
         this.Stellplaetze = Stellplaetze;
     }
 }
