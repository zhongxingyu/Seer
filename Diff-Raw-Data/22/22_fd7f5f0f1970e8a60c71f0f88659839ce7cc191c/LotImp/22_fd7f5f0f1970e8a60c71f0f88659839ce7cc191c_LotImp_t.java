 package Global.mechanics;
 
 import Global.Lot;
 import Global.LotHistoryObject;
 import Global.Ticket;
 import Global.User;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Author: artemlobachev
  * Date: 14.12.13
  */
 public class LotImp extends TicketImp implements Lot {
     private Date closeDate;
     private List<LotHistoryObject> history;
 
     public LotImp(Ticket ticket, Date closeDate, int startPrice){
         super(ticket.getOwner(),ticket.getRoute(),ticket.isTemporary());
         this.closeDate = closeDate;
         this.history = new ArrayList<>();
         this.history.add(new LotHistoryObjectImp(new Date(System.currentTimeMillis()), LotHistoryObject.Type.OPEN,new UserImp("Valera"),String.valueOf(startPrice)));
     }
 
     @Override
     public boolean risePrice(User user,Date date, int newPrice) {
         this.history.add(new LotHistoryObjectImp(date, LotHistoryObject.Type.RISE_PRICE,user,String.valueOf(newPrice)));
         return true;
     }
 
     @Override
     public int getCurrentPrice() {
         int rez = Integer.parseInt(this.history.get(0).getArg());
         for(int i = 1; i<this.history.size();i++){
             if (this.history.get(i).getType() == LotHistoryObject.Type.RISE_PRICE){
                 rez = Integer.parseInt(this.history.get(i).getArg());
             }
         }
         return rez;
     }
 
     @Override
     public Date getCloseDate() {
         return this.closeDate;
     }
 
     @Override
     public List<LotHistoryObject> getHistory() {
         return new ArrayList<>(history);
     }
 
     @Override
     public List<LotHistoryObject> getHistoryDESC(int maxItems) {
         ArrayList<LotHistoryObject> rez = new ArrayList<>(maxItems);
         int currentSize = 0;
         while (currentSize<maxItems && currentSize<this.history.size()){
             rez.add(this.history.get(this.history.size() - 1 - currentSize));
            currentSize++;
         }
         return rez;
     }
 }
