 package edu.buet.cse.eia.ch02.v2.persistence;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.TreeMap;
 
 import edu.buet.cse.eia.ch02.v2.model.Bid;
 
 /**
  * A dummy implementation of BidManager
  * @author shamim
  *
  */
 public class BidManager {
   private static final TreeMap<Long, Bid> bidMap = new TreeMap<>();
   private static long counter = 0L;
   
   public synchronized static Long addBid(Bid bid) { 
     bid.setId(++counter);
     bidMap.put(bid.getId(), bid);
     
     return bid.getId();
   }
   
   public synchronized static Bid getBid(Long id) {
     return bidMap.get(id);
   }
   
   public synchronized static List<Bid> getBids() {
     return new ArrayList<>(bidMap.values());
   }
   
   private BidManager() {
   }
 }
