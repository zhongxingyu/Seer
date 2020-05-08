package com.pa165.bookingmanager.dto.impl;
 
 import java.io.Serializable;
 import java.util.List;
 
 /**
  * @author Josef Stribny
  */
 public class HotelDtoImpl  implements HotelDto, Serializable
 {
     /**
      * Id
      */
     private Long id;
 
     /**
      * Name
      */
     private String name;
 
     /**
      * Rooms by id
      */
     private List<RoomDto> roomsById;
 
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
      * Get name
      *
      * @return
      */
     public String getName() {
         return name;
     }
 
     /**
      * Set name
      *
      * @param name
      */
     public void setName(String name) {
         this.name = name;
     }
 
     /**
      * Get rooms by id
      *
      * @return rooms by id
      */
     public List<RoomDto> getRoomsById() {
         return roomsById;
     }
 
     /**
      * Set rooms by id
      *
      * @param roomsById
      */
     public void setRoomsById(List<RoomDto> roomsById) {
         this.roomsById = roomsById;
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
         if (!(o instanceof HotelDto)) return false;
 
         HotelDto hotelDto = (HotelDto) o;
 
         if (!id.equals(hotelDto.id)) return false;
         if (!name.equals(hotelDto.name)) return false;
         if (roomsById != null ? !roomsById.equals(hotelDto.roomsById) : hotelDto.roomsById != null) return false;
 
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
         result = 31 * result + name.hashCode();
         result = 31 * result + (roomsById != null ? roomsById.hashCode() : 0);
         return result;
     }
 
     /**
      * To string
      *
      * @return string representation of object
      */
     @Override
     public String toString() {
         return "HotelDto{" +
                 "id=" + id +
                 ", name='" + name + '\'' +
                 ", roomsById=" + roomsById +
                 '}';
     }
 }
