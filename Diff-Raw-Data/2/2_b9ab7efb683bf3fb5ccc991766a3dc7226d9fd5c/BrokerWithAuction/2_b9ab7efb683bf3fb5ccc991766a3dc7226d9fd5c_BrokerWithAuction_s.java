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
 import java.util.Map.Entry;
 import sim.courier.Courier;
 import sim.courierworld.CourierWorld;
 import sim.courierworld.Hub;
 import sim.courierworld.Node;
 import sim.courierworld.Warehouse;
 
 /**
  * This broker ensures that the packages are delivered by using a Dutch auction
  *
  * @author drew
  */
 public class BrokerWithAuction extends Broker {
 
     public double explorationFactor = 0.5;
     public double explorationFactor0 = 0.5;
     public double interactionRate = 0.2;
     public int prevState = 0;
     public int numActions = 5;
     public double flowFactor = 0.1;
     public double gamma = 0.7;
     public int currentDecayedPacks = 0;
     public int avgPacksPerStep = 0;
     public int currentPacksPerStep = 0;
     public int currentAuctionedPacksPerStep = 0;
     public ArrayList<ArrayList<Double>> Q1 = new ArrayList<>();
     public ArrayList<ArrayList<Double>> Q2 = new ArrayList<>();
     public ArrayList<Double> PI1 = new ArrayList<>();
     public ArrayList<Double> PI2 = new ArrayList<>();
     public int numStates = 5;
     public double learningRate = 0.2;
     public double minBidRate = 0.1;
     public double maxBidRate = 2.0;
     public double minServiceRate = 0.1;
     public double maxServiceRate = 2.0;
     public int state;
 
     /**
      *
      * @param myHub
      */
     public BrokerWithAuction(Hub myHub) {
         super(myHub);
 
         for (int i = 0; i < numStates; i++) {
             Q1.add(new ArrayList<Double>());
             Q2.add(new ArrayList<Double>());
             PI1.add(maxServiceRate);
             PI2.add(minBidRate);
             for (int j = 0; j < numActions; j++) {
                 Q1.get(i).add(0.0);
                 Q2.get(i).add(0.0);
             }
         }
         state = 1;
     }
 
     @Override
     public String toString() {
        return "WithAuction"; //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public double getQuote(Warehouse myPackages) {
         quote = myPackages.getTotalNumPacks() * serviceRate;
         //System.err.println("+q" + quote);
         return quote;
     }
 
     public void updateRates() {
         serviceRate = PI1.get(getState());
         bidRate = PI2.get(getState());
         if (Math.random() < explorationFactor) {
             serviceRate = Math.max(maxServiceRate, Math.min(minServiceRate, PI1.get((int) (Math.floor(Math.random() * numStates)))));
             bidRate = Math.max(minBidRate, Math.min(maxBidRate, PI2.get((int) (Math.floor(Math.random() * numStates)))));
         }
         explorationFactor = explorationFactor0 * Math.exp(-Math.log(myHub.state.schedule.getSteps()) / 200.0);
 
         //System.err.println("+b " + bidRate + "," + "+s " + serviceRate + " " + explorationFactor);
     }
 
     @Override
     public void performAuctions(List<Courier> courierList, CourierWorld world, Node hubNode) {
         currentAuctionedPacksPerStep = 0;
         if (world.schedule.getSteps() == 0) {
             avgPacksPerStep = 0;
         } else {
             avgPacksPerStep = (int) ((succPakcages.getTotalNumPacks() + lostPackages.getTotalNumPacks() + myPackages.getTotalNumPacks()) / world.schedule.getSteps());
         }
 
         //do this for each package <dest, priority>
         Iterator<Map.Entry<Warehouse.Key, Integer>> iter = getMyPackages().getIterator();
         HashMap<Courier, Warehouse> bids = new HashMap<>();
 
         double currentBidRate = PI2.get(getState());
         if (world.random.nextDouble() < explorationFactor) {
             currentBidRate = PI2.get(world.random.nextInt(numStates));
         }
         while (iter.hasNext()) {
             Map.Entry<Warehouse.Key, Integer> stacks = iter.next();
             //all couriers have bid
             Collections.shuffle(courierList); // first come first serve
             for (Courier c : courierList) {
                 bids.put(c, new Warehouse());
                 // bid is the number of packs that the courier is willing to take
                 int bid = c.getSomeStacks(stacks, currentBidRate, hubNode, world);
 
                 if (bid > 0) {
 
                     bids.get(c).updateStack(stacks.getKey(), bid);
                     stacks.setValue(stacks.getValue() - bid);
                     currentAuctionedPacksPerStep += bid;
                 }
 
                 if (stacks.getValue() == 0) {
                     break;
                 }
             }
 
         }
         //decide on how to allocate 
 
         for (Entry<Courier, Warehouse> en : bids.entrySet()) {
             en.getKey().myPackages.addAll(en.getValue());
             succPakcages.addAll(en.getValue());
 
             profit -= (double) (bidRate * en.getValue().getTotalNumPacks());
             en.getKey().profit += (double) (bidRate * en.getValue().getTotalNumPacks());
 
         }
 
         currentPacksPerStep = 0;//should be reset to 0 after each round of auction
         updatePolicy(world);
         updateRates();
         //System.err.println("+p" + profit);
     }
 
     @Override
     public void addPackage(Warehouse myPackages, double fee) {
         super.addPackage(myPackages, fee);
         currentPacksPerStep += myPackages.getTotalNumPacks();
     }
 
     public int getState() {
         double sum = 0.0;
         for (Broker b : myHub.brokers) {
             sum += (Math.max(0, b.profit));
         }
         //return (int) Math.floor((defaultRate) * numStates);
         if (sum > 0.0) {
             return (int) Math.floor((Math.max(0, profit) / sum) * (numStates - 1));
         } else if (sum < 0.0) {
             sum = 0.0;
             for (Broker b : myHub.brokers) {
                 sum += b.profit;
             }
             return (int) Math.floor(((sum - profit) / sum) * (numStates - 1));
         } else {
             return numStates / 2;
         }
     }
 
     public void updatePolicy(CourierWorld world) {
         //System.err.println("currentSate : " + getState());
         if (world.schedule.getSteps() > 1) {
 
             //compute the joint reward based on both the rates
             double reward1 = 0, reward2 = 0;
 
             if (currentAuctionedPacksPerStep < currentPacksPerStep) {
                 reward1 = -1.0 + currentAuctionedPacksPerStep / (currentPacksPerStep + 1.0);
             } else {
                 reward1 = -1.0 + currentPacksPerStep / (1.0 + currentAuctionedPacksPerStep);
             }
 
             reward2 = 1.0 - currentDecayedPacks / (1.0 + currentAuctionedPacksPerStep);
             double rewardc1 = (1.0 - interactionRate) * reward1 + interactionRate * reward2;
             double rewardc2 = (1.0 - interactionRate) * reward2 + interactionRate * reward1;
             reward1 = rewardc1 * (1.5 - getState() / (double) numStates);
             reward2 = rewardc2 * (1.5 - getState() / (double) numStates);;
 
             int currentActionServiceRate = (int) Math.floor((numActions - 1) * (serviceRate - minServiceRate) / (maxServiceRate - minServiceRate));
             int currentActionBidRate = (int) Math.floor((numActions - 1) * (bidRate - minBidRate) / (maxBidRate - minBidRate));
 
             //System.err.println(serviceRate +" "+ (serviceRate - minServiceRate)/(maxServiceRate - minServiceRate) + " " + numActions * (serviceRate - minServiceRate) / (maxServiceRate - minServiceRate));
             Q1.get(prevState).set(currentActionServiceRate, learningRate * Q1.get(prevState).get(currentActionServiceRate) + (1 - learningRate) * (reward1 + gamma * Q1.get(state).get(currentActionServiceRate)));
             Q2.get(prevState).set(currentActionBidRate, learningRate * Q2.get(prevState).get(currentActionBidRate) + (1 - learningRate) * (reward2 + gamma * Q2.get(state).get(currentActionBidRate)));
 
             //update policy
             int bidActionIdx = 0;
             int serviceActionIdx = 0;
             double max_q1 = Q1.get(0).get(0);
             double max_q2 = Q2.get(0).get(0);
 
             //choose the best based on pareto optimality
             for (int i = 0; i < numActions; i++) {
                 if (max_q1 < Q1.get(prevState).get(i)) {
                     max_q1 = Q1.get(prevState).get(i);
                     serviceActionIdx = i;
                 }
                 if (max_q2 < Q2.get(prevState).get(i)) {
                     max_q2 = Q2.get(prevState).get(i);
                     bidActionIdx = i;
                 }
 
             }
 
             //update the bid and service rates for the previous state
             PI1.set(prevState, Math.max(minServiceRate, Math.min(maxServiceRate, (maxServiceRate - minServiceRate) * serviceActionIdx / numActions)));
             PI2.set(prevState, Math.max(minBidRate, Math.min(maxBidRate, (maxBidRate - minBidRate) * bidActionIdx / numActions)));
 
             prevState = state;
 
             state = getState();
             //System.err.println("+d" +  defaultRate);
         }
     }
 
     @Override
     public void decayPackages() {
         int prelostPacks = lostPackages.getTotalNumPacks();
         super.decayPackages(); //To change body of generated methods, choose Tools | Templates.
         currentDecayedPacks = lostPackages.getTotalNumPacks() - prelostPacks;
     }
 }
