 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package sim.broker;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import sim.courier.Courier;
 import sim.courierworld.CourierWorld;
 import sim.courierworld.Warehouse;
 
 /**
  * This broker ensures that the packages are delivered by using a Dutch auction
  *
  * @author drew
  */
 public class BrokerWithAuction extends Broker
 {
 
     public double learnedBidRate = 1.0;
     public double auctionBaseRate = 0.0;
     public int avgPacksPerStep = 0;
     public int currentPacksPerStep = 0;
     public ArrayList<Double> Q1 = new ArrayList<Double>();
     public ArrayList<Double> Q2 = new ArrayList<Double>();
     public int numStates = 5;
     public double learningRate = 0.5;
 
     public BrokerWithAuction()
     {
         for (int i = 0; i < numStates; i++)
         {
             Q1.add(0.0);
             Q2.add(0.0);
         }
     }
 
     @Override
     public double getQuote(Warehouse myPackages)
     {
         return learnedBidRate;
     }
 
     @Override
     public void performAuctions(List<Courier> courierList, CourierWorld world)
     {
        avgPacksPerStep = (int) ((succPakcages.getTotalNumPacks() + lostPackages.getTotalNumPacks() + myPackages.getTotalNumPacks()) / world.schedule.getSteps());
         //do this for each package <dest, priority>
         Iterator<Map.Entry<Warehouse.Key, Integer>> iter = getMyPackages().getIterator();
         HashMap<Courier, Warehouse> bids = new HashMap<>();
 
         while (iter.hasNext())
         {
             Map.Entry<Warehouse.Key, Integer> stacks = iter.next();
             //all couriers have bid
             Collections.shuffle(courierList); // first come first serve
             for (Courier c : courierList)
             {
                 bids.put(c, new Warehouse());
                 // bid is the number of packs that the courier is willing to take
                 int bid = c.getSomeStacks(stacks, bidRate);
                 if (bid > 0)
                 {
                     bids.get(c).updateStack(stacks.getKey(), bid);
                     stacks.setValue(stacks.getValue() - bid);
                 }
 
                 if (stacks.getValue() == 0)
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
 
             profit -= (int) (bidRate * bids.get(c).getTotalNumPacks());
 
         }
 
         currentPacksPerStep = 0;//should be reset to 0 after each round of auction
     }
 
     @Override
     public void addPackage(Warehouse myPackages, double fee)
     {
         super.addPackage(myPackages, fee);
         currentPacksPerStep += myPackages.getTotalNumPacks();
     }
 
     public void updatePolicy(CourierWorld world)
     {
         if (world.schedule.getSteps() > 1)
         {
 
             int state = (int) Math.floor(defaultRate * numStates);
            double reward = 
             Q1.set(state, learningRate * Q1.get(state) + (1.0 - learningRate));
         }
 
 
     }
 }
