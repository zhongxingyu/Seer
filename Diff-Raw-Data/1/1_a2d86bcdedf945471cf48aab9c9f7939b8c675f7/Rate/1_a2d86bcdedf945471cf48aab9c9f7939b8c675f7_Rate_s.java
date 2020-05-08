 package net.mklew.hotelms.domain.booking.reservation.rates;
 
 import net.mklew.hotelms.domain.room.Room;
 import org.joda.money.Money;
 
 /**
  * @author Marek Lewandowski <marek.m.lewandowski@gmail.com>
  * @since 9/27/12
  *        Time: 11:22 AM
  */
 public abstract class Rate
 {
     protected Long id; // hibernate
     protected Money standardPrice;
     protected Money upchargeExtraPerson;
     protected Money upchargeExtraBed;
     protected Room room;
 
     public Rate(Money standardPrice, Money upchargeExtraPerson, Money upchargeExtraBed, Room room)
     {
         this.standardPrice = standardPrice;
         this.upchargeExtraPerson = upchargeExtraPerson;
         this.upchargeExtraBed = upchargeExtraBed;
     }
 
     public abstract String getRateName();
 
     public Money standardPrice()
     {
         return standardPrice;
     }
 
     public Money upchargeExtraPerson()
     {
         return upchargeExtraPerson;
     }
 
     public Money upchargeExtraBed()
     {
         return upchargeExtraBed;
     }
 
     public Room getRoom()
     {
         return room;
     }
 
     // hibernate
     protected void setStandardPrice(Money standardPrice)
     {
         this.standardPrice = standardPrice;
     }
 
     // hibernate
     protected void setUpchargeExtraPerson(Money upchargeExtraPerson)
     {
         this.upchargeExtraPerson = upchargeExtraPerson;
     }
 
     // hibernate
     protected void setUpchargeExtraBed(Money upchargeExtraBed)
     {
         this.upchargeExtraBed = upchargeExtraBed;
     }
 
     // hibernate
     protected void setRoom(Room room)
     {
         this.room = room;
     }
 
     // hibernate
     protected Money getStandardPrice()
     {
         return standardPrice;
     }
 
     // hibernate
     protected Money getUpchargeExtraPerson()
     {
         return upchargeExtraPerson;
     }
 
     // hibernate
     protected Money getUpchargeExtraBed()
     {
         return upchargeExtraBed;
     }
 
     protected Long getId()
     {
         return id;
     }
 
     protected void setId(Long id)
     {
         this.id = id;
     }
 
     // hibernate
     Rate()
     {
         // hibernate
     }
 }
