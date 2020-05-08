package com.pa165.bookingmanager.dto.impl;
 
 import java.io.Serializable;
 import java.util.Date;
 
 /**
  * @author Josef Stribny
  */
 public class ReservationDtoImpl  implements ReservationDto, Serializable
 {
     /**
      * Id
      */
     private Long id;
 
     /**
      * Rooms by room id
      */
     private RoomDto roomByRoomId;
 
     /**
      * Reservation from
      */
     private Date reservationFrom;
 
     /**
      * Reservation to
      */
     private Date reservationTo;
 
     /**
      * Customer name
      */
     private String customerName;
 
     /**
      * Customer email
      */
     private String customerEmail;
 
     /**
      * Customer phone
      */
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
      * Get room by room id
      *
      * @return room by room id
      */
     public RoomDto getRoomByRoomId() {
         return roomByRoomId;
     }
 
     /**
      * Set room by room id
      *
      * @param roomByRoomId room by room id
      */
     public void setRoomByRoomId(RoomDto roomByRoomId) {
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
 
     /**
      * Equals
      *
      * @param o object
      * @return boolean
      */
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof ReservationDto)) return false;
 
         ReservationDto that = (ReservationDto) o;
 
         if (!customerEmail.equals(that.customerEmail)) return false;
         if (!customerName.equals(that.customerName)) return false;
         if (!customerPhone.equals(that.customerPhone)) return false;
         if (!id.equals(that.id)) return false;
         if (!reservationFrom.equals(that.reservationFrom)) return false;
         if (!reservationTo.equals(that.reservationTo)) return false;
         if (!roomByRoomId.equals(that.roomByRoomId)) return false;
 
         return true;
     }
 
     /**
      * Hash code
      *
      * @return hash code
      */
     @Override
     public int hashCode() {
         int result = id.hashCode();
         result = 31 * result + roomByRoomId.hashCode();
         result = 31 * result + reservationFrom.hashCode();
         result = 31 * result + reservationTo.hashCode();
         result = 31 * result + customerName.hashCode();
         result = 31 * result + customerEmail.hashCode();
         result = 31 * result + customerPhone.hashCode();
         return result;
     }
 
     /**
      * To string
      *
      * @return string representation of object
      */
     @Override
     public String toString() {
         return "ReservationDto{" +
                 "id=" + id +
                 ", roomByRoomId=" + roomByRoomId +
                 ", reservationFrom=" + reservationFrom +
                 ", reservationTo=" + reservationTo +
                 ", customerName='" + customerName + '\'' +
                 ", customerEmail='" + customerEmail + '\'' +
                 ", customerPhone='" + customerPhone + '\'' +
                 '}';
     }
 }
