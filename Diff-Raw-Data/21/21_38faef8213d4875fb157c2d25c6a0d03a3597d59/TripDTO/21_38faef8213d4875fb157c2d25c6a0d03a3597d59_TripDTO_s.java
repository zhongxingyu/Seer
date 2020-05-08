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
  * @author jakub
  */
 public class TripDTO {
 
     private Long id;
 
     private DateTime dateFrom;
 
     private DateTime dateTo;
 
     private String destination;
 
     private int availableTrips;
 
     private BigDecimal price;
     
    private List<ExcursionDTO> excursions;

    public List<ExcursionDTO> getExcursions() {
        return excursions;
    }

    public void setExcursions(List<ExcursionDTO> excursions) {
        this.excursions = excursions;
    }
 
     public TripDTO() {
     }
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public DateTime getDateTo() {
         return dateTo;
     }
 
     public void setDateTo(DateTime dateTo) {
         this.dateTo = dateTo;
     }
 
     public String getDestination() {
         return destination;
     }
 
     public void setDestination(String destination) {
         this.destination = destination;
     }
 
     public DateTime getDateFrom() {
         return dateFrom;
     }
 
     public void setDateFrom(DateTime dateFrom) {
         this.dateFrom = dateFrom;
     }
 
     public int getAvailableTrips() {
         return availableTrips;
     }
 
     public void setAvailableTrips(int availableTrips) {
         this.availableTrips = availableTrips;
     }
 
     public BigDecimal getPrice() {
         return price;
     }
 
     public void setPrice(BigDecimal price) {
         this.price = price;
     }
 
     @Override
     public int hashCode() {
         int hash = 5;
         hash = 79 * hash + (this.id != null ? this.id.hashCode() : 0);
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
         final TripDTO other = (TripDTO) obj;
         if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
        return "TripDTO{" + "id=" + id + ", dateFrom=" + dateFrom + ", dateTo=" + dateTo + ", destination=" + destination + ", availableTrips=" + availableTrips + ", price=" + price + ", excursions=" + excursions + '}';
     }
 
 }
