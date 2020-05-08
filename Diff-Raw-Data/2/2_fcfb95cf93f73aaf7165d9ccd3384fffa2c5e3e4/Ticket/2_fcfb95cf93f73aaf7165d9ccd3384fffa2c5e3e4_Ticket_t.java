 /**
  * Swift Ticket -- Back End
  *
  * Copyright (C) 2013, Jonathan Gillett, Daniel Smullen, and Rayan Alfaheid
  * All rights reserved.
  *
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /**
 * Ticket Provides methods and attributes for interacting with tickets,
  * which are entries representing events stored in the available tickets file.
  * 
  * @author Jonathan Gillett
  * @author Daniel Smullen
  * @author Rayan Alfaheid
  */
 public class Ticket
 {
     private String event;
     private String seller;
     private Integer volume;
     private Double price;
 
 
     /**
      * Constructor for the class. Accepts and populates class attributes with values passed in.
      * 
      * @param event String for the title of the event.
      * @param volume Integer for the amount of tickets available for the event.
      * @param price Dollar value for the price per ticket.
      */
     public Ticket(String event, String seller, Integer volume, Double price)
     {
         this.event = event;
         this.seller = seller;
         this.volume = volume;
         this.price = price;
     }
 
     /**
      * getEvent Access method for event titles.
      * 
      * @return Returns a string containing the title for the event.
      */
     public String getEvent()
     {
         return event;
     }
 
     /**
      * getSeller Access method for the ticket seller.
      * 
      * @return Returns a string containing the seller for the event.
      */
     public String getSeller()
     {
         return seller;
     }
     
     
     /**
      * getVolume Access method for the volume of tickets available.
      * 
      * @return Returns an integer for the volume of tickets available.
      */
     public Integer getVolume()
     {
         return this.volume;
     }
 
     /**
      * setVolume Mutator method which allows the volume of tickets available
      * to be manipulated.
      * 
      * @param volume An integer to set the volume of tickets to.
      */
     public void setVolume(Integer volume)
     {
         this.volume = volume;
     }
 
     
     /**
      * getPrice Access method for the price per ticket.
      * 
      * @return Returns a dollar value containing the price per ticket.
      */
     public Double getPrice()
     {
         return this.price;
     }
 
     /**
      * setPrice Mutator method which allows the price per ticket to be
      * manipulated.
      * 
      * @param price The amount, in dollars, to set the ticket price to.
      */
     public void setPrice(Double price)
     {
         this.price = price;
     }
 
 }
