 package Global.mechanics;
 
 import Global.*;
 import Global.MsgSystem.Abonent;
 import Global.MsgSystem.Messages.MsgBuyTicket;
 import Global.MsgSystem.Messages.MsgFindTicket;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import static java.lang.Thread.sleep;
 
 /**
  * Author: artemlobachev
  * Date: 14.12.13
  */
 public class MechanicSalesImp implements MechanicSales, Abonent, Runnable {
     protected static final long OWERFLOW = -1;
     protected static final int SLEEP_TIME = 1;
     protected final Address address;
     protected final MessageSystem ms;
     private final ConcurrentHashMap<Long, Boolean> foundTicketStatuses = new ConcurrentHashMap<>();
     private final ConcurrentHashMap<Long, HashSet<SingleTicket>> foundTicketResults = new ConcurrentHashMap<>();
     private final ConcurrentHashMap<Long, Boolean> buyRequestsStatuses = new ConcurrentHashMap<>();
     private final ConcurrentHashMap<Long, Boolean> BuyRequestsResults = new ConcurrentHashMap<>();
 
     public MechanicSalesImp(MessageSystem ms) {
         super();
         this.ms = ms;
         this.address = new Address();
         this.addMeToAddressService();
         this.ms.addService(this);
     }
 
     @Override
     public MessageSystem getMessageSystem() {
         return this.ms;
     }
 
     /**
      *
      * @param params Map of pairs MechanicSales.findParams/values
      * @return null if nothing found, Collection of Ticket objects otherwithe
      */
     @Override
     public Collection<Ticket> search(Map<String, String> params) {
         //TODO make real search: building tree, etc
         long requestId = this.findFreeKey(this.foundTicketStatuses);
         this.foundTicketStatuses.put(requestId, false);
         MsgFindTicket msg = new MsgFindTicket(this.address, this.ms.getAddressService().getAccountService(), params, requestId);
         this.ms.sendMessage(msg);
         while (!this.foundTicketStatuses.get(requestId)){
             try {
                 Thread.sleep(10);
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         }
         if (!this.foundTicketResults.get(requestId).isEmpty()){
             ArrayList<SingleTicket> singleTickets = new ArrayList<>();
             singleTickets.add(this.foundTicketResults.get(requestId).iterator().next());
             Ticket tmpTicket = new TicketImp(singleTickets,false);
             ArrayList<Ticket> rez = new ArrayList<>();
             rez.add(tmpTicket);
             //removing used values
             this.foundTicketStatuses.remove(requestId);
             this.foundTicketResults.remove(requestId);
             return rez;
         }
         //removing used values
         this.foundTicketStatuses.remove(requestId);
         this.foundTicketResults.remove(requestId);
         return null;
     }
 
     @Override
     public boolean buy(Ticket ticket, User passenger) {
         long requestId = this.findFreeKey(this.buyRequestsStatuses);
         MsgBuyTicket msg = new MsgBuyTicket(this.address, this.ms.getAddressService().getAccountService(), ticket, passenger, requestId);
         this.buyRequestsStatuses.put(requestId, false);
         this.ms.sendMessage(msg);
         while (!this.buyRequestsStatuses.get(requestId)){
             try {
                 Thread.sleep(10);
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         }
         boolean result = this.BuyRequestsResults.get(requestId);
         this.BuyRequestsResults.remove(requestId);
         this.buyRequestsStatuses.remove(requestId);
         return result;
     }
 
     @Override
     public void itemsFound(long requestId, Collection<SingleTicket> tickets) {
         //TODO: check if has answer
         HashSet<SingleTicket> singleTickets = new HashSet<>();
         for(SingleTicket ticket:tickets){
             singleTickets.add(ticket);
         }
         this.foundTicketResults.put(requestId, singleTickets);
         this.foundTicketStatuses.put(requestId, true);
     }
 
     @Override
     public void itemBought(long requestId, boolean result) {
         this.BuyRequestsResults.put(requestId,result);
         this.buyRequestsStatuses.put(requestId,true);
     }
 
     @Override
     public Address getAddress() {
         return this.address;
     }
 
     @Override
     public void run() {
         while (true) {
             this.ms.execForAbonent(this);
 
             try {
                 sleep(SLEEP_TIME);
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         }
     }
 
     protected long findFreeKey(ConcurrentHashMap<Long, Boolean> map){
         for(long rez = 0; rez<Long.MAX_VALUE;rez++) {
             if (!map.containsKey(new Long(rez))) {
                 return rez;
             }
         }
         return OWERFLOW;
     }
 
     protected void addMeToAddressService(){
         this.ms.getAddressService().setSalesMechanics(this.address);
     }
 }
