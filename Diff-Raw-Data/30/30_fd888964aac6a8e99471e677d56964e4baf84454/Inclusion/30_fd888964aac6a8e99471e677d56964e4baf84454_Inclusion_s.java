 package net.mklew.hotelms.domain.booking.reservation.rates.inclusion;
 
 
import net.mklew.hotelms.domain.money.Money;
 
 /**
  * /**
  * @author Marek Lewandowski <marek.m.lewandowski@gmail.com>
  * @since 9/27/12
  *        Time: 12:30 PM
  *
  * Inclusions, also refered to as Guest Services or Facilities. When
  * called Guest Services they come as Complimentary Guest Services (
  * free) or Additional Guest Services (paid).
  *
  * Inclusion is like a offer for some product where Package is a
  * bundle of inclusions with policies and whole price. Inclusions are
  * just for some kind of product or service.
  * Example inclusion could be "Daily lunch" or "Drink upon arrival"
  * or "Chocolates upon check-out".
  */
 public class Inclusion
 {
     private String name;
     private String description;
     private Money price;
     private Discount discount;
     private CalculationRate calculationRate;
     private ServedUpon servedUpon;
     private String category;
     private Product product;
 
    Name
            Description
    Price
    Maximum Discount
    Calculation Rate:
        - per person
    - per adult
    - per child
    - per baby
    - per room
    ServedUpon:
        - Everyday
    - Everyday but Check-in
    - Everyday but Check-out
    - Check-in and Check-out
    - Only on Check-in
    - Only on Check-out
    - Once
    - Everyday but check-in and check-out
    - Quantity based
    Category
    - Food
    - Drink
    - Service
    - ...
    Product

 }
