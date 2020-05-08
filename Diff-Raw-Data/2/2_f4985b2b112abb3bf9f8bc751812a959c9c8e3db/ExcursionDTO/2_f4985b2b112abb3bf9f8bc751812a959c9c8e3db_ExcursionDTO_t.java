 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.muni.fi.pa165.jtravelagency.dto;
 
 
 import java.math.BigDecimal;
 import java.util.List;
 import org.joda.time.DateTime;
 
 /**
  *
  * @author Peter Petrinec
  */
 public class ExcursionDTO {
     private Long id;
     private DateTime excursionDate;
     private String description;
     private BigDecimal price;
     private TripDTO trip;
 
     public TripDTO getTrip() {
         return trip;
     }
 
     public void setTrip(TripDTO trip) {
         this.trip = trip;
     }
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public DateTime getExcursionDate() {
         return excursionDate;
     }
 
     public void setExcursionDate(DateTime excursionDate) {
         this.excursionDate = excursionDate;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public BigDecimal getPrice() {
         return price;
     }
 
     public void setPrice(BigDecimal price) {
         this.price = price;
     }
 
     @Override
     public String toString() {
        return "ExcursionDTO{" + "id=" + id + ", excursionDate=" + excursionDate + ", description=" + description + ", price=" + price + "}";
     }
 
     @Override
     public int hashCode() {
         int hash = 5;
         hash = 97 * hash + (this.id != null ? this.id.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final ExcursionDTO other = (ExcursionDTO) obj;
         if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 }
