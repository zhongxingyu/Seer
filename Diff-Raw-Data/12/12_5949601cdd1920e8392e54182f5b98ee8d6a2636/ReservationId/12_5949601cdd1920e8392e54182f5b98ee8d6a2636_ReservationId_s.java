 package net.mklew.hotelms.domain.booking.reservation;
 
/**
 * @author: Marek Lewandowski <marek.m.lewandowski@gmail.com>
 * @since: 9/9/12
 * time 7:49 PM
 */
 
 import net.mklew.hotelms.domain.booking.Id;
 
 /**
 * Identifies reservation.
  *
  */
 public abstract class ReservationId
 {
     protected final Group group;
 
     public ReservationId(Group group)
     {
         this.group = group;
     }
 
     /**
      * Returns Id of reservation. This is either Id of single reservation or Id of group reservation.
      * @return Id of reservation
      */
     public abstract Id getId();
 
     /**
      * Returns group
      * @return Group if reservation belongs to group. Special case NoGroup otherwise
      */
     public Group getGroup()
     {
         return group;
     }
 }
