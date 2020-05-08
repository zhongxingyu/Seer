 package Global.mechanics;
 
 import Global.Ticket;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Author: artemlobachev
  * Date: 07.12.13
  */
 public class TicketImp implements Ticket {
 
     public TicketImp(String owner, Collection<SingleTicket> singleTickets, boolean fromDB) {
         super();
         this.owner = owner;
         this.isTemporary = !fromDB;
         this.tickets = new ArrayList<>();
         for (SingleTicket ticket : singleTickets) {
             this.tickets.add(ticket);
         }
     }
 
     public TicketImp(Collection<SingleTicket> singleTickets, boolean fromDB) {
         super();
         this.owner = null;
         this.isTemporary = !fromDB;
         this.tickets = new ArrayList<>();
         for (SingleTicket ticket : singleTickets) {
             this.tickets.add(ticket);
         }
     }
 
     public void save(){
         //TODO: send message to database
     }
     public void saved(){
         this.isTemporary = false;
     }
 
     public void changeOwner(String newOwner){
       //TODO:send message to database
     }
 
     @Override
     public String getOwner() {
         return this.owner;
     }
 
     @Override
     public String getDepartureAirport() {
         return this.tickets.get(0).getDepartureAirport();
     }
 
     @Override
     public Date getDepartureDateTime() {
         return this.tickets.get(0).getDepartureTime();
     }
 
     @Override
     public String getArrivalAirport() {
         return this.tickets.get(this.tickets.size() - 1).getArrivalAirport();
     }
 
     @Override
     public long getFlightTime() {
         return this.tickets.get(this.tickets.size() - 1).getDepartureTime().getTime() + this.tickets.get(this.tickets.size() - 1).getFlightTime() - this.tickets.get(0).getDepartureTime().getTime();
     }
 
     @Override
     public List<SingleTicket> getRoute() {
        return new ArrayList<>(this.tickets);  //TODO
     }
 
     @Override
     public seatClass getMinSeatClass() {
         Ticket.seatClass rez = seatClass.SEAT_CLASS_FIRST;
         for(SingleTicket ticket:this.tickets){
             if (ticket.getSeatClass().equals(seatClass.SEAT_CLASS_BUSINESS)) {
                 rez = seatClass.SEAT_CLASS_BUSINESS;
             }
         }
         for(SingleTicket ticket:this.tickets){
             if (ticket.getSeatClass().equals(seatClass.SEAT_CLASS_ECONOMIC)) {
                 rez = seatClass.SEAT_CLASS_ECONOMIC;
             }
         }
         return rez;
     }
 
     @Override
     public int getPrice() {
         int rez = 0;
         for (SingleTicket singleTicket:this.tickets){
             rez += singleTicket.getPrice();
         }
         return rez;
     }
 
     @Override
     public boolean isTemporary() {
         return this.isTemporary;
     }
 
     protected ArrayList<SingleTicket> tickets;
    private String owner;
     private boolean isTemporary;
 }
