 package cz.muni.fi.pompe.crental;
 
 import java.io.Serializable;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 
 /**
  * @author Roman Stehlik
  */
 @Entity
 @NamedQueries(value={
     @NamedQuery(name = "Car.SelectAllCars", query = "SELECT c FROM Car c"),
     @NamedQuery(name = "Car.SelectCarById", query = "SELECT c FROM Car c WHERE c.id = :id"),
    @NamedQuery(name = "Car.SelectCarByLicencePlate", query = "SELECT c FROM Car c WHERE c.licencePlate = :licencePlate")
 })
 public class Car implements Serializable {
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long id;
     private String carType;
     private String evidencePlate;
     
     
     public Car(){
         
         
     }
 
     public Long getId() {
         return id;
     }
 
     public void setCarType(String carType) {
         this.carType = carType;
     }
 
     public void setEvidencePlate(String evidencePlate) {
         this.evidencePlate = evidencePlate;
     }
 
     public String getCarType() {
         return carType;
     }
 
     public String getEvidencePlate() {
         return evidencePlate;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     @Override
     public int hashCode() {
         
         return (int) (id*17);
     }
 
     @Override
     public boolean equals(Object object) {
         // TODO: Warning - this method won't work in the case the id fields are not set
         if (!(object instanceof Car)) {
             return false;
         }
         Car other = (Car) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "id: "+ this.id +"; "+ "CarType: "+this.carType+"; "+"EvidencePlate: "+this.evidencePlate;
     }
     
 }
