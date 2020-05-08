 package com.pa165.bookingmanager.module.home.form;
 
 import java.util.Date;
 
 import javax.validation.constraints.Future;
 import javax.validation.constraints.NotNull;
 
 import org.hibernate.validator.constraints.NotEmpty;
 import org.springframework.format.annotation.DateTimeFormat;
 
 public class ReservationForm {
     /**
      * Id
      */
     private Long id;
 
     /**
      * Room room id
      */
     @NotNull
     private Long roomByRoomId;
 
     /**
      * Reservation from
      */
     @NotNull
     @Future
    @DateTimeFormat(pattern="MM/dd/yyyy")
     private Date reservationFrom;
 
     /**
      * Reservation to
      */
     @NotNull
     @Future
     @DateTimeFormat(pattern="MM/dd/yyyy")
     private Date reservationTo;
 
     /**
      * Customer name
      */
     @NotNull
     @NotEmpty
     private String customerName;
 
     /**
      * Customer email
      */
     @NotNull
     @NotEmpty
     private String customerEmail;
 
     /**
      * Customer phone
      */
     @NotNull
     @NotEmpty
     private String customerPhone;
 
     /**
      * Get id
      *
      * @return id
      */
     public Long getId() {
         return id;
     }
 
     /**
      * Set id
      *
      * @param id
      */
     public void setId(Long id) {
         this.id = id;
     }
 
     /**
      * Get room id
      *
      * @return room id
      */
     public Long getRoomByRoomId() {
         return roomByRoomId;
     }
 
     /**
      * Set room id
      *
      * @param roomByRoomId room id
      */
     public void setRoomByRoomId(Long roomByRoomId) {
         this.roomByRoomId = roomByRoomId;
     }
 
     /**
      * Get reservation from
      *
      * @return reservation from
      */
     public Date getReservationFrom() {
         return reservationFrom;
     }
 
     /**
      * Set reservation from
      *
      * @param reservationFrom reservation from
      */
     public void setReservationFrom(Date reservationFrom) {
         this.reservationFrom = reservationFrom;
     }
 
     /**
      * Get reservation to
      *
      * @return reservation to
      */
     public Date getReservationTo() {
         return reservationTo;
     }
 
     /**
      * Set reservation to
      *
      * @param reservationTo reservation to
      */
     public void setReservationTo(Date reservationTo) {
         this.reservationTo = reservationTo;
     }
 
     /**
      * Get customer name
      *
      * @return customer name
      */
     public String getCustomerName() {
         return customerName;
     }
 
     /**
      * Set customer name
      *
      * @param customerName customer name
      */
     public void setCustomerName(String customerName) {
         this.customerName = customerName;
     }
 
     /**
      * Get customer email
      *
      * @return customer email
      */
     public String getCustomerEmail() {
         return customerEmail;
     }
 
     /**
      * Set customer email
      *
      * @param customerEmail customer email
      */
     public void setCustomerEmail(String customerEmail) {
         this.customerEmail = customerEmail;
     }
 
     /**
      * Get customer phone
      *
      * @return customer phone
      */
     public String getCustomerPhone() {
         return customerPhone;
     }
 
     /**
      * Set customer phone
      *
      * @param customerPhone customer phone
      */
     public void setCustomerPhone(String customerPhone) {
         this.customerPhone = customerPhone;
     }
 }
