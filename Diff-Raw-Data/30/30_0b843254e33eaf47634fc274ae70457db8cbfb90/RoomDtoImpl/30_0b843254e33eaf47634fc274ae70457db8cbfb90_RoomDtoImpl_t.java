package com.pa165.bookingmanager.dto.impl;
 
 import java.io.Serializable;
 import java.math.BigDecimal;
 import java.util.List;
 
 /**
  * @author Josef Stribny
  */
 public class RoomDtoImpl  implements RoomDto, Serializable
 {
     /**
      * Id
      */
     private Long id;
 
     /**
      * Number
      */
     private String number;
 
     /**
      * Hotel by hotel id
      */
     private HotelDto hotelByHotelId;
 
     /**
      * Price
      */
     private BigDecimal price;
 
     /**
      * Reservation by id
      */
     private List<ReservationDto> reservationsById;
 
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
      * Get number
      *
      * @return number
      */
     public String getNumber() {
         return number;
     }
 
     /**
      * Set number
      *
      * @param number
      */
     public void setNumber(String number) {
         this.number = number;
     }
 
     /**
      * Get hotel by hotel id
      *
      * @return hotel by hotel id
      */
     public HotelDto getHotelByHotelId() {
         return hotelByHotelId;
     }
 
     /**
      * Set hotel by hotel id
      *
      * @param hotelByHotelId hotel by hotel id
      */
     public void setHotelByHotelId(HotelDto hotelByHotelId) {
         this.hotelByHotelId = hotelByHotelId;
     }
 
     /**
      * Get price
      *
      * @return price
      */
     public BigDecimal getPrice() {
         return price;
     }
 
     /**
      * Set price
      *
      * @param price
      */
     public void setPrice(BigDecimal price) {
         this.price = price;
     }
 
     /**
      * Get reservations by id
      *
      * @return reservations by id
      */
     public List<ReservationDto> getReservationsById() {
         return reservationsById;
     }
 
     /**
      * Set reservations by id
      *
      * @param reservationsById reservations by id
      */
     public void setReservationsById(List<ReservationDto> reservationsById) {
         this.reservationsById = reservationsById;
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
         if (!(o instanceof RoomDto)) return false;
 
         RoomDto roomDto = (RoomDto) o;
 
         if (!hotelByHotelId.equals(roomDto.hotelByHotelId)) return false;
         if (!id.equals(roomDto.id)) return false;
         if (!number.equals(roomDto.number)) return false;
         if (!price.equals(roomDto.price)) return false;
         if (!reservationsById.equals(roomDto.reservationsById)) return false;
 
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
         result = 31 * result + number.hashCode();
         result = 31 * result + hotelByHotelId.hashCode();
         result = 31 * result + price.hashCode();
         result = 31 * result + reservationsById.hashCode();
         return result;
     }
 
     /**
      * To string
      *
      * @return string representation of object
      */
     @Override
     public String toString() {
         return "RoomDto{" +
                 "id=" + id +
                 ", number='" + number + '\'' +
                 ", hotelByHotelId=" + hotelByHotelId +
                 ", price=" + price +
                 ", reservationsById=" + reservationsById +
                 '}';
     }
 }
