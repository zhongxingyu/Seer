 package org.jboss.jdf.example.ticketmonster.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.ElementCollection;
 import javax.persistence.Entity;
import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

 /**
  * An allocation consists of one or more contiguous sold seats
  * within a SectionRow.
  *
  * @author Shane Bryzak
  * @author Marius Bogoevici
  */
@Entity @JsonIgnoreProperties("booking")
 public class Allocation implements Serializable {
     private static final long serialVersionUID = 8738724150877088864L;
 
     @Id
     @GeneratedValue
     private Long id;
 
     @ManyToOne
     private Row row;
 
     @ManyToOne
     private Booking booking;
 
    @ElementCollection(fetch = FetchType.EAGER)
     public List<AllocationTicketCategoryCount> ticketsPerCategory = new ArrayList<AllocationTicketCategoryCount>();
 
     private int startSeat;
 
     private int endSeat;
 
 
     private int quantity;
 
     public Long getId() {
         return id;
     }
 
     public Row getRow() {
         return row;
     }
 
     public void setRow(Row row) {
         this.row = row;
     }
 
     public int getQuantity() {
         return quantity;
     }
 
     public void setQuantity(int quantity) {
         this.quantity = quantity;
     }
 
     public int getStartSeat() {
         return startSeat;
     }
 
     public void setStartSeat(int startSeat) {
         this.startSeat = startSeat;
     }
 
     public int getEndSeat() {
         return endSeat;
     }
 
     public void setEndSeat(int endSeat) {
         this.endSeat = endSeat;
     }
 
     public Booking getBooking() {
         return booking;
     }
 
     public void setBooking(Booking booking) {
         this.booking = booking;
     }
 
     public List<AllocationTicketCategoryCount> getTicketsPerCategory() {
         return ticketsPerCategory;
     }
 
     public void setTicketsPerCategory(List<AllocationTicketCategoryCount> ticketsPerCategory) {
         this.ticketsPerCategory = ticketsPerCategory;
     }
 
 }
