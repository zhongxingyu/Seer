 package org.jboss.jdf.example.ticketmonster.model;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.validation.Valid;
 import javax.validation.constraints.NotNull;
 
 import org.hibernate.validator.constraints.Email;
 import org.hibernate.validator.constraints.NotEmpty;
 
 /**
  * A Booking represents a set of tickets purchased for a performance.
  *
  * @author Marius Bogoevici
  */
 @Entity
 public class Booking {
 
     @Id
     @GeneratedValue
     private Long id;
 
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "booking")
     @NotEmpty
     @Valid
     private Set<Allocation> allocations = new HashSet<Allocation>();
 
     @ManyToOne
     private Performance performance;
 
     @NotEmpty
     private String cancellationCode;
 
     @NotNull
     private Date createdOn = new Date();
 
     @NotNull
     @NotEmpty
     @Email(message = "Not a valid email format")
     private String contactEmail;
 
     public Long getId() {
         return id;
     }
 
     public Set<Allocation> getAllocations() {
         return allocations;
     }
 
     public void setAllocations(Set<Allocation> allocations) {
         this.allocations = allocations;
     }
 
     public Date getCreatedOn() {
         return createdOn;
     }
 
     public void setCreatedOn(Date createdOn) {
         this.createdOn = createdOn;
     }
 
     public String getCancellationCode() {
         return cancellationCode;
     }
 
     public void setCancellationCode(String cancellationCode) {
         this.cancellationCode = cancellationCode;
     }
 
     public String getContactEmail() {
         return contactEmail;
     }
 
     public void setContactEmail(String contactEmail) {
         this.contactEmail = contactEmail;
     }
 
     public void addAllocation(Allocation allocation) {
         this.getAllocations().add(allocation);
         allocation.setBooking(this);
     }
 
     public Performance getPerformance() {
         return performance;
     }
 
     public void setPerformance(Performance performance) {
         this.performance = performance;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Booking booking = (Booking) o;
 
         if (id != null ? !id.equals(booking.id) : booking.id != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return id != null ? id.hashCode() : 0;
     }
 }
