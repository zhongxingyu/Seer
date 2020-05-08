 package org.jboss.jdf.example.ticketmonster.model;
 
 import static javax.persistence.CascadeType.ALL;
 import static javax.persistence.FetchType.EAGER;
 import static javax.persistence.GenerationType.IDENTITY;
 
 import java.io.Serializable;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.OrderBy;
 
 import org.hibernate.validator.constraints.NotEmpty;
 
 
 /**
 * A show is an instance of an event taking place at a particular venue. A show can have multiple
  * performances.
  *
  * @author Shane Bryzak
  */
 @Entity
 public class Show implements Serializable {
 
     private static final long serialVersionUID = -108405033615497885L;
 
     @Id
     @GeneratedValue(strategy=IDENTITY)
     private Long id;
 
     @ManyToOne
     private Event event;
 
     @ManyToOne
     private Venue venue;
 
     @OneToMany(fetch = EAGER, mappedBy = "show", cascade = ALL)
     @OrderBy("date asc")
     @NotEmpty
     private Set<Performance> performances = new HashSet<Performance>();
 
     @OneToMany(mappedBy = "show", cascade = ALL, fetch = EAGER)
     @NotEmpty
     private Set<PriceCategory> priceCategories = new HashSet<PriceCategory>();
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
 
     public Event getEvent() {
         return event;
     }
 
     public void setEvent(Event event) {
         this.event = event;
     }
 
     public Set<Performance> getPerformances() {
         return performances;
     }
 
     public void setPerformances(Set<Performance> performances) {
         this.performances = performances;
     }
 
     public Venue getVenue() {
         return venue;
     }
 
     public void setVenue(Venue venue) {
         this.venue = venue;
     }
 
     public Set<PriceCategory> getPriceCategories() {
         return priceCategories;
     }
 
     public void setPriceCategories(Set<PriceCategory> priceCategories) {
         this.priceCategories = priceCategories;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Show show = (Show) o;
 
         if (event != null ? !event.equals(show.event) : show.event != null) return false;
         if (venue != null ? !venue.equals(show.venue) : show.venue != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = event != null ? event.hashCode() : 0;
         result = 31 * result + (venue != null ? venue.hashCode() : 0);
         return result;
     }
 
     @Override
     public String toString() {
         return event + " at " + venue;
     }
 }
