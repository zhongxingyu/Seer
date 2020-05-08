 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 
 
 public class Market {
 
 	public Commodity[] commodities;
 	public Agent[] agents;
 	public BufferedWriter bwMarket;
 	public BufferedWriter bwCommodities;
 	private HashMap<Commodity, ArrayList<Bid>> buyBids;
 	private HashMap<Commodity, ArrayList<Bid>> sellBids;
     private HashMap<Commodity, ArrayList<Bid>> shuffledSellBids;
     private HashMap<Commodity, ArrayList<Bid>> shuffledBuyBids;
 
 	public Market()
 	{
 	}
 
     /**
      * Goes through one round.
      * Clears the bids, updates the agents, and completes transactions.
      */
 	public void update()
 	{
 		emptyBidLists();
 
         requestAgentBids();
 
 		shuffleBids();
 
         matchAndExecuteTrades();
         
 		printMarketInformation();
 	}
 
     private void emptyBidLists(){
         buyBids = new HashMap<Commodity, ArrayList<Bid>>();
         sellBids = new HashMap<Commodity, ArrayList<Bid>>();
         shuffledBuyBids = new HashMap<Commodity, ArrayList<Bid>>();
         shuffledSellBids = new HashMap<Commodity, ArrayList<Bid>>();
         for (Commodity commodity : commodities){
             buyBids.put(commodity, new ArrayList<Bid>());
             sellBids.put(commodity, new ArrayList<Bid>());
             shuffledBuyBids.put(commodity, new ArrayList<Bid>());
             shuffledSellBids.put(commodity, new ArrayList<Bid>());
         }
     }
 
     /**
      * Tell the agents to update themselves and submit their bids
      */
     private void requestAgentBids(){
     	for (Commodity commodity : commodities){
     		commodity.consumption = 0;
     	}
         for (Agent agent : agents) {
             agent.update();
         }
     }
 
     /**
      *  Prints out the current simulation information TODO: turn datapoints into csv / excell file to analyze
      */
     public void printMarketInformation(){
         System.out.println("Trading cycle complete");
         System.out.println("______________________");
         System.out.println("Current market prices:");
 
         String marketcsvline = "";
         String entitiescsvline = "";
         for (Commodity commodity : commodities){
             System.out.println(commodity.name + ": " + commodity.marketprice);
             marketcsvline = marketcsvline + commodity.marketprice + ",";
             entitiescsvline = entitiescsvline + commodity.consumption + ",";
         }
         double budgetvar = budgetVariance();
         entitiescsvline = entitiescsvline + budgetvar;
         System.out.println("Current budgets:");
         for (Agent agent : agents) {
             System.out.println(agent.name + ": " + agent.budget);
         }
         System.out.println("______________________");
         
         try {
 			bwMarket.write(marketcsvline.substring(0, marketcsvline.length()-1));
 			bwCommodities.write(entitiescsvline);
 			bwMarket.newLine();
 			bwMarket.flush();
 			bwCommodities.newLine();
 			bwCommodities.flush();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
     }
     
     public double budgetVariance()
     {
     	double avg = 0;
     	for(Agent agent: agents)
     	{
     		avg = avg + agent.budget;
     	}
     	avg = avg / agents.length;
     	double variance = 0;
     	for(Agent agent: agents)
     	{
     		variance = variance + Math.pow(avg - agent.budget, 2);
     	}
     	variance = variance / agents.length;
     	return variance;
     }
 
     /**
      * Accept a bid submitted by a user
      * @param bid
      */
 	public void acceptBid(Bid bid)
 	{
 		Commodity bidCommodity = bid.commodity;
         switch(bid.type){
             case BUY:
                 ArrayList<Bid> buyBidList = buyBids.get(bidCommodity);
                 buyBidList.add(bid);
                 buyBids.put(bidCommodity, buyBidList);
                 break;
             case SELL:
                 ArrayList<Bid> sellBidList = sellBids.get(bidCommodity);
                 sellBidList.add(bid);
                 sellBids.put(bidCommodity, sellBidList);
                 break;
         }
 	}
 
     /**
      * Shuffles the submitted bids and processes transactions for matches.
      */
 	private void shuffleBids()
 	{
 		/*
 		Shuffle sell bids and buy bids
 		 */
         for (Commodity commodity : commodities){
             ArrayList<Bid> listOfBuyBidsToShuffle = buyBids.get(commodity);
             Collections.shuffle(listOfBuyBidsToShuffle);
             shuffledBuyBids.put(commodity,listOfBuyBidsToShuffle);
             
             ArrayList<Bid> listOfSellBidsToShuffle = sellBids.get(commodity);
             Collections.shuffle(listOfSellBidsToShuffle);
             shuffledSellBids.put(commodity, listOfSellBidsToShuffle);
         }
 
 	}
 
     /**
      * Calculates the current market price. TODO: Configurable
      */
     private void matchAndExecuteTrades(){
         for (Commodity commodity : commodities){
         {
             double newMarketPrice = 0;
             int quantitySold = 0;
             ArrayList<Bid> buyList = shuffledBuyBids.get(commodity);
             ArrayList<Bid> sellList = shuffledSellBids.get(commodity);
             for (Bid buyBid : buyList) {
                 for (Bid sellBid : sellList) {
                     if (sellBid.quantity != 0 && buyBid.price >= sellBid.price) {
                         int maxBuyAmount = (int) Math.ceil((Math.log(sellBid.price * 1.0 / buyBid.price) / Math.log(1 / buyBid.marginalscalefactor))) + 1;
                         int maxBuyAmount2 = (int) (buyBid.spendingcap / sellBid.price);
                        maxBuyAmount = Math.min(maxBuyAmount, maxBuyAmount2);
                        int buyAmount = 0;
                        if (maxBuyAmount >= sellBid.quantity) {
                            buyAmount = buyBid.quantity;
                        } else {
                            buyAmount = maxBuyAmount;
                        }
                         if (buyAmount > 0) {
                             if (buyAmount >= sellBid.quantity) {
                                 transaction(buyBid.agent, sellBid.agent, sellBid.commodity, sellBid.quantity, sellBid.quantity * sellBid.price);
                                 buyBid.spendingcap = buyBid.spendingcap - sellBid.quantity * sellBid.price;
                                 buyBid.price = buyBid.price * Math.pow((1 / buyBid.marginalscalefactor), sellBid.quantity);
                                 quantitySold = quantitySold + sellBid.quantity;
                                 newMarketPrice = newMarketPrice + sellBid.quantity * sellBid.price;
                                 buyBid.quantity = buyBid.quantity - sellBid.quantity;
                                 sellBid.quantity = 0;
                                if (buyBid.quantity == 0) {
                                     break;
                                 }
                             } else {
                                 transaction(buyBid.agent, sellBid.agent, sellBid.commodity, buyAmount, buyAmount * sellBid.price);
                                 buyBid.spendingcap = buyBid.spendingcap - buyAmount * sellBid.price;
                                 buyBid.price = buyBid.price * Math.pow((1 / buyBid.marginalscalefactor), buyAmount);
                                 quantitySold = quantitySold + buyAmount;
                                 newMarketPrice = newMarketPrice + buyAmount * sellBid.price;
                                 sellBid.quantity = sellBid.quantity - buyAmount;
                                 buyBid.quantity = buyBid.quantity - buyAmount;
                                if (buyBid.quantity == 0) {
                                     break;
                                 }
                             }
                         }
                     }
                 }
             }
             if (quantitySold > 0)
             {
                 newMarketPrice = newMarketPrice / quantitySold;
             }
             else
             {
                 double highestbuy = 0;
                 double lowestsell = Double.MAX_VALUE;
                 for (Bid buybid : buyList) {
                     if (buybid.price > highestbuy) {
                         highestbuy = buybid.price;
                     }
                 }
                 for (Bid sellBid : sellList) {
                     if (sellBid.price < lowestsell) {
                         lowestsell = sellBid.price;
                     }
                 }
                 if (highestbuy > 0)
                 {
                     if(lowestsell < Double.MAX_VALUE)
                         newMarketPrice = (highestbuy + lowestsell) / 2;
                     else
                         newMarketPrice = highestbuy;
                 }
                 else
                 {
                     if(lowestsell < Double.MAX_VALUE)
                         newMarketPrice = lowestsell;
                     else
                         newMarketPrice = -1;
                 }
 
             }
             commodity.marketprice = newMarketPrice;
         }
         }
     }
 
     /**
      * Services a transaction between a buyer and a seller
      * @param buyer
      * @param seller
      * @param commodity
      * @param quantity
      * @param price
      */
 	private void transaction(Agent buyer, Agent seller, Commodity commodity, int quantity, double price)
 	{
 		buyer.budget = buyer.budget - price;
 		seller.budget = seller.budget + price;
 		buyer.inventory.put(commodity, buyer.inventory.get(commodity) + quantity);
 		seller.inventory.put(commodity, seller.inventory.get(commodity) - quantity);
 		System.out.println(buyer.name + " bought " + quantity + " units of " + commodity.name + " from " + seller.name + " at " + price/quantity + " galactic intracredits each");
 	}
 	
 	
 }
