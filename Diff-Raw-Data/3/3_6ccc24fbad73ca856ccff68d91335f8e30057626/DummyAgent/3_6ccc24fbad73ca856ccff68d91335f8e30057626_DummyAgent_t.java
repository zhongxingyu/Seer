 /**
  * TAC AgentWare
  * http://www.sics.se/tac        tac-dev@sics.se
  *
  * Copyright (c) 2001-2005 SICS AB. All rights reserved.
  *
  * SICS grants you the right to use, modify, and redistribute this
  * software for noncommercial purposes, on the conditions that you:
  * (1) retain the original headers, including the copyright notice and
  * this text, (2) clearly document the difference between any derived
  * software and the original, and (3) acknowledge your use of this
  * software in pertaining publications and reports.  SICS provides
  * this software "as is", without any warranty of any kind.  IN NO
  * EVENT SHALL SICS BE LIABLE FOR ANY DIRECT, SPECIAL OR INDIRECT,
  * PUNITIVE, INCIDENTAL OR CONSEQUENTIAL LOSSES OR DAMAGES ARISING OUT
  * OF THE USE OF THE SOFTWARE.
  *
  * -----------------------------------------------------------------
  *
  * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
  * Created : 23 April, 2002
  * Updated : $Date: 2005/06/07 19:06:16 $
  *	     $Revision: 1.1 $
  * ---------------------------------------------------------
  * DummyAgent is a simplest possible agent for TAC. It uses
  * the TACAgent agent ware to interact with the TAC server.
  *
  * Important methods in TACAgent:
  *
  * Retrieving information about the current Game
  * ---------------------------------------------
  * int getGameID()
  *  - returns the id of current game or -1 if no game is currently plaing
  *
  * getServerTime()
  *  - returns the current server time in milliseconds
  *
  * getGameTime()
  *  - returns the time from start of game in milliseconds
  *
  * getGameTimeLeft()
  *  - returns the time left in the game in milliseconds
  *
  * getGameLength()
  *  - returns the game length in milliseconds
  *
  * int getAuctionNo()
  *  - returns the number of auctions in TAC
  *
  * int getClientPreference(int client, int type)
  *  - returns the clients preference for the specified type
  *   (types are TACAgent.{ARRIVAL, DEPARTURE, HOTEL_VALUE, E1, E2, E3}
  *
  * int getAuctionFor(int category, int type, int day)
  *  - returns the auction-id for the requested resource
  *   (categories are TACAgent.{CAT_FLIGHT, CAT_HOTEL, CAT_ENTERTAINMENT
  *    and types are TACAgent.TYPE_INFLIGHT, TACAgent.TYPE_OUTFLIGHT, etc)
  *
  * int getAuctionCategory(int auction)
  *  - returns the category for this auction (CAT_FLIGHT, CAT_HOTEL,
  *    CAT_ENTERTAINMENT)
  *
  * int getAuctionDay(int auction)
  *  - returns the day for this auction.
  *
  * int getAuctionType(int auction)
  *  - returns the type for this auction (TYPE_INFLIGHT, TYPE_OUTFLIGHT, etc).
  *
  * int getOwn(int auction)
  *  - returns the number of items that the agent own for this
  *    auction
  *
  * Submitting Bids
  * ---------------------------------------------
  * void submitBid(Bid)
  *  - submits a bid to the tac server
  *
  * void replaceBid(OldBid, Bid)
  *  - replaces the old bid (the current active bid) in the tac server
  *
  *   Bids have the following important methods:
  *    - create a bid with new Bid(AuctionID)
  *
  *   void addBidPoint(int quantity, float price)
  *    - adds a bid point in the bid
  *
  * Help methods for remembering what to buy for each auction:
  * ----------------------------------------------------------
  * int getAllocation(int auctionID)
  *   - returns the allocation set for this auction
  * void setAllocation(int auctionID, int quantity)
  *   - set the allocation for this auction
  *
  *
  * Callbacks from the TACAgent (caused via interaction with server)
  *
  * bidUpdated(Bid bid)
  *  - there are TACAgent have received an answer on a bid query/submission
  *   (new information about the bid is available)
  * bidRejected(Bid bid)
  *  - the bid has been rejected (reason is bid.getRejectReason())
  * bidError(Bid bid, int error)
  *  - the bid contained errors (error represent error status - commandStatus)
  *
  * quoteUpdated(Quote quote)
  *  - new information about the quotes on the auction (quote.getAuction())
  *    has arrived
  * quoteUpdated(int category)
  *  - new information about the quotes on all auctions for the auction
  *    category has arrived (quotes for a specific type of auctions are
  *    often requested at once).
 
  * auctionClosed(int auction)
  *  - the auction with id "auction" has closed
  *
  * transaction(Transaction transaction)
  *  - there has been a transaction
  *
  * gameStarted()
  *  - a TAC game has started, and all information about the
  *    game is available (preferences etc).
  *
  * gameStopped()
  *  - the current game has ended
  *
  */
 
 package se.sics.tac.aw;
 import se.sics.tac.util.ArgEnumerator;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.logging.*;
 
 public class DummyAgent extends AgentImpl {
 
 	private static final Logger log =
 		Logger.getLogger(DummyAgent.class.getName());
 
 	private static final boolean DEBUG = false;
 
 	private float[] prices;
 	
 	/**
 	 * ID's for the relevant entertainment auctions format [eType][day]
 	 */
 	private int[][] entAuctionIds;
 	/**
 	 * Ordered array of the entertainment tickets we need in order of the bonus we'll receive for them.
 	 */
 	ArrayList<TicketPriorityEntry> entTicketPriorityList;
 	
 	/**
 	 * Lists each client's availability for entertainment.
 	 */
 	ArrayList<ClientEntertainmentAlloc> clientEntAvail;
 	
 	/**
 	 * Holds all of the tickets put up for sale.
 	 * When the game first starts, this list will also be used to generate the initial bids.
 	 */
 	ArrayList<TicketSale> ticketSales;
 	
 	/**
 	 * Holds the entries of all the tickets we want to buy
 	 */
 	ArrayList<TicketPurchase> ticketPurchases;
 	
 	//These booleans control what testing logs should be displayed
 	/**
 	 * Should the log for the entertainment functions be displayed
 	 */
 	private boolean LOG_ENTERTAINMENT = true;
 
 	protected void init(ArgEnumerator args) {
 		prices = new float[agent.getAuctionNo()];
 	}
 
 	public void quoteUpdated(Quote quote) {
 		int auction = quote.getAuction();
 		int auctionCategory = agent.getAuctionCategory(auction);
 		if (auctionCategory == TACAgent.CAT_HOTEL) {
 			int alloc = agent.getAllocation(auction);
 			if (alloc > 0 && quote.hasHQW(agent.getBid(auction)) &&
 					quote.getHQW() < alloc) {
 				Bid bid = new Bid(auction);
 				// Can not own anything in hotel auctions...
 				prices[auction] = quote.getAskPrice() + 50;
 				bid.addBidPoint(alloc, prices[auction]);
 				if (DEBUG) {
 					log.finest("submitting bid with alloc="
 							+ agent.getAllocation(auction)
 							+ " own=" + agent.getOwn(auction));
 				}
 				agent.submitBid(bid);
 			}
 		} else if (auctionCategory == TACAgent.CAT_ENTERTAINMENT) {
 			/*
 			int alloc = agent.getAllocation(auction) - agent.getOwn(auction);
 			if (alloc != 0) {
 				Bid bid = new Bid(auction);
 				if (alloc < 0)
 					prices[auction] = 200f - (agent.getGameTime() * 120f) / 720000;
 				else
 					prices[auction] = 50f + (agent.getGameTime() * 100f) / 720000;
 				bid.addBidPoint(alloc, prices[auction]);
 				if (DEBUG) {
 					log.finest("submitting bid with alloc="
 							+ agent.getAllocation(auction)
 							+ " own=" + agent.getOwn(auction));
 				}
 				agent.submitBid(bid);
 			}
 			*/
 		}
 	}
 
 	public void quoteUpdated(int auctionCategory) {
 		log.fine("All quotes for "
 				+ agent.auctionCategoryToString(auctionCategory)
 				+ " has been updated");
 	}
 
 	public void bidUpdated(Bid bid) {
 		log.fine("Bid Updated: id=" + bid.getID() + " auction="
 				+ bid.getAuction() + " state="
 				+ bid.getProcessingStateAsString());
 		log.fine("       Hash: " + bid.getBidHash());
 	}
 
 	public void bidRejected(Bid bid) {
 		log.warning("Bid Rejected: " + bid.getID());
 		log.warning("      Reason: " + bid.getRejectReason()
 				+ " (" + bid.getRejectReasonAsString() + ')');
 	}
 
 	public void bidError(Bid bid, int status) {
 		log.warning("Bid Error in auction " + bid.getAuction() + ": " + status
 				+ " (" + agent.commandStatusToString(status) + ')');
 	}
 	
 	public void transaction(Transaction transaction) {
 		int auction = transaction.getAuction();
 		int auctionCategory = agent.getAuctionCategory(auction);
 		if (auctionCategory == TACAgent.CAT_ENTERTAINMENT) {
 			//A negative quantity indicates that we sold a ticket
 			if(transaction.getQuantity()<0){
 				soldTicket(transaction);
 			}else{
 				//TODO purchaseTicket(transaction);
 			}
 			
 			//Now process the updated bid strings to reflect the new purchases/sales
 			processBids();
 		}
 	}
 
 	public void gameStarted() {
 		log.fine("Game " + agent.getGameID() + " started!");
 
 		//Functions dealing with entertainment auctions
 		getEntAuctionIds();			//Create an array containing all of the auction ID's
 		entTicketPriority();		//Create a list of the order entertainment tickets should be allocated in
 		createClientEntArray(); 	//Create a blank array with client details
 		ticketSales = new ArrayList<TicketSale>();	//Creates a blank array for generating ticketSales
 		allocateStartingTickets();	//Allocates the tickets we're assigned and sells the un-needed tickets
 		sellTickets();				//Puts all tickets we've allocated up for sale
 		ticketPurchases = new ArrayList<TicketPurchase>();	//Creates a blank array for generating ticketSales
 		buyTickets();				//Puts bids in for tickets to get additional fun bonuses
 		
 		processBids();				//Generates the initial bid strings as determined by the previous functions
 		
 		//Old Dummy Methods
 		calculateAllocation();
 		sendBids();
 	}
 
 	public void gameStopped() {
 		log.fine("Game Stopped!");
 	}
 
 	public void auctionClosed(int auction) {
 		log.fine("*** Auction " + auction + " closed!");
 	}
 
 	private void sendBids() {
 		for (int i = 0, n = agent.getAuctionNo(); i < n; i++) {
 			int alloc = agent.getAllocation(i) - agent.getOwn(i);
 			float price = -1f;
 			switch (agent.getAuctionCategory(i)) {
 			case TACAgent.CAT_FLIGHT:
 				if (alloc > 0) {
 					price = 1000;
 				}
 				break;
 			case TACAgent.CAT_HOTEL:
 				if (alloc > 0) {
 					price = 200;
 					prices[i] = 200f;
 				}
 				break;
 				
 			case TACAgent.CAT_ENTERTAINMENT:
 				/*
 				if (alloc < 0) {
 					price = 200;
 					prices[i] = 200f;
 				} else if (alloc > 0) {
 					price = 50;
 					prices[i] = 50f;
 				}
 				*/
 				break;
 				
 			default:
 				break;
 			}
 			if (price > 0) {
 				Bid bid = new Bid(i);
 				bid.addBidPoint(alloc, price);
 				if (DEBUG) {
 					log.finest("submitting bid with alloc=" + agent.getAllocation(i)
 							+ " own=" + agent.getOwn(i));
 				}
 				agent.submitBid(bid);
 			}
 		}
 	}
 
 	private void calculateAllocation() {
 		for (int i = 0; i < 8; i++) {
 			int inFlight = agent.getClientPreference(i, TACAgent.ARRIVAL);
 			int outFlight = agent.getClientPreference(i, TACAgent.DEPARTURE);
 			int hotel = agent.getClientPreference(i, TACAgent.HOTEL_VALUE);
 			int type;
 
 			// Get the flight preferences auction and remember that we are
 			// going to buy tickets for these days. (inflight=1, outflight=0)
 			int auction = agent.getAuctionFor(TACAgent.CAT_FLIGHT,
 					TACAgent.TYPE_INFLIGHT, inFlight);
 			agent.setAllocation(auction, agent.getAllocation(auction) + 1);
 			auction = agent.getAuctionFor(TACAgent.CAT_FLIGHT,
 					TACAgent.TYPE_OUTFLIGHT, outFlight);
 			agent.setAllocation(auction, agent.getAllocation(auction) + 1);
 
 			// if the hotel value is greater than 70 we will select the
 			// expensive hotel (type = 1)
 			if (hotel > 70) {
 				type = TACAgent.TYPE_GOOD_HOTEL;
 			} else {
 				type = TACAgent.TYPE_CHEAP_HOTEL;
 			}
 			// allocate a hotel night for each day that the agent stays
 			for (int d = inFlight; d < outFlight; d++) {
 				auction = agent.getAuctionFor(TACAgent.CAT_HOTEL, type, d);
 				log.finer("Adding hotel for day: " + d + " on " + auction);
 				agent.setAllocation(auction, agent.getAllocation(auction) + 1);
 			}
 			
 			
 			//TODO Remove Existing entertainment functions
 			/*
 			int eType = -1;
 			while((eType = nextEntType(i, eType)) > 0) {
 				auction = bestEntDay(inFlight, outFlight, eType);
 				log.finer("Adding entertainment " + eType + " on " + auction);
 				agent.setAllocation(auction, agent.getAllocation(auction) + 1);
 			}
 			*/
 		}
 	}
 	
 	/**
 	 * Gets all of the entertainment auction ID's
 	 */
 	private void getEntAuctionIds(){
 		entAuctionIds = new int[3][4];
 		for (int ent = 1; ent < 4; ent++){
 			for (int day =1; day < 5; day++){
 				entAuctionIds[ent-1][day-1]=agent.getAuctionFor(TACAgent.CAT_ENTERTAINMENT,ent, day);
 			}
 		}
 	}
 	
 	/**
 	 * Create an ordered array of the entertainment tickets we require
 	 */
 	private void entTicketPriority(){
 		ArrayList<TicketPriorityEntry> entPriority = new ArrayList<TicketPriorityEntry>();
 		for (int client=0; client<8; client++){
 			entPriority.add(new TicketPriorityEntry(client, 1,agent.getClientPreference(client, TACAgent.E1),-1));
 			entPriority.add(new TicketPriorityEntry(client, 2,agent.getClientPreference(client, TACAgent.E2),-1));
 			entPriority.add(new TicketPriorityEntry(client, 3,agent.getClientPreference(client, TACAgent.E3),-1));
 		}
 		
 		//Sort the list into the correct priority order
 		Collections.sort(entPriority);
 		Collections.reverse(entPriority);
 		entTicketPriorityList = entPriority;
 		
 		//For Testing, we will print this array
 		if(LOG_ENTERTAINMENT){
 			for (int i=0; i< entPriority.size(); i++){
 				log.finer("Position: " + i + " Client: "+ entPriority.get(i).getClient() + " eType: " + entPriority.get(i).geteType() + " Value: " + entPriority.get(i).getFunBonus());
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 */
 	private void createClientEntArray(){
 		
 		ArrayList<ClientEntertainmentAlloc> clientArray = new ArrayList<ClientEntertainmentAlloc>();
 		for (int client=0; client<8; client++){
 			ClientEntertainmentAlloc clientAllocation = new ClientEntertainmentAlloc(
 					new EntertainmentAllocation(agent.getClientPreference(client, TACAgent.E1),-1), 
 					new EntertainmentAllocation(agent.getClientPreference(client, TACAgent.E2),-1), 
 					new EntertainmentAllocation(agent.getClientPreference(client, TACAgent.E3),-1), 
 					agent.getClientPreference(client, TACAgent.DEPARTURE)-agent.getClientPreference(client, TACAgent.ARRIVAL),
 					client);
 			clientArray.add(clientAllocation);
 		}
 		clientEntAvail = clientArray;
 	}
 	
 	/**
 	 * This function allocates all of our starting entertainment tickets to a client.
 	 * If a ticket cannot be assigned, it gets added to the sale string as we will
 	 * only benefit from it if it is sold.
 	 */
 	private void allocateStartingTickets(){
 		for (int e = 0; e<3; e++){
 			for (int d = 0; d<4; d++){
 				int own = agent.getOwn(agent.getAuctionFor(TACAgent.CAT_ENTERTAINMENT, e+1, d+1));
 				if (LOG_ENTERTAINMENT) {
 					log.finest("We own " + own + "tickets of eType: " +(e+1)
 							+ " on day " + (d+1));
 				}
 				if (own>0){
 					int alloc =0;
 					while(alloc<own){
 						//For our starting tickets, we give them a value of 0
 						if(allocateTicket(d, e+1, 0, true)){
 							log.finest("Just allocated eType: " +(e+1)+ " on day " + (d+1));
 							alloc++;
 						}else{
 							//Any tickets leftover are sold
 							sellLeftoverTickets(e,d,own-alloc);
 							alloc=own+1;
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	
 	private boolean allocateTicket(int day, int eType, int value, boolean process){
 		for (int i=0; i<24; i++){
 			if (LOG_ENTERTAINMENT) {
 				log.finest("priority " + i + " day assigned: " + entTicketPriorityList.get(i).getDayAssigned() + 
 						" eType: "+ entTicketPriorityList.get(i).geteType() + "normal eType:" + eType);
 			}
 			//TODO add statement to process when some clients have already been assigned to
 			if(entTicketPriorityList.get(i).geteType()==eType && entTicketPriorityList.get(i).getDayAssigned()<1){
 				//Check whether the utility gained is greater than the value
 				//This should be the most we'll get, so if utility is less
 				if (value<entTicketPriorityList.get(i).getFunBonus()){
 					//If day during agent's visit
 					if (LOG_ENTERTAINMENT) {
 						log.finest("raw day " + i + " day arrival: " + (agent.getClientPreference(entTicketPriorityList.get(i).getClient(), TACAgent.ARRIVAL)-1) + 
 								" dept day: "+ (agent.getClientPreference(entTicketPriorityList.get(i).getClient(), TACAgent.DEPARTURE)-1));
 					}
 					if(agent.getClientPreference(entTicketPriorityList.get(i).getClient(), TACAgent.ARRIVAL)-1 < day+1 && 
 							day+1 < agent.getClientPreference(entTicketPriorityList.get(i).getClient(), TACAgent.DEPARTURE)){
 						//Check the client still has days left to visit entertainment
 						if(clientEntAvail.get(entTicketPriorityList.get(i).getClient()).getDaysAssigned()<clientEntAvail.get(entTicketPriorityList.get(i).getClient()).getDaysPossible()){
 							//If the entertainment type still hasn't been allocated to the client
 							if(clientEntAvail.get(entTicketPriorityList.get(i).getClient()).getEntertainmentAllocation(eType).getAssignedDay()<0){
 								//If the entertainment day hasn't been allocated to any other entertainment types
 								if(clientEntAvail.get(entTicketPriorityList.get(i).getClient()).dayAvailable(day)){
 									//If this call was for information only, we don't want to update our records
 									if(process){
 										updateRecords(day, eType,entTicketPriorityList.get(i).getClient() ,i);
 										return true;
 									}else{
 										return true;
 									}
 								}else{
 									//TODO handle re-allocating entertainment to get best possible fun bonus - recursive
 									if (LOG_ENTERTAINMENT) {
 										log.finest("assigned to another entertainment");
 									}
 								}
 							}
 						}
 					}
 				}else{
 					return false;
 				}
 			}
 		}
 		return false;
 	}
 	
 	private void updateRecords(int day, int eType, int client, int priorityPosn){
 		//Update the priority list to mark as assigned
 		TicketPriorityEntry tpe = entTicketPriorityList.get(priorityPosn);
 		tpe.setDayAssigned(day);
 		entTicketPriorityList.set(priorityPosn, tpe);
 		
 		//Update the client records
 		ClientEntertainmentAlloc cea = clientEntAvail.get(client);
 		cea.setDaysAssigned(cea.getDaysAssigned()+1);
 		cea.updateEntertainmentAllocation(eType, day);
 		
 	}
 	
 	/**
 	 * For each ticket left over, this creates an entry in the ticketSales array
 	 * to be added to the initial set of bids submitted
 	 * @param eType
 	 * @param day
 	 * @param quantity
 	 */
 	private void sellLeftoverTickets(int eType, int day, int quantity){
 		for(int i=0; i<quantity; i++){
 			TicketSale ticketSale = new TicketSale(entAuctionIds[eType][day],
 					-1,			//a client of -1 indicates surplus
 					eType,
 					100,		//The initial starting price for a surplus ticket is 100	
 					0,			//This ticket strictly has no value
 					SalePurpose.surplus);
 			ticketSales.add(ticketSale);
 		}
 	}
 	
 	/**
 	 * From the list of assigned entertainment tickets, this function generates
 	 * a sale price for them and adds them to the array list of bids to be submitted.<br/>
 	 * The sale price is greater than the fun bonus - this means our agent will always be better
 	 * of by selling a ticket, not worse.
 	 */
 	private void sellTickets(){
 		for (TicketPriorityEntry tpe: entTicketPriorityList){
 			//We want to sell tickets which have been assigned to a user using this method
 			if (tpe.getDayAssigned()>-1){
 				TicketSale ticketSale = new TicketSale(entAuctionIds[tpe.geteType()-1][tpe.getDayAssigned()],
 						tpe.getClient(),
 						tpe.geteType(),
 						tpe.getFunBonus()+50,	//The initial starting selling price is the fun bonus + 50
 						tpe.getFunBonus(),
 						SalePurpose.standard);
 				ticketSales.add(ticketSale);
 			}
 		}
 	}
 	
 	/**
 	 * Goes through all of the possible entertainment slots and tries to buy a tickets if the slot is available
 	 */
 	private void buyTickets(){
 		for(ClientEntertainmentAlloc cea: clientEntAvail){
 			for(int eType=1; eType<4; eType++){
 				if(cea.getEntertainmentAllocation(eType).getAssignedDay()<0){
 					log.finest("client: " +cea.getClient());
 					log.finest("eType: " + eType);
 					//If it isn't assigned, we want to purchase for any available day
 					generateTicketPurchases(cea, eType);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Using the ticketSales and ticketPurchases array list, this function creates
 	 * the initial bids for entertainment at the start of the game
 	 */
 	private void processBids(){
 		for(int e=0; e<3; e++){
 			for (int day=0; day<4; day++){
 				int auctionId = entAuctionIds[e][day];			
 				ArrayList<TicketSale> auctionSales = getSalesByAuction(auctionId);
 				ArrayList<TicketPurchase> auctionPurchases = getPurchasesByAuction(auctionId);
 				if(auctionSales.size()>0 | auctionPurchases.size()>0){
 					Bid bid = new Bid(auctionId);
 					if(auctionSales.size()>0){
 						for(TicketSale ts: auctionSales){	
 							bid.addBidPoint(-1, ts.getSalePrice());
 							
 						}
 					}
 					if(auctionPurchases.size()>0){
 						for(TicketPurchase tp: auctionPurchases){	
 							bid.addBidPoint(1, tp.getSalePrice());
 							
 						}
 					}
 					agent.submitBid(bid);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Returns all of the auctions in the ticketSales array which are for the specified auctionId
 	 * @param auctionId - The auction for which the corresponding sale entries are returned
 	 * @return
 	 */
 	private ArrayList<TicketSale> getSalesByAuction(int auctionId){
 		ArrayList<TicketSale> auctionSale = new ArrayList<TicketSale>();
 		//TODO Is there a better way than iterating through every entry?
 		for(TicketSale ticketSale: ticketSales){
 			if(ticketSale.getAuctionId() == auctionId){
 				auctionSale.add(ticketSale);
 			}
 		}
 		return auctionSale;
 	}
 	
 	/**
 	 * Returns all of the auctions in the ticketPurchases array which are for the specified auctionId
 	 * @param auctionId
 	 * @return
 	 */
 	private ArrayList<TicketPurchase> getPurchasesByAuction(int auctionId){
 		ArrayList<TicketPurchase> auctionPurchase = new ArrayList<TicketPurchase>();
 		//TODO Is there a better way than iterating through every entry?
 		for(TicketPurchase ticketPurchase: ticketPurchases){
 			if(ticketPurchase.getAuctionId() == auctionId){
 				auctionPurchase.add(ticketPurchase);
 			}
 		}
 		return auctionPurchase;
 	}
 	
 	/**
 	 * Handles whenever we sell a ticket
 	 * @param transacion
 	 */
 	private void soldTicket(Transaction transaction){
 		int auctionId = transaction.getAuction();
 		
 		//This assumes that we always sell the cheapest entry first and sell for more than the defined sale price
 		int i=0;
 		TicketSale soldItem = null;
 		int soldPos=-1;
 		while(i<ticketSales.size()){
 			if(ticketSales.get(i).getAuctionId()==auctionId && ticketSales.get(i).getSalePrice()<=transaction.getPrice()){
 				if(soldItem!=null){
 					if(soldItem.getSalePrice()>ticketSales.get(i).getSalePrice()){
 						soldItem = ticketSales.get(i);
 						soldPos=i;
 					}
 				}else{
 					soldItem = ticketSales.get(i);
 					soldPos=i;
 				}
 			}
 			i++;
 		}
 		
 		//We now know the item we sold so can update our records accordingly
 		if(soldItem!=null){
 			if(soldItem.getSalePurpose()!=SalePurpose.surplus){
 				//Updates the Client Entertainment Allocation List
 				ClientEntertainmentAlloc cea = clientEntAvail.get(soldItem.getClientId());
 				cea.updateEntertainmentAllocation(soldItem.geteType(), -1);	//Set day to -1 as no longer allocated
 				clientEntAvail.set(soldItem.getClientId(), cea);
 				
 				//Updates the priority list
 				int j=0;
 				while(j<entTicketPriorityList.size()){
 					if(entTicketPriorityList.get(j).getClient()==soldItem.getClientId() && entTicketPriorityList.get(j).geteType()==soldItem.geteType()){
 						TicketPriorityEntry tpe = entTicketPriorityList.get(j);
 						tpe.setDayAssigned(-1);
 						entTicketPriorityList.set(j, tpe);
 						
 						j=entTicketPriorityList.size();
 					}
 					j++;
 				}
 				
 				//Now we've sold the ticket, we can attempt to buy it again - for less!
 				generateTicketPurchases(clientEntAvail.get(soldItem.getClientId()), soldItem.geteType());
 			}
 			//Delete the entry in the sales list - this one is no longer for sale!
 			if(soldPos!=-1){
 				ticketSales.remove(soldPos);
 			}
 		}
 	}
 	
 	/**
 	 * Generates all of the possible ticket purchases for a defined client and entertainment type
 	 * @param cea - The client, contained within a ClientEntertainmentAlloc Item
 	 * @param eType - The entertainment type to buy tickets for
 	 */
 	private void generateTicketPurchases(ClientEntertainmentAlloc cea, int eType){
 		for(int day = agent.getClientPreference(cea.getClient(), TACAgent.ARRIVAL)-1; 
 		day< agent.getClientPreference(cea.getClient(), TACAgent.DEPARTURE-1); day++){
 		if(cea.dayAvailable(day)){
 			//We want to generate a buy price which is likely, but also worth the profit.
 			int buyPrice;
 			if(cea.getEntertainmentAllocation(eType).getFunBonus()<20){
 				buyPrice = cea.getEntertainmentAllocation(eType).getFunBonus()-2;
 			}else if(cea.getEntertainmentAllocation(eType).getFunBonus()<30){
 				buyPrice = cea.getEntertainmentAllocation(eType).getFunBonus()-10;
 			}else{
 				buyPrice = cea.getEntertainmentAllocation(eType).getFunBonus()-20;
 			}
			if(buyPrice<0){
				buyPrice=0;
			}
 			if (LOG_ENTERTAINMENT) {
 				log.finest("client: " +cea.getClient() + ", day: " +day +  ", eType: " +eType + ", buyPrice: " +buyPrice + ", funBonus: " +cea.getEntertainmentAllocation(eType).getFunBonus());
 			}
 			TicketPurchase ticketPurchase = new TicketPurchase(entAuctionIds[eType-1][day],
 					cea.getClient(),
 					eType,
 					buyPrice,	
 					cea.getEntertainmentAllocation(eType).getFunBonus());
 			ticketPurchases.add(ticketPurchase);
 		}
 	}
 	}
 	
 	private int bestEntDay(int inFlight, int outFlight, int type) {
 		for (int i = inFlight; i < outFlight; i++) {
 			int auction = agent.getAuctionFor(TACAgent.CAT_ENTERTAINMENT,
 					type, i);
 			if (agent.getAllocation(auction) < agent.getOwn(auction)) {
 				return auction;
 			}
 		}
 		// If no left, just take the first...
 		return agent.getAuctionFor(TACAgent.CAT_ENTERTAINMENT,
 				type, inFlight);
 	}
 
 	private int nextEntType(int client, int lastType) {
 		int e1 = agent.getClientPreference(client, TACAgent.E1);
 		int e2 = agent.getClientPreference(client, TACAgent.E2);
 		int e3 = agent.getClientPreference(client, TACAgent.E3);
 
 		// At least buy what each agent wants the most!!!
 		if ((e1 > e2) && (e1 > e3) && lastType == -1)
 			return TACAgent.TYPE_ALLIGATOR_WRESTLING;
 		if ((e2 > e1) && (e2 > e3) && lastType == -1)
 			return TACAgent.TYPE_AMUSEMENT;
 		if ((e3 > e1) && (e3 > e2) && lastType == -1)
 			return TACAgent.TYPE_MUSEUM;
 		return -1;
 	}
 
 
 
 	// -------------------------------------------------------------------
 	// Only for backward compability
 	// -------------------------------------------------------------------
 
 	public static void main (String[] args) {
 		TACAgent.main(args);
 	}
 
 } // DummyAgent
