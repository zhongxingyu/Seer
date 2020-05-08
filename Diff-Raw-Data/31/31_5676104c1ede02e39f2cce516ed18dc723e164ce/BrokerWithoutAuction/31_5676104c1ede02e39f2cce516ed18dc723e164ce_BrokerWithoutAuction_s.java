 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package sim.broker;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import sim.courier.Courier;
 import sim.courierworld.CourierWorld;
 import sim.courierworld.Warehouse;
 
 /**
  * This broker doesn't care about its client and getting the package delivered
  * it wants to make as much from the auction as possible. So, it uses an English
  * auction.
  *
  * @author drew
  */
 public class BrokerWithoutAuction extends Broker {
     public double baseRate = 1.0;//selling without profit
     public double  minBaseRate = 0.5;
     public double  maxBaseRate = 10.0;
     public double  profitFactor = 1.5;
     
     @Override
     public double getQuote(Warehouse myPackages)
     {
         return bidRate*profitFactor;
     }
 
     
 
     @Override
     public void performAuctions(List<Courier> courierList, CourierWorld world) {
         //get bid rate for current step
         bidRate = upDateBidRate();
         //do this for each package <dest, priority>
         Iterator<Map.Entry<Warehouse.Key, Integer>> iter = getMyPackages().getIterator();
         HashMap<Courier, Warehouse> bids = new HashMap<>();
 
         while (iter.hasNext()) {
             Map.Entry<Warehouse.Key, Integer> stacks = iter.next();
             //all couriers have bid
             Collections.shuffle(courierList); // first come first serve
             for (Courier c : courierList) {
                 bids.put(c, new Warehouse());
                 // bid is the number of packs that the courier is willing to take
                 int bid = c.getSomeStacks(stacks, bidRate); 
                 if (bid > 0) {
                     bids.get(c).updateStack(stacks.getKey(), bid);
                     stacks.setValue(stacks.getValue() - bid);
                 }
                 
                 if(stacks.getValue() == 0)
                 {
                     break;
                 }
             }
 
         }
         //decide on how to allocate 
        
        for (Courier c : courierList)
         {
            c.myPackages.addAll(bids.get(c));
            succPakcages.addAll(bids.get(c));
            
            profit -= (int)(bidRate * bids.get(c).getTotalNumPacks());
            
         }
 
     }
 
     private double upDateBidRate()
     {
        return  Math.random()*(maxBaseRate - minBaseRate) + minBaseRate;
     }
 
 }
